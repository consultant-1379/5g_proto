/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 9, 2024
 *     Author: ztsakon
 */

package com.ericsson.sc.proxyal.healtchecklogservice;

/**
 * 
 */

/**
 * Operational state of a producer as it is seen from the SCP/SEPP:
 * <ul>
 * <li>UNHEALTHY_HOST_EJECTION: Host ejection
 * <li>HEALTHY_HOST_ADDITION: Host addition after the result of the first ever
 * health check on it, which is the case that the configured healthy threshold
 * is bypassed and the host is immediately added
 * <li>DEGRADED_HEALTHY_HOST: Healthy host became degraded
 * <li>NO_LONGER_DEGRADED_HOST: A degraded host returned to being healthy
 * <ul>
 */
public enum OperationalState
{
    UNHEALTHY_HOST_EJECTION,
    HEALTHY_HOST_ADDITION,
    DEGRADED_HEALTHY_HOST,
    NO_LONGER_DEGRADED_HOST,
    FAILURE_ON_FIRST_CHECK,
    UNKNOWN
}
