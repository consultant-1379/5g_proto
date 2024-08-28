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

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.publisher.MonoSink;
import reactor.core.publisher.Operators;

/**
 * Subscriber that writes a stream of {@link ByteBuffer ByteBuffers} to a file.
 */
public final class FileWriteSubscriber implements Subscriber<ByteBuffer>
{

    // volatile ensures that writes to these fields by one thread will be
    // immediately visible to other threads.
    // An I/O pool thread will write to isWriting and read isCompleted,
    // while another thread may read isWriting and write to isCompleted.
    private volatile boolean isWriting = false;
    private volatile boolean isCompleted = false;

    private final AsynchronousFileChannel fileChannel;
    private final AtomicLong position;
    private final MonoSink<Void> emitter;

    private Subscription subscription;

    public FileWriteSubscriber(AsynchronousFileChannel fileChannel,
                               long position,
                               MonoSink<Void> emitter)
    {
        this.fileChannel = fileChannel;
        this.position = new AtomicLong(position);
        this.emitter = emitter;
    }

    @Override
    public void onSubscribe(Subscription s)
    {
        // Only set the Subscription if one has not been previously set.
        // Any additional Subscriptions will be cancelled.
        if (Operators.validate(this.subscription, s))
        {
            subscription = s;

            s.request(1);
        }
    }

    @Override
    public void onNext(ByteBuffer bytes)
    {
        try
        {
            if (isWriting)
            {
                onError(new IllegalStateException("Received onNext while processing another write operation."));
            }
            else
            {
                write(bytes);
            }
        }
        catch (Exception ex)
        {
            // If writing has an error, and it isn't caught, there is a possibility for it
            // to deadlock the reactive
            // stream. Catch the exception and propagate it manually so that doesn't happen.
            onError(ex);
        }
    }

    private void write(ByteBuffer bytes)
    {
        isWriting = true;

        fileChannel.write(bytes, position.get(), bytes, new CompletionHandler<Integer, ByteBuffer>()
        {
            @Override
            public void completed(Integer result,
                                  ByteBuffer attachment)
            {
                position.addAndGet(result);

                if (bytes.hasRemaining())
                {
                    // If the entire ByteBuffer hasn't been written send it to be written again
                    // until it completes.
                    write(bytes);
                }
                else
                {
                    isWriting = false;
                    if (isCompleted)
                    {
                        emitter.success();
                    }
                    else
                    {
                        subscription.request(1);
                    }
                }
            }

            @Override
            public void failed(Throwable exc,
                               ByteBuffer attachment)
            {
                onError(exc);
            }
        });
    }

    @Override
    public void onError(Throwable throwable)
    {
        isWriting = false;
        subscription.cancel();
        emitter.error(throwable);
    }

    @Override
    public void onComplete()
    {
        isCompleted = true;
        if (!isWriting)
        {
            emitter.success();
        }
    }
}