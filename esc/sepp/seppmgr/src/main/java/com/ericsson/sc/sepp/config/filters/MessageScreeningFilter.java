package com.ericsson.sc.sepp.config.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.expressionparser.ConditionParser;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter.Network;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningCase;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningRule;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction.ModifyJsonBodyActionType;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction.ScreeningActionType;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterData;
import com.ericsson.sc.sepp.config.ConfigUtils;
import com.ericsson.sc.sepp.model.ActionModifyJsonBody;
import com.ericsson.sc.sepp.model.ActionModifyJsonBody__1;
import com.ericsson.sc.sepp.model.ExternalNetwork;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.RequestScreeningCase;
import com.ericsson.sc.sepp.model.ResponseScreeningCase;
import com.ericsson.sc.sepp.model.ScreeningAction;
import com.ericsson.sc.sepp.model.ScreeningAction__1;
import com.ericsson.sc.sepp.model.ScreeningRule;
import com.ericsson.sc.sepp.model.ScreeningRule__1;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Utils;

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
 * Created on: Mar 16, 2021
 *     Author: epaxale, eavvann
 */

/**
 * 
 */
public class MessageScreeningFilter
{
    private static final Logger log = LoggerFactory.getLogger(MessageScreeningFilter.class);
    private static final Integer DEFAULT_MAX_MESSAGE_BYTES = 16000000;
    protected NfInstance seppInst;
    protected ProxySeppFilter seppFilter;
    protected final IfNetwork network;

    public MessageScreeningFilter(ProxySeppFilter seppFilter,
                                  NfInstance seppInst,
                                  IfNetwork network)
    {
        this.seppFilter = seppFilter;
        this.seppInst = seppInst;
        this.network = network;
    }

    /**
     * Create the SEPP screening filter based on the given configuration.
     *
     */
    public void create()
    {
        log.debug("Creating Proxy Message Screening cases.");

        var nwType = network instanceof ExternalNetwork ? Network.EXTERNAL : Network.INTERNAL;
        var inReqScreeningRef = ConfigUtils.getReferencedInRequestScreeningCases(network);
        var outRespScreeningRef = ConfigUtils.getReferencedOutResponseScreeningCases(network);
        var outReqScreeningRef = ConfigUtils.getReferencedOutRequestScreeningCases(seppInst.getNfPool());
        var inRespScreeningRef = ConfigUtils.getReferencedInResponseScreeningCases(seppInst.getNfPool());

        // unique referenced request/response screening cases
        var reqScreeningRef = new HashSet<String>(inReqScreeningRef);
        var respScreeningRef = new HashSet<String>(outRespScreeningRef);
        reqScreeningRef.addAll(outReqScreeningRef);
        respScreeningRef.addAll(inRespScreeningRef);

        if (!inReqScreeningRef.isEmpty())
        {
            var defaultInRequestScreeningCase = network.getInRequestScreeningCaseRef();
            seppFilter.setInReqScreeningRef(defaultInRequestScreeningCase);

            if (nwType.equals(Network.EXTERNAL))
            {
                ConfigUtils.createInScreeningKvTableForRPs(seppFilter, seppInst, ProxySeppFilter.INTERNAL_IN_REQUEST_TABLE_NAME);
            }
        }

        if (!outRespScreeningRef.isEmpty())
        {
            String defaultOutResponseScreeningCase = network.getOutResponseScreeningCaseRef();
            seppFilter.setOutRespScreeningRef(defaultOutResponseScreeningCase);

            if (nwType.equals(Network.EXTERNAL))
            {
                ConfigUtils.createOutScreeningKvTableForRPs(seppFilter, seppInst, ProxySeppFilter.INTERNAL_OUT_RESPONSE_TABLE_NAME);
            }
        }

        if (!outReqScreeningRef.isEmpty())
        {
            ConfigUtils.createOutRequestScreeningKvTableForPools(seppFilter, seppInst, ProxySeppFilter.INTERNAL_OUT_REQUEST_TABLE_NAME);
        }

        if (!inRespScreeningRef.isEmpty())
        {
            ConfigUtils.createInResponseScreeningKvTableForPools(seppFilter, seppInst, ProxySeppFilter.INTERNAL_IN_RESPONSE_TABLE_NAME);
        }

        if (seppInst.getMessageBodyLimits() != null)
        {
            if (seppInst.getMessageBodyLimits().getMaxBytes() != null)
            {
                seppFilter.setMaxMessageBytes(seppInst.getMessageBodyLimits().getMaxBytes());
            }
            else if (EnvVars.get("MAX_REQUEST_BYTES") != null)
            {
                seppFilter.setMaxMessageBytes(Integer.parseInt(EnvVars.get("MAX_REQUEST_BYTES")));
            }
            else
            {
                seppFilter.setMaxMessageBytes(DEFAULT_MAX_MESSAGE_BYTES);
            }

            if (seppInst.getMessageBodyLimits().getMaxLeaves() != null)
            {
                seppFilter.setMaxMessageLeaves(seppInst.getMessageBodyLimits().getMaxLeaves());
            }

            if (seppInst.getMessageBodyLimits().getMaxNestingDepth() != null)
            {
                seppFilter.setMaxMessageNestingDepth(seppInst.getMessageBodyLimits().getMaxNestingDepth());
            }
        }
        else if (EnvVars.get("MAX_REQUEST_BYTES") != null)
        {
            seppFilter.setMaxMessageBytes(Integer.parseInt(EnvVars.get("MAX_REQUEST_BYTES")));
        }
        else
        {
            seppFilter.setMaxMessageBytes(DEFAULT_MAX_MESSAGE_BYTES);
        }

        addUniqueProxyScreeningCases(reqScreeningRef, respScreeningRef);
    }

    protected ProxyScreeningCase createProxyRequestScreeningCase(RequestScreeningCase reqCase)
    {

        var proxyCase = new ProxyScreeningCase(reqCase.getName());
        proxyCase = addProxyMessageData(reqCase.getMessageDataRef(), proxyCase);

        for (ScreeningRule rule : reqCase.getScreeningRule())
        {
            proxyCase.addScreeningRule(new ProxyScreeningRule(rule.getName(),
                                                              ConditionParser.parse(rule.getCondition()),
                                                              addProxyRequestScreeningActions(rule.getScreeningAction())));
        }

        return proxyCase;
    }

    protected ProxyScreeningCase createProxyResponseScreeningCase(ResponseScreeningCase respCase)
    {

        var proxyCase = new ProxyScreeningCase(respCase.getName());
        proxyCase = addProxyMessageData(respCase.getMessageDataRef(), proxyCase);

        for (ScreeningRule__1 rule : respCase.getScreeningRule())
        {
            proxyCase.addScreeningRule(new ProxyScreeningRule(rule.getName(),
                                                              ConditionParser.parse(rule.getCondition()),
                                                              addProxyResponseScreeningActions(rule.getScreeningAction())));
        }

        return proxyCase;
    }

    public ProxyScreeningCase addProxyMessageData(List<String> messageDataRef,
                                                  ProxyScreeningCase proxyScreeningCase)
    {
        for (var mdName : messageDataRef)
        {
            var md = Utils.getByName(seppInst.getMessageData(), mdName);
            proxyScreeningCase.addMessageData(new ProxyFilterData(md.getName(),
                                                                  md.getVariableName(),
                                                                  md.getExtractorRegex(),
                                                                  md.getPath() != null,
                                                                  md.getBodyJsonPointer(),
                                                                  md.getHeader(),
                                                                  md.getRequestHeader(),
                                                                  md.getResponseHeader()));
        }

        return proxyScreeningCase;
    }

    public List<ProxyScreeningAction> addProxyRequestScreeningActions(List<ScreeningAction> actions)
    {

        List<ProxyScreeningAction> proxyActions = new ArrayList<>();

        actions.stream().forEach(action ->
        {

            if (action.getActionAddHeader() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(), ScreeningActionType.ActionAddHeader)
                                                                                                                .setHeaderName(action.getActionAddHeader()
                                                                                                                                     .getName())
                                                                                                                .setHeaderValue(action.getActionAddHeader()
                                                                                                                                      .getValue())
                                                                                                                .setIfExists(action.getActionAddHeader()
                                                                                                                                   .getIfExists()
                                                                                                                                   .value()));
            }

            if (action.getActionModifyHeader() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(),
                                                          ScreeningActionType.ActionModifyHeader).setHeaderName(action.getActionModifyHeader().getName())
                                                                                                 .setReplaceValue(action.getActionModifyHeader()
                                                                                                                        .getReplaceValue())
                                                                                                 .setReplaceFromVar(action.getActionModifyHeader()
                                                                                                                          .getReplaceFromVarName())
                                                                                                 .setPrependValue(action.getActionModifyHeader()
                                                                                                                        .getPrependValue())
                                                                                                 .setPrependFromVar(action.getActionModifyHeader()
                                                                                                                          .getPrependFromVarName())
                                                                                                 .setAppendValue(action.getActionModifyHeader()
                                                                                                                       .getAppendValue())
                                                                                                 .setAppendFromVar(action.getActionModifyHeader()
                                                                                                                         .getAppendFromVarName()));
            }
            if (action.getActionRemoveHeader() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(), ScreeningActionType.ActionRemoveHeader).setHeaderName(action.getActionRemoveHeader()
                                                                                                                                        .getName()));
            }

            if (action.getActionDropMessage() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(), ScreeningActionType.ActionDropMessage));
            }

            if (action.getActionExitScreeningCase() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(), ScreeningActionType.ActionExitScreeningCase));
            }

            if (action.getActionGoTo() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(), ScreeningActionType.ActionGoTo).setGoToCase(action.getActionGoTo()
                                                                                                                              .getRequestScreeningCaseRef()));
            }

            if (action.getActionRejectMessage() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(),
                                                          ScreeningActionType.ActionRejectMessage).setStatusCode(action.getActionRejectMessage().getStatus())
                                                                                                  .setTitle(action.getActionRejectMessage().getTitle())
                                                                                                  .setFormat(action.getActionRejectMessage()
                                                                                                                   .getFormat()
                                                                                                                   .value())
                                                                                                  .setCause(action.getActionRejectMessage().getCause())
                                                                                                  .setDetail(action.getActionRejectMessage().getDetail()));
            }

            if (action.getActionLog() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(),
                                                          ScreeningActionType.ActionLog).setLogValues(CommonConfigUtils.parseLogValuesFromText(action.getActionLog().getText())).setLogLevel(action.getActionLog().getLogLevel().value()).setMaxLogMessageLength(action.getActionLog().getMaxLogMessageLength()));
            }

            if (action.getActionCreateJsonBody() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(),
                                                          ScreeningActionType.ActionCreateJsonBody).setJsonBody(action.getActionCreateJsonBody().getJsonBody()));
            }

            if (action.getActionModifyJsonBody() != null)
            {

                ProxyScreeningAction proxyAction = setRequestModifyJsonBodyAction(new ProxyScreeningAction(action.getName(),
                                                                                                           ScreeningActionType.ActionModifyJsonBody),
                                                                                  action.getActionModifyJsonBody());
                proxyAction.getJsonPointer().ifPresentOrElse((p) ->
                {
                    proxyActions.add(proxyAction);
                }, () -> log.debug("A mandatory field in screening action: {} was not configured correctly.", action.getName()));
            }
        });

        return proxyActions;
    }

    public List<ProxyScreeningAction> addProxyResponseScreeningActions(List<ScreeningAction__1> actions)
    {

        List<ProxyScreeningAction> proxyActions = new ArrayList<>();

        actions.stream().forEach(action ->
        {

            if (action.getActionAddHeader() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(), ScreeningActionType.ActionAddHeader)
                                                                                                                .setHeaderName(action.getActionAddHeader()
                                                                                                                                     .getName())
                                                                                                                .setHeaderValue(action.getActionAddHeader()
                                                                                                                                      .getValue())
                                                                                                                .setIfExists(action.getActionAddHeader()
                                                                                                                                   .getIfExists()
                                                                                                                                   .value()));
            }

            if (action.getActionModifyHeader() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(),
                                                          ScreeningActionType.ActionModifyHeader).setHeaderName(action.getActionModifyHeader().getName())
                                                                                                 .setReplaceValue(action.getActionModifyHeader()
                                                                                                                        .getReplaceValue())
                                                                                                 .setReplaceFromVar(action.getActionModifyHeader()
                                                                                                                          .getReplaceFromVarName())
                                                                                                 .setPrependValue(action.getActionModifyHeader()
                                                                                                                        .getPrependValue())
                                                                                                 .setPrependFromVar(action.getActionModifyHeader()
                                                                                                                          .getPrependFromVarName())
                                                                                                 .setAppendValue(action.getActionModifyHeader()
                                                                                                                       .getAppendValue())
                                                                                                 .setAppendFromVar(action.getActionModifyHeader()
                                                                                                                         .getAppendFromVarName()));
            }

            if (action.getActionRemoveHeader() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(), ScreeningActionType.ActionRemoveHeader).setHeaderName(action.getActionRemoveHeader()
                                                                                                                                        .getName()));
            }

            if (action.getActionGoTo() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(), ScreeningActionType.ActionGoTo).setGoToCase(action.getActionGoTo()
                                                                                                                              .getResponseScreeningCaseRef()));
            }

            if (action.getActionExitScreeningCase() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(), ScreeningActionType.ActionExitScreeningCase));
            }

            if (action.getActionModifyStatusCode() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(),
                                                          ScreeningActionType.ActionModifyStatusCode).setStatusCode(action.getActionModifyStatusCode().getStatus()).setTitle(action.getActionModifyStatusCode().getTitle()).setCause(action.getActionModifyStatusCode().getCause()).setFormat(action.getActionModifyStatusCode().getFormat().value()).setDetail(action.getActionModifyStatusCode().getDetail()));
            }

            if (action.getActionLog() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(),
                                                          ScreeningActionType.ActionLog).setLogValues(CommonConfigUtils.parseLogValuesFromText(action.getActionLog().getText())).setLogLevel(action.getActionLog().getLogLevel().value()).setMaxLogMessageLength(action.getActionLog().getMaxLogMessageLength()));
            }

            if (action.getActionCreateJsonBody() != null)
            {
                proxyActions.add(new ProxyScreeningAction(action.getName(),
                                                          ScreeningActionType.ActionCreateJsonBody).setJsonBody(action.getActionCreateJsonBody().getJsonBody()));
            }

            if (action.getActionModifyJsonBody() != null)
            {

                ProxyScreeningAction proxyAction = setResponseModifyJsonBodyAction(new ProxyScreeningAction(action.getName(),
                                                                                                            ScreeningActionType.ActionModifyJsonBody),
                                                                                   action.getActionModifyJsonBody());
                proxyAction.getJsonPointer().ifPresentOrElse((p) ->
                {
                    proxyActions.add(proxyAction);
                }, () -> log.debug("A mandatory field in screening action: {} was not configured correctly.", action.getName()));
            }

        });

        return proxyActions;
    }

    protected ProxyScreeningAction setRequestModifyJsonBodyAction(ProxyScreeningAction proxyAction,
                                                                  ActionModifyJsonBody actionModifyJsonBody)
    {
        if (actionModifyJsonBody.getAddValue() != null)
        {

            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.AddValue)
                       .setBodyVarOrValue(actionModifyJsonBody.getAddValue().getValue())
                       .setIfPathNotExists(actionModifyJsonBody.getAddValue().getIfPathNotExists().value())
                       .setIfElementExists(actionModifyJsonBody.getAddValue().getIfElementExists().value());
        }
        else if (actionModifyJsonBody.getAddFromVarName() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.AddFromVar)
                       .setBodyVarOrValue(actionModifyJsonBody.getAddFromVarName().getVariable())
                       .setIfPathNotExists(actionModifyJsonBody.getAddFromVarName().getIfPathNotExists().value())
                       .setIfElementExists(actionModifyJsonBody.getAddFromVarName().getIfElementExists().value());
        }
        else if (actionModifyJsonBody.getReplaceValue() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.ReplaceValue)
                       .setBodyVarOrValue(actionModifyJsonBody.getReplaceValue().getValue());

        }
        else if (actionModifyJsonBody.getReplaceFromVarName() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.ReplaceFromVar)
                       .setBodyVarOrValue(actionModifyJsonBody.getReplaceFromVarName().getVariable());
        }
        else if (actionModifyJsonBody.getRemove() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer()).setModifyJsonBodyActionType(ModifyJsonBodyActionType.Remove);

        }
        else if (actionModifyJsonBody.getPrependValue() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.PrependValue)
                       .setBodyVarOrValue(actionModifyJsonBody.getPrependValue().getValue());
        }
        else if (actionModifyJsonBody.getPrependFromVarName() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.PrependFromVar)
                       .setBodyVarOrValue(actionModifyJsonBody.getPrependFromVarName().getVariable());
        }
        else if (actionModifyJsonBody.getAppendValue() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.AppendValue)
                       .setBodyVarOrValue(actionModifyJsonBody.getAppendValue().getValue());
        }
        else if (actionModifyJsonBody.getAppendFromVarName() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.AppendFromVar)
                       .setBodyVarOrValue(actionModifyJsonBody.getAppendFromVarName().getVariable());
        }
        else if (actionModifyJsonBody.getSearchReplaceString() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.SearchReplaceString)
                       .setSearchBackwards(actionModifyJsonBody.getSearchReplaceString().getSearchBackwards())
                       .setFullMatch(actionModifyJsonBody.getSearchReplaceString().getFullMatch())
                       .setCaseSensitive(actionModifyJsonBody.getSearchReplaceString().getCaseSensitive())
                       .setReplaceAll(actionModifyJsonBody.getSearchReplaceString().getReplaceAllOccurrences());

            if (actionModifyJsonBody.getSearchReplaceString().getSearchValue() != null)
                proxyAction.setSearchValue(actionModifyJsonBody.getSearchReplaceString().getSearchValue());
            else
                proxyAction.setSearchFromVar(actionModifyJsonBody.getSearchReplaceString().getSearchFromVarName());

            if (actionModifyJsonBody.getSearchReplaceString().getReplaceValue() != null)
                proxyAction.setReplaceValue(actionModifyJsonBody.getSearchReplaceString().getReplaceValue());
            else
                proxyAction.setReplaceFromVar(actionModifyJsonBody.getSearchReplaceString().getReplaceFromVarName());
        }
        else if (actionModifyJsonBody.getSearchReplaceRegex() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.SearchReplaceRegex)
                       .setSearchValue(actionModifyJsonBody.getSearchReplaceRegex().getSearchRegex())
                       .setReplaceAll(actionModifyJsonBody.getSearchReplaceRegex().getReplaceAllOccurrences());

            if (actionModifyJsonBody.getSearchReplaceRegex().getReplaceValue() != null)
                proxyAction.setReplaceValue(actionModifyJsonBody.getSearchReplaceRegex().getReplaceValue());
            else
                proxyAction.setReplaceFromVar(actionModifyJsonBody.getSearchReplaceRegex().getReplaceFromVarName());
        }

        return proxyAction;

    }

    protected ProxyScreeningAction setResponseModifyJsonBodyAction(ProxyScreeningAction proxyAction,
                                                                   ActionModifyJsonBody__1 actionModifyJsonBody)
    {
        if (actionModifyJsonBody.getAddValue() != null)
        {

            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.AddValue)
                       .setBodyVarOrValue(actionModifyJsonBody.getAddValue().getValue())
                       .setIfPathNotExists(actionModifyJsonBody.getAddValue().getIfPathNotExists().value())
                       .setIfElementExists(actionModifyJsonBody.getAddValue().getIfElementExists().value());
        }
        else if (actionModifyJsonBody.getAddFromVarName() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.AddFromVar)
                       .setBodyVarOrValue(actionModifyJsonBody.getAddFromVarName().getVariable())
                       .setIfPathNotExists(actionModifyJsonBody.getAddFromVarName().getIfPathNotExists().value())
                       .setIfElementExists(actionModifyJsonBody.getAddFromVarName().getIfElementExists().value());
        }
        else if (actionModifyJsonBody.getReplaceValue() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.ReplaceValue)
                       .setBodyVarOrValue(actionModifyJsonBody.getReplaceValue().getValue());

        }
        else if (actionModifyJsonBody.getReplaceFromVarName() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.ReplaceFromVar)
                       .setBodyVarOrValue(actionModifyJsonBody.getReplaceFromVarName().getVariable());
        }
        else if (actionModifyJsonBody.getRemove() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer()).setModifyJsonBodyActionType(ModifyJsonBodyActionType.Remove);

        }
        else if (actionModifyJsonBody.getPrependValue() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.PrependValue)
                       .setBodyVarOrValue(actionModifyJsonBody.getPrependValue().getValue());
        }
        else if (actionModifyJsonBody.getPrependFromVarName() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.PrependFromVar)
                       .setBodyVarOrValue(actionModifyJsonBody.getPrependFromVarName().getVariable());
        }
        else if (actionModifyJsonBody.getAppendValue() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.AppendValue)
                       .setBodyVarOrValue(actionModifyJsonBody.getAppendValue().getValue());
        }
        else if (actionModifyJsonBody.getAppendFromVarName() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.AppendFromVar)
                       .setBodyVarOrValue(actionModifyJsonBody.getAppendFromVarName().getVariable());
        }
        else if (actionModifyJsonBody.getSearchReplaceString() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.SearchReplaceString)
                       .setSearchBackwards(actionModifyJsonBody.getSearchReplaceString().getSearchBackwards())
                       .setFullMatch(actionModifyJsonBody.getSearchReplaceString().getFullMatch())
                       .setCaseSensitive(actionModifyJsonBody.getSearchReplaceString().getCaseSensitive())
                       .setReplaceAll(actionModifyJsonBody.getSearchReplaceString().getReplaceAllOccurrences());

            if (actionModifyJsonBody.getSearchReplaceString().getSearchValue() != null)
                proxyAction.setSearchValue(actionModifyJsonBody.getSearchReplaceString().getSearchValue());
            else
                proxyAction.setSearchFromVar(actionModifyJsonBody.getSearchReplaceString().getSearchFromVarName());

            if (actionModifyJsonBody.getSearchReplaceString().getReplaceValue() != null)
                proxyAction.setReplaceValue(actionModifyJsonBody.getSearchReplaceString().getReplaceValue());
            else
                proxyAction.setReplaceFromVar(actionModifyJsonBody.getSearchReplaceString().getReplaceFromVarName());
        }
        else if (actionModifyJsonBody.getSearchReplaceRegex() != null)
        {
            proxyAction.setJsonPointer(actionModifyJsonBody.getJsonPointer())
                       .setModifyJsonBodyActionType(ModifyJsonBodyActionType.SearchReplaceRegex)
                       .setSearchValue(actionModifyJsonBody.getSearchReplaceRegex().getSearchRegex())
                       .setReplaceAll(actionModifyJsonBody.getSearchReplaceRegex().getReplaceAllOccurrences());

            if (actionModifyJsonBody.getSearchReplaceRegex().getReplaceValue() != null)
                proxyAction.setReplaceValue(actionModifyJsonBody.getSearchReplaceRegex().getReplaceValue());
            else
                proxyAction.setReplaceFromVar(actionModifyJsonBody.getSearchReplaceRegex().getReplaceFromVarName());
        }

        return proxyAction;

    }

    protected void addProxyRequestScreeningCasesFromGoToActions(String referencedCase,
                                                                List<ProxyScreeningCase> goToScreeningCases)
    {
        seppInst.getRequestScreeningCase()
                .stream()
                .filter(reqCase -> reqCase.getName().equals(referencedCase))
                .flatMap(reqCase -> reqCase.getScreeningRule().stream())
                .flatMap(rule -> rule.getScreeningAction().stream())
                .forEach(action ->
                {

                    if (action.getActionGoTo() != null)
                    {
                        boolean result = goToScreeningCases.stream().anyMatch(sc -> sc.getName().equals(action.getActionGoTo().getRequestScreeningCaseRef()));
                        if (!result)
                        {
                            RequestScreeningCase reqCase = Utils.getByName(seppInst.getRequestScreeningCase(),
                                                                           action.getActionGoTo().getRequestScreeningCaseRef());
                            ProxyScreeningCase proxyCase = createProxyRequestScreeningCase(reqCase);
                            goToScreeningCases.add(proxyCase);

                            addProxyRequestScreeningCasesFromGoToActions(proxyCase.getName(), goToScreeningCases);
                        }
                    }
                });
    }

    protected void addProxyResponseScreeningCasesFromGoToActions(String referencedCase,
                                                                 List<ProxyScreeningCase> goToScreeningCases)
    {
        seppInst.getResponseScreeningCase()
                .stream()
                .filter(respCase -> respCase.getName().equals(referencedCase))
                .flatMap(respCase -> respCase.getScreeningRule().stream())
                .flatMap(rule -> rule.getScreeningAction().stream())
                .forEach(action ->
                {

                    if (action.getActionGoTo() != null)
                    {
                        boolean result = goToScreeningCases.stream().anyMatch(sc -> sc.getName().equals(action.getActionGoTo().getResponseScreeningCaseRef()));
                        if (!result)
                        {
                            ResponseScreeningCase respCase = Utils.getByName(seppInst.getResponseScreeningCase(),
                                                                             action.getActionGoTo().getResponseScreeningCaseRef());
                            ProxyScreeningCase proxyCase = createProxyResponseScreeningCase(respCase);
                            goToScreeningCases.add(proxyCase);

                            addProxyResponseScreeningCasesFromGoToActions(proxyCase.getName(), goToScreeningCases);
                        }
                    }
                });
    }

    protected void addUniqueProxyScreeningCases(Set<String> reqScreeningRef,
                                                Set<String> respScreeningRef)
    {
        if (!reqScreeningRef.isEmpty())
        {
            List<ProxyScreeningCase> goToReqScreeningCases = new ArrayList<>();

            seppInst.getRequestScreeningCase().stream().filter(reqCase -> reqScreeningRef.contains(reqCase.getName())).forEach(rc ->
            {
                ProxyScreeningCase proxyCase = createProxyRequestScreeningCase(rc);
                seppFilter.addScreeningCase(proxyCase);
                addProxyRequestScreeningCasesFromGoToActions(proxyCase.getName(), goToReqScreeningCases);
            });

            goToReqScreeningCases.stream()
                                 .filter(goToCase -> !reqScreeningRef.contains(goToCase.getName()))
                                 .forEach(prCase -> seppFilter.addScreeningCase(prCase));
        }

        if (!respScreeningRef.isEmpty())
        {
            List<ProxyScreeningCase> goToRespScreeningCases = new ArrayList<>();

            seppInst.getResponseScreeningCase().stream().filter(respCase -> respScreeningRef.contains(respCase.getName())).forEach(rc ->
            {
                ProxyScreeningCase proxyCase = createProxyResponseScreeningCase(rc);
                seppFilter.addScreeningCase(proxyCase);
                addProxyResponseScreeningCasesFromGoToActions(proxyCase.getName(), goToRespScreeningCases);
            });

            goToRespScreeningCases.stream()
                                  .filter(goToCase -> !respScreeningRef.contains(goToCase.getName()))
                                  .forEach(prCase -> seppFilter.addScreeningCase(prCase));

        }
    }

}
