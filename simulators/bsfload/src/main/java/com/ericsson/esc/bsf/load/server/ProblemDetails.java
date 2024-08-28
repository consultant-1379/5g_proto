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
 * Created on: Nov 19, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.server;

import java.util.List;

/**
 * Refers to request problem details.
 */
public class ProblemDetails
{
    private final String title;
    private final String detail;
    private final String cause;
    private final List<InvalidParameter> invalidParams;

    private ProblemDetails(String title,
                           String detail,
                           String cause,
                           List<InvalidParameter> invalidParams)
    {
        this.title = title;
        this.detail = detail;
        this.cause = cause;
        this.invalidParams = invalidParams;
    }

    public static ProblemDetails withDetail(String title,
                                            String detail)
    {
        return new ProblemDetails(title, detail, null, null);
    }

    public static ProblemDetails withCause(String title,
                                           String detail,
                                           String cause)
    {
        return new ProblemDetails(title, detail, cause, null);
    }

    public static ProblemDetails withInvalidParam(String title,
                                                  String detail,
                                                  List<InvalidParameter> invalidParams)
    {
        return new ProblemDetails(title, detail, null, invalidParams);
    }

    /**
     * @return the title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * @return the detail
     */
    public String getDetail()
    {
        return detail;
    }

    /**
     * @return the cause
     */
    public String getCause()
    {
        return cause;
    }

    /**
     * @return the invalidParams
     */
    public List<InvalidParameter> getInvalidParams()
    {
        return invalidParams;
    }

    @Override
    public String toString()
    {
        return "ProblemDetails [title=" + title + ", detail=" + detail + ", cause=" + cause + ", invalidParams=" + invalidParams + "]";
    }
}
