
package com.ericsson.sc.bsf.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "service-address-ref" })
public class Ingress
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Reference to the service-address the traffic of which is taken into account
     * for tapping
     * 
     */
    @JsonProperty("service-address-ref")
    @JsonPropertyDescription("Reference to the service-address the traffic of which is taken into account for tapping")
    private List<String> serviceAddressRef = new ArrayList<String>();

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

    public Ingress withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Reference to the service-address the traffic of which is taken into account
     * for tapping
     * 
     */
    @JsonProperty("service-address-ref")
    public List<String> getServiceAddressRef()
    {
        return serviceAddressRef;
    }

    /**
     * Reference to the service-address the traffic of which is taken into account
     * for tapping
     * 
     */
    @JsonProperty("service-address-ref")
    public void setServiceAddressRef(List<String> serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
    }

    public Ingress withServiceAddressRef(List<String> serviceAddressRef)
    {
        this.serviceAddressRef = serviceAddressRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Ingress.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("serviceAddressRef");
        sb.append('=');
        sb.append(((this.serviceAddressRef == null) ? "<null>" : this.serviceAddressRef));
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
        result = ((result * 31) + ((this.serviceAddressRef == null) ? 0 : this.serviceAddressRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Ingress) == false)
        {
            return false;
        }
        Ingress rhs = ((Ingress) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.serviceAddressRef == rhs.serviceAddressRef)
                    || ((this.serviceAddressRef != null) && this.serviceAddressRef.equals(rhs.serviceAddressRef))));
    }

}
