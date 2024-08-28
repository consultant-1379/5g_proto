
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfStaticScpInstanceDatum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "static-scp-instance" })
public class StaticScpInstanceDatum implements IfStaticScpInstanceDatum
{

    /**
     * Name of the static SCP instance data set (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the static SCP instance data set")
    private String name;
    /**
     * Static SCP instances in this data set (Required)
     * 
     */
    @JsonProperty("static-scp-instance")
    @JsonPropertyDescription("Static SCP instances in this data set")
    private List<StaticScpInstance> staticScpInstance = new ArrayList<StaticScpInstance>();

    /**
     * Name of the static SCP instance data set (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name of the static SCP instance data set (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public StaticScpInstanceDatum withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Static SCP instances in this data set (Required)
     * 
     */
    @JsonProperty("static-scp-instance")
    public List<StaticScpInstance> getStaticScpInstance()
    {
        return staticScpInstance;
    }

    /**
     * Static SCP instances in this data set (Required)
     * 
     */
    @JsonProperty("static-scp-instance")
    public void setStaticScpInstance(List<StaticScpInstance> staticScpInstance)
    {
        this.staticScpInstance = staticScpInstance;
    }

    public StaticScpInstanceDatum withStaticScpInstance(List<StaticScpInstance> staticScpInstance)
    {
        this.staticScpInstance = staticScpInstance;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(StaticScpInstanceDatum.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("staticScpInstance");
        sb.append('=');
        sb.append(((this.staticScpInstance == null) ? "<null>" : this.staticScpInstance));
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
        result = ((result * 31) + ((this.staticScpInstance == null) ? 0 : this.staticScpInstance.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof StaticScpInstanceDatum) == false)
        {
            return false;
        }
        StaticScpInstanceDatum rhs = ((StaticScpInstanceDatum) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.staticScpInstance == rhs.staticScpInstance)
                    || ((this.staticScpInstance != null) && this.staticScpInstance.equals(rhs.staticScpInstance))));
    }

}
