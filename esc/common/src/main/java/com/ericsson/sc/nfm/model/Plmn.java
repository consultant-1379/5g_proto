
package com.ericsson.sc.nfm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "mcc", "mnc" })
public class Plmn
{

    /**
     * Specifies the mobile country code of the PLMN (Required)
     * 
     */
    @JsonProperty("mcc")
    @JsonPropertyDescription("Specifies the mobile country code of the PLMN")
    private String mcc;
    /**
     * Specifies the mobile network code of the PLMN (Required)
     * 
     */
    @JsonProperty("mnc")
    @JsonPropertyDescription("Specifies the mobile network code of the PLMN")
    private String mnc;

    /**
     * Specifies the mobile country code of the PLMN (Required)
     * 
     */
    @JsonProperty("mcc")
    public String getMcc()
    {
        return mcc;
    }

    /**
     * Specifies the mobile country code of the PLMN (Required)
     * 
     */
    @JsonProperty("mcc")
    public void setMcc(String mcc)
    {
        this.mcc = mcc;
    }

    public Plmn withMcc(String mcc)
    {
        this.mcc = mcc;
        return this;
    }

    /**
     * Specifies the mobile network code of the PLMN (Required)
     * 
     */
    @JsonProperty("mnc")
    public String getMnc()
    {
        return mnc;
    }

    /**
     * Specifies the mobile network code of the PLMN (Required)
     * 
     */
    @JsonProperty("mnc")
    public void setMnc(String mnc)
    {
        this.mnc = mnc;
    }

    public Plmn withMnc(String mnc)
    {
        this.mnc = mnc;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Plmn.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("mcc");
        sb.append('=');
        sb.append(((this.mcc == null) ? "<null>" : this.mcc));
        sb.append(',');
        sb.append("mnc");
        sb.append('=');
        sb.append(((this.mnc == null) ? "<null>" : this.mnc));
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
        result = ((result * 31) + ((this.mcc == null) ? 0 : this.mcc.hashCode()));
        result = ((result * 31) + ((this.mnc == null) ? 0 : this.mnc.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Plmn) == false)
        {
            return false;
        }
        Plmn rhs = ((Plmn) other);
        return (((this.mcc == rhs.mcc) || ((this.mcc != null) && this.mcc.equals(rhs.mcc)))
                && ((this.mnc == rhs.mnc) || ((this.mnc != null) && this.mnc.equals(rhs.mnc))));
    }

}
