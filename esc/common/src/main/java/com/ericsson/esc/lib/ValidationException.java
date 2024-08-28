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

public class ValidationException extends BadRequestException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // initialized to false by default
    private boolean queryError;

    public enum ErrorType
    {
        SYNTAX_ERROR,
        SYNTAX_ERROR_OPTIONAL,
        SEMANTIC_ERROR_MISSING_PARAM,
        SEMANTIC_ERROR_WRONG_PARAM;
    }

    private final List<InvalidParam> invalidParams;
    private final ErrorType errorType;

    public ValidationException(ErrorType errorType,
                               List<InvalidParam> invalidParams,
                               Throwable cause)
    {
        super(errorType.name(), cause);
        this.errorType = errorType;
        this.invalidParams = invalidParams;
    }

    public ValidationException(ErrorType errorType,
                               List<InvalidParam> invalidParams)
    {
        super(errorType.name());
        this.errorType = errorType;
        this.invalidParams = invalidParams;
    }

    public ValidationException(ErrorType errorType,
                               String invalidParamName,
                               String invalidParamReason)
    {
        super(errorType.name());
        this.errorType = errorType;
        this.invalidParams = List.of(new InvalidParam(invalidParamName, invalidParamReason));
    }

    public ValidationException(ErrorType errorType,
                               String invalidParamName,
                               String invalidParamReason,
                               Throwable cause)
    {
        super(errorType.name(), cause);
        this.errorType = errorType;
        this.invalidParams = List.of(new InvalidParam(invalidParamName, invalidParamReason));
    }

    public ErrorType getErrorType()
    {
        return this.errorType;
    }

    public List<InvalidParam> getInvalidParams()
    {
        return this.invalidParams;
    }

    public boolean isQueryError()
    {
        return queryError;
    }

    public ValidationException setQueryParameter(boolean queryParameter)
    {
        this.queryError = queryParameter;
        return this;
    }
}
