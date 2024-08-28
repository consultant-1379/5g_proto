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

import com.ericsson.gs.tm.diameter.service.grpc.DiameterAvp;

public class AvpException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private final DiameterAvp failedAvp;
    private final ResultCode resultCode;

    public AvpException(Exception e,
                        DiameterAvp failedAvp,
                        ResultCode resultCode)
    {
        super(e);
        this.failedAvp = failedAvp;
        this.resultCode = resultCode;
    }

    public AvpException(DiameterAvp failedAvp,
                        ResultCode resultCode)
    {
        super();
        this.failedAvp = failedAvp;
        this.resultCode = resultCode;
    }

    public DiameterAvp getFailedAvp()
    {
        return this.failedAvp;
    }

    public ResultCode getResultCode()
    {
        return this.resultCode;
    }

}
