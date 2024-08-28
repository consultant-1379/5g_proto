
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Schema status related properties
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ready", "info" })
public class EricssonBsfStatus
{

    /**
     * Specifies if the schema is ready or not (Required)
     * 
     */
    @JsonProperty("ready")
    @JsonPropertyDescription("Specifies if the schema is ready or not")
    private Boolean ready;
    /**
     * More detailed information about the schema status when it is not ready
     * 
     */
    @JsonProperty("info")
    @JsonPropertyDescription("More detailed information about the schema status when it is not ready")
    private String info;

    /**
     * Specifies if the schema is ready or not (Required)
     * 
     */
    @JsonProperty("ready")
    public Boolean getReady()
    {
        return ready;
    }

    /**
     * Specifies if the schema is ready or not (Required)
     * 
     */
    @JsonProperty("ready")
    public void setReady(Boolean ready)
    {
        this.ready = ready;
    }

    public EricssonBsfStatus withReady(Boolean ready)
    {
        this.ready = ready;
        return this;
    }

    /**
     * More detailed information about the schema status when it is not ready
     * 
     */
    @JsonProperty("info")
    public String getInfo()
    {
        return info;
    }

    /**
     * More detailed information about the schema status when it is not ready
     * 
     */
    @JsonProperty("info")
    public void setInfo(String info)
    {
        this.info = info;
    }

    public EricssonBsfStatus withInfo(String info)
    {
        this.info = info;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonBsfStatus.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ready");
        sb.append('=');
        sb.append(((this.ready == null) ? "<null>" : this.ready));
        sb.append(',');
        sb.append("info");
        sb.append('=');
        sb.append(((this.info == null) ? "<null>" : this.info));
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
        result = ((result * 31) + ((this.ready == null) ? 0 : this.ready.hashCode()));
        result = ((result * 31) + ((this.info == null) ? 0 : this.info.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonBsfStatus) == false)
        {
            return false;
        }
        EricssonBsfStatus rhs = ((EricssonBsfStatus) other);
        return (((this.ready == rhs.ready) || ((this.ready != null) && this.ready.equals(rhs.ready)))
                && ((this.info == rhs.info) || ((this.info != null) && this.info.equals(rhs.info))));
    }

}
