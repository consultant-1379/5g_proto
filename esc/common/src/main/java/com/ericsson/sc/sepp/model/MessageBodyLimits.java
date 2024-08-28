
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines the limits for the message body
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "max-bytes", "max-leaves", "max-nesting-depth" })
public class MessageBodyLimits
{

    /**
     * Maximum message body size in bytes
     * 
     */
    @JsonProperty("max-bytes")
    @JsonPropertyDescription("Maximum message body size in bytes")
    private Integer maxBytes = 16000000;
    /**
     * Maximum integer of leaves in the JSON message body
     * 
     */
    @JsonProperty("max-leaves")
    @JsonPropertyDescription("Maximum integer of leaves in the JSON message body")
    private Integer maxLeaves;
    /**
     * Maximum nesting depth of the leaves in the JSON message body
     * 
     */
    @JsonProperty("max-nesting-depth")
    @JsonPropertyDescription("Maximum nesting depth of the leaves in the JSON message body")
    private Integer maxNestingDepth;

    /**
     * Maximum message body size in bytes
     * 
     */
    @JsonProperty("max-bytes")
    public Integer getMaxBytes()
    {
        return maxBytes;
    }

    /**
     * Maximum message body size in bytes
     * 
     */
    @JsonProperty("max-bytes")
    public void setMaxBytes(Integer maxBytes)
    {
        this.maxBytes = maxBytes;
    }

    public MessageBodyLimits withMaxBytes(Integer maxBytes)
    {
        this.maxBytes = maxBytes;
        return this;
    }

    /**
     * Maximum integer of leaves in the JSON message body
     * 
     */
    @JsonProperty("max-leaves")
    public Integer getMaxLeaves()
    {
        return maxLeaves;
    }

    /**
     * Maximum integer of leaves in the JSON message body
     * 
     */
    @JsonProperty("max-leaves")
    public void setMaxLeaves(Integer maxLeaves)
    {
        this.maxLeaves = maxLeaves;
    }

    public MessageBodyLimits withMaxLeaves(Integer maxLeaves)
    {
        this.maxLeaves = maxLeaves;
        return this;
    }

    /**
     * Maximum nesting depth of the leaves in the JSON message body
     * 
     */
    @JsonProperty("max-nesting-depth")
    public Integer getMaxNestingDepth()
    {
        return maxNestingDepth;
    }

    /**
     * Maximum nesting depth of the leaves in the JSON message body
     * 
     */
    @JsonProperty("max-nesting-depth")
    public void setMaxNestingDepth(Integer maxNestingDepth)
    {
        this.maxNestingDepth = maxNestingDepth;
    }

    public MessageBodyLimits withMaxNestingDepth(Integer maxNestingDepth)
    {
        this.maxNestingDepth = maxNestingDepth;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageBodyLimits.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("maxBytes");
        sb.append('=');
        sb.append(((this.maxBytes == null) ? "<null>" : this.maxBytes));
        sb.append(',');
        sb.append("maxLeaves");
        sb.append('=');
        sb.append(((this.maxLeaves == null) ? "<null>" : this.maxLeaves));
        sb.append(',');
        sb.append("maxNestingDepth");
        sb.append('=');
        sb.append(((this.maxNestingDepth == null) ? "<null>" : this.maxNestingDepth));
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
        result = ((result * 31) + ((this.maxNestingDepth == null) ? 0 : this.maxNestingDepth.hashCode()));
        result = ((result * 31) + ((this.maxBytes == null) ? 0 : this.maxBytes.hashCode()));
        result = ((result * 31) + ((this.maxLeaves == null) ? 0 : this.maxLeaves.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof MessageBodyLimits) == false)
        {
            return false;
        }
        MessageBodyLimits rhs = ((MessageBodyLimits) other);
        return ((((this.maxNestingDepth == rhs.maxNestingDepth) || ((this.maxNestingDepth != null) && this.maxNestingDepth.equals(rhs.maxNestingDepth)))
                 && ((this.maxBytes == rhs.maxBytes) || ((this.maxBytes != null) && this.maxBytes.equals(rhs.maxBytes))))
                && ((this.maxLeaves == rhs.maxLeaves) || ((this.maxLeaves != null) && this.maxLeaves.equals(rhs.maxLeaves))));
    }

}
