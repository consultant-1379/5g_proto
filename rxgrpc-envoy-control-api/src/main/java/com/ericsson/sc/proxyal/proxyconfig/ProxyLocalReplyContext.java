/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 16, 2020
 *     Author: echaias
 */

package com.ericsson.sc.proxyal.proxyconfig;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */

public class ProxyLocalReplyContext
{
    private static final Logger log = LoggerFactory.getLogger(ProxyLocalReplyContext.class);

    public enum LocalReplyMappings
    {
        RC_503_NO_HEALTHY_UPSTREAM(503),
        RC_504_GATEWAY_TIMEOUT(504);

        public final int rc;

        LocalReplyMappings(int rc)
        {
            this.rc = rc;
        }

    }

    private final LocalReplyMappings type;
    private final int status;
    private final String title;
    private final String format;
    private final String internalErrorMessage;
    private final String gatewayErrorMessage;

    /**
     * 
     */
    public ProxyLocalReplyContext(LocalReplyMappings type,
                                  int status,
                                  String title,
                                  String format,
                                  String internalErrorMessage,
                                  String gatewayErrorMessage)
    {
        this.type = type;
        this.status = status;
        this.title = title;
        this.format = format;
        this.internalErrorMessage = internalErrorMessage;
        this.gatewayErrorMessage = gatewayErrorMessage;
    }

    /**
     * @return the type
     */
    public LocalReplyMappings getType()
    {
        return type;
    }

    /**
     * @return the status
     */
    public int getStatus()
    {
        return status;
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @return the title
     */
    public String getFormat()
    {
        return format;
    }

    public String toJsonFormat()
    {
        return String.format("{\"title\": \"%s\", \"status\": \"%d\"}", title, status);

    }

    public boolean isJsonFormat()
    {
        if (type.equals(LocalReplyMappings.RC_503_NO_HEALTHY_UPSTREAM))
        {
            return format.equals(this.internalErrorMessage);
        }
        else if (type.equals(LocalReplyMappings.RC_504_GATEWAY_TIMEOUT))
        {
            // GatewayErrorMessage.Format.JSON.value()
            return format.equals(this.gatewayErrorMessage);
        }
        return false;
    }

    @Override
    public String toString()
    {
        if (format.equals("application/json"))
            return this.toJsonFormat();
        else
            return this.title;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyLocalReplyContext other = (ProxyLocalReplyContext) obj;

        if (type == null)
        {
            if (other.type != null)
                return false;
        }
        else if (!type.equals(other.type))
            return false;

        log.trace("LocalReplyContext type equal");

        if (status != other.status)
            return false;

        log.trace("LocalReplyContext status equal");

        if (title == null)
        {
            if (other.title != null)
                return false;
        }
        else if (!title.equals(other.title))
            return false;

        log.trace("LocalReplyContext title equal");

        if (format == null)
        {
            if (other.format != null)
                return false;
        }
        else if (!format.equals(other.format))
            return false;

        log.trace("LocalReplyContext format equal");
        log.trace("LocalReplyContext all fields equal");
        return true;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type, status, title, format);
    }
}
