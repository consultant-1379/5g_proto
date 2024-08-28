/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 12, 2018
 *     Author: eedstl
 */

package com.ericsson.utilities.logger;

import java.lang.management.ManagementFactory;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ProcessIdConverter extends ClassicConverter
{
//    private static final String PROCESS_ID = String.valueOf(ManagementFactory.getRuntimeMXBean().getPid()); // as of 1.10
    private static final String PROCESS_ID = String.valueOf(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);

    @Override
    public String convert(final ILoggingEvent event)
    {
        return PROCESS_ID;
    }
}