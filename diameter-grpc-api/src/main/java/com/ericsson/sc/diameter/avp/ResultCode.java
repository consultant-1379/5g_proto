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

public enum ResultCode
{

    // Protocol Errors

    /**
     * This error is given when Diameter cannot deliver the message to the
     * destination, either because no host within the realm supporting the required
     * application was available to process the request or because the
     * Destination-Host AVP was given without the associated Destination-Realm AVP.
     */
    DIAMETER_UNABLE_TO_DELIVER(3002),

    /**
     * When returned, a Diameter node SHOULD attempt to send the message to an
     * alternate peer. This error MUST only be used when a specific grpcServer is
     * requested, and it cannot provide the requested service.
     */
    DIAMETER_TOO_BUSY(3004),

    /*
     * Permanent Failures:
     */

    /**
     * The request contained an AVP with an invalid value in its data portion. A
     * Diameter message indicating this error MUST include the offending AVPs within
     * a Failed-AVP AVP.
     */
    DIAMETER_INVALID_AVP_VALUE(5004),

    /**
     * The request did not contain an AVP that is required by the Command Code
     * definition. If this value is sent in the Result-Code AVP, a Failed-AVP AVP
     * SHOULD be included in the message. The Failed-AVP AVP MUST contain an example
     * of the missing AVP complete with the Vendor-Id if applicable. The value field
     * of the missing AVP should be of correct minimum length and contain zeroes.
     */
    DIAMETER_MISSING_AVP(5005),

    /**
     * A message was received that included an AVP that appeared more often than
     * permitted in the message definition. The Failed-AVP AVP MUST be included and
     * contain a copy of the first instance of the offending AVP that exceeded the
     * maximum number of occurrences.
     */
    DIAMETER_AVP_OCCURS_TOO_MANY_TIMES(5009),

    /**
     * This error is returned when a request is rejected for unspecified reasons.
     */
    UNABLE_TO_COMPLY(5012);

    int code;

    ResultCode(int code)
    {
        this.code = code;
    }

    public int getCode()
    {
        return code;
    }
}
