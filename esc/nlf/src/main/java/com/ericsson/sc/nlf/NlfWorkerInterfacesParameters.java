/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 12, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.nlf;

import com.ericsson.utilities.common.EnvVars;

import io.vertx.core.json.JsonObject;

public class NlfWorkerInterfacesParameters
{
    public static final NlfWorkerInterfacesParameters instance = new NlfWorkerInterfacesParameters();
    public final String hostname;
    public final int portInternal;
    public final int portRest;
    public final int portRestOam;
    public final int concurrentStreamsMax;
    public final String serviceHostname;
    public final Integer oamServerPort;
    public final String dataMessageBusKf;
    public final int k8sProbeIfPort;
    public final boolean globalTlsEnabled;
    public final String mediatorHostname;
    public final Integer mediatorPort;
    public final String alarmHandlerHostName;
    public final boolean dcedTlsEnabled;
    public final Integer alarmHandlerPort;

    private NlfWorkerInterfacesParameters()
    {
        this.hostname = EnvVars.get("HOSTNAME");
        this.portInternal = Integer.parseInt(EnvVars.get("SERVICE_TARGET_PORT_INTERNAL", 8081));
        this.portRest = Integer.parseInt(EnvVars.get("SERVICE_TARGET_PORT_REST", 8083));
        this.portRestOam = Integer.parseInt(EnvVars.get("SERVICE_TARGET_PORT_REST_OAM", 8080));
        this.serviceHostname = EnvVars.get("SERVICE_HOST", "eric-sc-nlf");

        // nlf worker web server port to be used for incoming requests by mediator
        this.oamServerPort = Integer.parseInt(EnvVars.get("SERVICE_OAM_TARGET_PORT", 8082));
        this.concurrentStreamsMax = (int) Float.parseFloat(EnvVars.get("CONCURRENT_STREAMS_MAX", 20));
        this.dataMessageBusKf = EnvVars.get("DATA_MESSAGE_BUS_KF");
        this.k8sProbeIfPort = Integer.parseInt(EnvVars.get("SERVICE_K8S_PROBE_TARGET_PORT", 8081));
        this.globalTlsEnabled = Boolean.parseBoolean(EnvVars.get("GLOBAL_TLS_ENABLED"));
        this.mediatorHostname = EnvVars.get("CM_MEDIATOR", "eric-cm-mediator");
        this.mediatorPort = Integer.parseInt(EnvVars.get("CM_MEDIATOR_PORT", this.globalTlsEnabled ? 5004 : 5003));

        // Alarm Handler service hostname and port
        this.alarmHandlerHostName = EnvVars.get("ALARM_HANDLER_HOSTNAME", "eric-fh-alarm-handler");
        this.alarmHandlerPort = Integer.parseInt(EnvVars.get("ALARM_HANDLER_PORT", this.globalTlsEnabled ? 6006 : 6005));

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
        parameters.put("portInternal", portInternal);
        parameters.put("portRest", portRest);
        parameters.put("portRestOam", portRestOam);
        parameters.put("serviceHostname", serviceHostname);
        parameters.put("concurrentStreamsMax", concurrentStreamsMax);
        parameters.put("dataMessageBusKf", dataMessageBusKf);
        parameters.put("k8sProbeIfPort", k8sProbeIfPort);
        parameters.put("globalTlsEnabled", globalTlsEnabled);
        parameters.put("mediatorHostname", mediatorHostname);
        parameters.put("mediatorPort", mediatorPort);
        parameters.put("oamServerPort", oamServerPort);
        parameters.put("alarmHandlerHostName", alarmHandlerHostName);
        parameters.put("alarmHandlerPort", alarmHandlerPort);
        return parameters.encode();
    }
}
