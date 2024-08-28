
package com.ericsson.sc.nfm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "identity-start", "identity-end", "pattern" })
public class GpsiRange
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("id")
    private Integer id;
    /**
     * First value identifying the start of an identity range, to be used when the
     * range of identities can be represented as a numeric range, MSISDN ranges for
     * example. This string shall consist only of digits.
     * 
     */
    @JsonProperty("identity-start")
    @JsonPropertyDescription("First value identifying the start of an identity range, to be used when the range of identities can be represented as a numeric range, MSISDN ranges for example. This string shall consist only of digits.")
    private String identityStart;
    /**
     * Last value identifying the end of an identity range, to be used when the
     * range of identities can be represented as a numeric range, MSISDN ranges for
     * example. This string shall consist only of digits.
     * 
     */
    @JsonProperty("identity-end")
    @JsonPropertyDescription("Last value identifying the end of an identity range, to be used when the range of identities can be represented as a numeric range, MSISDN ranges for example. This string shall consist only of digits.")
    private String identityEnd;
    /**
     * Regular expression pattern representing the set of identities belonging to
     * this range. An identity value is considered part of the range if and only if
     * the identity string fully matches the regular expression. To be used when
     * identity is External Identifier or External Group Identifier or MSISDN.
     * 
     */
    @JsonProperty("pattern")
    @JsonPropertyDescription("Regular expression pattern representing the set of identities belonging to this range. An identity value is considered part of the range if and only if the identity string fully matches the regular expression. To be used when identity is External Identifier or External Group Identifier or MSISDN.")
    private String pattern;

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

    public GpsiRange withId(Integer id)
    {
        this.id = id;
        return this;
    }

    /**
     * First value identifying the start of an identity range, to be used when the
     * range of identities can be represented as a numeric range, MSISDN ranges for
     * example. This string shall consist only of digits.
     * 
     */
    @JsonProperty("identity-start")
    public String getIdentityStart()
    {
        return identityStart;
    }

    /**
     * First value identifying the start of an identity range, to be used when the
     * range of identities can be represented as a numeric range, MSISDN ranges for
     * example. This string shall consist only of digits.
     * 
     */
    @JsonProperty("identity-start")
    public void setIdentityStart(String identityStart)
    {
        this.identityStart = identityStart;
    }

    public GpsiRange withIdentityStart(String identityStart)
    {
        this.identityStart = identityStart;
        return this;
    }

    /**
     * Last value identifying the end of an identity range, to be used when the
     * range of identities can be represented as a numeric range, MSISDN ranges for
     * example. This string shall consist only of digits.
     * 
     */
    @JsonProperty("identity-end")
    public String getIdentityEnd()
    {
        return identityEnd;
    }

    /**
     * Last value identifying the end of an identity range, to be used when the
     * range of identities can be represented as a numeric range, MSISDN ranges for
     * example. This string shall consist only of digits.
     * 
     */
    @JsonProperty("identity-end")
    public void setIdentityEnd(String identityEnd)
    {
        this.identityEnd = identityEnd;
    }

    public GpsiRange withIdentityEnd(String identityEnd)
    {
        this.identityEnd = identityEnd;
        return this;
    }

    /**
     * Regular expression pattern representing the set of identities belonging to
     * this range. An identity value is considered part of the range if and only if
     * the identity string fully matches the regular expression. To be used when
     * identity is External Identifier or External Group Identifier or MSISDN.
     * 
     */
    @JsonProperty("pattern")
    public String getPattern()
    {
        return pattern;
    }

    /**
     * Regular expression pattern representing the set of identities belonging to
     * this range. An identity value is considered part of the range if and only if
     * the identity string fully matches the regular expression. To be used when
     * identity is External Identifier or External Group Identifier or MSISDN.
     * 
     */
    @JsonProperty("pattern")
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    public GpsiRange withPattern(String pattern)
    {
        this.pattern = pattern;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(GpsiRange.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("identityStart");
        sb.append('=');
        sb.append(((this.identityStart == null) ? "<null>" : this.identityStart));
        sb.append(',');
        sb.append("identityEnd");
        sb.append('=');
        sb.append(((this.identityEnd == null) ? "<null>" : this.identityEnd));
        sb.append(',');
        sb.append("pattern");
        sb.append('=');
        sb.append(((this.pattern == null) ? "<null>" : this.pattern));
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
        result = ((result * 31) + ((this.identityStart == null) ? 0 : this.identityStart.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.identityEnd == null) ? 0 : this.identityEnd.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof GpsiRange) == false)
        {
            return false;
        }
        GpsiRange rhs = ((GpsiRange) other);
        return (((((this.pattern == rhs.pattern) || ((this.pattern != null) && this.pattern.equals(rhs.pattern)))
                  && ((this.identityStart == rhs.identityStart) || ((this.identityStart != null) && this.identityStart.equals(rhs.identityStart))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.identityEnd == rhs.identityEnd) || ((this.identityEnd != null) && this.identityEnd.equals(rhs.identityEnd))));
    }

}
