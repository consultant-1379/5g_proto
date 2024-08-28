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

import com.datastax.oss.driver.api.core.data.UdtValue;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.MappingCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.ericsson.esc.bsf.openapi.model.BaseUeAddress;
import com.ericsson.esc.bsf.openapi.model.Ipv6Prefix;
import com.ericsson.esc.bsf.openapi.model.MacAddr48;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Cassandra driver codec for {@link BsfSchema.ue_address} UDT
 */
public final class BaseUeAddressCodec extends MappingCodec<UdtValue, BaseUeAddress>
{
    /**
     * The UDT type name
     */
    public static final String UDT_NAME = BsfSchema.ue_address.typeName();

    protected BaseUeAddressCodec(@NonNull TypeCodec<UdtValue> innerCodec)
    {
        super(innerCodec, GenericType.of(BaseUeAddress.class));
    }

    @Override
    protected BaseUeAddress innerToOuter(UdtValue value)
    {
        try
        {
            if (value == null)
            {
                return null;
            }
            // This shall throw if ipv6 address is written to database
            final Inet4Address ipv4Addr = (Inet4Address) value.getInetAddress(BsfSchema.ue_address.ipv4_addr.field());
            final var macAddrStr = value.getString(BsfSchema.ue_address.mac_addr48.field());
            return BaseUeAddress.create(ipv4Addr, //
                                        value.get(BsfSchema.ue_address.ipv6_prefix.field(), Ipv6Prefix.class),
                                        value.getString(BsfSchema.ue_address.ip_domain.field()),
                                        macAddrStr != null ? new MacAddr48(macAddrStr) : null);
        }
        catch (Exception e)
        {
            throw new MalformedDbContentException("Unexpected content in BSF database: " + value.getFormattedContents(), e);
        }
    }

    @Override
    protected UdtValue outerToInner(BaseUeAddress value)
    {
        if (value == null)
            return null;

        return getCqlType().newValue()
                           .setInetAddress(BsfSchema.ue_address.ipv4_addr.field(), value.getIpv4Addr().orElse(null))
                           .set(BsfSchema.ue_address.ipv6_prefix.field(), value.getIpv6Prefix().orElse(null), Ipv6Prefix.class)
                           .setString(BsfSchema.ue_address.mac_addr48.field(), value.getMacAddr48().map(MacAddr48::toString).orElse(null))
                           .setString(BsfSchema.ue_address.ip_domain.field(), value.getIpDomain().orElse(null));
    }

    @NonNull
    @Override
    public UserDefinedType getCqlType()
    {
        return (UserDefinedType) super.getCqlType();
    }
}
