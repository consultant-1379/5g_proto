/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 2, 2019
 *     Author: eedstl
 */

package com.ericsson.sim.chf.counts;

import com.ericsson.utilities.common.Count;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.test.RoutingContextBuffer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * Groups data for statistics (e.g. counts, event-sequence-array).
 */
public class Instance
{
    private static final ObjectMapper mapper = Jackson.om();

    @JsonProperty("inHttpRequestsPerIpFamily")
    private final Count.Pool inHttpRequestsPerIpFamily;

    @JsonProperty("outHttpResponsesPerIpFamily")
    private final Count.Pool outHttpResponsesPerIpFamily;

    @JsonProperty("inHttpRequests")
    private final Count inHttpRequests;

    @JsonProperty("inHttpResponsesPerStatus")
    private final Count.Pool inHttpResponsesPerStatus;

    @JsonProperty("outHttpRequests")
    private final Count outHttpRequests;

    @JsonProperty("outHttpResponsesPerStatus")
    private final Count.Pool outHttpResponsesPerStatus;

    @JsonProperty("historyOfEvents")
    private final Event.Sequence historyOfEvents;

    @JsonIgnore
    private RoutingContextBuffer context;

    public Instance(String id)
    {
        this.inHttpRequestsPerIpFamily = new Count.Pool();
        this.outHttpResponsesPerIpFamily = new Count.Pool();
        this.inHttpRequests = new Count();
        this.inHttpResponsesPerStatus = new Count.Pool();
        this.outHttpRequests = new Count();
        this.outHttpResponsesPerStatus = new Count.Pool();
        this.historyOfEvents = new Event.Sequence(id);
        this.context = null;
    }

    /**
     * Clears all counts and the event history.
     */
    @JsonIgnore
    public void clear()
    {
        this.inHttpRequestsPerIpFamily.clear();
        this.outHttpResponsesPerIpFamily.clear();
        this.inHttpRequests.clear();
        this.inHttpResponsesPerStatus.clear();
        this.outHttpRequests.clear();
        this.outHttpResponsesPerStatus.clear();
        this.historyOfEvents.clear();
        this.context = null;
    }

    /**
     * Returns the routing context of the latest HTTP request.
     * 
     * @return The routing context of the latest HTTP request.
     */
    public RoutingContextBuffer getContext()
    {
        return this.context;
    }

    /**
     * Returns the count of the number of incoming HTTP requests. The HTTP requests
     * in the map are indexed by the IP family used for the transport of the
     * request.
     * 
     * @return The map of counts of the number of incoming HTTP requests.
     */
    public Count.Pool getCountInHttpRequestsPerIpFamily()
    {
        return this.inHttpRequestsPerIpFamily;
    }

    /**
     * Returns the map of counts of outgoing HTTP responses. The HTTP responses in
     * the map are indexed by the IP family used for the transport of the response.
     * 
     * @return The map of counts of the number of outgoing HTTP responses.
     */
    public Count.Pool getCountOutHttpResponsesPerIpFamily()
    {
        return this.outHttpResponsesPerIpFamily;
    }

    /**
     * Returns the count of the number of incoming HTTP requests.
     * 
     * @return The count of the number of incoming HTTP requests.
     */
    public Count getCountInHttpRequests()
    {
        return this.inHttpRequests;
    }

    /**
     * Returns the map of counts of incoming HTTP responses. The HTTP responses in
     * the map are indexed by the HTTP-response-status of the response.
     * 
     * @return The map of counts of incoming HTTP responses.
     */
    public Count.Pool getCountInHttpResponsesPerStatus()
    {
        return this.inHttpResponsesPerStatus;
    }

    /**
     * Returns the count of the number of outgoing HTTP requests.
     * 
     * @return The count of the number of outgoing HTTP requests.
     */
    public Count getCountOutHttpRequests()
    {
        return this.outHttpRequests;
    }

    /**
     * Returns the map of counts of outgoing HTTP responses. The HTTP responses in
     * the map are indexed by the HTTP-response-status of the response.
     * 
     * @return The map of counts of outgoing HTTP responses.
     */
    public Count.Pool getCountOutHttpResponsesPerStatus()
    {
        return this.outHttpResponsesPerStatus;
    }

    /**
     * Returns the sequence of events (HTTP requests/responses in a time-line).
     * 
     * @return The sequence of events (HTTP requests/responses in a time-line).
     */
    public Event.Sequence getHistoryOfEvents()
    {
        return this.historyOfEvents;
    }

    /**
     * @param Sets the routing context.
     */
    public void setContext(final RoutingContext context)
    {
        this.context = RoutingContextBuffer.of(context);
    }

    /**
     * Returns a JSON representation of this object.
     * 
     * @see Instance#prettyPrintingDisable
     * @see Instance#prettyPrintingEnable
     * @return A JSON representation of this object.
     */
    @Override
    public String toString()
    {
        try
        {
            return mapper.writeValueAsString(this);
        }
        catch (JsonProcessingException e)
        {
            return e.toString();
        }
    }
}
