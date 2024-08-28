
package com.ericsson.sc.sepp.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "key-id-ref", "activation-date" })
public class ScramblingKey
{

    /**
     * The reference to the encryption identifier of the scrambling key which is
     * going to be used (Required)
     * 
     */
    @JsonProperty("key-id-ref")
    @JsonPropertyDescription("The reference to the encryption identifier of the scrambling key which is going to be used")
    private String keyIdRef;
    /**
     * The exact date and time when the key is activated. At least one of the
     * referenced scrambling keys must be activated already by the time the
     * configuration is passed (Required)
     * 
     */
    @JsonProperty("activation-date")
    @JsonPropertyDescription("The exact date and time when the key is activated. At least one of the referenced scrambling keys must be activated already by the time the configuration is passed")
    private Date activationDate;

    /**
     * The reference to the encryption identifier of the scrambling key which is
     * going to be used (Required)
     * 
     */
    @JsonProperty("key-id-ref")
    public String getKeyIdRef()
    {
        return keyIdRef;
    }

    /**
     * The reference to the encryption identifier of the scrambling key which is
     * going to be used (Required)
     * 
     */
    @JsonProperty("key-id-ref")
    public void setKeyIdRef(String keyIdRef)
    {
        this.keyIdRef = keyIdRef;
    }

    public ScramblingKey withKeyIdRef(String keyIdRef)
    {
        this.keyIdRef = keyIdRef;
        return this;
    }

    /**
     * The exact date and time when the key is activated. At least one of the
     * referenced scrambling keys must be activated already by the time the
     * configuration is passed (Required)
     * 
     */
    @JsonProperty("activation-date")
    public Date getActivationDate()
    {
        return activationDate;
    }

    /**
     * The exact date and time when the key is activated. At least one of the
     * referenced scrambling keys must be activated already by the time the
     * configuration is passed (Required)
     * 
     */
    @JsonProperty("activation-date")
    public void setActivationDate(Date activationDate)
    {
        this.activationDate = activationDate;
    }

    public ScramblingKey withActivationDate(Date activationDate)
    {
        this.activationDate = activationDate;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ScramblingKey.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("keyIdRef");
        sb.append('=');
        sb.append(((this.keyIdRef == null) ? "<null>" : this.keyIdRef));
        sb.append(',');
        sb.append("activationDate");
        sb.append('=');
        sb.append(((this.activationDate == null) ? "<null>" : this.activationDate));
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
        result = ((result * 31) + ((this.activationDate == null) ? 0 : this.activationDate.hashCode()));
        result = ((result * 31) + ((this.keyIdRef == null) ? 0 : this.keyIdRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ScramblingKey) == false)
        {
            return false;
        }
        ScramblingKey rhs = ((ScramblingKey) other);
        return (((this.activationDate == rhs.activationDate) || ((this.activationDate != null) && this.activationDate.equals(rhs.activationDate)))
                && ((this.keyIdRef == rhs.keyIdRef) || ((this.keyIdRef != null) && this.keyIdRef.equals(rhs.keyIdRef))));
    }

}
