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
 * Created on: Nov 3, 2020
 *     Author: epaxale et al
 */

package com.ericsson.sc.proxyal.proxyconfig.proxyhttpfilterconfig;

import java.util.HashSet;
import java.util.Set;

/**
 * 
 */
public class ProxyJwtProvider
{
    private final String name;
    private final String publicKeys;
    private final String payloadInMetadataName;
    private Set<String> audiences = new HashSet<>();

    public ProxyJwtProvider(String name,
                            String publicKeys,
                            String payloadInMetadataName)
    {
        this.name = name;
        this.publicKeys = publicKeys;
        this.payloadInMetadataName = payloadInMetadataName;
    }

    public ProxyJwtProvider(ProxyJwtProvider jwtProvider)
    {
        this.name = jwtProvider.getName();
        this.publicKeys = jwtProvider.getPublicKeys();
        jwtProvider.getAudiences().forEach(aud -> this.audiences.add(aud));
        this.payloadInMetadataName = jwtProvider.getPayloadInMetadataName();
    }

    /**
     * @return the publicKey
     */
    public String getPublicKeys()
    {
        return publicKeys;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    public void addAudiences(String audience)
    {
        audiences.add(audience);
    }

    /**
     * @return the audiences
     */
    public Set<String> getAudiences()
    {
        return audiences;
    }

    /**
     * @return the payload in metadata
     */
    public String getPayloadInMetadataName()
    {
        return payloadInMetadataName;
    }

    @Override
    public String toString()
    {
        return "\nName: " + name + "\nPublic Keys: " + publicKeys + "\nAudiences: " + audiences.toString();
    }

    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((audiences == null) ? 0 : audiences.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null)
            return true;

        if (!(o instanceof ProxyJwtProvider))
            return false;

        var other = (ProxyJwtProvider) o;

        var nameEquals = (this.name == null && other.name == null) || (this.name != null && this.name.equals(other.getName()));
        var audiencesEquals = (this.audiences == null && other.audiences == null) || (this.audiences != null && this.audiences.equals(other.getAudiences()));

        return nameEquals && audiencesEquals;
    }

}
