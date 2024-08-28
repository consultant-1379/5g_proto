/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 14, 2024
 *     Author: ztsakon
 */

package com.ericsson.sc.glue;

import java.util.List;

import com.ericsson.sc.nfm.model.ExpectedResponseHttpStatusCode;
import com.ericsson.sc.nfm.model.HttpMethod;

/**
 * 
 */
public interface IfActiveHealthCheck
{

    Integer getTimeInterval();

    Integer getUnhealthyThreshold();

    Integer getNoTrafficTimeInterval();

    Integer getTimeout();

    HttpMethod getHttpMethod();

    String getHttpPath();

    List<ExpectedResponseHttpStatusCode> getExpectedResponseHttpStatusCode();

}
