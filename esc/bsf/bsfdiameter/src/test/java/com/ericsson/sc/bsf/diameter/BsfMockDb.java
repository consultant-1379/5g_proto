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
 * Created on: Apr 12, 2021
 *     Author: echfari
 */
package com.ericsson.sc.bsf.diameter;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.ericsson.esc.bsf.db.MockNbsfManagementService;
import com.ericsson.esc.bsf.db.MockNbsfManagementService.State;
import com.ericsson.esc.bsf.openapi.model.BindingLevel;
import com.ericsson.esc.bsf.openapi.model.DiameterIdentity;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.openapi.model.Snssai;
import com.google.common.net.InetAddresses;

public class BsfMockDb
{
    private final long baseSupi = Long.valueOf("111111111111111");
    private final BigInteger baseIpv4Addr = InetAddresses.toBigInteger(InetAddresses.forString("10.0.0.1"));
    private final int pcfGrouppingFactor;

    private final List<PcfBinding> pcfBindings;
    private final MockNbsfManagementService mockNbsf;

    public BsfMockDb(final int numberOfBindings,
                     final int pcfGrouppingFactor)
    {
        this.pcfGrouppingFactor = pcfGrouppingFactor;
        mockNbsf = new MockNbsfManagementService();
        mockNbsf.setState(State.OK);
        this.pcfBindings = createBindings(numberOfBindings);
        mockNbsf.bulkRegister(this.pcfBindings);
    }

    public MockNbsfManagementService getDb()
    {
        return this.mockNbsf;
    }

    public List<PcfBinding> getBindings()
    {
        return this.pcfBindings;
    }

    public PcfBinding getBinding(int i)
    {
        return this.getBindings().get(i);
    }

    private PcfBinding createPcfBinding(int i)
    {
        final var supi = "imsi-" + baseSupi + i;
        final Inet4Address ipv4Addr = InetAddresses.fromIPv4BigInteger(baseIpv4Addr.add(BigInteger.valueOf(i)));
        final var ipDomain = "ericsson.se";
        final var dnn = "testDnn";
        final com.ericsson.esc.bsf.openapi.model.Snssai snssai = Snssai.create(2, "DEAD56");
        final var pcfId = UUID.randomUUID();
        final var pcfFqdn = String.format("pcf%s.3gppnetwork.org", i);
        final var pcfDiamHost = new DiameterIdentity(String.format("diamHost%s.ericsson.se", i));
        final var pcfDiamRealm = new DiameterIdentity("diamRealm.se");
        final var bindLevel = new BindingLevel("NF_INSTANCE");
        final var pcfSetId = String.format("setId%s", i % pcfGrouppingFactor);

        return PcfBinding.create(supi,
                                 null,
                                 ipv4Addr,
                                 null,
                                 ipDomain,
                                 null,
                                 dnn,
                                 pcfFqdn,
                                 null,
                                 pcfDiamHost,
                                 pcfDiamRealm,
                                 snssai,
                                 pcfId,
                                 null,
                                 null,
                                 null,
                                 null,
                                 pcfSetId,
                                 bindLevel);
    }

    private List<PcfBinding> createBindings(int i)
    {
        return IntStream.range(0, i).mapToObj(this::createPcfBinding).collect(Collectors.toList());
    }

}
