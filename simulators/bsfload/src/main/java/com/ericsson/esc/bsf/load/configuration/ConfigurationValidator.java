/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 25, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.configuration;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.esc.bsf.load.server.InvalidParameter;
import com.ericsson.esc.bsf.openapi.model.RecoveryTime;
import com.ericsson.esc.lib.ValidationException;

public class ConfigurationValidator
{
    private final List<InvalidParameter> invalidParams;

    public ConfigurationValidator()
    {
        invalidParams = new ArrayList<>();
    }

    public void addInvalidParameters(List<InvalidParameter> invalidParams)
    {
        this.invalidParams.addAll(invalidParams);
    }

    public void check(boolean valid,
                      String name,
                      String message)
    {
        if (!valid)
        {
            invalidParams.add(new InvalidParameter(name, message));
        }
    }

    public void checkNonNull(Object parameter,
                             String name,
                             String message)
    {
        if (parameter == null)
        {
            invalidParams.add(new InvalidParameter(name, message));
        }
    }

    public void checkNonNullPositive(Integer parameter,
                                     String name,
                                     String message)
    {
        if (parameter == null || parameter <= 0)
        {
            invalidParams.add(new InvalidParameter(name, message));
        }
    }

    public void checkNonNullPositive(Long parameter,
                                     String name,
                                     String message)
    {
        if (parameter == null || parameter <= 0)
        {
            invalidParams.add(new InvalidParameter(name, message));
        }
    }

    public void checkNonNullZeroOrPositive(Long parameter,
                                           String name,
                                           String message)
    {
        if (parameter == null || parameter < 0)
        {
            invalidParams.add(new InvalidParameter(name, message));
        }
    }

    public void checkNullOrNonNullPositive(Long parameter,
                                           String name,
                                           String message)
    {
        if (parameter != null && parameter <= 0)
        {
            invalidParams.add(new InvalidParameter(name, message));
        }
    }

    public void checkRecoveryTime(String recoveryTime,
                                  String name,
                                  String message)
    {
        try
        {
            new RecoveryTime(recoveryTime);
        }
        catch (ValidationException e)
        {
            invalidParams.add(new InvalidParameter(name, message));
        }
    }

    public List<InvalidParameter> getInvalidParam()
    {
        return invalidParams;
    }
}
