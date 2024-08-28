
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
@JsonPropertyOrder({ "body-json-pointer", "matching-condition", "perform-action-on-attribute" })
public class SearchInMessageBody
{

    /**
     * The element to be extracted from a JSON-formatted body with a JSON-Pointer
     * specification according to RFC 6901 (Required)
     * 
     */
    @JsonProperty("body-json-pointer")
    @JsonPropertyDescription("The element to be extracted from a JSON-formatted body with a JSON-Pointer specification according to RFC 6901")
    private String bodyJsonPointer;
    /**
     * The condition to match against the message data
     * 
     */
    @JsonProperty("matching-condition")
    @JsonPropertyDescription("The condition to match against the message data")
    private String matchingCondition;
    @JsonProperty("perform-action-on-attribute")
    private SearchInMessageBody.PerformActionOnAttribute performActionOnAttribute;

    /**
     * The element to be extracted from a JSON-formatted body with a JSON-Pointer
     * specification according to RFC 6901 (Required)
     * 
     */
    @JsonProperty("body-json-pointer")
    public String getBodyJsonPointer()
    {
        return bodyJsonPointer;
    }

    /**
     * The element to be extracted from a JSON-formatted body with a JSON-Pointer
     * specification according to RFC 6901 (Required)
     * 
     */
    @JsonProperty("body-json-pointer")
    public void setBodyJsonPointer(String bodyJsonPointer)
    {
        this.bodyJsonPointer = bodyJsonPointer;
    }

    public SearchInMessageBody withBodyJsonPointer(String bodyJsonPointer)
    {
        this.bodyJsonPointer = bodyJsonPointer;
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

    public SearchInMessageBody withMatchingCondition(String matchingCondition)
    {
        this.matchingCondition = matchingCondition;
        return this;
    }

    @JsonProperty("perform-action-on-attribute")
    public SearchInMessageBody.PerformActionOnAttribute getPerformActionOnAttribute()
    {
        return performActionOnAttribute;
    }

    @JsonProperty("perform-action-on-attribute")
    public void setPerformActionOnAttribute(SearchInMessageBody.PerformActionOnAttribute performActionOnAttribute)
    {
        this.performActionOnAttribute = performActionOnAttribute;
    }

    public SearchInMessageBody withPerformActionOnAttribute(SearchInMessageBody.PerformActionOnAttribute performActionOnAttribute)
    {
        this.performActionOnAttribute = performActionOnAttribute;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SearchInMessageBody.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("bodyJsonPointer");
        sb.append('=');
        sb.append(((this.bodyJsonPointer == null) ? "<null>" : this.bodyJsonPointer));
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
        result = ((result * 31) + ((this.bodyJsonPointer == null) ? 0 : this.bodyJsonPointer.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof SearchInMessageBody) == false)
        {
            return false;
        }
        SearchInMessageBody rhs = ((SearchInMessageBody) other);
        return ((((this.matchingCondition == rhs.matchingCondition)
                  || ((this.matchingCondition != null) && this.matchingCondition.equals(rhs.matchingCondition)))
                 && ((this.performActionOnAttribute == rhs.performActionOnAttribute)
                     || ((this.performActionOnAttribute != null) && this.performActionOnAttribute.equals(rhs.performActionOnAttribute))))
                && ((this.bodyJsonPointer == rhs.bodyJsonPointer) || ((this.bodyJsonPointer != null) && this.bodyJsonPointer.equals(rhs.bodyJsonPointer))));
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
        private final static Map<String, SearchInMessageBody.PerformActionOnAttribute> CONSTANTS = new HashMap<String, SearchInMessageBody.PerformActionOnAttribute>();

        static
        {
            for (SearchInMessageBody.PerformActionOnAttribute c : values())
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
        public static SearchInMessageBody.PerformActionOnAttribute fromValue(String value)
        {
            SearchInMessageBody.PerformActionOnAttribute constant = CONSTANTS.get(value);
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
