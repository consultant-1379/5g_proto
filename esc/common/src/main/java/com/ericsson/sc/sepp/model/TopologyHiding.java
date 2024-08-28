
package com.ericsson.sc.sepp.model;

import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-label", "condition", "pseudo-search-result", "ip-address-hiding", "fqdn-mapping", "fqdn-scrambling" })
public class TopologyHiding implements IfNamedListItem
{

    /**
     * Name identifying the topology hiding (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the topology hiding")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * A condition that filters the messages for which topology hiding is applicable
     * (Required)
     * 
     */
    @JsonProperty("condition")
    @JsonPropertyDescription("A condition that filters the messages for which topology hiding is applicable")
    private String condition;
    /**
     * Defines the data of the nrf-discovery response
     * 
     */
    @JsonProperty("pseudo-search-result")
    @JsonPropertyDescription("Defines the data of the nrf-discovery response")
    private PseudoSearchResult pseudoSearchResult;
    /**
     * Enable/Disable IP Address Hiding functionality based on the targeted nf-type
     * 
     */
    @JsonProperty("ip-address-hiding")
    @JsonPropertyDescription("Enable/Disable IP Address Hiding functionality based on the targeted nf-type")
    private IpAddressHiding ipAddressHiding;
    /**
     * Enable/Disable the FQDN Mapping functionality for NRF FQDNs
     * 
     */
    @JsonProperty("fqdn-mapping")
    @JsonPropertyDescription("Enable/Disable the FQDN Mapping functionality for NRF FQDNs")
    private FqdnMapping fqdnMapping;
    /**
     * Enable/Disable the FQDN Scrambling functionality
     * 
     */
    @JsonProperty("fqdn-scrambling")
    @JsonPropertyDescription("Enable/Disable the FQDN Scrambling functionality")
    private FqdnScrambling fqdnScrambling;

    /**
     * Name identifying the topology hiding (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the topology hiding (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public TopologyHiding withName(String name)
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

    public TopologyHiding withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * A condition that filters the messages for which topology hiding is applicable
     * (Required)
     * 
     */
    @JsonProperty("condition")
    public String getCondition()
    {
        return condition;
    }

    /**
     * A condition that filters the messages for which topology hiding is applicable
     * (Required)
     * 
     */
    @JsonProperty("condition")
    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public TopologyHiding withCondition(String condition)
    {
        this.condition = condition;
        return this;
    }

    /**
     * Defines the data of the nrf-discovery response
     * 
     */
    @JsonProperty("pseudo-search-result")
    public PseudoSearchResult getPseudoSearchResult()
    {
        return pseudoSearchResult;
    }

    /**
     * Defines the data of the nrf-discovery response
     * 
     */
    @JsonProperty("pseudo-search-result")
    public void setPseudoSearchResult(PseudoSearchResult pseudoSearchResult)
    {
        this.pseudoSearchResult = pseudoSearchResult;
    }

    public TopologyHiding withPseudoSearchResult(PseudoSearchResult pseudoSearchResult)
    {
        this.pseudoSearchResult = pseudoSearchResult;
        return this;
    }

    /**
     * Enable/Disable IP Address Hiding functionality based on the targeted nf-type
     * 
     */
    @JsonProperty("ip-address-hiding")
    public IpAddressHiding getIpAddressHiding()
    {
        return ipAddressHiding;
    }

    /**
     * Enable/Disable IP Address Hiding functionality based on the targeted nf-type
     * 
     */
    @JsonProperty("ip-address-hiding")
    public void setIpAddressHiding(IpAddressHiding ipAddressHiding)
    {
        this.ipAddressHiding = ipAddressHiding;
    }

    public TopologyHiding withIpAddressHiding(IpAddressHiding ipAddressHiding)
    {
        this.ipAddressHiding = ipAddressHiding;
        return this;
    }

    /**
     * Enable/Disable the FQDN Mapping functionality for NRF FQDNs
     * 
     */
    @JsonProperty("fqdn-mapping")
    public FqdnMapping getFqdnMapping()
    {
        return fqdnMapping;
    }

    /**
     * Enable/Disable the FQDN Mapping functionality for NRF FQDNs
     * 
     */
    @JsonProperty("fqdn-mapping")
    public void setFqdnMapping(FqdnMapping fqdnMapping)
    {
        this.fqdnMapping = fqdnMapping;
    }

    public TopologyHiding withFqdnMapping(FqdnMapping fqdnMapping)
    {
        this.fqdnMapping = fqdnMapping;
        return this;
    }

    /**
     * Enable/Disable the FQDN Scrambling functionality
     * 
     */
    @JsonProperty("fqdn-scrambling")
    public FqdnScrambling getFqdnScrambling()
    {
        return fqdnScrambling;
    }

    /**
     * Enable/Disable the FQDN Scrambling functionality
     * 
     */
    @JsonProperty("fqdn-scrambling")
    public void setFqdnScrambling(FqdnScrambling fqdnScrambling)
    {
        this.fqdnScrambling = fqdnScrambling;
    }

    public TopologyHiding withFqdnScrambling(FqdnScrambling fqdnScrambling)
    {
        this.fqdnScrambling = fqdnScrambling;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TopologyHiding.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("condition");
        sb.append('=');
        sb.append(((this.condition == null) ? "<null>" : this.condition));
        sb.append(',');
        sb.append("pseudoSearchResult");
        sb.append('=');
        sb.append(((this.pseudoSearchResult == null) ? "<null>" : this.pseudoSearchResult));
        sb.append(',');
        sb.append("ipAddressHiding");
        sb.append('=');
        sb.append(((this.ipAddressHiding == null) ? "<null>" : this.ipAddressHiding));
        sb.append(',');
        sb.append("fqdnMapping");
        sb.append('=');
        sb.append(((this.fqdnMapping == null) ? "<null>" : this.fqdnMapping));
        sb.append(',');
        sb.append("fqdnScrambling");
        sb.append('=');
        sb.append(((this.fqdnScrambling == null) ? "<null>" : this.fqdnScrambling));
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
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.fqdnScrambling == null) ? 0 : this.fqdnScrambling.hashCode()));
        result = ((result * 31) + ((this.condition == null) ? 0 : this.condition.hashCode()));
        result = ((result * 31) + ((this.fqdnMapping == null) ? 0 : this.fqdnMapping.hashCode()));
        result = ((result * 31) + ((this.pseudoSearchResult == null) ? 0 : this.pseudoSearchResult.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.ipAddressHiding == null) ? 0 : this.ipAddressHiding.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TopologyHiding) == false)
        {
            return false;
        }
        TopologyHiding rhs = ((TopologyHiding) other);
        return ((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                     && ((this.fqdnScrambling == rhs.fqdnScrambling) || ((this.fqdnScrambling != null) && this.fqdnScrambling.equals(rhs.fqdnScrambling))))
                    && ((this.condition == rhs.condition) || ((this.condition != null) && this.condition.equals(rhs.condition))))
                   && ((this.fqdnMapping == rhs.fqdnMapping) || ((this.fqdnMapping != null) && this.fqdnMapping.equals(rhs.fqdnMapping))))
                  && ((this.pseudoSearchResult == rhs.pseudoSearchResult)
                      || ((this.pseudoSearchResult != null) && this.pseudoSearchResult.equals(rhs.pseudoSearchResult))))
                 && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                && ((this.ipAddressHiding == rhs.ipAddressHiding) || ((this.ipAddressHiding != null) && this.ipAddressHiding.equals(rhs.ipAddressHiding))));
    }

}
