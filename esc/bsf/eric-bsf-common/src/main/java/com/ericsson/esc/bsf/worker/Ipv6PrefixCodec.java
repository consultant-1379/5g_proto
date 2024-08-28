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

import java.net.Inet6Address;

import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.ericsson.esc.bsf.openapi.model.Ipv6Prefix;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Cassandra driver codec for {@link BsfSchema.ipv6_prefix} UDT
 */
public final class Ipv6PrefixCodec extends MappingCodec<UdtValue, Ipv6Prefix>
{
    /**
     * The UDT type name
     */
    public static final String UDT_NAME = BsfSchema.ipv6_prefix.typeName();

    protected Ipv6PrefixCodec(@NonNull TypeCodec<UdtValue> innerCodec)
    {
        super(innerCodec, GenericType.of(Ipv6Prefix.class));
    }

    @Override
    protected Ipv6Prefix innerToOuter(UdtValue value)
    {
        try
        {
            if (value == null)
            {
                return null;
            }
            final Inet6Address prefixAddress = (Inet6Address) value.getInetAddress(BsfSchema.ipv6_prefix.prefix_address.field());
            final short prefixLength = value.get(BsfSchema.ipv6_prefix.prefix_length.field(), Short.class); // Throw if prefix length is null
            return new Ipv6Prefix(prefixAddress, //
                                  prefixLength);
        }
        catch (Exception e)
        {
            throw new MalformedDbContentException("Unexpected content in BSF database: " + value.getFormattedContents(), e);
        }
    }

    @Override
    protected UdtValue outerToInner(Ipv6Prefix value)
    {

        if (value == null)
        {
            return null;
        }

        return getCqlType() //
                           .newValue()
                           .setInetAddress("prefix_address", value.getPrefixAddress())
                           .setShort("prefix_length", value.getPrefixLength());
    }

    @NonNull
    @Override
    public UserDefinedType getCqlType()
    {
        return (UserDefinedType) super.getCqlType();
    }
}
