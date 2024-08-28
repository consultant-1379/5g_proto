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
 * Created on: Sep 12, 2023
 *     Author: zathsok
 */

package com.ericsson.sc.glue;

import java.util.List;

//import com.ericsson.sc.nfm.model.NfStatus;
import com.ericsson.utilities.common.IfNamedListItem;

/**
 * 
 */
public interface IfTypedSeppInstance extends IfNamedListItem, IfTypedNfAddressProperties
{
    /**
     * CAUTION: setter methods are not allowed in this interface since they might
     * cause problems in deserialization
     */

    String getNfType();

    void setNfType(String nfType);

    /**
     * The capacity of the SEPP instance
     */
    Integer getCapacity();

    /**
     * The priority of the SEPP instance
     */
    Integer getPriority();

    /**
     * Address of the service, at least one of FQDN or IPv4 or IPv6 address must be
     * given
     */
    IfAddress getAddress();

    /**
     * The SEPP instance identity
     */
    String getNfInstanceId();

    /**
     * The SEPP instance name
     */
    String getNfInstanceName();

    void setNfInstanceName(String nfName);

    /**
     * The geo locality of the SEPP
     */
    String getLocality();

    /**
     * The identity of the SEPP
     */
    List<String> getNfSetId();

    /**
     * The SCP domains this SEPP is associated with.
     */
    List<String> getScpDomain();

    String toString();

    int hashCode();

    boolean equals(Object other);

}
