/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 13, 2023
 *     Author: xzinale
 */

package com.ericsson.sc.sepp.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.ericsson.sc.expressionparser.ConditionParser;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningRule;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction.DynKVTable;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction.ModifyJsonBodyActionType;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction.ScrambleData;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction.ScreeningActionType;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterData;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFqdnHiding;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.IfProxyTopologyHiding.Locator;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.IfProxyTopologyHiding.Selector;
import com.ericsson.sc.sepp.model.CustomFqdnLocator;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.RequestMessage;
import com.ericsson.sc.sepp.model.ResponseMessage;
import com.ericsson.sc.sepp.model.SearchInHeader;
import com.ericsson.sc.sepp.model.SearchInMessageBody;
import com.ericsson.sc.sepp.model.SearchInQueryParameter;
import com.ericsson.utilities.common.Utils;

/**
 * 
 */
public class ThLocators
{
    private static final String MAPPING_RULE = "MappingRule";
    private static final String TH_RULE = "TopologyHidingRule";
    private static final String MAP = "Map";
    private static final String DEMAP = "Demap";
    private static final String ERASE = "Erase";
    private static final String ERASE_ACTION = "THEraseAction";
    private static final String REPLACE_WITH_OWN_FQDN = "ReplaceWithOwnFQDN";
    private static final String REPLACE_ACTION = "THReplaceAction";
    private static final String MAPPING_ACTION = "MappingAction";
    private static final String SCRAMBLE = "Scramble";
    private static final String DESCRAMBLE = "Descramble";
    private static final String SCRAMBLING_RULE = "ScramblingRule";
    private static final String SCRAMBLING_ACTION = "ScramblingAction";
    private static final String GRACEFUL_DEACTIVATION = "graceful-deactivation";
    private static final String ACTIVE = "active";

    private String adminState;
    private String unsuccessfulAction;
    private NfInstance seppInst;
    private String ownFqdn;

    public ThLocators(String fqdnHidingState,
                      String onFqdnHidingErrorAction,
                      NfInstance instance)
    {
        this.adminState = fqdnHidingState;
        this.unsuccessfulAction = onFqdnHidingErrorAction;
        this.seppInst = instance;

        this.ownFqdn = getOwnFqdn().orElseThrow();
    }

    private Optional<String> getOwnFqdn()
    {
        return seppInst.getServiceAddress()
                       .stream()
                       .filter(sa -> sa.getName().equals(seppInst.getOwnNetwork().get(0).getServiceAddressRef()))
                       .findAny()
                       .map(sa -> sa.getFqdn());
    }

    public void addCustomFqdnLocators(List<LocatorWrapper> defaultLocators,
                                      ProxyFqdnHiding pxFqdnHiding)
    {
        for (var fqdnLocator : defaultLocators)
        {
            if (fqdnLocator.cfl.getRequestMessage() != null)
            {
                var listMessageData = new ArrayList<ProxyFilterData>();

                var locator = addRequestParameters(fqdnLocator.cfl.getRequestMessage());

                Optional.ofNullable(fqdnLocator.cfl.getRequestMessage().getMessageDataRef()).ifPresent(l -> l.forEach(messageData ->
                {
                    var md = Utils.getByName(seppInst.getMessageData(), messageData);

                    if (md != null)
                    {
                        listMessageData.add(new ProxyFilterData(md.getName(),
                                                                md.getVariableName(),
                                                                md.getExtractorRegex(),
                                                                md.getPath() != null,
                                                                md.getBodyJsonPointer(),
                                                                md.getHeader(),
                                                                md.getRequestHeader(),
                                                                md.getResponseHeader()));
                    }
                }));

                locator.withMessageData(listMessageData);
                pxFqdnHiding.putToMapCustomFqdnLocator(new Selector().withName(fqdnLocator.cfl.getName())
                                                                     .withHttpMethod(Optional.ofNullable((fqdnLocator.cfl.getHttpMethod() == null) ? null
                                                                                                                                                   : fqdnLocator.cfl.getHttpMethod()
                                                                                                                                                                    .toString()))
                                                                     .withMessageOrigin(fqdnLocator.cfl.getMessageOrigin().ordinal())
                                                                     .withNotificationMessage(fqdnLocator.cfl.getNotificationMessage())
                                                                     .withResource(Optional.ofNullable(fqdnLocator.cfl.getResource()))
                                                                     .withServiceName(Optional.ofNullable(fqdnLocator.cfl.getServiceName()))
                                                                     .withServiceVersion(Optional.ofNullable(fqdnLocator.cfl.getServiceVersion()))
                                                                     .withIsResponse(0),
                                                       locator);
            }

            if (fqdnLocator.cfl.getResponseMessage() != null)
            {
                var listMessageData = new ArrayList<ProxyFilterData>();

                var locator = addResponseParameters(fqdnLocator.cfl.getResponseMessage());

                Optional.ofNullable(fqdnLocator.cfl.getResponseMessage().getMessageDataRef()).ifPresent(l -> l.forEach(messageData ->
                {
                    var md = Utils.getByName(seppInst.getMessageData(), messageData);

                    if (md != null)
                    {
                        listMessageData.add(new ProxyFilterData(md.getName(),
                                                                md.getVariableName(),
                                                                md.getExtractorRegex(),
                                                                md.getPath() != null,
                                                                md.getBodyJsonPointer(),
                                                                md.getHeader(),
                                                                md.getRequestHeader(),
                                                                md.getResponseHeader(),
                                                                1));
                    }
                }));

                locator.withMessageData(listMessageData);

                pxFqdnHiding.putToMapCustomFqdnLocator(new Selector().withName(fqdnLocator.cfl.getName())
                                                                     .withHttpMethod(Optional.ofNullable((fqdnLocator.cfl.getHttpMethod() == null) ? null
                                                                                                                                                   : fqdnLocator.cfl.getHttpMethod()
                                                                                                                                                                    .toString()))
                                                                     .withMessageOrigin(fqdnLocator.cfl.getMessageOrigin().ordinal())
                                                                     .withNotificationMessage(fqdnLocator.cfl.getNotificationMessage())
                                                                     .withResource(Optional.ofNullable(fqdnLocator.cfl.getResource()))
                                                                     .withServiceName(Optional.ofNullable(fqdnLocator.cfl.getServiceName()))
                                                                     .withServiceVersion(Optional.ofNullable(fqdnLocator.cfl.getServiceVersion()))
                                                                     .withIsResponse(1),
                                                       locator);
            }
        }
    }

    private Locator addRequestParameters(RequestMessage reqMessage)
    {

        var locator = new Locator();

        reqMessage.getSearchInHeader().forEach(head ->
        {
            var condition = "true";
            if (head.getMatchingCondition() != null)
            {
                condition = head.getMatchingCondition();
            }
            createRulesForHeaders(head, condition, locator);
        });

        reqMessage.getSearchInMessageBody().forEach(body ->
        {
            var condition = "true";
            if (body.getMatchingCondition() != null)
            {
                condition = body.getMatchingCondition();
            }
            createRulesForJsonPtrs(body, condition, locator);
        });

        reqMessage.getSearchInQueryParameter().forEach(query ->
        {
            var condition = "true";
            if (query.getMatchingCondition() != null)
            {
                condition = query.getMatchingCondition();
            }

            createRulesForQueryParams(query, condition, locator);
        });

        return locator;
    }

    private Locator addResponseParameters(ResponseMessage respMessage)
    {
        var locator = new Locator();

        respMessage.getSearchInHeader().forEach(head ->
        {
            var condition = "true";
            if (head.getMatchingCondition() != null)
            {
                condition = head.getMatchingCondition();
            }
            createRulesForHeaders(head, condition, locator);
        });

        respMessage.getSearchInMessageBody().forEach(body ->
        {
            var condition = "true";
            if (body.getMatchingCondition() != null)
            {
                condition = body.getMatchingCondition();
            }
            createRulesForJsonPtrs(body, condition, locator);
        });

        return locator;
    }

    private void createRulesForHeaders(SearchInHeader head,
                                       String condition,
                                       Locator locator)
    {
        switch (head.getPerformActionOnAttribute())
        {
            case MAP:

                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(MAPPING_RULE + MAP + head.getHeader(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(actionMapHeader(head.getHeader()))));
                break;

            case DE_MAP:

                locator.addUnHidingProxyScreeningRules(new ProxyScreeningRule(MAPPING_RULE + DEMAP + head.getHeader(),
                                                                              ConditionParser.parse(condition),
                                                                              List.of(actionDeMapHeader(head.getHeader()))));

                break;

            case ERASE:

                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(TH_RULE + ERASE + head.getHeader(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(new ProxyScreeningAction(ERASE_ACTION + head.getHeader(),
                                                                                                                 ScreeningActionType.ActionRemoveHeader).setHeaderName(head.getHeader()))));
                break;

            case REPLACE_WITH_OWN_FQDN:

                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(TH_RULE + REPLACE_WITH_OWN_FQDN + head.getHeader(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(new ProxyScreeningAction(REPLACE_ACTION + head.getHeader(),
                                                                                                                 ScreeningActionType.ActionModifyHeader).setHeaderName(head.getHeader())
                                                                                                                                                        .setReplaceValue(ownFqdn))));
                break;

            case SCRAMBLE:

                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(SCRAMBLING_RULE + SCRAMBLE + head.getHeader(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(actionScrambleHeader(head.getHeader()))));
                break;

            case DE_SCRAMBLE:

                locator.addUnHidingProxyScreeningRules(new ProxyScreeningRule(SCRAMBLING_RULE + SCRAMBLE + head.getHeader(),
                                                                              ConditionParser.parse(condition),
                                                                              List.of(actionDeScrambleHeader(head.getHeader()))));
                break;

            default:
                break;

        }
    }

    private void createRulesForJsonPtrs(SearchInMessageBody body,
                                        String condition,
                                        Locator locator)
    {
        switch (body.getPerformActionOnAttribute())
        {
            case MAP:
                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(MAPPING_RULE + MAP + body.getBodyJsonPointer(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(actionMapBody(body.getBodyJsonPointer()))));
                break;

            case DE_MAP:

                locator.addUnHidingProxyScreeningRules(new ProxyScreeningRule(MAPPING_RULE + DEMAP + body.getBodyJsonPointer(),

                                                                              ConditionParser.parse(condition),
                                                                              List.of(actionDeMapBody(body.getBodyJsonPointer()))));

                break;

            case ERASE:
                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(TH_RULE + ERASE + body.getBodyJsonPointer(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(new ProxyScreeningAction(MAPPING_ACTION + body.getBodyJsonPointer(),
                                                                                                                 ScreeningActionType.ActionModifyJsonBody).setModifyJsonBodyActionType(ModifyJsonBodyActionType.SearchReplaceRegex)
                                                                                                                                                          .setSearchValue(".*")
                                                                                                                                                          .setReplaceValue("")
                                                                                                                                                          .setJsonPointer(body.getBodyJsonPointer()))));
                break;

            case REPLACE_WITH_OWN_FQDN:
                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(TH_RULE + REPLACE_WITH_OWN_FQDN + body.getBodyJsonPointer(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(new ProxyScreeningAction(MAPPING_ACTION + body.getBodyJsonPointer(),
                                                                                                                 ScreeningActionType.ActionModifyJsonBody).setModifyJsonBodyActionType(ModifyJsonBodyActionType.SearchReplaceRegex)
                                                                                                                                                          .setSearchValue(".*")
                                                                                                                                                          .setReplaceValue(ownFqdn)
                                                                                                                                                          .setJsonPointer(body.getBodyJsonPointer()))));
                break;

            case SCRAMBLE:
                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(SCRAMBLING_RULE + SCRAMBLE + body.getBodyJsonPointer(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(actionScrambleBody(body.getBodyJsonPointer()))));
                break;

            case DE_SCRAMBLE:

                locator.addUnHidingProxyScreeningRules(new ProxyScreeningRule(SCRAMBLING_RULE + DESCRAMBLE + body.getBodyJsonPointer(),
                                                                              ConditionParser.parse(condition),
                                                                              List.of(actionDeScrambleBody(body.getBodyJsonPointer()))));
                break;

            default:
                break;
        }
    }

    private void createRulesForQueryParams(SearchInQueryParameter query,
                                           String condition,
                                           Locator locator)
    {
        switch (query.getPerformActionOnAttribute())
        {
            case MAP:

                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(MAPPING_RULE + MAP + query.getQueryParameter(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(actionMapQueryParam(query.getQueryParameter()))));

                break;

            case DE_MAP:

                locator.addUnHidingProxyScreeningRules(new ProxyScreeningRule(MAPPING_RULE + DEMAP + query.getQueryParameter(),
                                                                              ConditionParser.parse(condition),
                                                                              List.of(actionDeMapQueryParam(query.getQueryParameter()))));

                break;

            case ERASE:

                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(TH_RULE + ERASE + query.getQueryParameter(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(new ProxyScreeningAction(MAPPING_ACTION + query.getQueryParameter(),
                                                                                                                 ScreeningActionType.ActionModifyQueryParam).setQueryName(""))));
                break;

            case REPLACE_WITH_OWN_FQDN:

                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(TH_RULE + REPLACE_WITH_OWN_FQDN + query.getQueryParameter(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(new ProxyScreeningAction(MAPPING_ACTION + query.getQueryParameter(),
                                                                                                                 ScreeningActionType.ActionModifyQueryParam).setQueryName(ownFqdn))));
                break;

            case SCRAMBLE:

                if (!adminState.equals(GRACEFUL_DEACTIVATION))
                    locator.addHidingProxyScreeningRules(new ProxyScreeningRule(SCRAMBLING_RULE + SCRAMBLE + query.getQueryParameter(),
                                                                                ConditionParser.parse(condition),
                                                                                List.of(actionScrambleQueryParam(query.getQueryParameter()))));

                break;

            case DE_SCRAMBLE:

                locator.addUnHidingProxyScreeningRules(new ProxyScreeningRule(SCRAMBLING_RULE + DESCRAMBLE + query.getQueryParameter(),
                                                                              ConditionParser.parse(condition),
                                                                              List.of(actionDeScrambleQueryParam(query.getQueryParameter()))));
                break;

            default:
                break;
        }
    }

    private ProxyScreeningAction actionMapQueryParam(String queryParam)
    {
        return new ProxyScreeningAction(MAPPING_ACTION + queryParam,
                                        ScreeningActionType.ActionModifyQueryParam).setQueryName(queryParam)
                                                                                   .setDynKvTable(new DynKVTable(ProxySeppFilter.INTERNAL_FQDN_MAPPING_TABLE_NAME).setFcUnsuccessfulOp(unsuccessfulAction));
    }

    private ProxyScreeningAction actionDeMapQueryParam(String queryParam)
    {
        var kvt = new DynKVTable(ProxySeppFilter.INTERNAL_FQDN_UNMAPPING_TABLE_NAME);
        if (adminState.equals(ACTIVE))
        {
            kvt.setFcUnsuccessfulOp(unsuccessfulAction);
        }
        else
        {
            kvt.setDoNothing();
        }
        return new ProxyScreeningAction(MAPPING_ACTION + queryParam, ScreeningActionType.ActionModifyQueryParam).setQueryName(queryParam).setDynKvTable(kvt);
    }

    private ProxyScreeningAction actionMapHeader(String headerName)
    {
        return new ProxyScreeningAction(MAPPING_ACTION + headerName,
                                        ScreeningActionType.ActionModifyHeader).setHeaderName(headerName)
                                                                               .setDynKvTable(new DynKVTable(ProxySeppFilter.INTERNAL_FQDN_MAPPING_TABLE_NAME).setFcUnsuccessfulOp(unsuccessfulAction));
    }

    private ProxyScreeningAction actionDeMapHeader(String headerName)
    {
        var kvt = new DynKVTable(ProxySeppFilter.INTERNAL_FQDN_UNMAPPING_TABLE_NAME);
        if (adminState.equals(ACTIVE))
        {
            kvt.setFcUnsuccessfulOp(unsuccessfulAction);
        }
        else
        {
            kvt.setDoNothing();
        }
        return new ProxyScreeningAction(MAPPING_ACTION + headerName, ScreeningActionType.ActionModifyHeader).setHeaderName(headerName).setDynKvTable(kvt);
    }

    private ProxyScreeningAction actionMapBody(String bodyJsonPtr)
    {
        return new ProxyScreeningAction(MAPPING_ACTION + bodyJsonPtr,
                                        ScreeningActionType.ActionModifyJsonBody).setDynKvTable(new DynKVTable(ProxySeppFilter.INTERNAL_FQDN_MAPPING_TABLE_NAME).setFcUnsuccessfulOp(unsuccessfulAction))
                                                                                 .setModifyJsonBodyActionType(ModifyJsonBodyActionType.ReplaceFromMapTable)
                                                                                 .setJsonPointer(bodyJsonPtr);
    }

    private ProxyScreeningAction actionDeMapBody(String bodyJsonPtr)

    {
        var kvt = new DynKVTable(ProxySeppFilter.INTERNAL_FQDN_UNMAPPING_TABLE_NAME);
        if (adminState.equals(ACTIVE))
        {
            kvt.setFcUnsuccessfulOp(unsuccessfulAction);
        }
        else
        {
            kvt.setDoNothing();
        }
        return new ProxyScreeningAction(MAPPING_ACTION + bodyJsonPtr,
                                        ScreeningActionType.ActionModifyJsonBody).setDynKvTable(kvt)
                                                                                 .setModifyJsonBodyActionType(ModifyJsonBodyActionType.ReplaceFromMapTable)
                                                                                 .setJsonPointer(bodyJsonPtr);
    }

    private ProxyScreeningAction actionScrambleHeader(String headerName)
    {

        return new ProxyScreeningAction(SCRAMBLING_ACTION + headerName,
                                        ScreeningActionType.ActionModifyHeader).setHeaderName(headerName)
                                                                               .setScramblingData(new ScrambleData(unsuccessfulAction));
    }

    private ProxyScreeningAction actionDeScrambleHeader(String headerName)
    {
        ScrambleData scrambleData;

        if (adminState.equals(ACTIVE))
        {
            scrambleData = new ScrambleData(unsuccessfulAction);
        }
        else
        {
            scrambleData = new ScrambleData(true);
        }
        return new ProxyScreeningAction(SCRAMBLING_ACTION + headerName, ScreeningActionType.ActionModifyHeader).setHeaderName(headerName)
                                                                                                               .setScramblingData(scrambleData);
    }

    private ProxyScreeningAction actionScrambleBody(String jsonPtr)
    {
        return new ProxyScreeningAction(SCRAMBLING_ACTION + jsonPtr,
                                        ScreeningActionType.ActionModifyJsonBody).setModifyJsonBodyActionType(ModifyJsonBodyActionType.ReplaceScrambling)
                                                                                 .setJsonPointer(jsonPtr)
                                                                                 .setScramblingData(new ScrambleData(unsuccessfulAction));
    }

    private ProxyScreeningAction actionDeScrambleBody(String jsonPtr)
    {
        ScrambleData scrambleData;

        if (adminState.equals(ACTIVE))
        {
            scrambleData = new ScrambleData(unsuccessfulAction);
        }
        else
        {
            scrambleData = new ScrambleData(true);
        }
        return new ProxyScreeningAction(SCRAMBLING_ACTION + jsonPtr,
                                        ScreeningActionType.ActionModifyJsonBody).setModifyJsonBodyActionType(ModifyJsonBodyActionType.ReplaceScrambling)
                                                                                 .setJsonPointer(jsonPtr)
                                                                                 .setScramblingData(scrambleData);
    }

    private ProxyScreeningAction actionScrambleQueryParam(String paramName)
    {
        return new ProxyScreeningAction(SCRAMBLING_ACTION + paramName,
                                        ScreeningActionType.ActionModifyQueryParam).setQueryName(paramName)
                                                                                   .setScramblingData(new ScrambleData(unsuccessfulAction));
    }

    private ProxyScreeningAction actionDeScrambleQueryParam(String paramName)
    {
        ScrambleData scrambleData;

        if (adminState.equals(ACTIVE))
        {
            scrambleData = new ScrambleData(unsuccessfulAction);
        }
        else
        {
            scrambleData = new ScrambleData(true);
        }
        return new ProxyScreeningAction(SCRAMBLING_ACTION + paramName, ScreeningActionType.ActionModifyQueryParam).setQueryName(paramName)
                                                                                                                  .setScramblingData(scrambleData);
    }

    public static void setCustomFqdnLocator(List<CustomFqdnLocator> fqdnLocator,
                                            List<LocatorWrapper> defaultLocator)
    {
        for (var fqdnLoc : fqdnLocator)
        {
            var temp = new LocatorWrapper(fqdnLoc);

            defaultLocator.remove(temp);
            defaultLocator.add(temp);
        }
    }

    public static class LocatorWrapper
    {
        private CustomFqdnLocator cfl;

        public LocatorWrapper setCustomFqdnLocator(CustomFqdnLocator cl)
        {
            this.cfl = cl;
            return this;
        }

        public CustomFqdnLocator getCustomFqdnLocator()
        {
            return this.cfl;
        }

        public LocatorWrapper(CustomFqdnLocator customLocator)
        {
            this.cfl = customLocator;
        }

        @Override
        public boolean equals(Object other)
        {
            if (other == this)
            {
                return true;
            }
            if (!(other instanceof LocatorWrapper))
            {
                return false;
            }

            CustomFqdnLocator rhs = ((LocatorWrapper) other).cfl;
            return Objects.equals(this.cfl.getServiceName(), (rhs.getServiceName()))
                   && Objects.equals(this.cfl.getServiceVersion(), (((LocatorWrapper) other).cfl.getServiceVersion()))
                   && Objects.equals(this.cfl.getHttpMethod(), (((LocatorWrapper) other).cfl.getHttpMethod()))
                   && Objects.equals(this.cfl.getNotificationMessage(), (((LocatorWrapper) other).cfl.getNotificationMessage()))
                   && Objects.equals(this.cfl.getResource(), (((LocatorWrapper) other).cfl.getResource()))
                   && Objects.equals(this.cfl.getMessageOrigin(), (((LocatorWrapper) other).cfl.getMessageOrigin()));
        }

        @Override
        public int hashCode()
        {
            var result = 1;
            result = ((result * 31) + ((this.cfl.getServiceVersion() == null) ? 0 : this.cfl.getServiceVersion().hashCode()));
            result = ((result * 31) + ((this.cfl.getMessageOrigin() == null) ? 0 : this.cfl.getMessageOrigin().hashCode()));
            result = ((result * 31) + ((this.cfl.getResource() == null) ? 0 : this.cfl.getResource().hashCode()));
            result = ((result * 31) + ((this.cfl.getNotificationMessage() == null) ? 0 : this.cfl.getNotificationMessage().hashCode()));
            result = ((result * 31) + ((this.cfl.getServiceName() == null) ? 0 : this.cfl.getServiceName().hashCode()));
            result = ((result * 31) + ((this.cfl.getHttpMethod() == null) ? 0 : this.cfl.getHttpMethod().hashCode()));
            return result;
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append(CustomFqdnLocator.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
            sb.append("name");
            sb.append('=');
            sb.append(((this.cfl.getName() == null) ? "<null>" : this.cfl.getName()));
            sb.append(',');
            sb.append("serviceName");
            sb.append('=');
            sb.append(((this.cfl.getServiceName() == null) ? "<null>" : this.cfl.getServiceName()));
            sb.append(',');
            sb.append("serviceVersion");
            sb.append('=');
            sb.append(((this.cfl.getServiceVersion() == null) ? "<null>" : this.cfl.getServiceVersion()));
            sb.append(',');
            sb.append("notificationMessage");
            sb.append('=');
            sb.append(((this.cfl.getNotificationMessage() == null) ? "<null>" : this.cfl.getNotificationMessage()));
            sb.append(',');
            sb.append("httpMethod");
            sb.append('=');
            sb.append(((this.cfl.getHttpMethod() == null) ? "<null>" : this.cfl.getHttpMethod()));
            sb.append(',');
            sb.append("resource");
            sb.append('=');
            sb.append(((this.cfl.getResource() == null) ? "<null>" : this.cfl.getResource()));
            sb.append(',');
            sb.append("messageOrigin");
            sb.append('=');
            sb.append(((this.cfl.getMessageOrigin() == null) ? "<null>" : this.cfl.getMessageOrigin()));
            sb.append(',');
            sb.append("requestMessage");
            sb.append('=');
            sb.append(((this.cfl.getRequestMessage() == null) ? "<null>" : this.cfl.getRequestMessage()));
            sb.append(',');
            sb.append("responseMessage");
            sb.append('=');
            sb.append(((this.cfl.getResponseMessage() == null) ? "<null>" : this.cfl.getResponseMessage()));
            sb.append(',');
            if (sb.charAt((sb.length() - 1)) == ',')
            {
                sb.setCharAt((sb.length() - 1), ']');
            }
            else
            {
                sb.append(']');
            }
            return sb.toString();
        }

    }

}
