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
 * Created on: May 15, 2019
 *     Author: eedstl
 */

package com.ericsson.sc.glue;

import java.util.List;

public interface IfNfInstance extends IfGenericNfInstance
{
    <T extends IfStaticNfInstanceDatum> List<T> getStaticNfInstanceData();

    <T extends IfStaticScpInstanceDatum> List<T> getStaticScpInstanceData();

    <T extends IfStaticSeppInstanceDatum> List<T> getStaticSeppInstanceData();

    <T extends IfNfPool> List<T> getNfPool();

    <T extends IfRoutingCase> List<T> getRoutingCase();

    <T extends IfFailoverProfile> List<T> getFailoverProfile();

    <T extends IfIngressConnectionProfile> List<T> getIngressConnectionProfile();

    <T extends IfEgressConnectionProfile> List<T> getEgressConnectionProfile();

    String getEgressConnectionProfileRef();

    IfVtap getVtap();

    String getDnsProfileRef();

}
