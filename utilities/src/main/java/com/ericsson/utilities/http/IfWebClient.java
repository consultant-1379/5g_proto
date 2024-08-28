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
 * Created on: Feb 14, 2020
 *     Author: eedstl
 */

package com.ericsson.utilities.http;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;

/**
 * 
 */
public interface IfWebClient
{
    String toString();

    boolean equals(Object o);

    int hashCode();

    /**
     * Create an HTTP request to send to the server at the specified host and port.
     * 
     * @param method     the HTTP method
     * @param port       the port
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> request(HttpMethod method,
                                                                                                 int port,
                                                                                                 String host,
                                                                                                 String requestURI);

    /**
     * Like {@link io.vertx.reactivex.ext.web.client.WebClient#request} using the
     * <code>serverAddress</code> parameter to connect to the server instead of the
     * <code>port</code> and <code>host</code> parameters.
     * <p>
     * The request host header will still be created from the <code>port</code> and
     * <code>host</code> parameters.
     * <p>
     * Use to connect to a unix domain socket server.
     * 
     * @param method
     * @param serverAddress
     * @param port
     * @param host
     * @param requestURI
     * @return
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> request(HttpMethod method,
                                                                                                 io.vertx.reactivex.core.net.SocketAddress serverAddress,
                                                                                                 int port,
                                                                                                 String host,
                                                                                                 String requestURI);

    /**
     * Create an HTTP request to send to the server at the specified host and
     * default port.
     * 
     * @param method     the HTTP method
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> request(HttpMethod method,
                                                                                                 String host,
                                                                                                 String requestURI);

    /**
     * Like {@link io.vertx.reactivex.ext.web.client.WebClient#request} using the
     * <code>serverAddress</code> parameter to connect to the server instead of the
     * default port and <code>host</code> parameter.
     * <p>
     * The request host header will still be created from the default port and
     * <code>host</code> parameter.
     * <p>
     * Use to connect to a unix domain socket server.
     * 
     * @param method
     * @param serverAddress
     * @param host
     * @param requestURI
     * @return
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> request(HttpMethod method,
                                                                                                 io.vertx.reactivex.core.net.SocketAddress serverAddress,
                                                                                                 String host,
                                                                                                 String requestURI);

    /**
     * Create an HTTP request to send to the server at the default host and port.
     * 
     * @param method     the HTTP method
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> request(HttpMethod method,
                                                                                                 String requestURI);

    /**
     * Like {@link io.vertx.reactivex.ext.web.client.WebClient#request} using the
     * <code>serverAddress</code> parameter to connect to the server instead of the
     * default port and default host.
     * <p>
     * The request host header will still be created from the default port and
     * default host.
     * <p>
     * Use to connect to a unix domain socket server.
     * 
     * @param method
     * @param serverAddress
     * @param requestURI
     * @return
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> request(HttpMethod method,
                                                                                                 io.vertx.reactivex.core.net.SocketAddress serverAddress,
                                                                                                 String requestURI);

    /**
     * Create an HTTP request to send to the server at the specified host and port.
     * 
     * @param method  the HTTP method
     * @param options the request options
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> request(HttpMethod method,
                                                                                                 RequestOptions options);

    /**
     * Like {@link io.vertx.reactivex.ext.web.client.WebClient#request} using the
     * <code>serverAddress</code> parameter to connect to the server instead of the
     * <code>options</code> parameter.
     * <p>
     * The request host header will still be created from the <code>options</code>
     * parameter.
     * <p>
     * Use to connect to a unix domain socket server.
     * 
     * @param method
     * @param serverAddress
     * @param options
     * @return
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> request(HttpMethod method,
                                                                                                 io.vertx.reactivex.core.net.SocketAddress serverAddress,
                                                                                                 RequestOptions options);

    /**
     * Create an HTTP request to send to the server using an absolute URI
     * 
     * @param method      the HTTP method
     * @param absoluteURI the absolute URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> requestAbs(HttpMethod method,
                                                                                                    String absoluteURI);

    /**
     * Like {@link io.vertx.reactivex.ext.web.client.WebClient#requestAbs} using the
     * <code>serverAddress</code> parameter to connect to the server instead of the
     * <code>absoluteURI</code> parameter.
     * <p>
     * The request host header will still be created from the
     * <code>absoluteURI</code> parameter.
     * <p>
     * Use to connect to a unix domain socket server.
     * 
     * @param method
     * @param serverAddress
     * @param absoluteURI
     * @return
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> requestAbs(HttpMethod method,
                                                                                                    io.vertx.reactivex.core.net.SocketAddress serverAddress,
                                                                                                    String absoluteURI);

    /**
     * Create an HTTP GET request to send to the server at the default host and
     * port.
     * 
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> get(String requestURI);

    /**
     * Create an HTTP GET request to send to the server at the specified host and
     * port.
     * 
     * @param port       the port
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> get(int port,
                                                                                             String host,
                                                                                             String requestURI);

    /**
     * Create an HTTP GET request to send to the server at the specified host and
     * default port.
     * 
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> get(String host,
                                                                                             String requestURI);

    /**
     * Create an HTTP GET request to send to the server using an absolute URI,
     * specifying a response handler to receive the response
     * 
     * @param absoluteURI the absolute URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> getAbs(String absoluteURI);

    /**
     * Create an HTTP POST request to send to the server at the default host and
     * port.
     * 
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> post(String requestURI);

    /**
     * Create an HTTP POST request to send to the server at the specified host and
     * port.
     * 
     * @param port       the port
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> post(int port,
                                                                                              String host,
                                                                                              String requestURI);

    /**
     * Create an HTTP POST request to send to the server at the specified host and
     * default port.
     * 
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> post(String host,
                                                                                              String requestURI);

    /**
     * Create an HTTP POST request to send to the server using an absolute URI,
     * specifying a response handler to receive the response
     * 
     * @param absoluteURI the absolute URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> postAbs(String absoluteURI);

    /**
     * Create an HTTP PUT request to send to the server at the default host and
     * port.
     * 
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> put(String requestURI);

    /**
     * Create an HTTP PUT request to send to the server at the specified host and
     * port.
     * 
     * @param port       the port
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> put(int port,
                                                                                             String host,
                                                                                             String requestURI);

    /**
     * Create an HTTP PUT request to send to the server at the specified host and
     * default port.
     * 
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> put(String host,
                                                                                             String requestURI);

    /**
     * Create an HTTP PUT request to send to the server using an absolute URI,
     * specifying a response handler to receive the response
     * 
     * @param absoluteURI the absolute URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> putAbs(String absoluteURI);

    /**
     * Create an HTTP DELETE request to send to the server at the default host and
     * port.
     * 
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> delete(String requestURI);

    /**
     * Create an HTTP DELETE request to send to the server at the specified host and
     * port.
     * 
     * @param port       the port
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> delete(int port,
                                                                                                String host,
                                                                                                String requestURI);

    /**
     * Create an HTTP DELETE request to send to the server at the specified host and
     * default port.
     * 
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> delete(String host,
                                                                                                String requestURI);

    /**
     * Create an HTTP DELETE request to send to the server using an absolute URI,
     * specifying a response handler to receive the response
     * 
     * @param absoluteURI the absolute URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> deleteAbs(String absoluteURI);

    /**
     * Create an HTTP PATCH request to send to the server at the default host and
     * port.
     * 
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> patch(String requestURI);

    /**
     * Create an HTTP PATCH request to send to the server at the specified host and
     * port.
     * 
     * @param port       the port
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> patch(int port,
                                                                                               String host,
                                                                                               String requestURI);

    /**
     * Create an HTTP PATCH request to send to the server at the specified host and
     * default port.
     * 
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> patch(String host,
                                                                                               String requestURI);

    /**
     * Create an HTTP PATCH request to send to the server using an absolute URI,
     * specifying a response handler to receive the response
     * 
     * @param absoluteURI the absolute URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> patchAbs(String absoluteURI);

    /**
     * Create an HTTP HEAD request to send to the server at the default host and
     * port.
     * 
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> head(String requestURI);

    /**
     * Create an HTTP HEAD request to send to the server at the specified host and
     * port.
     * 
     * @param port       the port
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> head(int port,
                                                                                              String host,
                                                                                              String requestURI);

    /**
     * Create an HTTP HEAD request to send to the server at the specified host and
     * default port.
     * 
     * @param host       the host
     * @param requestURI the relative URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> head(String host,
                                                                                              String requestURI);

    /**
     * Create an HTTP HEAD request to send to the server using an absolute URI,
     * specifying a response handler to receive the response
     * 
     * @param absoluteURI the absolute URI
     * @return an HTTP client request object
     */
    io.vertx.reactivex.ext.web.client.HttpRequest<io.vertx.reactivex.core.buffer.Buffer> headAbs(String absoluteURI);

    /**
     * Close the client. Closing will close down any pooled connections. Clients
     * should always be closed after use.
     */
    void close(boolean reopen);
}