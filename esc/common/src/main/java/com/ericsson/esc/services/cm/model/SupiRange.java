
package com.ericsson.esc.services.cm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "pattern", "supi-start", "supi-end" })
public class SupiRange
{

    /**
     * Regular expression pattern representing the set of SUPIs belonging to this
     * range. A SUPI value is considered part of the range if, and only if, the SUPI
     * string fully matches the regular expression.
     * 
     */
    @JsonProperty("pattern")
    @JsonPropertyDescription("Regular expression pattern representing the set of SUPIs belonging to this range. A SUPI value is considered part of the range if, and only if, the SUPI string fully matches the regular expression.")
    private String pattern;
    /**
     * ID uniquely identifying the SUPI range (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("ID uniquely identifying the SUPI range")
    private Integer id;
    /**
     * First value identifying the start of a SUPI range, to be used when the range
     * of SUPI's can be represented as a numeric range, IMSI ranges for example.
     * This string shall consist only of digits.
     * 
     */
    @JsonProperty("supi-start")
    @JsonPropertyDescription("First value identifying the start of a SUPI range, to be used when the range of SUPI's can be represented as a numeric range, IMSI ranges for example. This string shall consist only of digits.")
    private String supiStart;
    /**
     * Last value identifying the end of a SUPI range, to be used when the range of
     * SUPI's can be represented as a numeric range, IMSI ranges for example. This
     * string shall consist only of digits.
     * 
     */
    @JsonProperty("supi-end")
    @JsonPropertyDescription("Last value identifying the end of a SUPI range, to be used when the range of SUPI's can be represented as a numeric range, IMSI ranges for example. This string shall consist only of digits.")
    private String supiEnd;

    /**
     * Regular expression pattern representing the set of SUPIs belonging to this
     * range. A SUPI value is considered part of the range if, and only if, the SUPI
     * string fully matches the regular expression.
     * 
     */
    @JsonProperty("pattern")
    public String getPattern()
    {
        return pattern;
    }

    /**
     * Regular expression pattern representing the set of SUPIs belonging to this
     * range. A SUPI value is considered part of the range if, and only if, the SUPI
     * string fully matches the regular expression.
     * 
     */
    @JsonProperty("pattern")
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public SupiRange withPattern(String pattern)
    {
        this.pattern = pattern;
        return this;
    }

    /**
     * ID uniquely identifying the SUPI range (Required)
     * 
     */
    @JsonProperty("id")
    public Integer getId()
    {
        return id;
    }

    /**
     * ID uniquely identifying the SUPI range (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(Integer id)
    {
        this.id = id;
    }

    public SupiRange withId(Integer id)
    {
        this.id = id;
        return this;
    }

    /**
     * First value identifying the start of a SUPI range, to be used when the range
     * of SUPI's can be represented as a numeric range, IMSI ranges for example.
     * This string shall consist only of digits.
     * 
     */
    @JsonProperty("supi-start")
    public String getSupiStart()
    {
        return supiStart;
    }

    /**
     * First value identifying the start of a SUPI range, to be used when the range
     * of SUPI's can be represented as a numeric range, IMSI ranges for example.
     * This string shall consist only of digits.
     * 
     */
    @JsonProperty("supi-start")
    public void setSupiStart(String supiStart)
    {
        this.supiStart = supiStart;
    }

    public SupiRange withSupiStart(String supiStart)
    {
        this.supiStart = supiStart;
        return this;
    }

    /**
     * Last value identifying the end of a SUPI range, to be used when the range of
     * SUPI's can be represented as a numeric range, IMSI ranges for example. This
     * string shall consist only of digits.
     * 
     */
    @JsonProperty("supi-end")
    public String getSupiEnd()
    {
        return supiEnd;
    }

    /**
     * Last value identifying the end of a SUPI range, to be used when the range of
     * SUPI's can be represented as a numeric range, IMSI ranges for example. This
     * string shall consist only of digits.
     * 
     */
    @JsonProperty("supi-end")
    public void setSupiEnd(String supiEnd)
    {
        this.supiEnd = supiEnd;
    }

    public SupiRange withSupiEnd(String supiEnd)
    {
        this.supiEnd = supiEnd;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SupiRange.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("pattern");
        sb.append('=');
        sb.append(((this.pattern == null) ? "<null>" : this.pattern));
        sb.append(',');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("supiStart");
        sb.append('=');
        sb.append(((this.supiStart == null) ? "<null>" : this.supiStart));
        sb.append(',');
        sb.append("supiEnd");
        sb.append('=');
        sb.append(((this.supiEnd == null) ? "<null>" : this.supiEnd));
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
        result = ((result * 31) + ((this.supiStart == null) ? 0 : this.supiStart.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.supiEnd == null) ? 0 : this.supiEnd.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof SupiRange) == false)
        {
            return false;
        }
        SupiRange rhs = ((SupiRange) other);
        return (((((this.pattern == rhs.pattern) || ((this.pattern != null) && this.pattern.equals(rhs.pattern)))
                  && ((this.supiStart == rhs.supiStart) || ((this.supiStart != null) && this.supiStart.equals(rhs.supiStart))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.supiEnd == rhs.supiEnd) || ((this.supiEnd != null) && this.supiEnd.equals(rhs.supiEnd))));
    }

}
