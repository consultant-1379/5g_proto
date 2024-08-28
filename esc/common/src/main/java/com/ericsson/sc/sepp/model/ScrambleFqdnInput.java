
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Contains the action input parameters from Yang model.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ericsson-sepp:fqdn", "ericsson-sepp:key-id", "ericsson-sepp:roaming-partner-ref", "ericsson-sepp:key-plain-text" })
public class ScrambleFqdnInput
{

    /**
     * The original FQDN to be scrambled (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:fqdn")
    @JsonPropertyDescription("The original FQDN to be scrambled")
    private String ericssonSeppFqdn;
    /**
     * The encryption identifier of the configured key and initial vector to be used
     * for scrambling the FQDN
     * 
     */
    @JsonProperty("ericsson-sepp:key-id")
    @JsonPropertyDescription("The encryption identifier of the configured key and initial vector to be used for scrambling the FQDN")
    private String ericssonSeppKeyId;
    /**
     * The roaming partner which the FQDN is scrambled for. The active key and the
     * initial vector of the given roaming-partner shall be used for the scrambling
     * process
     * 
     */
    @JsonProperty("ericsson-sepp:roaming-partner-ref")
    @JsonPropertyDescription("The roaming partner which the FQDN is scrambled for. The active key and the initial vector of the given roaming-partner shall be used for the scrambling process")
    private String ericssonSeppRoamingPartnerRef;
    /**
     * The key and the corresponding encryption identifier and initial vector to be
     * used for scrambling the given FQDN
     * 
     */
    @JsonProperty("ericsson-sepp:key-plain-text")
    @JsonPropertyDescription("The key and the corresponding encryption identifier and initial vector to be used for scrambling the given FQDN")
    private EricssonSeppKeyPlainText ericssonSeppKeyPlainText;

    /**
     * The original FQDN to be scrambled (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:fqdn")
    public String getEricssonSeppFqdn()
    {
        return ericssonSeppFqdn;
    }

    /**
     * The original FQDN to be scrambled (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:fqdn")
    public void setEricssonSeppFqdn(String ericssonSeppFqdn)
    {
        this.ericssonSeppFqdn = ericssonSeppFqdn;
    }

    public ScrambleFqdnInput withEricssonSeppFqdn(String ericssonSeppFqdn)
    {
        this.ericssonSeppFqdn = ericssonSeppFqdn;
        return this;
    }

    /**
     * The encryption identifier of the configured key and initial vector to be used
     * for scrambling the FQDN
     * 
     */
    @JsonProperty("ericsson-sepp:key-id")
    public String getEricssonSeppKeyId()
    {
        return ericssonSeppKeyId;
    }

    /**
     * The encryption identifier of the configured key and initial vector to be used
     * for scrambling the FQDN
     * 
     */
    @JsonProperty("ericsson-sepp:key-id")
    public void setEricssonSeppKeyId(String ericssonSeppKeyId)
    {
        this.ericssonSeppKeyId = ericssonSeppKeyId;
    }

    public ScrambleFqdnInput withEricssonSeppKeyId(String ericssonSeppKeyId)
    {
        this.ericssonSeppKeyId = ericssonSeppKeyId;
        return this;
    }

    /**
     * The roaming partner which the FQDN is scrambled for. The active key and the
     * initial vector of the given roaming-partner shall be used for the scrambling
     * process
     * 
     */
    @JsonProperty("ericsson-sepp:roaming-partner-ref")
    public String getEricssonSeppRoamingPartnerRef()
    {
        return ericssonSeppRoamingPartnerRef;
    }

    /**
     * The roaming partner which the FQDN is scrambled for. The active key and the
     * initial vector of the given roaming-partner shall be used for the scrambling
     * process
     * 
     */
    @JsonProperty("ericsson-sepp:roaming-partner-ref")
    public void setEricssonSeppRoamingPartnerRef(String ericssonSeppRoamingPartnerRef)
    {
        this.ericssonSeppRoamingPartnerRef = ericssonSeppRoamingPartnerRef;
    }

    public ScrambleFqdnInput withEricssonSeppRoamingPartnerRef(String ericssonSeppRoamingPartnerRef)
    {
        this.ericssonSeppRoamingPartnerRef = ericssonSeppRoamingPartnerRef;
        return this;
    }

    /**
     * The key and the corresponding encryption identifier and initial vector to be
     * used for scrambling the given FQDN
     * 
     */
    @JsonProperty("ericsson-sepp:key-plain-text")
    public EricssonSeppKeyPlainText getEricssonSeppKeyPlainText()
    {
        return ericssonSeppKeyPlainText;
    }

    /**
     * The key and the corresponding encryption identifier and initial vector to be
     * used for scrambling the given FQDN
     * 
     */
    @JsonProperty("ericsson-sepp:key-plain-text")
    public void setEricssonSeppKeyPlainText(EricssonSeppKeyPlainText ericssonSeppKeyPlainText)
    {
        this.ericssonSeppKeyPlainText = ericssonSeppKeyPlainText;
    }

    public ScrambleFqdnInput withEricssonSeppKeyPlainText(EricssonSeppKeyPlainText ericssonSeppKeyPlainText)
    {
        this.ericssonSeppKeyPlainText = ericssonSeppKeyPlainText;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ScrambleFqdnInput.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ericssonSeppFqdn");
        sb.append('=');
        sb.append(((this.ericssonSeppFqdn == null) ? "<null>" : this.ericssonSeppFqdn));
        sb.append(',');
        sb.append("ericssonSeppKeyId");
        sb.append('=');
        sb.append(((this.ericssonSeppKeyId == null) ? "<null>" : this.ericssonSeppKeyId));
        sb.append(',');
        sb.append("ericssonSeppRoamingPartnerRef");
        sb.append('=');
        sb.append(((this.ericssonSeppRoamingPartnerRef == null) ? "<null>" : this.ericssonSeppRoamingPartnerRef));
        sb.append(',');
        sb.append("ericssonSeppKeyPlainText");
        sb.append('=');
        sb.append(((this.ericssonSeppKeyPlainText == null) ? "<null>" : this.ericssonSeppKeyPlainText));
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
        result = ((result * 31) + ((this.ericssonSeppFqdn == null) ? 0 : this.ericssonSeppFqdn.hashCode()));
        result = ((result * 31) + ((this.ericssonSeppRoamingPartnerRef == null) ? 0 : this.ericssonSeppRoamingPartnerRef.hashCode()));
        result = ((result * 31) + ((this.ericssonSeppKeyPlainText == null) ? 0 : this.ericssonSeppKeyPlainText.hashCode()));
        result = ((result * 31) + ((this.ericssonSeppKeyId == null) ? 0 : this.ericssonSeppKeyId.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ScrambleFqdnInput) == false)
        {
            return false;
        }
        ScrambleFqdnInput rhs = ((ScrambleFqdnInput) other);
        return (((((this.ericssonSeppFqdn == rhs.ericssonSeppFqdn) || ((this.ericssonSeppFqdn != null) && this.ericssonSeppFqdn.equals(rhs.ericssonSeppFqdn)))
                  && ((this.ericssonSeppRoamingPartnerRef == rhs.ericssonSeppRoamingPartnerRef)
                      || ((this.ericssonSeppRoamingPartnerRef != null) && this.ericssonSeppRoamingPartnerRef.equals(rhs.ericssonSeppRoamingPartnerRef))))
                 && ((this.ericssonSeppKeyPlainText == rhs.ericssonSeppKeyPlainText)
                     || ((this.ericssonSeppKeyPlainText != null) && this.ericssonSeppKeyPlainText.equals(rhs.ericssonSeppKeyPlainText))))
                && ((this.ericssonSeppKeyId == rhs.ericssonSeppKeyId)
                    || ((this.ericssonSeppKeyId != null) && this.ericssonSeppKeyId.equals(rhs.ericssonSeppKeyId))));
    }

}
