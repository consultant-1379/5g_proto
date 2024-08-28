
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.glue.IfVtapIngress;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "own-network-ref", "external-network-ref", "enabled" })
public class Ingress implements IfVtapIngress
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
    @JsonProperty("own-network-ref")
    @JsonPropertyDescription("Reference to the own-network the traffic of which is taken into account for tapping")
    private List<String> ownNetworkRef = new ArrayList<String>();
    /**
     * Reference to the external-network the traffic of which is taken into account
     * for tapping
     * 
     */
    @JsonProperty("external-network-ref")
    @JsonPropertyDescription("Reference to the external-network the traffic of which is taken into account for tapping")
    private List<String> externalNetworkRef = new ArrayList<String>();
    /**
     * A switch that allows the operator to enable or disable traffic tapping for
     * the specific element
     * 
     */
    @JsonProperty("enabled")
    @JsonPropertyDescription("A switch that allows the operator to enable or disable traffic tapping for the specific element")
    private Boolean enabled = true;

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
     * Reference to the own-network the traffic of which is taken into account for
     * tapping
     * 
     */
    @JsonProperty("own-network-ref")
    public List<String> getOwnNetworkRef()
    {
        return ownNetworkRef;
    }

    /**
     * Reference to the own-network the traffic of which is taken into account for
     * tapping
     * 
     */
    @JsonProperty("own-network-ref")
    public void setOwnNetworkRef(List<String> ownNetworkRef)
    {
        this.ownNetworkRef = ownNetworkRef;
    }

    public Ingress withOwnNetworkRef(List<String> ownNetworkRef)
    {
        this.ownNetworkRef = ownNetworkRef;
        return this;
    }

    /**
     * Reference to the external-network the traffic of which is taken into account
     * for tapping
     * 
     */
    @JsonProperty("external-network-ref")
    public List<String> getExternalNetworkRef()
    {
        return externalNetworkRef;
    }

    /**
     * Reference to the external-network the traffic of which is taken into account
     * for tapping
     * 
     */
    @JsonProperty("external-network-ref")
    public void setExternalNetworkRef(List<String> externalNetworkRef)
    {
        this.externalNetworkRef = externalNetworkRef;
    }

    public Ingress withExternalNetworkRef(List<String> externalNetworkRef)
    {
        this.externalNetworkRef = externalNetworkRef;
        return this;
    }

    /**
     * A switch that allows the operator to enable or disable traffic tapping for
     * the specific element
     * 
     */
    @JsonProperty("enabled")
    public Boolean getEnabled()
    {
        return enabled;
    }

    /**
     * A switch that allows the operator to enable or disable traffic tapping for
     * the specific element
     * 
     */
    @JsonProperty("enabled")
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public Ingress withEnabled(Boolean enabled)
    {
        this.enabled = enabled;
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
        sb.append("ownNetworkRef");
        sb.append('=');
        sb.append(((this.ownNetworkRef == null) ? "<null>" : this.ownNetworkRef));
        sb.append(',');
        sb.append("externalNetworkRef");
        sb.append('=');
        sb.append(((this.externalNetworkRef == null) ? "<null>" : this.externalNetworkRef));
        sb.append(',');
        sb.append("enabled");
        sb.append('=');
        sb.append(((this.enabled == null) ? "<null>" : this.enabled));
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
        result = ((result * 31) + ((this.ownNetworkRef == null) ? 0 : this.ownNetworkRef.hashCode()));
        result = ((result * 31) + ((this.externalNetworkRef == null) ? 0 : this.externalNetworkRef.hashCode()));
        result = ((result * 31) + ((this.enabled == null) ? 0 : this.enabled.hashCode()));
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
        return (((((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                  && ((this.ownNetworkRef == rhs.ownNetworkRef) || ((this.ownNetworkRef != null) && this.ownNetworkRef.equals(rhs.ownNetworkRef))))
                 && ((this.externalNetworkRef == rhs.externalNetworkRef)
                     || ((this.externalNetworkRef != null) && this.externalNetworkRef.equals(rhs.externalNetworkRef))))
                && ((this.enabled == rhs.enabled) || ((this.enabled != null) && this.enabled.equals(rhs.enabled))));
    }

}
