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
 * Created on: Jan 8, 2019
 *     Author: eedstl
 */

package com.ericsson.sim.sepp.r17;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.common.CertificateObserver.Secret;
import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.common.WebClient;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.Links;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.ServiceName;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PatchItem;
import com.ericsson.utilities.common.Rdn;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.metrics.MetricRegister;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.exceptions.ProtocolViolationException;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;

public class NrfAdapter
{
    public static class Query
    {
        public static class Builder
        {
            private final Map<String, Object> params;
            private final StringBuilder query;

            private String separator;

            public Builder()
            {
                this.params = new HashMap<>();
                this.query = new StringBuilder();
                this.separator = "?";
            }

            public Builder add(final String name,
                               final Object value)
            {
                if (value != null)
                {
                    this.query.append(this.separator);
                    this.query.append(name);

                    final String paramVal = value.toString();

                    if (!paramVal.isEmpty())
                        this.query.append("=").append(paramVal);

                    this.separator = "&";

                    this.params.put(name, value);
                }

                return this;
            }

            public Query build()
            {
                return new Query(this.query.toString(), this.params);
            }
        }

        private final Map<String, Object> params;
        private final String query;

        private Query(String query,
                      Map<String, Object> params)
        {
            this.query = query;
            this.params = params;
        }

        public Object getParam(final String name)
        {
            return this.params.get(name);
        }

        @Override
        public String toString()
        {
            log.debug("query={}", this.query);
            return this.query;
        }
    }

    public static class Result<T>
    {
        private final HttpResponse<Buffer> response;
        private final T body;
        private final int statusCode;

        public Result(HttpResponse<Buffer> response)
        {
            this.response = response;
            this.body = null;
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
         * @return The body or null if there is no body.
         */
        public T getBody()
        {
            return this.body;
        }

        /**
         * If hasProblem() == true, this method must be used to get the body. Otherwise,
         * use getBody().
         * 
         * @return The body of the response or null.
         */
        public String getBodyAsString()
        {
            return this.response.bodyAsString();
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
         * @return The status code.
         */
        public int getStatusCode()
        {
            return this.statusCode;
        }

        /**
         * @return True if not (200 <= statusCode < 300).
         */
        public boolean hasProblem()
        {
            return this.statusCode < 200 || this.statusCode > 299;
        }
    }

    public static final int HTTP_KEEP_ALIVE_TIMEOUT_SECS = 24 * 60 * 60; // [s] Maximum time with no traffic on the line.

    public static final int HTTP_CONNECT_TIMEOUT_MILLIS = 1000; // [ms]
    private static final Logger log = LoggerFactory.getLogger(NrfAdapter.class);
    private static final ObjectMapper json = OpenApiObjectMapper.singleton();
    private static final io.prometheus.client.Counter ccOutReq = MetricRegister.singleton()
                                                                               .register(io.prometheus.client.Counter.build()
                                                                                                                     .namespace("nrf")
                                                                                                                     .subsystem("adapter")
                                                                                                                     .name("out_requests_total")
                                                                                                                     .labelNames("service",
                                                                                                                                 "nf",
                                                                                                                                 "nf_instance",
                                                                                                                                 "nrf_group",
                                                                                                                                 "nrf",
                                                                                                                                 "method",
                                                                                                                                 "path")
                                                                                                                     .help("Number of outgoing HTTP requests on the Nnrf interface")
                                                                                                                     .register());

    private static final io.prometheus.client.Counter ccInAns = MetricRegister.singleton()
                                                                              .register(io.prometheus.client.Counter.build()
                                                                                                                    .namespace("nrf")
                                                                                                                    .subsystem("adapter")
                                                                                                                    .name("in_answers_total")
                                                                                                                    .labelNames("service",
                                                                                                                                "nf",
                                                                                                                                "nf_instance",
                                                                                                                                "nrf_group",
                                                                                                                                "nrf",
                                                                                                                                "method",
                                                                                                                                "path",
                                                                                                                                "status")
                                                                                                                    .help("Number of incoming HTTP answers on the Nnrf interface")
                                                                                                                    .register());

    private static URL createUrl(final Url url,
                                 final String service,
                                 final String nfInstance,
                                 final String query)
    {
        final StringBuilder path = new StringBuilder("/").append(service).append("/v1/nf-instances");

        log.debug("url={}", url);

        if (!nfInstance.isEmpty())
            path.append("/").append(nfInstance).toString();

        if (!query.isEmpty())
            path.append(query.startsWith("?") ? "" : "?").append(query);

        URL result = url.getUrl();

        try
        {
            result = new URL(result.getProtocol(), result.getHost(), result.getPort(), path.toString());
        }
        catch (MalformedURLException e)
        {
            log.error("Error creating new URL from url='{}', nfInstance='{}', query='{}'. Cause: {}",
                      url,
                      nfInstance,
                      query,
                      Utils.toString(e, log.isDebugEnabled()));
        }

        return result;
    }

    private static void stepCcInAns(final Rdn nrf,
                                    final String service,
                                    final String method,
                                    final String path,
                                    final Integer statusCode)
    {
        log.debug("nrf={}, method={}, path={}, statusCode={}", nrf, method, path, statusCode);

        final List<String> labelValues = new ArrayList<>();
        labelValues.add(service);
        labelValues.addAll(MetricRegister.rdnToLabelValues(nrf)); // nf_type, nf_instance, nrf_group
        labelValues.add(method); // method
        labelValues.add(path); // path
        labelValues.add(HttpResponseStatus.valueOf(statusCode).toString()); // status

        ccInAns.labels(labelValues.toArray(new String[0])).inc();
    }

    private static void stepCcOutReq(final Rdn nrf,
                                     final String service,
                                     final String method,
                                     final String path)
    {
        log.debug("nrf={}, method={}, path={}", nrf, method, path);

        final List<String> labelValues = new ArrayList<>();
        labelValues.add(service);
        labelValues.addAll(MetricRegister.rdnToLabelValues(nrf)); // nf_type, nf_instance, nrf_group
        labelValues.add(method); // method
        labelValues.add(path); // path

        ccOutReq.labels(labelValues.toArray(new String[0])).inc();
    }

    private static void validateResponse(final HttpResponse<Buffer> resp,
                                         final String requiredContentType,
                                         final String body)
    {
        final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();

        final String contentType = resp.getHeader("Content-Type");

        if (contentType == null)
            throw new ProtocolViolationException(status + ": invalid response. Cause: unspecified content-type.");

        if (!contentType.toLowerCase().contains(requiredContentType))
            throw new ProtocolViolationException(status + ": invalid response. Cause: invalid content-type: '" + resp.getHeader("Content-Type") + "'.");

        if (body == null || body.isEmpty())
            throw new ProtocolViolationException(status + ": invalid response. Cause: no body in response.");
    }

    private final Rdn nrf;
    private final Url url;

    private final WebClient client;

    public NrfAdapter(final Rdn nrf,
                      final Url url)
    {
        this(nrf, url, null);
    }

    public NrfAdapter(final Rdn nrf,
                      final Url url,
                      final Flowable<Secret> secrets)
    {
        this.nrf = nrf;
        this.url = url;
        this.client = new WebClient(secrets)
        {
            @Override
            protected WebClientOptions modifyOptions(final WebClientOptions options)
            {
                return options.setKeepAliveTimeout(NrfAdapter.HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                              .setHttp2KeepAliveTimeout(NrfAdapter.HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                              .setConnectTimeout(NrfAdapter.HTTP_CONNECT_TIMEOUT_MILLIS);
            }
        };
    }

    public void close()
    {
        this.client.dispose();
    }

    public Single<Result<NFProfile>> deregisterNfInstance(final UUID nfInstanceId)
    {
        final URL url = createUrl(this.url, ServiceName.NNRF_NFM, nfInstanceId.toString(), "");
        final String urlStr = url.toString();
        final String urlPath = url.getPath();

        log.debug("Request: DELETE {}", urlStr);

        return this.client.requestAbs(HttpMethod.DELETE, this.url.getAddr(), urlStr)//
                          .rxSend()
                          .map(resp ->
                          {
                              final String body = resp.bodyAsString();
                              log.debug("DELETE response: {} {}", resp.statusCode(), body);

                              stepCcInAns(this.nrf, ServiceName.NNRF_NFM, "DELETE", urlPath, resp.statusCode());

                              switch (resp.statusCode())
                              {
                                  case 200: // Treat the same as 204, ignore body
                                  case 204:
                                      return new Result<NFProfile>(resp);

                                  default:
                                  {
                                      final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                      throw new RuntimeException(status + ": unsuccessful response received: '" + body + "'.");
                                  }
                              }
                          })
                          .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, "DELETE", urlPath));
    }

    public Single<Result<NFProfile>> getNfInstance(final String nfInstanceId)
    {
        final URL url = createUrl(this.url, ServiceName.NNRF_NFM, nfInstanceId.toString(), "");
        final String urlStr = url.toString();
        final String urlPath = url.getPath();

        log.debug("Request: GET {}", urlStr);

        return this.client.requestAbs(HttpMethod.GET, this.url.getAddr(), urlStr)//
                          .rxSend()
                          .map(resp ->
                          {
                              final String body = resp.bodyAsString();
                              log.debug("GET response: {} {}", resp.statusCode(), body);

                              stepCcInAns(this.nrf, ServiceName.NNRF_NFM, "GET", urlPath, resp.statusCode());

                              switch (resp.statusCode())
                              {
                                  case 200:
                                  {
                                      validateResponse(resp, "application/json", body);

                                      return new Result<>(resp, json.readValue(body, NFProfile.class));
                                  }

                                  default:
                                  {
                                      final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                      throw new RuntimeException(status + ": unsuccessful response received: '" + body + "'.");
                                  }
                              }
                          })
                          .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, "GET", urlPath))
                          .onErrorReturn(t -> new Result<NFProfile>(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
    }

    public Single<Result<Links>> getNfInstances(final String nfType,
                                                final Integer limit)
    {
        final URL url = createUrl(this.url, ServiceName.NNRF_NFM, "", new Query.Builder().add("nf-type", nfType).add("limit", limit).build().toString());
        final String urlStr = url.toString();
        final String urlPath = url.getPath();

        log.debug("Request: GET {}", urlStr);

        return this.client.requestAbs(HttpMethod.GET, this.url.getAddr(), urlStr)//
                          .rxSend()
                          .map(resp ->
                          {
                              final String body = resp.bodyAsString();
                              log.debug("GET response: {} {}", resp.statusCode(), body);

                              stepCcInAns(this.nrf, ServiceName.NNRF_NFM, "GET", urlPath, resp.statusCode());

                              switch (resp.statusCode())
                              {
                                  case 200:
                                  {
                                      validateResponse(resp, "application/json", body);

                                      final JsonObject bodyAsJsonObject = resp.bodyAsJsonObject();

                                      if (bodyAsJsonObject.getJsonObject("_links") == null)
                                          throw new ProtocolViolationException(resp.statusCode() + ": invalid response. Cause: no '_links' in body.");

                                      return new Result<>(resp, json.readValue(bodyAsJsonObject.getJsonObject("_links").toString(), Links.class));
                                  }

                                  default:
                                  {
                                      final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                      throw new RuntimeException(status + ": unsuccessful response received: '" + resp.bodyAsString() + "'.");
                                  }
                              }
                          })
                          .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, "GET", urlPath))
                          .onErrorReturn(t -> new Result<Links>(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
    }

    public Single<Result<NFProfile>> registerNfInstance(final UUID nfInstanceId,
                                                        final NFProfile nfProfile)
    {
        try
        {
            final URL url = createUrl(this.url, ServiceName.NNRF_NFM, nfInstanceId.toString(), "");
            final String urlStr = url.toString();
            final String urlPath = url.getPath();

            log.debug("Request: PUT {}", urlStr);

            final String nfProfileAsJsonStr = json.writeValueAsString(nfProfile);

            log.debug("nfProfile={}", nfProfileAsJsonStr);

            return this.client.requestAbs(HttpMethod.PUT, this.url.getAddr(), urlStr) //
                              .rxSendJsonObject(new JsonObject(nfProfileAsJsonStr))
                              .map(resp ->
                              {
                                  final String body = resp.bodyAsString();
                                  log.debug("PUT response: {} {}", resp.statusCode(), body);

                                  stepCcInAns(this.nrf, ServiceName.NNRF_NFM, "PUT", urlPath, resp.statusCode());

                                  switch (resp.statusCode())
                                  {
                                      case 200:
                                      case 201:
                                      {
                                          validateResponse(resp, "application/json", body);

                                          return new Result<NFProfile>(resp, json.readValue(body, NFProfile.class));
                                      }

                                      default:
                                      {
                                          final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                          throw new RuntimeException(status + ": unsuccessful response received: '" + body + "'.");
                                      }
                                  }
                              })
                              .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, "PUT", urlPath));
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Single<Result<SearchResult>> searchNfInstances(final String query,
                                                          final String targetApiRoot)
    {
        final URL url = createUrl(this.url, ServiceName.NNRF_DISC, "", query);
        final String urlStr = url.toString();
        final String urlPath = url.getPath();

        log.debug("Request: GET {}", urlStr);

        final HttpRequest<Buffer> request = this.client.requestAbs(HttpMethod.GET, this.url.getAddr(), urlStr);

        if (targetApiRoot != null && !targetApiRoot.isEmpty())
            request.putHeader("target-apiroot", targetApiRoot);

        return request.rxSend()//
                      .map(resp ->
                      {
                          final String body = resp.bodyAsString();
                          log.debug("GET response: {} {}", resp.statusCode(), body);

                          stepCcInAns(this.nrf, ServiceName.NNRF_DISC, "GET", urlPath, resp.statusCode());

                          if (resp.statusCode() == 200)
                          {
                              validateResponse(resp, "application/json", body);

                              return new Result<>(resp, json.readValue(body, SearchResult.class));
                          }
                          else if (resp.statusCode() < 300)
                          {
                              throw new ProtocolViolationException(resp.statusCode() + ": invalid response. Cause: unexpected status-code.");
                          }
                          else
                          {
                              return new Result<SearchResult>(resp);
                          }
                      })
                      .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_DISC, "GET", urlPath));
    }

    public Single<Result<NFProfile>> updateNfInstance(final UUID nfInstanceId,
                                                      final PatchItem... nfProfilePatch)
    {
        try
        {
            final URL url = createUrl(this.url, ServiceName.NNRF_NFM, nfInstanceId.toString(), "");
            final String urlStr = url.toString();
            final String urlPath = url.getPath();

            log.debug("Request: PATCH {}", urlStr);

            final String nfProfilePatchAsJsonStr = json.writeValueAsString(nfProfilePatch);

            log.debug("nfProfilePatch={}", nfProfilePatchAsJsonStr);

            final JsonArray nfProfilePatchAsJson = new JsonArray(nfProfilePatchAsJsonStr);

            return this.client.requestAbs(HttpMethod.PATCH, this.url.getAddr(), urlStr)//
                              .putHeader("Content-Type", "application/json-patch+json")
                              .rxSendJson(nfProfilePatchAsJson)
                              .map(resp ->
                              {
                                  final String body = resp.bodyAsString();
                                  log.debug("PATCH response: {} {}", resp.statusCode(), body);

                                  stepCcInAns(this.nrf, ServiceName.NNRF_NFM, "PATCH", urlPath, resp.statusCode());

                                  switch (resp.statusCode())
                                  {
                                      case 200:
                                      {
                                          validateResponse(resp, "application/json", body);

                                          return new Result<NFProfile>(resp, json.readValue(body, NFProfile.class));
                                      }

                                      case 204:
                                      case 404: // Allow the user to invoke registerNfInstance
                                          return new Result<NFProfile>(resp);

                                      default:
                                      {
                                          final String status = HttpResponseStatus.valueOf(resp.statusCode()).toString();
                                          throw new RuntimeException(status + ": unsuccessful response received: '" + body + "'.");
                                      }
                                  }
                              })
                              .doOnSubscribe(d -> stepCcOutReq(this.nrf, ServiceName.NNRF_NFM, "PATCH", urlPath));
        }
        catch (final Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
