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
 * Created on: Jan 16, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.http.openapi;

import io.vertx.reactivex.ext.web.RoutingContext;

public abstract class OpenApiTask
{
    public enum DataIndex
    {
        OPERATION_ID,
        HANDLER,
        REQUEST_BODY,
        IP_FAMILY;
    }

    public abstract static class Factory
    {
        private final String operationId;

        protected Factory(String operationId)
        {
            this.operationId = operationId;
        }

        public abstract OpenApiTask create(RoutingContext context);

        public String operationId()
        {
            return this.operationId;
        }
    }

    protected final RoutingContext context;

    protected OpenApiTask(RoutingContext context)
    {
        this.context = context;
    }

    public abstract void execute();
}
