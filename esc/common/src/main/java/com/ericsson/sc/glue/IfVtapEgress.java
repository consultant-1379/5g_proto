/**
 * COPYRIGHT ERICSSON GMBH 2022
 * <p>
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 * <p>
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 * <p>
 * Created on: Jul 21, 2022
 * Author: zpanevg
 */

package com.ericsson.sc.glue;

import java.util.List;

import com.ericsson.utilities.common.IfNamedListItem;

public interface IfVtapEgress extends IfNamedListItem
{
    String getName();

    Boolean getEnabled();

    List<String> getNfPoolRef();

    IfAllNfPools getAllNfPools();

}