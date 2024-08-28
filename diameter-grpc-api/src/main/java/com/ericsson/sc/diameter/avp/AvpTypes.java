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

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import com.ericsson.sc.diameter.FramedIpv6Prefix;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvp;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvp.Builder;
import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvp.DataCase;
import com.ericsson.gs.tm.diameter.service.grpc.GroupedAvp;
import com.google.common.net.InetAddresses;
import com.google.protobuf.ByteString;

public final class AvpTypes
{
    private AvpTypes()
    {

    }

    public static final AvpType<String> DIAMETER_IDENTITY = new AvpType<>(DataCase.DIAMETERIDENTITY)
    {
        @Override
        String getValue(DiameterAvp avp)
        {
            final var result = avp.getDiameterIdentity().toStringUtf8();
            if (result.isBlank())
                throw new IllegalArgumentException("DiameterIdentity is empty");
            return result;
        }

        @Override
        public void setValue(String value,
                             Builder avpBuilder)
        {
            avpBuilder.setDiameterIdentity(ByteString.copyFromUtf8(value));
        }

        @Override
        public void setEmpty(Builder avpBuilder)
        {
            setValue("example", avpBuilder);
        }
    };

    public static final AvpType<ByteString> OCTET_STRING = new AvpType<>(DataCase.OCTETSTRING)
    {
        @Override
        ByteString getValue(DiameterAvp avp)
        {
            return avp.getOctetString();
        }

        @Override
        public void setValue(ByteString value,
                             Builder avpBuilder)
        {
            avpBuilder.setOctetString(value);
        }

        @Override
        public void setEmpty(Builder avpBuilder)
        {
            setValue(ByteString.EMPTY, avpBuilder);
        }
    };

    public static final AvpType<Inet4Address> FRAMED_IP_ADDRESS = new AvpType<>(DataCase.OCTETSTRING)
    {
        @Override
        Inet4Address getValue(DiameterAvp avp)
        {
            try
            {
                return (Inet4Address) InetAddress.getByAddress(avp.getOctetString().toByteArray());
            }
            catch (Exception e)
            {
                throw new InvalidAvpValueException(e, avp);
            }
        }

        @Override
        public void setValue(Inet4Address value,
                             Builder avpBuilder)
        {
            avpBuilder.setOctetString(ByteString.copyFrom(value.getAddress()));
        }

        @Override
        public void setEmpty(Builder avpBuilder)
        {
            setValue((Inet4Address) InetAddresses.forString("0.0.0.0"), avpBuilder);
        }
    };

    public static final AvpType<FramedIpv6Prefix> FRAMED_IPV6_PREFIX = new AvpType<>(DataCase.OCTETSTRING)
    {
        @Override
        FramedIpv6Prefix getValue(DiameterAvp avp)
        {
            try
            {
                return FramedIpv6Prefix.fromAvpOctets(avp.getOctetString().toByteArray());
            }
            catch (Exception e)
            {
                throw new InvalidAvpValueException(e, avp);
            }
        }

        @Override
        public void setValue(FramedIpv6Prefix value,
                             Builder avpBuilder)
        {
            avpBuilder.setOctetString(ByteString.copyFrom(value.encode()));
        }

        @Override
        public void setEmpty(Builder avpBuilder)
        {
            setValue(FramedIpv6Prefix.fromInet6Address(0, (Inet6Address) InetAddresses.forString(" 0:0:0:0:0:0:0:0")), avpBuilder);
        }
    };

    public static final AvpType<InetAddress> ADDRESS = new AvpType<>(DataCase.ADDRESS)
    {
        @Override
        InetAddress getValue(DiameterAvp avp)
        {
            final var addr = avp.getAddress().toByteArray();
            try
            {
                return InetAddress.getByAddress(addr);
            }
            catch (Exception e)
            {
                throw new InvalidAvpValueException(e, avp);
            }
        }

        @Override
        public void setValue(InetAddress value,
                             Builder avpBuilder)
        {
            avpBuilder.setAddress(ByteString.copyFrom(value.getAddress()));
        }

        @Override
        public void setEmpty(Builder avpBuilder)
        {
            setValue(InetAddresses.forString("0.0.0.0"), avpBuilder);
        }
    };

    public static final AvpType<String> UTF8STRING = new AvpType<>(DataCase.UTF8STRING)
    {
        @Override
        String getValue(DiameterAvp avp)
        {
            return avp.getUtf8String().toStringUtf8();
        }

        @Override
        public void setValue(String value,
                             Builder avpBuilder)
        {
            avpBuilder.setUtf8String(ByteString.copyFromUtf8(value));
        }

        @Override
        public void setEmpty(Builder avpBuilder)
        {
            setValue("", avpBuilder);
        }
    };

    public static final AvpType<Integer> UNSIGNED32 = new AvpType<>(DataCase.UNSIGNED32)
    {
        @Override
        Integer getValue(DiameterAvp avp)
        {
            return avp.getUnsigned32();
        }

        @Override
        public void setValue(Integer value,
                             Builder avpBuilder)
        {
            avpBuilder.setUnsigned32(value);
        }

        @Override
        public void setEmpty(Builder avpBuilder)
        {
            setValue(0, avpBuilder);
        }
    };

    public static final AvpType<GroupedAvp> GROUPED = new AvpType<>(DataCase.GROUPED)
    {
        private final GroupedAvp empty = GroupedAvp.newBuilder().build();

        @Override
        GroupedAvp getValue(DiameterAvp avp)
        {
            return avp.getGrouped();
        }

        @Override
        public void setValue(GroupedAvp value,
                             Builder avpBuilder)
        {
            avpBuilder.setGrouped(value);
        }

        @Override
        public void setEmpty(Builder avpBuilder)
        {
            setValue(empty, avpBuilder);
        }
    };

}
