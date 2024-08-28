/**
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Aug 4, 2022
 *     Author: echfari
 */
package com.ericsson.sc.tapcol;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.tapcol.pcap.ConnectionTransformer;
import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;
import com.google.common.net.InetAddresses;

import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.CoreV1EndpointPort;
import io.kubernetes.client.openapi.models.V1LoadBalancerStatus;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.ClientBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.EmitFailureHandler;
import reactor.core.scheduler.Schedulers;

/**
 * Polls the k8s API server for specially tagged services. Selected services are
 * used to create connection transformers
 *
 */
public class TapcollectorServiceWatcher
{
    private static final Logger log = LoggerFactory.getLogger(TapcollectorServiceWatcher.class);

    private enum Lbl
    {
        PROCESS_SERVICE
    }

    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log);
    private final String namespace;
    private final CoreV1Api api;
    private final Flux<ConnectionTransformer> ctFlux;
    private final String svcLabelSelector;
    private final Sinks.One<Void> stopMono = Sinks.<Void>one();

    /**
     * 
     * @param namespace        The k8s namespace to use
     * @param svcLabelSelector The label selector to use
     * @param pollingPeriod    The polling period
     */
    public TapcollectorServiceWatcher(String namespace,
                                      String svcLabelSelector,
                                      Duration pollingPeriod)
    {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(svcLabelSelector);
        Objects.requireNonNull(pollingPeriod);

        this.svcLabelSelector = svcLabelSelector;
        this.namespace = namespace;
        try
        {
            final var client = ClientBuilder.standard().build();
            Configuration.setDefaultApiClient(client);
        }
        catch (IOException ioex)
        {
            throw new UncheckedIOException(ioex);
        }
        this.api = new CoreV1Api();
        final var ctConnectable = Flux.interval(pollingPeriod, Schedulers.boundedElastic())
                                      .takeUntilOther(stopMono.asMono())
                                      .onBackpressureDrop(tick -> log.error("Skipped polling, previous not finished yet"))
                                      .map(tick -> processServices())
                                      .doOnSubscribe(s -> log.info("Started k8s polling, period: {} , service label selector: {}",
                                                                   pollingPeriod,
                                                                   svcLabelSelector))
                                      .distinctUntilChanged()
                                      .doOnNext(next -> log.info("Built new connection transformer: {}", next))
                                      .replay(1);

        this.ctFlux = ctConnectable;
        ctConnectable.connect();
    }

    /**
     * 
     * @return A Flux of Connection Transformers
     */
    public Flux<ConnectionTransformer> transFlux()
    {
        return this.ctFlux;
    }

    /**
     * 
     * @return A Mono that completes as soon as the service processes all
     *         interesting services and has built at least one ConnectionTransformer
     */
    public Mono<Void> onStarted()
    {
        return this.ctFlux.take(1).then();
    }

    /**
     * Stop polling and perform cleanup
     */
    public synchronized void dispose()
    {
        this.stopMono.emitEmpty(EmitFailureHandler.FAIL_FAST);
    }

    private ConnectionTransformer processServices()
    {
        final var builder = ConnectionTransformer.builder();
        try
        {
            final var svcList = api.listNamespacedService(namespace, null, null, null, null, svcLabelSelector, null, null, null, null, null);
            final var svcListNames = svcList.getItems().stream().map(svc -> svc.getMetadata().getName()).collect(Collectors.toUnmodifiableList());
            log.debug("Examining services to extract own socket address replacement configuration, labelSelector: {} servicdes: {}",
                      svcLabelSelector,
                      svcListNames);

            svcList.getItems().forEach(svc -> processService(svc, builder));
        }
        catch (Exception e)
        {
            log.error("Failed to list Kubernetes services for label selector {}", svcLabelSelector, e);
        }
        return builder.build();
    }

    private void processService(V1Service svc,
                                ConnectionTransformer.Builder ct)
    {
        try
        {

            final var endpoints = api.listNamespacedEndpoints(namespace, null, null, null, null, svcLabelSelector, null, null, null, null, null).getItems();

            final var endpoint = endpoints.size() > 1 ? endpoints.stream()
                                                                 .filter(ep -> ep.getMetadata().getName().equals(svc.getMetadata().getName()))
                                                                 .findFirst()
                                                                 .get()
                                                      : endpoints.get(0);

            final var encPort = endpoint.getSubsets()
                                        .get(0)
                                        .getPorts()
                                        .stream()
                                        .filter(port -> "encrypted-port".equals(port.getName()))
                                        .map(CoreV1EndpointPort::getPort)
                                        .toList()
                                        .get(0);

            final var unencPort = endpoint.getSubsets()
                                          .get(0)
                                          .getPorts()
                                          .stream()
                                          .filter(port -> "unencrypted-port".equals(port.getName()))
                                          .map(CoreV1EndpointPort::getPort)
                                          .toList()
                                          .get(0);

            final var lbIngressIp = Optional.ofNullable(svc.getStatus().getLoadBalancer())
                                            .map(V1LoadBalancerStatus::getIngress)
                                            .filter(ingressList -> !ingressList.isEmpty())
                                            .map(ingressList -> ingressList.stream()//
                                                                           .filter(ingr -> ingr.getIp() != null)
                                                                           .map(ingrWithIP -> InetAddresses.forString(ingrWithIP.getIp()))
                                                                           .toList());

            log.debug("Processing k8s service {}.{} with IP: {} and endpoint: {}",
                      svc.getMetadata().getNamespace(),
                      svc.getMetadata().getName(),
                      lbIngressIp.get(),
                      endpoint.getMetadata().getName());

            lbIngressIp.ifPresent(ips ->
            {
                ips.forEach(ip ->
                {
                    log.debug("Mapping ip: {}", ip);
                    for (var port : svc.getSpec().getPorts())
                    {
                        final var targetPort = "unencrypted-port".equals(port.getName()) ? unencPort : encPort;
                        ct.withMapping(targetPort, ip, port.getPort());
                    }
                });
            });

        }
        catch (Exception e)
        {
            safeLog.log(Lbl.PROCESS_SERVICE, l -> l.warn("Ignored service {} due to unexpected error", svc.getMetadata().getName()));
            log.debug("Error: ", e);
        }
    }

}
