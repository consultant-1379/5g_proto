/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 8, 2021
 *     Author: echaias
 */

package com.ericsson.sc.glue;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.nfm.model.NfStatus;
import com.ericsson.utilities.common.IfNamedListItem;

/**
 * 
 */
public interface IfTypedNfInstance extends IfNamedListItem
{
    /**
     * CAUTION: setter methods are not allowed in this interface since they might
     * cause problems in deserialization
     */

    /**
     * The service for which an NF has registered.
     * 
     */
    default List<IfTypedNfService> fetchNfService()
    {
        if (this instanceof IfDiscoveredNfInstance)
            return ((IfDiscoveredNfInstance) this).getDiscoveredNfService();

        if (this instanceof IfStaticNfInstance)
            return ((IfStaticNfInstance) this).getStaticNfService();

        if (this instanceof IfStaticScpInstance)
            return ((IfStaticScpInstance) this).getStaticNfService();

        return new ArrayList<IfTypedNfService>();
    }

    String getNfType();

    default NfStatus getNfStatus()
    {
        return null;
    }

    /**
     * The NF instance identity.
     * 
     */
    String getNfInstanceId();

    /**
     * The geographic locality of the NF.
     * 
     */
    String getLocality();

    /**
     * The set identity of the NF.
     * 
     */
    List<String> getNfSetId();

    /**
     * The SCP domains this NF is associated with.
     * 
     */
    List<String> getScpDomain();

    String toString();

    int hashCode();

    boolean equals(Object other);

}