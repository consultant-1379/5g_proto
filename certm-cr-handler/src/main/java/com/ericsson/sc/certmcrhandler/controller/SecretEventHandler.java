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
package com.ericsson.sc.certmcrhandler.controller;

import io.fabric8.kubernetes.client.informers.ResourceEventHandler;

import java.util.Objects;

import io.fabric8.kubernetes.api.model.Secret;

public class SecretEventHandler implements ResourceEventHandler<Secret>
{
    private static final String CA_CRT = "cert1.pem";
    private static final String TLS_CRT = "tls.crt";
    private static final String TLS_KEY = "tls.key";

    private final SecretUpdateCallback updateCallback;

    public SecretEventHandler(SecretUpdateCallback updateCallback)
    {
        this.updateCallback = updateCallback;
    }

    @Override
    public void onUpdate(Secret oldSecret,
                         Secret newSecret)
    {
        updateCallback.handleSecretEvent(new ExternalCertificateInfo(SecretEventType.UPDATE,
                                                                     oldSecret.getMetadata().getName(),
                                                                     newSecret.getData().get(TLS_CRT),
                                                                     newSecret.getData().get(TLS_KEY),
                                                                     newSecret.getData().get(CA_CRT)));
    }

    @Override
    public void onAdd(Secret secret)
    {
        updateCallback.handleSecretEvent(new ExternalCertificateInfo(SecretEventType.ADD,
                                                                     secret.getMetadata().getName(),
                                                                     secret.getData().get(TLS_CRT),
                                                                     secret.getData().get(TLS_KEY),
                                                                     secret.getData().get(CA_CRT)));
    }

    @Override
    public void onDelete(Secret secret,
                         boolean deletedFinalStateUnknown)
    {
        updateCallback.handleSecretEvent(new ExternalCertificateInfo(SecretEventType.DELETE,
                                                                     secret.getMetadata().getName(),
                                                                     secret.getData().get(TLS_CRT),
                                                                     secret.getData().get(TLS_KEY),
                                                                     secret.getData().get(CA_CRT)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(updateCallback);
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
        SecretEventHandler other = (SecretEventHandler) obj;
        return Objects.equals(updateCallback, other.updateCallback);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "SecretEventHandler [updateCallback=" + updateCallback + "]";
    }

}