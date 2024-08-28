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

package com.ericsson.sc.sepp.validator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.ericsson.adpal.cm.validator.RuleResult;
import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.expressionparser.ConditionParser;
import com.ericsson.sc.expressionparser.ConditionParserValidator;
import com.ericsson.sc.expressionparser.NfConditionParserValidator;
import com.ericsson.sc.expressionparser.ScpConditionParserValidator;
import com.ericsson.sc.expressionparser.SeppConditionParserValidator;
import com.ericsson.sc.expressionparser.TphParserValidator;
import com.ericsson.sc.glue.IfNetwork;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.sepp.config.ConfigUtils;
import com.ericsson.sc.sepp.model.ActionRouteRoundRobin;
import com.ericsson.sc.sepp.model.CustomFqdnLocator.MessageOrigin;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.MessageDatum;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.sc.sepp.model.NfPeerInfo.OutMessageHandling;
import com.ericsson.sc.sepp.model.NfPool;
import com.ericsson.sc.sepp.model.OwnNetwork;
import com.ericsson.sc.sepp.model.PriorityGroup;
import com.ericsson.sc.sepp.model.ResponseScreeningCase;
import com.ericsson.sc.sepp.model.RoamingPartner;
import com.ericsson.sc.sepp.model.RoutingAction;
import com.ericsson.sc.sepp.model.SearchInHeader;
import com.ericsson.sc.sepp.model.SearchInMessageBody;
import com.ericsson.sc.sepp.model.SearchInQueryParameter;
import com.ericsson.sc.sepp.model.StaticNfService;
import com.ericsson.sc.sepp.model.TargetRoamingPartner;
import com.ericsson.sc.sepp.model.TopologyHiding;
import com.ericsson.sc.validator.Rule;
import com.ericsson.sc.validator.RuleSupplier;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.ParseException;
import com.ericsson.utilities.graphs.SingleDfsLoopDetector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;
import com.google.re2j.Pattern;
import com.google.re2j.PatternSyntaxException;

import io.kubernetes.client.openapi.models.V1Service;
import io.reactivex.Single;

/**
 * Enum that keeps the Sepp validation {@link Rule}
 */
public class SeppRules implements RuleSupplier<EricssonSepp>
{
    private final List<Rule<EricssonSepp>> rulesList;
    private final List<V1Service> k8sServiceList;
    private static final String RULE_NOT_APPLICABLE = "Rule is not applicable";

    private static final int CONDITION_EXPRESSIONS_MAX = 100;

    // Should it be an enum?? If so, under which package?
    private static final String IP_HIDING = "IP Hiding";
    private static final String PSEUDO_SEARCH_RESULT = "Pseudo Search Result";
    private static final String FQDN_MAPPING = "FQDN Mapping";
    private static final String FQDN_SCRAMBLING = "FQDN Scrambling";

    SeppRules(final List<V1Service> k8sServiceList)
    {
        this.k8sServiceList = k8sServiceList;
        this.rulesList = List.of( /*
                                   * Rule_1 validates that the extractor regular expressions defined in routing
                                   * data of the SEPP configuration are google RE2 compliant
                                   */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
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
                                                                                            errMsg.append("'extractor-regex' : ")
                                                                                                  .append(rData.getExtractorRegex())
                                                                                                  .append(" defined in 'message-data': ")
                                                                                                  .append(rData.getName())
                                                                                                  .append(" is not a valid regular expression.\n");
                                                                                        }
                                                                                    }
                                                                                }));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_2 validates that the conditions defined in routing case of the SEPP
                                  * configuration comply to the condition grammar, according to
                                  * {@link ConditionParserValidator}, and that their recursion depth does not
                                  * exceed 100 (bugfix dnd-34424)
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getRoutingCase()
                                                                                .forEach(rCase -> rCase.getRoutingRule() //
                                                                                                       .forEach(rRule ->
                                                                                                       {
                                                                                                           var predExpr = rRule.getCondition();
                                                                                                           // predicate expression is mandatory in yang and
                                                                                                           // empty string is evaluated to
                                                                                                           // true

                                                                                                           isApplicable.set(true);

                                                                                                           try
                                                                                                           {
                                                                                                               ConditionParserValidator.validate(predExpr);

                                                                                                               Map<FieldDescriptor, Object> allFields = ConditionParser.parse(predExpr)
                                                                                                                                                                       .construct()
                                                                                                                                                                       .getAllFields();

                                                                                                               var depth = countExpressionsRecursive(allFields)
                                                                                                                           + 1;

                                                                                                               if (depth >= CONDITION_EXPRESSIONS_MAX)
                                                                                                               {
                                                                                                                   ruleResult.set(false);
                                                                                                                   errMsg.append("Condition for routing rule "
                                                                                                                                 + rRule.getName()
                                                                                                                                 + " exceeds max number of expressions\n");
                                                                                                               }
                                                                                                           }
                                                                                                           catch (ParseException e)
                                                                                                           {
                                                                                                               ruleResult.set(false);
                                                                                                               errMsg.append(e.getMessage())
                                                                                                                     .append(" for 'condition' defined in 'routing-rule' : ")
                                                                                                                     .append(rRule.getName())
                                                                                                                     .append("\n")
                                                                                                                     .append(displayIndex(predExpr,
                                                                                                                                          e.line,
                                                                                                                                          e.charPos));
                                                                                                           }
                                                                                                       })));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, "RULE_NOT_APPLICABLE"));
                                 },

                                 /**
                                  * Rule_3 validates that the nf-match-condition, scp-match-condition and
                                  * sepp-match-condition of {@link com.ericsson.sc.sepp.model.PriorityGroup}
                                  * comply to the nf-match-condition, scp-match-condition and
                                  * sepp-match-condition grammar, according to
                                  * {@link NfConditionParserValidator}, {@link ScpConditionParserValidator} and
                                  * {@link SeppConditionParserValidator}
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getNfPool()
                                                                                .forEach(pool -> pool.getPriorityGroup()//
                                                                                                     .forEach(subpool ->
                                                                                                     {
                                                                                                         var nfMatchCondition = subpool.getNfMatchCondition();
                                                                                                         var scpMatchCondition = subpool.getScpMatchCondition();
                                                                                                         var seppMatchCondition = subpool.getSeppMatchCondition();

                                                                                                         if (nfMatchCondition != null
                                                                                                             && !nfMatchCondition.isEmpty())
                                                                                                         {
                                                                                                             isApplicable.set(true);
                                                                                                             try
                                                                                                             {
                                                                                                                 NfConditionParserValidator.validate(nfMatchCondition);
                                                                                                             }
                                                                                                             catch (ParseException e)
                                                                                                             {
                                                                                                                 ruleResult.set(false);
                                                                                                                 errMsg.append(e.getMessage())
                                                                                                                       .append(" 'nf-match-condition' defined in 'priority-group' : ")
                                                                                                                       .append(subpool.getName())
                                                                                                                       .append(" of 'nf-pool' : ")
                                                                                                                       .append(pool.getName())
                                                                                                                       .append("\n")
                                                                                                                       .append(displayIndex(nfMatchCondition,
                                                                                                                                            e.line,
                                                                                                                                            e.charPos));
                                                                                                             }
                                                                                                         }
                                                                                                         if (scpMatchCondition != null
                                                                                                             && !scpMatchCondition.isEmpty())
                                                                                                         {
                                                                                                             isApplicable.set(true);
                                                                                                             try
                                                                                                             {
                                                                                                                 ScpConditionParserValidator.validate(scpMatchCondition);
                                                                                                             }
                                                                                                             catch (ParseException e)
                                                                                                             {
                                                                                                                 ruleResult.set(false);
                                                                                                                 errMsg.append(e.getMessage())
                                                                                                                       .append(" 'scp-match-condition' defined in 'priority-group' : ")
                                                                                                                       .append(subpool.getName())
                                                                                                                       .append(" of 'nf-pool' : ")
                                                                                                                       .append(pool.getName())
                                                                                                                       .append("\n")
                                                                                                                       .append(displayIndex(scpMatchCondition,
                                                                                                                                            e.line,
                                                                                                                                            e.charPos));
                                                                                                             }
                                                                                                         }
                                                                                                         if (seppMatchCondition != null
                                                                                                             && !seppMatchCondition.isEmpty())
                                                                                                         {
                                                                                                             isApplicable.set(true);
                                                                                                             try
                                                                                                             {
                                                                                                                 SeppConditionParserValidator.validate(seppMatchCondition);
                                                                                                             }
                                                                                                             catch (ParseException e)
                                                                                                             {
                                                                                                                 ruleResult.set(false);
                                                                                                                 errMsg.append(e.getMessage())
                                                                                                                       .append(" 'sepp-match-condition' defined in 'priority-group' : ")
                                                                                                                       .append(subpool.getName())
                                                                                                                       .append(" of 'nf-pool' : ")
                                                                                                                       .append(pool.getName())
                                                                                                                       .append("\n")
                                                                                                                       .append(displayIndex(seppMatchCondition,
                                                                                                                                            e.line,
                                                                                                                                            e.charPos));
                                                                                                             }
                                                                                                         }
                                                                                                     })));
                                     }

                                     if (isApplicable.get())
                                     {
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));
                                     }
                                     else
                                     {
                                         return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                     }
                                 },

                                 /**
                                  * Rule_4 validates that the conditions defined in screening case of the SEPP
                                  * configuration comply to the condition grammar, according to
                                  * {@link ConditionParserValidator}, and that their recursion depth does not
                                  * exceed 100 (bugfix dnd-34424)
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getRequestScreeningCase()
                                                                                .stream()
                                                                                .filter(Objects::nonNull)
                                                                                .forEach(sCase -> sCase.getScreeningRule() //
                                                                                                       .forEach(sRule ->
                                                                                                       {
                                                                                                           var predExpr = sRule.getCondition();
                                                                                                           // predicate expression is mandatory in yang and
                                                                                                           // empty string is evaluated to
                                                                                                           // true
                                                                                                           isApplicable.set(true);
                                                                                                           try
                                                                                                           {
                                                                                                               ConditionParserValidator.validate(predExpr);

                                                                                                               Map<FieldDescriptor, Object> allFields = ConditionParser.parse(predExpr)
                                                                                                                                                                       .construct()
                                                                                                                                                                       .getAllFields();

                                                                                                               var depth = countExpressionsRecursive(allFields)
                                                                                                                           + 1;

                                                                                                               if (depth >= CONDITION_EXPRESSIONS_MAX)
                                                                                                               {
                                                                                                                   ruleResult.set(false);
                                                                                                                   errMsg.append("Condition for screening rule "
                                                                                                                                 + sRule.getName()
                                                                                                                                 + " exceeds max number of expressions\n");
                                                                                                               }
                                                                                                           }
                                                                                                           catch (ParseException e)
                                                                                                           {
                                                                                                               ruleResult.set(false);
                                                                                                               errMsg.append(e.getMessage())
                                                                                                                     .append(" for 'condition' defined in 'screening-rule' : ")
                                                                                                                     .append(sRule.getName())
                                                                                                                     .append("\n")
                                                                                                                     .append(displayIndex(predExpr,
                                                                                                                                          e.line,
                                                                                                                                          e.charPos));
                                                                                                           }
                                                                                                       })));

                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getResponseScreeningCase()
                                                                                .stream()
                                                                                .filter(Objects::nonNull)
                                                                                .forEach(sCase -> sCase.getScreeningRule() //
                                                                                                       .forEach(sRule ->
                                                                                                       {
                                                                                                           var predExpr = sRule.getCondition();
                                                                                                           // predicate expression is mandatory in yang and
                                                                                                           // empty string is evaluated to
                                                                                                           // true
                                                                                                           isApplicable.set(true);
                                                                                                           try
                                                                                                           {
                                                                                                               ConditionParserValidator.validate(predExpr);

                                                                                                               Map<FieldDescriptor, Object> allFields = ConditionParser.parse(predExpr)
                                                                                                                                                                       .construct()
                                                                                                                                                                       .getAllFields();

                                                                                                               var depth = countExpressionsRecursive(allFields)
                                                                                                                           + 1;

                                                                                                               if (depth >= CONDITION_EXPRESSIONS_MAX)
                                                                                                               {
                                                                                                                   ruleResult.set(false);
                                                                                                                   errMsg.append("Condition for screening rule "
                                                                                                                                 + sRule.getName()
                                                                                                                                 + " exceeds max number of expressions\n");
                                                                                                               }
                                                                                                           }
                                                                                                           catch (ParseException e)
                                                                                                           {
                                                                                                               ruleResult.set(false);
                                                                                                               errMsg.append(e.getMessage())
                                                                                                                     .append(" for 'condition' defined in 'screening-rule' : ")
                                                                                                                     .append(sRule.getName())
                                                                                                                     .append("\n")
                                                                                                                     .append(displayIndex(predExpr,
                                                                                                                                          e.line,
                                                                                                                                          e.charPos));
                                                                                                           }
                                                                                                       })));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_5 validates that the terminal actions defined in screening and routing
                                  * cases of the SEPP configuration have no following actions defined
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             if (!nfInstance.getRequestScreeningCase().isEmpty())
                                             {
                                                 isApplicable.set(true);

                                                 nfInstance.getRequestScreeningCase()
                                                           .forEach(sCase -> sCase.getScreeningRule()//
                                                                                  .forEach(sRule ->
                                                                                  {
                                                                                      for (var i = 0; i < sRule.getScreeningAction().size(); i++)
                                                                                      {
                                                                                          if (sRule.getScreeningAction()
                                                                                                   .get(i)
                                                                                                   .getActionExitScreeningCase() != null
                                                                                              && i != sRule.getScreeningAction().size() - 1)
                                                                                          {
                                                                                              ruleResult.set(false);
                                                                                              errMsg.append("The 'action-exit-screening-case' defined in 'screening-rule' : ")
                                                                                                    .append(sRule.getName())
                                                                                                    .append(" has other actions configured afterwards\n");
                                                                                          }

                                                                                          if (sRule.getScreeningAction().get(i).getActionGoTo() != null
                                                                                              && i != sRule.getScreeningAction().size() - 1)
                                                                                          {
                                                                                              ruleResult.set(false);
                                                                                              errMsg.append("The 'action-go-to' defined in 'screening-rule' : ")
                                                                                                    .append(sRule.getName())
                                                                                                    .append(" has other actions configured afterwards\n");
                                                                                          }

                                                                                          if (sRule.getScreeningAction().get(i).getActionRejectMessage() != null
                                                                                              && i != sRule.getScreeningAction().size() - 1)
                                                                                          {
                                                                                              ruleResult.set(false);
                                                                                              errMsg.append("The 'action-reject-message' defined in 'screening-rule' : ")
                                                                                                    .append(sRule.getName())
                                                                                                    .append(" has other actions configured afterwards\n");
                                                                                          }

                                                                                          if (sRule.getScreeningAction().get(i).getActionDropMessage() != null
                                                                                              && i != sRule.getScreeningAction().size() - 1)
                                                                                          {
                                                                                              ruleResult.set(false);
                                                                                              errMsg.append("The 'action-drop-message' defined in 'screening-rule' : ")
                                                                                                    .append(sRule.getName())
                                                                                                    .append(" has other actions configured afterwards\n");
                                                                                          }
                                                                                      }
                                                                                  }));
                                             }
                                         });

                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             if (!nfInstance.getResponseScreeningCase().isEmpty())
                                             {
                                                 isApplicable.set(true);

                                                 nfInstance.getResponseScreeningCase()
                                                           .forEach(sCase -> sCase.getScreeningRule()//
                                                                                  .forEach(sRule ->
                                                                                  {
                                                                                      for (var i = 0; i < sRule.getScreeningAction().size(); i++)
                                                                                      {
                                                                                          if (sRule.getScreeningAction()
                                                                                                   .get(i)
                                                                                                   .getActionExitScreeningCase() != null
                                                                                              && i != sRule.getScreeningAction().size() - 1)
                                                                                          {
                                                                                              ruleResult.set(false);
                                                                                              errMsg.append("The 'action-exit-screening-case' defined in 'screening-rule' : ")
                                                                                                    .append(sRule.getName())
                                                                                                    .append(" has other actions configured afterwards\n");
                                                                                          }

                                                                                          if (sRule.getScreeningAction().get(i).getActionGoTo() != null
                                                                                              && i != sRule.getScreeningAction().size() - 1)
                                                                                          {
                                                                                              ruleResult.set(false);
                                                                                              errMsg.append("The 'action-go-to' defined in 'screening-rule' : ")
                                                                                                    .append(sRule.getName())
                                                                                                    .append(" has other actions configured afterwards\n");
                                                                                          }

                                                                                          if (sRule.getScreeningAction()
                                                                                                   .get(i)
                                                                                                   .getActionModifyStatusCode() != null
                                                                                              && i != sRule.getScreeningAction().size() - 1)
                                                                                          {
                                                                                              ruleResult.set(false);
                                                                                              errMsg.append("The 'action-modify-status-code' defined in 'screening-rule' : ")
                                                                                                    .append(sRule.getName())
                                                                                                    .append(" has other actions configured afterwards\n");
                                                                                          }
                                                                                      }
                                                                                  }));
                                             }
                                         });
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             if (!nfInstance.getRoutingCase().isEmpty())
                                             {
                                                 isApplicable.set(true);

                                                 nfInstance.getRoutingCase().forEach(sCase ->
                                                 {
                                                     sCase.getRoutingRule().forEach(sRule ->
                                                     {
                                                         var raList = sRule.getRoutingAction();
                                                         boolean foundTerminalAction = false;

                                                         for (var i = 0; i < raList.size(); i++)
                                                         {
                                                             if (ConfigUtils.isTerminalAction(raList.get(i)))
                                                             {
                                                                 if (foundTerminalAction)
                                                                 {
                                                                     ruleResult.set(false);
                                                                     errMsg.append("The routing actions defined in 'routing-rule': ")
                                                                           .append(sRule.getName())
                                                                           .append(" are not valid. There are more than one terminal actions configured\n");
                                                                 }
                                                                 else
                                                                 {
                                                                     foundTerminalAction = true;
                                                                 }
                                                             }
                                                             else
                                                             {
                                                                 if (foundTerminalAction)
                                                                 {
                                                                     ruleResult.set(false);
                                                                     errMsg.append("The routing actions defined in 'routing-rule': ")
                                                                           .append(sRule.getName())
                                                                           .append(" are not valid. No more actions are allowed after a terminal action\n");
                                                                 }
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
                                  * Rule_6 triggers the loop detection mechanism and checks for potential loops
                                  */
                                 config ->
                                 {
                                     var count = new AtomicInteger(0);
                                     List<List<Integer>> casesConnections = new ArrayList<>();
                                     List<Integer> caseEdges = new ArrayList<>();
                                     Map<String, Integer> mapCaseToInteger = new HashMap<>();

                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         for (var nfInstance : config.getEricssonSeppSeppFunction().getNfInstance())
                                         {
                                             if (!nfInstance.getRequestScreeningCase().isEmpty())
                                             {
                                                 isApplicable.set(true);

                                                 for (var sCase : nfInstance.getRequestScreeningCase())
                                                 {
                                                     for (var sRule : sCase.getScreeningRule())
                                                     {
                                                         for (var i = 0; i < sRule.getScreeningAction().size(); i++)
                                                         {
                                                             if (sRule.getScreeningAction().get(i).getActionGoTo() != null)
                                                             {
                                                                 // from vertex
                                                                 if (!mapCaseToInteger.containsKey(sCase.getName()))
                                                                 {
                                                                     mapCaseToInteger.put(sCase.getName(), count.get());
                                                                     count.getAndIncrement();
                                                                 }

                                                                 caseEdges.add(mapCaseToInteger.get(sCase.getName()));
                                                                 // to vertex
                                                                 String goToCase = sRule.getScreeningAction()
                                                                                        .get(i)
                                                                                        .getActionGoTo()
                                                                                        .getRequestScreeningCaseRef();

                                                                 if (!mapCaseToInteger.containsKey(goToCase))
                                                                 {
                                                                     mapCaseToInteger.put(goToCase, count.get());
                                                                     count.getAndIncrement();
                                                                 }

                                                                 caseEdges.add(mapCaseToInteger.get(goToCase));
                                                                 casesConnections.add(caseEdges);
                                                                 caseEdges = new ArrayList<>();
                                                             }
                                                         }
                                                     }
                                                 }
                                             }
                                         }

                                         if (!casesConnections.isEmpty())
                                         {
                                             var graph = new SingleDfsLoopDetector(mapCaseToInteger.size());

                                             for (List<Integer> list : casesConnections)
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
                                                 casesConnections = graph.dfsTraversal();

                                                 for (List<Integer> list : casesConnections)
                                                 {
                                                     if (list.size() > 1)
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Loop Detected among  request screening cases.\n");
                                                         break;
                                                     }
                                                 }
                                             }
                                         }

                                         casesConnections = new ArrayList<>();
                                         mapCaseToInteger = new HashMap<>();
                                         caseEdges = new ArrayList<>();
                                         count = new AtomicInteger(0);

                                         if (ruleResult.get())
                                         {
                                             for (var nfInstance : config.getEricssonSeppSeppFunction().getNfInstance())
                                             {
                                                 if (!nfInstance.getResponseScreeningCase().isEmpty())
                                                 {
                                                     isApplicable.set(true);

                                                     for (var sCase : nfInstance.getResponseScreeningCase())
                                                     {
                                                         for (var sRule : sCase.getScreeningRule())
                                                         {
                                                             for (var i = 0; i < sRule.getScreeningAction().size(); i++)
                                                             {
                                                                 if (sRule.getScreeningAction().get(i).getActionGoTo() != null)
                                                                 {
                                                                     // from vertex
                                                                     if (!mapCaseToInteger.containsKey(sCase.getName()))
                                                                     {
                                                                         mapCaseToInteger.put(sCase.getName(), count.get());
                                                                         count.getAndIncrement();
                                                                     }

                                                                     caseEdges.add(mapCaseToInteger.get(sCase.getName()));
                                                                     // to vertex
                                                                     String goToCase = sRule.getScreeningAction()
                                                                                            .get(i)
                                                                                            .getActionGoTo()
                                                                                            .getResponseScreeningCaseRef();

                                                                     if (!mapCaseToInteger.containsKey(goToCase))
                                                                     {
                                                                         mapCaseToInteger.put(goToCase, count.get());
                                                                         count.getAndIncrement();
                                                                     }

                                                                     caseEdges.add(mapCaseToInteger.get(goToCase));
                                                                     casesConnections.add(caseEdges);
                                                                     caseEdges = new ArrayList<>();
                                                                 }
                                                             }
                                                         }
                                                     }
                                                 }
                                             }

                                             if (!casesConnections.isEmpty())
                                             {
                                                 var graph = new SingleDfsLoopDetector(mapCaseToInteger.size());

                                                 for (List<Integer> list : casesConnections)
                                                 {
                                                     if (list.get(0).equals(list.get(1)))
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Loop Detected among reponse screening cases.\n");
                                                         break;
                                                     }
                                                     else
                                                     {
                                                         graph.addEdge(list.get(0), list.get(1));
                                                     }
                                                 }

                                                 if (ruleResult.get())
                                                 {
                                                     casesConnections = graph.dfsTraversal();

                                                     for (List<Integer> list : casesConnections)
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

                                             casesConnections = new ArrayList<>();
                                             mapCaseToInteger = new HashMap<>();
                                             caseEdges = new ArrayList<>();
                                             count = new AtomicInteger(0);
                                         }
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_7 validates that the body-json-pointer of
                                  * {@link com.ericsson.sc.sepp.model.MessageDatum} is a valid body JSON Pointer
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getMessageData()//
                                                                                .forEach(messageData ->
                                                                                {
                                                                                    var bodyJsonPointer = messageData.getBodyJsonPointer();

                                                                                    if (bodyJsonPointer != null)
                                                                                    {
                                                                                        isApplicable.set(true);

                                                                                        var valid = validateJsonPointer(bodyJsonPointer);

                                                                                        if (!valid)
                                                                                        {
                                                                                            ruleResult.set(false);
                                                                                            errMsg.append("The body-json-pointer defined in 'message-data' : ")
                                                                                                  .append(messageData.getName())
                                                                                                  .append(" isn't a valid body json pointer. \n");
                                                                                        }
                                                                                    }
                                                                                }));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_8 validates that there is not request screening case with same name as a
                                  * response screening case
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     List<String> namesList = new ArrayList<>();

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
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
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * RULE_9 validates that no more than one priority-group configured under the
                                  * same nf-pool can have the same configured priority (DND-26222)
                                  */
                                 config ->
                                 {

                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
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
                                                     errMsg.append("'priority-group' : ")
                                                           .append(entry)
                                                           .append(" defined in 'nf-pool' : ")
                                                           .append(nfPool.getName())
                                                           .append(" cannot have the same 'priority' as another 'priority-group' under the same 'nf-pool'.\n");
                                                 }
                                             });
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                 },

                                 // rule 10
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         // collect all pools
                                         var nfPools = config.getEricssonSeppSeppFunction()
                                                             .getNfInstance()
                                                             .stream()
                                                             .filter(nfInst -> nfInst.getNfPool() != null)
                                                             .flatMap(nfInst -> nfInst.getNfPool().stream())
                                                             .collect(Collectors.toList());

                                         if (nfPools != null && !nfPools.isEmpty())
                                         {
                                             // get all pools that are included in preferred routing
                                             var targetPools = config.getEricssonSeppSeppFunction()
                                                                     .getNfInstance()
                                                                     .stream()
                                                                     .flatMap(nfInst -> nfInst.getRoutingCase().stream())
                                                                     .flatMap(rc -> rc.getRoutingRule().stream())
                                                                     .flatMap(rr -> rr.getRoutingAction().stream())
                                                                     .filter(ra -> CommonConfigUtils.isPreferredRoutingRule(ra)
                                                                                   && ra.getActionRoutePreferred().getTargetNfPool().getNfPoolRef() != null)
                                                                     .map(rr -> Utils.getByName(nfPools,
                                                                                                rr.getActionRoutePreferred().getTargetNfPool().getNfPoolRef()))
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
                                                     errMsg.append("'Pool' ")
                                                           .append(p.getName())
                                                           .append(" cannot have static SCP reference only on pool level (pure indirect routing), since it is used with preferred routing'.\n");
                                                 }
                                             });
                                         }
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_12 validates that no more than one network ( own-network or
                                  * external-network ) can reference the same service-address
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         isApplicable.set(true);

                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             Map<String, Set<IfNetwork>> networksSvcRefs = Stream.concat(nfInstance.getOwnNetwork().stream(),
                                                                                                         nfInstance.getExternalNetwork().stream())
                                                                                                 .collect(Collectors.groupingBy(IfNetwork::getServiceAddressRef,
                                                                                                                                Collectors.toSet()));
                                             for (var entry : networksSvcRefs.entrySet())
                                             {
                                                 if (entry.getValue().size() > 1) // two networks reference the same service address
                                                 {
                                                     ruleResult.set(false);
                                                     var ownNetworks = entry.getValue()
                                                                            .stream()
                                                                            .filter(OwnNetwork.class::isInstance)
                                                                            .map(IfNetwork::getName)
                                                                            .collect(Collectors.toList());

                                                     var externalNetworks = entry.getValue()
                                                                                 .stream()
                                                                                 .filter(nw -> !(nw instanceof OwnNetwork))
                                                                                 .map(IfNetwork::getName)
                                                                                 .collect(Collectors.toList());

                                                     var ownNetworksPart = "";
                                                     var externalNetworksPart = "";

                                                     if (!ownNetworks.isEmpty())
                                                     {
                                                         ownNetworksPart = "'Own-network(s)' : " + String.join(",", ownNetworks);
                                                     }

                                                     if (!externalNetworks.isEmpty())
                                                     {
                                                         externalNetworksPart = (ownNetworks.isEmpty() ? "'External-network(s)' : "
                                                                                                       : " and 'external-network(s)' : ")
                                                                                + String.join(",", externalNetworks);
                                                     }

                                                     errMsg.append(ownNetworksPart)
                                                           .append(externalNetworksPart)
                                                           .append(" can't reference the same 'service-address' : ")
                                                           .append(entry.getKey())
                                                           .append("\n");
                                                 }
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_13 validates that when tls-port is set in a service-address, then the
                                  * certificate references have also the appropriate values in the model
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             nfInstance.getOwnNetwork().forEach(ownNetwork ->
                                             {
                                                 var svcAddress = Utils.getByName(nfInstance.getServiceAddress(), ownNetwork.getServiceAddressRef());

                                                 if (svcAddress != null && svcAddress.getTlsPort() != null)
                                                 {
                                                     isApplicable.set(true);

                                                     if (svcAddress.getAsymmetricKeyRef() == null && svcAddress.getAsymKeyInRef() == null
                                                         && svcAddress.getAsymKeyOutRef() == null)
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Tls-port is enabled for service-address ")
                                                               .append(svcAddress.getName())
                                                               .append(", but no asymmetric-key is defined.\n");
                                                     }

                                                     if (ownNetwork.getTrustedCertificateList() == null && ownNetwork.getTrustedCertInListRef() == null
                                                         && ownNetwork.getTrustedCertOutListRef() == null)
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Tls-port is enabled for service-address ")
                                                               .append(svcAddress.getName())
                                                               .append(", but no trusted-certificate-list is set in own-network ")
                                                               .append(ownNetwork.getName())
                                                               .append(" .\n");
                                                     }
                                                 }
                                             });

                                             nfInstance.getExternalNetwork().forEach(extNetwork ->
                                             {
                                                 var svcAddress = Utils.getByName(nfInstance.getServiceAddress(), extNetwork.getServiceAddressRef());

                                                 if (svcAddress != null && svcAddress.getTlsPort() != null)
                                                 {
                                                     isApplicable.set(true);

                                                     if (svcAddress.getAsymmetricKeyRef() == null && svcAddress.getAsymKeyInRef() == null
                                                         && svcAddress.getAsymKeyOutRef() == null)
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Tls-port is enabled for service-address ")
                                                               .append(svcAddress.getName())
                                                               .append(", but no asymmetric-key is defined.\n");
                                                     }

                                                     if (extNetwork.getRoamingPartner() != null)
                                                     {
                                                         extNetwork.getRoamingPartner().stream().forEach(rp ->
                                                         {
                                                             if (rp.getTrustedCertificateList() == null && rp.getTrustedCertInListRef() == null
                                                                 && rp.getTrustedCertOutListRef() == null)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("Tls-port is enabled for service-address ")
                                                                       .append(svcAddress.getName())
                                                                       .append(", but no trusted-certificate-list is set in roaming-partner ")
                                                                       .append(rp.getName())
                                                                       .append(" .\n");
                                                             }
                                                         });
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
                                  * Rule_14 validates that all RPs have distinct names across all the external
                                  * networks
                                  */
                                 config ->

                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             var rps = nfInstance.getExternalNetwork()
                                                                 .stream()
                                                                 .filter(extNet -> extNet.getRoamingPartner() != null)
                                                                 .flatMap(extNet -> extNet.getRoamingPartner().stream())
                                                                 .collect(Collectors.toList());

                                             if (!rps.isEmpty())
                                             {
                                                 isApplicable.set(true);

                                                 rps.stream()
                                                    .filter(rp -> Collections.frequency(rps, rp) > 1) //
                                                    .distinct()
                                                    .forEach(rp ->
                                                    {
                                                        ruleResult.set(false);
                                                        errMsg.append("Roaming-partner with name ")
                                                              .append(rp.getName())
                                                              .append(", appears multiple times across the external-networks.\n");
                                                    });
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /**
                                  * Rule_15 validates that all roaming-partners have different domain-names with
                                  * each other
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             nfInstance.getExternalNetwork().forEach(exNetwork ->
                                             {
                                                 List<RoamingPartner> rps = exNetwork.getRoamingPartner();

                                                 if (rps.size() > 1)
                                                 {
                                                     isApplicable.set(true);

                                                     for (var i = 0; i < rps.size(); i++)
                                                     {
                                                         List<String> domainNamesCurrent = rps.get(i).getDomainName();

                                                         for (int j = i + 1; j < rps.size(); j++)
                                                         {
                                                             List<String> domainNamesNext = rps.get(j).getDomainName();

                                                             List<String> sameValues = domainNamesCurrent.stream()
                                                                                                         .filter(domainNamesNext::contains)
                                                                                                         .collect(Collectors.toList());

                                                             if (!sameValues.isEmpty())  // there are domain-names with same value in different Roaming Partners
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("Each Roaming Partner in the same external network should have unique domain names.")
                                                                       .append("The common domain names are ")
                                                                       .append(sameValues.toString());
                                                                 break;
                                                             }
                                                         }

                                                         if (!ruleResult.get())
                                                         {
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
                                  * Rule_17 validates that each callback-uri-json-pointer of
                                  * {@link com.ericsson.sc.sepp.model.CallbackUrus} is a valid body JSON Pointer
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             if (nfInstance.getTelescopicFqdn() != null)
                                             {
                                                 var tf = nfInstance.getTelescopicFqdn();

                                                 if (tf.getCallbackUri() != null && !tf.getCallbackUri().isEmpty())
                                                 {
                                                     isApplicable.set(true);

                                                     for (var callbackUri : tf.getCallbackUri())
                                                     {
                                                         for (var callbackUriJsonPointer : callbackUri.getCallbackUriJsonPointer())
                                                         {
                                                             var valid = validateJsonPointer(callbackUriJsonPointer);

                                                             if (!valid)
                                                             {
                                                                 ruleResult.set(false);
                                                                 errMsg.append("The callback-uri-json-pointer defined in 'callback-uri' : ")
                                                                       .append(callbackUri.getApiName())
                                                                       .append("/v")
                                                                       .append(callbackUri.getApiVersion())
                                                                       .append(" isn't a valid body json pointer. \n");
                                                             }
                                                         }
                                                     }
                                                 }
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },

                                 /*
                                  * Rule_18 validates that the domain names in the Roaming Partners are valid
                                  * IPv4, IPv6 or fqdn
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     final var ipv6PatternFull = "((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}"
                                                                 + "((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|"
                                                                 + "(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}"
                                                                 + "(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))(%[\\p{N}\\p{L}]+)?";
                                     final var ipv6PatternSmall = "(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|" + "((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)(%.+)?";
                                     final var ipv6NoZone = "[0-9a-fA-F:\\.]*";
                                     final var checkOnlyNumbersPattern = "[0-9.*]*";
                                     final var domainPattern = "((([a-zA-Z0-9*]([a-zA-Z0-9\\-*]){0,61})?[a-zA-Z0-9*]\\.)(([a-zA-Z0-9]([a-zA-Z0-9\\-]){0,61})?[a-zA-Z0-9]\\.)*"
                                                               + "([a-zA-Z0-9]([a-zA-Z0-9\\-]){0,61})?[a-zA-Z0-9]\\.?)|\\.";

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getExternalNetwork() //
                                                                                .forEach(externalNetwork ->
                                                                                {
                                                                                    if (externalNetwork.getRoamingPartner() != null)
                                                                                    {
                                                                                        isApplicable.set(true);

                                                                                        if (ruleResult.get())
                                                                                        {
                                                                                            for (RoamingPartner roamingPartner : externalNetwork.getRoamingPartner())
                                                                                            {
                                                                                                for (String domainName : roamingPartner.getDomainName())
                                                                                                {
                                                                                                    if (Pattern.matches(ipv6PatternFull, domainName)
                                                                                                        && Pattern.matches(ipv6PatternSmall, domainName)
                                                                                                        && Pattern.matches(ipv6NoZone, domainName))
                                                                                                    {
                                                                                                    }

                                                                                                    else if (Pattern.matches(domainPattern, domainName))
                                                                                                    {
                                                                                                        List<String> splitList = Arrays.asList(domainName.split("\\."));

                                                                                                        if (splitList.get(0).contains("*"))
                                                                                                        {
                                                                                                            if (Pattern.matches(checkOnlyNumbersPattern,
                                                                                                                                domainName))
                                                                                                            {
                                                                                                                ruleResult.set(false);
                                                                                                                errMsg.append("The domain-name ")
                                                                                                                      .append(domainName)
                                                                                                                      .append(" has only numbers, so a wildcard shouldn't exist.\n");
                                                                                                                break;
                                                                                                            }

                                                                                                            else
                                                                                                            {
                                                                                                                // check if wildcard appears in left-most spot
                                                                                                                // more than one time
                                                                                                                int occurences = StringUtils.countMatches(splitList.get(0),
                                                                                                                                                          "*");

                                                                                                                if (occurences > 1)
                                                                                                                {
                                                                                                                    ruleResult.set(false);
                                                                                                                    errMsg.append("The domain-name ")
                                                                                                                          .append(domainName)
                                                                                                                          .append(" has more than one wildcard.\n");
                                                                                                                    break;
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }

                                                                                                    else
                                                                                                    {
                                                                                                        ruleResult.set(false);
                                                                                                        errMsg.append("The domain-name ")
                                                                                                              .append(domainName)
                                                                                                              .append(" has invalid value.\n");
                                                                                                        break;
                                                                                                    }
                                                                                                }

                                                                                                if (!ruleResult.get())
                                                                                                    break;
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }));
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
                                     if (config == null || config.getEricssonSeppSeppFunction() == null)
                                         return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                     final var errMsg = new StringBuilder(100);

                                     var result = true;

                                     for (final NfInstance nfInstance : config.getEricssonSeppSeppFunction().getNfInstance())
                                     {
                                         if (nfInstance.getNrfService() == null || nfInstance.getNrfService().getNfManagement() == null
                                             || nfInstance.getNrfService().getNfManagement().getNrfGroupRef().isEmpty())
                                         {
                                             continue;
                                         }

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
                                                     errMsg.append(" nrf-group 'sepp-function nf-instance ")
                                                           .append(nfInstance.getName())
                                                           .append(" nrf-group ")
                                                           .append(groupName)
                                                           .append("' (referenced from 'sepp-function nf-instance ")
                                                           .append(nfInstance.getName())
                                                           .append(" nrf-service nf-management nrf-group-ref')")
                                                           .append(" or all its members must have attribute 'nf-profile-ref' defined.\n");
                                                     break;
                                                 }
                                             }
                                             else
                                             {
                                                 result = false;

                                                 errMsg.append(" nrf-group 'sepp-function nf-instance ")
                                                       .append(nfInstance.getName())
                                                       .append(" nrf-group ")
                                                       .append(groupName)
                                                       .append("' (referenced from 'sepp-function nf-instance ")
                                                       .append(nfInstance.getName())
                                                       .append(" nrf-service nf-management nrf-group-ref')")
                                                       .append(" must have been defined.\n");
                                                 break;
                                             }
                                         }

                                         if (!result)
                                             break;
                                     }
                                     return Single.just(new RuleResult(result, errMsg.toString()));
                                 },

                                 /**
                                  * Rule_19 cross checks the ports and tls-ports of the Service addresses along
                                  * with the available Kubernetes ports.
                                  */
                                 config ->
                                 {

                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     Set<String> targetPorts = new HashSet<>();

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             isApplicable.compareAndSet(false, true);

                                             // stores all the Service Addresses that are referenced on the networks
                                             Set<String> networksSvcRefs = Stream.concat(nfInstance.getOwnNetwork().stream(),
                                                                                         nfInstance.getExternalNetwork().stream())
                                                                                 .map(IfNetwork::getServiceAddressRef)
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
                                                             errMsg.append("Wrong configuration in service-address(es). Please make sure the following apply:\n")
                                                                   .append("* 'port' corresponds to an unencrypted port exposed by  the targeted kubernetes SEPP worker service\n")
                                                                   .append("* 'tls-port' corresponds to an encrypted port exposed by the targeted kubernetes SEPP worker service\n")
                                                                   .append("* Two service-addresses cannot use the same 'port'/'tls-port' if they correspond to the same kubernetes SEPP worker service\n");
                                                         }
                                                     }

                                                 }
                                             });
                                             if (ruleResult.get() && targetPorts.size() != listenerCount[0])
                                             {
                                                 ruleResult.set(false);
                                                 errMsg.append("Wrong configuration in service-address(es). Please make sure the following apply:\n")
                                                       .append("* 'port' corresponds to an unencrypted port exposed by  the targeted kubernetes SEPP worker service\n")
                                                       .append("* 'tls-port' corresponds to an encrypted port exposed by the targeted kubernetes SEPP worker service\n")
                                                       .append("* Two service-addresses cannot use the same 'port'/'tls-port' if they correspond to the same kubernetes SEPP worker service\n");
                                             }
                                         });
                                     }
                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                 },
                                 /**
                                  * Rule_20 validates that all Roaming Partners have unique names after all
                                  * special characters being replaced with underscore.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             nfInstance.getExternalNetwork().forEach(exNetwork ->
                                             {
                                                 List<RoamingPartner> rps = exNetwork.getRoamingPartner();

                                                 if (rps.size() > 1)
                                                 {
                                                     isApplicable.set(true);

                                                     List<String> originalRPNames = new ArrayList<>();
                                                     HashSet<String> storedRPNames = new HashSet<>();
                                                     HashSet<String> duplicatedRPNames = new HashSet<>();

                                                     rps.forEach(rp ->
                                                     {
                                                         originalRPNames.add(rp.getName());
                                                         String replacedName = rp.getName().replaceAll("[^a-zA-Z0-9]", "_");
                                                         if (storedRPNames.contains(replacedName))
                                                         {
                                                             duplicatedRPNames.add(replacedName);
                                                         }
                                                         else
                                                         {
                                                             storedRPNames.add(replacedName);
                                                         }
                                                     });

                                                     List<String> wrongRPNames = originalRPNames.stream()
                                                                                                .filter(rpName -> duplicatedRPNames.contains(rpName.replaceAll("[^a-zA-Z0-9]",
                                                                                                                                                               "_")))
                                                                                                .collect(Collectors.toList());

                                                     if (!wrongRPNames.isEmpty())  // there are names with same value after replacing special chars with
                                                                                   // underscore in different Roaming Partners
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Each Roaming Partner in the same external network should have unique name after replacing non-alphanumeric characters with underscore.")
                                                               .append(" The wrong names of RPs are ")
                                                               .append(wrongRPNames.toString());
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
                                  * Rule_21 validates that the conditions defined in topology hiding list of the
                                  * SEPP configuration comply to the condition grammar, according to
                                  * {@link TphParserValidator}
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getTopologyHiding().forEach(tph ->
                                               {
                                                   var tphExpr = tph.getCondition();
                                                   // predicate expression is mandatory in yang and
                                                   // empty string is evaluated to
                                                   // true

                                                   isApplicable.set(true);

                                                   try
                                                   {
                                                       TphParserValidator.validate(tphExpr);
                                                   }
                                                   catch (ParseException e)
                                                   {
                                                       ruleResult.set(false);
                                                       errMsg.append(e.getMessage())
                                                             .append(" for 'condition' defined in 'topology-hiding' : ")
                                                             .append(tph.getName())
                                                             .append("\n")
                                                             .append(displayIndex(tphExpr, e.line, e.charPos));
                                                   }
                                               }));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_22 validates that the nf-type defined in the condition under the
                                  * topology hiding list of the SEPP configuration for pseudo search result must
                                  * be 'UDM' or 'AUSF'.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {

                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .stream()
                                               .filter(nfInstance -> nfInstance.getTopologyHiding() != null && !nfInstance.getTopologyHiding().isEmpty())
                                               .flatMap(nf -> nf.getTopologyHiding().stream())
                                               .filter(tph -> tph.getPseudoSearchResult() != null)
                                               .forEach(tph ->
                                               {

                                                   var tphExpr = tph.getCondition();
                                                   // predicate expression is mandatory in yang

                                                   isApplicable.set(true);

                                                   var regex = ".*'([^']*)'.*";
                                                   var pattern = Pattern.compile(regex);
                                                   var matcher = pattern.matcher(tphExpr);
                                                   if (matcher.find())
                                                   {
                                                       var extractedNfType = matcher.group(1);

                                                       if (!extractedNfType.equalsIgnoreCase("UDM") && !extractedNfType.equalsIgnoreCase("AUSF"))
                                                       {
                                                           ruleResult.set(false);
                                                           errMsg.append("The target-nf-type extracted from the condition for pseudo search result must be 'UDM' or 'AUSF'. Targer-nf-type: ")
                                                                 .append(extractedNfType)
                                                                 .append("\n");
                                                       }
                                                   }
                                                   else
                                                   {
                                                       ruleResult.set(false);
                                                       errMsg.append("The value of the target-nf-type within the condition should be enclosed by single quotes.");
                                                   }
                                               });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_23 validates that the value of a json body action is a valid JSON value.
                                  * Also checks the path to be a valid body json pointer according to RFC 6901
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {

                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
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

                                                               if (!validateJsonPointer(sAction.getActionModifyJsonBody().getJsonPointer()))
                                                               {
                                                                   ruleResult.set(false);
                                                                   errMsg.append("The json-pointer '")
                                                                         .append(sAction.getActionModifyJsonBody().getJsonPointer())
                                                                         .append("' defined in screening-action ")
                                                                         .append(sAction.getName())
                                                                         .append(" isn't a valid body json pointer according to RFC 6901.\n");
                                                               }
                                                               else
                                                               {

                                                                   if (sAction.getActionModifyJsonBody().getAddValue() != null)
                                                                   {
                                                                       if (!isValueValidJson(sAction.getActionModifyJsonBody().getAddValue().getValue()))
                                                                       {
                                                                           ruleResult.set(false);
                                                                           errMsg.append("The screening-action's ")
                                                                                 .append(sAction.getName())
                                                                                 .append(" value '")
                                                                                 .append(sAction.getActionModifyJsonBody().getAddValue().getValue())
                                                                                 .append("' is not a valid JSON value\n");
                                                                       }
                                                                   }

                                                                   if (sAction.getActionModifyJsonBody().getReplaceValue() != null)
                                                                   {
                                                                       if (!isValueValidJson(sAction.getActionModifyJsonBody().getReplaceValue().getValue()))
                                                                       {
                                                                           ruleResult.set(false);
                                                                           errMsg.append("The screening-action's ")
                                                                                 .append(sAction.getName())
                                                                                 .append(" value '")
                                                                                 .append(sAction.getActionModifyJsonBody().getReplaceValue().getValue())
                                                                                 .append("' is not a valid JSON value\n");
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

                                                               if (!validateJsonPointer(sAction.getActionModifyJsonBody().getJsonPointer()))
                                                               {
                                                                   ruleResult.set(false);
                                                                   errMsg.append("The json-pointer '")
                                                                         .append(sAction.getActionModifyJsonBody().getJsonPointer())
                                                                         .append("' defined in screening-action ")
                                                                         .append(sAction.getName())
                                                                         .append(" isn't a valid body json pointer according to RFC 6901.\n");
                                                               }
                                                               else
                                                               {

                                                                   if (sAction.getActionModifyJsonBody().getAddValue() != null)
                                                                   {
                                                                       if (!isValueValidJson(sAction.getActionModifyJsonBody().getAddValue().getValue()))
                                                                       {
                                                                           ruleResult.set(false);
                                                                           errMsg.append("The screening-action's ")
                                                                                 .append(sAction.getName())
                                                                                 .append(" value '")
                                                                                 .append(sAction.getActionModifyJsonBody().getAddValue().getValue())
                                                                                 .append("' is not a valid JSON value\n");
                                                                       }
                                                                   }

                                                                   if (sAction.getActionModifyJsonBody().getReplaceValue() != null)
                                                                   {
                                                                       if (!isValueValidJson(sAction.getActionModifyJsonBody().getReplaceValue().getValue()))
                                                                       {
                                                                           ruleResult.set(false);
                                                                           errMsg.append("The screening-action's ")
                                                                                 .append(sAction.getName())
                                                                                 .append(" value '")
                                                                                 .append(sAction.getActionModifyJsonBody().getReplaceValue().getValue())
                                                                                 .append("' is not a valid JSON value\n");
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
                                  * Rule_24 validates that the value of a variable inside the add-from-var-name
                                  * replace-from-var-name, append-from-var-name, prepend-from-var-namem and
                                  * search-replace with variables of action-modify-json-body is a value that
                                  * exists in the message-data-ref
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {

                                         config.getEricssonSeppSeppFunction()
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
                                                                              errMsg.append("The screening-action's ")
                                                                                    .append(sAction.getName())
                                                                                    .append(" variable '")
                                                                                    .append(sAction.getActionModifyJsonBody().getAddFromVarName().getVariable())
                                                                                    .append("' is not referenced in the request-screening-case ")
                                                                                    .append(reqCase.getName())
                                                                                    .append("\n");
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
                                                                              errMsg.append("The screening-action's ")
                                                                                    .append(sAction.getName())
                                                                                    .append(" variable '")
                                                                                    .append(sAction.getActionModifyJsonBody()
                                                                                                   .getReplaceFromVarName()
                                                                                                   .getVariable())
                                                                                    .append("' is not referenced in the request-screening-case ")
                                                                                    .append(reqCase.getName())
                                                                                    .append("\n");
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
                                                                               errMsg.append("The screening-action's ")
                                                                                     .append(sAction.getName())
                                                                                     .append(" variable '")
                                                                                     .append(sAction.getActionModifyJsonBody()
                                                                                                    .getAddFromVarName()
                                                                                                    .getVariable())
                                                                                     .append("' is not referenced in the response-screening-case ")
                                                                                     .append(respCase.getName())
                                                                                     .append("\n");
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
                                                                               errMsg.append("The screening-action's ")
                                                                                     .append(sAction.getName())
                                                                                     .append(" variable '")
                                                                                     .append(sAction.getActionModifyJsonBody()
                                                                                                    .getReplaceFromVarName()
                                                                                                    .getVariable())
                                                                                     .append("' is not referenced in the response-screening-case ")
                                                                                     .append(respCase.getName())
                                                                                     .append("\n");
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
                                  * Rule_25 validates that a own network is referenced once across all the
                                  * ingress elements of vTap configuration
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
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
                                                                       errMsg.append("Own-network-refernce ")
                                                                             .append(ref)
                                                                             .append(" appears multiple times across the ingress list");
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
                                  * Rule_26 validates that a own network is referenced once across all the
                                  * ingress elements of vTap configuration
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             if (nfInstance.getVtap() != null && nfInstance.getVtap().getVtapConfiguration() != null
                                                 && nfInstance.getVtap().getVtapConfiguration().getProxy() != null)
                                             {
                                                 var extNetworkRefs = nfInstance.getVtap()
                                                                                .getVtapConfiguration()
                                                                                .getProxy()
                                                                                .getIngress()
                                                                                .stream()
                                                                                .filter(ingress -> !((com.ericsson.sc.sepp.model.Ingress) ingress).getExternalNetworkRef()
                                                                                                                                                  .isEmpty())
                                                                                .flatMap(ingress -> ((com.ericsson.sc.sepp.model.Ingress) ingress).getExternalNetworkRef()
                                                                                                                                                  .stream())
                                                                                .collect(Collectors.toList());

                                                 if (!extNetworkRefs.isEmpty())
                                                 {
                                                     isApplicable.set(true);
                                                     extNetworkRefs.stream()
                                                                   .filter(ref -> Collections.frequency(extNetworkRefs, ref) > 1)
                                                                   .distinct()
                                                                   .forEach(ref ->
                                                                   {
                                                                       ruleResult.set(false);
                                                                       errMsg.append("External-network-refernce ")
                                                                             .append(ref)
                                                                             .append(" appears multiple times across the ingress list");
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
                                  * Rule_27 validates that if an RP is used in a routing action, the
                                  * corresponding pool exists.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
                                         {
                                             var roamingPartnersInActions = nfInstance.getRoutingCase()
                                                                                      .stream()
                                                                                      .flatMap(rc -> rc.getRoutingRule().stream())
                                                                                      .flatMap(rr -> rr.getRoutingAction().stream())
                                                                                      .map(RoutingAction::getActionRouteRoundRobin)
                                                                                      .filter(Objects::nonNull)
                                                                                      .map(ActionRouteRoundRobin::getTargetRoamingPartner)
                                                                                      .filter(Objects::nonNull)
                                                                                      .map(TargetRoamingPartner::getRoamingPartnerRef);
                                             var notReferencedRps = roamingPartnersInActions.filter(rp -> !nfInstance.getNfPool()
                                                                                                                     .stream()
                                                                                                                     .map(NfPool::getRoamingPartnerRef)
                                                                                                                     .filter(Objects::nonNull)
                                                                                                                     .collect(Collectors.toList())
                                                                                                                     .contains(rp))
                                                                                            .collect(Collectors.toList());
                                             if (!notReferencedRps.isEmpty())
                                             {
                                                 isApplicable.set(true);
                                                 ruleResult.set(false);
                                                 errMsg.append("Roaming Partner(s) ")
                                                       .append(notReferencedRps.toString())
                                                       .append(" are used in a routing action, but there are no nf-pools that reference them.");
                                             }

                                         });

                                     }
                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));

                                 },
                                 /**
                                  * Rule_28 validates that every request-screening-case doesn't include
                                  * message-data-ref with response-header or screening rules with conditions
                                  * containing resp.header.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInst ->
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
                                                         errMsg.append("Request-screening-case '")
                                                               .append(reqCase.getName())
                                                               .append("' includes message-data-ref ")
                                                               .append(commonData.toString())
                                                               .append(" with response-header.\n");
                                                     }
                                                     if (!respHeaderRules.isEmpty())
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Screening-rule '")
                                                               .append(respHeaderRules.toString())
                                                               .append("' of request-screening-case '")
                                                               .append(reqCase.getName())
                                                               .append("' includes condition containing 'resp.header'.\n");
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
                                  * Rule_29 validates that every routing-case doesn't include message-data-ref
                                  * with response-header or routing rules with conditions containing resp.header.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInst ->
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
                                                         errMsg.append("Routing-case '")
                                                               .append(rc.getName())
                                                               .append("' includes message-data-ref ")
                                                               .append(commonData.toString())
                                                               .append(" with response-header.\n");
                                                     }
                                                     if (!respHeaderRules.isEmpty())
                                                     {
                                                         ruleResult.set(false);
                                                         errMsg.append("Routing-rule '")
                                                               .append(respHeaderRules.toString())
                                                               .append("' of routing-case '")
                                                               .append(rc.getName())
                                                               .append("' includes condition containing 'resp.header'.\n");
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
                                  * Rule_30 validates that the nf-match-condition and scp-match-condition of
                                  * {@link com.ericsson.sc.sepp.model.NfPool} comply to the nf-match-condition
                                  * and scp-match-condition grammar, according to
                                  * {@link NfConditionParserValidator} and {@link ScpConditionParserValidator}
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
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
                                                                                            errMsg.append(e.getMessage())
                                                                                                  .append(" for 'nf-match-condition' defined in 'NfPool' : ")
                                                                                                  .append(pool.getName())
                                                                                                  .append("\n")
                                                                                                  .append(displayIndex(nfMatchCondition, e.line, e.charPos));
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
                                                                                            errMsg.append(e.getMessage())
                                                                                                  .append(" for 'scp-match-condition' defined in 'NfPool' : ")
                                                                                                  .append(pool.getName())
                                                                                                  .append("\n")
                                                                                                  .append(displayIndex(scpMatchCondition, e.line, e.charPos));
                                                                                        }
                                                                                    }
                                                                                }));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_31 validates that the references under nfpool>network and
                                  * enternalNetwork>RP>N32C matches the RP names and nf pool names respectively
                                  * Also validates that the referenced static-nf-instances have only 1
                                  * static-nf-service that has configured only HTTPS scheme and maximum 1 IP
                                  * Address
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> ConfigUtils.getAllRoamingPartnersWithN32C(nfInstance.getExternalNetwork()
                                                                                                                          .stream()
                                                                                                                          .collect(Collectors.toList()))
                                                                                 .forEach(rp ->
                                                                                 {
                                                                                     isApplicable.set(true);
                                                                                     var rpName = rp.getName();
                                                                                     var poolRef = rp.getN32C().getNfPoolRef();

                                                                                     Optional.ofNullable(Utils.getByName(nfInstance.getNfPool(), poolRef))
                                                                                             .ifPresent(pool ->
                                                                                             {
                                                                                                 if (pool.getRoamingPartnerRef() == null
                                                                                                     || !pool.getRoamingPartnerRef().equals(rpName))
                                                                                                 {
                                                                                                     ruleResult.set(false);
                                                                                                     errMsg.append("The 'nf-pool': ")
                                                                                                           .append(pool.getName())
                                                                                                           .append(" refenced by the 'n32-c' container of 'roaming-partner': ")
                                                                                                           .append(rpName)
                                                                                                           .append(" must have a 'roaming-partner-ref' to ")
                                                                                                           .append(rpName)
                                                                                                           .append("\n");
                                                                                                 }

                                                                                                 var staticNfRefs = pool.getNfPoolDiscovery()
                                                                                                                        .stream()
                                                                                                                        .flatMap(poolDiscovery -> poolDiscovery.getStaticNfInstanceDataRef()
                                                                                                                                                               .stream())
                                                                                                                        .filter(Objects::nonNull)
                                                                                                                        .collect(Collectors.toList());

                                                                                                 var staticSeppRefs = pool.getStaticSeppInstanceDataRef()
                                                                                                                          .stream()
                                                                                                                          .filter(Objects::nonNull)
                                                                                                                          .collect(Collectors.toList());

                                                                                                 var staticScpRefs = pool.getStaticScpInstanceDataRef()
                                                                                                                         .stream()
                                                                                                                         .filter(Objects::nonNull)
                                                                                                                         .collect(Collectors.toList());

                                                                                                 if (staticScpRefs != null && !staticScpRefs.isEmpty())
                                                                                                 {
                                                                                                     ruleResult.set(false);
                                                                                                     errMsg.append("The 'nf-pool': ")
                                                                                                           .append(pool.getName())
                                                                                                           .append(" refenced by the 'n32-c' container of 'roaming-partner': ")
                                                                                                           .append(rpName)
                                                                                                           .append(" must not have a 'static-scp-instance-data-ref' ")
                                                                                                           .append("\n");
                                                                                                 }

                                                                                                 if (staticNfRefs != null && !staticNfRefs.isEmpty())
                                                                                                 {

                                                                                                     Utils.getListByNames(nfInstance.getStaticNfInstanceData(),
                                                                                                                          staticNfRefs)
                                                                                                          .stream()
                                                                                                          .flatMap(nfs -> nfs.getStaticNfInstance().stream())
                                                                                                          .forEach(nf ->
                                                                                                          {
                                                                                                              if (!nf.getStaticNfService().isEmpty())
                                                                                                              {
                                                                                                                  // check if each static-nf-instance
                                                                                                                  // has
                                                                                                                  // only 1 static-nf-service

                                                                                                                  if (nf.getStaticNfService().size() > 1)
                                                                                                                  {
                                                                                                                      ruleResult.set(false);
                                                                                                                      errMsg.append("'static-nf-instance' ")
                                                                                                                            .append(nf.getName())
                                                                                                                            .append(" can only have one 'static-nf-service'.\n");
                                                                                                                  }
                                                                                                                  var nfServ = nf.getStaticNfService()
                                                                                                                                 .toArray(new StaticNfService[0])[0];
                                                                                                                  if (!nfServ.getAddress()
                                                                                                                             .getScheme()
                                                                                                                             .equals(Scheme.HTTPS))
                                                                                                                  {

                                                                                                                      ruleResult.set(false);
                                                                                                                      errMsg.append("'static-nf-service' ")
                                                                                                                            .append(nfServ.getName())
                                                                                                                            .append(" defined under 'static-nf-instance' ")
                                                                                                                            .append(nf.getName())
                                                                                                                            .append(" can only have a 'scheme' of https.\n");
                                                                                                                  }

                                                                                                                  var fqdn = nfServ.getAddress().getFqdn();
                                                                                                                  if (fqdn == null || fqdn.isEmpty())
                                                                                                                  {
                                                                                                                      ruleResult.set(false);
                                                                                                                      errMsg.append("'static-nf-service' ")
                                                                                                                            .append(nfServ.getName())
                                                                                                                            .append(" defined under 'static-nf-instance' ")
                                                                                                                            .append(nf.getName())
                                                                                                                            .append(" must have FQDN configured when n32c is enabled. \n");
                                                                                                                  }

                                                                                                                  if (nfServ.getAddress()
                                                                                                                            .getMultipleIpEndpoint()
                                                                                                                            .size() > 1)
                                                                                                                  {
                                                                                                                      ruleResult.set(false);
                                                                                                                      errMsg.append("'static-nf-service' ")
                                                                                                                            .append(nfServ.getName())
                                                                                                                            .append(" defined under 'static-nf-instance' ")
                                                                                                                            .append(nf.getName())
                                                                                                                            .append(" can only have up to one ip-address.\n");
                                                                                                                  }
                                                                                                                  else
                                                                                                                  {
                                                                                                                      nfServ.getAddress()
                                                                                                                            .getMultipleIpEndpoint()
                                                                                                                            .forEach(ep ->
                                                                                                                            {
                                                                                                                                if (ep.getIpv4Address()
                                                                                                                                      .size() > 1
                                                                                                                                    || ep.getIpv6Address()
                                                                                                                                         .size() > 1)
                                                                                                                                {
                                                                                                                                    ruleResult.set(false);
                                                                                                                                    errMsg.append("'static-nf-service' ")
                                                                                                                                          .append(nfServ.getName())
                                                                                                                                          .append(" defined under 'static-nf-instance' ")
                                                                                                                                          .append(nf.getName())
                                                                                                                                          .append(" can only have up to one ip-address.\n");
                                                                                                                                }
                                                                                                                            });
                                                                                                                  }
                                                                                                              }
                                                                                                          });
                                                                                                 }

                                                                                                 if (staticSeppRefs != null && !staticSeppRefs.isEmpty())
                                                                                                 {
                                                                                                     Utils.getListByNames(nfInstance.getStaticSeppInstanceData(),
                                                                                                                          staticSeppRefs)
                                                                                                          .stream()
                                                                                                          .flatMap(sepps -> sepps.getStaticSeppInstance()
                                                                                                                                 .stream())
                                                                                                          .forEach(sepp ->
                                                                                                          {
                                                                                                              if (!sepp.getAddress()
                                                                                                                       .getScheme()
                                                                                                                       .equals(Scheme.HTTPS))
                                                                                                              {

                                                                                                                  ruleResult.set(false);
                                                                                                                  errMsg.append("'static-sepp-instance' ")
                                                                                                                        .append(sepp.getName())
                                                                                                                        .append(" can only have a 'scheme' of https.\n");
                                                                                                              }

                                                                                                              var fqdn = sepp.getAddress().getFqdn();
                                                                                                              if (fqdn == null || fqdn.isEmpty())
                                                                                                              {
                                                                                                                  ruleResult.set(false);
                                                                                                                  errMsg.append("'static-sepp-instance' ")
                                                                                                                        .append(sepp.getName())
                                                                                                                        .append(" must have FQDN configured when n32c is enabled. \n");
                                                                                                              }

                                                                                                              if (sepp.getAddress()
                                                                                                                      .getMultipleIpEndpoint()
                                                                                                                      .size() > 1)
                                                                                                              {
                                                                                                                  ruleResult.set(false);
                                                                                                                  errMsg.append("'static-sepp-instance' ")
                                                                                                                        .append(sepp.getName())
                                                                                                                        .append(" can only have up to one ip-address.\n");
                                                                                                              }
                                                                                                              else
                                                                                                              {
                                                                                                                  sepp.getAddress()
                                                                                                                      .getMultipleIpEndpoint()
                                                                                                                      .forEach(ep ->
                                                                                                                      {
                                                                                                                          if (ep.getIpv4Address().size() > 1
                                                                                                                              || ep.getIpv6Address().size() > 1)
                                                                                                                          {
                                                                                                                              ruleResult.set(false);
                                                                                                                              errMsg.append("'static-sepp-instance' ")
                                                                                                                                    .append(sepp.getName())
                                                                                                                                    .append(" can only have up to one ip-address.\n");
                                                                                                                          }
                                                                                                                      });
                                                                                                              }
                                                                                                          });
                                                                                                 }
                                                                                             });
                                                                                 }));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_40 validates that the nf-type defined in the condition under the
                                  * topology hiding list of the SEPP configuration for FQDN scrambling must be
                                  * 'not NRF'.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {

                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .stream()
                                               .filter(nfInstance -> nfInstance.getTopologyHiding() != null && !nfInstance.getTopologyHiding().isEmpty())
                                               .flatMap(nf -> nf.getTopologyHiding().stream())
                                               .filter(tph -> tph.getFqdnScrambling() != null)
                                               .forEach(tph ->
                                               {

                                                   var tphExpr = tph.getCondition();
                                                   // predicate expression is mandatory in yang

                                                   isApplicable.set(true);

                                                   var regex = "target-nf-type\\s*!=\\s*'NRF'";
                                                   var pattern = Pattern.compile(regex);
                                                   var matcher = pattern.matcher(tphExpr);
                                                   if (!matcher.find())
                                                   {
                                                       ruleResult.set(false);
                                                       errMsg.append("The condition for FQDN scrambling must be target-nf-type!='NRF'. Condition: ")
                                                             .append(tphExpr)
                                                             .append("\n");

                                                   }

                                               });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_32 validates that the possible combinations of Topology Hiding profiles
                                  * for the same roaming partner for the same nf-type include an Ip Address
                                  * Hiding function, an NRF FQDN mapping and an FQDN scrambling .
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         var result = config.getEricssonSeppSeppFunction().getNfInstance().stream().map(nfInst ->
                                         {
                                             if (nfInst.getTopologyHiding() == null || nfInst.getTopologyHiding().isEmpty())
                                             {
                                                 return Optional.of(Boolean.TRUE);
                                             }

                                             var tphList = nfInst.getTopologyHiding();

                                             return nfInst.getExternalNetwork().stream().map(extNtw -> extNtw.getRoamingPartner().stream().map(rp ->
                                             {
                                                 if ((rp.getTopologyHidingRef() != null && !rp.getTopologyHidingRef().isEmpty())
                                                     || (rp.getTopologyHidingWithAdminState() != null && !rp.getTopologyHidingWithAdminState().isEmpty()))
                                                 {
                                                     var tphListPerRp = new ArrayList<TopologyHiding>();
                                                     isApplicable.set(true);
                                                     if (rp.getTopologyHidingRef() != null)
                                                     {
                                                         tphListPerRp.addAll(Utils.getListByNames(tphList, rp.getTopologyHidingRef()));
                                                     }
                                                     if (rp.getTopologyHidingWithAdminState() != null)
                                                     {
                                                         tphListPerRp.addAll(Utils.getListByNames(tphList,
                                                                                                  rp.getTopologyHidingWithAdminState()
                                                                                                    .stream()
                                                                                                    .map(tphRef -> tphRef.getTphProfileRef())
                                                                                                    .collect(Collectors.toList())));
                                                     }

                                                     return validateTpHrefs(tphListPerRp);
                                                 }
                                                 var tphListPerExtNtw = new ArrayList<TopologyHiding>();
                                                 if ((extNtw.getTopologyHidingRef() != null && !extNtw.getTopologyHidingRef().isEmpty())
                                                     || (extNtw.getTopologyHidingWithAdminState() != null
                                                         && !extNtw.getTopologyHidingWithAdminState().isEmpty()))
                                                 {
                                                     isApplicable.set(true);
                                                     if (extNtw.getTopologyHidingRef() != null)
                                                     {
                                                         tphListPerExtNtw.addAll(Utils.getListByNames(tphList, extNtw.getTopologyHidingRef()));
                                                     }
                                                     if (extNtw.getTopologyHidingWithAdminState() != null)
                                                     {
                                                         tphListPerExtNtw.addAll(Utils.getListByNames(tphList,
                                                                                                      extNtw.getTopologyHidingWithAdminState()
                                                                                                            .stream()
                                                                                                            .map(tphRef -> tphRef.getTphProfileRef())
                                                                                                            .collect(Collectors.toList())));
                                                     }
                                                     return validateTpHrefs(tphListPerExtNtw);
                                                 }
                                                 // there is a tph profile but is not referenced
                                                 return Boolean.TRUE;
                                             }).allMatch(v -> v)).findAny();
                                         }).findAny();

                                         result.ifPresent(b -> b.ifPresent(ruleResult::set));
                                         errMsg.append("There are dublicated topology hiding profiles for the same nf type");
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_33 validates that subnets that refer to different nfs does not overlap.
                                  */
                                 Rule33.get().getRule(),
                                 /**
                                  * Rule_34 validates that the nf-type defined in the condition under the
                                  * 
                                  * topology hiding list of the SEPP configuration for NRF FQDN mapping must be
                                  * 'NRF'.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {

                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .stream()
                                               .filter(nfInstance -> nfInstance.getTopologyHiding() != null && !nfInstance.getTopologyHiding().isEmpty())
                                               .flatMap(nf -> nf.getTopologyHiding().stream())
                                               .filter(tph -> tph.getFqdnMapping() != null)
                                               .forEach(tph ->
                                               {

                                                   var tphExpr = tph.getCondition();
                                                   // predicate expression is mandatory in yang

                                                   isApplicable.set(true);

                                                   var regex = ".*'([^']*)'.*";
                                                   var pattern = Pattern.compile(regex);
                                                   var matcher = pattern.matcher(tphExpr);
                                                   if (matcher.find())
                                                   {
                                                       var extractedNfType = matcher.group(1);

                                                       if (!extractedNfType.equalsIgnoreCase("NRF"))
                                                       {
                                                           ruleResult.set(false);
                                                           errMsg.append("The target-nf-type extracted from the condition for NRF FQDN mapping must be 'NRF'. Targer-nf-type: ")
                                                                 .append(extractedNfType)
                                                                 .append("\n");
                                                       }
                                                   }
                                                   else
                                                   {
                                                       ruleResult.set(false);
                                                       errMsg.append("The value of the target-nf-type within the condition should be enclosed by single quotes.");
                                                   }
                                               });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_35 checks that the variable name of any message data starts with a
                                  * letter and contains only letters, digits and/or underscore
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInst ->
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
                                  * Rule_36 checks if check-san-on-egress is presented in an nf-pool that
                                  * references a roaming-partner the fqdns of all static and discovered
                                  * nf-instances of that nf-pool must be set
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().stream().forEach(nfInstance ->

                                 nfInstance.getNfPool()
                                           .stream()
                                           .filter(nfPool -> nfPool.getCheckSanOnEgress() != null)
                                           .flatMap(nfPool -> nfPool.getNfPoolDiscovery().stream())
                                           .forEach(nfPoolDiscovery ->
                                           {
                                               try
                                               {

                                                   isApplicable.set(true);
                                                   nfPoolDiscovery.getStaticNfInstanceDataRef()
                                                                  .stream()
                                                                  .forEach(staticNfInstanceDataRef -> nfInstance.getStaticNfInstanceData()
                                                                                                                .stream()
                                                                                                                .filter(staticNfInstanceData -> staticNfInstanceData.getName()
                                                                                                                                                                    .equals(staticNfInstanceDataRef))
                                                                                                                .flatMap(staticNfInstanceData -> staticNfInstanceData.getStaticNfInstance()
                                                                                                                                                                     .stream())
                                                                                                                .flatMap(staticNfInstance -> staticNfInstance.getStaticNfService()
                                                                                                                                                             .stream())
                                                                                                                .map(nfService -> nfService.getAddress())
                                                                                                                .map(address -> address.getFqdn())
                                                                                                                .forEach(fqdn ->
                                                                                                                {
                                                                                                                    if (fqdn == null)
                                                                                                                    {
                                                                                                                        throw new NullPointerException();
                                                                                                                    }
                                                                                                                }));

                                               }
                                               catch (NullPointerException e)
                                               {
                                                   ruleResult.set(false);
                                                   errMsg.append(e.getMessage()).append("All fqdns must be set when check-san-on-egress is presented;");

                                               }
                                           })

                                 );

                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_37 checks that parsed variables in message output of action-log in a
                                  * request screening case are valid and placed between double curly braces.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInst ->
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
                                  * Rule_38 checks that the resource field of custom fqdn locator in both
                                  * fqdn-mapping and fqdn-scrambling is a valid regular expression
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         var resourceMapping = config.getEricssonSeppSeppFunction()
                                                                     .getNfInstance()
                                                                     .stream()
                                                                     .filter(nfInstance -> nfInstance.getTopologyHiding() != null
                                                                                           && !nfInstance.getTopologyHiding().isEmpty())
                                                                     .flatMap(nfInstance -> nfInstance.getTopologyHiding().stream())
                                                                     .filter(tph -> tph.getFqdnMapping() != null
                                                                                    && tph.getFqdnMapping().getCustomFqdnLocator() != null)
                                                                     .flatMap(tphProf -> tphProf.getFqdnMapping().getCustomFqdnLocator().stream())
                                                                     .filter(customFqdnLocator -> customFqdnLocator.getResource() != null
                                                                                                  && !customFqdnLocator.getResource().isEmpty())
                                                                     .map(cfql -> cfql.getResource());

                                         var resourceScrambling = config.getEricssonSeppSeppFunction()
                                                                        .getNfInstance()
                                                                        .stream()
                                                                        .filter(nfInstance -> nfInstance.getTopologyHiding() != null
                                                                                              && !nfInstance.getTopologyHiding().isEmpty())
                                                                        .flatMap(nfInstance -> nfInstance.getTopologyHiding().stream())
                                                                        .filter(tph -> tph.getFqdnScrambling() != null
                                                                                       && tph.getFqdnScrambling().getCustomFqdnLocator() != null)
                                                                        .flatMap(tphProf -> tphProf.getFqdnScrambling().getCustomFqdnLocator().stream())
                                                                        .filter(customFqdnLocator -> customFqdnLocator.getResource() != null
                                                                                                     && !customFqdnLocator.getResource().isEmpty())
                                                                        .map(cfql -> cfql.getResource());

                                         Stream.concat(resourceMapping, resourceScrambling).forEach(resource ->
                                         {
                                             isApplicable.set(true);
                                             try
                                             {
                                                 Pattern.compile(resource);
                                             }
                                             catch (PatternSyntaxException e)
                                             {
                                                 ruleResult.set(false);
                                                 errMsg.append("Topology Hiding - Custom FQDN Locator - Resource '")
                                                       .append(resource)
                                                       .append("' is not a valid regular expression.\n");
                                             }
                                         });
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_39 validates that if 3gpp-Sbi-NF-Peer-Info header handling is enabled,
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

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nfInstance ->
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
                                                                    errMsg.append(" nrf-group 'sepp-function nf-instance ")
                                                                          .append(nfInstance.getName())
                                                                          .append(" nrf-group ")
                                                                          .append(nrfGroup.getName())
                                                                          .append("' referenced from 'sepp-function nf-instance ")
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
                                                                    errMsg.append(" nrf-group 'sepp-function nf-instance ")
                                                                          .append(nfInstance.getName())
                                                                          .append(" nrf-group ")
                                                                          .append(nrfGroup.getName())
                                                                          .append("' referenced from 'sepp-function nf-instance ")
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
                                                             errMsg.append(" nrf-group 'sepp-function nf-instance ")
                                                                   .append(nfInstance.getName())
                                                                   .append(" nrf-group ")
                                                                   .append(nrfGroup.getName())
                                                                   .append("' referenced from 'sepp-function nf-instance ")
                                                                   .append(nfInstance.getName())
                                                                   .append(" nrf-service nf-discovery nrf-group-ref'")
                                                                   .append(" or all its members must have attribute 'nf-profile-ref'")
                                                                   .append(", when 3gpp-Sbi-NF-Peer-Info header handling is enabled.\n");

                                                             ruleResult.set(false);
                                                         }
                                                     }
                                                     else
                                                     {
                                                         errMsg.append(" nrf-group 'sepp-function nf-instance ")
                                                               .append(nfInstance.getName())
                                                               .append(" nrf-group' referenced from 'sepp-function nf-instance ")
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
                                  * 
                                  * Rule_40 validates that the regular expressions defined in
                                  * search-replace-regex search regex in the modify-json-body configuration are
                                  * google RE2 compliant
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {

                                         config.getEricssonSeppSeppFunction()
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
                                  * Rule_41 checks that in case an FQDN Scrambling is configured, ScramblingKey
                                  * list must be configured as well
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nf -> nf.getExternalNetwork().stream().forEach(ext ->
                                         {
                                             if (ext.getTopologyHidingWithAdminState() != null && !ext.getTopologyHidingWithAdminState().isEmpty())
                                             {
                                                 ext.getTopologyHidingWithAdminState().forEach(ref ->
                                                 {
                                                     Optional.ofNullable(Utils.getByName(config.getEricssonSeppSeppFunction()
                                                                                               .getNfInstance()
                                                                                               .get(0)
                                                                                               .getTopologyHiding(),
                                                                                         ref.getTphProfileRef()))
                                                             .ifPresent(tph ->
                                                             {
                                                                 if (tph.getFqdnScrambling() != null)
                                                                 {
                                                                     isApplicable.set(true);
                                                                     if (ref.getScramblingKey().isEmpty())
                                                                     {
                                                                         ruleResult.set(false);
                                                                         errMsg.append("ScramblingKey List must be configured for the Fqdn Scrambling Profile under external-network/topology-hiding-with-admin-state \n");
                                                                     }
                                                                 }

                                                             });

                                                 });
                                             }
                                         }));
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nf -> nf.getExternalNetwork().stream().forEach(extNtw ->
                                         {
                                             if (extNtw.getRoamingPartner() != null && !extNtw.getRoamingPartner().isEmpty())
                                             {
                                                 extNtw.getRoamingPartner().stream().forEach(rp ->
                                                 {
                                                     if (rp.getTopologyHidingWithAdminState() != null && !rp.getTopologyHidingWithAdminState().isEmpty())
                                                     {
                                                         rp.getTopologyHidingWithAdminState().forEach(rpRef ->
                                                         {
                                                             Optional.ofNullable(Utils.getByName(config.getEricssonSeppSeppFunction()
                                                                                                       .getNfInstance()
                                                                                                       .get(0)
                                                                                                       .getTopologyHiding(),
                                                                                                 rpRef.getTphProfileRef()))
                                                                     .ifPresent(tph ->
                                                                     {
                                                                         if (tph.getFqdnScrambling() != null)
                                                                         {
                                                                             isApplicable.set(true);
                                                                             if (rpRef.getScramblingKey().isEmpty())
                                                                             {
                                                                                 ruleResult.set(false);
                                                                                 errMsg.append("ScramblingKey List must be configured for the Fqdn Scrambling Profile under roaming-partner/topology-hiding-with-admin-state. \n");
                                                                             }
                                                                         }

                                                                     });
                                                         });
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
                                  * Rule_42 checks that in case an FQDN Scrambling is configured under an
                                  * external network or a RP, then an NRF Mapping profile must be configured on
                                  * the same level as well
                                  */

                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);
                                     var mappingRefEn = new AtomicBoolean(false);
                                     var mappingRefRp = new AtomicBoolean(false);
                                     var scramblingRefEn = new AtomicBoolean(false);
                                     var scramblingRefRp = new AtomicBoolean(false);
                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         var extNets = config.getEricssonSeppSeppFunction()
                                                             .getNfInstance()
                                                             .stream()
                                                             .flatMap(nf -> nf.getExternalNetwork().stream())
                                                             .filter(ext -> ext.getTopologyHidingWithAdminState() != null
                                                                            && !ext.getTopologyHidingWithAdminState().isEmpty())
                                                             .collect(Collectors.toSet());

                                         for (var extNet : extNets)
                                         {
                                             for (var topohidAdminState : extNet.getTopologyHidingWithAdminState())
                                             {
                                                 Optional.ofNullable(Utils.getByName(config.getEricssonSeppSeppFunction()
                                                                                           .getNfInstance()
                                                                                           .get(0)
                                                                                           .getTopologyHiding(),
                                                                                     topohidAdminState.getTphProfileRef()))
                                                         .ifPresent(tph ->
                                                         {
                                                             if (tph.getFqdnScrambling() != null)
                                                             {
                                                                 isApplicable.set(true);
                                                                 scramblingRefEn.set(true);
                                                             }
                                                             else if (tph.getFqdnMapping() != null)
                                                             {
                                                                 mappingRefEn.set(true);
                                                             }
                                                         });
                                             }
                                             if (isApplicable.get() && scramblingRefEn.get() && !mappingRefEn.get())
                                             {
                                                 ruleResult.set(false);
                                                 errMsg.append("Missing nrf fqdn mapping profile for the external network: " + extNet.getName()
                                                               + ".In case an FQDN Scrambling is configured, "
                                                               + "an NRF Mapping profile must be configured on the same level as well \n");
                                             }
                                             mappingRefEn.set(false);
                                             scramblingRefEn.set(false);
                                         }

                                         var rps = config.getEricssonSeppSeppFunction()
                                                         .getNfInstance()
                                                         .stream()
                                                         .flatMap(nf -> nf.getExternalNetwork().stream())
                                                         .flatMap(ext -> ext.getRoamingPartner().stream())
                                                         .filter(rp -> rp.getTopologyHidingWithAdminState() != null
                                                                       && !rp.getTopologyHidingWithAdminState().isEmpty())
                                                         .collect(Collectors.toSet());

                                         for (var rp : rps)
                                         {
                                             for (var topohidAdminState : rp.getTopologyHidingWithAdminState())
                                             {
                                                 Optional.ofNullable(Utils.getByName(config.getEricssonSeppSeppFunction()
                                                                                           .getNfInstance()
                                                                                           .get(0)
                                                                                           .getTopologyHiding(),
                                                                                     topohidAdminState.getTphProfileRef()))
                                                         .ifPresent(tph ->
                                                         {
                                                             if (tph.getFqdnScrambling() != null)
                                                             {
                                                                 isApplicable.set(true);
                                                                 scramblingRefRp.set(true);
                                                             }
                                                             else if (tph.getFqdnMapping() != null)
                                                             {
                                                                 mappingRefRp.set(true);
                                                             }
                                                         });
                                             }

                                             if (isApplicable.get() && scramblingRefRp.get() && !mappingRefRp.get())
                                             {
                                                 ruleResult.set(false);
                                                 errMsg.append("Missing nrf fqdn mapping profile for the RP: " + rp.getName()
                                                               + ".In case an FQDN Scrambling is configured, "
                                                               + "an NRF Mapping profile must be configured on the same level as well \n");
                                             }
                                             mappingRefRp.set(false);
                                             scramblingRefRp.set(false);
                                         }
                                     }
                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_44 checks that for Mapping Profile, perform-action-on-attribute is not
                                  * scramble/de-scramble
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getTopologyHiding()
                                                                                .stream()
                                                                                .filter(tph -> tph.getFqdnMapping() != null)
                                                                                .forEach(tph -> tph.getFqdnMapping()
                                                                                                   .getCustomFqdnLocator()
                                                                                                   .stream()
                                                                                                   .filter(customFqdnLocator -> customFqdnLocator.getRequestMessage() != null)
                                                                                                   .forEach(customFqdnLocator ->
                                                                                                   {

                                                                                                       if (customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInHeader() != null
                                                                                                           && !customFqdnLocator.getRequestMessage()
                                                                                                                                .getSearchInHeader()
                                                                                                                                .isEmpty())
                                                                                                       {
                                                                                                           customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInHeader()
                                                                                                                            .stream()
                                                                                                                            .forEach(h ->
                                                                                                                            {
                                                                                                                                if (h.getPerformActionOnAttribute() != null)
                                                                                                                                {
                                                                                                                                    isApplicable.set(true);
                                                                                                                                    if (h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.DE_SCRAMBLE
                                                                                                                                        || h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.SCRAMBLE)
                                                                                                                                    {
                                                                                                                                        ruleResult.set(false);
                                                                                                                                        errMsg.append("Mapping Profile with custom-fqdn-locator, request-message/search-in-header/perform-action-on-attribute must not be scramble/de-scramble \n");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                       }

                                                                                                       if (customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInQueryParameter() != null
                                                                                                           && !customFqdnLocator.getRequestMessage()
                                                                                                                                .getSearchInQueryParameter()
                                                                                                                                .isEmpty())
                                                                                                       {
                                                                                                           customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInQueryParameter()
                                                                                                                            .stream()
                                                                                                                            .forEach(h ->
                                                                                                                            {
                                                                                                                                if (h.getPerformActionOnAttribute() != null)
                                                                                                                                {
                                                                                                                                    isApplicable.set(true);
                                                                                                                                    if (h.getPerformActionOnAttribute() == SearchInQueryParameter.PerformActionOnAttribute.DE_SCRAMBLE
                                                                                                                                        || h.getPerformActionOnAttribute() == SearchInQueryParameter.PerformActionOnAttribute.SCRAMBLE)
                                                                                                                                    {
                                                                                                                                        ruleResult.set(false);
                                                                                                                                        errMsg.append("Mapping Profile with custom-fqdn-locator, request-message/search-in-query-parameter/perform-action-on-attribute must not be scramble/de-scramble \n");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                       }

                                                                                                       if (customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInMessageBody() != null
                                                                                                           && !customFqdnLocator.getRequestMessage()
                                                                                                                                .getSearchInMessageBody()
                                                                                                                                .isEmpty())
                                                                                                       {
                                                                                                           customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInMessageBody()
                                                                                                                            .stream()
                                                                                                                            .forEach(h ->
                                                                                                                            {
                                                                                                                                if (h.getPerformActionOnAttribute() != null)
                                                                                                                                {
                                                                                                                                    isApplicable.set(true);
                                                                                                                                    if (h.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE
                                                                                                                                        || h.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)
                                                                                                                                    {
                                                                                                                                        ruleResult.set(false);
                                                                                                                                        errMsg.append("Mapping Profile with custom-fqdn-locator, request-message/search-in-message-body/perform-action-on-attribute must not be scramble/de-scramble \n");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                       }

                                                                                                   })));
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getTopologyHiding()
                                                                                .stream()
                                                                                .filter(tph -> tph.getFqdnMapping() != null)
                                                                                .forEach(tph -> tph.getFqdnMapping()
                                                                                                   .getCustomFqdnLocator()
                                                                                                   .stream()
                                                                                                   .filter(customFqdnLocator -> customFqdnLocator.getResponseMessage() != null)
                                                                                                   .forEach(customFqdnLocator ->
                                                                                                   {
                                                                                                       if (customFqdnLocator.getResponseMessage()
                                                                                                                            .getSearchInHeader() != null
                                                                                                           && !customFqdnLocator.getResponseMessage()
                                                                                                                                .getSearchInHeader()
                                                                                                                                .isEmpty())
                                                                                                       {
                                                                                                           customFqdnLocator.getResponseMessage()
                                                                                                                            .getSearchInHeader()
                                                                                                                            .stream()
                                                                                                                            .forEach(h ->
                                                                                                                            {
                                                                                                                                if (h.getPerformActionOnAttribute() != null)
                                                                                                                                {
                                                                                                                                    isApplicable.set(true);
                                                                                                                                    if (h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.DE_SCRAMBLE
                                                                                                                                        || h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.SCRAMBLE)
                                                                                                                                    {
                                                                                                                                        ruleResult.set(false);
                                                                                                                                        errMsg.append("Mapping Profile with custom-fqdn-locator, response-message/search-in-header/perform-action-on-attribute must not be scramble/de-scramble \n");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                       }

                                                                                                       if (customFqdnLocator.getResponseMessage()
                                                                                                                            .getSearchInMessageBody() != null
                                                                                                           && !customFqdnLocator.getResponseMessage()
                                                                                                                                .getSearchInMessageBody()
                                                                                                                                .isEmpty())
                                                                                                       {
                                                                                                           customFqdnLocator.getResponseMessage()
                                                                                                                            .getSearchInMessageBody()
                                                                                                                            .stream()
                                                                                                                            .forEach(h ->
                                                                                                                            {
                                                                                                                                if (h.getPerformActionOnAttribute() != null)
                                                                                                                                {
                                                                                                                                    isApplicable.set(true);
                                                                                                                                    if (h.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE
                                                                                                                                        || h.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)
                                                                                                                                    {
                                                                                                                                        ruleResult.set(false);
                                                                                                                                        errMsg.append("Mapping Profile with custom-fqdn-locator, response-message/search-in-message-body/perform-action-on-attribute must not be scramble/de-scramble \n");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                       }

                                                                                                   })));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_45 checks that for Scrambling Profile, perform-action-on-attribute is
                                  * not map/de-map
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getTopologyHiding()
                                                                                .stream()
                                                                                .filter(tph -> tph.getFqdnScrambling() != null)
                                                                                .forEach(tph -> tph.getFqdnScrambling()
                                                                                                   .getCustomFqdnLocator()
                                                                                                   .stream()
                                                                                                   .filter(customFqdnLocator -> customFqdnLocator.getRequestMessage() != null)
                                                                                                   .forEach(customFqdnLocator ->
                                                                                                   {
                                                                                                       if (customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInHeader() != null
                                                                                                           && !customFqdnLocator.getRequestMessage()
                                                                                                                                .getSearchInHeader()
                                                                                                                                .isEmpty())
                                                                                                       {
                                                                                                           customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInHeader()
                                                                                                                            .stream()
                                                                                                                            .forEach(h ->
                                                                                                                            {
                                                                                                                                if (h.getPerformActionOnAttribute() != null)
                                                                                                                                {
                                                                                                                                    isApplicable.set(true);
                                                                                                                                    if (h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.DE_MAP
                                                                                                                                        || h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.MAP)
                                                                                                                                    {
                                                                                                                                        ruleResult.set(false);
                                                                                                                                        errMsg.append("Scrambling Profile with custom-fqdn-locator, request-message/search-in-header/perform-action-on-attribute must not be map/de-map \n");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                       }

                                                                                                       if (customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInQueryParameter() != null
                                                                                                           && !customFqdnLocator.getRequestMessage()
                                                                                                                                .getSearchInQueryParameter()
                                                                                                                                .isEmpty())
                                                                                                       {
                                                                                                           customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInQueryParameter()
                                                                                                                            .stream()
                                                                                                                            .forEach(h ->
                                                                                                                            {
                                                                                                                                if (h.getPerformActionOnAttribute() != null)
                                                                                                                                {
                                                                                                                                    isApplicable.set(true);
                                                                                                                                    if (h.getPerformActionOnAttribute() == SearchInQueryParameter.PerformActionOnAttribute.DE_MAP
                                                                                                                                        || h.getPerformActionOnAttribute() == SearchInQueryParameter.PerformActionOnAttribute.MAP)
                                                                                                                                    {
                                                                                                                                        ruleResult.set(false);
                                                                                                                                        errMsg.append("Scrambling Profile with custom-fqdn-locator, request-message/search-in-query-parameter/perform-action-on-attribute must not be map/de-map \n");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                       }

                                                                                                       if (customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInMessageBody() != null
                                                                                                           && !customFqdnLocator.getRequestMessage()
                                                                                                                                .getSearchInMessageBody()
                                                                                                                                .isEmpty())
                                                                                                       {
                                                                                                           customFqdnLocator.getRequestMessage()
                                                                                                                            .getSearchInMessageBody()
                                                                                                                            .stream()
                                                                                                                            .forEach(h ->
                                                                                                                            {
                                                                                                                                if (h.getPerformActionOnAttribute() != null)
                                                                                                                                {
                                                                                                                                    isApplicable.set(true);
                                                                                                                                    if (h.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.DE_MAP
                                                                                                                                        || h.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.MAP)
                                                                                                                                    {
                                                                                                                                        ruleResult.set(false);
                                                                                                                                        errMsg.append("Scrambling Profile with custom-fqdn-locator, request-message/search-in-message-body/perform-action-on-attribute must not be map/de-map \n");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                       }

                                                                                                   })));
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .forEach(nfInstance -> nfInstance.getTopologyHiding()
                                                                                .stream()
                                                                                .filter(tph -> tph.getFqdnScrambling() != null)
                                                                                .forEach(tph -> tph.getFqdnScrambling()
                                                                                                   .getCustomFqdnLocator()
                                                                                                   .stream()
                                                                                                   .filter(customFqdnLocator -> customFqdnLocator.getResponseMessage() != null)
                                                                                                   .forEach(customFqdnLocator ->
                                                                                                   {
                                                                                                       if (customFqdnLocator.getResponseMessage()
                                                                                                                            .getSearchInHeader() != null
                                                                                                           && !customFqdnLocator.getResponseMessage()
                                                                                                                                .getSearchInHeader()
                                                                                                                                .isEmpty())
                                                                                                       {
                                                                                                           customFqdnLocator.getResponseMessage()
                                                                                                                            .getSearchInHeader()
                                                                                                                            .stream()
                                                                                                                            .forEach(h ->
                                                                                                                            {
                                                                                                                                if (h.getPerformActionOnAttribute() != null)
                                                                                                                                {
                                                                                                                                    isApplicable.set(true);
                                                                                                                                    if (h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.DE_MAP
                                                                                                                                        || h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.MAP)
                                                                                                                                    {
                                                                                                                                        ruleResult.set(false);
                                                                                                                                        errMsg.append("Scrambling Profile with custom-fqdn-locator, response-message/search-in-header/perform-action-on-attribute must not be map/de-map \n");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                       }
                                                                                                       if (customFqdnLocator.getResponseMessage()
                                                                                                                            .getSearchInMessageBody() != null
                                                                                                           && !customFqdnLocator.getResponseMessage()
                                                                                                                                .getSearchInMessageBody()
                                                                                                                                .isEmpty())
                                                                                                       {
                                                                                                           customFqdnLocator.getResponseMessage()
                                                                                                                            .getSearchInMessageBody()
                                                                                                                            .stream()
                                                                                                                            .forEach(h ->
                                                                                                                            {
                                                                                                                                if (h.getPerformActionOnAttribute() != null)
                                                                                                                                {
                                                                                                                                    isApplicable.set(true);
                                                                                                                                    if (h.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.DE_MAP
                                                                                                                                        || h.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.MAP)
                                                                                                                                    {
                                                                                                                                        ruleResult.set(false);
                                                                                                                                        errMsg.append("Scrambling Profile with custom-fqdn-locator, response-message/search-in-message-body/perform-action-on-attribute must not be map/de-map \n");
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                       }

                                                                                                   })));
                                     }

                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_46 checks that in case an FQDN Scrambling is configured, ScramblingKey
                                  * has at least one activation-date before or the same time as now.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(false);
                                     var errMsg = new StringBuilder(100);
                                     Date now = new Date();

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction().getNfInstance().forEach(nf -> nf.getExternalNetwork().stream().forEach(ext ->
                                         {
                                             if (ext.getTopologyHidingWithAdminState() != null && !ext.getTopologyHidingWithAdminState().isEmpty())
                                             {
                                                 ext.getTopologyHidingWithAdminState()
                                                    .forEach(ref -> Optional.ofNullable(Utils.getByName(config.getEricssonSeppSeppFunction()
                                                                                                              .getNfInstance()
                                                                                                              .get(0)
                                                                                                              .getTopologyHiding(),
                                                                                                        ref.getTphProfileRef()))
                                                                            .ifPresent(tph ->
                                                                            {
                                                                                if (tph.getFqdnScrambling() != null && !ref.getScramblingKey().isEmpty())
                                                                                {
                                                                                    isApplicable.set(true);

                                                                                    ruleResult.set(0 != ref.getScramblingKey()
                                                                                                           .stream()
                                                                                                           .filter(scramblekey -> scramblekey.getActivationDate() != null
                                                                                                                                  && scramblekey.getActivationDate()
                                                                                                                                                .compareTo(now) <= 0)
                                                                                                           .count());
                                                                                }
                                                                            }));

                                                 if (isApplicable.get() && !ruleResult.get())
                                                 {
                                                     errMsg.append("The earlier activation date for scrambling on External Network " + ext.getName()
                                                                   + " is in the future.\n");
                                                     return;
                                                 }

                                                 ext.getRoamingPartner().stream().forEach(rp ->
                                                 {
                                                     if (rp.getTopologyHidingWithAdminState() != null && !rp.getTopologyHidingWithAdminState().isEmpty())
                                                     {
                                                         rp.getTopologyHidingWithAdminState()
                                                           .forEach(rpRef -> Optional.ofNullable(Utils.getByName(config.getEricssonSeppSeppFunction()
                                                                                                                       .getNfInstance()
                                                                                                                       .get(0)
                                                                                                                       .getTopologyHiding(),
                                                                                                                 rpRef.getTphProfileRef()))
                                                                                     .ifPresent(tph ->
                                                                                     {
                                                                                         if (tph.getFqdnScrambling() != null
                                                                                             && !rpRef.getScramblingKey().isEmpty())
                                                                                         {
                                                                                             isApplicable.set(true);
                                                                                             ruleResult.set(0 != rpRef.getScramblingKey()
                                                                                                                      .stream()
                                                                                                                      .filter(scramblekey -> scramblekey.getActivationDate() != null
                                                                                                                                             && scramblekey.getActivationDate()
                                                                                                                                                           .compareTo(now) <= 0)
                                                                                                                      .count());
                                                                                         }

                                                                                     }));
                                                         if (isApplicable.get() && !ruleResult.get())
                                                         {
                                                             errMsg.append("The earlier activation date for scrambling on Roaming Partner " + rp.getName()
                                                                           + " is in the future.\n");
                                                             return;
                                                         }
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
                                  * Rule_47 checks that in case an FQDN Mapping it is not possible to set
                                  * performActionOnAttribute Map when configure message origin to be own network
                                  * and De_Map when configure message origin to be external network .
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(false);
                                     var errMsg = new StringBuilder(200);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .stream()
                                               .filter(nfInstance -> nfInstance.getTopologyHiding() != null && !nfInstance.getTopologyHiding().isEmpty())
                                               .flatMap(nf -> nf.getTopologyHiding().stream())
                                               .filter(tph -> tph.getFqdnMapping() != null)
                                               .filter(cs -> cs.getFqdnMapping().getCustomFqdnLocator() != null)
                                               .flatMap(cl -> cl.getFqdnMapping().getCustomFqdnLocator().stream())
                                               .forEach(customLocator ->
                                               {

                                                   if (customLocator.getMessageOrigin() == MessageOrigin.EXTERNAL_NETWORK)
                                                   {
                                                       if (customLocator.getRequestMessage() != null)
                                                       {
                                                           if (customLocator.getRequestMessage().getSearchInHeader() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInHeader().stream().forEach(h ->
                                                               {
                                                                   if (h.getPerformActionOnAttribute() != null
                                                                       && h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.MAP)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getRequestMessage().getSearchInMessageBody() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInMessageBody().stream().forEach(body ->
                                                               {
                                                                   if (body.getPerformActionOnAttribute() != null
                                                                       && body.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.MAP)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getRequestMessage().getSearchInQueryParameter() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInQueryParameter().stream().forEach(query ->
                                                               {
                                                                   if (query.getPerformActionOnAttribute() != null
                                                                       && query.getPerformActionOnAttribute() == SearchInQueryParameter.PerformActionOnAttribute.MAP)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                       }

                                                       if (customLocator.getResponseMessage() != null)
                                                       {
                                                           if (customLocator.getResponseMessage().getSearchInHeader() != null)
                                                           {
                                                               customLocator.getResponseMessage().getSearchInHeader().stream().forEach(h ->
                                                               {
                                                                   if (h.getPerformActionOnAttribute() != null
                                                                       && h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.DE_MAP)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getResponseMessage().getSearchInMessageBody() != null)
                                                           {
                                                               customLocator.getResponseMessage().getSearchInMessageBody().stream().forEach(body ->
                                                               {
                                                                   if (body.getPerformActionOnAttribute() != null
                                                                       && body.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.DE_MAP)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                       }
                                                       if (ruleResult.get() && isApplicable.get())
                                                       {

                                                           errMsg.append("It is not possible to set performActionOnAttribute Map for requests or De_MAP for responses if the message origin is set to External Network.");
                                                           return;
                                                       }
                                                   }
                                                   else if (customLocator.getMessageOrigin() == MessageOrigin.OWN_NETWORK)
                                                   {
                                                       if (customLocator.getRequestMessage() != null)
                                                       {
                                                           if (customLocator.getRequestMessage().getSearchInHeader() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInHeader().stream().forEach(h ->
                                                               {
                                                                   if (h.getPerformActionOnAttribute() != null
                                                                       && h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.DE_MAP)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getRequestMessage().getSearchInMessageBody() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInMessageBody().stream().forEach(body ->
                                                               {
                                                                   if (body.getPerformActionOnAttribute() != null
                                                                       && body.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.DE_MAP)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getRequestMessage().getSearchInQueryParameter() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInQueryParameter().stream().forEach(query ->
                                                               {
                                                                   if (query.getPerformActionOnAttribute() != null
                                                                       && query.getPerformActionOnAttribute() == SearchInQueryParameter.PerformActionOnAttribute.DE_MAP)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                       }

                                                       if (customLocator.getResponseMessage() != null)
                                                       {
                                                           if (customLocator.getResponseMessage().getSearchInHeader() != null)
                                                           {
                                                               customLocator.getResponseMessage().getSearchInHeader().stream().forEach(h ->
                                                               {
                                                                   if (h.getPerformActionOnAttribute() != null
                                                                       && h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.MAP)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getResponseMessage().getSearchInMessageBody() != null)
                                                           {
                                                               customLocator.getResponseMessage().getSearchInMessageBody().stream().forEach(body ->
                                                               {
                                                                   if (body.getPerformActionOnAttribute() != null
                                                                       && body.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.MAP)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                       }
                                                       if (ruleResult.get() && isApplicable.get())
                                                       {

                                                           errMsg.append("It is not possible to set performActionOnAttribute De_Map for requests or Map for responses if the message origin is set to Own Network.");
                                                           return;
                                                       }
                                                   }

                                               });
                                     }
                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(!ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_48 checks that in case an FQDN Scrambling it is not possible to set
                                  * performActionOnAttribute Scramble when configure message origin to be own
                                  * network and De_Scramble when configure message origin to be external network
                                  * .
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(false);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         config.getEricssonSeppSeppFunction()
                                               .getNfInstance()
                                               .stream()
                                               .filter(nfInstance -> nfInstance.getTopologyHiding() != null && !nfInstance.getTopologyHiding().isEmpty())
                                               .flatMap(nf -> nf.getTopologyHiding().stream())
                                               .filter(tph -> tph.getFqdnScrambling() != null)
                                               .filter(cs -> cs.getFqdnScrambling().getCustomFqdnLocator() != null)
                                               .flatMap(cl -> cl.getFqdnScrambling().getCustomFqdnLocator().stream())
                                               .forEach(customLocator ->
                                               {
                                                   if (customLocator.getMessageOrigin() == MessageOrigin.EXTERNAL_NETWORK)
                                                   {
                                                       if (customLocator.getRequestMessage() != null)
                                                       {
                                                           if (customLocator.getRequestMessage().getSearchInHeader() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInHeader().stream().forEach(h ->
                                                               {
                                                                   if (h.getPerformActionOnAttribute() != null
                                                                       && h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.SCRAMBLE)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getRequestMessage().getSearchInMessageBody() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInMessageBody().stream().forEach(body ->
                                                               {
                                                                   if (body.getPerformActionOnAttribute() != null
                                                                       && body.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getRequestMessage().getSearchInQueryParameter() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInQueryParameter().stream().forEach(query ->
                                                               {
                                                                   if (query.getPerformActionOnAttribute() != null
                                                                       && query.getPerformActionOnAttribute() == SearchInQueryParameter.PerformActionOnAttribute.SCRAMBLE)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                       }

                                                       if (customLocator.getResponseMessage() != null)
                                                       {
                                                           if (customLocator.getResponseMessage().getSearchInHeader() != null)
                                                           {
                                                               customLocator.getResponseMessage().getSearchInHeader().stream().forEach(h ->
                                                               {
                                                                   if (h.getPerformActionOnAttribute() != null
                                                                       && h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.DE_SCRAMBLE)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getResponseMessage().getSearchInMessageBody() != null)
                                                           {
                                                               customLocator.getResponseMessage().getSearchInMessageBody().stream().forEach(body ->
                                                               {
                                                                   if (body.getPerformActionOnAttribute() != null
                                                                       && body.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                       }
                                                       if (ruleResult.get())
                                                       {

                                                           errMsg.append("It is not possible to set performActionOnAttribute Scramble for requests or De_Scrable for responses when the message origin is set to External Network");
                                                           return;
                                                       }
                                                   }
                                                   else if (customLocator.getMessageOrigin() == MessageOrigin.OWN_NETWORK)
                                                   {
                                                       if (customLocator.getRequestMessage() != null)
                                                       {
                                                           if (customLocator.getRequestMessage().getSearchInHeader() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInHeader().stream().forEach(h ->
                                                               {
                                                                   if (h.getPerformActionOnAttribute() != null
                                                                       && h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.DE_SCRAMBLE)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getRequestMessage().getSearchInMessageBody() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInMessageBody().stream().forEach(body ->
                                                               {
                                                                   if (body.getPerformActionOnAttribute() != null
                                                                       && body.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.DE_SCRAMBLE)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getRequestMessage().getSearchInQueryParameter() != null)
                                                           {
                                                               customLocator.getRequestMessage().getSearchInQueryParameter().stream().forEach(query ->
                                                               {
                                                                   if (query.getPerformActionOnAttribute() != null
                                                                       && query.getPerformActionOnAttribute() == SearchInQueryParameter.PerformActionOnAttribute.DE_SCRAMBLE)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                       }

                                                       if (customLocator.getResponseMessage() != null)
                                                       {
                                                           if (customLocator.getResponseMessage().getSearchInHeader() != null)
                                                           {
                                                               customLocator.getResponseMessage().getSearchInHeader().stream().forEach(h ->
                                                               {
                                                                   if (h.getPerformActionOnAttribute() != null
                                                                       && h.getPerformActionOnAttribute() == SearchInHeader.PerformActionOnAttribute.SCRAMBLE)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                           if (customLocator.getResponseMessage().getSearchInMessageBody() != null)
                                                           {
                                                               customLocator.getResponseMessage().getSearchInMessageBody().stream().forEach(body ->
                                                               {
                                                                   if (body.getPerformActionOnAttribute() != null
                                                                       && body.getPerformActionOnAttribute() == SearchInMessageBody.PerformActionOnAttribute.SCRAMBLE)
                                                                   {
                                                                       ruleResult.set(true);
                                                                       isApplicable.set(true);
                                                                   }
                                                               });
                                                           }
                                                       }
                                                       if (ruleResult.get())
                                                       {

                                                           errMsg.append("It is not possible to set performActionOnAttribute De_Scramble for requests or Scrable for responses when the message origin is set to Own Network");
                                                           return;
                                                       }
                                                   }
                                               });
                                     }
                                     if (isApplicable.get())
                                         return Single.just(new RuleResult(!ruleResult.get(), errMsg.toString()));

                                     return Single.just(new RuleResult(true, RULE_NOT_APPLICABLE));
                                 },
                                 /**
                                  * Rule_49 check whether the resources configured in Yang are also defined via
                                  * Helm parameters.
                                  */
                                 Rule49.get().getRule(),
                                 /**
                                  * Rule_50 validates that the api-prefix per static-nf-service complies with the
                                  * format according to 3gpp standards.
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         var pchar = "(?:[a-zA-Z0-9\\-._~!$&'()*+,;=:@]|%[0-9A-F]{2})";
                                         var regex = "/(?:" + pchar + "+(?:/" + pchar + "*)*)?";

                                         config.getEricssonSeppSeppFunction()
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
                                                           errMsg.append(String.format("'api-prefix': %s defined in 'static-nf-service': %s of 'static-nf-instance': %s is not in a valid format according to 3gpp standards%n",
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
                                 /**
                                  * Rule_51 checks that the resource field of firewall in request validate
                                  * service operations contains valid regular expressions
                                  */
                                 config ->
                                 {
                                     var isApplicable = new AtomicBoolean(false);
                                     var ruleResult = new AtomicBoolean(true);
                                     var errMsg = new StringBuilder(100);

                                     if (config != null && config.getEricssonSeppSeppFunction() != null)
                                     {
                                         var resourceMappingAdd = config.getEricssonSeppSeppFunction()
                                                                        .getNfInstance()
                                                                        .stream()
                                                                        .filter(nfInstance -> nfInstance.getFirewallProfile() != null
                                                                                              && !nfInstance.getFirewallProfile().isEmpty())
                                                                        .flatMap(nfInstance -> nfInstance.getFirewallProfile().stream())
                                                                        .filter(fw -> fw.getRequest() != null
                                                                                      && fw.getRequest().getValidateServiceOperation() != null
                                                                                      && fw.getRequest()
                                                                                           .getValidateServiceOperation()
                                                                                           .getAdditionalAllowedOperations() != null
                                                                                      && !fw.getRequest()
                                                                                            .getValidateServiceOperation()
                                                                                            .getAdditionalAllowedOperations()
                                                                                            .isEmpty())
                                                                        .flatMap(fw1 -> fw1.getRequest()
                                                                                           .getValidateServiceOperation()
                                                                                           .getAdditionalAllowedOperations()
                                                                                           .stream())
                                                                        .map(res -> res.getResource())
                                                                        .flatMap(List::stream);

                                         var resourceMappingRm = config.getEricssonSeppSeppFunction()
                                                                       .getNfInstance()
                                                                       .stream()
                                                                       .filter(nfInstance -> nfInstance.getFirewallProfile() != null
                                                                                             && !nfInstance.getFirewallProfile().isEmpty())
                                                                       .flatMap(nfInstance -> nfInstance.getFirewallProfile().stream())
                                                                       .filter(fw -> fw.getRequest() != null
                                                                                     && fw.getRequest().getValidateServiceOperation() != null
                                                                                     && fw.getRequest()
                                                                                          .getValidateServiceOperation()
                                                                                          .getRemovedDefaultOperations() != null
                                                                                     && !fw.getRequest()
                                                                                           .getValidateServiceOperation()
                                                                                           .getRemovedDefaultOperations()
                                                                                           .isEmpty())
                                                                       .flatMap(fw1 -> fw1.getRequest()
                                                                                          .getValidateServiceOperation()
                                                                                          .getRemovedDefaultOperations()
                                                                                          .stream())
                                                                       .map(res -> res.getResource())
                                                                       .flatMap(List::stream);

                                         Stream.concat(resourceMappingAdd, resourceMappingRm).forEach(resource ->
                                         {
                                             isApplicable.set(true);
                                             try
                                             {
                                                 Pattern.compile(resource);
                                             }
                                             catch (PatternSyntaxException e)
                                             {
                                                 ruleResult.set(false);
                                                 errMsg.append("Firewall Profile- Validate service operations - Resource '")
                                                       .append(resource)
                                                       .append("' is not a valid regular expression.\n");
                                             }
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

                                     if (config != null && config.getEricssonSeppSeppFunction() != null && GRLEnabled == false)
                                     {
                                         config.getEricssonSeppSeppFunction()
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

                                 });
    }

    public final List<Rule<EricssonSepp>> getRules()
    {
        return this.rulesList;
    }

    private static String displayIndex(String msg,
                                       int line,
                                       int pos)
    {
        var mystr = new StringBuilder(100);
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

    private static boolean validateJsonPointer(String text)
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
     * 
     * @param tphList
     * @return A Boolean value describing whether the topology-hiding list contains
     *         multiple profiles with the same condition. Returns false if there are
     *         such elements in the list, otherwise returns true
     */
    private static Boolean validateTpHrefs(List<TopologyHiding> tphList)
    {
        // key:nf-type within the condition, value:list of topology hiding function
        Map<String, List<String>> condTphFuncMap = new HashMap<>();
        tphList.forEach(tph ->
        {

            var regex = ".*'([^']*)'.*";
            var pattern = Pattern.compile(regex);
            var matcher = pattern.matcher(tph.getCondition());

            if (matcher.find())
            {
                var nfType = matcher.group(1);
                if (tph.getIpAddressHiding() != null)
                {
                    condTphFuncMap.computeIfAbsent(nfType, k -> new ArrayList<>()).add(IP_HIDING);
                }

                if (tph.getPseudoSearchResult() != null)
                {
                    condTphFuncMap.computeIfAbsent(nfType, k -> new ArrayList<>()).add(PSEUDO_SEARCH_RESULT);
                }
                if (tph.getFqdnMapping() != null)
                {
                    condTphFuncMap.computeIfAbsent(nfType, k -> new ArrayList<>()).add(FQDN_MAPPING);
                }
                if (tph.getFqdnScrambling() != null)
                {
                    condTphFuncMap.computeIfAbsent(nfType, k -> new ArrayList<>()).add(FQDN_SCRAMBLING);
                }
            }
        });

        // false meaning there are duplicated entries for an nf-type
        var hasDuplicated = condTphFuncMap.entrySet().stream().map(entry -> entry.getValue().size() > 1).allMatch(v -> !v);
        if (!hasDuplicated)
        {
            return condTphFuncMap.entrySet()
                                 .stream()
                                 .filter(entry -> entry.getValue().size() > 1)
                                 .allMatch(v -> ((v.getValue().size() == 2) && (v.getKey().equalsIgnoreCase("NRF"))
                                                 && !(v.getValue().get(0).equals(v.getValue().get(1))))
                                                || ((v.getValue().size() == 3) && (v.getKey().equalsIgnoreCase("NRF"))
                                                    && !(v.getValue().get(0).equals(v.getValue().get(1))) && !(v.getValue().get(0).equals(v.getValue().get(2)))
                                                    && !(v.getValue().get(1).equals(v.getValue().get(2)))));

        }

        return hasDuplicated;
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
                        errMsg.append("Opening braces that are not closed found at index: " + start + " in action: " + actionName + ".\n");
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