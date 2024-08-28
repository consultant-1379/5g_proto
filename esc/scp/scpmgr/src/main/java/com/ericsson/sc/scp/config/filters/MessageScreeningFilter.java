package com.ericsson.sc.scp.config.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.expressionparser.ConditionParser;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig.ProxySeppFilter;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningCase;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningRule;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction.ModifyJsonBodyActionType;
import com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig.ProxyScreeningAction.ScreeningActionType;
import com.ericsson.sc.proxyal.proxyconfig.proxyroutingconfig.ProxyFilterData;
import com.ericsson.sc.scp.config.ConfigUtils;
import com.ericsson.sc.scp.model.ActionModifyJsonBody;
import com.ericsson.sc.scp.model.ActionModifyJsonBody__1;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.RequestScreeningCase;
import com.ericsson.sc.scp.model.ResponseScreeningCase;
import com.ericsson.sc.scp.model.ScreeningAction;
import com.ericsson.sc.scp.model.ScreeningAction__1;
import com.ericsson.sc.scp.model.ScreeningRule;
import com.ericsson.sc.scp.model.ScreeningRule__1;
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
    private NfInstance scpInst;
    private ProxySeppFilter scpFilter;
    private final IfNetwork network;

    public MessageScreeningFilter(ProxySeppFilter scpFilter,
                                  NfInstance scpInst,
                                  IfNetwork network)
    {
        this.scpFilter = scpFilter;
        this.scpInst = scpInst;
        this.network = network;
    }

    /**
     * Create the SCP screening filter based on the given configuration.
     *
     */
    public void create()
    {
        log.debug("Creating Proxy Message Screening cases.");

        var inReqScreeningRef = network.getInRequestScreeningCaseRef();
        scpFilter.setInReqScreeningRef(inReqScreeningRef);

        var outRespScreeningRef = network.getOutResponseScreeningCaseRef();
        scpFilter.setOutRespScreeningRef(outRespScreeningRef);

        var outReqScreeningRef = ConfigUtils.getReferencedOutRequestScreeningCases(scpInst);
        var inRespScreeningRef = ConfigUtils.getReferencedInResponseScreeningCases(scpInst);

        // unique referenced request/response screening cases
        var reqScreeningRef = new HashSet<String>(outReqScreeningRef);
        var respScreeningRef = new HashSet<String>(inRespScreeningRef);

        if (inReqScreeningRef != null)
        {
            reqScreeningRef.add(inReqScreeningRef);
        }

        if (outRespScreeningRef != null)
        {
            respScreeningRef.add(outRespScreeningRef);
        }

        if (!outReqScreeningRef.isEmpty())
        {
            ConfigUtils.createOutRequestScreeningKvTableForPools(scpFilter, scpInst, ProxySeppFilter.INTERNAL_OUT_REQUEST_TABLE_NAME);
        }

        if (!inRespScreeningRef.isEmpty())
        {
            ConfigUtils.createInResponseScreeningKvTableForPools(scpFilter, scpInst, ProxySeppFilter.INTERNAL_IN_RESPONSE_TABLE_NAME);
        }

        if (scpInst.getMessageBodyLimits() != null)
        {
            if (scpInst.getMessageBodyLimits().getMaxBytes() != null)
            {
                scpFilter.setMaxMessageBytes(scpInst.getMessageBodyLimits().getMaxBytes());
            }
            else if (EnvVars.get("MAX_REQUEST_BYTES") != null)
            {
                scpFilter.setMaxMessageBytes(Integer.parseInt(EnvVars.get("MAX_REQUEST_BYTES")));
            }
            else
            {
                scpFilter.setMaxMessageBytes(DEFAULT_MAX_MESSAGE_BYTES);
            }

            if (scpInst.getMessageBodyLimits().getMaxLeaves() != null)
            {
                scpFilter.setMaxMessageLeaves(scpInst.getMessageBodyLimits().getMaxLeaves());
            }

            if (scpInst.getMessageBodyLimits().getMaxNestingDepth() != null)
            {
                scpFilter.setMaxMessageNestingDepth(scpInst.getMessageBodyLimits().getMaxNestingDepth());
            }
        }
        else if (EnvVars.get("MAX_REQUEST_BYTES") != null)
        {
            scpFilter.setMaxMessageBytes(Integer.parseInt(EnvVars.get("MAX_REQUEST_BYTES")));
        }
        else
        {
            scpFilter.setMaxMessageBytes(DEFAULT_MAX_MESSAGE_BYTES);
        }

        addUniqueProxyScreeningCases(reqScreeningRef, respScreeningRef);
    }

    private ProxyScreeningCase createProxyRequestScreeningCase(RequestScreeningCase reqCase)
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

    private ProxyScreeningCase createProxyResponseScreeningCase(ResponseScreeningCase respCase)
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
            var md = Utils.getByName(scpInst.getMessageData(), mdName);
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
                                                                                                  .setCause(action.getActionRejectMessage().getCause())
                                                                                                  .setFormat(action.getActionRejectMessage()
                                                                                                                   .getFormat()
                                                                                                                   .value())
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
                                                          ScreeningActionType.ActionLog).setLogLevel(action.getActionLog().getLogLevel().value())
                                                                                        .setLogValues(CommonConfigUtils.parseLogValuesFromText(action.getActionLog()
                                                                                                                                                     .getText()))
                                                                                        .setMaxLogMessageLength(action.getActionLog()
                                                                                                                      .getMaxLogMessageLength()));
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

    private ProxyScreeningAction setRequestModifyJsonBodyAction(ProxyScreeningAction proxyAction,
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

    private ProxyScreeningAction setResponseModifyJsonBodyAction(ProxyScreeningAction proxyAction,
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

    private void addProxyRequestScreeningCasesFromGoToActions(String referencedCase,
                                                              List<ProxyScreeningCase> goToScreeningCases)
    {
        scpInst.getRequestScreeningCase()
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
                           RequestScreeningCase reqCase = Utils.getByName(scpInst.getRequestScreeningCase(),
                                                                          action.getActionGoTo().getRequestScreeningCaseRef());
                           ProxyScreeningCase proxyCase = createProxyRequestScreeningCase(reqCase);
                           goToScreeningCases.add(proxyCase);

                           addProxyRequestScreeningCasesFromGoToActions(proxyCase.getName(), goToScreeningCases);
                       }
                   }
               });
    }

    private void addProxyResponseScreeningCasesFromGoToActions(String referencedCase,
                                                               List<ProxyScreeningCase> goToScreeningCases)
    {
        scpInst.getResponseScreeningCase()
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
                           ResponseScreeningCase respCase = Utils.getByName(scpInst.getResponseScreeningCase(),
                                                                            action.getActionGoTo().getResponseScreeningCaseRef());
                           ProxyScreeningCase proxyCase = createProxyResponseScreeningCase(respCase);
                           goToScreeningCases.add(proxyCase);

                           addProxyResponseScreeningCasesFromGoToActions(proxyCase.getName(), goToScreeningCases);
                       }
                   }
               });
    }

    private void addUniqueProxyScreeningCases(HashSet<String> reqScreeningRef,
                                              HashSet<String> respScreeningRef)
    {
        if (!reqScreeningRef.isEmpty())
        {
            List<ProxyScreeningCase> goToReqScreeningCases = new ArrayList<>();

            scpInst.getRequestScreeningCase().stream().filter(reqCase -> reqScreeningRef.contains(reqCase.getName())).forEach(rc ->
            {
                ProxyScreeningCase proxyCase = createProxyRequestScreeningCase(rc);
                scpFilter.addScreeningCase(proxyCase);
                addProxyRequestScreeningCasesFromGoToActions(proxyCase.getName(), goToReqScreeningCases);
            });

            goToReqScreeningCases.stream()
                                 .filter(goToCase -> !reqScreeningRef.contains(goToCase.getName()))
                                 .forEach(prCase -> scpFilter.addScreeningCase(prCase));
        }

        if (!respScreeningRef.isEmpty())
        {
            List<ProxyScreeningCase> goToRespScreeningCases = new ArrayList<>();

            scpInst.getResponseScreeningCase().stream().filter(respCase -> respScreeningRef.contains(respCase.getName())).forEach(rc ->
            {
                ProxyScreeningCase proxyCase = createProxyResponseScreeningCase(rc);
                scpFilter.addScreeningCase(proxyCase);
                addProxyResponseScreeningCasesFromGoToActions(proxyCase.getName(), goToRespScreeningCases);
            });

            goToRespScreeningCases.stream()
                                  .filter(goToCase -> !respScreeningRef.contains(goToCase.getName()))
                                  .forEach(prCase -> scpFilter.addScreeningCase(prCase));
        }
    }

}
