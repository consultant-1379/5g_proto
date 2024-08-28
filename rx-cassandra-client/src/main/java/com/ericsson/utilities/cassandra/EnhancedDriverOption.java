/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 29, 2021
 *     Author: echfari
 */
package com.ericsson.utilities.cassandra;

import com.datastax.oss.driver.api.core.config.DriverOption;

import edu.umd.cs.findbugs.annotations.NonNull;

public enum EnhancedDriverOption implements DriverOption
{
    /**
     * Enable native TLS
     */
    TLS_ENABLED("netty.tls.enabled"),
    /**
     * Trusted Certificates in PEM format
     */
    TRUSTED_CERTS("netty.tls.trusted-certs"),
    /**
     * Client Certificate in PEM format
     */
    CLIENT_CERT("netty.tls.client-cert"),
    /**
     * Client key in PEM format
     */
    CLIENT_KEY("netty.tls.client-key"),
    /**
     * Enable server endpoint identitfication
     */
    VERIFY_HOST("netty.tls.verify-host");

    private final String path;

    EnhancedDriverOption(String path)
    {
        this.path = path;
    }

    @NonNull
    @Override
    public String getPath()
    {
        return path;
    }

}
