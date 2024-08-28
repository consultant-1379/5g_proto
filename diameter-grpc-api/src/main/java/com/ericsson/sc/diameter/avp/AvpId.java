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

import java.util.Objects;

public class AvpId
{

    private final int avpCode;
    private final int vendorId;

    static AvpId of(int avpCode)
    {
        return new AvpId(avpCode, 0);
    }

    static AvpId of(int avpCode,
                    int vendorId)
    {
        return new AvpId(avpCode, vendorId);
    }

    private AvpId(int avpCode,
                  int vendorId)
    {
        this.avpCode = avpCode;
        this.vendorId = vendorId;
    }

    public int getAvpCode()
    {
        return avpCode;
    }

    public int getVendorId()
    {
        return vendorId;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("AvpId [avpCode=");
        builder.append(avpCode);
        builder.append(", vendorId=");
        builder.append(vendorId);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(avpCode, vendorId);
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
        AvpId other = (AvpId) obj;
        return avpCode == other.avpCode && vendorId == other.vendorId;
    }

}
