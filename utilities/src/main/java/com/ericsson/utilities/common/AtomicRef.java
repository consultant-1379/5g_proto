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
 * Created on: Jul 31, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures atomic access to an Object reference.
 */
public class AtomicRef<T>
{
    private static final Logger log = LoggerFactory.getLogger(AtomicRef.class);

    private T value;

    /**
     * Constructs a new AtomicRef with an initial value set to the value passed.
     * 
     * @param value The initial value. May be null.
     */
    public AtomicRef(final T value)
    {
        this.value = value;
    }

    /**
     * @return The current value.
     */
    public synchronized T get()
    {
        return this.value;
    }

    /**
     * Retrieves the current value to return and set the value to the new value
     * passed.
     * 
     * @param value The value to be set.
     * @return The current value.
     */
    public synchronized T getAndSet(final T value)
    {
        final T result = this.value;
        this.value = value;
        return result;
    }

    /**
     * Set the value to the value passed if is not the same reference.
     * 
     * @param value The value to be set.
     * @return False if there was no change, true otherwise.
     */
    public synchronized boolean setIfChanged(final T value)
    {
        return this.setIfChanged(value, null);
    }

    /**
     * Set the value to the value passed if is not the same reference.
     * 
     * @param value   The value to be set.
     * @param logInfo If provided (!= null), this message is logged on info-level.
     *                Format like: "New value: {}, old value: {}".
     * @return False if there was no change, true otherwise.
     */
    public synchronized boolean setIfChanged(final T value,
                                             final String logInfo)
    {
        if (this.value == value)
            return false;

        if (logInfo != null && this.value != null)
            log.info(logInfo, value, this.value);

        this.value = value;
        return true;
    }
}
