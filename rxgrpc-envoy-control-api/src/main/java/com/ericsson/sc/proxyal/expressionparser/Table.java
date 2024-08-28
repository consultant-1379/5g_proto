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
 * Created on: Dec 1, 2020
 *     Author: enocakh
 */

package com.ericsson.sc.proxyal.expressionparser;

import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.GeneratedMessageV3.Builder;

public class Table<T> implements Expression
{
    private final String name;
    private final T key;

    public Table(String name,
                 T key)
    {
        this.name = name;
        this.key = key;
    }

    public Builder<?> construct()
    {
        return null;
    }

    @Override
    public String toString()
    {
        if (key instanceof String)
        {
            return String.format("term_kvt(%s,%s)", this.name, this.key);
        }
        else if (key instanceof Integer)
        {
            return String.format("term_dt(%s,%s)", this.name, this.key);
        }

        return StringUtils.EMPTY;
    }
}