/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 17, 2023
 *     Author: zdoukon
 */

package com.ericsson.sc.expressionparser;

import java.util.List;
import java.util.Optional;

public class ReturnValue
{
    private Optional<String> stringVal = Optional.empty();
    private Optional<List<String>> listVal = Optional.empty();
    private Optional<Boolean> boolVal = Optional.empty();
    private boolean caseInsensitive = false;

    public ReturnValue(String stringVal)
    {
        this.stringVal = Optional.of(stringVal);
    }

    public ReturnValue(List<String> listVal)
    {
        this.listVal = Optional.ofNullable(listVal);
    }

    public ReturnValue(Boolean boolVal)
    {
        this.boolVal = Optional.of(boolVal);
    }

    public Boolean isNull()
    {
        var listNullOrEmpty = listVal.isPresent() && !listVal.get().isEmpty();
        return !(stringVal.isPresent() || boolVal.isPresent() || listNullOrEmpty);
    }

    @Override
    public String toString()
    {
        return "ReturnValue [stringVal=" + stringVal + ", listVal=" + listVal + ", boolVal=" + boolVal + ", caseInsensitive=" + caseInsensitive + "]";
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
            return false;

        if (obj.getClass() != this.getClass())
            return false;

        final ReturnValue rv = (ReturnValue) obj;

        if (this.isNull() || rv.isNull())
            return false;

        if (this.stringVal.isPresent() && this.caseInsensitive)
            return rv.getListVal().orElse(List.of()).stream().anyMatch(lv -> lv.toLowerCase().equals(this.stringVal.get().toLowerCase()));

        if (this.listVal.isPresent() && this.caseInsensitive)
            return this.listVal.get().stream().anyMatch(lv -> lv.toLowerCase().equals(rv.getStringVal().orElse("").toLowerCase()));

        if (this.stringVal.isPresent())
            return rv.getListVal().orElse(List.of()).contains(this.stringVal.get());

        if (this.listVal.isPresent())
            return this.listVal.get().contains(rv.getStringVal().orElse(""));

        if (this.boolVal.isPresent() && rv.getBoolVal().isPresent())
            return this.boolVal.get().equals(rv.boolVal.get());

        return false;
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = result * 31 + this.boolVal.hashCode();
        result = result * 31 + this.listVal.hashCode();
        result = result * 31 + this.stringVal.hashCode();
        return result;
    }

    public void setCaseInsensitive(boolean caseInsensitive)
    {
        this.caseInsensitive = caseInsensitive;
    }

    public Optional<String> getStringVal()
    {
        return stringVal;
    }

    public Optional<List<String>> getListVal()
    {
        return listVal;
    }

    public Optional<Boolean> getBoolVal()
    {
        return boolVal;
    }
}
