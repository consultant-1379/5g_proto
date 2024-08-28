/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 1, 2021
 *     Author: echfari
 */
package com.ericsson.sc.bsf.diameter;

import java.net.URI;

import com.ericsson.utilities.common.EnvVars;

/**
 * Etcd client configuration parameters
 */
public class EtcdParams
{
    private final URI etcdEndpoint;
    private final String etcdUsername;
    private final String etcdPassword;

    /**
     * Create configuration from enviromental variables
     * 
     * @return
     */
    public static EtcdParams fromEnvironment()
    {
        final var etcdEp = URI.create(EnvVars.get("ETCD_ENDPOINT"));
        return new EtcdParams(etcdEp, EnvVars.get("ETCD_USERNAME"), EnvVars.get("ETCD_PASSWORD"));
    }

    /**
     * @return
     */
    public URI getEtcdEndpoint()
    {
        return etcdEndpoint;
    }

    public String getEtcdUsername()
    {
        return etcdUsername;
    }

    public String getEtcdPassword()
    {
        return etcdPassword;
    }

    EtcdParams(URI etcdEndpoint,
               String etcdUsername,
               String etcdPassword)
    {
        this.etcdEndpoint = etcdEndpoint;
        this.etcdPassword = etcdPassword;
        this.etcdUsername = etcdUsername;

    }
}
