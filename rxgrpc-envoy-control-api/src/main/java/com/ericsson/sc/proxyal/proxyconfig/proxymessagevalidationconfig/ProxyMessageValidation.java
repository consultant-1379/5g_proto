/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 17, 2024
 *     Author: ztsakon
 */

package com.ericsson.sc.proxyal.proxyconfig.proxymessagevalidationconfig;

import java.util.Objects;

import io.envoyproxy.envoy.extensions.filters.http.eric_proxy.v3.MessageValidation;

/**
 * 
 */
public class ProxyMessageValidation
{
    private final ProxyCheckMessageBytes validationCheckMessageBytes;
    private final ProxyCheckJsonLeaves validationCheckJsonLeaves;
    private final ProxyCheckJsonDepth validationCheckJsonDepth;
    private final ProxyCheckJsonSyntax validationCheckJsonSyntax;
    private final ProxyCheckHeaders validationCheckHeaders;
    private final ProxyCheckServiceOperations validationCheckServiceOps;

    /**
     * @param validationCheckMessageBytes
     * @param validationCheckJsonLeaves
     * @param validationCheckJsonDepth
     * @param validationCheckJsonSyntax
     * @param validationCheckHeaders
     * @param validationCheckServiceOps
     */
    public ProxyMessageValidation(ProxyCheckMessageBytes validationCheckMessageBytes,
                                  ProxyCheckJsonLeaves validationCheckJsonLeaves,
                                  ProxyCheckJsonDepth validationCheckJsonDepth,
                                  ProxyCheckJsonSyntax validationCheckJsonSyntax,
                                  ProxyCheckHeaders validationCheckHeaders,
                                  ProxyCheckServiceOperations validationCheckServiceOps)
    {
        super();
        this.validationCheckMessageBytes = validationCheckMessageBytes;
        this.validationCheckJsonLeaves = validationCheckJsonLeaves;
        this.validationCheckJsonDepth = validationCheckJsonDepth;
        this.validationCheckJsonSyntax = validationCheckJsonSyntax;
        this.validationCheckHeaders = validationCheckHeaders;
        this.validationCheckServiceOps = validationCheckServiceOps;
    }

    /**
     * @param validationCheckMessageBytes
     * @param validationCheckJsonLeaves
     * @param validationCheckJsonDepth
     * @param validationCheckJsonSyntax
     * @param validationCheckHeaders
     */
    public ProxyMessageValidation(ProxyCheckMessageBytes validationCheckMessageBytes,
                                  ProxyCheckJsonLeaves validationCheckJsonLeaves,
                                  ProxyCheckJsonDepth validationCheckJsonDepth,
                                  ProxyCheckJsonSyntax validationCheckJsonSyntax,
                                  ProxyCheckHeaders validationCheckHeaders)
    {
        super();
        this.validationCheckMessageBytes = validationCheckMessageBytes;
        this.validationCheckJsonLeaves = validationCheckJsonLeaves;
        this.validationCheckJsonDepth = validationCheckJsonDepth;
        this.validationCheckJsonSyntax = validationCheckJsonSyntax;
        this.validationCheckHeaders = validationCheckHeaders;
        this.validationCheckServiceOps = null;
    }

    /**
     * @param validationCheckMessageBytes
     * @param validationCheckJsonLeaves
     * @param validationCheckJsonDepth
     */
    public ProxyMessageValidation(ProxyCheckMessageBytes validationCheckMessageBytes,
                                  ProxyCheckJsonLeaves validationCheckJsonLeaves,
                                  ProxyCheckJsonDepth validationCheckJsonDepth)
    {
        super();
        this.validationCheckMessageBytes = validationCheckMessageBytes;
        this.validationCheckJsonLeaves = validationCheckJsonLeaves;
        this.validationCheckJsonDepth = validationCheckJsonDepth;
        this.validationCheckJsonSyntax = null;
        this.validationCheckHeaders = null;
        this.validationCheckServiceOps = null;
    }

    /**
     * @param proxyMessageValidation
     */
    public ProxyMessageValidation(ProxyMessageValidation proxyMessageValidation)
    {
        super();
        this.validationCheckMessageBytes = proxyMessageValidation.getValidationCheckMessageBytes();
        this.validationCheckJsonLeaves = proxyMessageValidation.getValidationCheckJsonLeaves();
        this.validationCheckJsonDepth = proxyMessageValidation.getValidationCheckJsonDepth();
        this.validationCheckJsonSyntax = proxyMessageValidation.getValidationCheckJsonSyntax();
        this.validationCheckHeaders = proxyMessageValidation.getValidationCheckHeaders();
        this.validationCheckServiceOps = proxyMessageValidation.getValidationCheckServiceOps();
    }

    /**
     * @return the validationCheckMessageBytes
     */
    public ProxyCheckMessageBytes getValidationCheckMessageBytes()
    {
        return validationCheckMessageBytes;
    }

    /**
     * @return the validationCheckJsonLeaves
     */
    public ProxyCheckJsonLeaves getValidationCheckJsonLeaves()
    {
        return validationCheckJsonLeaves;
    }

    /**
     * @return the validationCheckJsonDepth
     */
    public ProxyCheckJsonDepth getValidationCheckJsonDepth()
    {
        return validationCheckJsonDepth;
    }

    /**
     * @return the validationCheckJsonSyntax
     */
    public ProxyCheckJsonSyntax getValidationCheckJsonSyntax()
    {
        return validationCheckJsonSyntax;
    }

    /**
     * @return the validationCheckHeaders
     */
    public ProxyCheckHeaders getValidationCheckHeaders()
    {
        return validationCheckHeaders;
    }

    /**
     * @return the validationCheckServiceOps
     */
    public ProxyCheckServiceOperations getValidationCheckServiceOps()
    {
        return validationCheckServiceOps;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(validationCheckHeaders,
                            validationCheckJsonDepth,
                            validationCheckJsonLeaves,
                            validationCheckJsonSyntax,
                            validationCheckMessageBytes,
                            validationCheckServiceOps);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProxyMessageValidation other = (ProxyMessageValidation) obj;
        return Objects.equals(validationCheckHeaders, other.validationCheckHeaders) && Objects.equals(validationCheckJsonDepth, other.validationCheckJsonDepth)
               && Objects.equals(validationCheckJsonLeaves, other.validationCheckJsonLeaves)
               && Objects.equals(validationCheckJsonSyntax, other.validationCheckJsonSyntax)
               && Objects.equals(validationCheckServiceOps, other.validationCheckServiceOps)
               && Objects.equals(validationCheckMessageBytes, other.validationCheckMessageBytes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ProxyMessageValidation [validationCheckMessageBytes=" + validationCheckMessageBytes + ", validationCheckJsonLeaves=" + validationCheckJsonLeaves
               + ", validationCheckJsonDepth=" + validationCheckJsonDepth + ", validationCheckJsonSyntax=" + validationCheckJsonSyntax
               + ", validationCheckServiceOps=" + validationCheckServiceOps + ", validationCheckHeaders=" + validationCheckHeaders + "]";
    }

    public MessageValidation build()
    {
        var messageValidationBuilder = MessageValidation.newBuilder();
        if (this.validationCheckMessageBytes != null && this.validationCheckMessageBytes.getMaxMessageBytes().isPresent())
            messageValidationBuilder.setCheckMessageBytes(this.validationCheckMessageBytes.build());
        if (this.validationCheckJsonLeaves != null && this.validationCheckJsonLeaves.getMaxMessageLeaves().isPresent())
            messageValidationBuilder.setCheckJsonLeaves(this.validationCheckJsonLeaves.build());
        if (this.validationCheckJsonDepth != null && this.validationCheckJsonDepth.getMaxMessageNestingDepth().isPresent())
            messageValidationBuilder.setCheckJsonDepth(this.validationCheckJsonDepth.build());
        if (this.validationCheckJsonSyntax != null)
            messageValidationBuilder.setCheckJsonSyntax(this.validationCheckJsonSyntax.build());
        if (this.validationCheckHeaders != null)
            messageValidationBuilder.setCheckHeaders(this.validationCheckHeaders.build());
        if (this.validationCheckServiceOps != null)
            messageValidationBuilder.setCheckServiceOperations(this.validationCheckServiceOps.build());
        return messageValidationBuilder.build();
    }
}
