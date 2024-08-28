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
 * Created on: May 1, 2020
 *     Author: echfari
 */
package com.ericsson.sc.diameter;

/**
 * A Diameter GRPC AAA Service
 */
public final class AaaService
{
    private final String serviceName;
    private final String clientHostName;
    private final int clientPort;

    public AaaService(String serviceName,
                      String clientHostName,
                      int clientPort)
    {
        this.serviceName = serviceName;
        this.clientHostName = clientHostName;
        this.clientPort = clientPort;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public String getClientHostName()
    {
        return clientHostName;
    }

    public int getClientPort()
    {
        return clientPort;
    }

}
