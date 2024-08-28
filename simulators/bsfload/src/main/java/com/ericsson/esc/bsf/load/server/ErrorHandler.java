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
 * Created on: Nov 24, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * Provides generic error responses.
 */
public class ErrorHandler
{
    private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
    private static final JsonMapper jm = JsonMapper.builder().serializationInclusion(Include.NON_NULL).build();

    private ErrorHandler()
    {

    }

    public static Completable sendErrorResponse(RoutingContext routingContext,
                                                int statusCode,
                                                ProblemDetails problemDetails)
    {
        return Completable.defer(() ->
        {
            log.info("Sending error response with status: {}, problemDetails: {}", statusCode, problemDetails);

            routingContext.response().setStatusCode(statusCode);
            final var problemDetailsBytes = jm.writeValueAsBytes(problemDetails);
            return routingContext.response().rxEnd(Buffer.buffer(problemDetailsBytes));
        });
    }

    public static Completable sendNotFoundErrorResponse(RoutingContext routingContext)
    {
        return sendErrorResponse(routingContext,
                                 HttpResponseStatus.NOT_FOUND.code(),
                                 ProblemDetails.withDetail("Resource not found", "The requested workload does not exist"));
    }

    public static Completable sendConflictErrorResponse(RoutingContext routingContext)
    {
        return sendErrorResponse(routingContext,
                                 HttpResponseStatus.CONFLICT.code(),
                                 ProblemDetails.withDetail("State conflict", "The current state of the workload is not compatible with this operation"));
    }

    public static Completable sendInternalServerErrorResponse(RoutingContext routingContext)
    {
        return sendErrorResponse(routingContext,
                                 HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                                 ProblemDetails.withDetail("Unexpected error", "An undefined server error occurred"));
    }

    public static Completable sendInternalServerErrorResponse(RoutingContext routingContext,
                                                              Throwable cause)
    {
        return sendErrorResponse(routingContext,
                                 HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                                 ProblemDetails.withCause("Unexpected error", "An undefined server error occurred", cause.toString()));
    }

    public static Completable genericErrorHandler(RoutingContext routingContext,
                                                  Throwable t)
    {
        if (t instanceof IllegalArgumentException)
        {
            return ErrorHandler.sendErrorResponse(routingContext,
                                                  HttpResponseStatus.BAD_REQUEST.code(),
                                                  ProblemDetails.withCause("Invalid parameter", t.getLocalizedMessage(), t.getCause().toString()));
        }
        else
        {
            return ErrorHandler.sendInternalServerErrorResponse(routingContext, t);
        }
    }
}
