/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 8, 2022
 *     Author: echaias
 */

package com.ericsson.sc.sepp.manager;

import java.util.UUID;

/**
 * 
 */
public class N32cFaultySeppException extends Exception
{
    private static final long serialVersionUID = 1L;

    private final String seppFqdn;
    private final UUID id;

    public N32cFaultySeppException(String faultySepp,
                                   UUID id,
                                   String msg,
                                   Throwable e)
    {
        super(msg, e);
        this.seppFqdn = faultySepp;
        this.id = id;
    }

    public N32cFaultySeppException(String faultySepp,
                                   UUID id,
                                   String msg)
    {
        super(msg);
        this.seppFqdn = faultySepp;
        this.id = id;
    }

    public String getFaultySeppFqdn()
    {
        return this.seppFqdn;
    }

    public UUID getId()
    {
        return this.id;
    }
}
