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
 * Created on: Nov 2, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.reactivex.Completable;

/**
 * Provides a common interface for all metrics reporter implementations.
 * 
 * MetricsReporter implementers are responsible to provide the required metrics
 * configuration for Vert.x and to provide non blocking start and stop methods
 * to control the reporter.
 */
public interface MetricsReporter
{
    /**
     * This method returns the metrics registry used by this metrics reporter.
     * 
     * @return A MeterRegistry used by the reporter to exports metrics.
     */
    public MeterRegistry getRegistry();

    /**
     * This method starts the reporter that is used to publish the metrics. The
     * metrics can be exported via different reporters like a HTTP server or a
     * FileHandler. This operation should be non-blocking, so it should return right
     * after the reporter has started and not after completing.
     * 
     * @return Completable Completes when the initiation of the reporter is
     *         completed.
     */
    public Completable start();

    /**
     * This method stops the reporter from reporting any new metrics.
     * 
     * @return Completable Completes when the reporter is stopped gracefully and the
     *         relevant resources are released.
     */
    public Completable stop();
}
