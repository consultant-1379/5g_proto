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
 * Created on: Jul 1, 2021
 *     Author: eedstl
 */

package com.ericsson.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;

public class SimpleAuthProvider implements AuthenticationProvider
{
    private static final Logger log = LoggerFactory.getLogger(SimpleAuthProvider.class);
    private final JsonObject userCredentials;

    protected SimpleAuthProvider(JsonObject userCredentials)
    {
        this.userCredentials = userCredentials;
        log.info("Monitor username: {}", userCredentials.getString("username"));
        log.info("Monitor password: {}", userCredentials.getString("password"));
    }

    public void authenticate(Handler<AsyncResult<User>> resultHandler)
    {
        authenticate(this.userCredentials, resultHandler);
    }

    public void authenticate(JsonObject authInfo,
                             Handler<AsyncResult<User>> resultHandler)
    {
        log.info("Checking username: {}", authInfo.getString("username"));
        log.info("Checking password: {}", authInfo.getString("password"));

        if (authInfo.getString("username").equals(this.userCredentials.getValue("username"))
            && authInfo.getString("password").equals(this.userCredentials.getValue("password")))
        {
            log.info("Username {} authenticated successfully.", authInfo.getString("username"));
            resultHandler.handle(Future.succeededFuture(User.create(authInfo)));
        }
        else
        {
            log.debug("Username {} not authorized to access webserver.", authInfo.getString("username"));
            log.error("User authentication failed.");
            resultHandler.handle(Future.failedFuture("Unauthorized(401) User"));
        }

        log.info("Authentication procedure complete.");
    }
}
