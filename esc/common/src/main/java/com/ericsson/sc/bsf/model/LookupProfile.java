
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-label", "fallback-destination-realm", "fallback-destination-host" })
public class LookupProfile
{

    /**
     * Name of a lookup-profile instance. (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of a lookup-profile instance.")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * Destination realm where a diameter message will be routed if the binding
     * database lookup yields no result. (Required)
     * 
     */
    @JsonProperty("fallback-destination-realm")
    @JsonPropertyDescription("Destination realm where a diameter message will be routed if the binding database lookup yields no result.")
    private String fallbackDestinationRealm;
    /**
     * Destination host where a diameter message will be routed if the binding
     * database lookup yields no result.
     * 
     */
    @JsonProperty("fallback-destination-host")
    @JsonPropertyDescription("Destination host where a diameter message will be routed if the binding database lookup yields no result.")
    private String fallbackDestinationHost;

    /**
     * Name of a lookup-profile instance. (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name of a lookup-profile instance. (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public LookupProfile withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public LookupProfile withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Destination realm where a diameter message will be routed if the binding
     * database lookup yields no result. (Required)
     * 
     */
    @JsonProperty("fallback-destination-realm")
    public String getFallbackDestinationRealm()
    {
        return fallbackDestinationRealm;
    }

    /**
     * Destination realm where a diameter message will be routed if the binding
     * database lookup yields no result. (Required)
     * 
     */
    @JsonProperty("fallback-destination-realm")
    public void setFallbackDestinationRealm(String fallbackDestinationRealm)
    {
        this.fallbackDestinationRealm = fallbackDestinationRealm;
    }

    public LookupProfile withFallbackDestinationRealm(String fallbackDestinationRealm)
    {
        this.fallbackDestinationRealm = fallbackDestinationRealm;
        return this;
    }

    /**
     * Destination host where a diameter message will be routed if the binding
     * database lookup yields no result.
     * 
     */
    @JsonProperty("fallback-destination-host")
    public String getFallbackDestinationHost()
    {
        return fallbackDestinationHost;
    }

    /**
     * Destination host where a diameter message will be routed if the binding
     * database lookup yields no result.
     * 
     */
    @JsonProperty("fallback-destination-host")
    public void setFallbackDestinationHost(String fallbackDestinationHost)
    {
        this.fallbackDestinationHost = fallbackDestinationHost;
    }

    public LookupProfile withFallbackDestinationHost(String fallbackDestinationHost)
    {
        this.fallbackDestinationHost = fallbackDestinationHost;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(LookupProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("fallbackDestinationRealm");
        sb.append('=');
        sb.append(((this.fallbackDestinationRealm == null) ? "<null>" : this.fallbackDestinationRealm));
        sb.append(',');
        sb.append("fallbackDestinationHost");
        sb.append('=');
        sb.append(((this.fallbackDestinationHost == null) ? "<null>" : this.fallbackDestinationHost));
        sb.append(',');
        if (sb.charAt((sb.length() - 1)) == ',')
        {
            sb.setCharAt((sb.length() - 1), ']');
        }
        else
        {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.fallbackDestinationHost == null) ? 0 : this.fallbackDestinationHost.hashCode()));
        result = ((result * 31) + ((this.fallbackDestinationRealm == null) ? 0 : this.fallbackDestinationRealm.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof LookupProfile) == false)
        {
            return false;
        }
        LookupProfile rhs = ((LookupProfile) other);
        return (((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                  && ((this.fallbackDestinationHost == rhs.fallbackDestinationHost)
                      || ((this.fallbackDestinationHost != null) && this.fallbackDestinationHost.equals(rhs.fallbackDestinationHost))))
                 && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                && ((this.fallbackDestinationRealm == rhs.fallbackDestinationRealm)
                    || ((this.fallbackDestinationRealm != null) && this.fallbackDestinationRealm.equals(rhs.fallbackDestinationRealm))));
    }

}
