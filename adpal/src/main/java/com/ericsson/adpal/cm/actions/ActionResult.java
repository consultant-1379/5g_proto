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
 * Created on: Nov 17, 2020
 *     Author: echfari
 */
package com.ericsson.adpal.cm.actions;

import java.util.Objects;
import java.util.Optional;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents the result of a YANG action. The result could be an arbitrary JSON
 * document, or an error.
 */
public class ActionResult
{

    private final JsonNode output;
    private final ErrorResult errorResult;

    public static ActionResult error(ErrorType errorType,
                                     String message)
    {
        Objects.requireNonNull(errorType);
        Objects.requireNonNull(message);

        var errorResult = new ErrorResult(errorType, message);
        return new ActionResult(null, errorResult);
    }

    public static ActionResult success()
    {
        return new ActionResult(null, null);
    }

    public static ActionResult success(JsonNode output)
    {
        Objects.requireNonNull(output);
        return new ActionResult(output, null);
    }

    protected ActionResult(JsonNode output,
                           ErrorResult errorResult)
    {
        this.output = output;
        this.errorResult = errorResult;

        if (output != null && errorResult != null)
            throw new IllegalArgumentException("Both arguments non null output: " + output + " errorResult: " + errorResult);
    }

    public int resultCode()
    {
        if (errorResult != null)
            return this.errorResult.errorType.statusCode;
        else if (output != null)
            return 200;
        else
            return 204;
    }

    public JsonNode toJson()
    {

        if (this.errorResult != null)
        {
            final var json = Jackson.om().createObjectNode();
            json.put("message", this.errorResult.errorMessage);
            return json;
        }
        else if (this.output != null)
        {
            return this.output;
        }
        else
        {
            return Jackson.om().createObjectNode();
        }

    }

    public Optional<JsonNode> getOutput()
    {
        return Optional.ofNullable(output);
    }

    public Optional<ErrorResult> getError()
    {
        return Optional.ofNullable(errorResult);
    }

    @Override
    public String toString()
    {
        var builder = new StringBuilder();
        builder.append("ActionResult [output=");
        builder.append(output);
        builder.append(", errorResult=");
        builder.append(errorResult);
        builder.append("]");
        return builder.toString();
    }

    public enum ErrorType
    {
        BAD_REQUEST(400),
        NOT_FOUND(404),
        CONFLICT(409),
        INTERNAL_SERVER_ERROR(500),
        SERVICE_UNAVAILABLE(503);

        private int statusCode;

        ErrorType(int statusCode)
        {
            this.statusCode = statusCode;
        }

        public int getStatusCode()
        {
            return this.statusCode;
        }
    }

    public static class ErrorResult
    {
        public final ErrorType errorType;
        public final String errorMessage;

        public ErrorResult(ErrorType errorType,
                           String errorMessage)
        {
            super();
            this.errorType = errorType;
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString()
        {
            var builder = new StringBuilder();
            builder.append("ErrorResult [errorType=");
            builder.append(errorType);
            builder.append(", errorMessage=");
            builder.append(errorMessage);
            builder.append("]");
            return builder.toString();
        }
    }
}
