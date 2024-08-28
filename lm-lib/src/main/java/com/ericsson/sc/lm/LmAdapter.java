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
 * Created on: Jan 20, 2020
 *     Author: evouioa
 */

package com.ericsson.sc.lm;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.lm.LicenseContext.LicenseKey;
import com.ericsson.sc.lm.model.lr.GetLicenseResponse;
import com.ericsson.sc.lm.model.lr.LicenseRequestsResponse;
import com.ericsson.adpal.pm.PmAdapter;
import com.ericsson.adpal.pm.PmAdapter.Query.Response.Data;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;

/**
 * 
 */
public class LmAdapter
{

    private static final Logger log = LoggerFactory.getLogger(LmAdapter.class);
    ObjectMapper objectMapper = Jackson.om();
    private static final String RETRY_AFTER = "Retry-After";
    private final WebClientProvider lmConsumerClient;
    Vertx vertx;

    private static final int REQUEST_TIMEOUT_MILLIS = 5000;

    private static final String HTML_CONTENT_TYPE = "content-type";
    private static final String HTML_APPLICATION = "application/json";
    private static final String CAPACITY_KEY_ID = "capacityKeyId";
    private static final String TYPE = "type";
    private static final String USAGE = "usage";
    private static final String PRODUCT_TYPE = "productType";
    private static final String LICENSE_KEY_ID = "keyId";
    private static final String LICENSE_TYPE = "type";
    private static final String CONSUMER_ID = "consumerId";
    private static final String REPORTS = "reports";
    private static final String LICENSES = "licenses";

    private final String lmHost;
    private final int lmPort;
    private final PmAdapter pmAdapter;
    private final LicenseContext context;

    private static final URI lmConsumerClientCertsUri = URI.create("/run/secrets/lm/certificates");
    private static final URI sipTlsTrustedCaUri = URI.create("/run/secrets/siptls/ca");

    public LmAdapter(Vertx vertx,
                     PmAdapter pmAdapter,
                     String lmHost,
                     int lmPort,
                     LicenseContext context,
                     final String serviceName,
                     final boolean tlsEnabled)
    {
        this.lmHost = lmHost;
        this.lmPort = lmPort;
        this.pmAdapter = pmAdapter;
        this.vertx = vertx;
        this.context = context;

        // create client for fault indications to alarm handler
        final var tmpClient = WebClientProvider.builder().withHostName(serviceName);
        if (tlsEnabled)
            tmpClient.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(lmConsumerClientCertsUri.getPath()), //
                                                                  SipTlsCertWatch.trustedCert(sipTlsTrustedCaUri.getPath())));
        this.lmConsumerClient = tmpClient.build(vertx);
    }

    private <T> Single<LmResponse<?>> post(Request request,
                                           Class<T> responseClass)
    {
        return this.lmConsumerClient.getWebClient()
                                    .flatMap(client -> client.post(this.lmPort, this.lmHost, request.getURI())
                                                             .putHeader(HTML_CONTENT_TYPE, HTML_APPLICATION)
                                                             .rxSendJsonObject(request.getBody())
                                                             .timeout(REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                                                             .retry(1)
                                                             .doOnSubscribe(sub -> log.debug("Sending POST request:\n\t{}:{}\n\tURI:{}\n\tbody:{}",
                                                                                             this.lmHost,
                                                                                             this.lmPort,
                                                                                             request.uri,
                                                                                             request.body))
                                                             .map(response ->
                                                             {
                                                                 // DND 59947: add extra case for SERVICE_UNAVAILABLE
                                                                 if (response.statusCode() == HttpResponseStatus.OK.code())
                                                                     return new LmResponse<>(response.statusCode(),
                                                                                             objectMapper.readValue(response.bodyAsString(), responseClass),
                                                                                             "");
                                                                 if (response.statusCode() == HttpResponseStatus.SERVICE_UNAVAILABLE.code())
                                                                     return new LmResponse<>(response.statusCode(),
                                                                                             response.bodyAsString(),
                                                                                             response.getHeader(RETRY_AFTER));

                                                                 return new LmResponse<>(response.statusCode(), response.bodyAsString(), "");

                                                             })
                                                             .doOnError(e -> log.error("Error in POST request: {}", e.toString())));
    }

    private Single<LmResponse<String>> post(Request request)
    {
        return this.lmConsumerClient.getWebClient()
                                    .flatMap(client -> client.post(this.lmPort, this.lmHost, request.getURI())
                                                             .putHeader(HTML_CONTENT_TYPE, HTML_APPLICATION)
                                                             .rxSendJsonObject(request.getBody())
                                                             .timeout(REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                                                             .retry(1)
                                                             .doOnSubscribe(sub -> log.debug("Sending POST request:\n\t{}:{}\n\tURI:{}\n\tbody:{}",
                                                                                             this.lmHost,
                                                                                             this.lmPort,
                                                                                             request.uri,
                                                                                             request.body))
                                                             .map(response ->
                                                             {
                                                                 if (response.statusCode() == HttpResponseStatus.SERVICE_UNAVAILABLE.code())
                                                                     return new LmResponse<>(response.statusCode(),
                                                                                             response.bodyAsString(),
                                                                                             response.getHeader(RETRY_AFTER));

                                                                 return new LmResponse<>(response.statusCode(), response.bodyAsString(), "");

                                                             })
                                                             .doOnError(e -> log.error("Error in POST request: {}", e.toString())));
    }

    private Single<GetLicenseResponse> get(Request request)
    {
        return this.lmConsumerClient.getWebClient()
                                    .flatMap(client -> client.get(this.lmPort, this.lmHost, request.getURI())
                                                             .putHeader(HTML_CONTENT_TYPE, HTML_APPLICATION)
                                                             .rxSendJsonObject(request.getBody())
                                                             .timeout(REQUEST_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                                                             .doOnSubscribe(sub -> log.debug("Sending GET request: {}:{}{} {}",
                                                                                             this.lmHost,
                                                                                             this.lmPort,
                                                                                             request.uri,
                                                                                             request.body))
                                                             .map(response -> objectMapper.readValue(response.bodyAsString(), GetLicenseResponse.class))
                                                             .doOnError(e -> log.error("An error has occured: {}", e.toString())));
    }

    public Single<LmResponse<?>> requestLicenses()
    {
        var jsonObj = new JsonObject().put(PRODUCT_TYPE, this.context.getProductType());
        var jsonArray = new JsonArray();

        for (var li : this.context.getLicenseKeys())
        {
            jsonArray.add(new JsonObject().put(LICENSE_KEY_ID, li.getId()).put(LICENSE_TYPE, li.getLicenseType()));
        }

        final var request = new Request(QueryUri.POST_LICENSES, jsonObj.put(LICENSES, jsonArray));

        return this.post(request, LicenseRequestsResponse.class);
    }

    public Maybe<LmResponse<String>> reportUsage(String licenseKey)
    {
        final var licenseKeyEntry = this.context.find(licenseKey);

        return pmAdapter.post(licenseKeyEntry.getMetric())
                        .map(result -> new UsageReport(licenseKeyEntry, result))
                        .doOnError(e -> log.error("An error occured when getting pm counter: {}", e.toString()))
                        .filter(result -> result.getUsage().isPresent())
                        .map(usageResult ->
                        {
                            var usageValue = 0;
                            var res = usageResult.getUsage().get().getResult();

                            if (!res.isEmpty() && !res.get(0).getValue().isEmpty())
                            {
                                usageValue = res.get(0).getValue().get(1).intValue();
                            }

                            log.info("Usage Report = { License key: {}, Capacity: {} }", usageResult.getLicenseInfo().getId(), usageValue);

                            return new Request(QueryUri.USAGE_REPORT,
                                               new JsonObject().put(PRODUCT_TYPE, this.context.getProductType())
                                                               .put(CONSUMER_ID, this.context.getConsumerId())
                                                               .put(REPORTS,
                                                                    new JsonArray().add(new JsonObject().put(CAPACITY_KEY_ID,
                                                                                                             usageResult.getLicenseInfo().getId())
                                                                                                        .put(TYPE,
                                                                                                             usageResult.getLicenseInfo()
                                                                                                                        .getCapacityLicenseType())
                                                                                                        .put(USAGE, usageValue))));

                        })
                        .flatMap(request -> this.post(request).toMaybe());

    }

    public Single<GetLicenseResponse> getLicenses()
    {
        final var request = new Request(QueryUri.GET_LICENSES, new JsonObject().put(PRODUCT_TYPE, this.context.getProductType()));
        return this.get(request);
    }

    public enum QueryUri
    {
        USAGE_REPORT("/license-manager/api/v1/licenses/usage-reports"),
        POST_LICENSES("/license-manager/api/v1/licenses/requests"),
        GET_LICENSES("/license-manager/api/v1/licenses");

        private String uri;

        private QueryUri(String uri)
        {
            this.uri = uri;
        }

        @Override
        public String toString()
        {
            return this.uri;
        }
    }

    private class UsageReport
    {
        private final LicenseKey licenseInfo;
        private final Optional<Data> usage;

        public UsageReport(LicenseKey licenseInfo,
                           Optional<Data> pmResult)
        {
            this.usage = pmResult;
            this.licenseInfo = licenseInfo;
        }

        public LicenseKey getLicenseInfo()
        {
            return this.licenseInfo;
        }

        public Optional<Data> getUsage()
        {
            return this.usage;
        }

    }

    private class Request
    {
        private final QueryUri uri;
        private final JsonObject body;

        /**
         * @param postLicensesUri
         * @param put
         */
        public Request(QueryUri uri,
                       JsonObject body)
        {
            this.uri = uri;
            this.body = body;
        }

        public String getURI()
        {
            return this.uri.toString();
        }

        public JsonObject getBody()
        {
            return this.body;
        }
    }

    public static class LmResponse<T>
    {
        private final HttpResponseStatus statusCode;
        private T response;
        private String header;

        public LmResponse(int statusCode,
                          T response,
                          String header)
        {
            this.statusCode = HttpResponseStatus.valueOf(statusCode);
            this.response = response;
            this.header = header;
        }

        /**
         * @return the statusCode
         */
        public HttpResponseStatus getStatusCode()
        {
            return statusCode;
        }

        /**
         * @return the response
         */
        public T getResponse()
        {
            return response;
        }

        public String getHeader()
        {
            return header;
        }

        public String toString()
        {
            return "\tStatus code: " + this.statusCode + "\n\tResponse: " + this.response + "\n\tHeader: " + this.header;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (o == null || this.getClass() != o.getClass())
                return false;

            @SuppressWarnings("unchecked")
            LmResponse<T> response = (LmResponse<T>) o;

            return Objects.equals(this.getStatusCode(), response.getStatusCode()) && Objects.equals(this.getResponse(), response.getResponse())
                   && Objects.equals(this.getHeader(), response.getHeader());
        }
    }
}