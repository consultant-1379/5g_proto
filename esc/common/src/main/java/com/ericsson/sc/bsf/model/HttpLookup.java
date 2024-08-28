
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Configuration dedicated for multiple binding handling on http-lookup.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "user-label", "deletion-upon-lookup", "resolution-type", "query-parameter-combination" })
public class HttpLookup
{
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;

    /**
     * Deletion based on multiple-binding-resolution for http-lookup.
     * 
     */
    @JsonProperty("deletion-upon-lookup")
    @JsonPropertyDescription("Deletion based on multiple-binding-resolution for http-lookup.")
    private Boolean deletionUponLookup = true;
    @JsonProperty("resolution-type")
    private HttpLookup.ResolutionType resolutionType = HttpLookup.ResolutionType.fromValue("most-recent");
    /**
     * Configured query-parameter-combination for http-lookup.
     * 
     */
    @JsonProperty("query-parameter-combination")
    @JsonPropertyDescription("Configured query-parameter-combination for http-lookup.")
    private List<QueryParameterCombination> queryParameterCombination = new ArrayList<QueryParameterCombination>();

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public HttpLookup withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Deletion based on multiple-binding-resolution for http-lookup.
     * 
     */
    @JsonProperty("deletion-upon-lookup")
    public Boolean getDeletionUponLookup()
    {
        return deletionUponLookup;
    }

    /**
     * Deletion based on multiple-binding-resolution for http-lookup.
     * 
     */
    @JsonProperty("deletion-upon-lookup")
    public void setDeletionUponLookup(Boolean deletionUponLookup)
    {
        this.deletionUponLookup = deletionUponLookup;
    }

    public HttpLookup withDeletionUponLookup(Boolean deletionUponLookup)
    {
        this.deletionUponLookup = deletionUponLookup;
        return this;
    }

    @JsonProperty("resolution-type")
    public HttpLookup.ResolutionType getResolutionType()
    {
        return resolutionType;
    }

    @JsonProperty("resolution-type")
    public void setResolutionType(HttpLookup.ResolutionType resolutionType)
    {
        this.resolutionType = resolutionType;
    }

    public HttpLookup withResolutionType(HttpLookup.ResolutionType resolutionType)
    {
        this.resolutionType = resolutionType;
        return this;
    }

    /**
     * Configured query-parameter-combination for http-lookup.
     * 
     */
    @JsonProperty("query-parameter-combination")
    public List<QueryParameterCombination> getQueryParameterCombination()
    {
        return queryParameterCombination;
    }

    /**
     * Configured query-parameter-combination for http-lookup.
     * 
     */
    @JsonProperty("query-parameter-combination")
    public void setQueryParameterCombination(List<QueryParameterCombination> queryParameterCombination)
    {
        this.queryParameterCombination = queryParameterCombination;
    }

    public HttpLookup withQueryParameterCombination(List<QueryParameterCombination> queryParameterCombination)
    {
        this.queryParameterCombination = queryParameterCombination;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(HttpLookup.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("deletionUponLookup");
        sb.append('=');
        sb.append(((this.deletionUponLookup == null) ? "<null>" : this.deletionUponLookup));
        sb.append(',');
        sb.append("resolutionType");
        sb.append('=');
        sb.append(((this.resolutionType == null) ? "<null>" : this.resolutionType));
        sb.append(',');
        sb.append("queryParameterCombination");
        sb.append('=');
        sb.append(((this.queryParameterCombination == null) ? "<null>" : this.queryParameterCombination));
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
        result = ((result * 31) + ((this.resolutionType == null) ? 0 : this.resolutionType.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.deletionUponLookup == null) ? 0 : this.deletionUponLookup.hashCode()));
        result = ((result * 31) + ((this.queryParameterCombination == null) ? 0 : this.queryParameterCombination.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof HttpLookup) == false)
        {
            return false;
        }
        HttpLookup rhs = ((HttpLookup) other);
        return (((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                  && ((this.resolutionType == rhs.resolutionType) || ((this.resolutionType != null) && this.resolutionType.equals(rhs.resolutionType))))
                 && ((this.deletionUponLookup == rhs.deletionUponLookup)
                     || ((this.deletionUponLookup != null) && this.deletionUponLookup.equals(rhs.deletionUponLookup))))
                && ((this.queryParameterCombination == rhs.queryParameterCombination)
                    || ((this.queryParameterCombination != null) && this.queryParameterCombination.equals(rhs.queryParameterCombination))));
    }

    public enum ResolutionType
    {

        REJECT("reject"),
        MOST_RECENT("most-recent"),
        MOST_RECENT_CONDITIONAL("most-recent-conditional");

        private final String value;
        private final static Map<String, HttpLookup.ResolutionType> CONSTANTS = new HashMap<String, HttpLookup.ResolutionType>();

        static
        {
            for (HttpLookup.ResolutionType c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private ResolutionType(String value)
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
        public static HttpLookup.ResolutionType fromValue(String value)
        {
            HttpLookup.ResolutionType constant = CONSTANTS.get(value);
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
