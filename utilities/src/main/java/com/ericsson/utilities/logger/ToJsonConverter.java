/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 12, 2018
 *     Author: eedstl
 */

package com.ericsson.utilities.logger;

import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

public class ToJsonConverter extends CompositeConverter<ILoggingEvent>
{
    private static final ObjectMapper json = Jackson.om();

    @Override
    public String transform(final ILoggingEvent event,
                            final String in)
    {
        try
        {
            return json.writeValueAsString(in);
        }
        catch (JsonProcessingException e)
        {
            return "\"message\":\"" + e.getMessage().replace("\"", "\\\"") + "\"";
        }
    }
}