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
 * Created on: Apr 15, 2021
 *     Author: eedstl
 */

package com.ericsson.utilities.test;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * Use an instance of this class to buffer the request headers and the body of a
 * RoutingContext which may be lost otherwise when the request is handled.
 */
public class RoutingContextBuffer
{
    private static final Logger log = LoggerFactory.getLogger(RoutingContextBuffer.class);

    public static RoutingContextBuffer of(final RoutingContext context)
    {
        return new RoutingContextBuffer(context.request()
                                               .headers()
                                               .entries()
                                               .stream()
                                               .collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toList()))),
                                        context.getBodyAsString());
    }

    private final Map<String, List<String>> headers;
    private final String body;

    private RoutingContextBuffer(final Map<String, List<String>> headers,
                                 final String body)
    {
        this.headers = headers;
        this.body = body;

        log.info("headers={}, body={}", this.headers, this.body);
    }

    /**
     * @return The body.
     */
    public String getBody()
    {
        return this.body;
    }

    /**
     * @param name The name of the header.
     * @return The first value of header {@code name} or {@code null} if there is no
     *         header or it does not have a value.
     */
    public String getHeader(final String name)
    {
        if (this.headers == null)
            return null;

        final List<String> values = this.headers.get(name);

        if (values == null || values.isEmpty())
            return null;

        return values.get(0);
    }

    /**
     * @return All the headers. Please note that there can be more than one value
     *         per header.
     */
    public Map<String, List<String>> getHeaders()
    {
        return this.headers;
    }

    @Override
    public String toString()
    {
        return new StringBuilder().append("{").append("headers=").append(this.headers).append(", body=").append(this.body).toString();
    }
}
