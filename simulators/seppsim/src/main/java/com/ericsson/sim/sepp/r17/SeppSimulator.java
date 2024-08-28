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
 * Created on: Jul 16, 2020
 *     Author: eedstl
 */

package com.ericsson.sim.sepp.r17;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.ext.monitor.MonitorAdapter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Command;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Counter;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Instance;
import com.ericsson.adpal.ext.monitor.api.v0.commands.Result;
import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.common.Specs3gpp;
import com.ericsson.cnal.common.WebClient;
import com.ericsson.cnal.openapi.r17.SbiNfPeerInfo;
import com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession.AfCoordinationInfo;
import com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession.EbiArpMapping;
import com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession.NotificationInfo;
import com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession.PduSessionCreateData;
import com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession.PduSessionCreatedData;
import com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession.QosFlowSetupItem;
import com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession.SmContext;
import com.ericsson.cnal.openapi.r17.ts29502.nsmf.pdusession.SmContextRetrievedData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.niddau.AuthorizationData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.niddau.UserIdentifier;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.pp.PlmnEcInfo;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.AccessAndMobilitySubscriptionData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.DnnInfo;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.EmergencyInfo;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.EnhancedCoverageRestrictionData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.GroupIdentifiers;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.IdTranslationResult;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.LcsMoData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.LcsMoServiceClass;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.LcsPrivacyData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.LocationPrivacyInd;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.Lpi;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.Nssai;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.PduSession;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.PgwInfo;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SdmSubsModification;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SdmSubscription;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SessionManagementSubscriptionData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SharedData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SmfSelectionSubscriptionData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SmsManagementSubscriptionData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SmsSubscriptionData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SmsfInfo;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SnssaiInfo;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SubscriptionDataSets;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.TraceDataResponse;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.UeContextInAmfData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.UeContextInSmfData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.UeContextInSmsfData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.UeId;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.V2xSubscriptionData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.ssau.ServiceSpecificAuthorizationData;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau.AuthEvent;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau.AuthenticationInfoResult;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau.AvEpsAka;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau.HssAuthenticationInfoResult;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau.HssAuthenticationVectors;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau.HssAvType;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.Amf3GppAccessRegistration;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.AmfNon3GppAccessRegistration;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.EpsInterworkingInfo;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.EpsIwkPgw;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.ImsVoPs;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.IpSmGwRegistration;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.RegistrationDataSets;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.RegistrationLocationInfo;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.SmfRegistration;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.SmfRegistrationInfo;
import com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.SmsfRegistration;
import com.ericsson.cnal.openapi.r17.ts29509.nausf.ueauthentication.AuthResult;
import com.ericsson.cnal.openapi.r17.ts29509.nausf.ueauthentication.AuthenticationInfo;
import com.ericsson.cnal.openapi.r17.ts29509.nausf.ueauthentication.ConfirmationDataResponse;
import com.ericsson.cnal.openapi.r17.ts29509.nausf.ueauthentication.EapSession;
import com.ericsson.cnal.openapi.r17.ts29509.nausf.ueauthentication.UEAuthenticationCtx;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.bootstrapping.BootstrappingInfo;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.bootstrapping.Status;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.SearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.StoredSearchResult;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.Links;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFProfile;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFStatus;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.SubscriptionData;
import com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol.SessionRule;
import com.ericsson.cnal.openapi.r17.ts29512.npcf.smpolicycontrol.SmPolicyDecision;
import com.ericsson.cnal.openapi.r17.ts29518.namf.communication.AssignedEbiData;
import com.ericsson.cnal.openapi.r17.ts29518.namf.communication.N1N2MessageTransferCause;
import com.ericsson.cnal.openapi.r17.ts29518.namf.communication.N1N2MessageTransferRspData;
import com.ericsson.cnal.openapi.r17.ts29518.namf.communication.N2InformationTransferResult;
import com.ericsson.cnal.openapi.r17.ts29518.namf.communication.N2InformationTransferRspData;
import com.ericsson.cnal.openapi.r17.ts29518.namf.communication.NonUeN2InfoSubscriptionCreatedData;
import com.ericsson.cnal.openapi.r17.ts29518.namf.communication.UeN1N2InfoSubscriptionCreatedData;
import com.ericsson.cnal.openapi.r17.ts29518.namf.communication.UeRegStatusUpdateRspData;
import com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure.AmfCreatedEventSubscription;
import com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure.AmfEventSubscription;
import com.ericsson.cnal.openapi.r17.ts29518.namf.eventexposure.AmfUpdatedEventSubscription;
import com.ericsson.cnal.openapi.r17.ts29526.nnssaaf.nssaa.SliceAuthConfirmationData;
import com.ericsson.cnal.openapi.r17.ts29526.nnssaaf.nssaa.SliceAuthConfirmationResponse;
import com.ericsson.cnal.openapi.r17.ts29526.nnssaaf.nssaa.SliceAuthContext;
import com.ericsson.cnal.openapi.r17.ts29526.nnssaaf.nssaa.SliceAuthInfo;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.AccessType;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Ambr;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Arp;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Guami;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.InvalidParam;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Link;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.LinksValueSchema;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.LteV2xAuth;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.NrV2xAuth;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PatchResult;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnIdNid;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PreemptionCapability;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PreemptionVulnerability;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ProblemDetails;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.RatType;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.ReportItem;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.TraceData;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.TraceDepth;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.UeAuth;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.IpxProviderSecInfo;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.N32fContextInfo;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.N32fErrorInfo;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.N32fErrorType;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.ProtectionPolicy;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.SecNegotiateReqData;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.SecNegotiateRspData;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.SecParamExchReqData;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.SecParamExchRspData;
import com.ericsson.cnal.openapi.r17.ts29573.n32.handshake.SecurityCapability;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.sim.sepp.r17.SeppSimulator.Configuration.Disturbance;
import com.ericsson.utilities.common.Count;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Event;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.common.VersionInfo;
import com.ericsson.utilities.file.KeyCert;
import com.ericsson.utilities.file.KeyCertProvider;
import com.ericsson.utilities.file.TrustedCert;
import com.ericsson.utilities.file.TrustedCertProvider;
import com.ericsson.utilities.http.RouterHandler;
import com.ericsson.utilities.http.Url;
import com.ericsson.utilities.http.WebServer;
import com.ericsson.utilities.http.openapi.OpenApiServer;
import com.ericsson.utilities.http.openapi.OpenApiServer.Context3;
import com.ericsson.utilities.http.openapi.OpenApiServer.IfApiHandler;
import com.ericsson.utilities.http.openapi.OpenApiServer.IpFamily;
import com.ericsson.utilities.http.openapi.OpenApiTask;
import com.ericsson.utilities.http.openapi.OpenApiTask.DataIndex;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AtomicDouble;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.core.net.SocketAddress;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.ext.web.client.HttpResponse;

public class SeppSimulator implements Runnable, MonitorAdapter.CommandCounter.Provider, MonitorAdapter.CommandEsa.Provider
{
    public static class Builder
    {
        private static Pair<String, String> specR17N32Handshake = Pair.of(Specs3gpp.R17_N32_HANDSHAKE, "/n32c-handshake/v1");
        private static Pair<String, String> specR17NamfCommunication = Pair.of(Specs3gpp.R17_NAMF_COMMUNICATION, "/namf-comm/v1");
        private static Pair<String, String> specR17NamfEventExposure = Pair.of(Specs3gpp.R17_NAMF_EVENT_EXPOSURE, "/namf-evts/v1");
        private static Pair<String, String> specR17NausfUeAuthentication = Pair.of(Specs3gpp.R17_NAUSF_UE_AUTHENTICATION, "/nausf-auth/v1");
        private static Pair<String, String> specR17NnrfBootstrapping = Pair.of(Specs3gpp.R17_NNRF_BOOTSTRAPPING, "/bootstrapping");
        private static Pair<String, String> specR17NnrfNfDiscovery = Pair.of(Specs3gpp.R17_NNRF_NF_DISCOVERY, "/nnrf-disc/v1");
        private static Pair<String, String> specR17NnrfNfManagement = Pair.of(Specs3gpp.R17_NNRF_NF_MANAGEMENT, "/nnrf-nfm/v1");
        private static Pair<String, String> specR17NnssaafNssaa = Pair.of(Specs3gpp.R17_NNSSAAF_NSSAA, "/nnssaaf-nssaa/v1");
        private static Pair<String, String> specR17NpcfSmPolicyControl = Pair.of(Specs3gpp.R17_NPCF_SM_POLICY_CONTROL, "/npcf-smpolicycontrol/v1");
        private static Pair<String, String> specR17NsmfPduSession = Pair.of(Specs3gpp.R17_NSMF_PDU_SESSION, "/nsmf-pdusession/v1");
        private static Pair<String, String> specR17NudmNiddau = Pair.of(Specs3gpp.R17_NUDM_NIDDAU, "/nudm-niddau/v1");
        private static Pair<String, String> specR17NudmParameterProvision = Pair.of(Specs3gpp.R17_NUDM_PP, "/nudm-pp/v1");
        private static Pair<String, String> specR17NudmSsau = Pair.of(Specs3gpp.R17_NUDM_SSAU, "/nudm-ssau/v1");
        private static Pair<String, String> specR17NudmSubscriberDataManagement = Pair.of(Specs3gpp.R17_NUDM_SDM, "/nudm-sdm/v2");
        private static Pair<String, String> specR17NudmUeAuthentication = Pair.of(Specs3gpp.R17_NUDM_UEAU, "/nudm-ueau/v1");
        private static Pair<String, String> specR17NudmUeContextManagement = Pair.of(Specs3gpp.R17_NUDM_UECM, "/nudm-uecm/v1");
        private static Pair<String, String> specNapiTest = Pair.of(NAPI_TEST_YAML, "/napi-test/v1");
        private static Pair<String, String> specNapiNotifications = Pair.of(NAPI_NOTIFICATIONS_YAML, "/");

        private static final List<Pair<String, String>> ALL_SPECS = List.of(specR17N32Handshake,
                                                                            specR17NamfCommunication,
                                                                            specR17NamfEventExposure,
                                                                            specR17NausfUeAuthentication,
                                                                            specR17NnrfBootstrapping,
                                                                            specR17NnrfNfDiscovery,
                                                                            specR17NnrfNfManagement,
                                                                            specR17NnssaafNssaa,
                                                                            specR17NpcfSmPolicyControl,
                                                                            specR17NsmfPduSession,
                                                                            specR17NudmNiddau,
                                                                            specR17NudmParameterProvision,
                                                                            specR17NudmSsau,
                                                                            specR17NudmSubscriberDataManagement,
                                                                            specR17NudmUeAuthentication,
                                                                            specR17NudmUeContextManagement,
                                                                            specNapiTest,
                                                                            specNapiNotifications);

        /**
         * @param hosts Hosts for IPv4 or IPv6 or both, at most one of each kind
         * @param port  Common port
         */
        public static Builder of(final List<String> hosts,
                                 final Integer port)
        {
            return new Builder(hosts, port);
        }

        public static Builder of(final String host,
                                 final Integer port)
        {
            return of(List.of(host), port);
        }

        private final List<String> hosts;
        private final Integer port;

        private Configuration.LoadTestMode defaultLoadTestMode = new Configuration.LoadTestMode();
        private Integer portTls;
        private String certificatesPath;
        private List<Pair<String, String>> specs3gpp;
        private KeyCert keyCert;
        private TrustedCert trustedCert;
        private Boolean sni = false;

        private Builder(final List<String> hosts,
                        final Integer port)
        {
            this.hosts = hosts;
            this.port = port;
            this.portTls = 443;
            this.certificatesPath = EnvVars.get("CERTIFICATES_PATH", "");
            this.specs3gpp = ALL_SPECS;
        }

        public SeppSimulator build() throws Exception
        {
            if (this.specs3gpp != ALL_SPECS)
                this.addSpec(specNapiTest).addSpec(specNapiNotifications); // Always add this last.

            return new SeppSimulator(this.defaultLoadTestMode,
                                     this.hosts,
                                     this.port,
                                     this.portTls,
                                     this.certificatesPath,
                                     this.specs3gpp,
                                     this.keyCert,
                                     this.trustedCert,
                                     this.sni);
        }

        public Builder withApiR17N32Handshake()
        {
            return this.addSpec(specR17N32Handshake);
        }

        public Builder withApiR17Namf()
        {
            return this.addSpec(specR17NamfCommunication).addSpec(specR17NamfEventExposure);
        }

        public Builder withApiR17NamfCommunication()
        {
            return this.addSpec(specR17NamfCommunication);
        }

        public Builder withApiR17NamfEventExposure()
        {
            return this.addSpec(specR17NamfEventExposure);
        }

        public Builder withApiR17Nausf()
        {
            return this.addSpec(specR17NausfUeAuthentication);
        }

        public Builder withApiR17NausfUeAuthentication()
        {
            return this.addSpec(specR17NausfUeAuthentication);
        }

        public Builder withApiR17Nnrf()
        {
            return this.addSpec(specR17NnrfNfDiscovery);
        }

        public Builder withApiR17NnrfNfDiscovery()
        {
            return this.addSpec(specR17NnrfNfDiscovery);
        }

        public Builder withApiR17Npcf()
        {
            return this.addSpec(specR17NpcfSmPolicyControl);
        }

        public Builder withApiR17NpcfSmPolicyControl()
        {
            return this.addSpec(specR17NpcfSmPolicyControl);
        }

        public Builder withApiR17Nsmf()
        {
            return this.addSpec(specR17NsmfPduSession);
        }

        public Builder withApiR17NsmfPduSession()
        {
            return this.addSpec(specR17NsmfPduSession);
        }

        public Builder withApiR17Nudm()
        {
            return this.addSpec(specR17NudmNiddau)
                       .addSpec(specR17NudmParameterProvision)
                       .addSpec(specR17NudmSsau)
                       .addSpec(specR17NudmSubscriberDataManagement)
                       .addSpec(specR17NudmUeAuthentication)
                       .addSpec(specR17NudmUeContextManagement);
        }

        public Builder withApiR17NudmNiddau()
        {
            return this.addSpec(specR17NudmNiddau);
        }

        public Builder withApiR17NudmParameterProvision()
        {
            return this.addSpec(specR17NudmParameterProvision);
        }

        public Builder withApiR17NudmSsau()
        {
            return this.addSpec(specR17NudmSsau);
        }

        public Builder withApiR17NudmSubscriberDataManagement()
        {
            return this.addSpec(specR17NudmSubscriberDataManagement);
        }

        public Builder withApiR17NudmUeAuthentication()
        {
            return this.addSpec(specR17NudmUeAuthentication);
        }

        public Builder withApiR17NudmUeContextManagement()
        {
            return this.addSpec(specR17NudmUeContextManagement);
        }

        public Builder withCertificatesPath(String certificatesPath)
        {
            this.certificatesPath = certificatesPath;
            return this;
        }

        public Builder withDefaultLoadTestMode(Configuration.LoadTestMode defaultLoadTestMode)
        {
            this.defaultLoadTestMode = defaultLoadTestMode;
            return this;
        }

        public Builder withKeyCert(KeyCert keyCert)
        {
            this.keyCert = keyCert;
            return this;
        }

        public Builder withPortTls(Integer portTls)
        {
            this.portTls = portTls;
            return this;
        }

        public Builder withSni(Boolean sni)
        {
            this.sni = sni;
            return this;
        }

        public Builder withTrustedCert(TrustedCert trustedCert)
        {
            this.trustedCert = trustedCert;
            return this;
        }

        private Builder addSpec(final Pair<String, String> spec)
        {
            if (this.specs3gpp == ALL_SPECS)
                this.specs3gpp = new ArrayList<>();

            this.specs3gpp.add(spec);
            return this;
        }

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({ "loadTestMode", "ownRole", "ownDomain", "ownIpAddress", "api", "roamingPartner", "supportedPlmn" })
    public static class Configuration
    {
        @JsonPropertyOrder({ "n32Handshake",
                             "namfCommunication",
                             "namfEventExposure",
                             "napiNotifications",
                             "nnrfBootstrapping",
                             "nnrfNfDiscovery",
                             "nnrfNfManagement",
                             "nudmNiddau",
                             "nudmParameterProvision",
                             "nudmSsau",
                             "nudmSubscriberDataManagement",
                             "nudmUeAuthentication",
                             "nudmUeContextManagement" })
        public static class Api
        {
            @JsonPropertyOrder({ "disturbances" })
            public static class N32Handshake extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NamfCommunication extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NamfEventExposure extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NapiNotifications extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NapiTest extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NnrfBootstrapping extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "validityPeriodInSecs", "disturbances" })
            public static class NnrfNfDiscovery extends Disturbances
            {
                private static final int VALIDITY_PERIOD_SECS = 2 * 60 * 60; // 2 hours

                @JsonProperty("validityPeriodInSecs")
                private int validityPeriodInSecs = VALIDITY_PERIOD_SECS;

                public synchronized int getValidityPeriodInSecs()
                {
                    return this.validityPeriodInSecs;
                }

                @JsonIgnore
                public synchronized NnrfNfDiscovery setValidityPeriodInSecs(final int validityPeriodInSecs)
                {
                    log.info("NF discovery: new validity period [s]: {}", validityPeriodInSecs);
                    this.validityPeriodInSecs = validityPeriodInSecs;
                    this.setUpdated();

                    return this;
                }

                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NnrfNfManagement extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NpcfSmPolicyControl extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NsmfPduSession extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NudmNiddau extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NudmParameterProvision extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NudmSsau extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NudmSubscriberDataManagement extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NudmUeAuthentication extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonPropertyOrder({ "disturbances" })
            public static class NudmUeContextManagement extends Disturbances
            {
                @Override
                public String toString()
                {
                    try
                    {
                        return json.writeValueAsString(this);
                    }
                    catch (JsonProcessingException e)
                    {
                        return e.toString();
                    }
                }
            }

            @JsonProperty("n32Handshake")
            private final N32Handshake n32Handshake = new N32Handshake();

            @JsonProperty("namfCommunication")
            private final NamfCommunication namfCommunication = new NamfCommunication();

            @JsonProperty("namfEventExposure")
            private final NamfEventExposure namfEventExposure = new NamfEventExposure();

            @JsonProperty("napiNotifications")
            private final NapiNotifications napiNotifications = new NapiNotifications();

            @JsonProperty("napiTest")
            private final NapiTest napiTest = new NapiTest();

            @JsonProperty("nnrfBootstrapping")
            private final NnrfBootstrapping nnrfBootstrapping = new NnrfBootstrapping();

            @JsonProperty("nnrfNfDiscovery")
            private final NnrfNfDiscovery nnrfNfDiscovery = new NnrfNfDiscovery();

            @JsonProperty("nnrfNfManagement")
            private final NnrfNfManagement nnrfNfManagement = new NnrfNfManagement();

            @JsonProperty("npcfSmPolicyControl")
            private final NpcfSmPolicyControl npcfSmPolicyControl = new NpcfSmPolicyControl();

            @JsonProperty("nsmfPduSession")
            private final NsmfPduSession nsmfPduSession = new NsmfPduSession();

            @JsonProperty("nudmNiddau")
            private final NudmNiddau nudmNiddau = new NudmNiddau();

            @JsonProperty("nudmParameterProvision")
            private final NudmParameterProvision nudmParameterProvision = new NudmParameterProvision();

            @JsonProperty("nudmSsau")
            private final NudmSsau nudmSsau = new NudmSsau();

            @JsonProperty("nudmSubscriberDataManagement")
            private final NudmSubscriberDataManagement nudmSubscriberDataManagement = new NudmSubscriberDataManagement();

            @JsonProperty("nudmUeAuthentication")
            private final NudmUeAuthentication nudmUeAuthentication = new NudmUeAuthentication();

            @JsonProperty("nudmUeContextManagement")
            private final NudmUeContextManagement nudmUeContextManagement = new NudmUeContextManagement();

            public N32Handshake getN32Handshake()
            {
                return this.n32Handshake;
            }

            public NamfCommunication getNamfCommunication()
            {
                return this.namfCommunication;
            }

            public NamfEventExposure getNamfEventExposure()
            {
                return this.namfEventExposure;
            }

            public NapiNotifications getNapiNotifications()
            {
                return this.napiNotifications;
            }

            public NapiNotifications getNapiTest()
            {
                return this.napiNotifications;
            }

            public NnrfBootstrapping getNnrfBootstrapping()
            {
                return this.nnrfBootstrapping;
            }

            public NnrfNfDiscovery getNnrfNfDiscovery()
            {
                return this.nnrfNfDiscovery;
            }

            public NnrfNfManagement getNnrfNfManagement()
            {
                return this.nnrfNfManagement;
            }

            public NpcfSmPolicyControl getNpcfSmPolicyControl()
            {
                return this.npcfSmPolicyControl;
            }

            public NsmfPduSession getNsmfPduSession()
            {
                return this.nsmfPduSession;
            }

            public NudmNiddau getNudmNiddau()
            {
                return this.nudmNiddau;
            }

            public NudmParameterProvision getNudmParameterProvision()
            {
                return this.nudmParameterProvision;
            }

            public NudmSsau getNudmSsau()
            {
                return this.nudmSsau;
            }

            public NudmSubscriberDataManagement getNudmSubscriberDataManagement()
            {
                return this.nudmSubscriberDataManagement;
            }

            public NudmUeAuthentication getNudmUeAuthentication()
            {
                return this.nudmUeAuthentication;
            }

            public NudmUeContextManagement getNudmUeContextManagement()
            {
                return this.nudmUeContextManagement;
            }

            @Override
            public String toString()
            {
                try
                {
                    return json.writeValueAsString(this);
                }
                catch (JsonProcessingException e)
                {
                    return e.toString();
                }
            }
        }

        public static class Base
        {
            @JsonIgnore
            private boolean isDirty = false;

            @JsonIgnore
            protected synchronized boolean hasBeenUpdated()
            {
                boolean isDirty = this.isDirty;
                this.isDirty = false;
                return isDirty;
            }

            @JsonIgnore
            protected synchronized void setUpdated()
            {
                this.isDirty = true;
            }
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "weight", "status", "delayInMillis", "drop" })
        public static class Disturbance implements Comparable<Disturbance>
        {
            public static final long HTTP2_REFUSED_STREAM = 7; // See RFC 7540

            @JsonIgnore
            private Double weight = null;

            @JsonIgnore
            private HttpResponseStatus status = null;

            @JsonIgnore
            private Long delayInMillis = 0l;

            @JsonIgnore
            private Long drop = 0l;

            @JsonIgnore
            private Map<String, List<String>> additionalHeaders = new HashMap<>();

            @Override
            public int compareTo(Disturbance o)
            {
                int result = 0;

                if (this.status != o.status) // NOSONAR
                {
                    if (this.status == null)
                        result = -1;
                    else if (o.status == null)
                        result = 1;
                    else
                        result = this.status.compareTo(o.status);
                }

                if (result == 0)
                {
                    if (this.delayInMillis != o.delayInMillis) // NOSONAR
                    {
                        if (this.delayInMillis == null)
                            result = -1;
                        else if (o.delayInMillis == null)
                            result = 1;
                        else
                            result = this.delayInMillis.compareTo(o.delayInMillis);
                    }
                }

                if (result == 0)
                {
                    if (this.drop != o.drop) // NOSONAR
                    {
                        if (this.drop == null)
                            result = -1;
                        else if (o.drop == null)
                            result = 1;
                        else
                            result = this.drop.compareTo(o.drop);
                    }
                }

                return result;
            }

            @JsonIgnore
            public synchronized boolean doDrop()
            {
                return this.drop > 0;
            }

            @JsonIgnore
            public synchronized Disturbance doDrop(boolean drop)
            {
                return this.setDropAndReplyWith(Boolean.compare(drop, true) + 1L);
            }

            @JsonProperty("additionalHeaders")
            public synchronized Map<String, List<String>> getAdditionalHeaders()
            {
                return this.additionalHeaders;
            }

            @JsonProperty("delayInMillis")
            public synchronized Long getDelayInMillis()
            {
                return this.delayInMillis;
            }

            @JsonProperty("drop")
            public synchronized long getDrop()
            {
                return this.drop;
            }

            @JsonIgnore
            public synchronized HttpResponseStatus getStatus()
            {
                return this.status;
            }

            @JsonProperty("weight")
            public synchronized Double getWeight()
            {
                return this.weight;
            }

            @JsonProperty("additionalHeaders")
            public synchronized Disturbance setAdditionalHeaders(final Map<String, List<String>> additionalHeaders)
            {
                this.additionalHeaders = additionalHeaders;
                return this;
            }

            @JsonProperty("delayInMillis")
            public synchronized Disturbance setDelayInMillis(final Long delayInMillis)
            {
                this.delayInMillis = delayInMillis == null ? 0l : delayInMillis;
                log.info("HTTP response: new delay [ms]: {}", this.delayInMillis);

                return this;
            }

            @JsonProperty("drop")
            public synchronized Disturbance setDropAndReplyWith(long drop)
            {
                if (drop == 0)
                    log.info("HTTP response: will not be dropped");
                else if (drop == 1)
                    log.info("HTTP response: will be dropped");
                else
                    log.info("HTTP response: will be dropped and connection reset with error code {}", drop);

                this.drop = drop;

                return this;
            }

            @JsonIgnore
            public synchronized Disturbance setDropAndReplyWithRefuseStream()
            {
                return this.setDropAndReplyWith(HTTP2_REFUSED_STREAM);
            }

            @JsonIgnore
            public synchronized Disturbance setStatus(final HttpResponseStatus status)
            {
                log.info("HTTP response: new status: {}", status);
                this.status = status;

                return this;
            }

            @JsonProperty("weight")
            public synchronized Disturbance setWeight(final Double weight)
            {
                this.weight = weight;
                return this;
            }

            @Override
            public String toString()
            {
                try
                {
                    return json.writeValueAsString(this);
                }
                catch (JsonProcessingException e)
                {
                    return e.toString();
                }
            }

            @JsonProperty("status")
            private Integer getStatusAsInteger()
            {
                return this.getStatus() != null ? this.getStatus().code() : null;
            }

            @JsonProperty("status")
            private void setStatusFromInteger(final Integer status)
            {
                this.setStatus(HttpResponseStatus.valueOf(status));
            }
        }

        public static class Disturbances extends Base
        {
            private static final int capacity = 10000;
            private static final Disturbance none = new Disturbance();

            @JsonIgnore
            private final Random random = new Random(System.currentTimeMillis());

            @JsonIgnore
            private int cnt = 0;

            @JsonIgnore
            private Boolean isWeighted = false;

            @JsonIgnore
            private List<Disturbance> disturbances = null;

            @JsonIgnore
            private List<Disturbance> disturbancesAsConfigured = null;

            @JsonIgnore
            public void clearDisturbances()
            {
                this.disturbances = null;
                this.disturbancesAsConfigured = null;
            }

            @JsonIgnore
            public Disturbance getNext()
            {
                if (this.disturbances == null || this.disturbances.isEmpty())
                    return none;

                if (this.isWeighted)
                    return this.disturbances.get(this.random.nextInt(this.disturbances.size()));

                return this.disturbances.get(this.cnt++ % this.disturbances.size());
            }

            @JsonProperty("disturbances")
            public void setDisturbances(final Disturbance... disturbances)
            {
                this.disturbancesAsConfigured = Arrays.asList(disturbances);

                final Map<Disturbance, Double> weightPerDisturbance = new TreeMap<>();

                this.isWeighted = false;

                for (int i = 0; i < disturbances.length; ++i)
                {
                    final Disturbance disturbance = disturbances[i];

                    if (disturbance.getWeight() != null && disturbance.getWeight() > 0d)
                    {
                        final Double weight = weightPerDisturbance.get(disturbance);
                        weightPerDisturbance.put(disturbance, weight != null ? weight : 0d + disturbance.getWeight());
                        this.isWeighted = true;
                    }
                }

                if (this.isWeighted)
                {
                    final AtomicDouble sumOfWeights = new AtomicDouble(0d);

                    weightPerDisturbance.values().stream().forEach(sumOfWeights::addAndGet);

                    final List<Disturbance> distributionOfDisturbances = new ArrayList<>(capacity);
                    final AtomicInteger imax = new AtomicInteger(capacity);

                    weightPerDisturbance.entrySet().forEach(e ->
                    {
                        final long quota = Math.round(capacity * e.getValue() / sumOfWeights.get());

                        for (int i = 0; imax.get() > 0 && i < quota; imax.decrementAndGet(), i++)
                            distributionOfDisturbances.add(e.getKey());
                    });

                    this.disturbances = distributionOfDisturbances;
                }
                else
                {
                    this.disturbances = this.disturbancesAsConfigured;
                }
            }

            @JsonProperty("disturbances")
            private List<Disturbance> getDisturbancesAsConfigured()
            {
                return this.disturbancesAsConfigured;
            }
        }

        @JsonPropertyOrder({ "isEnabled" })
        public static class LoadTestMode
        {
            boolean isEnabled = false;

            @JsonProperty("isEnabled")
            public boolean isEnabled()
            {
                return this.isEnabled;
            }

            @JsonProperty("isEnabled")
            public LoadTestMode setEnabled(boolean isEnabled)
            {
                this.isEnabled = isEnabled;
                return this;
            }

            @Override
            public String toString()
            {
                try
                {
                    return json.writeValueAsString(this);
                }
                catch (JsonProcessingException e)
                {
                    return e.toString();
                }
            }
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "name", "sepp" })
        public static class RoamingPartner
        {
            @JsonProperty("name")
            private String name = null;

            @JsonProperty("sepp")
            private List<Sepp> sepp = new ArrayList<>();

            @JsonIgnore
            private boolean isDirty = false;

            public synchronized String getName()
            {
                return this.name;
            }

            public synchronized List<Sepp> getSepp()
            {
                return this.sepp;
            }

            @JsonIgnore
            public synchronized boolean hasBeenUpdated()
            {
                boolean isDirty = this.isDirty;
                this.isDirty = false;
                return isDirty;
            }

            public synchronized RoamingPartner setName(final String name)
            {
                this.name = name;
                this.isDirty = true;
                return this;
            }

            public synchronized RoamingPartner setSepp(final List<Sepp> sepp)
            {
                this.sepp = sepp;
                this.isDirty = true;
                return this;
            }

            @Override
            public String toString()
            {
                try
                {
                    return json.writeValueAsString(this);
                }
                catch (JsonProcessingException e)
                {
                    return e.toString();
                }
            }
        }

        public enum Role
        {
            SEPP_OR_NF("SEPP_OR_NF"),
            SEPP_ONLY("SEPP_ONLY"),
            NF_ONLY("NF_ONLY");

            private final String value;

            Role(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonPropertyOrder({ "name", "fqdn", "scheme", "port", "ipv4-address", "ipv6-address" })
        public static class Sepp
        {
            private static boolean isIpv6;

            static
            {
                try
                {
                    isIpv6 = isIpv6();
                }
                catch (UnknownHostException e)
                {
                    log.error("Error determining IP version. Cause: {}", e.getMessage());
                }
            }

            private static boolean isIpv6() throws UnknownHostException
            {
                boolean result = InetAddress.getByName(EnvVars.get("ERIC_SEPPSIM_PORT_80_TCP_ADDR")) instanceof Inet6Address;
                log.info("result={}", result);
                return result;
            }

            @JsonProperty("name")
            private String name;

            @JsonProperty("fqdn")
            private String fqdn;

            @JsonProperty("scheme")
            private String scheme;

            @JsonProperty("port")
            private Integer port;

            @JsonProperty("ipv4-address")
            private String ipv4Address;

            @JsonProperty("ipv6-address")
            private String ipv6Address;

            public String getFqdn()
            {
                return this.fqdn;
            }

            @JsonIgnore
            public String getIpAddress()
            {
                return isIpv6 ? this.ipv6Address : this.ipv4Address;
            }

            public String getIpv4Address()
            {
                return this.ipv4Address;
            }

            public String getIpv6Address()
            {
                return this.ipv6Address;
            }

            public String getName()
            {
                return this.name;
            }

            public Integer getPort()
            {
                return this.port;
            }

            public String getScheme()
            {
                return this.scheme;
            }

            public Sepp setFqdn(String fqdn)
            {
                this.fqdn = fqdn;
                return this;
            }

            public Sepp setIpv4Address(String ipv4Address)
            {
                this.ipv4Address = ipv4Address;
                return this;
            }

            public Sepp setIpv6Address(String ipv6Address)
            {
                this.ipv6Address = ipv6Address;
                return this;
            }

            public Sepp setName(String name)
            {
                this.name = name;
                return this;
            }

            public Sepp setPort(Integer port)
            {
                this.port = port;
                return this;
            }

            public Sepp setScheme(String scheme)
            {
                this.scheme = scheme;
                return this;
            }

            @Override
            public String toString()
            {
                try
                {
                    return json.writeValueAsString(this);
                }
                catch (JsonProcessingException e)
                {
                    return e.toString();
                }
            }
        }

        public static void main(String[] args) throws JsonMappingException, JsonProcessingException
        {
            final String s = "{\"loadTestMode\":{\"isEnabled\": false},\"ownDomain\": \"5g-bsf-eedstl\",\"api\": {\"nnrfNfManagement\":{\"disturbances\":[{\"delayInMillis\": 0,\"drop\": 0,\"additionalHeaders\":{\"h1\":[\"v11\",\"v12\"]}}]},\"nnrfNfDiscovery\": {\"validityPeriodInSecs\": 7200,\"disturbances\": [{\"delayInMillis\": 10},{},{\"doDrop\": true}]}},\"roamingPartner\": [{\"name\": \"rp-1\",\"sepp\": [{\"name\": \"sepp-1\",\"fqdn\": \"eric-seppsim-p.5g-bsf-eedbjhe\",\"scheme\": \"http\",\"port\": 80}]},{\"name\": \"rp-2\",\"sepp\": [{\"name\": \"sepp-1\",\"fqdn\": \"eric-seppsim-p.5g-bsf-emaekat\",\"scheme\": \"http\",\"port\": 80}]}]}";
            Configuration c = json.readValue(s, Configuration.class);
            MultiMap headers = MultiMap.caseInsensitiveMultiMap();
            c.getApi().getNnrfNfManagement().getNext().getAdditionalHeaders().forEach(headers::add);
            log.info("c={}", c);
            log.info("headers={}", headers);
        }

        @JsonProperty("loadTestMode")
        private LoadTestMode loadTestMode = null;

        @JsonProperty("ownDomain")
        private String ownDomain = "";

        @JsonProperty("ownIpAddress")
        private String ownIpAddress = "";

        @JsonProperty("ownRole")
        private Role ownRole = Role.SEPP_OR_NF;

        // A PLMN is a list of its PLMN IDs
        @JsonProperty("supportedPlmn")
        private List<List<PlmnId>> supportedPlmn = null;

        @JsonProperty("api")
        private Api api = new Api();

        @JsonProperty("roamingPartner")
        private List<RoamingPartner> roamingPartner = new ArrayList<>();

        @JsonIgnore
        public Sepp findSepp(final TargetApiRoot targetApiRoot)
        {
            for (RoamingPartner rp : this.getRoamingPartner())
            {
                for (Configuration.Sepp sepp : rp.getSepp())
                {
                    if (targetApiRoot.getHostName().isFqdn())
                    {
                        if (sepp.getFqdn() != null && sepp.getFqdn().endsWith(targetApiRoot.getHostName().getDomain()))
                            return sepp;
                    }
                    else if (targetApiRoot.getHostName().isIpv4())
                    {
                        if (sepp.getIpv4Address() != null && sepp.getIpv4Address().equals(targetApiRoot.getHostName().get()))
                            return sepp;
                    }
                    else if (targetApiRoot.getHostName().isIpv6())
                    {
                        if (sepp.getIpv6Address() != null && sepp.getIpv6Address().equals(targetApiRoot.getHostName().get()))
                            return sepp;
                    }
                }
            }

            return null;
        }

        public synchronized Api getApi()
        {
            return this.api;
        }

        public synchronized LoadTestMode getLoadTestMode()
        {
            return this.loadTestMode;
        }

        public synchronized String getOwnDomain()
        {
            return this.ownDomain;
        }

        public synchronized String getOwnIpAddress()
        {
            return this.ownIpAddress;
        }

        public synchronized Role getOwnRole()
        {
            return this.ownRole;
        }

        public synchronized List<RoamingPartner> getRoamingPartner()
        {
            return this.roamingPartner;
        }

        public synchronized List<List<PlmnId>> getSupportedPlmn()
        {
            return this.supportedPlmn;
        }

        public synchronized Configuration setApi(final Api api)
        {
            this.api = api;
            return this;
        }

        public synchronized Configuration setLoadTestMode(final LoadTestMode loadTestMode)
        {
            this.loadTestMode = loadTestMode;
            return this;
        }

        public synchronized Configuration setOwnDomain(final String domain)
        {
            this.ownDomain = domain;
            return this;
        }

        public synchronized Configuration setOwnIpAddress(final String ipAddress)
        {
            this.ownIpAddress = ipAddress;
            return this;
        }

        public synchronized Configuration setOwnRole(final Role ownRole)
        {
            this.ownRole = ownRole;
            return this;
        }

        public synchronized Configuration setRoamingPartner(final List<RoamingPartner> roamingPartner)
        {
            this.roamingPartner = roamingPartner;
            return this;
        }

        public synchronized Configuration setSupportedPlmn(final List<List<PlmnId>> supportedPlmn)
        {
            this.supportedPlmn = supportedPlmn;
            return this;
        }

        @Override
        public String toString()
        {
            try
            {
                return json.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                return e.toString();
            }
        }
    }

    public static class N32Handshake extends ApiHandler
    {
        public enum Operation
        {
            POST_EXCHANGE_CAPABILITY("PostExchangeCapability"),
            POST_EXCHANGE_PARAMS("PostExchangeParams"),
            POST_N32F_ERROR("PostN32fError"),
            POST_N32F_TERMINATE("PostN32fTerminate");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        /**
         * RFC 7518
         * 
         * <pre>
         *      5.1.  "enc" (Encryption Algorithm) Header Parameter Values for JWE
         *
         *         The table below is the set of "enc" (encryption algorithm) Header
         *         Parameter values that are defined by this specification for use with
         *         JWE.
         *
         *         +---------------+----------------------------------+----------------+
         *         | "enc" Param   | Content Encryption Algorithm     | Implementation |
         *         | Value         |                                  | Requirements   |
         *         +---------------+----------------------------------+----------------+
         *         | A128CBC-HS256 | AES_128_CBC_HMAC_SHA_256         | Required       |
         *         |               | authenticated encryption         |                |
         *         |               | algorithm, as defined in Section |                |
         *         |               | 5.2.3                            |                |
         *         | A192CBC-HS384 | AES_192_CBC_HMAC_SHA_384         | Optional       |
         *         |               | authenticated encryption         |                |
         *         |               | algorithm, as defined in Section |                |
         *         |               | 5.2.4                            |                |
         *         | A256CBC-HS512 | AES_256_CBC_HMAC_SHA_512         | Required       |
         *         |               | authenticated encryption         |                |
         *         |               | algorithm, as defined in Section |                |
         *         |               | 5.2.5                            |                |
         *         | A128GCM       | AES GCM using 128-bit key        | Recommended    |
         *         | A192GCM       | AES GCM using 192-bit key        | Optional       |
         *         | A256GCM       | AES GCM using 256-bit key        | Recommended    |
         *         +---------------+----------------------------------+----------------+
         * </pre>
         */
        private static final List<String> JWE_SUITES = List.of("A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512", "A128GCM", "A192GCM", "A256GCM");

        /**
         * RFC 7518
         * 
         * <pre>
         *      3.1.  "alg" (Algorithm) Header Parameter Values for JWS
         *
         *         The table below is the set of "alg" (algorithm) Header Parameter
         *         values defined by this specification for use with JWS, each of which
         *         is explained in more detail in the following sections:
         *
         *         +--------------+-------------------------------+--------------------+
         *         | "alg" Param  | Digital Signature or MAC      | Implementation     |
         *         | Value        | Algorithm                     | Requirements       |
         *         +--------------+-------------------------------+--------------------+
         *         | HS256        | HMAC using SHA-256            | Required           |
         *         | HS384        | HMAC using SHA-384            | Optional           |
         *         | HS512        | HMAC using SHA-512            | Optional           |
         *         | RS256        | RSASSA-PKCS1-v1_5 using       | Recommended        |
         *         |              | SHA-256                       |                    |
         *         | RS384        | RSASSA-PKCS1-v1_5 using       | Optional           |
         *         |              | SHA-384                       |                    |
         *         | RS512        | RSASSA-PKCS1-v1_5 using       | Optional           |
         *         |              | SHA-512                       |                    |
         *         | ES256        | ECDSA using P-256 and SHA-256 | Recommended+       |
         *         | ES384        | ECDSA using P-384 and SHA-384 | Optional           |
         *         | ES512        | ECDSA using P-521 and SHA-512 | Optional           |
         *         | PS256        | RSASSA-PSS using SHA-256 and  | Optional           |
         *         |              | MGF1 with SHA-256             |                    |
         *         | PS384        | RSASSA-PSS using SHA-384 and  | Optional           |
         *         |              | MGF1 with SHA-384             |                    |
         *         | PS512        | RSASSA-PSS using SHA-512 and  | Optional           |
         *         |              | MGF1 with SHA-512             |                    |
         *         | none         | No digital signature or MAC   | Optional           |
         *         |              | performed                     |                    |
         *         +--------------+-------------------------------+--------------------+
         * </pre>
         */
        private static final List<String> JWS_SUITES = List.of("HS256",
                                                               "HS384",
                                                               "HS512",
                                                               "RS256",
                                                               "RS384",
                                                               "RS512",
                                                               "ES256",
                                                               "ES384",
                                                               "ES512",
                                                               "PS256",
                                                               "PS384",
                                                               "PS512",
                                                               "none");

        public static N32Handshake createClient(final SeppSimulator owner)
        {
            return new N32Handshake(owner);
        }

        private static Url toUrl(final String hostAndPort,
                                 final String ip,
                                 final String path)
        {
            final int lastColon = hostAndPort.lastIndexOf(":");
            return new Url("https", hostAndPort.substring(0, lastColon), Integer.parseInt(hostAndPort.substring(lastColon + 1)), path, ip);
        }

        private Map<String /* sender */, Pair<SecNegotiateReqData, SecNegotiateRspData>> secNegotiations = new HashMap<>();

        private BiConsumer<RoutingContext, Event> handlePostExchangeCapability = (context,
                                                                                  event) ->
        {
            try
            {
                synchronized (this.secNegotiations)
                {
                    final String body = context.body().asString();
                    log.debug("body={}.", body);

                    final SecNegotiateReqData request = json.readValue(body, SecNegotiateReqData.class);
                    final String sender = request.getSender();

                    final Pair<SecNegotiateReqData, SecNegotiateRspData> secNegotiation = this.secNegotiations.get(sender);

                    if (secNegotiation != null && secNegotiation.getFirst().equals(request))
                    {
                        context.response()
                               .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                               .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                               .end(json.writeValueAsString(secNegotiation.getSecond()));

                        return;
                    }

                    // Evaluate the request data and generate the response with the negotiated data.

                    final SecNegotiateRspData response = new SecNegotiateRspData().sender(this.hostName + "." + this.owner.getConfiguration().getOwnDomain());

                    if (request.getSupportedSecCapabilityList().stream().filter(s -> s.equals("TLS")).count() != 0)
                    {
                        response.selectedSecCapability(SecurityCapability.TLS);

                        // 3GppSbiTargetApiRootSupported: This IE should be present and indicate that
                        // the 3gpp-Sbi-Target-apiRoot HTTP header is supported, if TLS security is
                        // negotiated for N32f message forwarding and the initiating SEPP indicated
                        // support of this header.

                        if (request.get3gppSbiTargetApiRootSupported())
                            response._3gppSbiTargetApiRootSupported(true);

                        // The SEPP shall select the PLMN from the list of supported PLMN(s) based on
                        // the received Target PLMN ID or PLMN specific fqdn used in the request and
                        // provide the selected PLMN's PLMN Id(s) in the plmnIdList.

                        // TODO: Below implementation is just an example of how it could possibly be
                        // done.
                        // The specific FQDN is assumed to look like this:
                        // sepp.<mcc>.<mnc>.de
                        // If the targetPlmnId is not present in the request, it is assumed that it is
                        // part of the request FQDN. Then it is extracted from there as the key to be
                        // looked up in the map of PLMNs which is created from the list of supported
                        // PLMN IDs.

                        final Map<PlmnId /* requestDomain */, List<PlmnId>> plmns = Optional.ofNullable(this.owner.getConfiguration().getSupportedPlmn())
                                                                                            .orElse(List.of(List.of(// Vodafone Germany
                                                                                                                    new PlmnId().mcc("262").mnc("02"),
                                                                                                                    new PlmnId().mcc("262").mnc("04"),
                                                                                                                    new PlmnId().mcc("262").mnc("09"),
                                                                                                                    new PlmnId().mcc("262").mnc("42"))))
                                                                                            .stream()
                                                                                            .flatMap(plmn -> Stream.of(plmn.stream()
                                                                                                                           .collect(Collectors.toMap(k -> new PlmnId().mcc(k.getMcc())
                                                                                                                                                                      .mnc(k.getMnc()),
                                                                                                                                                     v -> plmn))))
                                                                                            .reduce(new HashMap<>(),
                                                                                                    (result,
                                                                                                     element) ->
                                                                                                    {
                                                                                                        result.putAll(element);
                                                                                                        return result;
                                                                                                    });

                        PlmnId targetPlmnId = request.getTargetPlmnId();

                        if (targetPlmnId == null)
                        {
                            // The specific request FQDN is assumed to look like this: "sepp.<mcc>.<mnc>.de"
                            // Then the requestDomain is "<mcc>.<mnc>.de", and the targetPlmnId is made from
                            // its first and second token.

                            final String[] requestDomain = new HostName(new URL(context.request().absoluteURI()).getHost()).getDomain().split("[.]");

                            if (requestDomain.length == 3)
                                targetPlmnId = new PlmnId().mcc(requestDomain[0]).mnc(requestDomain[1]);
                        }

//                      if (targetPlmnId != null && plmns.containsKey(targetPlmnId))
//                      response.plmnIdList(plmns.get(targetPlmnId));

                        // DND-36603: Pre-process plmn-id lists In case mnc has only two digits, add the
                        // zero filler digit according in order to be filler agnostic during the list
                        // comparison only

                        if (targetPlmnId != null)
                        {
                            Map<PlmnId, List<PlmnId>> filledPlmns = new HashMap<>();
                            plmns.entrySet().stream().forEach(entry ->
                            {
                                if (entry.getKey().getMnc().length() == 2)
                                {
                                    PlmnId tmp = new PlmnId();
                                    tmp.setMcc(entry.getKey().getMcc());
                                    tmp.setMnc("0" + entry.getKey().getMnc());
                                    filledPlmns.put(tmp, entry.getValue());
                                }
                                else
                                    filledPlmns.put(entry.getKey(), entry.getValue());
                            });

                            PlmnId tmpTargetPlmnId = new PlmnId();
                            tmpTargetPlmnId.setMcc(targetPlmnId.getMcc());
                            if (targetPlmnId.getMnc().length() == 2)
                                tmpTargetPlmnId.setMnc("0" + targetPlmnId.getMnc());
                            else
                                tmpTargetPlmnId.setMnc(targetPlmnId.getMnc());

                            if (filledPlmns.containsKey(tmpTargetPlmnId))
                                response.plmnIdList(filledPlmns.get(tmpTargetPlmnId));
                        }

                        this.secNegotiations.put(sender, Pair.of(request, response));

                        context.response()
                               .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                               .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                               .end(json.writeValueAsString(response));
                    }
                    else
                    {
                        final ProblemDetails error = new ProblemDetails().title("Security capability handshake problem")
                                                                         .detail("Could not select TLS from supported security capbility list.")
                                                                         .cause("NEGOTIATION_NOT_ALLOWED")
                                                                         .status(HttpResponseStatus.FORBIDDEN.code());

                        context.response()
                               .setStatusCode(event.setResponse(HttpResponseStatus.FORBIDDEN).getResponse().getResultCode())
                               .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_PROBLEM_JSON)
                               .end(json.writeValueAsString(error));
                    }
                }
            }
            catch (

            Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handlePostExchangeParams = (context,
                                                                              event) ->
        {
            try
            {
                final String body = context.body().asString();
                log.debug("body={}.", body);

                final SecParamExchReqData request = json.readValue(body, SecParamExchReqData.class);

                final String n32fContextId = request.getN32fContextId();
                final List<IpxProviderSecInfo> ipxProviderSecInfoList = request.getIpxProviderSecInfoList();
                final List<String> jweCipherSuiteList = request.getJweCipherSuiteList();
                final List<String> jwsCipherSuiteList = request.getJwsCipherSuiteList();
                final ProtectionPolicy protectionPolicyInfo = request.getProtectionPolicyInfo();
                final String sender = request.getSender();

                // Evaluate the request data and generate the response with the negotiated data.
                // FIXME: For the time being a static response is generated.

                final SecParamExchRspData response = new SecParamExchRspData().n32fContextId(n32fContextId)
                                                                              .sender(this.hostName + "." + this.owner.getConfiguration().getOwnDomain());

                if (jweCipherSuiteList != null)
                {
                    final Optional<String> selectedCipherSuite = JWE_SUITES.stream()
                                                                           .filter(s -> jweCipherSuiteList.stream().anyMatch(ss -> ss.equals(s)))
                                                                           .findFirst();

                    if (selectedCipherSuite.isPresent())
                    {
                        response.selectedJweCipherSuite(selectedCipherSuite.get());
                    }
                    else
                    {
                        final ProblemDetails error = new ProblemDetails().title("Parameter exchange handshake problem")
                                                                         .detail("No common JWE cipher suite found in supported JWE cipher suite list.")
                                                                         .cause("REQUESTED_PARAM_MISMATCH")
                                                                         .status(HttpResponseStatus.CONFLICT.code());

                        context.response()
                               .setStatusCode(event.setResponse(HttpResponseStatus.CONFLICT).getResponse().getResultCode())
                               .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_PROBLEM_JSON)
                               .end(json.writeValueAsString(error));

                        return;
                    }
                }

                if (jwsCipherSuiteList != null)
                {
                    final Optional<String> selectedCipherSuite = JWS_SUITES.stream()
                                                                           .filter(s -> jwsCipherSuiteList.stream().anyMatch(ss -> ss.equals(s)))
                                                                           .findFirst();

                    if (selectedCipherSuite.isPresent())
                    {
                        response.selectedJwsCipherSuite(selectedCipherSuite.get());
                    }
                    else
                    {
                        final ProblemDetails error = new ProblemDetails().title("Parameter exchange handshake problem")
                                                                         .detail("No common JWS cipher suite found in supported JWS cipher suite list.")
                                                                         .cause("REQUESTED_PARAM_MISMATCH") // See TS 29.500, table 5.2.7.2-1
                                                                         .status(HttpResponseStatus.CONFLICT.code());

                        context.response()
                               .setStatusCode(event.setResponse(HttpResponseStatus.CONFLICT).getResponse().getResultCode())
                               .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_PROBLEM_JSON)
                               .end(json.writeValueAsString(error));

                        return;
                    }
                }

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(response));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handlePostN32fError = (context,
                                                                         event) ->
        {
            try
            {
                final String body = context.body().asString();
                log.debug("body={}.", body);

                final N32fErrorInfo request = json.readValue(body, N32fErrorInfo.class);

                if ((request.getFailedModificationList() == null || request.getFailedModificationList().isEmpty())
                    && (request.getN32fErrorType().equals(N32fErrorType.INTEGRITY_CHECK_ON_MODIFICATIONS_FAILED)
                        || request.getN32fErrorType().equals(N32fErrorType.MODIFICATIONS_INSTRUCTIONS_FAILED)))
                {
                    final ProblemDetails problem = new ProblemDetails().title("Inconsistent request data")
                                                                       .status(HttpResponseStatus.BAD_REQUEST.code())
                                                                       .detail("IE 'failedModificationList' shall be present for error types 'INTEGRITY_CHECK_ON_MODIFICATIONS_FAILED' or 'MODIFICATIONS_INSTRUCTIONS_FAILED'.")
                                                                       .cause("MANDATAORY_IE_MISSING") // See
                                                                                                       // TS
                                                                                                       // 29.500,
                                                                                                       // table
                                                                                                       // 5.2.7.2-1
                                                                       .addInvalidParamsItem(new InvalidParam().param("failedModificationList")
                                                                                                               .reason("Missing IE"));

                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.BAD_REQUEST).getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_PROBLEM_JSON)
                           .end(json.writeValueAsString(problem));

                    return;
                }

                context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handlePostN32fTerminate = (context,
                                                                             event) ->
        {
            try
            {
                final String body = context.body().asString();
                log.debug("body={}.", body);

                final N32fContextInfo request = json.readValue(body, N32fContextInfo.class);

                final String n32fContextId = request.getN32fContextId();

                final N32fContextInfo response = new N32fContextInfo().n32fContextId(n32fContextId);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(response));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        public N32Handshake(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.POST_EXCHANGE_CAPABILITY.value(), this.handlePostExchangeCapability);
            this.getHandlerByOperationId().put(Operation.POST_EXCHANGE_PARAMS.value(), this.handlePostExchangeParams);
            this.getHandlerByOperationId().put(Operation.POST_N32F_ERROR.value(), this.handlePostN32fError);
            this.getHandlerByOperationId().put(Operation.POST_N32F_TERMINATE.value(), this.handlePostN32fTerminate);
        }

        public Single<HttpResponse<Buffer>> customN32cClientRequest(final int port,
                                                                    final String ip,
                                                                    final String hostAndPort,
                                                                    final HttpMethod httpMethod,
                                                                    final MultiMap headerMap,
                                                                    final SecNegotiateReqData body)
        {
            // Attribute 'sender' is empty -> replace with actual value:

            if (body.getSender() != null)
            {
                if (body.getSender().isEmpty())
                    body.sender(this.hostName + "." + this.owner.getConfiguration().getOwnDomain());

                final Url url = toUrl(hostAndPort, ip, "/n32c-handshake/v1/exchange-capability");

                try
                {
                    log.info("SeppSim body post1:{}", json.writeValueAsString(body));
                    return this.client.requestAbs(httpMethod, SocketAddress.inetSocketAddress(port, ip), url.getUrl().toString())
                                      .putHeaders(headerMap)
                                      .rxSendJsonObject(new JsonObject(json.writeValueAsString(body)));
                }
                catch (JsonProcessingException e)
                {
                    return Single.error(e);
                }

            }
            else
            {
                final Url url = toUrl(hostAndPort, ip, "/n32c-handshake/v1/exchange-capability");

                try
                {
                    log.info("SeppSim body post2:{}", json.writeValueAsString(body));
                    return this.client.requestAbs(httpMethod, SocketAddress.inetSocketAddress(port, ip), url.getUrl().toString())
                                      .putHeaders(headerMap)
                                      .rxSend();
                }
                catch (JsonProcessingException e)
                {
                    return Single.error(e);
                }
            }
        }

        public Single<HttpResponse<Buffer>> postExchangeCapabilityRequest(final String hostAndPort,
                                                                          final String ip,
                                                                          final SecNegotiateReqData body)
        {
            // Attribute 'sender' is empty -> replace with actual value:

            if (body.getSender() != null && body.getSender().isEmpty())
                body.sender(this.hostName + "." + this.owner.getConfiguration().getOwnDomain());

            final Url url = toUrl(hostAndPort, ip, "/n32c-handshake/v1/exchange-capability");

            try
            {
                log.info("SeppSim body post:{}", json.writeValueAsString(body));
                return this.client.requestAbs(HttpMethod.POST, url.getAddr(), url.getUrl().toString())
                                  .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                                  .rxSendJsonObject(new JsonObject(json.writeValueAsString(body)));
            }
            catch (JsonProcessingException e)
            {
                return Single.error(e);
            }
        }

        public Single<HttpResponse<Buffer>> postExchangeParamsRequest(final String hostAndPort,
                                                                      final String ip,
                                                                      final SecParamExchReqData body)
        {
            // Attribute 'sender' is empty -> replace with actual value:

            if (body.getSender() != null && body.getSender().isEmpty())
                body.sender(this.hostName + "." + this.owner.getConfiguration().getOwnDomain());

            final Url url = toUrl(hostAndPort, ip, "/n32c-handshake/v1/exchange-params");

            try
            {
                return this.client.requestAbs(HttpMethod.POST, url.getAddr(), url.getUrl().toString())
                                  .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                                  .rxSendJsonObject(new JsonObject(json.writeValueAsString(body)));
            }
            catch (JsonProcessingException e)
            {
                return Single.error(e);
            }
        }

        public Single<HttpResponse<Buffer>> postN32fErrorRequest(final String hostAndPort,
                                                                 final String ip,
                                                                 final N32fErrorInfo body)
        {
            final Url url = toUrl(hostAndPort, ip, "/n32c-handshake/v1/n32f-error");

            try
            {
                return this.client.requestAbs(HttpMethod.POST, url.getAddr(), url.getUrl().toString())
                                  .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                                  .rxSendJsonObject(new JsonObject(json.writeValueAsString(body)));
            }
            catch (JsonProcessingException e)
            {
                return Single.error(e);
            }
        }

        public Single<HttpResponse<Buffer>> postN32fTerminateRequest(final String hostAndPort,
                                                                     final String ip,
                                                                     final N32fContextInfo body)
        {
            final Url url = toUrl(hostAndPort, ip, "/n32c-handshake/v1/n32f-terminate");

            try
            {
                return this.client.requestAbs(HttpMethod.POST, url.getAddr(), url.getUrl().toString())
                                  .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                                  .rxSendJsonObject(new JsonObject(json.writeValueAsString(body)));
            }
            catch (JsonProcessingException e)
            {
                return Single.error(e);
            }
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getN32Handshake();
        }
    }

    public static class NamfCommunication extends ApiHandler
    {
        public enum Operation
        {
            AMF_STATUS_CHANGE_SUBSCRIBE("AMFStatusChangeSubscribe"),
            AMF_STATUS_CHANGE_SUBSCRIBE_MODIFY("AMFStatusChangeSubscribeModfy"),
            AMF_STATUS_CHANGE_UNSUBSCRIBE("AMFStatusChangeUnSubscribe"),
            EBI_ASSIGNMENT("EBIAssignment"),
            N1_N2_MESSAGE_SUBSCRIBE("N1N2MessageSubscribe"),
            N1_N2_MESSAGE_TRANSFER("N1N2MessageTransfer"),
            N1_N2_MESSAGE_UNSUBSCRIBE("N1N2MessageUnSubscribe"),
            NON_UE_N2_INFO_SUBSCRIBE("NonUeN2InfoSubscribe"),
            NON_UE_N2_INFO_UNSUBSCRIBE("NonUeN2InfoUnSubscribe"),
            NON_UE_N2_MESSAGE_TRANSFER("NonUeN2MessageTransfer"),
            REGISTRATION_STATUS_UPDATE("RegistrationStatusUpdate"),
            RELEASE_UE_CONTEXT("ReleaseUEContext");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private static final String PAR_SUBSCRIPTION_ID = "subscriptionId";

        private final Map<String, com.ericsson.cnal.openapi.r17.ts29518.namf.communication.SubscriptionData> subscriptions = new ConcurrentHashMap<>();
        private final AtomicInteger subcriptionCnt = new AtomicInteger();

        private BiConsumer<RoutingContext, Event> handleAMFStatusChangeSubscribe = (context,
                                                                                    event) ->
        {
            try
            {
                final com.ericsson.cnal.openapi.r17.ts29518.namf.communication.SubscriptionData subscriptionData = json.readValue(context.body().asString(),
                                                                                                                                  com.ericsson.cnal.openapi.r17.ts29518.namf.communication.SubscriptionData.class);
                final String subscriptionId = "subscription-" + this.subcriptionCnt.incrementAndGet();

                this.subscriptions.put(subscriptionId, subscriptionData);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + subscriptionId)
                       .end(json.writeValueAsString(subscriptionData));
            }
            catch (Exception e)
            {
                log.error("Caught exception", e);
            }
        };

        private BiConsumer<RoutingContext, Event> handleAMFStatusChangeSubscribeModify = (context,
                                                                                          event) ->
        {
            try
            {
                final com.ericsson.cnal.openapi.r17.ts29518.namf.communication.SubscriptionData subscriptionData = json.readValue(context.body().asString(),
                                                                                                                                  com.ericsson.cnal.openapi.r17.ts29518.namf.communication.SubscriptionData.class);
                final String subscriptionId = context.request().getParam(PAR_SUBSCRIPTION_ID, "subscription-0");

                if (this.subscriptions.replace(subscriptionId, subscriptionData) == null)
                {
                    context.response().setStatusCode(event.setResponse(HttpResponseStatus.NOT_FOUND).getResponse().getResultCode()).end();
                    return;
                }

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(subscriptionData));
            }
            catch (Exception e)
            {
                log.error("Caught exception", e);
            }
        };

        private BiConsumer<RoutingContext, Event> handleAMFStatusChangeUnsubscribe = (context,
                                                                                      event) ->
        {
            this.subscriptions.remove(context.request().getParam(PAR_SUBSCRIPTION_ID, "subscription-0"));
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleEBIAssignment = (context,
                                                                         event) ->
        {
            try
            {
                final AssignedEbiData assignedEbiData = new AssignedEbiData();
                final List<Integer> releasedEbiList = new ArrayList<>();
                final List<EbiArpMapping> assignedEbiList = new ArrayList<>();
                final EbiArpMapping ebiArpMapping = new EbiArpMapping();
                final Arp arp = new Arp();

                arp.setPreemptCap(PreemptionCapability.MAY_PREEMPT);
                arp.setPreemptVuln(PreemptionVulnerability.PREEMPTABLE);
                arp.setPriorityLevel(1);
                releasedEbiList.add(10);
                ebiArpMapping.setEpsBearerId(1);
                ebiArpMapping.setArp(arp);
                assignedEbiList.add(ebiArpMapping);
                assignedEbiData.setAssignedEbiList(assignedEbiList);
                assignedEbiData.setPduSessionId(20);
                assignedEbiData.setReleasedEbiList(releasedEbiList);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(assignedEbiData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleN1N2MessageSubscribe = (context,
                                                                                event) ->
        {
            try
            {
                final UeN1N2InfoSubscriptionCreatedData ueN1N2InfoSubscriptionCreatedData = new UeN1N2InfoSubscriptionCreatedData();

                ueN1N2InfoSubscriptionCreatedData.setN1n2NotifySubscriptionId("NotifySubscriptionId-0");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + ueN1N2InfoSubscriptionCreatedData.getN1n2NotifySubscriptionId())
                       .end(json.writeValueAsString(ueN1N2InfoSubscriptionCreatedData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleN1N2MessageTransfer = (context,
                                                                               event) ->
        {
            try
            {
                final N1N2MessageTransferRspData n1n2MessageTransferRspData = new N1N2MessageTransferRspData();

                n1n2MessageTransferRspData.setCause(N1N2MessageTransferCause.ATTEMPTING_TO_REACH_UE);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(n1n2MessageTransferRspData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleN1N2MessageUnsubscribe = (context,
                                                                                  event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleNonUeN2InfoSubscribe = (context,
                                                                                event) ->
        {
            try
            {
                final NonUeN2InfoSubscriptionCreatedData nonUeN2InfoSubscriptionCreatedData = new NonUeN2InfoSubscriptionCreatedData();

                nonUeN2InfoSubscriptionCreatedData.setN2NotifySubscriptionId("N2NotifySubscriptionId-0");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + nonUeN2InfoSubscriptionCreatedData.getN2NotifySubscriptionId())
                       .end(json.writeValueAsString(nonUeN2InfoSubscriptionCreatedData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleNonUeN2InfoUnsubscribe = (context,
                                                                                  event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleNonUeN2MessageTransfer = (context,
                                                                                  event) ->
        {
            try
            {
                final N2InformationTransferRspData n22MessageTransferRspData = new N2InformationTransferRspData();

                n22MessageTransferRspData.setResult(N2InformationTransferResult.N2_INFO_TRANSFER_INITIATED);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(n22MessageTransferRspData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleRegistrationStatusUpdate = (context,
                                                                                    event) ->
        {
            try
            {
                final UeRegStatusUpdateRspData ueRegStatusUpdateRspData = new UeRegStatusUpdateRspData();

                ueRegStatusUpdateRspData.setRegStatusTransferComplete(true);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(ueRegStatusUpdateRspData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleReleaseUeContext = (context,
                                                                            event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        public NamfCommunication(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.AMF_STATUS_CHANGE_SUBSCRIBE.value(), this.handleAMFStatusChangeSubscribe);
            this.getHandlerByOperationId().put(Operation.AMF_STATUS_CHANGE_SUBSCRIBE_MODIFY.value(), this.handleAMFStatusChangeSubscribeModify);
            this.getHandlerByOperationId().put(Operation.AMF_STATUS_CHANGE_UNSUBSCRIBE.value(), this.handleAMFStatusChangeUnsubscribe);
            this.getHandlerByOperationId().put(Operation.EBI_ASSIGNMENT.value(), this.handleEBIAssignment);
            this.getHandlerByOperationId().put(Operation.N1_N2_MESSAGE_SUBSCRIBE.value(), this.handleN1N2MessageSubscribe);
            this.getHandlerByOperationId().put(Operation.N1_N2_MESSAGE_TRANSFER.value(), this.handleN1N2MessageTransfer);
            this.getHandlerByOperationId().put(Operation.N1_N2_MESSAGE_UNSUBSCRIBE.value(), this.handleN1N2MessageUnsubscribe);
            this.getHandlerByOperationId().put(Operation.NON_UE_N2_INFO_SUBSCRIBE.value(), this.handleNonUeN2InfoSubscribe);
            this.getHandlerByOperationId().put(Operation.NON_UE_N2_INFO_UNSUBSCRIBE.value(), this.handleNonUeN2InfoUnsubscribe);
            this.getHandlerByOperationId().put(Operation.NON_UE_N2_MESSAGE_TRANSFER.value(), this.handleNonUeN2MessageTransfer);
            this.getHandlerByOperationId().put(Operation.REGISTRATION_STATUS_UPDATE.value(), this.handleRegistrationStatusUpdate);
            this.getHandlerByOperationId().put(Operation.RELEASE_UE_CONTEXT.value(), this.handleReleaseUeContext);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNamfCommunication();
        }
    }

    public static class NamfEventExposure extends ApiHandler
    {

        public enum Operation
        {
            CREATE_SUBSCRIPTION("CreateSubscription"),
            MODIFY_SUBSCRIPTION("ModifySubscription"),
            DELETE_SUBSCRIPTION("DeleteSubscription");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private static final String PAR_SUBSCRIPTION_ID = "subscriptionId";

        private final Map<String, AmfEventSubscription> subscriptions = new ConcurrentHashMap<>();
        private final AtomicInteger subcriptionCnt = new AtomicInteger();

        private BiConsumer<RoutingContext, Event> handleCreateSubscription = (context,
                                                                              event) ->
        {
            try
            {
                final AmfEventSubscription subscription = json.readValue(context.body().asString(), AmfEventSubscription.class);
                final AmfCreatedEventSubscription subscriptionData = new AmfCreatedEventSubscription();
                final String subscriptionId = "subscription-" + this.subcriptionCnt.incrementAndGet();

                subscriptionData.subscription(subscription).subscriptionId(subscriptionId);

                this.subscriptions.put(subscriptionId, subscription);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + subscriptionId)
                       .end(json.writeValueAsString(subscriptionData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleDeleteSubscription = (context,
                                                                              event) ->
        {
            this.subscriptions.remove(context.request().getParam(PAR_SUBSCRIPTION_ID, "subscription-0"));
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleModifySubscription = (context,
                                                                              event) ->
        {
            try
            {
                final AmfEventSubscription subscription = this.subscriptions.get(context.request().getParam(PAR_SUBSCRIPTION_ID, "subscription-0"));

                if (subscription == null)
                {
                    context.response().setStatusCode(event.setResponse(HttpResponseStatus.NOT_FOUND).getResponse().getResultCode()).end();
                    return;
                }

                final AmfUpdatedEventSubscription subscriptionData = new AmfUpdatedEventSubscription().subscription(subscription);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(subscriptionData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        public NamfEventExposure(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.CREATE_SUBSCRIPTION.value(), this.handleCreateSubscription);
            this.getHandlerByOperationId().put(Operation.MODIFY_SUBSCRIPTION.value(), this.handleModifySubscription);
            this.getHandlerByOperationId().put(Operation.DELETE_SUBSCRIPTION.value(), this.handleDeleteSubscription);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNamfEventExposure();
        }
    }

    public static class NapiNotifications extends ApiHandler
    {
        public enum Operation
        {
            PROCESS_NOTIFICATION("ProcessNotification");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handleProcessNotification = (context,
                                                                               event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        public NapiNotifications(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.PROCESS_NOTIFICATION.value(), this.handleProcessNotification);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNapiNotifications();
        }
    }

    public static class NapiTest extends ApiHandler
    {
        public enum Operation
        {
            MESSAGE_SCREENING_MIRROR_REQUEST_BODY("MirrorRequestBody");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handleMirrorRequestBody = (context,
                                                                             event) ->
        {
            final String body = context.body().asString();
            final HttpServerResponse response = context.response().setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode());

            if (body == null)
                response.end();
            else
                response.putHeader(HD_CONTENT_TYPE, context.request().getHeader("content-type")).end(Buffer.buffer(body.getBytes()));
        };

        public NapiTest(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.MESSAGE_SCREENING_MIRROR_REQUEST_BODY.value(), this.handleMirrorRequestBody);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNapiTest();
        }
    }

    public static class NausfUeAuthentication extends ApiHandler
    {
        public enum Operation
        {
            X_AUTHENTICATIONS("X_Authentications"),
            X_CREATE_UE_AUTH_CONTEXT("X_CreateUeAuthContext"),
            DELETE_5G_AKA_AUTHENTICATION_RESULT("Delete5gAkaAuthenticationResult"),
            X_PUT_5G_AKA_CONFIRMATION("X_Put5gAkaConfirmation"),
            DELETE_EAP_AUTHENTICATION_RESULT("DeleteEapAuthenticationResult"),
            X_DEREGISTER("X_Deregister"),
            EAP_AUTH_METHOD("EapAuthMethod");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handleAuthentications = (context,
                                                                           event) ->
        {
            final String body = context.body().asString();
            log.debug("body={}.", body);

            try
            {
                final com.ericsson.cnal.openapi.r17.ts29509.nausf.ueauthentication.RgAuthCtx authCtx = new com.ericsson.cnal.openapi.r17.ts29509.nausf.ueauthentication.RgAuthCtx();
                authCtx.setAuthResult(AuthResult.SUCCESS);
                authCtx.setAuthInd(true);
                authCtx.setSupi("imsi-123456789");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + authCtx.getSupi())
                       .end(json.writeValueAsString(authCtx));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleCreateUeAuthContext = (context,
                                                                               event) ->
        {
            final String body = context.body().asString();
            log.debug("body={}.", body);

            try
            {
                final AuthenticationInfo info = json.readValue(body, AuthenticationInfo.class);
                final UEAuthenticationCtx ctx = new UEAuthenticationCtx();
                ctx.setServingNetworkName(info.getServingNetworkName());
                ctx.setAuthType(com.ericsson.cnal.openapi.r17.ts29509.nausf.ueauthentication.AuthType._5G_AKA);
                LinksValueSchema link = new LinksValueSchema();

                String uri = context.request().absoluteURI() + "/emaekat";

                link.setHref(uri);
                Map<String, LinksValueSchema> links = new HashMap<>();
                links.put("5g-aka", link);
                ctx.setLinks(links);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_3GPP_HAL_JSON)
                       .putHeader(HD_LOCATION, uri)
                       .end(json.writeValueAsString(ctx));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleDelete5gAkaAuthenticationResult = (context,
                                                                                           event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handlePut5gAkaConfirmation = (context,
                                                                                event) ->
        {
            try
            {
                final ConfirmationDataResponse confirmationDataResponse = new ConfirmationDataResponse();

                confirmationDataResponse.setAuthResult(AuthResult.SUCCESS);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_3GPP_HAL_JSON)
                       .end(json.writeValueAsString(confirmationDataResponse));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleDeleteEapAuthenticationResult = (context,
                                                                                         event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleDeregister = (context,
                                                                      event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleEapAuthMethod = (context,
                                                                         event) ->
        {
            try
            {
                final String body = context.body().asString();
                log.debug("body={}.", body);

                var eapSession = new EapSession();
                eapSession.setEapPayload("TESTEAPPAYLOAD".getBytes());
                eapSession.setSupi("fde21b56-2e47-49dd-9a1f-2769e5a8f45d");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(eapSession));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        public NausfUeAuthentication(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.X_AUTHENTICATIONS.value(), this.handleAuthentications);
            this.getHandlerByOperationId().put(Operation.X_CREATE_UE_AUTH_CONTEXT.value(), this.handleCreateUeAuthContext);
            this.getHandlerByOperationId().put(Operation.DELETE_5G_AKA_AUTHENTICATION_RESULT.value(), this.handleDelete5gAkaAuthenticationResult);
            this.getHandlerByOperationId().put(Operation.X_PUT_5G_AKA_CONFIRMATION.value(), this.handlePut5gAkaConfirmation);
            this.getHandlerByOperationId().put(Operation.DELETE_EAP_AUTHENTICATION_RESULT.value(), this.handleDeleteEapAuthenticationResult);
            this.getHandlerByOperationId().put(Operation.X_DEREGISTER.value(), this.handleDeregister);
            this.getHandlerByOperationId().put(Operation.EAP_AUTH_METHOD.value(), this.handleEapAuthMethod);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNudmUeAuthentication();
        }
    }

    @JsonPropertyOrder({ "nfInstance" })
    public static class NfInstance
    {
        public static class Pool
        {
            @JsonProperty("pool")
            private final Map<String, NfInstance> pool = new ConcurrentHashMap<>();

            public void clear()
            {
                this.pool.values().forEach(instance ->
                {
                    instance.getStatistics().clear();
                    instance.getContexts().clear();
                });
            }

            public NfInstance get(String nfInstanceId)
            {
                if (nfInstanceId == null)
                    nfInstanceId = DEFAULT_NF_INSTANCE_ID;

                if (this.pool.containsKey(nfInstanceId))
                    return this.pool.get(nfInstanceId);

                NfInstance value = new NfInstance(nfInstanceId);
                NfInstance prev = this.pool.putIfAbsent(nfInstanceId, value);
                return prev != null ? prev : value;
            }

            public Iterator<Entry<String, NfInstance>> iterator()
            {
                return this.pool.entrySet().iterator();
            }

            /**
             * Returns a JSON representation of this object.
             */
            @Override
            public String toString()
            {
                try
                {
                    return json.writeValueAsString(this);
                }
                catch (JsonProcessingException e)
                {
                    return e.toString();
                }
            }
        }

        private static final String DEFAULT_NF_INSTANCE_ID = "<default>";

        @JsonProperty("statistics")
        private final Statistics statistics;

        @JsonIgnore
        private List<RoutingContext> contexts;

        public NfInstance(final String nfInstanceId)
        {
            this.statistics = new Statistics(nfInstanceId);
            this.contexts = new ArrayList<>();
        }

        public synchronized void addContext(RoutingContext context)
        {
            if (this.contexts.size() > 32)
            {
                log.warn("Already 32 contexts in list. Current context not added: {}", context);
                return;
            }

            final String body = context.body().asString();

            if (body != null)
                context.put(DataIndex.REQUEST_BODY.name(), body);

            this.contexts.add(context);
        }

        public synchronized Map<String, Set<String>> getAllRequestHeaders()
        {
            return this.contexts.stream()
                                .flatMap(rc -> rc.request().headers().entries().stream())
                                .collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toSet())));
        }

        public synchronized Map<String, Set<String>> getAllResponseHeaders()
        {
            return this.contexts.stream()
                                .flatMap(rc -> rc.response().headers().entries().stream())
                                .collect(Collectors.groupingBy(Entry::getKey, Collectors.mapping(Entry::getValue, Collectors.toSet())));
        }

        public synchronized List<String> getAuthorityHeader()
        {
            return this.contexts.stream().map(c -> c.request().authority().toString()).collect(Collectors.toList());
        }

        public synchronized List<RoutingContext> getContexts()
        {
            return this.contexts;
        }

        public synchronized List<String> getRequestBody()
        {
            return this.contexts.stream()
                                .map(rc -> Optional.ofNullable((String) rc.get(DataIndex.REQUEST_BODY.name())))
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .collect(Collectors.toList());
        }

        public synchronized List<String> getRequestHeader(String headerName)
        {
            return this.contexts.stream()
                                .filter(rc -> rc.request().getHeader(headerName) != null)
                                .flatMap(rc -> rc.request().headers().getAll(headerName).stream())
                                .collect(Collectors.toList());
        }

        public synchronized List<String> getResponseHeader(String headerName)
        {
            return this.contexts.stream().flatMap(rc -> rc.response().headers().getAll(headerName).stream()).collect(Collectors.toList());
        }

        public Statistics getStatistics()
        {
            return this.statistics;
        }
    }

    public static class NnrfBootstrapping extends ApiHandler
    {
        public enum Operation
        {
            BOOTSTRAPPING_INFO_REQUEST("BootstrappingInfoRequest");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handleBootstrappingInfoRequest = (context,
                                                                                    event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());

            try
            {
                final String baseUri = (owner.webServerExtTls != null ? owner.webServerExtTls.get(0) : owner.webServerExt.get(0)).baseUri().toString();

                final BootstrappingInfo result = new BootstrappingInfo().status(Status.OPERATIVE)
                                                                        .links(Map.of("self",
                                                                                      new LinksValueSchema().href(new StringBuilder(baseUri).append("/bootstrapping")
                                                                                                                                            .toString())))
                                                                        .nrfFeatures(Map.of())
                                                                        .nrfInstanceId(UUID.randomUUID())
                                                                        .nrfSetId("set12.nrfset.5gc.mnc012.mcc345")
                                                                        .oauth2Required(Map.of());

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_3GPP_HAL_JSON)
                       .putHeader(HD_CACHE_CONTROL, " max-age=86400")
                       .putHeader(HD_ETAG, UUID.randomUUID().toString())
                       .end(json.writeValueAsString(result));
            }
            catch (final Exception e)
            {
                replyWithError(context,
                               event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                 "Error processing " + operationId + " request. Cause: " + e.toString()),
                               null,
                               null,
                               null);
            }
        };

        public NnrfBootstrapping(final SeppSimulator owner)
        {
            super(owner);

            this.getHandlerByOperationId().put(Operation.BOOTSTRAPPING_INFO_REQUEST.value, this.handleBootstrappingInfoRequest);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNnrfBootstrapping();
        }
    }

    public static class NnrfNfDiscovery extends ApiHandler
    {
        public enum Operation
        {
            NF_INSTANCES_SEARCH("SearchNFInstances"),
            RETRIEVE_STORED_SEARCH("RetrieveStoredSearch"),
            RETRIEVE_COMPLETE_SEARCH("RetrieveCompleteSearch");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handleSearchNfInstances = (context,
                                                                             event) ->
        {
            try
            {
                final SearchResult result = new SearchResult();
                result.setValidityPeriod(60 * 60 * 24); // One
                                                        // day

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(result));
            }
            catch (JsonProcessingException e)
            {
                final String msg = "Error generating response. Cause: " + e.getMessage();
                log.error(msg);
                replyWithError(context, event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg), null, null, null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleRetrieveStoredSearch = (context,
                                                                                event) ->
        {
            try
            {
                StoredSearchResult storedSearchResult = new StoredSearchResult();
                List<com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile> nfInstances = new ArrayList<>();
                com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile nfProfile = new com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile();

                nfProfile.setNfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                nfProfile.setNfType(NFType.NRF);
                nfProfile.setNfStatus(NFStatus.REGISTERED);

                nfInstances.add(nfProfile);
                storedSearchResult.setNfInstances(nfInstances);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(storedSearchResult));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleRetrieveCompleteSearch = (context,
                                                                                  event) ->
        {
            try
            {
                StoredSearchResult storedSearchResult = new StoredSearchResult();
                List<com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile> nfInstances = new ArrayList<>();
                com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile nfProfile = new com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfdiscovery.NFProfile();

                nfProfile.setNfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                nfProfile.setNfType(NFType.NRF);
                nfProfile.setNfStatus(NFStatus.REGISTERED);

                nfInstances.add(nfProfile);
                storedSearchResult.setNfInstances(nfInstances);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(storedSearchResult));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        public NnrfNfDiscovery(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.NF_INSTANCES_SEARCH.value(), this.handleSearchNfInstances);
            this.getHandlerByOperationId().put(Operation.RETRIEVE_STORED_SEARCH.value(), this.handleRetrieveStoredSearch);
            this.getHandlerByOperationId().put(Operation.RETRIEVE_COMPLETE_SEARCH.value(), this.handleRetrieveCompleteSearch);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNnrfNfDiscovery();
        }
    }

    public static class NnrfNfManagement extends ApiHandler
    {
        public enum Operation
        {
            NF_INSTANCE_DEREGISTER("DeregisterNFInstance"),
            NF_INSTANCE_REGISTER("RegisterNFInstance"),
            NF_INSTANCE_UPDATE("UpdateNFInstance"),
            NF_INSTANCE_GET("GetNFInstance"),
            NF_INSTANCES_GET("GetNFInstances"),
            SUBSCRIPTION_CREATE("CreateSubscription"),
            SUBSCRIPTION_UPDATE("UpdateSubscription"),
            SUBSCRIPTION_REMOVE("RemoveSubscription");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private static final String JP_NF_INSTANCE_ID = "/nfInstanceId";
        private static final String JP_NF_SET_ID_LIST = "/nfSetIdList";
        private static final String JP_NF_TYPE = "/nfType";

        private BiConsumer<RoutingContext, Event> handleNfInstanceDeregister = (context,
                                                                                event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleNfInstanceGet = (context,
                                                                         event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final String nfInstanceId = context.request().getParam("nfInstanceID");

            try
            {
                final NFProfile nfProfile = new NFProfile().nfInstanceId(UUID.fromString(nfInstanceId));

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(nfProfile));
            }
            catch (final Exception e)
            {
                replyWithError(context,
                               event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                 "Error processing " + operationId + " request. Cause: " + e.toString()),
                               nfInstanceId != null ? nfInstanceId : null,
                               null,
                               null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleNfInstancesGet = (context,
                                                                          event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());

            String nfType = null;

            try
            {
                final String paramNfType = context.request().getParam("nf-type");
                final String paramLimit = context.request().getParam("limit");

                if (paramNfType != null)
                {
                    try
                    {
                        nfType = paramNfType;
                    }
                    catch (IllegalArgumentException e)
                    {
                        replyWithError(context,
                                       event.setResponse(HttpResponseStatus.BAD_REQUEST, "Invalid parameter value: " + e.toString()),
                                       null,
                                       null,
                                       "nf-type");
                        return;
                    }
                }

                Integer limit = null;

                if (paramLimit != null && !paramLimit.isEmpty())
                    limit = Integer.valueOf(paramLimit);

                final Set<NFProfile> list = Set.of();
                final List<Link> links = new ArrayList<>();

                list.forEach(p ->
                {
                    Link l = new Link();
                    l.setHref(p.getNfInstanceId().toString());
                    links.add(l);
                });

                final Link self = new Link();
                self.setHref(UUID.randomUUID().toString());

                final Links data = new Links();
                data.setItem(links);
                data.setSelf(self);
                final JsonObject tmp = new JsonObject(json.writeValueAsString(data));
                final JsonObject result = new JsonObject();
                result.put("_links", tmp);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_3GPP_HAL_JSON)
                       .end(result.toString());
            }
            catch (final Exception e)
            {
                replyWithError(context,
                               event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                 "Error processing " + operationId + " request. Cause: " + e.toString()),
                               null,
                               nfType,
                               null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleNfInstanceRegister = (context,
                                                                              event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final String nfInstanceId = context.request().getParam("nfInstanceID");

            String nfType = null;

            try
            {
                final NFProfile nfProfile = json.readValue(context.body().asString(), NFProfile.class);

                nfType = nfProfile.getNfType();

                if (!nfProfile.getNfInstanceId().toString().equals(nfInstanceId))
                {
                    replyWithError(context,
                                   event.setResponse(HttpResponseStatus.BAD_REQUEST, "Parameter nfInstanceID does not match nfInstanceID in NFProfile"),
                                   nfInstanceId,
                                   nfType,
                                   "nfInstanceID");
                    return;
                }

                nfProfile.setHeartBeatTimer(60);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED, context.request().absoluteURI()).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI()) // URI
                                                                                // is
                                                                                // already
                                                                                // the
                                                                                // location
                                                                                // (PUT
                                                                                // not
                                                                                // POST).
                       .end(json.writeValueAsString(nfProfile));
            }
            catch (final Exception e)
            {
                replyWithError(context,
                               event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                 "Error processing " + operationId + " request. Cause: " + e.toString()),
                               nfInstanceId,
                               nfType,
                               null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleNfInstanceUpdate = (context,
                                                                            event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleSubscriptionCreate = (context,
                                                                              event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            String subscriptionId = "subscription-1";

            try
            {
                final SubscriptionData data = json.readValue(context.body().asString(), SubscriptionData.class);

                // For test only: if subscription ID in request starts with "test", take it.
                if (data.getSubscriptionId() != null && data.getSubscriptionId().startsWith("test"))
                    subscriptionId = data.getSubscriptionId();

                final Instant instant = Instant.ofEpochMilli(System.currentTimeMillis() + 1000 * 60);
                data.setValidityTime(instant.atZone(ZoneOffset.UTC).toOffsetDateTime());

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED, context.request().absoluteURI() + "/" + subscriptionId)
                                           .getResponse()
                                           .getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + subscriptionId)
                       .end(json.writeValueAsString(data));
            }
            catch (final Exception e)
            {
                replyWithError(context,
                               event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                 "Error processing " + operationId + " request. Cause: " + e.toString()),
                               subscriptionId,
                               null,
                               null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleSubscriptionRemove = (context,
                                                                              event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleSubscriptionUpdate = (context,
                                                                              event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        public NnrfNfManagement(final SeppSimulator owner)
        {
            super(owner);

            this.getHandlerByOperationId().put(Operation.NF_INSTANCE_DEREGISTER.value, this.handleNfInstanceDeregister);
            this.getHandlerByOperationId().put(Operation.NF_INSTANCE_REGISTER.value, this.handleNfInstanceRegister);
            this.getHandlerByOperationId().put(Operation.NF_INSTANCE_UPDATE.value, this.handleNfInstanceUpdate);
            this.getHandlerByOperationId().put(Operation.NF_INSTANCE_GET.value, this.handleNfInstanceGet);
            this.getHandlerByOperationId().put(Operation.NF_INSTANCES_GET.value, this.handleNfInstancesGet);
            this.getHandlerByOperationId().put(Operation.SUBSCRIPTION_CREATE.value, this.handleSubscriptionCreate);
            this.getHandlerByOperationId().put(Operation.SUBSCRIPTION_REMOVE.value, this.handleSubscriptionRemove);
            this.getHandlerByOperationId().put(Operation.SUBSCRIPTION_UPDATE.value, this.handleSubscriptionUpdate);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNnrfNfManagement();
        }
    }

    public static class NnssaafNssaa extends ApiHandler
    {
        public enum Operation
        {
            CREATE_SLICE_AUTHENTICATION_CONTEXT("CreateSliceAuthenticationContext"),
            CONFIRM_SLICE_AUTHENTICATION("ConfirmSliceAuthentication");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private static final String PAR_AUTH_CTX_ID = "authCtxId";

        private Map<String, SliceAuthContext> contexts = new ConcurrentHashMap<>();

        private BiConsumer<RoutingContext, Event> handleCreateSliceAuthenticationContext = (context,
                                                                                            event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final String authCtxId = "auth-ctx-1";

            try
            {
                final SliceAuthInfo authInfo = json.readValue(context.body().asString(), SliceAuthInfo.class);

                final SliceAuthContext authCtx = new SliceAuthContext().authCtxId(authCtxId)
                                                                       .eapMessage("eap-message".getBytes())
                                                                       .gpsi("msisdn-1234567890")
                                                                       .snssai(new Snssai().sst(0));

                this.contexts.put(authCtxId, authCtx);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED, context.request().absoluteURI() + "/" + authCtxId)
                                           .getResponse()
                                           .getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + authCtxId)
                       .end(json.writeValueAsString(authCtx));
            }
            catch (final Exception e)
            {
                replyWithError(context,
                               event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                 "Error processing " + operationId + " request. Cause: " + e.toString()),
                               authCtxId,
                               null,
                               null);
            }
        };

        private BiConsumer<RoutingContext, Event> handleConfirmSliceAuthentication = (context,
                                                                                      event) ->
        {
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final String authCtxId = context.request().getParam(PAR_AUTH_CTX_ID);

            try
            {
                final SliceAuthConfirmationData confData = json.readValue(context.body().asString(), SliceAuthConfirmationData.class);
                final SliceAuthConfirmationResponse confResp = new SliceAuthConfirmationResponse().authResult(this.contexts.containsKey(authCtxId) ? "EAP_SUCCESS"
                                                                                                                                                   : "EAP_FAILURE")
                                                                                                  .eapMessage(confData.getEapMessage())
                                                                                                  .gpsi(confData.getGpsi())
                                                                                                  .snssai(confData.getSnssai());

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(confResp));
            }
            catch (final Exception e)
            {
                replyWithError(context,
                               event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                 "Error processing " + operationId + " request. Cause: " + e.toString()),
                               authCtxId,
                               null,
                               null);
            }
        };

        public NnssaafNssaa(final SeppSimulator owner)
        {
            super(owner);

            this.getHandlerByOperationId().put(Operation.CREATE_SLICE_AUTHENTICATION_CONTEXT.value, this.handleCreateSliceAuthenticationContext);
            this.getHandlerByOperationId().put(Operation.CONFIRM_SLICE_AUTHENTICATION.value, this.handleConfirmSliceAuthentication);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNnrfNfManagement();
        }
    }

    public static class NpcfSmPolicyControl extends ApiHandler
    {
        public enum Operation
        {
            SM_POLICY_CREATE("CreateSMPolicy");// ,
// NOT IMPLEMENTED, YET            
//            SM_POLICY_GET("GetSMPolicy"),
//            SM_POLICY_UPDATE("UpdateSMPolicy"),
//            SM_POLICY_DELETE("DeleteSMPolicy");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handleSmPolicyCreate = (context,
                                                                          event) ->
        {
            try
            {
                final SmPolicyDecision result = new SmPolicyDecision();
                final SessionRule rule = new SessionRule().sessRuleId("sessionRule-1");
                result.sessRules(Map.of(rule.getSessRuleId(), rule));
                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/smpolicy-0")
                       .end(json.writeValueAsString(result));
            }
            catch (JsonProcessingException e)
            {
                final String msg = "Error generating response. Cause: " + e.getMessage();
                log.error(msg);
                replyWithError(context, event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR, msg), null, null, null);
            }
        };

        public NpcfSmPolicyControl(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.SM_POLICY_CREATE.value(), this.handleSmPolicyCreate);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNpcfSmPolicyControl();
        }
    }

    public static class NsmfPduSession extends ApiHandler
    {
        public enum Operation
        {
            RETRIEVE_SM_CONTEXT("RetrieveSmContext"),
            POST_PDU_SESSION("PostPduSessions"),
            UPDATE_PDU_SESSION("UpdatePduSession"),
            RELEASE_PDU_SESSION("ReleasePduSession"),
            RETRIEVE_PDU_SESSION("RetrievePduSession");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private static final String PAR_PDU_SESSION_REF = "pduSessionRef";

        private Map<String, PduSessionCreateData> pduSessions = new ConcurrentHashMap<>();

        private BiConsumer<RoutingContext, Event> handleRetrievSmContext = (context,
                                                                            event) ->
        {
            try
            {
                final SmContext smc = new SmContext();
                smc.pduSessionId(4711)
                   .dnn("DNN")
                   .sNssai(new Snssai().sst(0))
                   .pduSessionType("IPV6")
                   .sessionAmbr(new Ambr().downlink("1 Gbps").uplink("100 Mbps"))
                   .addQosFlowsListItem(new QosFlowSetupItem().qfi(0).qosRules("U3dhZ2dlciByb2Nrcw==".getBytes()))
                   .interPlmnApiRoot("inter.ericsson.com")
                   .intraPlmnApiRoot("intra.ericsson.com");

                final SmContextRetrievedData data = new SmContextRetrievedData().ueEpsPdnConnection("ABC").smContext(smc);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(data));
            }
            catch (JsonProcessingException e)
            {
                // TODO Auto-generated catch block
            }
        };

        private BiConsumer<RoutingContext, Event> handlePostPduSession = (context,
                                                                          event) ->
        {
            try
            {
                final PduSessionCreateData createData = json.readValue(context.body().asString(), PduSessionCreateData.class);
                final String pduSessionRef = "pduSession-" + createData.getPduSessionId();
                this.pduSessions.put(pduSessionRef, createData);
                final PduSessionCreatedData createdData = new PduSessionCreatedData().pduSessionType("IPV6")
                                                                                     .sscMode("0")
                                                                                     .smfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"))
                                                                                     .interPlmnApiRoot("inter.ericsson.com")
                                                                                     .intraPlmnApiRoot("intra.ericsson.com");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + pduSessionRef)
                       .end(json.writeValueAsString(createdData));
            }
            catch (JsonProcessingException e)
            {
                // TODO Auto-generated catch block
            }
        };

        private BiConsumer<RoutingContext, Event> handleReleasePduSession = (context,
                                                                             event) ->
        {
            final PduSessionCreateData createData = this.pduSessions.remove(context.request().getParam(PAR_PDU_SESSION_REF));

            if (createData != null)
                context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
            else
                context.response().setStatusCode(event.setResponse(HttpResponseStatus.NOT_FOUND).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleRetrievePduSession = (context,
                                                                              event) ->
        {
            try
            {
                final PduSessionCreateData createData = this.pduSessions.get(context.request().getParam(PAR_PDU_SESSION_REF));

                if (createData != null)
                {
                    final AfCoordinationInfo afInfo = new AfCoordinationInfo().addNotificationInfoListItem(new NotificationInfo().notifUri(context.request()
                                                                                                                                                  .absoluteURI()));
                    context.response()
                           .setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode())
                           .end(json.writeValueAsString(afInfo));
                }
                else
                {
                    context.response().setStatusCode(event.setResponse(HttpResponseStatus.NOT_FOUND).getResponse().getResultCode()).end();
                }
            }
            catch (JsonProcessingException e)
            {
                // TODO Auto-generated catch block
            }
        };

        public NsmfPduSession(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.RETRIEVE_SM_CONTEXT.value(), this.handleRetrievSmContext);
            this.getHandlerByOperationId().put(Operation.POST_PDU_SESSION.value(), this.handlePostPduSession);
            this.getHandlerByOperationId().put(Operation.RELEASE_PDU_SESSION.value(), this.handleReleasePduSession);
            this.getHandlerByOperationId().put(Operation.RETRIEVE_PDU_SESSION.value(), this.handleRetrievePduSession);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNsmfPduSession();
        }
    }

    public static class NudmNiddau extends ApiHandler
    {
        public enum Operation
        {
            AUTHORIZE_NIDD_DATA("AuthorizeNiddData");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handleAuthorizeNiddData = (context,
                                                                             event) ->
        {
            try
            {
                final AuthorizationData data = new AuthorizationData().addAuthorizationDataItem(new UserIdentifier().supi("imsi-123456789"));

                context.response().setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode()).end(json.writeValueAsString(data));
            }
            catch (JsonProcessingException e)
            {
                // TODO Auto-generated catch block
            }
        };

        public NudmNiddau(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.AUTHORIZE_NIDD_DATA.value(), this.handleAuthorizeNiddData);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNudmNiddau();
        }
    }

    public static class NudmParameterProvision extends ApiHandler
    {
        public enum Operation
        {
            CREATE_5G_VN_GROUP("Create 5G VN Group"),
            DELETE_5G_VN_GROUP("Delete 5G VN Group"),
            MODIFY_5G_VN_GROUP("Modify 5G VN Group"),
            UPDATE("Update");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handleCreate5gVnGroup = (context,
                                                                           event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleDelete5gVnGroup = (context,
                                                                           event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleModify5gVnGroup = (context,
                                                                           event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleUpdate = (context,
                                                                  event) ->
        {
            try
            {
                final PatchResult patchResult = new PatchResult();
                final List<ReportItem> report = new ArrayList<>();
                final ReportItem reportItem = new ReportItem();

                reportItem.setPath("/path/to/report/item");
                report.add(reportItem);
                patchResult.setReport(report);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_3GPP_HAL_JSON)
                       .end(json.writeValueAsString(patchResult));
            }
            catch (JsonProcessingException e)
            {
                // TODO Auto-generated catch block
            }
        };

        public NudmParameterProvision(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.CREATE_5G_VN_GROUP.value(), this.handleCreate5gVnGroup);
            this.getHandlerByOperationId().put(Operation.DELETE_5G_VN_GROUP.value(), this.handleDelete5gVnGroup);
            this.getHandlerByOperationId().put(Operation.MODIFY_5G_VN_GROUP.value(), this.handleModify5gVnGroup);
            this.getHandlerByOperationId().put(Operation.UPDATE.value(), this.handleUpdate);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNudmParameterProvision();
        }
    }

    public static class NudmSsau extends ApiHandler
    {
        public enum Operation
        {
            SERVICE_SPECIFIC_AUTHORIZATION("ServiceSpecificAuthorization"),
            SERVICE_SPECIFIC_AUTHORIZATION_REMOVAL("ServiceSpecificAuthorizationRemoval");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handleServiceSpecificAuthorization = (context,
                                                                                        event) ->
        {
            try
            {
                final ServiceSpecificAuthorizationData authData = new ServiceSpecificAuthorizationData();

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(authData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleServiceSpecificAuthorizationRemoval = (context,
                                                                                               event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        public NudmSsau(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.SERVICE_SPECIFIC_AUTHORIZATION.value(), this.handleServiceSpecificAuthorization);
            this.getHandlerByOperationId().put(Operation.SERVICE_SPECIFIC_AUTHORIZATION_REMOVAL.value(), this.handleServiceSpecificAuthorizationRemoval);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNudmSsau();
        }
    }

    public static class NudmSubscriberDataManagement extends ApiHandler
    {
        public enum Operation
        {
            CAG_ACK("CAG Ack"),
            GET_AM_DATA("GetAmData"),
            GET_DATA_SETS("GetDataSets"),
            GET_ECR_DATA("GetEcrData"),
            GET_GROUP_IDENTIFIERS("GetGroupIdentifiers"),
            GET_LCS_BCA_DATA("GetLcsBcaData"),
            GET_LCS_MO_DATA("GetLcsMoData"),
            GET_LCS_PRIVACY_DATA("GetLcsPrivacyData"),
            GET_NSSAI("GetNSSAI"),
            GET_SHARED_DATA("GetSharedData"),
            GET_SM_DATA("GetSmData"),
            GET_SMF_SEL_DATA("GetSmfSelData"),
            GET_SMS_DATA("GetSmsData"),
            GET_SMS_MNGT_DATA("GetSmsMngtData"),
            GET_SUPI_OR_GPSI("GetSupiOrGpsi"),
            GET_TRACE_CONFIG_DATA("GetTraceConfigData"),
            GET_UE_CTX_IN_AMF_DATA("GetUeCtxInAmfData"),
            GET_UE_CTX_IN_SMF_DATA("GetUeCtxInSmfData"),
            GET_UE_CTX_IN_SMSF_DATA("GetUeCtxInSmsfData"),
            GET_V2X_DATA("GetV2xData"),
            MODIFY("Modify"),
            MODIFY_SHARED_DATA_SUBS("ModifySharedDataSubs"),
            SNSSAIS_ACK("S-NSSAIs Ack"),
            SOR_ACK_INFO("SorAckInfo"),
            SUBSCRIBE("Subscribe"),
            SUBSCRIBE_TO_SHARED_DATA("SubscribeToSharedData"),
            UNSUBSCRIBE("Unsubscribe"),
            UNSUBSCRIBE_FOR_SHARED_DATA("UnsubscribeForSharedData"),
            UPDATE_SOR_INFO("Update SOR Info"),
            UPU_ACK("UpuAck");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private static final String PAR_SUBSCRIPTION_ID = "subscriptionId";

        private final Map<String, SdmSubscription> subscriptions = new ConcurrentHashMap<>();
        private final AtomicInteger subcriptionCnt = new AtomicInteger();
        private final Map<String, SdmSubscription> sdSubscriptions = new ConcurrentHashMap<>();
        private final AtomicInteger sdSubcriptionCnt = new AtomicInteger();

        private BiConsumer<RoutingContext, Event> handleCagAck = (context,
                                                                  event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleGetAmData = (context,
                                                                     event) ->
        {
            try
            {
                final AccessAndMobilitySubscriptionData accessAndMobilitySubscriptionData = new AccessAndMobilitySubscriptionData();
                accessAndMobilitySubscriptionData.setSupportedFeatures("d6bDEaEF879be");

                final Nssai nssai = new Nssai();
                final Snssai snssai = new Snssai();
                final ArrayList<Snssai> singleNssais = new ArrayList<>();

                snssai.setSst(200);
                snssai.setSd("EE80bF");
                singleNssais.add(snssai);
                nssai.setDefaultSingleNssais(singleNssais);

                accessAndMobilitySubscriptionData.setNssai(nssai);
                accessAndMobilitySubscriptionData.setRfspIndex(50);
                accessAndMobilitySubscriptionData.setSubsRegTimer(12);
                accessAndMobilitySubscriptionData.setActiveTime(20);
                accessAndMobilitySubscriptionData.setSorafRetrieval(true);

                final TraceData traceData = new TraceData();

                traceData.setTraceDepth(TraceDepth.MAXIMUM);
                traceData.setTraceRef("59317-5730A6");
                traceData.setNeTypeList("6CDcF7BCF");
                traceData.setEventList("12EaDA1");
                accessAndMobilitySubscriptionData.setTraceData(traceData);

                accessAndMobilitySubscriptionData.setNssaiInclusionAllowed(false);
                accessAndMobilitySubscriptionData.setEcRestrictionDataNb(false);
                accessAndMobilitySubscriptionData.setIabOperationAllowed(false);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(accessAndMobilitySubscriptionData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetDataSets = (context,
                                                                       event) ->
        {
            try
            {
                final SmsSubscriptionData smsSubscriptionData = new SmsSubscriptionData().smsSubscribed(false).sharedSmsSubsDataId("632667-VZpN");

                final SubscriptionDataSets subscriptionDataSets = new SubscriptionDataSets();
                subscriptionDataSets.setSmsSubsData(smsSubscriptionData);

                final AccessAndMobilitySubscriptionData amData = new AccessAndMobilitySubscriptionData().cMsisdn("46123456789");
                subscriptionDataSets.setAmData(amData);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(subscriptionDataSets));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetEcrData = (context,
                                                                      event) ->
        {
            try
            {
                final EnhancedCoverageRestrictionData enhancedCoverageRestrictionData = new EnhancedCoverageRestrictionData();
                final ArrayList<PlmnEcInfo> plmnEcInfoList = new ArrayList<>();
                final PlmnEcInfo plmnEcInfo = new PlmnEcInfo();

                final PlmnId plmnId = new PlmnId();
                plmnId.setMcc("846");
                plmnId.setMnc("29");

                plmnEcInfo.setPlmnId(plmnId);
                plmnEcInfoList.add(plmnEcInfo);
                plmnEcInfo.setEcRestrictionDataNb(false);

                enhancedCoverageRestrictionData.setPlmnEcInfoList(plmnEcInfoList);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(enhancedCoverageRestrictionData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetGroupIdentifiers = (context,
                                                                               event) ->
        {
            try
            {
                final GroupIdentifiers groupIdentifiers = new GroupIdentifiers();
                final UeId ueId = new UeId();
                final List<UeId> ueIdList = new ArrayList<>();

                ueId.setSupi("imsi-123456789");
                ueIdList.add(ueId);

                groupIdentifiers.setUeIdList(ueIdList);
                groupIdentifiers.setIntGroupId("DEd3dA4E-283-27-D19D0DaC6D3E5ADcD99F");
                groupIdentifiers.setExtGroupId("extgroupid-test123@123test");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(groupIdentifiers));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetLcsBcaData = (context,
                                                                         event) ->
        {
            try
            {
                // -------------NEEDS FIX---------------

//                final LcsBroadcastAssistanceTypesData lcsBroadcastAssistanceTypesData = new LcsBroadcastAssistanceTypesData();
//                final List<AsyncFile> locationAssistanceType = new ArrayList<>();
//                final Vertx vertx = Vertx.vertx();
//                final AsyncFile asyncFile = vertx.fileSystem().openBlocking("/tmp/test.file", new OpenOptions());
//                final Buffer data = Buffer.buffer("1");
//
//                asyncFile.write(data);
//                locationAssistanceType.add(asyncFile);
//                lcsBroadcastAssistanceTypesData.setLocationAssistanceType(locationAssistanceType);

//                context.response()
//                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
//                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
//                       .putHeader(HD_CACHE_CONTROL, "5")
//                       .putHeader(HD_ETAG, "4ae413bd")
//                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
//                       .end(json.writeValueAsString(lcsBroadcastAssistanceTypesData));
                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end();

            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetLcsMoData = (context,
                                                                        event) ->
        {
            try
            {
                final LcsMoData lcsMoData = new LcsMoData();
                final List<String> allowedServiceClasses = new ArrayList<>();

                allowedServiceClasses.add(LcsMoServiceClass.AUTONOMOUS_SELF_LOCATION);
                lcsMoData.setAllowedServiceClasses(allowedServiceClasses);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(lcsMoData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetLcsPrivacyData = (context,
                                                                             event) ->
        {
            try
            {
                final LcsPrivacyData lcsPrivacyData = new LcsPrivacyData();
                final Lpi lpi = new Lpi();

                lpi.setLocationPrivacyInd(LocationPrivacyInd.LOCATION_DISALLOWED);
                lcsPrivacyData.setLpi(lpi);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(lcsPrivacyData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetNssai = (context,
                                                                    event) ->
        {
            try
            {
                final Nssai nssai = new Nssai();
                final Snssai snssai = new Snssai();
                final ArrayList<Snssai> singleNssais = new ArrayList<>();

                snssai.setSst(200);
                snssai.setSd("EE80bF");
                singleNssais.add(snssai);
                nssai.setDefaultSingleNssais(singleNssais);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .putHeader(HD_LOCATION, context.request().absoluteURI())
                       .end(json.writeValueAsString(nssai));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetSharedData = (context,
                                                                         event) ->
        {
            try
            {
                // 400 Bad Request curl -v -X GET
                // http://localhost:8081/nudm-sdm/v2/shared-data?shared-data-ids=45ge4r | jq
                final List<SharedData> sharedDataList = new ArrayList<>();
                final SharedData sharedData = new SharedData();

                sharedData.setSharedDataId("68042-XHq0gsd");
                sharedDataList.add(sharedData);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(sharedDataList));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetSmData = (context,
                                                                     event) ->
        {
            try
            {
                final SessionManagementSubscriptionData sessionManagementSubscriptionData = new SessionManagementSubscriptionData();
                final Snssai singleNssai = new Snssai();

                singleNssai.setSst(8);
                singleNssai.setSd("B54aAF");

                sessionManagementSubscriptionData.setSingleNssai(singleNssai);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(sessionManagementSubscriptionData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetSmfSelData = (context,
                                                                         event) ->
        {
            try
            {
                final SmfSelectionSubscriptionData smfSelectionSubscriptionData = new SmfSelectionSubscriptionData();
                final SnssaiInfo snssaiInfo = new SnssaiInfo();
                final ArrayList<DnnInfo> dnnInfos = new ArrayList<>();
                final Map<String, SnssaiInfo> subscribedSnssaiInfos = new HashMap<>();
                final DnnInfo dnnInfo = new DnnInfo();

                dnnInfo.setDnn("Some Data Network Name");
                dnnInfos.add(dnnInfo);

                snssaiInfo.setDnnInfos(dnnInfos);
                subscribedSnssaiInfos.put("SnssaiInfo1", snssaiInfo);

                smfSelectionSubscriptionData.setSupportedFeatures("d6bDEaEF879be");
                smfSelectionSubscriptionData.setSubscribedSnssaiInfos(subscribedSnssaiInfos);
                smfSelectionSubscriptionData.setSharedSnssaiInfosId("76606-1tpn");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(smfSelectionSubscriptionData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetSmsData = (context,
                                                                      event) ->
        {
            try
            {
                final SmsSubscriptionData smsSubscriptionData = new SmsSubscriptionData();

                smsSubscriptionData.setSmsSubscribed(true);
                smsSubscriptionData.setSharedSmsSubsDataId("845768-L6");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(smsSubscriptionData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetSmsMngtData = (context,
                                                                          event) ->
        {
            try
            {
                final SmsManagementSubscriptionData smsManagementSubscriptionData = new SmsManagementSubscriptionData();
                final List<String> sharedSmsMngDataIds = new ArrayList<>();
                final TraceData traceData = new TraceData();

                traceData.setTraceDepth(TraceDepth.MAXIMUM);
                traceData.setTraceRef("59317-5730A6");
                sharedSmsMngDataIds.add("845768-L6");
                traceData.setNeTypeList("6CDcF7BCF");
                traceData.setEventList("12EaDA1");

                smsManagementSubscriptionData.setSupportedFeatures("d6bDEaEF879be");
                smsManagementSubscriptionData.setMtSmsSubscribed(true);
                smsManagementSubscriptionData.setMtSmsBarringAll(true);
                smsManagementSubscriptionData.setMtSmsBarringRoaming(true);
                smsManagementSubscriptionData.setMoSmsSubscribed(true);
                smsManagementSubscriptionData.setMoSmsBarringAll(true);
                smsManagementSubscriptionData.setMoSmsBarringRoaming(true);
                smsManagementSubscriptionData.setSharedSmsMngDataIds(sharedSmsMngDataIds);
                smsManagementSubscriptionData.setTraceData(traceData);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(smsManagementSubscriptionData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetSupiOrGpsi = (context,
                                                                         event) ->
        {
            try
            {
                final IdTranslationResult idTranslationResult = new IdTranslationResult();

                idTranslationResult.setSupi("imsi-123456789");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(idTranslationResult));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetTraceConfigData = (context,
                                                                              event) ->
        {
            try
            {
                final TraceDataResponse traceDataResponse = new TraceDataResponse();
                final TraceData traceData = new TraceData();

                traceData.setTraceDepth(TraceDepth.MAXIMUM);
                traceData.setTraceRef("59317-5730A6");
                traceData.setNeTypeList("6CDcF7BCF");
                traceData.setEventList("12EaDA1");

                traceDataResponse.setTraceData(traceData);
                traceDataResponse.setSharedTraceDataId("76086-1At");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(traceDataResponse));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetUeCtxInAmfData = (context,
                                                                             event) ->
        {
            try
            {
                final UeContextInAmfData ueContextInAmfData = new UeContextInAmfData();
                final EpsInterworkingInfo epsInterworkingInfo = new EpsInterworkingInfo();
                final Map<String, EpsIwkPgw> epsIwkPgwsMap = new HashMap<>();
                final EpsIwkPgw epsIwkPgws = new EpsIwkPgw();

                epsIwkPgws.setPgwFqdn("myhost.ericsson.com");
                epsIwkPgws.setSmfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                epsIwkPgwsMap.put("dnn1", epsIwkPgws);
                epsInterworkingInfo.setEpsIwkPgws(epsIwkPgwsMap);
                ueContextInAmfData.setEpsInterworkingInfo(epsInterworkingInfo);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(ueContextInAmfData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetUeCtxInSmfData = (context,
                                                                             event) ->
        {
            try
            {
                final UeContextInSmfData ueContextInSmfData = new UeContextInSmfData();
                final PduSession pduSession = new PduSession();
                final PgwInfo pgwInfo = new PgwInfo();
                final EmergencyInfo emergencyInfo = new EmergencyInfo();
                final ArrayList<PgwInfo> pgwInfos = new ArrayList<>();
                final Map<String, PduSession> pduSessions = new HashMap<>();
                final PlmnId plmnId = new PlmnId();

                plmnId.setMcc("145");
                plmnId.setMnc("90");
                pduSession.setDnn("dnn-test");
                pduSession.setSmfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                pduSession.setPlmnId(plmnId);
                pduSessions.put("pduSessionId1", pduSession);

                pgwInfo.setDnn("dnn-test");
                pgwInfo.setPgwFqdn("pgw-fqdn-test");
                pgwInfos.add(pgwInfo);

                emergencyInfo.setPgwFqdn("pgw-fqdn-test");

                ueContextInSmfData.setPgwInfo(pgwInfos);
                ueContextInSmfData.setPduSessions(pduSessions);
                ueContextInSmfData.setEmergencyInfo(emergencyInfo);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(ueContextInSmfData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetUeCtxInSmsfData = (context,
                                                                              event) ->
        {
            try
            {
                final UeContextInSmsfData ueContextInSmsfData = new UeContextInSmsfData();
                final SmsfInfo smsfInfo = new SmsfInfo();
                final PlmnId plmnId = new PlmnId();

                plmnId.setMcc("145");
                plmnId.setMnc("90");
                smsfInfo.setPlmnId(plmnId);
                smsfInfo.setSmsfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));

                ueContextInSmsfData.setSmsfInfo3GppAccess(smsfInfo);
                ueContextInSmsfData.setSmsfInfoNon3GppAccess(smsfInfo);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(ueContextInSmsfData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetV2xData = (context,
                                                                      event) ->
        {
            try
            {
                final V2xSubscriptionData v2xSubscriptionData = new V2xSubscriptionData();
                final NrV2xAuth nrV2xAuth = new NrV2xAuth();
                final LteV2xAuth lteV2xAuth = new LteV2xAuth();

                nrV2xAuth.setPedestrianUeAuth(UeAuth.AUTHORIZED);
                nrV2xAuth.setVehicleUeAuth(UeAuth.AUTHORIZED);

                lteV2xAuth.setPedestrianUeAuth(UeAuth.AUTHORIZED);
                lteV2xAuth.setVehicleUeAuth(UeAuth.AUTHORIZED);

                v2xSubscriptionData.setLteV2xServicesAuth(lteV2xAuth);
                v2xSubscriptionData.setNrV2xServicesAuth(nrV2xAuth);
                v2xSubscriptionData.setLtePc5Ambr("704.03 bps");
                v2xSubscriptionData.setNrUePc5Ambr("84.22 kbps");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_CACHE_CONTROL, "5")
                       .putHeader(HD_ETAG, "4ae413bd")
                       .putHeader(HD_LAST_MODIFIED, "Tue, 15 Nov 1994 12:45:26 GMT")
                       .end(json.writeValueAsString(v2xSubscriptionData));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleModify = (context,
                                                                  event) ->
        {
            try
            {
                final SdmSubsModification patch = json.readValue(context.body().asString(), SdmSubsModification.class);
                final String subscriptionId = context.request().getParam(PAR_SUBSCRIPTION_ID, "subscription-0");
                final SdmSubscription sdmSubscription = this.subscriptions.get(subscriptionId);

                if (sdmSubscription == null)
                {
                    context.response().setStatusCode(event.setResponse(HttpResponseStatus.NOT_FOUND).getResponse().getResultCode()).end();
                    return;
                }

                // TODO: apply patch on the sdmSubscription.

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(sdmSubscription));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleModifySharedDataSubs = (context,
                                                                                event) ->
        {
            try
            {
                final SdmSubsModification patch = json.readValue(context.body().asString(), SdmSubsModification.class);
                final String subscriptionId = context.request().getParam(PAR_SUBSCRIPTION_ID, "sd-subscription-0");
                final SdmSubscription sdmSubscription = this.sdSubscriptions.get(subscriptionId);

                if (sdmSubscription == null)
                {
                    context.response().setStatusCode(event.setResponse(HttpResponseStatus.NOT_FOUND).getResponse().getResultCode()).end();
                    return;
                }

                // TODO: apply patch on the sdmSubscription.

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(sdmSubscription));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleSnssaisAck = (context,
                                                                      event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleSorAckInfo = (context,
                                                                      event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleSubscribe = (context,
                                                                     event) ->
        {
            try
            {
                final SdmSubscription sdmSubscription = json.readValue(context.body().asString(), SdmSubscription.class);
                final String subscriptionId = "subscription-" + this.subcriptionCnt.incrementAndGet();

                this.subscriptions.put(subscriptionId, sdmSubscription);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + subscriptionId)
                       .end(json.writeValueAsString(sdmSubscription));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleSubscribeToSharedData = (context,
                                                                                 event) ->
        {
            try
            {
                final SdmSubscription sdmSubscription = json.readValue(context.body().asString(), SdmSubscription.class);
                final String subscriptionId = "sd-subscription-" + this.sdSubcriptionCnt.incrementAndGet();

                this.sdSubscriptions.put(subscriptionId, sdmSubscription);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/" + subscriptionId)
                       .end(json.writeValueAsString(sdmSubscription));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleUnsubscribe = (context,
                                                                       event) ->
        {
            this.subscriptions.remove(context.request().getParam(PAR_SUBSCRIPTION_ID, "subscription-0"));
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleUnsubscribeForSharedData = (context,
                                                                                    event) ->
        {
            this.sdSubscriptions.remove(context.request().getParam(PAR_SUBSCRIPTION_ID, "sd-subscription-0"));
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleUpdateSorInfo = (context,
                                                                         event) ->
        {
            try
            {
                final com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SorInfo sorInfo = new com.ericsson.cnal.openapi.r17.ts29503.nudm.sdm.SorInfo();

                sorInfo.setAckInd(Boolean.valueOf(true));
                sorInfo.setProvisioningTime(OffsetDateTime.parse("2020-08-19T14:25:46.703+02:00"));

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(sorInfo));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleUpuAck = (context,
                                                                  event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        public NudmSubscriberDataManagement(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.CAG_ACK.value(), this.handleCagAck);
            this.getHandlerByOperationId().put(Operation.GET_AM_DATA.value(), this.handleGetAmData);
            this.getHandlerByOperationId().put(Operation.GET_DATA_SETS.value(), this.handleGetDataSets);
            this.getHandlerByOperationId().put(Operation.GET_ECR_DATA.value(), this.handleGetEcrData);
            this.getHandlerByOperationId().put(Operation.GET_GROUP_IDENTIFIERS.value(), this.handleGetGroupIdentifiers);
            this.getHandlerByOperationId().put(Operation.GET_LCS_BCA_DATA.value(), this.handleGetLcsBcaData);
            this.getHandlerByOperationId().put(Operation.GET_LCS_MO_DATA.value(), this.handleGetLcsMoData);
            this.getHandlerByOperationId().put(Operation.GET_LCS_PRIVACY_DATA.value(), this.handleGetLcsPrivacyData);
            this.getHandlerByOperationId().put(Operation.GET_NSSAI.value(), this.handleGetNssai);
            this.getHandlerByOperationId().put(Operation.GET_SHARED_DATA.value(), this.handleGetSharedData);
            this.getHandlerByOperationId().put(Operation.GET_SM_DATA.value(), this.handleGetSmData);
            this.getHandlerByOperationId().put(Operation.GET_SMF_SEL_DATA.value(), this.handleGetSmfSelData);
            this.getHandlerByOperationId().put(Operation.GET_SMS_DATA.value(), this.handleGetSmsData);
            this.getHandlerByOperationId().put(Operation.GET_SMS_MNGT_DATA.value(), this.handleGetSmsMngtData);
            this.getHandlerByOperationId().put(Operation.GET_SUPI_OR_GPSI.value(), this.handleGetSupiOrGpsi);
            this.getHandlerByOperationId().put(Operation.GET_TRACE_CONFIG_DATA.value(), this.handleGetTraceConfigData);
            this.getHandlerByOperationId().put(Operation.GET_UE_CTX_IN_AMF_DATA.value(), this.handleGetUeCtxInAmfData);
            this.getHandlerByOperationId().put(Operation.GET_UE_CTX_IN_SMF_DATA.value(), this.handleGetUeCtxInSmfData);
            this.getHandlerByOperationId().put(Operation.GET_UE_CTX_IN_SMSF_DATA.value(), this.handleGetUeCtxInSmsfData);
            this.getHandlerByOperationId().put(Operation.GET_V2X_DATA.value(), this.handleGetV2xData);
            this.getHandlerByOperationId().put(Operation.MODIFY.value(), this.handleModify);
            this.getHandlerByOperationId().put(Operation.MODIFY_SHARED_DATA_SUBS.value(), this.handleModifySharedDataSubs);
            this.getHandlerByOperationId().put(Operation.SNSSAIS_ACK.value(), this.handleSnssaisAck);
            this.getHandlerByOperationId().put(Operation.SOR_ACK_INFO.value(), this.handleSorAckInfo);
            this.getHandlerByOperationId().put(Operation.SUBSCRIBE.value(), this.handleSubscribe);
            this.getHandlerByOperationId().put(Operation.SUBSCRIBE_TO_SHARED_DATA.value(), this.handleSubscribeToSharedData);
            this.getHandlerByOperationId().put(Operation.UNSUBSCRIBE.value(), this.handleUnsubscribe);
            this.getHandlerByOperationId().put(Operation.UNSUBSCRIBE_FOR_SHARED_DATA.value(), this.handleUnsubscribeForSharedData);
            this.getHandlerByOperationId().put(Operation.UPDATE_SOR_INFO.value(), this.handleUpdateSorInfo);
            this.getHandlerByOperationId().put(Operation.UPU_ACK.value(), this.handleUpuAck);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNudmSubscriberDataManagement();
        }
    }

    public static class NudmUeAuthentication extends ApiHandler
    {
        public enum Operation
        {
            GENERATE_AUTH_DATA("GenerateAuthData"),
            GET_RG_AUTH_DATA("GetRgAuthData"),
            CONFIRM_AUTH("ConfirmAuth"),
            GENERATE_AV("GenerateAv"),
            DELETE_AUTH("DeleteAuth");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handleConfirmAuth = (context,
                                                                       event) ->
        {
            final String body = context.body().asString();
            log.debug("body={}.", body);

            try
            {
                final AuthEvent authEvent = json.readValue(body, AuthEvent.class);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI() + "/auth-event-0")
                       .end(json.writeValueAsString(authEvent));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleDeleteAuth = (context,
                                                                      event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleGenerateAuthData = (context,
                                                                            event) ->
        {
            try
            {
                final AuthenticationInfoResult result = new AuthenticationInfoResult();

                result.setAuthType(com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau.AuthType._5G_AKA);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(result));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGenerateAv = (context,
                                                                      event) ->
        {
            try
            {
                final HssAuthenticationInfoResult result = new HssAuthenticationInfoResult();
                final HssAuthenticationVectors hssAuthenticationVectors = new HssAuthenticationVectors();
                final AvEpsAka item = new AvEpsAka();
                item.setAvType(HssAvType.EPS_AKA);
                item.setAutn("autn");
                item.setKasme("kasme");
                item.setRand("rand");
                item.setXres("xres");
                hssAuthenticationVectors.add(item);
                result.setHssAuthenticationVectors(hssAuthenticationVectors);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(result));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetRgAuthData = (context,
                                                                         event) ->
        {
            try
            {
                final com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau.RgAuthCtx ctx = new com.ericsson.cnal.openapi.r17.ts29503.nudm.ueau.RgAuthCtx();

                ctx.setAuthInd(true);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(ctx));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        public NudmUeAuthentication(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.CONFIRM_AUTH.value(), this.handleConfirmAuth);
            this.getHandlerByOperationId().put(Operation.DELETE_AUTH.value(), this.handleDeleteAuth);
            this.getHandlerByOperationId().put(Operation.GENERATE_AUTH_DATA.value(), this.handleGenerateAuthData);
            this.getHandlerByOperationId().put(Operation.GENERATE_AV.value(), this.handleGenerateAv);
            this.getHandlerByOperationId().put(Operation.GET_RG_AUTH_DATA.value(), this.handleGetRgAuthData);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNudmUeAuthentication();
        }
    }

    public static class NudmUeContextManagement extends ApiHandler
    {
        public enum Operation
        {
            TGPP_REGISTRATION("3GppRegistration"),
            TGPP_SMSF_DEREGISTRATION("3GppSmsfDeregistration"),
            TGPP_SMSF_REGISTRATION("3GppSmsfRegistration"),
            DEREGISTER_AMF("deregAMF"),
            GET_3GPP_REGISTRATION("Get3GppRegistration"),
            GET_3GPP_SMSF_REGISTRATION("Get3GppSmsfRegistration"),
            GET_IP_SM_GW_REGISTRATION("GetIpSmGwRegistration"),
            GET_LOCATION_INFO("GetLocationInfo"),
            GET_NON_3GPP_REGISTRATION("GetNon3GppRegistration"),
            GET_NON_3GPP_SMSF_REGISTRATION("GetNon3GppSmsfRegistration"),
            GET_REGISTRATIONS("GetRegistrations"),
            GET_SMF_REGISTRATION("GetSmfRegistration"),
            IP_SM_GW_DEREGISTRATION("IpSmGwDeregistration"),
            IP_SM_GW_REGISTRATION("IpSmGwRegistration"),
            NON_3GPP_REGISTRATION("Non3GppRegistration"),
            NON_3GPP_SMSF_DEREGISTRATION("Non3GppSmsfDeregistration"),
            NON_3GPP_SMSF_REGISTRATION("Non3GppSmsfRegistration"),
            PEI_UPDATE("PeiUpdate"),
            RETRIEVE_SMF_REGISTRATION("RetrieveSmfRegistration"),
            SMF_DEREGISTRATION("SmfDeregistration"),
            SMF_REGISTRATION("Registration"),
            TRIGGER_P_CSCF_RESTORATION("Trigger P-CSCF Restoration"),
            UPDATE_3GPP_REGISTRATION("Update3GppRegistration"),
            UPDATE_NON_3GPP_REGISTRATION("UpdateNon3GppRegistration");

            private final String value;

            Operation(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private BiConsumer<RoutingContext, Event> handle3GppRegistration = (context,
                                                                            event) ->
        {
            try
            {
                final Amf3GppAccessRegistration amf3Gpp = json.readValue(context.body().asString(), Amf3GppAccessRegistration.class);
                amf3Gpp.setAmfEeSubscriptionId("test.com/amf3Gpp/subscription-0");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI())
                       .end(json.writeValueAsString(amf3Gpp));
            }
            catch (Exception e)
            {
                // error
            }
        };

        private BiConsumer<RoutingContext, Event> handle3GppSmsfDeregistration = (context,
                                                                                  event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handle3GppSmsfRegistration = (context,
                                                                                event) ->
        {
            try
            {
                final SmsfRegistration smsfRegistration = json.readValue(context.body().asString(), SmsfRegistration.class);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI())
                       .end(json.writeValueAsString(smsfRegistration));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleDeregisterAMF = (context,
                                                                         event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleGet3GppRegistration = (context,
                                                                               event) ->
        {
            try
            {
                final Amf3GppAccessRegistration amf3Gpp = new Amf3GppAccessRegistration();
                final Guami guami = new Guami();
                final PlmnIdNid plmnIdNid = new PlmnIdNid();

                amf3Gpp.setAmfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                amf3Gpp.setDeregCallbackUri("test.com/amf3Gpp");
                amf3Gpp.setAmfEeSubscriptionId("test.com/amf3Gpp/subscription-0");
                plmnIdNid.setMcc("846");
                plmnIdNid.setMnc("29");
                guami.setPlmnId(plmnIdNid);
                guami.setAmfId("bdE4c3");
                amf3Gpp.setGuami(guami);
                amf3Gpp.setRatType(RatType.EUTRA);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(amf3Gpp));

            }
            catch (Exception e)
            {
                // error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGet3GppSmsfRegistration = (context,
                                                                                   event) ->
        {
            try
            {
                final SmsfRegistration smsfRegistration = new SmsfRegistration();
                final PlmnId plmnId = new PlmnId();

                smsfRegistration.setSmsfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                plmnId.setMcc("846");
                plmnId.setMnc("29");
                smsfRegistration.setPlmnId(plmnId);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(smsfRegistration));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetIpSmGwRegistration = (context,
                                                                                 event) ->
        {
            try
            {
                final IpSmGwRegistration ipSmGwRegistration = new IpSmGwRegistration();

                ipSmGwRegistration.setIpSmGwMapAddress("12345");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(ipSmGwRegistration));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetLocationInfo = (context,
                                                                           event) ->
        {
            try
            {
                final com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.LocationInfo locationInfo = new com.ericsson.cnal.openapi.r17.ts29503.nudm.uecm.LocationInfo();
                final RegistrationLocationInfo registrationLocationInfo = new RegistrationLocationInfo();
                final List<RegistrationLocationInfo> registrationLocationInfoList = new ArrayList<>();
                final List<AccessType> accessTypeList = new ArrayList<>();

                registrationLocationInfo.setAmfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                accessTypeList.add(AccessType._3GPP_ACCESS);
                registrationLocationInfo.setAccessTypeList(accessTypeList);

                registrationLocationInfoList.add(registrationLocationInfo);

                locationInfo.setRegistrationLocationInfoList(registrationLocationInfoList);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(locationInfo));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetNon3GppRegistration = (context,
                                                                                  event) ->
        {
            try
            {
                final Guami guami = new Guami();
                final PlmnIdNid plmnIdNid = new PlmnIdNid();

                plmnIdNid.setMcc("846");
                plmnIdNid.setMnc("29");
                guami.setPlmnId(plmnIdNid);
                guami.setAmfId("bdE4c3");

                final AmfNon3GppAccessRegistration amfNon3Gpp = new AmfNon3GppAccessRegistration();

                amfNon3Gpp.setAmfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                amfNon3Gpp.setImsVoPs(ImsVoPs.HOMOGENEOUS_NON_SUPPORT);
                amfNon3Gpp.setDeregCallbackUri("test.com/amfNon3Gpp");
                amfNon3Gpp.setAmfEeSubscriptionId("test.com/amfNon3Gpp/subscription-0");
                amfNon3Gpp.setGuami(guami);
                amfNon3Gpp.setRatType(RatType.EUTRA);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(amfNon3Gpp));

            }
            catch (Exception e)
            {

            }
        };

        private BiConsumer<RoutingContext, Event> handleGetNon3GppSmsfRegistration = (context,
                                                                                      event) ->
        {
            try
            {
                final SmsfRegistration smsfRegistration = new SmsfRegistration();
                final PlmnId plmnId = new PlmnId();

                smsfRegistration.setSmsfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                plmnId.setMcc("846");
                plmnId.setMnc("29");
                smsfRegistration.setPlmnId(plmnId);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(smsfRegistration));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetRegistrations = (context,
                                                                            event) ->
        {
            final String body = context.body().asString();
            log.debug("body={}.", body);

            try
            {
                final Amf3GppAccessRegistration amf3Gpp = new Amf3GppAccessRegistration();
                final Guami guami = new Guami();
                final PlmnIdNid plmnIdNid = new PlmnIdNid();

                amf3Gpp.setAmfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                amf3Gpp.setDeregCallbackUri("test.com/amf3Gpp");
                plmnIdNid.setMcc("846");
                plmnIdNid.setMnc("29");
                guami.setPlmnId(plmnIdNid);
                guami.setAmfId("bdE4c3");
                amf3Gpp.setGuami(guami);
                amf3Gpp.setRatType(RatType.EUTRA);

                final AmfNon3GppAccessRegistration amfNon3Gpp = new AmfNon3GppAccessRegistration();

                amfNon3Gpp.setAmfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                amfNon3Gpp.setImsVoPs(ImsVoPs.HOMOGENEOUS_NON_SUPPORT);
                amfNon3Gpp.setDeregCallbackUri("test.com/amfNon3Gpp");
                amfNon3Gpp.setGuami(guami);
                amfNon3Gpp.setRatType(RatType.EUTRA);

                final SmfRegistration smfRegistration = new SmfRegistration();
                final SmfRegistrationInfo smfRegistrationInfo = new SmfRegistrationInfo();
                final List<SmfRegistration> smfRegistrationList = new ArrayList<>();
                final Snssai singleNssai = new Snssai();
                final PlmnId plmnId = new PlmnId();

                smfRegistration.setSmfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                smfRegistration.setPduSessionId(5);
                singleNssai.setSst(8);
                singleNssai.setSd("B54aAF");
                smfRegistration.setSingleNssai(singleNssai);
                plmnId.setMcc("656");
                plmnId.setMnc("73");
                smfRegistration.setPlmnId(plmnId);

                smfRegistrationList.add(smfRegistration);
                smfRegistrationInfo.setSmfRegistrationList(smfRegistrationList);

                final SmsfRegistration smsf3Gpp = new SmsfRegistration();
                smsf3Gpp.setSmsfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                smsf3Gpp.setPlmnId(plmnId);

                final SmsfRegistration smsfNon3Gpp = new SmsfRegistration();
                smsfNon3Gpp.setSmsfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                smsfNon3Gpp.setPlmnId(plmnId);

                final RegistrationDataSets dataSet = new RegistrationDataSets();
                dataSet.setAmf3Gpp(amf3Gpp);
                dataSet.setAmfNon3Gpp(amfNon3Gpp);
                dataSet.setSmfRegistration(smfRegistrationInfo);
                dataSet.setSmsf3Gpp(smsf3Gpp);
                dataSet.setSmsfNon3Gpp(smsfNon3Gpp);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(dataSet));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleGetSmfRegistration = (context,
                                                                              event) ->
        {
            try
            {
                final SmfRegistration smfRegistration = new SmfRegistration();
                final List<SmfRegistration> smfRegistrationList = new ArrayList<>();
                final Snssai singleNssai = new Snssai();
                final PlmnId plmnId = new PlmnId();

                smfRegistration.setSmfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                smfRegistration.setPduSessionId(5);
                singleNssai.setSst(8);
                singleNssai.setSd("B54aAF");
                smfRegistration.setSingleNssai(singleNssai);
                plmnId.setMcc("846");
                plmnId.setMnc("29");
                smfRegistration.setPlmnId(plmnId);

                smfRegistrationList.add(smfRegistration);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(smfRegistrationList));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleIpSmGwDeregistration = (context,
                                                                                event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleIpSmGwRegistration = (context,
                                                                              event) ->
        {
            try
            {
                final IpSmGwRegistration ipSmGwRegistration = new IpSmGwRegistration();

                ipSmGwRegistration.setIpSmGwMapAddress("12345");

                String uri = context.request().absoluteURI();

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, uri)
                       .end(json.writeValueAsString(ipSmGwRegistration));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleNon3GppRegistration = (context,
                                                                               event) ->
        {
            try
            {
                final AmfNon3GppAccessRegistration amfNon3Gpp = json.readValue(context.body().asString(), AmfNon3GppAccessRegistration.class);
                amfNon3Gpp.setAmfEeSubscriptionId("test.com/amf3Gpp/subscription-0");

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI())
                       .end(json.writeValueAsString(amfNon3Gpp));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleNon3GppSmsfDeregistration = (context,
                                                                                     event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleNon3GppSmsfRegistration = (context,
                                                                                   event) ->
        {
            try
            {
                final SmsfRegistration smsfRegistration = json.readValue(context.body().asString(), SmsfRegistration.class);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI())
                       .end(json.writeValueAsString(smsfRegistration));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handlePeiUpdate = (context,
                                                                     event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleRetrieveSmfRegistration = (context,
                                                                                   event) ->
        {
            try
            {
                final SmfRegistration smfRegistration = new SmfRegistration();
                final Snssai singleNssai = new Snssai();
                final PlmnId plmnId = new PlmnId();

                smfRegistration.setSmfInstanceId(UUID.fromString("fde21b56-2e47-49dd-9a1f-2769e5a8f45d"));
                smfRegistration.setPduSessionId(5);
                singleNssai.setSst(8);
                singleNssai.setSd("B54aAF");
                smfRegistration.setSingleNssai(singleNssai);
                plmnId.setMcc("846");
                plmnId.setMnc("29");
                smfRegistration.setPlmnId(plmnId);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .end(json.writeValueAsString(smfRegistration));
            }
            catch (Exception e)
            {
                // reply with error
            }
        };

        private BiConsumer<RoutingContext, Event> handleSmfDeregistration = (context,
                                                                             event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleSmfRegistration = (context,
                                                                           event) ->
        {
            try
            {
                final SmfRegistration smfRegistration = json.readValue(context.body().asString(), SmfRegistration.class);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.CREATED).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_JSON)
                       .putHeader(HD_LOCATION, context.request().absoluteURI())
                       .end(json.writeValueAsString(smfRegistration));
            }
            catch (JsonProcessingException e)
            {
                // TODO Auto-generated catch block
            }
        };

        private BiConsumer<RoutingContext, Event> handleTriggerPcscfRestoration = (context,
                                                                                   event) ->
        {
            context.response().setStatusCode(event.setResponse(HttpResponseStatus.NO_CONTENT).getResponse().getResultCode()).end();
        };

        private BiConsumer<RoutingContext, Event> handleUpdate3GppRegistration = (context,
                                                                                  event) ->
        {
            try
            {
                final PatchResult patchResult = new PatchResult();
                final List<ReportItem> report = new ArrayList<>();
                final ReportItem reportItem = new ReportItem();

                reportItem.setPath("/path/to/report/item");
                report.add(reportItem);
                patchResult.setReport(report);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_3GPP_HAL_JSON)
                       .end(json.writeValueAsString(patchResult));
            }
            catch (JsonProcessingException e)
            {
                // TODO Auto-generated catch block
            }
        };

        private BiConsumer<RoutingContext, Event> handleUpdateNon3GppRegistration = (context,
                                                                                     event) ->
        {
            try
            {
                final PatchResult patchResult = new PatchResult();
                final List<ReportItem> report = new ArrayList<>();
                final ReportItem reportItem = new ReportItem();

                reportItem.setPath("/path/to/report/item");
                report.add(reportItem);
                patchResult.setReport(report);

                context.response()
                       .setStatusCode(event.setResponse(HttpResponseStatus.OK).getResponse().getResultCode())
                       .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_3GPP_HAL_JSON)
                       .end(json.writeValueAsString(patchResult));
            }
            catch (JsonProcessingException e)
            {
                // TODO Auto-generated catch block
            }
        };

        public NudmUeContextManagement(final SeppSimulator owner)
        {
            super(owner);
            this.getHandlerByOperationId().put(Operation.TGPP_REGISTRATION.value(), this.handle3GppRegistration);
            this.getHandlerByOperationId().put(Operation.TGPP_SMSF_DEREGISTRATION.value(), this.handle3GppSmsfDeregistration);
            this.getHandlerByOperationId().put(Operation.TGPP_SMSF_REGISTRATION.value(), this.handle3GppSmsfRegistration);
            this.getHandlerByOperationId().put(Operation.DEREGISTER_AMF.value(), this.handleDeregisterAMF);
            this.getHandlerByOperationId().put(Operation.GET_3GPP_REGISTRATION.value(), this.handleGet3GppRegistration);
            this.getHandlerByOperationId().put(Operation.GET_3GPP_SMSF_REGISTRATION.value(), this.handleGet3GppSmsfRegistration);
            this.getHandlerByOperationId().put(Operation.GET_IP_SM_GW_REGISTRATION.value(), this.handleGetIpSmGwRegistration);
            this.getHandlerByOperationId().put(Operation.GET_LOCATION_INFO.value(), this.handleGetLocationInfo);
            this.getHandlerByOperationId().put(Operation.GET_NON_3GPP_REGISTRATION.value(), this.handleGetNon3GppRegistration);
            this.getHandlerByOperationId().put(Operation.GET_NON_3GPP_SMSF_REGISTRATION.value(), this.handleGetNon3GppSmsfRegistration);
            this.getHandlerByOperationId().put(Operation.GET_REGISTRATIONS.value(), this.handleGetRegistrations);
            this.getHandlerByOperationId().put(Operation.GET_SMF_REGISTRATION.value(), this.handleGetSmfRegistration);
            this.getHandlerByOperationId().put(Operation.IP_SM_GW_DEREGISTRATION.value(), this.handleIpSmGwDeregistration);
            this.getHandlerByOperationId().put(Operation.IP_SM_GW_REGISTRATION.value(), this.handleIpSmGwRegistration);
            this.getHandlerByOperationId().put(Operation.NON_3GPP_REGISTRATION.value(), this.handleNon3GppRegistration);
            this.getHandlerByOperationId().put(Operation.NON_3GPP_SMSF_DEREGISTRATION.value(), this.handleNon3GppSmsfDeregistration);
            this.getHandlerByOperationId().put(Operation.NON_3GPP_SMSF_REGISTRATION.value(), this.handleNon3GppSmsfRegistration);
            this.getHandlerByOperationId().put(Operation.PEI_UPDATE.value(), this.handlePeiUpdate);
            this.getHandlerByOperationId().put(Operation.RETRIEVE_SMF_REGISTRATION.value(), this.handleRetrieveSmfRegistration);
            this.getHandlerByOperationId().put(Operation.SMF_DEREGISTRATION.value(), this.handleSmfDeregistration);
            this.getHandlerByOperationId().put(Operation.SMF_REGISTRATION.value(), this.handleSmfRegistration);
            this.getHandlerByOperationId().put(Operation.TRIGGER_P_CSCF_RESTORATION.value(), this.handleTriggerPcscfRestoration);
            this.getHandlerByOperationId().put(Operation.UPDATE_3GPP_REGISTRATION.value(), this.handleUpdate3GppRegistration);
            this.getHandlerByOperationId().put(Operation.UPDATE_NON_3GPP_REGISTRATION.value(), this.handleUpdateNon3GppRegistration);
        }

        @Override
        protected Configuration.Disturbances getDisturbances()
        {
            return this.owner.getConfiguration().getApi().getNudmUeContextManagement();
        }
    }

    @JsonPropertyOrder({ "countInHttpRequestsPerIpFamily",
                         "countOutHttpResponsesPerIpFamily",
                         "countInHttpRequests",
                         "countOutHttpResponsesPerStatus",
                         "historyOfEvents" })
    public static class Statistics
    {
        @JsonProperty("countInHttpRequestsPerIpFamily")
        private final Count.Pool countInHttpRequestsPerIpFamily;

        @JsonProperty("countOutHttpResponsesPerIpFamily")
        private final Count.Pool countOutHttpResponsesPerIpFamily;

        @JsonProperty("countInHttpRequests")
        private final Count countInHttpRequests;

        @JsonProperty("countOutHttpResponsesPerStatus")
        private final Count.Pool countOutHttpResponsesPerStatus;

        @JsonProperty("historyOfEvents")
        private final Event.Sequence historyOfEvents;

        public Statistics(final String nfInstanceId)
        {
            this.countInHttpRequestsPerIpFamily = new Count.Pool();
            this.countOutHttpResponsesPerIpFamily = new Count.Pool();
            this.countInHttpRequests = new Count();
            this.countOutHttpResponsesPerStatus = new Count.Pool();
            this.historyOfEvents = new Event.Sequence(nfInstanceId);
        }

        public void clear()
        {
            this.countInHttpRequestsPerIpFamily.clear();
            this.countOutHttpResponsesPerIpFamily.clear();
            this.countInHttpRequests.clear();
            this.countOutHttpResponsesPerStatus.clear();
            this.historyOfEvents.clear();
        }

        public Count getCountInHttpRequests()
        {
            return this.countInHttpRequests;
        }

        public Count.Pool getCountInHttpRequestsPerIpFamily()
        {
            return this.countInHttpRequestsPerIpFamily;
        }

        public Count.Pool getCountOutHttpResponsesPerIpFamily()
        {
            return this.countOutHttpResponsesPerIpFamily;
        }

        public Count.Pool getCountOutHttpResponsesPerStatus()
        {
            return this.countOutHttpResponsesPerStatus;
        }

        public Event.Sequence getHistoryOfEvents()
        {
            return this.historyOfEvents;
        }
    }

    private static abstract class ApiHandler implements IfApiHandler
    {

        private enum Role
        {
            C_SEPP("C-SEPP"),
            P_SEPP("P-SEPP"),
            NF("NF");

            private final String value;

            Role(final String value)
            {
                this.value = value;
            }

            public String value()
            {
                return this.value;
            }
        }

        private static final String CTX_DISTURBANCE = "DISTURBANCE";
        private static final String TARGET_APIROOT = "3gpp-sbi-target-apiroot";

        private static void forwardExternalProblem(final RoutingContext context,
                                                   final Event event,
                                                   final HttpResponse<Buffer> response)
        {
            event.setResponse(HttpResponseStatus.valueOf(response.statusCode()), response.bodyAsString());

            if (event.getResponse().getResultDetails() == null)
            {
                context.response().setStatusCode(event.getResponse().getResultCode()).end();
            }
            else
            {
                final String contentType = response.getHeader("content-type");

                if (contentType == null)
                {
                    context.response().setStatusCode(event.getResponse().getResultCode()).end(event.getResponse().getResultDetails());
                }
                else
                {
                    context.response()
                           .setStatusCode(event.getResponse().getResultCode())
                           .putHeader(HD_CONTENT_TYPE, contentType)
                           .end(event.getResponse().getResultDetails());
                }
            }
        }

        private static void forwardResult(final RoutingContext context,
                                          final Event event,
                                          final HttpResponse<Buffer> result) throws JsonProcessingException
        {

            final HttpServerResponse response = context.response()
                                                       .setStatusCode(event.setResponse(HttpResponseStatus.valueOf(result.statusCode()))
                                                                           .getResponse()
                                                                           .getResultCode());
            final String body = result.bodyAsString();

            if (body != null)
                response.end(body);
            else
                response.end();
        }

        private static String getVia(final RoutingContext context)
        {
            final StringBuilder b = new StringBuilder("[");
            context.request().headers().getAll(HD_VIA).forEach(v -> b.append(b.length() < 2 ? "" : " -> ").append("'").append(v).append("'"));
            return b.append("]").toString();
        }

        protected final SeppSimulator owner;
        protected final Map<String, BiConsumer<RoutingContext, Event>> handlerByOperationId;
        protected final String hostName;

        protected final WebClient client;

        protected ApiHandler(final SeppSimulator owner)
        {
            this.owner = owner;
            this.handlerByOperationId = new TreeMap<>();
            if (owner.getKeyCert() != null && owner.getTrustedCert() != null)
            {
                this.client = new WebClient(owner.getKeyCert().getCertificate(),
                                            owner.getKeyCert().getPrivateKey(),
                                            owner.getTrustedCert().getTrustedCertificate())
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
            else
            {
                this.client = new WebClient(owner.getCertificatesPath() + "/certificate.pem",
                                            owner.getCertificatesPath() + "/key.pem",
                                            owner.getCertificatesPath() + "/ca.pem")
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

            InetAddress ownAddress = null;

            try
            {
                ownAddress = InetAddress.getLocalHost();
            }
            catch (UnknownHostException e)
            {
                log.error("Could not determine own address. Cause: {}", e.toString());
            }

            this.hostName = EnvVars.get("HOSTNAME", ownAddress != null ? ownAddress.getHostName() : "localhost");
        }

        public Map<String, BiConsumer<RoutingContext, Event>> getHandlerByOperationId()
        {
            return this.handlerByOperationId;
        }

        public void handle(final RoutingContext context)
        {
            final IpFamily ipFamily = context.get(OpenApiTask.DataIndex.IP_FAMILY.name());
            final String nfInstanceId = context.request().getParam("nfInstanceID");
            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());
            final Event event = new Event(operationId, String.class.getName(), context.request().path());

            if (log.isDebugEnabled())
            {
                if (nfInstanceId != null)
                {
                    log.debug("{}: Received {} request for NF-instance {} via {}, host is: {}. Receiving node is: {}:{}",
                              ipFamily,
                              operationId,
                              nfInstanceId,
                              getVia(context),
                              context.request().authority().toString(),
                              this.owner.getConfiguration().getOwnIpAddress(),
                              this.owner.webServerExt.get(0).actualPort()); // The port is the same for all IP families
                }
                else
                {
                    log.debug("{}: Received {} request via {}, host is: {}. Receiving node is: {}:{}",
                              ipFamily,
                              operationId,
                              getVia(context),
                              context.request().authority().toString(),
                              this.owner.getConfiguration().getOwnIpAddress(),
                              this.owner.webServerExt.get(0).actualPort()); // The port is the same for all IP families
                }
            }

            // FIXME:
            // The following code is wrong, key should be the operationId rather than the
            // nfInstanceId. This can be seen in other places where it is correct.
            // However, as all TCs rely on the faulty implementation, the correction has
            // been commented out below as it yielded heaps of failing TCs.
            final NfInstance nfInstance = this.owner.getNfInstances().get(nfInstanceId);
            nfInstance.getStatistics().getCountInHttpRequests().inc();
            this.owner.getNfInstances().get(null).getStatistics().getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();

            if (!Optional.ofNullable(this.owner.getConfiguration().getLoadTestMode()).orElse(this.owner.defaultLoadTestMode).isEnabled())
            {
                nfInstance.getStatistics().getHistoryOfEvents().put(event);
                nfInstance.addContext(context);
            }

            // FIXME:
            // Below (correct) code is commented out for the reason as stated in the comment
            // above.
//            this.owner.getNfInstances().get(operationId).getStatistics().getCountInHttpRequests().inc();
//            this.owner.getNfInstances().get(null).getStatistics().getCountInHttpRequestsPerIpFamily().get(ipFamily.ordinal()).inc();
//
//            if (!this.owner.getConfiguration().getLoadTestMode().isEnabled())
//            {
//                this.owner.getNfInstances().get(operationId).getStatistics().getHistoryOfEvents().put(event);
//                this.owner.getNfInstances().get(operationId).addContext(context);
//            }

            final Disturbance disturbance = this.getDisturbances().getNext();

            if (disturbance.getDrop() > 0)
            {
                if (disturbance.getDrop() == 1)
                {
                    log.debug("Dropping {} request.", operationId);
                }
                else
                {
                    log.debug("Dropping {} request and resetting the connection with error code {}.", operationId, disturbance.getDrop());
                    context.response().reset(disturbance.getDrop());
                }

                return;
            }

            context.put(CTX_DISTURBANCE, disturbance);

            if (disturbance.getDelayInMillis() > 0)
            {
                context.vertx().setTimer(disturbance.getDelayInMillis(), t ->
                {
                    log.debug("{} response has been delayed by {} ms", operationId, disturbance.getDelayInMillis());

                    this.handle(context, event);
                });
            }
            else
            {
                this.handle(context, event);
            }
        }

        private String formatIPv4Ipv6Addr(String ip)
        {
            String ipAddr = ip;

            if (ip.contains(":"))
            {
                if (!ip.contains("["))
                {
                    ipAddr = "[" + ip + "]";
                }
            }
            return ipAddr;
        }

        private void handle(final RoutingContext context,
                            final Event event)
        {
            log.debug("Request headers: {}", context.request().headers());

            final IpFamily ipFamily = context.get(OpenApiTask.DataIndex.IP_FAMILY.name());

            // Mirror the header as received from the client.
            Optional.ofNullable(context.request().getHeader(HD_3GPP_SBI_CORRELATION_INFO))
                    .ifPresent(header -> context.response().putHeader(HD_3GPP_SBI_CORRELATION_INFO, header));

            // Prepare for local direct replies (300 forwarding, errors, or if role is NF).
            // If role is C_SEPP or P_SEPP, will be overwritten by the response received
            // from the server which is forwarded to the client.
            Optional.ofNullable(context.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                    .ifPresent(header -> context.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, SbiNfPeerInfo.swapSrcAndDstFields(header)));

            final Disturbance disturbance = context.get(CTX_DISTURBANCE);

            // Add additional headers from the disturbance, if available.
            disturbance.getAdditionalHeaders().forEach(context.response().headers()::add);

            log.debug("Response headers (initial): {}", context.response().headers());

            final String operationId = context.get(OpenApiTask.DataIndex.OPERATION_ID.name());

            final HttpResponseStatus status = disturbance.getStatus();

            if (status != null && 300 <= status.code())
            {
                replyWithError(context, event.setResponse(status, "Result code set by test case."), null, null, null);
                this.owner.getNfInstances().get(operationId).getStatistics().getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                this.owner.getNfInstances().get(null).getStatistics().getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                return;
            }

            final List<String> vias = new ArrayList<>(context.request().headers().getAll(HD_VIA));
            String headerTargetApiRoot = context.request().getHeader(TARGET_APIROOT);
            Role role = null;
            boolean r15Behaviour = false;

            if (headerTargetApiRoot == null)
            {
                if (this.owner.getConfiguration().getOwnRole() == Configuration.Role.SEPP_OR_NF
                    || this.owner.getConfiguration().getOwnRole() == Configuration.Role.NF_ONLY)
                {
                    role = Role.NF;
                    log.debug("Role: acting as {}.", role.value());

                    context.response().putHeader(HD_X_ORIGIN, this.hostName);

                    final BiConsumer<RoutingContext, Event> handler = context.get(OpenApiTask.DataIndex.HANDLER.name());
                    handler.accept(context, event);
                    this.owner.getNfInstances()
                              .get(operationId)
                              .getStatistics()
                              .getCountOutHttpResponsesPerStatus()
                              .get(event.getResponse().getResultCode())
                              .inc();
                    this.owner.getNfInstances().get(null).getStatistics().getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                    return;
                }

                // Obviously no NF, but no TARGET_APIROOT header received -> R15 behaviour
                // wanted: take destination from :authority pseudo header instead. Create an
                // artificial TARGET_APIROOT header from it in order to be able to reuse the old
                // functionality below.

                headerTargetApiRoot = new StringBuilder(context.request().scheme()).append("://").append(context.request().authority().toString()).toString();
                r15Behaviour = true;
            }

            TargetApiRoot targetApiRoot = null;

            try
            {
                targetApiRoot = new TargetApiRoot(headerTargetApiRoot);
            }
            catch (MalformedURLException e)
            {
                replyWithError(context,
                               event.setResponse(HttpResponseStatus.BAD_REQUEST, "Could not forward '" + operationId + "' request. Cause: " + e.getMessage()),
                               null,
                               null,
                               null);

                this.owner.getNfInstances().get(operationId).getStatistics().getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                this.owner.getNfInstances().get(null).getStatistics().getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                return;
            }

            final boolean isOwnTarget = targetApiRoot.getHostName()
                                                     .isFqdn() ? targetApiRoot.getHostName().get().endsWith(this.owner.getConfiguration().getOwnDomain())
                                                               : targetApiRoot.getHostName()
                                                                              .get()
                                                                              .equals(this.formatIPv4Ipv6Addr(this.owner.getConfiguration().getOwnIpAddress()));

            log.debug("isOwnTarget={}, headerTargetApiRoot={}, ownDomain={}, ownIpAddress={}, ownRole=",
                      isOwnTarget,
                      targetApiRoot,
                      this.owner.getConfiguration().getOwnDomain(),
                      this.owner.getConfiguration().getOwnIpAddress(),
                      this.owner.getConfiguration().getOwnRole());

            final String file = new StringBuilder(context.request().path()).append((context.request().query() != null
                                                                                    && !context.request().query().isEmpty() ? "?" + context.request().query()
                                                                                                                            : ""))
                                                                           .toString();

            HttpRequest<Buffer> request;
            String urlStr;

            if (isOwnTarget) // target in own domain, copy TARGET_APIROOT header to URL
            {
                role = Role.P_SEPP;
                log.debug("Role: acting as {}.", role.value());

                final Url url = new Url(targetApiRoot.getScheme(),
                                        targetApiRoot.getHostName().get(),
                                        targetApiRoot.getPort(),
                                        new StringBuilder(targetApiRoot.getPrefix()).append(file).toString(),
                                        this.owner.getConfiguration().getOwnIpAddress());

                request = this.client.requestAbs(context.request().method(), url.getAddr(), urlStr = url.getUrl().toString());

                request.putHeader(HD_3GPP_SBI_NF_PEER_INFO,
                                  SbiNfPeerInfo.of(context.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO)).shiftDstSeppToSrcSepp().toString());
            }
            else // target in roaming-partner's domain
            {
                role = Role.C_SEPP;
                log.debug("Role: acting as {}.", role.value());

                final Configuration.Sepp sepp = this.owner.getConfiguration().findSepp(targetApiRoot);

                if (sepp == null)
                {
                    replyWithError(context,
                                   event.setResponse(HttpResponseStatus.SERVICE_UNAVAILABLE,
                                                     "Could not forward '" + operationId + "' request. Cause: no server found for target: "
                                                                                             + targetApiRoot.getHostName().get()),
                                   null,
                                   null,
                                   null);

                    this.owner.getNfInstances()
                              .get(operationId)
                              .getStatistics()
                              .getCountOutHttpResponsesPerStatus()
                              .get(event.getResponse().getResultCode())
                              .inc();
                    this.owner.getNfInstances().get(null).getStatistics().getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                    return;
                }

                final Url url = new Url(sepp.getScheme(), sepp.getFqdn(), sepp.getPort(), file, sepp.getIpAddress());
                request = this.client.requestAbs(context.request().method(), url.getAddr(), urlStr = url.getUrl().toString());

                request.putHeader(HD_3GPP_SBI_NF_PEER_INFO,
                                  SbiNfPeerInfo.of(context.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                                               .shiftDstSeppToSrcSepp()
                                               .setDstSepp(sepp.getFqdn())
                                               .toString());

                if (r15Behaviour)
                    request.virtualHost(targetApiRoot.getHostName().get()).port(targetApiRoot.getPort());
                else
                    request.putHeader(TARGET_APIROOT, targetApiRoot.toString());
            }

            try
            {
                final String via = new StringBuilder(context.request().scheme()).append("/2 ").append(this.hostName)
//                                                                                .append(" (")
//                                                                                .append(role.value())
//                                                                                .append(")")
                                                                                .toString();
                vias.add(via);
                request.putHeader(HD_VIA, vias);

                final String body = context.body().asString();
                final Single<HttpResponse<Buffer>> single = (body == null ? request.rxSend()
                                                                          : request.putHeader(HD_CONTENT_TYPE, context.request().getHeader("content-type"))
                                                                                   .rxSendBuffer(Buffer.buffer(body.getBytes())));

                log.debug("urlStr={}, r15Behaviour={}, body={}", urlStr, r15Behaviour, body);

                single.timeout(10, TimeUnit.SECONDS)//
                      .doOnSubscribe(d -> log.debug("Processing '{}' request.", operationId))
                      .subscribe(result ->
                      {
                          result.headers().forEach(e -> context.response().putHeader(e.getKey(), e.getValue()));

                          final List<String> rVias = new ArrayList<>(context.response().headers().getAll(HD_VIA));
                          rVias.add(via);
                          context.response().putHeader(HD_VIA, rVias);

                          /**
                           * For the 3gpp-sbi-nf-peer-info header for the response to the left
                           * <ul>
                           * <li>take srcinst and dstinst from the response from the right
                           * <li>take srcservinst and dstservinst from the response from the right
                           * <li>take dstsepp from srcsepp from the request from the left
                           * <li>take srcsepp from dstsepp from the response from the right
                           * </ul>
                           * 
                           * <pre>
                           * LEFT             SEPP             RIGHT
                           *   | ---lRequest--> | ---rRequest--> |
                           *   | <--lResponse-- | <--rResponse-- |
                           * </pre>
                           */
                          final SbiNfPeerInfo rSnpi = SbiNfPeerInfo.of(result.headers().get(HD_3GPP_SBI_NF_PEER_INFO));
                          final SbiNfPeerInfo lSnpi = SbiNfPeerInfo.of(context.request().getHeader(HD_3GPP_SBI_NF_PEER_INFO))
                                                                   .setSrcInst(rSnpi.getSrcInst())
                                                                   .setDstInst(rSnpi.getDstInst())
                                                                   .setSrcServInst(rSnpi.getSrcServInst())
                                                                   .setDstServInst(rSnpi.getDstServInst())
                                                                   .shiftSrcSeppToDstSepp()
                                                                   .setSrcSepp(rSnpi.getDstSepp());

                          context.response().putHeader(HD_3GPP_SBI_NF_PEER_INFO, lSnpi.toString());

                          log.debug("Response headers (final): {}", context.response().headers());

                          if (result.statusCode() < 200 || result.statusCode() > 299)
                              forwardExternalProblem(context, event, result);
                          else
                              forwardResult(context, event, result);

                          this.owner.getNfInstances()
                                    .get(operationId)
                                    .getStatistics()
                                    .getCountOutHttpResponsesPerStatus()
                                    .get(event.getResponse().getResultCode())
                                    .inc();
                          this.owner.getNfInstances().get(null).getStatistics().getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                      }, e ->
                      {
                          log.error("Error processing '{}' request.", operationId, e);

                          replyWithError(context,
                                         event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                           "Error processing '" + operationId + "' request. Cause: " + e.toString()),
                                         null,
                                         null,
                                         null);

                          this.owner.getNfInstances()
                                    .get(operationId)
                                    .getStatistics()
                                    .getCountOutHttpResponsesPerStatus()
                                    .get(event.getResponse().getResultCode())
                                    .inc();
                          this.owner.getNfInstances().get(null).getStatistics().getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
                      });
            }
            catch (final Exception e)
            {
                log.error("Error processing '{}' request.", operationId, e);

                replyWithError(context,
                               event.setResponse(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                                                 "Error processing '" + operationId + "' request. Cause: " + e.toString()),
                               null,
                               null,
                               null);

                this.owner.getNfInstances().get(operationId).getStatistics().getCountOutHttpResponsesPerStatus().get(event.getResponse().getResultCode()).inc();
                this.owner.getNfInstances().get(null).getStatistics().getCountOutHttpResponsesPerIpFamily().get(ipFamily.ordinal()).inc();
            }
        }

        protected abstract Configuration.Disturbances getDisturbances();
    }

    private static class CommandConfig extends MonitorAdapter.CommandBase
    {
        private SeppSimulator handler;

        public CommandConfig(final SeppSimulator handler)
        {
            super("config", "Usage: command=config[&data=<json-formatted-config-data>]");
            this.handler = handler;
        }

        @Override
        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {
            final String data = (String) request.getAdditionalProperties().get("data");

            log.info("data='{}'", data);

            if (data != null)
            {
                try
                {
                    this.handler.setConfiguration(json.readValue(data, Configuration.class));
                }
                catch (Exception e)
                {
                    log.error("Error deserializing configuration", e);

                    result.setAdditionalProperty("errorMessage",
                                                 HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Invalid argument: 'data='" + data + "'. Details: "
                                                                 + e.getMessage() + ".");
                    return HttpResponseStatus.BAD_REQUEST;
                }
            }

            final String role = (String) request.getAdditionalProperties().get("role");

            log.info("role='{}'", role);

            if (role != null)
            {
                try
                {
                    if (role.equals("SEPP_ONLY"))
                    {
                        this.handler.getConfiguration().setOwnRole(this.handler.getConfiguration().getOwnRole().SEPP_ONLY);
                    }
                    else if (role.equals("NF_ONLY"))
                    {
                        this.handler.getConfiguration().setOwnRole(this.handler.getConfiguration().getOwnRole().NF_ONLY);
                    }
                    else if (role.equals("SEPP_OR_NF"))
                    {
                        this.handler.getConfiguration().setOwnRole(this.handler.getConfiguration().getOwnRole().SEPP_OR_NF);
                    }

                }
                catch (Exception e)
                {
                    log.error("Seppsim role setting failed!", e);

                    result.setAdditionalProperty("errorMessage",
                                                 HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Given argument: 'role='" + role + "'. Details: "
                                                                 + e.getMessage() + ".");
                    return HttpResponseStatus.BAD_REQUEST;
                }
            }

            result.setAdditionalProperty("config", this.handler.getConfiguration());

            return HttpResponseStatus.OK;
        }
    }

    private static class CommandInfo extends MonitorAdapter.CommandBase
    {
        private SeppSimulator handler;

        public CommandInfo(final SeppSimulator handler)
        {
            super("info",
                  "Usage: command=info[&requestHeader=<name> | &allRequestHeaders[=<true|false>] | &responseHeader=<name> | &allResponseHeaders[=<true|false>] | &inRequests[=<true|false>] | &outResponses[=<opId>] | &clear[=<true|false>] | &authorityHeader[=<true|false>]");
            this.handler = handler;
        }

        @Override
        public HttpResponseStatus execute(final Result result,
                                          final Command request)
        {
            final String requestHeaderName = (String) request.getAdditionalProperties().get("requestHeader");
            final boolean requestBody = Boolean.parseBoolean((String) request.getAdditionalProperties().get("requestBody"));
            final String responseHeaderName = (String) request.getAdditionalProperties().get("responseHeader");
            final boolean inRequests = Boolean.parseBoolean((String) request.getAdditionalProperties().get("inRequests"));
            final String outResponses = (String) request.getAdditionalProperties().get("outResponses");
            final boolean clear = Boolean.parseBoolean((String) request.getAdditionalProperties().get("clear"));
            final boolean allRequestHeaders = Boolean.parseBoolean((String) request.getAdditionalProperties().get("allRequestHeaders"));
            final boolean allResponseHeaders = Boolean.parseBoolean((String) request.getAdditionalProperties().get("allResponseHeaders"));
            final boolean authorityHeader = Boolean.parseBoolean((String) request.getAdditionalProperties().get("authorityHeader"));

            log.debug("requestHeader='{}'", requestHeaderName);
            log.debug("requestBody='{}'", requestBody);
            log.debug("responseHeader='{}'", responseHeaderName);
            log.debug("inRequests='{}'", inRequests);
            log.debug("outResponses='{}'", outResponses);
            log.debug("clear='{}'", clear);
            log.debug("allRequestHeaders='{}'", allRequestHeaders);
            log.debug("allResponseHeaders='{}'", allResponseHeaders);
            log.debug("authorityHeader='{}'", authorityHeader);

            if (clear)
            {
                this.handler.getNfInstances().clear();
                return HttpResponseStatus.OK;
            }

            if (requestHeaderName != null)
            {
                result.setAdditionalProperty("requestHeader", this.handler.getNfInstances().get(null).getRequestHeader(requestHeaderName));
                return HttpResponseStatus.OK;
            }

            if (responseHeaderName != null)
            {
                result.setAdditionalProperty("responseHeader", this.handler.getNfInstances().get(null).getResponseHeader(responseHeaderName));
                return HttpResponseStatus.OK;
            }

            if (inRequests)
            {
                result.setAdditionalProperty("inRequests", this.handler.getNfInstances().get(null).getStatistics().getCountInHttpRequests());
                return HttpResponseStatus.OK;
            }

            if (allRequestHeaders)
            {
                result.setAdditionalProperty("allRequestHeaders", this.handler.getNfInstances().get(null).getAllRequestHeaders());
                return HttpResponseStatus.OK;
            }

            if (allResponseHeaders)
            {
                result.setAdditionalProperty("allResponseHeaders", this.handler.getNfInstances().get(null).getAllResponseHeaders());
                return HttpResponseStatus.OK;
            }

            if (authorityHeader)
            {
                result.setAdditionalProperty("authorityHeader", this.handler.getNfInstances().get(null).getAuthorityHeader());
                return HttpResponseStatus.OK;
            }

            if (outResponses != null)
            {
                result.setAdditionalProperty("outResponses",
                                             this.handler.getNfInstances().get(outResponses).getStatistics().getCountOutHttpResponsesPerStatus());
                return HttpResponseStatus.OK;
            }

            if (requestBody)
            {
                result.setAdditionalProperty("requestBody", this.handler.getNfInstances().get(null).getRequestBody());
                return HttpResponseStatus.OK;
            }

            result.setAdditionalProperty("errorMessage", HttpResponseStatus.BAD_REQUEST.reasonPhrase() + ": Invalid argument: 'header'.");
            return HttpResponseStatus.BAD_REQUEST;
        }
    }

    private static class HostName
    {
        private enum Type
        {
            IPV4,
            IPV6,
            FQDN
        }

        private final String data;
        private final Type dataType;

        public HostName(final String hostName)
        {
            this.data = hostName;

            if (this.data.startsWith("[")) // IPv6 address
            {
                this.dataType = Type.IPV6;
            }
            else if (this.data.matches("[0-9.:]+")) // IPv4 address
            {
                this.dataType = Type.IPV4;
            }
            else // FQDN
            {
                this.dataType = Type.FQDN;
            }
        }

        public String get()
        {
            return this.data;
        }

        public String getDomain()
        {
            if (this.dataType != Type.FQDN)
                return "";

            final int dot = this.data.indexOf('.');

            return dot > 0 ? this.data.substring(dot + 1) : this.data; // FQDN is host name and domain at the same time.
        }

        public boolean isFqdn()
        {
            return this.dataType == Type.FQDN;
        }

        public boolean isIpv4()
        {
            return this.dataType == Type.IPV4;
        }

        public boolean isIpv6()
        {
            return this.dataType == Type.IPV6;
        }

        @Override
        public String toString()
        {
            return new StringBuilder("{dataType=").append(this.dataType).append(", data=").append(this.data).append("}").toString();
        }
    }

    private static class TargetApiRoot
    {
        final String raw;
        final String scheme;
        final HostName hostName;
        final Integer port;
        final String prefix;

        public TargetApiRoot(final String targetApiRoot) throws MalformedURLException
        {
            this.raw = targetApiRoot;

            final URL url = new URL(targetApiRoot);

            this.scheme = url.getProtocol().toLowerCase();

            if (!this.scheme.startsWith("http"))
                throw new MalformedURLException("Header '3pp-sbi-target-apiroot': invalid protocol '" + this.scheme + "'. Must be one of ['http','https'].");

            this.hostName = new HostName(url.getHost());

            this.port = url.getPort() < 0 ? this.scheme.equals("http") ? 80 : 443 : url.getPort();

            this.prefix = url.getPath().isEmpty() ? url.getPath()
                                                  : url.getPath().endsWith("/") ? url.getPath().substring(0, url.getPath().length() - 1) : url.getPath();

            log.debug("scheme={}, hostName={}, port={}, prefix={}", this.scheme, this.hostName, this.port, this.prefix);
        }

        public HostName getHostName()
        {
            return this.hostName;
        }

        public Integer getPort()
        {
            return this.port;
        }

        public String getPrefix()
        {
            return this.prefix;
        }

        public String getScheme()
        {
            return this.scheme;
        }

        @Override
        public String toString()
        {
            return this.raw;
        }
    }

    private static final String CT_APPLICATION_3GPP_HAL_JSON = "application/3gppHal+json; charset=utf-8";
    private static final String CT_APPLICATION_JSON = "application/json; charset=utf-8";
    private static final String CT_APPLICATION_PROBLEM_JSON = "application/problem+json; charset=utf-8";

    private static final String ENV_POD_IPS = "POD_IPS";

    private static final String HD_AUTHORITY = ":authority";
    private static final String HD_CACHE_CONTROL = "cache-control";
    private static final String HD_CONTENT_TYPE = "content-type";
    private static final String HD_ETAG = "etag";
    private static final String HD_HOST = "host";
    private static final String HD_LAST_MODIFIED = "last-modified";
    private static final String HD_LOCATION = "location";
    private static final String HD_VIA = "via";
    private static final String HD_X_ORIGIN = "x-origin";
    private static final String HD_3GPP_SBI_CORRELATION_INFO = "3gpp-sbi-correlation-info";
    private static final String HD_3GPP_SBI_NF_PEER_INFO = "3gpp-sbi-nf-peer-info";

    private static final String NAPI_NOTIFICATIONS_YAML = "3gpp/Napi_Notifications.yaml";
    private static final String NAPI_TEST_YAML = "3gpp/Napi_Test.yaml";
    private static final int HTTP_KEEP_ALIVE_TIMEOUT_SECS = 3600; // 1 h
    private static final int HTTP_CONNECT_TIMEOUT_MILLIS = 2000; // 2 s

    private static final Logger log = LoggerFactory.getLogger(SeppSimulator.class);
    private static final ObjectMapper json = OpenApiObjectMapper.singleton();

    public static void main(final String[] args)
    {
        int exitStatus = 0;

        log.info("Starting SEPP simulator, version: {}", VersionInfo.get());

        try
        {
            final Map<String, String> opts = new HashMap<>();

            for (String arg : args)
            {
                String[] tokens = arg.split("=");

                if (tokens.length != 2) // Arguments must look like that: host=127.0.0.1
                    continue;

                opts.put(tokens[0], tokens[1]);
            }

            final List<String> hosts = List.of(Stream.of(EnvVars.get(ENV_POD_IPS, opts.containsKey("host") ? opts.get("host") : isIpv6() ? "::" : "0.0.0.0")
                                                                .split(","))
                                                     .map(String::strip)
                                                     .map(ip -> ip.contains(".") ? "0.0.0.0" : "[::]")
                                                     .reduce(new String(),
                                                             (result,
                                                              item) -> !result.startsWith("[") ? item : result));

            log.info("POD_IPS={}, host={}, hosts={}", EnvVars.get(ENV_POD_IPS), opts.get("host"), hosts);

            SeppSimulator.Builder.of(hosts, opts.containsKey("port") ? Integer.parseInt(opts.get("port")) : 80)
                                 .withPortTls(opts.containsKey("portTls") ? Integer.parseInt(opts.get("portTls")) : 443)
                                 .withDefaultLoadTestMode(new Configuration.LoadTestMode().setEnabled(true)) // Enable load test mode when deployed.
                                 .build()
                                 .run();
        }
        catch (final Exception e)
        {
            log.error("Exception caught", e);
            exitStatus = 1;
        }

        log.info("Stopped SEPP simulator.");

        System.exit(exitStatus);
    }

    private static boolean isIpv6() throws UnknownHostException
    {
        boolean result = InetAddress.getByName(EnvVars.get("ERIC_SEPPSIM_PORT_80_TCP_ADDR")) instanceof Inet6Address;
        log.info("result={}", result);
        return result;
    }

    private static void replyWithError(final RoutingContext context,
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
        catch (final JsonProcessingException e)
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
               .putHeader(HD_CONTENT_TYPE, CT_APPLICATION_PROBLEM_JSON)
               .putHeader(HD_LOCATION, context.request().absoluteURI())
               .end(problemStr);
    }

    private final NfInstance.Pool nfInstances;
    private final String certificatesPath;
    private final List<WebServer> webServerExt;
    private final List<WebServer> webServerExtTls;
    private final WebServer webServerInt;
    private final TrustedCert trustedCert;
    private final KeyCert keyCert;
    private final MonitorAdapter monitored;
    private final Configuration.LoadTestMode defaultLoadTestMode;

    private Configuration config;

    private final List<String> hosts;
    private final Integer port;

    private SeppSimulator(final Configuration.LoadTestMode defaultLoadTestMode,
                          final List<String> hosts,
                          final Integer port,
                          final Integer portTls,
                          final String certificatesPath,
                          final List<Pair<String, String>> specs3gpp,
                          final KeyCert keyCert,
                          final TrustedCert trustedCert,
                          final Boolean sni) throws Exception
    {
        this.defaultLoadTestMode = defaultLoadTestMode;
        this.hosts = hosts;
        this.port = port;
        this.config = new Configuration();
        this.nfInstances = new NfInstance.Pool();
        this.keyCert = keyCert;
        this.trustedCert = trustedCert;
        this.certificatesPath = certificatesPath != null ? certificatesPath : "";

        final boolean useTls = !this.certificatesPath.isEmpty() || (keyCert != null && trustedCert != null);

        log.info("loadTestMode={}, hosts={}, port={}, portTls={}, useTls={}", this.defaultLoadTestMode, hosts, port, portTls, useTls);

        this.webServerExt = hosts.stream()
                                 .map(host -> WebServer.builder().withHost(host).withPort(port).build(VertxInstance.get()))
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
                                                              .build(VertxInstance.get()))
                                        .collect(Collectors.toList());
            if (Boolean.TRUE.equals(sni))
                this.webServerExtTls.forEach(webServer -> webServer.getHttpOptions().setSni(true));
        }
        else
        {
            log.info("Creating tls server with cert path");
            this.webServerExtTls = useTls ? hosts.stream()
                                                 .map(host -> WebServer.builder()
                                                                       .withHost(host)
                                                                       .withPort(portTls)
                                                                       .withTls(certificatesPath)
                                                                       .build(VertxInstance.get()))
                                                 .collect(Collectors.toList())
                                          : null;
        }

        this.webServerInt = WebServer.builder().withHost(Utils.getLocalAddress()).withPort(8080).build(VertxInstance.get());

        final List<Context3> contexts = specs3gpp.stream().map(spec ->
        {
            if (spec.getFirst().equals(Specs3gpp.R17_N32_HANDSHAKE))
                return new OpenApiServer.Context3(spec.getFirst(), new N32Handshake(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NAMF_COMMUNICATION))
                return new OpenApiServer.Context3(spec.getFirst(), new NamfCommunication(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NAMF_EVENT_EXPOSURE))
                return new OpenApiServer.Context3(spec.getFirst(), new NamfEventExposure(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NAUSF_UE_AUTHENTICATION))
                return new OpenApiServer.Context3(spec.getFirst(), new NausfUeAuthentication(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NNRF_BOOTSTRAPPING))
                return new OpenApiServer.Context3(spec.getFirst(), new NnrfBootstrapping(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NNRF_NF_DISCOVERY))
                return new OpenApiServer.Context3(spec.getFirst(), new NnrfNfDiscovery(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NNRF_NF_MANAGEMENT))
                return new OpenApiServer.Context3(spec.getFirst(), new NnrfNfManagement(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NNSSAAF_NSSAA))
                return new OpenApiServer.Context3(spec.getFirst(), new NnssaafNssaa(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NPCF_SM_POLICY_CONTROL))
                return new OpenApiServer.Context3(spec.getFirst(), new NpcfSmPolicyControl(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NSMF_PDU_SESSION))
                return new OpenApiServer.Context3(spec.getFirst(), new NsmfPduSession(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NUDM_NIDDAU))
                return new OpenApiServer.Context3(spec.getFirst(), new NudmNiddau(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NUDM_PP))
                return new OpenApiServer.Context3(spec.getFirst(), new NudmParameterProvision(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NUDM_SDM))
                return new OpenApiServer.Context3(spec.getFirst(), new NudmSubscriberDataManagement(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NUDM_SSAU))
                return new OpenApiServer.Context3(spec.getFirst(), new NudmSsau(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NUDM_UEAU))
                return new OpenApiServer.Context3(spec.getFirst(), new NudmUeAuthentication(this));

            if (spec.getFirst().equals(Specs3gpp.R17_NUDM_UECM))
                return new OpenApiServer.Context3(spec.getFirst(), new NudmUeContextManagement(this));

            if (spec.getFirst().equals(NAPI_TEST_YAML))
                return new OpenApiServer.Context3(spec.getFirst(), new NapiTest(this));

            if (spec.getFirst().equals(NAPI_NOTIFICATIONS_YAML))
                return new OpenApiServer.Context3(spec.getFirst(), new NapiNotifications(this));

            return null; // Should never happen :)
        }).collect(Collectors.toList());

        this.webServerExt.forEach(webServer -> new OpenApiServer(webServer).configure2(IpFamily.of(webServer.getHttpOptions().getHost()), contexts));

        if (useTls)
            this.webServerExtTls.forEach(webServer ->
            {
                new OpenApiServer(webServer).configure2(IpFamily.of(webServer.getHttpOptions().getHost()), contexts);
                webServer.getHttpOptions().setSni(true);
            });

        this.monitored = new MonitorAdapter(this.webServerInt,
                                            Arrays.asList(new MonitorAdapter.CommandCounter(this),
                                                          new MonitorAdapter.CommandEsa(this),
                                                          new CommandConfig(this),
                                                          new CommandInfo(this),
                                                          new MonitorAdapter.CommandCounter(this)),
                                            Arrays.asList(new CommandConfig(this)));
    }

    public String getCertificatesPath()
    {
        return this.certificatesPath;
    }

    public synchronized Configuration getConfiguration()
    {
        return this.config;
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

            for (Iterator<Entry<String, NfInstance>> itInstance = this.nfInstances.iterator(); itInstance.hasNext();)
            {
                final Entry<String, NfInstance> instance = itInstance.next();

                {
                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList("nfInstanceId")).append('=').append(Arrays.asList(instance.getKey()));
                    inRequests.add(new Instance(b.toString(), (double) instance.getValue().getStatistics().getCountInHttpRequests().get(readThenClear)));
                }

                for (Iterator<Entry<Integer, Count>> itCount = instance.getValue().getStatistics().getCountOutHttpResponsesPerStatus().iterator();
                     itCount.hasNext();)
                {
                    final Entry<Integer, Count> count = itCount.next();

                    final StringBuilder b = new StringBuilder();
                    b.append(Arrays.asList("nfInstanceId", "status")).append('=').append(Arrays.asList(instance.getKey(), count.getKey().toString()));
                    outAnswers.add(new Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                }

                for (Iterator<Entry<Integer, Count>> itCount = instance.getValue().getStatistics().getCountInHttpRequestsPerIpFamily().iterator();
                     itCount.hasNext();)
                {
                    final Entry<Integer, Count> count = itCount.next();

                    final StringBuilder b = new StringBuilder();

                    b.append(Arrays.asList("nfInstanceId", "ipFamily"))
                     .append('=')
                     .append(Arrays.asList(instance.getKey(), IpFamily.values()[count.getKey()].toString()));
                    inRequestsPerIpFamily.add(new Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                }

                for (Iterator<Entry<Integer, Count>> itCount = instance.getValue().getStatistics().getCountOutHttpResponsesPerIpFamily().iterator();
                     itCount.hasNext();)
                {
                    final Entry<Integer, Count> count = itCount.next();

                    final StringBuilder b = new StringBuilder();

                    b.append(Arrays.asList("nfInstanceId", "ipFamily"))
                     .append('=')
                     .append(Arrays.asList(instance.getKey(), IpFamily.values()[count.getKey()].toString()));
                    outAnswersPerIpFamily.add(new Instance(b.toString(), (double) count.getValue().get(readThenClear)));
                }
            }

            result.add(new Counter("eric_seppsim_http_in_requests_ipfamily_total", "Number of incoming HTTP requests per IP family", inRequestsPerIpFamily));
            result.add(new Counter("eric_seppsim_http_out_answers_ipfamily_total", "Number of outgoing HTTP answers per IP family", outAnswersPerIpFamily));
            result.add(new Counter("eric_seppsim_http_in_requests_total", "Number of incoming HTTP requests", inRequests));
            result.add(new Counter("eric_seppsim_http_out_answers_total", "Number of outgoing HTTP answers", outAnswers));
        }

        return result;
    }

    @Override
    public List<Event.Sequence> getEsa()
    {
        final List<Event.Sequence> result = new ArrayList<>();

        for (Iterator<Entry<String, NfInstance>> it = this.nfInstances.iterator(); it.hasNext();)
            result.add(it.next().getValue().getStatistics().getHistoryOfEvents());

        return result;
    }

    /**
     * This method is just used by test. Returns the concatenation of host and port.
     * Be aware that host will always be the first of hosts. This should be no
     * problem as long as test calls the build method for just one host address.
     * 
     * @return Concatenated string host:port
     */
    public String getHostAndPort()
    {
        return hosts.get(0) + ":" + port;
    }

    public KeyCert getKeyCert()
    {
        return keyCert;
    }

    public NfInstance.Pool getNfInstances()
    {
        return this.nfInstances;
    }

    public TrustedCert getTrustedCert()
    {
        return trustedCert;
    }

    @Override
    public void run()
    {
        Completable.complete()
                   .andThen(Flowable.fromIterable(this.webServerExt).flatMapCompletable(RouterHandler::startListener))
                   .andThen(this.webServerExtTls != null ? Flowable.fromIterable(this.webServerExtTls).flatMapCompletable(RouterHandler::startListener)
                                                         : Completable.complete())
                   .andThen(this.webServerInt.startListener())
                   .andThen(this.monitored.start())
                   .andThen(Completable.create(emitter ->
                   {
                       log.info("Registering shutdown hook");
                       Runtime.getRuntime().addShutdownHook(new Thread(() ->
                       {
                           log.info("Shutdown hook called");
                           this.stop().blockingAwait();
                           emitter.onComplete();
                       }));
                   }))
                   .blockingAwait();
    }

    public synchronized SeppSimulator setConfiguration(Configuration config)
    {
        this.config = config;
        return this;
    }

    public Completable stop()
    {
        return Completable.complete()
                          .andThen(this.monitored.stop().onErrorComplete())
                          .andThen(Flowable.fromIterable(this.webServerExt).flatMapCompletable(webServer -> webServer.stopListener().onErrorComplete()))
                          .andThen(this.webServerExtTls != null ? Flowable.fromIterable(this.webServerExtTls)
                                                                          .flatMapCompletable(webServer -> webServer.stopListener().onErrorComplete())
                                                                : Completable.complete())
                          .andThen(this.webServerInt.stopListener().onErrorComplete());
    }

}
