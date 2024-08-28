
package com.ericsson.esc.services.cm.model.diameter_adp;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "auth-application-id", "acct-application-id", "supported-vendor-id", "vendor-specific-application-id", "dictionary" })
public class Application
{

    /**
     * Used to specify the key of the applications (Required)
     * 
     */
    @JsonProperty("id")
    @JsonPropertyDescription("Used to specify the key of the applications")
    private String id;
    /**
     * Used to advertise support of the Authentication and Authorization portion of
     * a Diameter Application.
     * 
     */
    @JsonProperty("auth-application-id")
    @JsonPropertyDescription("Used to advertise support of the Authentication and Authorization portion of a Diameter Application.")
    private List<Long> authApplicationId = new ArrayList<Long>();
    /**
     * Used to advertise support of the Accounting portion of a Diameter
     * Application.
     * 
     */
    @JsonProperty("acct-application-id")
    @JsonPropertyDescription("Used to advertise support of the Accounting portion of a Diameter Application.")
    private List<Long> acctApplicationId = new ArrayList<Long>();
    /**
     * Used to advertise support for AVPs defined by vendors other than the device
     * vendor but including the application vendor.
     * 
     */
    @JsonProperty("supported-vendor-id")
    @JsonPropertyDescription("Used to advertise support for AVPs defined by vendors other than the device vendor but including the application vendor.")
    private List<Long> supportedVendorId = new ArrayList<Long>();
    /**
     * Used to advertise support of one or more Vendor-Specific Diameter
     * Applications represented by related vendor-specific-application-id instances.
     * 
     */
    @JsonProperty("vendor-specific-application-id")
    @JsonPropertyDescription("Used to advertise support of one or more Vendor-Specific Diameter Applications represented by related vendor-specific-application-id instances.")
    private List<String> vendorSpecificApplicationId = new ArrayList<String>();
    /**
     * Used to refer to the Diameter Application Specifications of the Diameter
     * Applications advertised by the applications instance. A Diameter Application
     * Specification is a dictionary holding the grammar of the diameter messages
     * used by a Diameter Application. These dictionaries are stored in related
     * dictionary instances. (Required)
     * 
     */
    @JsonProperty("dictionary")
    @JsonPropertyDescription("Used to refer to the Diameter Application Specifications of the Diameter Applications advertised by the applications instance. A Diameter Application Specification is a dictionary holding the grammar of the diameter messages used by a Diameter Application. These dictionaries are stored in related dictionary instances.")
    private List<String> dictionary = new ArrayList<String>();

    /**
     * Used to specify the key of the applications (Required)
     * 
     */
    @JsonProperty("id")
    public String getId()
    {
        return id;
    }

    /**
     * Used to specify the key of the applications (Required)
     * 
     */
    @JsonProperty("id")
    public void setId(String id)
    {
        this.id = id;
    }

    public Application withId(String id)
    {
        this.id = id;
        return this;
    }

    /**
     * Used to advertise support of the Authentication and Authorization portion of
     * a Diameter Application.
     * 
     */
    @JsonProperty("auth-application-id")
    public List<Long> getAuthApplicationId()
    {
        return authApplicationId;
    }

    /**
     * Used to advertise support of the Authentication and Authorization portion of
     * a Diameter Application.
     * 
     */
    @JsonProperty("auth-application-id")
    public void setAuthApplicationId(List<Long> authApplicationId)
    {
        this.authApplicationId = authApplicationId;
    }

    public Application withAuthApplicationId(List<Long> authApplicationId)
    {
        this.authApplicationId = authApplicationId;
        return this;
    }

    /**
     * Used to advertise support of the Accounting portion of a Diameter
     * Application.
     * 
     */
    @JsonProperty("acct-application-id")
    public List<Long> getAcctApplicationId()
    {
        return acctApplicationId;
    }

    /**
     * Used to advertise support of the Accounting portion of a Diameter
     * Application.
     * 
     */
    @JsonProperty("acct-application-id")
    public void setAcctApplicationId(List<Long> acctApplicationId)
    {
        this.acctApplicationId = acctApplicationId;
    }

    public Application withAcctApplicationId(List<Long> acctApplicationId)
    {
        this.acctApplicationId = acctApplicationId;
        return this;
    }

    /**
     * Used to advertise support for AVPs defined by vendors other than the device
     * vendor but including the application vendor.
     * 
     */
    @JsonProperty("supported-vendor-id")
    public List<Long> getSupportedVendorId()
    {
        return supportedVendorId;
    }

    /**
     * Used to advertise support for AVPs defined by vendors other than the device
     * vendor but including the application vendor.
     * 
     */
    @JsonProperty("supported-vendor-id")
    public void setSupportedVendorId(List<Long> supportedVendorId)
    {
        this.supportedVendorId = supportedVendorId;
    }

    public Application withSupportedVendorId(List<Long> supportedVendorId)
    {
        this.supportedVendorId = supportedVendorId;
        return this;
    }

    /**
     * Used to advertise support of one or more Vendor-Specific Diameter
     * Applications represented by related vendor-specific-application-id instances.
     * 
     */
    @JsonProperty("vendor-specific-application-id")
    public List<String> getVendorSpecificApplicationId()
    {
        return vendorSpecificApplicationId;
    }

    /**
     * Used to advertise support of one or more Vendor-Specific Diameter
     * Applications represented by related vendor-specific-application-id instances.
     * 
     */
    @JsonProperty("vendor-specific-application-id")
    public void setVendorSpecificApplicationId(List<String> vendorSpecificApplicationId)
    {
        this.vendorSpecificApplicationId = vendorSpecificApplicationId;
    }

    public Application withVendorSpecificApplicationId(List<String> vendorSpecificApplicationId)
    {
        this.vendorSpecificApplicationId = vendorSpecificApplicationId;
        return this;
    }

    /**
     * Used to refer to the Diameter Application Specifications of the Diameter
     * Applications advertised by the applications instance. A Diameter Application
     * Specification is a dictionary holding the grammar of the diameter messages
     * used by a Diameter Application. These dictionaries are stored in related
     * dictionary instances. (Required)
     * 
     */
    @JsonProperty("dictionary")
    public List<String> getDictionary()
    {
        return dictionary;
    }

    /**
     * Used to refer to the Diameter Application Specifications of the Diameter
     * Applications advertised by the applications instance. A Diameter Application
     * Specification is a dictionary holding the grammar of the diameter messages
     * used by a Diameter Application. These dictionaries are stored in related
     * dictionary instances. (Required)
     * 
     */
    @JsonProperty("dictionary")
    public void setDictionary(List<String> dictionary)
    {
        this.dictionary = dictionary;
    }

    public Application withDictionary(List<String> dictionary)
    {
        this.dictionary = dictionary;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Application.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("id");
        sb.append('=');
        sb.append(((this.id == null) ? "<null>" : this.id));
        sb.append(',');
        sb.append("authApplicationId");
        sb.append('=');
        sb.append(((this.authApplicationId == null) ? "<null>" : this.authApplicationId));
        sb.append(',');
        sb.append("acctApplicationId");
        sb.append('=');
        sb.append(((this.acctApplicationId == null) ? "<null>" : this.acctApplicationId));
        sb.append(',');
        sb.append("supportedVendorId");
        sb.append('=');
        sb.append(((this.supportedVendorId == null) ? "<null>" : this.supportedVendorId));
        sb.append(',');
        sb.append("vendorSpecificApplicationId");
        sb.append('=');
        sb.append(((this.vendorSpecificApplicationId == null) ? "<null>" : this.vendorSpecificApplicationId));
        sb.append(',');
        sb.append("dictionary");
        sb.append('=');
        sb.append(((this.dictionary == null) ? "<null>" : this.dictionary));
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
        result = ((result * 31) + ((this.dictionary == null) ? 0 : this.dictionary.hashCode()));
        result = ((result * 31) + ((this.vendorSpecificApplicationId == null) ? 0 : this.vendorSpecificApplicationId.hashCode()));
        result = ((result * 31) + ((this.authApplicationId == null) ? 0 : this.authApplicationId.hashCode()));
        result = ((result * 31) + ((this.supportedVendorId == null) ? 0 : this.supportedVendorId.hashCode()));
        result = ((result * 31) + ((this.id == null) ? 0 : this.id.hashCode()));
        result = ((result * 31) + ((this.acctApplicationId == null) ? 0 : this.acctApplicationId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Application) == false)
        {
            return false;
        }
        Application rhs = ((Application) other);
        return (((((((this.dictionary == rhs.dictionary) || ((this.dictionary != null) && this.dictionary.equals(rhs.dictionary)))
                    && ((this.vendorSpecificApplicationId == rhs.vendorSpecificApplicationId)
                        || ((this.vendorSpecificApplicationId != null) && this.vendorSpecificApplicationId.equals(rhs.vendorSpecificApplicationId))))
                   && ((this.authApplicationId == rhs.authApplicationId)
                       || ((this.authApplicationId != null) && this.authApplicationId.equals(rhs.authApplicationId))))
                  && ((this.supportedVendorId == rhs.supportedVendorId)
                      || ((this.supportedVendorId != null) && this.supportedVendorId.equals(rhs.supportedVendorId))))
                 && ((this.id == rhs.id) || ((this.id != null) && this.id.equals(rhs.id))))
                && ((this.acctApplicationId == rhs.acctApplicationId)
                    || ((this.acctApplicationId != null) && this.acctApplicationId.equals(rhs.acctApplicationId))));
    }

}
