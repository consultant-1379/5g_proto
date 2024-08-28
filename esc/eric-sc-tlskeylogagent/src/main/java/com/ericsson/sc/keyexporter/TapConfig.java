package com.ericsson.sc.keyexporter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Tap agent configuration
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TapConfig
{
    private final String serviceName;
    private final boolean tapEnabled;

    @JsonCreator
    public TapConfig(@JsonProperty("ServiceName") String serviceName,
                     @JsonProperty("TAP_ENABLED") boolean tapEnabled)
    {
        Objects.requireNonNull(serviceName);

        this.serviceName = serviceName;
        this.tapEnabled = tapEnabled;
    }

    @JsonGetter("ServiceName")
    public String getServiceName()
    {
        return serviceName;
    }

    @JsonGetter("TAP_ENABLED")
    public boolean isTapEnabled()
    {
        return tapEnabled;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(serviceName, tapEnabled);
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
        TapConfig other = (TapConfig) obj;
        return Objects.equals(serviceName, other.serviceName) && tapEnabled == other.tapEnabled;
    }

    public static Optional<TapConfig> fromString(String jsonString,
                                                 String fetchedServiceName)
    {
        try
        {
            return Jackson.om().readValue(jsonString, new TypeReference<List<TapConfig>>()
            {
            }).stream().filter(tapConfig -> tapConfig.serviceName.equals(fetchedServiceName)).findFirst();
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to parse tapconfig", e);
        }
    }
}
