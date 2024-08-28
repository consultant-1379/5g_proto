package com.ericsson.sc.glue;

/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 15, 2019
 *     Author: eedstl
 */

import java.util.List;

public interface IfNfFunction
{
    <T extends IfNfInstance> List<T> getNfInstance();
}