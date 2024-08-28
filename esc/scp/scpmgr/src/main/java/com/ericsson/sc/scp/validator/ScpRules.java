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
 * Created on: Nov 20, 2020
 *     Author: echaias
 */

package com.ericsson.sc.scp.validator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.ericsson.adpal.cm.validator.RuleResult;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.expressionparser.ConditionParser;
import com.ericsson.sc.expressionparser.ConditionParserValidator;
import com.ericsson.sc.expressionparser.NfConditionParserValidator;
import com.ericsson.sc.expressionparser.ScpConditionParserValidator;
import com.ericsson.sc.expressionparser.SeppConditionParserValidator;
import com.ericsson.sc.scp.config.ConfigUtils;
import com.ericsson.sc.scp.model.EricssonScp;
import com.ericsson.sc.scp.model.MessageDatum;
import com.ericsson.sc.scp.model.NfInstance;
import com.ericsson.sc.scp.model.NfPeerInfo.OutMessageHandling;
import com.ericsson.sc.scp.model.OwnNetwork;
import com.ericsson.sc.scp.model.PriorityGroup;
import com.ericsson.sc.scp.model.ResponseScreeningCase;
import com.ericsson.sc.validator.Rule;
import com.ericsson.sc.validator.RuleSupplier;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.ParseException;
import com.ericsson.utilities.graphs.SingleDfsLoopDetector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.util.RegexECMA262Helper;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;

import io.kubernetes.client.openapi.models.V1Service;
import io.reactivex.Single;

/**
 * Enum that keeps the Sepp validation {@link Rule}
 */

public class ScpRules implements RuleSupplier<EricssonScp>
{
    private final List<Rule<EricssonScp>> rulesList;
    private final List<V1Service> k8sServiceList;
    private static final String RULE_NOT_APPLICABLE = "Rule is not applicable";
    private static final String DISC_HEADER_PART_REGEX = "3gpp-sbi-discovery-.*";

    private static final int CONDITION_EXPRESSIONS_MAX = 100;

    ScpRules(final List<V1Service> k8sServiceList)
    {
        this.k8sServiceList = k8sServiceList;
        this.rulesList = List.of(
                                 /*
                                  * Rule_1 validates that the extractor regular expressions defined in message
                                  * data of the SEPP configuration are google RE2 compliant
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getMessageData()//
                                                                                .forEach(rData ->
                                                                                {
                                                                                    if (rData.getExtractorRegex() != null
                                                                                        && !rData.getExtractorRegex().isEmpty())
                                                                                    {
                                                                                        isApplicable.set(true);

                                                                                        try
                                                                                        {
                                                                                            Pattern.compile(rData.getExtractorRegex());
                                                                                        }
                                                                                        catch (PatternSyntaxException e)
                                                                                        {
                                                                                            ruleResult.set(false);
                                                                                            errMsg.append(String.format("'extractor-regex' : %s defined in 'message-data' : %s is not a valid regular expression%n",
                                                                                                                        rData.getExtractorRegex(),
                                                                                                                        rData.getName()));
                                                                                        }
                                                                                    }
                                                                                }));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_2 validates that the conditions defined in routing case of the SCP
                                  * configuration comply to the condition grammar, according to
                                  * {@link ConditionParserValidator}, and that their recursion depth does not
                                  * exceed 100 (bugfix dnd-34424)
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getRoutingCase() //
                                                                                .forEach(rCase -> rCase.getRoutingRule().forEach(rRule ->
                                                                                {
                                                                                    var predExpr = rRule.getCondition();
                                                                                    // condition is mandatory in yang and empty string is evaluated to
                                                                                    // true
                                                                                    isApplicable.set(true);

                                                                                    try
                                                                                    {
                                                                                        ConditionParserValidator.validate(predExpr);

                                                                                        Map<FieldDescriptor, Object> allFields = ConditionParser.parse(predExpr)
                                                                                                                                                .construct()
                                                                                                                                                .getAllFields();

                                                                                        var depth = countExpressionsRecursive(allFields) + 1;

                                                                                        if (depth >= CONDITION_EXPRESSIONS_MAX)
                                                                                        {
                                                                                            ruleResult.set(false);
                                                                                            errMsg.append("Condition for routing rule " + rRule.getName()
                                                                                                          + " exceeds max number of expressions\n");
                                                                                        }
                                                                                    }
                                                                                    catch (ParseException e)
                                                                                    {
                                                                                        ruleResult.set(false);
                                                                                        errMsg.append(e.getMessage()
                                                                                                      + " for 'condition' defined in 'routing-rule' : "
                                                                                                      + rRule.getName() + "\n");
                                                                                        errMsg.append(displayIndex(predExpr, e.line, e.charPos));
                                                                                    }
                                                                                })));

                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_3 validates that the nf-match-condition, scp-match-condition and
                                  * sepp-match-condition of {@link com.ericsson.sc.scp.model.PriorityGroup}
                                  * comply to the nf-match-condition, scp-match-condition and
                                  * sepp-match-condition grammar accordingly, based on
                                  * {@link NfConditionParserValidator}, {@link ScpConditionParserValidator},
                                  * {@link SeppConditionParserValidator}
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()

                                               .forEach(nfInstance -> nfInstance.getNfPool()//
                                                                                .forEach(pool -> pool.getPriorityGroup().forEach(subpool ->
                                                                                {
                                                                                    var nfMatchCondition = subpool.getNfMatchCondition();
                                                                                    var scpMatchCondition = subpool.getScpMatchCondition();
                                                                                    var seppMatchCondition = subpool.getSeppMatchCondition();

                                                                                    if (nfMatchCondition != null && !nfMatchCondition.isEmpty())
                                                                                    {
                                                                                        isApplicable.set(true);

                                                                                        try
                                                                                        {
                                                                                            NfConditionParserValidator.validate(nfMatchCondition);
                                                                                        }
                                                                                        catch (ParseException e)
                                                                                        {
                                                                                            ruleResult.set(false);
                                                                                            errMsg.append(e.getMessage()
                                                                                                          + " for 'nf-match-condition' defined in 'priority-group' : "
                                                                                                          + subpool.getName() + " of 'nf-pool' : "
                                                                                                          + pool.getName() + "\n");
                                                                                            errMsg.append(displayIndex(nfMatchCondition, e.line, e.charPos));
                                                                                        }
                                                                                    }
                                                                                    if (scpMatchCondition != null && !scpMatchCondition.isEmpty())
                                                                                    {
                                                                                        isApplicable.set(true);

                                                                                        try
                                                                                        {
                                                                                            ScpConditionParserValidator.validate(scpMatchCondition);
                                                                                        }
                                                                                        catch (ParseException e)
                                                                                        {
                                                                                            ruleResult.set(false);
                                                                                            errMsg.append(e.getMessage()
                                                                                                          + " for 'scp-match-condition' defined in 'priority-group' : "
                                                                                                          + subpool.getName() + " of 'nf-pool' : "
                                                                                                          + pool.getName() + "\n");
                                                                                            errMsg.append(displayIndex(scpMatchCondition, e.line, e.charPos));
                                                                                        }
                                                                                    }
                                                                                    if (seppMatchCondition != null && !seppMatchCondition.isEmpty())
                                                                                    {
                                                                                        isApplicable.set(true);

                                                                                        try
                                                                                        {
                                                                                            SeppConditionParserValidator.validate(seppMatchCondition);
                                                                                        }
                                                                                        catch (ParseException e)
                                                                                        {
                                                                                            ruleResult.set(false);
                                                                                            errMsg.append(e.getMessage()
                                                                                                          + " for 'sepp-match-condition' defined in 'priority-group' : "
                                                                                                          + subpool.getName() + " of 'nf-pool' : "
                                                                                                          + pool.getName() + "\n");
                                                                                            errMsg.append(displayIndex(seppMatchCondition, e.line, e.charPos));
                                                                                        }
                                                                                    }
                                                                                })));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_4 validates that the body-json-pointer of
                                  * {@link com.ericsson.sc.scp.model.MessageDatum} is a valid body JSON Pointer
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getMessageData()//
                                                                                .forEach(messageData ->
                                                                                {
                                                                                    var bodyJsonPath = messageData.getBodyJsonPointer();

                                                                                    if (bodyJsonPath != null)
                                                                                    {
                                                                                        isApplicable.set(true);

                                                                                        var valid = validateJsonPath(bodyJsonPath);

                                                                                        if (!valid)
                                                                                        {
                                                                                            ruleResult.set(false);
                                                                                            errMsg.append("The 'body-json-pointer' defined in 'message-data' '"
                                                                                                          + messageData.getName()
                                                                                                          + "' isn't a valid body json pointer according to RFC 6901.\n");
                                                                                        }
                                                                                    }
                                                                                }));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_5 validates that the routing-action list contains only one terminal
                                  * action as last action and max one action-slf-lookup
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()
                                               .stream()
                                               .flatMap(nfInst -> nfInst.getRoutingCase().stream())
                                               .flatMap(rc -> rc.getRoutingRule().stream())
                                               .forEach(rr ->
                                               {
                                                   var raList = rr.getRoutingAction();
                                                   boolean foundSlfAction = false;
                                                   boolean foundTerminalAction = false;
                                                   boolean foundNfDiscoveryAction = false;
                                                   isApplicable.set(true);

                                                   if (raList != null && !raList.isEmpty())
                                                   {
                                                       for (var i = 0; i < raList.size(); i++)
                                                       {
                                                           if (ConfigUtils.isSlfRoutingRule(raList.get(i)))
                                                           {
                                                               if (foundTerminalAction)
                                                               {
                                                                   ruleResult.set(false);
                                                                   errMsg.append("The routing-actions defined in 'routing-rule':" + rr.getName()
                                                                                 + " are not valid. If type 'action-slf-lookup' is included in action list,"
                                                                                 + "  it cannot be after a terminal action\n");
                                                               }
                                                               else if (foundSlfAction)
                                                               {
                                                                   ruleResult.set(false);
                                                                   errMsg.append("The routing-actions defined in 'routing-rule':" + rr.getName()
                                                                                 + " are not valid. Only one action of type 'action-slf-lookup' can be"
                                                                                 + " included in action list\n");
                                                               }
                                                               else
                                                               {
                                                                   foundSlfAction = true;
                                                               }
                                                           }
                                                           else if (ConfigUtils.isTerminalAction(raList.get(i)))
                                                           {
                                                               if (foundTerminalAction)
                                                               {
                                                                   ruleResult.set(false);
                                                                   errMsg.append("The routing actions defined in 'routing-rule': " + rr.getName()
                                                                                 + " are not valid. There are more than one terminal actions configured\n");
                                                               }
                                                               else
                                                               {
                                                                   foundTerminalAction = true;
                                                               }
                                                           }
                                                           else if (ConfigUtils.isNfDiscoveryAction(raList.get(i)))
                                                           {
                                                               if (foundTerminalAction)
                                                               {
                                                                   ruleResult.set(false);
                                                                   errMsg.append("The routing actions defined in 'routing-rule': " + rr.getName()
                                                                                 + " are not valid. The 'action-nf-discovery' cannot follow after a terminal action\n");
                                                               }
                                                               else if (foundNfDiscoveryAction)
                                                               {
                                                                   ruleResult.set(false);
                                                                   errMsg.append("The routing actions defined in 'routing-rule': " + rr.getName()
                                                                                 + " are not valid. The 'action-nf-discovery' cannot exist more than once\n");
                                                               }
                                                               else
                                                               {
                                                                   foundNfDiscoveryAction = true;
                                                               }
                                                           }
                                                           else
                                                           {
                                                               if (foundTerminalAction)
                                                               {
                                                                   ruleResult.set(false);
                                                                   errMsg.append("The routing actions defined in 'routing-rule': " + rr.getName()
                                                                                 + " are not valid. No more actions are allowed after a terminal action\n");

                                                               }
                                                           }
                                                       }
                                                   }
                                                   else
                                                   {
                                                       ruleResult.set(false);
                                                       errMsg.append("No routing-actions are defined inside the 'routing-rule': " + rr.getName());
                                                   }
                                               });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_6 validates that the conditions defined in screening case of the SCP
                                  * configuration comply to the condition grammar, according to
                                  * {@link ConditionParserValidator}, and that their recursion depth does not
                                  * exceed 100 (bugfix dnd-34424)
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             nfInstance.getRequestScreeningCase().forEach(sCase ->
                                             {
                                                 sCase.getScreeningRule().forEach(sRule ->
                                                 {
                                                     var predExpr = sRule.getCondition();
                                                     // predicate expression is mandatory in yang and empty string is evaluated to
                                                     // true
                                                     isApplicable.set(true);

                                                     try
                                                     {
                                                         ConditionParserValidator.validate(predExpr);

                                                         Map<FieldDescriptor, Object> allFields = ConditionParser.parse(predExpr).construct().getAllFields();

                                                         var depth = countExpressionsRecursive(allFields) + 1;

                                                         if (depth >= CONDITION_EXPRESSIONS_MAX)
                                                         {
                                                             ruleResult.set(false);
                                                             errMsg.append("Condition for request screening rule " + sRule.getName()
                                                                           + " exceeds max number of expressions\n");
                                                         }
                                                     }
                                                     catch (ParseException e)
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append(e.getMessage() + " for 'condition' defined in 'screening-rule' : " + sRule.getName()
                                                                       + "\n");
                                                         errMsg.append(displayIndex(predExpr, e.line, e.charPos));
                                                     }
                                                 });
                                             });
                                         });

                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             nfInstance.getResponseScreeningCase().forEach(sCase ->
                                             {
                                                 sCase.getScreeningRule().forEach(sRule ->
                                                 {
                                                     var predExpr = sRule.getCondition();
                                                     // predicate expression is mandatory in yang and empty string is evaluated to
                                                     // true
                                                     isApplicable.set(true);

                                                     try
                                                     {
                                                         ConditionParserValidator.validate(predExpr);

                                                         Map<FieldDescriptor, Object> allFields = ConditionParser.parse(predExpr).construct().getAllFields();

                                                         var depth = countExpressionsRecursive(allFields) + 1;

                                                         if (depth >= CONDITION_EXPRESSIONS_MAX)
                                                         {
                                                             ruleResult.set(false);
                                                             errMsg.append("Condition for response screening rule " + sRule.getName()
                                                                           + " exceeds max number of expressions\n");
                                                         }
                                                     }
                                                     catch (ParseException e)
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append(e.getMessage() + " for 'condition' defined in 'screening-rule' : " + sRule.getName()
                                                                       + "\n");
                                                         errMsg.append(displayIndex(predExpr, e.line, e.charPos));
                                                     }
                                                 });
                                             });
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_7 validates that the terminal actions defined in screening cases of the
                                  * SCP configuration have no following actions defined
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             if (!nfInstance.getRequestScreeningCase().isEmpty())
                                             {
                                                 isApplicable.set(true);

                                                 nfInstance.getRequestScreeningCase().forEach(sCase ->
                                                 {
                                                     sCase.getScreeningRule().forEach(sRule ->
                                                     {
                                                         for (var i = 0; i < sRule.getScreeningAction().size(); i++)
                                                         {
                                                             if (sRule.getScreeningAction().get(i).getActionExitScreeningCase() != null
                                                                 && i != sRule.getScreeningAction().size() - 1)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("The 'action-exit-screening-case' defined in 'screening-rule' : "
                                                                               + sRule.getName() + " has other actions configured afterwards\n");
                                                             }

                                                             if (sRule.getScreeningAction().get(i).getActionGoTo() != null
                                                                 && i != sRule.getScreeningAction().size() - 1)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("The 'action-go-to' defined in 'screening-rule' : " + sRule.getName()
                                                                               + " has other actions configured afterwards\n");
                                                             }

                                                             if (sRule.getScreeningAction().get(i).getActionRejectMessage() != null
                                                                 && i != sRule.getScreeningAction().size() - 1)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("The 'action-reject-message' defined in 'screening-rule' : " + sRule.getName()
                                                                               + " has other actions configured afterwards\n");
                                                             }

                                                             if (sRule.getScreeningAction().get(i).getActionDropMessage() != null
                                                                 && i != sRule.getScreeningAction().size() - 1)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("The 'action-drop-message' defined in 'screening-rule' : " + sRule.getName()
                                                                               + " has other actions configured afterwards\n");
                                                             }
                                                         }
                                                     });
                                                 });
                                             }
                                         });

                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             if (!nfInstance.getResponseScreeningCase().isEmpty())
                                             {
                                                 isApplicable.set(true);

                                                 nfInstance.getResponseScreeningCase().forEach(sCase ->
                                                 {
                                                     sCase.getScreeningRule().forEach(sRule ->
                                                     {
                                                         for (var i = 0; i < sRule.getScreeningAction().size(); i++)
                                                         {
                                                             if (sRule.getScreeningAction().get(i).getActionExitScreeningCase() != null
                                                                 && i != sRule.getScreeningAction().size() - 1)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("The 'action-exit-screening-case' defined in 'screening-rule' : "
                                                                               + sRule.getName() + " has other actions configured afterwards\n");
                                                             }

                                                             if (sRule.getScreeningAction().get(i).getActionGoTo() != null
                                                                 && i != sRule.getScreeningAction().size() - 1)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("The 'action-go-to' defined in 'screening-rule' : " + sRule.getName()
                                                                               + " has other actions configured afterwards\n");
                                                             }

                                                             if (sRule.getScreeningAction().get(i).getActionModifyStatusCode() != null
                                                                 && i != sRule.getScreeningAction().size() - 1)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("The 'action-modify-status-code' defined in 'screening-rule' : "
                                                                               + sRule.getName() + " has other actions configured afterwards\n");
                                                             }
                                                         }
                                                     });
                                                 });
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                 },
                                 /**
                                  * Rule_8 triggers the loop detection mechanism and checks for potential loops
                                  */
                                 new Rule<EricssonScp>()
                                 {
                                     private int count = 0;
                                     private List<List<Integer>> casesConnections = new ArrayList<>();
                                     private List<Integer> caseEdges = new ArrayList<>();
                                     private Map<String, Integer> mapCaseToInteger = new HashMap<>();

                                     @Override
                                     public Single<RuleResult> apply(EricssonScp config)
                                     {
                                         var isApplicable = new AtomicBoolean(false);
                                         var ruleResult = new AtomicBoolean(true);
                                         var errMsg = new StringBuilder(100);

                                         if (config != null && config.getEricssonScpScpFunction() != null)
                                         {
                                             config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                             {
                                                 if (!nfInstance.getRequestScreeningCase().isEmpty())
                                                 {
                                                     isApplicable.set(true);

                                                     nfInstance.getRequestScreeningCase().forEach(sCase ->
                                                     {
                                                         sCase.getScreeningRule().forEach(sRule ->
                                                         {
                                                             for (var i = 0; i < sRule.getScreeningAction().size(); i++)
                                                             {
                                                                 if (sRule.getScreeningAction().get(i).getActionGoTo() != null)
                                                                 {
                                                                     // from vertex
                                                                     if (!this.mapCaseToInteger.containsKey(sCase.getName()))
                                                                     {
                                                                         this.mapCaseToInteger.put(sCase.getName(), this.count);
                                                                         this.count++;
                                                                     }

                                                                     this.caseEdges.add(this.mapCaseToInteger.get(sCase.getName()));
                                                                     // to vertex
                                                                     String goToCase = sRule.getScreeningAction()
                                                                                            .get(i)
                                                                                            .getActionGoTo()
                                                                                            .getRequestScreeningCaseRef();

                                                                     if (!this.mapCaseToInteger.containsKey(goToCase))
                                                                     {
                                                                         this.mapCaseToInteger.put(goToCase, this.count);
                                                                         this.count++;
                                                                     }

                                                                     this.caseEdges.add(this.mapCaseToInteger.get(goToCase));
                                                                     this.casesConnections.add(this.caseEdges);
                                                                     this.caseEdges = new ArrayList<>();
                                                                 }
                                                             }
                                                         });
                                                     });
                                                 }
                                             });

                                             if (!this.casesConnections.isEmpty())
                                             {
                                                 var graph = new SingleDfsLoopDetector(this.mapCaseToInteger.size());

                                                 for (List<Integer> list : this.casesConnections)
                                                 {
                                                     if (list.get(0).equals(list.get(1)))
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Loop Detected among request screening cases.\n");
                                                         break;
                                                     }
                                                     else
                                                     {
                                                         graph.addEdge(list.get(0), list.get(1));
                                                     }
                                                 }

                                                 if (ruleResult.get())
                                                 {
                                                     this.casesConnections = graph.dfsTraversal();

                                                     for (List<Integer> list : this.casesConnections)
                                                     {
                                                         if (list.size() > 1)
                                                         {
                                                             ruleResult.set(false);
                                                             errMsg.append("Loop Detected among request screening cases.\n");
                                                             break;
                                                         }
                                                     }
                                                 }
                                             }

                                             this.casesConnections = new ArrayList<>();
                                             this.mapCaseToInteger = new HashMap<>();
                                             this.caseEdges = new ArrayList<>();
                                             this.count = 0;

                                             if (ruleResult.get())
                                             {
                                                 config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                                 {
                                                     if (!nfInstance.getResponseScreeningCase().isEmpty())
                                                     {
                                                         isApplicable.set(true);

                                                         nfInstance.getResponseScreeningCase().forEach(sCase ->
                                                         {
                                                             sCase.getScreeningRule().forEach(sRule ->
                                                             {
                                                                 for (var i = 0; i < sRule.getScreeningAction().size(); i++)
                                                                 {
                                                                     if (sRule.getScreeningAction().get(i).getActionGoTo() != null)
                                                                     {
                                                                         // from vertex
                                                                         if (!this.mapCaseToInteger.containsKey(sCase.getName()))
                                                                         {
                                                                             this.mapCaseToInteger.put(sCase.getName(), count);
                                                                             this.count++;
                                                                         }

                                                                         this.caseEdges.add(mapCaseToInteger.get(sCase.getName()));
                                                                         // to vertex
                                                                         String goToCase = sRule.getScreeningAction()
                                                                                                .get(i)
                                                                                                .getActionGoTo()
                                                                                                .getResponseScreeningCaseRef();

                                                                         if (!this.mapCaseToInteger.containsKey(goToCase))
                                                                         {
                                                                             this.mapCaseToInteger.put(goToCase, this.count);
                                                                             this.count++;
                                                                         }

                                                                         this.caseEdges.add(mapCaseToInteger.get(goToCase));
                                                                         this.casesConnections.add(this.caseEdges);
                                                                         this.caseEdges = new ArrayList<>();
                                                                     }
                                                                 }
                                                             });
                                                         });
                                                     }
                                                 });

                                                 if (!this.casesConnections.isEmpty())
                                                 {
                                                     var graph = new SingleDfsLoopDetector(this.mapCaseToInteger.size());

                                                     for (List<Integer> list : this.casesConnections)
                                                     {
                                                         if (list.get(0).equals(list.get(1)))
                                                         {
                                                             ruleResult.set(false);
                                                             errMsg.append("Loop Detected among response screening cases.\n");
                                                             break;
                                                         }
                                                         else
                                                         {
                                                             graph.addEdge(list.get(0), list.get(1));
                                                         }
                                                     }

                                                     if (ruleResult.get())
                                                     {
                                                         this.casesConnections = graph.dfsTraversal();

                                                         for (List<Integer> list : this.casesConnections)
                                                         {
                                                             if (list.size() > 1)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("Loop Detected among response screening cases.\n");
                                                                 break;
                                                             }
                                                         }
                                                     }
                                                 }

                                                 this.casesConnections = new ArrayList<>();
                                                 this.mapCaseToInteger = new HashMap<>();
                                                 this.caseEdges = new ArrayList<>();
                                                 this.count = 0;
                                             }

                                         }

                                         if (isApplicable.get())
                                             return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                         return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                     }
                                 },
                                 /**
                                  * Rule_9 validates that there is not request screening case with same name as a
                                  * response screening case
                                  */
                                 new Rule<EricssonScp>()
                                 {
                                     private List<String> namesList = new ArrayList<>();

                                     @Override
                                     public Single<RuleResult> apply(EricssonScp config)
                                     {
                                         var isApplicable = new AtomicBoolean(false);
                                         var ruleResult = new AtomicBoolean(true);
                                         var errMsg = new StringBuilder(100);

                                         if (config != null && config.getEricssonScpScpFunction() != null)
                                         {
                                             config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                             {
                                                 if (!nfInstance.getRequestScreeningCase().isEmpty() && !nfInstance.getResponseScreeningCase().isEmpty())
                                                 {
                                                     isApplicable.set(true);

                                                     nfInstance.getRequestScreeningCase().forEach(sCase -> namesList.add(sCase.getName()));

                                                     for (ResponseScreeningCase sc : nfInstance.getResponseScreeningCase())
                                                     {
                                                         if (namesList.contains(sc.getName()))
                                                         {
                                                             ruleResult.set(false);
                                                             errMsg.append("Same screening case name found in both request and response screening cases.\n");
                                                             break;
                                                         }
                                                     }

                                                     this.namesList = new ArrayList<>();
                                                 }
                                             });
                                         }

                                         if (isApplicable.get())
                                             return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                         return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                     }
                                 },
                                 /**
                                  * Rule_10 validates that predicate expressions containing the isinsubnet
                                  * operator have a valid CIDR range string after said operator.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             nfInstance.getRoutingCase().forEach(rCase ->
                                             {
                                                 rCase.getRoutingRule().forEach(rRule ->
                                                 {
                                                     var predExpr = rRule.getCondition();

                                                     if (predExpr.contains("isinsubnet"))
                                                     {
                                                         isApplicable.set(true);

                                                         java.util.regex.Pattern pat = java.util.regex.Pattern.compile("isinsubnet[ \\(]*'([^']+)");
                                                         var matcher = pat.matcher(predExpr);

                                                         while (matcher.find())
                                                         {
                                                             var ip = matcher.group(1);

                                                             try
                                                             {
                                                                 // split
                                                                 if (ip.contains("/"))
                                                                 {
                                                                     int index = ip.indexOf('/');
                                                                     var addressPart = ip.substring(0, index);
                                                                     var networkPart = ip.substring(index + 1);

                                                                     var inetAddress = InetAddress.getByName(addressPart);
                                                                     var prefixLength = Integer.parseInt(networkPart);

                                                                     // check if the prefixLength is in the correct bounds
                                                                     if (inetAddress.getHostAddress().contains(":"))
                                                                     {
                                                                         // ipv6 addr
                                                                         if (prefixLength < 0 || prefixLength > 128)
                                                                         {
                                                                             throw new IllegalArgumentException("Prefix length out of bounds");
                                                                         }
                                                                     }
                                                                     else if (prefixLength < 0 || prefixLength > 32) // ipv4
                                                                     {
                                                                         throw new IllegalArgumentException("Prefix length out of bounds");
                                                                     }
                                                                 }
                                                                 else
                                                                 {
                                                                     throw new IllegalArgumentException("A valid CIDR format must contain '/'");
                                                                 }
                                                             }
                                                             catch (UnknownHostException | IllegalArgumentException e)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("The 'condition' defined in 'routing-rule' : " + rRule.getName()
                                                                               + " does not contain a valid IP subnet: " + ip + "\n");

                                                                 if (e instanceof NumberFormatException)
                                                                 {
                                                                     errMsg.append("Expected integer after '/'\n");
                                                                 }
                                                                 else
                                                                 {
                                                                     errMsg.append(e.getMessage() + '\n');
                                                                 }
                                                             }

                                                         }
                                                     }

                                                 });
                                             });
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                 },
                                 /**
                                  * Rule_11 validates that slf-lookup attributes do not refer to the same
                                  * routing-case as the one in which action-slf-lookup is included.
                                  */
                                 config ->
                                 {

                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             nfInstance.getRoutingCase().forEach(rCase ->
                                             {
                                                 rCase.getRoutingRule().forEach(rRule ->
                                                 {
                                                     rRule.getRoutingAction().forEach(rAction ->
                                                     {
                                                         var actionSlfLookup = rAction.getActionSlfLookup();

                                                         if (actionSlfLookup != null)
                                                         {
                                                             isApplicable.set(true);

                                                             var slfLookupRef = actionSlfLookup.getSlfLookupProfileRef();

                                                             for (var slfLookup : nfInstance.getSlfLookupProfile())
                                                             {
                                                                 if (slfLookup.getName().equals(slfLookupRef))
                                                                 {
                                                                     if (slfLookup.getRoutingCaseDestinationUnknown() != null
                                                                         && slfLookup.getRoutingCaseDestinationUnknown().equals(rCase.getName()))
                                                                     {
                                                                         ruleResult.set(false);
                                                                         errMsg.append("'Destination-unknown-routing-case-ref' cannot be the same as the one in which 'action-slf-lookup' is included.\n");
                                                                         break;
                                                                     }

                                                                     if (slfLookup.getRoutingCaseIdentityMissing() != null
                                                                         && slfLookup.getRoutingCaseIdentityMissing().equals(rCase.getName()))
                                                                     {
                                                                         ruleResult.set(false);
                                                                         errMsg.append("'Identity-missing-routing-case-ref' cannot be the same as the one in which 'action-slf-lookup' is included.\n");
                                                                         break;
                                                                     }

                                                                     if (slfLookup.getRoutingCaseIdentityNotFound() != null
                                                                         && slfLookup.getRoutingCaseIdentityNotFound().equals(rCase.getName()))
                                                                     {
                                                                         ruleResult.set(false);
                                                                         errMsg.append("'Identity-not-found-routing-case-ref' cannot be the same as the one in which 'action-slf-lookup' is included.\n");
                                                                         break;
                                                                     }

                                                                     if (slfLookup.getRoutingCaseLookupFailure() != null
                                                                         && slfLookup.getRoutingCaseLookupFailure().equals(rCase.getName()))
                                                                     {
                                                                         ruleResult.set(false);
                                                                         errMsg.append("'Lookup-failure-routing-case-ref' cannot be the same as the one in which 'action-slf-lookup' is included.\n");
                                                                         break;
                                                                     }
                                                                 }
                                                             }
                                                         }
                                                     });
                                                 });
                                             });
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                 },

                                 /**
                                  * Rule_12 validates that no more than one own-network can reference the same
                                  * service-address
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         isApplicable.set(true);

                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             Set<String> serviceAdressRefs = new HashSet<>();

                                             var networks = nfInstance.getOwnNetwork()
                                                                      .stream()
                                                                      .filter(nw -> !serviceAdressRefs.add(nw.getServiceAddressRef()))
                                                                      .collect(Collectors.toSet());

                                             for (var nw : networks)
                                             {

                                                 ruleResult.set(false);
                                                 errMsg.append("'Own-network' : " + nw.getName() + " can't reference the same 'service-address' : "
                                                               + nw.getServiceAddressRef() + " as another configured 'own-network'\n");
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_13 validates that no more than one priority-group configured under the
                                  * same nf-pool can have the same configured priority (DND-26222)
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             nfInstance.getNfPool().forEach(nfPool ->
                                             {
                                                 Set<Integer> priorities = new HashSet<>();
                                                 Set<String> groups = nfPool.getPriorityGroup()
                                                                            .stream()
                                                                            .filter(pg -> !priorities.add(pg.getPriority()))
                                                                            .map(PriorityGroup::getName)
                                                                            .collect(Collectors.toSet());

                                                 // set being not empty signifies that at least one priority group was found
                                                 // first part of the condition is there due to multiple iterations

                                                 if (!isApplicable.get() && !priorities.isEmpty())
                                                 {
                                                     isApplicable.set(true);
                                                 }

                                                 // duplicate priorities found
                                                 if (!groups.isEmpty())
                                                 {
                                                     ruleResult.set(false);
                                                 }

                                                 for (var entry : groups)
                                                 {
                                                     errMsg.append(String.format("'priority-group' : %s defined in 'nf-pool' : %s cannot have the same 'priority' as another 'priority-group' under the same 'nf-pool'%n",
                                                                                 entry,
                                                                                 nfPool.getName()));
                                                 }
                                             });
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                 },
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         // collect all pools
                                         var nfPools = config.getEricssonScpScpFunction()
                                                             .getNfInstance()
                                                             .stream()
                                                             .filter(nfInst -> nfInst.getNfPool() != null)
                                                             .flatMap(nfInst -> nfInst.getNfPool().stream())
                                                             .collect(Collectors.toList());

                                         if (nfPools != null && !nfPools.isEmpty())
                                         {
                                             // get all pools that are included in preferred routing
                                             var targetPools = config.getEricssonScpScpFunction()
                                                                     .getNfInstance()
                                                                     .stream()
                                                                     .flatMap(nfInst -> nfInst.getRoutingCase().stream())
                                                                     .flatMap(rc -> rc.getRoutingRule().stream())
                                                                     .flatMap(rr -> rr.getRoutingAction().stream())
                                                                     .filter(ra -> CommonConfigUtils.isPreferredRoutingRule(ra)
                                                                                   && ra.getActionRoutePreferred().getTargetNfPool().getNfPoolRef() != null)
                                                                     .map(ra -> Utils.getByName(nfPools,
                                                                                                ra.getActionRoutePreferred().getTargetNfPool().getNfPoolRef()))
                                                                     .filter(Objects::nonNull)
                                                                     .collect(Collectors.toList());

                                             // in preferred routing we cannot have static scp on pool level only
                                             targetPools.stream().forEach(p ->
                                             {
                                                 if ((p.getStaticScpInstanceDataRef() != null && !p.getStaticScpInstanceDataRef().isEmpty())
                                                     && (p.getPriorityGroup() == null || p.getPriorityGroup().isEmpty()
                                                         || p.getPriorityGroup()
                                                             .stream()
                                                             .allMatch(pg -> pg.getStaticScpInstanceDataRef() == null
                                                                             || pg.getStaticScpInstanceDataRef().isEmpty())))
                                                 {
                                                     isApplicable.set(true);
                                                     ruleResult.set(false);
                                                     errMsg.append(String.format("'Pool %s cannot have static SCP reference only on pool level (pure indirect routing), since it is used with preferred routing'%n",
                                                                                 p.getName()));
                                                 }
                                             });

                                             // in preferred routing we cannot have static sepp on pool level only
                                             targetPools.stream().forEach(p ->
                                             {
                                                 if ((p.getStaticSeppInstanceDataRef() != null && !p.getStaticSeppInstanceDataRef().isEmpty())
                                                     && (p.getPriorityGroup() == null || p.getPriorityGroup().isEmpty()
                                                         || p.getPriorityGroup()
                                                             .stream()
                                                             .allMatch(pg -> pg.getStaticSeppInstanceDataRef() == null
                                                                             || pg.getStaticSeppInstanceDataRef().isEmpty())))
                                                 {
                                                     isApplicable.set(true);
                                                     ruleResult.set(false);
                                                     errMsg.append(String.format("'Pool %s cannot have static SEPP reference only on pool level (pure indirect routing), since it is used with preferred routing'%n",
                                                                                 p.getName()));
                                                 }
                                             });
                                         }
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_15 validates that the extractor-regex within the message-data attribute
                                  * referenced by the slf-lookup-profile contains a named capture group.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             nfInstance.getSlfLookupProfile().forEach(slfLookup ->
                                             {
                                                 var messageDataName = slfLookup.getMessageDataRef();

                                                 for (var messageData : nfInstance.getMessageData())
                                                 {
                                                     if (messageData.getName().equals(messageDataName) && messageData.getExtractorRegex() != null)
                                                     {
                                                         isApplicable.set(true);

                                                         var pattern = java.util.regex.Pattern.compile("\\?P<(.+)>");
                                                         var matcher = pattern.matcher(messageData.getExtractorRegex());

                                                         if (!matcher.find())
                                                         {
                                                             ruleResult.set(false);
                                                             errMsg.append("The 'extractor-regex' '" + messageData.getExtractorRegex()
                                                                           + "' defined in 'message-data' '" + messageData.getName()
                                                                           + "' must define a named capture group.\n");
                                                             break;
                                                         }
                                                     }
                                                 }
                                             });
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                 },
                                 /**
                                  * This rule validates that the nrf-group that is referenced from
                                  * nrf-service/nf-management/nrf-group-ref has a valid nf-profile-ref defined.
                                  * Otherwise, all of the nrf-group's nrfs must have a valid nf-profile-ref
                                  * defined.
                                  */
                                 config ->
                                 {
                                     if (config == null || config.getEricssonScpScpFunction() == null)
                                         return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                     final var errMsg = new StringBuilder(100);

                                     var result = true;

                                     for (final NfInstance nfInstance : config.getEricssonScpScpFunction().getNfInstance())
                                     {
                                         if (nfInstance.getNrfService() != null && nfInstance.getNrfService().getNfManagement() != null
                                             && !nfInstance.getNrfService().getNfManagement().getNrfGroupRef().isEmpty())
                                         {
                                             for (final String groupName : nfInstance.getNrfService().getNfManagement().getNrfGroupRef())
                                             {
                                                 final var nrfGroup = Utils.getByName(nfInstance.getNrfGroup(), groupName);

                                                 if (nrfGroup != null)
                                                 {
                                                     result = result && nrfGroup.getNfProfileRef() != null && !nrfGroup.getNfProfileRef().isEmpty()
                                                              || nrfGroup.getNrf()
                                                                         .stream()
                                                                         .allMatch(nrf -> nrf.getNfProfileRef() != null && !nrf.getNfProfileRef().isEmpty());

                                                     if (!result)
                                                     {
                                                         errMsg.append(" nrf-group 'scp-function nf-instance ")
                                                               .append(nfInstance.getName())
                                                               .append(" nrf-group ")
                                                               .append(groupName)
                                                               .append("' (referenced from 'scp-function nf-instance ")
                                                               .append(nfInstance.getName())
                                                               .append(" nrf-service nf-management nrf-group-ref')")
                                                               .append(" or all its members must have attribute 'nf-profile-ref' defined.\n");
                                                     }
                                                 }
                                                 else
                                                 {
                                                     result = false;

                                                     errMsg.append(" nrf-group 'scp-function nf-instance ")
                                                           .append(nfInstance.getName())
                                                           .append(" nrf-group ")
                                                           .append(groupName)
                                                           .append("' (referenced from 'scp-function nf-instance ")
                                                           .append(nfInstance.getName())
                                                           .append(" nrf-service nf-management nrf-group-ref')")
                                                           .append(" must have been defined.\n");
                                                 }

                                                 if (!result)
                                                     break;
                                             }

                                             if (!result)
                                                 break;
                                         }
                                     }

                                     return Single.just(new RuleResult(result, errMsg.toString()));
                                 },
                                 /**
                                  * Rule_17 cross checks the ports and tls-ports of the Service addresses along
                                  * with the available Kubernetes ports.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     Set<String> targetPorts = new HashSet<>();

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             isApplicable.compareAndSet(false, true);

                                             // stores all the Service Addresses that are referenced on the networks
                                             Set<String> networksSvcRefs = nfInstance.getOwnNetwork()
                                                                                     .stream()
                                                                                     .map(OwnNetwork::getServiceAddressRef)
                                                                                     .collect(Collectors.toSet());

                                             int[] listenerCount = { 0 };
                                             nfInstance.getServiceAddress().forEach(svcAddr ->
                                             {

                                                 if (networksSvcRefs.contains(svcAddr.getName()))
                                                 {
                                                     if (svcAddr.getPort() != null)
                                                     {
                                                         listenerCount[0]++;
                                                     }

                                                     if (svcAddr.getTlsPort() != null)
                                                     {
                                                         listenerCount[0]++;
                                                     }
                                                     // find the available kubernetes service ports and match them with Service
                                                     // address

                                                     var k8sServiceMatch = this.k8sServiceList.stream().filter(Objects::nonNull).filter(k8sSvc ->
                                                     {
                                                         var lbIngress = k8sSvc.getStatus().getLoadBalancer().getIngress();
                                                         var unencryptedPort = k8sSvc.getSpec()
                                                                                     .getPorts()
                                                                                     .stream()
                                                                                     .filter(port -> port.getName().equals("unencrypted-port"))
                                                                                     .findAny();
                                                         var encryptedPort = k8sSvc.getSpec()
                                                                                   .getPorts()
                                                                                   .stream()
                                                                                   .filter(port -> port.getName().equals("encrypted-port"))
                                                                                   .findAny();

                                                         // External ip checks
                                                         // Accepts only the combination <k8sService>.<externalIp>:<k8sService>.<Port>
                                                         if (lbIngress != null && !lbIngress.isEmpty())
                                                         {
                                                             try
                                                             {
                                                                 InetAddress svcIpv4 = null;
                                                                 InetAddress svcIpv6 = null;
                                                                 if (svcAddr.getIpv4Address() != null && !svcAddr.getIpv4Address().isEmpty())
                                                                 {
                                                                     svcIpv4 = InetAddress.getByName(svcAddr.getIpv4Address());
                                                                 }
                                                                 if (svcAddr.getIpv6Address() != null && !svcAddr.getIpv6Address().isEmpty())
                                                                 {
                                                                     svcIpv6 = InetAddress.getByName(svcAddr.getIpv6Address());
                                                                 }

                                                                 List<InetAddress> externalIPList = new ArrayList<>();
                                                                 for (var ingress : lbIngress)
                                                                 {
                                                                     InetAddress address = InetAddress.getByName(ingress.getIp());
                                                                     externalIPList.add(address);
                                                                 }
                                                                 // check if ip matched
                                                                 boolean isIpMatch = isIpMatched(svcIpv4, svcIpv6, externalIPList);
                                                                 if (isIpMatch)
                                                                 {
                                                                     // Check if the ports associated with the svcAddr match the desired ports for
                                                                     // IPv4 or IPv6

                                                                     if (svcAddr.getPort() != null && svcAddr.getTlsPort() != null)
                                                                     {
                                                                         if (unencryptedPort.isPresent()
                                                                             && Objects.equals(svcAddr.getPort(), unencryptedPort.get().getPort())
                                                                             && encryptedPort.isPresent()
                                                                             && Objects.equals(svcAddr.getTlsPort(), encryptedPort.get().getPort()))
                                                                         {
                                                                             return true;
                                                                         }
                                                                     }
                                                                     else if (svcAddr.getPort() != null)
                                                                     {
                                                                         if (unencryptedPort.isPresent()
                                                                             && Objects.equals(svcAddr.getPort(), unencryptedPort.get().getPort()))
                                                                         {
                                                                             return true;
                                                                         }
                                                                     }
                                                                     // only tls defined
                                                                     else
                                                                     {
                                                                         if (encryptedPort.isPresent()
                                                                             && Objects.equals(svcAddr.getTlsPort(), encryptedPort.get().getPort()))
                                                                         {
                                                                             return true;
                                                                         }
                                                                     }
                                                                     return false;
                                                                 }
                                                             }
                                                             catch (UnknownHostException e)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append(e.getMessage());
                                                                 return false;
                                                             }
                                                         }

                                                         if (k8sSvc.getSpec().getAllocateLoadBalancerNodePorts() == null
                                                             || Objects.equals(k8sSvc.getSpec().getAllocateLoadBalancerNodePorts(), Boolean.TRUE))
                                                         {
                                                             // Internal ip checks
                                                             // There is a limitation to check every internal ip because
                                                             // cluster-ip and node-ip can be also defined. So, the check is only on the
                                                             // internal ports.
                                                             if (svcAddr.getPort() != null && svcAddr.getTlsPort() != null)
                                                             {
                                                                 if (unencryptedPort.isPresent()
                                                                     && Objects.equals(svcAddr.getPort(), unencryptedPort.get().getNodePort())
                                                                     && encryptedPort.isPresent()
                                                                     && Objects.equals(svcAddr.getTlsPort(), encryptedPort.get().getNodePort()))
                                                                 {
                                                                     return true;
                                                                 }
                                                             }
                                                             else if (svcAddr.getPort() != null)
                                                             {
                                                                 if (unencryptedPort.isPresent()
                                                                     && Objects.equals(svcAddr.getPort(), unencryptedPort.get().getNodePort()))
                                                                 {
                                                                     return true;
                                                                 }
                                                             }
                                                             // only tls defined
                                                             else
                                                             {
                                                                 if (encryptedPort.isPresent()
                                                                     && Objects.equals(svcAddr.getTlsPort(), encryptedPort.get().getNodePort()))
                                                                 {
                                                                     return true;
                                                                 }
                                                             }
                                                         }
                                                         return false;
                                                     }).findAny();

                                                     if (k8sServiceMatch.isPresent())
                                                     {
                                                         if (svcAddr.getPort() != null)
                                                         {
                                                             k8sServiceMatch.get()
                                                                            .getSpec()
                                                                            .getPorts()
                                                                            .stream()
                                                                            .filter(port -> (port.getName().equals("unencrypted-port")
                                                                                             && port.getTargetPort() != null))
                                                                            .map(port -> port.getTargetPort().toString())
                                                                            .forEach(targetPorts::add);
                                                         }

                                                         if (svcAddr.getTlsPort() != null)
                                                         {
                                                             k8sServiceMatch.get()
                                                                            .getSpec()
                                                                            .getPorts()
                                                                            .stream()
                                                                            .filter(port -> (port.getName().equals("encrypted-port")
                                                                                             && port.getTargetPort() != null))
                                                                            .map(port -> port.getTargetPort().toString())
                                                                            .forEach(targetPorts::add);
                                                         }
                                                     }
                                                     else
                                                     {
                                                         if (ruleResult.get())
                                                         {
                                                             ruleResult.set(false);
                                                             errMsg.append("Wrong configuration in service-address(es). Please make sure the following apply:\n"
                                                                           + " * 'port' corresponds to an unencrypted port exposed by  the targeted kubernetes SCP worker service\n"
                                                                           + " * 'tls-port' corresponds to an encrypted port exposed by the targeted kubernetes SCP worker service\n"
                                                                           + " * Two service-addresses cannot use the same 'port'/'tls-port' if they correspond to the same kubernetes SCP worker service\n");
                                                         }
                                                     }

                                                 }
                                             });

                                             if (ruleResult.get() && targetPorts.size() != listenerCount[0])
                                             {
                                                 ruleResult.set(false);
                                                 errMsg.append("Wrong configuration in service-address(es). Please make sure the following apply:\n"
                                                               + " * 'port' corresponds to an unencrypted port exposed by  the targeted kubernetes SCP worker service\n"
                                                               + " * 'tls-port' corresponds to an encrypted port exposed by the targeted kubernetes SCP worker service\n"
                                                               + " * Two service-addresses cannot use the same 'port'/'tls-port' if they correspond to the same kubernetes SCP worker service\n");
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_18 validates that the nf-match-condition and scp-match-condition of
                                  * {@link com.ericsson.sc.scp.model.NfPool} comply to the nf-match-condition and
                                  * scp-match-condition grammar, according to {@link NfConditionParserValidator}
                                  * and {@link ScpConditionParserValidator}
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()

                                               .forEach(nfInstance -> nfInstance.getNfPool()//
                                                                                .forEach(pool ->
                                                                                {
                                                                                    var nfMatchCondition = pool.getNfMatchCondition();
                                                                                    var scpMatchCondition = pool.getScpMatchCondition();

                                                                                    if (nfMatchCondition != null && !nfMatchCondition.isEmpty())
                                                                                    {
                                                                                        isApplicable.set(true);

                                                                                        try
                                                                                        {
                                                                                            NfConditionParserValidator.validate(nfMatchCondition);
                                                                                        }
                                                                                        catch (ParseException e)
                                                                                        {
                                                                                            ruleResult.set(false);
                                                                                            errMsg.append(e.getMessage()
                                                                                                          + " for 'nf-match-condition' defined in 'NfPool' : "
                                                                                                          + pool.getName() + "\n");
                                                                                            errMsg.append(displayIndex(nfMatchCondition, e.line, e.charPos));
                                                                                        }
                                                                                    }
                                                                                    if (scpMatchCondition != null && !scpMatchCondition.isEmpty())
                                                                                    {
                                                                                        isApplicable.set(true);

                                                                                        try
                                                                                        {
                                                                                            ScpConditionParserValidator.validate(scpMatchCondition);
                                                                                        }
                                                                                        catch (ParseException e)
                                                                                        {
                                                                                            ruleResult.set(false);
                                                                                            errMsg.append(e.getMessage()
                                                                                                          + " for 'scp-match-condition' defined in 'NfPool' : "
                                                                                                          + pool.getName() + "\n");
                                                                                            errMsg.append(displayIndex(scpMatchCondition, e.line, e.charPos));
                                                                                        }
                                                                                    }
                                                                                }));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_19 validates that the value of a json body action is a valid JSON value.
                                  * Also checks the path to be a valid body json pointer according to RFC 6901
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {

                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()

                                               .forEach(nfInstance ->
                                               {

                                                   nfInstance.getRequestScreeningCase()
                                                             .stream()
                                                             .flatMap(reqCase -> reqCase.getScreeningRule()
                                                                                        .stream()
                                                                                        .flatMap(sRule -> sRule.getScreeningAction().stream()))
                                                             .forEach(sAction ->
                                                             {
                                                                 if (sAction.getActionModifyJsonBody() != null)
                                                                 {
                                                                     isApplicable.set(true);

                                                                     if (!validateJsonPath(sAction.getActionModifyJsonBody().getJsonPointer()))
                                                                     {
                                                                         ruleResult.set(false);
                                                                         errMsg.append("The json-pointer '" + sAction.getActionModifyJsonBody().getJsonPointer()
                                                                                       + "' defined in screening-action " + sAction.getName()
                                                                                       + " isn't a valid body json pointer according to RFC 6901.\n");
                                                                     }
                                                                     else
                                                                     {

                                                                         if (sAction.getActionModifyJsonBody().getAddValue() != null)
                                                                         {
                                                                             if (!isValueValidJson(sAction.getActionModifyJsonBody().getAddValue().getValue()))
                                                                             {
                                                                                 ruleResult.set(false);
                                                                                 errMsg.append("The screening action's " + sAction.getName() + " value '"
                                                                                               + sAction.getActionModifyJsonBody().getAddValue().getValue()
                                                                                               + "' is not a valid JSON value\n");
                                                                             }
                                                                         }

                                                                         if (sAction.getActionModifyJsonBody().getReplaceValue() != null)
                                                                         {
                                                                             if (!isValueValidJson(sAction.getActionModifyJsonBody()
                                                                                                          .getReplaceValue()
                                                                                                          .getValue()))
                                                                             {
                                                                                 ruleResult.set(false);
                                                                                 errMsg.append("The screening-action's " + sAction.getName() + " value '"
                                                                                               + sAction.getActionModifyJsonBody().getReplaceValue().getValue()
                                                                                               + "' is not a valid JSON value\n");
                                                                             }
                                                                         }
                                                                     }
                                                                 }
                                                                 else if (sAction.getActionCreateJsonBody() != null)
                                                                 {
                                                                     isApplicable.set(true);

                                                                     if (sAction.getActionCreateJsonBody().getJsonBody() != null
                                                                         && !isValueValidJson(sAction.getActionCreateJsonBody().getJsonBody()))
                                                                     {
                                                                         ruleResult.set(false);
                                                                         errMsg.append("The screening action's " + sAction.getName() + " JSON body '"
                                                                                       + sAction.getActionCreateJsonBody().getJsonBody()
                                                                                       + "' is not a valid JSON body\n");
                                                                     }
                                                                 }
                                                             });

                                                   nfInstance.getResponseScreeningCase()
                                                             .stream()
                                                             .flatMap(respCase -> respCase.getScreeningRule()
                                                                                          .stream()
                                                                                          .flatMap(sRule -> sRule.getScreeningAction().stream()))
                                                             .forEach(sAction ->
                                                             {
                                                                 if (sAction.getActionModifyJsonBody() != null)
                                                                 {
                                                                     isApplicable.set(true);

                                                                     if (!validateJsonPath(sAction.getActionModifyJsonBody().getJsonPointer()))
                                                                     {
                                                                         ruleResult.set(false);
                                                                         errMsg.append("The json-pointer '" + sAction.getActionModifyJsonBody().getJsonPointer()
                                                                                       + "' defined in screening-action " + sAction.getName()
                                                                                       + " isn't a valid body json pointer according to RFC 6901.\n");
                                                                     }
                                                                     else
                                                                     {

                                                                         if (sAction.getActionModifyJsonBody().getAddValue() != null)
                                                                         {
                                                                             if (!isValueValidJson(sAction.getActionModifyJsonBody().getAddValue().getValue()))
                                                                             {
                                                                                 ruleResult.set(false);
                                                                                 errMsg.append("The screening-action's " + sAction.getName() + " value '"
                                                                                               + sAction.getActionModifyJsonBody().getAddValue().getValue()
                                                                                               + "' is not a valid JSON value\n");
                                                                             }
                                                                         }

                                                                         if (sAction.getActionModifyJsonBody().getReplaceValue() != null)
                                                                         {
                                                                             if (!isValueValidJson(sAction.getActionModifyJsonBody()
                                                                                                          .getReplaceValue()
                                                                                                          .getValue()))
                                                                             {
                                                                                 ruleResult.set(false);
                                                                                 errMsg.append("The screening-action's " + sAction.getName() + " value '"
                                                                                               + sAction.getActionModifyJsonBody().getReplaceValue().getValue()
                                                                                               + "' is not a valid JSON value\n");
                                                                             }
                                                                         }
                                                                     }
                                                                 }
                                                                 else if (sAction.getActionCreateJsonBody() != null)
                                                                 {
                                                                     isApplicable.set(true);

                                                                     if (sAction.getActionCreateJsonBody().getJsonBody() != null
                                                                         && !isValueValidJson(sAction.getActionCreateJsonBody().getJsonBody()))
                                                                     {
                                                                         ruleResult.set(false);
                                                                         errMsg.append("The screening action's " + sAction.getName() + " JSON body '"
                                                                                       + sAction.getActionCreateJsonBody().getJsonBody()
                                                                                       + "' is not a valid JSON body\n");
                                                                     }
                                                                 }
                                                             });
                                               });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_20 validates that the value of a variable inside the add-from-var-name
                                  * replace-from-var-name, append-from-var-name, prepend-from-var-namem and
                                  * search-replace with variables of action-modify-json-body is a value that
                                  * exists in the message-data-ref
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {

                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()

                                               .forEach(nfInstance ->
                                               {

                                                   nfInstance.getRequestScreeningCase().stream().forEach(reqCase ->
                                                   {

                                                       List<String> referencedVariables = getReferencedMsgDataVariables(reqCase.getMessageDataRef(),
                                                                                                                        nfInstance.getMessageData());

                                                       reqCase.getScreeningRule()
                                                              .stream()
                                                              .flatMap(sRule -> sRule.getScreeningAction().stream())
                                                              .forEach(sAction ->
                                                              {
                                                                  if (sAction.getActionModifyJsonBody() != null)
                                                                  {

                                                                      if (sAction.getActionModifyJsonBody().getAddFromVarName() != null)
                                                                      {
                                                                          isApplicable.set(true);

                                                                          if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                   .getAddFromVarName()
                                                                                                                   .getVariable()))
                                                                          {
                                                                              ruleResult.set(false);
                                                                              errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                            + sAction.getActionModifyJsonBody()
                                                                                                     .getAddFromVarName()
                                                                                                     .getVariable()
                                                                                            + "' is not referenced in the request-screening-case "
                                                                                            + reqCase.getName() + "\n");
                                                                          }
                                                                      }

                                                                      else if (sAction.getActionModifyJsonBody().getReplaceFromVarName() != null)
                                                                      {
                                                                          isApplicable.set(true);

                                                                          if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                   .getReplaceFromVarName()
                                                                                                                   .getVariable()))
                                                                          {
                                                                              ruleResult.set(false);
                                                                              errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                            + sAction.getActionModifyJsonBody()
                                                                                                     .getReplaceFromVarName()
                                                                                                     .getVariable()
                                                                                            + "' is not referenced in the request-screening-case "
                                                                                            + reqCase.getName() + "\n");
                                                                          }
                                                                      }
                                                                      else if (sAction.getActionModifyJsonBody().getAppendFromVarName() != null)
                                                                      {
                                                                          isApplicable.set(true);

                                                                          if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                   .getAppendFromVarName()
                                                                                                                   .getVariable()))
                                                                          {
                                                                              ruleResult.set(false);
                                                                              errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                            + sAction.getActionModifyJsonBody()
                                                                                                     .getAppendFromVarName()
                                                                                                     .getVariable()
                                                                                            + "' is not referenced in the response-screening-case "
                                                                                            + reqCase.getName() + "\n");
                                                                          }
                                                                      }
                                                                      else if (sAction.getActionModifyJsonBody().getPrependFromVarName() != null)
                                                                      {
                                                                          isApplicable.set(true);

                                                                          if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                   .getPrependFromVarName()
                                                                                                                   .getVariable()))
                                                                          {
                                                                              ruleResult.set(false);
                                                                              errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                            + sAction.getActionModifyJsonBody()
                                                                                                     .getPrependFromVarName()
                                                                                                     .getVariable()
                                                                                            + "' is not referenced in the response-screening-case "
                                                                                            + reqCase.getName() + "\n");
                                                                          }
                                                                      }
                                                                      else if (sAction.getActionModifyJsonBody().getSearchReplaceString() != null
                                                                               && sAction.getActionModifyJsonBody()
                                                                                         .getSearchReplaceString()
                                                                                         .getSearchFromVarName() != null)
                                                                      {
                                                                          isApplicable.set(true);

                                                                          if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                   .getSearchReplaceString()
                                                                                                                   .getSearchFromVarName()))
                                                                          {
                                                                              ruleResult.set(false);
                                                                              errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                            + sAction.getActionModifyJsonBody()
                                                                                                     .getSearchReplaceString()
                                                                                                     .getSearchFromVarName()
                                                                                            + "' is not referenced in the response-screening-case "
                                                                                            + reqCase.getName() + "\n");
                                                                          }
                                                                      }
                                                                      else if (sAction.getActionModifyJsonBody().getSearchReplaceString() != null
                                                                               && sAction.getActionModifyJsonBody()
                                                                                         .getSearchReplaceString()
                                                                                         .getReplaceFromVarName() != null)
                                                                      {
                                                                          isApplicable.set(true);

                                                                          if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                   .getSearchReplaceString()
                                                                                                                   .getReplaceFromVarName()))
                                                                          {
                                                                              ruleResult.set(false);
                                                                              errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                            + sAction.getActionModifyJsonBody()
                                                                                                     .getSearchReplaceString()
                                                                                                     .getReplaceFromVarName()
                                                                                            + "' is not referenced in the response-screening-case "
                                                                                            + reqCase.getName() + "\n");
                                                                          }
                                                                      }
                                                                      else if (sAction.getActionModifyJsonBody().getSearchReplaceRegex() != null
                                                                               && sAction.getActionModifyJsonBody()
                                                                                         .getSearchReplaceRegex()
                                                                                         .getReplaceFromVarName() != null)
                                                                      {
                                                                          isApplicable.set(true);

                                                                          if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                   .getSearchReplaceRegex()
                                                                                                                   .getReplaceFromVarName()))
                                                                          {
                                                                              ruleResult.set(false);
                                                                              errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                            + sAction.getActionModifyJsonBody()
                                                                                                     .getSearchReplaceRegex()
                                                                                                     .getReplaceFromVarName()
                                                                                            + "' is not referenced in the response-screening-case "
                                                                                            + reqCase.getName() + "\n");
                                                                          }
                                                                      }
                                                                  }
                                                              });
                                                   });

                                                   nfInstance.getResponseScreeningCase().stream().forEach(respCase ->
                                                   {

                                                       List<String> referencedVariables = getReferencedMsgDataVariables(respCase.getMessageDataRef(),
                                                                                                                        nfInstance.getMessageData());

                                                       respCase.getScreeningRule()
                                                               .stream()
                                                               .flatMap(sRule -> sRule.getScreeningAction().stream())
                                                               .forEach(sAction ->
                                                               {
                                                                   if (sAction.getActionModifyJsonBody() != null)
                                                                   {

                                                                       if (sAction.getActionModifyJsonBody().getAddFromVarName() != null)
                                                                       {
                                                                           isApplicable.set(true);

                                                                           if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                    .getAddFromVarName()
                                                                                                                    .getVariable()))
                                                                           {
                                                                               ruleResult.set(false);
                                                                               errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                             + sAction.getActionModifyJsonBody()
                                                                                                      .getAddFromVarName()
                                                                                                      .getVariable()
                                                                                             + "' is not referenced in the response-screening-case "
                                                                                             + respCase.getName() + "\n");
                                                                           }
                                                                       }

                                                                       else if (sAction.getActionModifyJsonBody().getReplaceFromVarName() != null)
                                                                       {
                                                                           isApplicable.set(true);

                                                                           if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                    .getReplaceFromVarName()
                                                                                                                    .getVariable()))
                                                                           {
                                                                               ruleResult.set(false);
                                                                               errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                             + sAction.getActionModifyJsonBody()
                                                                                                      .getReplaceFromVarName()
                                                                                                      .getVariable()
                                                                                             + "' is not referenced in the response-screening-case "
                                                                                             + respCase.getName() + "\n");
                                                                           }
                                                                       }
                                                                       else if (sAction.getActionModifyJsonBody().getAppendFromVarName() != null)
                                                                       {
                                                                           isApplicable.set(true);

                                                                           if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                    .getAppendFromVarName()
                                                                                                                    .getVariable()))
                                                                           {
                                                                               ruleResult.set(false);
                                                                               errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                             + sAction.getActionModifyJsonBody()
                                                                                                      .getAppendFromVarName()
                                                                                                      .getVariable()
                                                                                             + "' is not referenced in the response-screening-case "
                                                                                             + respCase.getName() + "\n");
                                                                           }
                                                                       }
                                                                       else if (sAction.getActionModifyJsonBody().getPrependFromVarName() != null)
                                                                       {
                                                                           isApplicable.set(true);

                                                                           if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                    .getPrependFromVarName()
                                                                                                                    .getVariable()))
                                                                           {
                                                                               ruleResult.set(false);
                                                                               errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                             + sAction.getActionModifyJsonBody()
                                                                                                      .getPrependFromVarName()
                                                                                                      .getVariable()
                                                                                             + "' is not referenced in the response-screening-case "
                                                                                             + respCase.getName() + "\n");
                                                                           }
                                                                       }
                                                                       else if (sAction.getActionModifyJsonBody().getSearchReplaceString() != null
                                                                                && sAction.getActionModifyJsonBody()
                                                                                          .getSearchReplaceString()
                                                                                          .getSearchFromVarName() != null)
                                                                       {
                                                                           isApplicable.set(true);

                                                                           if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                    .getSearchReplaceString()
                                                                                                                    .getSearchFromVarName()))
                                                                           {
                                                                               ruleResult.set(false);
                                                                               errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                             + sAction.getActionModifyJsonBody()
                                                                                                      .getSearchReplaceString()
                                                                                                      .getSearchFromVarName()
                                                                                             + "' is not referenced in the response-screening-case "
                                                                                             + respCase.getName() + "\n");
                                                                           }
                                                                       }
                                                                       else if (sAction.getActionModifyJsonBody().getSearchReplaceString() != null
                                                                                && sAction.getActionModifyJsonBody()
                                                                                          .getSearchReplaceString()
                                                                                          .getReplaceFromVarName() != null)
                                                                       {
                                                                           isApplicable.set(true);

                                                                           if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                    .getSearchReplaceString()
                                                                                                                    .getReplaceFromVarName()))
                                                                           {
                                                                               ruleResult.set(false);
                                                                               errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                             + sAction.getActionModifyJsonBody()
                                                                                                      .getSearchReplaceString()
                                                                                                      .getReplaceFromVarName()
                                                                                             + "' is not referenced in the response-screening-case "
                                                                                             + respCase.getName() + "\n");
                                                                           }
                                                                       }
                                                                       else if (sAction.getActionModifyJsonBody().getSearchReplaceRegex() != null
                                                                                && sAction.getActionModifyJsonBody()
                                                                                          .getSearchReplaceRegex()
                                                                                          .getReplaceFromVarName() != null)
                                                                       {
                                                                           isApplicable.set(true);

                                                                           if (!referencedVariables.contains(sAction.getActionModifyJsonBody()
                                                                                                                    .getSearchReplaceRegex()
                                                                                                                    .getReplaceFromVarName()))
                                                                           {
                                                                               ruleResult.set(false);
                                                                               errMsg.append("The screening-action's " + sAction.getName() + " variable '"
                                                                                             + sAction.getActionModifyJsonBody()
                                                                                                      .getSearchReplaceRegex()
                                                                                                      .getReplaceFromVarName()
                                                                                             + "' is not referenced in the response-screening-case "
                                                                                             + respCase.getName() + "\n");
                                                                           }
                                                                       }
                                                                   }
                                                               });
                                                   });
                                               });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_21 validates that a own network is referenced once across all the
                                  * ingress elements of vTap configuration
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             if (nfInstance.getVtap() != null && nfInstance.getVtap().getVtapConfiguration() != null
                                                 && nfInstance.getVtap().getVtapConfiguration().getProxy() != null)
                                             {
                                                 var ownNetworkRefs = nfInstance.getVtap()
                                                                                .getVtapConfiguration()
                                                                                .getProxy()
                                                                                .getIngress()
                                                                                .stream()
                                                                                .filter(ingress -> !ingress.getOwnNetworkRef().isEmpty())
                                                                                .flatMap(ingress -> ingress.getOwnNetworkRef().stream())
                                                                                .collect(Collectors.toList());

                                                 if (!ownNetworkRefs.isEmpty())
                                                 {
                                                     isApplicable.set(true);
                                                     ownNetworkRefs.stream()
                                                                   .filter(ref -> Collections.frequency(ownNetworkRefs, ref) > 1)
                                                                   .distinct()
                                                                   .forEach(ref ->
                                                                   {
                                                                       ruleResult.set(false);
                                                                       errMsg.append("Own-network-refernce " + ref
                                                                                     + " appears multiple times across the ingress list");
                                                                   });
                                                 }

                                             }
                                         });

                                     }
                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                 },
                                 /**
                                  * Rule_22 validates that the regular expressions defined in address-domain of
                                  * the scp-info are compliant according to ECMA-262 dialect
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getNfProfile()//
                                                                                .forEach(nfProfile ->
                                                                                {
                                                                                    if (nfProfile.getScpInfo() != null
                                                                                        && nfProfile.getScpInfo().getAddressDomain() != null
                                                                                        && !nfProfile.getScpInfo().getAddressDomain().isEmpty())
                                                                                    {
                                                                                        isApplicable.set(true);

                                                                                        nfProfile.getScpInfo()
                                                                                                 .getAddressDomain()
                                                                                                 .stream()
                                                                                                 .forEach(addressDomain ->
                                                                                                 {
                                                                                                     var validRegex = RegexECMA262Helper.regexIsValid(addressDomain);

                                                                                                     if (!validRegex)
                                                                                                     {
                                                                                                         ruleResult.set(false);
                                                                                                         errMsg.append(String.format("'address-domain' : %s defined in 'nf-profile' : %s is not a valid regular expression according to ECMA-262 dialect %n",
                                                                                                                                     nfProfile.getScpInfo()
                                                                                                                                              .getAddressDomain(),
                                                                                                                                     nfProfile.getName()));
                                                                                                     }
                                                                                                 });
                                                                                    }
                                                                                }));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_23 validates that every request-screening-case doesn't include
                                  * message-data-ref with response-header or screening rules with conditions
                                  * containing resp.header.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInst ->
                                         {
                                             var respData = nfInst.getMessageData()
                                                                  .stream()
                                                                  .filter(md -> md.getResponseHeader() != null)
                                                                  .map(rd -> rd.getName())
                                                                  .collect(Collectors.toList());

                                             if (!nfInst.getRequestScreeningCase().isEmpty())
                                             {
                                                 isApplicable.set(true);

                                                 nfInst.getRequestScreeningCase().stream().forEach(reqCase ->
                                                 {
                                                     var messageDataRef = reqCase.getMessageDataRef();
                                                     var commonData = new ArrayList<String>(messageDataRef);
                                                     commonData.retainAll(respData);

                                                     var respHeaderRules = reqCase.getScreeningRule()
                                                                                  .stream()
                                                                                  .filter(sr -> sr.getCondition().contains("resp.header["))
                                                                                  .map(sr -> sr.getName())
                                                                                  .collect(Collectors.toList());

                                                     if (!commonData.isEmpty())
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Request-screening-case '" + reqCase.getName() + "' includes message-data-ref "
                                                                       + commonData.toString() + " with response-header.\n");
                                                     }
                                                     if (!respHeaderRules.isEmpty())
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Screening-rule '" + respHeaderRules.toString() + "' of request-screening-case '"
                                                                       + reqCase.getName() + "' includes condition containing 'resp.header'.\n");
                                                     }

                                                     reqCase.getScreeningRule()
                                                            .forEach(rule -> rule.getScreeningAction()
                                                                                 .stream()
                                                                                 .filter(action -> action.getActionLog() != null)
                                                                                 .forEach(action ->
                                                                                 {
                                                                                     if (action.getActionLog().getText().contains("{{resp."))
                                                                                     {
                                                                                         ruleResult.set(false);
                                                                                         errMsg.append("Screening-rule '" + rule.getName()
                                                                                                       + "' of request-screening-case '" + reqCase.getName()
                                                                                                       + "' includes log action containing 'resp.header' or 'resp.body'.\n");
                                                                                     }
                                                                                 }));
                                                 });
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_24 validates that every routing-case doesn't include message-data-ref
                                  * with response-header or routing rules with conditions containing resp.header.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInst ->
                                         {
                                             var respData = nfInst.getMessageData()
                                                                  .stream()
                                                                  .filter(md -> md.getResponseHeader() != null)
                                                                  .map(rd -> rd.getName())
                                                                  .collect(Collectors.toList());

                                             if (!nfInst.getRoutingCase().isEmpty())
                                             {
                                                 isApplicable.set(true);

                                                 nfInst.getRoutingCase().stream().forEach(rc ->
                                                 {
                                                     var messageDataRef = rc.getMessageDataRef();
                                                     var commonData = new ArrayList<String>(messageDataRef);
                                                     commonData.retainAll(respData);

                                                     var respHeaderRules = rc.getRoutingRule()
                                                                             .stream()
                                                                             .filter(rr -> rr.getCondition().contains("resp.header["))
                                                                             .map(rr -> rr.getName())
                                                                             .collect(Collectors.toList());

                                                     if (!commonData.isEmpty())
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Routing-case '" + rc.getName() + "' includes message-data-ref " + commonData.toString()
                                                                       + " with response-header.\n");
                                                     }
                                                     if (!respHeaderRules.isEmpty())
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Routing-rule '" + respHeaderRules.toString() + "' of routing-case '" + rc.getName()
                                                                       + "' includes condition containing 'resp.header'.\n");
                                                     }

                                                     rc.getRoutingRule()
                                                       .forEach(rule -> rule.getRoutingAction()
                                                                            .stream()
                                                                            .filter(action -> action.getActionLog() != null)
                                                                            .forEach(action ->
                                                                            {
                                                                                if (action.getActionLog().getText().contains("{{resp."))
                                                                                {
                                                                                    ruleResult.set(false);
                                                                                    errMsg.append("Routing-rule '" + rule.getName() + "' of routing-case '"
                                                                                                  + rc.getName()
                                                                                                  + "' includes log action containing 'resp.header' or 'resp.body'.\n");
                                                                                }
                                                                            }));
                                                 });
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_25 checks that the variable name of any message data starts with a
                                  * letter and contains only letters, digits and/or underscore
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInst ->
                                         {
                                             var regexMessageData = nfInst.getMessageData()
                                                                          .stream()
                                                                          .filter(md -> md.getExtractorRegex() != null)
                                                                          .collect(Collectors.toList());
                                             isApplicable.set(true);

                                             for (MessageDatum md : regexMessageData)
                                             {
                                                 var extractedVars = getExtractorRegexVariables(md.getExtractorRegex());
                                                 for (String varName : extractedVars)
                                                 {
                                                     if (!varName.matches("[a-zA-Z][a-zA-Z0-9_]*"))
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Extracted variable name " + varName + " in message-data " + md.getName()
                                                                       + " must start with a letter and contain only letters, digits and/or underscore.\n");
                                                     }
                                                 }
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_26 checks that the discovery paramaters defined under
                                  * discovery-parameter-to-use and add-discovery-parameter in action nf-discovery
                                  * do not include the header prefix '3gpp-sbi-discovery'
                                  */

                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         var actionNfDiscoveryList = config.getEricssonScpScpFunction()
                                                                           .getNfInstance()
                                                                           .stream()
                                                                           .flatMap(nfInst -> nfInst.getRoutingCase().stream())
                                                                           .flatMap(rc -> rc.getRoutingRule().stream())
                                                                           .flatMap(rr -> rr.getRoutingAction().stream())
                                                                           .filter(ConfigUtils::isNfDiscoveryRoutingRule)
                                                                           .collect(Collectors.toList());

                                         if (actionNfDiscoveryList.isEmpty())
                                         {
                                             return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                         }

                                         isApplicable.set(true);

                                         actionNfDiscoveryList.forEach(actionNfDiscovery ->
                                         {
                                             if (actionNfDiscovery.getActionNfDiscovery() != null
                                                 && actionNfDiscovery.getActionNfDiscovery().getUseDiscoveryParameter() != null
                                                 && actionNfDiscovery.getActionNfDiscovery().getUseDiscoveryParameter().getUseSelected() != null
                                                 && !actionNfDiscovery.getActionNfDiscovery().getUseDiscoveryParameter().getUseSelected().getName().isEmpty())
                                             {
                                                 actionNfDiscovery.getActionNfDiscovery()
                                                                  .getUseDiscoveryParameter()
                                                                  .getUseSelected()
                                                                  .getName()
                                                                  .stream()
                                                                  .forEach(discParam ->
                                                                  {
                                                                      if (discParam.matches(DISC_HEADER_PART_REGEX))
                                                                      {
                                                                          ruleResult.set(false);
                                                                          errMsg.append("The parameter name must be used instead of the header name in "
                                                                                        + discParam + " (consider removing '3gpp-sbi-discovery' part)");
                                                                          return;
                                                                      }
                                                                  });
                                             }

                                             actionNfDiscovery.getActionNfDiscovery().getAddDiscoveryParameter().stream().forEach(addDiscParam ->
                                             {
                                                 if (addDiscParam.getName().matches(DISC_HEADER_PART_REGEX))
                                                 {
                                                     ruleResult.set(false);
                                                     errMsg.append("The parameter name must be used instead of the header name in " + addDiscParam
                                                                   + " (consider removing '3gpp-sbi-discovery' part)");
                                                     return;
                                                 }
                                             });
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_27 checks that parsed variables in message output of action-log in a
                                  * routing and screening cases are valid and placed between double curly braces.
                                  * Also, for var.x variables, check that x is defined in message-data-ref
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInst ->
                                         {
                                             isApplicable.set(true);
                                             nfInst.getRequestScreeningCase().forEach(reqCase ->
                                             {
                                                 reqCase.getScreeningRule()
                                                        .forEach(rule -> rule.getScreeningAction()
                                                                             .stream()
                                                                             .filter(action -> action.getActionLog() != null)
                                                                             .forEach(actionLog ->
                                                                             {
                                                                                 String text = actionLog.getActionLog().getText();
                                                                                 errMsg.append(parseVariablesFromText(text, actionLog.getName()));
                                                                                 if (errMsg.length() > 0)
                                                                                 {
                                                                                     ruleResult.set(false);
                                                                                 }
                                                                             }));

                                             });

                                             nfInst.getResponseScreeningCase().forEach(respCase ->
                                             {
                                                 respCase.getScreeningRule()
                                                         .forEach(rule -> rule.getScreeningAction()
                                                                              .stream()
                                                                              .filter(action -> action.getActionLog() != null)
                                                                              .forEach(actionLog ->
                                                                              {
                                                                                  String text = actionLog.getActionLog().getText();
                                                                                  errMsg.append(parseVariablesFromText(text, actionLog.getName()));
                                                                                  if (errMsg.length() > 0)
                                                                                  {
                                                                                      ruleResult.set(false);
                                                                                  }
                                                                              }));
                                             });

                                             nfInst.getRoutingCase().forEach(routCase ->
                                             {
                                                 routCase.getRoutingRule()
                                                         .forEach(rule -> rule.getRoutingAction()
                                                                              .stream()
                                                                              .filter(action -> action.getActionLog() != null)
                                                                              .forEach(actionLog ->
                                                                              {

                                                                                  String text = actionLog.getActionLog().getText();
                                                                                  errMsg.append(parseVariablesFromText(text, actionLog.getName()));
                                                                                  if (errMsg.length() > 0)
                                                                                  {
                                                                                      ruleResult.set(false);
                                                                                  }
                                                                              }));
                                             });
                                         });
                                     }
                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * 
                                  * Rule_28 validates that the regular expressions defined in
                                  * search-replace-regex search regex in the modify-json-body configuration are
                                  * google RE2 compliant
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {

                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()

                                               .forEach(nfInstance ->
                                               {

                                                   nfInstance.getRequestScreeningCase().stream().forEach(reqCase ->
                                                   {

                                                       reqCase.getScreeningRule()
                                                              .stream()
                                                              .flatMap(sRule -> sRule.getScreeningAction().stream())
                                                              .forEach(sAction ->
                                                              {
                                                                  if (sAction.getActionModifyJsonBody() != null)
                                                                  {

                                                                      if (sAction.getActionModifyJsonBody().getSearchReplaceRegex() != null)
                                                                      {
                                                                          isApplicable.set(true);

                                                                          try
                                                                          {
                                                                              Pattern.compile(sAction.getActionModifyJsonBody()
                                                                                                     .getSearchReplaceRegex()
                                                                                                     .getSearchRegex());
                                                                          }
                                                                          catch (PatternSyntaxException e)
                                                                          {
                                                                              ruleResult.set(false);
                                                                              errMsg.append(String.format("'Regular expression' : %s defined in 'screening action' : %s is not a valid regular expression%n",
                                                                                                          sAction.getActionModifyJsonBody()
                                                                                                                 .getSearchReplaceRegex()
                                                                                                                 .getSearchRegex(),
                                                                                                          sAction.getName()));
                                                                          }

                                                                      }
                                                                  }
                                                              });
                                                   });

                                                   nfInstance.getResponseScreeningCase().stream().forEach(respCase ->
                                                   {
                                                       respCase.getScreeningRule()
                                                               .stream()
                                                               .flatMap(sRule -> sRule.getScreeningAction().stream())
                                                               .forEach(sAction ->
                                                               {
                                                                   if (sAction.getActionModifyJsonBody() != null)
                                                                   {

                                                                       if (sAction.getActionModifyJsonBody().getSearchReplaceRegex() != null)
                                                                       {
                                                                           isApplicable.set(true);

                                                                           try
                                                                           {
                                                                               Pattern.compile(sAction.getActionModifyJsonBody()
                                                                                                      .getSearchReplaceRegex()
                                                                                                      .getSearchRegex());
                                                                           }
                                                                           catch (PatternSyntaxException e)
                                                                           {
                                                                               ruleResult.set(false);
                                                                               errMsg.append(String.format("'Regular expression' : %s defined in 'screening action' : %s is not a valid regular expression%n",
                                                                                                           sAction.getActionModifyJsonBody()
                                                                                                                  .getSearchReplaceRegex()
                                                                                                                  .getSearchRegex(),
                                                                                                           sAction.getName()));
                                                                           }
                                                                       }
                                                                   }
                                                               });
                                                   });
                                               });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_29 validates that if 3gpp-Sbi-NF-Peer-Info header handling is enabled,
                                  * then all the NRFs used for NF discovery should refer to an NF Profile. If
                                  * there is no reference under the NRF, there should be one under NRF group.
                                  * References to NRFs used for NF discovery are searched in the following order:
                                  *
                                  * 1) nf-pool/nf-pool-discovery/nrf-query/nrf-group-ref
                                  *
                                  * 2) nf-pool/nf-pool-discovery/nrf-group-ref
                                  *
                                  * 3) nrf-service/nf-discovery/nrf-group-ref (single NRF group for NF discovery,
                                  * default option before SC1.15)
                                  *
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         config.getEricssonScpScpFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             if (nfInstance.getNfPeerInfo() != null
                                                 && nfInstance.getNfPeerInfo().getOutMessageHandling().equals(OutMessageHandling.ON))
                                             {
                                                 isApplicable.set(true);

                                                 // Support for multiple NRF groups for discovery:
                                                 // First check NRF groups referenced at
                                                 // nf-pool/nf-pool-discovery/nrf-query/nrf-group-ref level
                                                 var nfPools = nfInstance.getNfPool();
                                                 nfPools.stream()
                                                        .filter(Objects::nonNull)
                                                        .flatMap(nfPool -> nfPool.getNfPoolDiscovery().stream())
                                                        .filter(Objects::nonNull)
                                                        .flatMap(nfPoolDiscovery -> nfPoolDiscovery.getNrfQuery().stream())
                                                        .filter(Objects::nonNull)
                                                        .flatMap(nrfQuery -> nrfQuery.getNrfGroupRef().stream())
                                                        .forEach(nrfGroupRef ->
                                                        {
                                                            var nrfGroup = Utils.getByName(nfInstance.getNrfGroup(), nrfGroupRef);

                                                            if (nrfGroup != null)
                                                            {
                                                                var result = nrfGroup.getNfProfileRef() != null && !nrfGroup.getNfProfileRef().isEmpty()
                                                                             || nrfGroup.getNrf()
                                                                                        .stream()
                                                                                        .allMatch(nrf -> nrf.getNfProfileRef() != null
                                                                                                         && !nrf.getNfProfileRef().isEmpty());

                                                                if (!result)
                                                                {
                                                                    errMsg.append(" nrf-group 'scp-function nf-instance ")
                                                                          .append(nfInstance.getName())
                                                                          .append(" nrf-group ")
                                                                          .append(nrfGroup.getName())
                                                                          .append("' referenced from 'scp-function nf-instance ")
                                                                          .append(nfInstance.getName())
                                                                          .append(" nf-pool nf-pool-discovery nrf-query nrf-group-ref'")
                                                                          .append(" or all its members must have attribute 'nf-profile-ref'")
                                                                          .append(", when 3gpp-Sbi-NF-Peer-Info header handling is enabled.\n");

                                                                    ruleResult.set(false);
                                                                }
                                                            }
                                                        });

                                                 // Continue by checking the references at
                                                 // nf-pool/nf-pool-discovery/nrf-group-ref level
                                                 // Filter out nrf-group-refs from the previous step
                                                 nfPools.stream()
                                                        .filter(Objects::nonNull)
                                                        .flatMap(nfPool -> nfPool.getNfPoolDiscovery().stream())
                                                        .filter(Objects::nonNull)
                                                        .filter(nfPoolDiscovery -> nfPoolDiscovery.getNrfQuery() == null
                                                                                   || nfPoolDiscovery.getNrfQuery().isEmpty()
                                                                                   || nfPoolDiscovery.getNrfQuery()
                                                                                                     .stream()
                                                                                                     .anyMatch(nrfQuery -> nrfQuery.getNrfGroupRef() == null
                                                                                                                           || nrfQuery.getNrfGroupRef()
                                                                                                                                      .isEmpty()))
                                                        .flatMap(nfPoolDiscovery -> nfPoolDiscovery.getNrfGroupRef().stream())
                                                        .forEach(nrfGroupRef ->
                                                        {
                                                            var nrfGroup = Utils.getByName(nfInstance.getNrfGroup(), nrfGroupRef);

                                                            if (nrfGroup != null)
                                                            {
                                                                var result = nrfGroup.getNfProfileRef() != null && !nrfGroup.getNfProfileRef().isEmpty()
                                                                             || nrfGroup.getNrf()
                                                                                        .stream()
                                                                                        .allMatch(nrf -> nrf.getNfProfileRef() != null
                                                                                                         && !nrf.getNfProfileRef().isEmpty());

                                                                if (!result)
                                                                {
                                                                    errMsg.append(" nrf-group 'scp-function nf-instance ")
                                                                          .append(nfInstance.getName())
                                                                          .append(" nrf-group ")
                                                                          .append(nrfGroup.getName())
                                                                          .append("' referenced from 'scp-function nf-instance ")
                                                                          .append(nfInstance.getName())
                                                                          .append(" nf-pool nf-pool-discovery nrf-group-ref'")
                                                                          .append(" or all its members must have attribute 'nf-profile-ref'")
                                                                          .append(", when 3gpp-Sbi-NF-Peer-Info header handling is enabled.\n");

                                                                    ruleResult.set(false);
                                                                }
                                                            }
                                                        });

                                                 // Backward compatible rule for single NRF group:
                                                 // Continue by checking nrf-service/nf-discovery/nrf-group-ref
                                                 // if nrf-group-refs are not found at previous steps
                                                 var nfPoolWithoutRef = nfPools.stream()
                                                                               .filter(Objects::nonNull)
                                                                               .anyMatch(nfPool -> (nfPool.getNfPoolDiscovery() == null
                                                                                                    || nfPool.getNfPoolDiscovery().isEmpty()
                                                                                                    || nfPool.getNfPoolDiscovery()
                                                                                                             .stream()
                                                                                                             .anyMatch(nfPoolDiscovery -> (nfPoolDiscovery.getNrfGroupRef()
                                                                                                                                                          .isEmpty()
                                                                                                                                           || nfPoolDiscovery.getNrfGroupRef() == null)))
                                                                                                   && (nfPool.getNfPoolDiscovery()
                                                                                                             .stream()
                                                                                                             .anyMatch(nfPoolDiscovery -> (nfPoolDiscovery.getNrfQuery()
                                                                                                                                                          .isEmpty()
                                                                                                                                           || nfPoolDiscovery.getNrfQuery() == null))
                                                                                                       || nfPool.getNfPoolDiscovery()
                                                                                                                .stream()
                                                                                                                .flatMap(nfPoolDiscovery -> nfPoolDiscovery.getNrfQuery()
                                                                                                                                                           .stream())
                                                                                                                .anyMatch(nrfQuery -> nrfQuery.getNrfGroupRef()
                                                                                                                                              .isEmpty()
                                                                                                                                      || nrfQuery.getNrfGroupRef() == null)));

                                                 if ((nfPoolWithoutRef || nfPools.isEmpty() || nfPools == null) && nfInstance.getNrfService() != null
                                                     && nfInstance.getNrfService().getNfDiscovery() != null)
                                                 {
                                                     final var nrfGroup = Utils.getByName(nfInstance.getNrfGroup(),
                                                                                          nfInstance.getNrfService().getNfDiscovery().getNrfGroupRef());

                                                     if (nrfGroup != null)
                                                     {
                                                         var result = nrfGroup.getNfProfileRef() != null && !nrfGroup.getNfProfileRef().isEmpty()
                                                                      || nrfGroup.getNrf()
                                                                                 .stream()
                                                                                 .allMatch(nrf -> nrf.getNfProfileRef() != null
                                                                                                  && !nrf.getNfProfileRef().isEmpty());

                                                         if (!result)
                                                         {
                                                             errMsg.append(" nrf-group 'scp-function nf-instance ")
                                                                   .append(nfInstance.getName())
                                                                   .append(" nrf-group ")
                                                                   .append(nrfGroup.getName())
                                                                   .append("' referenced from 'scp-function nf-instance ")
                                                                   .append(nfInstance.getName())
                                                                   .append(" nrf-service nf-discovery nrf-group-ref'")
                                                                   .append(" or all its members must have attribute 'nf-profile-ref'")
                                                                   .append(", when 3gpp-Sbi-NF-Peer-Info header handling is enabled.\n");

                                                             ruleResult.set(false);
                                                         }
                                                     }
                                                     else
                                                     {
                                                         errMsg.append(" nrf-group 'scp-function nf-instance ")
                                                               .append(nfInstance.getName())
                                                               .append(" nrf-group' referenced from 'scp-function nf-instance ")
                                                               .append(nfInstance.getName())
                                                               .append(" nrf-service nf-discovery nrf-group-ref'")
                                                               .append(" must have been defined")
                                                               .append(", when 3gpp-Sbi-NF-Peer-Info header handling is enabled.\n");

                                                         ruleResult.set(false);
                                                     }
                                                 }
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_30 validates that the api-prefix per static-nf-service complies with the
                                  * format according to 3gpp standards.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonScpScpFunction() != null)
                                     {
                                         var pchar = "(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@]|%[0-9A-F]{2})";
                                         var regex = "/(?:" + pchar + "+(?:/" + pchar + "*)*)?";

                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()
                                               .stream()
                                               .flatMap(inst -> inst.getStaticNfInstanceData().stream())
                                               .flatMap(instData -> instData.getStaticNfInstance().stream())
                                               .forEach(nfInst ->
                                               {
                                                   nfInst.getStaticNfService().stream().filter(nfServ -> nfServ.getApiPrefix() != null).forEach(nfServ ->
                                                   {
                                                       isApplicable.set(true);

                                                       if (!nfServ.getApiPrefix().matches(regex))
                                                       {
                                                           ruleResult.set(false);
                                                           errMsg.append(String.format("'api-prefix': %s defined in 'static-nf-service': %s of 'static-nf-instance': %s is not in a valid format. API prefix format follows \"path-absolute\" syntax and that starts with a \"/\" reserved character as described in RFC 3986.%n",
                                                                                       nfServ.getApiPrefix(),
                                                                                       nfServ.getName(),
                                                                                       nfInst.getName()));
                                                       }
                                                   });
                                               });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     var GRLEnabled = Boolean.parseBoolean(EnvVars.get("GLOBAL_RATE_LIMIT_ENABLED", false));

                                     if (config != null && config.getEricssonScpScpFunction() != null && GRLEnabled == false)
                                     {
                                         config.getEricssonScpScpFunction()
                                               .getNfInstance()
                                               .stream()
                                               .filter(nfInst -> !nfInst.getGlobalRateLimitProfile().isEmpty() && nfInst.getGlobalRateLimitProfile().size() > 0)
                                               .forEach(nfInst ->
                                               {
                                                   isApplicable.set(true);
                                                   ruleResult.set(false);
                                                   errMsg.append(String.format("Global Rate Limit Profile list must be empty - The Global Rate limit is disabled"));
                                               });

                                     }
                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                 }

        );
    }

    public final List<Rule<EricssonScp>> getRules()
    {
        return this.rulesList;
    }

    private static String displayIndex(String msg,
                                       int line,
                                       int pos)
    {
        StringBuilder mystr = new StringBuilder(100);
        String[] lines = msg.split("\n");
        for (var j = 0; j < lines.length; j++)
        {
            mystr.append("| " + lines[j] + "\n");
            if (j == line - 1)
            {
                // this is our error line
                mystr.append("+");
                for (var i = 0; i < pos + 1; i++)
                {
                    // pos + 1 because we add '| ' at every line
                    mystr.append("-");
                }
                mystr.append("^\n");
            }
        }
        return mystr.toString();
    }

    private static boolean validateJsonPath(String text)
    {
        if (text.isBlank())
        {
            return true;
        }
        if (text.charAt(0) != '/')
        {
            return false;
        }
        if (text.substring(1).isBlank())
        {
            return true;
        }
        String[] subString = text.substring(1).split("/");
        java.util.regex.Pattern letter = java.util.regex.Pattern.compile("[a-zA-Z]");
        java.util.regex.Pattern digit = java.util.regex.Pattern.compile("[0-9]");
        java.util.regex.Pattern special = java.util.regex.Pattern.compile("[!@#$%^&*()_+=|<>?\"{}\\[\\]~-]");
        Matcher hasLetter;
        Matcher hasDigit;
        Matcher hasSpecial;
        for (String s : subString)
        {
            hasLetter = letter.matcher(s);
            hasDigit = digit.matcher(s);
            hasSpecial = special.matcher(s);
            if (!(hasLetter.find() || hasDigit.find() || hasSpecial.find()))
            {
                return false;
            }
        }

        return true;

    }

    private static boolean isValueValidJson(String value)
    {
        var objectMapper = new ObjectMapper();

        try
        {
            @SuppressWarnings("unused")
            var jsonNode = objectMapper.readTree(value);
            return true;
        }
        catch (JsonProcessingException e)
        {
            return false;
        }
    }

    private static List<String> getReferencedMsgDataVariables(List<String> refs,
                                                              List<MessageDatum> msgData)
    {
        List<String> referencedVariables = new ArrayList<>();
        for (String ref : refs)
        {

            for (MessageDatum datum : msgData)
            {

                if (datum.getName().equals(ref))
                {
                    if (datum.getVariableName() != null)
                    {
                        referencedVariables.add(datum.getVariableName());
                    }

                    else if (datum.getExtractorRegex() != null)
                    {
                        referencedVariables.addAll(getExtractorRegexVariables(datum.getExtractorRegex()));
                    }
                }
            }
        }

        return referencedVariables;
    }

    private static List<String> getExtractorRegexVariables(String extractorRegex)
    {
        List<String> definedVariables = new ArrayList<>();

        java.util.regex.Pattern pat = java.util.regex.Pattern.compile("<(.*?)>");
        var matcher = pat.matcher(extractorRegex);
        while (matcher.find())
        {
            definedVariables.add(matcher.group(1));
        }

        return definedVariables;
    }

    /**
     * Parses a map recursively, in order to find the depth of the map. The values
     * of the map are also maps, and parsing goes further down until it reaches a
     * leaf that has no more children. For each map, its child with the biggest
     * depth is kept and returned recursively. If a branch reaches the
     * CONDITION_EXPRESSIONS_MAX, then this value is returned directly, because it
     * is guaranteed to make the validator rule fail, so no more parsing is
     * necessary.
     * 
     * @param map The map to parse recursively.
     * @return the depth of the deepest branch of this map
     */
    private int countExpressionsRecursive(Map<FieldDescriptor, Object> map)
    {
        int maxChild = 1;
        for (FieldDescriptor key : map.keySet())
        {
            var value = map.get(key);

            // If value is of a different type, then it is a leaf and recursion can't go
            // deeper.
            // In that case, the value for this branch is 1.
            if (value instanceof GeneratedMessageV3)
            {
                GeneratedMessageV3 a = (GeneratedMessageV3) value;
                int branchDepth = countExpressionsRecursive(a.getAllFields()) + 1;

                // if recursion depth reaches (CONDITION_EXPRESSIONS_MAX-1) at this point, then
                // the rule is
                // definitely out of the limit, so exit here without parsing further.
                if (branchDepth >= CONDITION_EXPRESSIONS_MAX - 1)
                {
                    return CONDITION_EXPRESSIONS_MAX;
                }
                else if (branchDepth > maxChild) // keep the value of the branch with the biggest depth
                {
                    maxChild = branchDepth;
                }
            }
        }

        return maxChild;
    }

    /**
     * Takes a string from an action-log text and parses it to check the syntax. If
     * occurrences of "{{" or "}}" are found, it starts searching the string to find
     * variables inside double curly braces "{{...}}". Each defined variable is
     * checked for correct syntax. Allowed formats are: var.x, req.path, req.method,
     * req.body, req.header['x'], resp.body, resp.header['x']. It also checks that
     * no curly brace pair is left unopened or unclosed.
     * 
     * @param text       the string to parse
     * @param actionName the name of the action-log, used for the error message
     * @return returns the error message. If length is 0, then the text syntax is
     *         valid
     */
    private String parseVariablesFromText(String text,
                                          String actionName)
    {
        var errMsg = new StringBuilder(100);

        if (text.contains("{{") || text.contains("}}")) // if double braces are not present, then the text is a simple string
        {
            int start = text.indexOf("{{");
            int end = -2;

            while (start != -1) // when start==-1, it means no other {{ occurrence exists
            {
                end = text.indexOf("}}", end + 2);

                if (start > end)
                {
                    if (end == -1)
                    {
                        errMsg.append("Opening braces that are not closed found at index " + start + " in action: " + actionName + ".\n");
                        end = text.length();
                    }
                    else
                    {
                        errMsg.append("Closing braces that are not opened found at index " + end + " in action: " + actionName + ".\n");
                    }
                }
                else
                {
                    String textSubstr = text.substring(start + 2, end);
                    if (!textSubstr.matches("req\\.header\\[\\'[a-zA-Z0-9-]+\\'\\]|var\\.[a-zA-Z][a-zA-Z0-9_]*|resp\\.header\\[\\'[a-zA-Z0-9-]+\\'\\]|req\\.method|req\\.path|req\\.body|resp\\.body"))
                    {
                        errMsg.append("Invalid type of variable inside braces opening at index " + start + " in action: " + actionName + ".\n");
                    }
                }

                start = text.indexOf("{{", end + 2);
            }

            end = text.indexOf("}}", end + 2);
            if (end != -1)
            {
                errMsg.append("Closing braces that are not opened found at index " + end + " in action: " + actionName + ".\n");
            }
            if (text.contains("}}}")) // this is a special case that will not be caught by the default search.
            {
                end = text.indexOf("}}}");
                errMsg.append("More than 2 closing braces found at index " + end + " in action: " + actionName + ".\n");
            }
        }
        return errMsg.toString();
    }

    private boolean isIpMatched(InetAddress svcIpv4,
                                InetAddress svcIpv6,
                                List<InetAddress> externalIPList) throws UnknownHostException
    {

        boolean ipv4Match = svcIpv4 != null && externalIPList.contains(svcIpv4);
        boolean ipv6Match = svcIpv6 != null && externalIPList.contains(svcIpv6);

        // dualstack
        if (svcIpv4 != null && svcIpv6 != null)
        {
            return ipv4Match && ipv6Match;
        }
        // singlestack
        return ipv4Match || ipv6Match;

    }
}
