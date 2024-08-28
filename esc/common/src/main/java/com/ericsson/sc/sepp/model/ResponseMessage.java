
package com.ericsson.sc.sepp.model;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Parameters used to define the location of FQDNs in a response message
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "message-data-ref", "search-in-query-parameter", "search-in-header", "search-in-message-body" })
public class ResponseMessage
{

    /**
     * Reference to defined message-data.
     * 
     */
    @JsonProperty("message-data-ref")
    @JsonPropertyDescription("Reference to defined message-data.")
    private List<String> messageDataRef = new ArrayList<String>();
    /**
     * A list of the query parameters included in the request message, which are
     * used to locate specific information
     * 
     */
    @JsonProperty("search-in-query-parameter")
    @JsonPropertyDescription("A list of the query parameters included in the request message, which are used to locate specific information")
    private List<SearchInQueryParameter> searchInQueryParameter = new ArrayList<SearchInQueryParameter>();
    /**
     * A list of the headers of the message which are used to locate specific
     * information
     * 
     */
    @JsonProperty("search-in-header")
    @JsonPropertyDescription("A list of the headers of the message which are used to locate specific information")
    private List<SearchInHeader> searchInHeader = new ArrayList<SearchInHeader>();
    /**
     * A list of the elements conveying specific information that can be extracted
     * from the JSON-formatted body of the message
     * 
     */
    @JsonProperty("search-in-message-body")
    @JsonPropertyDescription("A list of the elements conveying specific information that can be extracted from the JSON-formatted body of the message")
    private List<SearchInMessageBody> searchInMessageBody = new ArrayList<SearchInMessageBody>();

    /**
     * Reference to defined message-data.
     * 
     */
    @JsonProperty("message-data-ref")
    public List<String> getMessageDataRef()
    {
        return messageDataRef;
    }

    /**
     * Reference to defined message-data.
     * 
     */
    @JsonProperty("message-data-ref")
    public void setMessageDataRef(List<String> messageDataRef)
    {
        this.messageDataRef = messageDataRef;
    }

    public ResponseMessage withMessageDataRef(List<String> messageDataRef)
    {
        this.messageDataRef = messageDataRef;
        return this;
    }

    /**
     * A list of the query parameters included in the request message, which are
     * used to locate specific information
     * 
     */
    @JsonProperty("search-in-query-parameter")
    public List<SearchInQueryParameter> getSearchInQueryParameter()
    {
        return searchInQueryParameter;
    }

    /**
     * A list of the query parameters included in the request message, which are
     * used to locate specific information
     * 
     */
    @JsonProperty("search-in-query-parameter")
    public void setSearchInQueryParameter(List<SearchInQueryParameter> searchInQueryParameter)
    {
        this.searchInQueryParameter = searchInQueryParameter;
    }

    public ResponseMessage withSearchInQueryParameter(List<SearchInQueryParameter> searchInQueryParameter)
    {
        this.searchInQueryParameter = searchInQueryParameter;
        return this;
    }

    /**
     * A list of the headers of the message which are used to locate specific
     * information
     * 
     */
    @JsonProperty("search-in-header")
    public List<SearchInHeader> getSearchInHeader()
    {
        return searchInHeader;
    }

    /**
     * A list of the headers of the message which are used to locate specific
     * information
     * 
     */
    @JsonProperty("search-in-header")
    public void setSearchInHeader(List<SearchInHeader> searchInHeader)
    {
        this.searchInHeader = searchInHeader;
    }

    public ResponseMessage withSearchInHeader(List<SearchInHeader> searchInHeader)
    {
        this.searchInHeader = searchInHeader;
        return this;
    }

    /**
     * A list of the elements conveying specific information that can be extracted
     * from the JSON-formatted body of the message
     * 
     */
    @JsonProperty("search-in-message-body")
    public List<SearchInMessageBody> getSearchInMessageBody()
    {
        return searchInMessageBody;
    }

    /**
     * A list of the elements conveying specific information that can be extracted
     * from the JSON-formatted body of the message
     * 
     */
    @JsonProperty("search-in-message-body")
    public void setSearchInMessageBody(List<SearchInMessageBody> searchInMessageBody)
    {
        this.searchInMessageBody = searchInMessageBody;
    }

    public ResponseMessage withSearchInMessageBody(List<SearchInMessageBody> searchInMessageBody)
    {
        this.searchInMessageBody = searchInMessageBody;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(ResponseMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("messageDataRef");
        sb.append('=');
        sb.append(((this.messageDataRef == null) ? "<null>" : this.messageDataRef));
        sb.append(',');
        sb.append("searchInQueryParameter");
        sb.append('=');
        sb.append(((this.searchInQueryParameter == null) ? "<null>" : this.searchInQueryParameter));
        sb.append(',');
        sb.append("searchInHeader");
        sb.append('=');
        sb.append(((this.searchInHeader == null) ? "<null>" : this.searchInHeader));
        sb.append(',');
        sb.append("searchInMessageBody");
        sb.append('=');
        sb.append(((this.searchInMessageBody == null) ? "<null>" : this.searchInMessageBody));
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
        result = ((result * 31) + ((this.messageDataRef == null) ? 0 : this.messageDataRef.hashCode()));
        result = ((result * 31) + ((this.searchInHeader == null) ? 0 : this.searchInHeader.hashCode()));
        result = ((result * 31) + ((this.searchInMessageBody == null) ? 0 : this.searchInMessageBody.hashCode()));
        result = ((result * 31) + ((this.searchInQueryParameter == null) ? 0 : this.searchInQueryParameter.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof ResponseMessage) == false)
        {
            return false;
        }
        ResponseMessage rhs = ((ResponseMessage) other);
        return (((((this.messageDataRef == rhs.messageDataRef) || ((this.messageDataRef != null) && this.messageDataRef.equals(rhs.messageDataRef)))
                  && ((this.searchInHeader == rhs.searchInHeader) || ((this.searchInHeader != null) && this.searchInHeader.equals(rhs.searchInHeader))))
                 && ((this.searchInMessageBody == rhs.searchInMessageBody)
                     || ((this.searchInMessageBody != null) && this.searchInMessageBody.equals(rhs.searchInMessageBody))))
                && ((this.searchInQueryParameter == rhs.searchInQueryParameter)
                    || ((this.searchInQueryParameter != null) && this.searchInQueryParameter.equals(rhs.searchInQueryParameter))));
    }

}
