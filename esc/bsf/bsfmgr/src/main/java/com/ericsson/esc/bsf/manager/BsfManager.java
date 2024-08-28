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
 * Created on: Oct 5, 2018
 *     Author: eedstl
 */

package com.ericsson.esc.bsf.manager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.AcmConfigHandler;
import com.ericsson.adpal.cm.CmAdapter;
import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.adpal.cm.PatchItem;
import com.ericsson.adpal.cm.PatchOperation;
import com.ericsson.adpal.cm.actions.ActionImplementer;
import com.ericsson.adpal.cm.actions.ActionSpec;
import com.ericsson.adpal.cm.state.RoutingParameter;
import com.ericsson.adpal.cm.state.StateDataInput;
import com.ericsson.adpal.cm.state.StateDataProvider;
import com.ericsson.adpal.ext.monitor.MonitorAdapter;
import com.ericsson.adpal.ext.monitor.MonitorAdapter.CommandTestAlarm;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.cnal.common.CertificateObserver;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.esc.bsf.db.BsfCassandraConfigBuilder;
import com.ericsson.esc.bsf.db.FullTableScanParameters;
import com.ericsson.esc.bsf.helper.BsfHelper;
import com.ericsson.esc.bsf.manager.statedataprovider.BsfNfDiscoveryStateDataHandler;
import com.ericsson.esc.bsf.manager.validator.BsfValidator;
import com.ericsson.esc.bsf.worker.BindingCleanupManager;
import com.ericsson.esc.bsf.worker.CmConfigurationUtil;
import com.ericsson.sc.bsf.etcd.PcfRtCachedServiceImpl;
import com.ericsson.sc.bsf.etcd.PcfRtDbEtcd;
import com.ericsson.sc.bsf.etcd.PcfRtService;
import com.ericsson.sc.bsf.etcd.PcfRtServiceImpl;
import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.ericsson.sc.bsf.model.DnsProfile;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.bsf.model.EricssonBsfBsfFunction;
import com.ericsson.sc.bsf.model.NfInstance;
import com.ericsson.sc.bsf.model.PcfRecoveryTime;
import com.ericsson.sc.bsf.model.glue.NfFunction;
import com.ericsson.sc.common.alarm.AlarmHandler;
import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;
import com.ericsson.sc.common.alarm.IfAlarmHandler;
import com.ericsson.sc.common.alarm.UnresolvableHostsAlarmHandler;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.fm.FmAlarmHandler;
import com.ericsson.sc.fm.FmAlarmServiceImpl;
import com.ericsson.sc.nfm.model.ServiceName;
import com.ericsson.sc.nfm.model.ServiceVersion;
import com.ericsson.sc.nrf.r17.ConfigComparators;
import com.ericsson.sc.nrf.r17.NrfDnsCache;
import com.ericsson.sc.pm.ScPmbrConfigHandler;
import com.ericsson.sc.rxetcd.JsonValueSerializer;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.RxLeaderElection;
import com.ericsson.sc.rxetcd.RxLeaderElection.LeaderStatus;
import com.ericsson.sc.rxkms.KmsClientUtilities;
import com.ericsson.sc.rxkms.KmsParameters;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.sc.util.tls.TlsKeylogger;
import com.ericsson.sc.utilities.dns.DnsCache;
import com.ericsson.sc.utilities.dns.IfDnsLookupContext;
import com.ericsson.utilities.cassandra.RxSession;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.KubeProbe;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.json.Json;
import com.ericsson.utilities.json.Json.Patch;
import com.ericsson.utilities.json.Smile;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.metrics.MetricRegister;
import com.ericsson.utilities.reactivex.RetryFunction;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.google.protobuf.ByteString;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.ClientBuilder;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.config.MeterFilter;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.micrometer.backends.BackendRegistries;
import io.vertx.reactivex.core.Vertx;

/**
 * Main BsfManager class, responsible for coordinating BSF management functions
 */
public class BsfManager
{
    private static class ConfigContext extends ConfigComparators.ChangeFlags
    {
        public static ConfigContext empty()
        {
            return new ConfigContext(Optional.empty(), Optional.of(List.of()));
        }

        public static ConfigContext of(final Optional<EricssonBsf> config)
        {
            return new ConfigContext(config, Optional.empty());
        }

        public static ConfigContext of(final Optional<EricssonBsf> config,
                                       final Optional<List<Json.Patch>> diff)
        {
            return new ConfigContext(config, diff);
        }

        private final Optional<EricssonBsf> config;

        private ConfigContext(final Optional<EricssonBsf> config,
                              final Optional<List<Json.Patch>> diff)
        {
            super(diff, Flags.F_ALL, Flags.F_NNRF_DISC_BSF, Flags.F_NNRF_NFM, Flags.F_NNRF_NFM_NRF_GROUP_INST_ID);

            this.config = config;
        }

        public Optional<EricssonBsf> getConfig()
        {
            return this.config;
        }

        public boolean isChangedAll()
        {
            return this.changeFlags.get(Flags.F_ALL);
        }

        public boolean isChangedNnrfDiscBsf()
        {
            return this.changeFlags.get(Flags.F_NNRF_DISC_BSF);
        }

        public boolean isChangedNnrfNfm()
        {
            return this.changeFlags.get(Flags.F_NNRF_NFM);
        }

        public boolean isChangedNnrfNfmNrfGroupInstId()
        {
            return this.changeFlags.get(Flags.F_NNRF_NFM_NRF_GROUP_INST_ID);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(BsfManager.class);
    private static final String SCHEMA_ERICSSON_BSF = "ericsson-bsf";

    // BSF manager Leader election related parameters
    private static final String LEADER_KEY = "/ericsson-sc/bsf/manager";
    private static final int LEADER_TTL = 13;
    private static final int RENEW_INTERVAL = 4;
    private static final int CLAIM_INTERVAL = 3;
    private static final int RECOVERY_DELAY = 12;
    private static final float REQUEST_LATENCY = 0.5f;
    private static final long RETRIES_FOR_LEADER_ELECTION = 10l;
    private static final int ETCD_REQUEST_TIMEOUT = 2;

    private static final String DEFAULT_ROUTE_ADDRESS = "[::]";

    private static final String JP_NF_INSTANCE_ID = "/nf-instance-id";
    private static final String KEY_BSF_FUNCTION = "ericsson-bsf:bsf-function";
    private static final String ENV_POD_NAME = "POD_NAME";
    private static final BsfManagerInterfacesParameters IF_PARAMS = BsfManagerInterfacesParameters.instance;
    private static final KmsClientUtilities kmsCLient = KmsClientUtilities.get(KmsParameters.instance, "eric-cm-key-role");

    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/manager/config/logcontrol").getPath();
    private static final String BSFMANAGER_CONTAINER_NAME = EnvVars.get("BSFMANAGER_CONTAINER_NAME");

    private static final String LEADER_ELECTION_STATE_FILE = "/leaderElection/isContender";
    private static final String LAST_UPDATE = "last-update";
    private static final String SDP_REGISTRATION_URI = URI.create("/cm/api/v1/schemas/ericsson-bsf/data-sources/").getPath();
    private static final String YANG_PATH_STATE_DATA = URI.create("/ericsson-bsf:bsf-function/nf-instance/bsf-service/nf-pool/nf-pool-discovery/last-update")
                                                          .getPath();

    private static final JsonValueSerializer<String, String> etcdSerializer = new JsonValueSerializer<>("",
                                                                                                        String.class,
                                                                                                        Smile.om(),
                                                                                                        (String key) -> ByteString.copyFrom(key,
                                                                                                                                            StandardCharsets.UTF_8),
                                                                                                        key -> new String(key, StandardCharsets.UTF_8));

    private final Vertx vertx;
    private final CertificateObserver certificateObserver;
    private final WebServer probeWebServer;
    private final RouterHandler oamWebServer;
    private final CmAdapter<EricssonBsf> cm;
    private final BehaviorSubject<ConfigContext> configFlow;
    private final NfFunction bsfFunction;
    private final MonitorAdapter monitored;
    private final LoadMeter loadMeter;
    private final DbStorageUtilizationSupervisor dbStorageUtilizationSupervisor;
    private final CmmPatch cmPatch;
    private final RxEtcd rxEtcd;
    private final RxLeaderElection election;
    private final RxSession cassandraDb;
    private final RxShutdownHook shutdownHook;
    private final BsfManagerParameters params;
    private final boolean enableLeaderElection;
    private final boolean bsfDiameterEnabled;
    private final KubeProbe kubeProbe;
    private final BsfValidator bsfValidator;
    private final String ownId;
    private final CoreV1Api api;
    private final ApiClient client;
    private final ActionImplementer actionImplementer;
    private final InitializeDbActionHandler initializeDbActionHandler;
    private final CheckDbSchemaActionHandler checkDbSchemaActionHandler;
    private final UpdateDbTopologyActionHandler updateDbTopologyActionHandler;
    private final CheckScanStatusActionHandler checkScanStatusActionHandler;
    private final ScPmbrConfigHandler pmbrCfgHandler;
    private final WebClientProvider webClientProvider;
    private final PcfDiscoverer pcfDiscoverer;
    private final Optional<PcfDbUpdater> pcfDbUpdater;
    private final Optional<PcfRtManager> pcfRtManager;
    private final Optional<TlsKeylogger> tlsKeyLogger;
    private final Optional<FullTableScanManager> fullTableScanManager;
    private final Optional<PcfRtService> pcfRtServiceCached;
    private final WebClientProvider alarmHandlerClient;
    private final IfAlarmHandler ah;
    private final UnresolvableHostsAlarmHandler unresolvableHostsAh;

    public BsfManager(BsfManagerParameters params,
                      RxShutdownHook shutdownHook) throws IOException
    {
        this.certificateObserver = new CertificateObserver(IF_PARAMS.trafficCertPath);
        this.ownId = EnvVars.get(ENV_POD_NAME);
        this.tlsKeyLogger = TlsKeylogger.fromEnvVars();
        this.vertx = VertxInstance.get();
        this.shutdownHook = shutdownHook;
        this.bsfValidator = new BsfValidator(IF_PARAMS.schemaName);
        this.params = params;
        this.enableLeaderElection = params.isEnableLeaderElection();
        this.bsfDiameterEnabled = params.isBsfDiameterEnabled();

        // create client for fault indications to alarm handler
        final var ahClient = WebClientProvider.builder().withHostName(IF_PARAMS.serviceHostname);
        if (IF_PARAMS.globalTlsEnabled)
            ahClient.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(IF_PARAMS.alarmHandlerClientCertPath), //
                                                                 SipTlsCertWatch.trustedCert(IF_PARAMS.sipTlsRootCaPath)));
        this.alarmHandlerClient = ahClient.build(this.vertx);

        // create alarm handler for requests to alarm handler service
        var fmAlarmHander = new FmAlarmHandler(this.alarmHandlerClient, // web client to be used for alarm raise/cease
                                               IF_PARAMS.alarmHandlerHostName, // alarm handler service server hostname
                                               IF_PARAMS.alarmHandlerPort, // alarm handler service server port
                                               IF_PARAMS.globalTlsEnabled); // indication if tls is enabled

        // create alarm service for updating the alarm through alarm handler service
        var fmAlarmService = new FmAlarmServiceImpl(fmAlarmHander);

        final var wcb = WebClientProvider.builder().withHostName(IF_PARAMS.serviceHostname);
        if (IF_PARAMS.globalTlsEnabled)
            wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(IF_PARAMS.mediatorClientCertPath),
                                                            SipTlsCertWatch.trustedCert(IF_PARAMS.sipTlsRootCaPath)));
        this.webClientProvider = wcb.build(this.vertx);

        this.cm = new CmAdapter<>(EricssonBsf.class,
                                  SCHEMA_ERICSSON_BSF,
                                  vertx,
                                  IF_PARAMS.mediatorPort,
                                  this.params.getCmMediatorHost(),
                                  webClientProvider,
                                  IF_PARAMS.globalTlsEnabled,
                                  IF_PARAMS.subscribeValidity,
                                  IF_PARAMS.subscribeRenewal,
                                  IF_PARAMS.subscribeHeartbeat);

        this.cmPatch = new CmmPatch(IF_PARAMS.mediatorPort, this.params.getCmMediatorHost(), webClientProvider, IF_PARAMS.globalTlsEnabled);

        this.configFlow = BehaviorSubject.<ConfigContext>create();
        this.cm.getNotificationHandler() //
               .getConfiguration() //
               .subscribeOn(Schedulers.io())
               .scan(ConfigContext.empty(),
                     (prev,
                      curr) ->
                     {
                         log.info("Processing config update.");

                         if (prev.getConfig().isPresent() && curr.isPresent())
                         {
                             final List<Patch> diffs = Json.diff(prev.getConfig().get(), curr.get());
                             log.debug("#diffs={}, diffs={}", diffs.size(), diffs);
                             return ConfigContext.of(curr, Optional.of(diffs));
                         }

                         if (prev.getConfig().isEmpty() && curr.isEmpty())
                             return ConfigContext.empty();

                         return ConfigContext.of(curr);
                     })
               .filter(ConfigContext::isChangedAll)
               .subscribe(this.configFlow);

        this.probeWebServer = WebServer.builder() //
                                       .withHost(DEFAULT_ROUTE_ADDRESS)
                                       .withPort(IF_PARAMS.probeServerPort)
                                       .build(this.vertx);

        // create web server for mediator notifications, yang-provider validations and
        // pm-server metrics scraping
        var iws = WebServer.builder().withHost(DEFAULT_ROUTE_ADDRESS).withPort(IF_PARAMS.oamServerPort);
        if (IF_PARAMS.globalTlsEnabled)
        {
            var oamTls = DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(IF_PARAMS.oamServerCertPath), // scp manager server certificate
                                                      SipTlsCertWatch.combine( // mediator server ca for verification of client certificates during
                                                                               // notifications
                                                                              SipTlsCertWatch.trustedCert(IF_PARAMS.mediatorServerCaPath),
                                                                              // yang provider server ca for verification of client certificates during
                                                                              // validation
                                                                              SipTlsCertWatch.trustedCert(IF_PARAMS.yangServerCaPath),
                                                                              // pm server ca for verification of client certificates during scraping of
                                                                              // metrics
                                                                              SipTlsCertWatch.trustedCert(IF_PARAMS.pmServerCaPath)));

            iws.withDynamicTls(oamTls);
        }
        this.oamWebServer = iws.build(this.vertx);

        // Add custom tags to all metrics
        BackendRegistries.getDefaultNow()
                         .config() //
                         .meterFilter(MeterFilter.commonTags(Tags.of("nf", "bsf")));
        PmAdapter.configureMetricsHandler(this.oamWebServer, true);
        this.pmbrCfgHandler = new ScPmbrConfigHandler(this.cmPatch);

        this.cassandraDb = BsfCassandraConfigBuilder.adminConfig(this.params.getDatabaseConfiguration(),
                                                                 IF_PARAMS.wcdbcdClientCertPath,
                                                                 IF_PARAMS.sipTlsRootCaPath)
                                                    .buildSession();

        final var edb = RxEtcd.newBuilder()
                              .withEndpoint(EnvVars.get("ETCD_ENDPOINT"))
                              .withConnectionRetries(10)
                              .withRequestTimeout(ETCD_REQUEST_TIMEOUT, TimeUnit.SECONDS);
        if (IF_PARAMS.dcedTlsEnabled)
            edb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(IF_PARAMS.dcedClientCertPath), //
                                                            SipTlsCertWatch.trustedCert(IF_PARAMS.sipTlsRootCaPath)));
        else
            edb.withUser(EnvVars.get("ETCD_USERNAME")).withPassword(EnvVars.get("ETCD_PASSWORD"));

        this.rxEtcd = edb.build();

        // Configure internal web server to answer to k8s readiness/liveness probes
        this.kubeProbe = KubeProbe.Handler.singleton().configure(this.probeWebServer).register(KubeProbe.of().setAlive(true).setReady(false));
        this.loadMeter = new LoadMeter(this.vertx, this.cm.getNotificationHandler().getConfiguration());
        this.dbStorageUtilizationSupervisor = new DbStorageUtilizationSupervisor(this.vertx,
                                                                                 PmServerParameters.fromEnvironment(),
                                                                                 this.cassandraDb,
                                                                                 this.params.getDatabaseConfiguration());
        this.ah = AlarmHandler.of(fmAlarmService);

        Alarm.Context alarmCtx = Alarm.Context.of(ah, "Bsf", SCHEMA_ERICSSON_BSF);
        this.unresolvableHostsAh = new UnresolvableHostsAlarmHandler(alarmCtx);

        this.bsfFunction = new NfFunction(alarmCtx, this.loadMeter, this.certificateObserver.getSecrets(), new Rdn("nf", "bsf-function"));

        this.election = new RxLeaderElection.Builder(this.rxEtcd, ownId, LEADER_KEY).leaderInterval(LEADER_TTL)
                                                                                    .renewInterval(RENEW_INTERVAL)
                                                                                    .claimInterval(CLAIM_INTERVAL)
                                                                                    .recoveryDelay(RECOVERY_DELAY)
                                                                                    .requestLatency(REQUEST_LATENCY)
                                                                                    .retries(RETRIES_FOR_LEADER_ELECTION)
                                                                                    .build()
                                                                                    .blockingGet();

        this.initializeDbActionHandler = new InitializeDbActionHandler(SCHEMA_ERICSSON_BSF, //
                                                                       this.cassandraDb, //
                                                                       this.params.getDatabaseConfiguration(), //
                                                                       fmAlarmService);
        final var actionInitializeDb = new ActionSpec(initializeDbActionHandler::executeAction,
                                                      "ericsson-bsf:bsf-function::nf-instance::bsf-service::binding-database::initialize-db");
        this.checkDbSchemaActionHandler = new CheckDbSchemaActionHandler(this.cassandraDb, this.params.getDatabaseConfiguration());
        final var actionCheckDbSchema = new ActionSpec(checkDbSchemaActionHandler::executeAction,
                                                       "ericsson-bsf:bsf-function::nf-instance::bsf-service::binding-database::check-db-schema");
        this.updateDbTopologyActionHandler = new UpdateDbTopologyActionHandler(this.cassandraDb, this.params.getDatabaseConfiguration());
        final var actionUpdateDbTopology = new ActionSpec(updateDbTopologyActionHandler::executeAction,
                                                          "ericsson-bsf:bsf-function::nf-instance::bsf-service::binding-database::update-db-topology");

        this.pcfDiscoverer = new PcfDiscoverer(this.bsfFunction,
                                               this.configFlow.filter(ConfigContext::isChangedNnrfDiscBsf)
                                                              .map(ConfigContext::getConfig)
                                                              .toFlowable(BackpressureStrategy.BUFFER));

        this.pcfDbUpdater = this.bsfDiameterEnabled ? Optional.of(new PcfDbUpdater(this.rxEtcd,
                                                                                   this.pcfDiscoverer.getDiscoveredPcfNfs(),
                                                                                   params.getPcfRtTTLsec()))
                                                    : Optional.empty();

        this.pcfRtServiceCached = params.getCheckRT() ? Optional.of(new PcfRtCachedServiceImpl(this.rxEtcd, params.getPcfRtTTLsec())) //
                                                      : Optional.empty();

        this.pcfRtManager = this.pcfRtServiceCached.map(pcfRtServ -> new PcfRtManager(new PcfRtDbUpdater(pcfRtServ, this.pcfDiscoverer.getDiscoveredPcfRts())));

        this.fullTableScanManager = pcfRtServiceCached.map(pcfRtServ ->
        {
            final var fullTableScanParameters = new FullTableScanParameters.Builder().setPageSize(Integer.parseInt(EnvVars.get("PAGE_SIZE")))
                                                                                     .setPageThrottlingMillis(Long.parseLong(EnvVars.get("PAGE_THROTTLING_MILLIS")))
                                                                                     .setDeleteThrottlingMillis(Long.parseLong(EnvVars.get("DELETE_THROTTLING_MILLIS")))
                                                                                     .build();
            final var nfInstanceFlow = this.cm.getNotificationHandler()
                                              .getConfiguration()
                                              .doOnNext(cfg -> log.debug("Processing changes received from CM Mediator"))
                                              .map(bsfCfg -> bsfCfg.flatMap(cfg -> CmConfigurationUtil.getNfInstance(cfg).map(NfInstance::getName)))
                                              .distinctUntilChanged() // Only emit changes
                                              .doOnNext(nfInstanceName -> log.info("BSF nfInstance changed: {}", nfInstanceName))
                                              .replay(1)
                                              .refCount()
                                              .filter(Optional::isPresent)
                                              .map(Optional::get)
                                              .toFlowable(BackpressureStrategy.LATEST);

            final var cleanupManager = new BindingCleanupManager(this.cassandraDb, this.params.getDatabaseConfiguration().getKeyspace(), nfInstanceFlow);

            final var scanConfig = this.cm.getNotificationHandler()
                                          .getConfiguration()
                                          .doOnNext(cfg -> log.debug("Processing CM config {}", cfg))
                                          .map(cfg -> CmConfigurationUtil.getBsfService(cfg).map(CmConfigurationUtil::getPcfRecoveryTime))
                                          .map(pcfRtCfg -> pcfRtCfg.map(PcfRecoveryTime::getBindingDatabaseScan))// Read BindingDatabaseScan config
                                          .map(dbScanCfg -> dbScanCfg.orElse(new BindingDatabaseScan().withConfiguration(BindingDatabaseScan.Configuration.DISABLED)))
                                          .toFlowable(BackpressureStrategy.LATEST);

            return new FullTableScanManager(pcfRtServ, fullTableScanParameters, cleanupManager, scanConfig);
        });

        this.checkScanStatusActionHandler = new CheckScanStatusActionHandler(this.fullTableScanManager);
        final var checkScanStatus = new ActionSpec(checkScanStatusActionHandler::executeAction,
                                                   "ericsson-bsf:bsf-function::nf-instance::bsf-service::pcf-recovery-time::binding-database-scan::check-scan-status");

        this.actionImplementer = new ActionImplementer(SCHEMA_ERICSSON_BSF,
                                                       this.oamWebServer,
                                                       IF_PARAMS.oamServerUri,
                                                       List.of(actionInitializeDb, actionCheckDbSchema, actionUpdateDbTopology, checkScanStatus),
                                                       this.params.getCmMediatorHost(),
                                                       IF_PARAMS.mediatorPort,
                                                       webClientProvider);

        final var getCommands = new ArrayList<>(List.of(new CommandConfig(this.cm.getNotificationHandler().getConfiguration()),
                                                        new MonitorAdapter.CommandCounter()));

        final var putCommands = new ArrayList<MonitorAdapter.CommandBase>(List.of(new CommandTestAlarm(SCHEMA_ERICSSON_BSF,
                                                                                                       fmAlarmService,
                                                                                                       NFType.BSF,
                                                                                                       this.params.getHostname())));

        pcfRtServiceCached.ifPresent(srv ->
        {
            // Create an "uncached" service that allows directly querying the etcd RT
            // database
            final var pcfRtServiceUncached = new PcfRtServiceImpl(rxEtcd, params.getPcfRtTTLsec());
            final var pcfRtDbEtcd = pcfRtServiceUncached.getEtcd();
            getCommands.add(new CommandDumpPcfRtDbEtcd(pcfRtServiceUncached, srv));
            putCommands.add(new CommandTruncatePcfRtDbEtcd(pcfRtDbEtcd));
        });

        this.monitored = new MonitorAdapter(this.probeWebServer, getCommands, putCommands);

        this.client = ClientBuilder.standard().build();
        Configuration.setDefaultApiClient(client);
        this.api = new CoreV1Api();

        final var dataHandler = new BsfNfDiscoveryStateDataHandler(this.rxEtcd, BsfManager.etcdSerializer);

        final var input = new StateDataInput(dataHandler, RoutingParameter.bsfLastUpdate);
        final var inputList = new ArrayList<StateDataInput>();
        inputList.add(input);

        final var sdp = new StateDataProvider(inputList);
        sdp.configureStateDataHandler(this.oamWebServer);
        log.info("State Data Provider has been initialized");
    }

    /**
     * @return A {@code Completable} that never completes, unless BSF manager has
     *         been gracefully terminated
     */
    public Completable run()
    {
        final Function<Pair<Pair<ConfigContext, ConfigContext>, Pair<ConfigContext, ConfigContext>>, Completable> updateNfFunction = pair ->
        {
            final Optional<EricssonBsf> config = pair.getSecond().getSecond().getConfig();

            log.info("updateNfFunction={}", log.isDebugEnabled() ? config : "<config not printed on info level>");

            // DNS related change when this function was called because of an update from
            // the DNS cache (then the previous and current configuration are the same).
            // User related change when there was change related to NF management.
            final boolean dnsRelated = pair.getFirst() == pair.getSecond();
            final boolean userRelated = !dnsRelated && pair.getSecond().getSecond().isChangedNnrfNfm();
            final boolean instIdRelated = !dnsRelated && pair.getSecond().getSecond().isChangedNnrfNfmNrfGroupInstId();
            log.info("userRelated={}, dnsRelated={}, instIdRelated={}", userRelated, dnsRelated, instIdRelated);

            try
            {
                if (config.isPresent())
                {
                    final EricssonBsfBsfFunction curr = config.get().getEricssonBsfBsfFunction();

                    log.debug("curr={}", curr);

                    if (curr != null)
                    {
                        // Config is empty -> update.
                        if (curr.getNfInstance() == null || curr.getNfInstance().isEmpty())
                        {
                            this.bsfFunction.update(curr);
                            return Completable.complete();
                        }

                        // Only nfInstanceId of NRF group is changed, which was initiated by ourselves
                        // -> ignore.
                        if (instIdRelated && !userRelated)
                            return Completable.complete();

                        // Take a copy of curr. This is needed as curr should remain unchanged; the copy
                        // is used for modifications and will be used for further processing.
                        final EricssonBsfBsfFunction copy = Json.copy(curr, EricssonBsfBsfFunction.class);

                        // In the copy, set defaults in the NfProfile of all NfInstances that shall be
                        // used for the registration with the NRF.

                        copy.getNfInstance().forEach(instance ->
                        {
                            if (instance.getNfProfile() != null)
                                instance.getNfProfile().forEach(profile ->
                                {
                                    if (profile.getNfService() != null)
                                        profile.getNfService().forEach(service ->
                                        {
                                            if (service.getServiceName().equals(ServiceName.NBSF_MANAGEMENT))
                                            {
                                                // Set supported versions and supportedFeatures for R16:

                                                service.setServiceVersion(List.of(new ServiceVersion().withApiVersionInUri("v1").withApiFullVersion("1.3.1")));

                                                // From TS 29.521, 5.8 Feature negotiation:
                                                // # Feature Name
                                                // 1 MultiUeAddr
                                                // 2 BindingUpdate
                                                // 3 SamePcf
                                                // This is encoded as string of nibbles [A-Fa-f0-9], with each of the four bits
                                                // of a nibble representing one service. Leading zeros can be omitted.
                                                // Examples:
                                                // "6" selects "SamePcf" and "BindingUpdate", "2" selects "BindingUpdate".

                                                if (service.getSupportedFeatures() == null)
                                                    service.setSupportedFeatures("1"); // Select only feature "MultiUeAddr".
                                            }
                                        });
                                });
                        });

                        // If the change is not DNS related:
                        // Calling update(copy) below may change copy, due to the update of the
                        // NF-instance ID of the NRF-groups. The delta of copy and curr reflects that
                        // change (only changed NF-instance IDs are considered) and can then be used for
                        // patching the configuration in CMM.
                        this.bsfFunction.update(copy, userRelated, dnsRelated);

                        if (dnsRelated)
                        {
                            log.info("DNS related update, configuration unchanged.");
                            return Completable.complete();
                        }

                        final List<PatchItem> patches = ConfigComparators.diffNrfGroupsNfInstanceId("/ericsson-bsf:bsf-function", curr, copy)
                                                                         .stream()
                                                                         .map(p -> new PatchItem(PatchOperation.fromValue(p.getOp().getValue()),
                                                                                                 p.getPath(),
                                                                                                 p.getFrom(),
                                                                                                 p.getValue()))
                                                                         .toList();

                        log.info("updateNfFunction: #patches={}, patches={}", patches.size(), log.isDebugEnabled() ? patches : "<not printed on info level>");

                        if (patches.isEmpty())
                            return Completable.complete();

                        return this.cmPatch.patch("/cm/api/v1/configurations/ericsson-bsf", patches)
                                           .doOnComplete(() -> log.info("Updated nfInstanceIds in configuration ericsson-bsf."))
                                           .doOnError(e -> log.warn("Could not update nfInstanceIds in configuration ericsson-bsf. Cause: {}", e.toString()))
                                           .onErrorComplete();
                    }
                }

                this.bsfFunction.stop();
            }
            catch (final Exception t)
            {
                log.warn("Ignoring new configuration. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(t, log.isDebugEnabled()));
            }

            return Completable.complete();
        };

        final var waitUntilLeader = this.enableLeaderElection ? updateLeaderElectionState(LeaderStatus.CONTENDER).andThen((this.election.leaderStatusUpdates() //
                                                                                                                                        .filter(status -> status.equals(LeaderStatus.LEADER))
                                                                                                                                        .firstOrError()
                                                                                                                                        .ignoreElement()
                                                                                                                                        .doOnSubscribe(disp -> log.info("Waiting to become leader"))
                                                                                                                                        .doOnComplete(() -> log.info("I am the leader"))))
                                                              : Completable.complete()

        ;
        final var leadershipLost = this.enableLeaderElection ? this.election.leaderStatusUpdates()
                                                                            .filter(status -> status.equals(LeaderStatus.CONTENDER))
                                                                            .firstOrError()
                                                                            .ignoreElement()
                                                                            .doOnComplete(() -> log.error("Lost leadership, shutting down"))
                                                                            .andThen(this.updateLeaderElectionState(LeaderStatus.CONTENDER))
                                                             : Completable.never();

        final Consumer<Optional<EricssonBsf>> registerAllUnresolvedNrfFqdnsWithDnsCache = o ->
        {
            final Set<IfDnsLookupContext> unresolvedNrfFqdns = new HashSet<>();

            o.ifPresent(config -> Optional.ofNullable(config.getEricssonBsfBsfFunction())
                                          .ifPresent(function -> unresolvedNrfFqdns.addAll(function.getNfInstance()
                                                                                                   .stream()
                                                                                                   .flatMap(inst -> inst.getNrfGroup()
                                                                                                                        .stream()
                                                                                                                        .flatMap(group -> group.getNrf()
                                                                                                                                               .stream()
                                                                                                                                               .filter(nrf -> nrf.getFqdn() != null
                                                                                                                                                              && !nrf.getFqdn()
                                                                                                                                                                     .isEmpty()
                                                                                                                                                              && (nrf.getIpEndpoint()
                                                                                                                                                                     .isEmpty()
                                                                                                                                                                  || nrf.getIpEndpoint()
                                                                                                                                                                        .stream()
                                                                                                                                                                        .allMatch(ep -> (ep.getIpv4Address() == null
                                                                                                                                                                                         || ep.getIpv4Address()
                                                                                                                                                                                              .isEmpty())
                                                                                                                                                                                        && (ep.getIpv6Address() == null
                                                                                                                                                                                            || ep.getIpv6Address()
                                                                                                                                                                                                 .isEmpty()))))
                                                                                                                                               .map(nrf ->
                                                                                                                                               {
                                                                                                                                                   if (group.getDnsProfileRef() != null)
                                                                                                                                                   {
                                                                                                                                                       final DnsProfile dnsProfile = Utils.getByName(inst.getDnsProfile(),
                                                                                                                                                                                                     group.getDnsProfileRef());

                                                                                                                                                       if (dnsProfile != null)
                                                                                                                                                       {
                                                                                                                                                           return DnsCache.LookupContext.of(nrf.getFqdn(),
                                                                                                                                                                                            dnsProfile.getIpFamilyResolution()
                                                                                                                                                                                                      .stream()
                                                                                                                                                                                                      .map(r -> com.ericsson.sc.utilities.dns.IpFamily.fromValue(r.value()))
                                                                                                                                                                                                      .collect(Collectors.toSet()));
                                                                                                                                                       }
                                                                                                                                                   }

                                                                                                                                                   return DnsCache.LookupContext.of(nrf.getFqdn(),
                                                                                                                                                                                    CommonConfigUtils.getDefaultIpFamilies(inst));
                                                                                                                                               })))
                                                                                                   .collect(Collectors.toSet()))));

            log.info("Found NRF FQDNs to be resolved: {}", unresolvedNrfFqdns);
            NrfDnsCache.singleton().publishHostsToResolve(unresolvedNrfFqdns);
        };

        final var managerChain = this.probeWebServer.startListener() // Start internal web server ASAP, so that k8s liveness probe does not fail
                                                    .andThen(this.rxEtcd.ready())
                                                    .andThen(this.certificateObserver.start())
                                                    .andThen(this.oamWebServer.startListener()) // Start oam web server
                                                    .andThen(this.cassandraDb.sessionHolder() //
                                                                             .ignoreElement()
                                                                             .doOnSubscribe(disp -> log.info("Waiting for Cassandra database to become ready"))
                                                                             .doOnComplete(() -> log.info("Cassandra database is ready")))
                                                    .doOnComplete(() -> this.kubeProbe.setReady(true)) // report ready to k8s readiness probes
                                                    .andThen(waitUntilLeader) // Wait until we are leader
                                                    .andThen(this.pmbrCfgHandler.createPmbrJobPatches())
                                                    .andThen(this.pmbrCfgHandler.createPmbrGroupPatches())
                                                    .andThen(this.bsfDiameterEnabled ? this.deployDefaultDiameterAdpConfiguration() : Completable.complete())
                                                    .andThen(this.deployDefaultAcmConfiguration())
                                                    .andThen(this.deployDefaultAcmGroupConfiguration())
                                                    .andThen(this.startValidator())
                                                    .andThen(this.ah.start())
                                                    .andThen(MetricRegister.singleton().start())
                                                    .andThen(this.loadMeter.start())
                                                    .andThen(this.unresolvableHostsAh.start())
                                                    .andThen(this.monitored.start().onErrorComplete())
                                                    .andThen(this.cm.getNotificationHandler().start(this.oamWebServer))
                                                    .andThen(this.actionImplementer.registerActionImplementer()) // Register BSF manager action implementer to
                                                    // CMM
                                                    .andThen(this.pcfRtManager.map(PcfRtManager::init).orElse(Completable.complete()))
                                                    .andThen(this.pcfDiscoverer.start())
                                                    .andThen(this.pcfRtServiceCached.map(PcfRtService::init).orElse(Completable.complete()))
                                                    .andThen(this.enableLeaderElection ? this.updateLeaderElectionState(LeaderStatus.LEADER)
                                                                                       : Completable.complete())
                                                    .andThen(this.registerStateDataProvider("bsf-nf-discovery-data-provider", YANG_PATH_STATE_DATA))
                                                    .andThen(Completable.ambArray(this.actionImplementer.run(), // Never completes
                                                                                  this.configFlow.subscribeOn(Schedulers.io())
                                                                                                 .map(ConfigContext::getConfig)
                                                                                                 .distinctUntilChanged()
                                                                                                 .doOnNext(registerAllUnresolvedNrfFqdnsWithDnsCache)
                                                                                                 .ignoreElements(),
                                                                                  Observable.combineLatest(this.configFlow.filter(ConfigContext::isChangedNnrfNfm)
                                                                                                                          .scan(Pair.of(ConfigContext.empty(),
                                                                                                                                        ConfigContext.empty()),
                                                                                                                                (r,
                                                                                                                                 o) -> Pair.of(r.getSecond(),
                                                                                                                                               o)),
                                                                                                           NrfDnsCache.singleton()
                                                                                                                      .getResolvedHosts()
                                                                                                                      .toObservable(),
                                                                                                           (config,
                                                                                                            dnsResults) -> config)
                                                                                            .scan(Pair.of(Pair.of(ConfigContext.empty(), ConfigContext.empty()),
                                                                                                          Pair.of(ConfigContext.empty(),
                                                                                                                  ConfigContext.empty())),
                                                                                                  (r,
                                                                                                   o) -> Pair.of(r.getSecond(), o))
                                                                                            .subscribeOn(Schedulers.io())
                                                                                            .flatMapCompletable(updateNfFunction),
                                                                                  leadershipLost, // Completes if leadership is lost
                                                                                  this.shutdownHook.get(), // Completes upon JVM shutdown indication
                                                                                  this.dbStorageUtilizationSupervisor.run(), // Completes upon unexpected
                                                                                  // termination of
                                                                                  // dbStorageUtilizationSupervisor
                                                                                  this.pcfDbUpdater.map(PcfDbUpdater::run).orElse(Completable.never()),
                                                                                  this.pcfRtManager.map(PcfRtManager::run).orElse(Completable.never()),
                                                                                  BsfHelper.monitorKms(kmsCLient),
                                                                                  this.pcfRtServiceCached.map(PcfRtService::run).orElse(Completable.never()),
                                                                                  this.fullTableScanManager.map(FullTableScanManager::run)
                                                                                                           .orElse(Completable.never()),
                                                                                  this.pcfDiscoverer.lastUpdateTimestampToEtcd(etcdSerializer,
                                                                                                                               this.rxEtcd,
                                                                                                                               LAST_UPDATE)))
                                                    .doOnComplete(() -> log.info("Graceful shutdown initiated"))
                                                    .doOnError(err -> log.error("Fatal error, shutting down", err))
                                                    .onErrorResumeNext(throwable -> this.stop().andThen(Completable.error(throwable)))
                                                    .andThen(this.stop());

        return (this.enableLeaderElection ? election.run() : Completable.never()) // Start leader election subsystem
                                                                                 .ambWith(managerChain); // Start the BSF manager master chain
    }

    /**
     * The registration of state data provider using the required json to
     * cm-mediator as a data-source
     * 
     * <p>
     * Required Json:
     * 
     * <pre>
     * {
     *     "endpoint": "https://eric-bsf-manager:8082 (service_name : oamPort)",
     *     "paths": [
     *        {
     *          "yangpath" : "/ericsson-bsf:bsf-function/nf-instance/bsf-service/nf-pool/nf-pool-discovery/last-update"
     *        }
     *      ]
     * }
     * </pre>
     * 
     * @param name the data-source that describes the data provider
     * @param path the yangpath that the state data will be tried to fetched
     * @return If registration is not successful completes with an error, otherwise
     *         registration has been achieved
     */
    private Completable registerStateDataProvider(final String name,
                                                  final String path)
    {
        final var endpoint = "endpoint";
        final var yangPath = "yangpath";
        final var paths = "paths";

        final var jsonRegistrationBody = new JsonObject();

        jsonRegistrationBody.put(endpoint, "https://" + IF_PARAMS.serviceHostname + ":" + IF_PARAMS.oamServerPort)
                            .put(paths, new JsonArray().add(new JsonObject().put(yangPath, path)));

        final var jsonEncodedAsString = jsonRegistrationBody.encodePrettily();
        log.info("The required json that is sent for state data provider is: {}", jsonEncodedAsString);

        return Completable.defer(() -> this.webClientProvider.getWebClient()
                                                             .flatMapCompletable(webClient -> webClient.put(IF_PARAMS.mediatorPort,
                                                                                                            this.params.getCmMediatorHost(),
                                                                                                            SDP_REGISTRATION_URI + name)
                                                                                                       .rxSendJsonObject(jsonRegistrationBody)
                                                                                                       .flatMapCompletable(resp -> (resp.statusCode() < 200
                                                                                                                                    || resp.statusCode() > 300) ? Completable.error(new IllegalArgumentException(String.format("Erroneous response from cm-mediator , status code: %d, body: %s", resp.statusCode(), resp.bodyAsString()))) : Completable.complete())
                                                                                                       .doOnComplete(() -> log.info("State Data Provider for {} was registered successfully",
                                                                                                                                    name))
                                                                                                       .doOnError(t -> log.error("Failed to register {}, with error {}",
                                                                                                                                 name,
                                                                                                                                 t)))
                                                             .retryWhen(new RetryFunction().withDelay(1 * 1000L).withRetries(5).create()));

    }

    /**
     * Update BSF manager POD label with leadership status, using k8s API Also, if
     * pod is contending in leader election create an empty contender file.
     * 
     * @param leaderStatus
     * @return A {@code Completable} that completes as soon as the BSF manager POD
     *         label has been updated
     */
    private Completable updateLeaderElectionState(LeaderStatus leaderStatus)
    {
        final var status = leaderStatus == LeaderStatus.LEADER ? "eric-bsf-manager-leader" : "eric-bsf-manager-contender";
        return Completable.fromAction(() ->
        {
            log.info("[ID: {}] leaderStatus: {}", ownId, status);
            try
            {
                // Update the leader election pod labels
                // This is a blocking operation
                this.api.patchNamespacedPod(EnvVars.get(ENV_POD_NAME),
                                            EnvVars.get("NAMESPACE"),
                                            new V1Patch("[{ \"op\": \"add\", \"path\": \"/metadata/labels/leader\", \"value\": \"" + status + "\" }]"),
                                            null,
                                            null,
                                            null,
                                            null,
                                            null);

                // If pod is contender then create the contender file otherwise delete it.
                if (leaderStatus == LeaderStatus.LEADER)
                    deleteContenderFile();
                else
                    createContenderFile();
            }
            catch (ApiException e)
            {
                log.error("Failed to update POD leadership label, responseBody: {}", e.getResponseBody(), e);
                // Ignore Failure
            }
            catch (Exception e)
            {
                log.error("Failed to update POD leadership status ", e);
                // Ignore failure
            }
        }).subscribeOn(Schedulers.io());
    }

    private void createContenderFile() throws IOException
    {
        File newFile = new File(LEADER_ELECTION_STATE_FILE);

        if (newFile.createNewFile())
            log.debug("Contender file: {} was created", LEADER_ELECTION_STATE_FILE);
        else
            log.debug("Contender file: {} already exists", LEADER_ELECTION_STATE_FILE);
    }

    private void deleteContenderFile() throws IOException
    {
        Path path = Paths.get(LEADER_ELECTION_STATE_FILE);

        if (Files.deleteIfExists(path))
            log.debug("Contender file: {} was deleted", LEADER_ELECTION_STATE_FILE);
        else
            log.debug("Contender file: {} does not exist", LEADER_ELECTION_STATE_FILE);
    }

    /**
     * 
     * @return A {@code Completable } that completes as soon as the BSF
     *         configuration validator has started. If CM validation is disabled, an
     *         empty Completable is returned.
     */
    private Completable startValidator()
    {
        // Validator is disabled for BSF. When enabled, Yang CA must be added in the
        // list of certificates
        if (IF_PARAMS.validatorEnabled)
        {
            log.debug("BSF Validator is enabled");
            return this.cm.getValidationHandler().start(this.oamWebServer, bsfValidator, SCHEMA_ERICSSON_BSF, IF_PARAMS.validatorName, IF_PARAMS.oamServerUri);
        }
        else
        {
            log.debug("BSF Validator is disabled");
            log.debug("Check if BSF Validator is already registered");

            if (this.cm.getValidationHandler().checkValidator(SCHEMA_ERICSSON_BSF, IF_PARAMS.validatorName, IF_PARAMS.oamServerUri))
                this.cm.getValidationHandler().deleteValidator(SCHEMA_ERICSSON_BSF, IF_PARAMS.validatorName, IF_PARAMS.oamServerUri);

            return Completable.complete();
        }
    }

    /**
     * Gracefully terminate BSF manager
     * 
     * @return A {@code Completable} that completes as soon as termination is
     *         finished, ignoring any errors
     */
    public Completable stop()
    {
        final Predicate<? super Throwable> logErr = t ->
        {
            log.warn("Ignored Exception during shutdown", t);
            return true;
        };

        return Completable.complete()
                          .andThen(this.certificateObserver.stop().onErrorComplete(logErr))
                          .andThen(MetricRegister.singleton().stop().onErrorComplete(logErr))
                          .andThen(this.loadMeter.stop().onErrorComplete(logErr))
                          .andThen(this.dbStorageUtilizationSupervisor.stop().onErrorComplete(logErr))
                          .andThen(this.monitored.stop().onErrorComplete(logErr))
                          .andThen(this.unresolvableHostsAh.stop().onErrorComplete(logErr))
                          .andThen(this.ah.stop().onErrorComplete(logErr))
                          .andThen(this.cm.getNotificationHandler().stop().onErrorComplete(logErr))
                          .andThen(this.oamWebServer.stopListener().onErrorComplete(logErr))
                          .andThen(this.probeWebServer.stopListener().onErrorComplete(logErr))
                          .andThen(this.cassandraDb.close().onErrorComplete(logErr))
                          .andThen(this.webClientProvider.close().onErrorComplete(logErr))
                          .andThen(this.pcfDiscoverer.stop().onErrorComplete(logErr))
                          .andThen(this.pcfRtManager.map(PcfRtManager::stop).orElse(Completable.complete()).onErrorComplete(logErr))
                          .andThen(this.pcfRtServiceCached.map(PcfRtService::stop).orElse(Completable.complete()).onErrorComplete(logErr))
                          .andThen(this.rxEtcd.close().onErrorComplete(logErr))
                          .andThen(this.tlsKeyLogger.map(TlsKeylogger::stop).orElse(Completable.complete()).onErrorComplete(logErr))
                          .andThen(this.alarmHandlerClient.close().onErrorComplete(logErr))
                          .andThen(this.vertx.rxClose().onErrorComplete(logErr))
                          .andThen(Completable.fromAction(() -> KmsClientUtilities.get(KmsParameters.instance, "eric-cm-key-role").dispose()));
    }

    /**
     * The BSF manager entry point
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args)
    {
        var exitStatus = 0;

        log.info("Starting BSF manager, version: {}", VersionInfo.get());
        try (var shutdownHook = new RxShutdownHook();
             var llc = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(),
                                           BSFMANAGER_CONTAINER_NAME))
        {
            final var params = BsfManagerParameters.fromEnvironment();
            final var manager = new BsfManager(params, shutdownHook);
            manager.run().blockingAwait();
            log.info("BSF manager terminated normally.");
            exitStatus = 0;
        }
        catch (Exception e)
        {
            log.error("BSF manager terminated abnormally due to exception", e);
            exitStatus = 1;
        }

        System.exit(exitStatus);
    }

    private Completable deployDefaultDiameterAdpConfiguration()
    {
        return Completable.defer(() ->
        {
            log.info("Deploying default Diameter ADP configuration.");

            final var stmDiameterConfig = new JsonObject(IOUtils.toString(BsfManager.class.getResourceAsStream("/bsf-diameter-adp.json"),
                                                                          StandardCharsets.UTF_8));

            return cmPatch.post("/cm/api/v1/configurations", stmDiameterConfig) //
                          .flatMapCompletable(statusCode ->
                          {
                              if (statusCode == HttpResponseStatus.CONFLICT.code())
                              {

                                  final var diameterConfigData = (JsonObject) stmDiameterConfig.getValue("data");
                                  final var diameterConfigDataDiameter = (JsonObject) diameterConfigData.getValue("ericsson-diameter-adp:diameter");
                                  final var diameterConfigDataDiameterService = (JsonObject) diameterConfigDataDiameter.getJsonArray("service").getValue(0);

                                  final var diameterConfigDataDiameterServicesID = diameterConfigDataDiameterService.getValue("id").toString();
                                  final var diameterConfigDataDiameterServicesSEE = diameterConfigDataDiameterService.getValue("service-execution-environment")
                                                                                                                     .toString();
                                  final var diameterConfigDataDiameterServicesREH = diameterConfigDataDiameterService.getValue("request-error-handler")
                                                                                                                     .toString();
                                  final var diameterConfigDataDiameterServicesApp = diameterConfigDataDiameterService.getJsonArray("application");
                                  final var diameterConfigDataDiameterServicesSIFT = diameterConfigDataDiameterService.getValue("session-id-format-type")
                                                                                                                      .toString();
                                  final var diameterConfigDataDiameterServicesLoopAvoid = diameterConfigDataDiameterService.getBoolean("predictive-loop-avoidance-enabled",
                                                                                                                                       false);

                                  final var diameterConfigDataDiameterApps = diameterConfigDataDiameter.getJsonArray("applications");
                                  final var diameterConfigDataDiameterVSAI = diameterConfigDataDiameter.getJsonArray("vendor-specific-application-id");
                                  final var diameterConfigDataDiameterDictionary = diameterConfigDataDiameter.getJsonArray("dictionary");

                                  final var patches = List.of(new PatchItem(PatchOperation.ADD,
                                                                            "/ericsson-diameter-adp:diameter/service/0/id",
                                                                            "",
                                                                            diameterConfigDataDiameterServicesID),
                                                              new PatchItem(PatchOperation.ADD,
                                                                            "/ericsson-diameter-adp:diameter/service/0/service-execution-environment",
                                                                            "",
                                                                            diameterConfigDataDiameterServicesSEE),
                                                              new PatchItem(PatchOperation.ADD,
                                                                            "/ericsson-diameter-adp:diameter/service/0/request-error-handler",
                                                                            "",
                                                                            diameterConfigDataDiameterServicesREH),
                                                              new PatchItem(PatchOperation.ADD,
                                                                            "/ericsson-diameter-adp:diameter/service/0/application",
                                                                            "",
                                                                            diameterConfigDataDiameterServicesApp),
                                                              new PatchItem(PatchOperation.ADD,
                                                                            "/ericsson-diameter-adp:diameter/service/0/session-id-format-type",
                                                                            "",
                                                                            diameterConfigDataDiameterServicesSIFT),
                                                              new PatchItem(PatchOperation.ADD,
                                                                            "/ericsson-diameter-adp:diameter/service/0/predictive-loop-avoidance-enabled",
                                                                            "",
                                                                            diameterConfigDataDiameterServicesLoopAvoid),

                                                              new PatchItem(PatchOperation.ADD,
                                                                            "/ericsson-diameter-adp:diameter/applications",
                                                                            "",
                                                                            diameterConfigDataDiameterApps),
                                                              new PatchItem(PatchOperation.ADD,
                                                                            "/ericsson-diameter-adp:diameter/vendor-specific-application-id",
                                                                            "",

                                                                            diameterConfigDataDiameterVSAI),
                                                              new PatchItem(PatchOperation.ADD,
                                                                            "/ericsson-diameter-adp:diameter/dictionary",
                                                                            "",
                                                                            diameterConfigDataDiameterDictionary));

                                  return this.cmPatch.patch("/cm/api/v1/configurations/ericsson-diameter-adp", patches)
                                                     .retryWhen(new RetryFunction().withDelay(5 * 1000L) // retry after 5 seconds
                                                                                   .withRetries(60) // give up after 5 minutes
                                                                                   .withRetryAction((error,
                                                                                                     retry) -> log.warn("Could not deploy Diameter ADP configuration, retrying: {}",
                                                                                                                        retry,
                                                                                                                        error))
                                                                                   .create())
                                                     .doOnComplete(() -> log.info("default Diameter ADP configuration updated with PATCH operation."));

                              }
                              else if (statusCode == HttpResponseStatus.CREATED.code())
                              {
                                  return Completable.complete();
                              }
                              else
                              {
                                  return Completable.error(new DefaultConfigurationException("default Diameter ADP deployment failed, statusCode: "
                                                                                             + statusCode));
                              }
                          })
                          .doOnError(throwable -> log.error("default Diameter ADP configuration deployment failed", throwable))
                          .doOnComplete(() -> log.info("default Diameter ADP configuration deployed successfuly."));
        }).onErrorResumeNext(error -> //
        error instanceof DefaultConfigurationException ? Completable.error(error)
                                                       : Completable.error(new DefaultConfigurationException("default Diameter ADP configuration deployment failed.",
                                                                                                             error))//
        );
    }

    private Completable deployDefaultAcmConfiguration()
    {
        return this.cmPatch.get("/cm/api/v1/configurations/ietf-netconf-acm") //
                           .flatMapCompletable(resp ->
                           {
                               final var bsfAdmin = "ericsson-bsf-manager-1-bsf-admin";
                               final var bsfSecAdmin = "ericsson-bsf-manager-2-bsf-security-admin";
                               final var bsfReadOnly = "ericsson-bsf-manager-3-bsf-read-only";

                               if (resp.contains(bsfAdmin) && resp.contains(bsfSecAdmin) && resp.contains(bsfReadOnly))
                               {
                                   return this.deployDefaultDiameterAcmRules(List.of(bsfAdmin, bsfReadOnly), resp);
                               }
                               else
                               {
                                   final var uriPath = "/ietf-netconf-acm:nacm/rule-list/1"; // NOSONAR
                                   final var patches = List.of(new PatchItem(PatchOperation.ADD,
                                                                             uriPath,
                                                                             "",
                                                                             AcmConfigHandler.generateBsfAdminAcmDefaultConfig(this.bsfDiameterEnabled)), //
                                                               new PatchItem(PatchOperation.ADD,
                                                                             uriPath,
                                                                             "",
                                                                             AcmConfigHandler.generateBsfSecAdminAcmDefaultConfig()),
                                                               new PatchItem(PatchOperation.ADD,
                                                                             uriPath,
                                                                             "",
                                                                             AcmConfigHandler.generateBsfReadOnlyAcmDefaultConfig(this.bsfDiameterEnabled)));

                                   return this.cmPatch.patch("/cm/api/v1/configurations/ietf-netconf-acm", patches)
                                                      .retryWhen(new RetryFunction().withDelay(5 * 1000L) // retry after 5 seconds
                                                                                    .withRetries(60) // give up after 5 minutes
                                                                                    .withRetryAction((error,
                                                                                                      retry) -> log.warn("Could not deploy default NACM configuration, retrying: {}",
                                                                                                                         retry,
                                                                                                                         error))
                                                                                    .create())
                                                      .doOnComplete(() -> log.info("Default NACM configuration added with PATCH operation."));

                               }
                           })
                           .doOnError(throwable -> log.warn("Default NACM configuration load Failed!", throwable))
                           .doOnComplete(() -> log.info("Default NACM configuration loaded successfully."))

                           .doOnError(t ->
                           {
                               throw new DefaultConfigurationException("NACM configuration Failed.", t);
                           });
    }

    private final Completable deployDefaultDiameterAcmRules(final List<String> groups,
                                                            final String cmmResponse)
    {
        if (this.bsfDiameterEnabled && cmmResponse.contains("ericsson-diameter-adp-1-bsf")) // it is assumed that both BSF groups contain the diameter rule
        {
            return Completable.complete().doOnComplete(() -> log.info("Diameter NACM rules already exist"));
        }
        else if (this.bsfDiameterEnabled)
        {
            final var inputJson = new JsonObject(cmmResponse);

            final var ruleLists = inputJson.getJsonObject("data").getJsonObject("ietf-netconf-acm:nacm").getJsonArray("rule-list");

            final var patches = new ArrayList<PatchItem>();
            final var ruleListIndex = new AtomicInteger(0);

            ruleLists.forEach(usergroup ->
            {
                final var currentGroup = ((JsonObject) usergroup).getString("name");

                if (groups.contains(currentGroup))
                {
                    patches.add(new PatchItem(PatchOperation.ADD,
                                              String.format("/ietf-netconf-acm:nacm/rule-list/%d/rule/-", ruleListIndex.get()),
                                              "",
                                              AcmConfigHandler.generateBsfDiameterRules(currentGroup.substring(currentGroup.lastIndexOf("bsf"),
                                                                                                               currentGroup.length()))));
                }

                ruleListIndex.incrementAndGet();
            });

            final var errMsg = "Could not patch of Diameter NACM groups";
            final var completeMsg = "Diameter NACM groups added successfully with PATCH operation";

            return this.patchAcm(patches, errMsg, completeMsg);
        }
        else
        {
            return Completable.complete().doOnComplete(() -> log.info("Diameter is disabled, skip adding diameter NACM rules"));
        }

    }

    private Completable deployDefaultAcmGroupConfiguration()
    {

        final var patches = List.of(new PatchItem(PatchOperation.ADD, "/ietf-netconf-acm:nacm/groups", "", new JsonObject()), // push empty groups container in
                                    // nacm
                                    new PatchItem(PatchOperation.ADD, "/ietf-netconf-acm:nacm/groups/group", "", new JsonArray()), // push empty group list i
                                    // nacm/groups
                                    new PatchItem(PatchOperation.ADD,
                                                  "/ietf-netconf-acm:nacm/groups/group",
                                                  "",
                                                  AcmConfigHandler.generateDefaultAdminGroupsConfig())); // push list of system groups
        final var errMsg = "Could not create list of NACM groups.";
        final var completeMsg = "NACM groups added successfully with PATCH operation.";

        return checkGroupsExists().flatMapCompletable(res ->
        {
            if (res.equals(Boolean.TRUE))
                return this.patchAcm(patches, errMsg, completeMsg); // push list of system groups
            else
                return Completable.complete().doOnComplete(() -> log.info("Nacm groups already exist.")); // groups already exist, do nothing but log
        });
    }

    private Completable patchAcm(List<PatchItem> patches,
                                 String errMsg,
                                 String completeMsg)
    {

        return this.cmPatch.patch("/cm/api/v1/configurations/ietf-netconf-acm", patches)
                           .retryWhen(new RetryFunction().withDelay(5 * 1000L) // retry after 5 seconds
                                                         .withRetries(60) // give up after 5 minutes
                                                         .withRetryAction((error,
                                                                           retry) -> log.warn("{}, retrying: {}", errMsg, retry, error))
                                                         .create())
                           .doOnComplete(() -> log.info(completeMsg));

    }

    private Single<Boolean> checkGroupsExists()
    {
        return this.cmPatch.get("/cm/api/v1/configurations/ietf-netconf-acm").map(res ->
        {
            var inputJson = new JsonObject(res);
            var nacm = inputJson.getJsonObject("data").getJsonObject("ietf-netconf-acm:nacm");

            Boolean passGroups = Boolean.TRUE;
            if (nacm.containsKey("groups"))
            {
                var groups = nacm.getJsonObject("groups");
                if (groups.containsKey("group"))
                {
                    var group = groups.getJsonArray("group");
                    if (!group.isEmpty())
                        passGroups = Boolean.FALSE;
                }
            }
            return passGroups;
        });
    }

    /**
     * SC Monitor interface for BSF manager
     */
    private static class CommandConfig extends MonitorAdapter.CommandBase
    {
        private final BehaviorSubject<Optional<EricssonBsf>> config;
        private static final EricssonBsf EMPTY_CFG = new EricssonBsf();

        public CommandConfig(final BehaviorSubject<Optional<EricssonBsf>> config)
        {
            super("config", "Usage: command=config");
            this.config = config;
        }

        @Override
        public HttpResponseStatus execute(Result result,
                                          Command request)
        {
            result.setAdditionalProperty("config", this.config.getValue().orElse(EMPTY_CFG));
            return HttpResponseStatus.OK;
        }
    }

    /**
     * Monitor command (GET) for dumping the PcfRt content from etcd Database.
     */
    private static class CommandDumpPcfRtDbEtcd extends MonitorAdapter.CommandBase
    {
        private final PcfRtServiceImpl uncachedPcfRtService;
        private final PcfRtService cachedPcfRtService;

        public CommandDumpPcfRtDbEtcd(PcfRtServiceImpl uncachedPcfRtService,
                                      PcfRtService cachedPcfRtService)
        {
            super("dumpPcfRtDb", "Usage: command=dumpPcfRtDb[&cached[=<false|true>]]");
            this.uncachedPcfRtService = uncachedPcfRtService;
            this.cachedPcfRtService = cachedPcfRtService;
        }

        @Override
        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {
            try
            {
                final boolean cached = Boolean.parseBoolean((String) request.getAdditionalProperties().get("cached"));
                final PcfRtService svc = cached ? this.cachedPcfRtService : this.uncachedPcfRtService;

                final var pcfRtList = svc.getAll() //
                                         .blockingGet(); // Blocking operation, but no problem since Monitor operates on IO scheduler
                log.info("Number of entries in PcfRt database: {}", pcfRtList.size());

                result.setAdditionalProperty("pcfRtList", pcfRtList);
                return HttpResponseStatus.OK;
            }
            catch (Exception e)
            {
                log.error("Unexpected error while executing dumpPcfRtDb monitor command", e);
                result.setAdditionalProperty("errorMessage", e.toString());
                return HttpResponseStatus.INTERNAL_SERVER_ERROR;
            }
        }
    }

    /**
     * Monitor command (PUT) for deleting the PcfRt content from etcd Database.
     */
    private static class CommandTruncatePcfRtDbEtcd extends MonitorAdapter.CommandBase
    {
        private final PcfRtDbEtcd pcfRtDb;

        public CommandTruncatePcfRtDbEtcd(PcfRtDbEtcd pcfRtDb)
        {
            super("truncatePcfRtDb", "Usage: command=truncatePcfRtDb");
            this.pcfRtDb = pcfRtDb;
        }

        @Override
        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {
            try
            {
                this.pcfRtDb.truncate().blockingAwait(); // Blocking operation, but no problem since Monitor operates on IO scheduler
                return HttpResponseStatus.OK;
            }
            catch (Exception e)
            {
                log.error("Unexpected error while executing truncatePcfRtDb monitor command", e);
                result.setAdditionalProperty("errorMessage", e.toString());
                return HttpResponseStatus.INTERNAL_SERVER_ERROR;
            }
        }
    }
}
