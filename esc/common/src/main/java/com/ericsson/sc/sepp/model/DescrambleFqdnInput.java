
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
@JsonPropertyOrder({ "ericsson-sepp:scrambled-fqdn", "ericsson-sepp:descrambling-key", "ericsson-sepp:initial-vector" })
public class DescrambleFqdnInput
{

    /**
     * The scrambled FQDN (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:scrambled-fqdn")
    @JsonPropertyDescription("The scrambled FQDN")
    private String ericssonSeppScrambledFqdn;
    /**
     * The key to be used for descrambling the FQDN, given as plain text
     * 
     */
    @JsonProperty("ericsson-sepp:descrambling-key")
    @JsonPropertyDescription("The key to be used for descrambling the FQDN, given as plain text")
    private String ericssonSeppDescramblingKey;
    /**
     * The initial vector to be used in AES GCM algorithm.
     * 
     */
    @JsonProperty("ericsson-sepp:initial-vector")
    @JsonPropertyDescription("The initial vector to be used in AES GCM algorithm.")
    private String ericssonSeppInitialVector;

    /**
     * The scrambled FQDN (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:scrambled-fqdn")
    public String getEricssonSeppScrambledFqdn()
    {
        return ericssonSeppScrambledFqdn;
    }

    /**
     * The scrambled FQDN (Required)
     * 
     */
    @JsonProperty("ericsson-sepp:scrambled-fqdn")
    public void setEricssonSeppScrambledFqdn(String ericssonSeppScrambledFqdn)
    {
        this.ericssonSeppScrambledFqdn = ericssonSeppScrambledFqdn;
    }

    public DescrambleFqdnInput withEricssonSeppScrambledFqdn(String ericssonSeppScrambledFqdn)
    {
        this.ericssonSeppScrambledFqdn = ericssonSeppScrambledFqdn;
        return this;
    }

    /**
     * The key to be used for descrambling the FQDN, given as plain text
     * 
     */
    @JsonProperty("ericsson-sepp:descrambling-key")
    public String getEricssonSeppDescramblingKey()
    {
        return ericssonSeppDescramblingKey;
    }

    /**
     * The key to be used for descrambling the FQDN, given as plain text
     * 
     */
    @JsonProperty("ericsson-sepp:descrambling-key")
    public void setEricssonSeppDescramblingKey(String ericssonSeppDescramblingKey)
    {
        this.ericssonSeppDescramblingKey = ericssonSeppDescramblingKey;
    }

    public DescrambleFqdnInput withEricssonSeppDescramblingKey(String ericssonSeppDescramblingKey)
    {
        this.ericssonSeppDescramblingKey = ericssonSeppDescramblingKey;
        return this;
    }

    /**
     * The initial vector to be used in AES GCM algorithm.
     * 
     */
    @JsonProperty("ericsson-sepp:initial-vector")
    public String getEricssonSeppInitialVector()
    {
        return ericssonSeppInitialVector;
    }

    /**
     * The initial vector to be used in AES GCM algorithm.
     * 
     */
    @JsonProperty("ericsson-sepp:initial-vector")
    public void setEricssonSeppInitialVector(String ericssonSeppInitialVector)
    {
        this.ericssonSeppInitialVector = ericssonSeppInitialVector;
    }

    public DescrambleFqdnInput withEricssonSeppInitialVector(String ericssonSeppInitialVector)
    {
        this.ericssonSeppInitialVector = ericssonSeppInitialVector;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(DescrambleFqdnInput.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("ericssonSeppScrambledFqdn");
        sb.append('=');
        sb.append(((this.ericssonSeppScrambledFqdn == null) ? "<null>" : this.ericssonSeppScrambledFqdn));
        sb.append(',');
        sb.append("ericssonSeppDescramblingKey");
        sb.append('=');
        sb.append(((this.ericssonSeppDescramblingKey == null) ? "<null>" : this.ericssonSeppDescramblingKey));
        sb.append(',');
        sb.append("ericssonSeppInitialVector");
        sb.append('=');
        sb.append(((this.ericssonSeppInitialVector == null) ? "<null>" : this.ericssonSeppInitialVector));
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
        result = ((result * 31) + ((this.ericssonSeppDescramblingKey == null) ? 0 : this.ericssonSeppDescramblingKey.hashCode()));
        result = ((result * 31) + ((this.ericssonSeppScrambledFqdn == null) ? 0 : this.ericssonSeppScrambledFqdn.hashCode()));
        result = ((result * 31) + ((this.ericssonSeppInitialVector == null) ? 0 : this.ericssonSeppInitialVector.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof DescrambleFqdnInput) == false)
        {
            return false;
        }
        DescrambleFqdnInput rhs = ((DescrambleFqdnInput) other);
        return ((((this.ericssonSeppDescramblingKey == rhs.ericssonSeppDescramblingKey)
                  || ((this.ericssonSeppDescramblingKey != null) && this.ericssonSeppDescramblingKey.equals(rhs.ericssonSeppDescramblingKey)))
                 && ((this.ericssonSeppScrambledFqdn == rhs.ericssonSeppScrambledFqdn)
                     || ((this.ericssonSeppScrambledFqdn != null) && this.ericssonSeppScrambledFqdn.equals(rhs.ericssonSeppScrambledFqdn))))
                && ((this.ericssonSeppInitialVector == rhs.ericssonSeppInitialVector)
                    || ((this.ericssonSeppInitialVector != null) && this.ericssonSeppInitialVector.equals(rhs.ericssonSeppInitialVector))));
    }

}
