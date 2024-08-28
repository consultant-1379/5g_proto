
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "primary-plmn-id-mcc", "primary-plmn-id-mnc", "additional-plmn-id", "security-capability", "supports-target-apiroot" })
public class OwnSecurityDatum
{

    /**
     * Specifies the mobile country code of own primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-plmn-id-mcc")
    @JsonPropertyDescription("Specifies the mobile country code of own primary PLMN Id.")
    private String primaryPlmnIdMcc;
    /**
     * Specifies the mobile network code of own primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-plmn-id-mnc")
    @JsonPropertyDescription("Specifies the mobile network code of own primary PLMN Id.")
    private String primaryPlmnIdMnc;
    /**
     * Additional PLMN Id(s).
     * 
     */
    @JsonProperty("additional-plmn-id")
    @JsonPropertyDescription("Additional PLMN Id(s)")
    private List<AdditionalPlmnId> additionalPlmnId = new ArrayList<AdditionalPlmnId>();
    /**
     * Supported security capabilities of requesting SEPP i.e. PRINS and/or TLS.
     * (Required)
     * 
     */
    @JsonProperty("security-capability")
    @JsonPropertyDescription("Supported security capabilities of requesting SEPP i.e. PRINS and/or TLS.")
    private List<SecurityCapability> securityCapability = new ArrayList<SecurityCapability>();
    /**
     * When true, TLS security using the 3gpp-Sbi-Target-apiRoot HTTP header is
     * supported for N32f message forwarding.
     * 
     */
    @JsonProperty("supports-target-apiroot")
    @JsonPropertyDescription("When true, TLS security using the 3gpp-Sbi-Target-apiRoot HTTP header is supported for N32f message forwarding.")
    private Boolean supportsTargetApiroot = true;

    /**
     * Specifies the mobile country code of own primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-plmn-id-mcc")
    public String getPrimaryPlmnIdMcc()
    {
        return primaryPlmnIdMcc;
    }

    /**
     * Specifies the mobile country code of own primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-plmn-id-mcc")
    public void setPrimaryPlmnIdMcc(String primaryPlmnIdMcc)
    {
        this.primaryPlmnIdMcc = primaryPlmnIdMcc;
    }

    public OwnSecurityDatum withPrimaryPlmnIdMcc(String primaryPlmnIdMcc)
    {
        this.primaryPlmnIdMcc = primaryPlmnIdMcc;
        return this;
    }

    /**
     * Specifies the mobile network code of own primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-plmn-id-mnc")
    public String getPrimaryPlmnIdMnc()
    {
        return primaryPlmnIdMnc;
    }

    /**
     * Specifies the mobile network code of own primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-plmn-id-mnc")
    public void setPrimaryPlmnIdMnc(String primaryPlmnIdMnc)
    {
        this.primaryPlmnIdMnc = primaryPlmnIdMnc;
    }

    public OwnSecurityDatum withPrimaryPlmnIdMnc(String primaryPlmnIdMnc)
    {
        this.primaryPlmnIdMnc = primaryPlmnIdMnc;
        return this;
    }

    /**
     * Additional PLMN Id(s).
     * 
     */
    @JsonProperty("additional-plmn-id")
    public List<AdditionalPlmnId> getAdditionalPlmnId()
    {
        return additionalPlmnId;
    }

    /**
     * Additional PLMN Id(s).
     * 
     */
    @JsonProperty("additional-plmn-id")
    public void setAdditionalPlmnId(List<AdditionalPlmnId> additionalPlmnId)
    {
        this.additionalPlmnId = additionalPlmnId;
    }

    public OwnSecurityDatum withAdditionalPlmnId(List<AdditionalPlmnId> additionalPlmnId)
    {
        this.additionalPlmnId = additionalPlmnId;
        return this;
    }

    /**
     * Supported security capabilities of requesting SEPP i.e. PRINS and/or TLS.
     * (Required)
     * 
     */
    @JsonProperty("security-capability")
    public List<SecurityCapability> getSecurityCapability()
    {
        return securityCapability;
    }

    /**
     * Supported security capabilities of requesting SEPP i.e. PRINS and/or TLS.
     * (Required)
     * 
     */
    @JsonProperty("security-capability")
    public void setSecurityCapability(List<SecurityCapability> securityCapability)
    {
        this.securityCapability = securityCapability;
    }

    public OwnSecurityDatum withSecurityCapability(List<SecurityCapability> securityCapability)
    {
        this.securityCapability = securityCapability;
        return this;
    }

    /**
     * When true, TLS security using the 3gpp-Sbi-Target-apiRoot HTTP header is
     * supported for N32f message forwarding.
     * 
     */
    @JsonProperty("supports-target-apiroot")
    public Boolean getSupportsTargetApiroot()
    {
        return supportsTargetApiroot;
    }

    /**
     * When true, TLS security using the 3gpp-Sbi-Target-apiRoot HTTP header is
     * supported for N32f message forwarding.
     * 
     */
    @JsonProperty("supports-target-apiroot")
    public void setSupportsTargetApiroot(Boolean supportsTargetApiroot)
    {
        this.supportsTargetApiroot = supportsTargetApiroot;
    }

    public OwnSecurityDatum withSupportsTargetApiroot(Boolean supportsTargetApiroot)
    {
        this.supportsTargetApiroot = supportsTargetApiroot;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(OwnSecurityDatum.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("primaryPlmnIdMcc");
        sb.append('=');
        sb.append(((this.primaryPlmnIdMcc == null) ? "<null>" : this.primaryPlmnIdMcc));
        sb.append(',');
        sb.append("primaryPlmnIdMnc");
        sb.append('=');
        sb.append(((this.primaryPlmnIdMnc == null) ? "<null>" : this.primaryPlmnIdMnc));
        sb.append(',');
        sb.append("additionalPlmnId");
        sb.append('=');
        sb.append(((this.additionalPlmnId == null) ? "<null>" : this.additionalPlmnId));
        sb.append(',');
        sb.append("securityCapability");
        sb.append('=');
        sb.append(((this.securityCapability == null) ? "<null>" : this.securityCapability));
        sb.append(',');
        sb.append("supportsTargetApiroot");
        sb.append('=');
        sb.append(((this.supportsTargetApiroot == null) ? "<null>" : this.supportsTargetApiroot));
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
        result = ((result * 31) + ((this.supportsTargetApiroot == null) ? 0 : this.supportsTargetApiroot.hashCode()));
        result = ((result * 31) + ((this.securityCapability == null) ? 0 : this.securityCapability.hashCode()));
        result = ((result * 31) + ((this.primaryPlmnIdMcc == null) ? 0 : this.primaryPlmnIdMcc.hashCode()));
        result = ((result * 31) + ((this.additionalPlmnId == null) ? 0 : this.additionalPlmnId.hashCode()));
        result = ((result * 31) + ((this.primaryPlmnIdMnc == null) ? 0 : this.primaryPlmnIdMnc.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof OwnSecurityDatum) == false)
        {
            return false;
        }
        OwnSecurityDatum rhs = ((OwnSecurityDatum) other);
        return ((((((this.supportsTargetApiroot == rhs.supportsTargetApiroot)
                    || ((this.supportsTargetApiroot != null) && this.supportsTargetApiroot.equals(rhs.supportsTargetApiroot)))
                   && ((this.securityCapability == rhs.securityCapability)
                       || ((this.securityCapability != null) && this.securityCapability.equals(rhs.securityCapability))))
                  && ((this.primaryPlmnIdMcc == rhs.primaryPlmnIdMcc)
                      || ((this.primaryPlmnIdMcc != null) && this.primaryPlmnIdMcc.equals(rhs.primaryPlmnIdMcc))))
                 && ((this.additionalPlmnId == rhs.additionalPlmnId)
                     || ((this.additionalPlmnId != null) && this.additionalPlmnId.equals(rhs.additionalPlmnId))))
                && ((this.primaryPlmnIdMnc == rhs.primaryPlmnIdMnc)
                    || ((this.primaryPlmnIdMnc != null) && this.primaryPlmnIdMnc.equals(rhs.primaryPlmnIdMnc))));
    }

}
