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
 * Created on: May 20, 2020
 *     Author: eedrak
 */

package com.ericsson.sc.proxyal.filtergen;

/**
 * 
 */
public class HttpHeaderAction extends Action
{
    private HttpHeaderAction()
    {
    }

    static HttpHeaderAction addHttpHeader(String header,
                                          String headerValue)
    {
        var action = new HttpHeaderAction();
        action.setLuaCode(new StringBuilder().append(action.getLuaHandle())
                                             .append("headers():add(")
                                             .append("\"")
                                             .append(header)
                                             .append("\"")
                                             .append(", ")
                                             .append("\"")
                                             .append(headerValue)
                                             .append("\"")
                                             .append(")")
                                             .toString());

        return action;
    }

    static HttpHeaderAction removeHttpHeader(String header)
    {
        var action = new HttpHeaderAction();
        action.setLuaCode(new StringBuilder().append(action.getLuaHandle())
                                             .append("headers():remove(")
                                             .append("\"")
                                             .append(header)
                                             .append("\"")
                                             .append(")")
                                             .toString());

        return action;
    }

    static HttpHeaderAction fromHttpHeader(String header)
    {
        return getHttpHeader(header);
    }

    static HttpHeaderAction getHttpHeader(String header)
    {
        var action = new HttpHeaderAction();
        action.setLuaCode(new StringBuilder().append(action.getLuaHandle())
                                             .append("headers():get(")
                                             .append("\"")
                                             .append(header)
                                             .append("\"")
                                             .append(")")
                                             .toString());
        return action;
    }

}
