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
 * Created on: Aug 10, 2019
 *     Author: xchrfar
 */
package com.ericsson.sc.pm;

import java.util.Optional;

import com.ericsson.adpal.cm.CmAdapter;
import com.ericsson.adpal.cm.model.Data;
import com.ericsson.sc.pm.model.pmbr.All;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.reactivex.core.Vertx;

/**
 * Provides access to PM Bulkd Reporter API
 */
public class PmbrAdapter
{
    private final CmAdapter<All> cm;

    public PmbrAdapter(Vertx vertx,
                       int cmPort,
                       String cmHost,
                       String cmUrlPrefix,
                       CmAdapter<All> cm)
    {
        this.cm = cm;
//        cm = new CmAdapter<>(All.class, PM_BR_SCHEMA_NAME, vertx, cmPort, cmHost, cmUrlPrefix));
    }

    // FIXME Configuration update with mapped POJOs does not work properly.
    public Completable updateConfiguration(Data config)
    {
        // FIXME revisit accepted HTTP result codes from CM mediator
        return cm.getConfiguration()
                 .updateRaw(config)
                 .flatMapCompletable(result -> result == HttpResponseStatus.OK.code()
                                               || result == HttpResponseStatus.CREATED.code() ? Completable.complete()
                                                                                              : Completable.error(new RuntimeException("Unable to update configuration, CM mediator responded with HTTP "
                                                                                                                                       + result)));
    }

    public Completable updateConfiguration(JsonNode config)
    {
        Data cfg = Jackson.om().convertValue(config, Data.class);
        return updateConfiguration(cfg);
    }

    public Single<Optional<All>> getConfiguration()
    {
        return cm.getConfiguration().get();
    }
}
