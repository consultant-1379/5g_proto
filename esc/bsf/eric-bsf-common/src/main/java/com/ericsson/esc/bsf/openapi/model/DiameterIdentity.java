/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 9, 2022
 *     Author: entngrg
 */

package com.ericsson.esc.bsf.openapi.model;

import java.util.Objects;

import com.ericsson.esc.lib.ValidationException;
import com.google.re2j.Pattern;

public class DiameterIdentity
{
    private final String diameterIdentityStr;
    private static final Pattern pattern = Pattern.compile("^([A-Za-z0-9]+([-A-Za-z0-9]+)\\.)+[a-z]{2,}$");
    private static final int DIAMETER_IDENTITY_STR_MAX_LENGTH = 260; // Actual maximum length for FQDNs is 255 characters,

    public DiameterIdentity(String diameterIdentityStr)
    {
        Objects.requireNonNull(diameterIdentityStr);

        // Ensure that given string length is not very large, so that regex evaluation
        // time is bounded, to protect against DoS attacks
        if (diameterIdentityStr.length() > DIAMETER_IDENTITY_STR_MAX_LENGTH || !pattern.matcher(diameterIdentityStr).matches())
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR, "pcfDiamHostpcfDiamRealm", "Invalid DiameterIdentity.");
        }
        this.diameterIdentityStr = diameterIdentityStr;
    }

    public String getDiameterIdentityStr()
    {
        return diameterIdentityStr;
    }

    @Override
    public String toString()
    {
        return this.diameterIdentityStr;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(diameterIdentityStr);
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
        DiameterIdentity other = (DiameterIdentity) obj;
        return Objects.equals(diameterIdentityStr, other.diameterIdentityStr);
    }

}
