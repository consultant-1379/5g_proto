/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 22, 2019
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.worker;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ericsson.esc.bsf.openapi.model.DiscoveryQuery;
import com.ericsson.esc.bsf.openapi.model.PcfBinding;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface NBsfManagementService
{
    Single<RegisterResult> register(PcfBinding binding,
                                    int ttlConfig);

    Single<DeregisterResult> deregister(UUID bindingId);

    Single<DiscoveryResult> discovery(DiscoveryQuery query);

    Completable init();

    Completable run();

    Completable stop();

    final class RegisterResult
    {
        private final PcfBinding pcfBinding;
        private final UUID bindingId;

        @Override
        public String toString()
        {
            return "RegisterResult [pcfBinding=" + pcfBinding + ", bindingId=" + bindingId + "]";
        }

        public RegisterResult(PcfBinding pcfBinding,
                              UUID bindingId)
        {
            this.pcfBinding = pcfBinding;
            this.bindingId = bindingId;
        }

        public PcfBinding getPcfBinding()
        {
            return this.pcfBinding;
        }

        public UUID getBindingId()
        {
            return this.bindingId;
        }

    }

    final class DiscoveryResult
    {

        public enum Status
        {
            OK,              // Single binding found
            NOT_FOUND,       // No binding found
            REJECT_MULTIPLE, // Multiple bindings found, rejected
            OK_MULTIPLE,     // Multiple bindings found, resolved
            TOO_MANY         // Too many bindings found
        }

        final Status result;
        final Optional<DiscoveredPcfBinding> pcfBinding;
        final List<StalePcfBinding> staleBindings;

        private DiscoveryResult(Status status,
                                Optional<DiscoveredPcfBinding> pcfBinding)
        {
            this.result = status;
            this.pcfBinding = pcfBinding;
            this.staleBindings = Collections.emptyList();
        }

        private DiscoveryResult(Status status,
                                Optional<DiscoveredPcfBinding> pcfBinding,
                                List<StalePcfBinding> staleBindings)
        {
            this.result = status;
            this.pcfBinding = pcfBinding;
            this.staleBindings = staleBindings;
        }

        public static DiscoveryResult notFound()
        {
            return new DiscoveryResult(Status.NOT_FOUND, Optional.empty());
        }

        public static DiscoveryResult notFound(List<StalePcfBinding> staleBindings)
        {
            return new DiscoveryResult(Status.NOT_FOUND, Optional.empty(), staleBindings);
        }

        public static DiscoveryResult rejectMultiple()
        {
            return new DiscoveryResult(Status.REJECT_MULTIPLE, Optional.empty());
        }

        public static DiscoveryResult found(DiscoveredPcfBinding pcfBinding)
        {
            return new DiscoveryResult(Status.OK, Optional.of(pcfBinding));
        }

        public static DiscoveryResult okMultiple(DiscoveredPcfBinding pcfBinding)
        {
            return new DiscoveryResult(Status.OK_MULTIPLE, Optional.of(pcfBinding));
        }

        public static DiscoveryResult okMultiple(DiscoveredPcfBinding pcfBinding,
                                                 List<StalePcfBinding> staleBindings)
        {
            return new DiscoveryResult(Status.OK_MULTIPLE, Optional.of(pcfBinding), staleBindings);
        }

        public static DiscoveryResult tooMany()
        {
            return new DiscoveryResult(Status.TOO_MANY, Optional.empty());
        }

        public Status getResult()
        {
            return result;
        }

        /**
         * @return the staleBindings
         */
        public List<StalePcfBinding> getStaleBindings()
        {
            return staleBindings;
        }

        public Optional<DiscoveredPcfBinding> getPcfBinding()
        {
            return pcfBinding;
        }

        @Override
        public String toString()
        {
            final var builder = new StringBuilder();
            builder.append("DiscoveryResult [result=");
            builder.append(result);
            builder.append(", pcfBinding=");
            builder.append(pcfBinding);
            builder.append("]");
            return builder.toString();
        }

    }

    enum DeregisterResult
    {
        OK,
        NOT_FOUND
    }

}
