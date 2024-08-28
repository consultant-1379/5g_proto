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
 *     Author: zavvann
 */

package com.ericsson.sc.proxyal.proxyconfig;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.google.protobuf.ListValue;
import com.google.protobuf.Value;

/**
 * 
 */
public class MetadataListValue<S extends MetadataValue> implements MetadataValue
{
    private List<S> mdValue = new ArrayList<>();

    public MetadataListValue()
    {
        this.mdValue = new ArrayList<>();
    }

    public MetadataListValue(List<S> mdValue)
    {
        super();
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
        return Value.newBuilder().setListValue(ListValue.newBuilder().addAllValues(mdValue.stream().map(v -> v.getMetadataValue()).toList())).build();
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
        this.mdValue = (List<S>) mdValue;
    }

    public void addInList(S element)
    {
        mdValue.add(element);
    }

    public List<S> getListValue()
    {
        return mdValue;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(mdValue);
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

        MetadataListValue<?> that = (MetadataListValue<?>) obj;
        return Objects.equals(mdValue, that.mdValue);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("[");

        for (S element : mdValue)
        {
            result.append(element.toString()).append(", ");
        }

        // Remove the trailing comma and space if the list is not empty
        if (!mdValue.isEmpty())
        {
            result.setLength(result.length() - 2);
        }

        result.append("]");

        return result.toString();
    }

}
