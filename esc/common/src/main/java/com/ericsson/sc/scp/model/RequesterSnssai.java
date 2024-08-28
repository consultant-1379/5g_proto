
package com.ericsson.sc.scp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "sst", "sd" })
public class RequesterSnssai
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Specifies the slice service type of the S-NSSAI (Required)
     * 
     */
    @JsonProperty("sst")
    @JsonPropertyDescription("Specifies the slice service type of the S-NSSAI")
    private Integer sst;
    /**
     * Specifies the slice differentiator of the S-NSSAI. Special character '-'
     * indicates an unspecified slice differentiator
     * 
     */
    @JsonProperty("sd")
    @JsonPropertyDescription("Specifies the slice differentiator of the S-NSSAI. Special character '-' indicates an unspecified slice differentiator")
    private String sd = "-";

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public RequesterSnssai withName(String name)
    {
        this.name = name;
        return this;
    }

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

    public RequesterSnssai withSst(Integer sst)
    {
        this.sst = sst;
        return this;
    }

    /**
     * Specifies the slice differentiator of the S-NSSAI. Special character '-'
     * indicates an unspecified slice differentiator
     * 
     */
    @JsonProperty("sd")
    public String getSd()
    {
        return sd;
    }

    /**
     * Specifies the slice differentiator of the S-NSSAI. Special character '-'
     * indicates an unspecified slice differentiator
     * 
     */
    @JsonProperty("sd")
    public void setSd(String sd)
    {
        this.sd = sd;
    }

    public RequesterSnssai withSd(String sd)
    {
        this.sd = sd;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RequesterSnssai.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
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
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.sd == null) ? 0 : this.sd.hashCode()));
        result = ((result * 31) + ((this.sst == null) ? 0 : this.sst.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RequesterSnssai) == false)
        {
            return false;
        }
        RequesterSnssai rhs = ((RequesterSnssai) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.sd == rhs.sd) || ((this.sd != null) && this.sd.equals(rhs.sd))))
                && ((this.sst == rhs.sst) || ((this.sst != null) && this.sst.equals(rhs.sst))));
    }

}
