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

/**
 * 
 */
public interface IfTypedScpDomainInfo extends IfTypedNfAddressProperties
{
    /**
     * CAUTION: setter methods are not allowed in this interface since they might
     * cause problems in deserialization
     */

    /**
     * Domain of the discovered scp
     * 
     */
    String getDomain();

    void setNfInstanceId(String nfInstanceId);

    void setNfType(String nfType);
}