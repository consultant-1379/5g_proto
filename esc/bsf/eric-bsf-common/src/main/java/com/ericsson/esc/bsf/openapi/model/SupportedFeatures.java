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

import java.util.Objects;
import java.util.regex.Pattern;

import com.ericsson.esc.lib.ValidationException;

public final class SupportedFeatures
{
    private final long supportedFeat;
    private static final Pattern pattern = Pattern.compile("^[A-Fa-f0-9]*$");

    public SupportedFeatures(String suppFeat)
    {
        Objects.requireNonNull(suppFeat);

        if (!pattern.matcher(suppFeat).matches())
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, "suppFeat", "Invalid supported features");
        }
        try
        {
            this.supportedFeat = Long.parseLong(suppFeat, 16);
        }
        catch (NumberFormatException e)
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, "suppFeat", "Invalid supported features", e);
        }
    }

    private SupportedFeatures(long suppFeat)
    {
        this.supportedFeat = suppFeat;
    }

    public SupportedFeatures commonSuppFeat(SupportedFeatures suppfeat)
    {
        return new SupportedFeatures(this.supportedFeat & suppfeat.getSupportedFeatures());
    }

    public long getSupportedFeatures()
    {
        return this.supportedFeat;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(supportedFeat);
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
        SupportedFeatures other = (SupportedFeatures) obj;
        return supportedFeat == other.supportedFeat;
    }

    @Override
    public String toString()
    {
        return Long.toString(this.supportedFeat, 16);
    }
}
