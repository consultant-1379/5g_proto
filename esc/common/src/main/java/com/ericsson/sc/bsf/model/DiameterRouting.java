
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Configuration settings for routing of diameter messages.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "no-binding-case", "pcf-reselection-case", "static-destination-profile", "lookup-profile", "route" })
public class DiameterRouting
{

    /**
     * The routing configuration in case binding is not resolved in the DB.
     * 
     */
    @JsonProperty("no-binding-case")
    @JsonPropertyDescription("The routing configuration in case binding is not resolved in the DB.")
    private List<NoBindingCase> noBindingCase = new ArrayList<NoBindingCase>();
    /**
     * The routing configuration in case of pcf reselection.
     * 
     */
    @JsonProperty("pcf-reselection-case")
    @JsonPropertyDescription("The routing configuration in case of pcf reselection.")
    private List<PcfReselectionCase> pcfReselectionCase = new ArrayList<PcfReselectionCase>();
    /**
     * The routing configuration for the static destination profile.
     * 
     */
    @JsonProperty("static-destination-profile")
    @JsonPropertyDescription("The routing configuration for the static destination profile.")
    private List<StaticDestinationProfile> staticDestinationProfile = new ArrayList<StaticDestinationProfile>();
    /**
     * The profile used by BSF for routing diameter messages that invoke database
     * lookups.
     * 
     */
    @JsonProperty("lookup-profile")
    @JsonPropertyDescription("The profile used by BSF for routing diameter messages that invoke database lookups.")
    private List<LookupProfile> lookupProfile = new ArrayList<LookupProfile>();
    /**
     * Route that will be used in forwarded diameter messages.
     * 
     */
    @JsonProperty("route")
    @JsonPropertyDescription("Route that will be used in forwarded diameter messages.")
    private List<Route> route = new ArrayList<Route>();

    /**
     * The routing configuration in case binding is not resolved in the DB.
     * 
     */
    @JsonProperty("no-binding-case")
    public List<NoBindingCase> getNoBindingCase()
    {
        return noBindingCase;
    }

    /**
     * The routing configuration in case binding is not resolved in the DB.
     * 
     */
    @JsonProperty("no-binding-case")
    public void setNoBindingCase(List<NoBindingCase> noBindingCase)
    {
        this.noBindingCase = noBindingCase;
    }

    public DiameterRouting withNoBindingCase(List<NoBindingCase> noBindingCase)
    {
        this.noBindingCase = noBindingCase;
        return this;
    }

    /**
     * The routing configuration in case of pcf reselection.
     * 
     */
    @JsonProperty("pcf-reselection-case")
    public List<PcfReselectionCase> getPcfReselectionCase()
    {
        return pcfReselectionCase;
    }

    /**
     * The routing configuration in case of pcf reselection.
     * 
     */
    @JsonProperty("pcf-reselection-case")
    public void setPcfReselectionCase(List<PcfReselectionCase> pcfReselectionCase)
    {
        this.pcfReselectionCase = pcfReselectionCase;
    }

    public DiameterRouting withPcfReselectionCase(List<PcfReselectionCase> pcfReselectionCase)
    {
        this.pcfReselectionCase = pcfReselectionCase;
        return this;
    }

    /**
     * The routing configuration for the static destination profile.
     * 
     */
    @JsonProperty("static-destination-profile")
    public List<StaticDestinationProfile> getStaticDestinationProfile()
    {
        return staticDestinationProfile;
    }

    /**
     * The routing configuration for the static destination profile.
     * 
     */
    @JsonProperty("static-destination-profile")
    public void setStaticDestinationProfile(List<StaticDestinationProfile> staticDestinationProfile)
    {
        this.staticDestinationProfile = staticDestinationProfile;
    }

    public DiameterRouting withStaticDestinationProfile(List<StaticDestinationProfile> staticDestinationProfile)
    {
        this.staticDestinationProfile = staticDestinationProfile;
        return this;
    }

    /**
     * The profile used by BSF for routing diameter messages that invoke database
     * lookups.
     * 
     */
    @JsonProperty("lookup-profile")
    public List<LookupProfile> getLookupProfile()
    {
        return lookupProfile;
    }

    /**
     * The profile used by BSF for routing diameter messages that invoke database
     * lookups.
     * 
     */
    @JsonProperty("lookup-profile")
    public void setLookupProfile(List<LookupProfile> lookupProfile)
    {
        this.lookupProfile = lookupProfile;
    }

    public DiameterRouting withLookupProfile(List<LookupProfile> lookupProfile)
    {
        this.lookupProfile = lookupProfile;
        return this;
    }

    /**
     * Route that will be used in forwarded diameter messages.
     * 
     */
    @JsonProperty("route")
    public List<Route> getRoute()
    {
        return route;
    }

    /**
     * Route that will be used in forwarded diameter messages.
     * 
     */
    @JsonProperty("route")
    public void setRoute(List<Route> route)
    {
        this.route = route;
    }

    public DiameterRouting withRoute(List<Route> route)
    {
        this.route = route;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(DiameterRouting.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("noBindingCase");
        sb.append('=');
        sb.append(((this.noBindingCase == null) ? "<null>" : this.noBindingCase));
        sb.append(',');
        sb.append("pcfReselectionCase");
        sb.append('=');
        sb.append(((this.pcfReselectionCase == null) ? "<null>" : this.pcfReselectionCase));
        sb.append(',');
        sb.append("staticDestinationProfile");
        sb.append('=');
        sb.append(((this.staticDestinationProfile == null) ? "<null>" : this.staticDestinationProfile));
        sb.append(',');
        sb.append("lookupProfile");
        sb.append('=');
        sb.append(((this.lookupProfile == null) ? "<null>" : this.lookupProfile));
        sb.append(',');
        sb.append("route");
        sb.append('=');
        sb.append(((this.route == null) ? "<null>" : this.route));
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
        result = ((result * 31) + ((this.noBindingCase == null) ? 0 : this.noBindingCase.hashCode()));
        result = ((result * 31) + ((this.pcfReselectionCase == null) ? 0 : this.pcfReselectionCase.hashCode()));
        result = ((result * 31) + ((this.route == null) ? 0 : this.route.hashCode()));
        result = ((result * 31) + ((this.staticDestinationProfile == null) ? 0 : this.staticDestinationProfile.hashCode()));
        result = ((result * 31) + ((this.lookupProfile == null) ? 0 : this.lookupProfile.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof DiameterRouting) == false)
        {
            return false;
        }
        DiameterRouting rhs = ((DiameterRouting) other);
        return ((((((this.noBindingCase == rhs.noBindingCase) || ((this.noBindingCase != null) && this.noBindingCase.equals(rhs.noBindingCase)))
                   && ((this.pcfReselectionCase == rhs.pcfReselectionCase)
                       || ((this.pcfReselectionCase != null) && this.pcfReselectionCase.equals(rhs.pcfReselectionCase))))
                  && ((this.route == rhs.route) || ((this.route != null) && this.route.equals(rhs.route))))
                 && ((this.staticDestinationProfile == rhs.staticDestinationProfile)
                     || ((this.staticDestinationProfile != null) && this.staticDestinationProfile.equals(rhs.staticDestinationProfile))))
                && ((this.lookupProfile == rhs.lookupProfile) || ((this.lookupProfile != null) && this.lookupProfile.equals(rhs.lookupProfile))));
    }

}
