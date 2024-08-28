
package com.ericsson.sc.scp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfStaticNfInstanceDatum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "static-nf-instance" })
public class StaticNfInstanceDatum implements IfStaticNfInstanceDatum
{

    /**
     * Name of the static NF instance data set (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the static NF instance data set")
    private String name;
    /**
     * Static NF instances in this data set (Required)
     * 
     */
    @JsonProperty("static-nf-instance")
    @JsonPropertyDescription("Static NF instances in this data set")
    private List<StaticNfInstance> staticNfInstance = new ArrayList<StaticNfInstance>();

    /**
     * Name of the static NF instance data set (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name of the static NF instance data set (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public StaticNfInstanceDatum withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Static NF instances in this data set (Required)
     * 
     */
    @JsonProperty("static-nf-instance")
    public List<StaticNfInstance> getStaticNfInstance()
    {
        return staticNfInstance;
    }

    /**
     * Static NF instances in this data set (Required)
     * 
     */
    @JsonProperty("static-nf-instance")
    public void setStaticNfInstance(List<StaticNfInstance> staticNfInstance)
    {
        this.staticNfInstance = staticNfInstance;
    }

    public StaticNfInstanceDatum withStaticNfInstance(List<StaticNfInstance> staticNfInstance)
    {
        this.staticNfInstance = staticNfInstance;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(StaticNfInstanceDatum.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("staticNfInstance");
        sb.append('=');
        sb.append(((this.staticNfInstance == null) ? "<null>" : this.staticNfInstance));
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
        result = ((result * 31) + ((this.staticNfInstance == null) ? 0 : this.staticNfInstance.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof StaticNfInstanceDatum) == false)
        {
            return false;
        }
        StaticNfInstanceDatum rhs = ((StaticNfInstanceDatum) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.staticNfInstance == rhs.staticNfInstance)
                    || ((this.staticNfInstance != null) && this.staticNfInstance.equals(rhs.staticNfInstance))));
    }

}
