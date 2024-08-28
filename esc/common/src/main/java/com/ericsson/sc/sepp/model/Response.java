
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Firewall rules for response messages. Order of applied rules is defined by
 * the system and described in the CPI.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "validate-message-headers",
                     "validate-message-body-size",
                     "validate-message-json-body-syntax",
                     "validate-message-json-body-leaves",
                     "validate-message-json-body-depth" })
public class Response
{

    /**
     * Validation against allowed or denied headers.
     * 
     */
    @JsonProperty("validate-message-headers")
    @JsonPropertyDescription("Validation against allowed or denied headers.")
    private ResponseValidateMessageHeaders validateMessageHeaders;
    /**
     * Validation of configured maximum size of the message body.
     * 
     */
    @JsonProperty("validate-message-body-size")
    @JsonPropertyDescription("Validation of configured maximum size of the message body.")
    private ResponseValidateMessageBodySize validateMessageBodySize;
    /**
     * Validation of message JSON body syntax.
     * 
     */
    @JsonProperty("validate-message-json-body-syntax")
    @JsonPropertyDescription("Validation of message JSON body syntax.")
    private ResponseValidateMessageJsonBodySyntax validateMessageJsonBodySyntax;
    /**
     * Validation of configured maximum integer of JSON body leaves in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-leaves")
    @JsonPropertyDescription("Validation of configured maximum integer of JSON body leaves in the message.")
    private ResponseValidateMessageJsonBodyLeaves validateMessageJsonBodyLeaves;
    /**
     * Validation of configured maximum JSON body depth in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-depth")
    @JsonPropertyDescription("Validation of configured maximum JSON body depth in the message.")
    private ResponseValidateMessageJsonBodyDepth validateMessageJsonBodyDepth;

    /**
     * Validation against allowed or denied headers.
     * 
     */
    @JsonProperty("validate-message-headers")
    public ResponseValidateMessageHeaders getValidateMessageHeaders()
    {
        return validateMessageHeaders;
    }

    /**
     * Validation against allowed or denied headers.
     * 
     */
    @JsonProperty("validate-message-headers")
    public void setValidateMessageHeaders(ResponseValidateMessageHeaders validateMessageHeaders)
    {
        this.validateMessageHeaders = validateMessageHeaders;
    }

    public Response withValidateMessageHeaders(ResponseValidateMessageHeaders validateMessageHeaders)
    {
        this.validateMessageHeaders = validateMessageHeaders;
        return this;
    }

    /**
     * Validation of configured maximum size of the message body.
     * 
     */
    @JsonProperty("validate-message-body-size")
    public ResponseValidateMessageBodySize getValidateMessageBodySize()
    {
        return validateMessageBodySize;
    }

    /**
     * Validation of configured maximum size of the message body.
     * 
     */
    @JsonProperty("validate-message-body-size")
    public void setValidateMessageBodySize(ResponseValidateMessageBodySize validateMessageBodySize)
    {
        this.validateMessageBodySize = validateMessageBodySize;
    }

    public Response withValidateMessageBodySize(ResponseValidateMessageBodySize validateMessageBodySize)
    {
        this.validateMessageBodySize = validateMessageBodySize;
        return this;
    }

    /**
     * Validation of message JSON body syntax.
     * 
     */
    @JsonProperty("validate-message-json-body-syntax")
    public ResponseValidateMessageJsonBodySyntax getValidateMessageJsonBodySyntax()
    {
        return validateMessageJsonBodySyntax;
    }

    /**
     * Validation of message JSON body syntax.
     * 
     */
    @JsonProperty("validate-message-json-body-syntax")
    public void setValidateMessageJsonBodySyntax(ResponseValidateMessageJsonBodySyntax validateMessageJsonBodySyntax)
    {
        this.validateMessageJsonBodySyntax = validateMessageJsonBodySyntax;
    }

    public Response withValidateMessageJsonBodySyntax(ResponseValidateMessageJsonBodySyntax validateMessageJsonBodySyntax)
    {
        this.validateMessageJsonBodySyntax = validateMessageJsonBodySyntax;
        return this;
    }

    /**
     * Validation of configured maximum integer of JSON body leaves in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-leaves")
    public ResponseValidateMessageJsonBodyLeaves getValidateMessageJsonBodyLeaves()
    {
        return validateMessageJsonBodyLeaves;
    }

    /**
     * Validation of configured maximum integer of JSON body leaves in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-leaves")
    public void setValidateMessageJsonBodyLeaves(ResponseValidateMessageJsonBodyLeaves validateMessageJsonBodyLeaves)
    {
        this.validateMessageJsonBodyLeaves = validateMessageJsonBodyLeaves;
    }

    public Response withValidateMessageJsonBodyLeaves(ResponseValidateMessageJsonBodyLeaves validateMessageJsonBodyLeaves)
    {
        this.validateMessageJsonBodyLeaves = validateMessageJsonBodyLeaves;
        return this;
    }

    /**
     * Validation of configured maximum JSON body depth in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-depth")
    public ResponseValidateMessageJsonBodyDepth getValidateMessageJsonBodyDepth()
    {
        return validateMessageJsonBodyDepth;
    }

    /**
     * Validation of configured maximum JSON body depth in the message.
     * 
     */
    @JsonProperty("validate-message-json-body-depth")
    public void setValidateMessageJsonBodyDepth(ResponseValidateMessageJsonBodyDepth validateMessageJsonBodyDepth)
    {
        this.validateMessageJsonBodyDepth = validateMessageJsonBodyDepth;
    }

    public Response withValidateMessageJsonBodyDepth(ResponseValidateMessageJsonBodyDepth validateMessageJsonBodyDepth)
    {
        this.validateMessageJsonBodyDepth = validateMessageJsonBodyDepth;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(Response.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
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
        if ((other instanceof Response) == false)
        {
            return false;
        }
        Response rhs = ((Response) other);
        return ((((((this.validateMessageBodySize == rhs.validateMessageBodySize)
                    || ((this.validateMessageBodySize != null) && this.validateMessageBodySize.equals(rhs.validateMessageBodySize)))
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
