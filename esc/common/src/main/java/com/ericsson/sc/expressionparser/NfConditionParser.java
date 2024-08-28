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
import java.util.Set;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.configutil.CommonConfigUtils;
import com.ericsson.sc.glue.IfTypedNfAddressProperties;
import com.ericsson.sc.glue.IfTypedNfInstance;
import com.ericsson.sc.glue.IfTypedNfService;
import com.ericsson.sc.proxyal.expressionparser.Operator;
import com.ericsson.sc.sepp.manager.NfMatchConditionParser;
import com.ericsson.sc.sepp.manager.NfMatchConditionParserBaseVisitor;
import com.ericsson.sc.sepp.manager.ExpressionLexer;
import com.ericsson.utilities.exceptions.BadConfigurationException;

public class NfConditionParser extends NfMatchConditionParserBaseVisitor<ReturnValue>
{
    private static final Logger log = LoggerFactory.getLogger(NfConditionParser.class);

    private Map<String, List<String>> metadataMap;

    public NfConditionParser(Map<String, List<String>> metadataMap)
    {
        super();
        this.metadataMap = metadataMap;
    }

    @Override
    public ReturnValue visitRoot(NfMatchConditionParser.RootContext ctx)
    {
        return visit(ctx.expression());
    }

    @Override
    public ReturnValue visitNfDataKnown(NfMatchConditionParser.NfDataKnownContext ctx)
    {
        return new ReturnValue(this.metadataMap.get(ctx.nfdata_fields().getText()));
    }

    @Override
    public ReturnValue visitNfDataNf(NfMatchConditionParser.NfDataNfContext ctx)
    {
        return new ReturnValue(this.metadataMap.get(ctx.nfdata_nf_fields().getText()));
    }

    @Override
    public ReturnValue visitNfDataService(NfMatchConditionParser.NfDataServiceContext ctx)
    {
        return new ReturnValue(this.metadataMap.get(ctx.nfdata_service_fields().getText()));
    }

    @Override
    public ReturnValue visitNfDataIpEndpoint(NfMatchConditionParser.NfDataIpEndpointContext ctx)
    {
        return new ReturnValue(this.metadataMap.get(ctx.nfdata_endpoint_fields().getText()));
    }

    @Override
    public ReturnValue visitNfDataUnknown(NfMatchConditionParser.NfDataUnknownContext ctx)
    {
        String nfDataType;

        // Remove quotes
        nfDataType = ctx.key_val().key.getText();
        nfDataType = nfDataType.substring(1, nfDataType.length() - 1);

        return new ReturnValue(this.metadataMap.get(nfDataType));
    }

    @Override
    public ReturnValue visitLiteralStr(NfMatchConditionParser.LiteralStrContext ctx)
    {
        var str = ctx.STRING().getText();
        return new ReturnValue(str.substring(1, str.length() - 1));
    }

    @Override
    public ReturnValue visitLiteralBool(NfMatchConditionParser.LiteralBoolContext ctx)
    {
        return new ReturnValue(Boolean.valueOf(ctx.BOOLEAN().getText().toLowerCase()));
    }

    @Override
    public ReturnValue visitBinaryExpr(NfMatchConditionParser.BinaryExprContext ctx)
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
    public ReturnValue visitParentheticExpr(NfMatchConditionParser.ParentheticExprContext ctx)
    {
        return visit(ctx.expr);
    }

    @Override
    public ReturnValue visitUnaryExpr(NfMatchConditionParser.UnaryExprContext ctx)
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

    public static List<IfTypedNfService> parse(String text,
                                               Stream<IfTypedNfInstance> streamInstances,
                                               Set<String> selectedInstanceNames)
    {
        if (text == null || text.isBlank())
        {
            List<IfTypedNfService> nfServiceList = new ArrayList<>();
            streamInstances.forEach(instance -> instance.fetchNfService().stream().forEach(service ->
            {
                nfServiceList.add(service);
                selectedInstanceNames.add(CommonConfigUtils.getUniqueIdForSvc(instance, service));
            }));
            return nfServiceList;
        }

        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);

        List<IfTypedNfService> nfServiceList = new ArrayList<>();
        streamInstances.forEach(instance -> instance.fetchNfService().stream().forEach(service ->
        {
            var nfDataMap = convertNfdataToMap(instance, service);
            var matchesConstraint = executeExpression(nfDataMap, parser);

            if (Boolean.TRUE.equals(matchesConstraint))
            {
                nfServiceList.add(service);
                selectedInstanceNames.add(CommonConfigUtils.getUniqueIdForSvc(instance, service));
            }
        }));

        return nfServiceList;
    }

    public static List<IfTypedNfAddressProperties> filterNfServices(String text,
                                                                    Stream<IfTypedNfInstance> streamInstances)
    {
        List<IfTypedNfAddressProperties> nfServiceList = new ArrayList<>();

        if (text == null || text.isBlank())
        {

            streamInstances.forEach(instance -> instance.fetchNfService().stream().forEach(service ->
            {
                nfServiceList.add(service);
            }));
            return nfServiceList;
        }

        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);

        streamInstances.forEach(instance -> instance.fetchNfService().stream().forEach(service ->
        {
            var nfDataMap = convertNfdataToMap(instance, service);
            var matchesConstraint = executeExpression(nfDataMap, parser);

            if (Boolean.TRUE.equals(matchesConstraint))
            {
                nfServiceList.add(service);

            }

        }));

        return nfServiceList;
    }

    private static Map<String, List<String>> convertNfdataToMap(IfTypedNfInstance instance,
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

        nfDataMap.put("nf-set-id", instance.getNfSetId());
        nfDataMap.put("scp-domain", (instance.getScpDomain()));

        // add all data from nf service
        if (service.getCapacity() != null)
        {
            nfDataMap.put("capacity", List.of(service.getCapacity().toString()));
        }

        if (service.getPriority() != null)
        {
            nfDataMap.put("priority", List.of(service.getPriority().toString()));
        }

        if (((IfTypedNfService) service).getStatus() != null)
        {
            nfDataMap.put("status", List.of(((IfTypedNfService) service).getStatus().value()));
        }

        if (((IfTypedNfService) service).getNfServiceId() != null)
        {
            nfDataMap.put("nf-service-id", List.of(((IfTypedNfService) service).getNfServiceId()));
        }

        if (((IfTypedNfService) service).getApiPrefix() != null)
        {
            nfDataMap.put("api-prefix", List.of(((IfTypedNfService) service).getApiPrefix()));
        }

        nfDataMap.put("set-id", ((IfTypedNfService) service).getSetId());

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
                                            NfMatchConditionParser parser)
    {
        parser.reset();
        ReturnValue res = new NfConditionParser(metadataMap).visit(parser.root());
        return res != null && res.getBoolVal().orElse(false);
    }
}
