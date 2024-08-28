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

import com.ericsson.sc.proxyal.proxyconfig.ProxyMetadataBuilder.MetaDataType;
import java.util.Objects;

import java.util.Map;
import java.util.HashMap;

/**
 * 
 */
public class ProxyMetadataMap
{
    private Map<MetaDataType, Map<String, MetadataValue>> metadataMap = new HashMap<>();

    public ProxyMetadataMap()
    {
        this.metadataMap = new HashMap<>();
    }

    public Map<MetaDataType, Map<String, MetadataValue>> getMetadataMap()
    {
        return metadataMap;
    }

    public void addMetadata(MetaDataType mdType,
                            String key,
                            MetadataValue value)
    {
        Map<String, MetadataValue> nestedMap = new HashMap<>();
        if (metadataMap.containsKey(mdType))
        {
            nestedMap = metadataMap.get(mdType);
            nestedMap.put(key, value);
        }
        else
        {
            nestedMap.put(key, value);
            metadataMap.put(mdType, nestedMap);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(metadataMap);
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
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyMetadataMap other = (ProxyMetadataMap) obj;

        return Objects.equals(metadataMap, other.metadataMap);
    }

}
