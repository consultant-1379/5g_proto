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
 * Created on: Apr 5, 2021
 *     Author: eaoknkr
 */

package com.ericsson.sc.glue;

import java.util.List;

import com.ericsson.sc.nfm.model.NfProfile;
import com.ericsson.utilities.common.IfNamedListItem;

/**
 * 
 */
public interface IfGenericNfInstance extends IfNamedListItem
{
    default String getNfInstanceId()
    {
        return null;
    }

    <T extends NfProfile> List<T> getNfProfile();

    <T extends IfServiceAddress> List<T> getServiceAddress();

    <T extends IfNrfGroup> List<T> getNrfGroup();

    IfNrfService getNrfService();

    <T extends IfDnsProfile> List<T> getDnsProfile();

}
