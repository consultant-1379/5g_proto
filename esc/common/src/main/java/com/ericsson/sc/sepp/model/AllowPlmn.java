
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Definition of the primary and additional PLMN Ids of the roaming partner's
 * PLMN.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "primary-id-mcc", "primary-id-mnc", "additional-id" })
public class AllowPlmn
{

    /**
     * Specifies the mobile country code of the primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-id-mcc")
    @JsonPropertyDescription("Specifies the mobile country code of the primary PLMN Id.")
    private String primaryIdMcc;
    /**
     * Specifies the mobile network code of the primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-id-mnc")
    @JsonPropertyDescription("Specifies the mobile network code of the primary PLMN Id.")
    private String primaryIdMnc;
    /**
     * Additional PLMN id(s) of the roaming partner's PLMN.
     * 
     */
    @JsonProperty("additional-id")
    @JsonPropertyDescription("Additional PLMN id(s) of the roaming partner's PLMN.")
    private List<AdditionalId> additionalId = new ArrayList<AdditionalId>();

    /**
     * Specifies the mobile country code of the primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-id-mcc")
    public String getPrimaryIdMcc()
    {
        return primaryIdMcc;
    }

    /**
     * Specifies the mobile country code of the primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-id-mcc")
    public void setPrimaryIdMcc(String primaryIdMcc)
    {
        this.primaryIdMcc = primaryIdMcc;
    }

    public AllowPlmn withPrimaryIdMcc(String primaryIdMcc)
    {
        this.primaryIdMcc = primaryIdMcc;
        return this;
    }

    /**
     * Specifies the mobile network code of the primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-id-mnc")
    public String getPrimaryIdMnc()
    {
        return primaryIdMnc;
    }

    /**
     * Specifies the mobile network code of the primary PLMN Id. (Required)
     * 
     */
    @JsonProperty("primary-id-mnc")
    public void setPrimaryIdMnc(String primaryIdMnc)
    {
        this.primaryIdMnc = primaryIdMnc;
    }

    public AllowPlmn withPrimaryIdMnc(String primaryIdMnc)
    {
        this.primaryIdMnc = primaryIdMnc;
        return this;
    }

    /**
     * Additional PLMN id(s) of the roaming partner's PLMN.
     * 
     */
    @JsonProperty("additional-id")
    public List<AdditionalId> getAdditionalId()
    {
        return additionalId;
    }

    /**
     * Additional PLMN id(s) of the roaming partner's PLMN.
     * 
     */
    @JsonProperty("additional-id")
    public void setAdditionalId(List<AdditionalId> additionalId)
    {
        this.additionalId = additionalId;
    }

    public AllowPlmn withAdditionalId(List<AdditionalId> additionalId)
    {
        this.additionalId = additionalId;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(AllowPlmn.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("primaryIdMcc");
        sb.append('=');
        sb.append(((this.primaryIdMcc == null) ? "<null>" : this.primaryIdMcc));
        sb.append(',');
        sb.append("primaryIdMnc");
        sb.append('=');
        sb.append(((this.primaryIdMnc == null) ? "<null>" : this.primaryIdMnc));
        sb.append(',');
        sb.append("additionalId");
        sb.append('=');
        sb.append(((this.additionalId == null) ? "<null>" : this.additionalId));
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
        result = ((result * 31) + ((this.primaryIdMcc == null) ? 0 : this.primaryIdMcc.hashCode()));
        result = ((result * 31) + ((this.additionalId == null) ? 0 : this.additionalId.hashCode()));
        result = ((result * 31) + ((this.primaryIdMnc == null) ? 0 : this.primaryIdMnc.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof AllowPlmn) == false)
        {
            return false;
        }
        AllowPlmn rhs = ((AllowPlmn) other);
        return ((((this.primaryIdMcc == rhs.primaryIdMcc) || ((this.primaryIdMcc != null) && this.primaryIdMcc.equals(rhs.primaryIdMcc)))
                 && ((this.additionalId == rhs.additionalId) || ((this.additionalId != null) && this.additionalId.equals(rhs.additionalId))))
                && ((this.primaryIdMnc == rhs.primaryIdMnc) || ((this.primaryIdMnc != null) && this.primaryIdMnc.equals(rhs.primaryIdMnc))));
    }

}
