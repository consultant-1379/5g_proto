
package com.ericsson.sc.bsf.model;

import java.util.Date;

import com.ericsson.sc.nfm.model.UpdateInterval;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "update-interval", "last-update" })
public class NfPoolDiscovery
{

    /**
     * Name identifying the nf-pool-discovery (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the nf-pool-discovery")
    private String name;
    /**
     * Time span between two nf-pool updates. Use special value infinite to stop
     * regular nf-pool updates
     * 
     */
    @JsonProperty("update-interval")
    @JsonPropertyDescription("Time span between two nf-pool updates. Use special value infinite to stop regular nf-pool updates")
    private UpdateInterval updateInterval = UpdateInterval.fromValue("1min");
    /**
     * Date and time of the last update of the discovered NF instances in this
     * nf-pool
     * 
     */
    @JsonProperty("last-update")
    @JsonPropertyDescription("Date and time of the last update of the discovered NF instances in this nf-pool")
    private Date lastUpdate;

    /**
     * Name identifying the nf-pool-discovery (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the nf-pool-discovery (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public NfPoolDiscovery withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Time span between two nf-pool updates. Use special value infinite to stop
     * regular nf-pool updates
     * 
     */
    @JsonProperty("update-interval")
    public UpdateInterval getUpdateInterval()
    {
        return updateInterval;
    }

    /**
     * Time span between two nf-pool updates. Use special value infinite to stop
     * regular nf-pool updates
     * 
     */
    @JsonProperty("update-interval")
    public void setUpdateInterval(UpdateInterval updateInterval)
    {
        this.updateInterval = updateInterval;
    }

    public NfPoolDiscovery withUpdateInterval(UpdateInterval updateInterval)
    {
        this.updateInterval = updateInterval;
        return this;
    }

    /**
     * Date and time of the last update of the discovered NF instances in this
     * nf-pool
     * 
     */
    @JsonProperty("last-update")
    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    /**
     * Date and time of the last update of the discovered NF instances in this
     * nf-pool
     * 
     */
    @JsonProperty("last-update")
    public void setLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
    }

    public NfPoolDiscovery withLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NfPoolDiscovery.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("updateInterval");
        sb.append('=');
        sb.append(((this.updateInterval == null) ? "<null>" : this.updateInterval));
        sb.append(',');
        sb.append("lastUpdate");
        sb.append('=');
        sb.append(((this.lastUpdate == null) ? "<null>" : this.lastUpdate));
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
        result = ((result * 31) + ((this.updateInterval == null) ? 0 : this.updateInterval.hashCode()));
        result = ((result * 31) + ((this.lastUpdate == null) ? 0 : this.lastUpdate.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NfPoolDiscovery) == false)
        {
            return false;
        }
        NfPoolDiscovery rhs = ((NfPoolDiscovery) other);
        return ((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                 && ((this.updateInterval == rhs.updateInterval) || ((this.updateInterval != null) && this.updateInterval.equals(rhs.updateInterval))))
                && ((this.lastUpdate == rhs.lastUpdate) || ((this.lastUpdate != null) && this.lastUpdate.equals(rhs.lastUpdate))));
    }

}
