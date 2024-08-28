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
 * Created on: Feb 7, 2019
 *     Author: eedstl
 */

package com.ericsson.sim.chf.r17;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.ext.monitor.MonitorAdapter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Counter;
//import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.common.WebClient;
import com.ericsson.cnal.openapi.r17.SbiNfPeerInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.InvalidParam;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PatchItem;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PatchOperation;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
import com.ericsson.cnal.openapi.r17.ts29594.nchf.spendinglimitcontrol.PolicyCounterInfo;
import com.ericsson.cnal.openapi.r17.ts29594.nchf.spendinglimitcontrol.SpendingLimitStatus;
import com.ericsson.cnal.openapi.r17.ts29594.nchf.spendinglimitcontrol.SubscriptionTerminationInfo;
import com.ericsson.cnal.openapi.r17.ts32291.nchf.convergedcharging.ChargingNotifyRequest;
import com.ericsson.cnal.openapi.r17.ts32291.nchf.convergedcharging.NotificationType;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.sim.chf.r17.NrfAdapter.Result;
import com.ericsson.sim.chf.r17.Statistics.ChargingInstance;
import com.ericsson.sim.chf.r17.Statistics.Nrf;
import com.ericsson.sim.chf.r17.Statistics.Subscription;
import com.ericsson.utilities.common.Count;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.file.KeyCert;
import com.ericsson.utilities.file.KeyCertProvider;
import com.ericsson.utilities.file.TrustedCert;
import com.ericsson.utilities.file.TrustedCertProvider;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.http.WebServerPool;
import com.ericsson.utilities.http.openapi.OpenApiServer.IpFamily;
import com.ericsson.utilities.http.openapi.OpenApiTask.DataIndex;
import com.ericsson.utilities.json.Json;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.ericsson.utilities.test.WeightedDisturbances;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.PemTrustOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.redis.client.Redis;
import io.vertx.reactivex.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;

/**
 * A simulator simulating the Charging Function in 5g.
 */
public class ChfSimulator implements Runnable, MonitorAdapter.CommandCounter.Provider, MonitorAdapter.CommandEsa.Provider
{
    public class Admin
    {
        public class Nrf
        {
            public Nrf()
            {
            }

            public Result<NFProfile> doNfInstanceDeregister()
            {
                return ChfSimulator.this.getNrfClient().nfInstanceDeregister().blockingGet();
            }

            public Result<NFProfile> doNfInstanceRegister()
            {
                return ChfSimulator.this.getNrfClient().nfInstanceRegister().blockingGet();
            }

            public Result<NFProfile> doNfInstanceUpdate()
            {
                return ChfSimulator.this.getNrfClient().nfInstanceUpdate().blockingGet();
            }

            public Nrf setAddr(final String host,
                               final Integer port)
            {
                return this.setAddr(null, host, port, null);
            }

            public Nrf setAddr(final String host,
                               final Integer port,
                               final String ip)
            {
                return this.setAddr(null, host, port, ip);
            }

            public Nrf setAddr(final String protocol,
                               final String host,
                               final Integer port,
                               final String ip)
            {
                this.handlerSetAddr((protocol != null ? protocol + "," : "") + host + "," + port + (ip != null ? "," + ip : ""));
                return this;
            }

            public Nrf setCapacity(final Integer capacity)
            {
                this.handlerSetNfCapacity(capacity);
                return this;
            }

            public Nrf setHeartbeat(Boolean on)
            {
                this.handlerSetHeartbeatOnOff(on);
                return this;
            }

            public Nrf setHeartbeatFullUpdateEveryNthTime(final Integer n)
            {
                this.handlerSetHeartbeatFullUpdateEveryNthTime(n);
                return this;
            }

            public Nrf setLoadInPercent(final Integer loadInPercent)
            {
                this.handlerSetNfLoadPercent(loadInPercent);
                return this;
            }

            public Nrf setPatch(final List<PatchItem> patch)
            {
                this.handlerSetNfPatch(patch);
                return this;
            }

            public Nrf setProfile(final NFProfile profile)
            {
                this.handlerSetNfProfile(profile);
                return this;
            }

            private Disposable handlerDoNfInstanceDeregister(final RoutingContext routingContext)
            {
                return ChfSimulator.this.getNrfClient()//
                                        .nfInstanceDeregister()
                                        .doOnSuccess(response ->
                                        {
                                            ChfSimulator.this.addXOriginHeader(routingContext);
                                            routingContext.response().setStatusCode(response.getStatusCode()).end(json.writeValueAsString(response.getBody()));
                                        })
                                        .doOnError(cause ->
                                        {
                                            ChfSimulator.this.addXOriginHeader(routingContext);
                                            routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(cause.toString());
                                        })
                                        .subscribe();
            }

            private Disposable handlerDoNfInstanceRegister(final RoutingContext routingContext)
            {
                return ChfSimulator.this.getNrfClient()//
                                        .nfInstanceRegister()
                                        .doOnSuccess(response ->
                                        {
                                            ChfSimulator.this.addXOriginHeader(routingContext);
                                            routingContext.response().setStatusCode(response.getStatusCode()).end(json.writeValueAsString(response.getBody()));
                                        })
                                        .doOnError(cause ->
                                        {
                                            ChfSimulator.this.addXOriginHeader(routingContext);
                                            routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(cause.toString());
                                        })
                                        .subscribe();
            }

            private Disposable handlerDoNfInstanceUpdate(final RoutingContext routingContext)
            {
                return ChfSimulator.this.getNrfClient()//
                                        .nfInstanceUpdate()
                                        .doOnSuccess(response ->
                                        {
                                            ChfSimulator.this.addXOriginHeader(routingContext);
                                            routingContext.response().setStatusCode(response.getStatusCode()).end(json.writeValueAsString(response.getBody()));
                                        })
                                        .doOnError(cause ->
                                        {
                                            ChfSimulator.this.addXOriginHeader(routingContext);
                                            routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(cause.toString());
                                        })
                                        .subscribe();
            }

            private void handlerSetAddr(final Object context)
            {
                final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

                try
                {
                    final String addr = routingContext != null ? URLDecoder.decode(routingContext.request().getParam("addr"), StandardCharsets.UTF_8.toString())
                                                               : (String) context;

                    final Matcher m = Pattern.compile("(?:(^[a-zA-Z]+),)?([^,]+),([0-9]+),?(.+)?$").matcher(addr); // Format:
                                                                                                                   // <protocol,>host,port<,ip>

                    if (!m.find())
                        throw new IOException("Input '" + addr + "' does not match '" + m.pattern().toString() + "'.");

                    ChfSimulator.this.getNrfClient().setAddr(m.group(1), m.group(2), Integer.valueOf(m.group(3)), m.group(4));

                    if (routingContext != null)
                        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                }
                catch (Exception e)
                {
                    log.error("Bad request. Cause: {}", e.getMessage());

                    if (routingContext != null)
                        routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(e.getMessage());
                }
            }

            private void handlerSetHeartbeatFullUpdateEveryNthTime(Object context)
            {
                final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

                ChfSimulator.this.getNrfClient()
                                 .setHeartbeatFullUpdateEveryNthTime(routingContext != null ? Integer.parseInt(routingContext.request().getParam("nthTime"))
                                                                                            : (Integer) context);

                if (routingContext != null)
                {
                    ChfSimulator.this.addXOriginHeader(routingContext);
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                }
            }

            private void handlerSetHeartbeatOnOff(Object context)
            {
                final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
                boolean on;

                if (routingContext != null)
                {
                    final String onOff = routingContext.request().getParam("onOff");
                    on = onOff.equalsIgnoreCase("on") || onOff.equalsIgnoreCase("true") || onOff.equalsIgnoreCase("yes");
                }
                else
                {
                    on = (Boolean) context;
                }

                if (Boolean.TRUE.equals(on))
                    ChfSimulator.this.getNrfClient().start().blockingAwait();
                else
                    ChfSimulator.this.getNrfClient().stop().blockingAwait();

                if (routingContext != null)
                {
                    ChfSimulator.this.addXOriginHeader(routingContext);
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                }
            }

            private void handlerSetNfCapacity(final Object context)
            {
                final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

                ChfSimulator.this.getNrfClient()
                                 .setCapacity(routingContext != null ? Integer.parseInt(routingContext.request().getParam("nfCapacity")) : (Integer) context);

                if (routingContext != null)
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
            }

            private void handlerSetNfLoadPercent(final Object context)
            {
                final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

                ChfSimulator.this.getNrfClient()
                                 .setLoadInPercent(routingContext != null ? Integer.parseInt(routingContext.request().getParam("nfLoadPercent"))
                                                                          : (Integer) context);

                if (routingContext != null)
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
            }

            @SuppressWarnings("unchecked")
            private void handlerSetNfPatch(final Object context)
            {
                final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

                if (routingContext != null)
                {
                    routingContext.request().bodyHandler(body ->
                    {
                        log.debug("body={}", body);

                        try
                        {
                            ChfSimulator.this.getNrfClient().setPatch(Arrays.asList(json.readValue(body.toString(), PatchItem[].class)));
                            routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                        }
                        catch (Exception e)
                        {
                            log.error("Bad request. Cause: {}", e.getMessage());
                            routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(e.getMessage());
                        }
                    });
                }
                else
                {
                    ChfSimulator.this.getNrfClient().setPatch((List<PatchItem>) context);
                }
            }

            private void handlerSetNfProfile(final Object context)
            {
                final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

                if (routingContext != null)
                {
                    routingContext.request().bodyHandler(body ->
                    {
                        log.debug("body={}", body);

                        final NrfClient client = ChfSimulator.this.getNrfClient();

                        try
                        {
                            NFProfile profile = json.readValue(body.toString(), NFProfile.class);

                            if (profile.getNfInstanceId() == null)
                                profile.setNfInstanceId(client.getNfInstanceId());

                            client.setProfile(profile);
                            routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                        }
                        catch (Exception e)
                        {
                            log.error("Bad request. Cause: {}", e.getMessage());
                            routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(e.getMessage());
                        }
                    });
                }
                else
                {
                    ChfSimulator.this.getNrfClient().setProfile((NFProfile) context);
                }
            }
        }

        /**
         * API supported by the simulator.
         */
        public static final String API = "/admin/v1";
        public static final String PATH_CHARGINGDATA = "/chargingdata";
        public static final String PATH_SUBSCRIPTIONS = "/subscriptions";
        public static final String OP_NOTIFY = "/notify";
        public static final String OP_TERMINATE = "/terminate";
        public static final String OP_RELEASE_ALL = "/delete_all";
        public static final String OP_ADD = "/add";
        public static final String OP_GET = "/get";
        public static final String OP_SHOW_ALL = "/show_all";
        public static final String OP_SHOW_CONFIG = "/show_config";
        public static final String OP_SET_REJ_PERCENT = "/set_rej_percent";
        public static final String OP_SET_REJ_ANSWER = "/set_rej_answer";
        public static final String OP_SET_REJ_MESSAGE = "/set_rej_message";
        public static final String OP_SET_DROP_ANSWER = "/set_drop_answer";
        public static final String OP_SET_DROP_MESSAGE = "/set_drop_message";
        public static final String OP_SET_DELAY_ANSWER = "/set_delay_answer";
        public static final String OP_SET_DELAY_MESSAGE = "/set_delay_message";
        public static final String OP_SET_WEIGHTED_DISTURBANCES = "/set_weighted_disturbances";
        public static final String OP_SET_OMIT_LOCATION_HEADER = "/set_omit_location_header";
        public static final String OP_ADD_DISTURBANCE = "/add_disturbance";
        public static final String OP_CLEAR_DISTURBANCES = "/clear_disturbances";
        public static final String OP_CLEAR_FLEXIBLE_DISTURBANCES = "/clear_flexible_disturbances";
        public static final String OP_CLEAR_SEMIPERMANENT_DISTURBANCES = "/clear_semipermanent_disturbances";
        public static final String OP_SHOW_DISTURBANCES = "/show_disturbances";
        public static final String OP_SET_ENVOY_DOMAIN = "/set_envoy_domain";
        public static final String OP_LOAD_TEST_MODE = "/load_test_mode";
        public static final String OP_LOAD_TEST_STATISTICS_MODE = "/load_test_statistics_mode";
        public static final String OP_SESSIONID_MODE = "/sessionid_mode";
        public static final String OP_NRF_DO_NF_INSTANCE_DEREGISTER = "/nrf/do_nf_instance_deregister";
        public static final String OP_NRF_DO_NF_INSTANCE_REGISTER = "/nrf/do_nf_instance_register";
        public static final String OP_NRF_DO_NF_INSTANCE_UPDATE = "/nrf/do_nf_instance_update";
        public static final String OP_NRF_SET_ADDR = "/nrf/set_addr";
        public static final String OP_NRF_SET_HEARTBEAT = "/nrf/set_heartbeat";
        public static final String OP_NRF_SET_HEARTBEAT_FULL_UPDATE_EVERY_NTH_TIME = "/nrf/set_heartbeat_full_update_every_nth_time";
        public static final String OP_NRF_SET_NF_CAPACITY = "/nrf/set_nf_capacity";
        public static final String OP_NRF_SET_NF_LOAD_PERCENT = "/nrf/set_nf_load_percent";
        public static final String OP_NRF_SET_NF_PATCH = "/nrf/set_nf_patch";
        public static final String OP_NRF_SET_NF_PROFILE = "/nrf/set_nf_profile";
        public static final String OP_NOTIFY_HEADER = "/notify_header";
        public static final String OP_SBI_NF_PEER_INFO = "/sbi_nf_peer_info";

        // Members are not private for simplicity of use.
        // Will be set only by handler-methods.
        int rejectPercent = 0;
        HttpResponseStatus rejectAnswer = HttpResponseStatus.INTERNAL_SERVER_ERROR;
        String rejectMessageType = "all"; // init/update/term/all
        WeightedDisturbances weightedDisturbances = WeightedDisturbances.fromString("1,none");
        String dropMessageType = "all"; // init/update/term/all
        String delayMessageType = "all"; // init/update/term/all
        Url envoyDomain = new Url("eric-scp-worker", 443, "");
        boolean omitLocationHeader = false;
        boolean loadTestMode = true;
        boolean loadTestStatisticsMode = false;
        boolean sessionIdMode = false;
        String notifyHeader = "x-notify-uri";
        String sbiNfPeerInfo = null;
        final Nrf nrf = new Nrf();

        public Admin(Vertx vertx,
                     List<? extends RouterHandler> rh,
                     List<? extends RouterHandler> rhTls)
        {
            // Register the admin URL endpoints we listen on:
            Router router = Router.router(vertx);

            router.post(PATH_CHARGINGDATA + OP_NOTIFY + "/:chargingDataRef" + "/:viaHeader").handler(this::handlerConvergedChargingNotify);
            router.post(PATH_CHARGINGDATA + OP_NOTIFY + "/:chargingDataRef").handler(this::handlerConvergedChargingNotify);
            router.post(PATH_SUBSCRIPTIONS + OP_NOTIFY + "/:subscriptionId").handler(this::handlerSpendingLimitControlNotify);
            router.post(PATH_SUBSCRIPTIONS + OP_TERMINATE + "/:subscriptionId").handler(this::handlerSpendingLimitControlTerminate);

            router.post(OP_RELEASE_ALL).handler(this::handlerReleaseAll);
            router.post(OP_SHOW_ALL).handler(this::handlerShowAll);

            router.post(OP_SET_REJ_PERCENT + "/:rejectPercent").handler(this::handlerSetRejPercent);
            router.post(OP_SET_REJ_ANSWER + "/:rejectAnswer").handler(this::handlerSetRejAnswer);
            router.post(OP_SET_REJ_MESSAGE + "/:rejectMessage").handler(this::handlerSetRejMessage);

            router.post(OP_SET_WEIGHTED_DISTURBANCES + "/:disturbances").handler(this::handlerSetWeightedDisturbances);

            router.post(OP_SET_DROP_ANSWER + "/:dropAnswer").handler(this::handlerSetDropAnswer);
            router.post(OP_SET_DROP_MESSAGE + "/:dropMessage").handler(this::handlerSetDropMessage);

            router.post(OP_SET_DELAY_ANSWER + "/:delayAnswer").handler(this::handlerSetDelayAnswer);
            router.post(OP_SET_DELAY_MESSAGE + "/:delayMessage").handler(this::handlerSetDelayMessage);

            router.post(OP_SET_OMIT_LOCATION_HEADER + "/:omitLocationHeader").handler(this::handlerSetOmitLocationHeader);

            router.post(OP_ADD_DISTURBANCE + "/:disturbance").handler(this::handlerAddDisturbance);
            router.post(OP_SHOW_DISTURBANCES).handler(this::handlerShowDisturbances);
            router.post(OP_CLEAR_DISTURBANCES).handler(this::handlerClearDisturbances);
            router.post(OP_CLEAR_FLEXIBLE_DISTURBANCES).handler(this::handlerClearFlexibleDisturbances);
            router.post(OP_CLEAR_SEMIPERMANENT_DISTURBANCES).handler(this::handlerClearSemipermanentDisturbances);
            router.post(OP_SHOW_CONFIG).handler(this::handlerShowConfig); // POST was a mistake, but is kept for
                                                                          // backward-compatibility
            router.get(OP_SHOW_CONFIG).handler(this::handlerShowConfig);

            router.post(OP_SET_ENVOY_DOMAIN + "/:envoyDomain").handler(this::handlerSetEnvoyDomain);

            router.post(OP_LOAD_TEST_MODE + "/:mode").handler(this::handlerLoadTestMode);
            router.post(OP_LOAD_TEST_STATISTICS_MODE + "/:mode").handler(this::handlerLoadTestStatisticsMode);
            router.post(OP_SESSIONID_MODE + "/:mode").handler(this::handlerSessionIdMode);
            router.post(OP_NOTIFY_HEADER + "/:notifyHeader").handler(this::handlerNotifyHeader);
            router.post(OP_SBI_NF_PEER_INFO + "/:sbiNfPeerInfo").handler(this::handler3gppSbiNfPeerInfo);

            router.post(OP_NRF_DO_NF_INSTANCE_DEREGISTER).handler(this.getNrf()::handlerDoNfInstanceDeregister);
            router.post(OP_NRF_DO_NF_INSTANCE_REGISTER).handler(this.getNrf()::handlerDoNfInstanceRegister);
            router.post(OP_NRF_DO_NF_INSTANCE_UPDATE).handler(this.getNrf()::handlerDoNfInstanceUpdate);
            router.post(OP_NRF_SET_ADDR + "/:addr").handler(this.getNrf()::handlerSetAddr);
            router.post(OP_NRF_SET_HEARTBEAT + "/:onOff").handler(this.getNrf()::handlerSetHeartbeatOnOff);
            router.post(OP_NRF_SET_HEARTBEAT_FULL_UPDATE_EVERY_NTH_TIME + "/:nthTime").handler(this.getNrf()::handlerSetHeartbeatFullUpdateEveryNthTime);
            router.post(OP_NRF_SET_NF_CAPACITY + "/:nfCapacity").handler(this.getNrf()::handlerSetNfCapacity);
            router.post(OP_NRF_SET_NF_LOAD_PERCENT + "/:nfLoadPercent").handler(this.getNrf()::handlerSetNfLoadPercent);
            router.post(OP_NRF_SET_NF_PATCH).handler(this.getNrf()::handlerSetNfPatch);
            router.post(OP_NRF_SET_NF_PROFILE).handler(this.getNrf()::handlerSetNfProfile);

            rh.forEach(h -> h.mountRouter(API, router));

            if (rhTls != null)
                rhTls.forEach(h -> h.mountRouter(API, router));
        }

        public void addDisturbance(final String disturbance)
        {
            this.handlerAddDisturbance(disturbance);
        }

        /**
         * Clear all disturbances, semi-permanent and flexible.
         */
        public void clearDisturbances()
        {
            this.handlerClearDisturbances(null);
        }

        /**
         * Clear all flexible disturbances
         */
        public void clearFlexibleDisturbances()
        {
            this.handlerClearFlexibleDisturbances(null);
        }

        /**
         * Clear all semipermanent disturbances
         */
        public void clearSemipermanentDisturbances()
        {
            this.handlerClearSemipermanentDisturbances(null);
        }

        public void convergedChargingNotify(final String chargingDataRef)
        {
            this.handlerConvergedChargingNotify(chargingDataRef);
        }

        public Nrf getNrf()
        {
            return this.nrf;
        }

        public void loadTestMode(final String mode)
        {
            this.handlerLoadTestMode(mode);
        }

        public void loadTestStatisticsMode(final String mode)
        {
            this.handlerLoadTestStatisticsMode(mode);
        }

        public void releaseAll()
        {
            this.handlerReleaseAll(null);
        }

        public void sessionIdMode(final String mode)
        {
            this.handlerSessionIdMode(mode);
        }

        public void set3gppSbiNfPeerInfo(final String peerInfo)
        {
            this.handler3gppSbiNfPeerInfo(peerInfo);
        }

        public void setDelayAnswer(final Integer delayAnswerMs)
        {
            this.handlerSetDelayAnswer(delayAnswerMs);
        }

        public void setDelayMessage(final String delayMessageType)
        {
            this.handlerSetDelayMessage(delayMessageType);
        }

        public void setDropAnswer(final Integer dropAnswer)
        {
            this.handlerSetDropAnswer(dropAnswer);
        }

        public void setDropMessage(final String dropMessageType)
        {
            this.handlerSetDropMessage(dropMessageType);
        }

        public void setEnvoyDomain(final String host,
                                   final Integer port)
        {
            this.setEnvoyDomain("http", host, port, host);
        }

        public void setEnvoyDomain(final String protocol,
                                   final String host,
                                   final Integer port,
                                   final String ip)
        {
            this.handlerSetEnvoyDomain(new StringBuilder(protocol).append(",").append(host).append(",").append(port).append(",").append(ip).toString());
        }

        public void setNotifyHeader(final String notifyHeader)
        {
            this.handlerNotifyHeader(notifyHeader);
        }

        public void setOmitLocationHeader(final Boolean omitLocationHeader)
        {
            this.handlerSetOmitLocationHeader(omitLocationHeader);
        }

        public void setRejAnswer(Integer rejectAnswer)
        {
            this.handlerSetRejAnswer(rejectAnswer);
        }

        public void setRejMessage(final String rejectMessageType)
        {
            this.handlerSetRejMessage(rejectMessageType);
        }

        public void setRejPercent(Integer rejectPercent)
        {
            this.handlerSetRejPercent(rejectPercent);
        }

        public void setWeightedDisturbances(final String disturbances)
        {
            this.handlerSetWeightedDisturbances(disturbances);
        }

        public void showAll()
        {
            this.handlerShowAll(null);
        }

        public void showConfig()
        {
            this.handlerShowConfig(null);
        }

        public void showDisturbances()
        {
            this.handlerShowDisturbances(null);
        }

        public void spendingLimitControlNotify(final String subscriptionId)
        {
            this.handlerSpendingLimitControlNotify(subscriptionId);
        }

        public void spendingLimitControlTerminate(final String subscriptionId)
        {
            this.handlerSpendingLimitControlTerminate(subscriptionId);
        }

        /**
         * Similar to SessionDataStore.getAllRefs(), used only when admin.sessionIdMode
         * is enabled. returns a String representation of all chargingDataRefs with new
         * session id and their notifyUris (if not null) for debugging
         */
        private String getAllNewIdRefs()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(System.lineSeparator());
            sb.append("----All allocated chargingDataRefs with sessionId:");
            sb.append(System.lineSeparator());

            if (ChfSimulator.this.allocatedNewIdRefs.isEmpty())
            {
                sb.append("No chargingDataRefs allocated.").append(System.lineSeparator());
            }
            else
            {
                for (var entry : Collections.unmodifiableSet(ChfSimulator.this.allocatedNewIdRefs))
                {
                    sb.append("chargingDataRef = ").append(entry).append(System.lineSeparator());
                }
            }
            sb.append("-------------------").append(System.lineSeparator());
            return sb.toString();
        }

        /**
         * Set the value for header 3gpp_Sbi_NF_Peer_Info to be sent in CC and SLC
         * messages initiated by the ChfSimulator (e.g. CC/SLC notify messages). If not
         * provided (default), the header is not added.
         *
         * @param context
         */
        private void handler3gppSbiNfPeerInfo(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

            this.sbiNfPeerInfo = routingContext != null ? routingContext.request().getParam("sbiNfPeerInfo") : (String) context;
            log.info("Set: sbiNfPeerInfo = {}", this.sbiNfPeerInfo);

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("3GPP SBI NF Peer Info is " + this.sbiNfPeerInfo);
        }

        /**
         * Handle AddDisturbance: Add a new disturbance to the flexible-disturbances
         * list "disturbances" is a single entry or a comma-separated list of entries.
         * <p>
         * An entry is one of:
         * <li>reject:code # responds with the given error code (e.g. reject:503)
         * <li>drop # does not reply to the message
         * <li>delay:millis # delays the response by millis ms (e.g. delay:300)
         * 
         * @param context
         */
        private void handlerAddDisturbance(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            final String disturbance = routingContext != null ? routingContext.request().getParam("disturbance") : (String) context;

            // Redis' "rpush" command requires a list where the key is the first element and
            // the values follow.
            String keyAndDisturbances = REDIS_KEY_FLEXI_DISTURBANCE + "," + disturbance;
            List<String> distElems = Arrays.asList(keyAndDisturbances.split(","));

            ChfSimulator.this.redis.rpush(distElems, res ->
            {
                if (res.succeeded())
                {
                    log.info("Added Flexible Disturbance: {}", distElems);

                    if (routingContext != null)
                        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
                }
                else
                {
                    log.error("Error adding a flexible disturbance. Cause: '{}'.", res.cause().toString());

                    if (routingContext != null)
                        routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(res.cause().toString());
                }
            });
        }

        /**
         * Handle ClearDisturbances: Remove all semi-permanent and all flexible
         * disturbances
         * 
         * @param routingContext
         */
        private void handlerClearDisturbances(final RoutingContext routingContext)
        {
            handlerClearFlexibleDisturbances(routingContext, false);
            handlerClearSemipermanentDisturbances(routingContext, false);

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Successfully cleared the Flexible and Semipermanent Disturbances\n");
        }

        /**
         * Handle ClearFlexibleDisturbances: Remove all flexible-disturbances by
         * clearing the list. This affects also all other ChfSim!
         * 
         * @param routingContext
         */
        private void handlerClearFlexibleDisturbances(final RoutingContext routingContext)
        {
            handlerClearFlexibleDisturbances(routingContext, true);
        }

        private void handlerClearFlexibleDisturbances(final RoutingContext routingContext,
                                                      boolean sendReply)
        {
            ChfSimulator.this.redis.del(Arrays.asList(REDIS_KEY_FLEXI_DISTURBANCE), res ->
            {
                if (res.succeeded())
                {
                    log.info("Successfully cleared the Flexible Disturbances");

                    if (sendReply && routingContext != null)
                        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Successfully cleared the Flexible Disturbances\n");
                }
                else
                {
                    log.error("Error clearing the list of flexible disturbances. Cause: '{}'.", res.cause().toString());

                    if (sendReply && routingContext != null)
                        routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(res.cause().toString());
                }
            });
        }

        /**
         * Handle ClearSemipermanentDisturbances: Remove the disturbances on this ChfSim
         * that affect all requests.
         */
        private void handlerClearSemipermanentDisturbances(final RoutingContext routingContext)
        {
            handlerClearSemipermanentDisturbances(routingContext, true);
        }

        private void handlerClearSemipermanentDisturbances(final RoutingContext routingContext,
                                                           boolean sendReply)
        {
            setRejPercent(0);
            setDropAnswer(0);
            setDelayAnswer(0);
            log.info("Successfully cleared the Semipermanent Disturbances");

            if (sendReply && routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Successfully cleared the Semipermanent Disturbances\n");
        }

        /**
         * Handle notify: send a notify message for a given chargingDataRef.
         * 
         * @param context
         */
        private void handlerConvergedChargingNotify(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            final String chargingDataRef = routingContext != null ? routingContext.request().getParam("chargingDataRef") : (String) context;

            // both new and default session id format are supported
            if ((this.sessionIdMode && this.isNewIdValid(chargingDataRef)) || ChfSimulator.this.sessionDataStore.isValid(chargingDataRef))
            {
                // request the notifyUri from the backend
                ChfSimulator.this.redis.get(chargingDataRef, res ->
                {
                    try
                    {
                        if (res.succeeded() && (res.result() != null))
                        {
                            log.info("CC Notify: Redis get succeeded '{}'.", res.result());
                            String notifyUri = res.result().toString();

                            if (notifyUri != null)
                            {
                                log.info("CC Notify: Sending 200 OK and notification to '{}' for '{}'.", notifyUri, chargingDataRef);
                                ChfSimulator.this.sendNchfConvergedChargingNotify(routingContext, chargingDataRef, notifyUri);
                            }
                            else
                            {
                                final String msg = "CC Notify: No notifyUri found in REDIS.";
                                log.error(msg);

                                if (routingContext != null)
                                {
                                    log.info("CC Notify: Sending 400 Bad Request for '{}'.", chargingDataRef);
                                    ChfSimulator.this.addXOriginHeader(routingContext);
                                    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(msg);
                                }
                            }
                        }
                        else
                        {
                            log.error("CC Notify: REDIS-get unsuccessful. Cause: '{}'.", res.cause().toString());

                            if (routingContext != null)
                            {
                                log.info("CC Notify: Sending 400 Bad Request for '{}'.", chargingDataRef);
                                ChfSimulator.this.addXOriginHeader(routingContext);
                                routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(res.cause().toString());
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        final String msg = "CC Notify: Connection was already closed when trying to send reply. This can happen when envoy times out earlier than our delay finishes.";
                        log.info(msg);

                        if (routingContext != null)
                        {
                            ChfSimulator.this.addXOriginHeader(routingContext);
                            routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(msg);
                        }
                    }
                });
            }
            else
            {
                final String msg = "CC Notify: Invalid CDR received '" + chargingDataRef + "'.";
                log.error(msg);

                if (routingContext != null)
                {
                    log.info("Notify: Sending 400 Bad Request for '{}'.", chargingDataRef);
                    ChfSimulator.this.addXOriginHeader(routingContext);
                    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(msg);
                }
            }
        }

        /**
         * Set mode for load testing on or off.
         * 
         * @param context
         */
        private void handlerLoadTestMode(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            String mode = routingContext != null ? routingContext.request().getParam("mode") : (String) context;

            this.loadTestMode = mode.equalsIgnoreCase("on");

            log.info("Set: loadTestmode = {}", this.loadTestMode);

            if (routingContext != null)
            {
                ChfSimulator.this.addXOriginHeader(routingContext); // Mark response with our hostname so that we can
                                                                    // see which chfsim responded
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Load Test Mode is " + mode);
            }
        }

        /**
         * Set mode for statistics on or off. Only effective when in load-testing mode.
         * 
         * @param context
         */
        private void handlerLoadTestStatisticsMode(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            String mode = routingContext != null ? routingContext.request().getParam("mode") : (String) context;

            this.loadTestStatisticsMode = mode.equalsIgnoreCase("on");

            log.info("Set: loadTestStatisticsMode = {}", this.loadTestStatisticsMode);

            if (routingContext != null)
            {
                ChfSimulator.this.addXOriginHeader(routingContext); // Mark response with our hostname so that we can
                                                                    // see which chfsim responded
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Load Test Statistics Mode is " + mode);
            }
        }

        /**
         * Set header for notify message. Valid values are x-notify-uri and
         * 3gpp-Sbi-Target-apiRoot. The default value is x-notify-uri.
         *
         * @param context
         */
        private void handlerNotifyHeader(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

            this.notifyHeader = routingContext != null ? routingContext.request().getParam("notifyHeader") : (String) context;
            log.info("Set: notifyHeader = {}", this.notifyHeader);

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Notify header is " + this.notifyHeader);
        }

        /**
         * Handle ReleaseAll: free all chargingDataRefs so that it's not necessary to
         * free them one by one.
         * 
         * @param routingContext
         */
        private void handlerReleaseAll(final RoutingContext routingContext)
        {
            log.info("Admin: Releasing all chargingDataRefs");

            vertx.executeBlocking(promise ->
            {
                ChfSimulator.this.sessionDataStore.releaseAllRefs();
                ChfSimulator.this.allocatedNewIdRefs = ConcurrentHashMap.<String>newKeySet();
                promise.complete();
            }, res ->
            {
                log.info("All chargingDataRefs have been released");
                if (routingContext != null)
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end();
            });
        }

        /**
         * Set mode for new extend session id with p-value on or off. Off is the default
         * choice and on for CUSTOM_CAF chfsim.
         *
         * @param context
         */
        private void handlerSessionIdMode(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            String mode = routingContext != null ? routingContext.request().getParam("mode") : (String) context;

            this.sessionIdMode = mode.equalsIgnoreCase("on");

            log.info("Set: sessionIdMode = {}", this.sessionIdMode);

            if (routingContext != null)
            {
                ChfSimulator.this.addXOriginHeader(routingContext); // Mark response with our hostname so that we can
                                                                    // see which chfsim responded
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Session Id Mode is " + mode);
            }
        }

        /**
         * Handle setDelayAnswer: Set the global variable delayAnswerMs
         * 
         * @param context
         */
        private void handlerSetDelayAnswer(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            final Integer delayAnswerMs = routingContext != null ? Integer.parseInt(routingContext.request().getParam("delayAnswer")) : (Integer) context;
            log.info("Set: delayAnswerMs = {}", delayAnswerMs);

            if (delayAnswerMs != 0)
                this.weightedDisturbances = WeightedDisturbances.fromString("1,delay," + delayAnswerMs);
            else // No delay => reset
                this.weightedDisturbances = WeightedDisturbances.fromString("1,none");

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Delay Answer by " + delayAnswerMs + " ms");
        }

        /**
         * Handle setDelayMessage Set the global variable delayMessageType
         * 
         * @param context
         */
        private void handlerSetDelayMessage(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

            this.delayMessageType = routingContext != null ? routingContext.request().getParam("delayMessage") : (String) context;
            log.info("Set: delayMessageType = {}", this.delayMessageType);

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Delay Message type " + this.delayMessageType);
        }

        /**
         * Handle setDropAnswer: Set the global variable dropAnswer
         * 
         * @param context
         */
        private void handlerSetDropAnswer(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

            final int dropAnswer = routingContext != null ? Integer.parseInt(routingContext.request().getParam("dropAnswer")) : (Integer) context;
            log.info("Set: dropAnswer = {}", dropAnswer);

            if (dropAnswer == 0)
                this.weightedDisturbances = WeightedDisturbances.fromString("1,none");
            else
                this.weightedDisturbances = WeightedDisturbances.fromString("1,drop," + dropAnswer);

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Drop Answer: " + dropAnswer);
        }

        /**
         * Handle setDropMessage Set the global variable dropMessageType
         * 
         * @param context
         */
        private void handlerSetDropMessage(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

            this.dropMessageType = routingContext != null ? routingContext.request().getParam("dropMessage") : (String) context;
            log.info("Set: dropMessageType = {}", this.dropMessageType);

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Drop Message type " + this.dropMessageType);
        }

        /**
         * Set envoy domain for sending notify requests
         * 
         * @param context
         */
        private void handlerSetEnvoyDomain(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;

            try
            {
                final String envoyDomain = routingContext != null ? URLDecoder.decode(routingContext.request().getParam("envoyDomain"),
                                                                                      StandardCharsets.UTF_8.toString())
                                                                  : (String) context;

                final Matcher m = Pattern.compile("(?:(^[a-zA-Z]+),)?([^,]+),([0-9]+),?(.+)?$").matcher(envoyDomain); // Format:
                                                                                                                      // <protocol,>host,port<,ip>

                if (!m.find())
                    throw new IOException("Input '" + envoyDomain + "' does not match '" + m.pattern().toString() + "'.");

                this.envoyDomain = new Url(m.group(1), m.group(2), Integer.valueOf(m.group(3)), this.envoyDomain.getUrl().getPath(), m.group(4));
                log.info("Set: envoyDomain = '{}'", this.envoyDomain);

                if (routingContext != null)
                {
                    ChfSimulator.this.addXOriginHeader(routingContext); // Mark response with our hostname so that we
                                                                        // can see which chfsim responded
                    routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Envoy domain is " + this.envoyDomain);
                }
            }
            catch (Exception e)
            {
                log.error("Bad request. Cause: {}", e.getMessage());

                if (routingContext != null)
                {
                    ChfSimulator.this.addXOriginHeader(routingContext); // Mark response with our hostname so that we
                                                                        // can see which chfsim responded
                    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(e.getMessage());
                }
            }
        }

        /**
         * Handle setOmitLocationHeader: Set the global variable omitLocationHeader
         * 
         * @param context
         */
        private void handlerSetOmitLocationHeader(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            String omitLocationHeader = routingContext != null ? routingContext.request().getParam("omitLocationHeader") : (String) context;

            this.omitLocationHeader = omitLocationHeader.equalsIgnoreCase("on");

            log.info("Set: omitLocationHeader = {}", this.omitLocationHeader);

            if (routingContext != null)
            {
                ChfSimulator.this.addXOriginHeader(routingContext); // Mark response with our hostname so that we can see which chfsim responded
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Omit location header is " + omitLocationHeader);
            }
        }

        /**
         * Handle setRejAnswer: Set the global variable rejectAnswer
         * 
         * @param context
         */
        private void handlerSetRejAnswer(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            this.rejectAnswer = HttpResponseStatus.valueOf(routingContext != null ? Integer.parseInt(routingContext.request().getParam("rejectAnswer"))
                                                                                  : (Integer) context);
            log.info("Set: rejectAnswer = {}", this.rejectAnswer);

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Reject Answer " + this.rejectAnswer);
        }

        /**
         * Handle setRejMessage Set the global variable rejectMessageType
         * 
         * @param context
         */
        private void handlerSetRejMessage(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            this.rejectMessageType = routingContext != null ? routingContext.request().getParam("rejectMessage") : (String) context;

            log.info("Set: rejectMessageType = {}", this.rejectMessageType);

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Reject Message type " + this.rejectMessageType);
        }

        /**
         * Handle setRejPercent: Set the global variable rejectPercent
         * 
         * @param context
         */
        private void handlerSetRejPercent(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            this.rejectPercent = routingContext != null ? Integer.parseInt(routingContext.request().getParam("rejectPercent")) : (Integer) context;
            log.info("Set: rejectPercent = {}", this.rejectPercent);

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Reject percent: " + this.rejectPercent + "%");

        }

        /**
         * Handle setWeigtedDisturbances: Set the global variable weightedDisturbances
         * <p>
         * The parameter <code>context</code> must be a string of the following format:
         * <p>
         * 
         * <pre>
         * weight ',' ( "delay" ',' delayInMillis | "drop" | "none" ) (';' weight ',' ( "delay" ',' delayInMillis | "drop" | "none" ) ) *
         * </pre>
         * 
         * Example:
         * 
         * <pre>
         * "10,delay,300;5,delay,500;3,drop"
         * </pre>
         * 
         * To reset just use this:
         * 
         * <pre>
         * "1,none"
         * </pre>
         * 
         * @param context
         */
        private void handlerSetWeightedDisturbances(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            this.weightedDisturbances = WeightedDisturbances.fromString(routingContext != null ? routingContext.request().getParam("disturbances")
                                                                                               : (String) context);
            log.info("Set: weightedDisturbances = {}", this.weightedDisturbances);

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end("Disturbances set: " + this.weightedDisturbances.toString());
        }

        /**
         * Handle ShowAll: print all allocated chargingDataRefs and their notifyUris If
         * new session id format is used, chargingDataRefs are stored in
         * allocatedNewIdRefs.
         * 
         * @param routingContext
         */
        private void handlerShowAll(final RoutingContext routingContext)
        {
            log.info(getAllNewIdRefs());
            ChfSimulator.this.sessionDataStore.logAllRefs();

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(ChfSimulator.this.sessionDataStore.getAllRefs() + getAllNewIdRefs());

        }

        /**
         * Handle ShowConfig: print all settings
         * 
         * @param routingContext
         */
        private void handlerShowConfig(final RoutingContext routingContext)
        {
            StringBuilder sb = new StringBuilder();

            sb.append(System.lineSeparator());
            sb.append("----Configuration:").append(System.lineSeparator());
            sb.append(" load test statistics: ").append(this.loadTestStatisticsMode).append(System.lineSeparator());
            sb.append(" load test mode: ").append(this.loadTestMode).append(System.lineSeparator());
            sb.append(" sessionId mode: ").append(this.sessionIdMode).append(System.lineSeparator());

            if (this.loadTestMode)
            {
                sb.append("   --> no checking of charging data ref, no backend update, no disturbances").append(System.lineSeparator());

                if (this.loadTestStatisticsMode)
                {
                    sb.append("    --> counters in CHFsim are stepped").append(System.lineSeparator());
                }
                else
                {
                    sb.append("    --> no counters are stepped, enable load_test_statistics_mode if counters are required").append(System.lineSeparator());
                }
            }
            else
            {
                sb.append("   --> realistic simulation of an OCC").append(System.lineSeparator());
            }

            sb.append("--Fixed Disturbances:").append(System.lineSeparator());
            sb.append(" rejectPercent = ").append(this.rejectPercent).append(System.lineSeparator());

            if (this.rejectPercent == 0)
            {
                sb.append("    --> Fixed Disturbances are off. Increase the reject percent value to enable them").append(System.lineSeparator());
            }

            sb.append(" rejectAnswer = ").append(this.rejectAnswer).append(System.lineSeparator());
            sb.append(" rejectMessageType = ").append(this.rejectMessageType).append(System.lineSeparator());
            sb.append(" dropMessageType = ").append(this.dropMessageType).append(System.lineSeparator());
            sb.append(" delayMessageType = ").append(this.delayMessageType).append(System.lineSeparator());
            sb.append(" weightedDisturbances = ").append(this.weightedDisturbances.toString()).append(System.lineSeparator());
            sb.append("-----------------------").append(System.lineSeparator());

            log.info(sb.toString());

            if (routingContext != null)
                routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(sb.toString());
        }

        /**
         * Handle ShowDisturbances: Print the current flexible-disturbances list
         * 
         * @param routingContext
         */
        private void handlerShowDisturbances(final RoutingContext routingContext)
        {
            ChfSimulator.this.redis.lrange(REDIS_KEY_FLEXI_DISTURBANCE, "0", "-1", res ->
            {
                if (res.succeeded())
                {
                    log.info("Flexible Disturbance: {}", res.result());

                    if (routingContext != null)
                        routingContext.response().setStatusCode(HttpResponseStatus.OK.code()).end(res.result().toString());
                }
                else
                {
                    log.error("Error getting the list of flexible disturbances. Cause: '{}'.", res.cause());

                    if (routingContext != null)
                        routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(res.cause().toString());
                }
            });
        }

        /**
         * Handle notify: send a notify message for a given subscriptionId.
         * 
         * @param context
         */
        private void handlerSpendingLimitControlNotify(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            final String subscriptionId = routingContext != null ? routingContext.request().getParam(SUBSCRIPTION_ID) : (String) context;

            if (ChfSimulator.this.sessionDataStore.isValid(subscriptionId))
            {
                // request the notifyUri from the backend
                ChfSimulator.this.redis.get(subscriptionId, res ->
                {
                    try
                    {
                        if (res.succeeded() && (res.result() != null))
                        {
                            log.info("SL Notify: Redis get succeeded '{}'.", res.result());
                            String notifyUri = res.result().toString();

                            if (notifyUri != null)
                            {
                                log.info("SL Notify: Sending 200 OK and notification to '{}' for '{}'.", notifyUri, subscriptionId);
                                ChfSimulator.this.sendNchfSpendingLimitControlNotify(routingContext, subscriptionId, notifyUri);
                            }
                            else
                            {
                                final String msg = "SL Notify: No notifyUri found in REDIS.";
                                log.error(msg);

                                if (routingContext != null)
                                {
                                    log.info("SL Notify: Sending 400 Bad Request for '{}'.", subscriptionId);
                                    ChfSimulator.this.addXOriginHeader(routingContext);
                                    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(msg);
                                }
                            }
                        }
                        else
                        {
                            log.error("SL Notify: REDIS-get unsuccessful. Cause: '{}'.", res.cause());

                            if (routingContext != null)
                            {
                                log.info("SL Notify: Sending 400 Bad Request for '{}'.", subscriptionId);
                                ChfSimulator.this.addXOriginHeader(routingContext);
                                routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(res.cause().toString());
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        final String msg = "SL Notify: Connection was already closed when trying to send reply. This can happen when envoy times out earlier than our delay finishes.";
                        log.info(msg);

                        if (routingContext != null)
                        {
                            ChfSimulator.this.addXOriginHeader(routingContext);
                            routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(msg);
                        }
                    }
                });
            }
            else
            {
                final String msg = "SL Notify: Invalid CDR received '" + subscriptionId + "'.";
                log.error(msg);

                if (routingContext != null)
                {
                    log.info("SL Notify: Sending 400 Bad Request for '{}'.", subscriptionId);
                    ChfSimulator.this.addXOriginHeader(routingContext);
                    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(msg);
                }
            }
        }

        /**
         * Handle terminate: send a terminate message for a given subscriptionId
         * 
         * @param context
         */
        private void handlerSpendingLimitControlTerminate(final Object context)
        {
            final RoutingContext routingContext = context instanceof RoutingContext ? (RoutingContext) context : null;
            final String subscriptionId = routingContext != null ? routingContext.request().getParam(SUBSCRIPTION_ID) : (String) context;

            if (ChfSimulator.this.sessionDataStore.isValid(subscriptionId))
            {
                // request the notifyUri from the backend
                ChfSimulator.this.redis.get(subscriptionId, res ->
                {
                    try
                    {
                        if (res.succeeded() && (res.result() != null))
                        {
                            log.info("SL Terminate: Redis get succeeded '{}'.", res.result());
                            String notifyUri = res.result().toString();

                            if (notifyUri != null)
                            {

                                log.info("SL Terminate: Sending 200 OK and notification to {} for '{}'.", notifyUri, subscriptionId);
                                ChfSimulator.this.sendNchfSpendingLimitControlTerminate(routingContext, subscriptionId, notifyUri);
                            }
                            else
                            {
                                final String msg = "Terminate: No notifyUri found in REDIS.";
                                log.error(msg);

                                if (routingContext != null)
                                {
                                    log.info("SL Notify: Sending 400 Bad Request for '{}'.", subscriptionId);
                                    ChfSimulator.this.addXOriginHeader(routingContext);
                                    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(msg);
                                }
                            }
                        }
                        else
                        {
                            log.error("SL Terminate: REDIS-get unsuccessful. Cause: '{}'.", res.cause());

                            if (routingContext != null)
                            {
                                log.info("SL Terminate: Sending 400 Bad Request for '{}'.", subscriptionId);
                                routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end(res.cause().toString());
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        final String msg = "SL Terminate: Connection was already closed when trying to send reply. This can happen when envoy times out earlier than our delay finishes.";
                        log.info(msg);

                        if (routingContext != null)
                        {
                            ChfSimulator.this.addXOriginHeader(routingContext);
                            routingContext.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code()).end(msg);
                        }
                    }
                });

                // Delete the subscription-ID from the front-end:
                ChfSimulator.this.sessionDataStore.releaseRef(subscriptionId);

                // Also delete the subscription-ID from the backend
                ChfSimulator.this.redis.del(Arrays.asList(subscriptionId), res ->
                {
                    if (res.succeeded() && (res.result().toInteger() == 1))
                        log.info("SL Terminate: delete from REDIS succeeded for '{}'.", subscriptionId);
                    else
                        log.error("SL Terminate: delete from REDIS failed for {} because the chargingDataRef was not found.", subscriptionId);
                });
            }
            else
            {
                if (routingContext != null)
                {
                    log.info("SL Terminate: sending 400 Bad Request for '{}'", subscriptionId);
                    ChfSimulator.this.addXOriginHeader(routingContext);
                    routingContext.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
                }
            }
        }

        /**
         * Similar to SessionDataStore.isValid() and used only when admin.sessionIdMode
         * is enabled.
         */
        private boolean isNewIdValid(String cdr)
        {
            return ChfSimulator.this.allocatedNewIdRefs.contains(cdr);
        }

        /**
         * Similar to SessionDataStore.releaseRef() and used only when
         * admin.sessionIdMode is enabled.
         */
        private boolean releaseNewIdRef(String cdr)
        {
            return ChfSimulator.this.allocatedNewIdRefs.remove(cdr);
        }
    }

    public enum EventTrigger
    {
        NCHF_CONVERGED_CHARGING_CREATE,
        NCHF_CONVERGED_CHARGING_NOTIFY,
        NCHF_CONVERGED_CHARGING_RELEASE,
        NCHF_CONVERGED_CHARGING_UPDATE,
        NCHF_SPENDING_LIMIT_CONTROL_MODIFY,
        NCHF_SPENDING_LIMIT_CONTROL_NOTIFY,
        NCHF_SPENDING_LIMIT_CONTROL_SUBSCRIBE,
        NCHF_SPENDING_LIMIT_CONTROL_TERMINATE,
        NCHF_SPENDING_LIMIT_CONTROL_UNSUBSCRIBE;
    }

    public static class NrfClient extends NrfAdapter
    {
        private final LoadMeter loadMeter;
        private NFProfile profile;
        private PatchItem[] patch;

        public NrfClient(WebClient client)
        {
            this(client, UUID.randomUUID(), null);
        }

        public NrfClient(WebClient client,
                         final LoadMeter loadMeter)
        {
            super(client, UUID.randomUUID());

            this.loadMeter = loadMeter;
            this.profile = this.buildProfile();
            this.patch = null;
        }

        public NrfClient(final WebClient client,
                         final UUID nfInstanceId)
        {
            this(client, nfInstanceId, null);
        }

        public NrfClient(final WebClient client,
                         final UUID nfInstanceId,
                         final LoadMeter loadMeter)
        {
            super(client, nfInstanceId);

            this.loadMeter = loadMeter;
            this.profile = this.buildProfile();
            this.patch = null;
        }

        /**
         * @return The NF instance ID.
         */
        public UUID getNfInstanceId()
        {
            return this.nfInstanceId;
        }

        /**
         * @return The patch
         */
        public synchronized PatchItem[] getPatch()
        {
            return this.patch;
        }

        /**
         * @return The profile
         */
        public synchronized NFProfile getProfile()
        {
            return this.profile;
        }

        public synchronized NrfClient setCapacity(Integer capacity)
        {
            log.info("capacity={}", capacity);
            this.profile.setCapacity(capacity);
            return this;
        }

        public synchronized NrfClient setLoadInPercent(Integer loadInPercent)
        {
            log.info("loadInPercent={}", loadInPercent);
            this.profile.setLoad(loadInPercent);
            return this;
        }

        /**
         * @param patch The patch to set.
         * @return This NrfClient.
         */
        public synchronized NrfClient setPatch(final List<PatchItem> patch)
        {
            try
            {
                log.info("patch={}", json.writeValueAsString(patch));
            }
            catch (JsonProcessingException e)
            {
                throw new RuntimeException(e); // Should not happen.
            }

            for (PatchItem item : patch)
            {
                if (item.getPath().contains("load"))
                    this.profile.setLoad(Integer.valueOf(item.getValue().toString()));
            }

            this.patch = patch != null ? patch.toArray(new PatchItem[0]) : null;
            return this;
        }

        /**
         * @param profile The profile to set.
         * @return This NrfClient.
         */
        public NrfClient setProfile(final NFProfile profile)
        {
            try
            {
                log.info("profile={}", json.writeValueAsString(profile));
            }
            catch (JsonProcessingException e)
            {
                throw new RuntimeException(e); // Should not happen.
            }

            this.profile = profile;
            return this;
        }

        private synchronized PatchItem[] buildPatch()
        {
            if (this.patch != null)
            {
                // Retrieve capacity and load from profile (which is the buffer for those
                // values) and set them in the patch.

                for (int i = 0; i < this.patch.length; ++i)
                {
                    final PatchItem item = this.patch[i];

                    if (item.getPath().contains("load"))
                        item.setValue(this.profile.getLoad());
                }

                return this.patch;
            }

            final List<PatchItem> items = new ArrayList<>();
            PatchItem patch;

            patch = new PatchItem();
            patch.setOp(PatchOperation.REPLACE);
            patch.setPath("/nfStatus");
            patch.setValue(NFStatus.REGISTERED);
            items.add(patch);

            // For the time being, use the same load value for overall-load and
            // per-service-load.

            patch = new PatchItem();
            patch.setOp(PatchOperation.REPLACE);
            patch.setPath("/load");
            patch.setValue(this.loadMeter != null ? (int) Math.round(this.loadMeter.getCount()) : this.profile.getLoad()); // 0
                                                                                                                           // <=
                                                                                                                           // value[%]
                                                                                                                           // <=
                                                                                                                           // 100.
            items.add(patch);

            // SPR does not allow additional info in PATCH, leads to 400 Bad Request reply
//            for (int i = 0; i < this.profile.getNfServices().size(); ++i)
//            {
//                patch = new PatchItem();
//                patch.setOp(PatchOperation.REPLACE);
//                patch.setPath("/nfServices/" + i + "/load");
//                patch.setValue(this.profile.getLoad()); // 0 <= value[%] <= 100
//                items.add(patch);
//            }

            return items.toArray(new PatchItem[0]);
        }

        private synchronized NFProfile buildProfile()
        {
            if (this.profile == null)
            {
                this.profile = new NFProfile();
                this.profile.setNfInstanceId(this.nfInstanceId);
                this.profile.setNfStatus(NFStatus.REGISTERED);
                this.profile.setNfType(NFType.CHF);
            }

            if (this.loadMeter != null)
                this.profile.setLoad((int) Math.round(this.loadMeter.getCount()));

            return this.profile;
        }

        @Override
        protected Context nfInstanceDeregisterContextCreate()
        {
            return new Context(this.getUrl(),
                               new StringBuilder("/nf-instances/").append(this.nfInstanceId.toString()).toString(),
                               null,
                               NrfAdapter.NRF_REQUEST_TIMEOUT_IN_SECS);
        }

        @Override
        protected Context nfInstanceGetContextCreate()
        {
            return new Context(this.getUrl(),
                               new StringBuilder("/nf-instances/").append(this.nfInstanceId.toString()).toString(),
                               null,
                               NrfAdapter.NRF_REQUEST_TIMEOUT_IN_SECS);
        }

        @Override
        protected Context nfInstanceRegisterContextCreate()
        {
            String body;

            try
            {
                body = json.writeValueAsString(this.buildProfile());
            }
            catch (JsonProcessingException e)
            {
                body = "";
                log.error("Error creating JSON body. Cause: {}", Utils.toString(e, log.isDebugEnabled()));
            }

            return new Context(this.getUrl(),
                               new StringBuilder("/nf-instances/").append(this.nfInstanceId.toString()).toString(),
                               body,
                               NrfAdapter.NRF_REQUEST_TIMEOUT_IN_SECS);
        }

        @Override
        protected Context nfInstanceUpdateContextCreate()
        {
            String body;

            try
            {
                body = json.writeValueAsString(this.buildPatch());
            }
            catch (JsonProcessingException e)
            {
                body = "";
                log.error("Error creating JSON body. Cause: {}", Utils.toString(e, log.isDebugEnabled()));
            }

            return new Context(this.getUrl(),
                               new StringBuilder("/nf-instances/").append(this.nfInstanceId.toString()).toString(),
                               body,
                               NrfAdapter.NRF_REQUEST_TIMEOUT_IN_SECS);
        }
    }

    /**
     * Function/lambda interface for the common disturbance function
     */
    public interface RequestHandlerFunction
    {
        public void op(final RoutingContext routingContect);
    }

    public static class Test
    {
        public static void main() throws Exception
        {
            Result<NFProfile> result;

            final String PATH_TO_CERTIFICATES = "/home/eedstl/Projects/5g_proto/scripts/certificates/certm_worker/keys/chf";
            final String protocol = "https"; // "https"; // "http";
            final String host = "eric-nrfsim.5g-bsf-eedstl";
            final int port = 30217; // Ports for HTTP and HTTPS taken from k describe svc eric-nrfsim -->
                                    // NodePort: http 32348/TCP, NodePort: https 30217/TCP
            final String ip = "10.41.83.188"; // IP taken from k describe svc eric-nrfsim -->
                                              // Annotations:
                                              // field.cattle.io/publicEndpoints:
                                              // [{"addresses":["10.41.83.188"],"port":32348,"protocol":"TCP","serviceName":"5g-bsf-eedstl:eric-nrfsim","allNodes":true},{"addresses":["10....

            {
                log.info("===================================================");
                log.info("===== Handling through ChfSimulator.Admin API =====");
                log.info("===================================================");

                final ChfSimulator sim = new ChfSimulator("localhost", 5080, 5443, PATH_TO_CERTIFICATES, 4711, 2, "XXX", true, null, null);

                Completable.fromAction(sim::run).subscribeOn(Schedulers.io()).subscribe();

                final Admin.Nrf nrfAdmin = sim.getAdmin().getNrf();

                // Set the target address.
                nrfAdmin.setAddr(protocol, host, port, ip);

                // Start heartbeat and test some variations
//                nrfAdmin.setHeartbeat(true);
//                log.info("Starting heartbeat, PATCH every time ======================");
//                Thread.sleep(62000);
//                log.info("Starting heartbeat, PUT every 3rd time ======================");
//                nrfAdmin.setHeartbeatFullUpdateEveryNthTime(3);
//                Thread.sleep(152000);
//                log.info("Starting heartbeat, PUT every time ======================");
//                nrfAdmin.setHeartbeatFullUpdateEveryNthTime(1);
//                Thread.sleep(62000);
//                log.info("Starting heartbeat, PATCH every time ======================");
//                nrfAdmin.setHeartbeatFullUpdateEveryNthTime(0);
//                Thread.sleep(62000);
//                log.info("Stopping heartbeat ======================");
//                nrfAdmin.setHeartbeat(false);
//                Thread.sleep(1000);

                // Configure load and capacity to be used by the NF instance update requests
                // (PATCH).
                nrfAdmin.setLoadInPercent(30).setCapacity(5);

                // Send the NF instance update request and await result.
                result = nrfAdmin.doNfInstanceUpdate();

                // Print result obtained and check counters.
                log.info("nrfClient.statistics={}, result={}",
                         sim.getNrfClient().getStatistics(),
                         result.hasProblems() ? result.getProblems().toString() : result.getBody() != null ? result.getBody().toString() : null);

                if (sim.getNrfClient().getStatistics().getCountInHttpResponsesPerStatus().get(404).get() == 1)
                    log.info("Received update reply not found");

                // Create a new NF profile.
                final NFProfile profile = new NFProfile();
                profile.setNfInstanceId(sim.getNrfClient().getNfInstanceId());
                profile.setNfStatus(NFStatus.REGISTERED);
                profile.setNfType(NFType.CHF);
                profile.setLoad(22);
                profile.setCapacity(9);

                // Configure this profile for use with the next NF instance register request.
                nrfAdmin.setProfile(profile);

                // Send the NF instance register request and await result.
                result = nrfAdmin.doNfInstanceRegister();

                // Compare the profile sent with the profile obtained, ignoring the heart-beat
                // timer field.
                final NFProfile lhs = sim.getNrfClient().getProfile();
                final NFProfile rhs = result.getBody();
                lhs.setHeartBeatTimer(rhs.getHeartBeatTimer()); // To ignore, set both sides to the same value.

                if (Json.isEqual(lhs, rhs))
                    log.info("Received NFProfile matches registered NFProfile.");
                else
                    log.info("Received NFProfile does not match registered NFProfile.");

                // Print result obtained.
                log.info("nrfClient.statistics={}, result={}", sim.getNrfClient().getStatistics(), (result.getBody() != null ? result.getBody() : null));

                // Create a new PATCH.
                final List<PatchItem> items = new ArrayList<>();
                PatchItem patch;

                patch = new PatchItem();
                patch.setOp(PatchOperation.REPLACE);
                patch.setPath("/nfStatus");
                patch.setValue(NFStatus.REGISTERED);
                items.add(patch);

                patch = new PatchItem();
                patch.setOp(PatchOperation.REPLACE);
                patch.setPath("/load");
                patch.setValue(1); // 0 <= value[%] <= 100.
                items.add(patch);

                nrfAdmin.setPatch(items);

                // Send the NF instance update request and await result.
                nrfAdmin.doNfInstanceUpdate();

                // Configure load and capacity to be used by the NF instance update requests
                // (PATCH).
                nrfAdmin.setLoadInPercent(25).setCapacity(17);

                // Send the NF instance update request and await result.
                nrfAdmin.doNfInstanceUpdate();

                // Print result obtained and check counters.
                log.info("nrfClient.statistics={}, result={}",
                         sim.getNrfClient().getStatistics(),
                         result.hasProblems() ? result.getProblems() : result.getBody() != null ? result.getBody() : null);

                if (sim.getNrfClient().getStatistics().getCountInHttpResponsesPerStatus().get(204).get() == 1)
                    log.info("Everything is ok.");

                // Send the NF instance deregister request and await result.
                result = nrfAdmin.doNfInstanceDeregister();

                // Print result obtained.
                log.info("nrfClient.statistics={}, result={}",
                         sim.getNrfClient().getStatistics(),
                         result.hasProblems() ? result.getProblems() : result.getBody() != null ? result.getBody() : null);
            }

            {
                log.info("==================================================================================================");
                log.info("===== Configuration using ChfSimulator.NrfClient directly (without instance of ChfSimulator) =====");
                log.info("==================================================================================================");

                final ChfSimulator.NrfClient client = new NrfClient(new WebClient(PATH_TO_CERTIFICATES + "/certificate.pem",
                                                                                  PATH_TO_CERTIFICATES + "/key.pem",
                                                                                  PATH_TO_CERTIFICATES + "/ca.pem"));
                client.setAddr(protocol, host, port, ip);

                // Start heartbeat and test some variations
//                client.start().blockingAwait();
//                log.info("Starting heartbeat, PATCH every time ======================");
//                Thread.sleep(62000);
//                log.info("Starting heartbeat, PUT every 3rd time ======================");
//                client.setHeartbeatFullUpdateEveryNthTime(3);
//                Thread.sleep(152000);
//                log.info("Starting heartbeat, PUT every time ======================");
//                client.setHeartbeatFullUpdateEveryNthTime(1);
//                Thread.sleep(62000);
//                log.info("Starting heartbeat, PATCH every time ======================");
//                client.setHeartbeatFullUpdateEveryNthTime(0);
//                Thread.sleep(62000);
//                log.info("Stopping heartbeat ======================");
//                client.stop().blockingAwait();
//                Thread.sleep(1000);

                // Configure load and capacity to be used by the NF instance update requests
                // (PATCH).
                client.setLoadInPercent(30).setCapacity(5);

                // Send the NF instance update request and await result.
                result = client.nfInstanceUpdate().blockingGet();

                // Print result obtained and check counters.
                log.info("nrfClient.statistics={}, result={}",
                         client.getStatistics(),
                         result.hasProblems() ? result.getProblems() : result.getBody() != null ? result.getBody() : null);

                if (client.getStatistics().getCountInHttpResponsesPerStatus().get(404).get() == 1)
                    log.info("Received update reply not found");

                // Create a new NF profile.
                final NFProfile profile = new NFProfile();
                profile.setNfInstanceId(client.getNfInstanceId());
                profile.setNfStatus(NFStatus.REGISTERED);
                profile.setNfType(NFType.CHF);
                profile.setLoad(22);
                profile.setCapacity(9);

                // Configure the client with this profile for use with the next NF instance
                // register request.
                client.setProfile(profile);

                // Send the NF instance register request and await result.
                result = client.nfInstanceRegister().blockingGet();

                // Compare the profile sent with the profile obtained, ignoring the heart-beat
                // timer field.
                final NFProfile lhs = client.getProfile();
                final NFProfile rhs = result.getBody();
                lhs.setHeartBeatTimer(rhs.getHeartBeatTimer()); // To ignore, set both sides to the same value.

                if (Json.isEqual(lhs, rhs))
                    log.info("Received NFProfile matches registered NFProfile.");
                else
                    log.info("Received NFProfile does not match registered NFProfile.");

                // Print result obtained.
                log.info("nrfClient.statistics={}, result={}",
                         client.getStatistics(),
                         result.hasProblems() ? result.getProblems() : result.getBody() != null ? result.getBody() : null);

                // Create a new PATCH.
                final List<PatchItem> items = new ArrayList<>();
                PatchItem patch;

                patch = new PatchItem();
                patch.setOp(PatchOperation.REPLACE);
                patch.setPath("/nfStatus");
                patch.setValue(NFStatus.REGISTERED);
                items.add(patch);

                patch = new PatchItem();
                patch.setOp(PatchOperation.REPLACE);
                patch.setPath("/load");
                patch.setValue(1); // 0 <= value[%] <= 100.
                items.add(patch);

                client.setPatch(items);

                // Send the NF instance update request and await result.
                client.nfInstanceUpdate().blockingGet();

                // Configure load and capacity to be used by the NF instance update requests
                // (PATCH).
                client.setLoadInPercent(25).setCapacity(17);

                // Send the NF instance update request and await result.
                result = client.nfInstanceUpdate().blockingGet();

                // Print result obtained.
                log.info("nrfClient.statistics={}, result={}", client.getStatistics(), (result.getBody() != null ? result.getBody() : null));

                if (client.getStatistics().getCountInHttpResponsesPerStatus().get(204).get() == 1)
                    log.info("Everything is ok.");

                // Send the NF instance deregister request and await result.
                result = client.nfInstanceDeregister().blockingGet();

                // Print result obtained.
                log.info("nrfClient.statistics={}, result={}", client.getStatistics(), (result.getBody() != null ? result.getBody() : null));
            }

            System.exit(0);
        }
    }

    private static class CommandInfo extends MonitorAdapter.CommandBase
    {
        private ChfSimulator handler;

        public CommandInfo(final ChfSimulator handler)
        {
            super("info",
                  "Usage: command=info[type[=<cc|slc>] &inRequests[=<true|false>] | &outAnswers[=<true|false>]  | &outRequests[=<true|false>]  | &inAnswers[=<true|false>]  | &clear[=<true|false>]");
            this.handler = handler;
        }

        @Override
        public HttpResponseStatus execute(final com.ericsson.adpal.ext.monitor.api.v0.commands.Result result,
                                          final Command request)
        {

            final var type = (String) request.getAdditionalProperties().get("type");
            final var inRequests = Boolean.parseBoolean((String) request.getAdditionalProperties().get("inRequests"));
            final var outAnswers = Boolean.parseBoolean((String) request.getAdditionalProperties().get("outAnswers"));
            final var outRequests = Boolean.parseBoolean((String) request.getAdditionalProperties().get("outRequests"));
            final var inAnswers = Boolean.parseBoolean((String) request.getAdditionalProperties().get("inAnswers"));

            final var clear = Boolean.parseBoolean((String) request.getAdditionalProperties().get("clear"));

            log.info("type='{}'", type);
            log.info("inRequests='{}'", inRequests);
            log.info("outAnswers='{}'", outAnswers);
            log.info("outRequests='{}'", outRequests);
            log.info("inAnswers='{}'", inAnswers);
            log.info("clear='{}'", clear);

            if (clear)
            {
                this.handler.getInstances().stream().forEach(inst -> inst.getValue().clear());
                this.handler.getSubscriptions().stream().forEach(inst -> inst.getValue().clear());
                return HttpResponseStatus.OK;
            }

            if (type == null || type.isEmpty())
            {
                result.setAdditionalProperty("errorMessage", HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Invalid argument: 'type'.");
                return HttpResponseStatus.BAD_REQUEST;
            }

            var instances = type.equals("cc") ? this.handler.getInstances() : this.handler.getSubscriptions();

            while (instances.iterator().hasNext())
            {
                var instance = instances.iterator().next();

                if (inRequests)
                {
                    result.setAdditionalProperty("inRequests", instance.getValue().getCountInHttpRequests());
                    return HttpResponseStatus.OK;
                }
                else if (outAnswers)
                {
                    result.setAdditionalProperty("outAnswers", instance.getValue().getCountOutHttpResponsesPerStatus());
                    return HttpResponseStatus.OK;
                }
                else if (outRequests)
                {
                    result.setAdditionalProperty("outRequests", instance.getValue().getCountOutHttpRequests());
                    return HttpResponseStatus.OK;
                }
                else if (inAnswers)
                {
                    result.setAdditionalProperty("inAnswers", instance.getValue().getCountInHttpResponsesPerStatus());
                    return HttpResponseStatus.OK;
                }
            }
            result.setAdditionalProperty("errorMessage", HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Invalid argument.");
            return HttpResponseStatus.BAD_REQUEST;
        }
    }

    private class RedisInitializer
    {
        private int numRetries = 3;
        private String containerName = REDIS_CONTAINER_NAME;
        private int containerPort = REDIS_CONTAINER_PORT;
        private final String chfsimDeployment = System.getProperty("chfsim.deployment") != null ? System.getProperty("chfsim.deployment") : "deployed";

        public RedisInitializer(String redisContainerName,
                                int redisContainerPort)
        {
            ChfSimulator.this.redis = null;

            if (!(redisContainerName == null))
            {
                this.containerName = redisContainerName;
            }

            if (!(redisContainerPort == REDIS_CONTAINER_PORT))
            {
                this.containerPort = redisContainerPort;
            }

        }

        public void start()
        {
            if (this.numRetries-- > 0)
                this.createRedisClient();
        }

        private void createRedisClient()
        {
            // Initialize the Redis client
            try
            {
                var localhost = isIpv6() ? "[::1]" : "127.0.0.1";
                var host = chfsimDeployment.equalsIgnoreCase("jcat") ? localhost : this.containerName;

                Redis.createClient(ChfSimulator.this.vertx,
                                   new RedisOptions().setConnectionString("redis://" + host + ":" + this.containerPort).setPassword("ericsson123!"))
                     .connect(onConnect ->
                     {
                         if (onConnect.succeeded())
                         {
                             log.info("REDIS connection successful");

                             ChfSimulator.this.redis = RedisAPI.api(onConnect.result());
                             // Delete keys to have a fresh start
                             ChfSimulator.this.redis.eval(Arrays.asList("local keys = redis.call('keys', ARGV[1]) \n for i=1,#keys,5000 do \n redis.call('del', unpack(keys, i, math.min(i+4999, #keys))) \n end \n return keys",
                                                                        "0",
                                                                        ":*"),
                                                          res ->
                                                          {
                                                          });
                         }
                         else
                         {
                             log.info("REDIS connection not successful. Cause: {}", onConnect.cause());

                             ChfSimulator.this.vertx.setTimer(2000, timer -> this.start());
                         }
                     });
            }
            catch (UnknownHostException e)
            {
                log.error("Error connection to REDIS. Cause: {}", e.toString());
            }
        }
    }

    /**
     * NCHF specific constants
     */
    public static final String API_CONVERGED_CHARGING = "/nchf-convergedcharging";
    public static final String API_SPENDING_LIMIT_CONTROL = "/nchf-spendinglimitcontrol";

    public static final String PATH_CHARGINGDATA = "/chargingdata";
    public static final String PATH_SUBSCRIPTIONS = "/subscriptions";

    public static final String OP_UPDATE = "/update";
    public static final String OP_DELETE = "/delete";
    public static final String OP_RELEASE = "/release";
    public static final String OP_NOTIFY = "/notify";
    public static final String OP_NOTIFY_CC_RECEIVER = "/notify_cc_receiver";
    public static final String OP_TERMINATE = "/terminate";

    private static final String CH_INSTANCE_ID = "chInstanceId";

    private static final String ENV_POD_IPS = "POD_IPS";

    private static final String SUBSCRIPTION_ID = "subscriptionId";
    private static final String CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";

    private static final String HD_CONTENT_TYPE = "Content-Type";
    private static final String HD_LOCATION = "Location";
    private static final String HD_X_NOTIFY_URI = "x-notify-uri";
    private static final String HD_3GPP_SBI_TARGET_APIROOT = "3gpp-Sbi-Target-apiRoot";
    private static final String HD_3GPP_SBI_NF_PEER_INFO = "3gpp-Sbi-NF-Peer-Info";

    /**
     * China Telecom (中国电信) related NCHF constants
     */
    public static final String API_CT_CONVERGED_CHARGING_NOTIFY_CG = "/nchf-convergedcharging/notify-cg";

    /**
     * 
     * Environment used by the CHF Simulator.
     */
    private static final String REDIS_KEY_FLEXI_DISTURBANCE = "flexiDist";

    // The body is just there to have a realistically sized message, the contents
    // are so not relevant:
    private static final String CREATE_RESPONSE_BODY = "{\"invocationSequenceNumber\":0,\"invocationTimeStamp\":\"2019-03-28T14:30:50.814+0100\",\"multipleUnitInformation\":[{\"quotaHoldingTime\":82400,\"uPFID\":\"123e-e8b-1d3-a46-421\",\"validityTime\":\"2019-03-29T13:24:10.812+0100\",\"grantedUnit\":{\"totalVolume\":211},\"ratingGroup\":100,\"resultCode\":\"SUCCESS\",\"volumeQuotaThreshold\":104857}],\"pDUSessionChargingInformation\":{\"chargingId\":123,\"userInformation\":{\"servedGPSI\":\"msisdn-77117777\",\"servedPEI\":\"imei-234567891098765\",\"unauthenticatedFlag\":true,\"roamerInOut\":\"OUT_BOUND\"},\"userLocationinfo\":{\"eutraLocation\":{\"tai\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"tac\":\"ab01\"},\"ecgi\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"eutraCellId\":\"abcAB12\"},\"ageOfLocationInformation\":32766,\"ueLocationTimestamp\":\"2019-03-28T14:30:50Z\",\"geographicalInformation\":\"234556ABCDEF2345\",\"geodeticInformation\":\"ABCDEFAB123456789023\",\"globalNgenbId\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"n3IwfId\":\"ABCD123\",\"ngRanNodeId\":\"MacroNGeNB-abc92\"}},\"nrLocation\":{\"tai\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"tac\":\"ab01\"},\"ncgi\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"nrCellId\":\"ABCabc123\"},\"ageOfLocationInformation\":1,\"ueLocationTimestamp\":\"2019-03-28T14:30:50Z\",\"geographicalInformation\":\"AB12334765498F12\",\"geodeticInformation\":\"AB12334765498F12ACBF\",\"globalGnbId\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"n3IwfId\":\"ABCD123\",\"ngRanNodeId\":\"MacroNGeNB-abc92\"}},\"n3gaLocation\":{\"n3gppTai\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"tac\":\"ab01\"},\"n3IwfId\":\"ABCD123\",\"ueIpv4Addr\":\"192.168.0.1\",\"ueIpv6Addr\":\"2001:db8:85a3:8d3:1319:8a2e:370:7348\",\"portNumber\":1}},\"userLocationTime\":\"2019-03-28T14:30:50Z\",\"uetimeZone\":\"+05:30\",\"pduSessionInformation\":{\"networkSlicingInfo\":{\"sNSSAI\":{\"sst\":0,\"sd\":\"Aaa123\"}},\"pduSessionID\":1,\"pduType\":\"IPV4\",\"sscMode\":\"SSC_MODE_1\",\"hPlmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"servingNodeID\":[{\"plmnId\":{\"mcc\":\"311\",\"mnc\":\"280\"},\"amfId\":\"ABab09\"}],\"servingNetworkFunctionID\":{\"servingNetworkFunctionName\":\"SMF\",\"servingNetworkFunctionInstanceid\":\"SMF_Instanceid_1\",\"gUAMI\":{\"plmnId\":{\"mcc\":\"311\",\"mnc\":\"280\"},\"amfId\":\"ABab09\"}},\"ratType\":\"EUTRA\",\"dnnId\":\"DN-AAA\",\"chargingCharacteristics\":\"AB\",\"chargingCharacteristicsSelectionMode\":\"HOME_DEFAULT\",\"startTime\":\"2019-03-28T14:30:50Z\",\"3gppPSDataOffStatus\":\"ACTIVE\",\"pduAddress\":{\"pduIPv4Address\":\"192.168.0.1\",\"pduIPv6Address\":\"2001:db8:85a3:8d3:1319:8a2e:370:7348\",\"pduAddressprefixlength\":0,\"IPv4dynamicAddressFlag\":true,\"IPv6dynamicAddressFlag\":true},\"qoSInformation\":{\"5qi\":254},\"servingCNPlmnId\":{\"mcc\":\"311\",\"mnc\":\"280\"}},\"unitCountInactivityTimer\":180},\"triggers\":[{\"triggerType\":\"QOS_CHANGE\",\"triggerCategory\":\"DEFERRED_REPORT\"},{\"triggerType\":\"USER_LOCATION_CHANGE\",\"triggerCategory\":\"DEFERRED_REPORT\"},{\"triggerType\":\"SERVING_NODE_CHANGE\",\"triggerCategory\":\"DEFERRED_REPORT\"},{\"triggerType\":\"CHANGE_OF_3GPP_PS_DATA_OFF_STATUS\",\"triggerCategory\":\"DEFERRED_REPORT\"},{\"triggerType\":\"UE_TIMEZONE_CHANGE\",\"triggerCategory\":\"IMMEDIATE_REPORT\"},{\"triggerType\":\"PLMN_CHANGE\",\"triggerCategory\":\"IMMEDIATE_REPORT\"},{\"triggerType\":\"RAT_CHANGE\",\"triggerCategory\":\"IMMEDIATE_REPORT\"},{\"triggerType\":\"UNUSED_QUOTA_TIMER\",\"triggerCategory\":\"IMMEDIATE_REPORT\"},{\"triggerType\":\"ADDITION_OF_UPF\",\"triggerCategory\":\"IMMEDIATE_REPORT\"},{\"triggerType\":\"REMOVAL_OF_UPF\",\"triggerCategory\":\"IMMEDIATE_REPORT\"},{\"timeLimit\":30,\"triggerType\":\"TIME_LIMIT\",\"triggerCategory\":\"IMMEDIATE_REPORT\"},{\"volumeLimit\":500,\"triggerType\":\"VOLUME_LIMIT\",\"triggerCategory\":\"IMMEDIATE_REPORT\"},{\"maxNumberOfccc\":5,\"triggerType\":\"MAX_NUMBER_OF_CHANGES_IN CHARGING_CONDITIONS\",\"triggerCategory\":\"IMMEDIATE_REPORT\"}]}";
    private static final String UPDATE_RESPONSE_BODY = "{\"invocationSequenceNumber\":1,\"invocationTimeStamp\":\"2019-03-28T14:30:51.888+0100\",\"multipleUnitInformation\":[{\"quotaHoldingTime\":82400,\"uPFID\":\"123e-e8b-1d3-a46-421\",\"validityTime\":\"2019-03-29T13:24:11.885+0100\",\"grantedUnit\":{\"totalVolume\":211},\"ratingGroup\":100,\"resultCode\":\"SUCCESS\",\"volumeQuotaThreshold\":104857}],\"pDUSessionChargingInformation\":{\"chargingId\":123,\"userInformation\":{\"servedGPSI\":\"msisdn-77117777\",\"servedPEI\":\"imei-234567891098765\",\"unauthenticatedFlag\":true,\"roamerInOut\":\"OUT_BOUND\"},\"userLocationinfo\":{\"eutraLocation\":{\"tai\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"tac\":\"ab01\"},\"ecgi\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"eutraCellId\":\"abcAB12\"},\"ageOfLocationInformation\":32766,\"ueLocationTimestamp\":\"2019-03-28T14:30:51Z\",\"geographicalInformation\":\"234556ABCDEF2345\",\"geodeticInformation\":\"ABCDEFAB123456789023\",\"globalNgenbId\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"n3IwfId\":\"ABCD123\",\"ngRanNodeId\":\"MacroNGeNB-abc92\"}},\"nrLocation\":{\"tai\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"tac\":\"ab01\"},\"ncgi\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"nrCellId\":\"ABCabc123\"},\"ageOfLocationInformation\":1,\"ueLocationTimestamp\":\"2019-03-28T14:30:51Z\",\"geographicalInformation\":\"AB12334765498F12\",\"geodeticInformation\":\"AB12334765498F12ACBF\",\"globalGnbId\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"n3IwfId\":\"ABCD123\",\"ngRanNodeId\":\"MacroNGeNB-abc92\"}},\"n3gaLocation\":{\"n3gppTai\":{\"plmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"tac\":\"ab01\"},\"n3IwfId\":\"ABCD123\",\"ueIpv4Addr\":\"192.168.0.1\",\"ueIpv6Addr\":\"2001:db8:85a3:8d3:1319:8a2e:370:7348\",\"portNumber\":1}},\"userLocationTime\":\"2019-03-28T14:30:51Z\",\"uetimeZone\":\"+05:30\",\"pduSessionInformation\":{\"networkSlicingInfo\":{\"sNSSAI\":{\"sst\":0,\"sd\":\"Aaa123\"}},\"pduSessionID\":1,\"pduType\":\"IPV4\",\"sscMode\":\"SSC_MODE_1\",\"hPlmnId\":{\"mcc\":\"374\",\"mnc\":\"645\"},\"servingNodeID\":[{\"plmnId\":{\"mcc\":\"311\",\"mnc\":\"280\"},\"amfId\":\"ABab09\"}],\"servingNetworkFunctionID\":{\"servingNetworkFunctionName\":\"SMF\",\"servingNetworkFunctionInstanceid\":\"SMF_Instanceid_1\",\"gUAMI\":{\"plmnId\":{\"mcc\":\"311\",\"mnc\":\"280\"},\"amfId\":\"ABab09\"}},\"ratType\":\"EUTRA\",\"dnnId\":\"DN-AAA\",\"chargingCharacteristics\":\"AB\",\"chargingCharacteristicsSelectionMode\":\"HOME_DEFAULT\",\"3gppPSDataOffStatus\":\"ACTIVE\",\"pduAddress\":{\"pduIPv4Address\":\"192.168.0.1\",\"pduIPv6Address\":\"2001:db8:85a3:8d3:1319:8a2e:370:7348\",\"pduAddressprefixlength\":0,\"IPv4dynamicAddressFlag\":true,\"IPv6dynamicAddressFlag\":true},\"qoSInformation\":{\"5qi\":254},\"servingCNPlmnId\":{\"mcc\":\"311\",\"mnc\":\"280\"},\"startTime\":\"2019-03-28T14:30:50Z\"},\"unitCountInactivityTimer\":180}}";
    private static final String SUBSCRIBE_RESPONSE_BODY = "{\"statusInfos\":{\"71008\":{\"policyCounterId\":\"71008\",\"currentStatus\":\"active\"},\"71009\":{\"policyCounterId\":\"71009\",\"currentStatus\":\"active\"},\"71300\":{\"policyCounterId\":\"71300\",\"currentStatus\":\"active\"},\"71000\":{\"policyCounterId\":\"71000\",\"currentStatus\":\"active\"}},\"supi\":\"imsi-310310140000120\"}";
    private static final String MODIFY_RESPONSE_BODY = "{\"statusInfos\":{\"71008\":{\"policyCounterId\":\"71008\",\"currentStatus\":\"active\"},\"71009\":{\"policyCounterId\":\"71009\",\"currentStatus\":\"active\"},\"71300\":{\"policyCounterId\":\"71300\",\"currentStatus\":\"active\"},\"71000\":{\"policyCounterId\":\"71000\",\"currentStatus\":\"active\"}},\"supi\":\"imsi-310310140000120\"}";

    private static final int HTTP_KEEP_ALIVE_TIMEOUT_SECS = 24 * 60 * 60; // [s] Maximum time with no traffic on the line.
    private static final int HTTP_CONNECT_TIMEOUT_MILLIS = 3000; // [ms]

    private static final String REDIS_CONTAINER_NAME = "eric-chfsim-redis";
    private static final int REDIS_CONTAINER_PORT = 6379;

    private static final Logger log = LoggerFactory.getLogger(ChfSimulator.class);
    private static final ObjectMapper json = OpenApiObjectMapper.singleton();

    public static void main(String[] args)
    {
        int exitStatus = 0;

        log.info("Started CHF simulator, version: {}", VersionInfo.get());

        try
        {
            final List<String> hosts = Stream.of(EnvVars.get(ENV_POD_IPS, isIpv6() ? "::" : "0.0.0.0").split(","))
                                             .map(ip -> ip.strip())
                                             .map(ip -> ip.contains(".") ? ip : "[" + ip + "]")
                                             .collect(Collectors.toList());

            log.info("POD_IPS={}, hosts={}", EnvVars.get(ENV_POD_IPS), hosts);

            ChfSimulator app = new ChfSimulator(hosts);
            app.run();
        }
        catch (Exception e)
        {
            log.error("Exception caught, stopping simulator.", e);
            exitStatus = 1;
        }

        log.info("Stopped CHF simulator.");

        System.exit(exitStatus);
    }

    private static boolean isIpv6() throws UnknownHostException
    {
        String serviceName = EnvVars.get("SERVICE_NAME");

        if (serviceName != null)
            serviceName = serviceName.toUpperCase().replace("-", "_");

        final boolean result = InetAddress.getByName(EnvVars.get(serviceName + "_PORT_80_TCP_ADDR")) instanceof Inet6Address;
        log.info("result={}", result);
        return result;
    }

    private final Statistics.ChargingInstance.Pool chInstances;
    private final Statistics.Subscription.Pool subscriptions;
    private final Statistics.Nrf.Pool nrfInstances;
    private final Vertx vertx;
    private final List<WebServerPool> webServerExt;
    private final List<WebServerPool> webServerExtTls;
    private final WebClient webClient;
    private final Admin admin;
    private final List<String> ips;
    private final Integer port;
    private WebServer webServerInt;
    private MonitorAdapter monitored;
    private String prefix = "";
    private int startIds = 1;
    private int numIds = 100000;
    private SessionDataStore sessionDataStore;
    private RedisAPI redis;
    private NrfClient nrfClient;
    private LoadMeter loadMeter;
    private String uuid = "";
    private Set<String> allocatedNewIdRefs;

    private List<RoutingContext> contexts = new ArrayList<>();

    public synchronized void addContext(RoutingContext context)
    {
        if (this.contexts.size() > 32)
        {
            log.warn("Already 32 contexts in list. Current context not added: {}", context);
            return;
        }

        this.contexts.add(context);
    }

    public synchronized Map<String, Set<String>> getAllRequestHeaders()
    {
        return this.contexts.stream()
                            .flatMap(rc -> rc.request().headers().entries().stream())
                            .collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toSet())));
    }

    public synchronized List<RoutingContext> getContexts()
    {
        return this.contexts;
    }

    public synchronized List<String> getRequestHeader(String headerName)
    {
        return this.contexts.stream()
                            .filter(rc -> rc.request().getHeader(headerName) != null)
                            .flatMap(rc -> rc.request().headers().getAll(headerName).stream())
                            .collect(Collectors.toList());
    }

    public synchronized void clearContexts()
    {
        this.contexts = new ArrayList<>();
    }

    /**
     * The following constructors are used by CHF simulator in stand-alone mode.
     */
    public ChfSimulator(final List<String> hosts) throws Exception
    {
        this(hosts, 80, 443);
    }

    public ChfSimulator(String host,
                        Integer port,
                        Integer portTls) throws Exception
    {
        this(List.of(host), port, portTls);
    }

    public ChfSimulator(List<String> hosts,
                        Integer port,
                        Integer portTls) throws Exception
    {
        // Regarding below START_IDS and NUM_IDS:
        // Get the start-value of the number-series for the CC+SLC session IDs
        // and how many IDs to generate before we roll over/exhaust the pool.
        // The values come in via environment variables defined typically in
        // the Helm chart.

        this(hosts,
             port,
             portTls,
             EnvVars.get("CHF_CERTIFICATES_PATH"),
             EnvVars.get("START_IDS") != null ? Integer.parseInt(EnvVars.get("START_IDS")) : 1,
             EnvVars.get("NUM_IDS") != null ? Integer.parseInt(EnvVars.get("NUM_IDS")) : 100000,
             EnvVars.get("CHF_DATA_REF_PREFIX") != null ? EnvVars.get("CHF_DATA_REF_PREFIX") : "CHFSIM-???",
             !Boolean.valueOf(EnvVars.get("CHF_USE_HTTP1")),
             null,
             null); // Default is HTTP2

        // Override settings from above constructor:

        this.webServerInt = WebServer.builder().withHost(com.ericsson.utilities.common.Utils.getLocalAddress()).withPort(8080).build(this.vertx);

        this.monitored = new MonitorAdapter(this.webServerInt,
                                            Arrays.asList(new MonitorAdapter.CommandCounter(this), new MonitorAdapter.CommandEsa(this), new CommandInfo(this)),
                                            Arrays.asList());

        if (EnvVars.get("ERIC_PM_SERVER_SERVICE_PORT") != null)
            this.loadMeter = new LoadMeter(VertxInstance.get());

        this.nrfClient = new NrfClient(this.webClient, this.loadMeter);
        this.admin.loadTestMode = true;
    }

    /**
     * The following constructors are used by JCAT function test.
     */

    public ChfSimulator(final String host,
                        final Integer port,
                        final Integer portTls,
                        final String certificatesPath,
                        final Integer datarefStart,
                        final Integer datarefNum,
                        final String prefix,
                        final boolean useHttp2,
                        final KeyCert keyCert,
                        final TrustedCert trustedCert)
    {
        this(List.of(host), port, portTls, certificatesPath, datarefStart, datarefNum, prefix, useHttp2, null, REDIS_CONTAINER_PORT, keyCert, trustedCert);
    }

    public ChfSimulator(final List<String> hosts,
                        final Integer port,
                        final Integer portTls,
                        final String certificatesPath,
                        final Integer datarefStart,
                        final Integer datarefNum,
                        final String prefix,
                        final boolean useHttp2,
                        final KeyCert keyCert,
                        final TrustedCert trustedCert)
    {
        this(hosts, port, portTls, certificatesPath, datarefStart, datarefNum, prefix, useHttp2, null, REDIS_CONTAINER_PORT, keyCert, trustedCert);
    }

    /**
     * This constructor is used by both CHF simulator in stand-alone mode and CHF
     * simulator running directly in JCAT function test.
     * 
     * @param host
     * @param port
     * @param portTls
     * @param certificatesPath
     * @param datarefStart
     * @param datarefNum
     * @param prefix
     * @param useHttp2           (optional)
     * @param redisContainerName (optional)
     */
    public ChfSimulator(final String host,
                        final Integer port,
                        final Integer portTls,
                        final String certificatesPath,
                        final Integer datarefStart,
                        final Integer datarefNum,
                        final String prefix,
                        final boolean useHttp2,
                        final String redisContainerName,
                        final int redisContainerPort,
                        final KeyCert keyCert,
                        final TrustedCert trustedCert)
    {
        this(List.of(host),
             port,
             portTls,
             certificatesPath,
             datarefStart,
             datarefNum,
             prefix,
             useHttp2,
             redisContainerName,
             redisContainerPort,
             keyCert,
             trustedCert);
    }

    public ChfSimulator(final List<String> hosts,
                        final Integer port,
                        final Integer portTls,
                        final String certificatesPath,
                        final Integer datarefStart,
                        final Integer datarefNum,
                        final String prefix,
                        final boolean useHttp2,
                        final String redisContainerName,
                        final int redisContainerPort,
                        final KeyCert keyCert,
                        final TrustedCert trustedCert)
    {
        this.chInstances = new Statistics.ChargingInstance.Pool();
        this.subscriptions = new Statistics.Subscription.Pool();
        this.nrfInstances = new Statistics.Nrf.Pool();

        // Only used for logging:
        this.ips = hosts;
        this.port = port;

        this.prefix = prefix;
        this.startIds = datarefStart;
        this.numIds = datarefNum;
        // fixed uuid is used as new session id with load_test_mode on in CC
        this.uuid = UUID.randomUUID().toString();

        this.sessionDataStore = new SessionDataStore(this.startIds, this.numIds, this.prefix);
        // new session id with p-value is not stored in SessionDataStore, because it's
        // designed only for set of ints. new sessionid now stored in
        // allocatedNewIdRefs, which is string set.
        this.allocatedNewIdRefs = ConcurrentHashMap.<String>newKeySet();

        final String path = certificatesPath != null ? certificatesPath : "";
        final boolean useTls = !path.isEmpty();

        this.vertx = VertxInstance.get();
        final int numReplicas = VertxInstance.getOptions().getEventLoopPoolSize();
        log.info("numReplicas={}, hosts={}, port={}, portTls={}, useTls={}", numReplicas, hosts, port, portTls, useTls);

        this.webServerExt = hosts.stream()
                                 .map(host -> WebServer.builder()
                                                       .withHost(host)
                                                       .withPort(port)
                                                       .withOptions(options -> options.getInitialSettings().setMaxConcurrentStreams(1000))
                                                       .build(this.vertx, numReplicas))
                                 .collect(Collectors.toList());

        if (keyCert != null && trustedCert != null)
        {
            log.info("Creating tls server with DynamicTlsCertManager");
            final DynamicTlsCertManager certManager = DynamicTlsCertManager.create(new KeyCertProvider()
            {
                @Override
                public Flowable<KeyCert> watchKeyCert()
                {
                    return Single.just(keyCert).toFlowable();
                }
            }, new TrustedCertProvider()
            {

                @Override
                public Flowable<TrustedCert> watchTrustedCerts()
                {
                    return Single.just(trustedCert).toFlowable();
                }
            });

            this.webServerExtTls = hosts.stream()
                                        .map(host -> WebServer.builder()
                                                              .withHost(host)
                                                              .withPort(portTls)
                                                              .withDynamicTls(certManager)
                                                              .withOptions(options -> options.getInitialSettings().setMaxConcurrentStreams(1000))
                                                              .build(this.vertx, numReplicas))
                                        .collect(Collectors.toList());
        }
        else
        {
            log.info("Creating tls server with cert path");
            this.webServerExtTls = useTls ? hosts.stream()
                                                 .map(host -> WebServer.builder()
                                                                       .withHost(host)
                                                                       .withPort(portTls)
                                                                       .withTls(certificatesPath)
                                                                       .build(this.vertx, numReplicas))
                                                 .collect(Collectors.toList())
                                          : null;
        }

        if (useTls && this.webServerExtTls != null)
            this.webServerExtTls.forEach(webServer -> webServer.getHttpOptions().setSni(true));

        if (keyCert != null && trustedCert != null)
        {
            final PemTrustOptions pemTrustOptions = new PemTrustOptions();
            trustedCert.getTrustedCertificate().forEach(ca -> pemTrustOptions.addCertValue(io.vertx.core.buffer.Buffer.buffer(ca)));

            this.webClient = new WebClient(keyCert.getCertificate(), keyCert.getPrivateKey(), trustedCert.getTrustedCertificate())
            {
                @Override
                protected WebClientOptions modifyOptions(final WebClientOptions options)
                {
                    options.setKeepAliveTimeout(HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                           .setEnabledSecureTransportProtocols(Set.of("TLSv1.2"))
                           .setConnectTimeout(HTTP_CONNECT_TIMEOUT_MILLIS);

                    if (useHttp2)
                        return options.setHttp2KeepAliveTimeout(ChfSimulator.HTTP_KEEP_ALIVE_TIMEOUT_SECS);

                    return options.setProtocolVersion(HttpVersion.HTTP_2).setKeepAlive(true);
                }
            };
        }
        else
        {
            this.webClient = new WebClient(path + "/certificate.pem", path + "/key.pem", path + "/ca.pem")
            {
                @Override
                protected WebClientOptions modifyOptions(final WebClientOptions options)
                {
                    return options.setKeepAliveTimeout(HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                                  .setHttp2KeepAliveTimeout(HTTP_KEEP_ALIVE_TIMEOUT_SECS)
                                  .setConnectTimeout(HTTP_CONNECT_TIMEOUT_MILLIS);
                }
            };
        }

        // Not used from plain Java
        this.webServerInt = null;
        this.monitored = null;
        this.loadMeter = null;

        this.nrfClient = new NrfClient(this.webClient, this.loadMeter);

        this.admin = new Admin(this.vertx, this.webServerExt, this.webServerExtTls);
        this.admin.loadTestMode = false;
        this.admin.loadTestStatisticsMode = false;

        String redContainerName = null;

        if (!(redisContainerName == null || redisContainerName.matches(REDIS_CONTAINER_NAME + "-\\w{1,15}$")))
        {
            redContainerName = null;
        }
        else
        {
            redContainerName = redisContainerName;
        }

        int redContainerPort;
        int redContainerPortRange;

        redContainerPortRange = redisContainerPort >= REDIS_CONTAINER_PORT ? redisContainerPort - REDIS_CONTAINER_PORT
                                                                           : REDIS_CONTAINER_PORT - redisContainerPort;

        if (!(redisContainerPort == REDIS_CONTAINER_PORT || (redContainerPortRange >= 0 && redContainerPortRange < 100)))
        {
            redContainerPort = REDIS_CONTAINER_PORT;
        }
        else
        {
            redContainerPort = redisContainerPort;
        }

        // Initialize the rest, which is common for both constructors:
        this.init(redContainerName, redContainerPort);
    }

    /**
     * The interface for administration of the CHF-simulator.
     * 
     * @return The interface for administration of the CHF-simulator.
     */
    public Admin getAdmin()
    {
        return this.admin;
    }

    public ChargingInstance getChargingInstance(String id)
    {
        return this.chInstances.get(id);
    }

    /**
     * @see com.ericsson.adpal.ext.monitor.MonitorAdapter.CommandCounter.Provider#
     *      getCounters()
     */
    @Override
    public List<Counter> getCounters(final boolean readThenClear)
    {
        final List<Counter> result = new ArrayList<>();

        {
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> inRequestsPerIpFamily = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> outAnswersPerIpFamily = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> inRequests = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> outAnswers = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> outRequests = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> inAnswers = new ArrayList<>();

            this.chInstances.stream().forEach(instance ->
            {
                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(CH_INSTANCE_ID)).append('=').append(Arrays.asList(instance.getKey()));
                    inRequests.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(),
                                                                                               (double) instance.getValue()
                                                                                                                .getCountInHttpRequests()
                                                                                                                .get(readThenClear)));
                }

                instance.getValue().getCountOutHttpResponsesPerStatus().stream().forEach(count ->
                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(CH_INSTANCE_ID, "status")).append('=').append(Arrays.asList(instance.getKey(), count.getKey().toString()));
                    outAnswers.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                });

                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(CH_INSTANCE_ID)).append('=').append(Arrays.asList(instance.getKey()));
                    outRequests.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(),
                                                                                                (double) instance.getValue()
                                                                                                                 .getCountOutHttpRequests()
                                                                                                                 .get(readThenClear)));
                }

                instance.getValue().getCountInHttpResponsesPerStatus().stream().forEach(count ->
                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(CH_INSTANCE_ID, "status")).append('=').append(Arrays.asList(instance.getKey(), count.getKey().toString()));
                    inAnswers.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                });

                instance.getValue().getCountInHttpRequestsPerIpFamily().stream().forEach(count ->
                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(CH_INSTANCE_ID, "ipFamily"))
                     .append('=')
                     .append(Arrays.asList(instance.getKey(), IpFamily.values()[count.getKey()].toString()));
                    inRequestsPerIpFamily.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(),
                                                                                                          (double) count.getValue().get(readThenClear)));
                });

                instance.getValue().getCountOutHttpResponsesPerIpFamily().stream().forEach(count ->
                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(CH_INSTANCE_ID, "ipFamily"))
                     .append('=')
                     .append(Arrays.asList(instance.getKey(), IpFamily.values()[count.getKey()].toString()));
                    outAnswersPerIpFamily.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(),
                                                                                                          (double) count.getValue().get(readThenClear)));
                });
            });

            result.add(new Counter("eric_chfsim_nchf_convergedcharging_http_in_requests_ipfamily_total",
                                   "Number of incoming HTTP requests on the Nchf_ConvergedCharging interface per IP family",
                                   inRequestsPerIpFamily));

            result.add(new Counter("eric_chfsim_nchf_convergedcharging_http_out_answers_ipfamily_total",
                                   "Number of outgoing HTTP answers on the Nchf_ConvergedCharging interface per IP family",
                                   outAnswersPerIpFamily));

            result.add(new Counter("eric_chfsim_nchf_convergedcharging_http_in_requests_total",
                                   "Number of incoming HTTP requests on the Nchf_ConvergedCharging interface",
                                   inRequests));

            result.add(new Counter("eric_chfsim_nchf_convergedcharging_http_out_answers_total",
                                   "Number of outgoing HTTP answers on the Nchf_ConvergedCharging interface",
                                   outAnswers));

            result.add(new Counter("eric_chfsim_nchf_convergedcharging_http_out_requests_total",
                                   "Number of outgoing HTTP requests on the Nchf_ConvergedCharging interface",
                                   outRequests));

            result.add(new Counter("eric_chfsim_nchf_convergedcharging_http_in_answers_total",
                                   "Number of incoming HTTP answers on the Nchf_ConvergedCharging interface",
                                   inAnswers));
        }

        {
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> inRequestsPerIpFamily = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> outAnswersPerIpFamily = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> inRequests = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> outAnswers = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> outRequests = new ArrayList<>();
            final List<com.ericsson.adpal.ext.monitor.api.v0.commands.Instance> inAnswers = new ArrayList<>();

            this.subscriptions.stream().forEach(instance ->
            {
                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(SUBSCRIPTION_ID)).append('=').append(Arrays.asList(instance.getKey()));
                    inRequests.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(),
                                                                                               (double) instance.getValue()
                                                                                                                .getCountInHttpRequests()
                                                                                                                .get(readThenClear)));
                }

                instance.getValue().getCountOutHttpResponsesPerStatus().stream().forEach(count ->
                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(SUBSCRIPTION_ID, "status")).append('=').append(Arrays.asList(instance.getKey(), count.getKey().toString()));
                    outAnswers.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                });

                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(SUBSCRIPTION_ID)).append('=').append(Arrays.asList(instance.getKey()));
                    outRequests.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(),
                                                                                                (double) instance.getValue()
                                                                                                                 .getCountOutHttpRequests()
                                                                                                                 .get(readThenClear)));
                }

                instance.getValue().getCountInHttpResponsesPerStatus().stream().forEach(count ->
                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(SUBSCRIPTION_ID, "status")).append('=').append(Arrays.asList(instance.getKey(), count.getKey().toString()));
                    inAnswers.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                });

                instance.getValue().getCountInHttpRequestsPerIpFamily().stream().forEach(count ->
                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(SUBSCRIPTION_ID, "ipFamily"))
                     .append('=')
                     .append(Arrays.asList(instance.getKey(), IpFamily.values()[count.getKey()].toString()));
                    inRequestsPerIpFamily.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(),
                                                                                                          (double) count.getValue().get(readThenClear)));
                });

                instance.getValue().getCountOutHttpResponsesPerIpFamily().stream().forEach(count ->
                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList(SUBSCRIPTION_ID, "ipFamily"))
                     .append('=')
                     .append(Arrays.asList(instance.getKey(), IpFamily.values()[count.getKey()].toString()));
                    outAnswersPerIpFamily.add(new com.ericsson.adpal.ext.monitor.api.v0.commands.Instance(b.toString(),
                                                                                                          (double) count.getValue().get(readThenClear)));
                });
            });

            result.add(new Counter("eric_chfsim_nchf_spendinglimitcontrol_http_in_requests_ipfamily_total",
                                   "Number of incoming HTTP requests on the Nchf_SpendingLimitControl interface per IP family",
                                   inRequestsPerIpFamily));

            result.add(new Counter("eric_chfsim_nchf_spendinglimitcontrol_http_out_answers_ipfamily_total",
                                   "Number of outgoing HTTP answers on the Nchf_SpendingLimitControl interface per IP family",
                                   outAnswersPerIpFamily));

            result.add(new Counter("eric_chfsim_nchf_spendinglimitcontrol_http_in_requests_total",
                                   "Number of incoming HTTP requests on the Nchf_SpendingLimitControl interface",
                                   inRequests));

            result.add(new Counter("eric_chfsim_nchf_spendinglimitcontrol_http_out_answers_total",
                                   "Number of outgoing HTTP answers on the Nchf_SpendingLimitControl interface",
                                   outAnswers));

            result.add(new Counter("eric_chfsim_nchf_spendinglimitcontrol_http_out_requests_total",
                                   "Number of outgoing HTTP requests on the Nchf_SpendingLimitControl interface",
                                   outRequests));

            result.add(new Counter("eric_chfsim_nchf_spendinglimitcontrol_http_in_answers_total",
                                   "Number of incoming HTTP answers on the Nchf_SpendingLimitControl interface",
                                   inAnswers));
        }

        return result;
    }

    @Override
    public List<Event.Sequence> getEsa()
    {
        final List<Event.Sequence> result = new ArrayList<>();

        for (Iterator<Entry<String, ChargingInstance>> it = this.chInstances.iterator(); it.hasNext();)
            result.add(it.next().getValue().getHistoryOfEvents());

        for (Iterator<Entry<String, Subscription>> it = this.subscriptions.iterator(); it.hasNext();)
            result.add(it.next().getValue().getHistoryOfEvents());

        return result;
    }

    /**
     * Only used from JCAT. In this case ips contains only one IP.
     */
    public String getHostPort()
    {
        final String ip = ips.get(0);

        if (ip.contains(":"))
        {
            return "[" + ip + "]" + ":" + port;
        }
        return ips + ":" + port;
    }

    public Statistics.ChargingInstance.Pool getInstances()
    {
        return this.chInstances;
    }

    public NrfClient getNrfClient()
    {
        return this.nrfClient;
    }

    public Nrf getNrfInstance(UUID id)
    {
        return this.nrfInstances.get(id.toString());
    }

    public Subscription getSubscription(String id)
    {
        return this.subscriptions.get(id);
    }

    public Statistics.Subscription.Pool getSubscriptions()
    {
        return this.subscriptions;
    }

    public void run()
    {
        if (this.admin.loadTestMode)
        {
            log.info("Running with load test mode {}",
                     (this.admin.loadTestMode ? "on. When load test mode is on, logging is greatly reduced, and the backend will not be used."
                                              : "off. Backend is used, verbose logging is on."));

            log.info("Running with load test mode statistics {}", (this.admin.loadTestStatisticsMode ? "on." : "off."));
        }

        try
        {
            Completable.complete()
                       .andThen(this.webServerInt != null ? this.webServerInt.startListener() : Completable.complete())
                       .andThen(Flowable.fromIterable(this.webServerExt).flatMapCompletable(webServer -> webServer.startListener()))
                       .andThen(this.webServerExtTls != null ? Flowable.fromIterable(this.webServerExtTls)
                                                                       .flatMapCompletable(webServer -> webServer.startListener())
                                                             : Completable.complete())
                       .andThen(this.loadMeter != null ? this.loadMeter.start() : Completable.complete())
                       .andThen(this.monitored != null ? this.monitored.start() : Completable.complete())
                       .andThen(Completable.create(emitter ->
                       {
                           log.info("Registering shutdown hook.");
                           Runtime.getRuntime().addShutdownHook(new Thread(() ->
                           {
                               log.info("Shutdown hook called.");
                               this.stop().blockingAwait();

                               emitter.onComplete();
                           }));
                       }))
                       .blockingAwait();
        }
        catch (Exception e)
        {
            log.error("Exception caught, stopping simulator.", e);
        }

        log.info("Stopped.");
    }

    public Completable stop()
    {
        return Completable.complete()
                          .andThen(this.monitored != null ? this.monitored.stop().onErrorComplete() : Completable.complete())
                          .andThen(this.loadMeter != null ? this.loadMeter.stop().onErrorComplete() : Completable.complete())
                          .andThen(this.webServerInt != null ? this.webServerInt.stopListener().onErrorComplete() : Completable.complete())
                          .andThen(Flowable.fromIterable(this.webServerExt).flatMapCompletable(webServer -> webServer.stopListener().onErrorComplete()))
                          .andThen(this.webServerExtTls != null ? Flowable.fromIterable(this.webServerExtTls)
                                                                          .flatMapCompletable(webServer -> webServer.stopListener().onErrorComplete())
                                                                : Completable.complete());
    }

    /**
     * Add a header that indicates the response is from this process, not Envoy
     * 
     * @param routingContext
     */
    private void addXOriginHeader(RoutingContext routingContext)
    {
        routingContext.response().headers().add("x-origin", EnvVars.get("HOSTNAME"));
    }

    /**
     * Create a "location" header for replies. The location header is an absolute
     * URL (= including http://...), with the address+port of the Chfsim, and no
     * query parameters. The location header contains the address of the newly
     * created resource.
     * 
     * @param absUri
     * @param createdInstance
     * @return the location header content (a string)
     */
    private String createLocationHeader(final String absUri,
                                        final String createdInstance)
    {
        // Strip an eventual query string:
        final String absUriWithoutQuery = absUri.split("\\?")[0];
        String uri = (absUriWithoutQuery.endsWith("/") ? absUriWithoutQuery : (absUriWithoutQuery + "/"));

        // Replace host and port with the chfsim's address and port:
        final String chfsimHost = EnvVars.get("SERVICE_NAME", this.prefix);
        uri = uri.replaceFirst("^(http://)([^/]+)(.+)$", "$1" + chfsimHost + ":80$3");
        uri = uri.replaceFirst("^(https://)([^/]+)(.+)$", "$1" + chfsimHost + ":443$3");

        return uri + createdInstance;
    }

    // according to RS_CSA, sessionId occ-xxxxxxxx is replaced by new
    // extendsessionId in form of
    // <sessionId(UUID)>p<partitionId>, and partitionId is taken from chfsim host
    // name suffix,
    // e.g. 29e5952d-f4b1-41a5-9151-dc61059fa37bp1
    private String extendSessionId(String uuid)
    {
        final String chfsimHost = EnvVars.get("SERVICE_NAME", this.prefix);

        if (!chfsimHost.contains("chfsim-unknown"))
        {
            // If for manual deployment, chfsimHost has the format "eric-chfsim-1"
            // Else for jcat GenericSCP_TMO_Tests TCs, chfsimHost follows prefix named
            // "chfsim1-"
            if (chfsimHost.contains("chfsim-"))
            {
                uuid = uuid + "p" + Integer.parseInt(chfsimHost.substring(chfsimHost.indexOf("chfsim-") + 7, chfsimHost.length()));
            }
            else if (chfsimHost.contains("chfsim"))
            {
                uuid = uuid + "p" + Integer.parseInt(chfsimHost.substring(chfsimHost.indexOf("chfsim") + 6, chfsimHost.length() - 1));
            }
        }

        return uuid;
    }

    /**
     * Send a reject with a specific answer-code as a result of a flexible
     * disturbance
     * 
     * @param routingContext
     * @param event
     * @param outHttpResponsesPerStatus
     * @param requestName
     * @param answerCode
     */
    private void flexibleDisturbanceReject(RoutingContext routingContext,
                                           Event event,
                                           Count.Pool outHttpResponsesPerStatus,
                                           String requestName,
                                           String answerCode)
    {
        final String msg = new StringBuilder(requestName).append(": Rejecting message with code ")
                                                         .append(answerCode)
                                                         .append(" due to flexible disturbance.")
                                                         .toString();
        this.replyWithError(routingContext, event.setResponse(HttpResponseStatus.valueOf(Integer.parseInt(answerCode)), msg), null, null, null);
        outHttpResponsesPerStatus.get(event.getResponse().getResultCode()).inc();
    }

    /**
     * Given a routingContext (= from an incoming message), try to extract the
     * NotifyURI from the JSON body. Return the NotifyURI as a string or null if no
     * body, no JSON body, or no "notifUri" inside the JSON body.
     * 
     * @param routingContext
     * @return String the notifUri or empty string if not found
     */
    private String getNotifUriFromJsonBody(final RoutingContext routingContext)
    {
        String notifyUri = "";

        if (routingContext.getBody() != null && routingContext.getBody().length() > 0)
        {
            final JsonObject body = routingContext.getBodyAsJson();

            if (body != null)
            {
                notifyUri = routingContext.getBodyAsJson().getString("notifUri");

                if (notifyUri == null)
                {
                    notifyUri = "";
                }
            }
        }

        return notifyUri;
    }

    /**
     * Given a routingContext (= from an incoming message), try to extract the
     * NotifyURI from the JSON body. Return the NotifyURI as a string or null if no
     * body, no JSON body, or no "notifyUri" inside the JSON body.
     * 
     * @param routingContext
     * @return String the notifyUri or empty string if not found
     */
    private String getNotifyUriFromJsonBody(final RoutingContext routingContext)
    {
        String notifyUri = "";

        if (routingContext.getBody() != null && routingContext.getBody().length() > 0)
        {
            final JsonObject body = routingContext.getBodyAsJson();

            if (body != null)
            {
                notifyUri = routingContext.getBodyAsJson().getString("notifyUri");

                if (notifyUri == null)
                {
                    notifyUri = "";
                }
            }
        }

        return notifyUri;
    }

    /**
     * Common disturbance function. First, it handles disturbances: reject, drop,
     * delay message. Second, if there were no global/semipermanent disturbances,
     * then check in Redis if flexible disturbances are available and execute that.
     * Third, it executes the code that handles the message itself. This code
     * typically has been passed as a lambda. It is necessary to execute the
     * request-handling code in here because the delay is asynchronous and needs the
     * code to be executed once the delay is over
     * 
     * @param routingContext
     * @param event
     * @param outHttpResponsesPerStatus
     * @param requestName
     * @param disturbanceMessageType
     * @param requestHandlerFunc
     */
    private void handleDisturbanceThenHandleRequest(RoutingContext routingContext,
                                                    Event event,
                                                    Count.Pool outHttpResponsesPerStatus,
                                                    String requestName,
                                                    String disturbanceMessageType,
                                                    RequestHandlerFunction requestHandlerFunc)
    {
        this.addXOriginHeader(routingContext); // Mark response with our hostname so that we can see which chfsim
                                               // responded

        // Simulate disturbance: reject message with error code
        if ((this.admin.rejectPercent >= (int) (Math.random() * 100) + 1)
            && (this.admin.rejectMessageType.equalsIgnoreCase(disturbanceMessageType) || this.admin.rejectMessageType.equalsIgnoreCase("all")))
        {
            final String msg = new StringBuilder(requestName).append(": Answering with ")
                                                             .append(this.admin.rejectAnswer)
                                                             .append(" due to set reject percentage of ")
                                                             .append(this.admin.rejectPercent)
                                                             .toString();
            this.replyWithError(routingContext, event.setResponse(this.admin.rejectAnswer, msg), null, null, null);
            outHttpResponsesPerStatus.get(event.getResponse().getResultCode()).inc();
            return;
        }

        final WeightedDisturbances.Disturbance disturbance = this.admin.weightedDisturbances.next();

        if (this.messageShallBeDropped(routingContext, requestName, disturbance, disturbanceMessageType))
            return;

        // Shall the reply be delayed?
        if (disturbance.isDelayAction() && disturbance.getDelayInMillis() > 0
            && (this.admin.delayMessageType.equalsIgnoreCase(disturbanceMessageType) || this.admin.delayMessageType.equalsIgnoreCase("all")))
        {
            log.info("{}: Delaying answer due to set delayAnswerMs = {} and delayMessageType = '{}'",
                     requestName,
                     disturbance.getDelayInMillis(),
                     this.admin.delayMessageType);

            this.vertx.setTimer(disturbance.getDelayInMillis(), id ->
            {
                // All disturbances done, if we made it here then handle the request:
                log.info("{}: Delay over", requestName);
                this.handleFlexibleDisturbanceThenHandleRequest(routingContext, event, outHttpResponsesPerStatus, requestName, requestHandlerFunc);
            });

        }
        else // handle the request without delay
        {
            this.handleFlexibleDisturbanceThenHandleRequest(routingContext, event, outHttpResponsesPerStatus, requestName, requestHandlerFunc);
        }
    }

    /**
     * Handle flexible disturbances. Flexible disturbances are a list of
     * one-shot-disturbances stored in Redis. They allow to precisely set a sequence
     * of disturbances which are executed on whichever chfsim receives the request.
     * 
     * @param routingContext
     * @param event
     * @param outHttpResponsesPerStatus
     * @param requestName
     * @param requestHandlerFunc
     */
    private void handleFlexibleDisturbanceThenHandleRequest(RoutingContext routingContext,
                                                            Event event,
                                                            Count.Pool outHttpResponsesPerStatus,
                                                            String requestName,
                                                            RequestHandlerFunction requestHandlerFunc)
    {
        // Query Redis for a disturbance
        this.redis.lpop(Arrays.asList(REDIS_KEY_FLEXI_DISTURBANCE), res ->
        {
            if (res.succeeded())
            {
                if (res.result() == null || res.result().toString().contains("OK")) // no flexible disturbance ->
                                                                                    // execute the request undisturbed
                {
                    requestHandlerFunc.op(routingContext);
                }
                else // there is a flexible disturbance
                {
                    log.info("{}: Get Flexible Disturbance: {}", requestName, res.result());
                    var words = res.result().toString().split(":");
                    var disturbance = words[0];
                    var parameter = (words.length > 1 ? words[1] : ""); // may be empty

                    if (disturbance.equals("reject"))
                    {
                        flexibleDisturbanceReject(routingContext, event, outHttpResponsesPerStatus, requestName, parameter);
                        return;
                    }

                    if (disturbance.equals("drop"))
                    {
                        if (parameter.equals("0")) // No drop wanted
                        {
                            requestHandlerFunc.op(routingContext);
                        }
                        else if (parameter.equals("1") || parameter.isEmpty()) // Normal drop, no dropAnswer
                        {
                            log.info("{}: Dropping message due to flexible disturbance", requestName);
                            return;
                        }
                        else // Drop with dropAnswer wanted (e.g. 7 -> REFUSED_STREAM)
                        {
                            routingContext.response().reset(Long.parseLong(parameter));

                            log.info("{}: Dropping message with dropAnswer {} due to flexible disturbance", requestName, parameter);
                            return;
                        }
                    }

                    if (disturbance.equals("delay-reject"))
                    {
                        log.info("{}: Delaying reply by {} ms due to flexible disturbance", requestName, parameter);
                        var delay = Integer.parseInt(parameter);

                        this.vertx.setTimer(delay, id ->
                        {
                            log.info("{}: Delay is over", requestName);
                            var answerCode = (words.length > 2 ? words[2] : "501");
                            flexibleDisturbanceReject(routingContext, event, outHttpResponsesPerStatus, requestName, answerCode);
                        });
                    }
                    else if (disturbance.equals("delay"))
                    {
                        log.info("{}: Delaying reply by {} ms due to flexible disturbance", requestName, parameter);
                        var delay = Integer.parseInt(parameter);

                        this.vertx.setTimer(delay, id ->
                        {
                            log.info("{}: Delay is over", requestName);
                            requestHandlerFunc.op(routingContext);
                        });
                    }
                    else if (disturbance.equals("nop"))
                    {
                        log.info("{}: No-operation (nop) disturbance, not applying any disturbance.", requestName);
                        requestHandlerFunc.op(routingContext);
                    }
                    else // unknown disturbance
                    {
                        log.error("{}: Error: Unknown disturbance: {}. Not applying any disturbance.", requestName, disturbance);
                        requestHandlerFunc.op(routingContext);
                    }
                }
            }
            else // Redis call failedloadTestMode
            {
                log.error("{}: ERROR getting the list of flexible disturbances, not applying any disturbance. Cause: '{}'.",
                          requestName,
                          res.cause().toString());
                requestHandlerFunc.op(routingContext);
            }
        });
    }

    /**
     * Handle Nchf_ConvergedCharging_Create request
     * <p>
     * A client wants to start a new charging session. Allocate a new free
     * chargingDataRef and return it to the requester. Everything else is not
     * simulated because we don't need that.
     * 
     * @param rtCtx
     */
    private void handlerNchfConvergedChargingCreate(final RoutingContext rtCtx)
    {
        Optional.ofNullable(rtCtx.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                .ifPresent(header -> rtCtx.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

        // China Telecom specific handling: if the CDR ID is in the request URI already,
        // it shall be ignored and an own CDR ID shall be generated.
        final boolean hasCdrIdInUrl = rtCtx.request().getParam("chargingDataRef") != null; // China Telecom specific
                                                                                           // interface
        final String absUri = hasCdrIdInUrl ? rtCtx.request().absoluteURI().substring(0, rtCtx.request().absoluteURI().lastIndexOf('/'))
                                            : rtCtx.request().absoluteURI();

        log.debug("hasCdrInUrl={}, absUri={}", hasCdrIdInUrl, absUri);

        if (this.admin.loadTestMode)
        {
            // In load-test-mode the charging data ref is ignored, so we can send a fixed
            // one. This is not only a bit faster but also ensures we never run out of IDs
            // (see DND-18795)

            // new session id with p-value used when sessionIdMode enabled, otherwise legacy
            // session id is used.

            final String cdr = this.admin.sessionIdMode ? extendSessionId(this.uuid) : this.prefix + this.startIds;
            ChargingInstance chInstance;
            if (this.admin.loadTestStatisticsMode)
            {
                chInstance = this.admin.sessionIdMode ? this.getChargingInstance(cdr.substring(cdr.lastIndexOf("p"), cdr.length()))
                                                      : this.getChargingInstance(cdr.split("-")[0]);
            }
            else
            {
                chInstance = null;
            }

            if (chInstance != null)
                chInstance.getCountInHttpRequests().inc();

            this.addXOriginHeader(rtCtx);

            final WeightedDisturbances.Disturbance disturbance = this.admin.weightedDisturbances.next();

            if (this.messageShallBeDropped(rtCtx, "CC CREATE", disturbance))
                return;

            if (disturbance.isDelayAction() && disturbance.getDelayInMillis() > 0)
            {
                rtCtx.vertx().setTimer(disturbance.getDelayInMillis(), __ ->
                {
                    log.debug("CC CREATE: response has been delayed by {} ms", disturbance.getDelayInMillis());

                    rtCtx.response()
                         .putHeader(HD_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8)
                         .setChunked(true)
                         .setStatusCode(HttpResponseStatus.CREATED.code());

                    if (!this.admin.omitLocationHeader)
                        rtCtx.response().putHeader(HD_LOCATION, this.createLocationHeader(absUri, cdr));

                    rtCtx.response().end(CREATE_RESPONSE_BODY);

                    if (chInstance != null)
                        chInstance.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.CREATED.code()).inc();
                });
            }
            else
            {
                rtCtx.response()
                     .putHeader(HD_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8)
                     .setChunked(true)
                     .setStatusCode(HttpResponseStatus.CREATED.code());

                if (!this.admin.omitLocationHeader)
                    rtCtx.response().putHeader(HD_LOCATION, this.createLocationHeader(absUri, cdr));

                rtCtx.response().end(CREATE_RESPONSE_BODY);

                if (chInstance != null)
                    chInstance.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.CREATED.code()).inc();
            }
        }
        else
        {
            final IpFamily ipFamily = rtCtx.get(DataIndex.IP_FAMILY.name());
            final Event event = new Event(EventTrigger.NCHF_CONVERGED_CHARGING_CREATE.name(), String.class.getName(), rtCtx.getBodyAsString());

            // new session id with p-value used when sessionIdMode enabled, otherwise legacy
            // session id is used, new sessionid created for each Create request.
            String cdr;
            ChargingInstance chInstance;
            if (this.admin.sessionIdMode)
            {
                cdr = extendSessionId(UUID.randomUUID().toString());
                // add the new session id string into allocatedNewIdRefs set
                this.allocatedNewIdRefs.add(cdr);
                chInstance = this.getChargingInstance(cdr.substring(cdr.lastIndexOf("p"), cdr.length()));
            }
            else
            {
                cdr = this.sessionDataStore.getRef();
                chInstance = this.getChargingInstance(cdr.split("-")[0]);
            }

            chInstance.setContext(rtCtx);
            chInstance.getHistoryOfEvents().put(event);
            chInstance.getCountInHttpRequests().inc();
            chInstance.getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            this.handleDisturbanceThenHandleRequest(rtCtx, event, chInstance.getCountOutHttpResponsesPerStatus(), "CC CREATE:", "init", routingContext ->
            {
                final String notifyUri = getNotifyUriFromJsonBody(routingContext);

                // Send request to the backend to store charging data ref+notifyUri
                this.redis.set(Arrays.asList(cdr, notifyUri), res ->
                {
                    try
                    {
                        if (res.succeeded())
                        {
                            log.info("CC Create: Sending 201 Created, new chargingDataRef = {}, notifyUri = {}, body length = {}, attempt = {}",
                                     cdr,
                                     notifyUri,
                                     routingContext.getBody() != null ? routingContext.getBody().length() : 0,
                                     rtCtx.request().getHeader("x-envoy-attempt-count"));

                            routingContext.response()
                                          .putHeader(HD_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8)
                                          .setChunked(true)
                                          .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode());

                            if (!this.admin.omitLocationHeader)
                                routingContext.response().putHeader(HD_LOCATION, this.createLocationHeader(absUri, cdr));

                            routingContext.response().end(CREATE_RESPONSE_BODY);
                        }
                        else // failed to store data in backend
                        {
                            final String msg = "CC Create: Failed to store chargingDataRef '" + cdr + "' and notifyUri '" + notifyUri + "'.";
                            this.replyWithError(routingContext, event.setResponse(this.admin.rejectAnswer, msg), null, null, null);
                        }
                    }
                    catch (IllegalStateException e)
                    {
                        final String msg = "CC Create: Connection was already closed when trying to send reply. This can happen when envoy times out earlier than our delay finishes.";
                        log.info(msg);
                        event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                    }
                    finally
                    {
                        chInstance.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                        chInstance.getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                    }
                });
            });
        }
    }

    /**
     * Handle Nchf_ConvergedCharging_Notify request
     * 
     * @param rtCtx
     */
    private void handlerNchfConvergedChargingNotify(final RoutingContext rtCtx)
    {
        Optional.ofNullable(rtCtx.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                .ifPresent(header -> rtCtx.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

        if (this.admin.loadTestMode)
        {
            // new session id with p-value used when sessionIdMode enabled, otherwise legacy
            // session id is used.
            final String cdr = this.admin.sessionIdMode ? extendSessionId(this.uuid) : this.prefix + this.startIds;
            ChargingInstance chInstance;
            if (this.admin.loadTestStatisticsMode)
            {
                chInstance = this.admin.sessionIdMode ? this.getChargingInstance(cdr.substring(cdr.lastIndexOf("p"), cdr.length()))
                                                      : this.getChargingInstance(cdr.split("-")[0]);
            }
            else
            {
                chInstance = null;
            }

            if (chInstance != null)
                chInstance.getCountInHttpRequests().inc();

            this.addXOriginHeader(rtCtx);

            final WeightedDisturbances.Disturbance disturbance = this.admin.weightedDisturbances.next();

            if (this.messageShallBeDropped(rtCtx, "CC NOTIFY", disturbance))
                return;

            if (disturbance.isDelayAction() && disturbance.getDelayInMillis() > 0)
            {
                rtCtx.vertx().setTimer(disturbance.getDelayInMillis(), __ ->
                {
                    log.debug("CC NOTIFY: response has been delayed by {} ms", disturbance.getDelayInMillis());

                    rtCtx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();

                    if (chInstance != null)
                        chInstance.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.NO_CONTENT.code()).inc();
                });
            }
            else
            {
                rtCtx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();

                if (chInstance != null)
                    chInstance.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.NO_CONTENT.code()).inc();
            }
        }
        else
        {
            final IpFamily ipFamily = rtCtx.get(DataIndex.IP_FAMILY.name());
            final Event event = new Event(EventTrigger.NCHF_CONVERGED_CHARGING_NOTIFY.name(), JsonObject.class.getName(), rtCtx.getBodyAsString());

            // new session id with p-value used when sessionIdMode enabled, otherwise legacy
            // session id is used, new sessionid created for each Notify request.
            String cdr;
            ChargingInstance chInstance;

            if (this.admin.sessionIdMode)
            {
                cdr = extendSessionId(UUID.randomUUID().toString());
                // add the new session id string into allocatedNewIdRefs set
                this.allocatedNewIdRefs.add(cdr);
                chInstance = this.getChargingInstance(cdr.substring(cdr.lastIndexOf("p"), cdr.length()));
            }
            else
            {
                cdr = this.sessionDataStore.getRef();
                chInstance = this.getChargingInstance(cdr.split("-")[0]);
            }

            chInstance.setContext(rtCtx);
            chInstance.getHistoryOfEvents().put(event);
            chInstance.getCountInHttpRequests().inc();
            chInstance.getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            this.handleDisturbanceThenHandleRequest(rtCtx, event, chInstance.getCountOutHttpResponsesPerStatus(), "CC NOTIFY:", "notify", routingContext ->
            {
                this.addXOriginHeader(rtCtx);
                rtCtx.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();

                chInstance.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                chInstance.getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
            });
        }
    }

    /**
     * Handle Nchf_ConvergedCharging_Release request A client wants to terminate the
     * charging session. If the supplied chargingDataRef is correct and in use, then
     * mark the chargingDataRef as available and return 204 No Content. Otherwise
     * return an error 400 Bad Request.
     * 
     * @param rtCtx
     */
    private void handlerNchfConvergedChargingRelease(final RoutingContext rtCtx)
    {
        Optional.ofNullable(rtCtx.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                .ifPresent(header -> rtCtx.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

        if (this.admin.loadTestMode)
        {
            // new session id with p-value used when sessionIdMode enabled, otherwise legacy
            // session id is used.
            final String cdr = this.admin.sessionIdMode ? extendSessionId(this.uuid) : this.prefix + this.startIds;
            ChargingInstance chInstance;
            if (this.admin.loadTestStatisticsMode)
            {
                chInstance = this.admin.sessionIdMode ? this.getChargingInstance(cdr.substring(cdr.lastIndexOf("p"), cdr.length()))
                                                      : this.getChargingInstance(cdr.split("-")[0]);
            }
            else
            {
                chInstance = null;
            }

            if (chInstance != null)
                chInstance.getCountInHttpRequests().inc();

            this.addXOriginHeader(rtCtx);

            final WeightedDisturbances.Disturbance disturbance = this.admin.weightedDisturbances.next();

            if (this.messageShallBeDropped(rtCtx, "CC RELEASE", disturbance))
                return;

            if (disturbance.isDelayAction() && disturbance.getDelayInMillis() > 0)
            {
                rtCtx.vertx().setTimer(disturbance.getDelayInMillis(), __ ->
                {
                    log.debug("CC RELEASE: response has been delayed by {} ms", disturbance.getDelayInMillis());

                    rtCtx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();

                    if (chInstance != null)
                        chInstance.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.NO_CONTENT.code()).inc();
                });
            }
            else
            {
                rtCtx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();

                if (chInstance != null)
                    chInstance.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.NO_CONTENT.code()).inc();
            }
        }
        else
        {
            final IpFamily ipFamily = rtCtx.get(DataIndex.IP_FAMILY.name());
            final Event event = new Event(EventTrigger.NCHF_CONVERGED_CHARGING_RELEASE.name(), String.class.getName(), rtCtx.getBodyAsString());
            // session id extracted from request uri is used.
            final String cdr = rtCtx.request().getParam("chargingDataRef");
            final ChargingInstance chInstance = this.admin.sessionIdMode ? this.getChargingInstance(cdr.substring(cdr.lastIndexOf("p"), cdr.length()))
                                                                         : this.getChargingInstance(cdr.split("-")[0]);

            chInstance.setContext(rtCtx);
            chInstance.getHistoryOfEvents().put(event);
            chInstance.getCountInHttpRequests().inc();
            chInstance.getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            this.handleDisturbanceThenHandleRequest(rtCtx, event, chInstance.getCountOutHttpResponsesPerStatus(), "CC RELEASE", "term", routingContext ->
            {
                // Delete/free the chargingDataRef from front-end
                // both new and default session id format are supported:
                if (this.admin.sessionIdMode && this.admin.isNewIdValid(cdr))
                {
                    this.admin.releaseNewIdRef(cdr);
                }
                else if (this.sessionDataStore.isValid(cdr))
                {
                    this.sessionDataStore.releaseRef(cdr);
                }

                // Also delete the chargingDataRef from backend
                this.redis.del(Arrays.asList(cdr), res ->
                {
                    try
                    {
                        if (res.succeeded() && (res.result().toInteger() == 1))
                        {
                            log.info("CC Release: Sending 204 No Content for {}, attempt = {}", cdr, rtCtx.request().getHeader("x-envoy-attempt-count"));
                            routingContext.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
                        }
                        else
                        {
                            final String msg = "CC Release: Not found: chargingDataRef '" + cdr + "'.";
                            this.replyWithError(routingContext, event.setResponse(HttpResponseStatus.BAD_REQUEST, msg), null, null, null);
                        }
                    }
                    catch (Exception e)
                    {
                        final String msg = "CC Release: Connection was already closed when trying to send reply. This can happen when envoy times out earlier than our delay finishes.";
                        log.info(msg);
                        event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                    }
                    finally
                    {
                        chInstance.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                        chInstance.getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                    }
                });
            });
        }
    }

    /**
     * Handle Nchf_ConvergedCharging_Update request A client wants to update the
     * spent units / charging record. If the chargingDataRef supplied by the client
     * is correct and in use, then return 200 OK, otherwise return an error 400 Bad
     * Request.
     * 
     * @param rtCtx
     */
    private void handlerNchfConvergedChargingUpdate(final RoutingContext rtCtx)
    {
        Optional.ofNullable(rtCtx.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                .ifPresent(header -> rtCtx.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

        if (this.admin.loadTestMode)
        {
            // new session id with p-value used when sessionIdMode enabled, otherwise legacy
            // session id is used.
            final String cdr = this.admin.sessionIdMode ? extendSessionId(this.uuid) : this.prefix + this.startIds;
            ChargingInstance chInstance;
            if (this.admin.loadTestStatisticsMode)
            {
                chInstance = this.admin.sessionIdMode ? this.getChargingInstance(cdr.substring(cdr.lastIndexOf("p"), cdr.length()))
                                                      : this.getChargingInstance(cdr.split("-")[0]);
            }
            else
            {
                chInstance = null;
            }

            if (chInstance != null)
                chInstance.getCountInHttpRequests().inc();

            final Event event = new Event(EventTrigger.NCHF_CONVERGED_CHARGING_UPDATE.name(), String.class.getName(), rtCtx.getBodyAsString());
            this.addXOriginHeader(rtCtx);

            final WeightedDisturbances.Disturbance disturbance = this.admin.weightedDisturbances.next();

            if (this.messageShallBeDropped(rtCtx, "CC UPDATE", disturbance))
                return;

            if (disturbance.isDelayAction() && disturbance.getDelayInMillis() > 0)
            {
                rtCtx.vertx().setTimer(disturbance.getDelayInMillis(), __ ->
                {
                    log.debug("CC UPDATE: response has been delayed by {} ms", disturbance.getDelayInMillis());

                    this.sendNchfConvergedChargingUpdateSuccessfulResponse(rtCtx, event);

                    if (chInstance != null)
                        chInstance.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                });
            }
            else
            {
                this.sendNchfConvergedChargingUpdateSuccessfulResponse(rtCtx, event);

                if (chInstance != null)
                    chInstance.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
            }
        }
        else
        {
            final IpFamily ipFamily = rtCtx.get(DataIndex.IP_FAMILY.name());
            final Event event = new Event(EventTrigger.NCHF_CONVERGED_CHARGING_UPDATE.name(), String.class.getName(), rtCtx.getBodyAsString());
            // session id extracted from request uri is used.
            final String cdr = rtCtx.request().getParam("chargingDataRef");
            final ChargingInstance chInstance = this.admin.sessionIdMode ? this.getChargingInstance(cdr.substring(cdr.lastIndexOf("p"), cdr.length()))
                                                                         : this.getChargingInstance(cdr.split("-")[0]);

            chInstance.setContext(rtCtx);
            chInstance.getHistoryOfEvents().put(event);
            chInstance.getCountInHttpRequests().inc();
            chInstance.getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            this.handleDisturbanceThenHandleRequest(rtCtx, event, chInstance.getCountOutHttpResponsesPerStatus(), "CC UPDATE", "update", routingContext ->
            {
                final String notifyUri = getNotifyUriFromJsonBody(routingContext);

                // Do we know about this session? If yes and if the notifyUri is empty, then we
                // don't have to involve the backend and can reply with success right away.
                // The notifyUri only needs to be updated in the backend because when a notify
                // is requested, it is always fetched from the backend.

                // both new and default session id format are supported
                if (((this.admin.sessionIdMode && this.admin.isNewIdValid(cdr)) || this.sessionDataStore.isValid(cdr)) && (notifyUri.isEmpty()))
                {
                    this.sendNchfConvergedChargingUpdateSuccessfulResponse(routingContext, event);
                    chInstance.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                    return;
                }

                // Send request to backend with notifyUri in header. This queries the backend
                // and, if successful, updates the notifyUri in the backend
                this.redis.getset(cdr, notifyUri, res ->
                {
                    try
                    {
                        if (res.succeeded() && (res.result() != null))
                        {
                            log.info("CC Update: Redis getset succeeded {}, attempt = {}", res.result(), rtCtx.request().getHeader("x-envoy-attempt-count"));
                            this.sendNchfConvergedChargingUpdateSuccessfulResponse(routingContext, event);
                        }
                        else
                        {
                            final String msg = "CC Update: Not found: chargingDataRef '" + cdr + "'.";
                            this.replyWithError(routingContext, event.setResponse(HttpResponseStatus.BAD_REQUEST, msg), null, null, null);
                        }
                    }
                    catch (Exception e)
                    {
                        final String msg = "CC Update: Connection was already closed when trying to send reply. This can happen when envoy times out earlier than our delay finishes.";
                        log.info(msg);
                        event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                    }
                    finally
                    {
                        chInstance.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                        chInstance.getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                    }
                });
            });
        }
    }

    /**
     * Handle Nchf_SpendingLimitControl_Modify request A client wants to update the
     * subscription. If the subscriptionId supplied by the client is correct and in
     * use, then return 200 OK, otherwise return an error 400 Bad Request
     * 
     * @param rtCtx
     */
    private void handlerNchfSpendingLimitControlModify(final RoutingContext rtCtx)
    {
        Optional.ofNullable(rtCtx.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                .ifPresent(header -> rtCtx.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

        if (this.admin.loadTestMode)
        {
            final String subscriptionId = this.prefix + this.startIds;
            final Subscription subscription = this.admin.loadTestStatisticsMode ? this.getSubscription(subscriptionId.split("-")[0]) : null;

            if (subscription != null)
                subscription.getCountInHttpRequests().inc();

            this.addXOriginHeader(rtCtx);

            final WeightedDisturbances.Disturbance disturbance = this.admin.weightedDisturbances.next();

            if (this.messageShallBeDropped(rtCtx, "SL MODIFY", disturbance))
                return;

            if (disturbance.isDelayAction() && disturbance.getDelayInMillis() > 0)
            {
                rtCtx.vertx().setTimer(disturbance.getDelayInMillis(), __ ->
                {
                    log.debug("SL MODIFY: response has been delayed by {} ms", disturbance.getDelayInMillis());

                    rtCtx.response()
                         .putHeader(HD_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8)
                         .setChunked(true)
                         .setStatusCode(HttpResponseStatus.OK.code())
                         .end(MODIFY_RESPONSE_BODY);

                    if (subscription != null)
                        subscription.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.OK.code()).inc();
                });
            }
            else
            {
                rtCtx.response()
                     .putHeader(HD_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8)
                     .setChunked(true)
                     .setStatusCode(HttpResponseStatus.OK.code())
                     .end(MODIFY_RESPONSE_BODY);

                if (subscription != null)
                    subscription.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.OK.code()).inc();
            }
        }
        else
        {
            final IpFamily ipFamily = rtCtx.get(DataIndex.IP_FAMILY.name());
            final Event event = new Event(EventTrigger.NCHF_SPENDING_LIMIT_CONTROL_MODIFY.name(), String.class.getName(), rtCtx.getBodyAsString());
            final String subscriptionId = rtCtx.request().getParam(SUBSCRIPTION_ID);
            final Subscription subscription = this.getSubscription(subscriptionId.split("-")[0]);

            subscription.setContext(rtCtx);
            subscription.getHistoryOfEvents().put(event);
            subscription.getCountInHttpRequests().inc();
            subscription.getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            this.handleDisturbanceThenHandleRequest(rtCtx, event, subscription.getCountOutHttpResponsesPerStatus(), "SL MODIFY", "update", routingContext ->
            {
                final String notifyUri = getNotifUriFromJsonBody(routingContext);

                // Send request to backend with notifyUri in header. This queries the backend
                // and, if successful, updates the notifyUri in the backend.
                // We do not have to update the local (= in the frontend) sessionDataStore
                // because on notify the backend is queried anyway.
                this.redis.getset(subscriptionId, notifyUri, res ->
                {
                    try
                    {
                        if (res.succeeded() && (res.result() != null))
                        {
                            log.info("SL Modify: Sending 200 OK for {}", routingContext.request().getParam(SUBSCRIPTION_ID));

                            routingContext.response()
                                          .putHeader(HD_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8)
                                          .setChunked(true)
                                          .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                                          .end(MODIFY_RESPONSE_BODY);
                        }
                        else
                        {
                            final String msg = "SL Modify: Not found: subscriptionId '" + subscriptionId + "'.";
                            this.replyWithError(routingContext, event.setResponse(HttpResponseStatus.BAD_REQUEST, msg), null, null, null);
                        }
                    }
                    catch (Exception e)
                    {
                        final String msg = "SL Modify: Connection was already closed when trying to send reply. This can happen when envoy times out earlier than our delay finishes.";
                        log.info(msg);
                        event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                    }
                    finally
                    {
                        subscription.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                        subscription.getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                    }
                });
            });
        }
    }

    /**
     * Handle Nchf_SpendingLimitControl_Notify request
     * 
     * @param rtCtx
     */
    private void handlerNchfSpendingLimitControlNotify(final RoutingContext rtCtx)
    {
        Optional.ofNullable(rtCtx.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                .ifPresent(header -> rtCtx.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

        if (this.admin.loadTestMode)
        {
            final String subscriptionId = this.prefix + this.startIds;
            final Subscription subscription = this.admin.loadTestStatisticsMode ? this.getSubscription(subscriptionId.split("-")[0]) : null;

            if (subscription != null)
                subscription.getCountInHttpRequests().inc();

            this.addXOriginHeader(rtCtx);

            final WeightedDisturbances.Disturbance disturbance = this.admin.weightedDisturbances.next();

            if (this.messageShallBeDropped(rtCtx, "SL NOTIFY", disturbance))
                return;

            if (disturbance.isDelayAction() && disturbance.getDelayInMillis() > 0)
            {
                rtCtx.vertx().setTimer(disturbance.getDelayInMillis(), __ ->
                {
                    log.debug("SL NOTIFY: response has been delayed by {} ms", disturbance.getDelayInMillis());

                    rtCtx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();

                    if (subscription != null)
                        subscription.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.NO_CONTENT.code()).inc();
                });
            }
            else
            {
                rtCtx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();

                if (subscription != null)
                    subscription.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.NO_CONTENT.code()).inc();
            }
        }
        else
        {
            final IpFamily ipFamily = rtCtx.get(DataIndex.IP_FAMILY.name());
            final Event event = new Event(EventTrigger.NCHF_SPENDING_LIMIT_CONTROL_NOTIFY.name(), JsonObject.class.getName(), rtCtx.getBodyAsString());
            final String subscriptionId = this.sessionDataStore.getRef();
            final Subscription subscription = this.getSubscription(subscriptionId.split("-")[0]);

            subscription.setContext(rtCtx);
            subscription.getHistoryOfEvents().put(event);
            subscription.getCountInHttpRequests().inc();
            subscription.getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            this.handleDisturbanceThenHandleRequest(rtCtx, event, subscription.getCountOutHttpResponsesPerStatus(), "SL NOTIFY:", "notify", routingContext ->
            {
                this.addXOriginHeader(rtCtx);
                rtCtx.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();

                subscription.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                subscription.getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
            });
        }
    }

    /**
     * Handle Nchf_SpendingLimitControl_Subscribe request A client wants to start a
     * new subscription. Allocate a new free sessionId and return it to the
     * requester. Everything else is not simulated because we don't need that.
     * 
     * @param rtCtx
     */
    private void handlerNchfSpendingLimitControlSubscribe(final RoutingContext rtCtx)
    {
        Optional.ofNullable(rtCtx.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                .ifPresent(header -> rtCtx.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

        if (this.admin.loadTestMode)
        {
            // In load-test-mode the subscription ID is ignored, so we can send a fixed
            // one. This is not only a bit faster but also ensures we never run out of IDs
            // (see DND-18795)
            final String subscriptionId = this.prefix + this.startIds;
            final Subscription subscription = this.admin.loadTestStatisticsMode ? this.getSubscription(subscriptionId.split("-")[0]) : null;

            if (subscription != null)
                subscription.getCountInHttpRequests().inc();

            this.addXOriginHeader(rtCtx);

            final WeightedDisturbances.Disturbance disturbance = this.admin.weightedDisturbances.next();

            if (this.messageShallBeDropped(rtCtx, "SL SUBSCRIBE", disturbance))
                return;

            if (disturbance.isDelayAction() && disturbance.getDelayInMillis() > 0)
            {
                rtCtx.vertx().setTimer(disturbance.getDelayInMillis(), __ ->
                {
                    log.debug("SL SUBSCRIBE: response has been delayed by {} ms", disturbance.getDelayInMillis());

                    rtCtx.response()
                         .putHeader(HD_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8)
                         .setChunked(true)
                         .setStatusCode(HttpResponseStatus.CREATED.code());

                    if (!this.admin.omitLocationHeader)
                        rtCtx.response().putHeader(HD_LOCATION, this.createLocationHeader(rtCtx.request().absoluteURI(), subscriptionId));

                    rtCtx.response().end(SUBSCRIBE_RESPONSE_BODY);

                    if (subscription != null)
                        subscription.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.CREATED.code()).inc();
                });
            }
            else
            {
                rtCtx.response()
                     .putHeader(HD_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8)
                     .setChunked(true)
                     .setStatusCode(HttpResponseStatus.CREATED.code());

                if (!this.admin.omitLocationHeader)
                    rtCtx.response().putHeader(HD_LOCATION, this.createLocationHeader(rtCtx.request().absoluteURI(), subscriptionId));

                rtCtx.response().end(SUBSCRIBE_RESPONSE_BODY);

                if (subscription != null)
                    subscription.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.CREATED.code()).inc();
            }
        }
        else
        {
            final IpFamily ipFamily = rtCtx.get(DataIndex.IP_FAMILY.name());
            final Event event = new Event(EventTrigger.NCHF_SPENDING_LIMIT_CONTROL_SUBSCRIBE.name(), String.class.getName(), rtCtx.getBodyAsString());
            final String subscriptionId = this.sessionDataStore.getRef();
            final Subscription subscription = this.getSubscription(subscriptionId.split("-")[0]);

            subscription.setContext(rtCtx);
            subscription.getHistoryOfEvents().put(event);
            subscription.getCountInHttpRequests().inc();
            subscription.getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            this.handleDisturbanceThenHandleRequest(rtCtx, event, subscription.getCountOutHttpResponsesPerStatus(), "SL SUBSCRIBE", "init", routingContext ->
            {
                final String notifyUri = getNotifUriFromJsonBody(routingContext);

                // Send request to the backend to store charging data ref+notifyUri
                this.redis.set(Arrays.asList(subscriptionId, notifyUri), res ->
                {
                    try
                    {
                        if (res.succeeded())
                        {
                            log.info("SL Subscribe: Sending 201 Created, new subscription ID = {}, notifyUri = {}, body length = {}, Redis: {}",
                                     subscriptionId,
                                     notifyUri,
                                     routingContext.getBody() != null ? routingContext.getBody().length() : 0,
                                     res.result());

                            routingContext.response()//
                                          .putHeader(HD_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8)
                                          .setChunked(true);

                            if (!this.admin.omitLocationHeader)
                                routingContext.response().putHeader(HD_LOCATION, this.createLocationHeader(rtCtx.request().absoluteURI(), subscriptionId));

                            this.addXOriginHeader(routingContext);

                            routingContext.response()
                                          .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                                          .end(SUBSCRIBE_RESPONSE_BODY);
                        }
                        else // failed to store data in backend
                        {
                            final String msg = "SL Subscribe: Failed to store subscriptionId '" + subscriptionId + "' and notifyUri '" + notifyUri + "'.";
                            this.replyWithError(routingContext, event.setResponse(this.admin.rejectAnswer, msg), null, null, null);
                        }
                    }
                    catch (Exception e)
                    {
                        final String msg = "SL Subscribe: Connection was already closed when trying to send reply. This can happen when envoy times out earlier than our delay finishes.";
                        log.info(msg);

                        event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                    }
                    finally
                    {
                        subscription.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                        subscription.getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                    }
                });
            });
        }
    }

    /**
     * Handle Nchf_SpendingLimitControl_Terminate request
     * 
     * @param rtCtx
     */
    private void handlerNchfSpendingLimitControlTerminate(final RoutingContext rtCtx)
    {
        Optional.ofNullable(rtCtx.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                .ifPresent(header -> rtCtx.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

        if (this.admin.loadTestMode)
        {
            final String subscriptionId = this.prefix + this.startIds;
            final Subscription subscription = this.admin.loadTestStatisticsMode ? this.getSubscription(subscriptionId.split("-")[0]) : null;

            if (subscription != null)
                subscription.getCountInHttpRequests().inc();

            this.addXOriginHeader(rtCtx);

            final WeightedDisturbances.Disturbance disturbance = this.admin.weightedDisturbances.next();

            if (this.messageShallBeDropped(rtCtx, "SL TERMINATE", disturbance))
                return;

            if (disturbance.isDelayAction() && disturbance.getDelayInMillis() > 0)
            {
                rtCtx.vertx().setTimer(disturbance.getDelayInMillis(), __ ->
                {
                    log.debug("SL TERMINATE: response has been delayed by {} ms", disturbance.getDelayInMillis());

                    rtCtx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();

                    if (subscription != null)
                        subscription.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.NO_CONTENT.code()).inc();
                });
            }
            else
            {
                rtCtx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();

                if (subscription != null)
                    subscription.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.NO_CONTENT.code()).inc();
            }
        }
        else
        {
            final IpFamily ipFamily = rtCtx.get(DataIndex.IP_FAMILY.name());
            final Event event = new Event(EventTrigger.NCHF_SPENDING_LIMIT_CONTROL_TERMINATE.name(), JsonObject.class.getName(), rtCtx.getBodyAsString());
            final String subscriptionId = this.sessionDataStore.getRef();
            final Subscription subscription = this.getSubscription(subscriptionId.split("-")[0]);

            subscription.setContext(rtCtx);
            subscription.getHistoryOfEvents().put(event);
            subscription.getCountInHttpRequests().inc();
            subscription.getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            this.handleDisturbanceThenHandleRequest(rtCtx, event, subscription.getCountOutHttpResponsesPerStatus(), "SL TERMINATE", "term", routingContext ->
            {
                this.addXOriginHeader(rtCtx);
                rtCtx.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();

                subscription.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                subscription.getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
            });
        }
    }

    /**
     * Handle Nchf_SpendingLimitControl_Unsubscribe request. A client wants to
     * terminate the subscription. If the supplied subscriptionId is correct and in
     * use, then mark the subscriptionId as available and return 204 No Content.
     * Otherwise return an error 400 Bad Request.
     * 
     * @param rtCtx
     */
    private void handlerNchfSpendingLimitControlUnsubscribe(final RoutingContext rtCtx)
    {
        Optional.ofNullable(rtCtx.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                .ifPresent(header -> rtCtx.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

        if (this.admin.loadTestMode)
        {
            final String subscriptionId = this.prefix + this.startIds;
            final Subscription subscription = this.admin.loadTestStatisticsMode ? this.getSubscription(subscriptionId.split("-")[0]) : null;

            if (subscription != null)
                subscription.getCountInHttpRequests().inc();

            this.addXOriginHeader(rtCtx);

            final WeightedDisturbances.Disturbance disturbance = this.admin.weightedDisturbances.next();

            if (this.messageShallBeDropped(rtCtx, "SL UNSUBSCRIBE", disturbance))
                return;

            if (disturbance.isDelayAction() && disturbance.getDelayInMillis() > 0)
            {
                rtCtx.vertx().setTimer(disturbance.getDelayInMillis(), __ ->
                {
                    log.debug("SL UNSUBSCRIBE: response has been delayed by {} ms", disturbance.getDelayInMillis());

                    rtCtx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();

                    if (subscription != null)
                        subscription.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.NO_CONTENT.code()).inc();
                });
            }
            else
            {
                rtCtx.response().setStatusCode(HttpResponseStatus.NO_CONTENT.code()).end();

                if (subscription != null)
                    subscription.getCountOutHttpResponsesPerStatus().get(HttpResponseStatus.NO_CONTENT.code()).inc();
            }
        }
        else
        {
            final IpFamily ipFamily = rtCtx.get(DataIndex.IP_FAMILY.name());
            final Event event = new Event(EventTrigger.NCHF_SPENDING_LIMIT_CONTROL_UNSUBSCRIBE.name(), String.class.getName(), rtCtx.getBodyAsString());
            final String subscriptionId = rtCtx.request().getParam(SUBSCRIPTION_ID);
            final Subscription subscription = this.getSubscription(subscriptionId.split("-")[0]);

            subscription.setContext(rtCtx);
            subscription.getHistoryOfEvents().put(event);
            subscription.getCountInHttpRequests().inc();
            subscription.getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            this.handleDisturbanceThenHandleRequest(rtCtx, event, subscription.getCountOutHttpResponsesPerStatus(), "SL UNSUBSCRIBE", "term", routingContext ->
            {
                // Delete the subscription-ID from the front-end:
                if (this.sessionDataStore.isValid(subscriptionId))
                {
                    this.sessionDataStore.releaseRef(subscriptionId);
                }

                // Also delete the subscription-ID from the backend
                this.redis.del(Arrays.asList(subscriptionId), res ->
                {
                    try
                    {
                        if (res.succeeded() && (res.result().toInteger() == 1))
                        {
                            log.info("SL Release: Sending 204 No Content for {}", subscriptionId);
                            routingContext.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
                        }
                        else
                        {
                            final String msg = "SL Release: Not found: subscriptionId '" + subscriptionId + "'.";
                            this.replyWithError(routingContext, event.setResponse(HttpResponseStatus.BAD_REQUEST, msg), null, null, null);
                        }
                    }
                    catch (Exception e)
                    {
                        final String msg = "SL Release: Connection was already closed when trying to send reply. This can happen when envoy times out earlier than our delay finishes.";
                        log.info(msg);
                        event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                    }
                    finally
                    {
                        subscription.getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                        subscription.getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                    }
                });
            });
        }
    }

    private void initRoutes(final WebServerPool webServerExt)
    {
        final IpFamily ipFamily = IpFamily.of(webServerExt.getHttpOptions().getHost());

        {
            // Register the Nchf_ConvergedCharging URL endpoints we listen on for China
            // Telecom (中国电信):
            Router router = Router.router(this.vertx);

            // OP_NOTIFY_CC_RECEIVER is not necessary as the router is mounted to
            // API_CT_CONVERGED_CHARGING_NOTIFY_CG, so it is unique already. But to keep the
            // code base small, allow also OP_NOTIFY_CC_RECEIVER at the end of the route.
            log.info("TEST 0");
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + "(" + OP_NOTIFY_CC_RECEIVER + ")?").handler(BodyHandler.create());
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + "(" + OP_NOTIFY_CC_RECEIVER + ")?")
                  .handler(ctx -> this.handlerNchfConvergedChargingNotify(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            webServerExt.mountRouter(API_CT_CONVERGED_CHARGING_NOTIFY_CG, router);
        }
        {
            // Register the Nchf_ConvergedCharging URL endpoints we listen on:
            Router router = Router.router(this.vertx);

            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + OP_NOTIFY_CC_RECEIVER).handler(BodyHandler.create());
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + OP_NOTIFY_CC_RECEIVER)
                  .handler(ctx -> this.handlerNchfConvergedChargingNotify(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            // Extra chargingDataRef in URI of CC CREATE request is specific to China
            // Telecom (中国电信):
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + "\\/(?<chargingDataRef>[^\\/]+)").handler(BodyHandler.create());
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + "\\/(?<chargingDataRef>[^\\/]+)")
                  .handler(ctx -> this.handlerNchfConvergedChargingCreate(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + "\\/?").handler(BodyHandler.create());
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + "\\/?")
                  .handler(ctx -> this.handlerNchfConvergedChargingCreate(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + "\\/(?<chargingDataRef>[^\\/]+)" + OP_UPDATE).handler(BodyHandler.create());
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + "\\/(?<chargingDataRef>[^\\/]+)" + OP_UPDATE)
                  .handler(ctx -> this.handlerNchfConvergedChargingUpdate(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + "\\/(?<chargingDataRef>[^\\/]+)" + OP_DELETE)
                  .handler(ctx -> this.handlerNchfConvergedChargingRelease(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_CHARGINGDATA + "\\/(?<chargingDataRef>[^\\/]+)" + OP_RELEASE)
                  .handler(ctx -> this.handlerNchfConvergedChargingRelease(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            webServerExt.mountRouter(API_CONVERGED_CHARGING, router);
        }
        {
            // Register the Nchf_SpendingLimitControl URL endpoints we listen on:
            Router router = Router.router(this.vertx);

            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_SUBSCRIPTIONS + "\\/?").handler(BodyHandler.create());
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_SUBSCRIPTIONS + "\\/?")
                  .handler(ctx -> this.handlerNchfSpendingLimitControlSubscribe(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_SUBSCRIPTIONS + OP_NOTIFY).handler(BodyHandler.create());
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_SUBSCRIPTIONS + OP_NOTIFY)
                  .handler(ctx -> this.handlerNchfSpendingLimitControlNotify(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_SUBSCRIPTIONS + OP_TERMINATE).handler(BodyHandler.create());
            router.postWithRegex("\\/(?<version>v\\d+)" + PATH_SUBSCRIPTIONS + OP_TERMINATE)
                  .handler(ctx -> this.handlerNchfSpendingLimitControlTerminate(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            router.putWithRegex("\\/(?<version>v\\d+)" + PATH_SUBSCRIPTIONS + "\\/(?<subscriptionId>[^\\/]+)\\/?").handler(BodyHandler.create());
            router.putWithRegex("\\/(?<version>v\\d+)" + PATH_SUBSCRIPTIONS + "\\/(?<subscriptionId>[^\\/]+)\\/?")
                  .handler(ctx -> this.handlerNchfSpendingLimitControlModify(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            router.deleteWithRegex("\\/(?<version>v\\d+)" + PATH_SUBSCRIPTIONS + "\\/(?<subscriptionId>[^\\/]+)\\/?")
                  .handler(ctx -> this.handlerNchfSpendingLimitControlUnsubscribe(ctx.put(DataIndex.IP_FAMILY.name(), ipFamily)));

            webServerExt.mountRouter(API_SPENDING_LIMIT_CONTROL, router);
        }
    }

    /**
     * Common code for both constructors.
     * 
     * @param redisHost
     */
    private void init(String redisContainerName,
                      int redisContainerPort)
    {
        this.webServerExt.forEach(this::initRoutes);
        this.webServerExtTls.forEach(this::initRoutes);

        // Initialize the Redis client
        RedisInitializer redis = new RedisInitializer(redisContainerName, redisContainerPort);
        redis.start();
    }

    private boolean messageShallBeDropped(final RoutingContext routingContext,
                                          final String requestName,
                                          final WeightedDisturbances.Disturbance disturbance)
    {
        return this.messageShallBeDropped(routingContext, requestName, disturbance, null);
    }

    private boolean messageShallBeDropped(final RoutingContext routingContext,
                                          final String requestName,
                                          final WeightedDisturbances.Disturbance disturbance,
                                          final String disturbanceMessageType)
    {
        // Simulate disturbance: don't reply to this message and (optionally) reset the
        // connection with drop answer

        final boolean messageShallBeDropped = disturbance.isDropAction()
                                              && (disturbanceMessageType == null || this.admin.dropMessageType.equalsIgnoreCase(disturbanceMessageType)
                                                  || this.admin.dropMessageType.equalsIgnoreCase("all"));

        if (messageShallBeDropped)
        {
            if (disturbance.getDropAnswer() > 1)
                routingContext.response().reset(disturbance.getDropAnswer());

            log.info("{}: Dropping message due to set dropAnswer {} and dropMessageType {}",
                     requestName,
                     disturbance.getDropAnswer(),
                     this.admin.dropMessageType);
        }

        return messageShallBeDropped;
    }

    private void replyWithError(final RoutingContext context,
                                final Event event,
                                final String nfInstanceId,
                                final String nfType,
                                final String invalidParameter)
    {
        final ProblemDetails problem = new ProblemDetails();

        problem.setStatus(event.getResponse().getResultCode());
        problem.setCause(event.getResponse().getResultReasonPhrase());

        if (nfInstanceId != null)
            problem.setInstance(nfInstanceId);

        if (nfType != null)
            problem.setType(nfType);

        if (event.getResponse().getResultDetails() != null)
            problem.setDetail(event.getResponse().getResultDetails());

        if (invalidParameter != null)
        {
            InvalidParam i = new InvalidParam();
            i.setParam(invalidParameter);
            problem.addInvalidParamsItem(i);
        }

        String problemStr;

        try
        {
            problemStr = json.writeValueAsString(problem);
        }
        catch (JsonProcessingException e)
        {
            problemStr = e.toString();
        }

        if (400 <= event.getResponse().getResultCode() && event.getResponse().getResultCode() < 500)
        {
            log.warn(problemStr);
        }
        else if (500 <= event.getResponse().getResultCode() && event.getResponse().getResultCode() < 600)
        {
            log.error(problemStr);
        }

        context.response()
               .setStatusCode(event.getResponse().getResultCode())
               .putHeader(HD_CONTENT_TYPE, "application/problem+json; charset=utf-8")
               .end(problemStr);
    }

    /**
     * Send a Notify request to envoy.
     * <p>
     * An extra header "x-notify-uri" is added that holds the final destination for
     * the Notify request.
     * <p>
     * However, that header is not added in case the API is China Telecom specific.
     * In this case the notifyUri passed is used directly as Notify-request URI.
     * 
     * @param routingContext
     * @param chargingDataRef
     * @param notifyUri
     */
    private void sendNchfConvergedChargingNotify(RoutingContext routingContext,
                                                 String chargingDataRef,
                                                 String notifyUri)
    {
        boolean isChinaTelecomSpecific = notifyUri.contains(API_CT_CONVERGED_CHARGING_NOTIFY_CG);

        final String csaNotifyUri = isChinaTelecomSpecific ? notifyUri
                                                           : new StringBuilder(this.admin.envoyDomain.getUrl().toString()).append("/notify-service/v1/notify")
                                                                                                                          .toString();

        final HttpRequest<Buffer> request = this.webClient.requestAbs(HttpMethod.POST, this.admin.envoyDomain.getAddr(), csaNotifyUri);

        if (isChinaTelecomSpecific)
        {
            log.info("(China Telekom) Sending Notify to address '{}' with uri '{}' for chargingDataRef '{}'.",
                     this.admin.envoyDomain.getAddr(),
                     csaNotifyUri,
                     chargingDataRef);
            final String viaHeader = routingContext != null ? routingContext.request().getParam("viaHeader") : null;
            if (viaHeader != null)
                request.headers().add("via", viaHeader);
        }
        else
        {
            log.info("Sending Notify to '{}' with header x-notify-uri '{}' for chargingDataRef '{}'.", csaNotifyUri, notifyUri, chargingDataRef);

            if (this.admin.notifyHeader.equalsIgnoreCase(HD_X_NOTIFY_URI))
                request.headers().add(HD_X_NOTIFY_URI, notifyUri);
            else
                request.headers().add(HD_3GPP_SBI_TARGET_APIROOT, notifyUri);

            if (this.admin.sbiNfPeerInfo != null)
                request.headers().add(HD_3GPP_SBI_NF_PEER_INFO, this.admin.sbiNfPeerInfo);
        }

        final ChargingNotifyRequest data = new ChargingNotifyRequest();
        data.setNotificationType(NotificationType.ABORT_CHARGING);

        try
        {
            final String requestBody = json.writeValueAsString(data);

            request//
                   .sendJsonObject(new JsonObject(requestBody), ar ->
                   {
                       final Event event = new Event(EventTrigger.NCHF_CONVERGED_CHARGING_NOTIFY.name(), JsonObject.class.getName(), requestBody);

                       final ChargingInstance chInstance = this.admin.sessionIdMode ? this.getChargingInstance(chargingDataRef.substring(chargingDataRef.lastIndexOf("p"),
                                                                                                                                         chargingDataRef.length()))
                                                                                    : this.getChargingInstance(chargingDataRef.split("-")[0]);

                       chInstance.setContext(routingContext);
                       chInstance.getHistoryOfEvents().put(event);

                       if (ar.succeeded())
                       {
                           final HttpResponse<Buffer> response = ar.result();
                           final String responseBody = response.body() == null ? "<no data in body>" : response.bodyAsString();
                           log.info("Received Notify response with status '{}' with data '{}'", response.statusCode(), responseBody);

                           event.setResponse(HttpResponseStatus.valueOf(response.statusCode()));

                           if (routingContext != null)
                           {
                               this.addXOriginHeader(routingContext);
                               routingContext.response().setStatusCode(event.getResponse().getResultCode()).end(responseBody);
                           }
                       }
                       else
                       {
                           final String msg = ar.cause().toString();
                           event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                           log.error("Error sending Notify request. Cause: '{}'.", log.isDebugEnabled() ? ar.cause() : msg);

                           if (routingContext != null)
                           {
                               this.addXOriginHeader(routingContext);
                               routingContext.response().setStatusCode(event.getResponse().getResultCode()).end(msg);
                           }
                       }

                       chInstance.getCountOutHttpRequests().inc();
                       chInstance.getCountInHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                   });
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * CC-UPDATE: Send a successful response. This is common code needed in several
     * branches.
     * 
     * @param routingContext
     * @param event
     */
    private void sendNchfConvergedChargingUpdateSuccessfulResponse(final RoutingContext routingContext,
                                                                   final Event event)
    {
        log.debug("CC Update: Sending 200 OK for {}", routingContext.request().getParam("chargingDataRef"));

        final HttpServerResponse response = routingContext.response();

        if (this.admin.sbiNfPeerInfo != null)
            response.headers().add(HD_3GPP_SBI_NF_PEER_INFO, this.admin.sbiNfPeerInfo);

        response.putHeader(HD_CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON_CHARSET_UTF_8)
                .setChunked(true)
                .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                .end(UPDATE_RESPONSE_BODY);
    }

    /**
     * Send a Notify request to the csaNotifyUri given, with an extra header
     * "x-notify-uri" that holds the final destination for the Notify request.
     * 
     * @param routingContext
     * @param subscriptionId
     * @param notifyUri
     * @param csaNotifyUri
     */
    private void sendNchfSpendingLimitControlNotify(RoutingContext routingContext,
                                                    String subscriptionId,
                                                    String notifyUri)
    {
        final String csaNotifyUri = new StringBuilder(this.admin.envoyDomain.getUrl().toString()).append("/notify-service/v1/notify").toString();

        log.info("Sending Notify to '{}' with header x-notify-uri '{}' for subscriptionId '{}'.", csaNotifyUri, notifyUri, subscriptionId);

        final HttpRequest<Buffer> request = this.webClient.requestAbs(HttpMethod.POST, this.admin.envoyDomain.getAddr(), csaNotifyUri);

        if (this.admin.notifyHeader.equalsIgnoreCase(HD_X_NOTIFY_URI))
            request.headers().add(HD_X_NOTIFY_URI, notifyUri);
        else
            request.headers().add(HD_3GPP_SBI_TARGET_APIROOT, notifyUri);

        if (this.admin.sbiNfPeerInfo != null)
            request.headers().add(HD_3GPP_SBI_NF_PEER_INFO, this.admin.sbiNfPeerInfo);

        final SpendingLimitStatus data = new SpendingLimitStatus();
        final PolicyCounterInfo info = new PolicyCounterInfo();
        info.setPolicyCounterId("a_policy_counter_id");
        info.setCurrentStatus("a_status");
        data.putStatusInfosItem(info.getPolicyCounterId(), info);

        try
        {
            final String requestBody = json.writeValueAsString(data);

            request//
                   .sendJsonObject(new JsonObject(requestBody), ar ->
                   {
                       final Event event = new Event(EventTrigger.NCHF_SPENDING_LIMIT_CONTROL_NOTIFY.name(), JsonObject.class.getName(), requestBody);
                       final Subscription subscription = this.getSubscription(subscriptionId.split("-")[0]);

                       subscription.setContext(routingContext);
                       subscription.getHistoryOfEvents().put(event);

                       if (ar.succeeded())
                       {
                           final HttpResponse<Buffer> response = ar.result();
                           final String responseBody = response.body() == null ? "<no data in body>" : response.bodyAsString();
                           log.info("Received Notify response with status '{}' with data '{}'", response.statusCode(), responseBody);

                           event.setResponse(HttpResponseStatus.valueOf(response.statusCode()));

                           if (routingContext != null)
                           {
                               this.addXOriginHeader(routingContext);
                               routingContext.response().setStatusCode(event.getResponse().getResultCode()).end(responseBody);
                           }
                       }
                       else
                       {
                           final String msg = ar.cause().toString();
                           event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                           log.error("Error sending Notify request. Cause: '{}'.", log.isDebugEnabled() ? ar.cause() : msg);

                           if (routingContext != null)
                           {
                               this.addXOriginHeader(routingContext);
                               routingContext.response().setStatusCode(event.getResponse().getResultCode()).end(msg);
                           }
                       }

                       subscription.getCountOutHttpRequests().inc();
                       subscription.getCountInHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                   });
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send a Terminate request to the csaNotifyUri given, with an extra header
     * "x-notify-uri" that holds the final destination for the Terminate request.
     * 
     * @param routingContext
     * @param subscriptionId
     * @param notifyUri
     * @param csaNotifyUri
     */
    private void sendNchfSpendingLimitControlTerminate(RoutingContext routingContext,
                                                       String subscriptionId,
                                                       String notifyUri)
    {
        final String csaNotifyUri = new StringBuilder(this.admin.envoyDomain.getUrl().toString()).append("/notify-service/v1/terminate").toString();

        log.info("Sending Terminate to '{}' with header x-notify-uri '{}' for subscriptionId '{}'.", csaNotifyUri, notifyUri, subscriptionId);

        final HttpRequest<Buffer> request = this.webClient.requestAbs(HttpMethod.POST, this.admin.envoyDomain.getAddr(), csaNotifyUri);

        if (this.admin.notifyHeader.equalsIgnoreCase(HD_X_NOTIFY_URI))
            request.headers().add(HD_X_NOTIFY_URI, notifyUri);
        else
            request.headers().add(HD_3GPP_SBI_TARGET_APIROOT, notifyUri);

        if (this.admin.sbiNfPeerInfo != null)
            request.headers().add(HD_3GPP_SBI_NF_PEER_INFO, this.admin.sbiNfPeerInfo);

        final SubscriptionTerminationInfo data = new SubscriptionTerminationInfo();
        data.setSupi(subscriptionId);

        try
        {
            final String requestBody = json.writeValueAsString(data);

            request//
                   .sendJsonObject(new JsonObject(requestBody), ar ->
                   {
                       final Event event = new Event(EventTrigger.NCHF_SPENDING_LIMIT_CONTROL_TERMINATE.name(), JsonObject.class.getName(), requestBody);
                       final Subscription subscription = this.getSubscription(subscriptionId.split("-")[0]);

                       subscription.setContext(routingContext);
                       subscription.getHistoryOfEvents().put(event);

                       if (ar.succeeded())
                       {
                           final HttpResponse<Buffer> response = ar.result();
                           final String responseBody = response.body() == null ? "<no data in body>" : response.bodyAsString();
                           log.info("Received Terminate response with status '{}' with data '{}'", response.statusCode(), responseBody);

                           event.setResponse(HttpResponseStatus.valueOf(response.statusCode()));

                           if (routingContext != null)
                           {
                               this.addXOriginHeader(routingContext);
                               routingContext.response().setStatusCode(event.getResponse().getResultCode()).end(responseBody);
                           }
                       }
                       else
                       {
                           final String msg = ar.cause().toString();
                           event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg);
                           log.error("Error sending Terminate request. Cause: '{}'.", log.isDebugEnabled() ? ar.cause() : msg);

                           if (routingContext != null)
                           {
                               this.addXOriginHeader(routingContext);
                               routingContext.response().setStatusCode(event.getResponse().getResultCode()).end(msg);
                           }
                       }

                       subscription.getCountOutHttpRequests().inc();
                       subscription.getCountInHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                   });
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

}
