
package com.ericsson.sc.sepp.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "header", "matching-condition", "perform-action-on-attribute" })
public class SearchInHeader
{

    /**
     * The header of the message. (Required)
     * 
     */
    @JsonProperty("header")
    @JsonPropertyDescription("The header of the message.")
    private String header;
    /**
     * The condition to match against the message data
     * 
     */
    @JsonProperty("matching-condition")
    @JsonPropertyDescription("The condition to match against the message data")
    private String matchingCondition;
    @JsonProperty("perform-action-on-attribute")
    private SearchInHeader.PerformActionOnAttribute performActionOnAttribute;

    /**
     * The header of the message. (Required)
     * 
     */
    @JsonProperty("header")
    public String getHeader()
    {
        return header;
    }

    /**
     * The header of the message. (Required)
     * 
     */
    @JsonProperty("header")
    public void setHeader(String header)
    {
        this.header = header;
    }

    public SearchInHeader withHeader(String header)
    {
        this.header = header;
        return this;
    }

    /**
     * The condition to match against the message data
     * 
     */
    @JsonProperty("matching-condition")
    public String getMatchingCondition()
    {
        return matchingCondition;
    }

    /**
     * The condition to match against the message data
     * 
     */
    @JsonProperty("matching-condition")
    public void setMatchingCondition(String matchingCondition)
    {
        this.matchingCondition = matchingCondition;
    }

    public SearchInHeader withMatchingCondition(String matchingCondition)
    {
        this.matchingCondition = matchingCondition;
        return this;
    }

    @JsonProperty("perform-action-on-attribute")
    public SearchInHeader.PerformActionOnAttribute getPerformActionOnAttribute()
    {
        return performActionOnAttribute;
    }

    @JsonProperty("perform-action-on-attribute")
    public void setPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute performActionOnAttribute)
    {
        this.performActionOnAttribute = performActionOnAttribute;
    }

    public SearchInHeader withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute performActionOnAttribute)
    {
        this.performActionOnAttribute = performActionOnAttribute;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SearchInHeader.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("header");
        sb.append('=');
        sb.append(((this.header == null) ? "<null>" : this.header));
        sb.append(',');
        sb.append("matchingCondition");
        sb.append('=');
        sb.append(((this.matchingCondition == null) ? "<null>" : this.matchingCondition));
        sb.append(',');
        sb.append("performActionOnAttribute");
        sb.append('=');
        sb.append(((this.performActionOnAttribute == null) ? "<null>" : this.performActionOnAttribute));
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
        result = ((result * 31) + ((this.matchingCondition == null) ? 0 : this.matchingCondition.hashCode()));
        result = ((result * 31) + ((this.performActionOnAttribute == null) ? 0 : this.performActionOnAttribute.hashCode()));
        result = ((result * 31) + ((this.header == null) ? 0 : this.header.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof SearchInHeader) == false)
        {
            return false;
        }
        SearchInHeader rhs = ((SearchInHeader) other);
        return ((((this.matchingCondition == rhs.matchingCondition)
                  || ((this.matchingCondition != null) && this.matchingCondition.equals(rhs.matchingCondition)))
                 && ((this.performActionOnAttribute == rhs.performActionOnAttribute)
                     || ((this.performActionOnAttribute != null) && this.performActionOnAttribute.equals(rhs.performActionOnAttribute))))
                && ((this.header == rhs.header) || ((this.header != null) && this.header.equals(rhs.header))));
    }

    public enum PerformActionOnAttribute
    {

        REPLACE_WITH_OWN_FQDN("replace-with-own-fqdn"),
        ERASE("erase"),
        MAP("map"),
        DE_MAP("de-map"),
        SCRAMBLE("scramble"),
        DE_SCRAMBLE("de-scramble");

        private final String value;
        private final static Map<String, SearchInHeader.PerformActionOnAttribute> CONSTANTS = new HashMap<String, SearchInHeader.PerformActionOnAttribute>();

        static
        {
            for (SearchInHeader.PerformActionOnAttribute c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private PerformActionOnAttribute(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static SearchInHeader.PerformActionOnAttribute fromValue(String value)
        {
            SearchInHeader.PerformActionOnAttribute constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

}
