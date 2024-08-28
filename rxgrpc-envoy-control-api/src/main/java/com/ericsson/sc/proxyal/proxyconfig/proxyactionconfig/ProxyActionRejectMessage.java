/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 5, 2021
 *     Author: eaoknkr
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig;

import java.util.Objects;
import java.util.Optional;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Action;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.MessageBodyType;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RejectMessageAction;
import io.reactivex.annotations.NonNull;

/**
 * 
 */
public class ProxyActionRejectMessage implements ProxyAction
{
    private final Integer status;
    private final Optional<String> title;
    private final Optional<String> detail;
    private final Optional<String> cause;
    private final String format;

    /**
     * @param name
     * @param key
     * @param tableLookup
     */
    public ProxyActionRejectMessage(Integer status,
                                    @NonNull Optional<String> title,
                                    @NonNull Optional<String> detail,
                                    @NonNull Optional<String> cause,
                                    String format)
    {
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.cause = cause;
        this.format = format;
    }

    public ProxyActionRejectMessage(ProxyActionRejectMessage arm)
    {
        this.status = arm.getStatus();
        this.title = arm.getTitle().map(v -> v);
        this.detail = arm.getDetail().map(v -> v);
        this.cause = arm.getCause().map(v -> v);
        this.format = arm.getFormat();
    }

    /**
     * @return the status
     */
    public Integer getStatus()
    {
        return status;
    }

    /**
     * @return the title
     */
    public Optional<String> getTitle()
    {
        return title;
    }

    /**
     * @return the cause
     */
    public Optional<String> getCause()
    {
        return cause;
    }

    /**
     * @return the detail
     */
    public Optional<String> getDetail()
    {
        return detail;
    }

    /**
     * @return the format
     */
    public String getFormat()
    {
        return format;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyActionRejectMessage [status=" + status + ", title=" + title + ", cause=" + cause + ", detail=" + detail + ", format=" + format + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(status, title, cause, detail, format);
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

        ProxyActionRejectMessage other = (ProxyActionRejectMessage) obj;

        return Objects.equals(status, other.status) && Objects.equals(title, other.title) && Objects.equals(cause, other.cause)
               && Objects.equals(detail, other.detail) && Objects.equals(format, other.format);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.proxyal.proxyconfig.ProxyAction#buildAction()
     */
    @Override
    public Action buildAction()
    {
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

        return Action.newBuilder().setActionRejectMessage(actionBuilder).build();

    }

}
