package com.ericsson.sc.sepp.manager;

import java.nio.charset.StandardCharsets;

//import com.ericsson.sc.bsf.etcd.PcfNf;
import com.ericsson.sc.rxetcd.JsonValueSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;

public class N32cSerializer
{
    static final String SEPP_PREFIX = "/ericsson/sc/sepp/";
    static final String N32C_PREFIX = SEPP_PREFIX + "n32c/";
    private final JsonValueSerializer<String, EtcdSecurityNegotiationDatum> nrfSerializer;

    /**
     * Create a new serialized
     * 
     * @param om A configured Jackson Object mapper to be used for JSON
     *           serialization
     */
    public N32cSerializer(ObjectMapper om)
    {
        this.nrfSerializer = new JsonValueSerializer<>(N32C_PREFIX,
                                                       EtcdSecurityNegotiationDatum.class,
                                                       om,
                                                       (String key) -> ByteString.copyFrom(key, StandardCharsets.UTF_8),
                                                       key -> new String(key, StandardCharsets.UTF_8));

    }

    /**
     * @return The serializer that can convert a EtcdSecurityNegotiationDatum object
     *         to a key-value pair, for storing to etcd database
     */
    public JsonValueSerializer<String, EtcdSecurityNegotiationDatum> EtcdSecurityNegotiationDatum()
    {
        return this.nrfSerializer;
    }
}
