package com.ericsson.sc.glue;

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
 * Created on: Mar 11, 2021
 *     Author: eaoknkr
 */

import java.util.List;
import java.util.Set;

import com.ericsson.utilities.common.IfNamedListItem;

/**
 * 
 */
public interface IfNfPoolDiscovery extends IfNamedListItem
{
    List<String> getStaticNfInstanceDataRef();

    <T extends IfDiscoveredNfInstance> List<T> getDiscoveredNfInstance();

    <T extends IfDiscoveredScpInstance> List<T> getDiscoveredScpInstance();
}
