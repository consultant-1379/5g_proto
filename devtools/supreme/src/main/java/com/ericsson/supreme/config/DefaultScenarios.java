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
 * Created on: Feb 3, 2022
 *     Author: eaoknkr
 */

package com.ericsson.supreme.config;

import com.ericsson.supreme.exceptions.ValidationException;

/**
 * 
 */
public class DefaultScenarios
{
    private String outputDir;
    private Integer expirationDays;

    public String getOutputDir()
    {
        return outputDir;
    }

    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }

    public Integer getExpirationDays()
    {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays)
    {
        this.expirationDays = expirationDays;
    }

    @Override
    public String toString()
    {
        var sb = new StringBuilder();
        sb.append("\n  outputDir: ");
        sb.append(outputDir);
        sb.append("\n  expirationDays: ");
        sb.append(expirationDays);
        return sb.toString();
    }

    /**
     * Check that mandatory fields are defined. Check that nested fields are valid.
     */
    public void validate()
    {
        if (outputDir == null)
        {
            throw new ValidationException("Mandatory field 'outputDir' was not defined in defaultScenarios");
        }

        if (expirationDays == null)
        {
            throw new ValidationException("Mandatory field 'expirationDays' was not defined in defaultScenarios");
        }
    }

}
