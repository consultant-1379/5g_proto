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
package com.ericsson.sc.util.tls;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.PipeSink;
import com.ericsson.utilities.common.EnvVars;

import io.netty.handler.ssl.OpenSsl;
import io.netty.internal.tcnative.KeylogCallback;
import io.netty.internal.tcnative.SSLContext;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

/**
 * Enables TLS key logging
 */
public class TlsKeylogger
{
    public static final String ENVVAR_FIFO_PATH = "TLS_KEYLOGGER_FIFO_PATH";
    public static final String ENVVAR_FIFO_UID = "TLS_KEYLOGGER_FIFO_UID";
    public static final String ENVVAR_FIFO_SIZE = "TLS_KEYLOGGER_BUFFER_SIZE";

    private static final int BUFFER_SIZE = 1000;
    private static final int LOGLINE_MAX_SIZE = 1022;
    private static final Logger log = LoggerFactory.getLogger(TlsKeylogger.class);

    private final PipeSink pipeSink;

    /**
     * Create a new key logger
     * 
     * @param pipePath   Path to named pipe to be used for exporting keys. The file
     *                   shall be created if not already exists
     * @param bufferSize The Maximum number of log lines to keep in memory. If size
     *                   is exceeded, new entries will overwrite old entries in
     *                   buffer.
     */
    public TlsKeylogger(String pipePath,
                        int gid,
                        int bufferSize)
    {
        OpenSsl.ensureAvailability();
        SSLContext.enableTracing(true);
        createFifoIfNotExists(pipePath, gid);

        pipeSink = new PipeSink(bufferSize, pipePath);
        KeylogCallback.setCallback(logline ->
        {
            try
            {
                if (logline.length() > LOGLINE_MAX_SIZE)
                {
                    // TODO use log compression
                    log.warn("Truncated TLS pre-master secret,length: {} limit: {} ", logline.length(), LOGLINE_MAX_SIZE);
                }
                else
                {
                    final var res = pipeSink.append(logline + '\n');
                    if (res != 0)
                    {
                        // TODO use log compression
                        log.warn("Discarded {} TLS pre-master secrets", res);
                    }
                }
            }
            catch (Exception e)
            {
                // TODO use log compression
                log.warn("Failed to log TLS pre-master secret", e);
            }
        });
        pipeSink.start();
    }

    /**
     * Creates a named pipe, if not already existing. This is a blocking call and
     * requires mkfifo and chgrp utilities being installed on the operating system.
     * 
     * @param pipePath The file path to create
     * @param If       positive, the GID to set to the created file
     */
    public static void createFifoIfNotExists(String pipePath,
                                             int gid)
    {
        try
        {
            final var fifoPath = Paths.get(pipePath);
            if (Files.exists(fifoPath))
            {
                return;
            }

            final var mode = 640; // rw-r-----
            var chgrpResult = Runtime.getRuntime().exec(new String[] { "/usr/bin/mkfifo", pipePath, "-m" + mode }).waitFor();
            if (chgrpResult != 0)
            {
                throw new IllegalArgumentException("Failed to create FIFO " + pipePath + " result code: " + chgrpResult);
            }
            if (gid > 0)
            {
                // Set FIFO gid to specific value
                chgrpResult = Runtime.getRuntime().exec(new String[] { "/usr/bin/chgrp", String.valueOf(gid), pipePath }).waitFor();
                if (chgrpResult != 0)
                {
                    throw new IllegalArgumentException("Failed to chgrp to gid " + gid + " FIFO " + pipePath + " result code: " + chgrpResult);
                }
            }
            log.info("Created FIFO {} uid: {} gid: {} mode: {}",
                     fifoPath, //
                     Files.getAttribute(Path.of(pipePath), "unix:uid"),
                     Files.getAttribute(Path.of(pipePath), "unix:gid"),
                     Files.getAttribute(Path.of(pipePath), "unix:mode"));
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
            throw new IllegalArgumentException("Interrupted while creating FIFO " + pipePath, ie);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to create FIFO " + pipePath, e);
        }
    }

    /**
     * Stop logging TLS keys, in a gracefull manner if possible. Once stopped, the
     * key logger cannot be restarted.
     * 
     * @return A Completable that performs the operation upon subscription
     */
    public Completable stop()
    {
        return Completable.fromAction(() -> pipeSink.blockingStop(2000)).subscribeOn(Schedulers.io());
    }

    /**
     * Create a key logger taking into account environmental variables
     * 
     * @return The newly created key logger or an empty optional if environmental
     *         variables are not set
     */
    public static Optional<TlsKeylogger> fromEnvVars()
    {
        final var bufferSize = Optional.ofNullable(EnvVars.get(ENVVAR_FIFO_SIZE)) //
                                       .map(Integer::parseInt)
                                       .orElse(BUFFER_SIZE);
        return Optional.ofNullable(EnvVars.get(ENVVAR_FIFO_PATH)) //
                       .map(path -> new TlsKeylogger(path,
                                                     Optional.ofNullable(EnvVars.get(ENVVAR_FIFO_UID)) //
                                                             .map(Integer::valueOf)
                                                             .orElse(-1), // default GID, means
                                                                          // GID should not be
                                                                          // set
                                                     bufferSize));
    }
}
