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

public enum CommandCode
{
    UNKNOWN(0),
    CAPABILITY_EXCHANGE(257),
    RE_AUTH(258),
    AUTHORIZE_AUTHENTICATE(265),
    ABORT_SESSION(274),
    SESSION_TERMINATION(275);

    private int value;

    private CommandCode(int value)
    {
        this.value = value;
    }

    public int value()
    {
        return value;
    }
}
