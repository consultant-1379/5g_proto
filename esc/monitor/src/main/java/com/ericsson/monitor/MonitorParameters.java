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
 * Created on: Jul 1, 2021
 *     Author: eedstl
 */

package com.ericsson.monitor;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.EnvVars;

import io.vertx.core.json.JsonObject;

public class MonitorParameters
{
    private static final Logger log = LoggerFactory.getLogger(MonitorParameters.class);

    public static MonitorParameters fromEnvironment()
    {
        return new MonitorParameters();
    }

    private final String hostname;
    private final String serviceHostname;
    private final Integer externalPort;
    private final Integer internalPort;
    private final boolean tlsEnabled;
    private final URI externalServerUri;
    private final URI internalServerUri;
    private final String username;
    private final String password;

    private MonitorParameters()
    {
        this.tlsEnabled = Boolean.parseBoolean(EnvVars.get("TLS_ENABLED"));
        this.hostname = EnvVars.get("HOSTNAME");
        this.serviceHostname = EnvVars.get("SERVICE_HOSTNAME");
        this.externalPort = Integer.parseInt(EnvVars.get("EXTERNAL_PORT", 8081));
        this.internalPort = Integer.parseInt(EnvVars.get("INTERNAL_PORT", 8080));
        this.externalServerUri = URI.create(this.tlsEnabled ? "https" : "http" + "://" + this.serviceHostname + ":" + this.externalPort + "/");
        this.internalServerUri = URI.create(this.tlsEnabled ? "https" : "http" + "://" + this.serviceHostname + ":" + this.internalPort + "/");
        this.username = EnvVars.get("USERNAME");
        this.password = EnvVars.get("PASSWORD");

        final Properties properties = new Properties();

        try
        {
            properties.load(this.getClass().getResourceAsStream("/monitor.properties"));
        }
        catch (IOException e)
        {
            log.error("Error reading the monitor properties file.");
        }
    }

    public Integer getExternalPort()
    {
        return this.externalPort;
    }

    public URI getExternalServerUri()
    {
        return this.externalServerUri;
    }

    public String getHostname()
    {
        return this.hostname;
    }

    public Integer getInternalPort()
    {
        return this.internalPort;
    }

    public URI getInternalServerUri()
    {
        return this.internalServerUri;
    }

    public String getPassword()
    {
        return this.password;
    }

    public String getServerHostName()
    {
        return this.serviceHostname;
    }

    public String getUsername()
    {
        return this.username;
    }

    public boolean isTlsEnabled()
    {
        return this.tlsEnabled;
    }

    @Override
    public String toString()
    {
        final JsonObject parameters = new JsonObject();
        parameters.put("hostname", hostname);
        parameters.put("serviceHostname", serviceHostname);
        parameters.put("internalPort", internalPort);
        parameters.put("externalPort", externalPort);
        parameters.put("tlsEnabled", tlsEnabled);
        parameters.put("externalServerUri", externalServerUri);
        parameters.put("internalServerUri", internalServerUri);
        return parameters.encode();
    }
}
