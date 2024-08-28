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

import java.time.OffsetDateTime;

/**
 * The claims inside a JWT payload
 */
public class JWSPayload
{
    private final String issuer;
    private final String subject;
    private final String audience;
    private final OffsetDateTime expirationTime;
    private final OffsetDateTime notValidBefore;
    private final OffsetDateTime issuedAt;
    private final String jwtId;
    private final String scope;
    private final AdditionalClaims additionalClaims;

    protected JWSPayload()
    {
        throw new UnsupportedOperationException("Default constructor should not be used");
    }

    private JWSPayload(final Builder builder)
    {
        this.issuer = builder.issuer;
        this.subject = builder.subject;
        this.audience = builder.audience;
        this.expirationTime = builder.expirationTime;
        this.notValidBefore = builder.notValidBefore;
        this.issuedAt = builder.issuedAt;
        this.jwtId = builder.jwtId;
        this.scope = builder.scope;
        this.additionalClaims = builder.additionalClaims;
    }

    public static class Builder
    {
        private String issuer;
        private String subject;
        private String audience;
        private OffsetDateTime expirationTime;
        private OffsetDateTime notValidBefore;
        private OffsetDateTime issuedAt;
        private String jwtId;
        private String scope;
        private AdditionalClaims additionalClaims;

        public Builder withIssuer(final String issuer)
        {
            this.issuer = issuer;
            return this;
        }

        public Builder withSubject(final String subject)
        {
            this.subject = subject;
            return this;
        }

        public Builder withAudience(final String audience)
        {
            this.audience = audience;
            return this;
        }

        public Builder withExpirationTime(final OffsetDateTime expirationTime)
        {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder withNotValidBefore(final OffsetDateTime notValidBefore)
        {
            this.notValidBefore = notValidBefore;
            return this;
        }

        public Builder withIssuedAt(final OffsetDateTime issuedAt)
        {
            this.issuedAt = issuedAt;
            return this;
        }

        public Builder withJwtId(final String jwtId)
        {
            this.jwtId = jwtId;
            return this;
        }

        public Builder withScope(final String scope)
        {
            this.scope = scope;
            return this;
        }

        public Builder withAdditionalClaims(final AdditionalClaims additionalClaims)
        {
            this.additionalClaims = additionalClaims;
            return this;
        }

        public JWSPayload build()
        {
            return new JWSPayload(this);
        }
    }

    /**
     * @return the issuer
     */
    public String getIssuer()
    {
        return this.issuer;
    }

    /**
     * @return the subject
     */
    public String getSubject()
    {
        return this.subject;
    }

    /**
     * @return the audience
     */
    public String getAudience()
    {
        return this.audience;
    }

    /**
     * @return the expirationTime
     */
    public OffsetDateTime getExpirationTime()
    {
        return this.expirationTime;
    }

    /**
     * @return the notValidBefore
     */
    public OffsetDateTime getNotValidBefore()
    {
        return this.notValidBefore;
    }

    /**
     * @return the issuedAt
     */
    public OffsetDateTime getIssuedAt()
    {
        return this.issuedAt;
    }

    /**
     * @return the jwtId
     */
    public String getJwtId()
    {
        return this.jwtId;
    }

    /**
     * @return the scope
     */
    public String getScope()
    {
        return this.scope;
    }

    /**
     * @return the additionalClaims
     */
    public AdditionalClaims getAdditionalClaims()
    {
        return this.additionalClaims;
    }

}
