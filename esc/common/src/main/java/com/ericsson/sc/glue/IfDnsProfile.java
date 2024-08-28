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
 * Created on: Sep 28, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.glue;

import java.util.List;

import com.ericsson.sc.model.IpFamilyResolution;
import com.ericsson.utilities.common.IfNamedListItem;

/**
 * 
 */
public interface IfDnsProfile extends IfNamedListItem
{

    /**
     * IP family for the DNS resolution of an FQDN
     * 
     */
    List<IpFamilyResolution> getIpFamilyResolution();

}