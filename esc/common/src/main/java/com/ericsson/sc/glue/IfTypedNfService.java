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
 * Created on: Feb 8, 2021
 *     Author: echaias
 */

package com.ericsson.sc.glue;

import java.util.List;

import com.ericsson.sc.nfm.model.NfStatus;

/**
 * 
 */
public interface IfTypedNfService extends IfTypedNfAddressProperties
{
    /**
     * CAUTION: setter methods are not allowed in this interface since they might
     * cause problems in deserialization
     */

    NfStatus getStatus();

    /**
     * The set identity of the service.
     * 
     */
    List<String> getSetId();

    /**
     * The nfType of the nfService based on the parent nfInstance.
     */
    void setNfType(String nfType);

    String getNfInstanceId();

    void setNfInstanceId(String nfInstanceId);

    String getNfInstanceName();

    void setNfInstanceName(String nfInstanceName);

    String getNfServiceId();

    String getApiPrefix();

}