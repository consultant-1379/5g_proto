
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Output schema for action
 * ericsson-sepp:sepp-function::nf-instance::fqdn-scrambling-command::descramble-fqdn
 * <p>
 * Generated by jsonschema-generator (1.28.0-5 2023-07-07 11:30:07 CEST) from
 * Yang module ericsson-sepp (3.3.16) on Oct 12, 2023, 10:41:35 AM. Features:
 * []. Deviations/extensions: []
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "ericsson-sepp:fqdn", "ericsson-sepp:key-id" })
public class EricssonSeppSeppFunctionNfInstanceFqdnScramblingCommandDescrambleFqdnOutput
{

    /**
     * The original value of the FQDN after descrambling (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:fqdn")
    @JsonPropertyDescription("The original value of the FQDN after descrambling")
    private String ericssonSeppFqdn;
    /**
     * The encryption identifier of the key used for descrambling the FQDN
     * (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:key-id")
    @JsonPropertyDescription("The encryption identifier of the key used for descrambling the FQDN")
    private String ericssonSeppKeyId;

    /**
     * The original value of the FQDN after descrambling (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:fqdn")
    public String getEricssonSeppFqdn()
    {
        return ericssonSeppFqdn;
    }

    /**
     * The original value of the FQDN after descrambling (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:fqdn")
    public void setEricssonSeppFqdn(String ericssonSeppFqdn)
    {
        this.ericssonSeppFqdn = ericssonSeppFqdn;
    }

    public EricssonSeppSeppFunctionNfInstanceFqdnScramblingCommandDescrambleFqdnOutput withEricssonSeppFqdn(String ericssonSeppFqdn)
    {
        this.ericssonSeppFqdn = ericssonSeppFqdn;
        return this;
    }

    /**
     * The encryption identifier of the key used for descrambling the FQDN
     * (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:key-id")
    public String getEricssonSeppKeyId()
    {
        return ericssonSeppKeyId;
    }

    /**
     * The encryption identifier of the key used for descrambling the FQDN
     * (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:key-id")
    public void setEricssonSeppKeyId(String ericssonSeppKeyId)
    {
        this.ericssonSeppKeyId = ericssonSeppKeyId;
    }

    public EricssonSeppSeppFunctionNfInstanceFqdnScramblingCommandDescrambleFqdnOutput withEricssonSeppKeyId(String ericssonSeppKeyId)
    {
        this.ericssonSeppKeyId = ericssonSeppKeyId;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(EricssonSeppSeppFunctionNfInstanceFqdnScramblingCommandDescrambleFqdnOutput.class.getName())
          .append('@')
          .append(Integer.toHexString(System.identityHashCode(this)))
          .append('[');
        sb.append("ericssonSeppFqdn");
        sb.append('=');
        sb.append(((this.ericssonSeppFqdn == null) ? "<null>" : this.ericssonSeppFqdn));
        sb.append(',');
        sb.append("ericssonSeppKeyId");
        sb.append('=');
        sb.append(((this.ericssonSeppKeyId == null) ? "<null>" : this.ericssonSeppKeyId));
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
        result = ((result * 31) + ((this.ericssonSeppKeyId == null) ? 0 : this.ericssonSeppKeyId.hashCode()));
        result = ((result * 31) + ((this.ericssonSeppFqdn == null) ? 0 : this.ericssonSeppFqdn.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof EricssonSeppSeppFunctionNfInstanceFqdnScramblingCommandDescrambleFqdnOutput) == false)
        {
            return false;
        }
        EricssonSeppSeppFunctionNfInstanceFqdnScramblingCommandDescrambleFqdnOutput rhs = ((EricssonSeppSeppFunctionNfInstanceFqdnScramblingCommandDescrambleFqdnOutput) other);
        return (((this.ericssonSeppKeyId == rhs.ericssonSeppKeyId)
                 || ((this.ericssonSeppKeyId != null) && this.ericssonSeppKeyId.equals(rhs.ericssonSeppKeyId)))
                && ((this.ericssonSeppFqdn == rhs.ericssonSeppFqdn)
                    || ((this.ericssonSeppFqdn != null) && this.ericssonSeppFqdn.equals(rhs.ericssonSeppFqdn))));
    }

}
