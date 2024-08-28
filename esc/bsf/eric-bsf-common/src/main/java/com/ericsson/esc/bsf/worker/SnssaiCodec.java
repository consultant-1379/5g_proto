/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 17, 2021
 *     Author: echfari
 */
package com.ericsson.esc.bsf.worker;

import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.ericsson.esc.bsf.openapi.model.Snssai;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Cassandra driver codec for {@link BsfSchema.snssai} UDT
 */
public final class SnssaiCodec extends MappingCodec<UdtValue, Snssai>
{
    /**
     * The UDT type name
     */
    public static final String UDT_NAME = BsfSchema.snssai.typeName();

    protected SnssaiCodec(@NonNull TypeCodec<UdtValue> innerCodec)
    {
        super(innerCodec, GenericType.of(Snssai.class));
    }

    @Override
    protected Snssai innerToOuter(UdtValue value)
    {
        try
        {
            if (value == null)
            {
                return null;
            }

            final var sst = value.get(BsfSchema.snssai.sst.field(), Integer.class); // sst cannot be null but value.getInt() is dangerous
            final var sd = value.getString(BsfSchema.snssai.sd.field());
            return Snssai.create(sst, sd);
        }
        catch (Exception e)
        {
            throw new MalformedDbContentException("Unexpected content in BSF database: " + value.getFormattedContents(), e);
        }
    }

    @Override
    protected UdtValue outerToInner(Snssai value)
    {
        if (value == null)
            return null;

        return getCqlType().newValue() //
                           .set(BsfSchema.snssai.sst.field(), value.getSst(), Integer.class) // sst cannot be null but value.setInt() is dangerous
                           .setString(BsfSchema.snssai.sd.field(), value.getSd());
    }

    @NonNull
    @Override
    public UserDefinedType getCqlType()
    {
        return (UserDefinedType) super.getCqlType();
    }

}
