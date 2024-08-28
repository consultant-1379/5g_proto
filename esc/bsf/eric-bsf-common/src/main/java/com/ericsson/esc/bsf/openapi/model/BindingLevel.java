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
 * Created on: Jan 28, 2021
 *     Author: entngrg
 */

package com.ericsson.esc.bsf.openapi.model;

import com.datastax.oss.driver.shaded.guava.common.base.Enums;
import com.ericsson.esc.lib.ValidationException;

public class BindingLevel
{
    private final String bindLevelStr;

    private enum BindingLevelEnum
    {
        NF_SET,
        NF_INSTANCE
    }

    public BindingLevel(String bindLevelStr)
    {
        if (bindLevelStr != null && !Enums.getIfPresent(BindingLevelEnum.class, bindLevelStr).isPresent())
        {
            throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "bindLevel", "Invalid bindLevel");
        }
        this.bindLevelStr = bindLevelStr;
    }

    public String getBindLevelStr()
    {
        return bindLevelStr;
    }

    @Override
    public String toString()
    {
        return this.bindLevelStr;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((bindLevelStr == null) ? 0 : bindLevelStr.hashCode());
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
        BindingLevel other = (BindingLevel) obj;
        if (bindLevelStr == null)
        {
            if (other.bindLevelStr != null)
            {
                return false;
            }
        }
        else if (!bindLevelStr.equals(other.bindLevelStr))
        {
            return false;
        }
        return true;
    }
}
