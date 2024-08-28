package com.ericsson.sc.sepp.config;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.sepp.model.CustomFqdnLocator;
import com.ericsson.sc.sepp.model.CustomFqdnLocator.HttpMethod;
import com.ericsson.sc.sepp.model.CustomFqdnLocator.MessageOrigin;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.RequestMessage;
import com.ericsson.sc.sepp.model.ResponseMessage;
import com.ericsson.sc.sepp.model.SearchInHeader;
import com.ericsson.sc.sepp.model.SearchInMessageBody;
import com.ericsson.sc.sepp.model.SearchInQueryParameter;

public class ThLocatorsMapping extends ThLocators
{
    private static final String SERVICE_VERSION = "v1";
    private static final String NNRF_DISCOVERY = "nnrf-disc";
    private static final String NNRF_MANAGEMENT = "nnrf-nfm";
    private static final String LOCATION_HEADER = "location";
    private static final String AUTHORITY_HEADER = ":authority";
    private static final String TARGET_API_ROOT = "3gpp-sbi-target-apiroot";
    private static final String LOCATION_MATCH_CONDITION = "(resp.header[':status'] == '307') or (resp.header[':status'] == '308')";
    private static final String TARGET_API_ROOT_CONDITION = "not req.header['3gpp-Sbi-target-apiRoot'] exists";
    private static final String NF_INSTANCES = "/nf-instances";
    private static final String SUBSCRIPTIONS = "/subscriptions";
    private static final String SUBSCRIPTIONS_ID = "/subscriptions/.*";
    private static final String BOOTSTRAPING = "bootstrapping";
    private static final String SEARCH_ID = "/searches/.*";
    private static final String BODY_JSON_POINTER_HNRFURI = "/hnrfUri";

    public ThLocatorsMapping(String fqdnHidingState,
                             String onFqdnHidingErrorAction,
                             NfInstance instance)
    {
        super(fqdnHidingState, onFqdnHidingErrorAction, instance);
    }

    public List<LocatorWrapper> createDefaultSelectors()
    {
        var defaultSelectors = new ArrayList<LocatorWrapper>();

        defaultSelectors.add(new LocatorWrapper(new CustomFqdnLocator().withName("Default1")
                                                                       .withServiceName(NNRF_DISCOVERY)
                                                                       .withServiceVersion(SERVICE_VERSION)
                                                                       .withNotificationMessage(false)
                                                                       .withHttpMethod(HttpMethod.GET)
                                                                       .withResource(NF_INSTANCES)
                                                                       .withMessageOrigin(MessageOrigin.EXTERNAL_NETWORK)
                                                                       .withRequestMessage(new RequestMessage().withSearchInQueryParameter(List.of(new SearchInQueryParameter().withQueryParameter("hnrf-uri")
                                                                                                                                                                               .withPerformActionOnAttribute(SearchInQueryParameter.PerformActionOnAttribute.DE_MAP),
                                                                                                                                                   new SearchInQueryParameter().withQueryParameter("nrf-disc-uri")
                                                                                                                                                                               .withPerformActionOnAttribute(SearchInQueryParameter.PerformActionOnAttribute.DE_MAP)))
                                                                                                               .withSearchInHeader(List.of(new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP),
                                                                                                                                           new SearchInHeader().withHeader(AUTHORITY_HEADER)
                                                                                                                                                               .withMatchingCondition(TARGET_API_ROOT_CONDITION)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP))))
                                                                       .withResponseMessage(new ResponseMessage().withSearchInHeader(List.of(new SearchInHeader().withHeader(LOCATION_HEADER)
                                                                                                                                                                 .withMatchingCondition(LOCATION_MATCH_CONDITION)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP),
                                                                                                                                             new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP)))
                                                                                                                 .withSearchInMessageBody(List.of(new SearchInMessageBody().withBodyJsonPointer("/nfInstanceList/*/nrfDiscApiUri")
                                                                                                                                                                           .withMatchingCondition(null)
                                                                                                                                                                           .withPerformActionOnAttribute(SearchInMessageBody.PerformActionOnAttribute.MAP))))));

        defaultSelectors.add(new LocatorWrapper(new CustomFqdnLocator().withName("Default2")
                                                                       .withServiceName(NNRF_MANAGEMENT)
                                                                       .withServiceVersion(SERVICE_VERSION)
                                                                       .withNotificationMessage(false)
                                                                       .withHttpMethod(HttpMethod.POST)
                                                                       .withResource(SUBSCRIPTIONS)
                                                                       .withMessageOrigin(MessageOrigin.EXTERNAL_NETWORK)
                                                                       .withRequestMessage(new RequestMessage().withSearchInMessageBody(List.of(new SearchInMessageBody().withBodyJsonPointer(BODY_JSON_POINTER_HNRFURI)
                                                                                                                                                                         .withPerformActionOnAttribute(SearchInMessageBody.PerformActionOnAttribute.DE_MAP)))
                                                                                                               .withSearchInHeader(List.of(new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP),
                                                                                                                                           new SearchInHeader().withHeader(AUTHORITY_HEADER)
                                                                                                                                                               .withMatchingCondition(TARGET_API_ROOT_CONDITION)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP))))
                                                                       .withResponseMessage(new ResponseMessage().withSearchInMessageBody(List.of(new SearchInMessageBody().withBodyJsonPointer(BODY_JSON_POINTER_HNRFURI)
                                                                                                                                                                           .withPerformActionOnAttribute(SearchInMessageBody.PerformActionOnAttribute.MAP)))
                                                                                                                 .withSearchInHeader(List.of(new SearchInHeader().withHeader(LOCATION_HEADER)
                                                                                                                                                                 .withMatchingCondition("(resp.header[':status'] == '307') or (resp.header[':status'] == '308') or (resp.header[':status'] == '201')")
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP),
                                                                                                                                             new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP))))));

        defaultSelectors.add(new LocatorWrapper(new CustomFqdnLocator().withName("Default3")
                                                                       .withServiceName(NNRF_MANAGEMENT)
                                                                       .withServiceVersion(SERVICE_VERSION)
                                                                       .withNotificationMessage(false)
                                                                       .withHttpMethod(HttpMethod.DELETE)
                                                                       .withResource(SUBSCRIPTIONS_ID)
                                                                       .withMessageOrigin(MessageOrigin.EXTERNAL_NETWORK)
                                                                       .withRequestMessage(new RequestMessage().withSearchInHeader(List.of(new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP),
                                                                                                                                           new SearchInHeader().withHeader(AUTHORITY_HEADER)
                                                                                                                                                               .withMatchingCondition(TARGET_API_ROOT_CONDITION)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP))))
                                                                       .withResponseMessage(new ResponseMessage().withSearchInHeader(List.of(new SearchInHeader().withHeader(LOCATION_HEADER)
                                                                                                                                                                 .withMatchingCondition(LOCATION_MATCH_CONDITION)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP),
                                                                                                                                             new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP))))));
        defaultSelectors.add(new LocatorWrapper(new CustomFqdnLocator().withName("Default4")
                                                                       .withServiceName(NNRF_MANAGEMENT)
                                                                       .withServiceVersion(SERVICE_VERSION)
                                                                       .withNotificationMessage(false)
                                                                       .withHttpMethod(HttpMethod.PATCH)
                                                                       .withResource(SUBSCRIPTIONS_ID)
                                                                       .withMessageOrigin(MessageOrigin.EXTERNAL_NETWORK)
                                                                       .withRequestMessage(new RequestMessage().withSearchInHeader(List.of(new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP),
                                                                                                                                           new SearchInHeader().withHeader(AUTHORITY_HEADER)
                                                                                                                                                               .withMatchingCondition(TARGET_API_ROOT_CONDITION)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP))))
                                                                       .withResponseMessage(new ResponseMessage().withSearchInHeader(List.of(new SearchInHeader().withHeader(LOCATION_HEADER)
                                                                                                                                                                 .withMatchingCondition(LOCATION_MATCH_CONDITION)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP),
                                                                                                                                             new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP))))));
        defaultSelectors.add(new LocatorWrapper(new CustomFqdnLocator().withName("Default5")
                                                                       .withServiceName(BOOTSTRAPING)
                                                                       .withNotificationMessage(false)
                                                                       .withHttpMethod(HttpMethod.GET)
                                                                       .withMessageOrigin(MessageOrigin.EXTERNAL_NETWORK)
                                                                       .withRequestMessage(new RequestMessage().withSearchInHeader(List.of(new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP),
                                                                                                                                           new SearchInHeader().withHeader(AUTHORITY_HEADER)
                                                                                                                                                               .withMatchingCondition(TARGET_API_ROOT_CONDITION)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP))))
                                                                       .withResponseMessage(new ResponseMessage().withSearchInHeader(List.of(new SearchInHeader().withHeader(LOCATION_HEADER)
                                                                                                                                                                 .withMatchingCondition(LOCATION_MATCH_CONDITION)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP),
                                                                                                                                             new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP))))));
        defaultSelectors.add(new LocatorWrapper(new CustomFqdnLocator().withName("Default6")
                                                                       .withServiceName(NNRF_DISCOVERY)
                                                                       .withServiceVersion(SERVICE_VERSION)
                                                                       .withNotificationMessage(false)
                                                                       .withHttpMethod(HttpMethod.GET)
                                                                       .withResource(SEARCH_ID)
                                                                       .withMessageOrigin(MessageOrigin.EXTERNAL_NETWORK)
                                                                       .withRequestMessage(new RequestMessage().withSearchInHeader(List.of(new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP),
                                                                                                                                           new SearchInHeader().withHeader(AUTHORITY_HEADER)
                                                                                                                                                               .withMatchingCondition(TARGET_API_ROOT_CONDITION)
                                                                                                                                                               .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.DE_MAP))))
                                                                       .withResponseMessage(new ResponseMessage().withSearchInHeader(List.of(new SearchInHeader().withHeader(LOCATION_HEADER)
                                                                                                                                                                 .withMatchingCondition(LOCATION_MATCH_CONDITION)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP),
                                                                                                                                             new SearchInHeader().withHeader(TARGET_API_ROOT)
                                                                                                                                                                 .withPerformActionOnAttribute(SearchInHeader.PerformActionOnAttribute.MAP))))));

        return defaultSelectors;
    }
}
