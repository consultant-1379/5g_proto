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
 * Created on: Mar 27, 2024
 *     Author: Avengers
 */

package com.ericsson.sc.certmcrhandler.k8s.resource;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum GeneratedSecretTypeEnum
{
    @JsonProperty("tls")
    TLS,
    @JsonProperty("opaque")
    OPAQUE;
}
