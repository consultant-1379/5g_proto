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
 * Created on: Nov 8, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.metrics;

/**
 * Custom unchecked exception for Metrics Exporter related exceptions.
 */
public class MetricsException extends RuntimeException
{

    private static final long serialVersionUID = -5575051804366442008L;

    public MetricsException(Throwable cause)
    {
        super(cause);
    }

    public MetricsException(String message,
                            Throwable cause)
    {
        super(message, cause);
    }
}
