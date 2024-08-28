package com.ericsson.adpal.cm.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Single;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * @author edimsyr The state data input needed for the State Data provider in
 *         order to correctly route to the appropriate handler
 */
public class StateDataInput
{

    private static final Logger log = LoggerFactory.getLogger(StateDataInput.class);

    private StateDataHandler stateDataHandler;
    private RoutingParameter routingParameter;
    private RoutingContext ctx;

    /**
     * @param handler the stateDataHandler implementation for each state data use
     *                case
     * @param path    the supported path for each handler
     */
    public StateDataInput(StateDataHandler handler,
                          RoutingParameter path)
    {
        super();
        stateDataHandler = handler;
        routingParameter = path;
    }

    public Single<StateDataInput> getInput()
    {
        return Single.create(emitter ->
        {
            try
            {
                emitter.onSuccess(new StateDataInput(stateDataHandler, routingParameter).withCtx(ctx));
            }
            catch (Exception e)
            {
                log.error("Error constructing StateDataInputr: ", e);
                emitter.onError(e);
            }
        });
    }

    public RoutingContext getCtx()
    {
        return ctx;
    }

    public void setCtx(RoutingContext ctx)
    {
        this.ctx = ctx;
    }

    public StateDataInput withCtx(RoutingContext ctx)
    {
        this.ctx = ctx;
        return this;
    }

    public StateDataHandler getStateDataHandler()
    {
        return stateDataHandler;
    }

    public RoutingParameter getRoutingParameter()
    {
        return routingParameter;
    }

    public void setStateDataHandler(StateDataHandler stateDataHandler)
    {
        this.stateDataHandler = stateDataHandler;
    }

    public void setRoutingParameter(RoutingParameter routingParameter)
    {
        this.routingParameter = routingParameter;
    }

    @Override
    public String toString()
    {
        return "StateDataInput [StateDataHandler=" + stateDataHandler.toString() + "]" + "[RoutingParameter=" + routingParameter.toString();
    }

}
