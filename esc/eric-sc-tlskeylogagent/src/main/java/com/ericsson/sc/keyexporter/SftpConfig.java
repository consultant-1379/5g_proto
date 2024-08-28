package com.ericsson.sc.keyexporter;

import java.util.List;
import java.util.Objects;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * TAP agent sftp configuration
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SftpConfig
{
    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String remotePath;

    @JsonCreator()
    public SftpConfig(@JsonProperty("host") String host,
                      @JsonProperty("sftpPort") int port,
                      @JsonProperty("username") String user,
                      @JsonProperty("password") String password,
                      @JsonProperty("uploadDir") String remotePath)
    {

        if (host == null)
            throw new IllegalArgumentException("Host cannot be null");
        if (user == null)
            throw new IllegalArgumentException("User cannot be null");
        if (password == null)
            throw new IllegalArgumentException("Password cannot be null");
        if (port <= 0)
            throw new IllegalArgumentException("Invalid sftp port: " + port);
        if (remotePath == null)
            throw new IllegalArgumentException("Sftp remote path cannot be null");

        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.remotePath = remotePath;
    }

    /**
     * @return current Sftp Host
     */
    @JsonGetter("host")
    public String getHost()
    {
        return this.host;
    }

    /**
     * @return current Sftp port
     */
    @JsonGetter("sftpPort")
    public int getPort()
    {
        return this.port;
    }

    /**
     * @return current Sftp User
     */
    @JsonGetter("userName")
    public String getUser()
    {
        return this.user;
    }

    /**
     * @return current Sftp password
     */
    @JsonGetter("password")
    public String getPassword()
    {
        return this.password;
    }

    /**
     * @return current Sftp remote path
     */
    @JsonGetter("uploadDir")
    public String getRemotePath()
    {
        return this.remotePath;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(host, password, port, remotePath, user);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SftpConfig other = (SftpConfig) obj;
        return Objects.equals(host, other.host) && Objects.equals(password, other.password) && port == other.port
               && Objects.equals(remotePath, other.remotePath) && Objects.equals(user, other.user);
    }

    public static SftpConfig fromString(String jsonString)
    {
        try
        {
            return Jackson.om().readValue(jsonString, new TypeReference<List<SftpConfig>>()
            {
            }).get(0);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to parse sftp config", e);
        }
    }
}
