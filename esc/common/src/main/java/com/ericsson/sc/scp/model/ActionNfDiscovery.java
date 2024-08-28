
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Perform delegated NF discovery by querying the NRF with the parameters from
 * the received request.Specific discovery parameters or all the discovery
 * parameters are extracted from the received request based on the configuration
 * of either use-discovery-parameter or add-discovery-parameter.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nrf-group-ref",
                     "request-timeout",
                     "endpoint-ip-family",
                     "use-discovery-parameter",
                     "add-discovery-parameter",
                     "nf-selection-on-priority" })
public class ActionNfDiscovery
{

    /**
     * The group of NRFs providing the Nnrf_NFDiscovery service (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    @JsonPropertyDescription("The group of NRFs providing the Nnrf_NFDiscovery service")
    private String nrfGroupRef;
    /**
     * The timeout of delegated discovery requests
     * 
     */
    @JsonProperty("request-timeout")
    @JsonPropertyDescription("The timeout of delegated discovery requests")
    private Integer requestTimeout = 10000;
    /**
     * IP family of the discovered endpoints to be used. Applicable only when FQDN
     * is not present. (Required)
     * 
     */
    @JsonProperty("endpoint-ip-family")
    @JsonPropertyDescription("IP family of the discovered endpoints to be used. Applicable only when FQDN is not present.")
    private List<EndpointIpFamily> endpointIpFamily = new ArrayList<EndpointIpFamily>();
    /**
     * Discovery parameters from the received request that are used in the delegated
     * discovery
     * 
     */
    @JsonProperty("use-discovery-parameter")
    @JsonPropertyDescription("Discovery parameters from the received request that are used in the delegated discovery")
    private UseDiscoveryParameter useDiscoveryParameter;
    /**
     * Discovery parameters and values to be used in addition or instead of the
     * parameters from the received request
     * 
     */
    @JsonProperty("add-discovery-parameter")
    @JsonPropertyDescription("Discovery parameters and values to be used in addition or instead of the parameters from the received request")
    private List<AddDiscoveryParameter> addDiscoveryParameter = new ArrayList<AddDiscoveryParameter>();
    /**
     * From the discovered list of NF-profiles, choose one NF from those with the
     * highest priority and store it as the preferred host in the variable given in
     * ‘variable-name-selected-host’. The nf-set-if of the chosen host is stored in
     * the variable given in ‘variable-name-nfset’.
     * 
     */
    @JsonProperty("nf-selection-on-priority")
    @JsonPropertyDescription("From the discovered list of NF-profiles, choose one NF from those with the highest priority and store it as the preferred host in the variable given in \u2018variable-name-selected-host\u2019. The nf-set-if of the chosen host is stored in the variable given in \u2018variable-name-nfset\u2019.")
    private NfSelectionOnPriority nfSelectionOnPriority;

    /**
     * The group of NRFs providing the Nnrf_NFDiscovery service (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    public String getNrfGroupRef()
    {
        return nrfGroupRef;
    }

    /**
     * The group of NRFs providing the Nnrf_NFDiscovery service (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    public void setNrfGroupRef(String nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
    }

    public ActionNfDiscovery withNrfGroupRef(String nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
        return this;
    }

    /**
     * The timeout of delegated discovery requests
     * 
     */
    @JsonProperty("request-timeout")
    public Integer getRequestTimeout()
    {
        return requestTimeout;
    }

    /**
     * The timeout of delegated discovery requests
     * 
     */
    @JsonProperty("request-timeout")
    public void setRequestTimeout(Integer requestTimeout)
    {
        this.requestTimeout = requestTimeout;
    }

    public ActionNfDiscovery withRequestTimeout(Integer requestTimeout)
    {
        this.requestTimeout = requestTimeout;
        return this;
    }

    /**
     * IP family of the discovered endpoints to be used. Applicable only when FQDN
     * is not present.
     * 
     */
    @JsonProperty("endpoint-ip-family")
    public List<EndpointIpFamily> getEndpointIpFamily()
    {
        return endpointIpFamily;
    }

    /**
     * IP family of the discovered endpoints to be used. Applicable only when FQDN
     * is not present.
     * 
     */
    @JsonProperty("endpoint-ip-family")
    public void setEndpointIpFamily(List<EndpointIpFamily> endpointIpFamily)
    {
        this.endpointIpFamily = endpointIpFamily;
    }

    public ActionNfDiscovery withEndpointIpFamily(List<EndpointIpFamily> endpointIpFamily)
    {
        this.endpointIpFamily = endpointIpFamily;
        return this;
    }

    /**
     * Discovery parameters from the received request that are used in the delegated
     * discovery
     * 
     */
    @JsonProperty("use-discovery-parameter")
    public UseDiscoveryParameter getUseDiscoveryParameter()
    {
        return useDiscoveryParameter;
    }

    /**
     * Discovery parameters from the received request that are used in the delegated
     * discovery
     * 
     */
    @JsonProperty("use-discovery-parameter")
    public void setUseDiscoveryParameter(UseDiscoveryParameter useDiscoveryParameter)
    {
        this.useDiscoveryParameter = useDiscoveryParameter;
    }

    public ActionNfDiscovery withUseDiscoveryParameter(UseDiscoveryParameter useDiscoveryParameter)
    {
        this.useDiscoveryParameter = useDiscoveryParameter;
        return this;
    }

    /**
     * Discovery parameters and values to be used in addition or instead of the
     * parameters from the received request
     * 
     */
    @JsonProperty("add-discovery-parameter")
    public List<AddDiscoveryParameter> getAddDiscoveryParameter()
    {
        return addDiscoveryParameter;
    }

    /**
     * Discovery parameters and values to be used in addition or instead of the
     * parameters from the received request
     * 
     */
    @JsonProperty("add-discovery-parameter")
    public void setAddDiscoveryParameter(List<AddDiscoveryParameter> addDiscoveryParameter)
    {
        this.addDiscoveryParameter = addDiscoveryParameter;
    }

    public ActionNfDiscovery withAddDiscoveryParameter(List<AddDiscoveryParameter> addDiscoveryParameter)
    {
        this.addDiscoveryParameter = addDiscoveryParameter;
        return this;
    }

    /**
     * From the discovered list of NF-profiles, choose one NF from those with the
     * highest priority and store it as the preferred host in the variable given in
     * ‘variable-name-selected-host’. The nf-set-if of the chosen host is stored in
     * the variable given in ‘variable-name-nfset’.
     * 
     */
    @JsonProperty("nf-selection-on-priority")
    public NfSelectionOnPriority getNfSelectionOnPriority()
    {
        return nfSelectionOnPriority;
    }

    /**
     * From the discovered list of NF-profiles, choose one NF from those with the
     * highest priority and store it as the preferred host in the variable given in
     * ‘variable-name-selected-host’. The nf-set-if of the chosen host is stored in
     * the variable given in ‘variable-name-nfset’.
     * 
     */
    @JsonProperty("nf-selection-on-priority")
    public void setNfSelectionOnPriority(NfSelectionOnPriority nfSelectionOnPriority)
    {
        this.nfSelectionOnPriority = nfSelectionOnPriority;
    }

    public ActionNfDiscovery withNfSelectionOnPriority(NfSelectionOnPriority nfSelectionOnPriority)
    {
        this.nfSelectionOnPriority = nfSelectionOnPriority;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ActionNfDiscovery.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("nrfGroupRef");
        sb.append('=');
        sb.append(((this.nrfGroupRef == null) ? "<null>" : this.nrfGroupRef));
        sb.append(',');
        sb.append("requestTimeout");
        sb.append('=');
        sb.append(((this.requestTimeout == null) ? "<null>" : this.requestTimeout));
        sb.append(',');
        sb.append("endpointIpFamily");
        sb.append('=');
        sb.append(((this.endpointIpFamily == null) ? "<null>" : this.endpointIpFamily));
        sb.append(',');
        sb.append("useDiscoveryParameter");
        sb.append('=');
        sb.append(((this.useDiscoveryParameter == null) ? "<null>" : this.useDiscoveryParameter));
        sb.append(',');
        sb.append("addDiscoveryParameter");
        sb.append('=');
        sb.append(((this.addDiscoveryParameter == null) ? "<null>" : this.addDiscoveryParameter));
        sb.append(',');
        sb.append("nfSelectionOnPriority");
        sb.append('=');
        sb.append(((this.nfSelectionOnPriority == null) ? "<null>" : this.nfSelectionOnPriority));
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
        result = ((result * 31) + ((this.nrfGroupRef == null) ? 0 : this.nrfGroupRef.hashCode()));
        result = ((result * 31) + ((this.useDiscoveryParameter == null) ? 0 : this.useDiscoveryParameter.hashCode()));
        result = ((result * 31) + ((this.addDiscoveryParameter == null) ? 0 : this.addDiscoveryParameter.hashCode()));
        result = ((result * 31) + ((this.nfSelectionOnPriority == null) ? 0 : this.nfSelectionOnPriority.hashCode()));
        result = ((result * 31) + ((this.endpointIpFamily == null) ? 0 : this.endpointIpFamily.hashCode()));
        result = ((result * 31) + ((this.requestTimeout == null) ? 0 : this.requestTimeout.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ActionNfDiscovery) == false)
        {
            return false;
        }
        ActionNfDiscovery rhs = ((ActionNfDiscovery) other);
        return (((((((this.nrfGroupRef == rhs.nrfGroupRef) || ((this.nrfGroupRef != null) && this.nrfGroupRef.equals(rhs.nrfGroupRef)))
                    && ((this.useDiscoveryParameter == rhs.useDiscoveryParameter)
                        || ((this.useDiscoveryParameter != null) && this.useDiscoveryParameter.equals(rhs.useDiscoveryParameter))))
                   && ((this.addDiscoveryParameter == rhs.addDiscoveryParameter)
                       || ((this.addDiscoveryParameter != null) && this.addDiscoveryParameter.equals(rhs.addDiscoveryParameter))))
                  && ((this.nfSelectionOnPriority == rhs.nfSelectionOnPriority)
                      || ((this.nfSelectionOnPriority != null) && this.nfSelectionOnPriority.equals(rhs.nfSelectionOnPriority))))
                 && ((this.endpointIpFamily == rhs.endpointIpFamily)
                     || ((this.endpointIpFamily != null) && this.endpointIpFamily.equals(rhs.endpointIpFamily))))
                && ((this.requestTimeout == rhs.requestTimeout) || ((this.requestTimeout != null) && this.requestTimeout.equals(rhs.requestTimeout))));
    }
}
