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
 * Created on: Mar 12, 2020
 *     Author: eedstl
 */

package com.ericsson.sim.chf.r17;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.adpal.pm.PmAdapter.Query;
import com.ericsson.adpal.pm.PmAdapter.Query.Response.Data.Result;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IfCountProvider;
import com.ericsson.utilities.exceptions.Utils;

import io.reactivex.Completable;
import io.reactivex.disposables.Disposable;
import io.vertx.reactivex.core.Vertx;

/**
 * Periodically reads the load counters of CHFSIM from the PM server.
 */
public class LoadMeter implements IfCountProvider
{
    private static final Logger log = LoggerFactory.getLogger(LoadMeter.class);

    private final PmAdapter.Inquisitor inquisitor;

    private List<Disposable> disposables;
    private Double totalLoad;

    public LoadMeter(final Vertx vertx)
    {
        this.disposables = new ArrayList<>();

        // In regular intervals, fetch from Prometheus:
        // 1. The average load of the CHF simulator
        final Query.Element cpuUsageSecsAvg = Query.sum(Query.rate(Query.metric("container_cpu_usage_seconds_total")
                                                                        .param("namespace", EnvVars.get("NAMESPACE"))
                                                                        .param("container", "eric-chfsim"),
                                                                   60))
                                                   .by("pod");
        final Query.Element cpuQuotaByPeriod = Query.sum(Query.div(Query.metric("container_spec_cpu_quota")
                                                                        .param("namespace", EnvVars.get("NAMESPACE"))
                                                                        .param("container", "eric-chfsim"),
                                                                   Query.metric("container_spec_cpu_period")
                                                                        .param("namespace", EnvVars.get("NAMESPACE"))
                                                                        .param("container", "eric-chfsim")))
                                                    .by("pod");

        this.inquisitor = new PmAdapter.Inquisitor(new PmAdapter(vertx,
                                                                 Integer.valueOf(EnvVars.get("ERIC_PM_SERVER_SERVICE_PORT")),
                                                                 EnvVars.get("ERIC_PM_SERVER_SERVICE_HOST")),
                                                   Query.mul(Query.value(100), Query.avg(Query.div(cpuUsageSecsAvg, cpuQuotaByPeriod))));
        this.totalLoad = 0.0;
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
            this.disposables.add(this.inquisitor.getData() //
                                                .doOnNext(this::setCount) //
                                                .subscribe(d ->
                                                {
                                                }, t -> log.error("Error retrieving load data. Cause: {}", Utils.toString(t, log.isDebugEnabled()))));
        }).mergeWith(this.inquisitor.start());
    }

    public Completable stop()
    {
        return Completable.fromAction(() -> this.disposables.forEach(d -> d.dispose())).mergeWith(this.inquisitor.stop());
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
    }
}
