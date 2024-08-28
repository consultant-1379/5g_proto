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
 * Created on: Feb 18, 2022
 *     Author: ecaoyuk
 */

package com.ericsson.sc.glue;

/**
 * Abstract interface IfTcpKeepalive implemented by TcpKeepalive and
 * TcpKeepalive__1 class in scp and sepp model
 */
public interface IfTcpKeepalive
{
    Integer getTime();

    Integer getInterval();

    Integer getProbes();
}
