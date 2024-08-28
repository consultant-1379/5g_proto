
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Action to perform on a message in case IP Address Hiding cannot be applied,
 * given that the message contains at lease one NF profile with no FQDN, neither
 * on the profile nor on the service level. If no action is configured, then IP
 * Address Hiding shall be performed despite missing FQDN.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "drop-message", "forward-message", "respond-with-error" })
public class OnFqdnAbsence
{

    /**
     * Drop message
     * 
     */
    @JsonProperty("drop-message")
    @JsonPropertyDescription("Drop message")
    private DropMessage dropMessage;
    /**
     * Forward message as is
     * 
     */
    @JsonProperty("forward-message")
    @JsonPropertyDescription("Forward message as is")
    private ForwardMessage forwardMessage;
    /**
     * Respond with a specific error message
     * 
     */
    @JsonProperty("respond-with-error")
    @JsonPropertyDescription("Respond with a specific error message")
    private RespondWithError respondWithError;

    /**
     * Drop message
     * 
     */
    @JsonProperty("drop-message")
    public DropMessage getDropMessage()
    {
        return dropMessage;
    }

    /**
     * Drop message
     * 
     */
    @JsonProperty("drop-message")
    public void setDropMessage(DropMessage dropMessage)
    {
        this.dropMessage = dropMessage;
    }

    public OnFqdnAbsence withDropMessage(DropMessage dropMessage)
    {
        this.dropMessage = dropMessage;
        return this;
    }

    /**
     * Forward message as is
     * 
     */
    @JsonProperty("forward-message")
    public ForwardMessage getForwardMessage()
    {
        return forwardMessage;
    }

    /**
     * Forward message as is
     * 
     */
    @JsonProperty("forward-message")
    public void setForwardMessage(ForwardMessage forwardMessage)
    {
        this.forwardMessage = forwardMessage;
    }

    public OnFqdnAbsence withForwardMessage(ForwardMessage forwardMessage)
    {
        this.forwardMessage = forwardMessage;
        return this;
    }

    /**
     * Respond with a specific error message
     * 
     */
    @JsonProperty("respond-with-error")
    public RespondWithError getRespondWithError()
    {
        return respondWithError;
    }

    /**
     * Respond with a specific error message
     * 
     */
    @JsonProperty("respond-with-error")
    public void setRespondWithError(RespondWithError respondWithError)
    {
        this.respondWithError = respondWithError;
    }

    public OnFqdnAbsence withRespondWithError(RespondWithError respondWithError)
    {
        this.respondWithError = respondWithError;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(OnFqdnAbsence.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("dropMessage");
        sb.append('=');
        sb.append(((this.dropMessage == null) ? "<null>" : this.dropMessage));
        sb.append(',');
        sb.append("forwardMessage");
        sb.append('=');
        sb.append(((this.forwardMessage == null) ? "<null>" : this.forwardMessage));
        sb.append(',');
        sb.append("respondWithError");
        sb.append('=');
        sb.append(((this.respondWithError == null) ? "<null>" : this.respondWithError));
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
        result = ((result * 31) + ((this.respondWithError == null) ? 0 : this.respondWithError.hashCode()));
        result = ((result * 31) + ((this.dropMessage == null) ? 0 : this.dropMessage.hashCode()));
        result = ((result * 31) + ((this.forwardMessage == null) ? 0 : this.forwardMessage.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof OnFqdnAbsence) == false)
        {
            return false;
        }
        OnFqdnAbsence rhs = ((OnFqdnAbsence) other);
        return ((((this.respondWithError == rhs.respondWithError) || ((this.respondWithError != null) && this.respondWithError.equals(rhs.respondWithError)))
                 && ((this.dropMessage == rhs.dropMessage) || ((this.dropMessage != null) && this.dropMessage.equals(rhs.dropMessage))))
                && ((this.forwardMessage == rhs.forwardMessage) || ((this.forwardMessage != null) && this.forwardMessage.equals(rhs.forwardMessage))));
    }

}
