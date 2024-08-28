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
 * Created on: May 7, 2021
 *     Author: eedstl
 */

package com.ericsson.utilities.http;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * Implementation of Kubernetes liveness- and readiness-probing. This must be
 * accompanied by the following entries for livenessProbe and readinessProbe in
 * the Helm chart of the service:
 * 
 * <pre>
 * spec:
 *  containers:
 *  - name: {{ .Values.service.name }}
 *    image: {{ .Values.spec.image }}:{{ .Values.spec.tag }}
 *    ports:
 *    - containerPort: 80
 *    livenessProbe:
 *      httpGet:
 *        path: /probes/healthz         <-- predefined
 *        port: 8081                    <-- at your choice
 *        httpHeaders:
 *        - name: X-Health-Check        <-- predefined
 *          value: livenessProbe        <-- predefined
 *      initialDelaySeconds: 10         <-- at your choice
 *      periodSeconds: 10               <-- (default) at your choice
 *      timeoutSeconds: 1               <-- (default) at your choice
 *      successThreshold: 1             <-- (default, must be 1 for liveness)
 *      failureThreshold: 3             <-- (default) at your choice
 *    readinessProbe:
 *      httpGet:
 *        path: /probes/healthz         <-- predefined
 *        port: 8081                    <-- at your choice
 *        httpHeaders:
 *        - name: X-Health-Check        <-- predefined
 *          value: readinessProbe       <-- predefined
 *      initialDelaySeconds: 10         <-- at your choice
 *      periodSeconds: 10               <-- (default) at your choice
 *      timeoutSeconds: 1               <-- (default) at your choice
 *      successThreshold: 1             <-- (default) at your choice
 *      failureThreshold: 3             <-- (default) at your choice
 * </pre>
 * 
 * Example usage:
 * 
 * <pre>
 * final KubeProbe kubeProbe;
 * 
 * Constructor()
 * {
 *     this.kubeProbe = KubeProbe.Handler.singleton().configure(this.k8sProbeIfWebServer).register(KubeProbe.of().setAlive(true).setReady(false));
 * }
 * 
 * Method()
 * {
 *     this.kubeProbe.setAlive(!isUnrecoverableError).setReady(!requiredResourceIsDown);
 * 
 *     if (thisObjectWillNotBeUsedAnyMore)
 *         KubeProbe.Handler.singleton().deregister(this.kubeProbe);
 * }
 * </pre>
 */
public class KubeProbe
{
    private static final Logger log = LoggerFactory.getLogger(KubeProbe.class);

    public static class Handler
    {
        private static final Handler singleton = new Handler();

        public static Handler singleton()
        {
            return singleton;
        }

        private final Set<KubeProbe> probes;

        private Handler()
        {
            this.probes = ConcurrentHashMap.<KubeProbe>newKeySet();
        }

        /**
         * Configure the server routes for the Kubernetes probing for this probe
         * handler. As the probe handler is a singleton, this method must be called only
         * once after the server has been created.
         * 
         * @param server The server to be configured for reception of Kubernetes probes.
         * @return This probe handler.
         */
        public Handler configure(final RouterHandler server)
        {
            server.configureRouter(router -> router.get("/probes/liveness").handler(this::checkLiveness));
            server.configureRouter(router -> router.get("/probes/readiness").handler(this::checkReadiness));

            return this;
        }

        /**
         * Deregisters the probe passed from this probe handler. The probe must have
         * been registered by calling {@link #register(KubeProbe)} before.
         * 
         * @param probe The probe to be deregistered.
         * @return The probe passed as parameter.
         */
        public KubeProbe deregister(final KubeProbe probe)
        {
            this.probes.remove(probe);
            return probe;
        }

        /**
         * Registers the probe passed with this probe handler. Users should not forget
         * to call {@link #deregister(KubeProbe)} when it is not used anymore.
         * 
         * @param probe The probe to be registered.
         * @return The probe passed as parameter.
         */
        public KubeProbe register(final KubeProbe probe)
        {
            this.probes.add(probe);
            return probe;
        }

        /**
         * Check the liveness and return the result. Liveness check fails on
         * unrecoverable (persistent) errors.
         * <p>
         * Liveness check yields a positive result if all registered probes are alive.
         * 
         * @return The result of the liveness check.
         */
        private void checkLiveness(final RoutingContext routingContext)
        {
            HttpResult result = this.probes.stream().allMatch(KubeProbe::isAlive) ? new HttpResult(HttpResponseStatus.OK)
                                                                                  : new HttpResult(HttpResponseStatus.INTERNAL_SERVER_ERROR);

            if (result.status().code() < 200 || result.status().code() > 299)
                log.warn("Checking liveness, sending result '{}'.", result);

            routingContext.response().setStatusCode(result.status().code()).setStatusMessage(result.cause()).end();
        }

        /**
         * Check the readiness and return the result. Readiness check fails on
         * recoverable (temporary) errors.
         * <p>
         * Readiness check yields a positive result if all registered probes are ready.
         * 
         * @return The result of the readiness check.
         */
        private void checkReadiness(final RoutingContext routingContext)
        {
            HttpResult result = this.probes.stream().allMatch(KubeProbe::isReady) ? new HttpResult(HttpResponseStatus.OK)
                                                                                  : new HttpResult(HttpResponseStatus.INTERNAL_SERVER_ERROR);

            if (result.status().code() < 200 || result.status().code() > 299)
                log.debug("Checking readiness, sending result '{}'.", result);

            routingContext.response().setStatusCode(result.status().code()).setStatusMessage(result.cause()).end();
        }
    }

    /**
     * @return A new probe which is set to not alive and not ready by default.
     */
    public static KubeProbe of()
    {
        return new KubeProbe();
    }

    private final AtomicBoolean isAlive;
    private final AtomicBoolean isReady;

    private KubeProbe()
    {
        this.isAlive = new AtomicBoolean(false);
        this.isReady = new AtomicBoolean(false);
    }

    public boolean isAlive()
    {
        return this.isAlive.get();
    }

    public boolean isReady()
    {
        return this.isReady.get();
    }

    public KubeProbe setAlive(final boolean isAlive)
    {
        this.isAlive.set(isAlive);
        return this;
    }

    public KubeProbe setReady(final boolean isReady)
    {
        this.isReady.set(isReady);
        return this;
    }
}
