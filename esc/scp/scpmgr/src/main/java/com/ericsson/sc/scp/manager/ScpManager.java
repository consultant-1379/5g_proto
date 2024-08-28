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
 * Created on: Jun 18, 2020
 *     Author: eedstl
 */

package com.ericsson.sc.scp.manager;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.AcmConfigHandler;
import com.ericsson.adpal.cm.CmAdapter;
import com.ericsson.adpal.cm.CmmPatch;
import com.ericsson.adpal.cm.PatchItem;
import com.ericsson.adpal.cm.PatchOperation;
import com.ericsson.adpal.ext.monitor.MonitorAdapter;
import com.ericsson.adpal.ext.monitor.MonitorAdapter.CommandTestAlarm;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.cnal.common.CertificateObserver;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.sc.common.alarm.AlarmHandler;
import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;
import com.ericsson.sc.common.alarm.IfAlarmHandler;
import com.ericsson.sc.common.alarm.UnresolvableHostsAlarmHandler;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.fm.FmAlarmHandler;
import com.ericsson.sc.fm.FmAlarmServiceImpl;
import com.ericsson.sc.nfm.model.ServiceName;
import com.ericsson.sc.nfm.model.ServiceVersion;
import com.ericsson.sc.nlf.client.NlfConfigurator;
import com.ericsson.sc.nrf.r17.ConfigComparators;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoverer;
import com.ericsson.sc.nrf.r17.NnrfNfDiscoveryAlarmHandler;
import com.ericsson.sc.nrf.r17.Nrf.Pool;
import com.ericsson.sc.nrf.r17.NrfDnsCache;
import com.ericsson.sc.pm.ScPmbrConfigHandler;
import com.ericsson.sc.proxyal.service.PvtbApiService;
import com.ericsson.sc.rlf.client.RlfConfigurator;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.RxLeaderElection;
import com.ericsson.sc.rxetcd.RxLeaderElection.LeaderStatus;
import com.ericsson.sc.scp.config.ConfigUtils;
import com.ericsson.sc.scp.model.DnsProfile;
import com.ericsson.sc.scp.model.EricssonScp;
import com.ericsson.sc.scp.model.EricssonScpScpFunction;
import com.ericsson.sc.scp.model.glue.NfFunction;
import com.ericsson.sc.scp.validator.ScpValidator;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.sc.util.tls.TlsKeylogger;
import com.ericsson.sc.utilities.dns.DnsCache;
import com.ericsson.sc.utilities.dns.IfDnsLookupContext;
import com.ericsson.sc.utilities.dns.IpFamily;
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
import com.ericsson.utilities.http.WebServerBuilder;
import com.ericsson.utilities.json.Json;
import com.ericsson.utilities.json.Json.Patch;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.metrics.MetricRegister;
import com.ericsson.utilities.reactivex.RetryFunction;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.PatchUtils;
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

public class ScpManager
{
    private static class CommandConfig extends MonitorAdapter.CommandBase
    {
        private final BehaviorSubject<Optional<EricssonScp>> config;

        public CommandConfig(final BehaviorSubject<Optional<EricssonScp>> config)
        {
            super("config", "Usage: command=config");
            this.config = config;
        }

        @Override
        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {
            this.config.getValue().ifPresent(cfg -> result.setAdditionalProperty("config", cfg));
            return HttpResponseStatus.OK;
        }
    }

    private static class ConfigContext extends ConfigComparators.ChangeFlags
    {
        public static ConfigContext empty()
        {
            return new ConfigContext(Optional.empty(), Optional.of(List.of()));
        }

        public static ConfigContext of(final Optional<EricssonScp> config)
        {
            return new ConfigContext(config, Optional.empty());
        }

        public static ConfigContext of(final Optional<EricssonScp> config,
                                       final Optional<List<Json.Patch>> diff)
        {
            return new ConfigContext(config, diff);
        }

        private final Optional<EricssonScp> config;

        private ConfigContext(final Optional<EricssonScp> config,
                              final Optional<List<Json.Patch>> diff)
        {
            super(diff,
                  Flags.F_ALL,
                  Flags.F_NNLF_DISC,
                  Flags.F_NNRF_DISC,
                  Flags.F_NNRF_DISC_CAPACITY,
                  Flags.F_NNRF_NFM,
                  Flags.F_NNRF_NFM_NRF_GROUP_INST_ID,
                  Flags.F_NRFL_RATE_LIMITING);

            this.config = config;
        }

        public Optional<EricssonScp> getConfig()
        {
            return this.config;
        }

        public boolean isChangedAll()
        {
            return this.changeFlags.get(Flags.F_ALL);
        }

        public boolean isChangedNnlfDisc()
        {
            return this.changeFlags.get(Flags.F_NNLF_DISC);
        }

        public boolean isChangedNnrfDisc()
        {
            return this.changeFlags.get(Flags.F_NNRF_DISC);
        }

        public boolean isChangedNnrfDiscCapacity()
        {
            return this.changeFlags.get(Flags.F_NNRF_DISC_CAPACITY);
        }

        public boolean isChangedNnrfNfm()
        {
            return this.changeFlags.get(Flags.F_NNRF_NFM);
        }

        public boolean isChangedNnrfNfmNrfGroupInstId()
        {
            return this.changeFlags.get(Flags.F_NNRF_NFM_NRF_GROUP_INST_ID);
        }

        public boolean isChangedNrlfRateLimiting()
        {
            return this.changeFlags.get(Flags.F_NRFL_RATE_LIMITING);
        }
    }

    private static final String ENV_CM_MEDIATOR = "CM_MEDIATOR";
    private static final String ENV_EXTERNAL_IPV4 = "EXTERNAL_IPV4";
    private static final String ENV_EXTERNAL_IPV6 = "EXTERNAL_IPV6";
    private static final String ENV_EXTERNAL_IPV4_MULTI_VPN = "EXTERNAL_IPV4_MULTI_VPN";
    private static final String ENV_EXTERNAL_IPV6_MULTI_VPN = "EXTERNAL_IPV6_MULTI_VPN";
    private static final String ENV_HOSTNAME = "HOSTNAME";
    private static final String ENV_LEADER_ELECTION_ENABLED = "LEADER_ELECTION_ENABLED";
    private static final String ENV_MULTIVPN_ENABLED = "MULTIVPN_ENABLED";
    private static final String ENV_NAMESPACE = "NAMESPACE";
    private static final String ENV_POD_NAME = "POD_NAME";

    private static final String DEFAULT_ROUTE_ADDRESS = "[::]";

    private static final String ETCD_ENDPOINT = "ETCD_ENDPOINT";
    private static final String ETCD_PASSWORD = "ETCD_PASSWORD";
    private static final String ETCD_USERNAME = "ETCD_USERNAME";

    private static final String JP_NF_INSTANCE_ID = "/nf-instance-id";
    private static final String KEY_SCP_FUNCTION = "ericsson-scp:scp-function";

    private static final String LE_LEADER_KEY = "/ericsson-sc/scp/manager";
    private static final int LE_LEADER_TTL = 13;
    private static final int LE_RENEW_INTERVAL = 4;
    private static final int LE_CLAIM_INTERVAL = 3;
    private static final int LE_RECOVERY_DELAY = 12;
    private static final float LE_REQUEST_LATENCY = 0.5f;
    private static final long RETRIES_FOR_LEADER_ELECTION = 10l;
    private static final int ETCD_REQUEST_TIMEOUT = 2;

    private static final String SCHEMA_ERICSSON_SCP = "ericsson-scp";

    private static final String STATUS_CONTENDER = "eric-scp-manager-contender";
    private static final String STATUS_LEADER = "eric-scp-manager-leader";

    private static final Logger log = LoggerFactory.getLogger(ScpManager.class);
    private static final ScpManagerInterfacesParameters params = ScpManagerInterfacesParameters.instance;

    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/scpmanager/config/logcontrol").getPath();
    private static final String CONTAINER_NAME = EnvVars.get("CONTAINER_NAME");

    private static final String LEADER_ELECTION_STATE_FILE = "/leaderElection/isContender";

    public static void main(String[] args)
    {
        int exitStatus = 0;

        log.info("Starting SCP manager, version: {}", VersionInfo.get());

        try (var shutdownHook = new RxShutdownHook();
             var llc = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(), CONTAINER_NAME))
        {
            final ScpManager manager = new ScpManager(shutdownHook);
            manager.run().blockingAwait();
        }
        catch (final Exception t)
        {
            log.error("Exception caught, exiting.", t);
            exitStatus = 1;
        }

        log.info("Stopped SCP manager.");

        System.exit(exitStatus);
    }

    private final RxShutdownHook shutdownHook;
    private final CertificateObserver certificateObserver;
    private final FmAlarmServiceImpl fmAlarmService;
    private final WebClientProvider alarmHandlerClient;
    private final NfInstanceAlarmHandler nfInstanceAh;
    private final NfInstanceAlarmHandlerUnavailable nfInsAhUn;
    private final IfAlarmHandler ah;
    private final NnrfNfDiscoveryAlarmHandler nnrfNfDiscoveryAh;
    private final UnresolvableHostsAlarmHandler unresolvableHostsAh;
    private final WebServer nrfWebServer;
    private final WebServer probeWebServer;
    private final RouterHandler oamWebServer;
    private final KubeProbe kubeProbe;
    private final CmAdapter<EricssonScp> cm;
    private final BehaviorSubject<ConfigContext> configFlow;
    private final NfFunction scpFunction;
    private final ScpServiceController scpService;
    private final MonitorAdapter monitored;
    private final LoadMeter loadMeter;
    private final CmmPatch cmPatch;
    private final Optional<RxEtcd> rxEtcd;
    private final Optional<RxLeaderElection> election;
    private final boolean enableLeaderElection;
    private final String ownId;
    private final CoreV1Api api;
    private final ApiClient client;
    private final ScPmbrConfigHandler pmbrCfgHandler;
    private final NnrfNfDiscovery nfDiscovery;
    private final ScpValidator scpValidator;
    private final WebClientProvider webClientProvider;
    private final List<V1Service> k8sServiceList; // this list is used by Ingress and Validator
    private final Optional<TlsKeylogger> tlsKeyLogger;
    private final Optional<PvtbApiService> pvtbService;

    public ScpManager(RxShutdownHook shutdownHook) throws IOException
    {
        this.certificateObserver = new CertificateObserver(params.trafficCertPath);

        this.tlsKeyLogger = TlsKeylogger.fromEnvVars();

        this.ownId = EnvVars.get(ENV_POD_NAME);
        this.enableLeaderElection = Boolean.valueOf(EnvVars.get(ENV_LEADER_ELECTION_ENABLED));

        this.client = ClientBuilder.standard().build();
        Configuration.setDefaultApiClient(this.client);
        this.api = new CoreV1Api();

        this.k8sServiceList = List.copyOf(this.createK8sServiceList());
        this.scpValidator = new ScpValidator(SCHEMA_ERICSSON_SCP, k8sServiceList);
        this.shutdownHook = shutdownHook;

        final var wcb = WebClientProvider.builder().withHostName(params.serviceHostname);
        if (params.globalTlsEnabled)
            wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.mediatorClientCertPath),
                                                            SipTlsCertWatch.trustedCert(params.sipTlsRootCaPath)));
        this.webClientProvider = wcb.build(VertxInstance.get());

        // create client for fault indications to alarm handler
        final var ahClient = WebClientProvider.builder().withHostName(params.serviceHostname);
        if (params.globalTlsEnabled)
            ahClient.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.alarmHandlerClientCertPath), //
                                                                 SipTlsCertWatch.trustedCert(params.sipTlsRootCaPath)));
        this.alarmHandlerClient = ahClient.build(VertxInstance.get());

        // create alarm handler for requests to alarm handler service
        var fmAlarmHandler = new FmAlarmHandler(this.alarmHandlerClient, // web client to be used for alarm raise/cease
                                                params.alarmHandlerHostName, // alarm handler service server hostname
                                                params.alarmHandlerPort, // alarm handler service server port
                                                params.globalTlsEnabled); // indication if tls is enabled

        // create alarm service for updating the alarm through alarm handler service
        this.fmAlarmService = new FmAlarmServiceImpl(fmAlarmHandler);
        this.ah = AlarmHandler.of(this.fmAlarmService);

        this.cm = new CmAdapter<>(EricssonScp.class,
                                  SCHEMA_ERICSSON_SCP,
                                  VertxInstance.get(),
                                  params.mediatorPort,
                                  EnvVars.get(ENV_CM_MEDIATOR),
                                  webClientProvider,
                                  params.globalTlsEnabled,
                                  params.subscribeValidity,
                                  params.subscribeRenewal,
                                  params.subscribeHeartbeat);

        this.cmPatch = new CmmPatch(params.mediatorPort, //
                                    EnvVars.get(ENV_CM_MEDIATOR), //
                                    this.webClientProvider, //
                                    params.globalTlsEnabled);

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

        this.nrfWebServer = WebServer.builder() //
                                     .withHost(DEFAULT_ROUTE_ADDRESS)
                                     .withPort(params.nrfPort)
                                     .build(VertxInstance.get());

        this.probeWebServer = WebServer.builder() //
                                       .withHost(DEFAULT_ROUTE_ADDRESS)
                                       .withPort(params.k8sProbePort)
                                       .build(VertxInstance.get());

        final WebServerBuilder internalWebServer = WebServer.builder().withHost(DEFAULT_ROUTE_ADDRESS).withPort(params.oamServerPort);

        if (params.globalTlsEnabled)
        {
            var oamTls = DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.oamServerCertPath), // scp manager server certificate
                                                      SipTlsCertWatch.combine(// mediator server ca for verification of client certificates during
                                                                              // notifications
                                                                              SipTlsCertWatch.trustedCert(params.mediatorServerCaPath),
                                                                              // yang provider server ca for verification of client certificates during
                                                                              // validation
                                                                              SipTlsCertWatch.trustedCert(params.yangServerCaPath),
                                                                              // pm server ca for verification of client certificates during scraping of
                                                                              // metrics
                                                                              SipTlsCertWatch.trustedCert(params.pmServerCaPath)));
            internalWebServer.withDynamicTls(oamTls);
        }

        this.oamWebServer = internalWebServer.build(VertxInstance.get());

        PmAdapter.configureMetricsHandler(this.oamWebServer);

        this.pmbrCfgHandler = new ScPmbrConfigHandler(this.cmPatch);
        this.kubeProbe = KubeProbe.Handler.singleton().configure(this.probeWebServer).register(KubeProbe.of().setAlive(true).setReady(false));
        this.loadMeter = new LoadMeter(VertxInstance.get(), this.cm.getNotificationHandler().getConfiguration());

        final Alarm.Context alarmCtx = Alarm.Context.of(this.ah, "Scp", SCHEMA_ERICSSON_SCP);

        this.nnrfNfDiscoveryAh = new NnrfNfDiscoveryAlarmHandler(alarmCtx);
        this.unresolvableHostsAh = new UnresolvableHostsAlarmHandler(alarmCtx);

        this.scpFunction = new NfFunction(alarmCtx, this.loadMeter, this.certificateObserver.getSecrets(), new Rdn("nf", "scp-function"));

        this.pvtbService = PvtbApiService.fromEnvVars(VertxInstance.get());
        this.scpService = new ScpServiceController(SCHEMA_ERICSSON_SCP,
                                                   this.fmAlarmService,
                                                   this.cm.getNotificationHandler().getConfiguration(),
                                                   this.k8sServiceList,
                                                   this.pvtbService.map(PvtbApiService::getPvtbConfigs));
        this.nfInstanceAh = new NfInstanceAlarmHandler(SCHEMA_ERICSSON_SCP,
                                                       this.fmAlarmService,
                                                       this.scpService.getOutlierEventStream(),
                                                       this.scpService.getHealthCheckEventStream(),
                                                       this.scpService.getDisconnections(),
                                                       this.cm.getNotificationHandler().getConfiguration());

        this.nfInsAhUn = new NfInstanceAlarmHandlerUnavailable(VertxInstance.get(),
                                                               SCHEMA_ERICSSON_SCP,
                                                               this.fmAlarmService,
                                                               this.cm.getNotificationHandler().getConfiguration());

        this.monitored = new MonitorAdapter(this.probeWebServer,
                                            Arrays.asList(new CommandConfig(this.cm.getNotificationHandler().getConfiguration()),
                                                          new MonitorAdapter.CommandCounter()),
                                            Arrays.asList(new CommandTestAlarm(SCHEMA_ERICSSON_SCP,
                                                                               this.fmAlarmService,
                                                                               NFType.CHF,
                                                                               EnvVars.get(ENV_HOSTNAME))));

        final var edb = RxEtcd.newBuilder()
                              .withEndpoint(EnvVars.get(ETCD_ENDPOINT))
                              .withConnectionRetries(10)
                              .withRequestTimeout(ETCD_REQUEST_TIMEOUT, TimeUnit.SECONDS);

        if (params.dcedTlsEnabled)
        {
            edb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.dcedClientCertPath),
                                                            SipTlsCertWatch.trustedCert(params.sipTlsRootCaPath)));
        }
        else
        {
            edb.withUser(EnvVars.get(ETCD_USERNAME)).withPassword(EnvVars.get(ETCD_PASSWORD));
        }
        this.rxEtcd = Optional.of(edb.build());

        this.election = this.enableLeaderElection ? rxEtcd.map(etcd -> new RxLeaderElection.Builder(etcd,
                                                                                                    ownId,
                                                                                                    LE_LEADER_KEY).leaderInterval(LE_LEADER_TTL)
                                                                                                                  .renewInterval(LE_RENEW_INTERVAL)
                                                                                                                  .claimInterval(LE_CLAIM_INTERVAL)
                                                                                                                  .recoveryDelay(LE_RECOVERY_DELAY)
                                                                                                                  .requestLatency(LE_REQUEST_LATENCY)
                                                                                                                  .retries(RETRIES_FOR_LEADER_ELECTION)
                                                                                                                  .build()
                                                                                                                  .blockingGet())
                                                  : Optional.empty();

        final NnrfNfDiscoverer discoverer = new NnrfNfDiscoverer(this.nrfWebServer, //
                                                                 nrfGroup ->
                                                                 {
                                                                     Optional<Pool> o = Optional.ofNullable(this.scpFunction.getNfInstance(0))
                                                                                                .map(i -> i.getNrfGroups().get(nrfGroup));
                                                                     log.debug("nrfGroup={}, pool={}", nrfGroup, o);
                                                                     return o;
                                                                 },
                                                                 this.configFlow.subscribeOn(Schedulers.io())
                                                                                .filter(ConfigContext::isChangedNnrfDisc)
                                                                                .map(ConfigContext::getConfig)
                                                                                .toFlowable(BackpressureStrategy.BUFFER)
                                                                                .map(NnrfNfDiscovery::mapConfigToInput),
                                                                 NnrfNfDiscoverer.SearchingContext.OutputStrategy.BEST_EFFORT);
        this.nfDiscovery = new NnrfNfDiscovery(discoverer, this.cmPatch, this.nnrfNfDiscoveryAh); // Discoverer started/stopped together with the handler.
    }

    public Completable run()
    {
        final Function<Pair<Pair<ConfigContext, ConfigContext>, Pair<ConfigContext, ConfigContext>>, Completable> updateNfFunction = pair ->
        {
            final Optional<EricssonScp> config = pair.getSecond().getSecond().getConfig();

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
                    final EricssonScpScpFunction curr = config.get().getEricssonScpScpFunction();

                    log.debug("curr={}", curr);

                    if (curr != null)
                    {
                        // Config is empty -> update.
                        if (curr.getNfInstance() == null || curr.getNfInstance().isEmpty())
                        {
                            this.scpFunction.update(curr);
                            return Completable.complete();
                        }

                        // Only nfInstanceId of NRF group is changed, which was initiated by ourselves
                        // -> ignore.
                        if (instIdRelated && !userRelated)
                            return Completable.complete();

                        // Take a copy of curr. This is needed as curr should remain unchanged; the copy
                        // is used for modifications and will be used for further processing.
                        final EricssonScpScpFunction copy = Json.copy(curr, EricssonScpScpFunction.class);

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
                                            if (service.getServiceVersion() == null || service.getServiceVersion().isEmpty())
                                            {
                                                if (service.getServiceName().equals(ServiceName.NCHF_CONVERGEDCHARGING))
                                                {
                                                    // Set supported versions for R15, R16:

                                                    service.setServiceVersion(List.of(new ServiceVersion().withApiVersionInUri("v2")
                                                                                                          .withApiFullVersion("2.0.0"),
                                                                                      new ServiceVersion().withApiVersionInUri("v3")
                                                                                                          .withApiFullVersion("3.0.1")));
                                                }
                                                else if (service.getServiceName().equals(ServiceName.NCHF_SPENDINGLIMITCONTROL))
                                                {
                                                    // Set supported versions for R15, R16:

                                                    service.setServiceVersion(List.of(new ServiceVersion().withApiVersionInUri("v1")
                                                                                                          .withApiFullVersion("1.1.3")));
                                                }
                                            }
                                        });
                                });
                        });

                        // If the change is not DNS related:
                        // Calling update(copy) below may change copy, due to the update of the
                        // NF-instance ID of the NRF-groups. The delta of copy and curr reflects that
                        // change (only changed NF-instance IDs are considered) and can then be used for
                        // patching the configuration in CMM.
                        this.scpFunction.update(copy, userRelated, dnsRelated);

                        if (dnsRelated)
                        {
                            log.info("DNS related update, configuration unchanged.");
                            return Completable.complete();
                        }

                        final List<PatchItem> patches = ConfigComparators.diffNrfGroupsNfInstanceId("/ericsson-scp:scp-function", curr, copy)
                                                                         .stream()
                                                                         .map(p -> new PatchItem(PatchOperation.fromValue(p.getOp().getValue()),
                                                                                                 p.getPath(),
                                                                                                 p.getFrom(),
                                                                                                 p.getValue()))
                                                                         .toList();

                        log.info("updateNfFunction: #patches={}, patches={}", patches.size(), log.isDebugEnabled() ? patches : "<not printed on info level>");

                        if (patches.isEmpty())
                            return Completable.complete();

                        return this.cmPatch.patch("/cm/api/v1/configurations/ericsson-scp", patches)
                                           .subscribeOn(Schedulers.io())
                                           .doOnComplete(() -> log.info("Updated nfInstanceIds in configuration ericsson-scp."))
                                           .doOnError(e -> log.warn("Could not update nfInstanceIds in configuration ericsson-scp. Cause: {}", e.toString()))
                                           .onErrorComplete();
                    }
                }

                this.scpFunction.stop();
            }
            catch (final Exception t)
            {
                log.warn("Ignoring new configuration. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(t, log.isDebugEnabled()));
            }

            return Completable.complete();
        };

        final Function<Pair<Optional<EricssonScp>, Map<String, IfDnsLookupContext>>, Completable> updateNlf = configAndDnsResults ->
        {
            Optional<EricssonScp> config = configAndDnsResults.getFirst();

            log.info("updateNlf={}", log.isDebugEnabled() ? config : "<config not printed on info level>");

            try
            {
                if (config.isPresent())
                    NlfConfigurator.singleton()
                                   .publish("scp", ConfigUtils.mapToNlfConfig(config.get().getEricssonScpScpFunction(), configAndDnsResults.getSecond()));
            }
            catch (final Exception t)
            {
                log.warn("Ignoring new configuration. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(t, log.isDebugEnabled()));
            }

            return Completable.complete();
        };

        final Function<Optional<EricssonScp>, Completable> updateRlf = config ->
        {
            var GRLEnabled = Boolean.parseBoolean(EnvVars.get("GLOBAL_RATE_LIMIT_ENABLED", false));

            if (GRLEnabled == false)
            {
                return Completable.complete();

            }

            log.info("updateRlf={}", log.isDebugEnabled() ? config : "<config not printed on info level>");

            try
            {
                if (config.isPresent())
                    RlfConfigurator.singleton().publish("scp", ConfigUtils.mapToRlfConfig(config.get().getEricssonScpScpFunction()));
            }
            catch (final Exception t)
            {
                log.warn("Ignoring new configuration. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(t, log.isDebugEnabled()));
            }

            return Completable.complete();
        };

        final Completable waitUntilLeader = this.enableLeaderElection ? updateLeaderElectionState(LeaderStatus.CONTENDER).andThen(this.election.map(RxLeaderElection::leaderStatusUpdates)
                                                                                                                                               .orElse(Observable.just(LeaderStatus.CONTENDER))
                                                                                                                                               .filter(status -> status.equals(LeaderStatus.LEADER))
                                                                                                                                               .firstOrError()
                                                                                                                                               .ignoreElement()
                                                                                                                                               .doOnSubscribe(disp -> log.info("Waiting to become leader"))
                                                                                                                                               .doOnComplete(() -> log.info("I am the leader")))
                                                                      : Completable.complete();

        final Completable leadershipLost = this.enableLeaderElection ? this.election.map(RxLeaderElection::leaderStatusUpdates)
                                                                                    .orElse(Observable.just(LeaderStatus.LEADER))
                                                                                    .filter(status -> status.equals(LeaderStatus.CONTENDER))
                                                                                    .firstOrError()
                                                                                    .ignoreElement()
                                                                                    .doOnComplete(() -> log.error("Lost leadership, shutting down"))
                                                                                    .andThen(this.updateLeaderElectionState(LeaderStatus.CONTENDER))
                                                                     : Completable.never();

        final Consumer<Optional<EricssonScp>> registerAllUnresolvedNrfFqdnsWithDnsCache = o ->
        {
            final Set<IfDnsLookupContext> unresolvedNrfFqdns = new HashSet<>();

            o.ifPresent(config -> Optional.ofNullable(config.getEricssonScpScpFunction())
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
                                                                                                                                                                                                      .map(r -> IpFamily.fromValue(r.value()))
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

        final Completable managerChain = this.probeWebServer.startListener() // Start internal web server ASAP, so that k8s liveness probe does not fail
                                                            .andThen(this.rxEtcd.map(RxEtcd::ready).orElse(Completable.complete())) // wait until etcd is
                                                                                                                                    // up
                                                            .andThen(this.certificateObserver.start())
//                                                                 .andThen(this.metricsIfWebServer.startListener())
                                                            .andThen(this.oamWebServer.startListener())
                                                            .doOnComplete(() -> this.kubeProbe.setReady(true)) // Ready
                                                            .andThen(waitUntilLeader)// Wait until we are leader
                                                            .andThen(NlfConfigurator.singleton().start())
                                                            .andThen(RlfConfigurator.singleton().start())
                                                            .andThen(this.nfDiscovery.start())
                                                            .andThen(this.pmbrCfgHandler.createPmbrJobPatches())
                                                            .andThen(this.pmbrCfgHandler.createPmbrGroupPatches())
                                                            .andThen(this.createAcmDefaultConf())
                                                            .andThen(this.deployDefaultAcmGroupConfiguration())
                                                            .andThen(this.startValidator())
//                                                            .andThen(this.alarm.start().onErrorComplete())
                                                            .andThen(this.scpService.start().onErrorComplete())
                                                            .andThen(this.monitored.start().onErrorComplete())
                                                            .andThen(MetricRegister.singleton().start())
                                                            .andThen(this.loadMeter.start())
                                                            .andThen(this.nfInstanceAh.start())
                                                            .andThen(this.nfInsAhUn.start())
                                                            .andThen(this.ah.start())
                                                            .andThen(this.nnrfNfDiscoveryAh.start())
                                                            .andThen(this.unresolvableHostsAh.start())
                                                            .andThen(this.cm.getNotificationHandler().start(this.oamWebServer))
                                                            .andThen(this.enableLeaderElection ? this.updateLeaderElectionState(LeaderStatus.LEADER)
                                                                                               : Completable.complete())
                                                            .andThen(Completable.ambArray(this.configFlow.subscribeOn(Schedulers.io())
                                                                                                         .map(ConfigContext::getConfig)
                                                                                                         .distinctUntilChanged()
                                                                                                         .doOnNext(registerAllUnresolvedNrfFqdnsWithDnsCache)
                                                                                                         .ignoreElements(),
                                                                                          Observable.combineLatest(this.configFlow.subscribeOn(Schedulers.io())
                                                                                                                                  .filter(ctx -> ctx.isChangedNnrfNfm()
                                                                                                                                                 || ctx.isChangedNnrfNfmNrfGroupInstId()
                                                                                                                                                 || ctx.isChangedNnrfDiscCapacity())
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
                                                                                                    .scan(Pair.of(Pair.of(ConfigContext.empty(),
                                                                                                                          ConfigContext.empty()),
                                                                                                                  Pair.of(ConfigContext.empty(),
                                                                                                                          ConfigContext.empty())),
                                                                                                          (r,
                                                                                                           o) -> Pair.of(r.getSecond(), o))
                                                                                                    .subscribeOn(Schedulers.io())
                                                                                                    .flatMapCompletable(updateNfFunction),
                                                                                          Observable.combineLatest(this.configFlow.subscribeOn(Schedulers.io())
                                                                                                                                  .filter(ConfigContext::isChangedNnlfDisc)
                                                                                                                                  .map(ConfigContext::getConfig),
                                                                                                                   NrfDnsCache.singleton()
                                                                                                                              .getResolvedHosts()
                                                                                                                              .toObservable(),
                                                                                                                   Pair::of)
                                                                                                    .subscribeOn(Schedulers.io())
                                                                                                    .flatMapCompletable(updateNlf),
                                                                                          this.configFlow.subscribeOn(Schedulers.io())
                                                                                                         .filter(ConfigContext::isChangedNrlfRateLimiting)
                                                                                                         .map(ConfigContext::getConfig)
                                                                                                         .flatMapCompletable(updateRlf),
                                                                                          this.pvtbService.map(PvtbApiService::run).orElse(Completable.never()),
                                                                                          leadershipLost,
                                                                                          this.shutdownHook.get()))
                                                            .onErrorResumeNext(e -> this.stop().andThen(Completable.error(e)))
                                                            .andThen(this.stop());

        return (this.enableLeaderElection ? election.map(RxLeaderElection::run).orElse(Completable.never()) : Completable.never()).ambWith(managerChain); // Start
                                                                                                                                                          // leader
                                                                                                                                                          // election
                                                                                                                                                          // subsystem
    }

    public Completable stop()
    {
        final Predicate<? super Throwable> logErr = t ->
        {
            log.warn("Ignored Exception during shutdown", t);
            return true;
        };

        return Completable.complete()
                          .andThen(NlfConfigurator.singleton().stop().onErrorComplete(logErr))
                          .andThen(RlfConfigurator.singleton().stop().onErrorComplete(logErr))
                          .andThen(this.certificateObserver.stop().onErrorComplete(logErr))
                          .andThen(this.nfDiscovery.stop().onErrorComplete(logErr))
//                          .andThen(this.alarm.stop().onErrorComplete(logErr))
                          .andThen(this.loadMeter.stop().onErrorComplete(logErr))
                          .andThen(MetricRegister.singleton().stop().onErrorComplete(logErr))
                          .andThen(this.monitored.stop().onErrorComplete(logErr))
                          .andThen(this.nfInstanceAh.stop().onErrorComplete(logErr))
                          .andThen(this.nfInsAhUn.stop().onErrorComplete(logErr))
                          .andThen(this.nnrfNfDiscoveryAh.stop().onErrorComplete(logErr))
                          .andThen(this.unresolvableHostsAh.stop().onErrorComplete(logErr))
                          .andThen(this.ah.stop().onErrorComplete(logErr))
                          .andThen(this.scpService.stop().onErrorComplete(logErr))
                          .andThen(this.cm.getNotificationHandler().stop().onErrorComplete(logErr))
                          .andThen(this.oamWebServer.stopListener().onErrorComplete(logErr))
                          .andThen(this.probeWebServer.stopListener().onErrorComplete(logErr))
                          .andThen(this.webClientProvider.close().onErrorComplete(logErr))
                          .andThen(this.rxEtcd.map(RxEtcd::close).orElse(Completable.complete()).onErrorComplete(logErr))
                          .andThen(this.tlsKeyLogger.map(TlsKeylogger::stop).orElse(Completable.complete()).onErrorComplete(logErr))
                          .andThen(this.alarmHandlerClient.close().onErrorComplete(logErr))
                          .andThen(VertxInstance.get().rxClose().onErrorComplete(logErr));
        // It is not the manager's responsibility to remove the configuration or the
        // schema. Both are needed in case of an upgrade.
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

    private Completable createAcmDefaultConf()
    {
        return this.cmPatch.get("/cm/api/v1/configurations/ietf-netconf-acm") //
                           .flatMapCompletable(resp ->
                           {

                               if (resp.contains("ericsson-scp-manager-1-scp-admin") && resp.contains("ericsson-scp-manager-2-scp-security-admin")
                                   && resp.contains("ericsson-scp-manager-3-scp-read-only"))
                               {
                                   log.info("NACM configuration already exists.");
                                   return Completable.complete();
                               }
                               else
                               {
                                   List<PatchItem> listOfPatches = new ArrayList<>();
                                   String uriPath = "/ietf-netconf-acm:nacm/rule-list/1";

                                   listOfPatches.add(new PatchItem(PatchOperation.ADD, uriPath, "", AcmConfigHandler.generateScpAdminAcmDefaultConfig()));
                                   listOfPatches.add(new PatchItem(PatchOperation.ADD, uriPath, "", AcmConfigHandler.generateScpSecAdminAcmDefaultConfig()));
                                   listOfPatches.add(new PatchItem(PatchOperation.ADD, uriPath, "", AcmConfigHandler.generateScpReadOnlyAcmDefaultConfig()));

                                   return this.cmPatch.patch("/cm/api/v1/configurations/ietf-netconf-acm", listOfPatches)
                                                      .retryWhen(handler -> handler.delay(5, TimeUnit.SECONDS))
                                                      .timeout(5, TimeUnit.MINUTES)
                                                      .doOnComplete(() -> log.info("Default NACM configuration added with PATCH operation."));
                               }
                           })
                           .doOnError(throwable -> log.warn("Default NACM configuration load Failed!", throwable))
                           .doOnComplete(() -> log.info("Default NACM configuration loaded successfully."));
    }

    private void createContenderFile() throws IOException
    {
        File newFile = new File(LEADER_ELECTION_STATE_FILE);

        if (newFile.createNewFile())
            log.debug("Contender file: {} was created", LEADER_ELECTION_STATE_FILE);
        else
            log.debug("Contender file: {} already exists", LEADER_ELECTION_STATE_FILE);
    }

    /**
     * Get the kubernetes scp-worker-services data and store them in a list, to be
     * used from the Validator and Ingress
     * 
     * @param
     * @return
     */
    // TODO use listNamespacedService and get all services that contain
    // "eric-scp-worker", ipv4/2/multvpn services has dependencies with chart
    // configuration and there is no need to check this again here
    private List<V1Service> createK8sServiceList()
    {
        final boolean enableMultiVpn = Boolean.parseBoolean(EnvVars.get(ENV_MULTIVPN_ENABLED));
        log.info("fetching kubernetes eric-scp-worker services data");
        return Single.defer(() ->
        {
            final List<V1Service> svcList = new ArrayList<>();
            svcList.add(api.readNamespacedService("eric-scp-worker", EnvVars.get(ENV_NAMESPACE), null));
            if (enableMultiVpn)
            {
                svcList.add(api.readNamespacedService("eric-scp-worker-2", EnvVars.get(ENV_NAMESPACE), null));
            }
            return Single.just(svcList);

        })
                     .doOnError(e -> log.warn("Failed to retrieve eric-scp-worker services data..retrying"))
                     .retryWhen(new RetryFunction().withDelay(5 * 1000L) // retry after 5 seconds
                                                   .withRetries(5) // give up after 25 seconds
                                                   .create())
                     .doOnError(e ->
                     {
                         if (e instanceof ApiException)
                         {
                             var a = (ApiException) e;
                             log.error("Failed to retrieve eric-scp-worker services data for namespace {}\ne.getResponseBody() : {}",
                                       EnvVars.get(ENV_NAMESPACE),
                                       a.getResponseBody());
                         }
                     })
                     .blockingGet();

    }

    private void deleteContenderFile() throws IOException
    {
        Path path = Paths.get(LEADER_ELECTION_STATE_FILE);

        if (Files.deleteIfExists(path))
            log.debug("Contender file: {} was deleted", LEADER_ELECTION_STATE_FILE);
        else
            log.debug("Contender file: {} does not exist", LEADER_ELECTION_STATE_FILE);
    }

    private Completable deployDefaultAcmGroupConfiguration()
    {
        final var patches = List.of(new PatchItem(PatchOperation.ADD, //
                                                  "/ietf-netconf-acm:nacm/groups", //
                                                  "", //
                                                  new JsonObject()), // push empty groups container in nacm
                                    new PatchItem(PatchOperation.ADD, //
                                                  "/ietf-netconf-acm:nacm/groups/group", //
                                                  "", //
                                                  new JsonArray()), // push empty group list in nacm/groups
                                    new PatchItem(PatchOperation.ADD, //
                                                  "/ietf-netconf-acm:nacm/groups/group", //
                                                  "", //
                                                  AcmConfigHandler.generateDefaultAdminGroupsConfig())); // push list of system groups
        final var errMsg = "Could not create list of NACM groups.";
        final var completeMsg = "NACM groups added successfully with PATCH operation.";

        return checkGroupsExists().flatMapCompletable(res ->
        {
            if (res.equals(Boolean.TRUE))
                return this.patchAcm(patches, errMsg, completeMsg); // push list of system groups
            else
                return Completable.complete() //
                                  .doOnComplete(() -> log.info("Nacm groups already exist.")); // groups already exist, do nothing but log
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

    private Completable startValidator()
    {
        if (params.validatorEnabled)
        {
            log.debug("SCP Validator is enabled");
            return this.cm.getValidationHandler().start(this.oamWebServer, scpValidator, SCHEMA_ERICSSON_SCP, params.validatorName, params.oamServerUri);
        }
        else
        {
            log.debug("SCP Validator is disabled");
            log.debug("Check if SCP Validator is already registered");

            if (this.cm.getValidationHandler().checkValidator(SCHEMA_ERICSSON_SCP, params.validatorName, params.oamServerUri))
                this.cm.getValidationHandler().deleteValidator(SCHEMA_ERICSSON_SCP, params.validatorName, params.oamServerUri);

            return Completable.complete();
        }
    }

    /**
     * Update POD label with leadership status using the k8s API Also, if pod is
     * contending in leader election create an empty contender file.
     * 
     * @param leaderStatus
     * @return
     */
    private Completable updateLeaderElectionState(LeaderStatus leaderStatus)
    {
        final String status = leaderStatus == LeaderStatus.LEADER ? STATUS_LEADER : STATUS_CONTENDER;

        return Completable.fromAction(() ->
        {
            log.info("[ID: {}] leaderStatus: {}", ownId, status);

            try
            {
                // Update the leader election pod labels
                PatchUtils.patch(V1Pod.class, () ->
                // This is a blocking operation
                this.api.patchNamespacedPodCall(EnvVars.get(ENV_POD_NAME),
                                                EnvVars.get(ENV_NAMESPACE),
                                                new V1Patch("[{ \"op\": \"add\", \"path\": \"/metadata/labels/leader\", \"value\": \"" + status + "\" }]"),
                                                null,
                                                null,
                                                null,
                                                null,
                                                null,
                                                null),
                                 V1Patch.PATCH_FORMAT_JSON_PATCH,
                                 this.api.getApiClient());

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
                log.error("Failed to update POD leadership status", e);
                // Ignore failure
            }
        }).subscribeOn(Schedulers.io());
    }
}
