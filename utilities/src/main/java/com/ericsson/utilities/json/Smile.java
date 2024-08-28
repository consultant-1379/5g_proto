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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.smile.SmileGenerator.Feature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 * JSON Smile CODEC
 */
public class Smile
{
    private static final ObjectMapper SMILE_INSTANCE = new ObjectMapper(new SmileFactory().enable(Feature.CHECK_SHARED_STRING_VALUES)).registerModule(new ParameterNamesModule())
                                                                                                                                      .registerModule(new Jdk8Module())
                                                                                                                                      .registerModule(new JavaTimeModule());

    private Smile()
    {
    }

    /**
     * @return A Java 8 Jackson ObjectMapper configured with Smile CODEC
     */
    public static ObjectMapper om()
    {
        return SMILE_INSTANCE;
    }
}
