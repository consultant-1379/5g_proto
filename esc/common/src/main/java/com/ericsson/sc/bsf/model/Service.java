
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "bsf-service-ref" })
public class Service
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Reference to the own-network the traffic of which is taken into account for
     * tapping
     * 
     */
    @JsonProperty("bsf-service-ref")
    @JsonPropertyDescription("Reference to the own-network the traffic of which is taken into account for tapping")
    private String bsfServiceRef;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public Service withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Reference to the own-network the traffic of which is taken into account for
     * tapping
     * 
     */
    @JsonProperty("bsf-service-ref")
    public String getBsfServiceRef()
    {
        return bsfServiceRef;
    }

    /**
     * Reference to the own-network the traffic of which is taken into account for
     * tapping
     * 
     */
    @JsonProperty("bsf-service-ref")
    public void setBsfServiceRef(String bsfServiceRef)
    {
        this.bsfServiceRef = bsfServiceRef;
    }

    public Service withBsfServiceRef(String bsfServiceRef)
    {
        this.bsfServiceRef = bsfServiceRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Service.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("bsfServiceRef");
        sb.append('=');
        sb.append(((this.bsfServiceRef == null) ? "<null>" : this.bsfServiceRef));
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
        result = ((result * 31) + ((this.bsfServiceRef == null) ? 0 : this.bsfServiceRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Service) == false)
        {
            return false;
        }
        Service rhs = ((Service) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.bsfServiceRef == rhs.bsfServiceRef) || ((this.bsfServiceRef != null) && this.bsfServiceRef.equals(rhs.bsfServiceRef))));
    }

}
