
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
 * Configuration dedicated for multiple binding handling on diameter-lookup.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "user-label", "deletion-upon-lookup", "resolution-type", "avp-combination" })
public class DiameterLookup
{
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * Deletion based on multiple-binding-resolution for diameter-lookup.
     * 
     */
    @JsonProperty("deletion-upon-lookup")
    @JsonPropertyDescription("Deletion based on multiple-binding-resolution for diameter-lookup.")
    private Boolean deletionUponLookup = true;
    @JsonProperty("resolution-type")
    private DiameterLookup.ResolutionType resolutionType = DiameterLookup.ResolutionType.fromValue("most-recent");
    /**
     * Configured avp-combination for diameter-lookup.
     * 
     */
    @JsonProperty("avp-combination")
    @JsonPropertyDescription("Configured avp-combination for diameter-lookup.")
    private List<AvpCombination> avpCombination = new ArrayList<AvpCombination>();

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

    public DiameterLookup withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Deletion based on multiple-binding-resolution for diameter-lookup.
     * 
     */
    @JsonProperty("deletion-upon-lookup")
    public Boolean getDeletionUponLookup()
    {
        return deletionUponLookup;
    }

    /**
     * Deletion based on multiple-binding-resolution for diameter-lookup.
     * 
     */
    @JsonProperty("deletion-upon-lookup")
    public void setDeletionUponLookup(Boolean deletionUponLookup)
    {
        this.deletionUponLookup = deletionUponLookup;
    }

    public DiameterLookup withDeletionUponLookup(Boolean deletionUponLookup)
    {
        this.deletionUponLookup = deletionUponLookup;
        return this;
    }

    @JsonProperty("resolution-type")
    public DiameterLookup.ResolutionType getResolutionType()
    {
        return resolutionType;
    }

    @JsonProperty("resolution-type")
    public void setResolutionType(DiameterLookup.ResolutionType resolutionType)
    {
        this.resolutionType = resolutionType;
    }

    public DiameterLookup withResolutionType(DiameterLookup.ResolutionType resolutionType)
    {
        this.resolutionType = resolutionType;
        return this;
    }

    /**
     * Configured avp-combination for diameter-lookup.
     * 
     */
    @JsonProperty("avp-combination")
    public List<AvpCombination> getAvpCombination()
    {
        return avpCombination;
    }

    /**
     * Configured avp-combination for diameter-lookup.
     * 
     */
    @JsonProperty("avp-combination")
    public void setAvpCombination(List<AvpCombination> avpCombination)
    {
        this.avpCombination = avpCombination;
    }

    public DiameterLookup withAvpCombination(List<AvpCombination> avpCombination)
    {
        this.avpCombination = avpCombination;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(DiameterLookup.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        sb.append("avpCombination");
        sb.append('=');
        sb.append(((this.avpCombination == null) ? "<null>" : this.avpCombination));
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
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.resolutionType == null) ? 0 : this.resolutionType.hashCode()));
        result = ((result * 31) + ((this.avpCombination == null) ? 0 : this.avpCombination.hashCode()));
        result = ((result * 31) + ((this.deletionUponLookup == null) ? 0 : this.deletionUponLookup.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof DiameterLookup) == false)
        {
            return false;
        }
        DiameterLookup rhs = ((DiameterLookup) other);
        return (((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                  && ((this.resolutionType == rhs.resolutionType) || ((this.resolutionType != null) && this.resolutionType.equals(rhs.resolutionType))))
                 && ((this.avpCombination == rhs.avpCombination) || ((this.avpCombination != null) && this.avpCombination.equals(rhs.avpCombination))))
                && ((this.deletionUponLookup == rhs.deletionUponLookup)
                    || ((this.deletionUponLookup != null) && this.deletionUponLookup.equals(rhs.deletionUponLookup))));
    }

    public enum ResolutionType
    {

        REJECT("reject"),
        MOST_RECENT("most-recent"),
        MOST_RECENT_CONDITIONAL("most-recent-conditional");

        private final String value;
        private final static Map<String, DiameterLookup.ResolutionType> CONSTANTS = new HashMap<String, DiameterLookup.ResolutionType>();

        static
        {
            for (DiameterLookup.ResolutionType c : values())
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
        public static DiameterLookup.ResolutionType fromValue(String value)
        {
            DiameterLookup.ResolutionType constant = CONSTANTS.get(value);
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
