
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "destination-realm", "destination-host" })
public class StaticDestinationProfile
{

    /**
     * Name of the static-destination-profile. (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the static-destination-profile.")
    private String name;
    /**
     * The default destination-realm in case of static destination profile
     * (Required)
     * 
     */
    @JsonProperty("destination-realm")
    @JsonPropertyDescription("The default destination-realm in case of static destination profile")
    private String destinationRealm;
    /**
     * The default destination-host in case of static destination profile
     * 
     */
    @JsonProperty("destination-host")
    @JsonPropertyDescription("The default destination-host in case of static destination profile")
    private String destinationHost;

    /**
     * Name of the static-destination-profile. (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name of the static-destination-profile. (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public StaticDestinationProfile withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The default destination-realm in case of static destination profile
     * (Required)
     * 
     */
    @JsonProperty("destination-realm")
    public String getDestinationRealm()
    {
        return destinationRealm;
    }

    /**
     * The default destination-realm in case of static destination profile
     * (Required)
     * 
     */
    @JsonProperty("destination-realm")
    public void setDestinationRealm(String destinationRealm)
    {
        this.destinationRealm = destinationRealm;
    }

    public StaticDestinationProfile withDestinationRealm(String destinationRealm)
    {
        this.destinationRealm = destinationRealm;
        return this;
    }

    /**
     * The default destination-host in case of static destination profile
     * 
     */
    @JsonProperty("destination-host")
    public String getDestinationHost()
    {
        return destinationHost;
    }

    /**
     * The default destination-host in case of static destination profile
     * 
     */
    @JsonProperty("destination-host")
    public void setDestinationHost(String destinationHost)
    {
        this.destinationHost = destinationHost;
    }

    public StaticDestinationProfile withDestinationHost(String destinationHost)
    {
        this.destinationHost = destinationHost;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(StaticDestinationProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("destinationRealm");
        sb.append('=');
        sb.append(((this.destinationRealm == null) ? "<null>" : this.destinationRealm));
        sb.append(',');
        sb.append("destinationHost");
        sb.append('=');
        sb.append(((this.destinationHost == null) ? "<null>" : this.destinationHost));
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
        result = ((result * 31) + ((this.destinationRealm == null) ? 0 : this.destinationRealm.hashCode()));
        result = ((result * 31) + ((this.destinationHost == null) ? 0 : this.destinationHost.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof StaticDestinationProfile) == false)
        {
            return false;
        }
        StaticDestinationProfile rhs = ((StaticDestinationProfile) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.destinationRealm == rhs.destinationRealm)
                     || ((this.destinationRealm != null) && this.destinationRealm.equals(rhs.destinationRealm))))
                && ((this.destinationHost == rhs.destinationHost) || ((this.destinationHost != null) && this.destinationHost.equals(rhs.destinationHost))));
    }

}
