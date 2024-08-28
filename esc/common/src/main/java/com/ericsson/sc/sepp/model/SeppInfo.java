
package com.ericsson.sc.sepp.model;

import com.ericsson.sc.nfm.model.RemotePlmn;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Specific data for the SEPP NF
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "sepp-prefix", "remote-plmn" })
public class SeppInfo
{

    /**
     * Optional string used to construct the apiRoot of the next hop SEPP
     * 
     */
    @JsonProperty("sepp-prefix")
    @JsonPropertyDescription("Optional string used to construct the apiRoot of the next hop SEPP")
    private String seppPrefix;
    /**
     * Remote PLMN(s) reachable through the SEPP
     * 
     */
    @JsonProperty("remote-plmn")
    @JsonPropertyDescription("Remote PLMN(s) reachable through the SEPP")
    private List<RemotePlmn> remotePlmn = new ArrayList<RemotePlmn>();

    /**
     * Optional string used to construct the apiRoot of the next hop SEPP
     * 
     */
    @JsonProperty("sepp-prefix")
    public String getSeppPrefix()
    {
        return seppPrefix;
    }

    /**
     * Optional string used to construct the apiRoot of the next hop SEPP
     * 
     */
    @JsonProperty("sepp-prefix")
    public void setSeppPrefix(String seppPrefix)
    {
        this.seppPrefix = seppPrefix;
    }

    public SeppInfo withSeppPrefix(String seppPrefix)
    {
        this.seppPrefix = seppPrefix;
        return this;
    }

    /**
     * Remote PLMN(s) reachable through the SEPP
     * 
     */
    @JsonProperty("remote-plmn")
    public List<RemotePlmn> getRemotePlmn()
    {
        return remotePlmn;
    }

    /**
     * Remote PLMN(s) reachable through the SEPP
     * 
     */
    @JsonProperty("remote-plmn")
    public void setRemotePlmn(List<RemotePlmn> remotePlmn)
    {
        this.remotePlmn = remotePlmn;
    }

    public SeppInfo withRemotePlmn(List<RemotePlmn> remotePlmn)
    {
        this.remotePlmn = remotePlmn;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SeppInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("seppPrefix");
        sb.append('=');
        sb.append(((this.seppPrefix == null) ? "<null>" : this.seppPrefix));
        sb.append(',');
        sb.append("remotePlmn");
        sb.append('=');
        sb.append(((this.remotePlmn == null) ? "<null>" : this.remotePlmn));
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
        result = ((result * 31) + ((this.seppPrefix == null) ? 0 : this.seppPrefix.hashCode()));
        result = ((result * 31) + ((this.remotePlmn == null) ? 0 : this.remotePlmn.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof SeppInfo) == false)
        {
            return false;
        }
        SeppInfo rhs = ((SeppInfo) other);
        return (((this.seppPrefix == rhs.seppPrefix) || ((this.seppPrefix != null) && this.seppPrefix.equals(rhs.seppPrefix)))
                && ((this.remotePlmn == rhs.remotePlmn) || ((this.remotePlmn != null) && this.remotePlmn.equals(rhs.remotePlmn))));
    }

}
