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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfAddress;
import com.ericsson.sc.glue.IfMultipleIpEndpoint;
import com.ericsson.sc.glue.IfTypedNfAddressProperties;
import com.ericsson.sc.glue.IfTypedSeppInstance;
import com.ericsson.sc.proxy.endpoints.RoundRobinEndpointCollector;
import com.ericsson.sc.proxyal.expressionparser.Operator;
import com.ericsson.sc.scp.model.Address;
import com.ericsson.sc.scp.model.MultipleIpEndpoint;
import com.ericsson.sc.scp.model.StaticSeppInstance;
import com.ericsson.sc.sepp.manager.SeppMatchConditionParser;
import com.ericsson.sc.sepp.manager.SeppMatchConditionParserBaseVisitor;
import com.ericsson.sc.sepp.manager.ExpressionLexer;
import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.exceptions.BadConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeppConditionParser extends SeppMatchConditionParserBaseVisitor<ReturnValue>
{

    private static final Logger log = LoggerFactory.getLogger(SeppConditionParser.class);

    private Map<String, List<String>> metadataMap;

    public SeppConditionParser(Map<String, List<String>> metadataMap)
    {
        super();
        this.metadataMap = metadataMap;
    }

    @Override
    public ReturnValue visitRoot(SeppMatchConditionParser.RootContext ctx)
    {
        return visit(ctx.expression());
    }

    @Override
    public ReturnValue visitNfDataKnown(SeppMatchConditionParser.NfDataKnownContext ctx)
    {
        return new ReturnValue(this.metadataMap.get(ctx.nfdata_fields().getText()));
    }

    @Override
    public ReturnValue visitNfDataSepp(SeppMatchConditionParser.NfDataSeppContext ctx)
    {
        return new ReturnValue(this.metadataMap.get(ctx.nfdata_sepp_fields().getText()));
    }

    @Override
    public ReturnValue visitNfDataIpEndpoint(SeppMatchConditionParser.NfDataIpEndpointContext ctx)
    {
        return new ReturnValue(this.metadataMap.get(ctx.nfdata_endpoint_fields().getText()));
    }

    @Override
    public ReturnValue visitNfDataUnknown(SeppMatchConditionParser.NfDataUnknownContext ctx)
    {
        String nfDataType;

        // Remove quotes
        nfDataType = ctx.key_val().key.getText();
        nfDataType = nfDataType.substring(1, nfDataType.length() - 1);

        return new ReturnValue(this.metadataMap.get(nfDataType));
    }

    @Override
    public ReturnValue visitLiteralStr(SeppMatchConditionParser.LiteralStrContext ctx)
    {
        var str = ctx.STRING().getText();
        return new ReturnValue(str.substring(1, str.length() - 1));
    }

    @Override
    public ReturnValue visitLiteralBool(SeppMatchConditionParser.LiteralBoolContext ctx)
    {
        return new ReturnValue(Boolean.valueOf(ctx.BOOLEAN().getText().toLowerCase()));
    }

    @Override
    public ReturnValue visitBinaryExpr(SeppMatchConditionParser.BinaryExprContext ctx)
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
    public ReturnValue visitParentheticExpr(SeppMatchConditionParser.ParentheticExprContext ctx)
    {
        return visit(ctx.expr);
    }

    @Override
    public ReturnValue visitUnaryExpr(SeppMatchConditionParser.UnaryExprContext ctx)
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

    public static List<IfTypedNfAddressProperties> filterStaticSepps(String text,
                                                                     Stream<IfTypedSeppInstance> streamInstances)
    {

        List<IfTypedNfAddressProperties> seppCompl = new ArrayList<>();

        if (text == null || text.isBlank())
        {
            streamInstances.forEach(instance ->
            {
                seppCompl.add(instance);
            });

            return seppCompl;
        }

        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new SeppMatchConditionParser(tokens);

        streamInstances.forEach(instance ->
        {
            var nfDataMap = convertNfdataToMap(instance);
            var matchesConstraint = executeExpression(nfDataMap, parser);

            if (Boolean.TRUE.equals(matchesConstraint))
                seppCompl.add(instance);

        });

        return seppCompl;
    }

    private static Map<String, List<String>> convertNfdataToMap(IfTypedSeppInstance instance)

    {
        Map<String, List<String>> nfDataMap = new HashMap<>();

        if (instance.getNfInstanceId() != null)
        {
            nfDataMap.put("nf-instance-id", List.of(instance.getNfInstanceId()));
        }

        if (instance.getNfType() != null)
        {
            nfDataMap.put("nf-type", List.of(instance.getNfType()));
        }

        if (instance.getLocality() != null)
        {
            nfDataMap.put("locality", List.of(instance.getLocality()));
        }

        nfDataMap.put("nf-set-id", instance.getNfSetId());
        nfDataMap.put("scp-domain", instance.getScpDomain());

        if (instance.getCapacity() != null)
        {
            nfDataMap.put("capacity", List.of(instance.getCapacity().toString()));
        }

        if (instance.getPriority() != null)
        {
            nfDataMap.put("priority", List.of(instance.getPriority().toString()));
        }

        if (instance.getAddress() != null)
        {
            if (instance.getAddress().getFqdn() != null)
            {
                nfDataMap.put("fqdn", List.of(instance.getAddress().getFqdn()));
            }

            if (instance.getAddress().getScheme() != null)
            {
                nfDataMap.put("scheme", List.of(instance.getAddress().getScheme().toString()));
            }

            if (instance.getAddress().getMultipleIpEndpoint() != null && !instance.getAddress().getMultipleIpEndpoint().isEmpty())
            {
                var ipv4addresses = new ArrayList<String>();
                var ipv6addresses = new ArrayList<String>();
                var ports = new ArrayList<String>();
                var transports = new ArrayList<String>();

                instance.getAddress().getMultipleIpEndpoint().forEach(ipEndpoint ->
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
                                            SeppMatchConditionParser parser)
    {
        parser.reset();
        ReturnValue res = new SeppConditionParser(metadataMap).visit(parser.root());

        return res != null && res.getBoolVal().orElse(false);
    }
}
