/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Dec 13, 2018
 *     Author: eedstl
 */

package com.ericsson.utilities.http;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Aggregation of HTTP status and detailed information about the cause. Can be
 * used in HTTP responses (setStatusMessage()).
 */
public class HttpResult
{
    private final HttpResponseStatus status;
    private final String cause;

    public HttpResult(HttpResponseStatus status)
    {
        this.status = status;
        this.cause = "";
    }

    public HttpResult(HttpResponseStatus status,
                      String cause)
    {
        this.status = status;
        this.cause = cause;
    }

    String cause()
    {
        return this.cause;
    }

    HttpResponseStatus status()
    {
        return this.status;
    }

    public String toString()
    {
        StringBuilder b = new StringBuilder();
        b.append(this.status());

        if (!this.cause().isEmpty())
            b.append(". Cause: ").append(this.cause());

        return b.toString();
    }
}
