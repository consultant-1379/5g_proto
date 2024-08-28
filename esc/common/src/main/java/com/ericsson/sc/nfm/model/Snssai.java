
package com.ericsson.sc.nfm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "sst", "sd" })
public class Snssai
{

    /**
     * Specifies the slice service type of the S-NSSAI (Required)
     * 
     */
    @JsonProperty("sst")
    @JsonPropertyDescription("Specifies the slice service type of the S-NSSAI")
    private Integer sst;
    /**
     * Specifies the optional slice differentiator of the S-NSSAI
     * 
     */
    @JsonProperty("sd")
    @JsonPropertyDescription("Specifies the optional slice differentiator of the S-NSSAI")
    private String sd;

    /**
     * Specifies the slice service type of the S-NSSAI (Required)
     * 
     */
    @JsonProperty("sst")
    public Integer getSst()
    {
        return sst;
    }

    /**
     * Specifies the slice service type of the S-NSSAI (Required)
     * 
     */
    @JsonProperty("sst")
    public void setSst(Integer sst)
    {
        this.sst = sst;
    }

    public Snssai withSst(Integer sst)
    {
        this.sst = sst;
        return this;
    }

    /**
     * Specifies the optional slice differentiator of the S-NSSAI
     * 
     */
    @JsonProperty("sd")
    public String getSd()
    {
        return sd;
    }

    /**
     * Specifies the optional slice differentiator of the S-NSSAI
     * 
     */
    @JsonProperty("sd")
    public void setSd(String sd)
    {
        this.sd = sd;
    }

    public Snssai withSd(String sd)
    {
        this.sd = sd;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Snssai.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("sst");
        sb.append('=');
        sb.append(((this.sst == null) ? "<null>" : this.sst));
        sb.append(',');
        sb.append("sd");
        sb.append('=');
        sb.append(((this.sd == null) ? "<null>" : this.sd));
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
        result = ((result * 31) + ((this.sst == null) ? 0 : this.sst.hashCode()));
        result = ((result * 31) + ((this.sd == null) ? 0 : this.sd.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Snssai) == false)
        {
            return false;
        }
        Snssai rhs = ((Snssai) other);
        return (((this.sst == rhs.sst) || ((this.sst != null) && this.sst.equals(rhs.sst)))
                && ((this.sd == rhs.sd) || ((this.sd != null) && this.sd.equals(rhs.sd))));
    }

}
