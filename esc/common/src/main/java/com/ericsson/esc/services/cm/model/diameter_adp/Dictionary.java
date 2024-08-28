
package com.ericsson.esc.services.cm.model.diameter_adp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "specification" })
public class Dictionary
{

    /**
     * Used to specify the key of the dictionary instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the dictionary instance.")
    private String id;
    /**
     * Used to load a diameter dictionary. (Required)
     * 
     */
    @JsonProperty("specification")
    @JsonPropertyDescription("Used to load a diameter dictionary.")
    private String specification;

    /**
     * Used to specify the key of the dictionary instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the dictionary instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public Dictionary withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to load a diameter dictionary. (Required)
     * 
     */
    @JsonProperty("specification")
    public String getSpecification()
    {
        return specification;
    }

    /**
     * Used to load a diameter dictionary. (Required)
     * 
     */
    @JsonProperty("specification")
    public void setSpecification(String specification)
    {
        this.specification = specification;
    }

    public Dictionary withSpecification(String specification)
    {
        this.specification = specification;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Dictionary.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("specification");
        sb.append('=');
        sb.append(((this.specification == null) ? "<null>" : this.specification));
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
        result = ((result * 31) + ((this.specification == null) ? 0 : this.specification.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Dictionary) == false)
        {
            return false;
        }
        Dictionary rhs = ((Dictionary) other);
        return (((this.specification == rhs.specification) || ((this.specification != null) && this.specification.equals(rhs.specification)))
                && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))));
    }

}
