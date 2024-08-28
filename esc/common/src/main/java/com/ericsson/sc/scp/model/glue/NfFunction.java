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
 * Created on: May 9, 2019
 *     Author: eedstl
 */

package com.ericsson.sc.scp.model.glue;

import com.ericsson.cnal.common.CertificateObserver.Secret;
import com.ericsson.sc.common.alarm.AlarmHandler.Alarm;
import com.ericsson.sc.glue.NFProfileBuilder;
import com.ericsson.sc.scp.model.EricssonScpScpFunction;
import com.ericsson.utilities.common.IfCountProvider;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.metrics.MetricRegister;

import io.reactivex.Flowable;

public class NfFunction
{
    final Alarm.Context alarmCtx;
    final IfCountProvider loadMeter;
    final Flowable<Secret> secrets;
    final Rdn rdn;

    protected final NfInstance.Pool pool;

    EricssonScpScpFunction function;

    public NfFunction(final Alarm.Context alarmCtx,
                      final IfCountProvider loadMeter,
                      final Flowable<Secret> secrets,
                      final Rdn rdn)
    {
        this.alarmCtx = alarmCtx;
        this.loadMeter = loadMeter;
        this.secrets = secrets;
        this.rdn = rdn;
        this.pool = this.createPool();
        this.function = null;
    }

    public NfInstance getNfInstance(final int index)
    {
        return this.pool.get(index);
    }

    public void stop()
    {
        this.update(null);
    }

    public void update(final EricssonScpScpFunction function)
    {
        this.update(function, true, false);
    }

    public void update(final EricssonScpScpFunction function,
                       final boolean userRelated,
                       final boolean dnsRelated)
    {
        if (function != null)
        {
            this.function = function;
            this.pool.update(function.getNfInstance(), userRelated, dnsRelated);
        }
        else
        {
            this.pool.stop();
        }
    }

    protected NfInstance.Pool createPool()
    {
        return new NfInstance.Pool(this,
                                   (parent,
                                    rdn) -> new NfInstance(parent, rdn, new NFProfileBuilder()),
                                   rdn -> MetricRegister.singleton().registerForRemoval(rdn));
    }
}
