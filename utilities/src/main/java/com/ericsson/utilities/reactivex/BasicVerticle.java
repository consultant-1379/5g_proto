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
 * Created on: 25 Jul 2019
 *     Author: enubars
 */

package com.ericsson.utilities.reactivex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
import io.vertx.core.AbstractVerticle;

/**
 * 
 */
public class BasicVerticle extends AbstractVerticle
{
    private static final Logger log = LoggerFactory.getLogger(VertxInstance.class);

    @Override
    public void start() throws Exception
    {
        log.info("Vertx worker started");
    }

    @Override
    public void stop() throws Exception
    {
        log.info("Vertx worke stopped");
    }
}
