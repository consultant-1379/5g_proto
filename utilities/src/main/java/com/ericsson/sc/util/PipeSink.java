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
 * Created on: Nov 10, 2021
 *     Author: echfari
 */
package com.ericsson.sc.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes entries to a named pipe
 */
public class PipeSink
{
    private static final Logger log = LoggerFactory.getLogger(PipeSink.class);
    private final BlockingQueue<String> queue;
    private final String filePath;
    private String latest;
    private Thread thread;
    private AtomicReference<State> state = new AtomicReference<>(State.TERMINATED);

    private enum State
    {
        RUNNING,
        PIPE_OPEN,
        DRAINING,
        TERMINATED
    }

    /**
     * 
     * @param bufferSize Maximum number of entries to hold in memory
     * @param filePath   The named pipe file path
     */
    public PipeSink(int bufferSize,
                    String filePath)
    {
        this.filePath = filePath;
        queue = new ArrayBlockingQueue<>(bufferSize);
        this.thread = new Thread(this::run, filePath);
        this.thread.setDaemon(true);
    }

    /**
     * Append an entry for output, in a non-blocking manner
     * 
     * @param entry The non-null entry to append
     * @return True if append was
     */
    public int append(String entry)
    {
        Objects.requireNonNull(entry);
        if (isAcceptingInput())
        {
            final var full = !this.queue.offer(entry);
            if (full)
            {
                // Buffer is full
                final var discardMax = 10;
                final var discarded = new ArrayList<String>(discardMax);
                this.queue.drainTo(discarded, discardMax);
                log.warn("Buffer is full, discarded {} records", discarded.size());
                if (!this.queue.offer(entry))
                {
                    throw new IllegalStateException("Overload");
                }
                else
                {
                    return discarded.size();
                }
            }
            else
            {
                return 0;
            }
        }
        else
        {
            throw new IllegalStateException("Pipe is closed");
        }
    }

    /**
     * Gracefully drain the buffer, close the pipe and terminate relevant threads.
     * 
     * @param timeoutMillis The maximum time to wait for graceful closure.
     */
    public void blockingStop(int timeoutMillis)
    {
        drain(timeoutMillis);
        forceStop();
        if (this.state.get() != State.RUNNING)
        {
            try
            {
                this.thread.join(timeoutMillis);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Open the pipe and start accepting input
     * 
     * @throws IllegalStateException if the process has been terminated with #foreSt
     */
    public void start()
    {
        this.thread.start();
    }

    private void forceStop()
    {
        this.thread.interrupt();
    }

    private void drain(long timeoutMillis)
    {
        log.info("Draining");
        if (this.setDraining())
        {
            try
            {
                this.thread.join(timeoutMillis);
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            log.info("Drained, success: {}", !this.thread.isAlive());
        }
    }

    private void run()
    {
        log.info("Thread started");
        this.state.set(State.RUNNING);
        while (this.state.get() == State.RUNNING)
        {
            log.info("Opening {}", filePath);
            try (final var fc = FileChannel.open(Paths.get(filePath), StandardOpenOption.APPEND))
            {
                this.setPipeOpen();
                log.info("Opened {}", filePath);
                writeBufferToFile(fc);
            }
            catch (IOException e)
            {
                log.warn("IO operation failed,path: {}", filePath, e);
                try
                {
                    this.setRunning();
                    Thread.sleep(1000);
                }
                catch (InterruptedException e1)
                {
                    this.state.set(State.TERMINATED);
                    Thread.currentThread().interrupt();
                }
            }
            catch (InterruptedException ie)
            {
                this.state.set(State.TERMINATED);
                Thread.currentThread().interrupt();
            }
        }
        setTerminated();
        log.info("Thread terminated");
    }

    private boolean setPipeOpen()
    {
        return this.state.compareAndSet(State.RUNNING, State.PIPE_OPEN);
    }

    private boolean setDraining()
    {
        return this.state.compareAndSet(State.PIPE_OPEN, State.DRAINING);
    }

    private boolean setRunning()
    {
        return this.state.compareAndSet(State.PIPE_OPEN, State.RUNNING);
    }

    private void setTerminated()
    {
        this.state.set(State.TERMINATED);
    }

    private boolean isAcceptingInput()
    {
        final var currentState = this.state.get();
        return (currentState == State.RUNNING) || (currentState == State.PIPE_OPEN);
    }

    private boolean isWrittingToPipe()
    {
        final var currentState = this.state.get();
        return (currentState == State.PIPE_OPEN || //
                (currentState == State.DRAINING && this.queue.isEmpty()));
    }

    private void writeBufferToFile(FileChannel fc) throws IOException, InterruptedException
    {
        while (isWrittingToPipe())
        {
            this.latest = this.latest == null ? queue.take() : this.latest;
            fc.write(ByteBuffer.wrap(latest.getBytes(StandardCharsets.UTF_8)));
            this.latest = null;
        }
    }
}
