
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Firewall rules for request messages. Order of applied rules is defined by the
 * system and described in the CPI.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "validate-service-operation",
                     "validate-message-headers",
                     "validate-message-body-size",
                     "validate-message-json-body-syntax",
                     "validate-message-json-body-leaves",
                     "validate-message-json-body-depth" })
public class Request
{

    /**
     * Validation against allowed operations. If no operations are added or removed
     * from the default list of allowed operations, only the default allowed
     * operations will be permitted. Default allowed operations are defined by the
     * system and described in the CPI.
     * 
     */
    @JsonProperty("validate-service-operation")
    @JsonPropertyDescription("Validation against allowed operations. If no operations are added or removed from the default list of allowed operations, only the default allowed operations will be permitted. Default allowed operations are defined by the system and described in the CPI.")
    private RequestValidateServiceOperation validateServiceOperation;
    /**
     * Validation against allowed or denied headers.
     * 
     */
    @JsonProperty("validate-message-headers")
    @JsonPropertyDescription("Validation against allowed or denied headers.")
    private RequestValidateMessageHeaders validateMessageHeaders;
    /**
     * Validation of configured maximum size of the message body.
     * 
     */
    @JsonProperty("validate-message-body-size")
    @JsonPropertyDescription("Validation of configured maximum size of the message body.")
    private RequestValidateMessageBodySize validateMessageBodySize;
    /**
     * Validation of message JSON body syntax.
     * 
     */
    @JsonProperty("validate-message-json-body-syntax")
    @JsonPropertyDescription("Validation of message JSON body syntax.")
    private RequestValidateMessageJsonBodySyntax validateMessageJsonBodySyntax;
    /**
     * Validation of configured maximum integer of JSON body leaves in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-leaves")
    @JsonPropertyDescription("Validation of configured maximum integer of JSON body leaves in the message.")
    private RequestValidateMessageJsonBodyLeaves validateMessageJsonBodyLeaves;
    /**
     * Validation of configured maximum JSON body depth in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-depth")
    @JsonPropertyDescription("Validation of configured maximum JSON body depth in the message.")
    private RequestValidateMessageJsonBodyDepth validateMessageJsonBodyDepth;

    /**
     * Validation against allowed operations. If no operations are added or removed
     * from the default list of allowed operations, only the default allowed
     * operations will be permitted. Default allowed operations are defined by the
     * system and described in the CPI.
     * 
     */
    @JsonProperty("validate-service-operation")
    public RequestValidateServiceOperation getValidateServiceOperation()
    {
        return validateServiceOperation;
    }

    /**
     * Validation against allowed operations. If no operations are added or removed
     * from the default list of allowed operations, only the default allowed
     * operations will be permitted. Default allowed operations are defined by the
     * system and described in the CPI.
     * 
     */
    @JsonProperty("validate-service-operation")
    public void setValidateServiceOperation(RequestValidateServiceOperation validateServiceOperation)
    {
        this.validateServiceOperation = validateServiceOperation;
    }

    public Request withValidateServiceOperation(RequestValidateServiceOperation validateServiceOperation)
    {
        this.validateServiceOperation = validateServiceOperation;
        return this;
    }

    /**
     * Validation against allowed or denied headers.
     * 
     */
    @JsonProperty("validate-message-headers")
    public RequestValidateMessageHeaders getValidateMessageHeaders()
    {
        return validateMessageHeaders;
    }

    /**
     * Validation against allowed or denied headers.
     * 
     */
    @JsonProperty("validate-message-headers")
    public void setValidateMessageHeaders(RequestValidateMessageHeaders validateMessageHeaders)
    {
        this.validateMessageHeaders = validateMessageHeaders;
    }

    public Request withValidateMessageHeaders(RequestValidateMessageHeaders validateMessageHeaders)
    {
        this.validateMessageHeaders = validateMessageHeaders;
        return this;
    }

    /**
     * Validation of configured maximum size of the message body.
     * 
     */
    @JsonProperty("validate-message-body-size")
    public RequestValidateMessageBodySize getValidateMessageBodySize()
    {
        return validateMessageBodySize;
    }

    /**
     * Validation of configured maximum size of the message body.
     * 
     */
    @JsonProperty("validate-message-body-size")
    public void setValidateMessageBodySize(RequestValidateMessageBodySize validateMessageBodySize)
    {
        this.validateMessageBodySize = validateMessageBodySize;
    }

    public Request withValidateMessageBodySize(RequestValidateMessageBodySize validateMessageBodySize)
    {
        this.validateMessageBodySize = validateMessageBodySize;
        return this;
    }

    /**
     * Validation of message JSON body syntax.
     * 
     */
    @JsonProperty("validate-message-json-body-syntax")
    public RequestValidateMessageJsonBodySyntax getValidateMessageJsonBodySyntax()
    {
        return validateMessageJsonBodySyntax;
    }

    /**
     * Validation of message JSON body syntax.
     * 
     */
    @JsonProperty("validate-message-json-body-syntax")
    public void setValidateMessageJsonBodySyntax(RequestValidateMessageJsonBodySyntax validateMessageJsonBodySyntax)
    {
        this.validateMessageJsonBodySyntax = validateMessageJsonBodySyntax;
    }

    public Request withValidateMessageJsonBodySyntax(RequestValidateMessageJsonBodySyntax validateMessageJsonBodySyntax)
    {
        this.validateMessageJsonBodySyntax = validateMessageJsonBodySyntax;
        return this;
    }

    /**
     * Validation of configured maximum integer of JSON body leaves in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-leaves")
    public RequestValidateMessageJsonBodyLeaves getValidateMessageJsonBodyLeaves()
    {
        return validateMessageJsonBodyLeaves;
    }

    /**
     * Validation of configured maximum integer of JSON body leaves in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-leaves")
    public void setValidateMessageJsonBodyLeaves(RequestValidateMessageJsonBodyLeaves validateMessageJsonBodyLeaves)
    {
        this.validateMessageJsonBodyLeaves = validateMessageJsonBodyLeaves;
    }

    public Request withValidateMessageJsonBodyLeaves(RequestValidateMessageJsonBodyLeaves validateMessageJsonBodyLeaves)
    {
        this.validateMessageJsonBodyLeaves = validateMessageJsonBodyLeaves;
        return this;
    }

    /**
     * Validation of configured maximum JSON body depth in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-depth")
    public RequestValidateMessageJsonBodyDepth getValidateMessageJsonBodyDepth()
    {
        return validateMessageJsonBodyDepth;
    }

    /**
     * Validation of configured maximum JSON body depth in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-depth")
    public void setValidateMessageJsonBodyDepth(RequestValidateMessageJsonBodyDepth validateMessageJsonBodyDepth)
    {
        this.validateMessageJsonBodyDepth = validateMessageJsonBodyDepth;
    }

    public Request withValidateMessageJsonBodyDepth(RequestValidateMessageJsonBodyDepth validateMessageJsonBodyDepth)
    {
        this.validateMessageJsonBodyDepth = validateMessageJsonBodyDepth;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Request.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("validateServiceOperation");
        sb.append('=');
        sb.append(((this.validateServiceOperation == null) ? "<null>" : this.validateServiceOperation));
        sb.append(',');
        sb.append("validateMessageHeaders");
        sb.append('=');
        sb.append(((this.validateMessageHeaders == null) ? "<null>" : this.validateMessageHeaders));
        sb.append(',');
        sb.append("validateMessageBodySize");
        sb.append('=');
        sb.append(((this.validateMessageBodySize == null) ? "<null>" : this.validateMessageBodySize));
        sb.append(',');
        sb.append("validateMessageJsonBodySyntax");
        sb.append('=');
        sb.append(((this.validateMessageJsonBodySyntax == null) ? "<null>" : this.validateMessageJsonBodySyntax));
        sb.append(',');
        sb.append("validateMessageJsonBodyLeaves");
        sb.append('=');
        sb.append(((this.validateMessageJsonBodyLeaves == null) ? "<null>" : this.validateMessageJsonBodyLeaves));
        sb.append(',');
        sb.append("validateMessageJsonBodyDepth");
        sb.append('=');
        sb.append(((this.validateMessageJsonBodyDepth == null) ? "<null>" : this.validateMessageJsonBodyDepth));
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
        result = ((result * 31) + ((this.validateServiceOperation == null) ? 0 : this.validateServiceOperation.hashCode()));
        result = ((result * 31) + ((this.validateMessageBodySize == null) ? 0 : this.validateMessageBodySize.hashCode()));
        result = ((result * 31) + ((this.validateMessageJsonBodyLeaves == null) ? 0 : this.validateMessageJsonBodyLeaves.hashCode()));
        result = ((result * 31) + ((this.validateMessageHeaders == null) ? 0 : this.validateMessageHeaders.hashCode()));
        result = ((result * 31) + ((this.validateMessageJsonBodySyntax == null) ? 0 : this.validateMessageJsonBodySyntax.hashCode()));
        result = ((result * 31) + ((this.validateMessageJsonBodyDepth == null) ? 0 : this.validateMessageJsonBodyDepth.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof Request) == false)
        {
            return false;
        }
        Request rhs = ((Request) other);
        return (((((((this.validateServiceOperation == rhs.validateServiceOperation)
                     || ((this.validateServiceOperation != null) && this.validateServiceOperation.equals(rhs.validateServiceOperation)))
                    && ((this.validateMessageBodySize == rhs.validateMessageBodySize)
                        || ((this.validateMessageBodySize != null) && this.validateMessageBodySize.equals(rhs.validateMessageBodySize))))
                   && ((this.validateMessageJsonBodyLeaves == rhs.validateMessageJsonBodyLeaves)
                       || ((this.validateMessageJsonBodyLeaves != null) && this.validateMessageJsonBodyLeaves.equals(rhs.validateMessageJsonBodyLeaves))))
                  && ((this.validateMessageHeaders == rhs.validateMessageHeaders)
                      || ((this.validateMessageHeaders != null) && this.validateMessageHeaders.equals(rhs.validateMessageHeaders))))
                 && ((this.validateMessageJsonBodySyntax == rhs.validateMessageJsonBodySyntax)
                     || ((this.validateMessageJsonBodySyntax != null) && this.validateMessageJsonBodySyntax.equals(rhs.validateMessageJsonBodySyntax))))
                && ((this.validateMessageJsonBodyDepth == rhs.validateMessageJsonBodyDepth)
                    || ((this.validateMessageJsonBodyDepth != null) && this.validateMessageJsonBodyDepth.equals(rhs.validateMessageJsonBodyDepth))));
    }

}
