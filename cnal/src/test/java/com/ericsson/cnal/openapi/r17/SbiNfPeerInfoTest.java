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
 * Created on: Aug 28, 2021
 *     Author: eedstl
 */

package com.ericsson.cnal.openapi.r17;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SbiNfPeerInfoTest
{
    private static final Logger log = LoggerFactory.getLogger(SbiNfPeerInfoTest.class);

    @Test
    void test_0_general()
    {
        final String headerIn = "srcinst=<srcinst>; srcservinst=<srcservinst>; srcscp=<srcscp>; srcsepp=<srcsepp>; dstinst=<dstinst>; dstservinst=<dstservinst>; dstscp=<dstscp>; dstsepp=<dstsepp>;";
        final String headerOut = "srcservinst=<srcservinst>;srcsepp=SEPP-<srcsepp>;srcscp=SCP-<srcscp>;srcinst=<srcinst>;dstservinst=<dstservinst>;dstsepp=SEPP-<dstsepp>;dstscp=SCP-<dstscp>;dstinst=<dstinst>";

        String result = SbiNfPeerInfo.of(headerIn).toString();
        log.info("result={}", result);
        Assertions.assertTrue(result.equals(headerOut));
    }

    @Test
    void test_1_swapSrcAndDstFields()
    {
        final String headerIn = "srcinst=<srcinst>; srcservinst=<srcservinst>; srcscp=<srcscp>; srcsepp=<srcsepp>; dstinst=<dstinst>; dstservinst=<dstservinst>; dstscp=<dstscp>; dstsepp=<dstsepp>;";
        final String headerOut = "dstinst=<srcinst>;dstservinst=<srcservinst>;dstscp=<srcscp>;dstsepp=<srcsepp>;srcinst=<dstinst>;srcservinst=<dstservinst>;srcscp=<dstscp>;srcsepp=<dstsepp>";

        String result = SbiNfPeerInfo.swapSrcAndDstFields(headerIn);
        log.info("result={}", result);
        Assertions.assertTrue(result.equals(headerOut));
    }
}
