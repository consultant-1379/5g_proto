/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 7, 2022
 *     Author: eaoknkr
 */

package com.ericsson.sc.scp.config.filters;

import com.ericsson.sc.glue.IEricProxyFilter;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.scp.model.NfInstance;

/**
 * 
 */
public class EricProxyFilterFactory
{
    private final NfInstance scpInst;
    private final boolean isTls;
    private final IfNetwork nw;

    public EricProxyFilterFactory(NfInstance scpInst,
                                  boolean isTls,
                                  IfNetwork nw)
    {
        this.scpInst = scpInst;
        this.isTls = isTls;
        this.nw = nw;
    }

    public IEricProxyFilter getFilter(EricProxyFilterType type)
    {
        if (type == EricProxyFilterType.RATE_LIMIT)
        {
            return new EricIngressRateLimitFilter(scpInst, this.nw);
        }
        else
        {
            return new EricRoutingScreeningFilter(scpInst, this.isTls, this.nw);
        }
    }

    public enum EricProxyFilterType
    {
        RATE_LIMIT,
        ROUTING_SCREENING;
    }

}
