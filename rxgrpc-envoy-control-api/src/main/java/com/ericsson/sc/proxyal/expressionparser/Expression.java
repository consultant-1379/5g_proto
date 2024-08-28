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
 * Created on: Nov 30, 2020
 *     Author: eaoknkr
 */

package com.ericsson.sc.proxyal.expressionparser;

import com.google.protobuf.GeneratedMessageV3.Builder;

/**
 * 
 */
public interface Expression
{
    public Builder<?> construct();
}
