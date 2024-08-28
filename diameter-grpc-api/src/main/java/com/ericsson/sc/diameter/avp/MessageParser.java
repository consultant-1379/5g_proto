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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.gs.tm.diameter.service.grpc.DiameterMessage;

public class MessageParser
{
    private static final Logger log = LoggerFactory.getLogger(MessageParser.class);
    private final Map<AvpId, AvpDef<?>> interestingAvps;

    protected MessageParser(Map<AvpId, AvpDef<?>> interestingAvps)
    {
        this.interestingAvps = interestingAvps;
    }

    public ParsedDiameterMessage parse(DiameterMessage dm)
    {
        return new ParsedDiameterMessage(dm, interestingAvps);
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class Builder
    {
        private final Map<AvpId, AvpDef<?>> interestingAvps = new HashMap<>();

        protected Builder()
        {
        }

        public Builder addAvpDefs(Iterable<AvpDef<?>> defs)
        {
            for (var def : defs)
            {
                log.debug("Adding AVP definition {}", def);
                this.interestingAvps.put(def.getId(), def);
            }
            return this;
        }

        public MessageParser build()
        {
            return new MessageParser(this.interestingAvps);
        }
    }
}
