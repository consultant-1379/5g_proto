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
public interface IfTypedScpInstance extends IfNamedListItem
{
    /**
     * CAUTION: setter methods are not allowed in this interface since they might
     * cause problems in deserialization
     */

    /**
     * The info about the discovered scp domain
     * 
     */
    default List<IfTypedScpDomainInfo> fetchScpDomainInfo()
    {
        if (this instanceof IfDiscoveredScpInstance)
            return ((IfDiscoveredScpInstance) this).getDiscoveredScpDomainInfo();

        if (this instanceof IfStaticScpInstance)
            return ((IfStaticScpInstance) this).getStaticScpDomainInfo();

        return new ArrayList<IfTypedScpDomainInfo>();
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
     * The set identity of the NFs served by the SCP
     * 
     */
    List<String> getServedNfSetId();

    /**
     * The SCP domains this NF is associated with.
     * 
     */
    List<String> getScpDomain();

    String toString();

    int hashCode();

    boolean equals(Object other);

}