package com.ericsson.supreme.kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.commonlibrary.netconf.Netconf11;
import com.ericsson.commonlibrary.netconf.NetconfHandler;
import com.ericsson.commonlibrary.netconf.exceptions.NetconfException;
import com.ericsson.commonlibrary.netconf.properties.NetconfProperties;
import com.ericsson.commonlibrary.netconf.rpc.RpcReply;
import com.ericsson.supreme.api.GeneratedCert;
import com.ericsson.supreme.exceptions.KubernetesClientException;
import com.ericsson.supreme.exceptions.NetconfClientException;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class NetconfClient
{
    private static final Logger log = LoggerFactory.getLogger(NetconfClient.class);

    private static final long RETRY_DELAY_5S = 5000L;
    private static final long RETRIES_30 = 30L;
    private static final String FAILED_CMD = "Failed to send NETCONF command {}";
    private static final String FAILED_CMD_RETRY = "Failed to send NETCONF command {}, retrying: {}";

    private String ip;
    private int port;
    private final String password;
    private final String username;

    /**
     * Automatically fetches the yang provider info from defined namespace and
     * kubeconfig
     * 
     * @param namespace
     * @param kubeconfig
     * @param username
     * @param password
     * @throws NetconfClientException
     */
    public NetconfClient(KubernetesClient kubeClient,
                         String username,
                         String password) throws NetconfClientException
    {
        this.password = password;
        this.username = username;
        init(kubeClient);
    }

    public NetconfClient(String ip,
                         int port,
                         String username,
                         String password)
    {
        this.password = password;
        this.username = username;
        this.ip = ip;
        this.port = port;
    }

    private void init(KubernetesClient kubeClient) throws NetconfClientException
    {
        try
        {
            var info = kubeClient.fetchYangProviderInfo();
            this.ip = info.getFirst();
            this.port = info.getSecond();

            log.info("Yang provider ip {}, port {}", this.ip, this.port);

        }
        catch (KubernetesClientException e)
        {
            throw new NetconfClientException("The yang provider info could not be fetched", e);
        }
    }

    public boolean installCertificate(String keystoreAsymKeyName,
                                      String keystoreCertName,
                                      GeneratedCert cert) throws NetconfClientException
    {

        var certificateRpc = generateCertificateRpc(keystoreAsymKeyName, keystoreCertName, cert.getPkcs12Base64Format(), cert.getPassword());
        return this.install(certificateRpc);
    }

    public boolean installCaItem(String truststoreCaListName,
                                 String truststoreCaName,
                                 GeneratedCert cert) throws NetconfClientException
    {
        var caRpc = generateCaItemRpc(truststoreCaListName, truststoreCaName, cert.getBase64Certificate());
        return this.install(caRpc);
    }

    public boolean installCaList(String truststoreCaListName) throws NetconfClientException
    {
        var caListRpc = generateCaListRpc(truststoreCaListName);
        return this.install(caListRpc);
    }

    private String generateCaListRpc(String truststoreCaListName)
    {
        return String.format(NetconfContext.CA_LIST_TEMPLATE, truststoreCaListName);
    }

    private String generateCaItemRpc(String truststoreCaListName,
                                     String certName,
                                     String certificateBase64p12)
    {
        return String.format(NetconfContext.CA_ITEM_TEMPLATE, truststoreCaListName, certName, certificateBase64p12);
    }

    private String generateCertificateRpc(String asymKeyName,
                                          String certName,
                                          String certificateBase64p12,
                                          String password)
    {
        return String.format(NetconfContext.CERT_TEMPLATE, asymKeyName, certName, certificateBase64p12, password);
    }

    private boolean install(String content) throws NetconfClientException
    {
        log.debug("connecting to the yang provider netconf interface via ssh...");
        RpcReply reply = null;

        log.debug("Netconf message: {}", content);

        var replyOpt = sendRequestSpecial(content, ip, username, password, port, RETRY_DELAY_5S, RETRIES_30);
        if (replyOpt.isPresent())
        {
            reply = replyOpt.get();
            log.debug("Reply: \n{}", reply);
        }

        if (reply != null && !reply.hasError())
        {
            log.debug("Netconf action related to certificates succeeded.");
            return true;
        }

        return false;
    }

    private Optional<RpcReply> sendRequestSpecial(String message,
                                                  String ip,
                                                  String user,
                                                  String pass,
                                                  Integer port,
                                                  Long delay,
                                                  Long retries) throws NetconfClientException
    {
        AtomicReference<Netconf11> session = new AtomicReference<>();

        var reply = Single.fromCallable(() ->
        {
            session.set(initSession(ip, user, pass, port));
            return Optional.of(session.get().send(message));
        })
                          .subscribeOn(Schedulers.io())
                          .doOnError(e -> log.warn(FAILED_CMD, message, e))
                          .retryWhen(new RetryFunction().withDelay(delay)
                                                        .withRetries(retries)
                                                        .withRetryAction((error,
                                                                          retry) ->
                                                        {
                                                            var per = retry * 100 / retries;
                                                            if (per > 80) // if percentage is above 80 report warnings
                                                                log.warn(FAILED_CMD_RETRY, message, retry, error);
                                                            else
                                                                log.info(FAILED_CMD_RETRY, message, retry, error);
                                                        })
                                                        .create())
                          .blockingGet();

        // terminate session is still connected
        this.termSession(session.get());

        return reply;
    }

    private Netconf11 initSession(String ip,
                                  String user,
                                  String pass,
                                  Integer port) throws NetconfClientException
    {
        log.debug("Entropy available: {}", getAvailableEntropy());
        try
        {
            var netconfProperties = new NetconfProperties();
            netconfProperties.setHost(ip);
            netconfProperties.setPort(port);
            netconfProperties.setUsername(user);
            netconfProperties.setPassword(pass);
            netconfProperties.setPrettyEnabled(false);

            var netconf = NetconfHandler.openSshSessionNetconf11(netconfProperties);

            if (netconf.isConnected())
                log.debug("Netconf session initialized successfully with server capabilities\n{}", netconf.getServerCapabilities());
            else
            {
                log.error("No ssh connection to YANG Provider Netconf interface. Netconf is not connected");
                throw new NetconfClientException("Failed to initialize new NETCONF SSH session to " + ip + ":" + port + " as " + user);
            }
            return netconf;
        }
        catch (NetconfException e)
        {
            throw new NetconfClientException("Failed to initialize new NETCONF SSH session to " + ip + ":" + port + " as " + user, e);
        }
    }

    private static String getAvailableEntropy()
    {
        try
        {
            var entropyAvail = Runtime.getRuntime().exec("cat /proc/sys/kernel/random/entropy_avail");
            var stdInput = new BufferedReader(new InputStreamReader(entropyAvail.getInputStream()));
            return stdInput.readLine();
        }
        catch (IOException e)
        {
            return "IOException";
        }

    }

    private void termSession(Netconf11 session) throws NetconfClientException
    {
        if (session.isConnected())
        {
            try
            {
                var sessionProperties = session.getNetconfProperties();
                session.closeSession();

                // checking if close session failed due to hanging command and force disconnect
                if (session.isConnected())
                    session.disconnect();

                // checking if session not disconnected and throw exception
                if (session.isConnected())
                    throw new NetconfClientException("Failed to close netconf session to " + sessionProperties.getHost() + ":" + sessionProperties.getPort()
                                                     + " as " + sessionProperties.getUsername());
            }
            catch (NetconfException e)
            {
                throw new NetconfClientException("Failed to close netconf session.");
            }
        }
        else
            log.debug("No active SSH NETCONF session identified for termination");
    }

    private class NetconfContext
    {
        public static final String CA_LIST_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<rpc message-id=\"1\"\n"
                                                      + "    xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" + "    <edit-config>\n"
                                                      + "        <target>\n" + "            <running/>\n" + "        </target>\n"
                                                      + "        <config xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
                                                      + "            <truststore xmlns=\"urn:ietf:params:xml:ns:yang:ietf-truststore\"\n"
                                                      + "                xmlns:ts=\"urn:ietf:params:xml:ns:yang:ietf-truststore\">\n"
                                                      + "                <certificates>\n" + "                    <name>%s</name>\n"
                                                      + "                    <description>A list of CA certificates</description>\n"
                                                      + "                </certificates>\n" + "            </truststore>\n" + "        </config>\n"
                                                      + "    </edit-config>\n" + "</rpc>\n" + "]]>]]>\n";

        public static final String CA_ITEM_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                                      + "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"> \n"
                                                      + "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\" \n"
                                                      + "    xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\"> \n"
                                                      + "    <truststore xmlns=\"urn:ietf:params:xml:ns:yang:ietf-truststore\"> \n" + "    <certificates>\n"
                                                      + "            <name>%s</name>\n"
                                                      + "            <install-certificate-pem xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-truststore-ext\">\n"
                                                      + "                <name>%s</name>\n" + "                <pem>%s</pem>\n"
                                                      + "            </install-certificate-pem> \n" + "    </certificates>\n" + "    </truststore> \n"
                                                      + "</action> \n" + "</rpc> \n" + "]]>]]>\n";

        public static final String CERT_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                                                   + "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
                                                   + "    <action xmlns=\"urn:ietf:params:xml:ns:yang:1\" xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n"
                                                   + "        <keystore xmlns=\"urn:ietf:params:xml:ns:yang:ietf-keystore\">\n"
                                                   + "            <asymmetric-keys>\n"
                                                   + "                <install-asymmetric-key-pkcs12 xmlns=\"urn:rdns:com:ericsson:oammodel:ericsson-keystore-ext\">\n"
                                                   + "                    <name>%s</name>\n" + "                    <certificate-name>%s</certificate-name>\n"
                                                   + "                    <p12>%s</p12>\n" + "                    <p12-password>%s</p12-password>\n"
                                                   + "                </install-asymmetric-key-pkcs12>\n" + "            </asymmetric-keys>\n"
                                                   + "        </keystore>\n" + "    </action>\n" + "</rpc>\n" + "]]>]]>";
    }

}
