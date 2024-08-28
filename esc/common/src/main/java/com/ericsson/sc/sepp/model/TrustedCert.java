
package com.ericsson.sc.sepp.model;

import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "trusted-cert-list-ref" })
public class TrustedCert implements IfNamedListItem
{

    /**
     * The name of the reference to the trusted-certificate-list installed in
     * ietf-trustore. (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("The name of the reference to the trusted-certificate-list installed in ietf-trustore.")
    private String name;
    /**
     * The reference to a trusted certificate list in the truststore.
     * 
     */
    @JsonProperty("trusted-cert-list-ref")
    @JsonPropertyDescription("The reference to a trusted certificate list in the truststore.")
    private String trustedCertListRef;

    /**
     * The name of the reference to the trusted-certificate-list installed in
     * ietf-trustore. (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * The name of the reference to the trusted-certificate-list installed in
     * ietf-trustore. (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public TrustedCert withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The reference to a trusted certificate list in the truststore.
     * 
     */
    @JsonProperty("trusted-cert-list-ref")
    public String getTrustedCertListRef()
    {
        return trustedCertListRef;
    }

    /**
     * The reference to a trusted certificate list in the truststore.
     * 
     */
    @JsonProperty("trusted-cert-list-ref")
    public void setTrustedCertListRef(String trustedCertListRef)
    {
        this.trustedCertListRef = trustedCertListRef;
    }

    public TrustedCert withTrustedCertListRef(String trustedCertListRef)
    {
        this.trustedCertListRef = trustedCertListRef;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TrustedCert.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("trustedCertListRef");
        sb.append('=');
        sb.append(((this.trustedCertListRef == null) ? "<null>" : this.trustedCertListRef));
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
        result = ((result * 31) + ((this.trustedCertListRef == null) ? 0 : this.trustedCertListRef.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TrustedCert) == false)
        {
            return false;
        }
        TrustedCert rhs = ((TrustedCert) other);
        return (((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name)))
                && ((this.trustedCertListRef == rhs.trustedCertListRef)
                    || ((this.trustedCertListRef != null) && this.trustedCertListRef.equals(rhs.trustedCertListRef))));
    }

}
