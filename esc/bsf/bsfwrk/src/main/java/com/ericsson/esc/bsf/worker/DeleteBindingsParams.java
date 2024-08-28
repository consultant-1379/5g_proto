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
 * Created on: Jan 30, 2011
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.worker;

import java.util.UUID;

import com.ericsson.esc.lib.OpenApiReqParams;
import com.ericsson.esc.lib.ValidationException;

import io.vertx.ext.web.api.RequestParameters;

public class DeleteBindingsParams implements OpenApiReqParams
{
    private final UUID bindingId;

    /**
     * 
     * @param requestParameters
     * @throws IllegalArgumentException
     * 
     */
    public DeleteBindingsParams(RequestParameters parsedParameters)
    {

        final var uuidStr = parsedParameters.pathParameter("bindingId").getString();
        try
        {
            this.bindingId = UUID.fromString(uuidStr);
        }
        catch (Exception e)
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, "bindingId", "Invalid bindingId " + uuidStr, e).setQueryParameter(false);
        }
    }

    public DeleteBindingsParams(UUID bindingId)
    {
        this.bindingId = bindingId;
    }

    public UUID getBindingId()
    {
        return bindingId;
    }
}
