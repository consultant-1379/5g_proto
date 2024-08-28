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
 * Created on: Nov 20, 2019
 *     Author: xchrfar
 */
package com.ericsson.esc.lib;

/**
 * HTTP Content types
 */
public class ContentTypes
{
    private ContentTypes()
    {
    }

    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";
    public static final String PROBLEM_JSON = "application/problem+json; charset=utf-8";
    public static final String CONTENT_TYPE_3GPPHAL_JSON = "application/3gppHal+json; charset=utf-8";
}
