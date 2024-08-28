/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 15, 2022
 *     Author: eaoknkr
 */

package com.ericsson.utilities.common;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EventTest
{
    private static final Logger log = LoggerFactory.getLogger(EventTest.class);

    @Test
    void test()
    {
        Event.Sequence events = new Event.Sequence("Test");

        Event.prettyPrintingEnable();

        for (int i = 0; i < 100; ++i)
            events.put(new Event("Trigger", "java.util.Long", String.valueOf(i)));

        log.info("events=" + events.toString());
    }
}
