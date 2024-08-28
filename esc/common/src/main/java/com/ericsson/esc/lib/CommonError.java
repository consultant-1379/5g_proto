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

import io.netty.handler.codec.http.HttpResponseStatus;

public enum CommonError
{
    INVALID_API(HttpResponseStatus.BAD_REQUEST,
                "The HTTP request contains an unsupported API name or API version in the URI"),
    INVALID_MSG_FORMAT(HttpResponseStatus.BAD_REQUEST,
                       "The HTTP request has an invalid format"),
    INVALID_QUERY_PARAM(HttpResponseStatus.BAD_REQUEST,
                        "The HTTP request contains an unsupported query parameter in the URI"),
    MANDATORY_QUERY_PARAM_INCORRECT(HttpResponseStatus.BAD_REQUEST,
                                    "A mandatory query parameter, or a conditional query parameter but mandatory required, for an HTTP method was received in the URI with semantically incorrect value."),
    MANDATORY_QUERY_PARAM_MISSING(HttpResponseStatus.BAD_REQUEST,
                                  "A mandatory query parameter, or a conditional query parameter but mandatory required, for an HTTP method is not included in the URI of the request."),
    OPTIONAL_QUERY_PARAM_INCORRECT(HttpResponseStatus.BAD_REQUEST,
                                   "An optional query parameter for an HTTP method was received in the URI with a semantically incorrect value that prevents successful processing of the service request."),
    MANDATORY_IE_INCORRECT(HttpResponseStatus.BAD_REQUEST,
                           "A mandatory IE or conditional IE in data structure, but mandatory required, for an HTTP method was received with a semantically incorrect value"),
    OPTIONAL_IE_INCORRECT(HttpResponseStatus.BAD_REQUEST,
                          "An optional IE in data structure for an HTTP method was received with a semantically incorrect value that prevents successful processing of the service request"),
    MANDATORY_IE_MISSING(HttpResponseStatus.BAD_REQUEST,
                         "IE which is defined as mandatory or as conditional in data structure, but mandatory required, for an HTTP method is not included in the payload body of the request"),
    UNSPECIFIED_MSG_FAILURE(HttpResponseStatus.BAD_REQUEST,
                            "The request is rejected due to unspecified client error"),
    MODIFICATION_NOT_ALLOWED(HttpResponseStatus.FORBIDDEN,
                             "The request is rejected because the contained modification instructions attempt to modify IE which is not allowed to be modified"),
    SUBSCRIPTION_NOT_FOUND(HttpResponseStatus.NOT_FOUND,
                           "The request for modification or deletion of subscription is rejected because the subscription is not found in the NF."),
    RESOURCE_URI_STRUCTURE_NOT_FOUND(HttpResponseStatus.NOT_FOUND,
                                     "The request is rejected because a fixed part after the first variable part of an \"apiSpecificResourceUriPart\" is not found"),
    INCORRECT_LENGTH(HttpResponseStatus.LENGTH_REQUIRED,
                     "The request is rejected due to incorrect value of a Content-length header field"),
    NF_CONGESTION_RISK(HttpResponseStatus.TOO_MANY_REQUESTS,
                       "The request is rejected due to excessive traffic which, if continued over time, may lead to (or may increase) an overload situation."),
    INSUFFICIENT_RESOURCES(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                           "The request is rejected due to insufficient resources"),
    UNSPECIFIED_NF_FAILURE(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                           "The request is rejected due to unspecified reason at the NF"),
    SYSTEM_FAILURE(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                   "The request is rejected due to generic error condition in the NF."),
    NF_CONGESTION(HttpResponseStatus.SERVICE_UNAVAILABLE,
                  "The NF experiences congestion and performs overload control, which does not allow the request to be processed"),
    // NbsfManagement Service errors
    // TODO use different enum for generic and different enum for specific errors
    MULTIPLE_BINDING_INFO_FOUND(HttpResponseStatus.BAD_REQUEST,
                                "The BSF found more than one binding resource"),
    // TODO This is not 3gpp specificed, own custom handling.
    RESOURCE_URI_NOT_FOUND(HttpResponseStatus.NOT_FOUND,
                           "The request is rejected because the requested resource is not found in the NF."),
    TOO_MANY_BINDINGS_FOUND(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                            "The BSF found too many binding resources");

    private HttpResponseStatus status;
    private String description;

    CommonError(HttpResponseStatus status,
                String description)
    {
        this.status = status;
        this.description = description;
    }

    public ProblemDetails problemDetails(Optional<String> details,
                                         Optional<List<InvalidParam>> invalidParams)
    {
        ProblemDetails pd = new ProblemDetails();

        pd.setStatus(this.status.code());
        pd.setTitle(this.status.reasonPhrase());
        pd.setCause(getCause());
        pd.setDetail(details.isPresent() ? details.get() : this.description);
        invalidParams.ifPresent(pd::setInvalidParams);
        return pd;
    }

    public HttpResponseStatus getStatus()
    {
        return this.status;
    }

    public String getDesciption()
    {
        return this.description;
    }

    public String getCause()
    {
        return this.name();
    }

}
