/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property of Ericsson
 * GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written permission of
 * Ericsson GmbH in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Created on: Oct 09, 2023 Author: zstoioa
 */

package com.ericsson.esc.bsf.worker;

import java.util.List;

import com.ericsson.utilities.http.WebServerPool;

public class ServerContainer
{
    public enum ServerTag
    {
        IPV4("IPv4"),
        IPV4TLS("IPv4 TLS"),
        IPV6("IPv6"),
        IPV6TLS("IPv6 TLS");

        private final String description;

        private ServerTag(String description)
        {
            this.description = description;
        }

        String getDescription()
        {
            return this.description;
        }
    }

    private final ServerTag tag;
    private final WebServerPool serverPool;
    private final List<NBsfManagementHandler> handlers;

    public ServerContainer(ServerTag tag,
                           WebServerPool serverPool,
                           List<NBsfManagementHandler> handlers)
    {
        this.tag = tag;
        this.serverPool = serverPool;
        this.handlers = handlers;
    }

    @Override
    public String toString()
    {
        return "ServerContainer [tag=" + tag + ", serverPool=" + serverPool + ", handlers=" + handlers + "]";
    }

    public ServerTag getTag()
    {
        return tag;
    }

    public WebServerPool getServerPool()
    {
        return serverPool;
    }

    public List<NBsfManagementHandler> getHandlers()
    {
        return handlers;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        result = prime * result + ((serverPool == null) ? 0 : serverPool.hashCode());
        result = prime * result + ((handlers == null) ? 0 : handlers.hashCode());
        return result;
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
        ServerContainer other = (ServerContainer) obj;
        if (tag != other.tag)
            return false;
        if (serverPool == null)
        {
            if (other.serverPool != null)
                return false;
        }
        else if (!serverPool.equals(other.serverPool))
            return false;
        if (handlers == null)
        {
            if (other.handlers != null)
                return false;
        }
        else if (!handlers.equals(other.handlers))
            return false;
        return true;
    }

}
