/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 20, 2020
 *     Author: entngrg
 */

package com.ericsson.sc.bsf.diameter;

import java.net.URI;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmAdapter;
import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.esc.bsf.db.BsfCassandraConfigBuilder;
import com.ericsson.esc.bsf.manager.BsfSchemaHandler;
import com.ericsson.esc.bsf.manager.BsfUserHandler;
import com.ericsson.esc.bsf.worker.BindingCleanupManager;
import com.ericsson.esc.bsf.worker.CmConfigurationUtil;
import com.ericsson.esc.bsf.worker.DefaultConfigItems;
import com.ericsson.esc.bsf.worker.MultipleBindingResolver;
import com.ericsson.esc.bsf.worker.NBsfManagementService;
import com.ericsson.esc.bsf.worker.NBsfManagementServiceImpl;
import com.ericsson.esc.bsf.worker.NBsfManagementServiceRt;
import com.ericsson.esc.bsf.worker.RecoveryTimeConfig;
import com.ericsson.sc.bsf.etcd.PcfDbEtcd;
import com.ericsson.sc.bsf.etcd.PcfDbWatcher;
import com.ericsson.sc.bsf.etcd.PcfRtCachedServiceImpl;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.diameter.AaaService;
import com.ericsson.sc.diameter.DiaGrpc;
import com.ericsson.sc.diameter.DiaGrpcClient;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager;
import com.ericsson.sc.util.tls.AdvancedTlsX509TrustManager.Verification;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
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
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.core.Vertx;

/**
 * Implements the Bsf Diameter AAA service
 */
public class BsfDiameter
{
    private static final String GRPC_OWN_HOST = "localhost";
    private static final int GRPC_OWN_PORT = 10000;
    private static final String GRPC_SERVER_HOST = GRPC_OWN_HOST;
    private static final int GRPC_SERVER_PORT = 20190;
    private static final Logger log = LoggerFactory.getLogger(BsfDiameter.class);
    private static final String AAA_SERVICE_NAME = "SC.bsf";
    private static final int DB_CONNECTION_RETRIES = 20;
    private final Vertx vertx;
    private final BsfDiameterParams params;
    private final RxShutdownHook shutdownHook;
    private final RxSession rxSession;
    private final WebServer probeWebServer;
    private final RouterHandler oamWebServer;
    private final NBsfManagementService nbsfManagementService;
    private final KubeProbe kubeProbe;
    private final DiaGrpc diaGrpc;
    private final CmAdapter<EricssonBsf> cm;
    private final CassandraMetricsExporter cassandraDbMetrics;
    private final BsfUserHandler dbUserHandler;
    private final RxEtcd rxEtcd;
    private final Flowable<Optional<BsfDiameterCfg>> bsfDiamterCfg;
    private final Flowable<MultipleBindingResolver> diameterLookupConfig;
    private final Flowable<RecoveryTimeConfig> rtResolver;
    private final DiaGrpcClient diaClient;
    private final PcfDbWatcher pcfDbWatcher;
    private final WebClientProvider webClientProvider;

    private static final BsfDiameterInterfacesParameters IF_PARAMS = BsfDiameterInterfacesParameters.instance;

    private static final String LOG_CONTROL_FILE = "logControl.json";
    private static final String LOG_CONTROL_PATH = URI.create("/etc/adp/").getPath();
    private static final String BSFDIAMETER_CONTAINER_NAME = EnvVars.get("BSFDIAMETER_CONTAINER_NAME");

    private static final String DEFAULT_ROUTE_ADDRESS = "[::]";

    public BsfDiameter(RxShutdownHook shutdownHook,
                       BsfDiameterParams params) throws UnknownHostException
    {
        // Initialize Vertx event loop
        this.vertx = VertxInstance.get();
        this.params = params;
        this.shutdownHook = shutdownHook;

        final var edb = RxEtcd.newBuilder()
                              .withEndpoint(params.getEtcdParams().getEtcdEndpoint().toString())
                              .withConnectionRetries(DB_CONNECTION_RETRIES)
                              .withRequestTimeout(10, TimeUnit.SECONDS);

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
            edb.withUser(params.getEtcdParams().getEtcdUsername()).withPassword(params.getEtcdParams().getEtcdPassword());
        }

        this.rxEtcd = edb.build();

        final var pcfDbEtcd = new PcfDbEtcd(this.rxEtcd, true, params.getPcfRtTTLsec());

        final var pcfRtService = Optional.of(params.getCheckPcfRt()) //
                                         .filter(Boolean::booleanValue)
                                         .map(check -> new PcfRtCachedServiceImpl(this.rxEtcd, params.getPcfRtTTLsec()));
        this.pcfDbWatcher = pcfDbEtcd.newWatcher();

        final var wcb = WebClientProvider.builder().withHostName(IF_PARAMS.serviceHostname);
        if (IF_PARAMS.globalTlsEnabled)
        {
            wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(IF_PARAMS.mediatorClientCertPath), //
                                                            SipTlsCertWatch.trustedCert(IF_PARAMS.sipTlsRootCaPath)));
        }
        this.webClientProvider = wcb.build(VertxInstance.get());

        // Initalize the CM Adapter
        this.cm = new CmAdapter<>(EricssonBsf.class,
                                  params.getCmSchemaName(),
                                  this.vertx,
                                  IF_PARAMS.mediatorPort,
                                  params.getCmMediatorHost(),
                                  webClientProvider,
                                  IF_PARAMS.globalTlsEnabled,
                                  IF_PARAMS.subscribeValidity,
                                  IF_PARAMS.subscribeRenewal,
                                  IF_PARAMS.subscribeHeartbeat);

        try
        {
            // Define web grpcServer used for internal communication
            this.probeWebServer = WebServer.builder() //
                                           .withHost(DEFAULT_ROUTE_ADDRESS)
                                           .withPort(IF_PARAMS.probeServerPort)
                                           .withListenAll()
                                           .build(vertx);

            // create web server for mediator notifications, yang-provider validations and
            // pm-server metrics scraping
            var iws = WebServer.builder().withHost(DEFAULT_ROUTE_ADDRESS).withPort(IF_PARAMS.oamServerPort);
            if (IF_PARAMS.globalTlsEnabled)
            {
                var oamTls = DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(IF_PARAMS.oamServerCertPath), // bsf diameter server certificate
                                                          SipTlsCertWatch.combine(// mediator server ca for verification of client certificates during
                                                                                  // notifications
                                                                                  SipTlsCertWatch.trustedCert(IF_PARAMS.mediatorServerCaPath),
                                                                                  // pm server ca for verification of client certificates during scraping of
                                                                                  // metrics
                                                                                  SipTlsCertWatch.trustedCert(IF_PARAMS.pmServerCaPath)));
                iws.withDynamicTls(oamTls);
            }
            this.oamWebServer = iws.build(VertxInstance.get());

            // Add custom tags to all metrics
            BackendRegistries.getDefaultNow()
                             .config() //
                             .meterFilter(MeterFilter.commonTags(Tags.of("nf", "bsf")));
            PmAdapter.configureMetricsHandler(this.oamWebServer, true); // Use the internal web server for PM metrics

            this.kubeProbe = KubeProbe.Handler.singleton().configure(this.probeWebServer).register(KubeProbe.of().setAlive(true).setReady(false));

            this.rxSession = BsfCassandraConfigBuilder.trafficConfig(params.getDatabaseConfiguration(),
                                                                     params.getMetricsConfigurator(),
                                                                     IF_PARAMS.wcdbcdClientCertPath,
                                                                     IF_PARAMS.sipTlsRootCaPath)
                                                      .buildSession();

            this.cassandraDbMetrics = new CassandraMetricsExporter(this.rxSession, params.getMetricsConfigurator().getDomainName());

            this.diameterLookupConfig = this.cm.getNotificationHandler()
                                               .getConfiguration()
                                               .doOnNext(cfg -> log.debug("Processing CM config {}", cfg))
                                               .map(cfg -> CmConfigurationUtil.getBsfService(cfg).map(CmConfigurationUtil::getMultipleBindingResolution))
                                               .filter(Optional::isPresent)
                                               .map(Optional::get)
                                               .distinctUntilChanged() // Only emit changes
                                               .map(mbr -> new MultipleBindingResolver(mbr.getDiameterLookup()))
                                               .toFlowable(BackpressureStrategy.LATEST);

            this.rtResolver = this.cm.getNotificationHandler()
                                     .getConfiguration()
                                     .doOnNext(cfg -> log.debug("Processing CM config, tring to get checkRT {}", cfg))
                                     .map(cfg -> CmConfigurationUtil.getBsfService(cfg)
                                                                    .map(CmConfigurationUtil::getPcfRecoveryTime)
                                                                    .orElse(DefaultConfigItems.generateDefaultPcfRt()))
                                     .distinctUntilChanged() // Only emit changes
                                     .map(rt -> new RecoveryTimeConfig(rt.getCheckUponLookup(), rt.getBindingDatabaseScan()))
                                     .toFlowable(BackpressureStrategy.LATEST);

            final var parentNbsfManagmentservice = new NBsfManagementServiceImpl(rxSession,
                                                                                 params.getDatabaseConfiguration().getKeyspace(),
                                                                                 this.diameterLookupConfig);
            this.nbsfManagementService = pcfRtService.<NBsfManagementService>map(srv -> new NBsfManagementServiceRt(parentNbsfManagmentservice,
                                                                                                                    srv,
                                                                                                                    this.rtResolver))
                                                     .orElse(parentNbsfManagmentservice);

            final var bsfAaaService = new AaaService(AAA_SERVICE_NAME, GRPC_OWN_HOST, GRPC_OWN_PORT);
            this.diaClient = new DiaGrpcClient(bsfAaaService, //
                                               2000,
                                               NettyChannelBuilder //
                                                                  .forAddress(GRPC_SERVER_HOST, GRPC_SERVER_PORT)
                                                                  .usePlaintext() // Do not use TLS ( which is
                                                                                  // default )
                                                                  .directExecutor()
                                                                  .build());

            this.bsfDiamterCfg = this.cm.getNotificationHandler()
                                        .getConfiguration()
                                        .doOnNext(cfg -> log.debug("Processing changes received from CM Mediator"))
                                        .map(BsfDiameter::cmConfigToBsfConfig)
                                        .distinctUntilChanged() // Only emit changes
                                        .doOnNext(cfg -> log.info("BSF CM configuration changed:  {}", cfg))
                                        .replay(1)
                                        .refCount()
                                        .toFlowable(BackpressureStrategy.LATEST);

            final var dbConf = this.params.getDatabaseConfiguration();

            final var bindingCleanup = new BindingCleanupManager(this.rxSession,
                                                                 dbConf.getKeyspace(),
                                                                 bsfDiamterCfg.map(conf -> conf.map(BsfDiameterCfg::getNfInstanceName).orElse("unknown")));

            this.diaGrpc = new DiaGrpc(diaClient, //
                                       NettyServerBuilder //
                                                         .forPort(GRPC_OWN_PORT)
                                                         .directExecutor(),
                                       new BsfDiaGrpcHandler(nbsfManagementService, bindingCleanup, bsfDiamterCfg, this.pcfDbWatcher.bsfDbView()));

            this.dbUserHandler = new BsfUserHandler(this.rxSession, dbConf.getKeyspace(), dbConf.getUser(), dbConf.getPassword());
        }
        catch (Exception e)
        {
            this.vertx.close();
            throw e;
        }
    }

    /**
     * 
     * @return A Completable that completes when bsf-diameter has terminated
     *         gracefully
     */
    Completable run()
    {
        final Completable waitForConfigPresence = this.bsfDiamterCfg.filter(Optional::isPresent) //
                                                                    .firstOrError()
                                                                    .ignoreElement()
                                                                    .doOnSubscribe(disp -> log.info("Waiting for initial BSF Diameter confguration"))
                                                                    .doOnComplete(() -> log.info("Initial BSF Diameter configuration received"));
        final var waitForConfigAbsence = this.bsfDiamterCfg.filter(Optional::isEmpty) //
                                                           .firstOrError()
                                                           .ignoreElement();
        final Flowable<Boolean> configPresence = this.bsfDiamterCfg //
                                                                   .map(Optional::isPresent)
                                                                   .doOnNext(present -> log.info("BSF Diameter configuration is now {}",
                                                                                                 Boolean.TRUE.equals(present) ? "PRESENT" : "ABSENT"))
                                                                   .distinctUntilChanged()
                                                                   .replay(1)
                                                                   .refCount();

        final var diaGrpcEngine = diaGrpc.run(this.shutdownHook.get() //
                                                               .ambWith(waitForConfigAbsence)) // run until either JVM terminates or CM configuration is removed
                                         .doOnNext(next -> log.info("DiaGrpc state change: {}", next))
                                         .doOnNext(next -> this.kubeProbe.setReady(true))
                                         .ignoreElements()
                                         .doOnError(err -> log.warn("Restarting {} service due to error", AAA_SERVICE_NAME, err))
                                         .doOnError(err -> this.kubeProbe.setReady(false))
                                         .doOnComplete(() -> this.kubeProbe.setReady(false))
                                         // TODO limit number of retries, use retry function
                                         .retryWhen(p -> p.delay(2, TimeUnit.SECONDS));

        final var controller = configPresence.takeUntil(this.shutdownHook.get().toFlowable())
                                             .concatMapCompletable(present -> Boolean.TRUE.equals(present) ? diaGrpcEngine : Completable.complete());

        return Completable.complete()
                          .andThen(this.probeWebServer.startListener()) // Start probe web server, for k8s probes and sc-monitor
                          .andThen(this.oamWebServer.startListener()) // Start oam web server, for mediator notifications and pm metrics scraping
                          .andThen(this.cm.getNotificationHandler() //
                                          .start(oamWebServer)) // Start receiving CM configuration change updates
                          .andThen(waitForConfigPresence) // Wait until an valid initial CM configuration is received
                          .andThen(BsfCassandraConfigBuilder //
                                                            .trafficConfigProbe(params.getDatabaseConfiguration(),
                                                                                params.getMetricsConfigurator(),
                                                                                IF_PARAMS.wcdbcdClientCertPath,
                                                                                IF_PARAMS.sipTlsRootCaPath)
                                                            .buildSession()
                                                            .testInitialConnection()) // Ensure Cassandra can be initialized, by using an
                                                                                      // auxiliary session
                          .andThen(BsfSchemaHandler.verifyBsfDb(this.rxSession, //
                                                                this.params.getDatabaseConfiguration() //
                                                                           .getKeyspace())) // Initialize Cassandra again and verify DB schema
                          .andThen(this.dbUserHandler.ensureBsfUserConfigured()) // Verify user permissions in DB
                          .andThen(this.cassandraDbMetrics.start()) // Initialize DB metrics
                          .andThen(this.nbsfManagementService.init()) // Initialize NbsfManagement service
                          .andThen(this.pcfDbWatcher.initialize()) // Start watching for etcd database changes
                          .andThen(controller // Start AAA service and wait until JVM is ordered to shut down
                                             .ambWith(this.nbsfManagementService.run()) // Supervise for unexpected errors
                          )
                          .onErrorResumeNext(throwable -> stop().andThen(Completable.error(throwable))) // try to stop gracefully
                          // even on error
                          .andThen(stop());
    }

    private Completable stop()

    {
        final Predicate<? super Throwable> logErr = t ->
        {
            log.warn("Ignored Exception during shutdown", t);
            return true;
        };
        return Completable.complete()
                          .doOnComplete(() -> this.kubeProbe.setReady(false)) // Report not ready to k8s
                                                                              // probes
                          .andThen(diaClient.shutdown().doOnSubscribe(disp -> log.info("Diameter proxy gRPC client shutting down")).onErrorComplete())
                          .andThen(this.cm.getNotificationHandler().stop().onErrorComplete(logErr))
                          .andThen(this.cassandraDbMetrics.stop())
                          .andThen(this.oamWebServer.stopListener())
                          .andThen(this.webClientProvider.close().onErrorComplete(logErr))
                          .andThen(this.nbsfManagementService.stop())
                          .andThen(this.rxSession.close().onErrorComplete(logErr))
                          .andThen(this.pcfDbWatcher.terminate().onErrorComplete(logErr))
                          .andThen(this.rxEtcd.close().onErrorComplete(logErr))
                          .andThen(this.vertx.rxClose().onErrorComplete(logErr)) // This should be performed last
                          .onErrorComplete(logErr);
    }

    public static void main(String[] args)
    {
        var exitStatus = 1;

        log.info("Starting bsf-diameter, version: {}", VersionInfo.get());

        try (var shutdownHook = new RxShutdownHook();
             var llcbsfdiameter = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(),
                                                      BSFDIAMETER_CONTAINER_NAME))
        {
            final var bsfDiameter = new BsfDiameter(shutdownHook, BsfDiameterParams.fromEnvironment());

            bsfDiameter.run().blockingAwait();
            log.info("bsf-diameter terminated normally");
            exitStatus = 0;
        }
        catch (Exception e)
        {
            log.error("bsf-diameter terminated abnormally due to exception", e);
            exitStatus = 1;
        }

        System.exit(exitStatus);

    }

    /**
     * Transform CM Configuration to BSF Diameter configuration
     *
     * @param bsfCfg a BSF configuration object
     * @return The BSF Diameter configuration
     */
    private static Optional<BsfDiameterCfg> cmConfigToBsfConfig(Optional<EricssonBsf> bsfCfg)
    {
        final var oNfInstance = bsfCfg.flatMap(CmConfigurationUtil::getNfInstance);
        final var oNfDiscovery = oNfInstance //
                                            .flatMap(nfi -> Optional.ofNullable(nfi.getNrfService()))
                                            .flatMap(nfService -> Optional.ofNullable(nfService.getNfDiscovery()));
        final var failoverEnabled = oNfDiscovery.isPresent(); // Presence of "NfDiscovery" service enables the failover
                                                              // feature

        return oNfInstance.flatMap(nfInstance -> CmConfigurationUtil //
                                                                    .getDiameterRouting(nfInstance) //
                                                                    .map(dr -> BsfDiameterCfg.create(//
                                                                                                     CmConfigurationUtil.getFallbackDestHostDefRoute(dr),
                                                                                                     CmConfigurationUtil.getFallbackDestRealmDefRoute(dr), //
                                                                                                     failoverEnabled, //
                                                                                                     nfInstance.getName(),
                                                                                                     CmConfigurationUtil.getStaticDestinationHost(dr),
                                                                                                     CmConfigurationUtil.getStaticDestinationRealm(dr),
                                                                                                     CmConfigurationUtil.getStatusCodeMatchCondition(dr))));
    }

}
