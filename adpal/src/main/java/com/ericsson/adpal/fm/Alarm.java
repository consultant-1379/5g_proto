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
 * Created on: Sep 2, 2019
 *     Author: eedstl
 */

package com.ericsson.adpal.fm;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.fm.FmAlarmService;
import com.ericsson.sc.fm.model.fi.FaultIndication;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Encapsulation of the definition of an Alarm. May be used for your
 * convenience.
 */
public class Alarm
{
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "detail", "reporters" })
    private static class Detail
    {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "details" })
        private static class Pool
        {
            private SortedMap<String, Detail> data = new TreeMap<>();

            public void add(final String reporter,
                            final String details)
            {
                this.data.computeIfAbsent(details, Detail::new).addReporter(reporter);
            }

            public void clear()
            {
                this.data.clear();
            }

            @JsonProperty("details")
            public Collection<Detail> details()
            {
                return this.data.values();
            }

            /**
             * Removes the reporter from the alarm details.
             * 
             * @param reporter The reporter to remove.
             * @return -1: reporter not found<br>
             *         0: reported removed && no reporters exist<br>
             *         1: reporter removed && reporters exist
             */
            public int remove(final String reporter)
            {
                final boolean removed = this.data.entrySet().stream().map(e -> e.getValue().removeReporter(reporter)).anyMatch(b -> b);
                this.data.entrySet().removeIf(e -> e.getValue().hasNoReporter());

                return !removed ? -1 : this.data.isEmpty() ? 0 : 1;
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

        private final String detail;
        private final Set<String> reporters = new TreeSet<>();

        public Detail(final String detail)
        {
            this.detail = detail;
        }

        public Detail addReporter(final String reporter)
        {
            this.reporters.add(reporter);
            return this;
        }

        @JsonProperty("detail")
        public String detail()
        {
            return this.detail;
        }

        public boolean hasNoReporter()
        {
            return this.reporters.isEmpty();
        }

        public boolean removeReporter(final String reporter)
        {
            return this.reporters.remove(reporter);
        }

        @JsonProperty("reporters")
        public Set<String> reporters()
        {
            return this.reporters;
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

    private static final Logger log = LoggerFactory.getLogger(Alarm.class);
    private static final ObjectMapper mapper = Jackson.om();

    private static String createDescription(String base,
                                            String details)
    {
        if (base == null)
            base = "";

        if (details == null)
            details = "";

        base = base.trim();
        details = details.trim();

        final StringBuilder b = new StringBuilder(base);

        b.append(base.endsWith(".") ? "" : ".");

        if (!details.isEmpty())
        {
            b.append(" Details: ").append(details);
            b.append(details.endsWith(".") ? "" : ".");
        }

        return b.toString();
    }

    private final FmAlarmService alarmService;
    private String faultyResource;
    private final String faultDescription;
    private final Detail.Pool details;
    private FaultIndication faultIndication;

    public Alarm(final String serviceName,
                 final FmAlarmService alarmService,
                 final String faultName,
                 final String faultyResource,
                 final String faultDescription,
                 final long expirationInSecs)
    {
        this.alarmService = alarmService;
        this.faultIndication = new FaultIndicationBuilder().withFaultName(faultName) //
                                                           .withFaultyResource(faultyResource) //
                                                           .withServiceName(serviceName) //
                                                           .withSeverity(Severity.MAJOR) //
                                                           .withDescription(faultDescription) //
                                                           .withExpiration(expirationInSecs) //
                                                           .build();
        this.faultyResource = faultyResource;
        this.faultDescription = faultDescription;
        this.details = new Detail.Pool();
    }

    public synchronized void cease() throws JsonProcessingException
    {
        this.details.clear();

        if (this.alarmService == null)
        {
            log.warn("Would cease alarm: faultName='{}', faultyResource='{}', faultDescription='{}'",
                     this.faultIndication.getFaultName(),
                     this.faultIndication.getFaultyResource(),
                     "");
            return;
        }

        var newFaultIndication = new FaultIndicationBuilder(this.faultIndication).withDescription("").build();
        this.alarmService.cease(newFaultIndication).subscribe(() ->
        {
        }, t -> log.error("Error ceasing alarm. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
    }

    public synchronized void cease(final String reporter) throws JsonProcessingException
    {
        if (reporter == null)
            return;

        final int result = this.details.remove(reporter);

        if (result == 1)
        {
            // Raise again to reflect the removed reporter in the alarm details.

            if (this.alarmService != null)
            {
                this.alarmService.raise(new FaultIndicationBuilder(this.faultIndication).withFaultyResource(this.faultyResource) //
                                                                                        .withDescription(createDescription(this.faultDescription,
                                                                                                                           this.details.toString()))
                                                                                        .build())
                                 .subscribe(() ->
                                 {
                                 }, t -> log.error("Error raising alarm. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
            }
        }
        else if (result == 0)
        {
            log.debug("details={}", this.details);

            if (this.alarmService == null)
            {
                log.warn("Would cease alarm: reporter='{}', faultName='{}', faultyResource='{}', faultDescription='{}'",
                         reporter,
                         this.faultIndication.getFaultName(),
                         this.faultIndication.getFaultyResource(),
                         "");

                return;
            }

            this.alarmService.cease(new FaultIndicationBuilder(this.faultIndication).withDescription("").build()).subscribe(() ->
            {
            }, t -> log.error("Error ceasing alarm. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
        }
    }

    public synchronized void raise(final Severity faultSeverity,
                                   final String details) throws JsonProcessingException
    {
        var newFaultIndication = new FaultIndicationBuilder(this.faultIndication).withSeverity(faultSeverity) //
                                                                                 .withFaultyResource(this.faultyResource) //
                                                                                 .withDescription(createDescription(this.faultDescription, details)) //
                                                                                 .build();

        if (this.alarmService == null)
        {
            log.warn("Would raise alarm: faultName='{}', faultyResource='{}', faultSeverity={}, faultDescription='{}', expirationInSecs={}",
                     this.faultIndication.getFaultName(),
                     this.faultIndication.getFaultyResource(),
                     this.faultIndication.getSeverity(),
                     this.faultIndication.getDescription(),
                     this.faultIndication.getExpiration());

            return;
        }

        this.alarmService.raise(newFaultIndication).subscribe(() ->
        {
        }, t -> log.error("Error raising alarm. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
    }

    public synchronized void raise(final Severity faultSeverity,
                                   final String reporter,
                                   final String details) throws JsonProcessingException
    {
        if (reporter == null)
            return;

        this.details.add(reporter, details);
        log.debug("details={}", this.details);

        var newFaultIndication = new FaultIndicationBuilder(this.faultIndication).withSeverity(faultSeverity) //
                                                                                 .withFaultyResource(this.faultyResource) //
                                                                                 .withDescription(createDescription(this.faultDescription,
                                                                                                                    this.details.toString())) //
                                                                                 .build();

        if (this.alarmService == null)
        {
            log.warn("Would raise alarm: reporter='{}', faultName='{}', faultyResource='{}', faultSeverity={}, faultDescription='{}', additionalInformation='{}', expirationInSecs={}",
                     reporter,
                     this.faultIndication.getFaultName(),
                     this.faultIndication.getFaultyResource(),
                     this.faultIndication.getSeverity(),
                     this.faultIndication.getDescription(),
                     this.details,
                     this.faultIndication.getExpiration());
            return;
        }

        this.alarmService.raise(newFaultIndication).subscribe(() ->
        {
        }, t -> log.error("Error raising alarm. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
    }

    public void raiseCritical() throws JsonProcessingException
    {
        this.raiseCritical(null);
    }

    public void raiseCritical(final String details) throws JsonProcessingException
    {
        this.raise(Severity.CRITICAL, details);
    }

    public void raiseCritical(final String reporter,
                              final String details) throws JsonProcessingException
    {
        this.raise(Severity.CRITICAL, reporter, details);
    }

    public void raiseMajor() throws JsonProcessingException
    {
        this.raiseMajor(null);
    }

    public void raiseMajor(final String details) throws JsonProcessingException
    {
        this.raise(Severity.MAJOR, details);
    }

    public void raiseMajor(final String reporter,
                           final String details) throws JsonProcessingException
    {
        this.raise(Severity.MAJOR, reporter, details);
    }

    public void raiseMinor() throws JsonProcessingException
    {
        this.raiseMinor(null);
    }

    public void raiseMinor(final String details) throws JsonProcessingException
    {
        this.raise(Severity.MINOR, details);
    }

    public void raiseMinor(final String reporter,
                           final String details) throws JsonProcessingException
    {
        this.raise(Severity.MINOR, reporter, details);
    }

    public void raiseWarning() throws JsonProcessingException
    {
        this.raiseWarning(null);
    }

    public void raiseWarning(final String details) throws JsonProcessingException
    {
        this.raise(Severity.WARNING, details);
    }

    public void raiseWarning(final String reporter,
                             final String details) throws JsonProcessingException
    {
        this.raise(Severity.WARNING, reporter, details);
    }

    public synchronized Alarm setFaultyResource(final String faultyResource)
    {
        this.faultyResource = faultyResource;
        log.debug("faultyResource={}", this.faultyResource);
        return this;
    }
}
