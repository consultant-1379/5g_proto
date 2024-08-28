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
 * Created on: Jun 18, 2020
 *     Author: eedstl
 */

package com.ericsson.sc.scp.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.adpal.pm.PmAdapter.Query;
import com.ericsson.adpal.pm.PmAdapter.Query.Response.Data.Result;
import com.ericsson.sc.scp.model.EricssonScp;
import com.ericsson.sc.scp.model.EricssonScpScpFunction;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IfCountProvider;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.metrics.MetricRegister;

import io.prometheus.client.Gauge;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.vertx.reactivex.core.Vertx;

/**
 * Periodically reads the load counters from the PM server, processes them and
 * stores the result back in PM.
 */
public class LoadMeter implements IfCountProvider
{
    private static final Logger log = LoggerFactory.getLogger(LoadMeter.class);

    private static final Gauge gcScpLoad = MetricRegister.singleton()
                                                         .register(Gauge.build()
                                                                        .namespace("scp")
                                                                        .name("load")
                                                                        .labelNames("nf", "nf_instance")
                                                                        .help("The SCP load [%].")
                                                                        .register());

    private final PmAdapter.Inquisitor inquisitor;

    private Flowable<Optional<EricssonScp>> config;
    private List<Disposable> disposables;
    private AtomicReference<String[]> labelValues;
    private Double totalLoad;

    public LoadMeter(final Vertx vertx,
                     final Observable<Optional<EricssonScp>> config)
    {
        this.disposables = new ArrayList<>();
        this.labelValues = new AtomicReference<>();

        // In regular intervals, fetch from Prometheus the average load of SCP.

        final Query.Element cpuUsage100Avg = Query.avg(Query.metric("job:container_cpu_usage_100").param("container", "eric-scp-worker"));

        // TODO: Move all env parameter parsing in one place, avoid reading parameters
        // k8s env parameters and in case of common function use input parameters (is
        // this tls port?)
        this.inquisitor = new PmAdapter.Inquisitor(new PmAdapter(vertx,
                                                                 Integer.parseInt(EnvVars.get("ERIC_PM_SERVER_SERVICE_PORT")),
                                                                 EnvVars.get("ERIC_PM_SERVER_SERVICE_HOST")),
                                                   cpuUsage100Avg);
        this.totalLoad = 0.0;

        // Subscribe to configuration updates so that we know the nfInstance's RDN.

        this.config = config.toFlowable(BackpressureStrategy.LATEST)//
                            .doOnSubscribe(d -> log.debug("Start observing configuration changes."))
                            .doOnNext(o ->
                            {
                                log.debug("Applying configuration changes.");

                                o.ifPresentOrElse(c ->
                                {
                                    String[] labelValues = null;

                                    final EricssonScpScpFunction scpFunction = c.getEricssonScpScpFunction();

                                    if (scpFunction != null && !scpFunction.getNfInstance().isEmpty())
                                    {
                                        final NfInstance nfInstance = scpFunction.getNfInstance().get(0); // TODO: remove this limitation

                                        if (nfInstance != null)
                                        {
                                            labelValues = MetricRegister.rdnToLabelValues(new Rdn("nf", "scp-function").add("nf-instance",
                                                                                                                            nfInstance.getName()))
                                                                        .toArray(new String[0]);
                                        }
                                    }

                                    this.labelValues.set(labelValues);
                                }, () -> this.labelValues.set(null));
                            });
    }

    @Override
    public synchronized Double getCount()
    {
        return this.totalLoad;
    }

    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            this.disposables.add(this.config.subscribe(c ->
            {
            }, t -> log.error("Error retrieving configuration data. Cause: {}", Utils.toString(t, log.isDebugEnabled()))));

            this.disposables.add(this.inquisitor.getData() //
                                                .doOnNext(this::setCount) //
                                                .subscribe(d ->
                                                {
                                                }, t -> log.error("Error retrieving load data. Cause: {}", Utils.toString(t, log.isDebugEnabled()))));
        }).mergeWith(this.inquisitor.start());
    }

    public Completable stop()
    {
        return Completable.fromAction(() -> this.disposables.forEach(Disposable::dispose)).mergeWith(this.inquisitor.stop());
    }

    private synchronized void setCount(List<PmAdapter.Query.Response.Data> data)
    {
        if (data == null)
            return;

        double loadCpuMax = 0.0;

        if (!data.isEmpty())
        {
            final List<Result> result = data.get(0).getResult();

            if (!result.isEmpty())
                loadCpuMax = result.get(0).getValue().get(1);
        }

        loadCpuMax = Math.min(Math.max(0, loadCpuMax), 100); // Make sure that 0 <= l <= 100

        this.totalLoad = loadCpuMax;

        final String[] labelValues = this.labelValues.get();

        if (labelValues != null)
        {
            log.debug("totalLoad={}", this.totalLoad);
            gcScpLoad.labels(labelValues).set(this.totalLoad);
        }
    }
}
