
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "replication-factor" })
public class EricssonBsfDatacenter
{

    /**
     * Name uniquely identifying the datacenter (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name uniquely identifying the datacenter")
    private String name;
    /**
     * Cassandra replication factor for the specific datacenter
     * 
     */
    @JsonProperty("replication-factor")
    @JsonPropertyDescription("Cassandra replication factor for the specific datacenter")
    private Integer replicationFactor = 2;

    /**
     * Name uniquely identifying the datacenter (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name uniquely identifying the datacenter (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public EricssonBsfDatacenter withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Cassandra replication factor for the specific datacenter
     * 
     */
    @JsonProperty("replication-factor")
    public Integer getReplicationFactor()
    {
        return replicationFactor;
    }

    /**
     * Cassandra replication factor for the specific datacenter
     * 
     */
    @JsonProperty("replication-factor")
    public void setReplicationFactor(Integer replicationFactor)
    {
        this.replicationFactor = replicationFactor;
    }

    public EricssonBsfDatacenter withReplicationFactor(Integer replicationFactor)
    {
        this.replicationFactor = replicationFactor;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonBsfDatacenter.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("replicationFactor");
        sb.append('=');
        sb.append(((this.replicationFactor == null) ? "<null>" : this.replicationFactor));
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
        result = ((result * 31) + ((this.replicationFactor == null) ? 0 : this.replicationFactor.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonBsfDatacenter) == false)
        {
            return false;
        }
        EricssonBsfDatacenter rhs = ((EricssonBsfDatacenter) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.replicationFactor == rhs.replicationFactor)
                    || ((this.replicationFactor != null) && this.replicationFactor.equals(rhs.replicationFactor))));
    }

}
