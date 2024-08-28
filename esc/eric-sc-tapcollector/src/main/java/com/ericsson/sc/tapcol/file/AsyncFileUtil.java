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
import java.nio.file.StandardOpenOption;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utility type exposing methods to deal with {@link Flux}.
 */
public final class AsyncFileUtil
{

    /**
     * s Writes the {@link ByteBuffer ByteBuffers} emitted by a {@link Flux} of
     * {@link ByteBuffer} to an {@link AsynchronousFileChannel}.
     * <p>
     * The {@code outFile} is not closed by this call, closing of the
     * {@code outFile} is managed by the caller.
     * <p>
     * The response {@link Mono} will emit an error if {@code content} or
     * {@code outFile} are null. Additionally, an error will be emitted if the
     * {@code outFile} wasn't opened with the proper open options, such as
     * {@link StandardOpenOption#WRITE}.
     *
     * @param content The {@link Flux} of {@link ByteBuffer} content.
     * @param outFile The {@link AsynchronousFileChannel}.
     * @return A {@link Mono} which emits a completion status once the {@link Flux}
     *         has been written to the {@link AsynchronousFileChannel}.
     */
    public static Mono<Void> writeFile(Flux<ByteBuffer> content,
                                       AsynchronousFileChannel outFile)
    {
        return writeFile(content, outFile, 0);
    }

    /**
     * Writes the {@link ByteBuffer ByteBuffers} emitted by a {@link Flux} of
     * {@link ByteBuffer} to an {@link AsynchronousFileChannel} starting at the
     * given {@code position} in the file.
     * <p>
     * The {@code outFile} is not closed by this call, closing of the
     * {@code outFile} is managed by the caller.
     * <p>
     * The response {@link Mono} will emit an error if {@code content} or
     * {@code outFile} are null or {@code position} is less than 0. Additionally, an
     * error will be emitted if the {@code outFile} wasn't opened with the proper
     * open options, such as {@link StandardOpenOption#WRITE}.
     *
     * @param content  The {@link Flux} of {@link ByteBuffer} content.
     * @param outFile  The {@link AsynchronousFileChannel}.
     * @param position The position in the file to begin writing the
     *                 {@code content}.
     * @return A {@link Mono} which emits a completion status once the {@link Flux}
     *         has been written to the {@link AsynchronousFileChannel}.
     */
    public static Mono<Void> writeFile(Flux<ByteBuffer> content,
                                       AsynchronousFileChannel outFile,
                                       long position)
    {
        if (content == null && outFile == null)
        {
            throw new IllegalArgumentException(("'content' and 'outFile' cannot be null."));
        }
        else if (content == null)
        {
            throw new IllegalArgumentException(("'content' cannot be null."));
        }
        else if (outFile == null)
        {
            throw new IllegalArgumentException(("'outFile' cannot be null."));
        }
        else if (position < 0)
        {
            throw new IllegalArgumentException(("'position' cannot be less than 0."));
        }

        return Mono.create(emitter -> content.subscribe(new FileWriteSubscriber(outFile, position, emitter)));
    }

    private AsyncFileUtil()
    {
    }
}
