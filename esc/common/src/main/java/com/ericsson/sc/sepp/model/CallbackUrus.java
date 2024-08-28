
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "api-name", "api-version", "callback-uri-json-pointer" })
public class CallbackUrus
{

    /**
     * The api-name, together with the api-version, are the key to look up the
     * callback-uri-json-pointer that is used to convert a callback URI. (Required)
     * 
     */
    @JsonProperty("api-name")
    @JsonPropertyDescription("The api-name, together with the api-version, are the key to look up the callback-uri-json-pointer that is used to convert a callback URI.")
    private String apiName;
    /**
     * The api-version, together with the api-name, are the key to look up the
     * callback-uri-json-pointer that is used to convert a callback URI. (Required)
     * 
     */
    @JsonProperty("api-version")
    @JsonPropertyDescription("The api-version, together with the api-name, are the key to look up the callback-uri-json-pointer that is used to convert a callback URI.")
    private Integer apiVersion;
    /**
     * A list of extended JSON-pointers to find callback-URIs in the body of the
     * request. The FQDNs in the found callback-URIs are converted to Telescopic
     * FQDNs. If the list contains more than one JSON-pointer, all of them are
     * searched and converted. If a JSON-pointer refers to a non-existing element,
     * it is ignored. If this list contains a single JSON-pointer that is the empty
     * string, it effectively disables the corresponding element in the
     * callback-uri-defaults with the same api-name and api-version. (Required)
     * 
     */
    @JsonProperty("callback-uri-json-pointer")
    @JsonPropertyDescription("A list of extended JSON-pointers to find callback-URIs in the body of the request. The FQDNs in the found callback-URIs are converted to Telescopic FQDNs. If the list contains more than one JSON-pointer, all of them are searched and converted. If a JSON-pointer refers to a non-existing element, it is ignored. If this list contains a single JSON-pointer that is the empty string, it effectively disables the corresponding element in the callback-uri-defaults with the same api-name and api-version.")
    private List<String> callbackUriJsonPointer = new ArrayList<String>();

    /**
     * The api-name, together with the api-version, are the key to look up the
     * callback-uri-json-pointer that is used to convert a callback URI. (Required)
     * 
     */
    @JsonProperty("api-name")
    public String getApiName()
    {
        return apiName;
    }

    /**
     * The api-name, together with the api-version, are the key to look up the
     * callback-uri-json-pointer that is used to convert a callback URI. (Required)
     * 
     */
    @JsonProperty("api-name")
    public void setApiName(String apiName)
    {
        this.apiName = apiName;
    }

    public CallbackUrus withApiName(String apiName)
    {
        this.apiName = apiName;
        return this;
    }

    /**
     * The api-version, together with the api-name, are the key to look up the
     * callback-uri-json-pointer that is used to convert a callback URI. (Required)
     * 
     */
    @JsonProperty("api-version")
    public Integer getApiVersion()
    {
        return apiVersion;
    }

    /**
     * The api-version, together with the api-name, are the key to look up the
     * callback-uri-json-pointer that is used to convert a callback URI. (Required)
     * 
     */
    @JsonProperty("api-version")
    public void setApiVersion(Integer apiVersion)
    {
        this.apiVersion = apiVersion;
    }

    public CallbackUrus withApiVersion(Integer apiVersion)
    {
        this.apiVersion = apiVersion;
        return this;
    }

    /**
     * A list of extended JSON-pointers to find callback-URIs in the body of the
     * request. The FQDNs in the found callback-URIs are converted to Telescopic
     * FQDNs. If the list contains more than one JSON-pointer, all of them are
     * searched and converted. If a JSON-pointer refers to a non-existing element,
     * it is ignored. If this list contains a single JSON-pointer that is the empty
     * string, it effectively disables the corresponding element in the
     * callback-uri-defaults with the same api-name and api-version. (Required)
     * 
     */
    @JsonProperty("callback-uri-json-pointer")
    public List<String> getCallbackUriJsonPointer()
    {
        return callbackUriJsonPointer;
    }

    /**
     * A list of extended JSON-pointers to find callback-URIs in the body of the
     * request. The FQDNs in the found callback-URIs are converted to Telescopic
     * FQDNs. If the list contains more than one JSON-pointer, all of them are
     * searched and converted. If a JSON-pointer refers to a non-existing element,
     * it is ignored. If this list contains a single JSON-pointer that is the empty
     * string, it effectively disables the corresponding element in the
     * callback-uri-defaults with the same api-name and api-version. (Required)
     * 
     */
    @JsonProperty("callback-uri-json-pointer")
    public void setCallbackUriJsonPointer(List<String> callbackUriJsonPointer)
    {
        this.callbackUriJsonPointer = callbackUriJsonPointer;
    }

    public CallbackUrus withCallbackUriJsonPointer(List<String> callbackUriJsonPointer)
    {
        this.callbackUriJsonPointer = callbackUriJsonPointer;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(CallbackUrus.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("apiName");
        sb.append('=');
        sb.append(((this.apiName == null) ? "<null>" : this.apiName));
        sb.append(',');
        sb.append("apiVersion");
        sb.append('=');
        sb.append(((this.apiVersion == null) ? "<null>" : this.apiVersion));
        sb.append(',');
        sb.append("callbackUriJsonPointer");
        sb.append('=');
        sb.append(((this.callbackUriJsonPointer == null) ? "<null>" : this.callbackUriJsonPointer));
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
        result = ((result * 31) + ((this.apiName == null) ? 0 : this.apiName.hashCode()));
        result = ((result * 31) + ((this.apiVersion == null) ? 0 : this.apiVersion.hashCode()));
        result = ((result * 31) + ((this.callbackUriJsonPointer == null) ? 0 : this.callbackUriJsonPointer.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof CallbackUrus) == false)
        {
            return false;
        }
        CallbackUrus rhs = ((CallbackUrus) other);
        return ((((this.apiName == rhs.apiName) || ((this.apiName != null) && this.apiName.equals(rhs.apiName)))
                 && ((this.apiVersion == rhs.apiVersion) || ((this.apiVersion != null) && this.apiVersion.equals(rhs.apiVersion))))
                && ((this.callbackUriJsonPointer == rhs.callbackUriJsonPointer)
                    || ((this.callbackUriJsonPointer != null) && this.callbackUriJsonPointer.equals(rhs.callbackUriJsonPointer))));
    }

}
