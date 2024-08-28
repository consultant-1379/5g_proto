/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 28, 2019
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.db;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.esc.bsf.openapi.model.BaseUeAddress;
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
import com.ericsson.esc.bsf.openapi.model.PcfBinding;
import com.ericsson.esc.bsf.worker.DiscoveredPcfBinding;
import com.ericsson.esc.bsf.worker.NBsfManagementService;

import io.reactivex.Completable;
import io.reactivex.Single;

public class MockNbsfManagementService implements NBsfManagementService
{
    private static final Logger log = LoggerFactory.getLogger(MockNbsfManagementService.class);

    public enum State
    {
        OK,
        ERROR,
        NEVER_FOUND,
        MULTIPLE_FOUND
    }

    private AtomicReference<State> state = new AtomicReference<>(State.OK);

    private final ConcurrentMap<UUID, DiscoveredPcfBinding> bindings = new ConcurrentHashMap<>();
    private final ConcurrentMap<BaseUeAddress, DiscoveredPcfBinding> bindingsByUeAddress = new ConcurrentHashMap<>();

    public State getState()
    {
        return this.state.get();
    }

    public void setState(State state)
    {
        this.state.set(state);
    }

    public void bulkRegister(Iterable<PcfBinding> bindings)
    {
        bindings.forEach(this::registerSync);
    }

    @Override
    public Single<RegisterResult> register(final PcfBinding binding,
                                           int ttlConfig)
    {
        if (getState() != State.OK)
            throw new IllegalStateException("unexpected register() error");
        return Single.fromCallable(() -> registerSync(binding));
    }

    @Override
    public Single<DeregisterResult> deregister(final UUID bindingId)
    {
        if (getState() != State.OK)
            throw new IllegalStateException("unexpected deregister() error");
        return Single.fromCallable(() -> deregisterSync(bindingId));
    }

    @Override
    public Single<DiscoveryResult> discovery(DiscoveryQuery query)
    {
        return Single.fromCallable(() ->
        {
            switch (getState())
            {
                case ERROR:
                    throw new IllegalStateException("unexpected discovery() error");
                case NEVER_FOUND:
                    return DiscoveryResult.notFound();
                case MULTIPLE_FOUND:
                    return DiscoveryResult.rejectMultiple();
                default:
                    break;
            }

            final var visitor = new DiscoveryQuery.Visitor()
            {
                Optional<DiscoveredPcfBinding> result = Optional.empty();

                @Override
                public void visit(UeAddr query)
                {
                    final var addr = BaseUeAddress.create(query.getUeAddress());
                    result = Optional.ofNullable(bindingsByUeAddress.get(addr));
                    log.info("Query for address {} result {}", addr, result);
                }

                @Override
                public void visit(UeAddrSupi query)
                {
                    // Not implemented yet
                }

                @Override
                public void visit(UeAddrGpsi query)
                {
                    // Not implemented yet
                }

                @Override
                public void visit(UeAddrDnn query)
                {
                }

                @Override
                public void visit(UeAddrDnnSnssai query)
                {
                    // Not implemented yet
                }

                @Override
                public void visit(UeAddrSupiDnn query)
                {
                    // Not implemented yet
                }

                @Override
                public void visit(UeAddrGpsiDnn query)
                {
                    // Not implemented yet
                }

                @Override
                public void visit(UeAddrSupiDnnSnssai query)
                {
                    // Not implemented yet
                }

                @Override
                public void visit(UeAddrGpsiDnnSnssai query)
                {
                    // Not implemented yet
                }

            };
            query.accept(visitor);
            return visitor.result.map(DiscoveryResult::found).orElse(DiscoveryResult.notFound());
        });
    }

    @Override
    public Completable init()
    {
        return Completable.complete();
    }

    @Override
    public Completable run()
    {
        return Completable.never();
    }

    private RegisterResult registerSync(PcfBinding binding)
    {
        final var bindingId = UUID.randomUUID();
        final var ueAddress = BaseUeAddress.create(binding.getIpv4Addr(), binding.getIpv6Prefix(), binding.getIpDomain(), binding.getMacAddr48());

        final var discBinding = new DiscoveredPcfBinding(binding, bindingId, Instant.now());
        bindings.put(bindingId, discBinding);
        bindingsByUeAddress.put(ueAddress, discBinding);
        return new RegisterResult(binding, bindingId);
    }

    private DeregisterResult deregisterSync(UUID bindingId)
    {
        final var binding = this.bindings.get(bindingId);
        if (binding != null)
        {
            final var ueAddress = BaseUeAddress.create(binding.getIpv4Addr(), binding.getIpv6Prefix(), binding.getIpDomain(), binding.getMacAddr48());
            this.bindingsByUeAddress.remove(ueAddress);
        }
        return DeregisterResult.OK;
    }

    @Override
    public String toString()
    {
        final var builder = new StringBuilder();
        builder.append("MockNbsfManagementService [state=");
        builder.append(state);
        builder.append("bindingsByUeAddress=");
        builder.append(bindingsByUeAddress);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public Completable stop()
    {
        return Completable.complete();
    }

}