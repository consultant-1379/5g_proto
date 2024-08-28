package com.ericsson.sc.bsf.diameter;

import java.net.URI;

import com.ericsson.utilities.common.EnvVars;

import io.vertx.core.json.JsonObject;

public class BsfDiameterInterfacesParameters
{

    public static final BsfDiameterInterfacesParameters instance = new BsfDiameterInterfacesParameters();

    public final String hostname;
    public final String serviceHostname;
    public final Integer oamServerPort;
    public final Integer probeServerPort;
    public final String oamServerCertPath;
    public final String mediatorServerCaPath;
    public final String pmServerCaPath;
    public final String mediatorClientCertPath;
    public final String dcedClientCertPath;
    public final String wcdbcdClientCertPath;
    public final String sipTlsRootCaPath;
    public final String mediatorHostname;
    public final Integer mediatorPort;
    public final boolean globalTlsEnabled;
    public final boolean dcedTlsEnabled;
    public final String schemaName;
    public final String validatorName;
    public final URI oamServerUri;
    public final boolean validatorEnabled;
    public final Integer subscribeValidity;
    public final float subscribeRenewal;
    public final Integer subscribeHeartbeat;

    private BsfDiameterInterfacesParameters()
    {
        // global.ericsson.tls.enabled indicator
        this.globalTlsEnabled = Boolean.parseBoolean(EnvVars.get("GLOBAL_TLS_ENABLED"));

        // dced-sc tls enable indicator
        // disabled if global tls is disabled
        // optionally may be disabled if global tls is enabled
        this.dcedTlsEnabled = Boolean.parseBoolean(EnvVars.get("DCED_TLS_ENABLED"));

        // pod name
        this.hostname = EnvVars.get("HOSTNAME");

        // service name
        this.serviceHostname = EnvVars.get("SERVICE_HOST", "eric-bsf-diameter");

        // bsf diameter web server port to be used for incoming requests by mediator and
        // pm-server
        this.oamServerPort = Integer.parseInt(EnvVars.get("SERVICE_OAM_TARGET_PORT", 8082));

        // bsf diameter web server port to be used for incoming requests by
        // liveness/readiness probes
        this.probeServerPort = Integer.parseInt(EnvVars.get("SERVICE_PROBE_PORT", 8081));

        // bsf-function schema name
        this.schemaName = "ericsson-bsf";

        // mediator server hostname and port
        this.mediatorHostname = EnvVars.get("CM_MEDIATOR", "eric-cm-mediator");
        this.mediatorPort = Integer.parseInt(EnvVars.get("CM_MEDIATOR_PORT", this.globalTlsEnabled ? 5004 : 5003));

        // mediator notification subscription validity period, renewal period and
        // heartbeat period
        this.subscribeValidity = Integer.parseInt(EnvVars.get("SUBSCRIBE_VALIDITY"));
        this.subscribeRenewal = Float.parseFloat(EnvVars.get("SUBSCRIBE_RENEWAL"));
        this.subscribeHeartbeat = Integer.parseInt(EnvVars.get("SUBSCRIBE_HEARTBEAT"));

        // bsf-diameter validator name
        this.validatorName = "bsf-validator";

        // bsf-diameter validator enabled indicator
        this.validatorEnabled = Boolean.parseBoolean(EnvVars.get("VALIDATOR_ENABLED"));

        // bsf-diameter web server for URI to be used for the notification subscriptions
        String httpPrefix = this.globalTlsEnabled ? "https" : "http";
        this.oamServerUri = URI.create(httpPrefix + "://" + this.serviceHostname + ":" + this.oamServerPort + "/");

        // Certificate to be used by bsf diameter for tls connection to all external web
        // clients (mediator notifications, yang-provider validations, pm scraping)
        this.oamServerCertPath = EnvVars.get("DIAMETER_SERVER_CERT_PATH", "/run/secrets/oam/certificates");

        // CA certificates to be used by bsf diameter for the verification of all
        // external web client tls connections
        this.mediatorServerCaPath = EnvVars.get("MEDIATOR_SERVER_CA_PATH", "/run/secrets/mediator/ca");
        this.pmServerCaPath = EnvVars.get("PM_SERVER_CA_PATH", "/run/secrets/pm/ca");

        // Certificates to be used by bsf diameter web client for tls connection to
        // mediator (mediator schema push, notification subscriptions and validator
        // configuration)
        this.mediatorClientCertPath = EnvVars.get("DIAMETER_MEDIATOR_CLIENT_CERT_PATH", "/run/secrets/mediator/certificates");

        // Certificates to be used by bsf diameter web client for tls connection to
        // dced-sc database
        this.dcedClientCertPath = EnvVars.get("DIAMETER_DCEDSC_CLIENT_CERT_PATH", "/run/secrets/dcedsc/certificates");

        // Certificates to be used by bsf diameter web client for tls connection to
        // wcdbcd database
        this.wcdbcdClientCertPath = EnvVars.get("DIAMETER_WCDBCD_CLIENT_CERT_PATH", "/run/secrets/wcdbcd/certificates");

        // CA certificates to be used by bsf diameter for the verification of all
        // external web server tls connections
        this.sipTlsRootCaPath = EnvVars.get("SIP_TLS_TRUSTED_ROOT_CA_PATH", "/run/secrets/siptls/ca");
    }

    @Override
    public String toString()
    {
        final var parameters = new JsonObject();
        parameters.put("hostname", hostname);
        parameters.put("serviceHostname", serviceHostname);
        parameters.put("oamServerPort", oamServerPort);
        parameters.put("probeServerPort", probeServerPort);
        parameters.put("oamServerCertPath", oamServerCertPath);
        parameters.put("mediatorServerCaPath", mediatorServerCaPath);
        parameters.put("pmServerCaPath", pmServerCaPath);
        parameters.put("mediatorClientCertPath", mediatorClientCertPath);
        parameters.put("dcedClientCertPath", dcedClientCertPath);
        parameters.put("wcdbcdClientCertPath", wcdbcdClientCertPath);
        parameters.put("sipTlsRootCaPath", sipTlsRootCaPath);
        parameters.put("mediatorHostname", mediatorHostname);
        parameters.put("mediatorPort", mediatorPort);
        parameters.put("globalTlsEnabled", globalTlsEnabled);
        parameters.put("dcedTlsEnabled", dcedTlsEnabled);
        parameters.put("schemaName", schemaName);
        parameters.put("validatorName", validatorName);
        parameters.put("oamServerUri", oamServerUri);
        parameters.put("validatorEnabled", validatorEnabled);
        parameters.put("subscribeValidity", subscribeValidity);
        parameters.put("subscribeRenewal", subscribeRenewal);
        parameters.put("subscribeHeartbeat", subscribeHeartbeat);
        return parameters.encode();
    }

}
