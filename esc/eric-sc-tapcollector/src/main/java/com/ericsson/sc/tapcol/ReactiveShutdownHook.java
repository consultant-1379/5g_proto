/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 20, 2019
 *     Author: xchrfar
 */
package com.ericsson.sc.tapcol;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Mono;

/**
 * Reactive wrapper for JVM shutdown hooks
 */
public class ReactiveShutdownHook implements AutoCloseable
{
    private static final Logger log = LoggerFactory.getLogger(ReactiveShutdownHook.class);
    private final Mono<Void> vmShutdown;
    private final CompletableFuture<Object> shutdownComplete = new CompletableFuture<>();

    /**
     * Create a shutdown hook
     * 
     * @see java.lang.Runtime#addShutdownHook(Thread)
     */
    public ReactiveShutdownHook()
    {
        this.vmShutdown = Mono.<Void>create(emitter ->
        {
            final var hook = new Thread(() ->
            {

                log.info("JVM is shutting down");

                // Signal JVM shutdown to observers
                emitter.success();

                try
                {
                    // Do not allow JVM shutdown to proceed, until close() has been called
                    shutdownComplete.get();
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                    // Continue JVM shutdown
                }
                catch (ExecutionException e)
                {
                    // Ignore exception, continue JVM shutdown
                }
            });
            hook.setName(ReactiveShutdownHook.class.getName());
            // Add hook to JVM, at subscription time
            log.debug("Added shutdown hook {}", hook);
            Runtime.getRuntime().addShutdownHook(hook);
        }).cache();
        this.vmShutdown.subscribe();
    }

    /**
     * 
     * @return A {@code Completable} that completes when JVM shutdown is initiated.
     *         JVM shutdown is delayed, until {@link close} has been called,
     *         typically from main thread. The shutdown hook is registered to JVM
     *         upon first {@code Completable} subscription.
     */
    public Mono<Void> get()
    {
        return this.vmShutdown;
    }

    /**
     * Indicate that JVM shutdown can proceed
     */
    private void signal()
    {
        this.shutdownComplete.complete(new Object());
    }

    @Override
    public void close()
    {
        this.signal();
    }
}
