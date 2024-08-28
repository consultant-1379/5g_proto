
package com.ericsson.esc.services.cm.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Specific data for the CHS NF
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "supi-range", "gpsi-range", "plmn-range" })
public class ChfInfo
{

    /**
     * Specifies the list of ranges of SUPIs that can be served by the CHF instance.
     * If not specified, the CHF can serve any SUPI
     * 
     */
    @JsonProperty("supi-range")
    @JsonPropertyDescription("Specifies the list of ranges of SUPIs that can be served by the CHF instance. If not specified, the CHF can serve any SUPI")
    private List<SupiRange> supiRange = new ArrayList<SupiRange>();
    /**
     * Specifies the list of ranges of GPSIs that can be served by the CHF instance.
     * If not specified, the CHF can serve any GPSI
     * 
     */
    @JsonProperty("gpsi-range")
    @JsonPropertyDescription("Specifies the list of ranges of GPSIs that can be served by the CHF instance. If not specified, the CHF can serve any GPSI")
    private List<GpsiRange> gpsiRange = new ArrayList<GpsiRange>();
    /**
     * Specifies the list of ranges of PLMNs that can be served by the CHF instance.
     * If not specified, the CHF can serve any PLMN
     * 
     */
    @JsonProperty("plmn-range")
    @JsonPropertyDescription("Specifies the list of ranges of PLMNs that can be served by the CHF instance. If not specified, the CHF can serve any PLMN")
    private List<PlmnRange> plmnRange = new ArrayList<PlmnRange>();

    /**
     * Specifies the list of ranges of SUPIs that can be served by the CHF instance.
     * If not specified, the CHF can serve any SUPI
     * 
     */
    @JsonProperty("supi-range")
    public List<SupiRange> getSupiRange()
    {
        return supiRange;
    }

    /**
     * Specifies the list of ranges of SUPIs that can be served by the CHF instance.
     * If not specified, the CHF can serve any SUPI
     * 
     */
    @JsonProperty("supi-range")
    public void setSupiRange(List<SupiRange> supiRange)
    {
        this.supiRange = supiRange;
    }

    public ChfInfo withSupiRange(List<SupiRange> supiRange)
    {
        this.supiRange = supiRange;
        return this;
    }

    /**
     * Specifies the list of ranges of GPSIs that can be served by the CHF instance.
     * If not specified, the CHF can serve any GPSI
     * 
     */
    @JsonProperty("gpsi-range")
    public List<GpsiRange> getGpsiRange()
    {
        return gpsiRange;
    }

    /**
     * Specifies the list of ranges of GPSIs that can be served by the CHF instance.
     * If not specified, the CHF can serve any GPSI
     * 
     */
    @JsonProperty("gpsi-range")
    public void setGpsiRange(List<GpsiRange> gpsiRange)
    {
        this.gpsiRange = gpsiRange;
    }

    public ChfInfo withGpsiRange(List<GpsiRange> gpsiRange)
    {
        this.gpsiRange = gpsiRange;
        return this;
    }

    /**
     * Specifies the list of ranges of PLMNs that can be served by the CHF instance.
     * If not specified, the CHF can serve any PLMN
     * 
     */
    @JsonProperty("plmn-range")
    public List<PlmnRange> getPlmnRange()
    {
        return plmnRange;
    }

    /**
     * Specifies the list of ranges of PLMNs that can be served by the CHF instance.
     * If not specified, the CHF can serve any PLMN
     * 
     */
    @JsonProperty("plmn-range")
    public void setPlmnRange(List<PlmnRange> plmnRange)
    {
        this.plmnRange = plmnRange;
    }

    public ChfInfo withPlmnRange(List<PlmnRange> plmnRange)
    {
        this.plmnRange = plmnRange;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ChfInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("supiRange");
        sb.append('=');
        sb.append(((this.supiRange == null) ? "<null>" : this.supiRange));
        sb.append(',');
        sb.append("gpsiRange");
        sb.append('=');
        sb.append(((this.gpsiRange == null) ? "<null>" : this.gpsiRange));
        sb.append(',');
        sb.append("plmnRange");
        sb.append('=');
        sb.append(((this.plmnRange == null) ? "<null>" : this.plmnRange));
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
        result = ((result * 31) + ((this.gpsiRange == null) ? 0 : this.gpsiRange.hashCode()));
        result = ((result * 31) + ((this.supiRange == null) ? 0 : this.supiRange.hashCode()));
        result = ((result * 31) + ((this.plmnRange == null) ? 0 : this.plmnRange.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ChfInfo) == false)
        {
            return false;
        }
        ChfInfo rhs = ((ChfInfo) other);
        return ((((this.gpsiRange == rhs.gpsiRange) || ((this.gpsiRange != null) && this.gpsiRange.equals(rhs.gpsiRange)))
                 && ((this.supiRange == rhs.supiRange) || ((this.supiRange != null) && this.supiRange.equals(rhs.supiRange))))
                && ((this.plmnRange == rhs.plmnRange) || ((this.plmnRange != null) && this.plmnRange.equals(rhs.plmnRange))));
    }

}
