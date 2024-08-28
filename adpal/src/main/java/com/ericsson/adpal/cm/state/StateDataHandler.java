package com.ericsson.adpal.cm.state;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * @author edimsyr An interface containing the methods to be implemented by a
 *         handler that will handle state data requests from yang handlerPath:
 *         needs to be implemented by each component individually (eg
 *         SeppStadeDataHandler) handlerName: for debugging purposes
 *         handleRequest: the main method handling the incoming request from
 *         Yang after the specific handler has been chosen
 */
public interface StateDataHandler
{
    public String handlerPath();

    public String handlerName();

    public Completable handleRequest(Single<StateDataInput> input);

}
