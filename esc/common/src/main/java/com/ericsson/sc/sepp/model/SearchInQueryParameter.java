
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
@JsonPropertyOrder({ "query-parameter", "matching-condition", "perform-action-on-attribute" })
public class SearchInQueryParameter
{

    /**
     * The query parameter (Required)
     * 
     */
    @JsonProperty("query-parameter")
    @JsonPropertyDescription("The query parameter")
    private String queryParameter;
    /**
     * The condition to match against the message data
     * 
     */
    @JsonProperty("matching-condition")
    @JsonPropertyDescription("The condition to match against the message data")
    private String matchingCondition;
    @JsonProperty("perform-action-on-attribute")
    private SearchInQueryParameter.PerformActionOnAttribute performActionOnAttribute;

    /**
     * The query parameter (Required)
     * 
     */
    @JsonProperty("query-parameter")
    public String getQueryParameter()
    {
        return queryParameter;
    }

    /**
     * The query parameter (Required)
     * 
     */
    @JsonProperty("query-parameter")
    public void setQueryParameter(String queryParameter)
    {
        this.queryParameter = queryParameter;
    }

    public SearchInQueryParameter withQueryParameter(String queryParameter)
    {
        this.queryParameter = queryParameter;
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

    public SearchInQueryParameter withMatchingCondition(String matchingCondition)
    {
        this.matchingCondition = matchingCondition;
        return this;
    }

    @JsonProperty("perform-action-on-attribute")
    public SearchInQueryParameter.PerformActionOnAttribute getPerformActionOnAttribute()
    {
        return performActionOnAttribute;
    }

    @JsonProperty("perform-action-on-attribute")
    public void setPerformActionOnAttribute(SearchInQueryParameter.PerformActionOnAttribute performActionOnAttribute)
    {
        this.performActionOnAttribute = performActionOnAttribute;
    }

    public SearchInQueryParameter withPerformActionOnAttribute(SearchInQueryParameter.PerformActionOnAttribute performActionOnAttribute)
    {
        this.performActionOnAttribute = performActionOnAttribute;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SearchInQueryParameter.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("queryParameter");
        sb.append('=');
        sb.append(((this.queryParameter == null) ? "<null>" : this.queryParameter));
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
        result = ((result * 31) + ((this.queryParameter == null) ? 0 : this.queryParameter.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof SearchInQueryParameter) == false)
        {
            return false;
        }
        SearchInQueryParameter rhs = ((SearchInQueryParameter) other);
        return ((((this.matchingCondition == rhs.matchingCondition)
                  || ((this.matchingCondition != null) && this.matchingCondition.equals(rhs.matchingCondition)))
                 && ((this.performActionOnAttribute == rhs.performActionOnAttribute)
                     || ((this.performActionOnAttribute != null) && this.performActionOnAttribute.equals(rhs.performActionOnAttribute))))
                && ((this.queryParameter == rhs.queryParameter) || ((this.queryParameter != null) && this.queryParameter.equals(rhs.queryParameter))));
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
        private final static Map<String, SearchInQueryParameter.PerformActionOnAttribute> CONSTANTS = new HashMap<String, SearchInQueryParameter.PerformActionOnAttribute>();

        static
        {
            for (SearchInQueryParameter.PerformActionOnAttribute c : values())
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
        public static SearchInQueryParameter.PerformActionOnAttribute fromValue(String value)
        {
            SearchInQueryParameter.PerformActionOnAttribute constant = CONSTANTS.get(value);
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
