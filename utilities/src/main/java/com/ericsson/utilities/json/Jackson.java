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
package com.ericsson.utilities.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * Jackson Utilities
 */
public class Jackson
{
    private static final JsonMapper OM = newOm();

    /**
     * prevent instantiation
     */
    private Jackson()
    {
    }

    /**
     * Create a new Jackson {@link ObjectMapper} instance, configured with java8
     * modules and {@link JsonInclude.Include#NON_NULL} serialization inclusion.
     * 
     * @return The newly created {@link ObjectMapper}
     */
    public static JsonMapper newOm()
    {
        return JsonMapper.builder()
                         .addModules(new ParameterNamesModule(), //
                                     new Jdk8Module(), //
                                     new JavaTimeModule(), //
                                     new JodaModule()) //
                         .serializationInclusion(JsonInclude.Include.NON_NULL)
                         // DND-29137: Deserialization of huge JSON numbers as BigIntegers might take too
                         // much time.
                         // To protect against DoS attacks, numbers are deserialized as longs by default.
                         .configure(DeserializationFeature.USE_LONG_FOR_INTS, true)
                         .build();
    }

    /**
     * Get the Jackson {@link ObjectMapper} shared instance.
     * <p>
     * The instance is configured according to {@link #newOm()}. The instance is
     * thread-safe if used for typical Jackson serialization/deserialization
     * operations.
     * <p>
     * Warning: Since the instance is global it should NEVER be reconfigured. If
     * different configuration is needed use {@link #newOm()}
     * 
     * @return The shared Jackson {@link ObjectMapper} instance
     */
    public static JsonMapper om()
    {
        return OM;
    }
}
