/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 10, 2022
 *     Author: eodnouk
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import java.util.Objects;
import java.util.Optional;

import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.ActionProfile;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.ActionProfile.ActionRejectMessage;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.MessageBodyType;
import io.envoyproxy.envoy.extensions.filters.http.eric_ingress_ratelimit.v3.RetryAfterHeaderFormat;

/**
 * 
 */
public class ProxyRateLimitActionProfile
{
    private Type type;
    private Integer status;
    private Optional<String> title = Optional.empty();
    private Optional<String> cause = Optional.empty();
    private Optional<String> detail = Optional.empty();
    private String format;
    private String retryAfterHeader;

    /**
     * @param type
     * @param status
     * @param title
     * @param detail
     * @param format
     * @param retryAfterHeader
     */
    public ProxyRateLimitActionProfile(Type type,
                                       Integer status,
                                       Optional<String> title,
                                       Optional<String> detail,
                                       Optional<String> cause,
                                       String format,
                                       String retryAfterHeader)
    {
        this.type = type;
        this.status = status;
        this.title = title;
        this.cause = cause;
        this.detail = detail;
        this.format = format;
        this.retryAfterHeader = retryAfterHeader;
    }

    /**
     * Copy constructor
     * 
     * @param anotherRlActionProfile
     */
    public ProxyRateLimitActionProfile(ProxyRateLimitActionProfile anotherRlActionProfile)
    {
        this.type = anotherRlActionProfile.getType();
        this.status = anotherRlActionProfile.getStatus();
        this.title = anotherRlActionProfile.getTitle();
        this.detail = anotherRlActionProfile.getDetail();
        this.format = anotherRlActionProfile.getFormat();
        this.retryAfterHeader = anotherRlActionProfile.getRetryAfterHeader();
    }

    /**
     * @param type
     */
    public ProxyRateLimitActionProfile(Type type)
    {
        this.type = type;
    }

    /**
     * @return the type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type)
    {
        this.type = type;
    }

    /**
     * @return the status
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Integer status)
    {
        this.status = status;
    }

    /**
     * @return the title
     */
    public Optional<String> getTitle()
    {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(Optional<String> title)
    {
        this.title = title;
    }

    /**
     * @return the cause
     */
    public Optional<String> getCause()
    {
        return cause;
    }

    /**
     * @param cause the cause to set
     */
    public void setCause(Optional<String> cause)
    {
        this.cause = cause;
    }

    /**
     * @return the detail
     */
    public Optional<String> getDetail()
    {
        return detail;
    }

    /**
     * @param detail the detail to set
     */
    public void setDetail(Optional<String> detail)
    {
        this.detail = detail;
    }

    /**
     * @return the format
     */
    public String getFormat()
    {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * @return the retry-after-header
     */
    public String getRetryAfterHeader()
    {
        return retryAfterHeader;
    }

    /**
     * @param format the retry-after-header to set
     */
    public void setRetryAfterHeader(String retryAfterHeader)
    {
        this.retryAfterHeader = retryAfterHeader;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(cause, detail, format, status, title, type);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyRateLimitActionProfile other = (ProxyRateLimitActionProfile) obj;
        return Objects.equals(cause, other.cause) && Objects.equals(detail, other.detail) && Objects.equals(format, other.format)
               && Objects.equals(status, other.status) && Objects.equals(title, other.title) && type == other.type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "RateLimitActionProfile [type=" + type + ", status=" + status + ", title=" + title + ", cause=" + cause + ", detail=" + detail + ", format="
               + format + "]";
    }

    public ActionProfile buildAction()
    {
        if (type.equals(Type.DROP))
        {
            return ActionProfile.newBuilder().setActionDropMessage(true).build();
        }
        if (type.equals(Type.PASS))
        {
            return ActionProfile.newBuilder().setActionPassMessage(true).build();
        }

        MessageBodyType mbtFormat;
        if (this.getFormat().equals("json"))
            mbtFormat = MessageBodyType.JSON;
        else
            mbtFormat = MessageBodyType.PLAIN_TEXT;

        RetryAfterHeaderFormat retryAfterHeaderFormat;
        if (this.getRetryAfterHeader().equals("delay-seconds"))
        {
            retryAfterHeaderFormat = RetryAfterHeaderFormat.SECONDS;
        }
        else if (this.getRetryAfterHeader().equals("HTTP-date"))
        {
            retryAfterHeaderFormat = RetryAfterHeaderFormat.HTTP_DATE;
        }
        else
            retryAfterHeaderFormat = RetryAfterHeaderFormat.DISABLED;

        var actionBuilder = ActionRejectMessage.newBuilder();
        this.getTitle().ifPresent(actionBuilder::setTitle);
        this.getCause().ifPresent(actionBuilder::setCause);
        this.getDetail().ifPresent(actionBuilder::setDetail);

        actionBuilder.setRetryAfterHeader(retryAfterHeaderFormat);

        actionBuilder.setStatus(this.getStatus()).setMessageFormat(mbtFormat);

        return ActionProfile.newBuilder().setActionRejectMessage(actionBuilder).build();
    }

    public enum Type
    {
        REJECT,
        DROP,
        PASS
    }
}
