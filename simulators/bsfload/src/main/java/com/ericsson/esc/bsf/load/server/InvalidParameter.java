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
 * Created on: Nov 23, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.server;

/**
 * Refers to request invalid parameter.
 */
public class InvalidParameter
{
    private final String param;
    private final String reason;

    public InvalidParameter(String param,
                            String reason)
    {
        this.param = param;
        this.reason = reason;
    }

    /**
     * @return the param
     */
    public String getParam()
    {
        return param;
    }

    /**
     * @return the reason
     */
    public String getReason()
    {
        return reason;
    }

    @Override
    public String toString()
    {
        return "InvalidParameter [param=" + param + ", reason=" + reason + "]";
    }
}
