
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Parameters that configure the handling of telescopic FQDN inside the own
 * network.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "required-for-nf-type", "callback-uri", "callback-uri-defaults" })
public class TelescopicFqdn
{

    /**
     * List of nf-types (according to TS 29.510) such as ‘amf’ or ‘smf’ that require
     * Telescopic-FQDN because they do not support the 3gpp-Sbi-Target-apiRoot
     * header. If the list is empty, no Telescopic-FQDN handling is performed.
     * 
     */
    @JsonProperty("required-for-nf-type")
    @JsonPropertyDescription("List of nf-types (according to TS 29.510) such as \u2018amf\u2019 or \u2018smf\u2019 that require Telescopic-FQDN because they do not support the 3gpp-Sbi-Target-apiRoot header. If the list is empty, no Telescopic-FQDN handling is performed.")
    private List<RequiredForNfType> requiredForNfType = new ArrayList<RequiredForNfType>();
    /**
     * Additions and overrides for locations of callback-URIs in requests that need
     * to be converted to Telescopic-FQDNs. Elements in this list override and add
     * to the pre-defined elements in callback-uri-defaults. If this list has
     * multiple entries for the same api-name and api-version combination, the last
     * one overwrites the previous ones.
     * 
     */
    @JsonProperty("callback-uri")
    @JsonPropertyDescription("Additions and overrides for locations of callback-URIs in requests that need to be converted to Telescopic-FQDNs. Elements in this list override and add to the pre-defined elements in callback-uri-defaults. If this list has multiple entries for the same api-name and api-version combination, the last one overwrites the previous ones.")
    private List<CallbackUrus> callbackUri = new ArrayList<CallbackUrus>();
    /**
     * Locations of callback-URIs in requests that need to be converted to
     * Telescopic-FQDNs. Elements in this list are pre-defined and can be overridden
     * or amended with elements in callback-uri.
     * 
     */
    @JsonProperty("callback-uri-defaults")
    @JsonPropertyDescription("Locations of callback-URIs in requests that need to be converted to Telescopic-FQDNs. Elements in this list are pre-defined and can be overridden or amended with elements in callback-uri.")
    private List<CallbackUriDefault> callbackUriDefaults = new ArrayList<CallbackUriDefault>();

    /**
     * List of nf-types (according to TS 29.510) such as ‘amf’ or ‘smf’ that require
     * Telescopic-FQDN because they do not support the 3gpp-Sbi-Target-apiRoot
     * header. If the list is empty, no Telescopic-FQDN handling is performed.
     * 
     */
    @JsonProperty("required-for-nf-type")
    public List<RequiredForNfType> getRequiredForNfType()
    {
        return requiredForNfType;
    }

    /**
     * List of nf-types (according to TS 29.510) such as ‘amf’ or ‘smf’ that require
     * Telescopic-FQDN because they do not support the 3gpp-Sbi-Target-apiRoot
     * header. If the list is empty, no Telescopic-FQDN handling is performed.
     * 
     */
    @JsonProperty("required-for-nf-type")
    public void setRequiredForNfType(List<RequiredForNfType> requiredForNfType)
    {
        this.requiredForNfType = requiredForNfType;
    }

    public TelescopicFqdn withRequiredForNfType(List<RequiredForNfType> requiredForNfType)
    {
        this.requiredForNfType = requiredForNfType;
        return this;
    }

    /**
     * Additions and overrides for locations of callback-URIs in requests that need
     * to be converted to Telescopic-FQDNs. Elements in this list override and add
     * to the pre-defined elements in callback-uri-defaults. If this list has
     * multiple entries for the same api-name and api-version combination, the last
     * one overwrites the previous ones.
     * 
     */
    @JsonProperty("callback-uri")
    public List<CallbackUrus> getCallbackUri()
    {
        return callbackUri;
    }

    /**
     * Additions and overrides for locations of callback-URIs in requests that need
     * to be converted to Telescopic-FQDNs. Elements in this list override and add
     * to the pre-defined elements in callback-uri-defaults. If this list has
     * multiple entries for the same api-name and api-version combination, the last
     * one overwrites the previous ones.
     * 
     */
    @JsonProperty("callback-uri")
    public void setCallbackUri(List<CallbackUrus> callbackUri)
    {
        this.callbackUri = callbackUri;
    }

    public TelescopicFqdn withCallbackUri(List<CallbackUrus> callbackUri)
    {
        this.callbackUri = callbackUri;
        return this;
    }

    /**
     * Locations of callback-URIs in requests that need to be converted to
     * Telescopic-FQDNs. Elements in this list are pre-defined and can be overridden
     * or amended with elements in callback-uri.
     * 
     */
    @JsonProperty("callback-uri-defaults")
    public List<CallbackUriDefault> getCallbackUriDefaults()
    {
        return callbackUriDefaults;
    }

    /**
     * Locations of callback-URIs in requests that need to be converted to
     * Telescopic-FQDNs. Elements in this list are pre-defined and can be overridden
     * or amended with elements in callback-uri.
     * 
     */
    @JsonProperty("callback-uri-defaults")
    public void setCallbackUriDefaults(List<CallbackUriDefault> callbackUriDefaults)
    {
        this.callbackUriDefaults = callbackUriDefaults;
    }

    public TelescopicFqdn withCallbackUriDefaults(List<CallbackUriDefault> callbackUriDefaults)
    {
        this.callbackUriDefaults = callbackUriDefaults;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(TelescopicFqdn.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("requiredForNfType");
        sb.append('=');
        sb.append(((this.requiredForNfType == null) ? "<null>" : this.requiredForNfType));
        sb.append(',');
        sb.append("callbackUri");
        sb.append('=');
        sb.append(((this.callbackUri == null) ? "<null>" : this.callbackUri));
        sb.append(',');
        sb.append("callbackUriDefaults");
        sb.append('=');
        sb.append(((this.callbackUriDefaults == null) ? "<null>" : this.callbackUriDefaults));
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
        result = ((result * 31) + ((this.callbackUriDefaults == null) ? 0 : this.callbackUriDefaults.hashCode()));
        result = ((result * 31) + ((this.requiredForNfType == null) ? 0 : this.requiredForNfType.hashCode()));
        result = ((result * 31) + ((this.callbackUri == null) ? 0 : this.callbackUri.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof TelescopicFqdn) == false)
        {
            return false;
        }
        TelescopicFqdn rhs = ((TelescopicFqdn) other);
        return ((((this.callbackUriDefaults == rhs.callbackUriDefaults)
                  || ((this.callbackUriDefaults != null) && this.callbackUriDefaults.equals(rhs.callbackUriDefaults)))
                 && ((this.requiredForNfType == rhs.requiredForNfType)
                     || ((this.requiredForNfType != null) && this.requiredForNfType.equals(rhs.requiredForNfType))))
                && ((this.callbackUri == rhs.callbackUri) || ((this.callbackUri != null) && this.callbackUri.equals(rhs.callbackUri))));
    }

}
