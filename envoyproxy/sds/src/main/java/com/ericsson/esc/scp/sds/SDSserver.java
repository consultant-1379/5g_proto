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
 * Created on: Jan 9, 2020
 *     Author: eaoknkr
 */

package com.ericsson.esc.scp.sds;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmAdapter;
import com.ericsson.sc.certmcrhandler.data.SecretTlsDataList;
import com.ericsson.sc.externalcertificates.ExternalCertificateController;
import com.ericsson.sc.proxyal.service.RxServer;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IP_VERSION;
import com.ericsson.utilities.common.RuntimeEnvironment;
import com.ericsson.utilities.file.CertificateWatch;
import com.ericsson.utilities.file.CertificateWatch.CertNamingConvention;
import com.ericsson.utilities.file.CertificateWatch.CertificateWatcher;
import com.ericsson.utilities.file.CertificateWatch.MonitoredCertificates;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.BehaviorSubject;

/**
 *
 */
public class SDSserver
{
    private static final Logger log = LoggerFactory.getLogger(SDSserver.class);

    private final Flowable<MonitoredCertificates> certificateWatcher;
    private Optional<ExternalCertificateController> extCertController = Optional.empty();

    private final SecretDiscoveryService sds;
    private final RxServer grpcServer;
    private final RxShutdownHook shutdownHook;
    private Optional<RouterHandler> oamWebServer = Optional.empty();

    // Certificate paths
    private static final URI SEPP_ASYM_PATH = URI.create(EnvVars.get("SEPP_CERT_PATH", "/run/secrets/sepp/traf/certificates"));
    private static final URI SEPP_CA_PATH = URI.create(EnvVars.get("SEPP_CA_PATH", "/run/secrets/sepp/traf/certificates/trustedAuthority"));
    private static final URI WORKER_INTERNAL_ASYM_PATH = URI.create(EnvVars.get("INTERNAL_CERT_ASYM_PATH", "/run/secrets/internal/asymmetric"));
    private static final URI SCP_ASYM_PATH = URI.create(EnvVars.get("SCP_CERT_PATH", "/run/secrets/scp/traf/certificates"));
    private static final URI SCP_CA_PATH = URI.create(EnvVars.get("SCP_CA_PATH", "/run/secrets/scp/traf/certificates/trustCA"));
    private static final URI MEDIATOR_CERT_PATH = URI.create(EnvVars.get("MEDIATOR_CLIENT_CERT_PATH", "/run/secrets/mediator/certificates"));
    private static final URI SIP_TLS_TRUSTED_ROOT_CA_PATH = URI.create(EnvVars.get("SIP_TLS_TRUSTED_ROOT_CA_PATH", "/run/secrets/siptls/ca"));
    private static final URI SDS_SERVER_CERT_PATH = URI.create(EnvVars.get("SERVER_CERT_PATH", "/run/secrets/oam/certificates"));
    private static final URI OAM_SERVER_MEDIATOR_CA_PATH = URI.create(EnvVars.get("MEDIATOR_SERVER_CA_PATH", "/run/secrets/mediator/ca"));
    private static final URI OAM_SERVER_PM_CA_PATH = URI.create(EnvVars.get("PM_CA_PATH", "/run/secrets/pm/ca"));
    private static final URI N32C_CA_PATH = URI.create(EnvVars.get("N32C_CA_PATH", "/run/secrets/n32c/ca"));
    private static final URI N32C_CLIENT_CERT_PATH = URI.create(EnvVars.get("N32C_CLIENT_CERT_PATH", "/run/secrets/n32c/client/certificates"));

    private static final Boolean GLOBAL_TLS_ENABLED = Boolean.parseBoolean(EnvVars.get("GLOBAL_TLS_ENABLED"));
    private static final Boolean N32C_RESP_SEPP_TLS_ENABLED = Boolean.parseBoolean(EnvVars.get("N32C_RESP_TLS_ENABLED", true));
    private static final Boolean N32C_INIT_SEPP_TLS_ENABLED = Boolean.parseBoolean(EnvVars.get("N32C_INIT_TLS_ENABLED", true));

    private static final String SDS_HOST_NAME = EnvVars.get("HOSTNAME");
    private static final Integer MEDIATOR_PORT = GLOBAL_TLS_ENABLED.booleanValue() ? 5004 : 5003;
    private static final Integer OAM_SERVER_PORT = 8089;
    private static final String DEFAULT_ROUTE_ADDRESS = "[::]";

    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/worker/config/logcontrol").getPath();
    private static final String CONTAINER_NAME = EnvVars.get("CONTAINER_NAME");
    private IP_VERSION ipVersion = null;

    private Optional<CmAdapter<EricssonSepp>> cm = Optional.empty();
    private WebClientProvider webClientProvider;

    public SDSserver(RxShutdownHook shutdownHook,
                     Class<EricssonSepp> clazz,
                     String schema) throws NumberFormatException
    {
        final var wcb = WebClientProvider.builder().withHostName(SDS_HOST_NAME);
        if (GLOBAL_TLS_ENABLED.booleanValue())
            wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(MEDIATOR_CERT_PATH.getPath()),
                                                            SipTlsCertWatch.trustedCert(SIP_TLS_TRUSTED_ROOT_CA_PATH.getPath())));
        this.webClientProvider = wcb.build(VertxInstance.get());

        Set<String> asym = Arrays.asList(new File(SEPP_ASYM_PATH.getPath()).listFiles(File::isDirectory))
                                 .stream()
                                 .map(File::getName)
                                 .collect(Collectors.toSet());
        log.debug("External Certificates: List of asymmetric keys found: {}", asym);

        Set<String> ca = Arrays.asList(new File(SEPP_CA_PATH.getPath()).listFiles(File::isDirectory)).stream().map(File::getName).collect(Collectors.toSet());
        log.debug("External Certificates: List of CAs found: {}", ca);

        Set<String> internalAsym = Arrays.asList(new File(WORKER_INTERNAL_ASYM_PATH.getPath()).listFiles(File::isDirectory))
                                         .stream()
                                         .map(File::getName)
                                         .collect(Collectors.toSet());
        log.debug("Internal Certificates: List of asymmetric keys found: {}", internalAsym);

        var cwb = CertificateWatch.create()
                                  .withCertificateWatcher(CertificateWatcher.create()
                                                                            .withAsymKeyRoot(SEPP_ASYM_PATH.getPath()) //
                                                                            .withCaRoot(SEPP_CA_PATH.getPath()) //
                                                                            .withAsymmetricKeys(asym) //
                                                                            .withCaCerts(ca) //
                                                                            .withCertificateNamingConvention(CertNamingConvention.EXTERNAL) //
                                                                            .build())
                                  .withCertificateWatcher(CertificateWatcher.create()
                                                                            .withAsymKeyRoot(WORKER_INTERNAL_ASYM_PATH.getPath()) //
                                                                            .withCaRoot(SIP_TLS_TRUSTED_ROOT_CA_PATH.getPath()) //
                                                                            .withDefaultCaName("internal_ca") //
                                                                            .withAsymmetricKeys(internalAsym) //
                                                                            .withCertificateNamingConvention(CertNamingConvention.INTERNAL) //
                                                                            .build())
                                  .withCertificateWatcher(CertificateWatcher.create()
                                                                            .withAsymKeyRoot(SDS_SERVER_CERT_PATH.getPath()) //
                                                                            .withDefaultAsymKeyName("pm_worker_server_cert") //
                                                                            .withCaRoot(OAM_SERVER_PM_CA_PATH.getPath()) //
                                                                            .withDefaultCaName("internal_pm_ca") //
                                                                            .withCertificateNamingConvention(CertNamingConvention.INTERNAL) //
                                                                            .build());
        if (N32C_INIT_SEPP_TLS_ENABLED.booleanValue())
        {
            cwb.withCertificateWatcher(CertificateWatcher.create()
                                                         .withAsymKeyRoot(SDS_SERVER_CERT_PATH.getPath()) //
                                                         .withDefaultAsymKeyName("n32c_server_cert") //
                                                         .withCaRoot(N32C_CA_PATH.getPath()) //
                                                         .withDefaultCaName("internal_n32c_client_ca") //
                                                         .withCertificateNamingConvention(CertNamingConvention.INTERNAL) //
                                                         .build());
        }
        if (N32C_RESP_SEPP_TLS_ENABLED.booleanValue())
        {
            cwb.withCertificateWatcher(CertificateWatcher.create()
                                                         .withAsymKeyRoot(N32C_CLIENT_CERT_PATH.getPath()) //
                                                         .withDefaultAsymKeyName("n32c_client_cert") //
                                                         .withCaRoot(SIP_TLS_TRUSTED_ROOT_CA_PATH.getPath()) //
                                                         .withDefaultCaName("internal_n32c_server_ca") //
                                                         .withCertificateNamingConvention(CertNamingConvention.INTERNAL) //
                                                         .build());
        }
        this.certificateWatcher = cwb.build().watch();

        var ows = WebServer.builder().withHost(DEFAULT_ROUTE_ADDRESS).withPort(OAM_SERVER_PORT);
        if (GLOBAL_TLS_ENABLED.booleanValue())
            ows.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(SDS_SERVER_CERT_PATH.getPath()),
                                                            SipTlsCertWatch.trustedCert(OAM_SERVER_MEDIATOR_CA_PATH.getPath())));
        this.oamWebServer = Optional.of(ows.build(VertxInstance.get()));

        this.cm = Optional.of(new CmAdapter<>(clazz,
                                              schema,
                                              VertxInstance.get(),
                                              MEDIATOR_PORT,
                                              EnvVars.get("CM_MEDIATOR", "eric-cm-mediator"),
                                              this.webClientProvider,
                                              GLOBAL_TLS_ENABLED,
                                              Integer.parseInt(EnvVars.get("SUBSCRIBE_VALIDITY")),
                                              Float.parseFloat(EnvVars.get("SUBSCRIBE_RENEWAL")),
                                              Integer.parseInt(EnvVars.get("SUBSCRIBE_HEARTBEAT"))));

        var configFlow = BehaviorSubject.<Optional<EricssonSepp>>create();
        this.cm.get().getNotificationHandler().getConfiguration().subscribe(configFlow);

        var secretWatcherFlow = BehaviorSubject.<SecretTlsDataList>create();
        // start secret watcher
        this.extCertController = Optional.ofNullable(new ExternalCertificateController());
        this.extCertController.ifPresent((extCert -> extCert.getSecretWatcher().getTrafficSecretDataSbj().subscribe(secretWatcherFlow)));

        this.sds = new SeppSecretDiscoveryService(this.certificateWatcher, secretWatcherFlow, configFlow);
        this.shutdownHook = shutdownHook;
        this.grpcServer = new RxServer("/mnt/sds_uds", sds);
    }

    public SDSserver(RxShutdownHook shutdownHook)
    {
        Set<String> internalAsym = Arrays.asList(new File(WORKER_INTERNAL_ASYM_PATH.getPath()).listFiles(File::isDirectory))
                                         .stream()
                                         .map(File::getName)
                                         .collect(Collectors.toSet());
        log.debug("Internal Certificates: List of asymmetric keys found: {}", internalAsym);

        this.certificateWatcher = CertificateWatch.create()
                                                  .withCertificateWatcher(CertificateWatcher.create()
                                                                                            .withAsymKeyRoot(SCP_ASYM_PATH.getPath()) //
                                                                                            .withDefaultAsymKeyName("default_asym") //
                                                                                            .withCaRoot(SCP_CA_PATH.getPath()) //
                                                                                            .withCertificateNamingConvention(CertNamingConvention.EXTERNAL) //
                                                                                            .withDefaultCaName("default_ca") //
                                                                                            .build())
                                                  .withCertificateWatcher(CertificateWatcher.create()
                                                                                            .withAsymKeyRoot(WORKER_INTERNAL_ASYM_PATH.getPath()) //
                                                                                            .withCaRoot(SIP_TLS_TRUSTED_ROOT_CA_PATH.getPath())
                                                                                            .withDefaultCaName("internal_ca") //
                                                                                            .withAsymmetricKeys(internalAsym) //
                                                                                            .withCertificateNamingConvention(CertNamingConvention.INTERNAL) //

                                                                                            .build())
                                                  .withCertificateWatcher(CertificateWatcher.create()
                                                                                            .withAsymKeyRoot(SDS_SERVER_CERT_PATH.getPath()) //
                                                                                            .withDefaultAsymKeyName("pm_worker_server_cert") //
                                                                                            .withCaRoot(OAM_SERVER_PM_CA_PATH.getPath()) //
                                                                                            .withDefaultCaName("internal_pm_ca") //
                                                                                            .withCertificateNamingConvention(CertNamingConvention.INTERNAL) //
                                                                                            .build())
                                                  .build()
                                                  .watch();
        this.sds = new SecretDiscoveryService(this.certificateWatcher);
        this.grpcServer = new RxServer("/mnt/sds_uds", sds);
        this.shutdownHook = shutdownHook;
    }

    public Completable run()
    {
        return Completable.complete() //
                          .andThen(this.oamWebServer.map(RouterHandler::startListener).orElse(Completable.complete()))
                          .andThen(this.cm.map(adapter -> adapter.getNotificationHandler().start(this.oamWebServer.get())).orElse(Completable.complete()))
                          .andThen(this.grpcServer.start())
                          .andThen(this.shutdownHook.get())
                          .andThen(this.stop());
    }

    public Completable stop()
    {
        final Predicate<? super Throwable> logErr = t ->
        {
            log.info("Ignored Exception during shutdown", t);
            return true;
        };
        log.info("Stopping SDS");
        return Completable.complete()
                          .andThen(Completable.fromRunnable(() -> this.extCertController.ifPresent(extCert -> extCert.stopSDS().onErrorComplete(logErr))))
                          .andThen(this.grpcServer.stop().onErrorComplete())
                          .andThen(this.cm.map(adapter -> adapter.getNotificationHandler().stop().onErrorComplete()).orElse(Completable.complete()))
                          .andThen(this.oamWebServer.map(server -> server.stopListener().onErrorComplete()).orElse(Completable.complete()))
                          .andThen(VertxInstance.get().rxClose().onErrorComplete());
    }

    public static void main(String[] args)
    {
        var exitStatus = 0;
        log.info("Starting SDS");
        try (var shutdownHook = new RxShutdownHook();
             var llc = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(), CONTAINER_NAME))
        {
            if (EnvVars.get("NF") != null && EnvVars.get("NF").equals("sepp"))
            {
                final var sdsServer = new SDSserver(shutdownHook, EricssonSepp.class, "ericsson-sepp");
                sdsServer.run().blockingAwait();
            }
            else
            {
                final var sdsServer = new SDSserver(shutdownHook);
                sdsServer.run().blockingAwait();
            }
        }
        catch (final Exception t)
        {
            log.error("Exception caught, exiting.", t);
            exitStatus = 1;
        }
        log.info("Stopped SDS.");
        System.exit(exitStatus);
    }

    public boolean isVersionIpv4()
    {
        return this.ipVersion != null ? this.ipVersion.equals(IP_VERSION.IPV4) : RuntimeEnvironment.getDeployedIpVersion().equals(IP_VERSION.IPV4);
    }
}
