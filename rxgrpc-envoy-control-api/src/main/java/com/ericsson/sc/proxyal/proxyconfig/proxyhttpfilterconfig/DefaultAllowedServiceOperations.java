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
 * Created on: Apr 17, 2024
 *     Author: xvonmar
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import java.util.ArrayList;
import java.util.List;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.MessageSelector;

public class DefaultAllowedServiceOperations
{
    public static final String NSFM_PDUSESSION = "nsmf-pdusession";
    public static final String NDUM_UECM = "nudm-uecm";
    public static final String NAUSF_AUTH = "nausf-auth";
    public static final String NUDM_SDM = "nudm-sdm";
    public static final String NUDM_EE = "nudm-ee";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_PATCH = "PATCH";
    public static final String HTTP_DELETE = "DELETE";
    public static final String HTTP_PUT = "PUT";
    public static final String HTTP_GET = "GET";

    List<MessageSelector> operations = new ArrayList<>();

    public DefaultAllowedServiceOperations()
    {
        createAllowedServiceOperations();
    }

    private List<MessageSelector> createAllowedServiceOperations()
    {
        // TS29502_Nsmf_PDUSession.yaml
        //

        // TS29502_Nsmf_PDUSession.yaml /sm-contexts post PostSmContexts Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NSFM_PDUSESSION))
                                      .addAllResourceMatchers(List.of("/sm-contexts"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29502_Nsmf_PDUSession.yaml /sm-contexts post / callback / post
        // smContextStatusNotification Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("Nsmf_PDUSession_smContextStatusNotification"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(true)
                                      .build());

        // TS29502_Nsmf_PDUSession.yaml /sm-contexts/{smContextRef}/retrieve post
        // RetrieveSmContext Rel15, Rel16
        // TS29502_Nsmf_PDUSession.yaml /sm-contexts/{smContextRef}/modify post
        // UpdateSmContext Rel15, Rel16
        // TS29502_Nsmf_PDUSession.yaml /sm-contexts/{smContextRef}/release post
        // ReleaseSmContext Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NSFM_PDUSESSION))
                                      .addAllResourceMatchers(List.of("/sm-contexts/.+/(retrieve|modify|release)"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions post PostPduSessions Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NSFM_PDUSESSION))
                                      .addAllResourceMatchers(List.of("/pdu-sessions"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions post / callback / post
        // NotifyStatus Rel15, Rel16
        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions post / callback / post
        // ModifyPduSession Rel15, Rel16
        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions post / callback / post
        // NotifyStatus-isfm Rel16
        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions post / callback / post
        // ModifyPduSession-ismf Rel16
        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions post / callback / post
        // TransferMtData Rel16
        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions post / callback / post
        // TransferMtData-ismf Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("Nsmf_PDUSession_StatusNotify",
                                                              "Nsmf_PDUSession_Update",
                                                              "Nsmf_PDUSession_statusNotification-ismf",
                                                              "Nsmf_PDUSession_update-ismf",
                                                              "Nsmf_PDUSession_transferMtData",
                                                              "Nsmf_PDUSession_transferMtData-ismf"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(true)
                                      .build());

        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions/{pduSessionRef}/modify post
        // UpdatePduSession Rel15, Rel16
        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions/{pduSessionRef}/release post
        // ReleasePduSession Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NSFM_PDUSESSION))
                                      .addAllResourceMatchers(List.of("/pdu-sessions/.+/(modify|release)"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions/{pduSessionRef}/retrieve post
        // RetrievePduSession Rel16
        // TS29502_Nsmf_PDUSession.yaml /pdu-sessions/{pduSessionRef}/transfer-mo-data
        // post TransferMoData Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NSFM_PDUSESSION))
                                      .addAllResourceMatchers(List.of("/pdu-sessions/.+/(retrieve|transfer-mo-data)"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_EE.yaml
        //

        // TS29503_Nudm_EE.yaml /{ueIdentity}/ee-subscriptions post CreateEeSubscription
        // Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_EE))
                                      .addAllResourceMatchers(List.of("/(msisdn-[0-9]{5,15}|extid-[^@]+@[^@]+|extgroupid-[^@]+@[^@]+|anyUE)/ee-subscriptions"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_EE.yaml /{ueIdentity}/ee-subscriptions post / callback / post
        // eventOccurrenceNotification Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("Nudm_EE_eventOccurrenceNotification"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(true)
                                      .build());

        // TS29503_Nudm_EE.yaml /{ueIdentity}/ee-subscriptions/{subscriptionId} delete
        // DeleteEeSubscription Rel15, Rel16
        // TS29503_Nudm_EE.yaml /{ueIdentity}/ee-subscriptions/{subscriptionId} patch
        // UpdateEeSubscription Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_EE))
                                      .addAllResourceMatchers(List.of("/(msisdn-[0-9]{5,15}|extid-[^@]+@[^@]+|extgroupid-[^@]+@[^@]+|anyUE)/ee-subscriptions/[^/]+"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_DELETE, HTTP_PATCH))
                                      .setIsNotification(false)
                                      .build());
        // TS29503_Nudm_SDM.yaml
        //

        // TS29503_Nudm_SDM.yaml /{supi}/am-data/cag-ack put CAGAck Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/(imsi-[0-9]{5,15}|nai-.+|gci-.+|gli-.+)/am-data/cag-ack"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_PUT))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_SDM.yaml /{supi} get GetDataSets Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/(imsi-[0-9]{5,15}|nai-.+|gci-.+|gli-.+)"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_GET))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_SDM.yaml /{supi}/nssai get GetNSSAI Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/am-data get GetAmData Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/smf-select-data get GetSmfSelData Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/ue-context-in-smsf-data get GetUeCtxInSmsfData
        // Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/trace-data get GetTraceConfigData Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/sm-data get GetSmData Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/sms-data get GetSmsData Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/sms-mng-data get GetSmsMngtData Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/lcs-mo-data get GetLcsMoData Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/lcs-bca-data get GetLcsBcaData Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/v2x-data get GetV2xData Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/(imsi-[0-9]{5,15}|nai-.+|gci-.+|gli-.+)/(nssai|am-data|smf-select-data|ue-context-in-smf-data|ue-context-in-smsf-data|trace-data|sm-data|sms-data|sms-mng-data|lcs-mo-data|lcs-bca-data|v2x-data)"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_GET))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_SDM.yaml /{supi}/sdm-subscriptions post Subscribe Rel15
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/(imsi-[0-9]{5,15}|nai-.+|gci-.+|gli-.+)/sdm-subscriptions"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_SDM.yaml /{supi}/sdm-subscriptions/{subscriptionId} delete
        // Unsubscribe Rel15
        // TS29503_Nudm_SDM.yaml /{supi}/sdm-subscriptions/{subscriptionId} patch Modify
        // Rel15
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/(imsi-[0-9]{5,15}|nai-.+|gci-.+|gli-.+)/sdm-subscriptions/[^/]+"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_DELETE, HTTP_PATCH))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_SDM.yaml /{supi}/am-data/sor-ack put SorAckInfo Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/am-data/upu-ack put UpuAck Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /{supi}/am-data/subscribed-snssais-ack put S-NSSAIsAck
        // Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/(imsi-[0-9]{5,15}|nai-.+|gci-.+|gli-.+)/am-data/(sor-ack|upu-ack|subscribed-snssais-ack)"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_PUT))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_SDM.yaml /shared-data get GetSharedData Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/shared-data"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_GET))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_SDM.yaml /shared-data-subscriptions post SubscribeToSharedData
        // Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/shared-data-subscriptions"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_SDM.yaml /shared-data-subscriptions/{subscriptionId} delete
        // UnsubscribeForSharedData Rel15, Rel16
        // TS29503_Nudm_SDM.yaml /shared-data-subscriptions/{subscriptionId} patch
        // ModifySharedDataSubs Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/shared-data-subscriptions/[^/]+"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_DELETE, HTTP_PATCH))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_SDM.yaml /{supi}/am-data/update-sor post UpdateSORInfo Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/(imsi-[0-9]{5,15}|nai-.+|gci-.+|gli-.+)/am-data/update-sor"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_SDM.yaml /group-data/group-identifiers get GetGroupIdentifiers
        // Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NUDM_SDM))
                                      .addAllResourceMatchers(List.of("/group-data/group-identifiers"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_GET))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_UECM.yaml
        //

        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/amf-3gpp-access put
        // 3GppRegistration Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/amf-3gpp-access patch
        // Update3GppRegistration Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/amf-non-3gpp-access put
        // Non3GppRegistration Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/amf-non-3gpp-access patch
        // UpdateNon3GppRegistration Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NDUM_UECM))
                                      .addAllResourceMatchers(List.of("/(imsi-[0-9]{5,15}|nai-.+|msisdn-[0-9]{5,15}|extid-[^@]+@[^@]+|gci-.+|gli-.+)/registrations/amf-3gpp-access",
                                                                      "/(imsi-[0-9]{5,15}|nai-.+|msisdn-[0-9]{5,15}|extid-[^@]+@[^@]+|gci-.+|gli-.+)/registrations/amf-non-3gpp-access"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_PUT, HTTP_PATCH))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/amf-3gpp-access put / callback /
        // post deregistrationeNotification Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/amf-3gpp-access put / callback /
        // post pcscfRestorationNotification Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/amf-non-3gpp-access put /
        // callback / post deregistrationeNotification Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/amf-non-3gpp-access put /
        // callback / post pcscfRestorationNotification Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/smf-registrations/{pduSessionId}
        // put / callback / post pcscfRestorationNotification Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/smf-registrations/{pduSessionId}
        // put / callback / post deregistrationeNotification Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("Nudm_UECM_PCSCFRestorationNotification", "Nudm_UECM_DeregistrationNotification"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(true)
                                      .build());

        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/smsf-3gpp-access put
        // 3GppSmsfRegistration Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/smsf-3gpp-access delete
        // 3GppSmsfDeregistration Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/smsf-non-3gpp-access put
        // Non3GppSmsfRegistration Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/smsf-non-3gpp-access delete
        // Non3GppSmsfDeregistration Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/smf-registrations/{pduSessionId}
        // put Registration Rel15, Rel16
        // TS29503_Nudm_UECM.yaml /{ueId}/registrations/smf-registrations/{pduSessionId}
        // delete SmfDeregistration Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NDUM_UECM))
                                      .addAllResourceMatchers(List.of("/(imsi-[0-9]{5,15}|nai-.+|msisdn-[0-9]{5,15}|extid-[^@]+@[^@]+|gci-.+|gli-.+)/registrations/smf-registrations/[0-9]+",
                                                                      "/(imsi-[0-9]{5,15}|nai-.+|msisdn-[0-9]{5,15}|extid-[^@]+@[^@]+|gci-.+|gli-.+)/registrations/smsf-3gpp-access",
                                                                      "/(imsi-[0-9]{5,15}|nai-.+|msisdn-[0-9]{5,15}|extid-[^@]+@[^@]+|gci-.+|gli-.+)/registrations/smsf-non-3gpp-access"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_PUT, HTTP_DELETE))
                                      .setIsNotification(false)
                                      .build());

        // TS29503_Nudm_UECM.yaml /{ueId}/registrations get GetRegistrations Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NDUM_UECM))
                                      .addAllResourceMatchers(List.of("/(imsi-[0-9]{5,15}|nai-.+|msisdn-[0-9]{5,15}|extid-[^@]+@[^@]+|gci-.+|gli-.+)/registrations"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_GET))
                                      .setIsNotification(false)
                                      .build());

        // TS29509_Nausf_UEAuthentication.yaml
        //

        // TS29509_Nausf_UEAuthentication.yaml /ue-authentications post Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NAUSF_AUTH))
                                      .addAllResourceMatchers(List.of("/ue-authentications"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29509_Nausf_UEAuthentication.yaml
        // /ue-authentications/{authCtxId}/5g-aka-confirmation put Rel15, Rel16
        // TS29509_Nausf_UEAuthentication.yaml
        // /ue-authentications/{authCtxId}/5g-aka-confirmation delete
        // Delete5gAkaAuthenticationResult Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NAUSF_AUTH))
                                      .addAllResourceMatchers(List.of("/ue-authentications/[^/]+/5g-aka-confirmation")) // no slashes
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_PUT, HTTP_DELETE))
                                      .setIsNotification(false)
                                      .build());

        // TS29509_Nausf_UEAuthentication.yaml
        // /ue-authentications/{authCtxId}/eap-session post EapAuthMethod Rel15, Rel16
        // TS29509_Nausf_UEAuthentication.yaml
        // /ue-authentications/{authCtxId}/eap-session delete
        // DeleteEapAuthenticationResult Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NAUSF_AUTH))
                                      .addAllResourceMatchers(List.of("/ue-authentications/[^/]+/eap-session"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST, HTTP_DELETE))
                                      .setIsNotification(false)
                                      .build());

        // TS29509_Nausf_UEAuthentication.yaml /rg-authentications post Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of(NAUSF_AUTH))
                                      .addAllResourceMatchers(List.of("/rg-authentications"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29510_Nnrf_AccessToken.yaml
        //

        // TS29510_Nnrf_AccessToken.yaml /oauth2/token post AccessTokenRequest Rel15,
        // Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("oauth2"))
                                      .addAllResourceMatchers(List.of("/token"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29510_Nnrf_Bootstrapping.yaml
        //

        // TS29510_Nnrf_Bootstrapping.yaml /bootstrapping get BootstrappingInfoRequest
        // Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("bootstrapping"))
                                      .addAllResourceMatchers(List.of("/"))
                                      .addAllHttpMethods(List.of(HTTP_GET))
                                      .setIsNotification(false)
                                      .build());

        // TS29510_Nnrf_NFDiscovery.yaml
        //

        // TS29510_Nnrf_NFDiscovery.yaml /searches/{searchId} get RetrieveStoredSearch
        // Rel16
        // TS29510_Nnrf_NFDiscovery.yaml /nf-instances get SearchNFInstances Rel15,
        // Rel16
        // TS29510_Nnrf_NFDiscovery.yaml /searches/{searchId}/complete get
        // RetrieveCompleteSearch Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("nnrf-disc"))
                                      .addAllResourceMatchers(List.of("/searches/[^/]+(/complete)?", "/nf-instances"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_GET))
                                      .setIsNotification(false)
                                      .build());

        // TS29518_Namf_EventExposure.yaml
        //

        // TS29518_Namf_EventExposure.yaml /subscriptions post / callback / post
        // onEventReport Rel15, Rel16
        // TS29518_Namf_EventExposure.yaml /subscriptions post / callback / post
        // onSubscriptionIdChangeEvtReport Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("Namf_EventExposure_Notify"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(true)
                                      .build());

        // TS29518_Namf_Location.yaml
        //

        // TS29518_Namf_Location.yaml /{ueContextId}/provide-loc-info post
        // ProvideLocationInfo Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("namf-loc"))
                                      .addAllResourceMatchers(List.of("/(5g-guti-[0-9]{5,6}[0-9a-fA-F]{14}|imsi-[0-9]{5,15}|nai-.+| gci-.+|gli-.+|imei-[0-9]{15}|imeisv-[0-9]{16})/provide-loc-info"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        // TS29518_Namf_MT.yaml
        //

        // TS29518_Namf_MT.yaml /ue-contexts/{ueContextId} get
        // ProvideDomainSelectionInfo Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("namf-mt"))
                                      .addAllResourceMatchers(List.of("/ue-contexts/(5g-guti-[0-9]{5,6}[0-9a-fA-F]{14}|imsi-[0-9]{5,15}|nai-.+| gci-.+|gli-.+|imei-[0-9]{15}|imeisv-[0-9]{16})"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_GET))
                                      .setIsNotification(false)
                                      .build());

        // TS29531_Nnssf_NSSelection.yaml
        //

        // TS29531_Nnssf_NSSelection.yaml /network-slice-information get NSSelectionGet
        // Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("nnssf-nsselection"))
                                      .addAllResourceMatchers(List.of("/network-slice-information"))
                                      .addAllApiVersions(List.of("v2"))
                                      .addAllHttpMethods(List.of(HTTP_GET))
                                      .setIsNotification(false)
                                      .build());

        // TS29573_JOSEProtectedMessageForwarding.yaml
        //

        // TS29573_JOSEProtectedMessageForwarding.yaml /n32f-process post
        // PostN32fProcess Rel15, Rel16
        // TS29573_JOSEProtectedMessageForwarding.yaml /n32f-process options
        // N32fProcessOptions Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("n32f-forward"))
                                      .addAllResourceMatchers(List.of("/n32f-process"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST, "OPTIONS"))
                                      .setIsNotification(false)
                                      .build());

        // TS29573_N32_Handshake.yaml
        //

        // TS29573_N32_Handshake.yaml /exchange-capability post PostExchangeCapability
        // Rel15, Rel16
        // TS29573_N32_Handshake.yaml /exchange-params post PostExchangeParams Rel15,
        // Rel16
        // TS29573_N32_Handshake.yaml /n32f-terminate post PostN32fTerminate Rel15,
        // Rel16
        // TS29573_N32_Handshake.yaml /n32f-error post PostN32fError Rel15, Rel16
        operations.add(MessageSelector.newBuilder()
                                      .addAllApiNames(List.of("n32c-handshake"))
                                      .addAllResourceMatchers(List.of("/exchange-capability", "/exchange-params", "/n32f-terminate", "/n32f-error"))
                                      .addAllApiVersions(List.of("v1"))
                                      .addAllHttpMethods(List.of(HTTP_POST))
                                      .setIsNotification(false)
                                      .build());

        return operations;
    }

    /**
     * @return the operations
     */
    public List<MessageSelector> getOperations()
    {
        return operations;
    }

    /**
     * @param filters the filters to set
     */
    public void setOperations(List<MessageSelector> operations)
    {
        this.operations = operations;
    }

}
