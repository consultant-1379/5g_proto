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
import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.diameter.FramedIpv6Prefix;
import com.ericsson.gs.tm.diameter.service.grpc.GroupedAvp;
import com.google.protobuf.ByteString;

/**
 * Diameter dictionary of known AVPs. Those AVPs can be accessed via instances
 * of {@link ParsedDiameterMessage}
 */
public final class Avps
{

    /**
     * All The diameter AVPs defined in this class, in a List form
     */
    private static final List<AvpDef<?>> allAvpDefs = new ArrayList<>();

    //
    // IETEF AVPs, Vendor-Id = 0
    //
    public static final AvpDef<Inet4Address> FRAMED_IP_ADDRESS = add(AvpDef.create(AvpTypes.FRAMED_IP_ADDRESS) //
                                                                           .setAvpCode(8)
                                                                           .setMbit()
                                                                           .build());
    public static final AvpDef<FramedIpv6Prefix> FRAMED_IPV6_PREFIX = add(AvpDef.create(AvpTypes.FRAMED_IPV6_PREFIX) //
                                                                                .setAvpCode(97)
                                                                                .setMbit()
                                                                                .build());
    public static final AvpDef<String> SESSION_ID = add(AvpDef.create(AvpTypes.UTF8STRING) //
                                                              .setAvpCode(263)
                                                              .setMbit()
                                                              .build());
    public static final AvpDef<String> ORIGIN_HOST = add(AvpDef.create(AvpTypes.DIAMETER_IDENTITY) //
                                                               .setAvpCode(264)
                                                               .setMbit()
                                                               .build());
    public static final AvpDef<Integer> RESULT_CODE = add(AvpDef.create(AvpTypes.UNSIGNED32) //
                                                                .setAvpCode(268)
                                                                .setMbit()
                                                                .build());
    public static final AvpDef<Integer> ORIGIN_STATE_ID = add(AvpDef.create(AvpTypes.UNSIGNED32) //
                                                                    .setAvpCode(278)
                                                                    .setMbit()
                                                                    .build());
    public static final AvpDef<GroupedAvp> FAILED_AVP = add(AvpDef.create(AvpTypes.GROUPED) //
                                                                  .setAvpCode(279)
                                                                  .setMbit()
                                                                  .build());
    public static final AvpDef<String> DESTINATION_REALM = add(AvpDef.create(AvpTypes.DIAMETER_IDENTITY) //
                                                                     .setAvpCode(283)
                                                                     .setMbit()
                                                                     .build());
    public static final AvpDef<String> PROXY_INFO = add(AvpDef.create(AvpTypes.DIAMETER_IDENTITY) //
                                                              .setAvpCode(284)
                                                              .setMbit()
                                                              .build());
    public static final AvpDef<String> DESTINATION_HOST = add(AvpDef.create(AvpTypes.DIAMETER_IDENTITY) //
                                                                    .setAvpCode(293)
                                                                    .setMbit()
                                                                    .build());
    public static final AvpDef<String> ORIGIN_REALM = add(AvpDef.create(AvpTypes.DIAMETER_IDENTITY) //
                                                                .setAvpCode(296)
                                                                .setMbit()
                                                                .build());

    //
    // 3GPP AVPs
    //
    public static final AvpDef<ByteString> IP_DOMAIN_ID = add(AvpDef.create(AvpTypes.OCTET_STRING) //
                                                                    .setAvpCode(537, VendorId.THREEGPP)
                                                                    .setMbit()
                                                                    .build());

    //
    // !!! Ensure that the above line is the final static member variable !!!!
    //
    private static final MessageParser MP = MessageParser.create().addAvpDefs(allAvpDefs).build();

    /**
     * 
     * @return An object that can parse the AVPs defined in this class
     */
    public static MessageParser messageParser()
    {
        return MP;
    }

    private static <T> AvpDef<T> add(AvpDef<T> def)
    {

        allAvpDefs.add(def);
        return def;
    }

    private Avps()
    {

    }

}
