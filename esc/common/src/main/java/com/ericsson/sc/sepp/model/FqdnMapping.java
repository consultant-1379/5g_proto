
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Enable/Disable the FQDN Mapping functionality for NRF FQDNs
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "custom-fqdn-locator", "on-fqdn-mapping-unsuccessful" })
public class FqdnMapping
{

    /**
     * The parameters to define the location of the FQDNs that need to be
     * mapped/scrambled according to the specified service
     * 
     */
    @JsonProperty("custom-fqdn-locator")
    @JsonPropertyDescription("The parameters to define the location of the FQDNs that need to be mapped/scrambled according to the specified service")
    private List<CustomFqdnLocator> customFqdnLocator = new ArrayList<CustomFqdnLocator>();
    /**
     * Action to perform in case FQDN mapping cannot be applied
     * 
     */
    @JsonProperty("on-fqdn-mapping-unsuccessful")
    @JsonPropertyDescription("Action to perform in case FQDN mapping cannot be applied")
    private OnFqdnMappingUnsuccessful onFqdnMappingUnsuccessful;

    /**
     * The parameters to define the location of the FQDNs that need to be
     * mapped/scrambled according to the specified service
     * 
     */
    @JsonProperty("custom-fqdn-locator")
    public List<CustomFqdnLocator> getCustomFqdnLocator()
    {
        return customFqdnLocator;
    }

    /**
     * The parameters to define the location of the FQDNs that need to be
     * mapped/scrambled according to the specified service
     * 
     */
    @JsonProperty("custom-fqdn-locator")
    public void setCustomFqdnLocator(List<CustomFqdnLocator> customFqdnLocator)
    {
        this.customFqdnLocator = customFqdnLocator;
    }

    public FqdnMapping withCustomFqdnLocator(List<CustomFqdnLocator> customFqdnLocator)
    {
        this.customFqdnLocator = customFqdnLocator;
        return this;
    }

    /**
     * Action to perform in case FQDN mapping cannot be applied
     * 
     */
    @JsonProperty("on-fqdn-mapping-unsuccessful")
    public OnFqdnMappingUnsuccessful getOnFqdnMappingUnsuccessful()
    {
        return onFqdnMappingUnsuccessful;
    }

    /**
     * Action to perform in case FQDN mapping cannot be applied
     * 
     */
    @JsonProperty("on-fqdn-mapping-unsuccessful")
    public void setOnFqdnMappingUnsuccessful(OnFqdnMappingUnsuccessful onFqdnMappingUnsuccessful)
    {
        this.onFqdnMappingUnsuccessful = onFqdnMappingUnsuccessful;
    }

    public FqdnMapping withOnFqdnMappingUnsuccessful(OnFqdnMappingUnsuccessful onFqdnMappingUnsuccessful)
    {
        this.onFqdnMappingUnsuccessful = onFqdnMappingUnsuccessful;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(FqdnMapping.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("customFqdnLocator");
        sb.append('=');
        sb.append(((this.customFqdnLocator == null) ? "<null>" : this.customFqdnLocator));
        sb.append(',');
        sb.append("onFqdnMappingUnsuccessful");
        sb.append('=');
        sb.append(((this.onFqdnMappingUnsuccessful == null) ? "<null>" : this.onFqdnMappingUnsuccessful));
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
        result = ((result * 31) + ((this.onFqdnMappingUnsuccessful == null) ? 0 : this.onFqdnMappingUnsuccessful.hashCode()));
        result = ((result * 31) + ((this.customFqdnLocator == null) ? 0 : this.customFqdnLocator.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof FqdnMapping) == false)
        {
            return false;
        }
        FqdnMapping rhs = ((FqdnMapping) other);
        return (((this.onFqdnMappingUnsuccessful == rhs.onFqdnMappingUnsuccessful)
                 || ((this.onFqdnMappingUnsuccessful != null) && this.onFqdnMappingUnsuccessful.equals(rhs.onFqdnMappingUnsuccessful)))
                && ((this.customFqdnLocator == rhs.customFqdnLocator)
                    || ((this.customFqdnLocator != null) && this.customFqdnLocator.equals(rhs.customFqdnLocator))));
    }

}
