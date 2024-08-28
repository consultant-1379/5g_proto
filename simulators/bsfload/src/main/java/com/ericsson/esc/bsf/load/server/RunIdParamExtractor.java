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
 * Created on: Nov 17, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.server;

import java.util.UUID;

import com.ericsson.esc.lib.OpenApiReqParams;

import io.vertx.ext.web.api.RequestParameters;

public class RunIdParamExtractor implements OpenApiReqParams
{
    private final UUID runId;

    public RunIdParamExtractor(RequestParameters parsedParams)
    {
        final var uuidStr = parsedParams.pathParameter("runId").getString();
        try
        {
            this.runId = UUID.fromString(uuidStr);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Unable to parse the runId parameter", e);
        }
    }

    /**
     * @return the runId
     */
    public UUID getRunId()
    {
        return runId;
    }
}
