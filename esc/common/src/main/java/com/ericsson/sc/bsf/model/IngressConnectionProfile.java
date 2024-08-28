
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-label", "hpack-table-size", "dscp-marking" })
public class IngressConnectionProfile
{

    /**
     * Name identifying the ingress-connection-profile (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the ingress-connection-profile")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * Maximum table size (in octets) that the encoder is permitted to use for the
     * dynamic HPACK table. Valid values range from 0 to 4294967295 (2^32 - 1). 0
     * effectively disables header compression.
     * 
     */
    @JsonProperty("hpack-table-size")
    @JsonPropertyDescription("Maximum table size (in octets) that the encoder is permitted to use for the dynamic HPACK table. Valid values range from 0 to 4294967295 (2^32 - 1). 0 effectively disables header compression.")
    private Long hpackTableSize = 4096L;
    /**
     * DSCP value used for IP packets sent over this connection
     * 
     */
    @JsonProperty("dscp-marking")
    @JsonPropertyDescription("DSCP value used for IP packets sent over this connection")
    private Integer dscpMarking = 0;

    /**
     * Name identifying the ingress-connection-profile (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the ingress-connection-profile (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public IngressConnectionProfile withName(String name)
    {
        this.name = name;
        return this;
    }

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

    public IngressConnectionProfile withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * Maximum table size (in octets) that the encoder is permitted to use for the
     * dynamic HPACK table. Valid values range from 0 to 4294967295 (2^32 - 1). 0
     * effectively disables header compression.
     * 
     */
    @JsonProperty("hpack-table-size")
    public Long getHpackTableSize()
    {
        return hpackTableSize;
    }

    /**
     * Maximum table size (in octets) that the encoder is permitted to use for the
     * dynamic HPACK table. Valid values range from 0 to 4294967295 (2^32 - 1). 0
     * effectively disables header compression.
     * 
     */
    @JsonProperty("hpack-table-size")
    public void setHpackTableSize(Long hpackTableSize)
    {
        this.hpackTableSize = hpackTableSize;
    }

    public IngressConnectionProfile withHpackTableSize(Long hpackTableSize)
    {
        this.hpackTableSize = hpackTableSize;
        return this;
    }

    /**
     * DSCP value used for IP packets sent over this connection
     * 
     */
    @JsonProperty("dscp-marking")
    public Integer getDscpMarking()
    {
        return dscpMarking;
    }

    /**
     * DSCP value used for IP packets sent over this connection
     * 
     */
    @JsonProperty("dscp-marking")
    public void setDscpMarking(Integer dscpMarking)
    {
        this.dscpMarking = dscpMarking;
    }

    public IngressConnectionProfile withDscpMarking(Integer dscpMarking)
    {
        this.dscpMarking = dscpMarking;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(IngressConnectionProfile.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("hpackTableSize");
        sb.append('=');
        sb.append(((this.hpackTableSize == null) ? "<null>" : this.hpackTableSize));
        sb.append(',');
        sb.append("dscpMarking");
        sb.append('=');
        sb.append(((this.dscpMarking == null) ? "<null>" : this.dscpMarking));
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
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.hpackTableSize == null) ? 0 : this.hpackTableSize.hashCode()));
        result = ((result * 31) + ((this.dscpMarking == null) ? 0 : this.dscpMarking.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof IngressConnectionProfile) == false)
        {
            return false;
        }
        IngressConnectionProfile rhs = ((IngressConnectionProfile) other);
        return (((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                  && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                 && ((this.hpackTableSize == rhs.hpackTableSize) || ((this.hpackTableSize != null) && this.hpackTableSize.equals(rhs.hpackTableSize))))
                && ((this.dscpMarking == rhs.dscpMarking) || ((this.dscpMarking != null) && this.dscpMarking.equals(rhs.dscpMarking))));
    }

}
