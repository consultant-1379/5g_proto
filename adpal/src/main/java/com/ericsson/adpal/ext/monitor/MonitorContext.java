package com.ericsson.adpal.ext.monitor;

/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property of Ericsson
 * GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written permission of
 * Ericsson GmbH in accordance with the terms and conditions stipulated in the
 * agreement/contract under which the program(s) have been supplied.
 *
 * Created on: Nov 14, 2018 Author: eedstl
 */

public class MonitorContext
{
    /**
     * Environment used by the Monitor.
     */
    // TODO: Avoid hardcoded hostname in shared libraries and try to use input
    // parameters from the classes using this class
    public static final String MONITOR_SERVICE = "eric-sc-monitor";
    public static final String MONITOR_API = "/monitor/api/v0";
    // TODO: Make port configurable from external helm parameters

    // TODO: Avoid hardcoded port numbers in shared libraries and try to use input
    // parameters from the classes using this class
    public static final int MONITOR_PORT_HTTP_INTERNAL = 8080;

    /**
     * Operations supported by the Monitor.
     */
    public enum Operation
    {
        COMMANDS("/commands"),
        REGISTER("/register"),
        SUBSCRIPTIONS("/subscriptions");

        private final String op;

        public String getName()
        {
            return this.op;
        }

        private Operation(final String name)
        {
            this.op = name;
        }
    }
}
