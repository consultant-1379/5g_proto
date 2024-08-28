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

/**
 * 
 */
public interface IfTypedNfAddressProperties
{
    /**
     * CAUTION: setter methods are not allowed in this interface since they might
     * cause problems in deserialization
     */

    /**
     * Name identifying the service supported. (Required)
     * 
     */
    String getName();

    /**
     * Specifies the capacity of this service.
     * 
     */
    Integer getCapacity();

    /**
     * The priority of the service.
     * 
     */
    Integer getPriority();

    /**
     * Address of the service, at least one of FQDN or IPv4 or IPv6 address must be
     * given.
     * 
     */
    IfAddress getAddress();

    /**
     * The nfType of the parent nfInstance.
     */
    String getNfType();

    void setNfType(String nfType);

    /**
     * The SEPP instance identity of the parent nfInstance.
     */
    String getNfInstanceId();

    /**
     * The SEPP instance name of the parent nfInstance.
     */
    String getNfInstanceName();

    /**
     * The prefix of the service.
     */
    String getPrefix();

    void setPrefix(String prefix);

    String toString();

    int hashCode();

    boolean equals(Object other);

}