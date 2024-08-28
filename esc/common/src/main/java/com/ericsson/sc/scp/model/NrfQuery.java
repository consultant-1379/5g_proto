
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "nrf-group-ref",
                     "requester-nf-type",
                     "nf-type",
                     "nf-set-id",
                     "nf-service-set-id",
                     "preferred-locality",
                     "requester-plmn",
                     "requester-snssai",
                     "scp-domain",
                     "service-name",
                     "query-parameter" })
public class NrfQuery
{

    /**
     * Name identifying the nrf-query (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the nrf-query")
    private String name;
    /**
     * Reference to a list of NRF groups to be used for the NF discovery. If
     * unspecified, the NRF group referenced by
     * nf-pool/nf-pool-discovery/nrf-group-ref applies (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    @JsonPropertyDescription("Reference to a list of NRF groups to be used for the NF discovery. If unspecified, the NRF groups referenced by nf-pool/nf-pool-discovery/nrf-group-ref apply")
    private List<String> nrfGroupRef = new ArrayList<String>();
    /**
     * The NF type of the requester NF (according to TS 29.510)
     * 
     */
    @JsonProperty("requester-nf-type")
    @JsonPropertyDescription("The NF type of the requester NF (according to TS 29.510)")
    private String requesterNfType = "SCP";
    /**
     * The NF type of the targeted NF (according to TS 29.510) (Required)
     * 
     */
    @JsonProperty("nf-type")
    @JsonPropertyDescription("The NF type of the targeted NF (according to TS 29.510)")
    private String nfType;
    /**
     * The set identity of the targeted NF
     * 
     */
    @JsonProperty("nf-set-id")
    @JsonPropertyDescription("The set identity of the targeted NF")
    private String nfSetId;
    /**
     * The set identity of the targeted NF service
     * 
     */
    @JsonProperty("nf-service-set-id")
    @JsonPropertyDescription("The set identity of the targeted NF service")
    private String nfServiceSetId;
    /**
     * The preferred locality of the targeted NF
     * 
     */
    @JsonProperty("preferred-locality")
    @JsonPropertyDescription("The preferred locality of the targeted NF")
    private String preferredLocality;
    /**
     * The PLMN identity of the requester NF. Format: MCC (3 digits) followed by MNC
     * (2 or 3 digits). Example for MCC=123 and MNC=45: 12345
     * 
     */
    @JsonProperty("requester-plmn")
    @JsonPropertyDescription("The PLMN identity of the requester NF. Format: MCC (3 digits) followed by MNC (2 or 3 digits). Example for MCC=123 and MNC=45: 12345")
    private List<String> requesterPlmn = new ArrayList<String>();
    /**
     * Slice info of the requester NF
     * 
     */
    @JsonProperty("requester-snssai")
    @JsonPropertyDescription("Slice info of the requester NF")
    private List<RequesterSnssai> requesterSnssai = new ArrayList<RequesterSnssai>();
    /**
     * The SCP domain the target NF or SCP belongs to
     * 
     */
    @JsonProperty("scp-domain")
    @JsonPropertyDescription("The SCP domain the target NF or SCP belongs to")
    private List<String> scpDomain = new ArrayList<String>();
    /**
     * The name of the service offered by the targeted NF
     * 
     */
    @JsonProperty("service-name")
    @JsonPropertyDescription("The name of the service offered by the targeted NF")
    private List<String> serviceName = new ArrayList<String>();
    /**
     * Custom query parameter used for parameters not covered by non-custom
     * attributes in nrf-query. Query parameter names already covered may not be
     * used. Apart from this, possible name and value are according to TS 29.510
     * 
     */
    @JsonProperty("query-parameter")
    @JsonPropertyDescription("Custom query parameter, possible name and value according to TS 29.510")
    private List<QueryParameter> queryParameter = new ArrayList<QueryParameter>();

    /**
     * Name identifying the nrf-query (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the nrf-query (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public NrfQuery withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Reference to a list of NRF groups to be used for the NF discovery. If
     * unspecified, the NRF group referenced by
     * nf-pool/nf-pool-discovery/nrf-group-ref applies (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    public List<String> getNrfGroupRef()
    {
        return nrfGroupRef;
    }

    /**
     * Reference to a list of NRF groups to be used for the NF discovery. If
     * unspecified, the NRF group referenced by
     * nf-pool/nf-pool-discovery/nrf-group-ref applies (Required)
     * 
     */
    @JsonProperty("nrf-group-ref")
    public void setNrfGroupRef(List<String> nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
    }

    public NrfQuery withNrfGroupRef(List<String> nrfGroupRef)
    {
        this.nrfGroupRef = nrfGroupRef;
        return this;
    }

    /**
     * The NF type of the requester NF (according to TS 29.510)
     * 
     */
    @JsonProperty("requester-nf-type")
    public String getRequesterNfType()
    {
        return requesterNfType;
    }

    /**
     * The NF type of the requester NF (according to TS 29.510)
     * 
     */
    @JsonProperty("requester-nf-type")
    public void setRequesterNfType(String requesterNfType)
    {
        this.requesterNfType = requesterNfType;
    }

    public NrfQuery withRequesterNfType(String requesterNfType)
    {
        this.requesterNfType = requesterNfType;
        return this;
    }

    /**
     * The NF type of the targeted NF (according to TS 29.510) (Required)
     * 
     */
    @JsonProperty("nf-type")
    public String getNfType()
    {
        return nfType;
    }

    /**
     * The NF type of the targeted NF (according to TS 29.510) (Required)
     * 
     */
    @JsonProperty("nf-type")
    public void setNfType(String nfType)
    {
        this.nfType = nfType;
    }

    public NrfQuery withNfType(String nfType)
    {
        this.nfType = nfType;
        return this;
    }

    /**
     * The set identity of the targeted NF
     * 
     */
    @JsonProperty("nf-set-id")
    public String getNfSetId()
    {
        return nfSetId;
    }

    /**
     * The set identity of the targeted NF
     * 
     */
    @JsonProperty("nf-set-id")
    public void setNfSetId(String nfSetId)
    {
        this.nfSetId = nfSetId;
    }

    public NrfQuery withNfSetId(String nfSetId)
    {
        this.nfSetId = nfSetId;
        return this;
    }

    /**
     * The set identity of the targeted NF service
     * 
     */
    @JsonProperty("nf-service-set-id")
    public String getNfServiceSetId()
    {
        return nfServiceSetId;
    }

    /**
     * The set identity of the targeted NF service
     * 
     */
    @JsonProperty("nf-service-set-id")
    public void setNfServiceSetId(String nfServiceSetId)
    {
        this.nfServiceSetId = nfServiceSetId;
    }

    public NrfQuery withNfServiceSetId(String nfServiceSetId)
    {
        this.nfServiceSetId = nfServiceSetId;
        return this;
    }

    /**
     * The preferred locality of the targeted NF
     * 
     */
    @JsonProperty("preferred-locality")
    public String getPreferredLocality()
    {
        return preferredLocality;
    }

    /**
     * The preferred locality of the targeted NF
     * 
     */
    @JsonProperty("preferred-locality")
    public void setPreferredLocality(String preferredLocality)
    {
        this.preferredLocality = preferredLocality;
    }

    public NrfQuery withPreferredLocality(String preferredLocality)
    {
        this.preferredLocality = preferredLocality;
        return this;
    }

    /**
     * The PLMN identity of the requester NF. Format: MCC (3 digits) followed by MNC
     * (2 or 3 digits). Example for MCC=123 and MNC=45: 12345
     * 
     */
    @JsonProperty("requester-plmn")
    public List<String> getRequesterPlmn()
    {
        return requesterPlmn;
    }

    /**
     * The PLMN identity of the requester NF. Format: MCC (3 digits) followed by MNC
     * (2 or 3 digits). Example for MCC=123 and MNC=45: 12345
     * 
     */
    @JsonProperty("requester-plmn")
    public void setRequesterPlmn(List<String> requesterPlmn)
    {
        this.requesterPlmn = requesterPlmn;
    }

    public NrfQuery withRequesterPlmn(List<String> requesterPlmn)
    {
        this.requesterPlmn = requesterPlmn;
        return this;
    }

    /**
     * Slice info of the requester NF
     * 
     */
    @JsonProperty("requester-snssai")
    public List<RequesterSnssai> getRequesterSnssai()
    {
        return requesterSnssai;
    }

    /**
     * Slice info of the requester NF
     * 
     */
    @JsonProperty("requester-snssai")
    public void setRequesterSnssai(List<RequesterSnssai> requesterSnssai)
    {
        this.requesterSnssai = requesterSnssai;
    }

    public NrfQuery withRequesterSnssai(List<RequesterSnssai> requesterSnssai)
    {
        this.requesterSnssai = requesterSnssai;
        return this;
    }

    /**
     * The SCP domain the target NF or SCP belongs to
     * 
     */
    @JsonProperty("scp-domain")
    public List<String> getScpDomain()
    {
        return scpDomain;
    }

    /**
     * The SCP domain the target NF or SCP belongs to
     * 
     */
    @JsonProperty("scp-domain")
    public void setScpDomain(List<String> scpDomain)
    {
        this.scpDomain = scpDomain;
    }

    public NrfQuery withScpDomain(List<String> scpDomain)
    {
        this.scpDomain = scpDomain;
        return this;
    }

    /**
     * The name of the service offered by the targeted NF
     * 
     */
    @JsonProperty("service-name")
    public List<String> getServiceName()
    {
        return serviceName;
    }

    /**
     * The name of the service offered by the targeted NF
     * 
     */
    @JsonProperty("service-name")
    public void setServiceName(List<String> serviceName)
    {
        this.serviceName = serviceName;
    }

    public NrfQuery withServiceName(List<String> serviceName)
    {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Custom query parameter used for parameters not covered by non-custom
     * attributes in nrf-query. Query parameter names already covered may not be
     * used. Apart from this, possible name and value are according to TS 29.510
     * 
     */
    @JsonProperty("query-parameter")
    public List<QueryParameter> getQueryParameter()
    {
        return queryParameter;
    }

    /**
     * Custom query parameter used for parameters not covered by non-custom
     * attributes in nrf-query. Query parameter names already covered may not be
     * used. Apart from this, possible name and value are according to TS 29.510
     * 
     */
    @JsonProperty("query-parameter")
    public void setQueryParameter(List<QueryParameter> queryParameter)
    {
        this.queryParameter = queryParameter;
    }

    public NrfQuery withQueryParameter(List<QueryParameter> queryParameter)
    {
        this.queryParameter = queryParameter;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NrfQuery.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("nrfGroupRef");
        sb.append('=');
        sb.append(((this.nrfGroupRef == null) ? "<null>" : this.nrfGroupRef));
        sb.append(',');
        sb.append("requesterNfType");
        sb.append('=');
        sb.append(((this.requesterNfType == null) ? "<null>" : this.requesterNfType));
        sb.append(',');
        sb.append("nfType");
        sb.append('=');
        sb.append(((this.nfType == null) ? "<null>" : this.nfType));
        sb.append(',');
        sb.append("nfSetId");
        sb.append('=');
        sb.append(((this.nfSetId == null) ? "<null>" : this.nfSetId));
        sb.append(',');
        sb.append("nfServiceSetId");
        sb.append('=');
        sb.append(((this.nfServiceSetId == null) ? "<null>" : this.nfServiceSetId));
        sb.append(',');
        sb.append("preferredLocality");
        sb.append('=');
        sb.append(((this.preferredLocality == null) ? "<null>" : this.preferredLocality));
        sb.append(',');
        sb.append("requesterPlmn");
        sb.append('=');
        sb.append(((this.requesterPlmn == null) ? "<null>" : this.requesterPlmn));
        sb.append(',');
        sb.append("requesterSnssai");
        sb.append('=');
        sb.append(((this.requesterSnssai == null) ? "<null>" : this.requesterSnssai));
        sb.append(',');
        sb.append("scpDomain");
        sb.append('=');
        sb.append(((this.scpDomain == null) ? "<null>" : this.scpDomain));
        sb.append(',');
        sb.append("serviceName");
        sb.append('=');
        sb.append(((this.serviceName == null) ? "<null>" : this.serviceName));
        sb.append(',');
        sb.append("queryParameter");
        sb.append('=');
        sb.append(((this.queryParameter == null) ? "<null>" : this.queryParameter));
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
        result = ((result * 31) + ((this.scpDomain == null) ? 0 : this.scpDomain.hashCode()));
        result = ((result * 31) + ((this.requesterSnssai == null) ? 0 : this.requesterSnssai.hashCode()));
        result = ((result * 31) + ((this.nfType == null) ? 0 : this.nfType.hashCode()));
        result = ((result * 31) + ((this.requesterPlmn == null) ? 0 : this.requesterPlmn.hashCode()));
        result = ((result * 31) + ((this.serviceName == null) ? 0 : this.serviceName.hashCode()));
        result = ((result * 31) + ((this.nrfGroupRef == null) ? 0 : this.nrfGroupRef.hashCode()));
        result = ((result * 31) + ((this.preferredLocality == null) ? 0 : this.preferredLocality.hashCode()));
        result = ((result * 31) + ((this.nfServiceSetId == null) ? 0 : this.nfServiceSetId.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.nfSetId == null) ? 0 : this.nfSetId.hashCode()));
        result = ((result * 31) + ((this.queryParameter == null) ? 0 : this.queryParameter.hashCode()));
        result = ((result * 31) + ((this.requesterNfType == null) ? 0 : this.requesterNfType.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NrfQuery) == false)
        {
            return false;
        }
        NrfQuery rhs = ((NrfQuery) other);
        return (((((((((((((this.scpDomain == rhs.scpDomain) || ((this.scpDomain != null) && this.scpDomain.equals(rhs.scpDomain)))
                          && ((this.requesterSnssai == rhs.requesterSnssai)
                              || ((this.requesterSnssai != null) && this.requesterSnssai.equals(rhs.requesterSnssai))))
                         && ((this.nfType == rhs.nfType) || ((this.nfType != null) && this.nfType.equals(rhs.nfType))))
                        && ((this.requesterPlmn == rhs.requesterPlmn) || ((this.requesterPlmn != null) && this.requesterPlmn.equals(rhs.requesterPlmn))))
                       && ((this.serviceName == rhs.serviceName) || ((this.serviceName != null) && this.serviceName.equals(rhs.serviceName))))
                      && ((this.nrfGroupRef == rhs.nrfGroupRef) || ((this.nrfGroupRef != null) && this.nrfGroupRef.equals(rhs.nrfGroupRef))))
                     && ((this.preferredLocality == rhs.preferredLocality)
                         || ((this.preferredLocality != null) && this.preferredLocality.equals(rhs.preferredLocality))))
                    && ((this.nfServiceSetId == rhs.nfServiceSetId) || ((this.nfServiceSetId != null) && this.nfServiceSetId.equals(rhs.nfServiceSetId))))
                   && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                  && ((this.nfSetId == rhs.nfSetId) || ((this.nfSetId != null) && this.nfSetId.equals(rhs.nfSetId))))
                 && ((this.queryParameter == rhs.queryParameter) || ((this.queryParameter != null) && this.queryParameter.equals(rhs.queryParameter))))
                && ((this.requesterNfType == rhs.requesterNfType) || ((this.requesterNfType != null) && this.requesterNfType.equals(rhs.requesterNfType))));
    }

}
