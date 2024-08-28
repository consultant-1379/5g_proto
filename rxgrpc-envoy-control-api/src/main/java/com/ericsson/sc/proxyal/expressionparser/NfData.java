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
 * Created on: Jan 14, 2021
 *     Author: eavapsr
 */

package com.ericsson.sc.proxyal.expressionparser;

import com.google.protobuf.GeneratedMessageV3.Builder;

/**
 * 
 */
public class NfData implements Expression
{

    private final String type;

    public NfData(String type)
    {
        this.type = type;
    }

    public Builder<?> construct()
    {
        return null;
    }

    @Override
    public String toString()
    {

        return String.format("term_nfdata(\":%s\")", this.type);
    }

}
