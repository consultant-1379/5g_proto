
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfNrfGroup;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "nf-instance-id", "nf-profile-ref", "dns-profile-ref", "nrf" })
public class NrfGroup implements IfNrfGroup
{

    /**
     * Name identifying the NRF registration group (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the NRF registration group")
    private String name;
    /**
     * ID identifying the NF Profile of this SEPP instance, given per NRF Group
     * 
     */
    @JsonProperty("nf-instance-id")
    @JsonPropertyDescription("ID identifying the NF Profile of this SEPP instance, given per NRF Group")
    private String nfInstanceId;
    /**
     * The NF profile with which to register to the NRF for the Nnrf_NFManagement
     * service. If specified, the attributes configured in the NF profile on NRF
     * level will override the corresponding attributes of the NF profile on NRF
     * group level
     * 
     */
    @JsonProperty("nf-profile-ref")
    @JsonPropertyDescription("The NF profile with which to register to the NRF for the Nnrf_NFManagement service. If specified, the attributes configured in the NF profile on NRF level will override the corresponding attributes of the NF profile on NRF group level")
    private String nfProfileRef;
    /**
     * Reference to a defined dns-profile that will be used for DNS resolution of
     * the FQDN of the NRFs in the NRF group
     * 
     */
    @JsonProperty("dns-profile-ref")
    @JsonPropertyDescription("Reference to a defined dns-profile that will be used for DNS resolution of the FQDN of the NRFs in the NRF group")
    private String dnsProfileRef;
    /**
     * The configuration for a single NRF within this NRF group (Required)
     * 
     */
    @JsonProperty("nrf")
    @JsonPropertyDescription("The configuration for a single NRF within this NRF group")
    private List<Nrf> nrf = new ArrayList<Nrf>();

    /**
     * Name identifying the NRF registration group (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the NRF registration group (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public NrfGroup withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * ID identifying the NF Profile of this SEPP instance, given per NRF Group
     * 
     */
    @JsonProperty("nf-instance-id")
    public String getNfInstanceId()
    {
        return nfInstanceId;
    }

    /**
     * ID identifying the NF Profile of this SEPP instance, given per NRF Group
     * 
     */
    @JsonProperty("nf-instance-id")
    public void setNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
    }

    public NrfGroup withNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
        return this;
    }

    /**
     * The NF profile with which to register to the NRF for the Nnrf_NFManagement
     * service. If specified, the attributes configured in the NF profile on NRF
     * level will override the corresponding attributes of the NF profile on NRF
     * group level
     * 
     */
    @JsonProperty("nf-profile-ref")
    public String getNfProfileRef()
    {
        return nfProfileRef;
    }

    /**
     * The NF profile with which to register to the NRF for the Nnrf_NFManagement
     * service. If specified, the attributes configured in the NF profile on NRF
     * level will override the corresponding attributes of the NF profile on NRF
     * group level
     * 
     */
    @JsonProperty("nf-profile-ref")
    public void setNfProfileRef(String nfProfileRef)
    {
        this.nfProfileRef = nfProfileRef;
    }

    public NrfGroup withNfProfileRef(String nfProfileRef)
    {
        this.nfProfileRef = nfProfileRef;
        return this;
    }

    /**
     * Reference to a defined dns-profile that will be used for DNS resolution of
     * the FQDN of the NRFs in the NRF group
     * 
     */
    @JsonProperty("dns-profile-ref")
    public String getDnsProfileRef()
    {
        return dnsProfileRef;
    }

    /**
     * Reference to a defined dns-profile that will be used for DNS resolution of
     * the FQDN of the NRFs in the NRF group
     * 
     */
    @JsonProperty("dns-profile-ref")
    public void setDnsProfileRef(String dnsProfileRef)
    {
        this.dnsProfileRef = dnsProfileRef;
    }

    public NrfGroup withDnsProfileRef(String dnsProfileRef)
    {
        this.dnsProfileRef = dnsProfileRef;
        return this;
    }

    /**
     * The configuration for a single NRF within this NRF group (Required)
     * 
     */
    @JsonProperty("nrf")
    public List<Nrf> getNrf()
    {
        return nrf;
    }

    /**
     * The configuration for a single NRF within this NRF group (Required)
     * 
     */
    @JsonProperty("nrf")
    public void setNrf(List<Nrf> nrf)
    {
        this.nrf = nrf;
    }

    public NrfGroup withNrf(List<Nrf> nrf)
    {
        this.nrf = nrf;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NrfGroup.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("nfInstanceId");
        sb.append('=');
        sb.append(((this.nfInstanceId == null) ? "<null>" : this.nfInstanceId));
        sb.append(',');
        sb.append("nfProfileRef");
        sb.append('=');
        sb.append(((this.nfProfileRef == null) ? "<null>" : this.nfProfileRef));
        sb.append(',');
        sb.append("dnsProfileRef");
        sb.append('=');
        sb.append(((this.dnsProfileRef == null) ? "<null>" : this.dnsProfileRef));
        sb.append(',');
        sb.append("nrf");
        sb.append('=');
        sb.append(((this.nrf == null) ? "<null>" : this.nrf));
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
        result = ((result * 31) + ((this.nfInstanceId == null) ? 0 : this.nfInstanceId.hashCode()));
        result = ((result * 31) + ((this.dnsProfileRef == null) ? 0 : this.dnsProfileRef.hashCode()));
        result = ((result * 31) + ((this.nrf == null) ? 0 : this.nrf.hashCode()));
        result = ((result * 31) + ((this.nfProfileRef == null) ? 0 : this.nfProfileRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NrfGroup) == false)
        {
            return false;
        }
        NrfGroup rhs = ((NrfGroup) other);
        return ((((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                   && ((this.nfInstanceId == rhs.nfInstanceId) || ((this.nfInstanceId != null) && this.nfInstanceId.equals(rhs.nfInstanceId))))
                  && ((this.dnsProfileRef == rhs.dnsProfileRef) || ((this.dnsProfileRef != null) && this.dnsProfileRef.equals(rhs.dnsProfileRef))))
                 && ((this.nrf == rhs.nrf) || ((this.nrf != null) && this.nrf.equals(rhs.nrf))))
                && ((this.nfProfileRef == rhs.nfProfileRef) || ((this.nfProfileRef != null) && this.nfProfileRef.equals(rhs.nfProfileRef))));
    }

}
