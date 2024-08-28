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
package com.ericsson.sc.keyexporter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;

/**
 * Reads line from a named pipe into a BlockingQueue
 */
public class PipeReader
{
    /**
     * log limiter labels
     */
    private enum Lbl
    {
        PIPE_READ_ERROR,
        BUFFER_FULL
    }

    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log);
    private Loggers<Lbl> safeLogBuffer = LogLimitter.create(Lbl.class, log, 1800000); // 1.800.000 milliseconds 1 log every 30 mins

    private static final long DRAIN_TIMEOUT = 2000; // 2 seconds
    private static final Logger log = LoggerFactory.getLogger(PipeReader.class);
    private final BlockingQueue<String> queue;
    private final Thread thread;
    private final String pipePath;
    private AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private AtomicReference<Reader> reader = new AtomicReference<>();
    private final int bufferSize;

    public enum State
    {
        RUNNING,
        PIPE_OPEN,
        CLOSED;
    }

    public PipeReader(String pipePath,
                      int bufferSize)
    {
        Objects.requireNonNull(pipePath);
        this.bufferSize = bufferSize;
        this.queue = new LinkedBlockingQueue<>(bufferSize);
        this.pipePath = pipePath;
        this.thread = new Thread(this::run);
        this.thread.setDaemon(true); // Thread might block while opening pipe, thus we might never be able to stop it
    }

    /**
     * Stop reading from pipe, possibly blocking until associated thread has
     * finished. Once stopped, the reader cannot be restarted
     */
    public void stopBlocking()
    {
        if (this.state.get().equals(State.PIPE_OPEN))
        {
            try
            {
                this.reader.get().close();
            }
            catch (IOException e)
            {
                log.warn("Failed to close pipe {}", pipePath, e);
            }
        }
        this.state.set(State.CLOSED);
        try
        {
            this.thread.join(DRAIN_TIMEOUT);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 
     * @return The queue that contains the entries read from the pipe
     */
    public BlockingQueue<String> getQueue()
    {
        return this.queue;
    }

    /**
     * Open the pipe and start reading data. This will result in a new thread being
     * started.
     */
    public void start()
    {
        this.thread.start();
    }

    private void run()
    {
        state.set(State.RUNNING);
        while (state.get() != State.CLOSED)
        {
            try (final var fr = new BufferedReader(new FileReader(pipePath)))
            {
                this.reader.set(fr);
                state.set(State.PIPE_OPEN);
                log.info("Opened pipe: {}", pipePath);
                while (state.get() == State.PIPE_OPEN)
                {
                    final var line = fr.readLine();
                    if (line != null)
                    {
                        pushDataToQueue(line);
                    }
                    else
                    {
                        setStateRunning();
                    }
                }
            }
            catch (Exception e)
            {

                if (!state.get().equals(State.CLOSED))
                {
                    safeLog.log(Lbl.PIPE_READ_ERROR, logger -> logger.warn("Failed to read from pipe {}", pipePath, e));
                    setStateRunning();

                    try
                    {
                        Thread.sleep(1000); // sleep for 1 second, then retry
                    }
                    catch (InterruptedException e1)
                    {
                        log.warn("Interrupted", e1);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        } // try again
    }

    private boolean pushDataToQueue(String line)
    {
        if (!queue.offer(line))
        {
            final var dropped = new ArrayList<String>(10);
            queue.drainTo(dropped, 10);
            safeLogBuffer.log(Lbl.BUFFER_FULL, logger -> logger.info("Buffer full: {}, dropped {}", this.bufferSize, dropped.size()));
            final var res = queue.offer(line); // ignore any errors
            if (!res)
            {
                // This cannot happen
                log.error("Buffer full, new entry dropped");
                return false;
            }
        }
        return true;
    }

    private boolean setStateRunning()
    {
        return this.state.compareAndSet(State.PIPE_OPEN, State.RUNNING);
    }
}
