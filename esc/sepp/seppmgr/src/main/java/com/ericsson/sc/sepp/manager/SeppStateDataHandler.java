package com.ericsson.sc.sepp.manager;

import com.ericsson.adpal.cm.state.StateDataHandler;

/**
 * @author edimsyr The SEPP instance of StateDataHandler with the approriate
 *         first part of the path Every state data handler that will be used for
 *         State data in Sepp shall implement this Handler
 */
public interface SeppStateDataHandler extends StateDataHandler
{
    public default String handlerPath()
    {
        return "/ericsson-sepp:sepp-function/nf-instance/";
    }

}
