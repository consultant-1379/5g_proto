/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 5, 2018
 *     Author: eedstl
 */

package com.ericsson.adpal.cm;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public final class CmmPatch
{

    private static final Logger log = LoggerFactory.getLogger(CmmPatch.class);

    // FIXME do we really need two different retry strategies?
    private static final RetryFunction DEFAULT_RETRY_FUNCTION = new RetryFunction() //
                                                                                   .withDelay(1 * 1000L) // 1 second
                                                                                   .withRetries(5); // t seconds total
    private static final RetryFunction RETRY_FUNCTION = new RetryFunction().withDelay(5 * 1000L) // 5 seconds
                                                                           .withRetries(60); // 5 minutes total
    private static final String APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE.toString();
    private final String cmHost;
    private final int cmPort;
    private final Boolean sslEnabled;

    private WebClientProvider clientSubject;

    public CmmPatch(int cmPort,
                    String cmHost,
                    WebClientProvider subject,
                    boolean sslEnabled)
    {
        this.cmHost = cmHost;
        this.cmPort = cmPort;
        this.clientSubject = subject;
        this.sslEnabled = sslEnabled;
    }

    private JsonArray convertPatchItem(List<PatchItem> item)
    {
        final var arr = new JsonArray();
        for (final var it : item)
        {
            final var obj = new JsonObject();
            obj.put("op", it.getOp().toString());
            obj.put("path", it.getPath());
            obj.put("value", it.getValue());

            arr.add(obj);
        }

        if (log.isDebugEnabled())
            log.debug("Prepared patch: #patches={}, patches={}", arr.size(), arr);
        else
            log.info("Prepared patch: #patches={}", arr.size());

        return arr;
    }

    public Completable patch(String path,
                             List<PatchItem> listOfPatchItems)
    {

        return this.clientSubject.getWebClient()
                                 .flatMapCompletable(webClient -> webClient.patch(this.cmPort, this.cmHost, path)
                                                                           .ssl(sslEnabled)
                                                                           .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                                                           .rxSendJson(convertPatchItem(listOfPatchItems))
                                                                           .doOnError(err -> log.warn("CMM PATCH request failed: {}", path, err))
                                                                           .doOnSuccess(resp -> log.debug("CMM PATCH {}, statusCode: {}, statusMessage: {}, body: {}",
                                                                                                          path,
                                                                                                          resp.statusCode(),
                                                                                                          resp.statusMessage(),
                                                                                                          resp.bodyAsString()))
                                                                           .flatMapCompletable(resp -> resp.statusCode() == HttpResponseStatus.OK.code() ? Completable.complete()
                                                                                                                                                         : Completable.error(new RuntimeException("PATCH operation failed. statusCode: "
                                                                                                                                                                                                  + resp.statusCode()
                                                                                                                                                                                                  + ", body: "
                                                                                                                                                                                                  + resp.bodyAsString()))))
                                 .retryWhen(DEFAULT_RETRY_FUNCTION.create());
    }

    public Completable patchWithNoRetry(String path,
                                        List<PatchItem> listOfPatchItems,
                                        int reqeustTimeoutMillis)
    {

        return this.clientSubject.getWebClient()
                                 .flatMapCompletable(webClient -> webClient.patch(this.cmPort, this.cmHost, path)
                                                                           .timeout(reqeustTimeoutMillis)
                                                                           .ssl(sslEnabled)
                                                                           .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                                                           .rxSendJson(convertPatchItem(listOfPatchItems))
                                                                           .doOnError(err -> log.warn("CMM PATCH request failed: {}", path, err))
                                                                           .doOnSuccess(resp -> log.info("CMM PATCH {}, statusCode: {}, statusMessage: {}, body: {}",
                                                                                                         path,
                                                                                                         resp.statusCode(),
                                                                                                         resp.statusMessage(),
                                                                                                         resp.bodyAsString()))
                                                                           .flatMapCompletable(resp -> resp.statusCode() == HttpResponseStatus.OK.code() ? Completable.complete()
                                                                                                                                                         : Completable.error(new RuntimeException("PATCH operation failed. statusCode: "
                                                                                                                                                                                                  + resp.statusCode()
                                                                                                                                                                                                  + ", body: "
                                                                                                                                                                                                  + resp.bodyAsString()))));
    }

    public Single<Integer> post(String path,
                                JsonObject file)
    {

        return this.clientSubject.getWebClient()
                                 .flatMap(wc -> wc.post(this.cmPort, this.cmHost, path)
                                                  .ssl(this.sslEnabled)
                                                  .putHeader(CONTENT_TYPE, APPLICATION_JSON)

                                                  .rxSendJsonObject(file)
                                                  .doOnError(throwable -> log.warn("CMM POST operation failed: {}", path, throwable))
                                                  .doOnSuccess(resp -> log.debug("CMM POST {}, statusCode: {}, statudMessage: {}, body: {}",
                                                                                 path,
                                                                                 resp.statusCode(),
                                                                                 resp.statusMessage(),
                                                                                 resp.bodyAsString()))
                                                  .flatMap(resp -> resp.statusCode() == HttpResponseStatus.CREATED.code()
                                                                   || resp.statusCode() == HttpResponseStatus.CONFLICT.code() ? Single.just(resp.statusCode())
                                                                                                                              : Single.error(new RuntimeException("diameter configuration failed. statusCode: "
                                                                                                                                                                  + resp.statusCode()
                                                                                                                                                                  + ", body:"
                                                                                                                                                                  + resp.bodyAsString()))))
                                 .retryWhen(RETRY_FUNCTION.create());

    }

    public Single<String> get(String path)
    {

        return this.clientSubject.getWebClient()
                                 .flatMap(wc -> wc.get(this.cmPort, this.cmHost, path)
                                                  .ssl(this.sslEnabled)
                                                  .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                                  .rxSend()
                                                  .doOnError(throwable -> log.warn("GET request towards CMM failed: {}", path, throwable))
                                                  .doOnSuccess(resp -> log.debug("CMM GET {}, statusCode: {}, statudMessage: {}, body: {}",
                                                                                 path,
                                                                                 resp.statusCode(),
                                                                                 resp.statusMessage(),
                                                                                 resp.bodyAsString()))
                                                  .flatMap(resp -> resp.statusCode() == HttpResponseStatus.OK.code() ? Single.just(resp.bodyAsString())
                                                                                                                     : Single.error(new RuntimeException("CMM GET request failed. statusCode: "
                                                                                                                                                         + resp.statusCode()
                                                                                                                                                         + ", body: "
                                                                                                                                                         + resp.bodyAsString()))))
                                 .retryWhen(RETRY_FUNCTION.create());

    }

    public Completable put(String path,
                           JsonObject file)
    {

        return this.clientSubject.getWebClient()
                                 .flatMapCompletable(wc -> wc.put(this.cmPort, this.cmHost, path)
                                                             .ssl(this.sslEnabled)
                                                             .putHeader(CONTENT_TYPE, APPLICATION_JSON)

                                                             .rxSendJsonObject(file)
                                                             .doOnError(throwable -> log.warn("CMM PUT operation failed: {}", path, throwable))
                                                             .doOnSuccess(resp -> log.debug("CMM PUT {}, statusCode: {}, statudMessage: {}, body: {}",
                                                                                            path,
                                                                                            resp.statusCode(),
                                                                                            resp.statusMessage(),
                                                                                            resp.bodyAsString()))
                                                             .flatMapCompletable(resp -> resp.statusCode() == HttpResponseStatus.OK.code() ? Completable.complete()
                                                                                                                                           : Completable.error(new RuntimeException("CMM PUT request failed. statusCode: "
                                                                                                                                                                                    + resp.statusCode()
                                                                                                                                                                                    + ", body: "
                                                                                                                                                                                    + resp.bodyAsString()))))
                                 .retryWhen(RETRY_FUNCTION.create());

    }
}
