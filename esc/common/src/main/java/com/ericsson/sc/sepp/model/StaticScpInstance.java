
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.sc.glue.IfStaticScpInstance;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "nf-instance-id",
                     "static-nf-service",
                     "nf-type",
                     "locality",
                     "nf-set-id",
                     "scp-domain",
                     "served-nf-set-id",
                     "static-scp-domain-info" })
public class StaticScpInstance implements IfStaticScpInstance
{

    /**
     * The NF instance identity
     * 
     */
    @JsonProperty("nf-instance-id")
    @JsonPropertyDescription("The NF instance identity")
    private String nfInstanceId;
    /**
     * The set identity of the NFs served by the SCP
     * 
     */
    @JsonProperty("served-nf-set-id")
    @JsonPropertyDescription("The set identity of the NFs served by the SCP")
    private List<String> servedNfSetId = new ArrayList<String>();
    /**
     * The service for which the SCP has registered
     * 
     */
    @JsonProperty("static-nf-service")
    @JsonPropertyDescription("The service for which the SCP has registered")
    private List<StaticNfService> staticNfService = new ArrayList<StaticNfService>();
    /**
     * The info about the static scp domain
     * 
     */
    @JsonProperty("static-scp-domain-info")
    @JsonPropertyDescription("The info about the static scp domain")
    private List<StaticScpDomainInfo> staticScpDomainInfo = new ArrayList<StaticScpDomainInfo>();
    /**
     * Name identifying the profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the profile")
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

    public StaticScpInstance withNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
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

    public StaticScpInstance withServedNfSetId(List<String> servedNfSetId)
    {
        this.servedNfSetId = servedNfSetId;
        return this;
    }

    /**
     * The service for which the SCP has registered
     * 
     */
    @JsonProperty("static-nf-service")
    public List<StaticNfService> getStaticNfService()
    {
        return staticNfService;
    }

    /**
     * The service for which the SCP has registered
     * 
     */
    @JsonProperty("static-nf-service")
    public void setStaticNfService(List<StaticNfService> staticNfService)
    {
        this.staticNfService = staticNfService;
    }

    public StaticScpInstance withStaticNfService(List<StaticNfService> staticNfService)
    {
        this.staticNfService = staticNfService;
        return this;
    }

    /**
     * The info about the static scp domain
     * 
     */
    @JsonProperty("static-scp-domain-info")
    public List<StaticScpDomainInfo> getStaticScpDomainInfo()
    {
        return staticScpDomainInfo;
    }

    /**
     * The info about the static scp domain
     * 
     */
    @JsonProperty("static-scp-domain-info")
    public void setStaticScpDomainInfo(List<StaticScpDomainInfo> staticScpDomainInfo)
    {
        this.staticScpDomainInfo = staticScpDomainInfo;
    }

    public StaticScpInstance withStaticScpDomainInfo(List<StaticScpDomainInfo> staticScpDomainInfo)
    {
        this.staticScpDomainInfo = staticScpDomainInfo;
        return this;
    }

    /**
     * Name identifying the profile (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the profile (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public StaticScpInstance withName(String name)
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

    public StaticScpInstance withNfType(String nfType)
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

    public StaticScpInstance withLocality(String locality)
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

    public StaticScpInstance withNfSetId(List<String> nfSetId)
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

    public StaticScpInstance withScpDomain(List<String> scpDomain)
    {
        this.scpDomain = scpDomain;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(StaticScpInstance.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("nfInstanceId");
        sb.append('=');
        sb.append(((this.nfInstanceId == null) ? "<null>" : this.nfInstanceId));
        sb.append(',');
        sb.append("servedNfSetId");
        sb.append('=');
        sb.append(((this.servedNfSetId == null) ? "<null>" : this.servedNfSetId));
        sb.append(',');
        sb.append("staticNfService");
        sb.append('=');
        sb.append(((this.staticNfService == null) ? "<null>" : this.staticNfService));
        sb.append(',');
        sb.append("staticScpDomainInfo");
        sb.append('=');
        sb.append(((this.staticScpDomainInfo == null) ? "<null>" : this.staticScpDomainInfo));
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

    @Override
    public int hashCode()
    {
        int result = 1;
        result = ((result * 31) + ((this.scpDomain == null) ? 0 : this.scpDomain.hashCode()));
        result = ((result * 31) + ((this.nfInstanceId == null) ? 0 : this.nfInstanceId.hashCode()));
        result = ((result * 31) + ((this.staticScpDomainInfo == null) ? 0 : this.staticScpDomainInfo.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.nfType == null) ? 0 : this.nfType.hashCode()));
        result = ((result * 31) + ((this.locality == null) ? 0 : this.locality.hashCode()));
        result = ((result * 31) + ((this.nfSetId == null) ? 0 : this.nfSetId.hashCode()));
        result = ((result * 31) + ((this.staticNfService == null) ? 0 : this.staticNfService.hashCode()));
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
        if ((other instanceof StaticScpInstance) == false)
        {
            return false;
        }
        StaticScpInstance rhs = ((StaticScpInstance) other);
        return ((((((((((this.scpDomain == rhs.scpDomain) || ((this.scpDomain != null) && this.scpDomain.equals(rhs.scpDomain)))
                       && ((this.nfInstanceId == rhs.nfInstanceId) || ((this.nfInstanceId != null) && this.nfInstanceId.equals(rhs.nfInstanceId))))
                      && ((this.staticScpDomainInfo == rhs.staticScpDomainInfo)
                          || ((this.staticScpDomainInfo != null) && this.staticScpDomainInfo.equals(rhs.staticScpDomainInfo))))
                     && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                    && ((this.nfType == rhs.nfType) || ((this.nfType != null) && this.nfType.equals(rhs.nfType))))
                   && ((this.locality == rhs.locality) || ((this.locality != null) && this.locality.equals(rhs.locality))))
                  && ((this.nfSetId == rhs.nfSetId) || ((this.nfSetId != null) && this.nfSetId.equals(rhs.nfSetId))))
                 && ((this.staticNfService == rhs.staticNfService) || ((this.staticNfService != null) && this.staticNfService.equals(rhs.staticNfService))))
                && ((this.servedNfSetId == rhs.servedNfSetId) || ((this.servedNfSetId != null) && this.servedNfSetId.equals(rhs.servedNfSetId))));
    }

}
