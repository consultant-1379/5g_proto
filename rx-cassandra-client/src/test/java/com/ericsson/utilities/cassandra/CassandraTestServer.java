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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Manages Cassandra servers do be used for tests. This server supports both TLS
 * and non TLS connections,with predefined CA and private key. TLS endpoint
 * identification is disabled.
 */
public class CassandraTestServer
{
    private static final Logger log = LoggerFactory.getLogger(CassandraTestServer.class);

    private final CassandraContainer<?> cassandraContainer;
    private final String username;
    private final String password;

    public CassandraTestServer()
    {
        final var cassandraDockerImage = DockerImageName.parse("armdockerhub.rnd.ericsson.se/cassandra:3.11.2").asCompatibleSubstituteFor("cassandra");
        this.cassandraContainer = new CassandraContainer<>(cassandraDockerImage);

        cassandraContainer.addEnv("JVM_OPTS", "-Xms1024M -Xmx2048M");
        cassandraContainer.withConfigurationOverride("cassandra_testbed_config");

        cassandraContainer.withCopyFileToContainer(MountableFile.forClasspathResource("certificates/set1/server/container.p12"), "certs/container.p12");
        cassandraContainer.withCopyFileToContainer(MountableFile.forClasspathResource("certificates/set1/truststore.pfx"), "certs/truststore.pfx");

        this.username = cassandraContainer.getUsername();
        this.password = cassandraContainer.getPassword();
    }

    public void startCassandra()
    {
        log.info("Starting Cassandra test container.");
        this.cassandraContainer.start();
    }

    public void stopCassandra()
    {
        log.info("Stopping Cassandra test container.");
        this.cassandraContainer.stop();
    }

    public String getCassandraHost()
    {
        return this.cassandraContainer.getHost();
    }

    public Integer getPort()
    {
        return this.cassandraContainer.getFirstMappedPort();
    }

    public String getContactPoint()
    {
        return getCassandraHost() + ":" + getPort();
    }

    /**
     * @return the username
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }
}
