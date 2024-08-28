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
 * Created on: Oct 16, 2018
 *     Author: xchrfar
 */
package com.ericsson.esc.lib;

import io.vertx.reactivex.ext.web.RoutingContext;

public class OpenApiReq<T extends OpenApiReqParams>
{
    final RoutingContext routingContext;
    final T params;

    OpenApiReq(RoutingContext routingContext,
               T params)
    {
        this.routingContext = routingContext;
        this.params = params;
    }

    public RoutingContext getRoutingContext()
    {
        return this.routingContext;
    }

    public T getParams()
    {
        return this.params;
    }
}
