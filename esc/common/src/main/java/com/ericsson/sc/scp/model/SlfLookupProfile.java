
package com.ericsson.sc.scp.model;

import com.ericsson.sc.IdentityType;
import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "nrf-group-ref",
                     "request-timeout",
                     "identity-type",
                     "requester-nf-type",
                     "target-nf-type",
                     "message-data-ref",
                     "result-variable-name",
                     "routing-case-identity-not-found",
                     "routing-case-destination-unknown",
                     "routing-case-identity-missing",
                     "routing-case-lookup-failure" })
public class SlfLookupProfile implements IfNamedListItem
{

    /**
     * Name identifying the SLF lookup profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the SLF lookup profile")
    private String name;
    /**
     * Reference to the NRF group to be used by SLF for the NF discovery. If
     * unspecified, the NRF group referenced by
     * nrf-service/nf-discovery/nrf-group-ref applies
     * 
     */
    @JsonProperty("nrf-group-ref")
    @JsonPropertyDescription("Reference to the NRF group to be used by SLF for the NF discovery. If unspecified, the NRF group referenced by nrf-service/nf-discovery/nrf-group-ref applies")
    private String nrfGroupRef;
    /**
     * The timeout of SLF interrogation requests
     * 
     */
    @JsonProperty("request-timeout")
    @JsonPropertyDescription("The timeout of SLF interrogation requests")
    private Integer requestTimeout = 10000;
    /**
     * The type of subscriber identifier based on which messages should be routed
     * 
     */
    @JsonProperty("identity-type")
    @JsonPropertyDescription("The type of subscriber identifier based on which messages should be routed")
    private IdentityType identityType = IdentityType.fromValue("supi");
    /**
     * The NF type of the NF requesting the SLF interrogation (according to TS
     * 29.510)
     * 
     */
    @JsonProperty("requester-nf-type")
    @JsonPropertyDescription("The NF type of the NF requesting the SLF interrogation (according to TS 29.510)")
    private String requesterNfType = "SMF";
    /**
     * The NF type of the targeted NF for SLF interrogation (according to TS 29.510)
     * 
     */
    @JsonProperty("target-nf-type")
    @JsonPropertyDescription("The NF type of the targeted NF for SLF interrogation (according to TS 29.510)")
    private String targetNfType = "CHF";
    /**
     * Reference to message-data MO that defines the data to extract from a message
     * (Required)
     * 
     */
    @JsonProperty("message-data-ref")
    @JsonPropertyDescription("Reference to message-data MO that defines the data to extract from a message")
    private String messageDataRef;
    /**
     * Variable name to store the outcome of the SLF interrogation
     * 
     */
    @JsonProperty("result-variable-name")
    @JsonPropertyDescription("Variable name to store the outcome of the SLF interrogation")
    private String resultVariableName = "region";
    /**
     * Routing case where the execution will continue when the interrogated
     * supi/gpsi is not found in the SLF lookup
     * 
     */
    @JsonProperty("routing-case-identity-not-found")
    @JsonPropertyDescription("Routing case where the execution will continue when the interrogated supi/gpsi is not found in the SLF lookup")
    private String routingCaseIdentityNotFound;
    /**
     * Routing case where the execution will continue when the SLF interrogation
     * result does not exist
     * 
     */
    @JsonProperty("routing-case-destination-unknown")
    @JsonPropertyDescription("Routing case where the execution will continue when the SLF interrogation result does not exist")
    private String routingCaseDestinationUnknown;
    /**
     * Routing case where the execution will continue when the subscriber identity
     * cannot be retrieved from the request message
     * 
     */
    @JsonProperty("routing-case-identity-missing")
    @JsonPropertyDescription("Routing case where the execution will continue when the subscriber identity cannot be retrieved from the request message")
    private String routingCaseIdentityMissing;
    /**
     * Routing case where the execution will continue when the SLF interrogation
     * fails
     * 
     */
    @JsonProperty("routing-case-lookup-failure")
    @JsonPropertyDescription("Routing case where the execution will continue when the SLF interrogation fails")
    private String routingCaseLookupFailure;

    /**
     * Name identifying the SLF lookup profile (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the SLF lookup profile (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public SlfLookupProfile withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Reference to the NRF group to be used by SLF for the NF discovery. If
     * unspecified, the NRF group referenced by
     * nrf-service/nf-discovery/nrf-group-ref applies
     * 
     */
    @JsonProperty("nrf-group-ref")
    public String getNrfGroupRef()
    {
        return nrfGroupRef;
    }

    /**
     * Reference to the NRF group to be used by SLF for the NF discovery. If
     * unspecified, the NRF group referenced by
     * nrf-service/nf-discovery/nrf-group-ref applies
     * 
     */
    @JsonProperty("nrf-group-ref")
    public void setNrfGroupRef(String nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
    }

    public SlfLookupProfile withNrfGroupRef(String nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
        return this;
    }

    /**
     * The timeout of SLF interrogation requests
     * 
     */
    @JsonProperty("request-timeout")
    public Integer getRequestTimeout()
    {
        return requestTimeout;
    }

    /**
     * The timeout of SLF interrogation requests
     * 
     */
    @JsonProperty("request-timeout")
    public void setRequestTimeout(Integer requestTimeout)
    {
        this.requestTimeout = requestTimeout;
    }

    public SlfLookupProfile withRequestTimeout(Integer requestTimeout)
    {
        this.requestTimeout = requestTimeout;
        return this;
    }

    /**
     * The type of subscriber identifier based on which messages should be routed
     * 
     */
    @JsonProperty("identity-type")
    public IdentityType getIdentityType()
    {
        return identityType;
    }

    /**
     * The type of subscriber identifier based on which messages should be routed
     * 
     */
    @JsonProperty("identity-type")
    public void setIdentityType(IdentityType identityType)
    {
        this.identityType = identityType;
    }

    public SlfLookupProfile withIdentityType(IdentityType identityType)
    {
        this.identityType = identityType;
        return this;
    }

    /**
     * The NF type of the NF requesting the SLF interrogation (according to TS
     * 29.510)
     * 
     */
    @JsonProperty("requester-nf-type")
    public String getRequesterNfType()
    {
        return requesterNfType;
    }

    /**
     * The NF type of the NF requesting the SLF interrogation (according to TS
     * 29.510)
     * 
     */
    @JsonProperty("requester-nf-type")
    public void setRequesterNfType(String requesterNfType)
    {
        this.requesterNfType = requesterNfType;
    }

    public SlfLookupProfile withRequesterNfType(String requesterNfType)
    {
        this.requesterNfType = requesterNfType;
        return this;
    }

    /**
     * The NF type of the targeted NF for SLF interrogation (according to TS 29.510)
     * 
     */
    @JsonProperty("target-nf-type")
    public String getTargetNfType()
    {
        return targetNfType;
    }

    /**
     * The NF type of the targeted NF for SLF interrogation (according to TS 29.510)
     * 
     */
    @JsonProperty("target-nf-type")
    public void setTargetNfType(String targetNfType)
    {
        this.targetNfType = targetNfType;
    }

    public SlfLookupProfile withTargetNfType(String targetNfType)
    {
        this.targetNfType = targetNfType;
        return this;
    }

    /**
     * Reference to message-data MO that defines the data to extract from a message
     * (Required)
     * 
     */
    @JsonProperty("message-data-ref")
    public String getMessageDataRef()
    {
        return messageDataRef;
    }

    /**
     * Reference to message-data MO that defines the data to extract from a message
     * (Required)
     * 
     */
    @JsonProperty("message-data-ref")
    public void setMessageDataRef(String messageDataRef)
    {
        this.messageDataRef = messageDataRef;
    }

    public SlfLookupProfile withMessageDataRef(String messageDataRef)
    {
        this.messageDataRef = messageDataRef;
        return this;
    }

    /**
     * Variable name to store the outcome of the SLF interrogation
     * 
     */
    @JsonProperty("result-variable-name")
    public String getResultVariableName()
    {
        return resultVariableName;
    }

    /**
     * Variable name to store the outcome of the SLF interrogation
     * 
     */
    @JsonProperty("result-variable-name")
    public void setResultVariableName(String resultVariableName)
    {
        this.resultVariableName = resultVariableName;
    }

    public SlfLookupProfile withResultVariableName(String resultVariableName)
    {
        this.resultVariableName = resultVariableName;
        return this;
    }

    /**
     * Routing case where the execution will continue when the interrogated
     * supi/gpsi is not found in the SLF lookup
     * 
     */
    @JsonProperty("routing-case-identity-not-found")
    public String getRoutingCaseIdentityNotFound()
    {
        return routingCaseIdentityNotFound;
    }

    /**
     * Routing case where the execution will continue when the interrogated
     * supi/gpsi is not found in the SLF lookup
     * 
     */
    @JsonProperty("routing-case-identity-not-found")
    public void setRoutingCaseIdentityNotFound(String routingCaseIdentityNotFound)
    {
        this.routingCaseIdentityNotFound = routingCaseIdentityNotFound;
    }

    public SlfLookupProfile withRoutingCaseIdentityNotFound(String routingCaseIdentityNotFound)
    {
        this.routingCaseIdentityNotFound = routingCaseIdentityNotFound;
        return this;
    }

    /**
     * Routing case where the execution will continue when the SLF interrogation
     * result does not exist
     * 
     */
    @JsonProperty("routing-case-destination-unknown")
    public String getRoutingCaseDestinationUnknown()
    {
        return routingCaseDestinationUnknown;
    }

    /**
     * Routing case where the execution will continue when the SLF interrogation
     * result does not exist
     * 
     */
    @JsonProperty("routing-case-destination-unknown")
    public void setRoutingCaseDestinationUnknown(String routingCaseDestinationUnknown)
    {
        this.routingCaseDestinationUnknown = routingCaseDestinationUnknown;
    }

    public SlfLookupProfile withRoutingCaseDestinationUnknown(String routingCaseDestinationUnknown)
    {
        this.routingCaseDestinationUnknown = routingCaseDestinationUnknown;
        return this;
    }

    /**
     * Routing case where the execution will continue when the subscriber identity
     * cannot be retrieved from the request message
     * 
     */
    @JsonProperty("routing-case-identity-missing")
    public String getRoutingCaseIdentityMissing()
    {
        return routingCaseIdentityMissing;
    }

    /**
     * Routing case where the execution will continue when the subscriber identity
     * cannot be retrieved from the request message
     * 
     */
    @JsonProperty("routing-case-identity-missing")
    public void setRoutingCaseIdentityMissing(String routingCaseIdentityMissing)
    {
        this.routingCaseIdentityMissing = routingCaseIdentityMissing;
    }

    public SlfLookupProfile withRoutingCaseIdentityMissing(String routingCaseIdentityMissing)
    {
        this.routingCaseIdentityMissing = routingCaseIdentityMissing;
        return this;
    }

    /**
     * Routing case where the execution will continue when the SLF interrogation
     * fails
     * 
     */
    @JsonProperty("routing-case-lookup-failure")
    public String getRoutingCaseLookupFailure()
    {
        return routingCaseLookupFailure;
    }

    /**
     * Routing case where the execution will continue when the SLF interrogation
     * fails
     * 
     */
    @JsonProperty("routing-case-lookup-failure")
    public void setRoutingCaseLookupFailure(String routingCaseLookupFailure)
    {
        this.routingCaseLookupFailure = routingCaseLookupFailure;
    }

    public SlfLookupProfile withRoutingCaseLookupFailure(String routingCaseLookupFailure)
    {
        this.routingCaseLookupFailure = routingCaseLookupFailure;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SlfLookupProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("nrfGroupRef");
        sb.append('=');
        sb.append(((this.nrfGroupRef == null) ? "<null>" : this.nrfGroupRef));
        sb.append(',');
        sb.append("requestTimeout");
        sb.append('=');
        sb.append(((this.requestTimeout == null) ? "<null>" : this.requestTimeout));
        sb.append(',');
        sb.append("identityType");
        sb.append('=');
        sb.append(((this.identityType == null) ? "<null>" : this.identityType));
        sb.append(',');
        sb.append("requesterNfType");
        sb.append('=');
        sb.append(((this.requesterNfType == null) ? "<null>" : this.requesterNfType));
        sb.append(',');
        sb.append("targetNfType");
        sb.append('=');
        sb.append(((this.targetNfType == null) ? "<null>" : this.targetNfType));
        sb.append(',');
        sb.append("messageDataRef");
        sb.append('=');
        sb.append(((this.messageDataRef == null) ? "<null>" : this.messageDataRef));
        sb.append(',');
        sb.append("resultVariableName");
        sb.append('=');
        sb.append(((this.resultVariableName == null) ? "<null>" : this.resultVariableName));
        sb.append(',');
        sb.append("routingCaseIdentityNotFound");
        sb.append('=');
        sb.append(((this.routingCaseIdentityNotFound == null) ? "<null>" : this.routingCaseIdentityNotFound));
        sb.append(',');
        sb.append("routingCaseDestinationUnknown");
        sb.append('=');
        sb.append(((this.routingCaseDestinationUnknown == null) ? "<null>" : this.routingCaseDestinationUnknown));
        sb.append(',');
        sb.append("routingCaseIdentityMissing");
        sb.append('=');
        sb.append(((this.routingCaseIdentityMissing == null) ? "<null>" : this.routingCaseIdentityMissing));
        sb.append(',');
        sb.append("routingCaseLookupFailure");
        sb.append('=');
        sb.append(((this.routingCaseLookupFailure == null) ? "<null>" : this.routingCaseLookupFailure));
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
        result = ((result * 31) + ((this.routingCaseIdentityNotFound == null) ? 0 : this.routingCaseIdentityNotFound.hashCode()));
        result = ((result * 31) + ((this.routingCaseIdentityMissing == null) ? 0 : this.routingCaseIdentityMissing.hashCode()));
        result = ((result * 31) + ((this.routingCaseLookupFailure == null) ? 0 : this.routingCaseLookupFailure.hashCode()));
        result = ((result * 31) + ((this.resultVariableName == null) ? 0 : this.resultVariableName.hashCode()));
        result = ((result * 31) + ((this.targetNfType == null) ? 0 : this.targetNfType.hashCode()));
        result = ((result * 31) + ((this.nrfGroupRef == null) ? 0 : this.nrfGroupRef.hashCode()));
        result = ((result * 31) + ((this.identityType == null) ? 0 : this.identityType.hashCode()));
        result = ((result * 31) + ((this.routingCaseDestinationUnknown == null) ? 0 : this.routingCaseDestinationUnknown.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.messageDataRef == null) ? 0 : this.messageDataRef.hashCode()));
        result = ((result * 31) + ((this.requesterNfType == null) ? 0 : this.requesterNfType.hashCode()));
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
        if ((other instanceof SlfLookupProfile) == false)
        {
            return false;
        }
        SlfLookupProfile rhs = ((SlfLookupProfile) other);
        return (((((((((((((this.routingCaseIdentityNotFound == rhs.routingCaseIdentityNotFound)
                           || ((this.routingCaseIdentityNotFound != null) && this.routingCaseIdentityNotFound.equals(rhs.routingCaseIdentityNotFound)))
                          && ((this.routingCaseIdentityMissing == rhs.routingCaseIdentityMissing)
                              || ((this.routingCaseIdentityMissing != null) && this.routingCaseIdentityMissing.equals(rhs.routingCaseIdentityMissing))))
                         && ((this.routingCaseLookupFailure == rhs.routingCaseLookupFailure)
                             || ((this.routingCaseLookupFailure != null) && this.routingCaseLookupFailure.equals(rhs.routingCaseLookupFailure))))
                        && ((this.resultVariableName == rhs.resultVariableName)
                            || ((this.resultVariableName != null) && this.resultVariableName.equals(rhs.resultVariableName))))
                       && ((this.targetNfType == rhs.targetNfType) || ((this.targetNfType != null) && this.targetNfType.equals(rhs.targetNfType))))
                      && ((this.nrfGroupRef == rhs.nrfGroupRef) || ((this.nrfGroupRef != null) && this.nrfGroupRef.equals(rhs.nrfGroupRef))))
                     && ((this.identityType == rhs.identityType) || ((this.identityType != null) && this.identityType.equals(rhs.identityType))))
                    && ((this.routingCaseDestinationUnknown == rhs.routingCaseDestinationUnknown)
                        || ((this.routingCaseDestinationUnknown != null) && this.routingCaseDestinationUnknown.equals(rhs.routingCaseDestinationUnknown))))
                   && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                  && ((this.messageDataRef == rhs.messageDataRef) || ((this.messageDataRef != null) && this.messageDataRef.equals(rhs.messageDataRef))))
                 && ((this.requesterNfType == rhs.requesterNfType) || ((this.requesterNfType != null) && this.requesterNfType.equals(rhs.requesterNfType))))
                && ((this.requestTimeout == rhs.requestTimeout) || ((this.requestTimeout != null) && this.requestTimeout.equals(rhs.requestTimeout))));
    }

}
