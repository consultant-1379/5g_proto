
package com.ericsson.sc.bsf.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "default-static-destination-profile-ref" })
public class NoBindingCase
{

    /**
     * Name of the no-binding-case. (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name of the no-binding-case.")
    private String name;
    /**
     * The default static destination profile in case of no-binding (Required)
     * 
     */
    @JsonProperty("default-static-destination-profile-ref")
    @JsonPropertyDescription("The default static destination profile in case of no-binding")
    private String defaultStaticDestinationProfileRef;

    /**
     * Name of the no-binding-case. (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name of the no-binding-case. (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public NoBindingCase withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The default static destination profile in case of no-binding (Required)
     * 
     */
    @JsonProperty("default-static-destination-profile-ref")
    public String getDefaultStaticDestinationProfileRef()
    {
        return defaultStaticDestinationProfileRef;
    }

    /**
     * The default static destination profile in case of no-binding (Required)
     * 
     */
    @JsonProperty("default-static-destination-profile-ref")
    public void setDefaultStaticDestinationProfileRef(String defaultStaticDestinationProfileRef)
    {
        this.defaultStaticDestinationProfileRef = defaultStaticDestinationProfileRef;
    }

    public NoBindingCase withDefaultStaticDestinationProfileRef(String defaultStaticDestinationProfileRef)
    {
        this.defaultStaticDestinationProfileRef = defaultStaticDestinationProfileRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(NoBindingCase.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("defaultStaticDestinationProfileRef");
        sb.append('=');
        sb.append(((this.defaultStaticDestinationProfileRef == null) ? "<null>" : this.defaultStaticDestinationProfileRef));
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
        result = ((result * 31) + ((this.defaultStaticDestinationProfileRef == null) ? 0 : this.defaultStaticDestinationProfileRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof NoBindingCase) == false)
        {
            return false;
        }
        NoBindingCase rhs = ((NoBindingCase) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.defaultStaticDestinationProfileRef == rhs.defaultStaticDestinationProfileRef)
                    || ((this.defaultStaticDestinationProfileRef != null)
                        && this.defaultStaticDestinationProfileRef.equals(rhs.defaultStaticDestinationProfileRef))));
    }

}
