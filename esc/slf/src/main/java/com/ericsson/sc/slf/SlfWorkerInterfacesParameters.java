package com.ericsson.sc.slf;

import java.net.URI;

import com.ericsson.utilities.common.EnvVars;

import io.vertx.core.json.JsonObject;

public class SlfWorkerInterfacesParameters
{
    public static final SlfWorkerInterfacesParameters instance = new SlfWorkerInterfacesParameters();

    public final String hostname;
    public final String serviceHostname;
    public final Integer oamServerPort;
    public final String oamServerCertPath;
    public final String mediatorServerCaPath;
    public final String pmServerCaPath;
    public final String workerIfClientCaPath;
    public final String slfMediatorClientCertPath;
    public final String sipTlsRootCaPath;
    public final String mediatorHostname;
    public final Integer mediatorPort;
    public final boolean globalTlsEnabled;
    public final String schemaName;
    public final Integer k8sProbeIfPort;
    public final URI mediatorUri;
    public final Integer subscribeValidity;
    public final float subscribeRenewal;
    public final Integer subscribeHeartbeat;
    public final String alarmHandlerHostName;
    public final Integer alarmHandlerPort;
    public final String alarmHandlerClientCertPath;
    public final String dcedClientCertPath;
    public final boolean dcedTlsEnabled;

    private SlfWorkerInterfacesParameters()
    {
        // global.ericsson.tls.enabled indicator
        this.globalTlsEnabled = Boolean.parseBoolean(EnvVars.get("GLOBAL_TLS_ENABLED", "true"));

        // pod name
        this.hostname = EnvVars.get("HOSTNAME");

        // service name
        this.serviceHostname = EnvVars.get("SERVICE_HOST", "eric-scp-manager");

        // slf worker web server port to be used for incoming requests by mediator
        this.oamServerPort = Integer.parseInt(EnvVars.get("SERVICE_OAM_TARGET_PORT", 8082));

        // slf worker web server port to be used for incoming requests by
        // liveness/readiness probes and for sc-monitor incoming requests
        this.k8sProbeIfPort = Integer.parseInt(EnvVars.get("SERVICE_K8S_PROBE_TARGET_PORT", 8085));

        // scp-function schema name
        this.schemaName = "ericsson-scp";

        // mediator server hostname and port
        this.mediatorHostname = EnvVars.get("CM_MEDIATOR", "eric-cm-mediator");
        this.mediatorPort = Integer.parseInt(EnvVars.get("CM_MEDIATOR_PORT", this.globalTlsEnabled ? 5004 : 5003));

        // mediator notification subscription validity period, renewal period and
        // heartbeat period
        this.subscribeValidity = Integer.parseInt(EnvVars.get("SUBSCRIBE_VALIDITY", -1));
        this.subscribeRenewal = Float.parseFloat(EnvVars.get("SUBSCRIBE_RENEWAL", -1));
        this.subscribeHeartbeat = Integer.parseInt(EnvVars.get("SUBSCRIBE_HEARTBEAT", -1));

        // slf worker web server for URI to be used for the notification subscriptions
        // and validator registration
        String httpPrefix = this.globalTlsEnabled ? "https" : "http";
        this.mediatorUri = URI.create(httpPrefix + "://" + this.serviceHostname + ":" + this.oamServerPort + "/");

        // Certificate to be used by slf worker for tls connection to all external web
        // clients (mediator notifications, yang-provider validations, pm scraping)
        this.oamServerCertPath = EnvVars.get("SLF_SERVER_CERT_PATH", "/run/secrets/oam/certificates");

        // CA certificates to be used by slf worker for the verification of all
        // external web client tls connections
        this.mediatorServerCaPath = EnvVars.get("MEDIATOR_SERVER_CA_PATH", "/run/secrets/mediator/ca");
        this.pmServerCaPath = EnvVars.get("PM_SERVER_CA_PATH", "/run/secrets/pm/ca");

        // Alarm Handler service hostname and port
        this.alarmHandlerHostName = EnvVars.get("ALARM_HANDLER_HOSTNAME", "eric-fh-alarm-handler");
        this.alarmHandlerPort = Integer.parseInt(EnvVars.get("ALARM_HANDLER_PORT", this.globalTlsEnabled ? 6006 : 6005));

        // Certificates to be used by slf worker web client for tls connection to
        // alarm handler service for fault indications
        this.alarmHandlerClientCertPath = EnvVars.get("ALARM_HANDLER_CLIENT_CERT_PATH", "/run/secrets/fhah/certificates");

        // Certificates to be used by slf worker web client for tls connection to
        // mediator (mediator schema push, notification subscriptions and validator
        // configuration)
        this.slfMediatorClientCertPath = EnvVars.get("SLF_MEDIATOR_CLIENT_CERT_PATH", "/run/secrets/mediator/certificates");

        // CA certificates to be used by slf worker for the verification of all
        // external web server tls connections
        this.sipTlsRootCaPath = EnvVars.get("SIP_TLS_TRUSTED_ROOT_CA_PATH", "/run/secrets/siptls/ca");

        // CA certificate to verify envoy worker clients connected with SLF worker
        this.workerIfClientCaPath = EnvVars.get("WORKER_CLIENT_CA_PATH", "/run/secrets/worker/ca");

        // Certificates to be used by rlf web client for tls connection to
        // dced-sc database
        this.dcedClientCertPath = EnvVars.get("DCEDSC_CLIENT_CERT_PATH", "/run/secrets/dcedsc/certificates");

        // dced-sc tls enable indicator
        // disabled if global tls is disabled
        // optionally may be disabled if global tls is enabled
        this.dcedTlsEnabled = Boolean.parseBoolean(EnvVars.get("DCED_TLS_ENABLED"));

    }

    @Override
    public String toString()
    {
        var parameters = new JsonObject();
        parameters.put("hostname", hostname);
        parameters.put("serviceHostname", serviceHostname);
        parameters.put("oamServerPort", oamServerPort);
        parameters.put("oamServerCertPath", oamServerCertPath);
        parameters.put("mediatorServerCaPath", mediatorServerCaPath);
        parameters.put("pmServerCaPath", pmServerCaPath);
        parameters.put("slfMediatorClientCertPath", slfMediatorClientCertPath);
        parameters.put("sipTlsRootCaPath", sipTlsRootCaPath);
        parameters.put("mediatorHostname", mediatorHostname);
        parameters.put("mediatorPort", mediatorPort);
        parameters.put("globalTlsEnabled", globalTlsEnabled);
        parameters.put("schemaName", schemaName);
        parameters.put("k8sProbeIfPort", k8sProbeIfPort);
        parameters.put("mediatorUri", mediatorUri);
        parameters.put("subscribeValidity", subscribeValidity);
        parameters.put("subscribeRenewal", subscribeRenewal);
        parameters.put("subscribeHeartbeat", subscribeHeartbeat);
        parameters.put("alarmHandlerHostName", alarmHandlerHostName);
        parameters.put("alarmHandlerPort", alarmHandlerPort);
        parameters.put("alarmHandlerClientCertPath", alarmHandlerClientCertPath);
        parameters.put("dcedClientCertPath", dcedClientCertPath);
        parameters.put("dcedTlsEnabled", dcedTlsEnabled);
        return parameters.encode();
    }
}
