/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 12, 2023
 *     Author: zpavcha
 */

package com.ericsson.esc.jwt;

import java.util.List;
import java.util.Objects;

import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;

/**
 * Contains the additional claims regarding the NF Producer that exist in the
 * payload of JWT for oAuth2.0.
 */
public class AdditionalClaims
{
    private final List<Snssai> producerSnssaiList;
    private final List<String> producerNsiList;
    private final String producerNfSetId;
    private final PlmnId consumerPlmnId;
    private final PlmnId producerPlmnId;

    private AdditionalClaims(final Builder builder)
    {
        this.producerSnssaiList = builder.producerSnssaiList;
        this.producerNsiList = builder.producerNsiList;
        this.producerNfSetId = builder.producerNfSetId;
        this.consumerPlmnId = builder.consumerPlmnId;
        this.producerPlmnId = builder.producerPlmnId;
    }

    public static class Builder
    {
        private List<Snssai> producerSnssaiList;
        private List<String> producerNsiList;
        private String producerNfSetId;
        private PlmnId consumerPlmnId;
        private PlmnId producerPlmnId;

        public Builder withProducerSnssaiList(final List<Snssai> producerSnssaiList)
        {
            this.producerSnssaiList = producerSnssaiList;
            return this;
        }

        public Builder withProducerNsiList(final List<String> producerNsiList)
        {
            this.producerNsiList = producerNsiList;
            return this;
        }

        public Builder withProducerNfSetId(final String producerNfSetId)
        {
            this.producerNfSetId = producerNfSetId;
            return this;
        };

        public Builder withConsumerPlmnId(final PlmnId consumerPlmnId)
        {
            this.consumerPlmnId = consumerPlmnId;
            return this;
        }

        public Builder withProducerPlmnId(final PlmnId producerPlmnId)
        {
            this.producerPlmnId = producerPlmnId;
            return this;
        }

        public AdditionalClaims build()
        {
            return new AdditionalClaims(this);
        }

    }

    /**
     * @return the producerSnssaiList
     */
    public List<Snssai> getProducerSnssaiList()
    {
        return producerSnssaiList;
    }

    /**
     * @return the producerNsiList
     */
    public List<String> getProducerNsiList()
    {
        return producerNsiList;
    }

    /**
     * @return the nfSetId
     */
    public String getProducerNfSetId()
    {
        return producerNfSetId;
    }

    /**
     * @return the consumerPlmnId
     */
    public PlmnId getConsumerPlmnId()
    {
        return consumerPlmnId;
    }

    /**
     * @return the producerPlmnId
     */
    public PlmnId getProducerPlmnId()
    {
        return producerPlmnId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(consumerPlmnId, producerNfSetId, producerNsiList, producerPlmnId, producerSnssaiList);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AdditionalClaims other = (AdditionalClaims) obj;
        return Objects.equals(consumerPlmnId, other.consumerPlmnId) && Objects.equals(producerNfSetId, other.producerNfSetId)
               && Objects.equals(producerNsiList, other.producerNsiList) && Objects.equals(producerPlmnId, other.producerPlmnId)
               && Objects.equals(producerSnssaiList, other.producerSnssaiList);
    }

    @Override
    public String toString()
    {
        return String.format("AdditionalClaims [producerSnssaiList=%s, producerNsiList=%s, producerNfSetId=%s, consumerPlmnId=%s, producerPlmnId=%s]",
                             producerSnssaiList,
                             producerNsiList,
                             producerNfSetId,
                             consumerPlmnId,
                             producerPlmnId);
    }

}
