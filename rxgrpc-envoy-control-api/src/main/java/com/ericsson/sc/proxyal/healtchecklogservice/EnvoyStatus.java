/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 9, 2024
 *     Author: ztsakon
 */

package com.ericsson.sc.proxyal.healtchecklogservice;

/**
 * Helper class to transport Envoy's status from Aggregated Discovery Service to
 * the HealthCheck Log Service
 */
public class EnvoyStatus
{
    final private String envoyId;
    final private String url;
    final private String cluster;
    final private OperationalState operationalState;

    public EnvoyStatus(String envoyId,
                       String url,
                       String cluster,
                       OperationalState status)
    {
        this.envoyId = envoyId;
        this.url = url;
        this.operationalState = status;
        this.cluster = cluster;
    }

    /**
     * @return the envoyId
     */
    public String getEnvoyId()
    {
        return this.envoyId;
    }

    /**
     * @return the URL of the producer
     */
    public String getUrl()
    {
        return this.url;
    }

    /**
     * @return the affected cluster
     */
    public String getCluster()
    {
        return this.cluster;
    }

    /**
     * @return the operationalState
     */
    public OperationalState getOperationalState()
    {
        return this.operationalState;
    }

    public String toString()
    {
        return "Reporting Envoy: " + this.envoyId + ", affected producer: " + url + ", state: " + opState(operationalState);
    }

    public String opState(OperationalState opstate)
    {
        if (opstate == OperationalState.HEALTHY_HOST_ADDITION)
        {
            return "Healthy host addition";
        }
        else if (opstate.equals(OperationalState.UNHEALTHY_HOST_EJECTION))
        {
            return "Unhealthy host ejection";
        }
        else if (opstate.equals(OperationalState.DEGRADED_HEALTHY_HOST))
        {
            return "Healthy host degraded";
        }
        else if (opstate.equals(OperationalState.NO_LONGER_DEGRADED_HOST))
        {
            return "Host no longer degraded";
        }
        else
            return ("Unknown");

    }
}
