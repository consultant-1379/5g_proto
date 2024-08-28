/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 1, 2024
 *     Author: znpvaap
 */

package com.ericsson.esc.bsf.manager.statedataprovider;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.rxetcd.EtcdEntries;
import com.ericsson.sc.rxetcd.EtcdEntry;
import com.ericsson.sc.rxetcd.EtcdKv;
import com.ericsson.sc.rxetcd.JsonValueSerializer;
import com.ericsson.sc.rxetcd.RxEtcd;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.http.HttpServerResponse;

/**
 * Handle the fetch of data from etcd, provide the http response to yang
 */
public class BsfModelLastUpdateDataResponse
{

    private static final Logger log = LoggerFactory.getLogger(BsfModelLastUpdateDataResponse.class);

    private static final String LAST_UPDATE = "last-update";

    private final RxEtcd etcd;
    private final HttpServerResponse httpResponse;
    private final JsonValueSerializer<String, String> etcdSerializer;

    public BsfModelLastUpdateDataResponse(RxEtcd etcd,
                                          HttpServerResponse httpResponse,
                                          JsonValueSerializer<String, String> etcdSerializer)
    {
        this.etcd = etcd;
        this.httpResponse = httpResponse;
        this.etcdSerializer = etcdSerializer;
    }

    private Single<JsonObject> fetchStateDataFromEtcd()
    {
        return this.etcd.ready()
                        .andThen(this.etcd.get(this.etcdSerializer.keyBytes(LAST_UPDATE))
                                          .map(getResp -> new EtcdEntries<>(getResp.getHeader().getRevision(),
                                                                            getResp.getKvs() //
                                                                                   .stream()
                                                                                   .map(kv ->
                                                                                   {
                                                                                       try
                                                                                       {
                                                                                           return new EtcdEntry<>(this.etcdSerializer, kv);
                                                                                       }
                                                                                       catch (NullPointerException e)
                                                                                       {
                                                                                           log.warn("Serializer, value or key is not instanitated as an etcd entry: {}",
                                                                                                    kv.getKey(),
                                                                                                    e);
                                                                                           return null;
                                                                                       }
                                                                                   })
                                                                                   .filter(Objects::nonNull)
                                                                                   .toList()))
                                          .map(this::transformEtcdEntriesToJson));
    }

    private JsonObject transformEtcdEntriesToJson(EtcdEntries<String, String> entries)
    {
        log.debug("The etcd entries are {}", entries);

        final var etcdValue = entries.getEntries().stream().findFirst().map(EtcdKv::getValue);
        final var jsonObject = etcdValue.isPresent() ? JsonObject.of(LAST_UPDATE, etcdValue.get()) : JsonObject.of();

        log.debug("The Json object is for the yang response is: {}", jsonObject);
        return jsonObject;
    }

    public Completable respond()
    {
        return fetchStateDataFromEtcd().flatMapCompletable(fetchedData -> fetchedData.isEmpty() ? httpResponse.setStatusCode(204)
                                                                                                              .rxEnd()
                                                                                                              .doOnComplete(() -> log.info("No data was found in etcd"))
                                                                                                : httpResponse.putHeader("content-type", "application/json")
                                                                                                              .rxEnd(fetchedData.encodePrettily()))
                                       .doOnError(e -> log.error("Unable to fetch Nf discovery last-update data from etcd", e));
    }

}
