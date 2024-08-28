/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 17, 2024
 *     Author: ztsakon
 */

package com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig;

import java.util.Objects;
import java.util.Optional;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ActionOnFailure;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.MessageBodyType;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RejectMessageAction;

/**
 * 
 */
public class ProxyActionOnFailure
{
    private ActionOnFailureType type;
    private Integer status;
    private Optional<String> title = Optional.empty();
    private Optional<String> cause = Optional.empty();
    private Optional<String> detail = Optional.empty();
    private String format;

    /**
     * @param type
     * @param status
     * @param title
     * @param cause
     * @param detail
     * @param format
     */
    public ProxyActionOnFailure(ActionOnFailureType type,
                                Integer status,
                                Optional<String> title,
                                Optional<String> cause,
                                Optional<String> detail,
                                String format)
    {
        super();
        this.type = type;
        this.status = status;
        this.title = title;
        this.cause = cause;
        this.detail = detail;
        this.format = format;
    }

    /**
     * @param proxyActionOnFailure
     */
    public ProxyActionOnFailure(ProxyActionOnFailure proxyActionOnFailure)
    {
        super();
        this.type = proxyActionOnFailure.getType();
        this.status = proxyActionOnFailure.getStatus();
        this.title = proxyActionOnFailure.getTitle();
        this.cause = proxyActionOnFailure.getCause();
        this.detail = proxyActionOnFailure.getDetail();
        this.format = proxyActionOnFailure.getFormat();
    }

    /**
     * @return the type
     */
    public ActionOnFailureType getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(ActionOnFailureType type)
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyActionOnFailure [type=" + type + ", status=" + status + ", title=" + title + ", cause=" + cause + ", detail=" + detail + ", format="
               + format + "]";
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
        ProxyActionOnFailure other = (ProxyActionOnFailure) obj;
        return Objects.equals(cause, other.cause) && Objects.equals(detail, other.detail) && Objects.equals(format, other.format)
               && Objects.equals(status, other.status) && Objects.equals(title, other.title) && type == other.type;
    }

    public ActionOnFailure buildActionOnFailure()
    {
        if (type.equals(ActionOnFailureType.FRW_MODIFIED))
        {
            return ActionOnFailure.newBuilder().setRemoveDeniedHeaders(true).build();
        }
        if (type.equals(ActionOnFailureType.FRW_UNMODIFIED))
        {
            return ActionOnFailure.newBuilder().setForwardUnmodifiedMessage(true).build();
        }
        if (type.equals(ActionOnFailureType.DROP))
        {
            return ActionOnFailure.newBuilder().setDropMessage(true).build();
        }

        MessageBodyType mbtFormat;
        if (this.getFormat().equals("json"))
            mbtFormat = MessageBodyType.JSON;
        else
            mbtFormat = MessageBodyType.PLAIN_TEXT;

        var actionBuilder = RejectMessageAction.newBuilder();
        this.getTitle().ifPresent(actionBuilder::setTitle);
        this.getCause().ifPresent(actionBuilder::setCause);
        this.getDetail().ifPresent(actionBuilder::setDetail);
        actionBuilder.setStatus(this.getStatus()).setMessageFormat(mbtFormat);

        return ActionOnFailure.newBuilder().setRespondWithError(actionBuilder).build();
    }

    public enum ActionOnFailureType
    {
        REJECT,
        DROP,
        FRW_MODIFIED,
        FRW_UNMODIFIED
    }
}
