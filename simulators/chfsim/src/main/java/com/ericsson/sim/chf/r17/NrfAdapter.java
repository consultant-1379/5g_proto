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
 * Created on: Nov 28, 2019
 *     Author: eedstl
 */

package com.ericsson.sim.chf.r17;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.common.WebClient;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.Links;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.http.Url;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.ProtocolViolationException;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.net.SocketAddress;
import io.vertx.reactivex.ext.web.client.HttpResponse;

public class NrfAdapter
{
    public static class Result<T>
    {
        private final HttpResponse<Buffer> response;
        private final Object body;
        private final int statusCode;

        public Result(HttpResponse<Buffer> response)
        {
            this.response = response;
            this.body = null;
            this.statusCode = response.statusCode();
        }

        public Result(final HttpResponse<Buffer> response,
                      final ProblemDetails problems)
        {
            this.response = response;
            this.body = problems;
            this.statusCode = response.statusCode();
        }

        public Result(final HttpResponse<Buffer> response,
                      final T body)
        {
            this.response = response;
            this.body = body;
            this.statusCode = response.statusCode();
        }

        public Result(final int statusCode)
        {
            this.response = null;
            this.body = null;
            this.statusCode = statusCode;
        }

        /**
         * @return If hasProblems() returns false, the body or null if there is no body
         *         in the response.
         */
        @SuppressWarnings("unchecked")
        public T getBody()
        {
            return (T) this.body;
        }

        /**
         * @param name The name of the header to be returned.
         * @return The value of the header or null if there is no header of the name
         *         passed.
         */
        public String getHeader(final String name)
        {
            return this.response != null ? this.response.getHeader(name) : null;
        }

        /**
         * @return If hasProblems() returns true, the problem details or null if there
         *         is no body in the response.
         */
        public ProblemDetails getProblems()
        {
            return (ProblemDetails) this.body;
        }

        /**
         * @return The status code.
         */
        public int getStatusCode()
        {
            return this.statusCode;
        }

        /**
         * Always call this method before calling getBody() or getProblems().
         * 
         * @return False if 200 <= statusCode < 300, true otherwise.
         */
        public boolean hasProblems()
        {
            return !(200 <= this.statusCode && this.statusCode < 300);
        }
    }

    protected static class Context
    {
        private SocketAddress serverAddr; // This is used to connect to the server.
        private String url; // This is used in the URL and for the HTTP HOST header.
        private String body;
        private long requestTimeoutInSecs;
        private long nowInMillis;

        public Context(final Url serverUrl,
                       final String path,
                       final String body,
                       final long requestTimeoutInSecs)
        {
            this.serverAddr = serverUrl.getAddr();
            this.url = serverUrl.getUrl().toString() + path;
            this.body = body;
            this.requestTimeoutInSecs = requestTimeoutInSecs;
        }

        public String getBody()
        {
            return this.body;
        }

        public synchronized long getNowInMillis()
        {
            log.debug("nowInMillis={}", this.nowInMillis);
            return this.nowInMillis;
        }

        public long getRequestTimeoutInSecs()
        {
            return this.requestTimeoutInSecs;
        }

        public SocketAddress getServerAddr()
        {
            return this.serverAddr;
        }

        public String getUrl()
        {
            return this.url;
        }

        public synchronized void setNowInMillis()
        {
            this.nowInMillis = System.currentTimeMillis();
            log.debug("nowInMillis={}", this.nowInMillis);
        }
    }

    protected enum EventTrigger
    {
        NNRF_MANAGEMENT_DEREGISTER,
        NNRF_MANAGEMENT_REGISTER,
        NNRF_MANAGEMENT_UPDATE;
    }

    public static final int HTTP_KEEP_ALIVE_TIMEOUT_SECS = 24 * 60 * 60; // [s] Maximum time with no traffic on the line.
    private static final Logger log = LoggerFactory.getLogger(NrfAdapter.class);
    protected static final ObjectMapper json = OpenApiObjectMapper.singleton();
    protected static final long NRF_REQUEST_TIMEOUT_IN_SECS = 6;

    protected final UUID nfInstanceId;

    private final WebClient client;
    private final Statistics.Nrf statistics;

    private Url url;
    private Single<Result<NFProfile>> nfInstanceDeregister;
    private Single<Result<NFProfile>> nfInstanceGet;
    private Single<Result<NFProfile>> nfInstanceRegister;
    private Single<Result<NFProfile>> nfInstanceUpdate;
    private Single<Result<NFProfile>> nfInstanceUpdateIfDue;
    private long lastSuccessfulUpdateInMillis;
    private long heartbeatTimeoutInMillis;
    private long heartbeatCount; // Number of successful updates.
    private int heartbeatFullUpdateEveryNthTime; // 0 means disabled, i.e. all heart-beats are sent as PATCHes.
    private Disposable updater;

    public NrfAdapter(final WebClient client,
                      final UUID nfInstanceId)
    {
        this.client = client;
        this.nfInstanceId = nfInstanceId;
        this.statistics = new Statistics.Nrf(this.nfInstanceId.toString());

        this.url = new Url("eric-nrfsim", 80, "/nnrf-nfm/v1");

        this.nfInstanceDeregister = this.nfInstanceDeregisterCreate();
        this.nfInstanceGet = this.nfInstanceGetCreate();
        this.nfInstanceRegister = this.nfInstanceRegisterCreate();
        this.nfInstanceUpdate = this.nfInstanceUpdateCreate();
        this.nfInstanceUpdateIfDue = this.nfInstanceUpdateIfDueCreate();

        this.lastSuccessfulUpdateInMillis = 0;
        this.heartbeatTimeoutInMillis = 1000; // Default: 1 Hz
        this.heartbeatCount = 0;
        this.heartbeatFullUpdateEveryNthTime = 0; // 0 means inactive, i.e. all updates will be sent as PATCHes.
    }

    public Statistics.Nrf getStatistics()
    {
        return this.statistics;
    }

    public Single<Result<NFProfile>> nfInstanceDeregister()
    {
        return this.nfInstanceDeregister;
    }

    public Single<Result<NFProfile>> nfInstanceGet()
    {
        return this.nfInstanceGet;
    }

    public Single<Result<NFProfile>> nfInstanceRegister()
    {
        return this.nfInstanceRegister;
    }

    public Single<Result<Links>> nfInstancesGet(final String nfType,
                                                final Integer limit)
    {
        return this.nfInstancesGetCreate(nfType, limit);
    }

    public Single<Result<NFProfile>> nfInstanceUpdate()
    {
        return this.nfInstanceUpdate;
    }

    public synchronized NrfAdapter setAddr(final String host,
                                           final Integer port)
    {
        return this.setAddr(this.url.getUrl().getProtocol(), host, port, null);
    }

    public synchronized NrfAdapter setAddr(final String host,
                                           final Integer port,
                                           final String ip)
    {
        return this.setAddr(this.url.getUrl().getProtocol(), host, port, ip);
    }

    public synchronized NrfAdapter setAddr(final String protocol,
                                           final String host,
                                           final Integer port,
                                           final String ip)
    {
        this.url = new Url(protocol, host, port, this.url.getUrl().getPath(), ip);
        return this;
    }

    public synchronized NrfAdapter setHeartbeatFullUpdateEveryNthTime(int n)
    {
        this.heartbeatFullUpdateEveryNthTime = Math.max(0, n);
        return this;
    }

    public Completable start()
    {
        return Completable.fromAction(() ->
        {
            if (this.updater == null)
            {
                this.updater = this.nfInstanceUpdateIfDue.onErrorReturnItem(new Result<NFProfile>(HttpResponseStatus.OK.code()))
                                                         .repeatWhen(handler -> handler.delay(1, TimeUnit.SECONDS))
                                                         .doOnSubscribe(d -> log.info("Started updating NRF."))
                                                         .ignoreElements()
                                                         .doOnDispose(() -> log.info("Stopped updating NRF."))
                                                         .subscribe(() -> log.info("Stopped updating NRF."),
                                                                    t -> log.error("Stopped updating NRF. Cause: {}", Utils.toString(t, log.isDebugEnabled())));
            }
        });
    }

    public Completable stop()
    {
        return Completable.fromAction(() ->
        {
            if (this.updater != null)
            {
                this.updater.dispose();
                this.updater = null;
            }
        });
    }

    private boolean condHeartbeatFullUpdateIsDue(final Result<NFProfile> response)
    {
        return (this.heartbeatFullUpdateEveryNthTime != 0 && (this.heartbeatCount % this.heartbeatFullUpdateEveryNthTime) == 0);
    }

    private boolean condHeartbeatTimerHasNotExpired(final Result<NFProfile> response)
    {
        return (System.currentTimeMillis() - this.lastSuccessfulUpdateInMillis < this.heartbeatTimeoutInMillis);
    }

    private boolean condResponseIsOk(final Result<NFProfile> response)
    {
        return (response.getStatusCode() >= 200 && response.getStatusCode() < 300);
    }

    private Single<Result<NFProfile>> nfInstanceDeregisterCreate()
    {
        return Single.fromCallable(this::nfInstanceDeregisterContextCreate)//
                     .flatMap(ctx -> nfInstanceDeregisterSend(ctx));
    }

    private Single<Result<NFProfile>> nfInstanceDeregisterSend(Context ctx)
    {
        return this.client.requestAbs(HttpMethod.DELETE, ctx.getServerAddr(), ctx.getUrl()) //
                          .rxSend()
                          .subscribeOn(Schedulers.io())
                          .timeout(ctx.getRequestTimeoutInSecs(), TimeUnit.SECONDS)
                          .map(resp ->
                          {
                              final String body = resp.bodyAsString();
                              log.debug("DELETE response: {} {}", resp.statusCode(), body);

                              this.stepCcInAns("DELETE", ctx.getUrl(), resp.statusCode());

                              switch (resp.statusCode())
                              {
                                  case 200: // Treat the same as 204, ignore body
                                  case 204:
                                  {
                                      this.heartbeatTimeoutInMillis = 1000; // Reset to initial value
                                      this.heartbeatCount = 0;
                                      return new Result<NFProfile>(resp);
                                  }

                                  default:
                                      return this.resultWithProblemDetails(resp, body);
                              }
                          })
                          .doOnSuccess(response ->
                          {
                              final Event event = this.statistics.getHistoryOfEvents().getLatest();
                              event.setResponse(HttpResponseStatus.valueOf(response.getStatusCode()));

                              this.statistics.getCountOutHttpRequests().inc();
                              this.statistics.getCountInHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                          })
                          .doOnError(cause ->
                          {
                              final String msg = cause.toString();
                              final Event event = this.statistics.getHistoryOfEvents().getLatest();
                              event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                              log.error("Error sending Deregister request. Cause: '{}'.", log.isDebugEnabled() ? cause : msg);

                              this.statistics.getCountOutHttpRequests().inc();
                              this.statistics.getCountInHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                          })
                          .doOnSubscribe(d ->
                          {
                              this.statistics.getHistoryOfEvents()
                                             .put(new Event(EventTrigger.NNRF_MANAGEMENT_DEREGISTER.name(), JsonObject.class.getName(), ctx.getBody()));

                              this.stepCcOutReq("DELETE", ctx.getUrl());
                          });
    }

    private Single<Result<NFProfile>> nfInstanceGetCreate()
    {
        return Single.fromCallable(this::nfInstanceGetContextCreate)//
                     .flatMap(ctx -> nfInstanceGetSend(ctx));
    }

    private Single<Result<NFProfile>> nfInstanceGetSend(Context ctx)
    {
        return this.client.requestAbs(HttpMethod.GET, ctx.getServerAddr(), ctx.getUrl()) //
                          .rxSend()
                          .subscribeOn(Schedulers.io())
                          .timeout(ctx.getRequestTimeoutInSecs(), TimeUnit.SECONDS)
                          .map(resp ->
                          {
                              final String body = resp.bodyAsString();
                              log.debug("GET response: {} {}", resp.statusCode(), body);

                              this.stepCcInAns("GET", ctx.getUrl(), resp.statusCode());

                              switch (resp.statusCode())
                              {
                                  case 200:
                                  {
                                      final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                      final String contentType = resp.getHeader("Content-Type");

                                      if (contentType == null)
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: unspecified content-type.");

                                      if (!contentType.toLowerCase().contains("application/json"))
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: invalid content-type: '" + contentType
                                                                               + "'.");

                                      if (body == null)
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: no body in response.");

                                      return new Result<>(resp, json.readValue(body, NFProfile.class));
                                  }

                                  default:
                                      return this.resultWithProblemDetails(resp, body);
                              }
                          })
                          .doOnSubscribe(d -> this.stepCcOutReq("GET", ctx.getUrl()))
                          .doOnError(t -> log.error("Error sending request. Cause: {}", t.getMessage()))
                          .onErrorReturn(e -> new Result<NFProfile>(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
    }

    private Single<Result<NFProfile>> nfInstanceRegisterCreate()
    {
        return Single.fromCallable(this::nfInstanceRegisterContextCreate)//
                     .flatMap(ctx -> nfInstanceRegisterSend(ctx));
    }

    private Single<Result<NFProfile>> nfInstanceRegisterSend(Context ctx)
    {
        return this.client.requestAbs(HttpMethod.PUT, ctx.getServerAddr(), ctx.getUrl()) //
                          .rxSendJsonObject(new JsonObject(ctx.getBody()))
                          .subscribeOn(Schedulers.io())
                          .timeout(ctx.getRequestTimeoutInSecs(), TimeUnit.SECONDS)
                          .map(resp ->
                          {
                              final String body = resp.bodyAsString();
                              log.debug("PUT response: {} {}", resp.statusCode(), body);

                              this.stepCcInAns("PUT", ctx.getUrl(), resp.statusCode());

                              switch (resp.statusCode())
                              {
                                  case 200:
                                  case 201:
                                  {
                                      final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                      final String contentType = resp.getHeader("Content-Type");

                                      if (contentType == null)
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: unspecified content-type.");

                                      if (!contentType.toLowerCase().contains("application/json"))
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: invalid content-type: '" + contentType
                                                                               + "'.");

                                      if (body == null || body.isEmpty())
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: no body in response.");

                                      this.lastSuccessfulUpdateInMillis = ctx.getNowInMillis();
                                      this.heartbeatCount++;

                                      final Result<NFProfile> result = new Result<>(resp, json.readValue(body, NFProfile.class));

                                      final boolean ok = result.getBody() != null && result.getBody().getHeartBeatTimer() != null;

                                      if (ok)
                                          this.heartbeatTimeoutInMillis = Math.min(result.getBody().getHeartBeatTimer() * 1000,
                                                                                   NrfAdapter.HTTP_KEEP_ALIVE_TIMEOUT_SECS - 1);

                                      return result;
                                  }

                                  default:
                                      return this.resultWithProblemDetails(resp, body);
                              }
                          })
                          .doOnSuccess(response ->
                          {
                              final Event event = this.statistics.getHistoryOfEvents().getLatest();
                              event.setResponse(HttpResponseStatus.valueOf(response.getStatusCode()), json.writeValueAsString(response.getBody()));

                              this.statistics.getCountOutHttpRequests().inc();
                              this.statistics.getCountInHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                          })
                          .doOnError(cause ->
                          {
                              final String msg = cause.toString();
                              final Event event = this.statistics.getHistoryOfEvents().getLatest();
                              event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                              log.error("Error sending Update request. Cause: '{}'.", log.isDebugEnabled() ? cause : msg);

                              this.statistics.getCountOutHttpRequests().inc();
                              this.statistics.getCountInHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                          })
                          .doOnSubscribe(d ->
                          {
                              ctx.setNowInMillis();

                              this.statistics.getHistoryOfEvents()
                                             .put(new Event(EventTrigger.NNRF_MANAGEMENT_REGISTER.name(), JsonObject.class.getName(), ctx.getBody()));

                              this.stepCcOutReq("PUT", ctx.getUrl());
                          });
    }

    private Single<Result<Links>> nfInstancesGetCreate(final String nfType,
                                                       final Integer limit)
    {
        final StringBuilder b = new StringBuilder("/nf-instances/");
        String sep = "?";

        if (nfType != null)
        {
            b.append(sep).append("nf-type=").append(nfType);
            sep = "&";
        }

        if (limit != null)
        {
            b.append(sep).append("limit=").append(limit);
            sep = "&";
        }

        return Single.just(new Context(this.getUrl(), b.toString(), null, NRF_REQUEST_TIMEOUT_IN_SECS))//
                     .flatMap(ctx -> nfInstancesGetSend(ctx));
    }

    private Single<Result<Links>> nfInstancesGetSend(Context ctx)
    {
        return this.client.requestAbs(HttpMethod.GET, ctx.getServerAddr(), ctx.getUrl()) //
                          .rxSend()
                          .subscribeOn(Schedulers.io())
                          .timeout(ctx.getRequestTimeoutInSecs(), TimeUnit.SECONDS)
                          .map(resp ->
                          {
                              log.debug("GET response: {} {}", resp.statusCode(), resp.bodyAsString());

                              this.stepCcInAns("GET", ctx.getUrl(), resp.statusCode());

                              switch (resp.statusCode())
                              {
                                  case 200:
                                  {
                                      final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                      final String contentType = resp.getHeader("Content-Type");

                                      if (contentType == null)
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: unspecified content-type.");

                                      if (!contentType.toLowerCase().contains("application/3gpphal+json"))
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: invalid content-type: '" + contentType
                                                                               + "'.");

                                      final JsonObject bodyAsJsonObject = resp.bodyAsJsonObject();

                                      if (bodyAsJsonObject == null)
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: no body in response.");

                                      if (bodyAsJsonObject.getJsonObject("_links") == null)
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: no '_links' in body.");

                                      return new Result<>(resp, json.readValue(bodyAsJsonObject.getJsonObject("_links").toString(), Links.class));
                                  }

                                  default:
                                  {
                                      final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                      throw new RuntimeException(status + ": unsuccessful response received: '" + resp.bodyAsString() + "'.");
                                  }
                              }
                          })
                          .doOnSubscribe(d -> this.stepCcOutReq("GET", ctx.getUrl()))
                          .doOnError(t -> log.error("Error sending request. Cause: {}", t.getMessage()))
                          .onErrorReturn(e -> new Result<Links>(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
    }

    private Single<Result<NFProfile>> nfInstanceUpdateCreate()
    {
        return Single.fromCallable(this::nfInstanceUpdateContextCreate)//
                     .flatMap(ctx -> nfInstanceUpdateSend(ctx));
    }

    private Single<Result<NFProfile>> nfInstanceUpdateIfDueCreate()
    {
        return Single.just(new Result<NFProfile>(HttpResponseStatus.OK.code()))
                     .filter(this::condHeartbeatTimerHasNotExpired)
                     .switchIfEmpty(Single.just(new Result<NFProfile>(HttpResponseStatus.NOT_FOUND.code()))
                                          .filter(this::condHeartbeatFullUpdateIsDue)
                                          .switchIfEmpty(this.nfInstanceUpdateCreate())
                                          .filter(this::condResponseIsOk)
                                          .switchIfEmpty(this.nfInstanceRegisterCreate()));
    }

    private Single<Result<NFProfile>> nfInstanceUpdateSend(Context ctx)
    {
        return this.client.requestAbs(HttpMethod.PATCH, ctx.getServerAddr(), ctx.getUrl()) //
                          .putHeader("Content-Type", "application/json-patch+json")
                          .rxSendJson(new JsonArray(ctx.getBody()))//
                          .subscribeOn(Schedulers.io())
                          .timeout(ctx.getRequestTimeoutInSecs(), TimeUnit.SECONDS)
                          .map(resp ->
                          {
                              final String body = resp.bodyAsString();
                              log.debug("PATCH response: {} {}", resp.statusCode(), body);

                              this.stepCcInAns("PATCH", ctx.getUrl(), resp.statusCode());

                              switch (resp.statusCode())
                              {
                                  case 200:
                                  {
                                      final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                      final String contentType = resp.getHeader("Content-Type");

                                      if (contentType == null)
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: unspecified content-type.");

                                      if (!contentType.toLowerCase().contains("application/json"))
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: invalid content-type: '" + contentType
                                                                               + "'.");

                                      if (body == null || body.isEmpty())
                                          throw new ProtocolViolationException(status + ": invalid response. Cause: no body in response.");

                                      this.lastSuccessfulUpdateInMillis = ctx.getNowInMillis();
                                      this.heartbeatCount++;

                                      final Result<NFProfile> result = new Result<>(resp, json.readValue(body, NFProfile.class));

                                      if (result.getBody().getHeartBeatTimer() != null)
                                          this.heartbeatTimeoutInMillis = Math.min(result.getBody().getHeartBeatTimer() * 1000,
                                                                                   NrfAdapter.HTTP_KEEP_ALIVE_TIMEOUT_SECS - 1);

                                      return result;
                                  }

                                  case 204:
                                  {
                                      this.lastSuccessfulUpdateInMillis = ctx.getNowInMillis();
                                      this.heartbeatCount++;
                                      return new Result<NFProfile>(resp);
                                  }

                                  default:
                                      return this.resultWithProblemDetails(resp, body);
                              }
                          })
                          .doOnSuccess(response ->
                          {
                              final Event event = this.statistics.getHistoryOfEvents().getLatest();
                              event.setResponse(HttpResponseStatus.valueOf(response.getStatusCode(), json.writeValueAsString(response.getBody())));

                              this.statistics.getCountOutHttpRequests().inc();
                              this.statistics.getCountInHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                          })
                          .doOnError(cause ->
                          {
                              final String msg = cause.toString();
                              final Event event = this.statistics.getHistoryOfEvents().getLatest();
                              event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                              log.error("Error sending Update request. Cause: '{}'.", log.isDebugEnabled() ? cause : msg);

                              this.statistics.getCountOutHttpRequests().inc();
                              this.statistics.getCountInHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                          })
                          .doOnSubscribe(d ->
                          {
                              ctx.setNowInMillis();

                              this.statistics.getHistoryOfEvents()
                                             .put(new Event(EventTrigger.NNRF_MANAGEMENT_UPDATE.name(), JsonObject.class.getName(), ctx.getBody()));

                              this.stepCcOutReq("PATCH", ctx.getUrl());
                          });
    }

    private Result<NFProfile> resultWithProblemDetails(HttpResponse<Buffer> resp,
                                                       final String body) throws IOException, JsonParseException, JsonMappingException
    {
        {
            final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
            log.info("Unexpected response received for Register request: {}, body='{}'.", status, body);

            final String contentType = resp.getHeader("Content-Type");

            if (contentType == null)
                throw new ProtocolViolationException(status + ": invalid response. Cause: unspecified content-type.");

            if (!contentType.toLowerCase().contains("application/problem+json"))
                throw new ProtocolViolationException(status + ": invalid response. Cause: invalid content-type: '" + contentType + "'.");

            if (body == null)
                throw new ProtocolViolationException(status + ": invalid response. Cause: no body in response.");

            return new Result<NFProfile>(resp, json.readValue(body, ProblemDetails.class));
        }
    }

    protected synchronized int getHeartbeatFullUpdateEveryNthTime()
    {
        return this.heartbeatFullUpdateEveryNthTime;
    }

    protected synchronized Url getUrl()
    {
        return this.url;
    }

    protected Context nfInstanceDeregisterContextCreate()
    {
        throw new RuntimeException("Method not implemented in subclass.");
    }

    protected Context nfInstanceGetContextCreate()
    {
        throw new RuntimeException("Method not implemented in subclass.");
    }

    protected Context nfInstanceRegisterContextCreate()
    {
        throw new RuntimeException("Method not implemented in subclass.");
    }

    protected Context nfInstanceUpdateContextCreate()
    {
        throw new RuntimeException("Method not implemented in subclass.");
    }

    protected void stepCcInAns(final String method,
                               final String path,
                               final Integer statusCode)
    {
        // No action here, do it in subclass knowing the count and its requirements.
    }

    protected void stepCcOutReq(final String method,
                                final String path)
    {
        // No action here, do it in subclass knowing the count and its requirements.
    }
}
