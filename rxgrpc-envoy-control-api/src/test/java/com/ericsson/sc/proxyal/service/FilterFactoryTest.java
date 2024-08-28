package com.ericsson.sc.proxyal.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.proxyal.service.FilterFactory;

class FilterFactoryTest
{
    private static final Logger log = LoggerFactory.getLogger(FilterFactoryTest.class);

    @Disabled
    @Test
    void testGetCsaLuaFilter2()
    {
        assertNotNull(FilterFactory.getCsaLuaFilter("30080",
                                                    "30443",
                                                    "csa.ericsson.se",
                                                    "",
                                                    "nchf_convergedcharging",
                                                    "v1",
                                                    "chargingdata",
                                                    "",
                                                    "nchf_spendinglimitcontrol",
                                                    "v1",
                                                    "subscriptions"));
    }

    @Test
    void testGetCsaLuaFilter()
    {
        Map<String, Map<String, String>> contextData = new HashMap<String, Map<String, String>>();
        contextData.put("common",
                        Map.of("ownIp",
                               "ALEXcsa.ericsson.se", //
                               "ownPort",
                               "ALEX80",
                               "ownTlsPort",
                               "TEST443"));
        contextData.put("convergedCharging",
                        Map.of("apiRoot",
                               "ALEX",
                               "apiName",
                               "ALEXnchf%-convergedcharging",
                               "apiVersion",
                               "ALEXv1",
                               "apiSRUP",
                               "ALEXchargingdata",
                               "labelSelector",
                               "regex([^-]+)"));
        contextData.put("spendingLimitControl",
                        Map.of("apiRoot", "ANDER", "apiName", "ANDERnchf%-spendinglimitcontrol", "apiVersion", "ANDERv1", "apiSRUP", "ANDERsubscriptions"));

        log.info(FilterFactory.getCsaLuaFilter(contextData));
    }
}
