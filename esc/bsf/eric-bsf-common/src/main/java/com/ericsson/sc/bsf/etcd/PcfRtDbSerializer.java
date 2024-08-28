/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 12, 2022
 *     Author: estoioa
 */

package com.ericsson.sc.bsf.etcd;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.ericsson.sc.rxetcd.EtcdSerializer;
import com.google.protobuf.ByteString;

public class PcfRtDbSerializer
{
    static final String BSF_PREFIX = "/ericsson/sc/bsf/";
//    path to pcf recovery time recordings
    static final String PCFRT_PREFIX = BSF_PREFIX + "PcfRt/";
    private final EtcdSerializer<UUID, Long> recTimeSerializer;

    /**
     * Create a new serialized/deserialized key/value pair
     * 
     */
    public PcfRtDbSerializer()
    {
        this.recTimeSerializer = new EtcdSerializer<>(PCFRT_PREFIX,
                                                      (UUID key) -> ByteString.copyFrom(key.toString(), StandardCharsets.UTF_8),
                                                      key -> UUID.fromString(new String(key, StandardCharsets.UTF_8)),
                                                      (Long value) -> ByteString.copyFrom(ByteBuffer.allocate(8)
                                                                                                    .order(ByteOrder.BIG_ENDIAN)
                                                                                                    .putLong(value)
                                                                                                    .array()),
                                                      value -> ByteBuffer.wrap(value).getLong());

    }

    /**
     * @return The serializer that can convert a PcfRt properties to a key-value
     *         pair, for storing to etcd database
     */
    public EtcdSerializer<UUID, Long> pcfRt()
    {
        return this.recTimeSerializer;
    }
}
