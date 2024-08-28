
package com.ericsson.esc.services.cm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "plmn-start", "pattern", "plmn-end" })
public class PlmnRange
{

    /**
     * First value identifying the start of a PLMN range. The string shall be
     * encoded as follows: <MCC><MNC>. For example '12340'.
     * 
     */
    @JsonProperty("plmn-start")
    @JsonPropertyDescription("First value identifying the start of a PLMN range. The string shall be encoded as follows: <MCC><MNC>. For example '12340'.")
    private String plmnStart;
    /**
     * Regular expression pattern representing the set of PLMNs belonging to this
     * range. A PLMN value is considered part of the range if and only if the PLMN
     * string, formatted as <MCC><MNC>, fully matches the regular expression. For
     * example '^1234[0-9]$'
     * 
     */
    @JsonProperty("pattern")
    @JsonPropertyDescription("Regular expression pattern representing the set of PLMNs belonging to this range. A PLMN value is considered part of the range if and only if the PLMN string, formatted as <MCC><MNC>, fully matches the regular expression. For example '^1234[0-9]$'")
    private String pattern;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private Integer id;
    /**
     * Last value identifying the end of a PLMN range. The string shall be encoded
     * as follows: <MCC><MNC>. For example '12349'.
     * 
     */
    @JsonProperty("plmn-end")
    @JsonPropertyDescription("Last value identifying the end of a PLMN range. The string shall be encoded as follows: <MCC><MNC>. For example '12349'.")
    private String plmnEnd;

    /**
     * First value identifying the start of a PLMN range. The string shall be
     * encoded as follows: <MCC><MNC>. For example '12340'.
     * 
     */
    @JsonProperty("plmn-start")
    public String getPlmnStart()
    {
        return plmnStart;
    }

    /**
     * First value identifying the start of a PLMN range. The string shall be
     * encoded as follows: <MCC><MNC>. For example '12340'.
     * 
     */
    @JsonProperty("plmn-start")
    public void setPlmnStart(String plmnStart)
    {
        this.plmnStart = plmnStart;
    }

    public PlmnRange withPlmnStart(String plmnStart)
    {
        this.plmnStart = plmnStart;
        return this;
    }

    /**
     * Regular expression pattern representing the set of PLMNs belonging to this
     * range. A PLMN value is considered part of the range if and only if the PLMN
     * string, formatted as <MCC><MNC>, fully matches the regular expression. For
     * example '^1234[0-9]$'
     * 
     */
    @JsonProperty("pattern")
    public String getPattern()
    {
        return pattern;
    }

    /**
     * Regular expression pattern representing the set of PLMNs belonging to this
     * range. A PLMN value is considered part of the range if and only if the PLMN
     * string, formatted as <MCC><MNC>, fully matches the regular expression. For
     * example '^1234[0-9]$'
     * 
     */
    @JsonProperty("pattern")
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public PlmnRange withPattern(String pattern)
    {
        this.pattern = pattern;
        return this;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public Integer getId()
    {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(Integer id)
    {
        this.id = id;
    }

    public PlmnRange withId(Integer id)
    {
        this.id = id;
        return this;
    }

    /**
     * Last value identifying the end of a PLMN range. The string shall be encoded
     * as follows: <MCC><MNC>. For example '12349'.
     * 
     */
    @JsonProperty("plmn-end")
    public String getPlmnEnd()
    {
        return plmnEnd;
    }

    /**
     * Last value identifying the end of a PLMN range. The string shall be encoded
     * as follows: <MCC><MNC>. For example '12349'.
     * 
     */
    @JsonProperty("plmn-end")
    public void setPlmnEnd(String plmnEnd)
    {
        this.plmnEnd = plmnEnd;
    }

    public PlmnRange withPlmnEnd(String plmnEnd)
    {
        this.plmnEnd = plmnEnd;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(PlmnRange.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("plmnStart");
        sb.append('=');
        sb.append(((this.plmnStart == null) ? "<null>" : this.plmnStart));
        sb.append(',');
        sb.append("pattern");
        sb.append('=');
        sb.append(((this.pattern == null) ? "<null>" : this.pattern));
        sb.append(',');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("plmnEnd");
        sb.append('=');
        sb.append(((this.plmnEnd == null) ? "<null>" : this.plmnEnd));
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
        result = ((result * 31) + ((this.pattern == null) ? 0 : this.pattern.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.plmnStart == null) ? 0 : this.plmnStart.hashCode()));
        result = ((result * 31) + ((this.plmnEnd == null) ? 0 : this.plmnEnd.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof PlmnRange) == false)
        {
            return false;
        }
        PlmnRange rhs = ((PlmnRange) other);
        return (((((this.pattern == rhs.pattern) || ((this.pattern != null) && this.pattern.equals(rhs.pattern)))
                  && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                 && ((this.plmnStart == rhs.plmnStart) || ((this.plmnStart != null) && this.plmnStart.equals(rhs.plmnStart))))
                && ((this.plmnEnd == rhs.plmnEnd) || ((this.plmnEnd != null) && this.plmnEnd.equals(rhs.plmnEnd))));
    }

}
