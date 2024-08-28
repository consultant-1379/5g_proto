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
 * Created on: Jul 6, 2023
 *     Author: zpavcha
 */

package com.ericsson.esc.jwt;

import java.util.Set;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;

/**
 * The JOSE header that holds all the required information for signing a JWT
 * hence producing a JWS.
 */
public class JOSEHeader
{
    private final JWSAlgorithm algorithm;
    private final JOSEObjectType type;
    private final String keyId;
    private final Set<String> critical;

    private JOSEHeader(Builder builder)
    {
        this.algorithm = builder.algorithm;
        this.type = builder.type;
        this.keyId = builder.keyId;
        this.critical = builder.critical;
    }

    public static class Builder
    {
        private JWSAlgorithm algorithm = JWSAlgorithm.HS256;
        private JOSEObjectType type = JOSEObjectType.JWT;
        private String keyId;
        private Set<String> critical;

        public Builder withAlgorithm(final JWSAlgorithm algorithm)
        {
            this.algorithm = algorithm;
            return this;
        }

        public Builder withType(final JOSEObjectType type)
        {
            this.type = type;
            return this;
        }

        public Builder withKeyId(final String keyId)
        {
            this.keyId = keyId;
            return this;
        }

        public Builder withCritical(final Set<String> critical)
        {
            this.critical = critical;
            return this;
        }

        public JOSEHeader build()
        {
            return new JOSEHeader(this);
        }
    }

    /**
     * @return the algorithm
     */
    public JWSAlgorithm getAlgorithm()
    {
        return this.algorithm;
    }

    /**
     * @return the type
     */
    public JOSEObjectType getType()
    {
        return this.type;
    }

    /**
     * @return the critical
     */
    public Set<String> getCritical()
    {
        return this.critical;
    }

    /**
     * @return the keyId
     */
    public String getKeyId()
    {
        return this.keyId;
    }

}
