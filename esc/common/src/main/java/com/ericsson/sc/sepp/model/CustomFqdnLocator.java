
package com.ericsson.sc.sepp.model;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name",
                     "request-message",
                     "response-message",
                     "service-name",
                     "service-version",
                     "notification-message",
                     "http-method",
                     "resource",
                     "message-origin" })
public class CustomFqdnLocator
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("name")
    private String name;
    /**
     * Parameters used to define the location of FQDNs in a request message
     * 
     */
    @JsonProperty("request-message")
    @JsonPropertyDescription("Parameters used to define the location of FQDNs in a request message")
    private RequestMessage requestMessage;

    /**
     * Parameters used to define the location of FQDNs in a response message
     * 
     */
    @JsonProperty("response-message")
    @JsonPropertyDescription("Parameters used to define the location of FQDNs in a response message")
    private ResponseMessage responseMessage;
    /**
     * The name of the service
     * 
     */
    @JsonProperty("service-name")
    @JsonPropertyDescription("The name of the service")
    private String serviceName;
    /**
     * The version of the service specified by 'v' and a integer, ex. 'v1', 'v2' etc
     * 
     */
    @JsonProperty("service-version")
    @JsonPropertyDescription("The version of the service specified by 'v' and a integer, ex. 'v1', 'v2' etc")
    private String serviceVersion;
    /**
     * Set to 'true' when referring to Notification messages
     * 
     */
    @JsonProperty("notification-message")
    @JsonPropertyDescription("Set to 'true' when referring to Notification messages")
    private Boolean notificationMessage;
    /**
     * The HTTP method
     * 
     */
    @JsonProperty("http-method")
    @JsonPropertyDescription("The HTTP method")
    private CustomFqdnLocator.HttpMethod httpMethod;
    /**
     * The explicit part in the path between two slashes '/' which uniquely
     * identifies a message. For example, setting 'sm-contexts' in resource
     * attribute will match against the messages with
     * {apiRoot}/nsmf-pdusession/<apiVersion>/sm-contexts/{smContextRef} path.
     * 
     */
    @JsonProperty("resource")
    @JsonPropertyDescription("The explicit part in the path between two slashes '/' which uniquely identifies a message. For example, setting 'sm-contexts' in resource attribute will match against the messages with {apiRoot}/nsmf-pdusession/<apiVersion>/sm-contexts/{smContextRef} path.")
    private String resource;
    /**
     * Set to 'own-network' if the message originates in the own network or
     * 'extenal-network' otherwise
     * 
     */
    @JsonProperty("message-origin")
    @JsonPropertyDescription("Set to 'own-network' if the message originates in the own network or 'extenal-network' otherwise")
    private CustomFqdnLocator.MessageOrigin messageOrigin;

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

    public CustomFqdnLocator withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Parameters used to define the location of FQDNs in a request message
     * 
     */
    @JsonProperty("request-message")
    public RequestMessage getRequestMessage()
    {
        return requestMessage;
    }

    /**
     * Parameters used to define the location of FQDNs in a request message
     * 
     */
    @JsonProperty("request-message")
    public void setRequestMessage(RequestMessage requestMessage)
    {
        this.requestMessage = requestMessage;
    }

    public CustomFqdnLocator withRequestMessage(RequestMessage requestMessage)
    {
        this.requestMessage = requestMessage;
        return this;
    }

    /**
     * Parameters used to define the location of FQDNs in a response message
     * 
     */
    @JsonProperty("response-message")
    public ResponseMessage getResponseMessage()
    {
        return responseMessage;
    }

    /**
     * Parameters used to define the location of FQDNs in a response message
     * 
     */
    @JsonProperty("response-message")
    public void setResponseMessage(ResponseMessage responseMessage)
    {
        this.responseMessage = responseMessage;
    }

    public CustomFqdnLocator withResponseMessage(ResponseMessage responseMessage)
    {
        this.responseMessage = responseMessage;
        return this;
    }

    /**
     * The name of the service
     * 
     */
    @JsonProperty("service-name")
    public String getServiceName()
    {
        return serviceName;
    }

    /**
     * The name of the service
     * 
     */
    @JsonProperty("service-name")
    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    public CustomFqdnLocator withServiceName(String serviceName)
    {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * The version of the service specified by 'v' and a integer, ex. 'v1', 'v2' etc
     * 
     */
    @JsonProperty("service-version")
    public String getServiceVersion()
    {
        return serviceVersion;
    }

    /**
     * The version of the service specified by 'v' and a integer, ex. 'v1', 'v2' etc
     * 
     */
    @JsonProperty("service-version")
    public void setServiceVersion(String serviceVersion)
    {
        this.serviceVersion = serviceVersion;
    }

    public CustomFqdnLocator withServiceVersion(String serviceVersion)
    {
        this.serviceVersion = serviceVersion;
        return this;
    }

    /**
     * Set to 'true' when referring to Notification messages
     * 
     */
    @JsonProperty("notification-message")
    public Boolean getNotificationMessage()
    {
        return notificationMessage;
    }

    /**
     * Set to 'true' when referring to Notification messages
     * 
     */
    @JsonProperty("notification-message")
    public void setNotificationMessage(Boolean notificationMessage)
    {
        this.notificationMessage = notificationMessage;
    }

    public CustomFqdnLocator withNotificationMessage(Boolean notificationMessage)
    {
        this.notificationMessage = notificationMessage;
        return this;
    }

    /**
     * The HTTP method
     * 
     */
    @JsonProperty("http-method")
    public CustomFqdnLocator.HttpMethod getHttpMethod()
    {
        return httpMethod;
    }

    /**
     * The HTTP method
     * 
     */
    @JsonProperty("http-method")
    public void setHttpMethod(CustomFqdnLocator.HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;
    }

    public CustomFqdnLocator withHttpMethod(CustomFqdnLocator.HttpMethod httpMethod)
    {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * The explicit part in the path between two slashes '/' which uniquely
     * identifies a message. For example, setting 'sm-contexts' in resource
     * attribute will match against the messages with
     * {apiRoot}/nsmf-pdusession/<apiVersion>/sm-contexts/{smContextRef} path.
     * 
     */
    @JsonProperty("resource")
    public String getResource()
    {
        return resource;
    }

    /**
     * The explicit part in the path between two slashes '/' which uniquely
     * identifies a message. For example, setting 'sm-contexts' in resource
     * attribute will match against the messages with
     * {apiRoot}/nsmf-pdusession/<apiVersion>/sm-contexts/{smContextRef} path.
     * 
     */
    @JsonProperty("resource")
    public void setResource(String resource)
    {
        this.resource = resource;
    }

    public CustomFqdnLocator withResource(String resource)
    {
        this.resource = resource;
        return this;
    }

    /**
     * Set to 'own-network' if the message originates in the own network or
     * 'extenal-network' otherwise
     * 
     */
    @JsonProperty("message-origin")
    public CustomFqdnLocator.MessageOrigin getMessageOrigin()
    {
        return messageOrigin;
    }

    /**
     * Set to 'own-network' if the message originates in the own network or
     * 'extenal-network' otherwise
     * 
     */
    @JsonProperty("message-origin")
    public void setMessageOrigin(CustomFqdnLocator.MessageOrigin messageOrigin)
    {
        this.messageOrigin = messageOrigin;
    }

    public CustomFqdnLocator withMessageOrigin(CustomFqdnLocator.MessageOrigin messageOrigin)
    {
        this.messageOrigin = messageOrigin;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(CustomFqdnLocator.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("requestMessage");
        sb.append('=');
        sb.append(((this.requestMessage == null) ? "<null>" : this.requestMessage));
        sb.append(',');
        sb.append("responseMessage");
        sb.append('=');
        sb.append(((this.responseMessage == null) ? "<null>" : this.responseMessage));
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
        sb.append("messageOrigin");
        sb.append('=');
        sb.append(((this.messageOrigin == null) ? "<null>" : this.messageOrigin));
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
        result = ((result * 31) + ((this.requestMessage == null) ? 0 : this.requestMessage.hashCode()));
        result = ((result * 31) + ((this.serviceVersion == null) ? 0 : this.serviceVersion.hashCode()));
        result = ((result * 31) + ((this.messageOrigin == null) ? 0 : this.messageOrigin.hashCode()));
        result = ((result * 31) + ((this.resource == null) ? 0 : this.resource.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.notificationMessage == null) ? 0 : this.notificationMessage.hashCode()));
        result = ((result * 31) + ((this.responseMessage == null) ? 0 : this.responseMessage.hashCode()));
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
        if ((other instanceof CustomFqdnLocator) == false)
        {
            return false;
        }
        CustomFqdnLocator rhs = ((CustomFqdnLocator) other);
        return ((((((((((this.requestMessage == rhs.requestMessage) || ((this.requestMessage != null) && this.requestMessage.equals(rhs.requestMessage)))
                       && ((this.serviceVersion == rhs.serviceVersion) || ((this.serviceVersion != null) && this.serviceVersion.equals(rhs.serviceVersion))))
                      && ((this.messageOrigin == rhs.messageOrigin) || ((this.messageOrigin != null) && this.messageOrigin.equals(rhs.messageOrigin))))
                     && ((this.resource == rhs.resource) || ((this.resource != null) && this.resource.equals(rhs.resource))))
                    && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                   && ((this.notificationMessage == rhs.notificationMessage)
                       || ((this.notificationMessage != null) && this.notificationMessage.equals(rhs.notificationMessage))))
                  && ((this.responseMessage == rhs.responseMessage) || ((this.responseMessage != null) && this.responseMessage.equals(rhs.responseMessage))))
                 && ((this.serviceName == rhs.serviceName) || ((this.serviceName != null) && this.serviceName.equals(rhs.serviceName))))
                && ((this.httpMethod == rhs.httpMethod) || ((this.httpMethod != null) && this.httpMethod.equals(rhs.httpMethod))));
    }

    public enum HttpMethod
    {

        GET("get"),
        PUT("put"),
        DELETE("delete"),
        POST("post"),
        HEAD("head"),
        CONNECT("connect"),
        OPTIONS("options"),
        PATCH("patch"),
        TRACE("trace");

        private final String value;
        private final static Map<String, CustomFqdnLocator.HttpMethod> CONSTANTS = new HashMap<String, CustomFqdnLocator.HttpMethod>();

        static
        {
            for (CustomFqdnLocator.HttpMethod c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private HttpMethod(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static CustomFqdnLocator.HttpMethod fromValue(String value)
        {
            CustomFqdnLocator.HttpMethod constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

    public enum MessageOrigin
    {

        OWN_NETWORK("own-network"),
        EXTERNAL_NETWORK("external-network");

        private final String value;
        private final static Map<String, CustomFqdnLocator.MessageOrigin> CONSTANTS = new HashMap<String, CustomFqdnLocator.MessageOrigin>();

        static
        {
            for (CustomFqdnLocator.MessageOrigin c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private MessageOrigin(String value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            return this.value;
        }

        @JsonValue
        public String value()
        {
            return this.value;
        }

        @JsonCreator
        public static CustomFqdnLocator.MessageOrigin fromValue(String value)
        {
            CustomFqdnLocator.MessageOrigin constant = CONSTANTS.get(value);
            if (constant == null)
            {
                throw new IllegalArgumentException(value);
            }
            else
            {
                return constant;
            }
        }

    }

}
