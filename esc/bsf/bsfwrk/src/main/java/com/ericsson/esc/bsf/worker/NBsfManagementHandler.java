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
 * Created on: Apr 1, 2020
 *     Author: echfari
 */
package com.ericsson.esc.bsf.worker;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.RequestThrottlingException;
import com.datastax.oss.driver.api.core.servererrors.UnauthorizedException;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.openapi.model.SupportedFeatures;
import com.ericsson.esc.bsf.worker.AuthAccessTokenValidator.ErrorType;
import com.ericsson.esc.bsf.worker.NBsfManagementService.DeregisterResult;
import com.ericsson.esc.bsf.worker.NBsfManagementService.DiscoveryResult;
import com.ericsson.esc.bsf.worker.NBsfManagementService.RegisterResult;
import com.ericsson.esc.lib.CommonError;
import com.ericsson.esc.lib.CommonErrorHandler;
import com.ericsson.esc.lib.ContentTypes;
import com.ericsson.esc.lib.DbMissingPermissionsException;
import com.ericsson.esc.lib.OpenApiHandler;
import com.ericsson.esc.lib.OpenApiOp;
import com.ericsson.esc.lib.OpenApiReq;
import com.ericsson.esc.lib.TooManyRequestsException;
import com.ericsson.utilities.http.WebServerRouter;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.AsyncCache;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.prometheus.client.Counter;
import io.prometheus.client.Counter.Child;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.vertx.core.http.HttpHeaders;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * Implements the 3gpp nbsf-management service
 */
public class NBsfManagementHandler
{
    private static final Logger log = LoggerFactory.getLogger(NBsfManagementHandler.class);
    private static final ObjectMapper om = Jackson.om();
    private static final String API_ROOT = "/nbsf-management/v1";
    private static final URL SPEC_URL = NBsfManagementHandler.class.getClassLoader().getResource("specs/3gpp/TS29521_Nbsf_Management.yaml");
    private final Flowable<BsfCmConfig> bsfConfig;
    private final Counters counters = new Counters();
    private final NBsfManagementService nbsfManagementService;
    private final BindingCleanupManager bindingCleanup;
    private final OpenApiHandler handler;
    private final SupportedFeatures bsfSuppFeat = new SupportedFeatures("1"); // BSF 1.4 will only support the MultiUeAddr optional feature
    private final boolean tls;
    private AsyncCache<String, String> tokenCache;

    public NBsfManagementHandler(WebServerRouter webServerRouter,
                                 NBsfManagementService nbsfManagementService,
                                 BindingCleanupManager bindingCleanup,
                                 Observable<BsfCmConfig> bsfConfig,
                                 boolean tls,
                                 Path tempDirectoryPath,
                                 AsyncCache<String, String> tokenCache)
    {
        this.nbsfManagementService = nbsfManagementService;
        this.bsfConfig = bsfConfig.toFlowable(BackpressureStrategy.LATEST);
        this.bindingCleanup = bindingCleanup;
        this.tls = tls;
        this.tokenCache = tokenCache;
        this.handler = OpenApiHandler //
                                     .builder()
                                     .setRouter(webServerRouter)
                                     .setSpecUrl(SPEC_URL)
                                     .setMountPoint(API_ROOT)
                                     .setDirectoryTempPath(tempDirectoryPath)
                                     .build()
                                     .addRequestHandler(new OpenApiOp<>("getPcfBindings", GetBindingsParams::new),
                                                        this::getPcfBindings,
                                                        this.bsfConfig,
                                                        this::errorHandler)
                                     .addRequestHandler(new OpenApiOp<>("postPcfBindings", PostBindingsParams::new),
                                                        this::postPcfBindings,
                                                        this.bsfConfig,
                                                        this::errorHandler)
                                     .addRequestHandler(new OpenApiOp<>("deletePcfBindings", DeleteBindingsParams::new),
                                                        this::deletePcfBindings,
                                                        this.bsfConfig,
                                                        this::errorHandler);

    }

    public NBsfManagementHandler(WebServerRouter webServerRouter,
                                 NBsfManagementService nbsfManagementService,
                                 BindingCleanupManager bindingCleanup,
                                 Observable<BsfCmConfig> bsfConfig,
                                 boolean tls,
                                 AsyncCache<String, String> tokenCache)
    {
        this(webServerRouter, nbsfManagementService, bindingCleanup, bsfConfig, tls, null, tokenCache);

    }

    public Counters getCounters()
    {
        return this.counters;
    }

    public Completable start()
    {
        return this.handler.start();
    }

    public Completable stop()
    {
        return this.handler.stop()//
                           .andThen(this.bindingCleanup.stop());
    }

    public Completable stopExceptCleanup()
    {
        return this.handler.stop();
    }

    public Completable run()
    {
        return this.handler.run();
    }

    private Completable errorHandler(RoutingContext rc,
                                     Throwable t)
    {
        log.debug("Handling unexpected exception {}", t.getClass());
        if (t instanceof RequestThrottlingException)
        {
            t = new TooManyRequestsException("Throttling due to Database Overload");
        }
        if (t instanceof UnauthorizedException)
        {
            t = new DbMissingPermissionsException("Missing permissions in BSF database keyspace");
        }

        final var err = t;

        return Completable.defer(() -> this.bsfConfig.firstOrError().flatMapCompletable(cfg ->
        {
            final var rctx = cfg.isOutMessageHandling() ? editHeaders(rc, cfg.getNfInstanceId()) : rc;

            return CommonErrorHandler.sendErrorResponse(rctx, err).onErrorComplete().doOnComplete(() -> stepTrafficCounters(rctx, cfg));
        }));

    }

    private void stepTrafficCounters(RoutingContext rc,
                                     BsfCmConfig bsfConfig)
    {
        final var httpMethod = rc.request().method().name();
        final var nfInstanceName = bsfConfig.getNfInstanceName();
        final var status = rc.response().getStatusCode() + " " + rc.response().getStatusMessage();

        log.debug("Stepping traffic counters, method: {} , nfInstanceName: {}, status: {}", httpMethod, nfInstanceName, status);

        counters.getCcInReq(httpMethod, // HTTP method
                            bsfConfig.getNfInstanceName())
                .inc();
        counters.getCcOutAns(httpMethod, // HTTP method
                             status, // status
                             nfInstanceName)
                .inc();
    }

    private void stepMultipleBindingsCounter(String resolution,
                                             BsfCmConfig bsfConfig)
    {
        final var nfInstanceName = bsfConfig.getNfInstanceName();

        log.debug("Stepping multiple bindings counter, resolution: {}, nfInstanceName: {}", resolution, nfInstanceName);

        counters.getCcMultipleBindings(resolution, nfInstanceName).inc();
    }

    private Completable getPcfBindings(OpenApiReq<GetBindingsParams> req,
                                       BsfCmConfig bsfConfig)
    {
        final var params = req.getParams();
        final var rc = bsfConfig.isOutMessageHandling() ? editHeaders(req.getRoutingContext(), bsfConfig.getNfInstanceId()) : req.getRoutingContext();
        final var clientSuppFeat = params.getSuppFeat();
        final var isAuthEnabled = tls ? Boolean.TRUE.equals(bsfConfig.getOauth().getValue1()) : Boolean.TRUE.equals(bsfConfig.getOauth().getValue0());
        final var authorized = isAuthEnabled ? AuthAccessTokenValidator.validateToken(rc, bsfConfig, this.tokenCache)
                                             : new Pair<Boolean, Reason>(Boolean.TRUE, null);

        return authorized.getValue0().booleanValue() ? nbsfManagementService.discovery(params.getQuery())
                                                                            // transform binding, in discovery result, if exists and if query has supp-Feat
                                                                            // attribute.
                                                                            .map(result -> result.pcfBinding.isPresent()
                                                                                           && clientSuppFeat.isPresent() ? transformDiscoveryResult(result.pcfBinding.get(), clientSuppFeat.get()) : result)
                                                                            .flatMapCompletable(result -> discoveryResultActions(result, rc, bsfConfig))
                                                                            .doOnComplete(() -> stepTrafficCounters(req.getRoutingContext(), bsfConfig))
                                                     : authErrorResponse(rc, authorized.getValue1())
                                                                                                    .doOnComplete(() -> stepTrafficCounters(req.getRoutingContext(),
                                                                                                                                            bsfConfig));

    }

    private Completable postPcfBindings(OpenApiReq<PostBindingsParams> req,
                                        BsfCmConfig bsfConfig)
    {
        final var ctx = bsfConfig.isOutMessageHandling() ? editHeaders(req.getRoutingContext(), bsfConfig.getNfInstanceId()) : req.getRoutingContext();
        final var requestUri = parseUri(ctx);
        if (requestUri == null) // If the absoluteUri cannot be resolved from the RoutingContext(e.g. in ipv6
        // hosts without []) the handler must reply with 400 error response.
        {
            return parsingErrorResponse(ctx).doOnComplete(() -> stepTrafficCounters(req.getRoutingContext(), bsfConfig));
        }
        final var binding = req.getParams().getPcfBinding();
        final var ttlConfig = bsfConfig.getBindingTimeout();
        final var isAuthEnabled = tls ? Boolean.TRUE.equals(bsfConfig.getOauth().getValue1()) : Boolean.TRUE.equals(bsfConfig.getOauth().getValue0());

        final var authorized = isAuthEnabled ? AuthAccessTokenValidator.validateToken(ctx, bsfConfig, this.tokenCache)
                                             : new Pair<Boolean, Reason>(Boolean.TRUE, null);

        return authorized.getValue0().booleanValue() ? nbsfManagementService.register(binding, ttlConfig)
                                                                            // transform register result only if binding has the suppFeat attribute.
                                                                            .map(registerResult -> registerResult.getPcfBinding()
                                                                                                                 .getSuppFeat() == null ? registerResult
                                                                                                                                        : transformRegisterResult(registerResult))

                                                                            .flatMapCompletable(registerResult ->
                                                                            {
                                                                                ctx.response()
                                                                                   .putHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.CONTENT_TYPE_JSON);
                                                                                // bindingId is returned via the location response header
                                                                                ctx.response()
                                                                                   .putHeader(HttpHeaders.LOCATION,
                                                                                              requestUri.resolve(registerResult.getBindingId().toString())
                                                                                                        .toASCIIString());
                                                                                ctx.response().setStatusCode(HttpResponseStatus.CREATED.code());
                                                                                // Write the new binding to response
                                                                                final var bindingBytes = om.writeValueAsBytes(registerResult.getPcfBinding());
                                                                                return ctx.response().rxEnd(Buffer.buffer(bindingBytes));
                                                                            })
                                                                            .doOnComplete(() -> stepTrafficCounters(req.getRoutingContext(), bsfConfig))
                                                     : authErrorResponse(ctx, authorized.getValue1())
                                                                                                     .doOnComplete(() -> stepTrafficCounters(req.getRoutingContext(),
                                                                                                                                             bsfConfig));

    }

    private Completable deletePcfBindings(OpenApiReq<DeleteBindingsParams> req,
                                          BsfCmConfig bsfConfig)
    {
        final var ctx = bsfConfig.isOutMessageHandling() ? editHeaders(req.getRoutingContext(), bsfConfig.getNfInstanceId()) : req.getRoutingContext();
        final var isAuthEnabled = tls ? Boolean.TRUE.equals(bsfConfig.getOauth().getValue1()) : Boolean.TRUE.equals(bsfConfig.getOauth().getValue0());
        final var authorized = isAuthEnabled ? AuthAccessTokenValidator.validateToken(ctx, bsfConfig, this.tokenCache)
                                             : new Pair<Boolean, Reason>(Boolean.TRUE, null);

        return authorized.getValue0().booleanValue() ? nbsfManagementService.deregister(req.getParams().getBindingId()) //
                                                                            .flatMapCompletable(deregisterResult ->
                                                                            {
                                                                                final var statusCode = (deregisterResult == DeregisterResult.OK) ? HttpResponseStatus.NO_CONTENT
                                                                                                                                                 : HttpResponseStatus.NOT_FOUND;
                                                                                ctx.response().setStatusCode(statusCode.code());

                                                                                return ctx.response().rxEnd(Buffer.buffer());
                                                                            })
                                                                            .doOnComplete(() -> stepTrafficCounters(req.getRoutingContext(), bsfConfig))
                                                     : authErrorResponse(ctx, authorized.getValue1())
                                                                                                     .doOnComplete(() -> stepTrafficCounters(req.getRoutingContext(),
                                                                                                                                             bsfConfig));

    }

    private static Completable authErrorResponse(RoutingContext rc,
                                                 Reason reason)
    {
        final var requestUri = parseUri(rc);
        final var uri = requestUri == null ? rc.request().uri() : rc.request().absoluteURI(); // This check applies only for get and delete operations. In case
                                                                                              // of register request and requestUri==null the handler responds
                                                                                              // with 400 code.
        final var lastIndex = uri.lastIndexOf("/v1/");
        final var realm = uri.substring(0, lastIndex).concat("/v1/");
        final var resp = rc.response();

        resp.setStatusCode(HttpResponseStatus.UNAUTHORIZED.code());
        if (reason.errorType().equals(ErrorType.INSUFFICIENT_SCOPE.getProblemDetails()))
        {
            resp.setStatusCode(HttpResponseStatus.FORBIDDEN.code());
        }
        return CommonErrorHandler.sendErrorResponse(resp.putHeader("WWW-Authenticate", String.format(reason.headerMsg(), realm)),
                                                    Optional.of(reason.errorType()));

    }

    private static Completable parsingErrorResponse(RoutingContext rc)
    {
        final var resp = rc.response();
        resp.setStatusCode(HttpResponseStatus.BAD_REQUEST.code());
        return CommonErrorHandler.sendErrorResponse(resp, CommonError.INVALID_MSG_FORMAT);
    }

    private Completable discoveryResultActions(DiscoveryResult result,
                                               RoutingContext rc,
                                               BsfCmConfig bsfConfig)
    {
        // Check staleBindingsList. If not empty delete each binding.
        if (!result.getStaleBindings().isEmpty())
            result.getStaleBindings().forEach(this.bindingCleanup::deleteBindingAsync);

        if (result.getPcfBinding().isPresent())
        {
            // Binding found
            rc.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.CONTENT_TYPE_JSON);
            rc.response().setStatusCode(HttpResponseStatus.OK.code());

            // Check if MultipleBindingsCounter should be stepped
            if (result.getResult().equals(DiscoveryResult.Status.OK_MULTIPLE))
                stepMultipleBindingsCounter("resolved", bsfConfig);

            // Serialize binding to bytes and send asynchronously
            try
            {
                final var serializedBinding = om.writeValueAsBytes(result.getPcfBinding().get());
                return rc.response().rxEnd(Buffer.buffer(serializedBinding));
            }
            catch (JsonProcessingException e)
            {
                log.error("Unable to serialize binding with error: ", e);
                return CommonErrorHandler.sendErrorResponse(rc.response(), CommonError.UNSPECIFIED_NF_FAILURE);
            }
        }
        else
        {
            // Unique binding not found
            switch (result.getResult())
            {
                case REJECT_MULTIPLE:
                    stepMultipleBindingsCounter("rejected", bsfConfig);
                    return CommonErrorHandler.sendErrorResponse(rc.response(), CommonError.MULTIPLE_BINDING_INFO_FOUND);
                case TOO_MANY:
                    stepMultipleBindingsCounter("too_many", bsfConfig);
                    return CommonErrorHandler.sendErrorResponse(rc.response(), CommonError.TOO_MANY_BINDINGS_FOUND);
                case NOT_FOUND:
                    rc.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                    // DND-21914 rxEnd() causes memory leak
                    return rc.response().rxEnd(Buffer.buffer());
                default:
                    // This should never happen
                    log.error("Unexpected result code");
                    return CommonErrorHandler.sendErrorResponse(rc.response(), CommonError.UNSPECIFIED_NF_FAILURE);
            }
        }
    }

    /*
     * This implements peerInfo functionality for BSF.
     */
    RoutingContext editHeaders(RoutingContext ctx,
                               String nfInstanceId)
    {
        /*
         *
         * srcinst -> dstinst, srcservinst -> dstservinst, srcscp -> dstscp
         * 
         */
        final var reqPeerInfo = ctx.request().headers().get("3gpp-Sbi-NF-Peer-Info");

        List<String> respPeerInfoList = new ArrayList<>();
        respPeerInfoList.add("srcinst=" + nfInstanceId);

        if (reqPeerInfo != null)
        {
            final var infoList = Arrays.asList(reqPeerInfo.trim().split(";"));
            infoList.stream().map(this::generateRespPeerInfo).filter(output -> !output.equals("")).forEach(respPeerInfoList::add);
        }

        final var respPeerInfo = String.join(";", respPeerInfoList);
        ctx.response().putHeader("3gpp-Sbi-NF-Peer-Info", respPeerInfo);

        return ctx;
    }

    private String generateRespPeerInfo(String info)
    {
        final var splitedInfo = info.trim().split("=");
        if (splitedInfo.length >= 2)
        {
            final var peerType = splitedInfo[0];
            final var value = splitedInfo[1];
            switch (peerType)
            {
                case "srcinst":
                    return "dstinst=" + value;
                case "srcservinst":
                    return "dstservinst=" + value;
                case "srcscp":
                    return "dstscp=" + value;
                default:
                    return "";
            }

        }
        else
        {
            return "";
        }
    }

    /**
     * 
     * @param ctx The RoutingContext object
     * @return The same uri, possibly with a slash appended on its path, or null in
     *         case absoluteUri cannot be resolved
     * @throws IllegalArgumentException when input URI is invalid
     */
    private static URI parseUri(RoutingContext ctx)
    {
        try
        {
            final var uri = ctx.request().absoluteURI();
            final var parsedUri = URI.create(uri);
            final var path = parsedUri.getPath();
            return path.endsWith("/") ? parsedUri
                                      : new URI(parsedUri.getScheme(),
                                                parsedUri.getUserInfo(),
                                                parsedUri.getHost(),
                                                parsedUri.getPort(),
                                                path + "/",
                                                parsedUri.getQuery(),
                                                parsedUri.getFragment());
        }
        catch (URISyntaxException e)
        {
            // This should never happen
            throw new IllegalArgumentException("Failed to sanitize URI: " + ctx.request().absoluteURI(), e);
        }
        catch (NullPointerException e)
        {
            // In case absolute URI cannot be parsed and request.absoluteURI() throws a null
            // pointer exception
            log.debug("Failed to parse the absolute URI", e);
            return null;
        }
    }

    /**
     * 
     * @param registerResult The result of register procedure
     * @return A new register result with the common supported feature attribute
     *         inside binding and the same result id
     */
    private RegisterResult transformRegisterResult(RegisterResult registerResult)
    {
        final var suppFeatBinding = PcfBinding.create(registerResult.getPcfBinding(), registerResult.getPcfBinding().getSuppFeat().commonSuppFeat(bsfSuppFeat));

        return new RegisterResult(suppFeatBinding, registerResult.getBindingId());
    }

    /**
     * 
     * @param result         The result of discovery procedure
     * @param clientSuppFeat The client supported feature inside the discovery query
     * @return A new discovery result with the common supported feature attribute
     *         inside binding
     */
    private DiscoveryResult transformDiscoveryResult(DiscoveredPcfBinding resultBinding,
                                                     SupportedFeatures clientSuppFeat)
    {
        return DiscoveryResult.found(new DiscoveredPcfBinding(resultBinding, clientSuppFeat.commonSuppFeat(bsfSuppFeat)));
    }

    static class Counters
    {
        /**
         * Prometheus counter for incoming requests
         */
        private static final Counter ccInReq = io.prometheus.client.Counter.build()
                                                                           .namespace("bsf")
                                                                           .name("in_requests_total")
                                                                           .labelNames("method", "nf_instance", "nf", "service")
                                                                           .help("Number of incoming HTTP requests on the Nbsf_Management interface")
                                                                           .register();

        /**
         * Prometheus counter for outgoing answers
         */
        private static final Counter ccOutAns = io.prometheus.client.Counter.build()
                                                                            .namespace("bsf")
                                                                            .name("out_answers_total")
                                                                            .labelNames("method", "status", "nf_instance", "nf", "service")
                                                                            .help("Number of outgoing HTTP answers on the Nbsf_Management interface")
                                                                            .register();

        /**
         * Prometheus counter for multiple bindings resolution
         */
        private static final Counter ccMultipleBindings = io.prometheus.client.Counter.build()
                                                                                      .namespace("bsf")
                                                                                      .name("multiple_bindings_found_total")
                                                                                      .labelNames("resolution", "interface", "nf_instance", "nf")
                                                                                      .help("Number of times multiple bindings were retrieved")
                                                                                      .register();

        public Child getCcInReq(String httpMethod,
                                String nfInstance)
        {
            return ccInReq.labels(httpMethod, // method
                                  nfInstance, // nf_instance
                                  "bsf", // nf
                                  "nbsf-management"); // service
        }

        public Child getCcOutAns(String httpMethod,
                                 String status,
                                 String nfInstance)
        {
            return ccOutAns.labels(httpMethod, // method
                                   status, // status
                                   nfInstance, // nf_instance
                                   "bsf", // nf
                                   "nbsf-management"); // service
        }

        public Child getCcMultipleBindings(String resolution,
                                           String nfInstance)
        {
            return ccMultipleBindings.labels(resolution,    // resolution type
                                             "http_lookup", // interface
                                             nfInstance,    // nf_instance
                                             "bsf");        // nf
        }
    }

}
