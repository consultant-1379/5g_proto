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
 * Created on: Jun 17, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Representation of an event.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "trigger", "request", "response" })
public class Event
{
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "timeStamp", "count" })
    public static class UniqueKey implements Comparable<UniqueKey>
    {
        private static AtomicLong cnt = new AtomicLong(0);

        @JsonIgnore
        private final Long timeStamp;

        @JsonIgnore
        private final Long count;

        public UniqueKey()
        {
            this(System.currentTimeMillis());
        }

        public UniqueKey(final Long timeStamp)
        {
            this.timeStamp = timeStamp;
            this.count = UniqueKey.cnt.getAndIncrement();
        }

        @JsonIgnore
        @Override
        public int compareTo(final UniqueKey other)
        {
            final int compareTimeStamps = this.getTimeStamp().compareTo(other.getTimeStamp());

            if (compareTimeStamps != 0)
                return compareTimeStamps;

            return this.getCount().compareTo(other.getCount());
        }

        @JsonProperty("count")
        public Long getCount()
        {
            return this.count;
        }

        @JsonProperty("timeStamp")
        public Long getTimeStamp()
        {
            return this.timeStamp;
        }

        /**
         * Returns a JSON representation of this object.
         * 
         * @see Event#prettyPrintingDisable
         * @see Event#prettyPrintingEnable
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "timeStamp", "dataType", "data" })
    public static class Request
    {
        @JsonIgnore
        private final long timeStamp;

        @JsonIgnore
        private final String dataType;

        @JsonIgnore
        private final String data;

        @JsonIgnore
        private final UniqueKey key;

        public Request(String dataType,
                       String data)
        {
            this.timeStamp = System.currentTimeMillis();
            this.dataType = dataType;
            this.data = data;
            this.key = new UniqueKey(this.timeStamp);
        }

        @JsonProperty("data")
        public String getData()
        {
            return this.data;
        }

        @JsonProperty("dataType")
        public String getDataType()
        {
            return this.dataType;
        }

        @JsonIgnore
        public UniqueKey getKey()
        {
            return this.key;
        }

        @JsonProperty("timeStamp")
        public long getTimeStampInMillis()
        {
            return this.timeStamp;
        }

        /**
         * Returns a JSON representation of this object.
         * 
         * @see Event#prettyPrintingDisable
         * @see Event#prettyPrintingEnable
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "timeStamp", "result", "resultDetails" })
    public static class Response
    {
        @JsonIgnore
        private final long timeStamp;

        @JsonIgnore
        private final HttpResponseStatus result;

        @JsonIgnore
        private final String resultDetails;

        public Response(HttpResponseStatus result,
                        String resultDetails)
        {
            this.timeStamp = System.currentTimeMillis();
            this.result = result;
            this.resultDetails = resultDetails;
        }

        @JsonIgnore
        public HttpResponseStatus getResult()
        {
            return this.result;
        }

        @JsonProperty("result")
        public String getResultAsString()
        {
            return this.result.toString();
        }

        @JsonIgnore
        public int getResultCode()
        {
            return this.result.code();
        }

        @JsonProperty("resultDetails")
        public String getResultDetails()
        {
            return this.resultDetails;
        }

        @JsonIgnore
        public String getResultReasonPhrase()
        {
            return this.result.reasonPhrase();
        }

        @JsonProperty("timeStamp")
        public long getTimeStampInMillis()
        {
            return timeStamp;
        }

        /**
         * Returns a JSON representation of this object.
         * 
         * @see Event#prettyPrintingDisable
         * @see Event#prettyPrintingEnable
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "id", "events" })
    public static class Sequence
    {
        @JsonIgnore
        private static final int MAX_NUM_EVENTS = 32;

        @JsonIgnore
        public static final int MAX_TIME_SPAN_IN_MILLIS = 5 * 60 * 1000; // 5 min

        @JsonIgnore
        private String id;

        @JsonIgnore
        private SortedMap<Event.UniqueKey, Event> events;
        private Event latest;

        /**
         * Constructor.
         * 
         * @param id
         */
        public Sequence(String id)
        {
            this.id = id;
            this.events = new ConcurrentSkipListMap<>();
            this.latest = null;
        }

        /**
         * Constructor.
         * 
         * @param id
         * @param events
         */
        public Sequence(String id,
                        List<Event> events)
        {
            this.id = id;
            events.forEach(e -> this.events.put(e.getRequest().getKey(), e));
        }

        /**
         * Clear all events.
         */
        @JsonIgnore
        public void clear()
        {
            this.events.clear();
        }

        /**
         * Get a list of all events.
         * 
         * @return A list of all events.
         */
        @JsonIgnore
        public List<Event> get()
        {
            return this.get(MAX_TIME_SPAN_IN_MILLIS, null);
        }

        /**
         * Get a list of all events which are not older than (now - timSpanInMillis).
         * 
         * @param timeSpanInMillis
         * @return A list of all events which are not older than (now -
         *         timSpanInMillis).
         */
        @JsonIgnore
        public List<Event> get(final long timeSpanInMillis)
        {
            return this.get(timeSpanInMillis, null);
        }

        /**
         * Get a list of all events for a certain trigger which are not older than (now
         * - timSpanInMillis).
         * 
         * @param timeSpanInMillis
         * @param trigger
         * @return A list of all events for a certain trigger which are not older than
         *         (now - timSpanInMillis).
         */
        @JsonIgnore
        public List<Event> get(final long timeSpanInMillis,
                               final String trigger)
        {
            long nowInMillis = System.currentTimeMillis();

            List<Event> result = new ArrayList<>();

            this.events.values().forEach(v ->
            {
                if (nowInMillis - v.getRequest().getTimeStampInMillis() <= timeSpanInMillis && (trigger == null || trigger.equals(v.getTrigger())))
                    result.add(v);
            });

            return result;
        }

        /**
         * Get the ID of this sequence of events.
         * 
         * @return The ID of this sequence of events.
         */
        @JsonProperty("id")
        public String getId()
        {
            return this.id;
        }

        /**
         * Get the latest event from the list.
         * 
         * @return the latest event from the list.
         */
        @JsonIgnore
        public Event getLatest()
        {
            return this.latest;
        }

        /**
         * Get an iterator over all the entries in this sequence of events. Usage:
         * 
         * <pre>
         * {@code
         * Iterator<Entry<Long, Event>> it = s.iterator();
         * while (it.hasNext())
         * {
         *     final Entry<Long, Event> e = it.next();
         *     // do sth. useful with e
         * }
         * </pre>
         * 
         * @return An iterator over all the entries in this sequence of events.
         */
        @JsonIgnore
        public Iterator<Entry<UniqueKey, Event>> iterator()
        {
            return this.events.entrySet().iterator();
        }

        /**
         * Add an event to the sequence of events.
         * 
         * @param item
         */
        @JsonIgnore
        public void put(final Event item)
        {
            long nowInMillis = System.currentTimeMillis();

            // If there are more than MAX_NUM_EVENTS events in the list, remove those which
            // are older than MAX_TIME_SPAN_IN_MILLIS.

            while (this.events.size() > MAX_NUM_EVENTS
                   && nowInMillis - this.events.get(this.events.firstKey()).getRequest().getTimeStampInMillis() > MAX_TIME_SPAN_IN_MILLIS)
            {
                this.events.remove(this.events.firstKey());
            }

            this.events.put(item.request.getKey(), item);
            this.latest = item;
        }

        /**
         * Returns a JSON representation of this object.
         * 
         * @see Event#prettyPrintingDisable
         * @see Event#prettyPrintingEnable
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

        @JsonProperty("events")
        private List<Event> getEvents()
        {
            return new ArrayList<>(this.events.values());
        }
    }

    private static final ObjectMapper mapper = Jackson.om();

    /**
     * Disable pretty-printing of JSON output.
     */
    public static void prettyPrintingDisable()
    {
        mapper.disable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Enable pretty-printing of JSON output.
     */
    public static void prettyPrintingEnable()
    {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @JsonIgnore
    private final String trigger;

    @JsonIgnore
    private Request request;

    @JsonIgnore
    private Response response;

    public Event(String trigger,
                 String requestDataType,
                 String requestData)
    {
        this.trigger = trigger;
        this.request = new Request(requestDataType, requestData);
        this.response = null;
    }

    @JsonProperty("request")
    public Request getRequest()
    {
        return this.request;
    }

    @JsonProperty("response")
    public synchronized Response getResponse()
    {
        return this.response;
    }

    @JsonProperty("trigger")
    public String getTrigger()
    {
        return trigger;
    }

    public synchronized Event setResponse(HttpResponseStatus result)
    {
        return this.setResponse(result, null);
    }

    public synchronized Event setResponse(HttpResponseStatus result,
                                          String resultDetails)
    {
        this.response = new Response(result, resultDetails);
        return this;
    }

    /**
     * Returns a JSON representation of this object.
     * 
     * @see Event#prettyPrintingDisable
     * @see Event#prettyPrintingEnable
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
