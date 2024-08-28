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
 * Created on: Mar 18, 2020
 *     Author: ekonpap
 */

package com.ericsson.esc.bsf.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.adpal.pm.PmAdapter.Query;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.IfCountProvider;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.metrics.MetricRegister;

import io.prometheus.client.Gauge;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.BehaviorSubject;
import io.vertx.reactivex.core.Vertx;

public class LoadMeter implements IfCountProvider
{
    private static final String ENV_NAMESPACE = "NAMESPACE";
    private static final String ENV_PM_SERVER_SERVICE_HOST = "ERIC_PM_SERVER_SERVICE_HOST";
    private static final String ENV_PM_SERVER_SERVICE_PORT = "ERIC_PM_SERVER_SERVICE_PORT";
    private static final boolean BSF_DIAMETER_ENABLED = Boolean.parseBoolean(EnvVars.get("BSF_DIAMETER_ENABLED", false));
    private static final String WCDBCD_POD_TMPL = EnvVars.get("CASSANDRA_CONTACT_POINT", "eric-bsf-wcdb-cd-datacenter1-rack1:9042").split(":")[0].concat("-.*");

    private static final Logger log = LoggerFactory.getLogger(LoadMeter.class);

    /// BSF load - total max
    private static final Gauge gcBsfLoad = MetricRegister.singleton()
                                                         .register(Gauge.build()
                                                                        .namespace("bsf")
                                                                        .name("load")
                                                                        .labelNames("nf", "nf_instance")
                                                                        .help("The BSF load [%].")
                                                                        .register());

    // and a list with the rest
    private static final List<Gauge> gcLoads = generateGcLoads(BSF_DIAMETER_ENABLED);

    private final PmAdapter.Inquisitor inquisitor;
    private Flowable<Optional<EricssonBsf>> config;
    private List<Disposable> disposables;
    private AtomicReference<String[]> labelValues;
    private Double totalLoad;

    public LoadMeter(final Vertx vertx,
                     final BehaviorSubject<Optional<EricssonBsf>> config)
    {
        this.disposables = new ArrayList<>();
        this.labelValues = new AtomicReference<>();

        // In regular intervals, fetch from Prometheus:
        // the average load of BSF workers

        // 1. worker
        final Query.Element cpuUsageSecsAvgWorker = Query.sum(Query.rate(Query.metric("container_cpu_usage_seconds_total")
                                                                              .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                              .param("container", "eric-bsf-worker"),
                                                                         60))
                                                         .by("pod");

        final Query.Element cpuQuotaByPeriodWorker = Query.sum(Query.div(Query.metric("container_spec_cpu_quota")
                                                                              .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                              .param("container", "eric-bsf-worker"),
                                                                         Query.metric("container_spec_cpu_period")
                                                                              .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                              .param("container", "eric-bsf-worker")))
                                                          .by("pod");

        final Query.Element workerQuery = Query.mul(Query.value(100), Query.avg(Query.div(cpuUsageSecsAvgWorker, cpuQuotaByPeriodWorker)));

        // 2. cassandra
        final Query.Element cpuUsageSecsAvgCassandra = Query.sum(Query.rate(Query.metric("container_cpu_usage_seconds_total")
                                                                                 .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                 .param("container", "cassandra")
                                                                                 .param("pod", WCDBCD_POD_TMPL, true),
                                                                            60))
                                                            .by("pod");

        final Query.Element cpuQuotaByPeriodCassandra = Query.sum(Query.div(Query.metric("container_spec_cpu_quota")
                                                                                 .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                 .param("container", "cassandra")
                                                                                 .param("pod", WCDBCD_POD_TMPL, true),
                                                                            Query.metric("container_spec_cpu_period")
                                                                                 .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                 .param("container", "cassandra")
                                                                                 .param("pod", WCDBCD_POD_TMPL, true)))
                                                             .by("pod");

        final Query.Element cassandraQuery = Query.mul(Query.value(100), Query.avg(Query.div(cpuUsageSecsAvgCassandra, cpuQuotaByPeriodCassandra)));

        if (BSF_DIAMETER_ENABLED)
        {
            // 3. diameter (in stm-diameter)
            final Query.Element cpuUsageSecsAvgDiameter = Query.sum(Query.rate(Query.metric("container_cpu_usage_seconds_total")
                                                                                    .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                    .param("container", "diameter"),
                                                                               60))
                                                               .by("pod");

            final Query.Element cpuQuotaByPeriodDiameter = Query.sum(Query.div(Query.metric("container_spec_cpu_quota")
                                                                                    .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                    .param("container", "diameter"),
                                                                               Query.metric("container_spec_cpu_period")
                                                                                    .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                    .param("container", "diameter")))
                                                                .by("pod");

            final Query.Element diameterQuery = Query.mul(Query.value(100), Query.avg(Query.div(cpuUsageSecsAvgDiameter, cpuQuotaByPeriodDiameter)));

            // 4. diameterproxygrpc
            final Query.Element cpuUsageSecsAvgGrpc = Query.sum(Query.rate(Query.metric("container_cpu_usage_seconds_total")
                                                                                .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                .param("container", "diameterproxygrpc"),
                                                                           60))
                                                           .by("pod");

            final Query.Element cpuQuotaByPeriodGrpc = Query.sum(Query.div(Query.metric("container_spec_cpu_quota")
                                                                                .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                .param("container", "diameterproxygrpc"),
                                                                           Query.metric("container_spec_cpu_period")
                                                                                .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                .param("container", "diameterproxygrpc")))
                                                            .by("pod");

            final Query.Element grpcQuery = Query.mul(Query.value(100), Query.avg(Query.div(cpuUsageSecsAvgGrpc, cpuQuotaByPeriodGrpc)));

            // 5. bsfdiameter
            final Query.Element cpuUsageSecsAvgBsfDiameter = Query.sum(Query.rate(Query.metric("container_cpu_usage_seconds_total")
                                                                                       .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                       .param("container", "bsfdiameter"),
                                                                                  60))
                                                                  .by("pod");

            final Query.Element cpuQuotaByPeriodBsfDiameter = Query.sum(Query.div(Query.metric("container_spec_cpu_quota")
                                                                                       .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                       .param("container", "bsfdiameter"),
                                                                                  Query.metric("container_spec_cpu_period")
                                                                                       .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                       .param("container", "bsfdiameter")))
                                                                   .by("pod");

            final Query.Element bsfDiameterQuery = Query.mul(Query.value(100), Query.avg(Query.div(cpuUsageSecsAvgBsfDiameter, cpuQuotaByPeriodBsfDiameter)));

            // 6. dsl
            final Query.Element cpuUsageSecsAvgBsfDsl = Query.sum(Query.rate(Query.metric("container_cpu_usage_seconds_total")
                                                                                  .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                  .param("container", "dsl"),
                                                                             60))
                                                             .by("pod");

            final Query.Element cpuQuotaByPeriodBsfDsl = Query.sum(Query.div(Query.metric("container_spec_cpu_quota")
                                                                                  .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                  .param("container", "dsl"),
                                                                             Query.metric("container_spec_cpu_period")
                                                                                  .param("namespace", EnvVars.get(ENV_NAMESPACE))
                                                                                  .param("container", "dsl")))
                                                              .by("pod");

            final Query.Element dslDiameterQuery = Query.mul(Query.value(100), Query.avg(Query.div(cpuUsageSecsAvgBsfDsl, cpuQuotaByPeriodBsfDsl)));

            this.inquisitor = new PmAdapter.Inquisitor(new PmAdapter(vertx,
                                                                     Integer.parseInt(EnvVars.get(ENV_PM_SERVER_SERVICE_PORT)),
                                                                     EnvVars.get(ENV_PM_SERVER_SERVICE_HOST)),
                                                       workerQuery,
                                                       cassandraQuery,
                                                       diameterQuery,
                                                       grpcQuery,
                                                       bsfDiameterQuery,
                                                       dslDiameterQuery);
        }
        else
        {
            this.inquisitor = new PmAdapter.Inquisitor(new PmAdapter(vertx,
                                                                     Integer.parseInt(EnvVars.get(ENV_PM_SERVER_SERVICE_PORT)),
                                                                     EnvVars.get(ENV_PM_SERVER_SERVICE_HOST)),
                                                       workerQuery,
                                                       cassandraQuery);
        }

        // initializing totalLoad
        this.totalLoad = 0.0;

        // Subscribe to configuration updates so that we know:
        // The nfInstance's RDN (for the counter towards NRF)
        this.config = config.toFlowable(BackpressureStrategy.LATEST)//
                            .doOnSubscribe(d -> log.debug("Start observing configuration changes."))
                            .doOnNext(o ->
                            {
                                log.debug("Applying configuration changes.");

                                o.ifPresentOrElse(c ->
                                {
                                    String[] labelValues = null;

                                    final var bsfFunction = o.get().getEricssonBsfBsfFunction();

                                    if (bsfFunction != null && !bsfFunction.getNfInstance().isEmpty())
                                    {
                                        final var nfInstance = bsfFunction.getNfInstance().get(0);

                                        if (nfInstance != null)
                                        {
                                            labelValues = MetricRegister.rdnToLabelValues(new Rdn("nf", "bsf-function").add("nf-instance",
                                                                                                                            nfInstance.getName()))
                                                                        .toArray(new String[0]);
                                        }
                                    }

                                    this.labelValues.set(labelValues);
                                }, () -> this.labelValues.set(null));
                            });
    }

    private static final List<Gauge> generateGcLoads(final boolean bsfDiameterEnabled)
    {
        final var loads = new ArrayList<Gauge>();

        loads.add(MetricRegister.singleton()
                                .register(Gauge.build()
                                               .namespace("bsf")
                                               .name("worker_load")
                                               .labelNames("nf", "nf_instance")
                                               .help("The BSF worker load [%].")
                                               .register()));

        loads.add(MetricRegister.singleton()
                                .register(Gauge.build()
                                               .namespace("bsf")
                                               .name("cassandra_load")
                                               .labelNames("nf", "nf_instance")
                                               .help("The Cassandra load [%].")
                                               .register()));

        if (bsfDiameterEnabled)
        {
            loads.add(MetricRegister.singleton()
                                    .register(Gauge.build()
                                                   .namespace("bsf")
                                                   .name("fe_diameter_load")
                                                   .labelNames("nf", "nf_instance")
                                                   .help("BSF FE load [%].")
                                                   .register()));
            loads.add(MetricRegister.singleton()
                                    .register(Gauge.build()
                                                   .namespace("bsf")
                                                   .name("proxy_grpc_diameter_load")
                                                   .labelNames("nf", "nf_instance")
                                                   .help("The Diameter proxy grpc load [%].")
                                                   .register()));
            loads.add(MetricRegister.singleton()
                                    .register(Gauge.build()
                                                   .namespace("bsf")
                                                   .name("diameter_load")
                                                   .labelNames("nf", "nf_instance")
                                                   .help("The GC Diameter load [%].")
                                                   .register()));
            loads.add(MetricRegister.singleton()
                                    .register(Gauge.build()
                                                   .namespace("bsf")
                                                   .name("dsl_load")
                                                   .labelNames("nf", "nf_instance")
                                                   .help("The DSL load [%].")
                                                   .register()));
        }

        loads.trimToSize();

        return loads;
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
                                                .subscribe(__ ->
                                                {
                                                }, t -> log.error("Error retrieving load data. Cause: {}", Utils.toString(t, log.isDebugEnabled()))));
        }).mergeWith(this.inquisitor.start());
    }

    public Completable stop()
    {
        return Completable.fromAction(() -> this.disposables.forEach(Disposable::dispose)).mergeWith(this.inquisitor.stop());
    }

    @Override
    public synchronized Double getCount()
    {
        return this.totalLoad;
    }

    private synchronized void setCount(List<PmAdapter.Query.Response.Data> data)
    {
        double totalLoadCpuMax = 0.0;  // bsfload total
        double load = 0.0; // one for each

        if (data == null)
        {
            return;
        }

        final String[] labelValues = this.labelValues.get();

        // The order of the data list must be the same as the order of the list of
        // gauges -- i runs on both of them
        for (int i = 0; i < data.size(); i++)
        {
            var result = data.get(i).getResult();
            if (!result.isEmpty())
            {
                // keep the max out of the whole lot
                totalLoadCpuMax = Math.max(totalLoadCpuMax, result.get(0).getValue().get(1));

                // but save each individually too
                load = result.get(0).getValue().get(1);
                load = Math.min(Math.max(0, load), 100); // Make sure that 0 <= l <= 100

                // set the gauges
                if (labelValues != null)
                {
                    gcLoads.get(i).labels(labelValues).set(load);
                }
            }
        }

        // report the max too
        totalLoadCpuMax = Math.min(Math.max(0, totalLoadCpuMax), 100); // Make sure that 0 <= l <= 100

        // Storing calculated totalLoadCpuMax -- it's needed by getCount
        this.totalLoad = totalLoadCpuMax;

        if (labelValues != null)
        {
            log.debug("total bsf Load: {}", totalLoad);
            gcBsfLoad.labels(labelValues).set(this.totalLoad);
        }
    }
}
