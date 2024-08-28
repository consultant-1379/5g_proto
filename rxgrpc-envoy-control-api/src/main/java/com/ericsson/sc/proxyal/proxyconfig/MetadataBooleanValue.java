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

import java.util.Objects;

import com.google.protobuf.Value;

/**
 * 
 */
public class MetadataBooleanValue implements MetadataValue
{

    private Boolean mdValue;

    public MetadataBooleanValue(Boolean mdValue)
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
        return Value.newBuilder().setBoolValue(mdValue).build();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.ericsson.sc.proxyal.proxyconfig.MetadataValue#setMetadataValue(java.lang.
     * Object)
     */
    @Override
    public <T> void setMetadataValue(T mdValue)
    {
        this.mdValue = (Boolean) mdValue;
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

        MetadataBooleanValue that = (MetadataBooleanValue) obj;
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
        return mdValue.toString();
    }

}
