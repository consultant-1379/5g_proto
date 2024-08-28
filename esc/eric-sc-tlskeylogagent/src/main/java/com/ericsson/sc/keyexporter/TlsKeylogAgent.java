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
 * Created on: Nov 17, 2021
 *     Author: echfari
 */
package com.ericsson.sc.keyexporter;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.tls.TlsKeylogger;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.file.KubernetesFileWatch;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.reactivex.RxShutdownHook;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class TlsKeylogAgent
{
    private static final String VTAP_CONFIG_DIR = Optional.ofNullable(EnvVars.get("VTAP_CONFIG_DIR")) //
                                                          .orElse("/run/vtapconfig");
    private static final String VTAP_CONFIG_FILE = "tap_config.json";
    private static final String VTAP_SFTP_CONFIG_DIR = Optional.ofNullable(EnvVars.get("VTAP_SFTP_CONFIG_DIR")) //
                                                               .orElse("/run/secrets/vtap_sftp_config");
    private static final String VTAP_SFTP_CONFIG_FILE = "sftpConfig.json";

    private static final String SERVICE_NAME = EnvVars.get("SERVICE_NAME");

    private static final int DEFAULT_BUFFER_SIZE = 50000;

    private static final long FILE_SIZE_LIMIT = 1000 * 1000 * Long.parseLong(EnvVars.get("FILE_SIZE_LIMIT", 1000)); // bytes

    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/logcontrol/config").getPath();
    private static final String CONTAINER_NAME = EnvVars.get("CONTAINER_NAME");

    private static final Logger log = LoggerFactory.getLogger(TlsKeylogAgent.class);

    private final RxShutdownHook shutdownHook;
    private final PipeReader pipeReader;
    private final AtomicReference<SftpExporter> exporter = new AtomicReference<>();
    private Completable mainChain;

    private TlsKeylogAgent(RxShutdownHook shutdownHook)
    {
        this.shutdownHook = shutdownHook;
        pipeReader = new PipeReader(EnvVars.get(TlsKeylogger.ENVVAR_FIFO_PATH), // Mandatory environmental variable
                                    Optional.ofNullable(EnvVars.get(TlsKeylogger.ENVVAR_FIFO_SIZE)) // Optional environmental variable
                                            .map(Integer::parseInt)
                                            .orElse(DEFAULT_BUFFER_SIZE)

        );

        pipeReader.start();

        final var tcFlow = KubernetesFileWatch.create()
                                              .withRoot(VTAP_CONFIG_DIR)
                                              .withFile(VTAP_CONFIG_FILE)
                                              .build() //
                                              .watch(true)
                                              .map(p -> p.get(VTAP_CONFIG_FILE))
                                              .map(inputConfig -> TapConfig.fromString(inputConfig, SERVICE_NAME))
                                              .doOnNext(tapConfig ->
                                              {
                                                  if (tapConfig.isEmpty())
                                                      log.warn("Wrong service name, tap configuration ignored");
                                              })
                                              .doOnError(err -> log.error("Failed to parse TAP configuration, reconfiguration failed", err))
                                              .onErrorReturnItem(Optional.empty()) // Ignore any errors, treat them as empty configuration
                                              .filter(Optional::isPresent)
                                              .map(Optional::get);
        final var scFlow = KubernetesFileWatch.create()
                                              .withRoot(VTAP_SFTP_CONFIG_DIR)
                                              .withFile(VTAP_SFTP_CONFIG_FILE)
                                              .build() //
                                              .watch(true)
                                              .map(p -> p.get(VTAP_SFTP_CONFIG_FILE))
                                              .map(SftpConfig::fromString)
                                              .map(Optional::of)
                                              .doOnError(err -> log.error("Failed to parse SFTP configuration, reconfiguration failed", err))
                                              .onErrorReturnItem(Optional.empty()) // Ignore any errors, treat them as empty configuration
                                              .filter(Optional::isPresent)
                                              .map(Optional::get);

        final var configFlow = Flowable.combineLatest(tcFlow, scFlow, Pair::of);
        this.mainChain = configFlow //
                                   .distinctUntilChanged()
                                   .concatMapCompletable(config -> this.handleConfigChange(config.getLeft(), config.getRight()));
    }

    private Completable run()
    {
        return mainChain //
                        .andThen(this.shutdownHook.get())
                        .andThen(stop());
    }

    private Completable stopExporting()
    {
        return Completable.fromAction(() ->
        {
            // Shutdown operation requested
            final var previous = this.exporter.getAndSet(null);
            if (previous != null)
            {
                log.info("Stopping sftp exporter");
                previous.stopBlocking();
                log.info("sftp exporter stopped");
            }
        }).subscribeOn(Schedulers.io());
    }

    private Completable stop()
    {
        return Completable.fromAction(pipeReader::stopBlocking)
                          .subscribeOn(Schedulers.io())
                          .onErrorComplete() //
                          .mergeWith(this.stopExporting())
                          .onErrorComplete();
    }

    private Completable handleConfigChange(TapConfig tapConfig,
                                           SftpConfig sftpConfig)
    {
        if (!tapConfig.isTapEnabled())
        {
            // Shutdown operation requested
            return this.stopExporting();
        }
        else
        {
            final var start = Completable.fromAction(() ->
            {
                // Start operation requested
                final var sftpExporter = new SftpExporter(this.pipeReader.getQueue(), sftpConfig, EnvVars.get("HOSTNAME"), FILE_SIZE_LIMIT);
                final var updated = this.exporter.compareAndSet(null, sftpExporter);
                if (updated)
                {
                    log.info("Starting sftp exporter");
                    sftpExporter.start();
                }
            }).subscribeOn(Schedulers.io());

            return this.stopExporting().andThen(start);
        }
    }

    public static void main(String[] args)
    {
        var exitStatus = 1;

        log.info("Starting TLS keylog agent, version: {}", VersionInfo.get());
        try (var shutdownHook = new RxShutdownHook();
             var llc = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(), CONTAINER_NAME))
        {

            final var agent = new TlsKeylogAgent(shutdownHook);
            agent.run().blockingAwait();
            log.info("Agent terminated normally.");
            exitStatus = 0;
        }
        catch (Exception e)
        {
            log.error("Agent terminated abnormally due to exception", e);
            exitStatus = 1;
        }

        System.exit(exitStatus);
    }

}
