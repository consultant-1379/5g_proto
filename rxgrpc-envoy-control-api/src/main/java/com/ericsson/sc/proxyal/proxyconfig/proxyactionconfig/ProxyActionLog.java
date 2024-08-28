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
 * Created on: Oct 4, 2022
 *     Author: eavvann
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyactionconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Action;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.LogAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.LogValue;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.LoggingLevel;

/**
 *
 */
public class ProxyActionLog implements ProxyAction
{
    private String logLevel = "debug";
    private Integer maxLogMessageLength = 250;
    private List<LogValue> logValues = new ArrayList<>();

    /**
     * @param text
     * @param logLevel
     * @param maxLogMessageLength
     * @param logValues
     */
    public ProxyActionLog(List<LogValue> logValues,
                          String logLevel,
                          Integer maxLogMessageLength)
    {
        this.logLevel = logLevel;
        this.maxLogMessageLength = maxLogMessageLength;
        this.logValues = logValues;
    }

    public ProxyActionLog(ProxyActionLog arm)
    {
        this.logLevel = arm.getLogLevel();
        this.maxLogMessageLength = arm.getMaxLogMessageLength();
        this.logValues = arm.getLogValues();
    }

    /**
     * @return the logLevel
     */
    public String getLogLevel()
    {
        return logLevel;
    }

    /**
     * @return the maxLogMessageLength
     */
    public Integer getMaxLogMessageLength()
    {
        return maxLogMessageLength;
    }

    /**
     * @return the logValues
     */
    public List<LogValue> getLogValues()
    {
        return logValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyActionLog [log-values=" + logValues.toString() + ", log-level=" + logLevel + ", max-log-message-length=" + maxLogMessageLength + "]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(logValues, logLevel, maxLogMessageLength);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;

        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        ProxyActionLog other = (ProxyActionLog) obj;

        return Objects.equals(logValues, other.logValues) && Objects.equals(logLevel, other.logLevel)
               && Objects.equals(maxLogMessageLength, other.maxLogMessageLength);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.proxyal.proxyconfig.ProxyAction#buildAction()
     */
    @Override
    public Action buildAction()
    {
        LoggingLevel loggingLevel = LoggingLevel.DEBUG;

        if (this.logLevel.equals("warn"))
            loggingLevel = LoggingLevel.WARN;
        else if (this.logLevel.equals("error"))
            loggingLevel = LoggingLevel.ERROR;
        else if (this.logLevel.equals("trace"))
            loggingLevel = LoggingLevel.TRACE;
        else if (this.logLevel.equals("info"))
            loggingLevel = LoggingLevel.INFO;

        var actionBuilder = LogAction.newBuilder();
        actionBuilder.setLogLevel(loggingLevel);
        actionBuilder.setMaxLogMessageLength(this.maxLogMessageLength);
        actionBuilder.addAllLogValues(logValues);

        return Action.newBuilder().setActionLog(actionBuilder).build();
    }

}
