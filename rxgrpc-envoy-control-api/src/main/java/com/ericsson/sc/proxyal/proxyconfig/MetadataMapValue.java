/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Dec 13, 2023
 *     Author: zmavioa
 */

package com.ericsson.sc.proxyal.proxyconfig;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 
 */
public class MetadataMapValue implements MetadataValue
{
    private Map<String, String> mdValue;

    public MetadataMapValue(Map<String, String> mdValue)
    {
        this.mdValue = mdValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.sc.proxyal.proxyconfig.MetadataValue#getMedataValue()
     */
    @Override
    public Value getMetadataValue()
    {
        Map<String, Value> md = new HashMap<>();
        this.mdValue.entrySet().forEach(entry -> md.put(entry.getKey(), Value.newBuilder().setStringValue(entry.getValue()).build()));

        var innerStruct = Struct.newBuilder().putAllFields(md).build();
        return Value.newBuilder().setStructValue(innerStruct).build();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconfig.MetadataValue#setMetadataValue(java.lang.
     * Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void setMetadataValue(T mdValue)
    {
        this.mdValue = (Map<String, String>) mdValue;
    }

    public Map<String, String> getMapValue()
    {
        return mdValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        MetadataMapValue that = (MetadataMapValue) obj;
        return Objects.equals(mdValue, that.mdValue);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mdValue);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("{");

        for (Map.Entry<String, String> entry : mdValue.entrySet())
        {
            result.append(entry.getKey()).append("=").append(entry.getValue()).append(", ");
        }

        // Remove the trailing comma and space if the map is not empty
        if (!mdValue.isEmpty())
        {
            result.setLength(result.length() - 2);
        }

        result.append("}");

        return result.toString();
    }

}
