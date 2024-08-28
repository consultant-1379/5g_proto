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

import com.ericsson.esc.lib.OpenApiReqParams;

import io.vertx.ext.web.api.RequestParameters;

public class EmptyParamExtractor implements OpenApiReqParams
{
    public EmptyParamExtractor(RequestParameters parsedParams)
    {
        // Does not extract any parameter from the request.
    }
}
