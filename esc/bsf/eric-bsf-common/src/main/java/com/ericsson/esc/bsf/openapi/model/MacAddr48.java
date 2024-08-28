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

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import com.ericsson.esc.lib.ValidationException;

public class MacAddr48
{
    private final String macAddr48Str;
    private static final Pattern pattern = Pattern.compile("^([0-9a-fA-F]{2})((-[0-9a-fA-F]{2}){5})$");
    private static final int MAC_ADDR_STR_MAX_LENGTH = 20; // Actual maximum length is 17 characters,

    public MacAddr48(String macAddr48Str)
    {
        Objects.requireNonNull(macAddr48Str);

        // Ensure that given string length is not very large, so that regex evaluation
        // time is bounded, to protect against DoS attacks
        if (macAddr48Str.length() > MAC_ADDR_STR_MAX_LENGTH || !pattern.matcher(macAddr48Str).matches())
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, "macAddr48", "Invalid macAddr48");
        }
        this.macAddr48Str = macAddr48Str.toUpperCase(Locale.getDefault());
    }

    public String getMacAddr48Str()
    {
        return macAddr48Str;
    }

    @Override
    public String toString()
    {
        return this.macAddr48Str;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(macAddr48Str);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MacAddr48 other = (MacAddr48) obj;
        return Objects.equals(macAddr48Str, other.macAddr48Str);
    }

}
