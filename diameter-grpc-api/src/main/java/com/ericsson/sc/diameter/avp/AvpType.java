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
 * Created on: May 1, 2020
 *     Author: echfari
 */
package com.ericsson.sc.diameter.avp;

import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvp;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvp.DataCase;

public abstract class AvpType<T>
{
    protected final DataCase dataCase;

    protected AvpType(DataCase dataCase)
    {
        this.dataCase = dataCase;
    }

    abstract T getValue(DiameterAvp avp);

    public T value(DiameterAvp avp)
    {
        if (avp.getDataCase().equals(this.dataCase))
        {
            return getValue(avp);
        }
        else
        {
            throw new InvalidAvpValueException(avp);
        }
    }

    public abstract void setValue(T value,
                                  DiameterAvp.Builder avpBuilder);

    public abstract void setEmpty(DiameterAvp.Builder avpBuilder);

    @Override
    public String toString()
    {
        var builder = new StringBuilder();
        builder.append("AvpType [dataCase=");
        builder.append(dataCase);
        builder.append("]");
        return builder.toString();
    }

}
