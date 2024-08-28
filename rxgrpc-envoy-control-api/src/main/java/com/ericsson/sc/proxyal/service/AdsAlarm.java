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
 * Created on: Jun 18, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.service;

import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.ericsson.adpal.fm.Alarm;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 
 */
class AdsAlarm
{

    private final Alarm alarm;
    private TreeSet<String> reporters;
    private String details;
    private Optional<String> presetFaultyResource;
    private boolean isRaised;
    private final Lock lock;

    public AdsAlarm(Alarm alarm)
    {
        this.alarm = alarm;
        this.isRaised = false;
        this.reporters = new TreeSet<>();
        this.presetFaultyResource = Optional.empty();
        this.lock = new ReentrantLock();
    }

    public void raiseMajor(final String reporter,
                           final String details) throws JsonProcessingException
    {
        this.lock.lock();
        try
        {
            this.reporters.add(reporter);
            this.details = details;
            this.isRaised = true;
            if (presetFaultyResource.isPresent())
            {
                this.alarm.setFaultyResource(presetFaultyResource.get());
                this.presetFaultyResource = Optional.empty();
            }
            this.alarm.raiseMajor(reporter, details);
        }
        finally
        {
            this.lock.unlock();
        }
    }

    public void reRaiseMajor() throws JsonProcessingException
    {
        this.lock.lock();
        try
        {
            if (this.isRaised)
            {
                // We just need one reporter that had already raised this alarm
                // since the underlying Alarm class also keeps track of the reporters:
                this.alarm.raiseMajor(this.reporters.first(), this.details);
            }
        }
        finally
        {
            this.lock.unlock();
        }
    }

    public void cease(final String reporter) throws JsonProcessingException
    {
        this.lock.lock();
        try
        {
            this.reporters.remove(reporter);
            if (this.reporters.isEmpty())
            {
                this.isRaised = false;
            }

            this.alarm.cease(reporter);
        }
        finally
        {
            this.lock.unlock();
        }
    }

    /**
     * Pre-set the faulty resource. This is typically called when new configuration
     * is received from CM. The alarm's faulty resource is only changed when the
     * next alarm is raised. This way it is still possible to cease or re-raise
     * alarms with the previous faulty resource until a new alarm shall be raised.
     * 
     * @param resource
     */
    public void presetFaultyResource(String resource)
    {
        this.lock.lock();
        try
        {
            this.presetFaultyResource = (resource == null) ? Optional.empty() : Optional.of(resource);
        }
        finally
        {
            this.lock.unlock();
        }
    }

}
