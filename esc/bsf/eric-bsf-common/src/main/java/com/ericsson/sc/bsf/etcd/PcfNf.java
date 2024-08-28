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
 * Created on: Mar 9, 2021
 *     Author: echfari
 */
package com.ericsson.sc.bsf.etcd;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * PCF data received from NRF. Objects of this class can be serialized to JSON.
 * Objects can be created via {@link PcfNf.Builder} or deserialized from JSON
 * Objects are immutable and modification of its contents is not allowed
 */
@JsonInclude(Include.NON_EMPTY)
@JsonDeserialize(builder = PcfNf.Builder.class)
public final class PcfNf
{
    private final UUID nfInstanceId;
    private final String nfStatus;
    private final List<String> nfSetIdList;
    private final String rxDiamHost;
    private final String rxDiamRealm;

    private PcfNf(Builder builder)
    {
        this.nfInstanceId = builder.nfInstanceId;
        this.nfStatus = builder.nfStatus;
        this.nfSetIdList = builder.nfSetIdList != null ? Collections.unmodifiableList(builder.nfSetIdList) : List.of();
        this.rxDiamHost = builder.rxDiamHost;
        this.rxDiamRealm = builder.rxDiamRealm;

        // Validate
        Objects.requireNonNull(nfStatus);
        Objects.requireNonNull(nfInstanceId);
        Objects.requireNonNull(rxDiamHost);
        Objects.requireNonNull(rxDiamRealm);
    }

    /**
     * 
     * @return The non-null NF Instance id
     */
    @JsonGetter
    public UUID getNfInstanceId()
    {
        return nfInstanceId;
    }

    /**
     * 
     * @return The non-null NF status
     */
    @JsonGetter
    public String getNfStatus()
    {
        return nfStatus;
    }

    /**
     * 
     * @return An unmodifiable list with the PCF set ids
     */
    @JsonGetter
    public List<String> getNfSetIdList()
    {
        return nfSetIdList;
    }

    @JsonGetter
    public String getRxDiamHost()
    {
        return rxDiamHost;
    }

    @JsonGetter
    public String getRxDiamRealm()
    {
        return rxDiamRealm;
    }

    /**
     * Create a new Builder for building PcfNf objects
     * 
     * @return The newly created builder
     */
    public static Builder newBuilder()
    {
        return new Builder();
    }

    public static Builder newBuilder(PcfNf pcfNf)
    {
        return new Builder(pcfNf);
    }

    @JsonPOJOBuilder
    public static class Builder
    {
        public Builder()
        {
        }

        public Builder(PcfNf pcfNf)
        {
            this.nfInstanceId = pcfNf.nfInstanceId;
            this.nfSetIdList = pcfNf.nfSetIdList;
            this.nfStatus = pcfNf.nfStatus;
            this.rxDiamHost = pcfNf.rxDiamHost;
            this.rxDiamRealm = pcfNf.rxDiamRealm;
        }

        public Builder withNfInstanceId(UUID nfInstanceId)
        {
            this.nfInstanceId = nfInstanceId;
            return this;
        }

        public Builder withNfStatus(String nfStatus)
        {
            this.nfStatus = nfStatus;
            return this;
        }

        public Builder withNfSetIdList(List<String> nfSetIdList)
        {
            this.nfSetIdList = nfSetIdList;
            return this;
        }

        public Builder withRxDiamHost(String rxDiamHost)
        {
            this.rxDiamHost = rxDiamHost;
            return this;
        }

        public Builder withRxDiamRealm(String rxDiamRealm)
        {
            this.rxDiamRealm = rxDiamRealm;
            return this;
        }

        /**
         * Build PcfNf object
         * 
         * @return The newly built object
         */
        public PcfNf build()
        {
            return new PcfNf(this);
        }

        private UUID nfInstanceId;
        private String nfStatus;
        private List<String> nfSetIdList;
        private String rxDiamHost;
        private String rxDiamRealm;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(nfInstanceId, nfSetIdList, nfStatus, rxDiamHost, rxDiamRealm);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        PcfNf other = (PcfNf) obj;
        return Objects.equals(nfInstanceId, other.nfInstanceId) && Objects.equals(nfSetIdList, other.nfSetIdList) && Objects.equals(nfStatus, other.nfStatus)
               && Objects.equals(rxDiamHost, other.rxDiamHost) && Objects.equals(rxDiamRealm, other.rxDiamRealm);
    }

    @Override
    public String toString()
    {
        var builder2 = new StringBuilder();
        builder2.append("PcfNf [nfInstanceId=");
        builder2.append(nfInstanceId);
        builder2.append(", nfStatus=");
        builder2.append(nfStatus);
        builder2.append(", nfSetIdList=");
        builder2.append(nfSetIdList);
        builder2.append(", rxDiamHost=");
        builder2.append(rxDiamHost);
        builder2.append(", rxDiamRealm=");
        builder2.append(rxDiamRealm);
        builder2.append("]");
        return builder2.toString();
    }

}
