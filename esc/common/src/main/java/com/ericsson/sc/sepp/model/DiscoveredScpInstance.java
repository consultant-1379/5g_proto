
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.sc.glue.IfDiscoveredScpInstance;
import com.ericsson.sc.nfm.model.NfStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nf-instance-id",
                     "nf-status",
                     "served-nf-set-id",
                     "discovered-scp-domain-info",
                     "name",
                     "nf-type",
                     "locality",
                     "nf-set-id",
                     "scp-domain" })
public class DiscoveredScpInstance implements IfDiscoveredScpInstance, Comparable<DiscoveredScpInstance>
{

    /**
     * The NF instance identity
     * 
     */
    @JsonProperty("nf-instance-id")
    @JsonPropertyDescription("The NF instance identity")
    private String nfInstanceId;
    /**
     * The status of the NF
     * 
     */
    @JsonProperty("nf-status")
    @JsonPropertyDescription("The status of the NF")
    private NfStatus nfStatus;
    /**
     * The set identity of the NFs served by the SCP
     * 
     */
    @JsonProperty("served-nf-set-id")
    @JsonPropertyDescription("The set identity of the NFs served by the SCP")
    private List<String> servedNfSetId = new ArrayList<String>();
    /**
     * The info about the discovered scp domain
     * 
     */
    @JsonProperty("discovered-scp-domain-info")
    @JsonPropertyDescription("The info about the discovered scp domain")
    private List<DiscoveredScpDomainInfo> discoveredScpDomainInfo = new ArrayList<DiscoveredScpDomainInfo>();
    /**
     * Human readable name of the NF (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Human readable name of the NF")
    private String name;
    /**
     * The type of the NF (according to TS 29.510)
     * 
     */
    @JsonProperty("nf-type")
    @JsonPropertyDescription("The type of the NF (according to TS 29.510)")
    private String nfType;
    /**
     * The geographic locality of the NF
     * 
     */
    @JsonProperty("locality")
    @JsonPropertyDescription("The geographic locality of the NF")
    private String locality;
    /**
     * The set identity of the NF
     * 
     */
    @JsonProperty("nf-set-id")
    @JsonPropertyDescription("The set identity of the NF")
    private List<String> nfSetId = new ArrayList<String>();
    /**
     * The SCP domains this NF is associated with
     * 
     */
    @JsonProperty("scp-domain")
    @JsonPropertyDescription("The SCP domains this NF is associated with")
    private List<String> scpDomain = new ArrayList<String>();

    /**
     * The NF instance identity
     * 
     */
    @JsonProperty("nf-instance-id")
    public String getNfInstanceId()
    {
        return nfInstanceId;
    }

    /**
     * The NF instance identity
     * 
     */
    @JsonProperty("nf-instance-id")
    public void setNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
    }

    public DiscoveredScpInstance withNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
        return this;
    }

    /**
     * The status of the NF
     * 
     */
    @JsonProperty("nf-status")
    public NfStatus getNfStatus()
    {
        return nfStatus;
    }

    /**
     * The status of the NF
     * 
     */
    @JsonProperty("nf-status")
    public void setNfStatus(NfStatus nfStatus)
    {
        this.nfStatus = nfStatus;
    }

    public DiscoveredScpInstance withNfStatus(NfStatus nfStatus)
    {
        this.nfStatus = nfStatus;
        return this;
    }

    /**
     * The set identity of the NFs served by the SCP
     * 
     */
    @JsonProperty("served-nf-set-id")
    public List<String> getServedNfSetId()
    {
        return servedNfSetId;
    }

    /**
     * The set identity of the NFs served by the SCP
     * 
     */
    @JsonProperty("served-nf-set-id")
    public void setServedNfSetId(List<String> servedNfSetId)
    {
        this.servedNfSetId = servedNfSetId;
    }

    public DiscoveredScpInstance withServedNfSetId(List<String> servedNfSetId)
    {
        this.servedNfSetId = servedNfSetId;
        return this;
    }

    /**
     * The info about the discovered scp domain
     * 
     */
    @JsonProperty("discovered-scp-domain-info")
    public List<DiscoveredScpDomainInfo> getDiscoveredScpDomainInfo()
    {
        return discoveredScpDomainInfo;
    }

    /**
     * The info about the discovered scp domain
     * 
     */
    @JsonProperty("discovered-scp-domain-info")
    public void setDiscoveredScpDomainInfo(List<DiscoveredScpDomainInfo> discoveredScpDomainInfo)
    {
        this.discoveredScpDomainInfo = discoveredScpDomainInfo;
    }

    public DiscoveredScpInstance withDiscoveredScpDomainInfo(List<DiscoveredScpDomainInfo> discoveredScpDomainInfo)
    {
        this.discoveredScpDomainInfo = discoveredScpDomainInfo;
        return this;
    }

    /**
     * Human readable name of the NF (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Human readable name of the NF (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public DiscoveredScpInstance withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The type of the NF (according to TS 29.510)
     * 
     */
    @JsonProperty("nf-type")
    public String getNfType()
    {
        return nfType;
    }

    /**
     * The type of the NF (according to TS 29.510)
     * 
     */
    @JsonProperty("nf-type")
    public void setNfType(String nfType)
    {
        this.nfType = nfType;
    }

    public DiscoveredScpInstance withNfType(String nfType)
    {
        this.nfType = nfType;
        return this;
    }

    /**
     * The geographic locality of the NF
     * 
     */
    @JsonProperty("locality")
    public String getLocality()
    {
        return locality;
    }

    /**
     * The geographic locality of the NF
     * 
     */
    @JsonProperty("locality")
    public void setLocality(String locality)
    {
        this.locality = locality;
    }

    public DiscoveredScpInstance withLocality(String locality)
    {
        this.locality = locality;
        return this;
    }

    /**
     * The set identity of the NF
     * 
     */
    @JsonProperty("nf-set-id")
    public List<String> getNfSetId()
    {
        return nfSetId;
    }

    /**
     * The set identity of the NF
     * 
     */
    @JsonProperty("nf-set-id")
    public void setNfSetId(List<String> nfSetId)
    {
        this.nfSetId = nfSetId;
    }

    public DiscoveredScpInstance withNfSetId(List<String> nfSetId)
    {
        this.nfSetId = nfSetId;
        return this;
    }

    /**
     * The SCP domains this NF is associated with
     * 
     */
    @JsonProperty("scp-domain")
    public List<String> getScpDomain()
    {
        return scpDomain;
    }

    /**
     * The SCP domains this NF is associated with
     * 
     */
    @JsonProperty("scp-domain")
    public void setScpDomain(List<String> scpDomain)
    {
        this.scpDomain = scpDomain;
    }

    public DiscoveredScpInstance withScpDomain(List<String> scpDomain)
    {
        this.scpDomain = scpDomain;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(DiscoveredScpInstance.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("nfInstanceId");
        sb.append('=');
        sb.append(((this.nfInstanceId == null) ? "<null>" : this.nfInstanceId));
        sb.append(',');
        sb.append("nfStatus");
        sb.append('=');
        sb.append(((this.nfStatus == null) ? "<null>" : this.nfStatus));
        sb.append(',');
        sb.append("servedNfSetId");
        sb.append('=');
        sb.append(((this.servedNfSetId == null) ? "<null>" : this.servedNfSetId));
        sb.append(',');
        sb.append("discoveredScpDomainInfo");
        sb.append('=');
        sb.append(((this.discoveredScpDomainInfo == null) ? "<null>" : this.discoveredScpDomainInfo));
        sb.append(',');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("nfType");
        sb.append('=');
        sb.append(((this.nfType == null) ? "<null>" : this.nfType));
        sb.append(',');
        sb.append("locality");
        sb.append('=');
        sb.append(((this.locality == null) ? "<null>" : this.locality));
        sb.append(',');
        sb.append("nfSetId");
        sb.append('=');
        sb.append(((this.nfSetId == null) ? "<null>" : this.nfSetId));
        sb.append(',');
        sb.append("scpDomain");
        sb.append('=');
        sb.append(((this.scpDomain == null) ? "<null>" : this.scpDomain));
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

    @JsonIgnore
    @Override
    public int compareTo(DiscoveredScpInstance o)
    {
        return this.getNfInstanceId().compareTo(o.getNfInstanceId());
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.scpDomain == null) ? 0 : this.scpDomain.hashCode()));
        result = ((result * 31) + ((this.nfInstanceId == null) ? 0 : this.nfInstanceId.hashCode()));
        result = ((result * 31) + ((this.nfStatus == null) ? 0 : this.nfStatus.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.nfType == null) ? 0 : this.nfType.hashCode()));
        result = ((result * 31) + ((this.locality == null) ? 0 : this.locality.hashCode()));
        result = ((result * 31) + ((this.nfSetId == null) ? 0 : this.nfSetId.hashCode()));
        result = ((result * 31) + ((this.discoveredScpDomainInfo == null) ? 0 : this.discoveredScpDomainInfo.hashCode()));
        result = ((result * 31) + ((this.servedNfSetId == null) ? 0 : this.servedNfSetId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof DiscoveredScpInstance) == false)
        {
            return false;
        }
        DiscoveredScpInstance rhs = ((DiscoveredScpInstance) other);
        return ((((((((((this.scpDomain == rhs.scpDomain) || ((this.scpDomain != null) && this.scpDomain.equals(rhs.scpDomain)))
                       && ((this.nfInstanceId == rhs.nfInstanceId) || ((this.nfInstanceId != null) && this.nfInstanceId.equals(rhs.nfInstanceId))))
                      && ((this.nfStatus == rhs.nfStatus) || ((this.nfStatus != null) && this.nfStatus.equals(rhs.nfStatus))))
                     && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                    && ((this.nfType == rhs.nfType) || ((this.nfType != null) && this.nfType.equals(rhs.nfType))))
                   && ((this.locality == rhs.locality) || ((this.locality != null) && this.locality.equals(rhs.locality))))
                  && ((this.nfSetId == rhs.nfSetId) || ((this.nfSetId != null) && this.nfSetId.equals(rhs.nfSetId))))
                 && ((this.discoveredScpDomainInfo == rhs.discoveredScpDomainInfo)
                     || ((this.discoveredScpDomainInfo != null) && this.discoveredScpDomainInfo.equals(rhs.discoveredScpDomainInfo))))
                && ((this.servedNfSetId == rhs.servedNfSetId) || ((this.servedNfSetId != null) && this.servedNfSetId.equals(rhs.servedNfSetId))));
    }

}
