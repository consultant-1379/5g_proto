package com.ericsson.sc.sepp.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.sc.sepp.model.MessageDatum;
import com.ericsson.sc.sepp.model.CustomFqdnLocator.HttpMethod;
import com.ericsson.sc.sepp.model.CustomFqdnLocator.MessageOrigin;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.Path;
import com.ericsson.sc.sepp.model.SearchInHeader;
import com.ericsson.sc.sepp.model.SearchInMessageBody;
import com.ericsson.sc.sepp.model.SearchInQueryParameter;
import com.ericsson.utilities.common.Triplet;

public class ThLocatorsScrambling extends ThLocators
{
    private static final String SERVICE_VERSION = "v1";
    private static final String SERVICE_VERSION_2 = "v2";
    private static final String NNRF_DISCOVERY = "nnrf-disc";
    private static final String NNRF_MANAGEMENT = "nnrf-nfm";
    private static final String NNRF_MANAGEMENT_NOTIFY = "Nnrf_NFManagement_NFStatusNotify";
    private static final String NSMF_PDUSESSION = "nsmf-pdusession";
    private static final String NSMF_PDUSESSION_UPDATE = "Nsmf_PDUSession_Update";
    private static final String NSMF_PDUSESSION_STATUS_NOTIFY = "Nsmf_PDUSession_StatusNotify";
    private static final String NUDM_SUBSCRIBERDATAMANAGEMENT = "nudm-sdm";
    private static final String NUDM_UECONTEXTMANAGEMENT = "nudm-uecm";
    private static final String NAMF_COMMUNICATIONS = "namf-comm";
    private static final String NAMF_EVENTEXPOSURE = "namf-evts";
    private static final String NAMF_EVENTEXPOSURE_NOTIFY = "Namf_EventExposure_Notify";
    private static final String NNSSAAF_NSSAA = "nnssaaf-nssaa";
    private static final String LOCATION_HEADER = "location";
    private static final String AUTHORITY_HEADER = ":authority";
    private static final String TARGET_API_ROOT = "3gpp-sbi-target-apiroot";
    private static final String TARGET_API_ROOT_CONDITION = "not req.header['3gpp-Sbi-target-apiRoot'] exists";
    private static final String TARGET_NF_TYPE_CONDITION = "not (var.target_nf_type == 'NRF')";
    private static final String SERVICE_CONDITION = "not (req.apiname == 'nnrf-disc') and not(req.apiname == 'nnrf-nfm') and not(req.apiname == 'bootstrapping') and not(req.apiname == 'nnrf-oauth2')";
    private static final String NF_TYPE_CONDITION = "not (var.nf_type == 'NRF') and (var.nf_type exists)";

    private static final String NF_INSTANCES = "/nf-instances";
    private static final String NF_STATUS_NOTIFY_URI_STRING = "/nfStatusNotificationUri";
    private static final String SUBSCRIPTIONS = "/subscriptions";
    private static final String SUBSCRIPTIONS_ID = "/subscriptions/.*";
    private static final String SEARCH_ID = "/searches/.*";
    private static final String PDU_SESSION = "/pdu-sessions";
    private static final String SDM_SUBSCRIPTIONS = "/.*/sdm-subscriptions";
    private static final String SDM_SUBSCRIPTIONS_ID = "/.*/sdm-subscriptions/.*";
    private static final String SDM_SUBSCRIPTIONS_SHARED_DATA = "/shared-data-subscriptions";
    private static final String SDM_SUBSCRIPTIONS_SHARED_DATA_ID = "/shared-data-subscriptions/.*";
    private static final String AMF_REGISTRATIONS = "/.*/registrations/amf-3gpp-access";
    private static final String AMF_REGISTRATIONS_NON_3GPP_ACCESS = "/.*/registrations/amf-non-3gpp-access";
    private static final String AMF_EE_SUB_ID_STRING = "/amfEeSubscriptionId";
    private static final String SMF_REGISTRATION = "/.*/registrations/smf-registrations/.*";
    private static final String SMSF_REGISTRATIONS = "/.*/registrations/smsf-3gpp-access";
    private static final String SMSF_REGISTRATIONS_NON_3GPP_ACCESS = "/.*/registrations/smsf-non-3gpp-access";
    private static final String SLICE_AUTHENTICATIONS = "/slice-authentications";
    private static final String SMF_PDU_SESSION_MODIFY = "/pdu-sessions/.*/modify";
    private static final String SM_CONTEXT_RETRIEVE = "/sm-contexts/.*/retrieve";
    private static final String PDU_SESSION_RETRIEVE = "/pdu-session/.*/retrieve";
    private static final String REGISTRATIONS = "/.*/registrations";

    public ThLocatorsScrambling(String fqdnHidingState,
                                String onFqdnHidingErrorAction,
                                NfInstance instance)
    {
        super(fqdnHidingState, onFqdnHidingErrorAction, instance);
    }

    public List<LocatorWrapper> createDefaultSelectors(NfInstance seppInst)
    {
        var defaultSelectors = new ArrayList<LocatorWrapper>();
        List<MessageDatum> messageData = new ArrayList<>();

        messageData.addAll(seppInst.getMessageData());

        MessageDatum tarMessageData = new MessageDatum().withName("tar_nfType")
                                                        .withPath(new Path())
                                                        .withExtractorRegex("target-nf-type=(?P<nf_type_1>[A-Za-z]+)")
                                                        .withVariableName("target_nf_type");
        messageData.add(tarMessageData);
        MessageDatum nfTypeMessageData = new MessageDatum().withName("nfType").withBodyJsonPointer("/nfProfile/nfType").withVariableName("nf_type");
        messageData.add(nfTypeMessageData);
        seppInst.setMessageData(messageData);

        var messageLocatorBuilder = new TphMessageLocatorBuilder();

        messageLocatorBuilder.createMessageSelector("ScrDefault1",
                                                    NNRF_DISCOVERY,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.GET,
                                                    NF_INSTANCES,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createRequestMessageDataRef(List.of(tarMessageData.getName()));
        messageLocatorBuilder.createQueryParameterLocator(Set.of(Triplet.of("target-nf-fqdn",
                                                                            TARGET_NF_TYPE_CONDITION,
                                                                            SearchInQueryParameter.PerformActionOnAttribute.DE_SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/nfInstances/*/fqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/interPlmnFqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServices/*/fqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServices/*/defaultNotificationSubscriptions/*/callbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServices/*/defaultNotificationSubscriptions/*/interPlmnCallbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServices/*/interPlmnFqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServiceList/*/defaultNotificationSubscriptions/*/callbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServiceList/*/defaultNotificationSubscriptions/*/interPlmnCallbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServiceList/*/fqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServiceList/*/interPlmnFqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/defaultNotificationSubscriptions/*/interPlmnCallbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/defaultNotificationSubscriptions/*/callbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));

        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault2",
                                                    NNRF_DISCOVERY,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.GET,
                                                    SEARCH_ID,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createRequestMessageDataRef(List.of(tarMessageData.getName()));
        messageLocatorBuilder.createQueryParameterLocator(Set.of(Triplet.of("target-nf-fqdn",
                                                                            TARGET_NF_TYPE_CONDITION,
                                                                            SearchInQueryParameter.PerformActionOnAttribute.DE_SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/nfInstances/*/fqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/interPlmnFqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServices/*/fqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServices/*/defaultNotificationSubscriptions/*/callbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServices/*/defaultNotificationSubscriptions/*/interPlmnCallbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServices/*/interPlmnFqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServiceList/*/defaultNotificationSubscriptions/*/callbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServiceList/*/defaultNotificationSubscriptions/*/interPlmnCallbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServiceList/*/fqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/nfServiceList/*/interPlmnFqdn",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/defaultNotificationSubscriptions/*/interPlmnCallbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/nfInstances/*/defaultNotificationSubscriptions/*/callbackUri",
                                                                                 TARGET_NF_TYPE_CONDITION,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));

        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault3",
                                                    NNRF_MANAGEMENT_NOTIFY,
                                                    SERVICE_VERSION,
                                                    true,
                                                    HttpMethod.POST,
                                                    null,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageDataRef(List.of(nfTypeMessageData.getName()));
        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/nfProfile/fqdn",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/interPlmnFqdn",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/nfServices/*/fqdn",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/nfServices/*/interPlmnFqdn",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/nfServices/*/defaultNotificationSubscriptions/*/interPlmnCallbackUri",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/nfServices/*/defaultNotificationSubscriptions/*/callbackUri",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/nfServiceList/*/fqdn",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/nfServiceList/*/defaultNotificationSubscriptions/*/interPlmnCallbackUri",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/nfServiceList/*/defaultNotificationSubscriptions/*/callbackUri",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/nfServiceList/*/interPlmnFqdn",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/defaultNotificationSubscriptions/*/interPlmnCallbackUri",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/nfProfile/defaultNotificationSubscriptions/*/callbackUri",
                                                                                NF_TYPE_CONDITION,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));

        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault4",
                                                    NNRF_MANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    SUBSCRIPTIONS,
                                                    MessageOrigin.OWN_NETWORK);
        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of(NF_STATUS_NOTIFY_URI_STRING,
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of(NF_STATUS_NOTIFY_URI_STRING,
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault5", null, null, true, null, null, MessageOrigin.EXTERNAL_NETWORK);
        messageLocatorBuilder.createRequestHeaderLocator(Set.of(Triplet.of(TARGET_API_ROOT, null, SearchInHeader.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                Triplet.of(AUTHORITY_HEADER,
                                                                           TARGET_API_ROOT_CONDITION,
                                                                           SearchInHeader.PerformActionOnAttribute.DE_SCRAMBLE)));
        messageLocatorBuilder.createResponseHeaderLocator(Set.of(Triplet.of(TARGET_API_ROOT, null, SearchInHeader.PerformActionOnAttribute.SCRAMBLE),
                                                                 Triplet.of(LOCATION_HEADER, null, SearchInHeader.PerformActionOnAttribute.SCRAMBLE)));

        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault6", null, null, false, null, null, MessageOrigin.EXTERNAL_NETWORK);
        messageLocatorBuilder.createRequestHeaderLocator(Set.of(Triplet.of(TARGET_API_ROOT,
                                                                           SERVICE_CONDITION,
                                                                           SearchInHeader.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                Triplet.of(AUTHORITY_HEADER,
                                                                           TARGET_API_ROOT_CONDITION + " and " + SERVICE_CONDITION,
                                                                           SearchInHeader.PerformActionOnAttribute.DE_SCRAMBLE)));

        messageLocatorBuilder.createResponseHeaderLocator(Set.of(Triplet.of(TARGET_API_ROOT,
                                                                            SERVICE_CONDITION,
                                                                            SearchInHeader.PerformActionOnAttribute.SCRAMBLE),
                                                                 Triplet.of(LOCATION_HEADER,
                                                                            SERVICE_CONDITION,
                                                                            SearchInHeader.PerformActionOnAttribute.SCRAMBLE)));

        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault7",
                                                    NSMF_PDUSESSION,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    PDU_SESSION,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/vsmfPduSessionUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/vsmfPduSessionUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault8",
                                                    NUDM_SUBSCRIBERDATAMANAGEMENT,
                                                    SERVICE_VERSION_2,
                                                    false,
                                                    HttpMethod.POST,
                                                    SDM_SUBSCRIPTIONS_SHARED_DATA,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/callbackReference",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/dataRestorationCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/callbackReference",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault9",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.PUT,
                                                    AMF_REGISTRATIONS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/deregCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/pcscfRestorationCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/dataRestorationCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of(AMF_EE_SUB_ID_STRING,
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/deregCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/pcscfRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of(AMF_EE_SUB_ID_STRING,
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault10",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.PUT,
                                                    AMF_REGISTRATIONS_NON_3GPP_ACCESS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/deregCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/dataRestorationCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/pcscfRestorationCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of(AMF_EE_SUB_ID_STRING,
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/deregCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/pcscfRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of(AMF_EE_SUB_ID_STRING,
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault11",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.PUT,
                                                    SMF_REGISTRATION,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/deregCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/dataRestorationCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/pcscfRestorationCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/deregCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/pcscfRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault12",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.PUT,
                                                    SMSF_REGISTRATIONS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/dataRestorationCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault13",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.PUT,
                                                    SMSF_REGISTRATIONS_NON_3GPP_ACCESS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/dataRestorationCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault14",
                                                    NAMF_COMMUNICATIONS,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    SUBSCRIPTIONS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/amfStatusUri", null, SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/amfStatusUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault15",
                                                    NAMF_COMMUNICATIONS,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.PUT,
                                                    SUBSCRIPTIONS_ID,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/amfStatusUri", null, SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/amfStatusUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault16",
                                                    NAMF_EVENTEXPOSURE,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    SUBSCRIPTIONS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/subscription/eventNotifyUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/subscription/subsChangeNotifyUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/subscription/eventNotifyUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/subscription/subsChangeNotifyUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault17",
                                                    NNSSAAF_NSSAA,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    SLICE_AUTHENTICATIONS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/reauthNotifUri ",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/revocNotifUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault18",
                                                    NSMF_PDUSESSION,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    PDU_SESSION,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/oldSmContextRef",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                     Triplet.of("/oldPduSessionRef",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/interPlmnApiRoot",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/intraPlmnApiRoot",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault19",
                                                    NUDM_SUBSCRIBERDATAMANAGEMENT,
                                                    SERVICE_VERSION_2,
                                                    false,
                                                    HttpMethod.POST,
                                                    SDM_SUBSCRIPTIONS,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/monitoredResourceUris/*",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/monitoredResourceUris/*",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault20",
                                                    NUDM_SUBSCRIBERDATAMANAGEMENT,
                                                    SERVICE_VERSION_2,
                                                    false,
                                                    HttpMethod.POST,
                                                    SDM_SUBSCRIPTIONS_SHARED_DATA,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/monitoredResourceUris/*",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/monitoredResourceUris/*",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault21",
                                                    NUDM_SUBSCRIBERDATAMANAGEMENT,
                                                    SERVICE_VERSION_2,
                                                    false,
                                                    HttpMethod.PATCH,
                                                    SDM_SUBSCRIPTIONS_ID,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/monitoredResourceUris/*",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/monitoredResourceUris/*",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault22",
                                                    NUDM_SUBSCRIBERDATAMANAGEMENT,
                                                    SERVICE_VERSION_2,
                                                    false,
                                                    HttpMethod.PATCH,
                                                    SDM_SUBSCRIPTIONS_SHARED_DATA_ID,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/monitoredResourceUris/*",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/monitoredResourceUris/*",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();

        messageLocatorBuilder.createMessageSelector("ScrDefault23",
                                                    NAMF_EVENTEXPOSURE,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    SUBSCRIPTIONS,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/subscriptionId",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/subscriptionId",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault24",
                                                    NSMF_PDUSESSION_UPDATE,
                                                    SERVICE_VERSION,
                                                    true,
                                                    HttpMethod.POST,
                                                    null,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/hsmfPduSessionUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault25",
                                                    NSMF_PDUSESSION,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    SM_CONTEXT_RETRIEVE,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/smContext/interPlmnApiRoot",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/smContext/intraPlmnApiRoot",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/smContext/hSmfUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/smContext/pduSessionRef",
                                                                                 null,

                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/afCoordinationInfo/notificationInfoList/*/notifUri ",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault26",
                                                    NSMF_PDUSESSION,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    SMF_PDU_SESSION_MODIFY,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/interPlmnApiRoot",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                      Triplet.of("/intraPlmnApiRoot",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault27",
                                                    NAMF_EVENTEXPOSURE,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.PATCH,
                                                    SUBSCRIPTIONS_ID,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/subscriptionId",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();

        messageLocatorBuilder.createMessageSelector("ScrDefault28",
                                                    NSMF_PDUSESSION_STATUS_NOTIFY,
                                                    SERVICE_VERSION,
                                                    true,
                                                    HttpMethod.POST,
                                                    null,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/interPlmnApiRoot",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/intraPlmnApiRoot",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/oldPduSessionRef",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();

        messageLocatorBuilder.createMessageSelector("ScrDefault29",
                                                    NSMF_PDUSESSION,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    SMF_PDU_SESSION_MODIFY,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/vsmfPduSessionUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();

        messageLocatorBuilder.createMessageSelector("ScrDefault30",
                                                    NAMF_EVENTEXPOSURE_NOTIFY,
                                                    SERVICE_VERSION,
                                                    true,
                                                    HttpMethod.POST,
                                                    null,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/eventSubsSyncInfo/subscriptionList/.*/subId",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/eventSubsSyncInfo/subscriptionList/.*/oldsubid",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/reportList/*/subscriptionId",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault31",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.GET,
                                                    REGISTRATIONS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/amf3Gpp/amfEeSubscriptionId",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/amf3Gpp/deregCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/amf3Gpp/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/amf3Gpp/pcscfRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/amfNon3Gpp/amfEeSubscriptionId",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/amfNon3Gpp/deregCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/amfNon3Gpp/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/amfNon3Gpp/pcscfRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/smfRegistration/smfRegistrationList/*/deregCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/smfRegistration/smfRegistrationList/*/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/smfRegistration/smfRegistrationList/*/pcscfRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/smsf3Gpp/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/smsfNon3Gpp/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)

        ));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault32",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.GET,
                                                    AMF_REGISTRATIONS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/deregCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/pcscfRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of(AMF_EE_SUB_ID_STRING,
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault33",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.GET,
                                                    AMF_REGISTRATIONS_NON_3GPP_ACCESS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/deregCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/pcscfRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of(AMF_EE_SUB_ID_STRING,
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault34",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.GET,
                                                    "/.*/registrations/smf-registrations",
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/smfRegistrationList/*/deregCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/smfRegistrationList/*/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/smfRegistrationList/*/pcscfRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault35",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.GET,
                                                    SMF_REGISTRATION,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/deregCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/pcscfRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault36",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.GET,
                                                    SMSF_REGISTRATIONS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault37",
                                                    NUDM_UECONTEXTMANAGEMENT,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.GET,
                                                    SMSF_REGISTRATIONS_NON_3GPP_ACCESS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault38",
                                                    NUDM_SUBSCRIBERDATAMANAGEMENT,
                                                    SERVICE_VERSION_2,
                                                    false,
                                                    HttpMethod.PATCH,
                                                    SDM_SUBSCRIPTIONS_SHARED_DATA_ID,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/callbackReference",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault39",
                                                    NUDM_SUBSCRIBERDATAMANAGEMENT,
                                                    SERVICE_VERSION_2,
                                                    false,
                                                    HttpMethod.PATCH,
                                                    SDM_SUBSCRIPTIONS_ID,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/callbackReference",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));

        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault40",
                                                    NUDM_SUBSCRIBERDATAMANAGEMENT,
                                                    SERVICE_VERSION_2,
                                                    false,
                                                    HttpMethod.POST,
                                                    SDM_SUBSCRIPTIONS,
                                                    MessageOrigin.OWN_NETWORK);

        messageLocatorBuilder.createRequestMessageBodyLocator(Set.of(Triplet.of("/callbackReference",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE),
                                                                     Triplet.of("/dataRestorationCallbackUri",
                                                                                null,
                                                                                SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/callbackReference",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE),
                                                                      Triplet.of("/dataRestorationCallbackUri",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        messageLocatorBuilder = new TphMessageLocatorBuilder();
        messageLocatorBuilder.createMessageSelector("ScrDefault41",
                                                    NSMF_PDUSESSION,
                                                    SERVICE_VERSION,
                                                    false,
                                                    HttpMethod.POST,
                                                    PDU_SESSION_RETRIEVE,
                                                    MessageOrigin.EXTERNAL_NETWORK);

        messageLocatorBuilder.createResponseMessageBodyLocator(Set.of(Triplet.of("/afCoordinationInfo/notificationInfoList/*/notifUri ",
                                                                                 null,
                                                                                 SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)));
        defaultSelectors.add(new LocatorWrapper(messageLocatorBuilder.buildMessageLocator()));
        return defaultSelectors;
    }
}
