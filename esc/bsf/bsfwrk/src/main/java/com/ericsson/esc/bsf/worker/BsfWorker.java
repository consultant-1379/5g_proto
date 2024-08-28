/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 16, 2018
 *     Author: eedstl
 */

package com.ericsson.esc.bsf.worker;

import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmAdapter;
import com.ericsson.adpal.ext.monitor.MonitorAdapter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.esc.bsf.db.BsfCassandraConfigBuilder;
import com.ericsson.esc.bsf.helper.BsfHelper;
import com.ericsson.esc.bsf.manager.BsfSchemaHandler;
import com.ericsson.esc.bsf.manager.BsfUserHandler;
import com.ericsson.esc.bsf.worker.ServerContainer.ServerTag;
import com.ericsson.sc.bsf.etcd.PcfRtCachedServiceImpl;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.bsf.model.IngressConnectionProfile;
import com.ericsson.sc.bsf.model.ServiceAddress;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.ericsson.sc.proxyal.service.PvtbApiService;
import com.ericsson.sc.proxyal.service.PvtbConfig;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxkms.KmsClientUtilities;
import com.ericsson.sc.rxkms.KmsParameters;
import com.ericsson.sc.sockettrace.RxSocketTraceHandler;
import com.ericsson.sc.sockettrace.TapcolTraceSink;
import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager;
import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager.Verification;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.sc.util.tls.TlsKeylogger;
import com.ericsson.sc.vertx.trace.VertxSocketTracer;
import com.ericsson.utilities.cassandra.CassandraMetricsExporter;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.KubeProbe;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.http.WebServerBuilder;
import com.ericsson.utilities.http.WebServerPool;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.core.Vertx;

public class BsfWorker
{
    private static final Logger log = LoggerFactory.getLogger(BsfWorker.class);

    private final RxShutdownHook shutdownHook;
    private final Optional<TlsKeylogger> tlsKeyLogger;
    private final Vertx vertx;

    private final WebServer probeWebServer;
    private final RouterHandler oamWebServer;
    private final KubeProbe kubeProbe;
    private final List<NBsfManagementHandler> bsfManagement = new ArrayList<>();
    private final CmAdapter<EricssonBsf> cm;
    private final RxSession cassandraDb;
    private final BsfWorkerParameters params;
    private final MonitorAdapter monitored;
    private final Observable<Optional<BsfCmConfig>> bsfConfig;
    private final Flowable<MultipleBindingResolver> httpLookupConfig;
    private final Flowable<RecoveryTimeConfig> rtConfig;
    private final CassandraMetricsExporter cassandraDbMetrics;
    private final ServerController serverController;
    private final BsfUserHandler dbUserHandler;
    private final WebClientProvider webClientProvider;
    private final NBsfManagementService nbsfManagementService;
    private final Optional<RxEtcd> rxEtcd;
    private final Completable pvtbService;

    private final BindingCleanupManager bindingCleanup;
    private final Observable<BsfCmConfig> nonEmptyBsfCmConfig;
    private final int replicas;
    private final Flowable<Boolean> bsfCfgVtap;
    private final Flowable<Optional<ServiceAddress>> serviceAddr;
    private final Flowable<IngressConnectionProfile> ingressConProfile;

    private final Flowable<Boolean> vtapEnabledFromPolling;
    private final Optional<TapcolTraceSink> tapcolSink;
    private Optional<PvtbApiService> pollSvc;

    private @NonNull AsyncCache<String, String> tokenCache;

    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/worker/config/logcontrol").getPath();
    private static final String BSFWORKER_CONTAINER_NAME = EnvVars.get("BSFWORKER_CONTAINER_NAME");
    private static final int TOKEN_CACHE_LENGHT = Integer.parseInt(EnvVars.get("OAUTH2_TOKEN_CACHE_LENGHT", 100));

    private static final String DEFAULT_ROUTE_ADDRESS = "[::]";

    private static final KmsClientUtilities kmsCLient = KmsClientUtilities.get(KmsParameters.instance, "eric-cm-key-role");
    private static final ConcurrentLinkedQueue<String> decryptedKeys = new ConcurrentLinkedQueue<>();

    private static final BsfWorkerInterfacesParameters IF_PARAMS = BsfWorkerInterfacesParameters.instance;

    public BsfWorker(BsfWorkerParameters params,
                     RxShutdownHook shutdownHook) throws UnknownHostException
    {
        pollSvc = PvtbApiService.fromEnvVars(VertxInstance.get());

        this.vtapEnabledFromPolling = pollSvc.map(srv -> srv.getPvtbConfigs().map(cfg -> cfg.map(PvtbConfig::getIsConfigured).orElse(false)))
                                             .orElse(Flowable.just(Boolean.FALSE));

        this.pvtbService = PvtbApiService.fromEnvVars(VertxInstance.get()).map(PvtbApiService::run).orElse(Completable.never());

        this.shutdownHook = shutdownHook;

        this.tlsKeyLogger = TlsKeylogger.fromEnvVars();
        this.params = params;
        log.info("BSF Worker configuration parameters: {}", this.params);
        this.vertx = VertxInstance.get();

        this.rxEtcd = Optional.of(params.getCheckPcfRt()).filter(Boolean::booleanValue).map(check ->
        {
            final var edb = RxEtcd.newBuilder().withEndpoint(EnvVars.get("ETCD_ENDPOINT")).withConnectionRetries(10).withRequestTimeout(5, TimeUnit.SECONDS);

            if (IF_PARAMS.dcedTlsEnabled)
            {
                edb.withTls()
                   .withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(IF_PARAMS.dcedClientCertPath), //
                                                                SipTlsCertWatch.trustedCert(IF_PARAMS.sipTlsRootCaPath),
                                                                AdvancedTlsX509TrustManager.newBuilder()
                                                                                           .setVerification(Verification.CERTIFICATE_AND_HOST_NAME_VERIFICATION)
                                                                                           .build()));
            }
            else
            {
                edb.withUser(EnvVars.get("ETCD_USERNAME")).withPassword(EnvVars.get("ETCD_PASSWORD"));
            }
            return edb.build();
        });

        final var pcfRtService = this.rxEtcd.map(etcd -> new PcfRtCachedServiceImpl(etcd, params.getPcfRtTTLsec()));

        final var wcb = WebClientProvider.builder().withHostName(IF_PARAMS.serviceHostname);
        if (IF_PARAMS.globalTlsEnabled)
            wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(IF_PARAMS.mediatorClientCertPath),
                                                            SipTlsCertWatch.trustedCert(IF_PARAMS.sipTlsRootCaPath)));
        this.webClientProvider = wcb.build(VertxInstance.get());

        this.cm = new CmAdapter<>(EricssonBsf.class,
                                  this.params.getCmSchemaName(),
                                  vertx,
                                  IF_PARAMS.mediatorPort,
                                  this.params.getCmMediatorHost(),
                                  webClientProvider,
                                  IF_PARAMS.globalTlsEnabled,
                                  IF_PARAMS.subscribeValidity,
                                  IF_PARAMS.subscribeRenewal,
                                  IF_PARAMS.subscribeHeartbeat);

        this.replicas = this.params.getWebServerPoolSize() <= 0 ? VertxInstance.getOptions().getEventLoopPoolSize() : this.params.getWebServerPoolSize();

        this.probeWebServer = WebServer.builder() //
                                       .withHost(DEFAULT_ROUTE_ADDRESS)
                                       .withPort(IF_PARAMS.probeServerPort)
                                       .build(vertx);

        // create web server for mediator notifications and pm-server metrics scraping
        var iws = WebServer.builder().withHost(DEFAULT_ROUTE_ADDRESS).withPort(IF_PARAMS.oamServerPort);
        if (IF_PARAMS.globalTlsEnabled)
        {
            var oamTls = DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(IF_PARAMS.oamServerCertPath), // bsf worker server certificate
                                                      SipTlsCertWatch.combine(// mediator server ca for verification of client certificates during
                                                                              // notifications
                                                                              SipTlsCertWatch.trustedCert(IF_PARAMS.mediatorServerCaPath),
                                                                              // pm server ca for verification of client certificates during scraping of
                                                                              // metrics
                                                                              SipTlsCertWatch.trustedCert(IF_PARAMS.pmServerCaPath)));
            iws.withDynamicTls(oamTls);
        }
        this.oamWebServer = iws.build(VertxInstance.get());

        PmAdapter.configureMetricsHandler(this.oamWebServer, true); // Use the internal web

        final var configFlow = this.getConfigFlow();
        final var bsfCfgController = new BsfCfgController();

        this.bsfConfig = bsfCfgController//
                                         .extractBsfConfigFromEricBsf(configFlow, this.params.getDefaultZeroBindingTimeout());

        // Add custom tags to all PM metrics
        BackendRegistries.getDefaultNow()
                         .config() //
                         .meterFilter(MeterFilter.commonTags(Tags.of("nf", "bsf")))
                         .meterFilter(new MeterFilter() // Add custom tags "local_type" and "nf_instance", To be used by PMBR
                         {
                             static final String UNKNOWN_NF = "";
                             final AtomicReference<String> nfInstanceName = new AtomicReference<>(UNKNOWN_NF);
                             { // NOSONAR
                                 bsfConfig.map(cfg -> cfg.flatMap(c -> Optional.ofNullable(c.getNfInstanceName())).orElse(UNKNOWN_NF))
                                          .distinctUntilChanged()
                                          .subscribe(nfInstanceName::set, err -> log.error("Failed to acquire nfInstanceName", err));
                             }

                             @Override
                             public Meter.Id map(final Meter.Id id)
                             {
                                 final var tlsPort = String.valueOf(params.getTlsPort());
                                 final var nonTlsPort = String.valueOf(params.getWorkerPort());

                                 final var matches = id.getName().startsWith("vertx") && //
                                 StreamSupport.stream(id.getTagsAsIterable().spliterator(), false)//
                                              .anyMatch(tag -> tag.getKey().equals("local")
                                                               && (findPort(tag.getValue(), tlsPort) || findPort(tag.getValue(), nonTlsPort)));

                                 final var localType = Tag.of("local_type", matches ? "external" : "internal");
                                 final var nfInstanceLabel = Tag.of("nf_instance", nfInstanceName.get());
                                 return id.withTags(List.of(nfInstanceLabel, localType));
                             }
                         });

        this.serviceAddr = bsfCfgController.extractSrvAddrConfigFromEricBsf(configFlow);

        this.bsfCfgVtap = bsfCfgController.extractVtapConfigFromEricBsf(configFlow);

        this.httpLookupConfig = bsfCfgController.extractMultipleBindingResolutionHttpLookUpFromEricBsf(configFlow);

        this.rtConfig = bsfCfgController.extractRtConfigFromEricBsf(configFlow);

        this.ingressConProfile = bsfCfgController.extractIngressConnectionProfileFromEricBsf(configFlow);

        this.cassandraDb = BsfCassandraConfigBuilder.trafficConfig(this.params.getDatabaseConfiguration(),
                                                                   this.params.getMetricsConfigurator(),
                                                                   IF_PARAMS.wcdbcdClientCertPath,
                                                                   IF_PARAMS.sipTlsRootCaPath)
                                                    .buildSession();

        this.cassandraDbMetrics = new CassandraMetricsExporter(this.cassandraDb, this.params.getMetricsConfigurator().getDomainName());
        final var parentNbsfManagmentservice = new NBsfManagementServiceImpl(cassandraDb,
                                                                             this.params.getDatabaseConfiguration().getKeyspace(),
                                                                             this.httpLookupConfig);

        // use original NbsfManagementService or create and use a new service which
        // checks recoveryTime
        this.nbsfManagementService = pcfRtService.<NBsfManagementService>map(srv -> new NBsfManagementServiceRt(parentNbsfManagmentservice, srv, this.rtConfig))
                                                 .orElse(parentNbsfManagmentservice);

        // delete token cache entries upon configuration changes
        this.bsfConfig//
                      .distinctUntilChanged()
                      .doOnNext(next ->
                      {
                          this.tokenCache.asMap().clear();
                          decryptedKeys.clear();
                          log.debug("Truncate and clean up oAuth2.0 token cache and internal list of keys.");
                      })
                      .doOnError(err -> log.error("Failed to truncate oAuth2.0 token cache", err))
                      .subscribe();

        this.nonEmptyBsfCmConfig = bsfConfig //
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .flatMap(BsfWorker::decryptKeyInCfg)
                                            .replay(1)
                                            .refCount();

        final var dbconf = this.params.getDatabaseConfiguration();

        final var nfInstanceFlow = nonEmptyBsfCmConfig.map(BsfCmConfig::getNfInstanceName).toFlowable(BackpressureStrategy.LATEST);

        this.tapcolSink = Boolean.parseBoolean(EnvVars.get("VTAP_ENABLED")) ? Optional.of(new TapcolTraceSink(VertxInstance.get(),
                                                                                                              "localhost",
                                                                                                              Integer.valueOf(EnvVars.get("TRACE_SINK_PORT")),
                                                                                                              nfInstanceFlow))
                                                                            : Optional.empty();

        this.tapcolSink.map(tracesink -> new RxSocketTraceHandler(tracesink,
                                                                  Long.valueOf(EnvVars.get("TRACE_SINK_PACKET_BUFFER_SIZE")),
                                                                  Integer.valueOf(EnvVars.get("TRACE_SINK_SEGMENT_LIMIT_BYTES")),
                                                                  nfInstanceFlow))
                       .ifPresent(VertxSocketTracer::setGlobalTracer);

        this.bindingCleanup = new BindingCleanupManager(this.cassandraDb, dbconf.getKeyspace(), nfInstanceFlow);

        this.kubeProbe = KubeProbe.Handler.singleton().configure(this.probeWebServer).register(KubeProbe.of().setAlive(true).setReady(false));

        this.serverController = new ServerController(this.kubeProbe);

        this.monitored = new MonitorAdapter(this.probeWebServer,
                                            Arrays.asList(new CommandConfig(this.cm.getNotificationHandler().getConfiguration()),
                                                          new MonitorAdapter.CommandCounter()),
                                            Arrays.asList());

        this.dbUserHandler = new BsfUserHandler(this.cassandraDb, dbconf.getKeyspace(), dbconf.getUser(), dbconf.getPassword());

        this.tokenCache = Caffeine.newBuilder().maximumSize(TOKEN_CACHE_LENGHT).buildAsync();

    }

    Flowable<BsfSrvCfg> createBsfSrvCfg(Flowable<Boolean> vtapPolling,
                                        Flowable<Boolean> bsfVtapCfg,
                                        Flowable<IngressConnectionProfile> ingressConProfile,
                                        Flowable<Optional<ServiceAddress>> srvAddr)
    {
        final var combinedVtap = Flowable.combineLatest(vtapPolling,
                                                        bsfVtapCfg,
                                                        (polling,
                                                         tapCfg) -> polling && tapCfg)
                                         .distinctUntilChanged();

        final var srvOptions = Flowable.combineLatest(combinedVtap, ingressConProfile, BsfSrvOptions::new).distinctUntilChanged();

        return Flowable.combineLatest(srvOptions, srvAddr, BsfSrvCfg::new).distinctUntilChanged();
    }

    private Observable<Optional<EricssonBsf>> getConfigFlow()
    {

        return this.cm.getNotificationHandler().getConfiguration().doOnNext(cfg -> log.debug("Processing CM config {}", cfg));

    }

    private Completable serverController()
    {

        return createBsfSrvCfg(this.vtapEnabledFromPolling, this.bsfCfgVtap, this.ingressConProfile, this.serviceAddr).concatMapSingle(srvCfg ->
        {
            log.info("Server cfg: {}", srvCfg);
            log.info("Webservers are configured in: {}", srvCfg.getStack().getDescription());

            final var webServers = createWebservers(srvCfg);

            return Single.just(new Pair<>(webServers, srvCfg.getBsfSrvOptions()));
        }).switchMapCompletable(newTargetState ->
        {
            final var newServers = newTargetState.getValue0();
            final var srvOptions = newTargetState.getValue1();

            log.debug("A new List of servers generated: {}", newServers);

            final var turnOffReadiness = this.serverController.decideToSetReadinessToFalse(srvOptions, newServers)
                                                              .filter(Boolean.TRUE::equals)
                                                              .flatMapCompletable(res -> this.serverController.updateReadinessState(false));

            final var stopServers = this.serverController.stopServers(srvOptions, newServers);
            final var startServers = this.serverController.startServers(srvOptions, newServers);

            final var turnOnReadiness = Single.defer(this.serverController::decideToSetReadinessToTrue)
                                              .filter(Boolean.TRUE::equals)
                                              .flatMapCompletable(res -> this.serverController.updateReadinessState(true))
                                              .doOnComplete(() -> log.info("Bsf worker is ready"));

            return turnOffReadiness.andThen(stopServers).andThen(startServers).andThen(turnOnReadiness);
        });

    }

    private Completable run()
    {

        // Complete as soon as a valid BSF configuration is received
        final var initialBsfConfigCheck = this.nonEmptyBsfCmConfig.firstOrError()
                                                                  .ignoreElement()
                                                                  .doOnComplete(() -> log.info("Received initial, non-empty BSF configuration from CMM"));

        return Completable.complete()
                          .andThen(this.probeWebServer.startListener()) // Start internal web server as soon as possible
                          .andThen(this.oamWebServer.startListener()) // Start internal web server as soon as possible
                          .andThen(this.monitored.start().onErrorComplete()) // Start the monitor interface
                          .andThen(this.cm.getNotificationHandler().start(this.oamWebServer)) // Start receiving CM notifications
                          .andThen(initialBsfConfigCheck) // Ensure valid initial CM config has been received
                          .andThen(BsfCassandraConfigBuilder.trafficConfigProbe(this.params.getDatabaseConfiguration(),
                                                                                this.params.getMetricsConfigurator(),
                                                                                IF_PARAMS.wcdbcdClientCertPath,
                                                                                IF_PARAMS.sipTlsRootCaPath)
                                                            .buildSession()
                                                            .testInitialConnection()) // Ensure that connection with Cassandra can be established, via temporary
                          // dedicated session
                          .andThen(BsfSchemaHandler.verifyBsfDb(this.cassandraDb, //
                                                                this.params.getDatabaseConfiguration().getKeyspace())) // Check BSF database
                          .andThen(this.dbUserHandler.ensureBsfUserConfigured())
                          .andThen(this.cassandraDbMetrics.start())
                          .andThen(this.nbsfManagementService.init()) // Initialize NbsfManagement backend
                          .andThen(this.tapcolSink.map(TapcolTraceSink::init).orElse(Completable.complete())) // Initialize Tapcol trace sink
                          .andThen( // Complete as soon as at least one of the Completables below complete
                                   Completable.ambArray(this.pollSvc.map(PvtbApiService::run).orElse(Completable.never()),
                                                        BsfHelper.monitorKms(kmsCLient),
                                                        this.serverController(), // Supervise for fatal errors, never completes
                                                        this.pvtbService,
                                                        this.nbsfManagementService.run(), // Supervise for fatal errors, never completes
                                                        this.shutdownHook.get()) // Completes as soon as JVM is ordered to shut down
                          )

                          .andThen(stop())
                          .onErrorResumeNext(throwable -> stop().andThen(Completable.error(throwable))) // try to stop gracefully, even after unexpected error
        ;
    }

    private Completable stop()
    {
        final Predicate<? super Throwable> logErr = t ->
        {
            log.warn("Ignored Exception during shutdown", t);
            return true;
        };

        return Completable.complete()//
                          .doOnSubscribe(disp -> log.info("Initiated gracefull shutdown"))
                          .andThen(this.monitored.stop().onErrorComplete(logErr))
                          .andThen(this.cm.getNotificationHandler().stop().onErrorComplete(logErr))
                          .andThen(Completable.mergeArray(this.serverController.stopAllRunningServers())) // stop all external servers
                          // gracefully
                          .andThen(this.probeWebServer.stopListener().onErrorComplete(logErr))
                          .andThen(this.oamWebServer.stopListener().onErrorComplete(logErr))
                          .andThen(Observable.fromIterable(bsfManagement).flatMapCompletable(NBsfManagementHandler::stop).onErrorComplete(logErr))
                          .andThen(this.nbsfManagementService.stop().onErrorComplete(logErr))
                          .andThen(this.cassandraDbMetrics.stop().onErrorComplete(logErr))
                          .andThen(this.cassandraDb.close().onErrorComplete(logErr))
                          .andThen(this.webClientProvider.close().onErrorComplete(logErr))
                          .andThen(this.rxEtcd.map(RxEtcd::close).orElse(Completable.complete()).onErrorComplete(logErr)) // close etcd client, if exists
                          .andThen(this.tlsKeyLogger.map(TlsKeylogger::stop).orElse(Completable.complete()).onErrorComplete(logErr))
                          .andThen(this.vertx.rxClose().onErrorComplete(logErr))
                          .andThen(Completable.fromAction(() -> KmsClientUtilities.get(KmsParameters.instance, "eric-cm-key-role").dispose()));
    }

    private List<ServerContainer> createWebservers(final BsfSrvCfg srvCfg)
    {
        final var srvOptions = srvCfg.getBsfSrvOptions();
        final var serverContainers = new ArrayList<ServerContainer>();

        switch (srvCfg.getStack())
        {

            case DUAL:
            {
                if (srvCfg.isPort())
                {
                    buildServerContainer(srvOptions, ServerTag.IPV4).ifPresent(serverContainers::add);
                    buildServerContainer(srvOptions, ServerTag.IPV6).ifPresent(serverContainers::add);
                }
                if (srvCfg.isTlsPort())
                {
                    buildServerContainer(srvOptions, ServerTag.IPV4TLS).ifPresent(serverContainers::add);
                    buildServerContainer(srvOptions, ServerTag.IPV6TLS).ifPresent(serverContainers::add);
                }
                break;
            }

            case IPV4:
            {
                if (srvCfg.isPort())
                {
                    buildServerContainer(srvOptions, ServerTag.IPV4).ifPresent(serverContainers::add);
                }
                if (srvCfg.isTlsPort())
                {
                    buildServerContainer(srvOptions, ServerTag.IPV4TLS).ifPresent(serverContainers::add);
                }
                break;
            }
            case IPV6:
            {
                if (srvCfg.isPort())
                {
                    buildServerContainer(srvOptions, ServerTag.IPV6).ifPresent(serverContainers::add);
                }
                if (srvCfg.isTlsPort())
                {
                    buildServerContainer(srvOptions, ServerTag.IPV6TLS).ifPresent(serverContainers::add);
                }
                break;
            }

            default:
            {
                log.info("Cannot start webservers. There were no ipv4address, ipv6address or Fqdn set in the configuration.");
            }
        }
        return serverContainers;
    }

    Optional<ServerContainer> buildServerContainer(final BsfSrvOptions srvOptions,
                                                   final ServerTag srvTag)
    {
        final var desc = srvTag.getDescription();
        final var tls = desc.contains("TLS");
        final var ipv6 = desc.contains("v6");

        final var listeningAddrToUse = ipv6 ? params.getIpv6ListeningAddress() : params.getIpv4ListeningAddress();

        final var webServer = buildWebSrvPool(srvOptions, tls, listeningAddrToUse);

        if (Objects.isNull(webServer))
        {
            return Optional.empty();
        }
        else
        {
            final var handlers = new ArrayList<NBsfManagementHandler>();
            webServer.childRouters()
                     .forEach(routerCfg -> handlers.add(new NBsfManagementHandler(routerCfg, //
                                                                                  this.nbsfManagementService,
                                                                                  this.bindingCleanup,
                                                                                  this.nonEmptyBsfCmConfig,
                                                                                  tls,
                                                                                  Path.of("/vertx"),
                                                                                  this.tokenCache)));
            log.info("{} server built successfully", desc);
            return Optional.of(new ServerContainer(srvTag, webServer, handlers));
        }

    }

    WebServerPool buildWebSrvPool(BsfSrvOptions srvOptions,
                                  boolean tls,
                                  Optional<String> listeningIp)
    {
        return listeningIp.map(ip ->
        {

            log.debug("Building web server with tls: {}", tls);

            final var port = tls ? this.params.getTlsPort() : this.params.getWorkerPort();

            // Build server pool
            WebServerBuilder newSrvBuilder = WebServer.builder() //
                                                      .withHost(ip)
                                                      .withPort(port)
                                                      .withHttpTracing(false)
                                                      .withGlobalTracing(srvOptions.getGlobalTracing()) // vertx tracing enabled
                                                      .withOptions(options -> options.setTrafficClass((options.getTrafficClass() & 0x03)
                                                                                                      | (srvOptions.getDscp() << 2))
                                                                                     .getInitialSettings()
                                                                                     .setHeaderTableSize(srvOptions.getHpack())
                                                                                     .setMaxConcurrentStreams(this.params.getMaxConcurrentStreams()));

            if (tls)
            {

                final var certficatesPath = this.params.getCertificatesPath();
                final var trustCAPAth = this.params.getTrustCAPath();

                newSrvBuilder.withDynamicTls(DynamicTlsCertManager //
                                                                  .create(SipTlsCertWatch.builder() //
                                                                                         .withKeyFileName("key.pem")
                                                                                         .withCertFileName("certificate.pem")
                                                                                         .buildKeyCert(certficatesPath),
                                                                          SipTlsCertWatch.builder() //
                                                                                         .withTrustedCertFileName("cert1.pem")
                                                                                         .buildTrustedCert(trustCAPAth)));

            }

            final var newSrv = newSrvBuilder.build(this.vertx, this.replicas);

            log.debug("New server built: {}", newSrv);

            return newSrv;

        }).orElse(null);

    }

    private boolean findPort(String tag,
                             String port)

    {

        final var ipParts = tag.split(":");

        final var portTaken = ipParts[ipParts.length - 1];

        return portTaken.contains(port);

    }

    public static void main(String[] args)
    {
        var exitStatus = 1;
        log.info("Starting BSF worker, version: {}, number of available CPU cores: {}", VersionInfo.get(), Runtime.getRuntime().availableProcessors());
        try (var shutdownHook = new RxShutdownHook();
             var llcbsfworker = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(),
                                                    BSFWORKER_CONTAINER_NAME))
        {
            final var params = BsfWorkerParameters.fromEnvironment();
            final var worker = new BsfWorker(params, shutdownHook);

            worker.run().blockingAwait();
            log.info("BSF worker terminated normally.");
            exitStatus = 0;
        }
        catch (Exception e)
        {
            log.error("BSF worker terminated abnormally due to exception", e);
            exitStatus = 1;
        }

        System.exit(exitStatus);
    }

    private static Observable<BsfCmConfig> decryptKeyInCfg(BsfCmConfig config)
    {
        return Observable.fromIterable(config.getOAuthkeyProfiles()).flatMapSingle(prof ->
        {
            final var ekey = Oauth2KeyProfile.Type.JWK.equals(prof.getType()) ? prof.getJsonBody() : prof.getValue();

            return !decryptedKeys.contains(ekey) ? BsfWorker.decryptOauthProfile(ekey, prof) : Single.just(prof);
        }).toList().map(profiles ->
        {
            config.setOAuthKeyProfilesMap(profiles);
            return config;
        }).toObservable();

    }

    private static Single<Oauth2KeyProfile> decryptOauthProfile(String ekey,
                                                                Oauth2KeyProfile prof)
    {
        return BsfHelper.decrypt(kmsCLient, ekey)//
                        .map(dkey ->
                        {
                            if (dkey.isEmpty())
                            {
                                return prof;
                            }
                            final var decryptedKey = dkey.get();
                            decryptedKeys.add(decryptedKey);

                            return Oauth2KeyProfile.Type.JWK.equals(prof.getType()) ? new Oauth2KeyProfile().withKeyId(prof.getKeyId())
                                                                                                            .withType(prof.getType())
                                                                                                            .withJsonBody(decryptedKey)
                                                                                    : new Oauth2KeyProfile().withKeyId(prof.getKeyId())
                                                                                                            .withType(prof.getType())
                                                                                                            .withAlg(prof.getAlg())
                                                                                                            .withValue(decryptedKey);

                        });
    }

    private static class CommandConfig extends MonitorAdapter.CommandBase
    {
        private final BehaviorSubject<Optional<EricssonBsf>> config;

        public CommandConfig(final BehaviorSubject<Optional<EricssonBsf>> config)
        {
            super("config", "Usage: command=config");
            this.config = config;
        }

        @Override
        public HttpResponseStatus execute(Result result,
                                          Command request)
        {
            this.config.getValue().ifPresent(cfg -> result.setAdditionalProperty("config", cfg));
            return HttpResponseStatus.OK;
        }
    }

}