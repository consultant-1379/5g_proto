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
 * Created on: Mar 27, 2024
 *     Author: Avengers
 */

package com.ericsson.sc.certmcrhandler.k8s.io;

import io.reactivex.Completable;

import java.util.List;
import java.util.Objects;

import com.ericsson.sc.certmcrhandler.k8s.resource.ExternalCertificateSpec;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class SecretReader
{
    private K8sClient client;

    public SecretReader(String k8Config,
                        String namespace)
    {
        this.client = new K8sClient(k8Config, namespace);
    }

    SecretReader()
    {
        this.client = new K8sClient();
    }

    public Secret getSecret(String name)
    {
        return this.client.getSecrets(name);
    }

    public List<Secret> getSecretList(String name)
    {
        return this.client.getSecretList(name);
    }

    public Completable createSecretForCr(ExternalCertificateSpec spec)
    {

        ObjectMeta metadata = new ObjectMeta();
        Secret secret = new Secret();

        metadata.setName(spec.getGeneratedSecretName());
        secret.setMetadata(metadata);

        return this.client.createSecret(secret);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(client);
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
        SecretReader other = (SecretReader) obj;
        return Objects.equals(client, other.client);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "SecretReader [client=" + client + "]";
    }

}