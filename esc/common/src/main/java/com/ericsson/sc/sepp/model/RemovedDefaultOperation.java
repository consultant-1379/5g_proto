
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.ericsson.sc.nfm.model.HttpMethod;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "service-name", "service-version", "notification-message", "http-method", "resource" })
public class RemovedDefaultOperation
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * The name of the service.
     * 
     */
    @JsonProperty("service-name")
    @JsonPropertyDescription("The name of the service.")
    private List<String> serviceName = new ArrayList<String>();
    /**
     * The version of the service specified by 'v' and a integer, ex. 'v1', 'v2'
     * etc. If left empty, serves as a wildcard.
     * 
     */
    @JsonProperty("service-version")
    @JsonPropertyDescription("The version of the service specified by 'v' and a integer, ex. 'v1', 'v2' etc. If left empty, serves as a wildcard.")
    private List<String> serviceVersion = new ArrayList<String>();
    /**
     * Set to 'true' when referring to Notification messages.
     * 
     */
    @JsonProperty("notification-message")
    @JsonPropertyDescription("Set to 'true' when referring to Notification messages.")
    private Boolean notificationMessage = false;
    /**
     * The HTTP method. If left empty, serves as a wildcard.
     * 
     */
    @JsonProperty("http-method")
    @JsonPropertyDescription("The HTTP method. If left empty, serves as a wildcard.")
    private List<HttpMethod> httpMethod = new ArrayList<HttpMethod>();
    /**
     * The explicit part in the path between two slashes '/' which uniquely
     * identifies a message. It is interpreted as a regex. If left empty, it matches
     * all. For example, setting 'sm-contexts' in resource attribute will match
     * against the messages with
     * {apiRoot}/nsmf-pdusession/<apiVersion>/sm-contexts/{smContextRef} path.
     * 
     */
    @JsonProperty("resource")
    @JsonPropertyDescription("The explicit part in the path between two slashes '/' which uniquely identifies a message. It is interpreted as a regex. If left empty, it matches all. For example, setting 'sm-contexts' in resource attribute will match against the messages with {apiRoot}/nsmf-pdusession/<apiVersion>/sm-contexts/{smContextRef} path.")
    private List<String> resource = new ArrayList<String>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public RemovedDefaultOperation withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * The name of the service.
     * 
     */
    @JsonProperty("service-name")
    public List<String> getServiceName()
    {
        return serviceName;
    }

    /**
     * The name of the service.
     * 
     */
    @JsonProperty("service-name")
    public void setServiceName(List<String> serviceName)
    {
        this.serviceName = serviceName;
    }

    public RemovedDefaultOperation withServiceName(List<String> serviceName)
    {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * The version of the service specified by 'v' and a integer, ex. 'v1', 'v2'
     * etc. If left empty, serves as a wildcard.
     * 
     */
    @JsonProperty("service-version")
    public List<String> getServiceVersion()
    {
        return serviceVersion;
    }

    /**
     * The version of the service specified by 'v' and a integer, ex. 'v1', 'v2'
     * etc. If left empty, serves as a wildcard.
     * 
     */
    @JsonProperty("service-version")
    public void setServiceVersion(List<String> serviceVersion)
    {
        this.serviceVersion = serviceVersion;
    }

    public RemovedDefaultOperation withServiceVersion(List<String> serviceVersion)
    {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Set to 'true' when referring to Notification messages.
     * 
     */
    @JsonProperty("notification-message")
    public Boolean getNotificationMessage()
    {
        return notificationMessage;
    }

    /**
     * Set to 'true' when referring to Notification messages.
     * 
     */
    @JsonProperty("notification-message")
    public void setNotificationMessage(Boolean notificationMessage)
    {
        this.notificationMessage = notificationMessage;
    }

    public RemovedDefaultOperation withNotificationMessage(Boolean notificationMessage)
    {
        this.notificationMessage = notificationMessage;
        return this;
    }

    /**
     * The HTTP method. If left empty, serves as a wildcard.
     * 
     */
    @JsonProperty("http-method")
    public List<HttpMethod> getHttpMethod()
    {
        return httpMethod;
    }

    /**
     * The HTTP method. If left empty, serves as a wildcard.
     * 
     */
    @JsonProperty("http-method")
    public void setHttpMethod(List<HttpMethod> httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    public RemovedDefaultOperation withHttpMethod(List<HttpMethod> httpMethod)
    {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * The explicit part in the path between two slashes '/' which uniquely
     * identifies a message. It is interpreted as a regex. If left empty, it matches
     * all. For example, setting 'sm-contexts' in resource attribute will match
     * against the messages with
     * {apiRoot}/nsmf-pdusession/<apiVersion>/sm-contexts/{smContextRef} path.
     * 
     */
    @JsonProperty("resource")
    public List<String> getResource()
    {
        return resource;
    }

    /**
     * The explicit part in the path between two slashes '/' which uniquely
     * identifies a message. It is interpreted as a regex. If left empty, it matches
     * all. For example, setting 'sm-contexts' in resource attribute will match
     * against the messages with
     * {apiRoot}/nsmf-pdusession/<apiVersion>/sm-contexts/{smContextRef} path.
     * 
     */
    @JsonProperty("resource")
    public void setResource(List<String> resource)
    {
        this.resource = resource;
    }

    public RemovedDefaultOperation withResource(List<String> resource)
    {
        this.resource = resource;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RemovedDefaultOperation.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("serviceName");
        sb.append('=');
        sb.append(((this.serviceName == null) ? "<null>" : this.serviceName));
        sb.append(',');
        sb.append("serviceVersion");
        sb.append('=');
        sb.append(((this.serviceVersion == null) ? "<null>" : this.serviceVersion));
        sb.append(',');
        sb.append("notificationMessage");
        sb.append('=');
        sb.append(((this.notificationMessage == null) ? "<null>" : this.notificationMessage));
        sb.append(',');
        sb.append("httpMethod");
        sb.append('=');
        sb.append(((this.httpMethod == null) ? "<null>" : this.httpMethod));
        sb.append(',');
        sb.append("resource");
        sb.append('=');
        sb.append(((this.resource == null) ? "<null>" : this.resource));
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
        result = ((result * 31) + ((this.serviceVersion == null) ? 0 : this.serviceVersion.hashCode()));
        result = ((result * 31) + ((this.resource == null) ? 0 : this.resource.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.notificationMessage == null) ? 0 : this.notificationMessage.hashCode()));
        result = ((result * 31) + ((this.serviceName == null) ? 0 : this.serviceName.hashCode()));
        result = ((result * 31) + ((this.httpMethod == null) ? 0 : this.httpMethod.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RemovedDefaultOperation) == false)
        {
            return false;
        }
        RemovedDefaultOperation rhs = ((RemovedDefaultOperation) other);
        return (((((((this.serviceVersion == rhs.serviceVersion) || ((this.serviceVersion != null) && this.serviceVersion.equals(rhs.serviceVersion)))
                    && ((this.resource == rhs.resource) || ((this.resource != null) && this.resource.equals(rhs.resource))))
                   && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                  && ((this.notificationMessage == rhs.notificationMessage)
                      || ((this.notificationMessage != null) && this.notificationMessage.equals(rhs.notificationMessage))))
                 && ((this.serviceName == rhs.serviceName) || ((this.serviceName != null) && this.serviceName.equals(rhs.serviceName))))
                && ((this.httpMethod == rhs.httpMethod) || ((this.httpMethod != null) && this.httpMethod.equals(rhs.httpMethod))));
    }

}
