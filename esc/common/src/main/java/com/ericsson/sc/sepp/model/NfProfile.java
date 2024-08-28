
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.nfm.model.NfStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Profile consisting of general parameters of the network function, included in
 * the NRF discovery response
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "nf-instance-id", "fqdn", "nf-type", "nf-status" })
public class NfProfile
{

    /**
     * The NF instance identity
     * 
     */
    @JsonProperty("nf-instance-id")
    @JsonPropertyDescription("The NF instance identity")
    private String nfInstanceId;
    /**
     * The service address that is used to hide the actual service address.
     * According to RFC 1035 only letters, digits and hyphen (-) are allowed.
     * (Required)
     * 
     */
    @JsonProperty("fqdn")
    @JsonPropertyDescription("The service address that is used to hide the actual service address. According to RFC 1035 only letters, digits and hyphen (-) are allowed.")
    private String fqdn;
    /**
     * The NF type of the hidden NF (Required)
     * 
     */
    @JsonProperty("nf-type")
    @JsonPropertyDescription("The NF type of the hidden NF")
    private String nfType;
    /**
     * The status of the NF (Required)
     * 
     */
    @JsonProperty("nf-status")
    @JsonPropertyDescription("The status of the NF")
    private NfStatus nfStatus;

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

    public NfProfile withNfInstanceId(String nfInstanceId)
    {
        this.nfInstanceId = nfInstanceId;
        return this;
    }

    /**
     * The service address that is used to hide the actual service address.
     * According to RFC 1035 only letters, digits and hyphen (-) are allowed.
     * (Required)
     * 
     */
    @JsonProperty("fqdn")
    public String getFqdn()
    {
        return fqdn;
    }

    /**
     * The service address that is used to hide the actual service address.
     * According to RFC 1035 only letters, digits and hyphen (-) are allowed.
     * (Required)
     * 
     */
    @JsonProperty("fqdn")
    public void setFqdn(String fqdn)
    {
        this.fqdn = fqdn;
    }

    public NfProfile withFqdn(String fqdn)
    {
        this.fqdn = fqdn;
        return this;
    }

    /**
     * The NF type of the hidden NF (Required)
     * 
     */
    @JsonProperty("nf-type")
    public String getNfType()
    {
        return nfType;
    }

    /**
     * The NF type of the hidden NF (Required)
     * 
     */
    @JsonProperty("nf-type")
    public void setNfType(String nfType)
    {
        this.nfType = nfType;
    }

    public NfProfile withNfType(String nfType)
    {
        this.nfType = nfType;
        return this;
    }

    /**
     * The status of the NF (Required)
     * 
     */
    @JsonProperty("nf-status")
    public NfStatus getNfStatus()
    {
        return nfStatus;
    }

    /**
     * The status of the NF (Required)
     * 
     */
    @JsonProperty("nf-status")
    public void setNfStatus(NfStatus nfStatus)
    {
        this.nfStatus = nfStatus;
    }

    public NfProfile withNfStatus(NfStatus nfStatus)
    {
        this.nfStatus = nfStatus;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("nfInstanceId");
        sb.append('=');
        sb.append(((this.nfInstanceId == null) ? "<null>" : this.nfInstanceId));
        sb.append(',');
        sb.append("fqdn");
        sb.append('=');
        sb.append(((this.fqdn == null) ? "<null>" : this.fqdn));
        sb.append(',');
        sb.append("nfType");
        sb.append('=');
        sb.append(((this.nfType == null) ? "<null>" : this.nfType));
        sb.append(',');
        sb.append("nfStatus");
        sb.append('=');
        sb.append(((this.nfStatus == null) ? "<null>" : this.nfStatus));
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
        result = ((result * 31) + ((this.nfType == null) ? 0 : this.nfType.hashCode()));
        result = ((result * 31) + ((this.nfInstanceId == null) ? 0 : this.nfInstanceId.hashCode()));
        result = ((result * 31) + ((this.fqdn == null) ? 0 : this.fqdn.hashCode()));
        result = ((result * 31) + ((this.nfStatus == null) ? 0 : this.nfStatus.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfProfile) == false)
        {
            return false;
        }
        NfProfile rhs = ((NfProfile) other);
        return (((((this.nfType == rhs.nfType) || ((this.nfType != null) && this.nfType.equals(rhs.nfType)))
                  && ((this.nfInstanceId == rhs.nfInstanceId) || ((this.nfInstanceId != null) && this.nfInstanceId.equals(rhs.nfInstanceId))))
                 && ((this.fqdn == rhs.fqdn) || ((this.fqdn != null) && this.fqdn.equals(rhs.fqdn))))
                && ((this.nfStatus == rhs.nfStatus) || ((this.nfStatus != null) && this.nfStatus.equals(rhs.nfStatus))));
    }

}
