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
 * Created on: Mar 8, 2021
 *     Author: echfari
 */
package com.ericsson.sc.bsf.etcd;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.ericsson.sc.rxetcd.JsonValueSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;

/**
 * Serializes PCF database objects for storing to etcd database
 */
public class PcfDbSerializer
{

    static final String BSF_PREFIX = "/ericsson/sc/bsf/";
    static final String PCFNF_PREFIX = BSF_PREFIX + "PcfNf/";
    private final JsonValueSerializer<UUID, PcfNf> nrfSerializer;

    /**
     * Create a new serialized
     * 
     * @param om A configured Jackson Object mapper to be used for JSON
     *           serialization
     */
    public PcfDbSerializer(ObjectMapper om)
    {
        this.nrfSerializer = new JsonValueSerializer<>(PCFNF_PREFIX,
                                                       PcfNf.class,
                                                       om,
                                                       (UUID key) -> ByteString.copyFrom(key.toString(), StandardCharsets.UTF_8),
                                                       key -> UUID.fromString(new String(key, StandardCharsets.UTF_8)));

    }

    /**
     * @return The serializer that can convert a PcfNf object to a key-value pair,
     *         for storing to etcd database
     */
    public JsonValueSerializer<UUID, PcfNf> pcfNf()
    {
        return this.nrfSerializer;
    }
}
