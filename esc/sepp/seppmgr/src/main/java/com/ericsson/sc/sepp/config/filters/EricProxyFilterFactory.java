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

package com.ericsson.sc.sepp.config.filters;

import com.ericsson.sc.glue.IEricProxyFilter;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.ServiceAddress;

/**
 * 
 */
public class EricProxyFilterFactory
{
    private final ServiceAddress svcAddress;
    private final boolean isTls;
    private final NfInstance seppInst;
    private final IfNetwork nw;

    public EricProxyFilterFactory(NfInstance seppInstance,
                                  ServiceAddress svcAddress,
                                  boolean isTls,
                                  IfNetwork nw)
    {
        this.seppInst = seppInstance;
        this.svcAddress = svcAddress;
        this.isTls = isTls;
        this.nw = nw;
    }

    public IEricProxyFilter getFilter(EricProxyFilterType type)
    {

        switch (type)
        {
            case RATE_LIMIT:
                return new EricIngressRateLimitFilter(seppInst, this.nw, this.isTls);
            case ROUTING_SCREENING:
                return new EricRoutingScreeningFilter(seppInst, svcAddress, this.isTls, this.nw);
            case N32C_EGRESS_SCREENING:
                return new EricScreeningN32cFilter(seppInst);
            default:
                throw new IllegalStateException("Unknown eric filter type requested");
        }
    }

    public enum EricProxyFilterType
    {
        RATE_LIMIT,
        ROUTING_SCREENING,
        N32C_EGRESS_SCREENING,
    }

}
