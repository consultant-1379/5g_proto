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
 * Created on: Jul 13, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.server;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.esc.bsf.load.metrics.MetricsHandler;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.reactivex.RxShutdownHook;
import com.ericsson.utilities.reactivex.VertxBuilder;

import io.reactivex.Completable;
import io.reactivex.functions.Predicate;
import io.vertx.core.VertxOptions;
import io.vertx.reactivex.core.Vertx;

/**
 * BSF HTTP request traffic generator.
 */
public class BsfLoad
{
    private static final Logger log = LoggerFactory.getLogger(BsfLoad.class);

    private final BsfLoadService service;
    private final BsfLoadServiceHandler handler;
    private final MetricsHandler metricsHandler;
    private final RxShutdownHook shutdownHook;
    private final Vertx vertx;
    private final WebServer webServer;

    public BsfLoad(BsfLoadParameters params,
                   RxShutdownHook shutdownHook) throws SocketException
    {
        final var hostAddress = decideListeningIP();
        this.metricsHandler = new MetricsHandler(hostAddress, params);
        final var vertxOptions = new VertxOptions().setMetricsOptions(this.metricsHandler.getMetricsOptions());
        this.vertx = VertxBuilder.newInstance().setOptions(vertxOptions).modifyRxSchedulers(false).build();

        this.metricsHandler.initServer(vertx);
        this.webServer = WebServer.builder() //
                                  .withHost(hostAddress)
                                  .withPort(params.getBsfLoadPort())
                                  .build(this.vertx);

        this.service = new BsfLoadService(metricsHandler, vertx);
        this.handler = new BsfLoadServiceHandler(service, webServer);
        this.shutdownHook = shutdownHook;
    }

    public Completable run()
    {
        return this.webServer.startListener() //
                             .andThen(this.metricsHandler.start())
                             .andThen(this.handler.start())
                             .andThen(this.shutdownHook.get())
                             .andThen(this.stop())
                             .onErrorResumeNext(throwable -> stop().andThen(Completable.error(throwable)));
    }

    public Completable stop()
    {
        final Predicate<? super Throwable> logErr = t ->
        {
            log.warn("Ignored Exception during shutdown", t);
            return true;
        };

        return Completable.fromAction(() -> log.info("Graceful shutdown..."))
                          .andThen(this.metricsHandler.stop().onErrorComplete(logErr))
                          .andThen(this.handler.stop().onErrorComplete(logErr))
                          .andThen(this.webServer.stopListener().onErrorComplete(logErr));
    }

    private String decideListeningIP() throws SocketException
    {
        List<InetAddress> address = new ArrayList<>();

        NetworkInterface.getNetworkInterfaces().asIterator().forEachRemaining(i -> i.getInetAddresses().asIterator().forEachRemaining(address::add));

        var numOfIpv6 = address.stream().filter(Inet6Address.class::isInstance).findAny().orElse(null);

        return numOfIpv6 != null ? "::" : "0.0.0.0";
    }

    public static void main(String[] args)
    {
        final var params = BsfLoadParameters.fromEnvironment();
        var exitStatus = 1;

        try (var shutdownHook = new RxShutdownHook())
        {
            final var bsfLoad = new BsfLoad(params, shutdownHook);
            bsfLoad.run().blockingAwait();
            exitStatus = 0;
        }
        catch (Exception e)
        {
            log.error("BsfLoad terminated abnormally due to exception", e);
            exitStatus = 1;
        }

        System.exit(exitStatus);
    }
}
