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
 * Created on: Mar 24, 2020
 *     Author: eedstl
 */

package com.ericsson.utilities.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.reactivex.core.net.SocketAddress;

/**
 * Encapsulates an URL and its resolved IP address.
 */
public class Url
{
    private static final Logger log = LoggerFactory.getLogger(Url.class);

    /**
     * Merges absUrlPatch in absUrlBase and returns the result, i.e. missing parts
     * in absUrlBase are patched with the corresponding parts of absUrlPatch.
     * 
     * @param absUrlBase  Absolute URL that is the base for the merge.
     * @param absUrlPatch Absolute URL that is merged in absUrlBase.
     * @return The resulting URL.
     * @throws MalformedURLException
     */
    public static String merge(final String absUrlBase,
                               final String absUrlPatch) throws MalformedURLException
    {
        final URL urlBase = URI.create(absUrlBase).toURL();
        final URL urlPatch = URI.create(absUrlPatch).toURL();

        String host;
        int port;

        if (urlBase.getHost() == null || urlBase.getHost().isEmpty())
        {
            host = urlPatch.getHost();
            port = urlPatch.getPort();
        }
        else
        {
            host = urlBase.getHost();
            port = urlBase.getPort();
        }

        if (port < 0)
            port = urlBase.getProtocol().equalsIgnoreCase("https") ? 443 : 80;

        final String query = urlBase.getQuery() == null ? urlPatch.getQuery() : urlBase.getQuery();
        final String file = query == null ? urlBase.getPath() : urlBase.getPath() + "?" + query;

        return new URL(urlBase.getProtocol(), host, port, file).toString();
    }

    private final SocketAddress addr;
    private final URL url;

    public Url(final String host,
               final Integer port,
               final String path)
    {
        this(host, port, path, null);
    }

    public Url(final String host,
               final Integer port,
               final String path,
               final String ip)
    {
        this("http", host, port, path, ip);
    }

    public Url(final String protocol,
               final String host,
               final Integer port,
               final String path)
    {
        this(protocol, host, port, path, null);
    }

    public Url(String protocol,
               String host,
               Integer port,
               String file, // path + query
               String ip)
    {
        protocol = protocol != null && !protocol.strip().isEmpty() ? protocol.strip() : "http";

        if (host != null)
        {
            host = host.strip();
            host = host.matches("^\\[.+\\]$") ? host.substring(1, host.length() - 1) : host; // "[a.b]" -> "a.b"
        }

        port = port != null && port >= 0 ? port : -1 /* use default port for protocol given */;

        file = file.strip();

        if (ip != null)
        {
            ip = ip.strip();
            ip = ip.matches("^\\[.+\\]$") ? ip.substring(1, ip.length() - 1) : ip; // "[1:2]" -> "1:2"
        }

        if (host == null)
        {
            host = ip;
        }
        else if (ip == null || ip.isEmpty())
        {
            ip = host;
        }

        try
        {
            this.url = new URL(protocol, host, port, file);
            this.addr = SocketAddress.inetSocketAddress(port, ip);
        }
        catch (Exception e)
        {
            log.error("Could not create URL for protocol='{}', host='{}', port='{}', path='{}', ip={}. Cause: {}",
                      protocol,
                      host,
                      port,
                      file,
                      ip,
                      e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Url(final URL url)
    {
        this(url.getProtocol(), url.getHost(), url.getPort(), url.getFile(), null);
    }

    public Url(final URL url,
               final SocketAddress addr)
    {
        this.url = url;
        this.addr = addr;
    }

    @Override
    public boolean equals(final Object rhs)
    {
        if (this == rhs)
            return true;

        if (rhs == null)
            return false;

        return this.toString().equals(rhs.toString());
    }

    public SocketAddress getAddr()
    {
        return this.addr;
    }

    public URL getUrl()
    {
        return this.url;
    }

    @Override
    public int hashCode()
    {
        return this.toString().hashCode();
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append("{ ")
                                  .append("url=")
                                  .append(this.url.toString())
                                  .append(", addr=")
                                  .append(this.addr.toString())
                                  .append(" }")
                                  .toString();
    }
}
