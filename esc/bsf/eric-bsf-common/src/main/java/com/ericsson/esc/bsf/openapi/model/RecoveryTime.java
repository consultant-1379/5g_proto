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
 * Created on: Feb 4, 2019
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.openapi.model;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import com.ericsson.esc.lib.ValidationException;

/**
 * An RFC3339 encoded timestamp, representing a recovery time
 */
public class RecoveryTime
{
    private final String recoveryTimeStr;
    private static final Pattern RFC3339_PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})" // yyyy-MM-dd
                                                                   + "([Tt](\\d{2}):(\\d{2}):(\\d{2})(\\.\\d+)?)?" // 'T'HH:mm:ss.milliseconds
                                                                   + "([Zz]|([+-])(\\d{2}):(\\d{2}))?"); // 'Z' or time zone shift HH:mm following '+' or '-'
    private static final int RFC3339_STR_MAX_PARSE_LENGTH = 100; // Maximum allowable string length

    /**
     * 
     * @param recoveryTime A non null, valid recovery time
     * @throws ValidationException if given string is not an RFC3339 timestamp
     */
    public RecoveryTime(final String recoveryTime)
    {
        Objects.requireNonNull(recoveryTime);
        // Ensure that string length is bounded before feeding it to regex mathcer. This
        // is a DoS attack protection.
        if (recoveryTime.length() > RFC3339_STR_MAX_PARSE_LENGTH || !RFC3339_PATTERN.matcher(recoveryTime).matches())
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "recoveryTime", "Invalid recoveryTime");
        }

        this.recoveryTimeStr = recoveryTime.toUpperCase(Locale.getDefault());

    }

    /**
     * 
     * @return A valid timestamp, as plain string
     */
    public String getRecoveryTimeStr()
    {
        return recoveryTimeStr;
    }

    /**
     * Parse the recovery time
     * 
     * @throws DateTimeParseException
     * @return The parsed recovery time
     */
    public TemporalAccessor parse()
    {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(this.recoveryTimeStr);
    }

    @Override
    public String toString()
    {
        return this.recoveryTimeStr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((recoveryTimeStr == null) ? 0 : recoveryTimeStr.hashCode());
        return result;
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
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        RecoveryTime other = (RecoveryTime) obj;
        if (recoveryTimeStr == null)
        {
            if (other.recoveryTimeStr != null)
            {
                return false;
            }
        }
        else if (!recoveryTimeStr.equals(other.recoveryTimeStr))
        {
            return false;
        }
        return true;
    }
}
