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
 * Created on: Apr 14, 2021
 *     Author: eedstl
 */

package com.ericsson.cnal.common;

import org.openapitools.jackson.nullable.JsonNullableModule;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class OpenApiObjectMapper
{
    private static final ObjectMapper singleton = com.ericsson.utilities.json.Jackson.newOm().registerModule(new JsonNullableModule())
//                                                                                     .setSerializationInclusion(Include.NON_EMPTY)
//                                                                                     .setSerializationInclusion(Include.NON_DEFAULT)
                                                                                     .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                                                                     .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
                                                                                     .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                                                                                     .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                                                                                     .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

    /**
     * Create the Jackson {@link ObjectMapper} shared instance that is compliant to
     * the OpenAPI.
     * 
     * @return The shared Jackson {@link ObjectMapper} instance.
     */
    public static ObjectMapper singleton()
    {
        return singleton;
    }

    /**
     * Prevent instantiation.
     */
    private OpenApiObjectMapper()
    {
    }
}
