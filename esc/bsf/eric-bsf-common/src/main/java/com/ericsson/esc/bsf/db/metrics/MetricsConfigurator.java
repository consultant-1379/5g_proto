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
 * Created on: Aug 07, 2020
 *     Author: eaopmrk
 */

package com.ericsson.esc.bsf.db.metrics;

import java.util.ArrayList;
import java.util.List;

public class MetricsConfigurator
{

    private CqlRequests cqlRequests;
    private CqlMessages cqlMessages;
    private Throttling throttling;
    private List<String> sessionMetrics = new ArrayList<>();
    private List<String> nodeMetrics = new ArrayList<>();
    private String sessionName;
    private String domainName;

    public MetricsConfigurator(MetricsConfiguratorBuilder builder)
    {
        this.cqlRequests = builder.cqlRequests;
        this.cqlMessages = builder.cqlMessages;
        this.throttling = builder.throttling;
        this.sessionMetrics = builder.sessionMetrics;
        this.nodeMetrics = builder.nodeMetrics;
        this.sessionName = builder.sessionName;
        this.domainName = builder.domainName;
    }

    public String getSessionName()
    {
        return this.sessionName;
    }

    public String getDomainName()
    {
        return this.domainName;
    }

    public CqlRequests getCqlRequests()
    {
        return this.cqlRequests;
    }

    public CqlMessages getCqlMessages()
    {
        return this.cqlMessages;
    }

    public Throttling getThrottling()
    {
        return this.throttling;
    }

    public List<String> getSessionMetrics()
    {
        return this.sessionMetrics;
    }

    public List<String> getNodeMetrics()
    {
        return this.nodeMetrics;
    }

    @Override
    public String toString()
    {
        return "Session name: " + this.sessionName + ", Domain name: " + this.domainName + ", CqlRequests: " + this.cqlRequests + ", CqlMessages: "
               + this.cqlMessages + ", Throttling: " + this.throttling + " , " + "SessionMetrics: " + this.sessionMetrics + ", NodeMetrics: "
               + this.nodeMetrics;
    }

    public static class MetricsConfiguratorBuilder
    {
        private CqlRequests cqlRequests;
        private CqlMessages cqlMessages;
        private Throttling throttling;
        private List<String> sessionMetrics = new ArrayList<>();
        private List<String> nodeMetrics = new ArrayList<>();
        private String sessionName;
        private String domainName;

        public MetricsConfiguratorBuilder(String sessionName,
                                          String domainName)
        {
            this.sessionName = sessionName;
            this.domainName = domainName;
            this.setSessionMetrics();
            this.setNodeMetrics();
        }

        private void setSessionMetrics()
        {
            this.sessionMetrics.add("bytes-sent");
            this.sessionMetrics.add("bytes-received");
            this.sessionMetrics.add("connected-nodes");
            this.sessionMetrics.add("cql-requests");
            this.sessionMetrics.add("cql-client-timeouts");
            this.sessionMetrics.add("throttling.delay");
            this.sessionMetrics.add("throttling.queue-size");
            this.sessionMetrics.add("throttling.errors");
            this.sessionMetrics.add("cql-prepared-cache-size");
        }

        private void setNodeMetrics()
        {
            this.nodeMetrics.add("pool.open-connections");
            this.nodeMetrics.add("pool.available-streams");
            this.nodeMetrics.add("pool.in-flight");
            this.nodeMetrics.add("pool.orphaned-streams");
            this.nodeMetrics.add("bytes-send");
            this.nodeMetrics.add("bytes-received");
            this.nodeMetrics.add("cql-messages");
            this.nodeMetrics.add("errors.request.unsent");
            this.nodeMetrics.add("errors.request.aborted");
            this.nodeMetrics.add("errors.request.write-timeouts");
            this.nodeMetrics.add("errors.request.read-timeouts");
            this.nodeMetrics.add("errors.request.unavailables");
            this.nodeMetrics.add("errors.request.others");
            this.nodeMetrics.add("retries.total");
            this.nodeMetrics.add("retries.aborted");
            this.nodeMetrics.add("retries.read-timeout");
            this.nodeMetrics.add("retries.write-timeout");
            this.nodeMetrics.add("retries.unavailable");
            this.nodeMetrics.add("retries.other");
            this.nodeMetrics.add("ignores.total");
            this.nodeMetrics.add("ignores.aborted");
            this.nodeMetrics.add("ignores.read-timeout");
            this.nodeMetrics.add("ignores.write-timeout");
            this.nodeMetrics.add("ignores.unavailable");
            this.nodeMetrics.add("ignores.other");
            this.nodeMetrics.add("speculative-executions");
            this.nodeMetrics.add("errors.connection.init");
            this.nodeMetrics.add("errors.connection.auth");
        }

        public MetricsConfiguratorBuilder withCqlRequests(CqlRequests cqlRequests)
        {
            this.cqlRequests = cqlRequests;
            return this;
        }

        public MetricsConfiguratorBuilder withCqlMessages(CqlMessages cqlMessages)
        {
            this.cqlMessages = cqlMessages;
            return this;
        }

        public MetricsConfiguratorBuilder withThrottling(Throttling throttling)
        {
            this.throttling = throttling;
            return this;
        }

        public MetricsConfiguratorBuilder withSessionMetrics(List<String> sessionMetrics)
        {
            this.sessionMetrics = sessionMetrics;
            return this;
        }

        public MetricsConfiguratorBuilder withNodeMetrics(List<String> nodeMetrics)
        {
            this.nodeMetrics = nodeMetrics;
            return this;
        }

        public MetricsConfigurator build()
        {
            return new MetricsConfigurator(this);
        }

    }
    /*
     * public MetricsConfiguration() { this.setSessionMetrics(); // Default values
     * according to cassandra datastax java-driver
     * this.setCqlRequestsHighestLatency(3); //10
     * this.setCqlRequestsSignificantDigits(Duration.ofMillis(3)); //3
     * this.setCqlRequestsRefreshInterval(Duration.ofMillis(5)); //10
     * this.setThrottlingHighestLatency(3);
     * this.setThrottlingSignificantDigits(Duration.ofMillis(3));
     * this.setThrottlingRefreshInterval(Duration.ofMillis(5));
     * this.setNodeMetrics(); this.setCqlMessagesHighestLatency(3);
     * this.setCqlMessagesSignificantDigits(Duration.ofMillis(3));
     * this.setCqlMessagesRefreshInterval(Duration.ofMillis(5)); }
     * 
     * public void setCqlRequestsConfiguration(int cqlRequestsHighest, long
     * cqlRequestsDigits, long cqlRequestsInterval) {
     * this.setCqlRequestsHighestLatency(cqlRequestsHighest);
     * this.setCqlRequestsSignificantDigits(Duration.ofMillis(cqlRequestsDigits));
     * this.setCqlRequestsRefreshInterval(Duration.ofMillis(cqlRequestsInterval)); }
     * 
     * public void setThrottlingConfiguration(int throttlingLatency, long
     * throttlingDigits, long throttlingInterval) {
     * this.setThrottlingHighestLatency(throttlingLatency);
     * this.setThrottlingSignificantDigits(Duration.ofMillis(throttlingDigits));
     * this.setThrottlingRefreshInterval(Duration.ofMillis(throttlingInterval)); }
     * 
     * public void setCqlMessagesConfiguration(int cqlMessagesHighest, long
     * cqlMessagesDigits, long cqlMessagesInterval) {
     * this.setCqlMessagesHighestLatency(cqlMessagesHighest);
     * this.setCqlMessagesSignificantDigits(Duration.ofMillis(cqlMessagesDigits));
     * this.setCqlMessagesRefreshInterval(Duration.ofMillis(cqlMessagesInterval)); }
     * 
     * 
     * public void setSessionMetrics() { this.sessionMetrics.add("bytes-sent");
     * this.sessionMetrics.add("bytes-received");
     * this.sessionMetrics.add("connected-nodes");
     * this.sessionMetrics.add("cql-requests");
     * this.sessionMetrics.add("cql-client-timeouts");
     * this.sessionMetrics.add("cql-prepared-cache-size");
     * this.sessionMetrics.add("throttling.delay");
     * this.sessionMetrics.add("throttling.queue-size");
     * this.sessionMetrics.add("throttling.errors"); }
     * 
     * 
     * public void setNodeMetrics() { this.nodeMetrics.add("pool.open-connections");
     * this.nodeMetrics.add("pool.available-streams");
     * this.nodeMetrics.add("pool.in-flight");
     * this.nodeMetrics.add("pool.orphaned-streams");
     * this.nodeMetrics.add("pool.bytes-send");
     * this.nodeMetrics.add("pool.bytes-received");
     * this.nodeMetrics.add("cql-messages");
     * this.nodeMetrics.add("errors.request.unsent");
     * this.nodeMetrics.add("errors.request.aborted");
     * this.nodeMetrics.add("errors.request.write-timeouts");
     * this.nodeMetrics.add("errors.request.read-timeouts");
     * this.nodeMetrics.add("errors.request.unavailables");
     * this.nodeMetrics.add("errors.request.others");
     * this.nodeMetrics.add("retries.total");
     * this.nodeMetrics.add("retries.aborted");
     * this.nodeMetrics.add("retries.read-timeout");
     * this.nodeMetrics.add("retries.write-timeout");
     * this.nodeMetrics.add("retries.unavailable");
     * this.nodeMetrics.add("retries.other"); this.nodeMetrics.add("ignores.total");
     * this.nodeMetrics.add("ignores.aborted");
     * this.nodeMetrics.add("ignores.read-timeout");
     * this.nodeMetrics.add("ignores.write-timeout");
     * this.nodeMetrics.add("ignores.unavailable");
     * this.nodeMetrics.add("ignores.other");
     * this.nodeMetrics.add("speculative-executions");
     * this.nodeMetrics.add("errors.connection.init");
     * this.nodeMetrics.add("errors.connection.auth"); }
     * 
     * 
     */
}