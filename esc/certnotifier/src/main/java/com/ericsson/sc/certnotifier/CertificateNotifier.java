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
 * Created on: May 4, 2020
 *     Author: eaopmrk
 */

package com.ericsson.sc.certnotifier;

import java.io.File;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IP_VERSION;
import com.ericsson.utilities.common.RuntimeEnvironment;
import com.ericsson.utilities.file.CertificateWatch;
import com.ericsson.utilities.file.CertificateWatch.CertNamingConvention;
import com.ericsson.utilities.file.CertificateWatch.CertificateWatcher;
import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 *
 */
public class CertificateNotifier
{

    private static final Logger log = LoggerFactory.getLogger(CertificateNotifier.class);

    private static final String LOG_CONTROL_FILE = "logcontrol.json";
    private static final String LOG_CONTROL_PATH = URI.create("/worker/config/logcontrol").getPath();
    private static final String CONTAINER_NAME = EnvVars.get("CONTAINER_NAME");

    private final RxShutdownHook shutdownHook;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        var exitStatus = 0;
        log.info("Starting Certificate Notifier.");

        try (var shutdownHook = new RxShutdownHook();
             var llc = new LogLevelChanger(ConfigmapWatch.builder().withFileName(LOG_CONTROL_FILE).withRoot(LOG_CONTROL_PATH).build(), CONTAINER_NAME))
        {
            final var certNotifier = new CertificateNotifier(shutdownHook);
            certNotifier.run().blockingAwait();
        }
        catch (final Exception e)
        {
            log.error("Exception caught. Exiting ", e);
            exitStatus = 1;
        }

        log.info("Stopped Certificate Notifier.");

        System.exit(exitStatus);
    }

    private final CertificateAlarmHandlerClient certAlarmHandlerClient;
    private final CertificateServer certServer;
    private final WebServer webServer;
    private static final int WEBSERVER_PORT = 8088;
    private static final int CERT_ALARM_HANDLER_PORT = 8089;
    private static final String DEFAULT_ROUTE_ADDRESS = "[::]";

    private static final String SEPP_ASYM_PATH = EnvVars.get("SEPP_CERTIFICATE_PATH", "/run/secrets/sepp/traf/certificates");
    private static final String SEPP_CA_PATH = EnvVars.get("SEPP_ROOT_CA_PATH", "/run/secrets/sepp/traf/certificates/trustedAuthority");

    private static final String DEFAULT_ASYM_PATH = EnvVars.get("DEFAULT_CERTIFICATE_PATH", "/run/secrets/certnotifier/certificates/");
    private static final String DEFAULT_CA_PATH = EnvVars.get("DEFAULT_ROOT_CA_PATH", "/run/secrets/certnotifier/certificates/trustCA");

    private static final String DEFAULT_ASYM_NAME = "default_asym";
    private static final String DEFAULT_CA_NAME = "default_ca";
    private IP_VERSION ipVersion = null;

    public CertificateNotifier(RxShutdownHook shutdownHook) throws UnknownHostException
    {
        this.shutdownHook = shutdownHook;
        this.webServer = WebServer.builder().withHost(DEFAULT_ROUTE_ADDRESS).withPort(WEBSERVER_PORT).build(VertxInstance.get());

        Flowable<ListNotification> certificateFlow;

        if (EnvVars.get("NF") != null && EnvVars.get("NF").equals("sepp"))
        {
            Set<String> asym = Arrays.asList(new File(SEPP_ASYM_PATH).listFiles(File::isDirectory)).stream().map(File::getName).collect(Collectors.toSet());
            log.info("List of asymmetric keys found: {}", asym);

            Set<String> ca = Arrays.asList(new File(SEPP_CA_PATH).listFiles(File::isDirectory)).stream().map(File::getName).collect(Collectors.toSet());
            log.info("List of CAs found: {}", ca);

            certificateFlow = CertificateWatch.create()
                                              .withCertificateWatcher(CertificateWatcher.create()
                                                                                        .withAsymKeyRoot(SEPP_ASYM_PATH) //
                                                                                        .withCaRoot(SEPP_CA_PATH) //
                                                                                        .withAsymmetricKeys(asym) //
                                                                                        .withCaCerts(ca) //
                                                                                        .withCertificateNamingConvention(CertNamingConvention.EXTERNAL) //
                                                                                        .build()) //
                                              .build()
                                              .watch()
                                              .map(monitoredCerts ->
                                              {

                                                  var asymKeys = monitoredCerts.getAllAsymKeys()
                                                                               .entrySet()
                                                                               .stream()
                                                                               .map(e -> new AsymmetricKey(e.getKey(),
                                                                                                           !e.getValue().getKey().isBlank(),
                                                                                                           !e.getValue().getCertificate().isBlank()))
                                                                               .collect(Collectors.toList());

                                                  var cas = monitoredCerts.getAllCaCerts()
                                                                          .entrySet()
                                                                          .stream()
                                                                          .map(e -> new TrustedAuthority(e.getKey(), !e.getValue().getCa().isBlank()))
                                                                          .collect(Collectors.toList());

                                                  return new ListNotification(asymKeys, cas);
                                              });
        }
        else
        {
            certificateFlow = CertificateWatch.create()
                                              .withCertificateWatcher(CertificateWatcher.create() //
                                                                                        .withAsymKeyRoot(DEFAULT_ASYM_PATH) //
                                                                                        .withDefaultAsymKeyName(DEFAULT_ASYM_NAME) //
                                                                                        .withCaRoot(DEFAULT_CA_PATH) //
                                                                                        .withDefaultCaName(DEFAULT_CA_NAME) //
                                                                                        .withCertificateNamingConvention(CertNamingConvention.EXTERNAL) //
                                                                                        .build()) //
                                              .build()
                                              .watch()
                                              .map(monitoredCerts ->
                                              {
                                                  var notification = new ListNotification();
                                                  // always one asym key
                                                  var tempAsym = monitoredCerts.getAsymmetricKey(DEFAULT_ASYM_NAME);
                                                  if (tempAsym != null)
                                                  {
                                                      var ak = new AsymmetricKey(tempAsym.getName(),
                                                                                 !tempAsym.getKey().isBlank(),
                                                                                 !tempAsym.getCertificate().isBlank());

                                                      notification.withAsymmetricKeys(List.of(ak));
                                                  }

                                                  var tempCa = monitoredCerts.getCaCert(DEFAULT_CA_NAME);
                                                  if (tempCa != null)
                                                  {
                                                      var cas = new TrustedAuthority(tempCa.getName(), !tempCa.getCa().isBlank());
                                                      notification.withTrustedAuthorities(List.of(cas));
                                                  }

                                                  return notification;
                                              });
        }

        this.certServer = new CertificateServer(this.webServer, certificateFlow);
        this.certAlarmHandlerClient = new CertificateAlarmHandlerClient("eric-sc-manager", CERT_ALARM_HANDLER_PORT, certificateFlow);
    }

    public Completable run()
    {
        return Completable.complete()//
                          .andThen(this.certServer.start())
                          .andThen(Completable.ambArray(this.certAlarmHandlerClient.run(), this.shutdownHook.get()))
                          .onErrorResumeNext(throwable -> stop().andThen(Completable.error(throwable)))
                          .andThen(this.stop());
    }

    public Completable stop()
    {
        return Completable.complete()//
                          .andThen(this.certServer.stop().onErrorComplete())
                          .andThen(certAlarmHandlerClient.stop());
    }

    public boolean isVersionIpv4()
    {
        return this.ipVersion != null ? this.ipVersion.equals(IP_VERSION.IPV4) : RuntimeEnvironment.getDeployedIpVersion().equals(IP_VERSION.IPV4);
    }
}
