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
package com.ericsson.sc.tapcol.file;

import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.tapcol.PacketSink;
import com.ericsson.sc.tapcol.pcap.TapPacket;
import com.ericsson.sc.utils.pcapng.PcapNgBuilder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Streams packets to file system
 */
public class FilePacketSink implements PacketSink
{
    private static final Logger log = LoggerFactory.getLogger(FilePacketSink.class);
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public Mono<Void> consumePacketStream(Flux<List<TapPacket>> packetFlux)
    {
        final var bufFlux = packetFlux //
                                      .switchOnFirst((first,
                                                      in) ->
                                      {
                                          final var fst = first.hasValue() ? first.get() : null;

                                          return in //
                                                   .map(pkt ->
                                                   {
                                                       final var buff = Unpooled.buffer();
                                                       if (pkt == fst)
                                                       {
                                                           PcapNgBuilder.writeSHB(buff);
                                                           PcapNgBuilder.writeIDB(buff);
                                                       }
                                                       PcapNgBuilder.writeEPB(buff, pkt.get(0).getPacket(), pkt.get(0).getTimeStamp());
                                                       return buff;
                                                   }) //
                                                   .map(ByteBuf::nioBuffer);
                                      });
        return createFile().flatMap(asyncFile ->
        {
            final var closeAsync = closeFile(asyncFile);
            return AsyncFileUtil.writeFile(bufFlux, asyncFile.getT2()) //
                                .doFinally(signal -> closeAsync.subscribe())
                                .then(closeAsync);
        });
    }

    private static Mono<Void> closeFile(Tuple2<Path, AsynchronousFileChannel> asyncFile)
    {
        return Mono.defer(() ->
        {
            log.info("Closing file {}", asyncFile.getT1());
            try
            {
                asyncFile.getT2().close();
            }
            catch (Exception e)
            {
                log.warn("Failed to close file {}", asyncFile.getT1(), e);
                return Mono.empty(); // ignore file close errors
            }
            return Mono.empty();
        }).cache().then();
    }

    private Mono<Tuple2<Path, AsynchronousFileChannel>> createFile()
    {
        return Mono.fromCallable(() ->

        {
            final var attrs = new FileAttribute[0];
            final var fn = Files.createTempFile("tapcol", ".pcapng", PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
            log.info("Created file {}", fn);
            final var asyncFile = AsynchronousFileChannel.open(fn, //
                                                               Set.of(StandardOpenOption.WRITE, StandardOpenOption.CREATE),
                                                               executor,
                                                               attrs);
            return Tuples.of(fn, asyncFile);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
