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
 * Created on: March 11, 2021
 *     Author: eevagal
 */

package com.ericsson.esc.bsf.manager;

import com.ericsson.utilities.common.EnvVars;

public class PmServerParameters
{

    private final String namespace;
    private final String pmServerSvcHost;
    private final int pmServerSvcPort;

    public PmServerParameters(String namespace,
                              String pmServerSvcHost,
                              int pmServerSvcPort)
    {
        this.namespace = namespace;
        this.pmServerSvcHost = pmServerSvcHost;
        this.pmServerSvcPort = pmServerSvcPort;
    }

    public String getNamespace()
    {
        return this.namespace;
    }

    public String getPmServerSvcHost()
    {
        return this.pmServerSvcHost;
    }

    public int getPmServerSvcPort()
    {
        return this.pmServerSvcPort;
    }

    public static PmServerParameters fromEnvironment()
    {
        return new PmServerParameters(EnvVars.get("NAMESPACE"),
                                      EnvVars.get("ERIC_PM_SERVER_SERVICE_HOST"),
                                      Integer.parseInt(EnvVars.get("ERIC_PM_SERVER_SERVICE_PORT")));
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PmServerParameters [namespace=");
        builder.append(namespace);
        builder.append(", pmServerSvcHost=");
        builder.append(pmServerSvcHost);
        builder.append(", pmServerSvcPort=");
        builder.append(pmServerSvcPort);
        builder.append("]");
        return builder.toString();
    }

}
