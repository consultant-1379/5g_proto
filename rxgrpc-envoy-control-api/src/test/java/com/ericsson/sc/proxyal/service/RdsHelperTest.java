/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Sep 9, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.proxyconfig.proxyendpointconfig.ProxyEndpoint;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRetryPolicy;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRouteMatch;

/**
 * 
 */
class RdsHelperTest
{

    private static final Logger log = LoggerFactory.getLogger(RdsHelperTest.class);

    @Test
    void testBuildRouteBasic()
    {
        var pxRoute = new ProxyRoute("ut_service", new ProxyRouteMatch().setPrefix("/"), null, null, "x-cluster", null, null, null, 30.0);
        var route = RdsHelper.buildRoute(pxRoute, List.of(new ProxyEndpoint("fqdn", 0)));
        assertNotNull(route);
        log.debug("route:{}.", route);
    }

    @Test
    void testBuildRouteWithRetryOmitHosts()
    {
        int hostSelectionRetryMaxAttempts = 0;
        String retryHostPredicate = "envoy.retry_host_predicates.previous_hosts";
        String retryPriority = "envoy.retry_priorities.previous_priorities";

        var retryPolicy = new ProxyRetryPolicy("retryOn", 0, 30.0, retryHostPredicate, retryPriority, 1, hostSelectionRetryMaxAttempts);
        var omitHostsMd = new HashMap<String, String>();
        omitHostsMd.put("host", "omithost");

        retryPolicy.setOmitHostMetadata(retryPolicy.ERIC_OMIT_HOST_MD_DYNAMIC);

        var pxRoute = new ProxyRoute("ut_service", new ProxyRouteMatch().setPrefix("/"), retryPolicy, null, "x-cluster", null, null, null, 30.0);

        var route = RdsHelper.buildRoute(pxRoute, List.of(new ProxyEndpoint("fqdn", 0)));

        assertNotNull(route);
        log.debug("route:{}.", route);
    }

}
