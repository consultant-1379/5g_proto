
package com.ericsson.esc.services.cm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Specific data for the NF instance type
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "chf-info", "bsf-info" })
public class NfSpecificInfo
{

    /**
     * Specific data for the CHF NF
     * 
     */
    @JsonProperty("chf-info")
    @JsonPropertyDescription("Specific data for the CHF NF")
    private ChfInfo chfInfo;
    /**
     * Specific data for the BSF NF
     * 
     */
    @JsonProperty("bsf-info")
    @JsonPropertyDescription("Specific data for the BSF NF")
    private BsfInfo bsfInfo;

    /**
     * Specific data for the CHF NF
     * 
     */
    @JsonProperty("chf-info")
    public ChfInfo getChfInfo()
    {
        return chfInfo;
    }

    /**
     * Specific data for the CHF NF
     * 
     */
    @JsonProperty("chf-info")
    public void setChfInfo(ChfInfo chfInfo)
    {
        this.chfInfo = chfInfo;
    }

    public NfSpecificInfo withChfInfo(ChfInfo chfInfo)
    {
        this.chfInfo = chfInfo;
        return this;
    }

    /**
     * Specific data for the BSF NF
     * 
     */
    @JsonProperty("bsf-info")
    public BsfInfo getBsfInfo()
    {
        return bsfInfo;
    }

    /**
     * Specific data for the BSF NF
     * 
     */
    @JsonProperty("bsf-info")
    public void setBsfInfo(BsfInfo bsfInfo)
    {
        this.bsfInfo = bsfInfo;
    }

    public NfSpecificInfo withBsfInfo(BsfInfo bsfInfo)
    {
        this.bsfInfo = bsfInfo;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfSpecificInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("chfInfo");
        sb.append('=');
        sb.append(((this.chfInfo == null) ? "<null>" : this.chfInfo));
        sb.append(',');
        sb.append("bsfInfo");
        sb.append('=');
        sb.append(((this.bsfInfo == null) ? "<null>" : this.bsfInfo));
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
        result = ((result * 31) + ((this.chfInfo == null) ? 0 : this.chfInfo.hashCode()));
        result = ((result * 31) + ((this.bsfInfo == null) ? 0 : this.bsfInfo.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfSpecificInfo) == false)
        {
            return false;
        }
        NfSpecificInfo rhs = ((NfSpecificInfo) other);
        return (((this.chfInfo == rhs.chfInfo) || ((this.chfInfo != null) && this.chfInfo.equals(rhs.chfInfo)))
                && ((this.bsfInfo == rhs.bsfInfo) || ((this.bsfInfo != null) && this.bsfInfo.equals(rhs.bsfInfo))));
    }

}
