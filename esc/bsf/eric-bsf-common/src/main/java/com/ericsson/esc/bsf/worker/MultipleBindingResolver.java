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
 * Created on: Sep 6, 2022
 *     Author: ekilagg
 */
package com.ericsson.esc.bsf.worker;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddr;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrDnn;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrDnnSnssai;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrGpsi;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrGpsiDnn;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrGpsiDnnSnssai;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrSupi;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrSupiDnn;
import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery.UeAddrSupiDnnSnssai;
import com.ericsson.esc.bsf.openapi.model.UeAddress;
import com.ericsson.sc.bsf.model.Combination;
import com.ericsson.sc.bsf.model.Combination_;
import com.ericsson.sc.bsf.model.DiameterLookup;
import com.ericsson.sc.bsf.model.HttpLookup;

/**
 * Decides if a a database query matches a MultipleBindingResolution CM rule
 */
public final class MultipleBindingResolver
{
    private final List<Set<QueryParams>> queriesMostRecent;
    private boolean applyAllQueries = false;
    private boolean deleteMultipleBindings = true;

    /**
     * Create a resolver for HTTP triggered database queries
     * 
     * @param httpLookup The rule to use
     */
    public MultipleBindingResolver(HttpLookup httpLookup)
    {
        switch (httpLookup.getResolutionType())
        {
            case REJECT:
                this.queriesMostRecent = List.of();
                break;
            case MOST_RECENT:
                this.queriesMostRecent = List.of();
                this.applyAllQueries = true;
                break;
            case MOST_RECENT_CONDITIONAL:
                this.queriesMostRecent = httpLookup.getQueryParameterCombination()
                                                   .stream() //
                                                   .map(qParam -> qParam.getCombination()
                                                                        .stream()
                                                                        .map(QueryParams::fromRule)
                                                                        .collect(Collectors.toUnmodifiableSet()))
                                                   .collect(Collectors.toUnmodifiableList());
                break;
            default:
                throw new IllegalArgumentException("Unexpected HTTP resolution type: " + httpLookup.getResolutionType());
        }

        this.deleteMultipleBindings = httpLookup.getDeletionUponLookup() == null ? true : httpLookup.getDeletionUponLookup();
    }

    /**
     * Create a resolver for Diameter triggered database queries
     * 
     * @param diameterLookup The rule to use
     */
    public MultipleBindingResolver(DiameterLookup diameterLookup)
    {

        switch (diameterLookup.getResolutionType())
        {
            case REJECT:
                this.queriesMostRecent = List.of();
                break;
            case MOST_RECENT:
                this.queriesMostRecent = List.of();
                this.applyAllQueries = true;
                break;
            case MOST_RECENT_CONDITIONAL:
                this.queriesMostRecent = diameterLookup.getAvpCombination()
                                                       .stream() //
                                                       .map(qParam -> qParam.getCombination()
                                                                            .stream()
                                                                            .map(QueryParams::fromRule)
                                                                            .collect(Collectors.toUnmodifiableSet()))
                                                       .collect(Collectors.toUnmodifiableList());
                break;
            default:
                throw new IllegalArgumentException("Unexpected HTTP resolution type: " + diameterLookup.getResolutionType());
        }

        this.deleteMultipleBindings = diameterLookup.getDeletionUponLookup() == null ? true : diameterLookup.getDeletionUponLookup();
    }

    /**
     * 
     * @param query The database query to evaluate
     * @return True if the given query matches the configured rule
     */
    public boolean isQueryApplicable(DiscoveryQuery query)
    {
        final var visitor = new Visitor();
        query.accept(visitor);

        return visitor.result;
    }

    public boolean getDeleteMultipleBindings()
    {
        return this.deleteMultipleBindings;
    }

    private final class Visitor implements DiscoveryQuery.Visitor
    {
        private boolean result;

        @Override
        public void visit(UeAddr query)
        {
            final var querySet = EnumSet.copyOf(processUeAddress(query.getUeAddress()));

            this.result = processQuery(querySet);
        }

        @Override
        public void visit(UeAddrDnn query)
        {
            final var querySet = EnumSet.copyOf(processUeAddress(query.getUeAddress()));
            querySet.add(QueryParams.DNN);

            this.result = processQuery(querySet);
        }

        @Override
        public void visit(UeAddrDnnSnssai query)
        {
            final var querySet = EnumSet.copyOf(processUeAddress(query.getUeAddress()));
            querySet.add(QueryParams.DNN);
            querySet.add(QueryParams.SNSSAI);

            this.result = processQuery(querySet);
        }

        @Override
        public void visit(UeAddrSupi query)
        {
            final var querySet = EnumSet.copyOf(processUeAddress(query.getUeAddress()));
            querySet.add(QueryParams.SUPI);

            this.result = processQuery(querySet);
        }

        @Override
        public void visit(UeAddrSupiDnn query)
        {
            final var querySet = EnumSet.copyOf(processUeAddress(query.getUeAddress()));
            querySet.add(QueryParams.SUPI);
            querySet.add(QueryParams.DNN);

            this.result = processQuery(querySet);
        }

        @Override
        public void visit(UeAddrSupiDnnSnssai query)
        {
            final var querySet = EnumSet.copyOf(processUeAddress(query.getUeAddress()));
            querySet.add(QueryParams.SUPI);
            querySet.add(QueryParams.DNN);
            querySet.add(QueryParams.SNSSAI);

            this.result = processQuery(querySet);
        }

        @Override
        public void visit(UeAddrGpsi query)
        {
            final var querySet = EnumSet.copyOf(processUeAddress(query.getUeAddress()));
            querySet.add(QueryParams.GPSI);

            this.result = processQuery(querySet);
        }

        @Override
        public void visit(UeAddrGpsiDnn query)
        {
            final var querySet = EnumSet.copyOf(processUeAddress(query.getUeAddress()));
            querySet.add(QueryParams.GPSI);
            querySet.add(QueryParams.DNN);

            this.result = processQuery(querySet);
        }

        @Override
        public void visit(UeAddrGpsiDnnSnssai query)
        {
            final var querySet = EnumSet.copyOf(processUeAddress(query.getUeAddress()));
            querySet.add(QueryParams.GPSI);
            querySet.add(QueryParams.DNN);
            querySet.add(QueryParams.SNSSAI);

            this.result = processQuery(querySet);
        }

        private boolean processQuery(Set<QueryParams> input)
        {
            return applyAllQueries || queriesMostRecent.stream() //
                                                       .anyMatch(input::containsAll);
        }

        private EnumSet<QueryParams> processUeAddress(UeAddress ueAddr)
        {
            switch (ueAddr.getType())
            {
                case INET4:
                    return ueAddr.getIpDomain().isEmpty() ? EnumSet.of(QueryParams.IPV4_ADDR) : EnumSet.of(QueryParams.IPV4_ADDR, QueryParams.IP_DOMAIN);
                case INET6:
                    return EnumSet.of(QueryParams.IPV6_PREFIX);
                case INET4_6:
                    return ueAddr.getIpDomain().isEmpty() ? EnumSet.of(QueryParams.IPV4_ADDR, QueryParams.IPV6_PREFIX)
                                                          : EnumSet.of(QueryParams.IPV4_ADDR, QueryParams.IP_DOMAIN, QueryParams.IPV6_PREFIX);
                case MAC:
                    return EnumSet.of(QueryParams.MAC_ADDR48);

                default:
                    throw new IllegalArgumentException("Unknown query type: " + ueAddr.getType());
            }
        }

    }

    private enum QueryParams
    {
        IPV4_ADDR,
        IPV6_PREFIX,
        MAC_ADDR48,
        IP_DOMAIN,
        SUPI,
        GPSI,
        DNN,
        SNSSAI;

        public static QueryParams fromRule(Combination httpComb)
        {
            switch (httpComb)
            {
                case IPV_4_ADDR:
                    return QueryParams.IPV4_ADDR;
                case IPV_6_PREFIX:
                    return QueryParams.IPV6_PREFIX;
                case MAC_ADDR_48:
                    return QueryParams.MAC_ADDR48;
                case IP_DOMAIN:
                    return QueryParams.IP_DOMAIN;
                case SUPI:
                    return QueryParams.SUPI;
                case GPSI:
                    return QueryParams.GPSI;
                case DNN:
                    return QueryParams.DNN;
                case SNSSAI:
                    return QueryParams.SNSSAI;
                default:
                    throw new IllegalArgumentException("Unexpected HTTP Combination value: " + httpComb);
            }
        }

        public static QueryParams fromRule(Combination_ diameterComb)
        {
            switch (diameterComb)
            {
                case IPV_4:
                    return QueryParams.IPV4_ADDR;
                case IPV_6:
                    return QueryParams.IPV6_PREFIX;
                case IP_DOMAIN:
                    return QueryParams.IP_DOMAIN;
                default:
                    throw new IllegalArgumentException("Unexpected Diameter Combination value: " + diameterComb);
            }
        }
    }

    @Override
    public String toString()
    {
        var builder = new StringBuilder();
        builder.append("MultipleBindingResolver [queriesMostRecent=");
        builder.append(queriesMostRecent);
        builder.append(", applyAllQueries=");
        builder.append(applyAllQueries);
        builder.append("]");
        return builder.toString();
    }
}
