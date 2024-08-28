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
 * Created on: Nov 16, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.server;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.esc.bsf.load.server.BsfLoadService.ResultStatus;
import com.ericsson.esc.lib.ContentTypes;
import com.ericsson.esc.lib.OpenApiHandler;
import com.ericsson.esc.lib.OpenApiOp;
import com.ericsson.esc.lib.OpenApiReq;
import com.ericsson.utilities.http.WebServer;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.vertx.core.http.HttpHeaders;
import io.vertx.reactivex.core.buffer.Buffer;

/**
 * Establishes the request routing to the appropriate handler.
 */
public class BsfLoadServiceHandler
{
    private static final Logger log = LoggerFactory.getLogger(BsfLoadServiceHandler.class);
    private static final JsonMapper jm = JsonMapper.builder().serializationInclusion(Include.NON_NULL).build();

    private static final URL SPEC_URL = BsfLoadServiceHandler.class.getClassLoader().getResource("bsf-load-api.yaml");
    private static final String MOUNT_PATH = "/bsf-load";
    private final OpenApiHandler handler;
    private final BsfLoadService service;

    public BsfLoadServiceHandler(BsfLoadService service,
                                 WebServer webServer)
    {
        this.service = service;
        this.handler = OpenApiHandler.builder()
                                     .setRouter(webServer)
                                     .setSpecUrl(SPEC_URL)
                                     .setMountPoint(MOUNT_PATH)
                                     .build()
                                     .addRequestHandler(new OpenApiOp<>("executeRun", ConfigurationParamExtractor::new),
                                                        this::executeRunHandler,
                                                        ErrorHandler::genericErrorHandler)
                                     .addRequestHandler(new OpenApiOp<>("getRunAll", EmptyParamExtractor::new),
                                                        this::getRunAllHandler,
                                                        ErrorHandler::genericErrorHandler)
                                     .addRequestHandler(new OpenApiOp<>("getRun", RunIdParamExtractor::new),
                                                        this::getRunHandler,
                                                        ErrorHandler::genericErrorHandler)
                                     .addRequestHandler(new OpenApiOp<>("deleteRunAll", EmptyParamExtractor::new),
                                                        this::deleteRunAllHandler,
                                                        ErrorHandler::genericErrorHandler)
                                     .addRequestHandler(new OpenApiOp<>("deleteRun", RunIdParamExtractor::new),
                                                        this::deleteRunHandler,
                                                        ErrorHandler::genericErrorHandler)
                                     .addRequestHandler(new OpenApiOp<>("terminateRun", RunIdParamExtractor::new),
                                                        this::terminateRunHandler,
                                                        ErrorHandler::genericErrorHandler);
    }

    public Completable start()
    {
        return this.handler.start();
    }

    public Completable stop()
    {
        return this.handler.stop();
    }

    private Completable executeRunHandler(OpenApiReq<ConfigurationParamExtractor> req)
    {
        final var params = req.getParams();
        final var routingContext = req.getRoutingContext();
        final var requestUri = parseUri(routingContext.request().absoluteURI());

        log.info("Received 'execute-run' request for configuration: {}", params.getConfiguration());

        return service.executeWorkLoad(params.getConfiguration()).flatMapCompletable(result ->
        {
            switch (result.getStatus())
            {
                case INVALID_CONFIGURATION:
                    return ErrorHandler.sendErrorResponse(routingContext,
                                                          HttpResponseStatus.BAD_REQUEST.code(),
                                                          ProblemDetails.withInvalidParam("Invalid configuration",
                                                                                          "The provided configuration is invalid",
                                                                                          result.getInvalidParams()));

                case SERVICE_BUSY:
                    return ErrorHandler.sendErrorResponse(routingContext,
                                                          HttpResponseStatus.SERVICE_UNAVAILABLE.code(),
                                                          ProblemDetails.withDetail("Server busy",
                                                                                    "The server is currently occupied with executing another workload"));

                case WORKLOAD_CREATED:
                    final var entries = result.getWorkLoadEntries();
                    final var entry = entries.get(0);
                    log.info("Workload created for runId: {}, configuration: {}", entry.getRunId(), entry.getConfiguration());

                    routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.CONTENT_TYPE_JSON);
                    routingContext.response().putHeader(HttpHeaders.LOCATION, requestUri.resolve(entry.getRunId().toString()).toASCIIString());
                    routingContext.response().setStatusCode(HttpResponseStatus.CREATED.code());
                    final var entriesBytes = jm.writeValueAsBytes(entries);
                    return routingContext.response().rxEnd(Buffer.buffer(entriesBytes));

                default:
                    return ErrorHandler.sendInternalServerErrorResponse(routingContext);
            }
        });
    }

    private Completable getRunAllHandler(OpenApiReq<EmptyParamExtractor> req)
    {
        final var routingContext = req.getRoutingContext();

        log.info("Received 'get-run-all' request");

        return service.getWorkLoads().flatMapCompletable(result ->
        {
            if (result.getStatus().equals(ResultStatus.WORKLOAD_FETCHED))
            {
                log.info("Fetched all workloads: {}", result);

                routingContext.response().setStatusCode(HttpResponseStatus.OK.code());
                routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.CONTENT_TYPE_JSON);
                final var workloadsBytes = jm.writeValueAsBytes(result.getWorkLoadEntries());
                return routingContext.response().rxEnd(Buffer.buffer(workloadsBytes));
            }
            else
            {
                return ErrorHandler.sendInternalServerErrorResponse(routingContext);
            }
        });
    }

    private Completable getRunHandler(OpenApiReq<RunIdParamExtractor> req)
    {
        final var params = req.getParams();
        final var routingContext = req.getRoutingContext();

        log.info("Received 'get-run' request for runId: {}", params.getRunId());

        return service.getWorkLoad(params.getRunId()).flatMapCompletable(result ->
        {
            switch (result.getStatus())
            {
                case NOT_FOUND:
                    return ErrorHandler.sendNotFoundErrorResponse(routingContext);

                case WORKLOAD_FETCHED:
                    final var entries = result.getWorkLoadEntries();
                    final var entry = entries.get(0);
                    log.info("Workload fetched for runId: {}, configuration: {}", entry.getRunId(), entry.getConfiguration());

                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code());
                    routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, ContentTypes.CONTENT_TYPE_JSON);
                    final var entriesBytes = jm.writeValueAsBytes(entries);
                    return routingContext.response().rxEnd(Buffer.buffer(entriesBytes));

                default:
                    return ErrorHandler.sendInternalServerErrorResponse(routingContext);
            }
        });
    }

    private Completable deleteRunAllHandler(OpenApiReq<EmptyParamExtractor> req)
    {
        final var routingContext = req.getRoutingContext();

        log.info("Received 'delete-run-all' request");

        return service.deleteWorkLoads().flatMapCompletable(result ->
        {
            if (result.getStatus().equals(ResultStatus.WORKLOAD_DELETED))
            {
                log.info("Deleted all workloads that are not in 'RUNNING' state");

                routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                return routingContext.response().rxEnd(Buffer.buffer());
            }
            else
            {
                return ErrorHandler.sendInternalServerErrorResponse(routingContext);
            }
        });
    }

    private Completable deleteRunHandler(OpenApiReq<RunIdParamExtractor> req)
    {
        final var params = req.getParams();
        final var routingContext = req.getRoutingContext();

        log.info("Received 'delete-run' request for runId: {}", params.getRunId());

        return service.deleteWorkLoad(params.getRunId()).flatMapCompletable(result ->
        {
            switch (result.getStatus())
            {
                case NOT_FOUND:
                    return ErrorHandler.sendNotFoundErrorResponse(routingContext);

                case INVALID_OPERATION:
                    return ErrorHandler.sendConflictErrorResponse(routingContext);

                case WORKLOAD_DELETED:
                    log.info("Workload deleted for runId: {}", params.getRunId());

                    routingContext.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code());
                    return routingContext.response().rxEnd(Buffer.buffer());

                default:
                    return ErrorHandler.sendInternalServerErrorResponse(routingContext);
            }
        });
    }

    private Completable terminateRunHandler(OpenApiReq<RunIdParamExtractor> req)
    {
        final var params = req.getParams();
        final var routingContext = req.getRoutingContext();

        log.info("Received 'terminate-run' request for runId: {}", params.getRunId());

        return service.terminateWorkLoad(params.getRunId()).flatMapCompletable(result ->
        {
            switch (result.getStatus())
            {
                case NOT_FOUND:
                    return ErrorHandler.sendNotFoundErrorResponse(routingContext);

                case INVALID_OPERATION:
                    return ErrorHandler.sendConflictErrorResponse(routingContext);

                case WORKLOAD_TERMINATED:
                    log.info("Workload terminated for runId: {}", params.getRunId());

                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code());
                    return routingContext.response().rxEnd(Buffer.buffer());

                default:
                    return ErrorHandler.sendInternalServerErrorResponse(routingContext);
            }
        });
    }

    /**
     * Normalize the URI of the incoming request.
     * 
     * @param uri A String containing a valid URI.
     * @return The same uri, possibly with a slash appended on its path.
     * @throws IllegalArgumentException when input URI is invalid.
     */
    private URI parseUri(String uri)
    {
        final var parsedUri = URI.create(uri);
        final var path = parsedUri.getPath();
        try
        {
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
            throw new IllegalArgumentException("Failed to sanitize URI: " + uri, e);
        }
    }
}
