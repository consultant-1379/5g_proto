
package com.ericsson.sc.scp.model;

import com.ericsson.utilities.common.IfNamedListItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name", "user-label", "path", "request-header", "response-header", "header", "body-json-pointer", "variable-name", "extractor-regex" })
public class MessageDatum implements IfNamedListItem
{

    /**
     * Name identifying the message data set (Required)
     * 
     */
    @JsonProperty("name")
    @JsonPropertyDescription("Name identifying the message data set")
    private String name;
    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    @JsonPropertyDescription("Label for free use")
    private String userLabel;
    /**
     * If present, specifies that the content of the URL path is used as the source
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("If present, specifies that the content of the URL path is used as the source")
    private Path path;
    /**
     * If present, specifies the name of the request header from which to read the
     * content
     * 
     */
    @JsonProperty("request-header")
    @JsonPropertyDescription("If present, specifies the name of the request header from which to read the content")
    private String requestHeader;
    /**
     * If present, specifies the name of the response header from which to read the
     * content
     * 
     */
    @JsonProperty("response-header")
    @JsonPropertyDescription("If present, specifies the name of the response header from which to read the content")
    private String responseHeader;
    /**
     * If present, specifies the name of the request or response header from which
     * to read the content depending on if it is used in the request or response
     * path
     * 
     */
    @JsonProperty("header")
    @JsonPropertyDescription("If present, specifies the name of the request or response header from which to read the content depending on if it is used in the request or response path")
    private String header;
    /**
     * If configured, specifies the element that will be extracted from a
     * JSON-formatted body with a JSON-Pointer specification according to RFC 6901
     * 
     */
    @JsonProperty("body-json-pointer")
    @JsonPropertyDescription("If configured, specifies the element that will be extracted from a JSON-formatted body with a JSON-Pointer specification according to RFC 6901")
    private String bodyJsonPointer;
    /**
     * The name of the variable in which to store the extracted data
     * 
     */
    @JsonProperty("variable-name")
    @JsonPropertyDescription("The name of the variable in which to store the extracted data")
    private String variableName;
    /**
     * Regular expression name captures (?<name>re) used to extract data into the
     * variable 'name'. If the regular expression does not match, all variables are
     * left undefined. The names of named-captures’ respective variables must begin
     * with a letter and contain only letters, digits, or underscore
     * 
     */
    @JsonProperty("extractor-regex")
    @JsonPropertyDescription("Regular expression name captures (?<name>re) used to extract data into the variable 'name'. If the regular expression does not match, all variables are left undefined. The names of named-captures\u2019 respective variables must begin with a letter and contain only letters, digits, or underscore")
    private String extractorRegex;

    /**
     * Name identifying the message data set (Required)
     * 
     */
    @JsonProperty("name")
    public String getName()
    {
        return name;
    }

    /**
     * Name identifying the message data set (Required)
     * 
     */
    @JsonProperty("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public MessageDatum withName(String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public String getUserLabel()
    {
        return userLabel;
    }

    /**
     * Label for free use
     * 
     */
    @JsonProperty("user-label")
    public void setUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
    }

    public MessageDatum withUserLabel(String userLabel)
    {
        this.userLabel = userLabel;
        return this;
    }

    /**
     * If present, specifies that the content of the URL path is used as the source
     * 
     */
    @JsonProperty("path")
    public Path getPath()
    {
        return path;
    }

    /**
     * If present, specifies that the content of the URL path is used as the source
     * 
     */
    @JsonProperty("path")
    public void setPath(Path path)
    {
        this.path = path;
    }

    public MessageDatum withPath(Path path)
    {
        this.path = path;
        return this;
    }

    /**
     * If present, specifies the name of the request header from which to read the
     * content
     * 
     */
    @JsonProperty("request-header")
    public String getRequestHeader()
    {
        return requestHeader;
    }

    /**
     * If present, specifies the name of the request header from which to read the
     * content
     * 
     */
    @JsonProperty("request-header")
    public void setRequestHeader(String requestHeader)
    {
        this.requestHeader = requestHeader;
    }

    public MessageDatum withRequestHeader(String requestHeader)
    {
        this.requestHeader = requestHeader;
        return this;
    }

    /**
     * If present, specifies the name of the response header from which to read the
     * content
     * 
     */
    @JsonProperty("response-header")
    public String getResponseHeader()
    {
        return responseHeader;
    }

    /**
     * If present, specifies the name of the response header from which to read the
     * content
     * 
     */
    @JsonProperty("response-header")
    public void setResponseHeader(String responseHeader)
    {
        this.responseHeader = responseHeader;
    }

    public MessageDatum withResponseHeader(String responseHeader)
    {
        this.responseHeader = responseHeader;
        return this;
    }

    /**
     * If present, specifies the name of the request or response header from which
     * to read the content depending on if it is used in the request or response
     * path
     * 
     */
    @JsonProperty("header")
    public String getHeader()
    {
        return header;
    }

    /**
     * If present, specifies the name of the request or response header from which
     * to read the content depending on if it is used in the request or response
     * path
     * 
     */
    @JsonProperty("header")
    public void setHeader(String header)
    {
        this.header = header;
    }

    public MessageDatum withHeader(String header)
    {
        this.header = header;
        return this;
    }

    /**
     * If configured, specifies the element that will be extracted from a
     * JSON-formatted body with a JSON-Pointer specification according to RFC 6901
     * 
     */
    @JsonProperty("body-json-pointer")
    public String getBodyJsonPointer()
    {
        return bodyJsonPointer;
    }

    /**
     * If configured, specifies the element that will be extracted from a
     * JSON-formatted body with a JSON-Pointer specification according to RFC 6901
     * 
     */
    @JsonProperty("body-json-pointer")
    public void setBodyJsonPointer(String bodyJsonPointer)
    {
        this.bodyJsonPointer = bodyJsonPointer;
    }

    public MessageDatum withBodyJsonPointer(String bodyJsonPointer)
    {
        this.bodyJsonPointer = bodyJsonPointer;
        return this;
    }

    /**
     * The name of the variable in which to store the extracted data
     * 
     */
    @JsonProperty("variable-name")
    public String getVariableName()
    {
        return variableName;
    }

    /**
     * The name of the variable in which to store the extracted data
     * 
     */
    @JsonProperty("variable-name")
    public void setVariableName(String variableName)
    {
        this.variableName = variableName;
    }

    public MessageDatum withVariableName(String variableName)
    {
        this.variableName = variableName;
        return this;
    }

    /**
     * Regular expression name captures (?<name>re) used to extract data into the
     * variable 'name'. If the regular expression does not match, all variables are
     * left undefined. The names of named-captures’ respective variables must begin
     * with a letter and contain only letters, digits, or underscore
     * 
     */
    @JsonProperty("extractor-regex")
    public String getExtractorRegex()
    {
        return extractorRegex;
    }

    /**
     * Regular expression name captures (?<name>re) used to extract data into the
     * variable 'name'. If the regular expression does not match, all variables are
     * left undefined. The names of named-captures’ respective variables must begin
     * with a letter and contain only letters, digits, or underscore
     * 
     */
    @JsonProperty("extractor-regex")
    public void setExtractorRegex(String extractorRegex)
    {
        this.extractorRegex = extractorRegex;
    }

    public MessageDatum withExtractorRegex(String extractorRegex)
    {
        this.extractorRegex = extractorRegex;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(MessageDatum.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null) ? "<null>" : this.name));
        sb.append(',');
        sb.append("userLabel");
        sb.append('=');
        sb.append(((this.userLabel == null) ? "<null>" : this.userLabel));
        sb.append(',');
        sb.append("path");
        sb.append('=');
        sb.append(((this.path == null) ? "<null>" : this.path));
        sb.append(',');
        sb.append("requestHeader");
        sb.append('=');
        sb.append(((this.requestHeader == null) ? "<null>" : this.requestHeader));
        sb.append(',');
        sb.append("responseHeader");
        sb.append('=');
        sb.append(((this.responseHeader == null) ? "<null>" : this.responseHeader));
        sb.append(',');
        sb.append("header");
        sb.append('=');
        sb.append(((this.header == null) ? "<null>" : this.header));
        sb.append(',');
        sb.append("bodyJsonPointer");
        sb.append('=');
        sb.append(((this.bodyJsonPointer == null) ? "<null>" : this.bodyJsonPointer));
        sb.append(',');
        sb.append("variableName");
        sb.append('=');
        sb.append(((this.variableName == null) ? "<null>" : this.variableName));
        sb.append(',');
        sb.append("extractorRegex");
        sb.append('=');
        sb.append(((this.extractorRegex == null) ? "<null>" : this.extractorRegex));
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
        result = ((result * 31) + ((this.path == null) ? 0 : this.path.hashCode()));
        result = ((result * 31) + ((this.userLabel == null) ? 0 : this.userLabel.hashCode()));
        result = ((result * 31) + ((this.variableName == null) ? 0 : this.variableName.hashCode()));
        result = ((result * 31) + ((this.extractorRegex == null) ? 0 : this.extractorRegex.hashCode()));
        result = ((result * 31) + ((this.name == null) ? 0 : this.name.hashCode()));
        result = ((result * 31) + ((this.responseHeader == null) ? 0 : this.responseHeader.hashCode()));
        result = ((result * 31) + ((this.header == null) ? 0 : this.header.hashCode()));
        result = ((result * 31) + ((this.requestHeader == null) ? 0 : this.requestHeader.hashCode()));
        result = ((result * 31) + ((this.bodyJsonPointer == null) ? 0 : this.bodyJsonPointer.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof MessageDatum) == false)
        {
            return false;
        }
        MessageDatum rhs = ((MessageDatum) other);
        return ((((((((((this.userLabel == rhs.userLabel) || ((this.userLabel != null) && this.userLabel.equals(rhs.userLabel)))
                       && ((this.path == rhs.path) || ((this.path != null) && this.path.equals(rhs.path))))
                      && ((this.variableName == rhs.variableName) || ((this.variableName != null) && this.variableName.equals(rhs.variableName))))
                     && ((this.extractorRegex == rhs.extractorRegex) || ((this.extractorRegex != null) && this.extractorRegex.equals(rhs.extractorRegex))))
                    && ((this.name == rhs.name) || ((this.name != null) && this.name.equals(rhs.name))))
                   && ((this.responseHeader == rhs.responseHeader) || ((this.responseHeader != null) && this.responseHeader.equals(rhs.responseHeader))))
                  && ((this.header == rhs.header) || ((this.header != null) && this.header.equals(rhs.header))))
                 && ((this.requestHeader == rhs.requestHeader) || ((this.requestHeader != null) && this.requestHeader.equals(rhs.requestHeader))))
                && ((this.bodyJsonPointer == rhs.bodyJsonPointer) || ((this.bodyJsonPointer != null) && this.bodyJsonPointer.equals(rhs.bodyJsonPointer))));
    }

}
