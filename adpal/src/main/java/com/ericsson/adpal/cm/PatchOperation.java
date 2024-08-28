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
 * Created on: Apr 9, 2020
 *     Author: epaxale
 */

package com.ericsson.adpal.cm;

/**
 * 
 */

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets PatchOperation
 */
public enum PatchOperation
{

    ADD("add"),

    COPY("copy"),

    MOVE("move"),

    REMOVE("remove"),

    REPLACE("replace"),

    TEST("test");

    private String value;

    PatchOperation(String value)
    {
        this.value = value;
    }

    @JsonValue
    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return String.valueOf(value);
    }

    public static PatchOperation fromValue(String value)
    {
        for (PatchOperation b : PatchOperation.values())
        {
            if (b.value.equals(value))
            {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }

}