package com.ericsson.sc.scp.manager;

import java.net.URI;

import com.ericsson.utilities.common.EnvVars;

import io.vertx.core.json.JsonObject;

public class ScpManagerInterfacesParameters
{

    public static final ScpManagerInterfacesParameters instance = new ScpManagerInterfacesParameters();

    public final String hostname;
    public final String serviceHostname;
    public final Integer nrfPort;
    public final Integer oamServerPort;
    public final String oamServerCertPath;
    public final String yangServerCaPath;
    public final String mediatorServerCaPath;
    public final String pmServerCaPath;
    public final String mediatorClientCertPath;
    public final String dcedClientCertPath;
    public final String sipTlsRootCaPath;
    public final String mediatorHostname;
    public final Integer mediatorPort;
    public final boolean globalTlsEnabled;
    public final boolean dcedTlsEnabled;
    public final String schemaName;
    public final String validatorName;
    public final Integer k8sProbePort;
    public final URI oamServerUri;
    public final boolean validatorEnabled;
    public final Integer subscribeValidity;
    public final float subscribeRenewal;
    public final Integer subscribeHeartbeat;
    public final String alarmHandlerHostName;
    public final Integer alarmHandlerPort;
    public final String alarmHandlerClientCertPath;
    public final String trafficCertPath;
    public final String trafficCaPath;

    private ScpManagerInterfacesParameters()
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
        this.serviceHostname = EnvVars.get("SERVICE_HOST", "eric-scp-manager");

        // scp manager web server port to be used for incoming requests by mediator,
        // yang-provider and pm-server
        this.oamServerPort = Integer.parseInt(EnvVars.get("SERVICE_OAM_TARGET_PORT", 8082));

        // scp manager web server port to be used for liveness/readiness probes
        this.k8sProbePort = Integer.parseInt(EnvVars.get("SERVICE_K8S_PROBE_TARGET_PORT", 8081));

        // scp manager web server port to be used for nrf discovery responses
        this.nrfPort = Integer.parseInt(EnvVars.get("SERVICE_NRF_TARGET_PORT", 8080));

        // scp-function schema name
        this.schemaName = "ericsson-scp";

        // mediator server hostname and port
        this.mediatorHostname = EnvVars.get("CM_MEDIATOR", "eric-cm-mediator");
        this.mediatorPort = Integer.parseInt(EnvVars.get("CM_MEDIATOR_PORT", this.globalTlsEnabled ? 5004 : 5003));

        // mediator notification subscription validity period, renewal period and
        // heartbeat period
        this.subscribeValidity = Integer.parseInt(EnvVars.get("SUBSCRIBE_VALIDITY", 14400));
        this.subscribeRenewal = Float.parseFloat(EnvVars.get("SUBSCRIBE_RENEWAL", 0.25));
        this.subscribeHeartbeat = Integer.parseInt(EnvVars.get("SUBSCRIBE_HEARTBEAT", 300));

        // scp-manager validator name
        this.validatorName = "scp-validator";
        // scp-manager validator enabled indicator
        this.validatorEnabled = Boolean.parseBoolean(EnvVars.get("VALIDATOR_ENABLED", true));

        // scp-manager web server for URI to be used for the notification subscriptions
        // and validator registration
        String httpPrefix = this.globalTlsEnabled ? "https" : "http";
        this.oamServerUri = URI.create(httpPrefix + "://" + this.serviceHostname + ":" + this.oamServerPort + "/");

        // Certificate to be used by scp manager for tls connection to all external web
        // clients (mediator notifications, yang-provider validations, pm scraping)
        this.oamServerCertPath = EnvVars.get("MANAGER_SERVER_CERT_PATH", "/run/secrets/oam/certificates");

        // CA certificates to be used by scp manager for the verification of all
        // external web client tls connections
        this.yangServerCaPath = EnvVars.get("YANG_SERVER_CA_PATH", "/run/secrets/yang/ca");
        this.mediatorServerCaPath = EnvVars.get("MEDIATOR_SERVER_CA_PATH", "/run/secrets/mediator/ca");
        this.pmServerCaPath = EnvVars.get("PM_SERVER_CA_PATH", "/run/secrets/pm/ca");

        // Alarm Handler service hostname and port
        this.alarmHandlerHostName = EnvVars.get("ALARM_HANDLER_HOSTNAME", "eric-fh-alarm-handler");
        this.alarmHandlerPort = Integer.parseInt(EnvVars.get("ALARM_HANDLER_PORT", this.globalTlsEnabled ? 6006 : 6005));

        // Certificates to be used by scp manager web client for tls connection to
        // alarm handler service for fault indications
        this.alarmHandlerClientCertPath = EnvVars.get("ALARM_HANDLER_CLIENT_CERT_PATH", "/run/secrets/fhah/certificates");

        // Certificates to be used by scp manager web client for tls connection to
        // mediator (mediator schema push, notification subscriptions and validator
        // configuration)
        this.mediatorClientCertPath = EnvVars.get("MANAGER_MEDIATOR_CLIENT_CERT_PATH", "/run/secrets/mediator/certificates");

        // Certificates to be used by scp manager web client for tls connection to
        // dced-sc database
        this.dcedClientCertPath = EnvVars.get("MANAGER_DCEDSC_CLIENT_CERT_PATH", "/run/secrets/dcedsc/certificates");

        // CA certificates to be used by scp manager for the verification of all
        // external web server tls connections
        this.sipTlsRootCaPath = EnvVars.get("SIP_TLS_TRUSTED_ROOT_CA_PATH", "/run/secrets/siptls/ca");

        this.trafficCertPath = EnvVars.get("MANAGER_TRAFFIC_CERTIFICATE_PATH", "/run/secrets/scp-manager/certificates");
        this.trafficCaPath = EnvVars.get("MANAGER_TRAFFIC_ROOT_CA_PATH", "/run/secrets/scp-manager/certificates/trustCA");
    }

    @Override
    public String toString()
    {
        var parameters = new JsonObject();
        parameters.put("hostname", hostname);
        parameters.put("serviceHostname", serviceHostname);
        parameters.put("nrfPort", nrfPort);
        parameters.put("oamServerPort", oamServerPort);
        parameters.put("oamServerCertPath", oamServerCertPath);
        parameters.put("yangServerCaPath", yangServerCaPath);
        parameters.put("mediatorServerCaPath", mediatorServerCaPath);
        parameters.put("pmServerCaPath", pmServerCaPath);
        parameters.put("mediatorClientCertPath", mediatorClientCertPath);
        parameters.put("dcedClientCertPath", dcedClientCertPath);
        parameters.put("sipTlsRootCaPath", sipTlsRootCaPath);
        parameters.put("mediatorHostname", mediatorHostname);
        parameters.put("mediatorPort", mediatorPort);
        parameters.put("globalTlsEnabled", globalTlsEnabled);
        parameters.put("dcedTlsEnabled", dcedTlsEnabled);
        parameters.put("schemaName", schemaName);
        parameters.put("validatorName", validatorName);
        parameters.put("k8sProbePort", k8sProbePort);
        parameters.put("oamServerUri", oamServerUri);
        parameters.put("validatorEnabled", validatorEnabled);
        parameters.put("subscribeValidity", subscribeValidity);
        parameters.put("subscribeRenewal", subscribeRenewal);
        parameters.put("subscribeHeartbeat", subscribeHeartbeat);
        parameters.put("alarmHandlerHostName", alarmHandlerHostName);
        parameters.put("alarmHandlerPort", alarmHandlerPort);
        parameters.put("alarmHandlerClientCertPath", alarmHandlerClientCertPath);
        parameters.put("trafficCertPath", this.trafficCertPath);
        parameters.put("trafficCaPath", this.trafficCaPath);
        return parameters.encode();
    }

}
