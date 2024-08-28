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
 * Created on: Oct 7, 2020
 *     Author: echfari
 */
package com.ericsson.utilities.http;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;

public interface WebServerRouter
{
    public void mountRouter(String mountPoint,
                            Router router);

    void configureRouter(Consumer<Router> consumer);

    URI baseUri();

    List<WebServerRouter> childRouters();

    Vertx getVertx();
}
