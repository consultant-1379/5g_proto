
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "static-nf-instance-ref", "static-sepp-instance-ref" })
public class SeppNameRef
{

    /**
     * Reference to the responding SEPP name
     * 
     */
    @JsonProperty("static-nf-instance-ref")
    @JsonPropertyDescription("Reference to the responding SEPP name")
    private String staticNfInstanceRef;
    /**
     * Reference to the responding SEPP name
     * 
     */
    @JsonProperty("static-sepp-instance-ref")
    @JsonPropertyDescription("Reference to the responding SEPP name")
    private String staticSeppInstanceRef;

    /**
     * Reference to the responding SEPP name
     * 
     */
    @JsonProperty("static-nf-instance-ref")
    public String getStaticNfInstanceRef()
    {
        return staticNfInstanceRef;
    }

    /**
     * Reference to the responding SEPP name
     * 
     */
    @JsonProperty("static-nf-instance-ref")
    public void setStaticNfInstanceRef(String staticNfInstanceRef)
    {
        this.staticNfInstanceRef = staticNfInstanceRef;
    }

    public SeppNameRef withStaticNfInstanceRef(String staticNfInstanceRef)
    {
        this.staticNfInstanceRef = staticNfInstanceRef;
        return this;
    }

    /**
     * Reference to the responding SEPP name
     * 
     */
    @JsonProperty("static-sepp-instance-ref")
    public String getStaticSeppInstanceRef()
    {
        return staticSeppInstanceRef;
    }

    /**
     * Reference to the responding SEPP name
     * 
     */
    @JsonProperty("static-sepp-instance-ref")
    public void setStaticSeppInstanceRef(String staticSeppInstanceRef)
    {
        this.staticSeppInstanceRef = staticSeppInstanceRef;
    }

    public SeppNameRef withStaticSeppInstanceRef(String staticSeppInstanceRef)
    {
        this.staticSeppInstanceRef = staticSeppInstanceRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(SeppNameRef.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("staticNfInstanceRef");
        sb.append('=');
        sb.append(((this.staticNfInstanceRef == null) ? "<null>" : this.staticNfInstanceRef));
        sb.append(',');
        sb.append("staticSeppInstanceRef");
        sb.append('=');
        sb.append(((this.staticSeppInstanceRef == null) ? "<null>" : this.staticSeppInstanceRef));
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
        result = ((result * 31) + ((this.staticNfInstanceRef == null) ? 0 : this.staticNfInstanceRef.hashCode()));
        result = ((result * 31) + ((this.staticSeppInstanceRef == null) ? 0 : this.staticSeppInstanceRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof SeppNameRef) == false)
        {
            return false;
        }
        SeppNameRef rhs = ((SeppNameRef) other);
        return (((this.staticNfInstanceRef == rhs.staticNfInstanceRef)
                 || ((this.staticNfInstanceRef != null) && this.staticNfInstanceRef.equals(rhs.staticNfInstanceRef)))
                && ((this.staticSeppInstanceRef == rhs.staticSeppInstanceRef)
                    || ((this.staticSeppInstanceRef != null) && this.staticSeppInstanceRef.equals(rhs.staticSeppInstanceRef))));
    }

}
