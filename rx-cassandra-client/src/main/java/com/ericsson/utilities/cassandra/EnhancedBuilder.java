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

import java.util.Objects;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.session.ProgrammaticArguments;
import com.datastax.oss.driver.api.core.session.SessionBuilder;

/**
 * An enhanced cassandra session builder that supports native TLS
 */
public class EnhancedBuilder extends SessionBuilder<EnhancedBuilder, CqlSession>
{
    private final TrustManager tm;
    private final KeyManager km;

    public static SessionBuilder<EnhancedBuilder, CqlSession> create()
    {
        return new EnhancedBuilder();
    }

    public static SessionBuilder<EnhancedBuilder, CqlSession> create(KeyManager km,
                                                                     TrustManager tm)
    {
        return new EnhancedBuilder(km, tm);
    }

    private static final Logger log = LoggerFactory.getLogger(EnhancedBuilder.class);

    private EnhancedBuilder()
    {
        this.km = null;
        this.tm = null;
    }

    public EnhancedBuilder(KeyManager km,
                           TrustManager tm)
    {
        Objects.requireNonNull(km);
        Objects.requireNonNull(tm);
        this.km = km;
        this.tm = tm;
    }

    @Override
    protected DriverContext buildContext(DriverConfigLoader configLoader,
                                         ProgrammaticArguments programmaticArguments)
    {
        log.debug("Cassandra driver configuration, default profile: {}", configLoader.getInitialConfig().getDefaultProfile().entrySet());
        return new EnhancedDriverContext(configLoader, programmaticArguments, km, tm);
    }

    @Override
    protected CqlSession wrap(CqlSession defaultSession)
    {
        // Nothing to do here, nothing changes on the session type
        return defaultSession;
    }
}
