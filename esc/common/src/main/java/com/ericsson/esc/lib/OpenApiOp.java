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

import java.util.function.Function;

import io.vertx.ext.web.api.RequestParameters;
import io.vertx.reactivex.ext.web.RoutingContext;

public class OpenApiOp<T extends OpenApiReqParams>
{

    final String operationId;
    final Function<RequestParameters, T> creator;

    public OpenApiOp(String operationId,
                     Function<RequestParameters, T> generator)
    {
        this.operationId = operationId;
        this.creator = generator;
    }

    String getOperationId()
    {
        return this.operationId;
    }

    OpenApiReq<T> create(RoutingContext rc)
    {
        final var parsedParams = rc.<RequestParameters>get("parsedParameters");
        final var params = this.creator.apply(parsedParams);
        return new OpenApiReq<>(rc, params);
    }
}
