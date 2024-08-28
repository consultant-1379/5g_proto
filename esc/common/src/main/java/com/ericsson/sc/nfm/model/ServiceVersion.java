
package com.ericsson.sc.nfm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "api-version-in-uri", "api-full-version" })
public class ServiceVersion
{

    /**
     * Version of the service instance to be used in the URI for accessing the API
     * (Required)
     * 
     */
    @JsonProperty("api-version-in-uri")
    @JsonPropertyDescription("Version of the service instance to be used in the URI for accessing the API")
    private String apiVersionInUri;
    /**
     * Full version integer of the API (Required)
     * 
     */
    @JsonProperty("api-full-version")
    @JsonPropertyDescription("Full version integer of the API")
    private String apiFullVersion;

    /**
     * Version of the service instance to be used in the URI for accessing the API
     * (Required)
     * 
     */
    @JsonProperty("api-version-in-uri")
    public String getApiVersionInUri()
    {
        return apiVersionInUri;
    }

    /**
     * Version of the service instance to be used in the URI for accessing the API
     * (Required)
     * 
     */
    @JsonProperty("api-version-in-uri")
    public void setApiVersionInUri(String apiVersionInUri)
    {
        this.apiVersionInUri = apiVersionInUri;
    }

    public ServiceVersion withApiVersionInUri(String apiVersionInUri)
    {
        this.apiVersionInUri = apiVersionInUri;
        return this;
    }

    /**
     * Full version integer of the API (Required)
     * 
     */
    @JsonProperty("api-full-version")
    public String getApiFullVersion()
    {
        return apiFullVersion;
    }

    /**
     * Full version integer of the API (Required)
     * 
     */
    @JsonProperty("api-full-version")
    public void setApiFullVersion(String apiFullVersion)
    {
        this.apiFullVersion = apiFullVersion;
    }

    public ServiceVersion withApiFullVersion(String apiFullVersion)
    {
        this.apiFullVersion = apiFullVersion;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ServiceVersion.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("apiVersionInUri");
        sb.append('=');
        sb.append(((this.apiVersionInUri == null) ? "<null>" : this.apiVersionInUri));
        sb.append(',');
        sb.append("apiFullVersion");
        sb.append('=');
        sb.append(((this.apiFullVersion == null) ? "<null>" : this.apiFullVersion));
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
        result = ((result * 31) + ((this.apiFullVersion == null) ? 0 : this.apiFullVersion.hashCode()));
        result = ((result * 31) + ((this.apiVersionInUri == null) ? 0 : this.apiVersionInUri.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ServiceVersion) == false)
        {
            return false;
        }
        ServiceVersion rhs = ((ServiceVersion) other);
        return (((this.apiFullVersion == rhs.apiFullVersion) || ((this.apiFullVersion != null) && this.apiFullVersion.equals(rhs.apiFullVersion)))
                && ((this.apiVersionInUri == rhs.apiVersionInUri) || ((this.apiVersionInUri != null) && this.apiVersionInUri.equals(rhs.apiVersionInUri))));
    }

}
