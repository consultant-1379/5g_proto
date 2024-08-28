/**
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 27, 2022
 *     Author: echfari
 */
package com.ericsson.sc.tapcol.recorder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Empty;
import reactor.core.scheduler.Schedulers;

/**
 * Writes PVTB GRE packets to pcapng file
 *
 */
public class GrePacketWriter
{
    private static final Logger log = LoggerFactory.getLogger(GrePacketWriter.class);
    private final Mono<FileOutputStream> createFileMono;
    private final Mono<Void> mainChain;
    private final Mono<Void> closeFileMono;
    private final String filename;
    private final AtomicBoolean sync = new AtomicBoolean();
    private final AtomicLong dataPacketsWritten = new AtomicLong();
    private final AtomicLong pcapNgHeadersWritten = new AtomicLong();
    private final AtomicLong bytesWritten = new AtomicLong();
    private final Empty<Void> stopped = Sinks.<Void>empty();
    private final byte[] pcapgHeader;

    /**
     * 
     * @param packets      GRE packet stream to consume
     * @param filename     Output file
     * @param pcapngHeader Initial pcapng headers. Might be null
     */
    public GrePacketWriter(Flux<GrePacket> packets,
                           String filename,
                           byte[] pcapngHeader)
    {
        Objects.requireNonNull(packets);
        Objects.requireNonNull(filename);

        this.filename = filename;
        this.pcapgHeader = pcapngHeader;
        final var packetFlux = packets //
                                      .doOnSubscribe(s -> log.info("Packet Writer started processing GRE stream"))
                                      .takeUntilOther(stopped.asMono())
                                      .bufferTimeout(1000, Duration.ofSeconds(1))
                                      .doOnDiscard(GrePacket.class, gre ->
                                      {
                                          log.info("Discarding GRE packet"); // TODO set level to debug
                                          if (gre.getPayload().refCnt() != 0)
                                              gre.getPayload().release();
                                      })
                                      .doOnNext(grePackets -> grePackets.sort((x,
                                                                               y) -> Integer.compare(x.getSequenceNumber(), y.getSequenceNumber()))); // sort
                                                                                                                                                      // packets
                                                                                                                                                      // in
                                                                                                                                                      // buffer
                                                                                                                                                      // according
                                                                                                                                                      // to GRE
                                                                                                                                                      // sequence
                                                                                                                                                      // number
        createFileMono = createFile(filename).cache();

        this.closeFileMono = createFileMono //
                                           .publishOn(Schedulers.boundedElastic())
                                           .doOnNext(f ->
                                           {
                                               try
                                               {
                                                   log.info("Closing file {}", this.filename);
                                                   f.close();
                                               }
                                               catch (Exception e)
                                               {
                                                   log.warn("Failed to close file {}", this.filename, e);
                                               }
                                           })
                                           .then()
                                           .subscribeOn(Schedulers.boundedElastic()) //
                                           .cache();

        this.mainChain = createFileMono.map(FileOutputStream::getChannel)
                                       .flatMap(channel -> packetFlux.concatMap(gres -> writeAndRelease(gres, channel)).ignoreElements())
                                       .doOnError(err -> log.error("Packet writer stopped unexpectedly", err))
                                       .then(closeFileMono)
                                       .doFinally(ev -> closeFileMono.subscribe())
                                       .cache();
    }

    /**
     * Start writing packets to pcapng file. This method returns immediately
     */
    public void start()
    {
        this.mainChain.subscribe();
    }

    /**
     * 
     * @return A Mono that completes as soon as the packet writer terminates
     */
    public Mono<Void> onFinished()
    {
        return this.mainChain;
    }

    /**
     * 
     * @return The number of data packets written to pcapng file
     */
    public long getDataPacketWritten()
    {
        return this.dataPacketsWritten.get();
    }

    /**
     * 
     * @return The number of pcapng headers written to pcapng file. Initial headers
     *         are not included.
     */
    public long getpcapNgHeadersWritten()
    {
        return this.pcapNgHeadersWritten.get();
    }

    /**
     * 
     * @return The total number of bytes written to pcapng file
     */
    public long getBytesWritten()
    {
        return this.bytesWritten.get();
    }

    /**
     * Stop packet writer. This method returns immediately. Use
     * {@link #onFinished()} to be informed about the exact termination time
     * 
     * @throws IllegalStateException upon unexpected race conditions
     */
    public void stop()
    {
        synchronized (this)
        {
            final var res = this.stopped.tryEmitEmpty();
            switch (res)
            {
                case OK:
                    break;
                case FAIL_TERMINATED:
                case FAIL_CANCELLED:
                case FAIL_ZERO_SUBSCRIBER:
                    log.warn("Stopped in state {}", res);
                    break;
                default:
                    throw new IllegalStateException("Failed to stop, EmitResult=" + res);
            }
        }
    }

    private Mono<Void> writeAndRelease(List<GrePacket> gres,
                                       FileChannel channel)
    {
        return Mono.<Void>fromCallable(() ->
        {
            try
            {
                writePackets(gres, channel);
                return null;
            }
            finally
            {
                gres.stream().forEach(gre -> gre.getPayload().release()); // Release buffers. If memory is not release here, memory leak is certain
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private void writePackets(List<GrePacket> gres,
                              FileChannel channel) throws IOException
    {
        long pos = channel.position();

        for (var gre : gres)
        {
            switch (gre.getPayloadType())
            {
                case JSON:
                    final var json = gre.getPayload().toString(StandardCharsets.UTF_8);
                    log.info("Got JSON headers {}", json);
                    break;
                case PCAPNG_HEADERS:
                    this.sync.compareAndSet(false, true);
                    // fallthrough
                case PCAPNG_DATA:
                    if (!sync.get())
                    {
                        if (this.pcapgHeader != null)
                        {
                            final var buf = Unpooled.wrappedBuffer(this.pcapgHeader);
                            try
                            {
                                pos = writePacket(channel, buf, pos);
                            }
                            finally
                            {
                                buf.release();
                            }
                            sync.set(true);
                        }
                        else
                        {
                            log.warn("Dropped out of sync GRE packet: {}", gre);
                            continue;
                        }
                    }
                    pos = writePacket(channel, gre, pos);
                    break;
                default:
                    log.warn("Dropped unknown GRE packet: {}", gre);
            }
        }
        if (log.isDebugEnabled())
            log.debug("Wrote {} PCAPNG data packets ", dataPacketsWritten.get());
    }

    private long writePacket(final FileChannel channel,
                             final GrePacket gre,
                             long pos) throws IOException
    {

        if (gre.getPayloadType() == GrePacket.PayloadType.PCAPNG_DATA)
            dataPacketsWritten.incrementAndGet();
        else if (gre.getPayloadType() == GrePacket.PayloadType.PCAPNG_HEADERS)
            pcapNgHeadersWritten.incrementAndGet();
        return writePacket(channel, gre.getPayload(), pos);
    }

    private long writePacket(final FileChannel channel,
                             final ByteBuf buf,
                             long pos) throws IOException
    {
        final var size = buf.readableBytes();
        pos += buf.readBytes(channel, channel.position(), size);
        channel.position(pos);
        this.bytesWritten.addAndGet(size);
        return pos;
    }

    private static Mono<FileOutputStream> createFile(String output)
    {
        return Mono.fromCallable(() -> new FileOutputStream(output, false)) //
                   .subscribeOn(Schedulers.boundedElastic());
    }

}