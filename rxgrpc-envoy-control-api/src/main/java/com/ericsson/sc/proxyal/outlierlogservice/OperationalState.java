/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Sep 11, 2019
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.outlierlogservice;

/**
 * Operational state of a producer as it is seen from the SCP:
 * <ul>
 * <li>REACHABLE: not detected for ejection and not blocked
 * <li>UNREACHABLE: detected for ejection, but ejection is not enforced
 * <li>BLOCKED: ejection is enforced
 * <ul>
 */
public enum OperationalState
{
    REACHABLE,
    UNREACHABLE,
    BLOCKED,
}
