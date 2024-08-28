package com.ericsson.sc.rlf;

import com.ericsson.utilities.common.EnvVars;
import io.vertx.core.json.JsonObject;

public class RlfWorkerInterfacesParameters
{
    public static final RlfWorkerInterfacesParameters instance = new RlfWorkerInterfacesParameters();
    public final String hostname;
    public final int portInternal;
    public final int portRest;
    public final int portRestOam;
    public final int concurrentStreamsMax;
    public final String serviceHostname;
    public final Integer oamServerPort;
    public final int k8sProbeIfPort;
    public final boolean globalTlsEnabled;
    public final String mediatorHostname;
    public final Integer mediatorPort;
    public final String mediatorIfServerCaPath;
    public final String mediatorIfClientCertPath;
    public final String mediatorIfClientCaPath;
    public final String managerIfServerCertPath;
    public final String managerIfClientCaPath;
    public final String oamServerCertPath;
    public final String pmServerCaPath;
    public final boolean dcedTlsEnabled;
    public final String dcedClientCertPath;
    public final String sipTlsRootCaPath;

    private RlfWorkerInterfacesParameters()
    {
        this.hostname = EnvVars.get("HOSTNAME");
        this.portInternal = Integer.parseInt(EnvVars.get("SERVICE_TARGET_PORT_INTERNAL", 8085));
        this.portRest = Integer.parseInt(EnvVars.get("SERVICE_TARGET_PORT_REST", 8081));
        this.portRestOam = Integer.parseInt(EnvVars.get("SERVICE_TARGET_PORT_REST_OAM", 8080));
        this.serviceHostname = EnvVars.get("SERVICE_HOST", "eric-sc-rlf");

        // rlf worker web server port to be used for incoming requests by mediator
        this.oamServerPort = Integer.parseInt(EnvVars.get("SERVICE_OAM_TARGET_PORT", 8082));
        this.concurrentStreamsMax = (int) Float.parseFloat(EnvVars.get("CONCURRENT_STREAMS_MAX", 20));
        this.k8sProbeIfPort = Integer.parseInt(EnvVars.get("SERVICE_K8S_PROBE_TARGET_PORT", 8085));
        this.globalTlsEnabled = Boolean.parseBoolean(EnvVars.get("GLOBAL_TLS_ENABLED"));
        this.mediatorHostname = EnvVars.get("CM_MEDIATOR", "eric-cm-mediator");
        this.mediatorPort = Integer.parseInt(EnvVars.get("CM_MEDIATOR_PORT", this.globalTlsEnabled ? 5004 : 5003));
        this.mediatorIfServerCaPath = EnvVars.get("OAM_SERVER_MEDIATOR_CA_PATH", "/interfaces/server/oam/certificates/mediator");
        this.mediatorIfClientCertPath = EnvVars.get("MEDIATOR_INTERFACE_CLIENT_CERTIFICATES_PATH", "/interfaces/mediator/client/certificates");
        this.mediatorIfClientCaPath = EnvVars.get("MEDIATOR_INTERFACE_CLIENT_CA_PATH", "/interfaces/mediator/client/certificates/trustCA");
        this.managerIfServerCertPath = EnvVars.get("MANAGERS_INTERFACE_SERVER_CERT_PATH", "/run/secrets/rlf-manager-server-cert");
        this.managerIfClientCaPath = EnvVars.get("MANAGERS_INTERFACE_CLIENT_CA_PATH", "/run/secrets/client-ca");

        // Certificate to be used by rlf worker for tls connection to all external web
        // clients (mediator notifications, yang-provider validations, pm scraping)
        this.oamServerCertPath = EnvVars.get("OAM_SERVER_CERT_PATH", "/interfaces/server/oam/certificates");

        this.pmServerCaPath = EnvVars.get("PM_SERVER_CA_PATH", "/run/secrets/pm/ca");

        // CA certificates to be used by rlf for the verification of all
        // external web server tls connections
        this.sipTlsRootCaPath = EnvVars.get("SIP_TLS_TRUSTED_ROOT_CA_PATH", "/run/secrets/siptls/ca");

        // dced-sc tls enable indicator
        // disabled if global tls is disabled
        // optionally may be disabled if global tls is enabled
        this.dcedTlsEnabled = Boolean.parseBoolean(EnvVars.get("DCED_TLS_ENABLED"));

        // Certificates to be used by rlf web client for tls connection to
        // dced-sc database
        this.dcedClientCertPath = EnvVars.get("RLF_DCEDSC_CLIENT_CERT_PATH", "/run/secrets/dcedsc/certificates");
    }

    @Override
    public String toString()
    {
        var parameters = new JsonObject();
        parameters.put("hostname", hostname);
        parameters.put("portInternal", portInternal);
        parameters.put("portRest", portRest);
        parameters.put("portRestOam", portRestOam);
        parameters.put("serviceHostname", serviceHostname);
        parameters.put("concurrentStreamsMax", concurrentStreamsMax);
        parameters.put("k8sProbeIfPort", k8sProbeIfPort);
        parameters.put("globalTlsEnabled", globalTlsEnabled);
        parameters.put("mediatorHostname", mediatorHostname);
        parameters.put("mediatorPort", mediatorPort);
        parameters.put("mediatorIfServerCaPath", mediatorIfServerCaPath);
        parameters.put("mediatorIfClientCertPath", mediatorIfClientCertPath);
        parameters.put("mediatorIfClientCaPath", mediatorIfClientCaPath);
        parameters.put("managerIfServerCertPath", managerIfServerCertPath);
        parameters.put("managerIfClientCaPath", managerIfClientCaPath);
        parameters.put("oamServerCertPath", oamServerCertPath);
        parameters.put("pmServerCaPath", pmServerCaPath);
        parameters.put("oamServerPort", oamServerPort);
        parameters.put("sipTlsRootCaPath", sipTlsRootCaPath);
        return parameters.encode();
    }

}
