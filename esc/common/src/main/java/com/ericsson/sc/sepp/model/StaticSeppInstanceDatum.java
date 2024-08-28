
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfStaticSeppInstanceDatum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "static-sepp-instance" })
public class StaticSeppInstanceDatum implements IfStaticSeppInstanceDatum
{

    /**
     * Name of the static SEPP instance data set (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the static SEPP instance data set")
    private String name;
    /**
     * Static SEPP instances in this data set (Required)
     * 
     */
    @JsonProperty("static-sepp-instance")
    @JsonPropertyDescription("Static SEPP instances in this data set")
    private List<StaticSeppInstance> staticSeppInstance = new ArrayList<StaticSeppInstance>();

    /**
     * Name of the static SEPP instance data set (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name of the static SEPP instance data set (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public StaticSeppInstanceDatum withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Static SEPP instances in this data set (Required)
     * 
     */
    @JsonProperty("static-sepp-instance")
    public List<StaticSeppInstance> getStaticSeppInstance()
    {
        return staticSeppInstance;
    }

    /**
     * Static SEPP instances in this data set (Required)
     * 
     */
    @JsonProperty("static-sepp-instance")
    public void setStaticSeppInstance(List<StaticSeppInstance> staticSeppInstance)
    {
        this.staticSeppInstance = staticSeppInstance;
    }

    public StaticSeppInstanceDatum withStaticSeppInstance(List<StaticSeppInstance> staticSeppInstance)
    {
        this.staticSeppInstance = staticSeppInstance;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(StaticSeppInstanceDatum.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("staticSeppInstance");
        sb.append('=');
        sb.append(((this.staticSeppInstance == null) ? "<null>" : this.staticSeppInstance));
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
        result = ((result * 31) + ((this.staticSeppInstance == null) ? 0 : this.staticSeppInstance.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof StaticSeppInstanceDatum) == false)
        {
            return false;
        }
        StaticSeppInstanceDatum rhs = ((StaticSeppInstanceDatum) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.staticSeppInstance == rhs.staticSeppInstance)
                    || ((this.staticSeppInstance != null) && this.staticSeppInstance.equals(rhs.staticSeppInstance))));
    }

}
