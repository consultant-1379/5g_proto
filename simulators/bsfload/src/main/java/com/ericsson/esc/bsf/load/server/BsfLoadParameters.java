/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 28, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.server;

import com.ericsson.utilities.common.EnvVars;

public class BsfLoadParameters
{
    private final int bsfLoadPort;
    private final Boolean metricsEnabled;
    private final Boolean metricsJvmEnabled;
    private final String metricsPath;
    private final int metricsPort;

    public BsfLoadParameters(int bsfLoadPort,
                             Boolean metricsEnabled,
                             Boolean metricsJvmEnabled,
                             String metricsPath,
                             int metricsPort)
    {
        this.bsfLoadPort = bsfLoadPort;
        this.metricsEnabled = metricsEnabled;
        this.metricsJvmEnabled = metricsJvmEnabled;
        this.metricsPath = metricsPath;
        this.metricsPort = metricsPort;
    }

    public static BsfLoadParameters fromEnvironment()
    {
        final var bsfLoadPortEnv = Integer.parseInt(EnvVars.get("BSF_LOAD_PORT", 80));
        final var metricsEnabledEnv = Boolean.parseBoolean(EnvVars.get("METRICS_ENABLED"));
        final var metricsJvmEnabledEnv = Boolean.parseBoolean(EnvVars.get("METRICS_JVM_ENABLED"));
        final var metricsPathEnv = EnvVars.get("METRICS_PATH", "/metrics");
        final var metricsPortEnv = Integer.parseInt(EnvVars.get("METRICS_PORT", 8081));

        return new BsfLoadParameters(bsfLoadPortEnv, //
                                     metricsEnabledEnv,
                                     metricsJvmEnabledEnv,
                                     metricsPathEnv,
                                     metricsPortEnv);
    }

    /**
     * @return the bsfLoadPort
     */
    public int getBsfLoadPort()
    {
        return bsfLoadPort;
    }

    /**
     * @return the metricsEnabled
     */
    public Boolean getMetricsEnabled()
    {
        return metricsEnabled;
    }

    /**
     * @return the metricsJvmEnabled
     */
    public Boolean getMetricsJvmEnabled()
    {
        return metricsJvmEnabled;
    }

    /**
     * @return the metricsPath
     */
    public String getMetricsPath()
    {
        return metricsPath;
    }

    /**
     * @return the metricsPort
     */
    public int getMetricsPort()
    {
        return metricsPort;
    }

    @Override
    public String toString()
    {
        return "BsfLoadParameters [bsfLoadPort=" + bsfLoadPort + ", metricsEnabled=" + metricsEnabled + ", metricsJvmEnabled=" + metricsJvmEnabled
               + ", metricsPath=" + metricsPath + ", metricsPort=" + metricsPort + "]";
    }
}
