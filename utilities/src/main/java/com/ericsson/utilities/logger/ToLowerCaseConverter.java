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
 * Created on: Nov 15, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;

public class ToLowerCaseConverter extends CompositeConverter<ILoggingEvent>
{
    @Override
    public String transform(final ILoggingEvent event,
                            String in)
    {
        return in.toLowerCase();
    }
}
