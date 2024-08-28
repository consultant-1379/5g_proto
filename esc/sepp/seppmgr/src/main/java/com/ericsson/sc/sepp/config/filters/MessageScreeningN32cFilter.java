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
 * Created on: Oct 24, 2022
 *     Author: echaias
 */

package com.ericsson.sc.sepp.config.filters;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.sepp.config.ConfigUtils;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.utilities.common.Utils;

/**
 * 
 */
public class MessageScreeningN32cFilter extends MessageScreeningFilter
{
    private static final Logger log = LoggerFactory.getLogger(MessageScreeningN32cFilter.class);

    public MessageScreeningN32cFilter(ProxySeppFilter seppFilter,
                                      NfInstance seppInst)
    {
        super(seppFilter, seppInst, null);

    }

    /**
     * Create the SEPP screening filter for the internal n32c listener
     *
     */
    @Override
    public void create()
    {
        log.debug("Creating Proxy Message Screening cases for N32c");

        var n32cPools = ConfigUtils.getAllRoamingPartnersWithN32C(seppInst.getExternalNetwork())
                                   .stream()
                                   .map(rp -> Utils.getByName(seppInst.getNfPool(), rp.getN32C().getNfPoolRef()))
                                   .filter(Objects::nonNull)
                                   .collect(Collectors.toList());

        var outReqScreeningRef = ConfigUtils.getReferencedOutRequestScreeningCases(n32cPools);
        var inRespScreeningRef = ConfigUtils.getReferencedInResponseScreeningCases(n32cPools);

        if (!outReqScreeningRef.isEmpty())
        {
            ConfigUtils.createOutRequestScreeningKvTableForPools(seppFilter,
                                                                 seppInst,
                                                                 ProxySeppFilter.INTERNAL_OUT_REQUEST_TABLE_NAME,
                                                                 Optional.ofNullable(n32cPools));
        }

        if (!inRespScreeningRef.isEmpty())
        {
            ConfigUtils.createInResponseScreeningKvTableForPools(seppFilter,
                                                                 seppInst,
                                                                 ProxySeppFilter.INTERNAL_IN_RESPONSE_TABLE_NAME,
                                                                 Optional.ofNullable(n32cPools));
        }
        addUniqueProxyScreeningCases(outReqScreeningRef, inRespScreeningRef);
    }
}
