/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Sep 6, 2022
 *     Author: esrhpac
 */

package com.ericsson.sc.sepp.manager;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.SecNegotiateReqData;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.SecNegotiateRspData;
import com.ericsson.sc.glue.IfTypedNfAddressProperties;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.proxy.ConfigHelper;
import com.ericsson.sc.rxetcd.EtcdEntries;
import com.ericsson.sc.rxetcd.EtcdEntry;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.sepp.config.ConfigUtils;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.EricssonSeppSeppFunction;
import com.ericsson.sc.sepp.model.N32C__1;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.ReceivedPlmnId;
import com.ericsson.sc.sepp.model.StaticNfService;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Triplet;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.reactivex.RetryBackoffFunction;
import com.ericsson.utilities.reactivex.RetryFunction;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.api.KeyValue;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.net.SocketAddress;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpResponse;

/**
 * Encapsulates all handling for n32c Interface.
 */
public class N32cInterface
{
    private static final Logger log = LoggerFactory.getLogger(N32cInterface.class);
    private static final SeppManagerInterfacesParameters params = SeppManagerInterfacesParameters.instance;
    private static final URI EXCHANGE_CAPABILITY_URI_PATH = URI.create("/n32c-handshake/v1/exchange-capability");
    private static final URI LISTENER_CONNECTION_URI_PATH = URI.create("/n32c-listener-connection");
    private static final String APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String CONTENT_TYPE = HttpHeaders.CONTENT_TYPE.toString();
    private static final String X_HOST_HEADER = "x-host";
    private static final String X_CLUSTER_HEADER = "x-cluster";
    private static final String CONTENT_TYPE_HEADER = "content-type";
    private static final String CONTENT_LENGTH = HttpHeaders.CONTENT_LENGTH.toString();
    // initializing sepp scenario tls enable indicator
    // This only concerns the internal part of the communication (manager->worker)
    // disabled if global tls is disabled
    // optionally may be disabled if global tls is enabled
    private static final Boolean N32C_INIT_TLS_ENABLED = Boolean.parseBoolean(EnvVars.get("N32C_INIT_TLS_ENABLED", true));
    private static final URI N32C_CERT_PATH = URI.create(EnvVars.get("N32C_CLIENT_CERT_PATH", "/run/secrets/n32c/client/certificates"));
    private static final URI N32C_CA_PATH = URI.create(EnvVars.get("SIP_TLS_TRUSTED_ROOT_CA_PATH", "/run/secrets/siptls/ca"));
    private static final ObjectMapper json = OpenApiObjectMapper.singleton();
    private static final String KEY_SECURITY_NEGOTIATION_DATA = "security-negotiation-data";
//    private static final String KEY_STATIC_NFINSTANCE_REF = "static-nf-instance-ref";
    private static final String KEY_SEPP_NAME = "sepp-name";
    private static final String KEY_ROAMING_PARTNER_REF = "roaming-partner-ref";
    private static final String KEY_OPERATIONAL_STATE = "operational-state";
    private static final String KEY_REASON = "reason";
    private static final String KEY_LAST_UPDATE = "last-update";
    private static final String KEY_NUMBER_OF_FAILURES = "num-of-failures";
    private static final String KEY_SECURITY_CAPABILITY = "security-capability";
    private static final String KEY_RECEIVED_PLMN_ID = "received-plmn-id";
    private static final String KEY_MCC = "mcc";
    private static final String KEY_MNC = "mnc";
    private static final String KEY_VALUE = "value";
    private static final String KEY_SUPPORTS_TARGET_APIROOT = "supports-target-apiroot";
    private static final String OPERATIONAL_STATUS_INACTIVE = "inactive";
    private static final String OPERATIONAL_STATUS_ACTIVE = "active";
    private static final String OPERATIONAL_STATUS_FAULTY = "faulty";
    private static final String PREFIX = "/ericsson/sc/sepp/n32c";
    private static final int REQUEST_TIMEOUT_MILLIS = 5000;
    private static final String CONTENT_METHOD = HttpMethod.POST.toString();
    private static final String METHOD_NOT_ALLOWED = "Method Not Allowed";
    private static final String LENGTH_REQUIRED = "Length Required";
    private static final String BAD_REQUEST = "Bad Request";

    private static final String UNSUPPORTED_MEDIA_TYPE = "Unsupported Media Type";
    private static final String FORBIDDEN = "Forbidden";
    private static final String CT_APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String CT_APPLICATION_PROBLEM_JSON = "application/problem+json; charset=utf-8";
    private static final String SERVICE_ERIC_SEPP_WORKER = "eric-sepp-worker";
    private static final String SERVICE_ERIC_SEPP_WORKER_N32C = "eric-sepp-worker-n32c";
    private static final int PORT_REST_N32C = 8043;
    private final WebClientProvider n32cClient;
    private N32cAlarmHandler n32cAh;
    private RxEtcd rxEtcd;
    private ObjectMapper om = Jackson.om();
    private N32cSerializer serializer;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");// yyyy-MM-dd'T'HH:mm:ssZ
    private Optional<EricssonSepp> config;
    private Map<String, UUID> retriableSepps = Collections.synchronizedMap(new HashMap<>());  // synchronized might be slower compared to concurrent, however in
                                                                                              // concurrent the behavior of clear() is undefined

    private final RetryFunction dbReadRetryPolicy = new RetryFunction().withDelay(5 * 1000L) // 5 seconds
                                                                       .withRetries(60); // 5 minutes total
    private final RetryFunction dbReadRetryPolicy2 = new RetryFunction().withDelay(20 * 1000L) // 20 seconds
                                                                        .withRetries(60); // 5 minutes total

    private final RetryBackoffFunction sendRetryPolicy = new RetryBackoffFunction().withMaxBackoff(60000L)
                                                                                   .withBackoff(-1, 1000L)
                                                                                   .withJitter(100)
                                                                                   .withRetryAction((err,
                                                                                                     retry) -> log.debug("{}: Failed, reason: {}, retry: {}",
                                                                                                                         Thread.currentThread().getName(),
                                                                                                                         err.getMessage(),
                                                                                                                         retry))
                                                                                   .withPredicate(e ->
                                                                                   {
                                                                                       if (e instanceof N32cFaultySeppException)
                                                                                       {
                                                                                           var fse = (N32cFaultySeppException) e;
                                                                                           return Optional.ofNullable(retriableSepps.get(fse.getFaultySeppFqdn()))
                                                                                                          .filter(id -> id.equals(fse.getId()))
                                                                                                          .isPresent();

                                                                                       }
                                                                                       return false;
                                                                                   });

    enum ETCD_OPERATION
    {
        GET,
        PUT,
        DELETE
    }

    public N32cInterface(RxEtcd rxEtcd,
                         N32cAlarmHandler n32cAh)
    {
        this.rxEtcd = rxEtcd;
        this.n32cAh = n32cAh;
        this.n32cClient = createWebClient();
        this.serializer = new N32cSerializer(om);
    }

    private static WebClientProvider createWebClient()
    {
        var wcb = WebClientProvider.builder().withHostName(params.serviceHostname);
        if (N32C_INIT_TLS_ENABLED.booleanValue())
        {
            wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(N32C_CERT_PATH.getPath()),
                                                            SipTlsCertWatch.trustedCert(N32C_CA_PATH.getPath())));

        }

        return wcb.build(VertxInstance.get());

    }

    private Completable listenerResponse(int response)
    {
        log.debug("n32c listener is ready and the status code is {}", response);
        return Completable.complete();
    }

    public Completable checkTheConnection()
    {

        var scheme = N32C_INIT_TLS_ENABLED.booleanValue() ? "https://" : "http://";

        return this.n32cClient.getWebClient()
                              .flatMapCompletable(wc -> wc.requestAbs(HttpMethod.GET,
                                                                      SocketAddress.inetSocketAddress(PORT_REST_N32C, SERVICE_ERIC_SEPP_WORKER_N32C),
                                                                      buildUrl(scheme,
                                                                               SERVICE_ERIC_SEPP_WORKER,
                                                                               PORT_REST_N32C,
                                                                               LISTENER_CONNECTION_URI_PATH.getPath()))
                                                          .timeout(REQUEST_TIMEOUT_MILLIS)
                                                          .rxSend()
                                                          .flatMapCompletable(resp -> listenerResponse(resp.statusCode())))
                              .doOnError(e -> log.warn("Failed to send a GET request to listener with reason:{}", e.getMessage()))
                              .retryWhen(dbReadRetryPolicy2.create());

    }

    public void setConfig(Optional<EricssonSepp> config)
    {
        this.config = config;
    }

    /**
     * Invoked from the manager in case new configuration arrives that disables N32c
     * i.e makes the (own) N32c container empty. Such scenarios don't result in the
     * {@link #sendN32cRequest(EricssonSeppSeppFunction, boolean)} invocations
     * however clearing the shared collection that holds retries information is
     * still needed
     */
    public void clearStaleRetries()
    {
        this.retriableSepps.clear();
    }

    /**
     * 
     * Create a method which collect the own Security-Negotiate-Data and puts in to
     * an object of type SecNegotiateRspData or type SecNegotiateReqData based to
     * input data.
     * 
     * @return
     */
    private class OwnSecNegotiateData
    {
        private String sender;
        private List<String> selectedSecCapability;
        private List<PlmnId> plmnIdList;
        private Boolean sbiTargetApiRootSupported;

        public OwnSecNegotiateData()
        {
            this.sender = null;
            this.selectedSecCapability = null;
            this.plmnIdList = null;
            this.sbiTargetApiRootSupported = false;

        }

        private String getSender()
        {
            return this.sender;
        }

        private List<String> getSelectedSecCapability()
        {
            return this.selectedSecCapability;
        }

        private List<PlmnId> getPlmnIdList()
        {
            return this.plmnIdList;
        }

        private Boolean getSbiTargetApiRootSupported()
        {
            return this.sbiTargetApiRootSupported;
        }

        private void setSender(String sender)
        {
            this.sender = sender;
        }

        private void setselectedSecCapability(List<String> supportedSecCapabilityList)
        {
            this.selectedSecCapability = supportedSecCapabilityList;
        }

        private void setPlmnIdList(List<PlmnId> plmnIdList)
        {
            this.plmnIdList = plmnIdList;
        }

        private void setSbiTargetApiRootSupported(Boolean sbiTargetApiRootSupported)
        {
            this.sbiTargetApiRootSupported = sbiTargetApiRootSupported;
        }

    }

    /**
     * 
     * Prepares security negotiation data to be used in initiating and responding of
     * N32-C.
     * 
     * @param
     *
     * @return OwnSecNegotiateData
     */
    private OwnSecNegotiateData prepareSecNegotiateData()
    {

        final EricssonSeppSeppFunction curr = this.config.get().getEricssonSeppSeppFunction();
        // Create a HashMap which contains one empty object of SecNegotiateRspData type
        // and one empty object of SecNegotiateReqData type
        var n32c = curr.getNfInstance().get(0).getN32C().getOwnSecurityData().get(0);

        // collect the ownNetwork Security-Negotiate-Data
        OwnSecNegotiateData ownSecNegotiateData = new OwnSecNegotiateData();

        // collect the ownNetwork Security-Negotiate-Data
        List<PlmnId> plmnIdList = new ArrayList<>();
        String primaryPlmnIdMcc = n32c.getPrimaryPlmnIdMcc();
        String primaryPlmnIdMnc = n32c.getPrimaryPlmnIdMnc();
        plmnIdList.add(new PlmnId().mcc(primaryPlmnIdMcc).mnc(primaryPlmnIdMnc));

        n32c.getAdditionalPlmnId().forEach(additionalPlmnId -> plmnIdList.add(new PlmnId().mcc(additionalPlmnId.getMcc()).mnc(additionalPlmnId.getMnc())));

        List<String> supportedSecCapabilityList = new ArrayList<>();
        n32c.getSecurityCapability().forEach(securityCapability -> supportedSecCapabilityList.add(securityCapability.toString()));

        Boolean sbiTargetApiRootSupported = n32c.getSupportsTargetApiroot();
        var sender = Utils.getByName(curr.getNfInstance().get(0).getServiceAddress(),
                                     curr.getNfInstance().get(0).getExternalNetwork().get(0).getServiceAddressRef());

        // Put the Security Negotiate Data in the object that needs it.
        ownSecNegotiateData.setSender(sender.getFqdn());
        ownSecNegotiateData.setselectedSecCapability(supportedSecCapabilityList);
        ownSecNegotiateData.setPlmnIdList(plmnIdList);
        ownSecNegotiateData.setSbiTargetApiRootSupported(sbiTargetApiRootSupported);

        return ownSecNegotiateData;

    }

    /**
     * 
     * Create a method which takes as an input the context of the request from
     * remote sepp and responds to it after the protocol check with the appropriate
     * answer.
     * 
     * @param context
     *
     * @return
     */
    public Completable handlePostExchangeCapability(RoutingContext context)
    {
        log.info("N32-c: Post exchange capability received from remote sepp");
        try
        {
            // check based to the context
            var contextError = handleTheReqContext(context);
            if (contextError.getStatus() != null)
            {
                String errorBody = json.writeValueAsString(contextError);
                log.warn(errorBody);

                context.response().setStatusCode(contextError.getStatus()).putHeader(CONTENT_TYPE_HEADER, CT_APPLICATION_PROBLEM_JSON).end(errorBody);
                // step the failures counter and incoming request counter.
                N32cCounters.stepCcRespondingN32cInReq("sepp-context-error", "");
                N32cCounters.stepCcRespondingN32cFailures("sepp-context-error", "", errorBody);
                return Completable.complete();
            }

            // Get from the request context the Security-Negotiate-Data
            final String body = context.getBodyAsString();

            SecNegotiateReqData request = json.readValue(body, SecNegotiateReqData.class);

            // Get from configuration the Security-Negotiate-Data of own network
            var secNegotiateData = prepareSecNegotiateData();

            SecNegotiateRspData response = new SecNegotiateRspData();
            response.setSender(secNegotiateData.getSender());
            response.setSelectedSecCapability(secNegotiateData.getSelectedSecCapability().get(0));
            response.set3gppSbiTargetApiRootSupported(secNegotiateData.getSbiTargetApiRootSupported());
            response.setPlmnIdList(secNegotiateData.getPlmnIdList());

            // Check if the Security-Negotiate-Data of request are supported
            var protocolLogicalErrors = checkTheSecNegReqData(request, response);

            ProblemDetails problemDetails = protocolLogicalErrors.getFirst();
            String details = protocolLogicalErrors.getSecond();
            // Get the sepp and the roaming partner name
            List<String> seppData = protocolLogicalErrors.getThird();

            if (!details.equals(""))
            {
                log.warn("Exchange Capability operation failed with reason: {} ", details);

                String errorDetails = json.writeValueAsString(problemDetails);
                log.warn(errorDetails);
                context.response().setStatusCode(problemDetails.getStatus()).putHeader(CONTENT_TYPE_HEADER, CT_APPLICATION_PROBLEM_JSON).end(errorDetails);
                if (!Objects.equals(details, "No sender received") && !Objects.equals(details, "Unknown sender"))
                {
                    // step the incoming request counter
                    N32cCounters.stepCcRespondingN32cInReq(seppData.get(0), seppData.get(1));
                    // step the failures counter
                    N32cCounters.stepCcRespondingN32cFailures(seppData.get(0), seppData.get(1), problemDetails.toString());
                    return writeSecurityNegotiationDatumForFaulty(new SecurityNegotiationItemBuilder().newItem()
                                                                                                      .withSeppName(seppData.get(0))
                                                                                                      .withRoamingPartnerRef(seppData.get(1))
                                                                                                      .withReason(details)); // Complete.complete() is returned
                                                                                                                             // here as it's not considered an
                                                                                                                             // error case

                }
                else
                {
                    // setp the incoming request counter
                    N32cCounters.stepCcRespondingN32cInReq("sepp-bad-sender", "");
                    // step the failures counter
                    N32cCounters.stepCcRespondingN32cFailures("sepp-bad-sender", "", problemDetails.toString());
                    return Completable.complete();
                }
            }

            // if the context has not the key value 3gppSbiTargetApiRootSupported create a
            // response without it else response with the properly value
            JSONObject jsonResponse = new JSONObject(json.writeValueAsString(response));

            if (!context.getBodyAsJson().containsKey("3GppSbiTargetApiRootSupported") || Boolean.FALSE.equals(request.get3gppSbiTargetApiRootSupported()))
            {
                jsonResponse.put("3GppSbiTargetApiRootSupported", false);
            }

            // Prepare the PlmnIds which are in the request body to write them to the
            // database
            List<ReceivedPlmnId> receivedPlmnIds = new ArrayList<>();
            if (request.getPlmnIdList() != null)
            {
                request.getPlmnIdList().stream().filter(Objects::nonNull).forEach(plmn ->
                {
                    ReceivedPlmnId receivedPlmnId = new ReceivedPlmnId();
                    receivedPlmnId.setMcc(plmn.getMcc());
                    receivedPlmnId.setMnc(plmn.getMnc());
                    receivedPlmnIds.add(receivedPlmnId);
                });
            }

            context.response().setStatusCode(HttpResponseStatus.OK.code()).putHeader(CONTENT_TYPE_HEADER, CT_APPLICATION_JSON).end(jsonResponse.toString());

            // setp the incoming request counter
            N32cCounters.stepCcRespondingN32cInReq(seppData.get(0), seppData.get(1));
            // step the succ counter
            N32cCounters.stepCcRespondingN32cOutSuccResp(seppData.get(0), seppData.get(1), context.response().getStatusCode());
            return writeSecurityNegotiationDatum(new SecurityNegotiationItemBuilder().newItem()
                                                                                     .withSeppName(seppData.get(0))
                                                                                     .withRoamingPartnerRef(seppData.get(1))
                                                                                     .withOperationalState(OPERATIONAL_STATUS_ACTIVE)
                                                                                     .withSecCap(jsonResponse.getString("selectedSecCapability"))
                                                                                     .withTar(jsonResponse.get("3GppSbiTargetApiRootSupported").toString())
                                                                                     .withPlmnIds(receivedPlmnIds));

        }
        catch (Exception e)
        {
            // Catch the case that body is empty or not in json format
            log.error("Exchange Capability operation failed with reason: {}", e.toString());
            N32cCounters.stepCcRespondingN32cInReq("sepp-external-error", "");
            JsonObject item = new JsonObject().put("cause", "UNSPECIFIED_MSG_FAILURE")
                                              .put("title", BAD_REQUEST)
                                              .put("status", 400)
                                              .put("detail", "Error Message Format");
            context.response()
                   .setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                   .putHeader(CONTENT_TYPE_HEADER, CT_APPLICATION_PROBLEM_JSON)
                   .end(item.toString());
            N32cCounters.stepCcRespondingN32cFailures("sepp-external-error", "", item.toString());
            return Completable.error(e);
        }

    }

    /**
     * 
     * Create a method which take as an input the context of the request and return
     * the Problem Details if exist.
     * 
     * @param context
     *
     * @return ProblemDetails
     */
    private ProblemDetails handleTheReqContext(RoutingContext context)
    {
        log.debug("Header validity checks start");
        if (!Objects.equals(context.request().method().toString(), CONTENT_METHOD))
        {
            return new ProblemDetails().title(METHOD_NOT_ALLOWED)
                                       .detail("Method not allowed, POST expected")
                                       .status(HttpResponseStatus.METHOD_NOT_ALLOWED.code());
        }

        // Content Length header not present
        if (context.request().getHeader(CONTENT_LENGTH) == null)
        {
            return new ProblemDetails().title(LENGTH_REQUIRED)
                                       .detail("The CONTENT-LENGTH header is empty")
                                       .cause("INCORRECT_LENGTH")
                                       .status(HttpResponseStatus.LENGTH_REQUIRED.code());
        }
        // Content type is not json
        if (!context.request().getHeader(CONTENT_TYPE).toLowerCase().contains("json") && !context.request().getHeader(CONTENT_TYPE).isEmpty())
        {
            return new ProblemDetails().title(UNSUPPORTED_MEDIA_TYPE)
                                       .detail("The CONTENT-TYPE header is not JSON")
                                       .cause("INVALID_MSG_FORMAT")
                                       .status(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE.code());
        }
        log.debug("Header validity checks finished without errors");
        return new ProblemDetails();
    }

    /**
     * 
     * Create a method which takes as an input the SecNegotiateReqData and the
     * SecNegotiateRspData and returns the Problem Details of the handshake if
     * exist.
     * 
     * @param secNegotiateReqData
     * @param secNegotiateRspData
     * 
     * @return Triplet<ProblemDetails, String, List<String>>
     */
    private Triplet<ProblemDetails, String, List<String>> checkTheSecNegReqData(SecNegotiateReqData secNegotiateReqData,
                                                                                SecNegotiateRspData secNegotiateRspData)
    {
        log.debug("Check for protocol logical errors start");

        ProblemDetails problemDetails = new ProblemDetails();
        String details = "";

        // protocal check
        // all mandatory elements present (sender,selectedSecCapability)

        // This sonar warning remains, because we might have a null scenario
        if (secNegotiateReqData.getSender() == null || secNegotiateReqData.getSender().isEmpty())
        {

            problemDetails.title(BAD_REQUEST)
                          .detail("Mandatory IE Missing: Sender")
                          .cause("MANDATORY_IE_MISSING")
                          .status(HttpResponseStatus.BAD_REQUEST.code());
            details = "No sender received";

            return Triplet.of(problemDetails, details, null);
        }

        // search if the sender exist in the configuration
        List<String> seppData = searchForTheSepp(secNegotiateReqData.getSender());

        if (seppData.isEmpty())
        {
            problemDetails.title(BAD_REQUEST)
                          .detail("Mandatory element sender does not match any of the configured remote sepp addresses")
                          .cause("UNSPECIFIED_MSG_FAILURE")
                          .status(HttpResponseStatus.BAD_REQUEST.code());

            details = "Unknown sender";

            return Triplet.of(problemDetails, details, seppData);
        }

        // This sonar warning remains, because we might have a null scenario
        if (secNegotiateReqData.getSupportedSecCapabilityList() == null || secNegotiateReqData.getSupportedSecCapabilityList().isEmpty())
        {
            details = "Mandatory IE Missing: SelectedSecCapability";
            problemDetails.title(BAD_REQUEST).detail(details).cause("MANDATORY_IE_MISSING").status(HttpResponseStatus.BAD_REQUEST.code());

            return Triplet.of(problemDetails, details, seppData);
        }

        if (!secNegotiateReqData.getSupportedSecCapabilityList().contains("TLS"))
        {
            details = "Mismatch in the value of selectedSecCapability, expected TLS, received: " + secNegotiateReqData.getSupportedSecCapabilityList();
            problemDetails.title(FORBIDDEN)
                          .detail("Security Capability received is not supported")
                          .cause("NEGOTIATION_NOT_ALLOWED")
                          .status(HttpResponseStatus.FORBIDDEN.code());

            return Triplet.of(problemDetails, details, seppData);
        }

        if (secNegotiateReqData.getPlmnIdList() != null && !secNegotiateReqData.getPlmnIdList().isEmpty())
        {
            List<PlmnId> reqPlmIdList = secNegotiateReqData.getPlmnIdList();

            // collect the allow Plmn ids for the sepp
            final EricssonSeppSeppFunction curr = this.config.get().getEricssonSeppSeppFunction();
            List<PlmnId> allowPlmnIdList = new ArrayList<>();

            allowPlmnIdList.addAll(ConfigUtils.getAllowPlmnIdListFromRoamingPartner(curr.getNfInstance().get(0).getExternalNetwork(), seppData.get(1)));

            // check if sepp include the property Plmn Ids
            if (allowPlmnIdList.isEmpty() || !preprocessPlmnIdLists(allowPlmnIdList).containsAll(preprocessPlmnIdLists(reqPlmIdList)))
            {
                var detail = "Received plmnIdList does not match (totally or partially) the configured allow-plmn list of the RP, Received plmnIdList: "
                             + secNegotiateReqData.getPlmnIdList() + ", Configured allow-plmn list of the RP: " + allowPlmnIdList;
                details = detail.replace("class PlmnId ", "").replace("\n    ", " ").replace("\n", "");

                problemDetails.title(BAD_REQUEST)
                              .detail("Received plmnIds do not match the configured ones")
                              .cause("UNSPECIFIED_MSG_FAILURE")
                              .status(HttpResponseStatus.BAD_REQUEST.code());

                return Triplet.of(problemDetails, details, seppData);

            }

        }
        if (secNegotiateReqData.getTargetPlmnId() != null
            && !(preprocessPlmnIdLists(secNegotiateRspData.getPlmnIdList())).containsAll(preprocessPlmnIdLists(List.of(secNegotiateReqData.getTargetPlmnId()))))
        {
            var detail = "Received target plmnId does not match the configured plmnIds. Received target plmnId: "
                         + secNegotiateReqData.getTargetPlmnId().toString() + ", Configured plmnId list of the RP: "
                         + secNegotiateRspData.getPlmnIdList().toString();

            details = detail.replace("class PlmnId ", "").replace("\n    ", " ").replace("\n", "");

            problemDetails.title(BAD_REQUEST)
                          .detail("Target plmnId is not configured")
                          .cause("UNSPECIFIED_MSG_FAILURE")
                          .status(HttpResponseStatus.BAD_REQUEST.code());

            return Triplet.of(problemDetails, details, seppData);
        }
        log.debug("Check for protocol logical errors finished without findings");
        return Triplet.of(problemDetails, details, seppData);
    }

    /**
     * 
     * Create a method which takes as input the sender and search if it exists in
     * the configuration and return the sepp and roaming partner name.
     * 
     * @param sender
     *
     * @return List<String>
     */
    List<String> searchForTheSepp(String sender)
    {
        final EricssonSeppSeppFunction curr = this.config.get().getEricssonSeppSeppFunction();
        List<String> seppData = new ArrayList<>();
        curr.getNfInstance().get(0).getExternalNetwork().stream().takeWhile(value -> seppData.isEmpty()).forEach(nw ->

        ConfigUtils.getRoamingPartnersWithN32C(nw).stream().takeWhile(value -> seppData.isEmpty()).forEach(rp ->
        {
            var nfPool = Utils.getByName(curr.getNfInstance().get(0).getNfPool(), rp.getN32C().getNfPoolRef());

            if (nfPool.getStaticSeppInstanceDataRef() != null && !nfPool.getStaticSeppInstanceDataRef().isEmpty())
            {
                var staticSeppInstanceDatum = Utils.getByName(curr.getNfInstance().get(0).getStaticSeppInstanceData(),
                                                              nfPool.getStaticSeppInstanceDataRef().get(0));

                staticSeppInstanceDatum.getStaticSeppInstance().stream().takeWhile(value -> seppData.isEmpty()).forEach(seppInstance ->
                {
                    if (sender.equals(seppInstance.getAddress().getFqdn()))
                    {
                        seppData.add(seppInstance.getName());
                        seppData.add(rp.getName());
                    }
                });
            }
            else
            {
                var staticNfInstanceDatum = Utils.getByName(curr.getNfInstance().get(0).getStaticNfInstanceData(),
                                                            nfPool.getNfPoolDiscovery().get(0).getStaticNfInstanceDataRef().get(0));
                staticNfInstanceDatum.getStaticNfInstance().stream().takeWhile(value -> seppData.isEmpty()).forEach(nfInstance ->
                {
                    if (sender.equals(nfInstance.getStaticNfService().toArray(new StaticNfService[0])[0].getAddress().getFqdn()))
                    {
                        seppData.add(nfInstance.getName());
                        seppData.add(rp.getName());
                    }
                });
            }
        }));
        return seppData;
    }

    /**
     * 
     * Create a method which takes as input the Sepp Instance name and returns the
     * fqdn and port that are associated with the name.
     * 
     * @param seppName
     *
     * @return Triplet<String, Integer, String>
     */
    Triplet<String, Integer, String> getDataBySeppName(String seppName)
    {
        final NfInstance nfInstance = this.config.get().getEricssonSeppSeppFunction().getNfInstance().get(0);
        final var roamingPartners = ConfigUtils.getAllRoamingPartnersWithN32C(nfInstance.getExternalNetwork());

        Optional<Triplet<String, Integer, String>> result = Optional.empty();

        for (var rp : roamingPartners)
        {
            var nfPool = Utils.getByName(nfInstance.getNfPool(), rp.getN32C().getNfPoolRef());
            var staticNfInstanceProperties = ConfigHelper.getAllNfServices(nfPool, nfInstance);
            var staticSeppInstanceProperties = ConfigHelper.getStaticProxyServices(nfPool, nfInstance);
            if (staticNfInstanceProperties != null && !staticNfInstanceProperties.isEmpty() && !result.isPresent())
            {
                result = staticNfInstanceProperties.stream().filter(staticNf -> staticNf.getNfInstanceName().equals(seppName)).findFirst().map(staticNf ->
                {
                    String nfInsDataName = nfPool.getNfPoolDiscovery().get(0).getStaticNfInstanceDataRef().get(0) != null ? nfPool.getNfPoolDiscovery()
                                                                                                                                  .get(0)
                                                                                                                                  .getStaticNfInstanceDataRef()
                                                                                                                                  .get(0)
                                                                                                                          : "";
                    String fqdn = staticNf.getAddress().getFqdn();
                    int port = staticNf.getAddress().getMultipleIpEndpoint().isEmpty() ? 443 : staticNf.getAddress().getMultipleIpEndpoint().get(0).getPort();

                    return Triplet.of(fqdn, port, nfInsDataName);
                });
            }

            if (staticSeppInstanceProperties != null && !staticSeppInstanceProperties.isEmpty() && !result.isPresent())
            {
                result = staticSeppInstanceProperties.stream().filter(staticSepp -> staticSepp.getName().equals(seppName)).findFirst().map(staticSepp ->
                {
                    String seppInsDataName = nfPool.getStaticSeppInstanceDataRef().get(0) != null ? nfPool.getStaticSeppInstanceDataRef().get(0) : "";
                    String fqdn = staticSepp.getAddress().getFqdn();
                    int port = staticSepp.getAddress().getMultipleIpEndpoint().isEmpty() ? 443
                                                                                         : staticSepp.getAddress().getMultipleIpEndpoint().get(0).getPort();
                    return Triplet.of(fqdn, port, seppInsDataName);
                });
            }
        }
        return result.orElse(null);
    }

    private static class RoamingData
    {
        private String fqdn;
        private String port;
        private String roamingPartner;
        private String remoteSeppName;
        private String poolName;
        private List<PlmnId> allowedPlmnIdList;
        private SecNegotiateReqData secNegotiateReqData;
        private final UUID roamingDataId;

        RoamingData(UUID rdId)
        {
            this.roamingDataId = rdId;
        }

        private RoamingData withFqdn(String fqdn)
        {
            this.fqdn = fqdn.toLowerCase(); // converting fqdn to lowercase for insensitivity
            return this;
        }

        private RoamingData withPort(String port)
        {
            this.port = port;
            return this;
        }

        private RoamingData withRoamingPartner(String roamingPartner)
        {
            this.roamingPartner = roamingPartner;
            return this;

        }

        private RoamingData withRemoteSepp(String remoteSeppName)
        {
            this.remoteSeppName = remoteSeppName;
            return this;
        }

        private RoamingData withAllowedPlmnIds(List<PlmnId> allowedPlmnIdList)
        {
            this.allowedPlmnIdList = allowedPlmnIdList;
            return this;

        }

        private RoamingData withSecNegotiateReqData(SecNegotiateReqData secNegotiateReqData)
        {
            this.secNegotiateReqData = secNegotiateReqData;
            return this;
        }

        private RoamingData withPoolName(String poolName)
        {
            this.poolName = poolName;
            return this;
        }

        public String getRpN32ClusterName()
        {
            return new StringBuilder().append(poolName).append("#!_#N32C_#!_#").append(roamingPartner).toString();
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append("Fqdn/IP:port: ")
                                      .append(fqdn + ":" + port)
                                      .append("\n")
                                      .append("Roaming Partner Name: ")
                                      .append(roamingPartner)
                                      .append("\n")
                                      .append("Remote Sepp Name: ")
                                      .append(remoteSeppName)
                                      .append("\n")
                                      .append("Target Cluster: ")
                                      .append(getRpN32ClusterName())
                                      .append("\n")
                                      .append("SecNegotiateReqData: ")
                                      .append(secNegotiateReqData)
                                      .append("\n")
                                      .append("unique-id ")
                                      .append(roamingDataId)
                                      .toString();
        }
    }

    /**
     * Create a list of RoamingData objects which contain the appropriate data to
     * initiate a n32c request per remote sepp of a Roaming Partner
     * 
     * @param EricssonSeppSeppFunction
     * @param roamingDataId            an identifier used to distinguish between
     *                                 calls of
     *                                 {@link #sendN32cRequest(EricssonSeppSeppFunction, boolean)}
     * @return A list of RoamingData per remote sepp of a Roaming Partner
     */
    private List<RoamingData> collectRoamingData(final EricssonSeppSeppFunction curr,
                                                 final UUID roamingDataId)
    {
        List<RoamingData> roamingDataList = new ArrayList<>();
        try
        {
            if (curr == null || curr.getNfInstance() == null || curr.getNfInstance().isEmpty())
            {
                return Collections.emptyList();
            }

            // collect data of roaming partners which support the N32c
            curr.getNfInstance().get(0).getExternalNetwork().stream().forEach(nw -> ConfigUtils.getRoamingPartnersWithN32C(nw).forEach(rp ->
            {

                final List<PlmnId> allowPlmnIdList = ConfigUtils.getAllowPlmnIdListFromRoamingPartner(curr.getNfInstance().get(0).getExternalNetwork(),
                                                                                                      rp.getName());
                // For each rp create a new object of SecNegotiateReqData
                SecNegotiateReqData secNegotiateReqData = new SecNegotiateReqData();

                String rpPrimaryIdMcc = rp.getN32C().getAllowPlmn().getPrimaryIdMcc();
                String rpPrimaryIdMnc = rp.getN32C().getAllowPlmn().getPrimaryIdMnc();

                PlmnId targetPlmnId = new PlmnId().mcc(rpPrimaryIdMcc).mnc(rpPrimaryIdMnc);

                // collect data of own network
                var secNegotiateData = prepareSecNegotiateData();

                secNegotiateReqData.setSender(secNegotiateData.getSender());
                secNegotiateReqData.setSupportedSecCapabilityList(secNegotiateData.getSelectedSecCapability());
                secNegotiateReqData.set3gppSbiTargetApiRootSupported(secNegotiateData.getSbiTargetApiRootSupported());
                secNegotiateReqData.setPlmnIdList(secNegotiateData.getPlmnIdList());
                secNegotiateReqData.setTargetPlmnId(targetPlmnId);
                // End of object SecNegotiateReqData

                var nfPool = Utils.getByName(curr.getNfInstance().get(0).getNfPool(), rp.getN32C().getNfPoolRef());
                log.debug("Collect data nfPool: {}", nfPool.getName());

                // Collect all the address properties from the static-sepp-instance-data or
                // static-nf-instance-data which referenced by the nf-pool which referenced by a
                // RP with n32-c enabled.
                //
                // A nf-pool which referenced by A RP with n32-c can reference either
                // static-nf-instance-data (deprecated for nfType SEPP) or
                // static-sepp-instance-data.
                var proxySeppServicesForN32c = (nfPool.getStaticSeppInstanceDataRef() != null
                                                && !nfPool.getStaticSeppInstanceDataRef().isEmpty())
                                                                                                     ? ConfigHelper.getStaticProxyServices(nfPool,
                                                                                                                                           curr.getNfInstance()
                                                                                                                                               .get(0))
                                                                                                     : ConfigHelper.getAllNfServices(nfPool,
                                                                                                                                     curr.getNfInstance()
                                                                                                                                         .get(0));
                // Creates a list of host metadata per SEPP related to n32-c functionality.
                proxySeppServicesForN32c.stream().filter(Objects::nonNull).forEach(svc ->
                {
                    var roamingDataPerRemoteSepp = new RoamingData(roamingDataId).withRoamingPartner(rp.getName())
                                                                                 .withAllowedPlmnIds(allowPlmnIdList)
                                                                                 .withSecNegotiateReqData(secNegotiateReqData)
                                                                                 .withPoolName(nfPool.getName())
                                                                                 .withRemoteSepp(svc.getNfInstanceName())
                                                                                 .withFqdn(svc.getAddress().getFqdn())
                                                                                 .withPort(rpPrimaryIdMnc);

                    var defaultPort = svc.getAddress().getScheme() == Scheme.HTTPS ? "443" : "80";
                    var multEnd = svc.getAddress().getMultipleIpEndpoint();
                    if (multEnd != null && !multEnd.isEmpty())
                    {
                        // it is assumed that only one multipleIpEndpoint is defined -> at most one port
                        // definition
                        // If this isn't the case a roaming datum needs to be made for each port
                        var multIpEp = multEnd.get(0);
                        var port = multIpEp.getPort() == null ? defaultPort : multIpEp.getPort().toString();
                        roamingDataPerRemoteSepp.withPort(port);
                    }
                    else
                    {
                        roamingDataPerRemoteSepp.withPort(defaultPort);

                    }
                    roamingDataList.add(roamingDataPerRemoteSepp);
                });
            }));
        }
        catch (Exception e)
        {
            log.error("Caught Exception :{}.", e.toString());
        }
        return roamingDataList;
    }

    /**
     * Prepare the URL to be according ipv4/ipv6
     * 
     * @param scheme
     * @param host
     * @param port
     * @param path
     *
     * @return host:port or [ipv6]:port
     */
    private static String buildUrl(final String scheme,
                                   String host,
                                   final int port,
                                   final String path)
    {
        var sb = new StringBuilder(scheme);
        host = host.strip();
        if (!host.startsWith("[") && host.contains(":"))
        {
            sb.append("[").append(host).append("]");
        }
        else
        {
            sb.append(host);
        }
        return sb.append(":").append(port).append(path).toString();
    }

    /**
     * Create a method which takes as input the RoamingData and send a POST request
     * with the appropriate data.
     * 
     * @param roamingData
     *
     * @return
     */
    private Completable postExchangeCapabilityRequest(final RoamingData roamingData)
    {

        log.debug("Start send N32-c POST exchange capability request for:\n{}", roamingData);

        var scheme = N32C_INIT_TLS_ENABLED.booleanValue() ? "https://" : "http://";
        return Completable.defer(() ->

        // check the map if someone erased (new config arrived and handshake succeeded
        // for this sepp)
        Optional.ofNullable(retriableSepps.get(roamingData.fqdn))
                .filter(id -> id.equals(roamingData.roamingDataId))
                .isPresent() ? Completable.complete()
                             : Completable.error(new N32cFaultySeppException(roamingData.fqdn,
                                                                             roamingData.roamingDataId,
                                                                             roamingData.fqdn + " removed or altered")))
                          .andThen(this.n32cClient.getWebClient()
                                                  .flatMapCompletable(wc -> wc.requestAbs(HttpMethod.POST,
                                                                                          SocketAddress.inetSocketAddress(PORT_REST_N32C,
                                                                                                                          SERVICE_ERIC_SEPP_WORKER_N32C),
                                                                                          buildUrl(scheme,
                                                                                                   SERVICE_ERIC_SEPP_WORKER,
                                                                                                   PORT_REST_N32C,
                                                                                                   EXCHANGE_CAPABILITY_URI_PATH.getPath()))
                                                                              .putHeader(CONTENT_TYPE, APPLICATION_JSON)
                                                                              .putHeader(X_HOST_HEADER, roamingData.fqdn + ":" + roamingData.port)
                                                                              .putHeader(X_CLUSTER_HEADER, roamingData.getRpN32ClusterName())
                                                                              .timeout(REQUEST_TIMEOUT_MILLIS)
                                                                              .rxSendJsonObject(new JsonObject(json.writeValueAsString(roamingData.secNegotiateReqData)))
                                                                              .doOnSubscribe(d -> N32cCounters.stepCcInitiatingN32cOutReq(roamingData.remoteSeppName,
                                                                                                                                          roamingData.roamingPartner,
                                                                                                                                          HttpMethod.POST,
                                                                                                                                          EXCHANGE_CAPABILITY_URI_PATH.toString()))
                                                                              .doOnError(throwable -> N32cCounters.stepCcInitiatingN32cFailures(roamingData.remoteSeppName,
                                                                                                                                                roamingData.roamingPartner,
                                                                                                                                                throwable.toString()))
                                                                              .flatMapCompletable(resp -> resp.statusCode() == HttpResponseStatus.OK.code() ? handleTheResponse(resp,
                                                                                                                                                                                roamingData.roamingPartner,
                                                                                                                                                                                roamingData.remoteSeppName,
                                                                                                                                                                                roamingData.secNegotiateReqData,
                                                                                                                                                                                roamingData.allowedPlmnIdList)
                                                                                                                                                            : handleTheUnSucResponse(resp,
                                                                                                                                                                                     roamingData.roamingPartner,
                                                                                                                                                                                     roamingData.remoteSeppName))))
                          .doOnError(e -> log.warn("Failed to send n32c request handshake to remote sepp: {} with reason:{}",
                                                   roamingData.remoteSeppName,
                                                   e.getMessage()))
                          // transform underlying error to a custom exception containing information for
                          // the faulty sepp as well as an id signifying the "configuration update batch"
                          .onErrorResumeNext(e -> Completable.error(new N32cFaultySeppException(roamingData.fqdn, roamingData.roamingDataId, e.getMessage())))
                          .retryWhen(sendRetryPolicy.create())
                          .onErrorComplete(); // retries run forever, however when new configuration is loaded, the predicate
                                              // of the retry function
                                              // is not satisfied anymore meaning an error will be propagated which we want to
                                              // ignore

    }

    /**
     * Sends the n32-c request towards roaming partners.
     * 
     * @param config
     * @param reinit: If true, then n32-c reinitiation process applies. The request
     *                is send towards all sepps regardless their operational state.
     *                Otherwise, when false, the normal case applies and the request
     *                is sent only towards the non-active.
     * 
     * @return
     */
    public Completable sendN32cRequest(final EricssonSeppSeppFunction config,
                                       boolean reinit)
    {
        // A unique id is used to distinguish between potential retry batches, i.e.
        // each time sendN32cRequest is called
        final var rdId = UUID.randomUUID();
        var roamingDataList = collectRoamingData(config, rdId);
        List<Completable> requests = new ArrayList<>();
        log.info("Roaming Data collected");
        // past mappings are cleared, so that threads performing stale retries terminate
        retriableSepps.clear();

        roamingDataList.forEach(rd -> requests.add(readSecurityNegotiationDatumOpStateValue(rd.roamingPartner,
                                                                                            rd.remoteSeppName).retryWhen(dbReadRetryPolicy.create())
                                                                                                              .doOnError(e -> log.warn("Failed to read the etcd for seppName: {}, exception {}",
                                                                                                                                       rd.remoteSeppName,
                                                                                                                                       e))
                                                                                                              .flatMapCompletable(item ->
                                                                                                              {
                                                                                                                  log.debug("Reinitiation phase {} with operational state {}",
                                                                                                                            reinit,
                                                                                                                            item);
                                                                                                                  if (reinit
                                                                                                                      || !Objects.equals(item,
                                                                                                                                         OPERATIONAL_STATUS_ACTIVE))
                                                                                                                  {
                                                                                                                      // each roaming datum corresponds to one
                                                                                                                      // SEPP and subsequently
                                                                                                                      // makes an entry to the (potentially)
                                                                                                                      // retriableSepps map
                                                                                                                      retriableSepps.put(rd.fqdn,
                                                                                                                                         rd.roamingDataId);
                                                                                                                      return postExchangeCapabilityRequest(rd);
                                                                                                                  }
                                                                                                                  else
                                                                                                                  {
                                                                                                                      return Completable.complete();
                                                                                                                  }
                                                                                                              })));

        return Flowable.fromIterable(requests).doOnNext(r -> r.subscribeOn(Schedulers.io()).subscribe()).ignoreElements();
    }

    /**
     * Create a method which will handle the response of the http request with
     * different protocol and logic check.
     * 
     * @param response
     * @param allowPlmnIdList
     * @param SecNegotiateRspData
     * @param SecNegotiateReqData
     *
     * @return String
     */
    private String handleTheResponseProtocolAndLogicCheck(HttpResponse<Buffer> response,
                                                          SecNegotiateRspData secNegotiateRspData,
                                                          SecNegotiateReqData secNegotiateReqData,
                                                          List<PlmnId> allowPlmnIdList)
    {
        log.debug("Check for protocol logical errors start");
        // protocol check
        // Content Length header not present
        if (response.getHeader(CONTENT_LENGTH) == null)
        {
            return "Incorrect Length";
        }
        // Content type is not json
        if (!response.getHeader(CONTENT_TYPE).toLowerCase().contains("json") && !response.getHeader(CONTENT_TYPE).isEmpty())
        {
            return "Unsupported Media Type";
        }
        // all mandatory elements present (sender,selectedSecCapability)

        // This sonar warning remains, because we might have a null scenario
        if (secNegotiateRspData.getSender() == null || secNegotiateRspData.getSender().isEmpty())
        {
            return "Mandatory IE Missing: Sender";

        }
        // This sonar warning remains, because we might have a null scenario
        if (secNegotiateRspData.getSelectedSecCapability() == null || secNegotiateRspData.getSelectedSecCapability().isEmpty())
        {
            return "Mandatory IE Missing: SelectedSecCapability";
        }
        // check if TLS is selected
        if (!"TLS".equals(secNegotiateRspData.getSelectedSecCapability()))
        {
            return "Mismatch in the value of selectedSecCapability, expected TLS, received: " + secNegotiateRspData.getSelectedSecCapability();

        }

        // 3GppSbiTargetApiRootSupported. if as initiating SEPP, FALSE is sent and the
        // Responding SEPP responds with TRUE then the initiating SEPP shall mark the
        // operational-state as “faulty” with logging of the reason as well.
        if (Boolean.FALSE.equals(secNegotiateReqData.get3gppSbiTargetApiRootSupported())
            && Boolean.TRUE.equals(secNegotiateRspData.get3gppSbiTargetApiRootSupported()))
        {
            return "3GppSbiTargetApiRootSupported expected FALSE but received TRUE";

        }
        // If PLMN ids are received, check them against the allow-plmn-list. If it
        // is a subset or identical with that list, then it is ok, otherwise faulty
        if (secNegotiateRspData.getPlmnIdList() != null && !secNegotiateRspData.getPlmnIdList().isEmpty()
            && !preprocessPlmnIdLists(allowPlmnIdList).containsAll(preprocessPlmnIdLists(secNegotiateRspData.getPlmnIdList())))
        {
            var detail = "Received plmnIdList does not match (totally or partially) the configured allow-plmn list of the RP, Received plmnIdList: "
                         + secNegotiateRspData.getPlmnIdList() + ", Configured allow-plmn list of the RP: " + allowPlmnIdList;
            return detail.replace("class PlmnId ", "").replace("\n    ", " ").replace("\n", "");
        }
        log.debug("Check for protocol logical errors finished without findings");
        return "";
    }

    /**
     * Create a method which will handle the response of the http request with 200
     * OK response code and the protocol check.
     * 
     * @param response
     * @param sepp
     * @param secNegotiateReqData
     * @param allowPlmnIdList
     *
     * @return status code
     * @throws JsonProcessingException
     */
    private Completable handleTheResponse(HttpResponse<Buffer> response,
                                          String rpName,
                                          String seppName,
                                          SecNegotiateReqData secNegotiateReqData,
                                          List<PlmnId> allowPlmnIdList) throws JsonProcessingException
    {
        String httpResponse = "ExchangeCapability: " + EXCHANGE_CAPABILITY_URI_PATH + ", statusCode: " + response.statusCode() + ", statusMessage: "
                              + response.statusMessage() + ", body: " + response.bodyAsString() + ", headers: " + response.headers();
        log.debug(httpResponse);

        // protocol check
        final String responseBody = response.bodyAsString();
        // get the secNegotiateRspData when from the successful response.
        final SecNegotiateRspData secNegotiateRspData = json.readValue(responseBody, SecNegotiateRspData.class);

        var protocolAndLogicError = handleTheResponseProtocolAndLogicCheck(response, secNegotiateRspData, secNegotiateReqData, allowPlmnIdList);
        if (!protocolAndLogicError.isEmpty())
        {
            log.warn(protocolAndLogicError);
            N32cCounters.stepCcInitiatingN32cProtocolFailures(seppName, rpName, protocolAndLogicError);
            return writeSecurityNegotiationDatumForFaulty(new SecurityNegotiationItemBuilder().newItem()
                                                                                              .withSeppName(seppName)
                                                                                              .withRoamingPartnerRef(rpName)
                                                                                              .withReason(protocolAndLogicError)).andThen(Completable.error(new Exception("Exchange Capability operation failed with reason: Protocol and Logic Error")));
        }
        var secCap = response.bodyAsJsonObject().getValue("selectedSecCapability").toString();
        var tar = response.bodyAsJsonObject().getValue("3GppSbiTargetApiRootSupported").toString();
        var plmnIds = response.bodyAsJsonObject().getJsonArray("plmnIdList");

        List<ReceivedPlmnId> receivedPlmnIds = new ArrayList<>();
        if (plmnIds != null && !plmnIds.isEmpty())
        {
            for (var i = 0; i < plmnIds.size(); i++)
            {
                ReceivedPlmnId tmp = new ReceivedPlmnId();
                tmp.setMcc(plmnIds.getJsonObject(i).getValue("mcc").toString());
                tmp.setMnc(plmnIds.getJsonObject(i).getValue("mnc").toString());
                receivedPlmnIds.add(tmp);
            }
        }

        // if all the protocol check and logic check pass, then update operational-state
        // to active and return 200.
        N32cCounters.stepCcInitiatingN32cInSuccResp(seppName, rpName, HttpMethod.POST, EXCHANGE_CAPABILITY_URI_PATH.toString(), response.statusCode());
        return writeSecurityNegotiationDatum(new SecurityNegotiationItemBuilder().newItem()
                                                                                 .withSeppName(seppName)
                                                                                 .withRoamingPartnerRef(rpName)
                                                                                 .withOperationalState(OPERATIONAL_STATUS_ACTIVE)
                                                                                 .withSecCap(secCap)
                                                                                 .withTar(tar)
                                                                                 .withPlmnIds(receivedPlmnIds));

    }

    /**
     * Create a method which will handle the response of the http request with
     * different response code than 200 OK.
     * 
     * @param response
     * @param sepp
     * 
     * @return status code
     * @throws JsonProcessingException
     */
    private Completable handleTheUnSucResponse(HttpResponse<Buffer> response,
                                               String rpName,
                                               String seppName) throws JsonProcessingException
    {

        if (response != null)
        {
            String unsucessfulStatusCodeError;
            String cause = "";
            String detail = "";
            // try to return the error status and error detail/cause, if there is no error
            // detail/cause, just return the status code and status message.
            try
            {
                if (response.bodyAsJsonObject().containsKey("detail"))
                {
                    detail = response.bodyAsJsonObject().getValue("detail").toString();
                }
                if (response.bodyAsJsonObject().containsKey("cause"))
                {
                    cause = response.bodyAsJsonObject().getValue("cause").toString();
                }

                unsucessfulStatusCodeError = "status code: " + response.statusCode() + ", title: " + response.statusMessage() + ", detail: " + detail
                                             + ", cause: " + cause;
            }
            catch (Exception e)
            {
                unsucessfulStatusCodeError = "status code: " + response.statusCode() + ", title: " + response.statusMessage();
            }
            log.warn(unsucessfulStatusCodeError);
            N32cCounters.stepCcInitiatingN32cFailures(seppName, rpName, unsucessfulStatusCodeError);
            return writeSecurityNegotiationDatumForFaulty(new SecurityNegotiationItemBuilder().newItem()
                                                                                              .withSeppName(seppName)
                                                                                              .withRoamingPartnerRef(rpName)
                                                                                              .withReason(unsucessfulStatusCodeError)).andThen(Completable.error(new Exception("Exchange Capability operation failed with reason: " + unsucessfulStatusCodeError)));

        }
        else
        {
            String error = "Response Is Empty";
            log.warn(error);
            N32cCounters.stepCcInitiatingN32cFailures(seppName, rpName, error);
            return writeSecurityNegotiationDatumForFaulty(new SecurityNegotiationItemBuilder().newItem()
                                                                                              .withSeppName(seppName)
                                                                                              .withRoamingPartnerRef(rpName)
                                                                                              .withReason(error)).andThen(Completable.error(new Exception("Exchange Capability operation failed with reason: " + error)));
        }
    }

    private synchronized Single<List<Pair<String, String>>> execute(ETCD_OPERATION operation,
                                                                    ByteSequence key,
                                                                    ByteSequence value,
                                                                    GetOption getOption,
                                                                    DeleteOption deleteOption)
    {
        if (operation == ETCD_OPERATION.GET)
        {
            return getOption != null ? this.rxEtcd.get(key, getOption)
                                                  .map(response -> response.getKvs()
                                                                           .stream()
                                                                           .map(item -> Pair.of(item.getKey().toString(), item.getValue().toString()))
                                                                           .collect(Collectors.toList()))
                                     : this.rxEtcd.get(key)
                                                  .map(response -> response.getKvs()
                                                                           .stream()
                                                                           .map(item -> Pair.of(item.getKey().toString(), item.getValue().toString()))
                                                                           .collect(Collectors.toList()));

        }
        else if (operation == ETCD_OPERATION.DELETE)

        {
            return deleteOption != null ? this.rxEtcd.delete(key, deleteOption).map(item -> List.of(Pair.of("", "")))
                                        : this.rxEtcd.delete(key).map(item -> List.of(Pair.of("", "")));
        }
        else
        {
            return this.rxEtcd.put(key, value).map(item -> List.of(Pair.of("", "")));
        }
    }

    public static class SecurityNegotiationItemBuilder
    {

        private String seppName;
        private String roamingPartnerRef;
        private JsonObject item;

        public String getSeppName()
        {
            return this.seppName;
        }

        public String getRoamingPartnerRef()
        {
            return this.roamingPartnerRef;
        }

        public SecurityNegotiationItemBuilder withSeppName(String seppName)
        {
            this.seppName = seppName;
            this.item.put(KEY_SEPP_NAME, seppName);
            return this;
        }

        public SecurityNegotiationItemBuilder withRoamingPartnerRef(String roamingPartnerRef)
        {
            this.roamingPartnerRef = roamingPartnerRef;
            this.item.put(KEY_ROAMING_PARTNER_REF, roamingPartnerRef);
            return this;
        }

        public SecurityNegotiationItemBuilder withOperationalState(String operationalState)
        {
            JsonObject operState = new JsonObject().put(KEY_VALUE, operationalState);
            this.item.put(KEY_OPERATIONAL_STATE, operState);
            return this;
        }

        public SecurityNegotiationItemBuilder withSecCap(String secCap)
        {
            this.item.put(KEY_SECURITY_CAPABILITY, secCap);
            return this;
        }

        public SecurityNegotiationItemBuilder withTar(String tar)
        {
            this.item.put(KEY_SUPPORTS_TARGET_APIROOT, tar);
            return this;
        }

        public SecurityNegotiationItemBuilder withPlmnIds(List<ReceivedPlmnId> plmnIds)
        {
            List<JsonObject> receivedPlmnIds = new ArrayList<>();
            plmnIds.stream().filter(Objects::nonNull).forEach(plmn ->
            {
                JsonObject tmp = new JsonObject();
                tmp.put(KEY_MCC, plmn.getMcc());
                tmp.put(KEY_MNC, plmn.getMnc());
                receivedPlmnIds.add(tmp);
            });

            this.item.put(KEY_RECEIVED_PLMN_ID, receivedPlmnIds);
            return this;
        }

        public SecurityNegotiationItemBuilder withReason(String reason)
        {
            JsonObject operState = new JsonObject().put(KEY_VALUE, OPERATIONAL_STATUS_FAULTY);
            operState.put(KEY_REASON, reason);
            this.item.put(KEY_OPERATIONAL_STATE, operState);
            return this;
        }

        public SecurityNegotiationItemBuilder withNumberOfFailures(Integer numberOfFailures)
        {
            this.item.put(KEY_NUMBER_OF_FAILURES, numberOfFailures);
            return this;
        }

        public SecurityNegotiationItemBuilder updateTimestamp()
        {
            var timeStampEpoch = new Date(System.currentTimeMillis());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
            String timeStamp = sdf.format(timeStampEpoch);

            this.item.put(KEY_LAST_UPDATE, timeStamp);
            return this;
        }

        public SecurityNegotiationItemBuilder newItem()
        {
            this.item = new JsonObject().put(KEY_NUMBER_OF_FAILURES, 0);
            this.updateTimestamp();
            return this;
        }

        public JsonObject buildAsJson()
        {
            return this.item;
        }
    }

    /**
     * Write the security datum in etcd db with the bare mininum data key:
     * /ericssson/sc/sepp/n32c/<rp_name>/<sepp_name>
     * 
     * 
     */
    public Completable writeSecurityNegotiationDatum(SecurityNegotiationItemBuilder builder)
    {
        var item = builder.buildAsJson();

        String keyStr = PREFIX + "/" + builder.getRoamingPartnerRef() + "/" + builder.getSeppName();

        final var key = ByteSequence.from(keyStr.getBytes());
        final var value = ByteSequence.from(item.toString().getBytes());

        log.debug("Writing the item to the db with key: {}", keyStr);

        return this.execute(ETCD_OPERATION.PUT, key, value, null, null).ignoreElement();
    }

    /**
     * Write the security datum in etcd db for faulty state.
     * 
     */

    public Completable writeSecurityNegotiationDatumForFaulty(SecurityNegotiationItemBuilder builder)
    {
        return readSecurityNegotiationDataNumOfFailures(builder.getRoamingPartnerRef(), builder.getSeppName()).flatMapCompletable(entry ->
        {
            return writeSecurityNegotiationDatum(builder.withNumberOfFailures(entry + 1).updateTimestamp());
        }

        );
    }

    /**
     * Write the security negotiation data to etcd at initial config for n32c.
     * 
     * @param EricssonSeppSeppFunction configuration
     * 
     * @return
     */
    public Completable writeSecurityNegotiationDataAtInitialConfig(final EricssonSeppSeppFunction config)
    {
        return Completable.fromAction(() ->
        {
            List<Completable> actions = new ArrayList<>();

            // Loop into the n32-c enabled RPs, get the sepps connected to them and create
            // the entries in the database with operational state inactive
            Map<String, String> sepp2rp = readN32CConfig(config);
            sepp2rp.forEach((sepp,
                             rp) ->
            {
                log.debug("Initial N32-C configuration for {} {}", sepp, rp);
                actions.add(writeSecurityNegotiationDatum(new SecurityNegotiationItemBuilder().newItem()
                                                                                              .withSeppName(sepp)
                                                                                              .withRoamingPartnerRef(rp)
                                                                                              .withOperationalState(OPERATIONAL_STATUS_INACTIVE)));
            });

            Flowable.fromIterable(actions)
                    .doOnNext(t -> t.subscribeOn(Schedulers.io()).subscribe())
                    .doOnError(e -> log.error("Error while writing init data"))
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        });
    }

    /**
     * Update the etcd database by deleted the removed or n32-c disabled roaming
     * partners and sepps according to the configuration
     * 
     * @param EricssonSeppSeppFunction configuration
     * @param List<JsonObject>         entries
     * 
     * @return
     */
    public Completable deleteRemovedSecNegDataAfterConfigUpdate(final EricssonSeppSeppFunction config,
                                                                List<JsonObject> entries)
    {
        return Completable.fromAction(() ->
        {
            List<Completable> actions = new ArrayList<>();

            // Fetch the N32-C enabled rps and sepps
            Map<String, String> sepp2rp = readN32CConfig(config);

            // Loop into the db entries and compare with configuration
            entries.stream().forEach(entry ->
            {
                var rp = entry.getString(KEY_ROAMING_PARTNER_REF);
                var sepp = entry.getString(KEY_SEPP_NAME);

                var value = sepp2rp.get(sepp);
                // If the sepp or rp does not exist anymore in the configuration
                // delete the respective entry
                //
                if (value == null)
                    actions.add(deleteSecurityNegotiationDatum(rp, sepp));

                // if the sepp is not connected to the same rp as before, delete the entry
                else if (value.equals(rp))
                    actions.add(Completable.complete());

                else
                    actions.add(deleteSecurityNegotiationDatum(rp, sepp));
            });

            Flowable.fromIterable(actions)
                    .doOnNext(t -> t.subscribeOn(Schedulers.io()).subscribe())
                    .doOnError(e -> log.error("Error while deleting etcd data"))
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        });
    }

    /**
     * Update the etcd database for the newly n32-c enabled roaming partners and
     * newly introduced sepps All new sepps will be initiated to inactive
     * 
     * @param EricssonSeppSeppFunction configuration
     * 
     * @return
     */
    public Completable updateSecurityNegotiationDataAfterConfigUpdate(final EricssonSeppSeppFunction config)
    {
        return Completable.fromAction(() ->
        {
            List<Completable> actions = new ArrayList<>();

            // For each RP fetch its sepps
            // Check in etcd db in an entry already exists. If notCreate an entry in the DB
            // with operational state inactive
            Map<String, String> sepp2rp = readN32CConfig(config);
            sepp2rp.forEach((sepp,
                             rp) -> actions.add(readSecurityNegotiationDatum(rp, sepp).flatMapCompletable(entry ->
                             {
                                 if (entry.isEmpty())
                                     return writeSecurityNegotiationDatum(new SecurityNegotiationItemBuilder().newItem()
                                                                                                              .withSeppName(sepp)
                                                                                                              .withRoamingPartnerRef(rp)
                                                                                                              .withOperationalState(OPERATIONAL_STATUS_INACTIVE));

                                 return Completable.complete();
                             })));

            Flowable.fromIterable(actions)
                    .doOnNext(t -> t.subscribeOn(Schedulers.io()).subscribe())
                    .doOnError(e -> log.error("Error while updating etcd data"))
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        });
    }

    /**
     * Read the security negotiation data for a specific key
     *
     * @param roamingPartnerRef
     * @param staticNfInstanceRef
     *
     * @return Single<JsonObject>
     *
     */
    public Single<JsonObject> readSecurityNegotiationDatum(String roamingPartnerRef,
                                                           String staticNfInstanceRef)
    {
        String key = PREFIX + "/" + roamingPartnerRef + "/" + staticNfInstanceRef;
        final var k = ByteSequence.from(key.getBytes());
        log.debug("Key: {}", k);

        return this.execute(ETCD_OPERATION.GET, k, null, null, null).map(item ->
        {
            var jsonTmp = new JsonObject();

            item.stream().forEach(pair ->
            {
                var object = new JsonObject(pair.getValue());
                log.debug("pair: {}  -> {}", pair.getKey(), pair.getValue());
                jsonTmp.put(key, object);
            });

            log.debug("ETCD json {}", jsonTmp);
            return jsonTmp;
        });
    }

    public Single<JsonObject> readDataForStateDataProvider(N32cPathParameters params)
    {
        String key = PREFIX + "/" + params.getRoamingPartner() + "/" + params.getNfInstanceRef();
        final var k = ByteSequence.from(key.getBytes());
        log.debug("Key: {}", k);
        return this.execute(ETCD_OPERATION.GET, k, null, null, null)
                   .map(getResp -> new EtcdEntries<>(0,
                                                     getResp //
                                                            .stream()
                                                            .map(this::securityNegotiationDatumMapper)
                                                            .filter(Objects::nonNull)
                                                            .collect(Collectors.toList())))
                   .map(etcdEntries ->
                   {
                       if (params.getLastValue().equals(KEY_RECEIVED_PLMN_ID))
                           return receivedPlmnIdJsonMapper(etcdEntries);
                       else if (params.getLastValue().equals(KEY_SEPP_NAME))
                           return singleNfInstanceEtcdJsonMapper(etcdEntries);
                       else if (params.getLastValue().equals(KEY_LAST_UPDATE))
                           return singleElementJsonMapper(etcdEntries, params.getLastValue());
                       else if (params.getLastValue().equals(KEY_OPERATIONAL_STATE))
                           return singleElementJsonMapper(etcdEntries, params.getLastValue());
                       else if (params.getLastValue().equals(KEY_SUPPORTS_TARGET_APIROOT))
                           return singleElementJsonMapper(etcdEntries, params.getLastValue());
                       else if (params.getLastValue().equals(KEY_SECURITY_CAPABILITY))
                           return singleElementJsonMapper(etcdEntries, params.getLastValue());
                       else
                           return fromEtcdEntryToJsonObjectMapper(etcdEntries);
                   });

    }

    /**
     * Helper function that prepares a map with the n32-c enabled sepp-rp pairs that
     * exist under the configuration
     * 
     * @param EricssonSeppSeppFunction configuration
     * 
     * @return Map<String, String>
     */
    public Map<String, String> readN32CConfig(final EricssonSeppSeppFunction config)
    {
        Map<String, String> sepp2rp = new HashMap<>();

        // Fetch roaming partners with N32-C enabled
        final var roamingPartners = ConfigUtils.getAllRoamingPartnersWithN32C(config.getNfInstance().get(0).getExternalNetwork());

        // For each RP fetch its sepps
        roamingPartners.stream().forEach(rp ->
        {
            var nfPool = Utils.getByName(config.getNfInstance().get(0).getNfPool(), rp.getN32C().getNfPoolRef());

            nfPool.getNfPoolDiscovery().stream().flatMap(npD -> npD.getStaticNfInstanceDataRef().stream()).filter(Objects::nonNull).forEach(st ->
            {
                var staticNfDatum = Utils.getByName(config.getNfInstance().get(0).getStaticNfInstanceData(), st);
                staticNfDatum.getStaticNfInstance().stream().forEach(sepp -> sepp2rp.put(sepp.getName(), rp.getName()));
            });

            nfPool.getStaticSeppInstanceDataRef().stream().filter(Objects::nonNull).forEach(st ->
            {
                var staticSeppDatum = Utils.getByName(config.getNfInstance().get(0).getStaticSeppInstanceData(), st);
                staticSeppDatum.getStaticSeppInstance().stream().forEach(sepp -> sepp2rp.put(sepp.getName(), rp.getName()));
            });
        });
        return sepp2rp;
    }

    /**
     * Helper function that prepares a map with the n32-c enabled RP(s) and
     * corresponding n32c data that exist under the configuration
     * 
     * @param EricssonSeppSeppFunction configuration
     * 
     * @return Map<String, N32C_1>
     */
    public Map<String, N32C__1> readN32cRPs(final EricssonSeppSeppFunction config)
    {
        Map<String, N32C__1> rp2N32c = new HashMap<>();
        final var roamingPartners = ConfigUtils.getAllRoamingPartnersWithN32C(config.getNfInstance().get(0).getExternalNetwork());
        roamingPartners.stream().forEach(rp -> rp2N32c.put(rp.getName(), rp.getN32C()));

        return rp2N32c;
    }

    /**
     * Helper function that prepares a map with the n32-c enabled sepp-rp pairs that
     * exist under the configuration, including all the data for each SEPP
     * 
     * @param EricssonSeppSeppFunction configuration
     * 
     * @return Map<String, List<IfTypedNfAddressProperties>>
     */
    public Map<String, List<IfTypedNfAddressProperties>> readN32CConfigFull(final EricssonSeppSeppFunction config)
    {
        Map<String, List<IfTypedNfAddressProperties>> sepp2rp = new HashMap<>();

        // Fetch roaming partners with N32-C enabled
        final var roamingPartners = ConfigUtils.getAllRoamingPartnersWithN32C(config.getNfInstance().get(0).getExternalNetwork());

        // For each RP fetch its sepps
        roamingPartners.stream().forEach(rp ->
        {
            List<IfTypedNfAddressProperties> seppList = new ArrayList<>();

            var nfPool = Utils.getByName(config.getNfInstance().get(0).getNfPool(), rp.getN32C().getNfPoolRef());

            var staticNfInstanceProperties = ConfigHelper.getAllNfServices(nfPool, config.getNfInstance().get(0));
            var staticSeppInstanceProperties = ConfigHelper.getStaticProxyServices(nfPool, config.getNfInstance().get(0));

            // Under nf-pool which referenced by a RP which has enabled n32-c
            // only static-sepp-instance-data or static-nf-instance-data can be
            // configured
            if (staticNfInstanceProperties != null && !staticNfInstanceProperties.isEmpty())
                seppList.addAll(staticNfInstanceProperties);
            else
                seppList.addAll(staticSeppInstanceProperties);

            sepp2rp.put(rp.getName(), seppList);
        });
        return sepp2rp;
    }

    /**
     * Read the security negotiation data for a specific roaming partner
     *
     * @param roamingPartnerRef
     *
     * @return Single<List<JsonObject>>
     *
     */
    public Single<List<JsonObject>> readSecurityNegotiationDataForRoamingPartner(String roamingPartnerRef)
    {
        final var prp = PREFIX + "/" + roamingPartnerRef;
        final var k = ByteSequence.from(prp.getBytes());
        log.debug("Prefix with roaming partner is: {}", k);

        final var options = GetOption.newBuilder().isPrefix(true).withLimit(0).build();

        return this.execute(ETCD_OPERATION.GET, k, null, options, null).map(item ->
        {
            List<JsonObject> secDataList = new ArrayList<>();

            item.stream().forEach(pair ->
            {
                var object = new JsonObject(pair.getValue());
                log.debug("pair: {}  -> {}", pair.getKey(), pair.getValue());
                secDataList.add(object);
            });
            log.debug("ETCD secDataList {}", secDataList);
            return secDataList;
        });
    }

    /**
     * Deletes Security Negotiation Datum given the key
     * 
     * @param roamingPartnerRef
     * @param staticNfInstanceRef
     * 
     * @return
     */
    public Completable deleteSecurityNegotiationDatum(String roamingPartnerRef,
                                                      String staticNfInstanceRef)
    {
        String key = PREFIX + "/" + roamingPartnerRef + "/" + staticNfInstanceRef;
        final var k = ByteSequence.from(key.getBytes());
        log.debug("Deleting item with key: {}", key);

        return this.execute(ETCD_OPERATION.DELETE, k, null, null, null).ignoreElement();
    }

    /**
     * Deletes all Security Negotiation Data
     * 
     * @param
     * 
     * @return
     */
    public Completable deleteAllSecurityNegotiationData()
    {
        final var options = DeleteOption.newBuilder().isPrefix(true).build();

        return this.execute(ETCD_OPERATION.DELETE, ByteSequence.from(PREFIX.getBytes()), null, null, options).ignoreElement();
    }

    /**
     * Reads all Security Negotiation Data
     * 
     * @param
     * 
     * @return Single<List<JsonObject>>
     * 
     */
    public Single<List<JsonObject>> readSecurityNegotiationData()
    {
        final var k = ByteSequence.from(PREFIX.getBytes());
        log.debug("Prefix is: {}", k);

        final var options = GetOption.newBuilder().isPrefix(true).withLimit(0).build();

        return this.execute(ETCD_OPERATION.GET, k, null, options, null).map(item ->
        {
            List<JsonObject> secDataList = new ArrayList<>();

            item.stream().forEach(pair ->
            {
                var object = new JsonObject(pair.getValue());
                log.debug("pair:{} -> {}", pair.getKey(), pair.getValue());
                secDataList.add(object);
            });
            log.debug("ETCD secDataList {}", secDataList);
            return secDataList;
        });
    }

    /**
     * 
     * @return
     */
    public Single<Stream<String>> getNfInstances()
    {
        final var options = GetOption.newBuilder().isPrefix(true).withKeysOnly(true).withLimit(0).build();

        return execute(ETCD_OPERATION.GET, serializer.EtcdSecurityNegotiationDatum().getPrefixBytes(), null, options, null).map(response -> response.stream()
                                                                                                                                                    .map(kv ->
                                                                                                                                                    {
                                                                                                                                                        try
                                                                                                                                                        {
                                                                                                                                                            return serializer.EtcdSecurityNegotiationDatum()
                                                                                                                                                                             .key(ByteSequence.from(kv.getKey()
                                                                                                                                                                                                      .getBytes()));
                                                                                                                                                        }
                                                                                                                                                        catch (Exception e)
                                                                                                                                                        {
                                                                                                                                                            log.warn("Ignored invalid DynamicProducer, key: {}",
                                                                                                                                                                     kv.getKey(),
                                                                                                                                                                     e);
                                                                                                                                                            return null;
                                                                                                                                                        }
                                                                                                                                                    })
                                                                                                                                                    .filter(Objects::nonNull));
    }

    /**
     * 
     * @return All the EtcdSecurityNegotiationDatum objects stored in database
     */
    private Single<EtcdEntries<String, EtcdSecurityNegotiationDatum>> getSecurityNegotiationDatum()
    {
        final var options = GetOption.newBuilder().isPrefix(true).withLimit(0).build();

        return execute(ETCD_OPERATION.GET, serializer.EtcdSecurityNegotiationDatum().getPrefixBytes(), null, options, null) //
                                                                                                                           .map(getResp -> new EtcdEntries<>(0,
                                                                                                                                                             getResp //
                                                                                                                                                                    .stream()
                                                                                                                                                                    .map(this::securityNegotiationDatumMapper)
                                                                                                                                                                    .filter(Objects::nonNull)
                                                                                                                                                                    .collect(Collectors.toList())));
    }

    public Single<JsonObject> securityNegotiationDataFromRoamingPartner(String rpName)
    {
        var data = getSecurityNegotiationDatum();
        var dataArray = new JsonArray();
        return data.map(entries ->
        {
            log.debug("Get Entries:{}", entries.getEntries());

            entries.getEntries().stream().filter(entry ->
            {
                // Splits the key to roaming-partner-ref name and static-nf-instance-ref

                var splitKey = entry.getKey().split("/");
                return splitKey[0].equals(rpName);
            }).forEach(k ->
            {
                try
                {
                    dataArray.add(new JsonObject(om.writeValueAsString(k.getValue().convertToYang())));
                }
                catch (JsonProcessingException e)
                {
                    log.error("Security Negotiation Data from Roaming Partner failed with JsonProcessingException", e);
                }
            });
            if (dataArray.isEmpty())
                return new JsonObject();

            return new JsonObject().put(KEY_SECURITY_NEGOTIATION_DATA, dataArray);
        });

    }

    /**
     * read the security datum in etcd db of the number of failures:
     * /ericssson/sc/sepp/n32c/<rp_name>/<sepp_name>
     * 
     * @param roaming-partner-ref
     * @param static-nf-instance-ref *
     * 
     * @return Single<Integer>
     * 
     */
    public Single<Integer> readSecurityNegotiationDataNumOfFailures(String roamingPartnerRef,
                                                                    String staticNfInstanceRef)
    {
        Single<JsonObject> result = readSecurityNegotiationDatum(roamingPartnerRef, staticNfInstanceRef);
        String key = PREFIX + "/" + roamingPartnerRef + "/" + staticNfInstanceRef;
        return result.map(item -> item.getJsonObject(key).getInteger(KEY_NUMBER_OF_FAILURES));
    }

    /**
     * read the security datum in etcd db and fetch the operational state:
     * /ericssson/sc/sepp/n32c/<rp_name>/<sepp_name>
     * 
     * @param roaming-partner-ref
     * @param static-nf-instance-ref
     * 
     * @return Single<String>
     * 
     */
    public Single<String> readSecurityNegotiationDatumOpStateValue(String roamingPartnerRef,
                                                                   String staticNfInstanceRef)
    {
        Single<JsonObject> result = readSecurityNegotiationDatum(roamingPartnerRef, staticNfInstanceRef);
        String key = PREFIX + "/" + roamingPartnerRef + "/" + staticNfInstanceRef;
        return result.map(item -> item.getJsonObject(key).getJsonObject(KEY_OPERATIONAL_STATE).getValue(KEY_VALUE).toString());

    }

    /**
     * Etcd watcher: triggered every time that an update in etcd db occurs
     * concerning N32-C entries. For every update concerning N32c Handshakes, if the
     * operational-state is reported as "faulty", raises the relevant alarm, and if
     * it is reported as "active", ceases the alarm.
     * 
     * 
     */

    public Flowable<Void> watchEtcdEventsforAlarm()
    {
        log.info("Watch Events Started");
        var watchOption = WatchOption.builder().isPrefix(true).build();
        return this.rxEtcd.watch(ByteSequence.from(PREFIX.getBytes()), watchOption).flatMap(notification ->
        {
            var events = notification.getEvents();
            for (WatchEvent event : events)
            {
                log.debug("New etcd event {}", event);

                var eventKV = event.getKeyValue().getValue();
                log.debug("Event KeyValue {}", eventKV);

                var eventKVBytes = new String(eventKV.getBytes(), StandardCharsets.UTF_8);
                try
                {
                    if (!eventKVBytes.isEmpty() && eventKVBytes != null)
                    {
                        JSONObject eventData = new JSONObject(eventKVBytes);
                        log.debug("Event Data {}", eventData);

                        String operationalState = eventData.optJSONObject(KEY_OPERATIONAL_STATE).optString(KEY_VALUE, "");

                        if (OPERATIONAL_STATUS_FAULTY.equals(operationalState))
                        {
                            String seppName = eventData.getString(KEY_SEPP_NAME);
                            String roamingPartnerRef = eventData.getString(KEY_ROAMING_PARTNER_REF);
                            String reason = eventData.optJSONObject(KEY_OPERATIONAL_STATE)
                                                     .optString(KEY_REASON, "The Security Capability Negotiation for the SEPP Instance failed");
                            Triplet<String, Integer, String> dataForAlarm = getDataBySeppName(seppName);

                            final NfInstance nfInstance = this.config.get().getEricssonSeppSeppFunction().getNfInstance().get(0);

                            StringBuilder addInfoBuilder = new StringBuilder();
                            addInfoBuilder.append("nf=sepp-instance,nf-instance=")
                                          .append(nfInstance.getName())
                                          .append(",static-sepp-instance-data=")
                                          .append(dataForAlarm.getThird())
                                          .append(",static-sepp-instance=")
                                          .append(seppName)
                                          .append(",")
                                          .append(dataForAlarm.getFirst())  // fqdn
                                          .append(":")
                                          .append(dataForAlarm.getSecond()) // port
                                          .append(" | ")
                                          .append("nf=sepp-function,nf-instance=")
                                          .append(nfInstance.getName())
                                          .append(",external-network=")
                                          .append(nfInstance.getExternalNetwork().get(0).getName())
                                          .append(",roaming-partner=")
                                          .append(roamingPartnerRef);

                            String additionalInformation = addInfoBuilder.toString();
                            n32cAh.alarmSecurityCapabilityNegotiationRaise(seppName, roamingPartnerRef, reason, dataForAlarm, additionalInformation);
                        }
                        else if (OPERATIONAL_STATUS_ACTIVE.equals(operationalState))
                        {
                            String roamingPartnerRef = eventData.getString(KEY_ROAMING_PARTNER_REF);
                            String seppName = eventData.getString(KEY_SEPP_NAME);
                            Triplet<String, Integer, String> dataForAlarm = getDataBySeppName(seppName);
                            n32cAh.alarmSecurityCapabilityNegotiationCease(seppName, roamingPartnerRef, dataForAlarm);
                        }
                    }
                }
                catch (JSONException e)
                {
                    log.error("Error parsing event data: {}", e.getMessage());
                }
            }
            return Flowable.empty(); // Emit nothing and keep the subscription active
        });
    }

    /**
     * Etcd watcher: triggered every time that an update in etcd db occurs
     * concerning N32-C entries. A struct containing the latest data will fetched
     * 
     * @return seppDataFlowable<Optional<Map<String, Triplet<String, String,
     *         Boolean>>>>
     * 
     */
    public Flowable<Optional<Map<String, Triplet<String, String, Boolean>>>> watchSecurityNegotiationData()
    {
        final Map<String, Triplet<String, String, Boolean>> seppData = new HashMap<>();

        // watch(key) works only with the whole key if no WatchOption is specified.
        var watchOption = WatchOption.builder().isPrefix(true).build();
        return this.rxEtcd.watch(ByteSequence.from(PREFIX.getBytes()), watchOption).flatMap(notification ->
        {
            Map<String, Triplet<String, String, Boolean>> seppDataCopied = new HashMap<>();

            log.debug("New etcd notification {}", notification);

            var events = notification.getEvents();
            log.debug("New etcd event {}", events);

            events.stream().filter(Objects::nonNull).forEach(event ->
            {
                var eventType = event.getEventType();
                log.debug("eventType: {}", eventType);

                var eventValue = event.getKeyValue();
                log.debug("eventValue: {}", eventValue);

                switch (event.getEventType())
                {
                    case PUT:
                        var eventKVBytes = new String(event.getKeyValue().getValue().getBytes(), StandardCharsets.UTF_8);
                        var eventData = new JsonObject(eventKVBytes);
                        log.debug("Event Data {}", eventData);

                        var roamingPartnerRef = eventData.containsKey(KEY_ROAMING_PARTNER_REF) ? eventData.getString(KEY_ROAMING_PARTNER_REF) : "default";
                        var staticNfInstanceRef = eventData.containsKey(KEY_SEPP_NAME) ? eventData.getString(KEY_SEPP_NAME) : "default";
                        var operationalState = eventData.containsKey(KEY_OPERATIONAL_STATE) ? eventData.getJsonObject(KEY_OPERATIONAL_STATE)
                                                                                                       .getString(KEY_VALUE)
                                                                                            : "default";
                        var supportsTargetApiRoot = eventData.containsKey(KEY_SUPPORTS_TARGET_APIROOT) ? eventData.getString(KEY_SUPPORTS_TARGET_APIROOT)
                                                                                                       : "false";

                        seppData.put(staticNfInstanceRef, Triplet.of(roamingPartnerRef, operationalState, Boolean.parseBoolean(supportsTargetApiRoot)));
                        log.debug("seppData updated for: {}", staticNfInstanceRef);
                        break;
                    case DELETE:
                        final var nfKey = new String(event.getKeyValue().getKey().getBytes());
                        var keyParts = nfKey.split("/");
                        var seppName = keyParts[keyParts.length - 1];
                        log.debug("Removing {}", seppName);
                        seppData.remove(seppName);
                        break;
                    default:
                        log.warn("Ignoring unrecognized n32c etcd Watch event type: {}", event.getEventType());
                }
            });

            seppData.entrySet().forEach(entry ->
            {
                log.debug("seppName: {},  roamingPartnerRef: {}, operationalState: {} and supportsTargetApiRoot: {}",
                          entry.getKey(),
                          entry.getValue().getFirst(),
                          entry.getValue().getSecond(),
                          entry.getValue().getThird());
            });

            seppDataCopied.putAll(seppData);
            log.debug("seppData created for: {}, {}", seppData, seppData.entrySet());
            return Flowable.just(Optional.of(seppDataCopied));
        }).doOnError(error -> log.error("Error occurred during n32c watchResponse: {}", error.getMessage())).retryWhen(dbReadRetryPolicy.create());
    }

    /**
     * Reads Security Negotiation Data in order to raise the SEPP, Security
     * Negotiation Capability Failed alarm.
     * 
     * @param
     * @return Single<List<JsonObject>>
     * 
     */
    public Completable raiseSecurityCapabilityNegotiationAlarmForFaultyOpState(List<JsonObject> entries)
    {
        return Completable.fromAction(() ->
        {
            // Loop into the db entries and compare with configuration
            entries.stream().forEach(entry ->
            {

                String operationalState = entry.getJsonObject(KEY_OPERATIONAL_STATE).getString(KEY_VALUE);
                if (OPERATIONAL_STATUS_FAULTY.equals(operationalState))
                {
                    String seppName = entry.getString(KEY_SEPP_NAME);
                    String roamingPartnerRef = entry.getString(KEY_ROAMING_PARTNER_REF);
                    String reason = entry.getJsonObject(KEY_OPERATIONAL_STATE).getString(KEY_REASON);

                    log.debug("seppName {}", seppName);
                    log.debug("roamingPartnerRef {}", roamingPartnerRef);
                    log.debug("reason {}", reason);

                    Triplet<String, Integer, String> dataForAlarm = getDataBySeppName(seppName);
                    final NfInstance nfInstance = this.config.get().getEricssonSeppSeppFunction().getNfInstance().get(0);

                    StringBuilder addInfoBuilder = new StringBuilder();
                    addInfoBuilder.append("nf=sepp-instance,nf-instance=")
                                  .append(nfInstance.getName())
                                  .append(",static-sepp-instance-data=")
                                  .append(dataForAlarm.getThird())
                                  .append(",static-sepp-instance=")
                                  .append(seppName)
                                  .append(",")
                                  .append(dataForAlarm.getFirst())  // fqdn
                                  .append(":")
                                  .append(dataForAlarm.getSecond()) // port
                                  .append(" | ")
                                  .append("nf=sepp-function,nf-instance=")
                                  .append(nfInstance.getName())
                                  .append(",external-network=")
                                  .append(nfInstance.getExternalNetwork().get(0).getName())
                                  .append(",roaming-partner=")
                                  .append(roamingPartnerRef);

                    String additionalInformation = addInfoBuilder.toString();

                    try
                    {
                        n32cAh.alarmSecurityCapabilityNegotiationRaise(seppName, roamingPartnerRef, reason, dataForAlarm, additionalInformation);
                    }
                    catch (JsonProcessingException e)
                    {
                        log.error("Caught Exception while raising alarm {}.", e.toString());
                    }
                }
            });
        });
    }

    public EtcdEntry<String, EtcdSecurityNegotiationDatum> securityNegotiationDatumMapper(Pair<String, String> p)
    {
        try
        {
            var keyValueBuilder = KeyValue.newBuilder();
            keyValueBuilder.setKey(ByteString.copyFrom(p.getKey(), StandardCharsets.UTF_8));
            keyValueBuilder.setLease(0);
            keyValueBuilder.setModRevision(0);
            keyValueBuilder.setValue(ByteString.copyFrom(p.getValue(), StandardCharsets.UTF_8));
            var keyValue = keyValueBuilder.build();

            var keyUtf8 = keyValue.getKey().toStringUtf8();
            log.debug("Key value at etcd keyValue: key: {} value: {}", keyUtf8, p.getValue());

            return new EtcdEntry<>(serializer.EtcdSecurityNegotiationDatum(), new io.etcd.jetcd.KeyValue(keyValue, ByteSequence.EMPTY));
        }
        catch (Exception e)
        {
            log.error("Ignored invalid DynamicProducer, key: {}", p.getKey(), e);
            return null;
        }
    }

    public JsonObject fromEtcdEntryToJsonObjectMapper(EtcdEntries<String, EtcdSecurityNegotiationDatum> entries)
    {
        var dataArray = new JsonArray();

        entries.getEntries().stream().forEach(k ->
        {
            try
            {
                dataArray.add(new JsonObject(om.writeValueAsString(k.getValue().convertToYang())));
            }
            catch (JsonProcessingException e)
            {
                log.error("Error while mapping EtcdEntries to JsonObjects: {}", e.toString());
            }
        });
        if (dataArray.isEmpty())
            return new JsonObject();
        return new JsonObject().put(KEY_SECURITY_NEGOTIATION_DATA, dataArray);
    }

    public JsonObject receivedPlmnIdJsonMapper(EtcdEntries<String, EtcdSecurityNegotiationDatum> entries)
    {
        var dataArray = new JsonArray();
        return entries.getEntries().stream().findFirst().map(k ->
        {
            k.getValue().convertToYang().getReceivedPlmnId().forEach(id ->
            {

                try
                {
                    dataArray.add(new JsonObject(om.writeValueAsString(id)));
                }
                catch (JsonProcessingException e)
                {
                    log.error("Error while mapping EtcdEntries to JsonObjects", e);
                }
            });
            return new JsonObject().put(KEY_RECEIVED_PLMN_ID, dataArray);
        }).orElseGet(JsonObject::new);
    }

    public JsonObject singleElementJsonMapper(EtcdEntries<String, EtcdSecurityNegotiationDatum> entries,
                                              String elementType)
    {
        return entries.getEntries().stream().findFirst().map(k ->
        {
            Object value;
            switch (elementType)
            {
                case KEY_LAST_UPDATE:
                    var dateFormater = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                    value = dateFormater.format(k.getValue().convertToYang().getLastUpdate());
                    break;
                case KEY_SECURITY_CAPABILITY:
                    value = k.getValue().convertToYang().getSecurityCapability();
                    break;
                case KEY_SUPPORTS_TARGET_APIROOT:
                    value = k.getValue().convertToYang().getSupportsTargetApiroot();
                    break;
                case KEY_OPERATIONAL_STATE:
                    value = k.getValue().convertToYang().getOperationalState();
                    break;
                default:
                    value = null;
                    break;
            }
            if (value == null)
                return new JsonObject();
            return new JsonObject().put(elementType, value);
        }).orElseGet(JsonObject::new);
    }

    public JsonObject nfInstanceEtcdJsonMapper(EtcdEntries<String, EtcdSecurityNegotiationDatum> entries)
    {
        return entries.getEntries().stream().findFirst().map(k ->
        {
            try
            {
                return new JsonObject(om.writeValueAsString(k.getValue().convertToYang()));
            }
            catch (JsonProcessingException e)
            {
                log.error("Error while mapping EtcdEntries to JsonObjects: {}", e.toString());
                return null;
            }
        }).orElseGet(JsonObject::new);
    }

    public JsonObject singleNfInstanceEtcdJsonMapper(EtcdEntries<String, EtcdSecurityNegotiationDatum> entries)
    {
        return entries.getEntries().stream().findFirst().map(k ->
        {
            try
            {
                return new JsonObject(om.writeValueAsString(k.getValue().convertToYang()));
            }
            catch (JsonProcessingException e)
            {
                log.error("Error while mapping EtcdEntries to JsonObjects", e);
                return null;
            }
        }).orElseGet(JsonObject::new);

    }

    /**
     * DND-36603: Pre-process plmn-id lists In case mnc has only two digits, add the
     * zero filler digit according in order to be filler agnostic during the list
     * comparison only
     * 
     * @param plmnIdListOriginal
     *
     * @return plmnIdListFilled
     * @throws
     */
    private List<PlmnId> preprocessPlmnIdLists(List<PlmnId> plmnIdListOriginal)
    {
        List<PlmnId> plmnIdListFilled = new ArrayList<>();

        plmnIdListOriginal.stream().forEach(plmn ->
        {
            if (plmn.getMnc().length() == 2)
            {
                PlmnId tmp = new PlmnId();
                tmp.setMcc(plmn.getMcc());
                var filledMnc = "0" + plmn.getMnc();
                tmp.setMnc(filledMnc);
                plmnIdListFilled.add(tmp);
            }
            else
                plmnIdListFilled.add(plmn);

        });

        return plmnIdListFilled;
    }
}
