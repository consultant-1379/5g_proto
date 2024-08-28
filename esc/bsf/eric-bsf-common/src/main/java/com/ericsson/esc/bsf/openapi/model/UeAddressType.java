/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 17, 2021
 *     Author: echfari
 */
package com.ericsson.esc.bsf.openapi.model;

/**
 * Type of UE address in a PCF binding or a discovery query
 */
public enum UeAddressType
{
    /**
     * IPv4 address only
     */
    INET4,
    /**
     * IPv6 address only
     */
    INET6,
    /**
     * IPv4 and IPv6 address
     */
    INET4_6,
    /**
     * MAC address only
     */
    MAC
}