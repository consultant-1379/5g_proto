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
 * Created on: Mar 26, 2021
 *     Author: eaoknkr
 */

package com.ericsson.sc.glue;

import java.util.Collection;

import com.ericsson.sc.proxyal.proxyconfig.proxyclusterconfig.ProxyCluster;
import com.ericsson.sc.proxyal.proxyconfig.proxyrouteconfig.ProxyRoute;

/**
 * 
 */
public interface IfEgress extends ConfigConverter
{
    Collection<ProxyCluster> getClusters();

    Collection<ProxyRoute> getRoutesForVHostName(String vHostName);

}
