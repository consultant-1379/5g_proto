
package com.ericsson.sc.sepp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Action to perform in case FQDN mapping cannot be applied
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "drop-message", "respond-with-error" })
public class OnFqdnMappingUnsuccessful
{

    /**
     * Drop message
     * 
     */
    @JsonProperty("drop-message")
    @JsonPropertyDescription("Drop message")
    private DropMessage dropMessage;
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

    public OnFqdnMappingUnsuccessful withDropMessage(DropMessage dropMessage)
    {
        this.dropMessage = dropMessage;
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

    public OnFqdnMappingUnsuccessful withRespondWithError(RespondWithError respondWithError)
    {
        this.respondWithError = respondWithError;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(OnFqdnMappingUnsuccessful.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("dropMessage");
        sb.append('=');
        sb.append(((this.dropMessage == null) ? "<null>" : this.dropMessage));
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
        result = ((result * 31) + ((this.dropMessage == null) ? 0 : this.dropMessage.hashCode()));
        result = ((result * 31) + ((this.respondWithError == null) ? 0 : this.respondWithError.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof OnFqdnMappingUnsuccessful) == false)
        {
            return false;
        }
        OnFqdnMappingUnsuccessful rhs = ((OnFqdnMappingUnsuccessful) other);
        return (((this.dropMessage == rhs.dropMessage) || ((this.dropMessage != null) && this.dropMessage.equals(rhs.dropMessage)))
                && ((this.respondWithError == rhs.respondWithError)
                    || ((this.respondWithError != null) && this.respondWithError.equals(rhs.respondWithError))));
    }

}
