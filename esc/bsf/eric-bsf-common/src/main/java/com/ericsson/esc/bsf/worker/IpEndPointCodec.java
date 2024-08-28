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

import java.net.Inet4Address;
import java.net.Inet6Address;

import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.ericsson.esc.bsf.openapi.model.IpEndPoint;
import com.ericsson.esc.bsf.openapi.model.TransportProtocol;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Cassandra driver codec for {@link BsfSchema.ip_end_point} UDT
 */
public class IpEndPointCodec extends MappingCodec<UdtValue, IpEndPoint>
{
    /**
     * The UDT type name
     */
    public static final String UDT_NAME = BsfSchema.ip_end_point.typeName();

    protected IpEndPointCodec(@NonNull TypeCodec<UdtValue> innerCodec)
    {
        super(innerCodec, GenericType.of(IpEndPoint.class));
    }

    @Override
    protected IpEndPoint innerToOuter(UdtValue value)
    {
        try
        {
            if (value == null)
            {
                return null;
            }

            final var ipv4Address = (Inet4Address) value.getInetAddress(BsfSchema.ip_end_point.ipv4_address.field());
            final var ipv6Address = (Inet6Address) value.getInetAddress(BsfSchema.ip_end_point.ipv6_address.field());
            final var port = value.get(BsfSchema.ip_end_point.port.field(), Integer.class); // Should not use value.getInt() because port might be null
            final var transport = value.getString(BsfSchema.ip_end_point.transport.field());
            final var transportEnum = transport != null ? TransportProtocol.fromValue(transport) : null;

            return IpEndPoint.create(ipv4Address, ipv6Address, transportEnum, port);
        }
        catch (Exception e)
        {
            throw new MalformedDbContentException("Unexpected content in BSF database: " + value.getFormattedContents(), e);
        }

    }

    @Override
    protected UdtValue outerToInner(IpEndPoint value)
    {
        if (value == null)
            return null;

        return getCqlType().newValue()
                           .setInetAddress(BsfSchema.ip_end_point.ipv4_address.field(), value.getIpv4Address())
                           .setInetAddress(BsfSchema.ip_end_point.ipv6_address.field(), value.getIpv6Address())
                           .set(BsfSchema.ip_end_point.port.field(), value.getPort(), Integer.class) // Should not use value.setInt() because port might be null
                           .setString(BsfSchema.ip_end_point.transport.field(), value.getTransport() != null ? value.getTransport().toString() : null);
    }

    @NonNull
    @Override
    public UserDefinedType getCqlType()
    {
        return (UserDefinedType) super.getCqlType();
    }

}
