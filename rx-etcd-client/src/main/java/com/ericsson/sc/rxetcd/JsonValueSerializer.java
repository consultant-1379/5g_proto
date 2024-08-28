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
 * Created on: Feb 12, 2020
 *     Author: echfari
 */
package com.ericsson.sc.rxetcd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;

import io.reactivex.functions.Function;

/**
 * An etcd serializer that serializes values to JSON using Jackson library
 * 
 * @param <K> The key type
 * @param <V> The value type
 */
public class JsonValueSerializer<K, V> extends EtcdSerializer<K, V>
{

    public JsonValueSerializer(String prefix,
                               Class<V> clazz,
                               ObjectMapper om,
                               Function<K, ByteString> keySerializer,
                               Function<byte[], K> keyDeserializer)
    {
        super(prefix, keySerializer, keyDeserializer, (V val) -> ByteString.copyFrom(om.writeValueAsBytes(val)), val -> om.readValue(val, clazz));
    }

}
