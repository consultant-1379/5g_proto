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
 * Created on: Mar 10, 2021
 *     Author: epaxale
 */

package com.ericsson.sc.proxyal.proxyconfig.proxymessagescreenigconfig;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.Action;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.AddHeaderAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.AddToJson;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.AddToJson.IfJsonElementExists;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.AddToJson.IfJsonPathNotExists;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.CreateBodyAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.DynamicKvtLookup;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.IfExists;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.JsonOperation;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.LogAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.LogValue;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.LoggingLevel;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.MessageBodyType;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ModifyHeaderAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ModifyJsonBodyAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ModifyJsonValue;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ModifyQueryParamAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ModifyStatusCodeAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RejectMessageAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RemoveFromJson;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.RemoveHeaderAction;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ReplaceInJson;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.ScramblingProfile;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.SearchAndReplace;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.SearchAndReplace.ReplaceOptions;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.SearchAndReplace.SearchOptions;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.StringModifier;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.StringModifierList;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.VarHeaderConstValue;
import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.VarOrString;

/**
 * 
 */
public class ProxyScreeningAction
{

    public enum ScreeningActionType
    {
        ActionAddHeader,
        ActionModifyHeader,
        ActionRemoveHeader,
        ActionDropMessage,
        ActionExitScreeningCase,
        ActionGoTo,
        ActionRejectMessage,
        ActionModifyStatusCode,
        ActionLog,
        ActionModifyJsonBody,
        ActionCreateJsonBody,
        ActionModifyQueryParam
    }

    public enum ModifyJsonBodyActionType
    {
        AddValue,
        AddFromVar,
        ReplaceValue,
        ReplaceFromVar,
        Remove,
        ReplaceFromMapTable,
        ReplaceScrambling,
        PrependValue,
        PrependFromVar,
        AppendValue,
        AppendFromVar,
        SearchReplaceString,
        SearchReplaceRegex
    }

    private enum VarOrValue
    {
        Var,
        Value
    }

    private static final String JSON_CONTENT_TYPE = "application/json";
    private final String name;
    private final ScreeningActionType action;
    private String headerName;
    private Optional<String> headerValue = Optional.empty();
    private Optional<String> searchValue = Optional.empty();
    private Optional<String> searchFromVar = Optional.empty();
    private Optional<String> replaceValue = Optional.empty();
    private Optional<String> replaceFromVar = Optional.empty();
    private Optional<String> prependValue = Optional.empty();
    private Optional<String> prependFromVar = Optional.empty();
    private Optional<String> appendValue = Optional.empty();
    private Optional<String> appendFromVar = Optional.empty();
    private Optional<String> goToCase = Optional.empty();
    private Optional<Integer> statusCode = Optional.empty();
    private Optional<String> title = Optional.empty();
    private Optional<String> cause = Optional.empty();
    private MessageBodyType format;
    private Optional<String> detail = Optional.empty();
    private Optional<List<LogValue>> logValues = Optional.empty();
    private LoggingLevel logLevel;
    private Optional<Integer> maxLogMessageLength;
    private IfExists ifExists;
    private ModifyJsonBodyActionType modifyJsonBodyActionType;
    private Optional<String> jsonPointer = Optional.empty();
    private Optional<String> bodyVarOrValue = Optional.empty();
    private IfJsonPathNotExists ifPathNotExists;
    private IfJsonElementExists ifElementExists;
    private Optional<String> jsonBody = Optional.empty();
    private Optional<DynKVTable> dynKvTable = Optional.empty();
    private Optional<ScrambleData> scrambleData = Optional.empty();
    private Optional<String> queryName = Optional.empty();
    private boolean isCaseSensitive;
    private boolean isFullMatch;
    private boolean isSearchBackwards;
    private boolean isReplaceAll;

    public ProxyScreeningAction(String name,
                                ScreeningActionType action)
    {
        this.name = name;
        this.action = action;
    }

    /**
     * @param proxyMessageScreeningBuilder
     */
    public ProxyScreeningAction(ProxyScreeningAction proxyMessageScreeningBuilder)
    {
        this.name = proxyMessageScreeningBuilder.getName();
        this.action = proxyMessageScreeningBuilder.getAction();
        this.headerName = proxyMessageScreeningBuilder.getHeaderName();
        this.headerValue = proxyMessageScreeningBuilder.getHeaderValue();
        this.searchValue = proxyMessageScreeningBuilder.getSearchValue();
        this.searchFromVar = proxyMessageScreeningBuilder.getSearchFromVar();
        this.replaceValue = proxyMessageScreeningBuilder.getReplaceValue();
        this.replaceFromVar = proxyMessageScreeningBuilder.getReplaceFromVar();
        this.prependValue = proxyMessageScreeningBuilder.getPrependValue();
        this.prependFromVar = proxyMessageScreeningBuilder.getPrependFromVar();
        this.appendValue = proxyMessageScreeningBuilder.getAppendValue();
        this.appendFromVar = proxyMessageScreeningBuilder.getAppendFromVar();
        this.goToCase = proxyMessageScreeningBuilder.getGoToCase();
        this.statusCode = proxyMessageScreeningBuilder.getStatusCode();
        this.title = proxyMessageScreeningBuilder.getTitle();
        this.cause = proxyMessageScreeningBuilder.getCause();
        this.format = proxyMessageScreeningBuilder.getFormat();
        this.detail = proxyMessageScreeningBuilder.getDetail();
        this.logValues = proxyMessageScreeningBuilder.getLogValues();
        this.logLevel = proxyMessageScreeningBuilder.getLogLevel();
        this.maxLogMessageLength = proxyMessageScreeningBuilder.getMaxLogMessageLength();
        this.ifExists = proxyMessageScreeningBuilder.getIfExists();
        this.modifyJsonBodyActionType = proxyMessageScreeningBuilder.getModifyJsonBodyActionType();
        this.jsonPointer = proxyMessageScreeningBuilder.getJsonPointer();
        this.bodyVarOrValue = proxyMessageScreeningBuilder.getBodyVarOrValue();
        this.ifPathNotExists = proxyMessageScreeningBuilder.getIfPathNotExists();
        this.ifElementExists = proxyMessageScreeningBuilder.getIfElementExists();
        this.dynKvTable = proxyMessageScreeningBuilder.getDynKvTable();
        this.scrambleData = proxyMessageScreeningBuilder.getScrambleData();
        this.queryName = proxyMessageScreeningBuilder.getQueryName();
        this.isCaseSensitive = proxyMessageScreeningBuilder.isCaseSensitive();
        this.isFullMatch = proxyMessageScreeningBuilder.isFullMatch();
        this.isSearchBackwards = proxyMessageScreeningBuilder.isSearchBackwards();
        this.isReplaceAll = proxyMessageScreeningBuilder.isReplaceAll();
    }

    public static class DynKVTable
    {
        private String name;
        private String fcUnsuccessfulOp;
        boolean doNothing = false;

        public DynKVTable(String name)
        {
            this.name = name;
        }

        public DynKVTable(String name,
                          String fcUnsuccessfulOp)
        {
            this.name = name;
            this.fcUnsuccessfulOp = fcUnsuccessfulOp;
        }

        public DynKVTable(String name,
                          boolean doNothing)
        {
            this.name = name;
            this.doNothing = doNothing;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getFcUnsuccessfulOp()
        {
            return fcUnsuccessfulOp;
        }

        public DynKVTable setFcUnsuccessfulOp(String fcUnsuccessfulOp)
        {
            this.fcUnsuccessfulOp = fcUnsuccessfulOp;
            return this;
        }

        public boolean doNothing()
        {
            return this.doNothing;
        }

        public DynKVTable setDoNothing()
        {
            this.doNothing = true;
            return this;
        }
    }

    public static class ScrambleData
    {
        private String onFqdnUnsuccessfullAction;
        boolean doNothing = false;

        public ScrambleData(String onFqdnUnsuccessfullAction)
        {
            this.onFqdnUnsuccessfullAction = onFqdnUnsuccessfullAction;
        }

        public ScrambleData(boolean don)
        {
            this.doNothing = don;
        }

        public String getOnFqdnUnsuccessfullAction()
        {
            return onFqdnUnsuccessfullAction;
        }

        public void setOnFqdnUnsuccessfullAction(String unsuccsessfullAction)
        {
            this.onFqdnUnsuccessfullAction = unsuccsessfullAction;

        }

        public boolean doNothing()
        {
            return this.doNothing;
        }
    }

    /**
     * @return the action
     */
    public ScreeningActionType getAction()
    {
        return action;
    }

    /**
     * @return the headerName
     */
    public String getHeaderName()
    {
        return headerName;
    }

    /**
     * @param headerName the headerName to set
     */
    public ProxyScreeningAction setHeaderName(String headerName)
    {
        this.headerName = headerName;
        return this;
    }

    /**
     * @return the jsonPointer
     */
    public Optional<String> getJsonPointer()
    {
        return jsonPointer;
    }

    /**
     * @param jsonPointer the jsonPointer to set
     */
    public ProxyScreeningAction setJsonPointer(String jsonPointer)
    {
        this.jsonPointer = Optional.ofNullable(jsonPointer);
        return this;
    }

    /**
     * @return the modifyJsonBodyActionType
     */
    public ModifyJsonBodyActionType getModifyJsonBodyActionType()
    {
        return modifyJsonBodyActionType;
    }

    /**
     * @param modifyJsonBodyActionType the modifyJsonBodyActionType to set
     */
    public ProxyScreeningAction setModifyJsonBodyActionType(ModifyJsonBodyActionType modifyJsonBodyActionType)
    {
        this.modifyJsonBodyActionType = modifyJsonBodyActionType;
        return this;
    }

    /**
     * @return the bodyVarOrValue
     */
    public Optional<String> getBodyVarOrValue()
    {
        return bodyVarOrValue;
    }

    /**
     * @param modifyJsonBodyActionType the modifyJsonBodyActionType to set
     */
    public ProxyScreeningAction setBodyVarOrValue(String bodyVarOrValue)
    {
        this.bodyVarOrValue = Optional.ofNullable(bodyVarOrValue);
        return this;
    }

    /**
     * @return the headerValue
     */
    public Optional<String> getHeaderValue()
    {
        return headerValue;
    }

    /**
     * @param headerValue the headerValue to set
     */
    public ProxyScreeningAction setHeaderValue(String headerValue)
    {
        this.headerValue = Optional.ofNullable(headerValue);
        return this;
    }

    /**
     * @return the searchValue
     */
    public Optional<String> getSearchValue()
    {
        return searchValue;
    }

    /**
     * @param searchValue the searchValue to set
     */
    public ProxyScreeningAction setSearchValue(String searchValue)
    {
        this.searchValue = Optional.ofNullable(searchValue);
        return this;
    }

    /**
     * @return the searchFromVar
     */
    public Optional<String> getSearchFromVar()
    {
        return searchFromVar;
    }

    /**
     * @param searchFromVar the searchFromVar to set
     */
    public ProxyScreeningAction setSearchFromVar(String searchFromVar)
    {
        this.searchFromVar = Optional.ofNullable(searchFromVar);
        return this;
    }

    /**
     * @return the replaceValue
     */
    public Optional<String> getReplaceValue()
    {
        return replaceValue;
    }

    /**
     * @param replaceValue the replaceValue to set
     */
    public ProxyScreeningAction setReplaceValue(String replaceValue)
    {
        this.replaceValue = Optional.ofNullable(replaceValue);
        return this;
    }

    /**
     * @return the replaceFromVar
     */
    public Optional<String> getReplaceFromVar()
    {
        return replaceFromVar;
    }

    /**
     * @param replaceFromVar the replaceFromVar to set
     */
    public ProxyScreeningAction setReplaceFromVar(String replaceFromVar)
    {
        this.replaceFromVar = Optional.ofNullable(replaceFromVar);
        return this;
    }

    /**
     * @return the isCaseSensitive
     */
    public boolean isCaseSensitive()
    {
        return isCaseSensitive;
    }

    /**
     * @param isCaseSensitive the isCaseSensitive to set
     */
    public ProxyScreeningAction setCaseSensitive(boolean isCaseSensitive)
    {
        this.isCaseSensitive = isCaseSensitive;
        return this;
    }

    /**
     * @return the prependValue
     */
    public Optional<String> getPrependValue()
    {
        return prependValue;
    }

    /**
     * @param prependValue the prependValue to set
     */
    public ProxyScreeningAction setPrependValue(String prependValue)
    {
        this.prependValue = Optional.ofNullable(prependValue);
        return this;
    }

    /**
     * @return the prependFromVar
     */
    public Optional<String> getPrependFromVar()
    {
        return prependFromVar;
    }

    /**
     * @param prependFromVar the prependFromVar to set
     */
    public ProxyScreeningAction setPrependFromVar(String prependFromVar)
    {
        this.prependFromVar = Optional.ofNullable(prependFromVar);
        return this;
    }

    /**
     * @return the appendValue
     */
    public Optional<String> getAppendValue()
    {
        return appendValue;
    }

    /**
     * @param appendValue the appendValue to set
     */
    public ProxyScreeningAction setAppendValue(String appendValue)
    {
        this.appendValue = Optional.ofNullable(appendValue);
        return this;
    }

    /**
     * @return the appendFromVar
     */
    public Optional<String> getAppendFromVar()
    {
        return appendFromVar;
    }

    /**
     * @param appendFromVar the appendFromVar to set
     */
    public ProxyScreeningAction setAppendFromVar(String appendFromVar)
    {
        this.appendFromVar = Optional.ofNullable(appendFromVar);
        return this;
    }

    /**
     * @return the goToCase
     */
    public Optional<String> getGoToCase()
    {
        return goToCase;
    }

    /**
     * @param goToCase the goToCase to set
     */
    public ProxyScreeningAction setGoToCase(String goToCase)
    {
        this.goToCase = Optional.ofNullable(goToCase);
        return this;
    }

    /**
     * @return the statusCode
     */
    public Optional<Integer> getStatusCode()
    {
        return statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    public ProxyScreeningAction setStatusCode(Integer statusCode)
    {
        this.statusCode = Optional.ofNullable(statusCode);
        return this;
    }

    /**
     * @return the title
     */
    public Optional<String> getTitle()
    {
        return title;
    }

    /**
     * @param statusMessage the statusMessage to set
     */
    public ProxyScreeningAction setTitle(String title)
    {
        this.title = Optional.ofNullable(title);
        return this;
    }

    /**
     * @return the cause
     */
    public Optional<String> getCause()
    {
        return cause;
    }

    /**
     * @param cause the cause to set
     */
    public ProxyScreeningAction setCause(String cause)
    {
        this.cause = Optional.ofNullable(cause);
        return this;
    }

    /**
     * @return the format
     */
    public MessageBodyType getFormat()
    {
        return format;
    }

    /**
     * @param format the format to set
     */
    public ProxyScreeningAction setFormat(String format)
    {
        if (format.equals("json"))
            this.format = MessageBodyType.JSON;
        if (format.equals("text"))
            this.format = MessageBodyType.PLAIN_TEXT;

        return this;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the detail
     */
    public Optional<String> getDetail()
    {
        return detail;
    }

    /**
     * @param detail the detail to set
     */
    public ProxyScreeningAction setDetail(String detail)
    {
        this.detail = Optional.ofNullable(detail);
        return this;
    }

    /**
     * @return the logValues
     */
    public Optional<List<LogValue>> getLogValues()
    {
        return logValues;
    }

    /**
     * @param logValues the logValues to set
     */
    public ProxyScreeningAction setLogValues(List<LogValue> logValues)
    {
        this.logValues = Optional.ofNullable(logValues);

        return this;
    }

    /**
     * @return the logLevel
     */
    public LoggingLevel getLogLevel()
    {
        return logLevel;
    }

    /**
     * @param logLevel the logLevel to set
     */
    public ProxyScreeningAction setLogLevel(String logLevel)
    {
        if (logLevel.equals("debug"))
            this.logLevel = LoggingLevel.DEBUG;
        else if (logLevel.equals("warn"))
            this.logLevel = LoggingLevel.WARN;
        else if (logLevel.equals("error"))
            this.logLevel = LoggingLevel.ERROR;
        else if (logLevel.equals("trace"))
            this.logLevel = LoggingLevel.TRACE;
        else if (logLevel.equals("info"))
            this.logLevel = LoggingLevel.INFO;

        return this;
    }

    /**
     * @return the maxLogMessageLength
     */
    public Optional<Integer> getMaxLogMessageLength()
    {
        return maxLogMessageLength;
    }

    /**
     * @param maxLogMessageLength the maxLogMessageLength to set
     */
    public ProxyScreeningAction setMaxLogMessageLength(Integer maxLogMessageLength)
    {
        this.maxLogMessageLength = Optional.ofNullable(maxLogMessageLength);
        return this;
    }

    /**
     * @return the ifExists
     */
    public IfExists getIfExists()
    {
        return ifExists;
    }

    /**
     * @param ifExists the ifExists to set
     */
    public ProxyScreeningAction setIfExists(String ifExists)
    {
        if (ifExists.equals("no-action"))
            this.ifExists = IfExists.NO_ACTION;
        else if (ifExists.equals("add"))
            this.ifExists = IfExists.ADD;
        else if (ifExists.equals("replace"))
            this.ifExists = IfExists.REPLACE;

        return this;
    }

    /**
     * @return the ifPathNotExists
     */
    public IfJsonPathNotExists getIfPathNotExists()
    {
        return ifPathNotExists;
    }

    /**
     * @param ifPathNotExists the ifPathNotExists to set
     */
    public ProxyScreeningAction setIfPathNotExists(String ifPathNotExists)
    {
        if (ifPathNotExists.equals("no-action"))
            this.ifPathNotExists = IfJsonPathNotExists.DO_NOTHING;
        else if (ifPathNotExists.equals("create"))
            this.ifPathNotExists = IfJsonPathNotExists.CREATE;

        return this;
    }

    /**
     * @return the ifElementExists
     */
    public IfJsonElementExists getIfElementExists()
    {
        return ifElementExists;
    }

    /**
     * @param ifElementExists the ifElementExists to set
     */
    public ProxyScreeningAction setIfElementExists(String ifElementExists)
    {
        if (ifElementExists.equals("no-action"))
            this.ifElementExists = IfJsonElementExists.NO_ACTION;
        else if (ifElementExists.equals("replace"))
            this.ifElementExists = IfJsonElementExists.REPLACE;

        return this;
    }

    /**
     * @return the jsonBody
     */
    public Optional<String> getJsonBody()
    {
        return jsonBody;
    }

    /**
     * @param jsonBody the jsonBody to set
     */
    public ProxyScreeningAction setJsonBody(String jsonBody)
    {
        this.jsonBody = Optional.ofNullable(jsonBody);
        return this;
    }

    public Optional<DynKVTable> getDynKvTable()
    {
        return this.dynKvTable;
    }

    public ProxyScreeningAction setDynKvTable(DynKVTable dynKvTable)
    {
        this.dynKvTable = Optional.ofNullable(dynKvTable);
        return this;
    }

    public Optional<ScrambleData> getScrambleData()
    {
        return this.scrambleData;
    }

    public ProxyScreeningAction setScramblingData(ScrambleData scrambleData)
    {
        this.scrambleData = Optional.ofNullable(scrambleData);
        return this;
    }

    public Optional<String> getQueryName()
    {
        return queryName;
    }

    public ProxyScreeningAction setQueryName(String queryName)
    {
        this.queryName = Optional.ofNullable(queryName);
        return this;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(action,
                            appendFromVar,
                            appendValue,
                            detail,
                            format,
                            goToCase,
                            headerName,
                            headerValue,
                            ifExists,
                            logLevel,
                            name,
                            prependFromVar,
                            prependValue,
                            replaceFromVar,
                            replaceValue,
                            searchFromVar,
                            searchValue,
                            statusCode,
                            logValues,
                            maxLogMessageLength,
                            title,
                            cause,
                            jsonBody,
                            isCaseSensitive,
                            isFullMatch,
                            isSearchBackwards,
                            isReplaceAll);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyScreeningAction other = (ProxyScreeningAction) obj;
        return action == other.action && Objects.equals(appendFromVar, other.appendFromVar) && Objects.equals(appendValue, other.appendValue)
               && Objects.equals(detail, other.detail) && format == other.format && Objects.equals(goToCase, other.goToCase)
               && Objects.equals(headerName, other.headerName) && Objects.equals(headerValue, other.headerValue) && ifExists == other.ifExists
               && logLevel == other.logLevel && Objects.equals(name, other.name) && Objects.equals(prependFromVar, other.prependFromVar)
               && Objects.equals(prependValue, other.prependValue) && Objects.equals(replaceFromVar, other.replaceFromVar)
               && Objects.equals(isCaseSensitive, other.isCaseSensitive) && Objects.equals(isFullMatch, other.isFullMatch)
               && Objects.equals(isSearchBackwards, other.isSearchBackwards) && Objects.equals(isReplaceAll, other.isReplaceAll)
               && Objects.equals(searchValue, other.searchValue) && Objects.equals(searchFromVar, other.searchFromVar)
               && Objects.equals(replaceValue, other.replaceValue) && Objects.equals(statusCode, other.statusCode) && Objects.equals(logValues, other.logValues)
               && Objects.equals(maxLogMessageLength, other.maxLogMessageLength) && Objects.equals(title, other.title) && Objects.equals(cause, other.cause)
               && Objects.equals(jsonBody, other.jsonBody);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ericsson.sc.proxyal.proxyconfig.ProxyBuilder#build(java.lang.Object[])
     */
    public Action.Builder initBuilder()
    {
        var saBuilder = Action.newBuilder();
        switch (this.action)
        {
            case ActionAddHeader:
            {
                var actionBuilder = AddHeaderAction.newBuilder()
                                                   .setName(this.getHeaderName())
                                                   .setValue(VarHeaderConstValue.newBuilder().setTermString(this.getHeaderValue().orElse("")))
                                                   .setIfExists(this.getIfExists());

                saBuilder.setActionAddHeader(actionBuilder);
                break;
            }
            case ActionRemoveHeader:
            {
                var actionBuilder = RemoveHeaderAction.newBuilder().setName(this.getHeaderName());
                saBuilder.setActionRemoveHeader(actionBuilder);
                break;
            }
            case ActionModifyHeader:
            {

                var actionBuilder = ModifyHeaderAction.newBuilder().setName(this.getHeaderName());

                var stringModifierList = StringModifierList.newBuilder();

                /**
                 * String modifier is meant to look into the value and modify the value. That's
                 * why actions appendValue, prependValue, setTableLookup and
                 * setScramblingProfile are configured under the StringModifier. On the other
                 * hand, action replaceValue is meant to replace directly the value without
                 * looking into it, that's why it is set directly under the Action
                 */
                var actionStringModifier = StringModifier.newBuilder();

                this.getReplaceValue().ifPresent(v -> actionBuilder.setReplaceValue(VarHeaderConstValue.newBuilder().setTermString(v)));
                this.getReplaceFromVar().ifPresent(v -> actionBuilder.setReplaceValue(VarHeaderConstValue.newBuilder().setTermVar(v)));
                this.getPrependValue().ifPresent(v -> actionStringModifier.setPrepend(VarHeaderConstValue.newBuilder().setTermString(v)));
                this.getPrependFromVar().ifPresent(v -> actionStringModifier.setPrepend(VarHeaderConstValue.newBuilder().setTermVar(v)));
                this.getAppendValue().ifPresent(v -> actionStringModifier.setAppend(VarHeaderConstValue.newBuilder().setTermString(v)));
                this.getAppendFromVar().ifPresent(v -> actionStringModifier.setAppend(VarHeaderConstValue.newBuilder().setTermVar(v)));
                this.getDynKvTable().ifPresent(v ->
                {
                    var kvTable = DynamicKvtLookup.newBuilder().setLookupTableName(v.getName());
                    if (v.doNothing())
                    {
                        kvTable.setDoNothing(true);
                    }
                    else
                    {
                        kvTable.setFcUnsuccessfulOperation(v.getFcUnsuccessfulOp());
                    }
                    actionStringModifier.setTableLookup(kvTable);
                });
                this.getScrambleData().ifPresent(v ->
                {
                    var scramProf = ScramblingProfile.newBuilder();
                    if (v.doNothing())
                    {
                        scramProf.setDoNothing(true);
                    }
                    else
                    {
                        scramProf.setFcUnsuccessfulOperation(v.getOnFqdnUnsuccessfullAction());
                    }
                    actionStringModifier.setScramblingProfile(scramProf);
                });

                stringModifierList.addStringModifiers(actionStringModifier);
                actionBuilder.setUseStringModifiers(stringModifierList);
                saBuilder.setActionModifyHeader(actionBuilder);

                break;
            }
            case ActionRejectMessage:
            {
                var actionBuilder = RejectMessageAction.newBuilder();
                this.getStatusCode().ifPresent(actionBuilder::setStatus);
                this.getTitle().ifPresent(actionBuilder::setTitle);
                this.getCause().ifPresent(actionBuilder::setCause);
                this.getDetail().ifPresent(actionBuilder::setDetail);
                actionBuilder.setMessageFormat(this.getFormat());

                saBuilder.setActionRejectMessage(actionBuilder.build());
                break;
            }
            case ActionModifyStatusCode:
            {
                var actionBuilder = ModifyStatusCodeAction.newBuilder();
                this.getStatusCode().ifPresent(actionBuilder::setStatus);
                this.getTitle().ifPresent(actionBuilder::setTitle);
                this.getCause().ifPresent(actionBuilder::setCause);
                this.getDetail().ifPresent(actionBuilder::setDetail);
                actionBuilder.setMessageFormat(this.getFormat());

                saBuilder.setActionModifyStatusCode(actionBuilder.build());
                break;
            }
            case ActionDropMessage:
            {
                saBuilder.setActionDropMessage(true);
                break;
            }
            case ActionExitScreeningCase:
            {
                saBuilder.setActionExitFilterCase(true);
                break;
            }
            case ActionGoTo:
            {
                this.getGoToCase().ifPresent(saBuilder::setActionGotoFilterCase);
                break;
            }
            case ActionLog:
            {
                var actionBuilder = LogAction.newBuilder()
                                             .addAllLogValues(logValues.get())
                                             .setLogLevel(this.getLogLevel())
                                             .setMaxLogMessageLength(this.getMaxLogMessageLength().orElseThrow());
                saBuilder.setActionLog(actionBuilder.build());
                break;
            }
            case ActionCreateJsonBody:
            {
                var actionBuilder = CreateBodyAction.newBuilder().setName(this.name);
                actionBuilder.setContentType(JSON_CONTENT_TYPE);
                this.getJsonBody().ifPresent(actionBuilder::setContent);
                saBuilder.setActionCreateBody(actionBuilder.build());
                break;
            }
            case ActionModifyJsonBody:
            {
                var jsonOperationBuilder = buildJsonOperation();

                if (!jsonOperationBuilder.getAllFields().isEmpty())
                {
                    var actionBuilder = ModifyJsonBodyAction.newBuilder().setName(this.getName()).setJsonOperation(jsonOperationBuilder);
                    saBuilder.setActionModifyJsonBody(actionBuilder);
                }
                break;
            }

            case ActionModifyQueryParam:
            {
                var actionBuilder = ModifyQueryParamAction.newBuilder().setKeyName(this.queryName.get());
                var stringModifierList = StringModifierList.newBuilder();
                var actionStringModifier = StringModifier.newBuilder();

                this.getDynKvTable().ifPresent(v ->
                {
                    var kvTable = DynamicKvtLookup.newBuilder().setLookupTableName(v.getName());
                    if (v.doNothing())
                    {
                        kvTable.setDoNothing(true);
                    }
                    else
                    {
                        kvTable.setFcUnsuccessfulOperation(v.getFcUnsuccessfulOp());
                    }
                    actionStringModifier.setTableLookup(kvTable);
                });
                this.getScrambleData().ifPresent(v ->
                {
                    var scramProf = ScramblingProfile.newBuilder();
                    if (v.doNothing())
                    {
                        scramProf.setDoNothing(true);
                    }
                    else
                    {
                        scramProf.setFcUnsuccessfulOperation(v.getOnFqdnUnsuccessfullAction());
                    }
                    actionStringModifier.setScramblingProfile(scramProf);
                });

                stringModifierList.addStringModifiers(actionStringModifier);
                actionBuilder.setUseStringModifiers(stringModifierList);
                saBuilder.setActionModifyQueryParam(actionBuilder);
                break;
            }
            default:
                break;
        }
        return saBuilder;
    }

    private JsonOperation.Builder buildJsonOperation()
    {
        var jsonOperationBuilder = JsonOperation.newBuilder();
        switch (this.getModifyJsonBodyActionType())
        {
            case AddValue:
            {
                var actionBuilder = AddToJson.newBuilder()
                                             .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()))
                                             .setValue(buildString(VarOrValue.Value, this.getBodyVarOrValue()))
                                             .setIfPathNotExists(this.getIfPathNotExists())
                                             .setIfElementExists(this.getIfElementExists());

                jsonOperationBuilder.setAddToJson(actionBuilder);
                break;
            }
            case AddFromVar:
            {
                var actionBuilder = AddToJson.newBuilder()
                                             .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()))
                                             .setValue(buildString(VarOrValue.Var, this.getBodyVarOrValue()))
                                             .setIfPathNotExists(this.getIfPathNotExists())
                                             .setIfElementExists(this.getIfElementExists());

                jsonOperationBuilder.setAddToJson(actionBuilder);
                break;
            }
            case ReplaceValue:
            {
                var actionBuilder = ReplaceInJson.newBuilder()
                                                 .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()))
                                                 .setValue(buildString(VarOrValue.Value, this.getBodyVarOrValue()));

                jsonOperationBuilder.setReplaceInJson(actionBuilder);
                break;
            }
            case ReplaceFromVar:
            {
                var actionBuilder = ReplaceInJson.newBuilder()
                                                 .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()))
                                                 .setValue(buildString(VarOrValue.Var, this.getBodyVarOrValue()));

                jsonOperationBuilder.setReplaceInJson(actionBuilder);
                break;
            }
            case Remove:
            {
                var actionBuilder = RemoveFromJson.newBuilder().setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()));

                jsonOperationBuilder.setRemoveFromJson(actionBuilder);
                break;
            }

            case ReplaceFromMapTable:
            {

                var actionBuilder = ModifyJsonValue.newBuilder().setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()));
                var actionStringModifier = StringModifier.newBuilder();
                this.getDynKvTable().ifPresent(v ->
                {
                    var kvTable = DynamicKvtLookup.newBuilder().setLookupTableName(v.getName());
                    if (v.doNothing())
                    {
                        kvTable.setDoNothing(true);
                    }
                    else
                    {
                        kvTable.setFcUnsuccessfulOperation(v.getFcUnsuccessfulOp());
                    }
                    actionStringModifier.setTableLookup(kvTable);
                });
                actionBuilder.addStringModifiers(actionStringModifier);
                jsonOperationBuilder.setModifyJsonValue(actionBuilder);
                break;

            }
            case ReplaceScrambling:
            {
                /**
                 * setEnableExceptionHandling is set to TRUE intentionally in order to throw an
                 * error in case the FQDN to be scrambled is NOT 3GPP Compliant
                 */
                var actionBuilder = ModifyJsonValue.newBuilder()
                                                   .setEnableExceptionHandling(true)
                                                   .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()));
                var actionStringModifier = StringModifier.newBuilder();
                this.getScrambleData().ifPresent(v ->
                {
                    var scramProf = ScramblingProfile.newBuilder();
                    if (v.doNothing())
                    {
                        scramProf.setDoNothing(true);
                    }
                    else
                    {
                        scramProf.setFcUnsuccessfulOperation(v.getOnFqdnUnsuccessfullAction());
                    }
                    actionStringModifier.setScramblingProfile(scramProf);
                });
                actionBuilder.addStringModifiers(actionStringModifier);
                jsonOperationBuilder.setModifyJsonValue(actionBuilder);
                break;

            }
            case PrependValue:
            {
                var actionBuilder = ModifyJsonValue.newBuilder()
                                                   .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()))
                                                   .addStringModifiers(StringModifier.newBuilder()
                                                                                     .setPrepend(VarHeaderConstValue.newBuilder()
                                                                                                                    .setTermString(this.getBodyVarOrValue()
                                                                                                                                       .orElseThrow())));

                jsonOperationBuilder.setModifyJsonValue(actionBuilder);
                break;
            }

            case PrependFromVar:
            {
                var actionBuilder = ModifyJsonValue.newBuilder()
                                                   .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()))
                                                   .addStringModifiers(StringModifier.newBuilder()
                                                                                     .setPrepend(VarHeaderConstValue.newBuilder()
                                                                                                                    .setTermVar(this.getBodyVarOrValue()
                                                                                                                                    .orElseThrow())));

                jsonOperationBuilder.setModifyJsonValue(actionBuilder);
                break;
            }

            case AppendValue:
            {
                var actionBuilder = ModifyJsonValue.newBuilder()
                                                   .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()))
                                                   .addStringModifiers(StringModifier.newBuilder()
                                                                                     .setAppend(VarHeaderConstValue.newBuilder()
                                                                                                                   .setTermString(this.getBodyVarOrValue()
                                                                                                                                      .orElseThrow())));

                jsonOperationBuilder.setModifyJsonValue(actionBuilder);
                break;
            }

            case AppendFromVar:
            {
                var actionBuilder = ModifyJsonValue.newBuilder()
                                                   .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()))
                                                   .addStringModifiers(StringModifier.newBuilder()
                                                                                     .setAppend(VarHeaderConstValue.newBuilder()
                                                                                                                   .setTermVar(this.getBodyVarOrValue()
                                                                                                                                   .orElseThrow())));

                jsonOperationBuilder.setModifyJsonValue(actionBuilder);
                break;
            }

            case SearchReplaceString:
            {

                var searchValue = this.getSearchValue().isPresent() ? VarHeaderConstValue.newBuilder().setTermString(this.getSearchValue().get())
                                                                    : VarHeaderConstValue.newBuilder().setTermVar(this.getSearchFromVar().get());

                var replaceValue = this.getReplaceValue().isPresent() ? VarHeaderConstValue.newBuilder().setTermString(this.getReplaceValue().get())
                                                                      : VarHeaderConstValue.newBuilder().setTermVar(this.getReplaceFromVar().get());
                var actionBuilder = ModifyJsonValue.newBuilder()
                                                   .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()))
                                                   .addStringModifiers(StringModifier.newBuilder()
                                                                                     .setSearchAndReplace(SearchAndReplace.newBuilder()
                                                                                                                          .setSearchValue(searchValue)
                                                                                                                          .setReplaceValue(replaceValue)
                                                                                                                          .setReplaceOptions(ReplaceOptions.newBuilder()
                                                                                                                                                           .setReplaceAllOccurances(this.isReplaceAll()))
                                                                                                                          .setSearchOptions(SearchOptions.newBuilder()
                                                                                                                                                         .setSearchFromEnd(this.isSearchBackwards())
                                                                                                                                                         .setCaseSensitive(this.isCaseSensitive())
                                                                                                                                                         .setFullMatch(this.isFullMatch()))));

                jsonOperationBuilder.setModifyJsonValue(actionBuilder);
                break;
            }

            case SearchReplaceRegex:
            {

                var searchValue = VarHeaderConstValue.newBuilder().setTermString(this.getSearchValue().get());

                var replaceValue = this.getReplaceValue().isPresent() ? VarHeaderConstValue.newBuilder().setTermString(this.getReplaceValue().get())
                                                                      : VarHeaderConstValue.newBuilder().setTermVar(this.getReplaceFromVar().get());
                var actionBuilder = ModifyJsonValue.newBuilder()
                                                   .setJsonPointer(buildString(VarOrValue.Value, this.getJsonPointer()))
                                                   .addStringModifiers(StringModifier.newBuilder()
                                                                                     .setSearchAndReplace(SearchAndReplace.newBuilder()
                                                                                                                          .setSearchValue(searchValue)
                                                                                                                          .setReplaceValue(replaceValue)
                                                                                                                          .setReplaceOptions(ReplaceOptions.newBuilder()
                                                                                                                                                           .setReplaceAllOccurances(this.isReplaceAll()))
                                                                                                                          .setSearchOptions(SearchOptions.newBuilder()
                                                                                                                                                         .setRegexSearch(true))));

                jsonOperationBuilder.setModifyJsonValue(actionBuilder);
                break;
            }

            default:
                break;
        }
        return jsonOperationBuilder;
    }

    private VarOrString.Builder buildString(VarOrValue option,
                                            Optional<String> value)
    {
        return option.equals(VarOrValue.Value) ? VarOrString.newBuilder().setTermString(value.orElseThrow())
                                               : VarOrString.newBuilder().setTermVar(value.orElseThrow());
    }

    public void setDynKvTable(Optional<DynKVTable> dynKvTable)
    {
        this.dynKvTable = dynKvTable;
    }

    /**
     * @return the isFullMatch
     */
    public boolean isFullMatch()
    {
        return isFullMatch;
    }

    /**
     * @param isFullMatch the isFullMatch to set
     */
    public ProxyScreeningAction setFullMatch(boolean isFullMatch)
    {
        this.isFullMatch = isFullMatch;
        return this;
    }

    /**
     * @return the isSearchBackwards
     */
    public boolean isSearchBackwards()
    {
        return isSearchBackwards;
    }

    /**
     * @param isSearchBackwards the isSearchBackwards to set
     */
    public ProxyScreeningAction setSearchBackwards(boolean isSearchBackwards)
    {
        this.isSearchBackwards = isSearchBackwards;
        return this;
    }

    /**
     * @return the isReplaceAll
     */
    public boolean isReplaceAll()
    {
        return isReplaceAll;
    }

    /**
     * @param isReplaceAll the isReplaceAll to set
     */
    public ProxyScreeningAction setReplaceAll(boolean isReplaceAll)
    {
        this.isReplaceAll = isReplaceAll;
        return this;
    }
}
