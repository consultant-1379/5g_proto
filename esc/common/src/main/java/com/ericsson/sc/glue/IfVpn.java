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
 * Created on: Mar 30, 2021
 *     Author: eaoknkr
 */

package com.ericsson.sc.glue;

import com.ericsson.sc.Tls;

/**
 * 
 */
public interface IfVpn
{
    Tls getTls();

    String getName();

    Integer getPort();

    String getIpv4Address();

    String getIpv6Address();
}
