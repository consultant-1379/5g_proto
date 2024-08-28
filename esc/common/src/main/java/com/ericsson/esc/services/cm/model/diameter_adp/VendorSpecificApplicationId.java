
package com.ericsson.esc.services.cm.model.diameter_adp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "vendor-id", "auth-application-id", "acct-application-id", "user-label" })
public class VendorSpecificApplicationId
{

    /**
     * The key of the vendor-specific-application-id instance. (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("The key of the vendor-specific-application-id instance.")
    private String id;
    /**
     * Used to indicate the identity of the vendor who might have authorship of the
     * Vendor-Specific Diameter Application. (Required)
     * 
     */
    @JsonProperty("vendor-id")
    @JsonPropertyDescription("Used to indicate the identity of the vendor who might have authorship of the Vendor-Specific Diameter Application.")
    private Long vendorId;
    /**
     * Used to advertise support of the Authentication and Authorization portion of
     * a Vendor-Specific Diameter Application.
     * 
     */
    @JsonProperty("auth-application-id")
    @JsonPropertyDescription("Used to advertise support of the Authentication and Authorization portion of a Vendor-Specific Diameter Application.")
    private Long authApplicationId;
    /**
     * Used in order to advertise support of the Accounting portion of a
     * Vendor-Specific Diameter Application.
     * 
     */
    @JsonProperty("acct-application-id")
    @JsonPropertyDescription("Used in order to advertise support of the Accounting portion of a Vendor-Specific Diameter Application.")
    private Long acctApplicationId;
    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use.")
    private String userLabel;

    /**
     * The key of the vendor-specific-application-id instance. (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * The key of the vendor-specific-application-id instance. (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public VendorSpecificApplicationId withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to indicate the identity of the vendor who might have authorship of the
     * Vendor-Specific Diameter Application. (Required)
     * 
     */
    @JsonProperty("vendor-id")
    public Long getVendorId()
    {
        return vendorId;
    }

    /**
     * Used to indicate the identity of the vendor who might have authorship of the
     * Vendor-Specific Diameter Application. (Required)
     * 
     */
    @JsonProperty("vendor-id")
    public void setVendorId(Long vendorId)
    {
        this.vendorId = vendorId;
    }

    public VendorSpecificApplicationId withVendorId(Long vendorId)
    {
        this.vendorId = vendorId;
        return this;
    }

    /**
     * Used to advertise support of the Authentication and Authorization portion of
     * a Vendor-Specific Diameter Application.
     * 
     */
    @JsonProperty("auth-application-id")
    public Long getAuthApplicationId()
    {
        return authApplicationId;
    }

    /**
     * Used to advertise support of the Authentication and Authorization portion of
     * a Vendor-Specific Diameter Application.
     * 
     */
    @JsonProperty("auth-application-id")
    public void setAuthApplicationId(Long authApplicationId)
    {
        this.authApplicationId = authApplicationId;
    }

    public VendorSpecificApplicationId withAuthApplicationId(Long authApplicationId)
    {
        this.authApplicationId = authApplicationId;
        return this;
    }

    /**
     * Used in order to advertise support of the Accounting portion of a
     * Vendor-Specific Diameter Application.
     * 
     */
    @JsonProperty("acct-application-id")
    public Long getAcctApplicationId()
    {
        return acctApplicationId;
    }

    /**
     * Used in order to advertise support of the Accounting portion of a
     * Vendor-Specific Diameter Application.
     * 
     */
    @JsonProperty("acct-application-id")
    public void setAcctApplicationId(Long acctApplicationId)
    {
        this.acctApplicationId = acctApplicationId;
    }

    public VendorSpecificApplicationId withAcctApplicationId(Long acctApplicationId)
    {
        this.acctApplicationId = acctApplicationId;
        return this;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use.
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public VendorSpecificApplicationId withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(VendorSpecificApplicationId.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("vendorId");
        sb.append('=');
        sb.append(((this.vendorId == null) ? "<null>" : this.vendorId));
        sb.append(',');
        sb.append("authApplicationId");
        sb.append('=');
        sb.append(((this.authApplicationId == null) ? "<null>" : this.authApplicationId));
        sb.append(',');
        sb.append("acctApplicationId");
        sb.append('=');
        sb.append(((this.acctApplicationId == null) ? "<null>" : this.acctApplicationId));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
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
        result = ((result * 31) + ((this.vendorId == null) ? 0 : this.vendorId.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.acctApplicationId == null) ? 0 : this.acctApplicationId.hashCode()));
        result = ((result * 31) + ((this.authApplicationId == null) ? 0 : this.authApplicationId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof VendorSpecificApplicationId) == false)
        {
            return false;
        }
        VendorSpecificApplicationId rhs = ((VendorSpecificApplicationId) other);
        return ((((((this.vendorId == rhs.vendorId) || ((this.vendorId != null) && this.vendorId.equals(rhs.vendorId)))
                   && ((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel))))
                  && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                 && ((this.acctApplicationId == rhs.acctApplicationId)
                     || ((this.acctApplicationId != null) && this.acctApplicationId.equals(rhs.acctApplicationId))))
                && ((this.authApplicationId == rhs.authApplicationId)
                    || ((this.authApplicationId != null) && this.authApplicationId.equals(rhs.authApplicationId))));
    }

}
