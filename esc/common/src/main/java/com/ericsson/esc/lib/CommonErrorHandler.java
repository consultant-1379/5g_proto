/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 20, 2019
 *     Author: xchrfar
 */
package com.ericsson.esc.lib;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Completable;
import io.vertx.core.http.HttpHeaders;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.RoutingContext;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;

public class CommonErrorHandler
{
    // log limiter labels
    private enum Lbl
    {
        INTERNAL_ERROR,
        FAILED_RESPONSE
    }

    private CommonErrorHandler()
    {
    }

    private static final ObjectMapper mapper = Jackson.om();
    private static final Logger log = LoggerFactory.getLogger(CommonErrorHandler.class);
    /*
     * Rate limits logging to 2 logs/sec
     */
    private static final Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log);

    public static Completable sendErrorResponse(RoutingContext rc,
                                                Throwable t)
    {
        return Completable.defer(() ->
        {
            if (!(t instanceof BadRequestException))
            {
                // log exceptions not related to input validation
                safeLog.log(Lbl.INTERNAL_ERROR, logger -> logger.warn("Failed to handle HTTP request due to internal error", t));
            }
            final var pd = generateDetails(t);
            rc.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.PROBLEM_JSON);
            // ProblemDetails.status should not be null
            rc.response().setStatusCode(pd.getStatus());
            return rc.response().rxEnd(Buffer.buffer(mapper.writeValueAsBytes(pd)));
        }) //
                          .doOnError(err -> safeLog.log(Lbl.FAILED_RESPONSE, logger -> logger.warn("Failed to send HTTP error response to client", err)))
                          .onErrorComplete();
    }

    private static ProblemDetails generateDetails(Throwable t)
    {
        if (t instanceof ValidationException)
        {
            final ValidationException ve = (ValidationException) t;
            return generateProblemDetails(ve);
        }
        else if (t instanceof MalformedMessageException)
        {
            return CommonError.INVALID_MSG_FORMAT.problemDetails(Optional.ofNullable(t.getMessage()), Optional.empty());
        }
        else if (t instanceof TooManyRequestsException)
        {
            return CommonError.NF_CONGESTION_RISK.problemDetails(Optional.ofNullable(t.getMessage()), Optional.empty());
        }
        else if (t instanceof DbMissingPermissionsException)
        {
            return CommonError.INSUFFICIENT_RESOURCES.problemDetails(Optional.ofNullable(t.getMessage()), Optional.empty());
        }
        else
        {
            return CommonError.UNSPECIFIED_NF_FAILURE.problemDetails(Optional.empty(), Optional.empty());
        }
    }

    public static Completable sendErrorResponse(HttpServerResponse response,
                                                Optional<String> problemDetails)
    {
        return sendErrorResponse(response, null, problemDetails, Optional.empty());
    }

    public static Completable sendErrorResponse(HttpServerResponse response,
                                                CommonError commonError)
    {
        return sendErrorResponse(response, commonError, Optional.empty(), Optional.empty());
    }

    public static Completable sendErrorResponse(HttpServerResponse response,
                                                CommonError commonError,
                                                Optional<String> problemDetails)
    {
        return sendErrorResponse(response, commonError, problemDetails, Optional.empty());
    }

    public static Completable sendErrorResponse(HttpServerResponse response,
                                                CommonError commonError,
                                                Optional<String> details,
                                                Optional<List<InvalidParam>> invalidParams)
    {
        return Completable.defer(() ->
        {
            response.putHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.PROBLEM_JSON);
            // ProblemDetails.status should not be null

            ProblemDetails pd;
            if (commonError != null)
            {
                pd = commonError.problemDetails(details, invalidParams);
                response.setStatusCode(pd.getStatus());
            }
            else
            {
                // status code of the response is supposed to be already set
                pd = new ProblemDetails();
                pd.setDetail(details.orElseThrow());
                pd.setStatus(response.getStatusCode());
                pd.setTitle(response.getStatusMessage());
            }

            return response.rxEnd(Buffer.buffer(mapper.writeValueAsBytes(pd)));
        }) //
                          .doOnError(err -> log.error("Failed to send HTTP error response to client", err))
                          .onErrorComplete();
    }

    public static ProblemDetails generateProblemDetails(ValidationException ve)
    {
        switch (ve.getErrorType())
        {
            case SYNTAX_ERROR:
                return ve.isQueryError() ? CommonError.MANDATORY_QUERY_PARAM_INCORRECT.problemDetails(Optional.ofNullable(ve.getMessage()),
                                                                                                      Optional.ofNullable(ve.getInvalidParams()))
                                         : CommonError.MANDATORY_IE_INCORRECT.problemDetails(Optional.ofNullable(ve.getMessage()),
                                                                                             Optional.ofNullable(ve.getInvalidParams()));
            case SYNTAX_ERROR_OPTIONAL:
                return ve.isQueryError() ? CommonError.OPTIONAL_QUERY_PARAM_INCORRECT.problemDetails(Optional.ofNullable(ve.getMessage()),
                                                                                                     Optional.ofNullable(ve.getInvalidParams()))
                                         : CommonError.OPTIONAL_IE_INCORRECT.problemDetails(Optional.ofNullable(ve.getMessage()),
                                                                                            Optional.ofNullable(ve.getInvalidParams()));
            case SEMANTIC_ERROR_MISSING_PARAM:
                return ve.isQueryError() ? CommonError.MANDATORY_QUERY_PARAM_MISSING.problemDetails(Optional.ofNullable(ve.getMessage()),
                                                                                                    Optional.ofNullable(ve.getInvalidParams()))
                                         : CommonError.MANDATORY_IE_MISSING.problemDetails(Optional.ofNullable(ve.getMessage()),
                                                                                           Optional.ofNullable(ve.getInvalidParams()));
            case SEMANTIC_ERROR_WRONG_PARAM:
                return ve.isQueryError() ? CommonError.INVALID_QUERY_PARAM.problemDetails(Optional.ofNullable(ve.getMessage()),
                                                                                          Optional.ofNullable(ve.getInvalidParams()))
                                         : CommonError.MANDATORY_IE_INCORRECT.problemDetails(Optional.ofNullable(ve.getMessage()),
                                                                                             Optional.ofNullable(ve.getInvalidParams()));
            default:
                return CommonError.UNSPECIFIED_MSG_FAILURE.problemDetails(Optional.ofNullable(ve.getMessage()), Optional.ofNullable(ve.getInvalidParams()));
        }
    }
}
