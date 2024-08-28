/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 17, 2023
 *     Author: zpavcha
 */

package com.ericsson.sc.bsf.diameter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * A filter that can be applied in an appender of a logback.xml file in order to
 * avoid excessive logging.
 */
public class BsfDiameterLogFilter extends Filter<ILoggingEvent>
{
    private static final String NETTY_CLIENT_LOGGER = "io.grpc.netty.NettyClientHandler";
    private static final String NETTY_SERVER_LOGGER = "io.grpc.netty.NettyServerHandler";
    private static final String DATASTAX_IN_FLIGHT_HANDLER_LOGGER = "com.datastax.oss.driver.internal.core.channel.InFlightHandler";

    @Override
    public FilterReply decide(ILoggingEvent event)
    {
        if (this.isDebugNettyLog(event) || this.isDebugDatastaxInFlightHandlerLog(event))
        {
            return FilterReply.DENY;
        }
        return FilterReply.ACCEPT;
    }

    /**
     * Checks whether a logging event (i.e. a log message) is of level DEBUG or
     * greater and is generated from either NettyClientHandler or NettyServerHandler
     * classes.
     * 
     * @param event The logging event.
     * @return True if both conditions are met, false otherwise.
     */
    private boolean isDebugNettyLog(ILoggingEvent event)
    {
        return event.getLevel().isGreaterOrEqual(Level.DEBUG)
               && (event.getLoggerName().equals(NETTY_CLIENT_LOGGER) || event.getLoggerName().equals(NETTY_SERVER_LOGGER));
    }

    /**
     * Checks whether a logging event (i.e. a log message) is of level DEBUG or
     * greater and is generated from the InFlightHandler class.
     * 
     * @param event The logging event.
     * @return True if both conditions are met, false otherwise.
     */
    private boolean isDebugDatastaxInFlightHandlerLog(ILoggingEvent event)
    {
        return event.getLevel().isGreaterOrEqual(Level.DEBUG) && event.getLoggerName().equals(DATASTAX_IN_FLIGHT_HANDLER_LOGGER);
    }
}
