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
 * Created on: Sep 26, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.utilities.dns;

import java.util.Map;

public interface IfDnsLookupContext
{
    String getHost();

    ResolutionResult getIpAddr(final IpFamily ipFamily);

    Map<IpFamily, ResolutionResult> getIpAddrs();

    boolean isNotResolvedYet();

    boolean isNumericHost();

    boolean isResolved();

    IfDnsLookupContext putIpAddr(final IpFamily ipFamily,
                                 final ResolutionResult ipAddr);

}