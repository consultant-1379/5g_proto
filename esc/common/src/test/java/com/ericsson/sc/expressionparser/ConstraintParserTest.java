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
 * Created on: Jan 20, 2021
 *     Author: enocakh
 */
package com.ericsson.sc.expressionparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.ericsson.sc.glue.IfTypedNfInstance;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.nfm.model.ServiceName;
import com.ericsson.sc.nfm.model.Transport;
import com.ericsson.sc.sepp.manager.NfMatchConditionParser;
import com.ericsson.sc.sepp.manager.ExpressionLexer;
import com.ericsson.sc.sepp.model.Address;
import com.ericsson.sc.sepp.model.MultipleIpEndpoint;
import com.ericsson.sc.sepp.model.StaticNfInstance;
import com.ericsson.sc.sepp.model.StaticNfService;

class ConstraintParserTest
{
// Test Parsing functionality
    @Tag("integration")
    @Test
    void testExistsTrue()
    {
        String text = "nfdata.locality exists";
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);
        var metaDataMap = new HashedMap<String, List<String>>();
        metaDataMap.put("locality", List.of("west"));
        assertTrue(NfConditionParser.executeExpression(metaDataMap, parser));
    }

    @Tag("integration")
    @Test
    void testExistsFalse()
    {
        String text = "nfdata.locality exists";
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);
        var metaDataMap = new HashedMap<String, List<String>>();
        metaDataMap.put("not_locality", List.of("west"));
        assertTrue(!NfConditionParser.executeExpression(metaDataMap, parser));
    }

    @Tag("integration")
    @Test
    void testEqOp()
    {
        String text = "nfdata.locality == 'north'";
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);
        var metaDataMap = new HashedMap<String, List<String>>();
        metaDataMap.put("locality", List.of("north"));
        metaDataMap.put("scp-domain", List.of("north"));
        assertTrue(NfConditionParser.executeExpression(metaDataMap, parser));
    }

    @Tag("integration")
    @Test
    void testOrOp()
    {
        String text = "nfdata.locality == 'west' or nfdata.scp-domain == 'east'";
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);
        var metaDataMap = new HashedMap<String, List<String>>();
        metaDataMap.put("locality", List.of("east"));
        metaDataMap.put("scp-domain", List.of("east"));
        assertTrue(NfConditionParser.executeExpression(metaDataMap, parser));
    }

    @Tag("integration")
    @Test
    void testAndOp()
    {
        String text = "nfdata.locality == 'west' and nfdata.scp-domain == 'east'";
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);
        var metaDataMap = new HashedMap<String, List<String>>();
        metaDataMap.put("locality", List.of("west"));
        metaDataMap.put("scp-domain", List.of("east"));
        assertTrue(NfConditionParser.executeExpression(metaDataMap, parser));
    }

    @Tag("integration")
    @Test
    void testEqNotWellKnown()
    {
        String text = "nfdata['key'] == 'value'";
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);
        var metaDataMap = new HashedMap<String, List<String>>();
        metaDataMap.put("key", List.of("value"));
        assertTrue(NfConditionParser.executeExpression(metaDataMap, parser));
    }

    @Tag("integration")
    @Test
    void testNot()
    {
        String text = "not nfdata['key'] exists";
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);
        var metaDataMap = new HashedMap<String, List<String>>();
        assertTrue(NfConditionParser.executeExpression(metaDataMap, parser));
    }

    @Tag("integration")
    @Test
    void testComplicated()
    {
        String text = "(not nfdata['key'] exists and nfdata.locality == 'west') or nfdata.scp-domain == 'east'";
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);
        var metaDataMap = new HashedMap<String, List<String>>();
        metaDataMap.put("locality", List.of("west"));
        assertTrue(NfConditionParser.executeExpression(metaDataMap, parser));
    }

    @Tag("integration")
    @Test
    void testEqList()
    {
        String text = "nfdata.scp-domain == 'east'";
        var lexer = new ExpressionLexer(CharStreams.fromString(text));
        var tokens = new CommonTokenStream(lexer);
        var parser = new NfMatchConditionParser(tokens);
        var metaDataMap = new HashedMap<String, List<String>>();
        metaDataMap.put("scp-domain", List.of("north", "east", "west"));
        assertTrue(NfConditionParser.executeExpression(metaDataMap, parser));
    }

    // Test filtering functionality
    @Tag("integration")
    @Test
    void testEmptyExpression()
    {
        String text = "";
        Set<String> dummySet = new HashSet<>();
        var res = NfConditionParser.parse(text, createStreamInstanceEmpty(2, 2), dummySet).toArray();
        assertEquals(4, res.length);
    }

    @Tag("integration")
    @Test
    void testFilterOneNf()
    {
        String text = "nfdata.capacity == '101'";
        Set<String> dummySet = new HashSet<>();
        var res = NfConditionParser.parse(text, createStreamInstance(2, 2), dummySet).toArray();
        assertEquals(1, res.length);
    }

    @Tag("integration")
    @Test
    void testFilterAllNfs()
    {
        String text = "nfdata.set-id exists and nfdata.transport == 'tcp'";
        Set<String> dummySet = new HashSet<>();
        var res = NfConditionParser.parse(text, createStreamInstance(2, 2), dummySet).toArray();
        assertEquals(2 * 2, res.length);
    }

    @Tag("integration")
    @Test
    void testFilterNoneNfs()
    {
        String text = "nfdata.nf-set-id exists";
        Set<String> dummySet = new HashSet<>();
        var res = NfConditionParser.parse(text, createStreamInstance(2, 2), dummySet).toArray();
        assertEquals(0, res.length);
    }

    @Tag("integration")
    @Test
    void testFilterAddress()
    {
        String text = "nfdata.ipv4-address == 'ipv40_0aa' ";
        Set<String> dummySet = new HashSet<>();
        var res = NfConditionParser.parse(text, createStreamInstance(2, 2), dummySet).toArray();
        assertEquals(1, res.length);
    }

    @Tag("integration")
    @Test
    void testMapCreationNullAttributes()
    {
        String text = "nfdata.nf-set-id exists";
        Set<String> dummySet = new HashSet<>();
        var res = NfConditionParser.parse(text, createStreamInstance(2, 2), dummySet).toArray();
        assertEquals(0, res.length);
    }

    @Tag("integration")
    @Test
    void testMapCreationNullAddress()
    {
        String text = "nfdata.nf-set-id exists";
        Set<String> dummySet = new HashSet<>();
        var res = NfConditionParser.parse(text, createStreamInstance(2, 2), dummySet).toArray();
        assertEquals(0, res.length);
    }

    private Stream<IfTypedNfInstance> createStreamInstance(int numInstances,
                                                           int numServices)
    {
        int port = 80;

        List<StaticNfInstance> nfInstList = new ArrayList<>();
        for (int j = 0; j < numInstances; j++)
        {
            List<StaticNfService> nfServList = new ArrayList<>();
            String nfInstanceID = "instid" + j;
            String nfLocality = "locality" + j;
            List<String> nfSetId = null;// List.of("nftype" + j, "nftypeb" + j);
            List<String> nfScpDomain = List.of("scp" + j, "scp" + j);
            for (int i = 0; i < numServices; i++)
            {
                String fqdn = "fqdn_" + j + "_" + i;
                String ipv4 = "ipv4" + j + "_" + i;
                String setid = "setid" + j + "_" + i;

                StaticNfService nfServ = new StaticNfService().withName(ServiceName.DEFAULT.value())
                                                              .withSetId(List.of(setid))
                                                              .withCapacity(j * 100 + i)
                                                              .withPriority(j * 100 + i)
                                                              .withAddress(new Address().withFqdn(fqdn)
                                                                                        .withScheme(Scheme.HTTP)
                                                                                        .withMultipleIpEndpoint(Arrays.asList(new MultipleIpEndpoint().withPort(port)
                                                                                                                                                      .withTransport(Transport.TCP)
                                                                                                                                                      .withIpv4Address(Arrays.asList(ipv4
                                                                                                                                                                                     + "aa",
                                                                                                                                                                                     ipv4 + "ab")),
                                                                                                                              new MultipleIpEndpoint().withPort(port)
                                                                                                                                                      .withIpv4Address(Arrays.asList(ipv4
                                                                                                                                                                                     + "ba",
                                                                                                                                                                                     ipv4 + "bb")))));
                nfServList.add(nfServ);
            }
            nfInstList.add(new StaticNfInstance().withName("name" + j)
                                                 .withStaticNfService(nfServList)
                                                 .withLocality(nfLocality)
                                                 .withNfInstanceId(nfInstanceID)
                                                 .withScpDomain(nfScpDomain)
                                                 .withNfSetId(nfSetId));
        }

        return nfInstList.stream().map(c -> (IfTypedNfInstance) c);

    }

    private Stream<IfTypedNfInstance> createStreamInstanceEmpty(int numInstances,
                                                                int numServices)
    {
        List<StaticNfInstance> nfInstList = new ArrayList<>();
        for (int j = 0; j < numInstances; j++)
        {
            List<StaticNfService> nfServList = new ArrayList<>();
            for (int i = 0; i < numServices; i++)
            {
                StaticNfService nfServ = new StaticNfService().withName(ServiceName.DEFAULT.value());
                nfServList.add(nfServ);
            }
            nfInstList.add(new StaticNfInstance().withName("name" + j).withStaticNfService(nfServList));
        }

        return nfInstList.stream().map(c -> (IfTypedNfInstance) c);

    }

    private Stream<IfTypedNfInstance> createStreamInstanceEmptyWithAddress(int numInstances,
                                                                           int numServices)
    {
        List<StaticNfInstance> nfInstList = new ArrayList<>();

        List<StaticNfService> nfServList = new ArrayList<>();

        StaticNfService nfServ = new StaticNfService().withName(ServiceName.DEFAULT.value()).withAddress(new Address());
        nfServList.add(nfServ);
        nfServ = new StaticNfService().withName(ServiceName.DEFAULT.value())
                                      .withAddress(new Address().withScheme(Scheme.HTTP).withMultipleIpEndpoint(Arrays.asList(new MultipleIpEndpoint())));

        nfServList.add(nfServ);
        nfServ = new StaticNfService().withName(ServiceName.DEFAULT.value())
                                      .withAddress(new Address().withScheme(Scheme.HTTP)
                                                                .withMultipleIpEndpoint(Arrays.asList(new MultipleIpEndpoint().withPort(1)
                                                                                                                              .withIpv4Address(Arrays.asList("")))));

        nfServList.add(nfServ);
        nfInstList.add(new StaticNfInstance().withName("name").withStaticNfService(nfServList));

        return nfInstList.stream().map(c -> (IfTypedNfInstance) c);

    }
}
