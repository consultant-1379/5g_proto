
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Enable/Disable the FQDN Scrambling functionality
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "custom-fqdn-locator", "on-fqdn-scrambling-unsuccessful" })
public class FqdnScrambling
{

    /**
     * The parameters to define the location of the FQDNs that need to be scrambled
     * according to the specified service
     * 
     */
    @JsonProperty("custom-fqdn-locator")
    @JsonPropertyDescription("The parameters to define the location of the FQDNs that need to be scrambled according to the specified service")
    private List<CustomFqdnLocator> customFqdnLocator = new ArrayList<CustomFqdnLocator>();
    /**
     * Action to perform in case FQDN scrambling cannot be applied. Several reasons
     * can invoke this action. For example, if the key for scrambling/de-scrambling
     * is missing, or if the received FQDN is invalid.
     * 
     */
    @JsonProperty("on-fqdn-scrambling-unsuccessful")
    @JsonPropertyDescription("Action to perform in case FQDN scrambling cannot be applied. Several reasons can invoke this action. For example, if the key for scrambling/de-scrambling is missing, or if the received FQDN is invalid.")
    private OnFqdnScramblingUnsuccessful onFqdnScramblingUnsuccessful;

    /**
     * The parameters to define the location of the FQDNs that need to be scrambled
     * according to the specified service
     * 
     */
    @JsonProperty("custom-fqdn-locator")
    public List<CustomFqdnLocator> getCustomFqdnLocator()
    {
        return customFqdnLocator;
    }

    /**
     * The parameters to define the location of the FQDNs that need to be scrambled
     * according to the specified service
     * 
     */
    @JsonProperty("custom-fqdn-locator")
    public void setCustomFqdnLocator(List<CustomFqdnLocator> customFqdnLocator)
    {
        this.customFqdnLocator = customFqdnLocator;
    }

    public FqdnScrambling withCustomFqdnLocator(List<CustomFqdnLocator> customFqdnLocator)
    {
        this.customFqdnLocator = customFqdnLocator;
        return this;
    }

    /**
     * Action to perform in case FQDN scrambling cannot be applied. Several reasons
     * can invoke this action. For example, if the key for scrambling/de-scrambling
     * is missing, or if the received FQDN is invalid.
     * 
     */
    @JsonProperty("on-fqdn-scrambling-unsuccessful")
    public OnFqdnScramblingUnsuccessful getOnFqdnScramblingUnsuccessful()
    {
        return onFqdnScramblingUnsuccessful;
    }

    /**
     * Action to perform in case FQDN scrambling cannot be applied. Several reasons
     * can invoke this action. For example, if the key for scrambling/de-scrambling
     * is missing, or if the received FQDN is invalid.
     * 
     */
    @JsonProperty("on-fqdn-scrambling-unsuccessful")
    public void setOnFqdnScramblingUnsuccessful(OnFqdnScramblingUnsuccessful onFqdnScramblingUnsuccessful)
    {
        this.onFqdnScramblingUnsuccessful = onFqdnScramblingUnsuccessful;
    }

    public FqdnScrambling withOnFqdnScramblingUnsuccessful(OnFqdnScramblingUnsuccessful onFqdnScramblingUnsuccessful)
    {
        this.onFqdnScramblingUnsuccessful = onFqdnScramblingUnsuccessful;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(FqdnScrambling.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("customFqdnLocator");
        sb.append('=');
        sb.append(((this.customFqdnLocator == null) ? "<null>" : this.customFqdnLocator));
        sb.append(',');
        sb.append("onFqdnScramblingUnsuccessful");
        sb.append('=');
        sb.append(((this.onFqdnScramblingUnsuccessful == null) ? "<null>" : this.onFqdnScramblingUnsuccessful));
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
        result = ((result * 31) + ((this.onFqdnScramblingUnsuccessful == null) ? 0 : this.onFqdnScramblingUnsuccessful.hashCode()));
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
        if ((other instanceof FqdnScrambling) == false)
        {
            return false;
        }
        FqdnScrambling rhs = ((FqdnScrambling) other);
        return (((this.onFqdnScramblingUnsuccessful == rhs.onFqdnScramblingUnsuccessful)
                 || ((this.onFqdnScramblingUnsuccessful != null) && this.onFqdnScramblingUnsuccessful.equals(rhs.onFqdnScramblingUnsuccessful)))
                && ((this.customFqdnLocator == rhs.customFqdnLocator)
                    || ((this.customFqdnLocator != null) && this.customFqdnLocator.equals(rhs.customFqdnLocator))));
    }

}
