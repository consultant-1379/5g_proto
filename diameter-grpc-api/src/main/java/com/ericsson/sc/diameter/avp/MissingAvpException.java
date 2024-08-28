/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 1, 2020
 *     Author: echfari
 */
package com.ericsson.sc.diameter.avp;

public class MissingAvpException extends AvpException
{

    private static final long serialVersionUID = 1L;

    public MissingAvpException(AvpDef<?> missingAvp)
    {
        super(missingAvp.withValue(), ResultCode.DIAMETER_MISSING_AVP);
    }
}
