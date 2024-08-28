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
 * Created on: Feb 4, 2019
 *     Author: xchrfar
 */

package com.ericsson.esc.bsf.openapi.model;

import java.util.List;
import java.util.Optional;

import com.ericsson.esc.lib.InvalidParam;
import com.ericsson.esc.lib.ValidationException;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface DiscoveryQuery
{
    ObjectMapper mapper = Jackson.om();

    /**
     * @Throws IllegalArgumentException
     * @param rp
     * @return
     */
    static DiscoveryQuery fromQueryParameters(io.vertx.ext.web.api.RequestParameters rp)
    {
        try
        {
            final var ueAddress = UeAddress.fromQueryParameters(rp);
            final var dnn = Optional.ofNullable(rp.queryParameter("dnn")).map(x -> x.getString());
            final var gpsi = Optional.ofNullable(rp.queryParameter("gpsi")).map(x -> x.getString());
            final var supi = Optional.ofNullable(rp.queryParameter("supi")).map(x -> x.getString());
            final var snssai = Optional.ofNullable(rp.queryParameter("snssai")).map(x -> x.getString()).map(snssaiStr ->
            {
                try
                {
                    return mapper.readValue(snssaiStr, Snssai.class);
                }
                catch (JsonProcessingException ex)
                {
                    if (ex.getCause() != null && ex.getCause() instanceof RuntimeException)
                    {
                        throw (RuntimeException) ex.getCause();
                    }
                    else
                    {
                        throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "snssai", "Invalid S-NSSAI.", ex);
                    }
                }
                catch (Exception e)
                {
                    throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "snssai", "Invalid S-NSSAI.", e);
                }
            });

            if (supi.isPresent() && supi.get().isEmpty())
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "supi", "Invalid supi");
            }

            if (gpsi.isPresent() && gpsi.get().isEmpty())
            {
                throw new ValidationException(ValidationException.ErrorType.SYNTAX_ERROR_OPTIONAL, "gpsi", "Invalid gpsi");
            }

            // Semantic checks
            if (!ueAddress.isPresent())
            {
                final String errorMsg = "One of ipv4Addr, ipv6Prefix or macAddr48 shall be present as query parameter";
                throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_MISSING_PARAM,
                                              List.of(new InvalidParam("ipv4Addr", errorMsg),
                                                      new InvalidParam("ipv6Prefix", errorMsg),
                                                      new InvalidParam("macAddr48", errorMsg)));
            }
            else
            {
                if (ueAddress.get().getIpv4Addr().isPresent() && ueAddress.get().getIpv6Prefix().isPresent())
                {
                    throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_WRONG_PARAM, "ipv4Addr", "Cannot be combined with ipv6Prefix");
                }
            }

            // UE Address is present
            if (snssai.isPresent() && (!dnn.isPresent() || !ueAddress.get().getIpv4Addr().isPresent()))
            {
                throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_WRONG_PARAM, "snssai", "snssai requires ipv4Addr and dnn");
            }

            if (gpsi.isPresent() && supi.isPresent())
            {
                throw new ValidationException(ValidationException.ErrorType.SEMANTIC_ERROR_WRONG_PARAM, "gpsi", "gpsi cannot be combined with supi");
            }

            if (!gpsi.isPresent() && !supi.isPresent() && !dnn.isPresent())
            {
                return new UeAddr(ueAddress.get());
            }

            // UE address AND ( gpsi OR supi OR dnn ) are present

            return gpsi.isPresent() ? //
                                    dnn.isPresent() ? //
                                                    snssai.isPresent() ? //
                                                                       new UeAddrGpsiDnnSnssai(ueAddress.get(), gpsi.get(), dnn.get(), snssai.get()) : //
                                                                       new UeAddrGpsiDnn(ueAddress.get(), gpsi.get(), dnn.get())
                                                    : //
                                                    new UeAddrGpsi(ueAddress.get(), gpsi.get())
                                    : supi.isPresent() ? //
                                                       dnn.isPresent() ? //
                                                                       snssai.isPresent() ? //
                                                                                          new UeAddrSupiDnnSnssai(ueAddress.get(),
                                                                                                                  supi.get(),
                                                                                                                  dnn.get(),
                                                                                                                  snssai.get())
                                                                                          : //
                                                                                          new UeAddrSupiDnn(ueAddress.get(), supi.get(), dnn.get())
                                                                       : //
                                                                       new UeAddrSupi(ueAddress.get(), supi.get())
                                                       : // no gpsi nor supi, thus dnn is be present
                                                       snssai.isPresent() ? //
                                                                          new UeAddrDnnSnssai(ueAddress.get(), dnn.get(), snssai.get()) : //
                                                                          new UeAddrDnn(ueAddress.get(), dnn.get());
        }
        catch (ValidationException ve) // NOPMD by xgeoant on 4/3/19, 4:28 PM
        {
            ve.setQueryParameter(true);
            throw ve;
        }
    }

    interface Visitor
    {
        void visit(DiscoveryQuery.UeAddr query);

        void visit(DiscoveryQuery.UeAddrSupi query);

        void visit(DiscoveryQuery.UeAddrGpsi query);

        void visit(DiscoveryQuery.UeAddrDnn query);

        void visit(DiscoveryQuery.UeAddrDnnSnssai query);

        void visit(DiscoveryQuery.UeAddrSupiDnn query);

        void visit(DiscoveryQuery.UeAddrGpsiDnn query);

        void visit(DiscoveryQuery.UeAddrSupiDnnSnssai query);

        void visit(DiscoveryQuery.UeAddrGpsiDnnSnssai query);

    }

    void accept(Visitor visitor);

    final class UeAddr implements DiscoveryQuery
    {
        private final UeAddress ueAddress;

        public UeAddress getUeAddress()
        {
            return ueAddress;
        }

        public UeAddr(UeAddress ueAddress)
        {
            this.ueAddress = ueAddress;
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        @Override
        public String toString()
        {
            return "UeAddr [ueAddress=" + ueAddress + "]";
        }

    }

    final class UeAddrSupi implements DiscoveryQuery
    {
        private final UeAddress ueAddress;
        private final String supi;

        public UeAddrSupi(UeAddress ueAddress,
                          String supi)
        {
            this.ueAddress = ueAddress;
            this.supi = supi;
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        public UeAddress getUeAddress()
        {
            return ueAddress;
        }

        public String getSupi()
        {
            return supi;
        }
    }

    final class UeAddrSupiDnn implements DiscoveryQuery
    {
        private final UeAddress ueAddress;
        private final String supi;
        private final String dnn;

        public UeAddrSupiDnn(UeAddress ueAddress,
                             String supi,
                             String dnn)
        {
            this.ueAddress = ueAddress;
            this.supi = supi;
            this.dnn = dnn;
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        public UeAddress getUeAddress()
        {
            return ueAddress;
        }

        public String getSupi()
        {
            return supi;
        }

        public String getDnn()
        {
            return dnn;
        }
    }

    final class UeAddrSupiDnnSnssai implements DiscoveryQuery
    {
        private final UeAddress ueAddress;
        private final String supi;
        private final String dnn;
        private final Snssai snssai;

        public UeAddrSupiDnnSnssai(UeAddress ueAddress,
                                   String supi,
                                   String dnn,
                                   Snssai snssai)
        {
            this.ueAddress = ueAddress;
            this.supi = supi;
            this.dnn = dnn;
            this.snssai = snssai;
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        public UeAddress getUeAddress()
        {
            return ueAddress;
        }

        public String getSupi()
        {
            return supi;
        }

        public String getDnn()
        {
            return dnn;
        }

        public Snssai getSnssai()
        {
            return this.snssai;
        }
    }

    final class UeAddrDnn implements DiscoveryQuery
    {
        private final UeAddress ueAddress;
        private final String dnn;

        public UeAddrDnn(UeAddress ueAddress,
                         String dnn)
        {
            this.ueAddress = ueAddress;
            this.dnn = dnn;
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        public UeAddress getUeAddress()
        {
            return ueAddress;
        }

        public String getDnn()
        {
            return dnn;
        }
    }

    final class UeAddrDnnSnssai implements DiscoveryQuery
    {
        private final UeAddress ueAddress;
        private final String dnn;
        private final Snssai snssai;

        public UeAddrDnnSnssai(UeAddress ueAddress,
                               String dnn,
                               Snssai snssai)
        {
            this.ueAddress = ueAddress;
            this.dnn = dnn;
            this.snssai = snssai;
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        public UeAddress getUeAddress()
        {
            return ueAddress;
        }

        public String getDnn()
        {
            return dnn;
        }

        public Snssai getSnssai()
        {
            return snssai;
        }

        @Override
        public String toString()
        {
            return "UeAddrDnnSnssai [ueAddress=" + ueAddress + ", dnn=" + dnn + ", snssai=" + snssai + "]";
        }
    }

    final class UeAddrGpsi implements DiscoveryQuery
    {
        private final UeAddress ueAddress;
        private final String gpsi;

        public UeAddrGpsi(UeAddress ueAddress,
                          String gpsi)
        {
            this.ueAddress = ueAddress;
            this.gpsi = gpsi;
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        public UeAddress getUeAddress()
        {
            return ueAddress;
        }

        public String getGpsi()
        {
            return gpsi;
        }
    }

    final class UeAddrGpsiDnn implements DiscoveryQuery
    {
        private final UeAddress ueAddress;
        private final String gpsi;
        private final String dnn;

        public UeAddrGpsiDnn(UeAddress ueAddress,
                             String gpsi,
                             String dnn)
        {
            this.ueAddress = ueAddress;
            this.gpsi = gpsi;
            this.dnn = dnn;
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        public UeAddress getUeAddress()
        {
            return ueAddress;
        }

        public String getGpsi()
        {
            return gpsi;
        }

        public String getDnn()
        {
            return dnn;
        }
    }

    final class UeAddrGpsiDnnSnssai implements DiscoveryQuery
    {
        private final UeAddress ueAddress;
        private final String gpsi;
        private final String dnn;
        private final Snssai snssai;

        public UeAddrGpsiDnnSnssai(UeAddress ueAddress,
                                   String gpsi,
                                   String dnn,
                                   Snssai snssai)
        {
            this.ueAddress = ueAddress;
            this.gpsi = gpsi;
            this.dnn = dnn;
            this.snssai = snssai;
        }

        @Override
        public void accept(Visitor visitor)
        {
            visitor.visit(this);
        }

        public UeAddress getUeAddress()
        {
            return ueAddress;
        }

        public String getGpsi()
        {
            return gpsi;
        }

        public String getDnn()
        {
            return dnn;
        }

        public Snssai getSnssai()
        {
            return snssai;
        }
    }
}
