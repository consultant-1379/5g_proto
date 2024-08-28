/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 26, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.esc.bsf.load.configuration.BsfLoadConfiguration;
import com.ericsson.esc.bsf.load.configuration.TrafficSetConfiguration;
import com.ericsson.esc.bsf.load.configuration.TrafficSetConfiguration.LoadType;
import com.ericsson.esc.bsf.load.metrics.MetricsHandler;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.net.OpenSSLEngineOptions;
import io.vertx.core.net.PemKeyCertOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;

/**
 * Handles the execution of a traffic workload.
 */
public class WorkLoad
{
    private static final Logger log = LoggerFactory.getLogger(WorkLoad.class);

    private final BindingStorage bindingStorage;
    private final BsfLoadConfiguration configuration;
    private final MetricsHandler metricsHandler;
    private final List<TrafficSet> setupMix;
    private final List<TrafficSet> trafficMix;
    private final Set<UUID> trafficSetsToRecord;
    private final UUID runId;
    private final List<WebClient> webClients = new ArrayList<>();

    /**
     * log limiter labels
     */
    private enum Lbl
    {
        UNEXPECTED
    }

    public WorkLoad(BsfLoadConfiguration configuration,
                    UUID runId,
                    MetricsHandler metricsHandler,
                    Vertx vertx)
    {
        this.bindingStorage = new BindingStorage();
        this.configuration = configuration;
        this.metricsHandler = metricsHandler;
        this.trafficSetsToRecord = new HashSet<>();
        this.runId = runId;

        // private key and certificate can be inserted as strings via configuration or
        // stored in the given path inside the pod.
        final var keyCertFromConfig = configuration.getTls().getKey() != null && configuration.getTls().getCert() != null;
        final var keyCertOptions = (keyCertFromConfig) ? new PemKeyCertOptions().addKeyValue(Buffer.buffer(configuration.getTls().getKey()))
                                                                                .addCertValue(Buffer.buffer(configuration.getTls().getCert()))

                                                       : new PemKeyCertOptions().setCertPath(configuration.getTls().getCertPath())
                                                                                .setKeyPath(configuration.getTls().getKeyPath());

        // Define options for each web client.
        var webClientOptions = new WebClientOptions().setOpenSslEngineOptions(new OpenSSLEngineOptions())
                                                     .setProtocolVersion(HttpVersion.HTTP_2)
                                                     .setHttp2MultiplexingLimit(configuration.getHttp2Streams())
                                                     .setHttp2MaxPoolSize(configuration.getMaxTcpConnectionsPerClient())
                                                     .setHttp2KeepAliveTimeout(configuration.getHttp2KeepAliveTimeout())
                                                     .setHttp2ClearTextUpgrade(false)
                                                     .setTcpFastOpen(true)
                                                     .setTcpNoDelay(true)
                                                     .setTcpQuickAck(true)
                                                     .setSsl(configuration.getTls().getEnabled())
                                                     .setEnabledSecureTransportProtocols(Set.of("TLSv1.2", "TLSv1.3"))
                                                     .setVerifyHost(configuration.getTls().getVerifyHost())
                                                     .setUseAlpn(true)
                                                     .setPemKeyCertOptions(keyCertOptions)
                                                     .setTrustAll(true);

        // Create list of clients.
        IntStream.range(0, configuration.getTcpClients()).mapToObj(tick -> WebClient.create(vertx, webClientOptions)).forEach(webClients::add);

        markSetRecording(configuration.getSetupTrafficMix());
        markSetRecording(configuration.getTrafficMix());

        // Create and sort setup traffic mix.
        this.setupMix = configuration.getSetupTrafficMix() //
                                     .stream()
                                     .map(set -> new TrafficSet(configuration, set, webClients))
                                     .sorted()
                                     .collect(Collectors.toList());

        // Create and sort traffic mix.
        this.trafficMix = configuration.getTrafficMix() //
                                       .stream()
                                       .map(set -> new TrafficSet(configuration, set, webClients))
                                       .sorted()
                                       .collect(Collectors.toList());
    }

    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log);

    public Completable run()
    {
        return this.metricsHandler.startReporters(this.configuration.getMetrics(), this.runId) //
                                  .andThen(this.runSetupTraffic())
                                  .andThen(this.runTrafficMix())
                                  .andThen(this.stop())
                                  .onErrorResumeNext(throwable -> this.stop().andThen(Completable.error(throwable)));
    }

    public Completable stop()
    {
        return this.metricsHandler.stopReporters().andThen(Completable.fromAction(() ->
        {
            this.webClients.forEach(WebClient::close);
            this.setupMix.clear();
            this.trafficMix.clear();
            this.bindingStorage.deleteAll();
        }));
    }

    /**
     * Runs the setup traffic. Each traffic set that belongs to the setup traffic is
     * only executed once. The time needed to execute the setup traffic is not
     * included in the configured duration.
     * 
     * @return Completable Completes when the setup traffic is executed.
     */
    public Completable runSetupTraffic()
    {
        return Completable.fromAction(() -> log.info("Starting setup traffic."))
                          .andThen(Flowable.fromIterable(this.setupMix) //
                                           .observeOn(Schedulers.computation())
                                           .concatMapCompletable(this::executeSet))
                          .andThen(Completable.fromAction(this.bindingStorage::deleteAll))
                          .doOnComplete(() -> log.info("Setup traffic completed."));
    }

    /**
     * Runs the traffic mix. When duration is configured the traffic sets might be
     * executed multiple times and some traffic sets might not be executed at all.
     * When duration is not configured, each traffic set is executed only once.
     * 
     * @return Completable Completes when the duration is elapsed or when all
     *         traffic sets are executed if duration is not defined.
     */
    public Completable runTrafficMix()
    {
        Completable trafficMixChain;
        var executeSetChain = Flowable.fromIterable(this.trafficMix)
                                      .observeOn(Schedulers.computation())
                                      .concatMapCompletable(this::executeSet)
                                      .andThen(Completable.fromAction(this.bindingStorage::deleteAll));

        if (configuration.getDuration() != null)
        {
            var durationTimer = Flowable.timer(this.configuration.getDuration(), TimeUnit.SECONDS, Schedulers.io()) //
                                        .doOnComplete(() -> log.info("Reached the duration limit. Terminating."))
                                        .ignoreElements();

            trafficMixChain = Completable.ambArray(durationTimer, executeSetChain.repeat());
        }
        else
            trafficMixChain = executeSetChain;

        return Completable.fromAction(() -> log.info("Starting traffic mix.")) //
                          .andThen(trafficMixChain)
                          .doOnComplete(() -> log.info("Traffic mix completed."));
    }

    /**
     * Executes a trafficSet. Stores the bindingIds of the responses if it is
     * required. It also provides the stored bindingIds to deregister sets.
     * 
     * @param set The traffic set
     * @return Completable when the traffic set execution is completed
     */
    private Completable executeSet(TrafficSet set)
    {
        var setConf = set.getSetConfig();

        // Register set: Check if binding storage is required.
        if (this.trafficSetsToRecord.contains(setConf.getId()))
        {
            this.bindingStorage.create(setConf.getName());
        }

        // Deregister set: Check if deregistration of known bindings is required.
        Flowable<String> inputIds = null;
        if (setConf.deregisterKnownIds())
        {
            setConf.setNumRequests(this.bindingStorage.getCount(setConf.getTrafficSetRef()));
            inputIds = this.bindingStorage.get(setConf.getTrafficSetRef());
        }

        log.debug("Executing traffic set {}: {}", setConf.getName(), setConf);

        return set.execute(inputIds) //
                  .compose(recordBindingIds(setConf))
                  .compose(generateStats())
                  .last(new Rate())
                  .doOnSuccess(finalRate ->
                  {
                      var finalStats = finalRate.stats;
                      var durationInNano = System.nanoTime() - finalRate.stats.getStartTime();
                      var durationInSec = durationInNano / (1000L * 1000L * 1000d);
                      var avgTps = finalStats.getTotal() / durationInSec;
                      log.debug("Set: {}, Duration: {}s, Avg TPS: {}, Success Rate: {}%", setConf.getName(), durationInSec, avgTps, finalStats.getSR());
                  })
                  .ignoreElement()
                  .doOnError(e -> safeLog.log(Lbl.UNEXPECTED, l -> log.error("Unexpected error: {} {}", e.getCause(), e.getMessage())))
                  .doOnComplete(() -> log.debug("Execution completed."));
    }

    /**
     * Stores the bindingIds of the register bindings, when the traffic set is
     * marked for recording.
     * 
     * @param setConf The traffic set configuration
     * @return A Flowable of responses
     */
    private FlowableTransformer<Response, Response> recordBindingIds(TrafficSetConfiguration setConf)
    {
        return responses -> responses.map(resp ->
        {
            if (this.trafficSetsToRecord.contains(setConf.getId()) && resp.resourceId != null)
                this.bindingStorage.store(setConf.getName(), resp.resourceId);

            return resp;
        });
    }

    /**
     * Generates and logs statistics such as TPS and success rate. The statistics
     * are sampled per second.
     * 
     * @return A Flowable of Rates.
     */
    private FlowableTransformer<Response, Rate> generateStats()
    {
        return responses -> responses.scan(new Stats(), Stats::new)
                                     .sample(1, TimeUnit.SECONDS, true)
                                     .scan(new Rate(), Rate::new)
                                     .doOnNext(rate -> log.debug("{}", rate));
    }

    /**
     * Marks the register sets for which the bindingIds must be recorded.
     * 
     * @param list A list of all TrafficSetConfigurations.
     */
    private void markSetRecording(List<TrafficSetConfiguration> list)
    {
        // Creates a map using as key the set-name and as value the set.
        var setMap = list.stream().collect(Collectors.toMap(TrafficSetConfiguration::getName, set -> set));

        // Marks all eligible register sets for recording
        list.stream().filter(set -> set.getType().equals(LoadType.DEREGISTER) && //
                                    set.getTrafficSetRef() != null && //
                                    !set.getTrafficSetRef().isEmpty())
            .forEach(set ->
            {
                var trafficSetConfig = setMap.get(set.getTrafficSetRef());
                this.trafficSetsToRecord.add(trafficSetConfig.getId());
            });
    }

}
