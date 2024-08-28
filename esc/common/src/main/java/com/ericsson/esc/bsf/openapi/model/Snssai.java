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
 * Created on: Jan 25, 2019
 *     Author: zmelpan
 */

package com.ericsson.esc.bsf.openapi.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import com.ericsson.esc.lib.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 */
@JsonInclude(Include.NON_NULL)
public final class Snssai
{

    private final Integer sst;
    private final String sd;
    private static final Pattern sdPattern = Pattern.compile("^[A-Fa-f0-9]{6}$");

    /**
     * 
     * @param sst
     * @param sd  A valid SD or null
     */
    private Snssai(Integer sst,
                   String sd)
    {
        if (sst == null)
        {
            throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_MISSING_PARAM, "sst", "sst is mandatory");
        }

        if (sst < 0 || sst > 255)
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, "sst", "Invalid sst");
        }

        if (sd != null && !sdPattern.matcher(sd).matches())
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "sd", "Invalid sd");
        }

        this.sst = sst;
        this.sd = sd != null ? sd.toUpperCase(Locale.getDefault()) : null;
    }

    public static Snssai create(Integer sst,
                                String sd)
    {
        return new Snssai(sst, sd);
    }

    @JsonCreator
    public static Snssai createJson(@JsonProperty("sst") String sst,
                                    @JsonProperty("sd") String sd)
    {
        Integer sstToInt = null;

        if (sst != null)
        {
            try
            {
                sstToInt = Integer.parseUnsignedInt(sst);
            }
            catch (Exception e)
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, "sst", "Invalid sst", e);
            }
        }

        return create(sstToInt, sd);
    }

    /**
     * 
     * @return The Slice Service Type
     */
    public Integer getSst()
    {
        return sst;
    }

    /**
     * 
     * @return The Slice Differentiator
     */
    public String getSd()
    {
        return sd;
    }

    @Override
    public boolean equals(java.lang.Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        var snssai = (Snssai) o;

        return Objects.equals(sst, snssai.sst) && Objects.equals(sd, snssai.sd);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sst, sd);
    }

    @Override
    public String toString()
    {
        var sb = new StringBuilder();
        sb.append("{sst: ").append(sst).append(", sd: ").append(sd).append('}');

        return sb.toString();
    }
}