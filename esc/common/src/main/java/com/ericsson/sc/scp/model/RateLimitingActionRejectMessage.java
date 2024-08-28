
package com.ericsson.sc.scp.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Rejects an http request and sends back a response with an operator defined
 * status code and title with detailed explanation
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "status", "title", "cause", "detail", "format", "retry-after-header" })
public class RateLimitingActionRejectMessage
{

    /**
     * Status-code of the reply
     * 
     */
    @JsonProperty("status")
    @JsonPropertyDescription("Status-code of the reply")
    private Integer status = 429;
    /**
     * A short, human-readable summary of the problem
     * 
     */
    @JsonProperty("title")
    @JsonPropertyDescription("A short, human-readable summary of the problem")
    private String title = "Too Many Requests";
    /**
     * The cause attribute in the json ProblemDetails element of HTTP error messages
     * (according to TS 29.500).
     * 
     */
    @JsonProperty("cause")
    @JsonPropertyDescription("The cause attribute in the json ProblemDetails element of HTTP error messages (according to TS 29.500).")
    private String cause = "NF_CONGESTION_RISK";
    /**
     * A human-readable explanation of the problem. Only used when the format
     * attribute has the value 'json'
     * 
     */
    @JsonProperty("detail")
    @JsonPropertyDescription("A human-readable explanation of the problem. Only used when the format attribute has the value 'json'")
    private String detail = "request_rate_limit";
    /**
     * Format of the error message. If the default value 'json' is used, the
     * response body is formatted according to RFC 7807. If the format value used is
     * 'text', a header 'content-type: text/plain' is added in the response body and
     * only the value of the title attribute is stored.
     * 
     */
    @JsonProperty("format")
    @JsonPropertyDescription("Format of the error message. If the default value 'json' is used, the response body is formatted according to RFC 7807. If the format value used is 'text', a header 'content-type: text/plain' is added in the response body and only the value of the title attribute is stored.")
    private RateLimitingActionRejectMessage.Format format = RateLimitingActionRejectMessage.Format.fromValue("json");
    /**
     * Format of the retry-after header. If the default value 'delay-seconds' is
     * used, the retry-after-header is formatted according to RFC 7231. If the
     * retry-after-header value used is 'HTTP-date', the corresponding format
     * according to RFC 7231 is used. If the value used is 'disabled' then the
     * retry-after-header is disabled
     * 
     */
    @JsonProperty("retry-after-header")
    @JsonPropertyDescription("Format of the retry-after header. If the default value 'delay-seconds' is used, the retry-after-header is formatted according to RFC 7231. If the retry-after-header value used is 'HTTP-date', the corresponding format according to RFC 7231 is used. If the value used is 'disabled' then the retry-after-header is disabled")
    private RateLimitingActionRejectMessage.RetryAfterHeader retryAfterHeader = RateLimitingActionRejectMessage.RetryAfterHeader.fromValue("delay-seconds");

    /**
     * Status-code of the reply
     * 
     */
    @JsonProperty("status")
    public Integer getStatus()
    {
        return status;
    }

    /**
     * Status-code of the reply
     * 
     */
    @JsonProperty("status")
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public RateLimitingActionRejectMessage withStatus(Integer status)
    {
        this.status = status;
        return this;
    }

    /**
     * A short, human-readable summary of the problem
     * 
     */
    @JsonProperty("title")
    public String getTitle()
    {
        return title;
    }

    /**
     * A short, human-readable summary of the problem
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title)
    {
        this.title = title;
    }

    public RateLimitingActionRejectMessage withTitle(String title)
    {
        this.title = title;
        return this;
    }

    /**
     * The cause attribute in the json ProblemDetails element of HTTP error messages
     * (according to TS 29.500).
     * 
     */
    @JsonProperty("cause")
    public String getCause()
    {
        return cause;
    }

    /**
     * The cause attribute in the json ProblemDetails element of HTTP error messages
     * (according to TS 29.500).
     * 
     */
    @JsonProperty("cause")
    public void setCause(String cause)
    {
        this.cause = cause;
    }

    public RateLimitingActionRejectMessage withCause(String cause)
    {
        this.cause = cause;
        return this;
    }

    /**
     * A human-readable explanation of the problem. Only used when the format
     * attribute has the value 'json'
     * 
     */
    @JsonProperty("detail")
    public String getDetail()
    {
        return detail;
    }

    /**
     * A human-readable explanation of the problem. Only used when the format
     * attribute has the value 'json'
     * 
     */
    @JsonProperty("detail")
    public void setDetail(String detail)
    {
        this.detail = detail;
    }

    public RateLimitingActionRejectMessage withDetail(String detail)
    {
        this.detail = detail;
        return this;
    }

    /**
     * Format of the error message. If the default value 'json' is used, the
     * response body is formatted according to RFC 7807. If the format value used is
     * 'text', a header 'content-type: text/plain' is added in the response body and
     * only the value of the title attribute is stored.
     * 
     */
    @JsonProperty("format")
    public RateLimitingActionRejectMessage.Format getFormat()
    {
        return format;
    }

    /**
     * Format of the error message. If the default value 'json' is used, the
     * response body is formatted according to RFC 7807. If the format value used is
     * 'text', a header 'content-type: text/plain' is added in the response body and
     * only the value of the title attribute is stored.
     * 
     */
    @JsonProperty("format")
    public void setFormat(RateLimitingActionRejectMessage.Format format)
    {
        this.format = format;
    }

    public RateLimitingActionRejectMessage withFormat(RateLimitingActionRejectMessage.Format format)
    {
        this.format = format;
        return this;
    }

    /**
     * Format of the retry-after header. If the default value 'delay-seconds' is
     * used, the retry-after-header is formatted according to RFC 7231. If the
     * retry-after-header value used is 'HTTP-date', the corresponding format
     * according to RFC 7231 is used. If the value used is 'disabled' then the
     * retry-after-header is disabled
     * 
     */
    @JsonProperty("retry-after-header")
    public RateLimitingActionRejectMessage.RetryAfterHeader getRetryAfterHeader()
    {
        return retryAfterHeader;
    }

    /**
     * Format of the retry-after header. If the default value 'delay-seconds' is
     * used, the retry-after-header is formatted according to RFC 7231. If the
     * retry-after-header value used is 'HTTP-date', the corresponding format
     * according to RFC 7231 is used. If the value used is 'disabled' then the
     * retry-after-header is disabled
     * 
     */
    @JsonProperty("retry-after-header")
    public void setRetryAfterHeader(RateLimitingActionRejectMessage.RetryAfterHeader retryAfterHeader)
    {
        this.retryAfterHeader = retryAfterHeader;
    }

    public RateLimitingActionRejectMessage withRetryAfterHeader(RateLimitingActionRejectMessage.RetryAfterHeader retryAfterHeader)
    {
        this.retryAfterHeader = retryAfterHeader;
        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(RateLimitingActionRejectMessage.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("status");
        sb.append('=');
        sb.append(((this.status == null) ? "<null>" : this.status));
        sb.append(',');
        sb.append("title");
        sb.append('=');
        sb.append(((this.title == null) ? "<null>" : this.title));
        sb.append(',');
        sb.append("cause");
        sb.append('=');
        sb.append(((this.cause == null) ? "<null>" : this.cause));
        sb.append(',');
        sb.append("detail");
        sb.append('=');
        sb.append(((this.detail == null) ? "<null>" : this.detail));
        sb.append(',');
        sb.append("format");
        sb.append('=');
        sb.append(((this.format == null) ? "<null>" : this.format));
        sb.append(',');
        sb.append("retryAfterHeader");
        sb.append('=');
        sb.append(((this.retryAfterHeader == null) ? "<null>" : this.retryAfterHeader));
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
        result = ((result * 31) + ((this.retryAfterHeader == null) ? 0 : this.retryAfterHeader.hashCode()));
        result = ((result * 31) + ((this.format == null) ? 0 : this.format.hashCode()));
        result = ((result * 31) + ((this.cause == null) ? 0 : this.cause.hashCode()));
        result = ((result * 31) + ((this.detail == null) ? 0 : this.detail.hashCode()));
        result = ((result * 31) + ((this.title == null) ? 0 : this.title.hashCode()));
        result = ((result * 31) + ((this.status == null) ? 0 : this.status.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if ((other instanceof RateLimitingActionRejectMessage) == false)
        {
            return false;
        }
        RateLimitingActionRejectMessage rhs = ((RateLimitingActionRejectMessage) other);
        return (((((((this.retryAfterHeader == rhs.retryAfterHeader) || ((this.retryAfterHeader != null) && this.retryAfterHeader.equals(rhs.retryAfterHeader)))
                    && ((this.format == rhs.format) || ((this.format != null) && this.format.equals(rhs.format))))
                   && ((this.cause == rhs.cause) || ((this.cause != null) && this.cause.equals(rhs.cause))))
                  && ((this.detail == rhs.detail) || ((this.detail != null) && this.detail.equals(rhs.detail))))
                 && ((this.title == rhs.title) || ((this.title != null) && this.title.equals(rhs.title))))
                && ((this.status == rhs.status) || ((this.status != null) && this.status.equals(rhs.status))));
    }

    public enum Format
    {

        TEXT("text"),
        JSON("json");

        private final String value;
        private final static Map<String, RateLimitingActionRejectMessage.Format> CONSTANTS = new HashMap<String, RateLimitingActionRejectMessage.Format>();

        static
        {
            for (RateLimitingActionRejectMessage.Format c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private Format(String value)
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
        public static RateLimitingActionRejectMessage.Format fromValue(String value)
        {
            RateLimitingActionRejectMessage.Format constant = CONSTANTS.get(value);
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

    public enum RetryAfterHeader
    {

        DELAY_SECONDS("delay-seconds"),
        HTTP_DATE("HTTP-date"),
        DISABLED("disabled");

        private final String value;
        private final static Map<String, RateLimitingActionRejectMessage.RetryAfterHeader> CONSTANTS = new HashMap<String, RateLimitingActionRejectMessage.RetryAfterHeader>();

        static
        {
            for (RateLimitingActionRejectMessage.RetryAfterHeader c : values())
            {
                CONSTANTS.put(c.value, c);
            }
        }

        private RetryAfterHeader(String value)
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
        public static RateLimitingActionRejectMessage.RetryAfterHeader fromValue(String value)
        {
            RateLimitingActionRejectMessage.RetryAfterHeader constant = CONSTANTS.get(value);
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
