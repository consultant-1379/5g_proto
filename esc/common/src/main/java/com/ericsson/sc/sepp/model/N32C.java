
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Defines data to be used for the N32 handshake procedure between the SEPPs in
 * two PLMNs.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "own-security-data" })
public class N32C
{

    /**
     * Definition of own security data.
     * 
     */
    @JsonProperty("own-security-data")
    @JsonPropertyDescription("Definition of own security data.")
    private List<OwnSecurityDatum> ownSecurityData = new ArrayList<OwnSecurityDatum>();

    /**
     * Definition of own security data.
     * 
     */
    @JsonProperty("own-security-data")
    public List<OwnSecurityDatum> getOwnSecurityData()
    {
        return ownSecurityData;
    }

    /**
     * Definition of own security data.
     * 
     */
    @JsonProperty("own-security-data")
    public void setOwnSecurityData(List<OwnSecurityDatum> ownSecurityData)
    {
        this.ownSecurityData = ownSecurityData;
    }

    public N32C withOwnSecurityData(List<OwnSecurityDatum> ownSecurityData)
    {
        this.ownSecurityData = ownSecurityData;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(N32C.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ownSecurityData");
        sb.append('=');
        sb.append(((this.ownSecurityData == null) ? "<null>" : this.ownSecurityData));
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
        result = ((result * 31) + ((this.ownSecurityData == null) ? 0 : this.ownSecurityData.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof N32C) == false)
        {
            return false;
        }
        N32C rhs = ((N32C) other);
        return ((this.ownSecurityData == rhs.ownSecurityData) || ((this.ownSecurityData != null) && this.ownSecurityData.equals(rhs.ownSecurityData)));
    }

}
