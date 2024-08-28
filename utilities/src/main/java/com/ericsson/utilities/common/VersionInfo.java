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
 * Created on: Jul 20, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for generic versionInfo-retrieval.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "commit", "build" })
public class VersionInfo
{
    private static final Logger log = LoggerFactory.getLogger(VersionInfo.class);
    private static final ObjectMapper mapper = Jackson.om();

    private static final VersionInfo versionInfo = readVersionInfo();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "id" })
    public static class BuildInfo
    {
        private final String time;

        private BuildInfo()
        {
            this.time = "undefined";
        }

        private BuildInfo(final String time)
        {
            this.time = time;
        }

        /**
         * Returns the time of the build.
         * 
         * @return The time of the build.
         */
        @JsonProperty("time")
        public String getTime()
        {
            return this.time;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "id" })
    public static class CommitInfo
    {
        private final String id;

        private CommitInfo()
        {
            this.id = "undefined";
        }

        private CommitInfo(final String id)
        {
            this.id = id;
        }

        /**
         * Returns the commit ID.
         * 
         * @return The commit ID.
         */
        @JsonProperty("id")
        public String getId()
        {
            return this.id;
        }
    }

    public static VersionInfo get()
    {
        return versionInfo;
    }

    private static VersionInfo readVersionInfo()
    {

        try (final var stream = ClassLoader.getSystemClassLoader().getResourceAsStream("git.properties"))
        {
            if (stream != null)
            {
                final var props = new Properties();
                props.load(stream);
                final var commitId = props.getProperty("git.commit.id.full");
                final var buildTime = props.getProperty("git.build.time");

                return new VersionInfo(commitId != null ? new CommitInfo(commitId) : new CommitInfo(), //
                                       buildTime != null ? new BuildInfo(buildTime) : new BuildInfo());
            }
            else
            {
                log.warn("No version information found");
            }
        }
        catch (Exception e)
        {
            log.error("Failed to retrieve version information", e);
        }
        return new VersionInfo();
    }

    @JsonProperty("commit")
    private final CommitInfo commit;

    @JsonProperty("build")
    private final BuildInfo build;

    private VersionInfo()
    {
        this.commit = new CommitInfo();
        this.build = new BuildInfo();
    }

    private VersionInfo(final CommitInfo commit,
                        final BuildInfo build)
    {
        this.commit = commit;
        this.build = build;
    }

    /**
     * Returns the commit information.
     * 
     * @return The commit information.
     */
    @JsonProperty("commit")
    public CommitInfo getCommit()
    {
        return this.commit;
    }

    /**
     * Returns the build information.
     * 
     * @return The build information.
     */
    @JsonProperty("build")
    public BuildInfo getBuild()
    {
        return this.build;
    }

    /**
     * Returns a JSON representation of this object.
     * 
     * @return A JSON representation of this object.
     */
    @Override
    public String toString()
    {
        try
        {
            return mapper.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            return e.toString();
        }
    }
}
