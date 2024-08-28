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
 * Created on: Jan 14, 2021
 *     Author: enocakh
 */

package com.ericsson.sc.expressionparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfNfInstance;
import com.ericsson.sc.glue.IfStaticScpInstance;
import com.ericsson.sc.glue.IfStaticScpInstanceDatum;
import com.ericsson.sc.glue.IfTypedNfAddressProperties;
import com.ericsson.sc.glue.IfTypedNfService;
import com.ericsson.sc.glue.IfTypedScpDomainInfo;
import com.ericsson.sc.glue.IfTypedScpInstance;
import com.ericsson.sc.glue.IfTypedSeppInstance;
import com.ericsson.sc.proxy.ProxyConstants.METADATA;
import com.ericsson.sc.proxyal.expressionparser.Operator;
import com.ericsson.sc.scp.model.Address;
import com.ericsson.sc.scp.model.MultipleIpEndpoint;
import com.ericsson.sc.scp.model.StaticScpDomainInfo;
import com.ericsson.sc.scp.model.StaticScpInstance;
import com.ericsson.sc.scp.model.StaticSeppInstance;
import com.ericsson.sc.sepp.manager.ScpMatchConditionParser;
import com.ericsson.sc.sepp.manager.ScpMatchConditionParserBaseVisitor;
import com.ericsson.sc.sepp.manager.ExpressionLexer;
import com.ericsson.sc.sepp.manager.NfMatchConditionParser;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.common.Utils;
import com.ericsson.utilities.exceptions.BadConfigurationException;

public class ScpConditionParser extends ScpMatchConditionParserBaseVisitor<ReturnValue>
{
    private static final Logger log = LoggerFactory.getLogger(ScpConditionParser.class);

    private Map<String, List<String>> metadataMap;

    public ScpConditionParser(Map<String, List<String>> metadataMap)
    {
        super();
        this.metadataMap = metadataMap;
    }

    @Override
    public ReturnValue visitRoot(ScpMatchConditionParser.RootContext ctx)
    {
        return visit(ctx.expression());
    }

//    @Override
//    public ReturnValue visitNfDataKnown(ScpMatchConditionParser.NfDataKnownContext ctx)
//    {
//        return new ReturnValue(this.metadataMap.get(ctx.nfdata_fields().getText()));
//    }

    @Override
    public ReturnValue visitNfDataScp(ScpMatchConditionParser.NfDataScpContext ctx)
    {
        return new ReturnValue(this.metadataMap.get(ctx.nfdata_scp_fields().getText()));
    }

    @Override
    public ReturnValue visitNfDataScpDomainInfo(ScpMatchConditionParser.NfDataScpDomainInfoContext ctx)
    {
        return new ReturnValue(this.metadataMap.get(ctx.nfdata_scp_domain_info_fields().getText()));
    }

    @Override
    public ReturnValue visitNfDataIpEndpoint(ScpMatchConditionParser.NfDataIpEndpointContext ctx)
    {
        return new ReturnValue(this.metadataMap.get(ctx.nfdata_endpoint_fields().getText()));
    }

    @Override
    public ReturnValue visitNfDataUnknown(ScpMatchConditionParser.NfDataUnknownContext ctx)
    {
        String nfDataType;

        // Remove quotes
        nfDataType = ctx.key_val().key.getText();
        nfDataType = nfDataType.substring(1, nfDataType.length() - 1);

        return new ReturnValue(this.metadataMap.get(nfDataType));
    }

    @Override
    public ReturnValue visitLiteralStr(ScpMatchConditionParser.LiteralStrContext ctx)
    {
        var str = ctx.STRING().getText();
        return new ReturnValue(str.substring(1, str.length() - 1));
    }

    @Override
    public ReturnValue visitLiteralBool(ScpMatchConditionParser.LiteralBoolContext ctx)
    {
        return new ReturnValue(Boolean.valueOf(ctx.BOOLEAN().getText().toLowerCase()));
    }

    @Override
    public ReturnValue visitBinaryExpr(ScpMatchConditionParser.BinaryExprContext ctx)
    {
        var operator = Operator.fromString(ctx.op.getText());
        var left = this.visit(ctx.left);
        var right = this.visit(ctx.right);

        if (left.isNull() || right.isNull())
        {
            return new ReturnValue(false);
        }

        if (operator.equals(Operator.CASEINSENSITIVEQUAL))
            left.setCaseInsensitive(true);

        switch (operator)
        {
            case EQUAL:
                return new ReturnValue(left.equals(right));

            case CASEINSENSITIVEQUAL:
                return new ReturnValue(left.equals(right));

            case AND:
                return new ReturnValue(left.getBoolVal().orElseThrow(() -> new BadConfigurationException("Missing left operand for AND operation."))
                                       && right.getBoolVal().orElseThrow(() -> new BadConfigurationException("Missing right operand for AND operation.")));

            case OR:
                return new ReturnValue(left.getBoolVal().orElseThrow(() -> new BadConfigurationException("Missing left operand for OR operation."))
                                       || right.getBoolVal().orElseThrow(() -> new BadConfigurationException("Missing right operand for OR operation.")));

            default:
                throw new BadConfigurationException("Unhandled unsupported operation.");
        }
    }

    @Override
    public ReturnValue visitParentheticExpr(ScpMatchConditionParser.ParentheticExprContext ctx)
    {
        return visit(ctx.expr);
    }

    @Override
    public ReturnValue visitUnaryExpr(ScpMatchConditionParser.UnaryExprContext ctx)
    {
        var operator = Operator.valueOf(ctx.op.getText().toUpperCase());
        var arg = this.visit(ctx.arg);

        switch (operator)
        {
            case EXISTS:
                return new ReturnValue(!arg.isNull());

            case NOT:
                return new ReturnValue(!arg.getBoolVal().orElseThrow(() -> new BadConfigurationException("Missing boolean operand for NOT operation.")));

            default:
                throw new BadConfigurationException("Unhandled unsupported operation.");
        }
    }

    // Filter discovered SCP instances
    public static List<IfTypedScpDomainInfo> filterScpDomains(String text,
                                                              Stream<IfTypedScpInstance> streamInstances)
    {
        List<IfTypedScpDomainInfo> domainInfoList = new ArrayList<>();

        if (text == null || text.isBlank())
        {
            streamInstances.forEach(instance ->
            {
                instance.fetchScpDomainInfo().stream().forEach(domainInfo ->
                {
                    domainInfoList.add(domainInfo);
                });
            });

            return domainInfoList;
        }

        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new ScpMatchConditionParser(tokens);

        streamInstances.forEach(instance ->
        {
            instance.fetchScpDomainInfo().stream().forEach(domainInfo ->
            {
                var nfDataMap = convertScpdataToMap(instance, domainInfo);
                var matchesConstraint = executeExpression(nfDataMap, parser);

                if (Boolean.TRUE.equals(matchesConstraint))
                {
                    domainInfoList.add(domainInfo);
                }
            });
        });

        return domainInfoList;
    }

    // Filter static SCP instances
    public static List<IfTypedNfAddressProperties> filterStaticScps(String text,
                                                                    Stream<IfStaticScpInstance> streamInstances)
    {
        List<IfTypedNfAddressProperties> complList = new ArrayList<>();

        if (text == null || text.isBlank())
        {
            streamInstances.forEach(instance ->
            {
                instance.fetchScpDomainInfo().stream().forEach(domainInfo ->
                {
                    complList.add(domainInfo);
                });

                instance.fetchNfService().stream().forEach(service ->
                {
                    complList.add(service);
                });
            });

            return complList;
        }

        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new ScpMatchConditionParser(tokens);

        streamInstances.forEach(instance ->
        {
            instance.fetchScpDomainInfo().stream().forEach(domainInfo ->
            {
                var nfDataMap = convertScpdataToMap(instance, domainInfo);
                var matchesConstraint = executeExpression(nfDataMap, parser);

                if (Boolean.TRUE.equals(matchesConstraint))
                {
                    complList.add(domainInfo);
                }
            });

            instance.fetchNfService().stream().forEach(service ->
            {
                var nfDataMap = convertScpdataToMap(instance, service);
                var matchesConstraint = executeExpression(nfDataMap, parser);

                if (Boolean.TRUE.equals(matchesConstraint))
                {
                    complList.add(service);
                }
            });
        });

        return complList;
    }

    private static Map<String, List<String>> convertScpdataToMap(IfTypedScpInstance instance,
                                                                 IfTypedNfAddressProperties service)
    {
        Map<String, List<String>> nfDataMap = new HashMap<>();

        // add all data from nf instance
        if (instance.getNfInstanceId() != null)
        {
            nfDataMap.put("nf-instance-id", List.of(instance.getNfInstanceId()));
        }

        if (instance.getNfType() != null)
        {
            nfDataMap.put("nf-type", List.of(instance.getNfType()));
        }

        if (instance.getNfStatus() != null)
        {
            nfDataMap.put("nf-status", List.of(instance.getNfStatus().value()));
        }

        if (instance.getLocality() != null)
        {
            nfDataMap.put("locality", List.of(instance.getLocality()));
        }

        if (service instanceof IfTypedNfService)
        {
            if (((IfTypedNfService) service).getNfServiceId() != null)
            {
                nfDataMap.put("nf-service-id", List.of(((IfTypedNfService) service).getNfServiceId()));
            }
        }

        nfDataMap.put("nf-set-id", instance.getNfSetId());
        nfDataMap.put("scp-domain", (instance.getScpDomain()));

        if (service.getCapacity() != null)
        {
            nfDataMap.put("capacity", List.of(service.getCapacity().toString()));
        }

        if (service.getPriority() != null)
        {
            nfDataMap.put("priority", List.of(service.getPriority().toString()));
        }

        if (service.getAddress() != null)
        {
            if (service.getAddress().getFqdn() != null)
            {
                nfDataMap.put("fqdn", List.of(service.getAddress().getFqdn()));
            }

            if (service.getAddress().getScheme() != null)
            {
                nfDataMap.put("scheme", List.of(service.getAddress().getScheme().toString()));
            }

            if (service.getAddress().getMultipleIpEndpoint() != null && !service.getAddress().getMultipleIpEndpoint().isEmpty())
            {
                var ipv4addresses = new ArrayList<String>();
                var ipv6addresses = new ArrayList<String>();
                var ports = new ArrayList<String>();
                var transports = new ArrayList<String>();

                service.getAddress().getMultipleIpEndpoint().forEach(ipEndpoint ->
                {
                    if (ipEndpoint.getIpv4Address() != null && !ipEndpoint.getIpv4Address().isEmpty())
                    {
                        ipEndpoint.getIpv4Address().forEach(ipv4addresses::add);
                    }

                    if (ipEndpoint.getIpv6Address() != null && !ipEndpoint.getIpv6Address().isEmpty())
                    {
                        ipEndpoint.getIpv6Address().forEach(ipv6addresses::add);
                    }

                    if (ipEndpoint.getPort() != null)
                    {
                        ports.add(ipEndpoint.getPort().toString());
                    }

                    if (ipEndpoint.getTransport() != null)
                    {
                        transports.add(ipEndpoint.getTransport().toString());
                    }
                });

                nfDataMap.put("ipv4-address", ipv4addresses);
                nfDataMap.put("ipv6-address", ipv6addresses);
                nfDataMap.put("port", ports);
                nfDataMap.put("transport", transports);
            }
        }

        return nfDataMap;
    }

    public static Boolean executeExpression(Map<String, List<String>> metadataMap,
                                            ScpMatchConditionParser parser)
    {
        parser.reset();
        ReturnValue res = new ScpConditionParser(metadataMap).visit(parser.root());

        return res != null && res.getBoolVal().orElse(false);
    }
}
