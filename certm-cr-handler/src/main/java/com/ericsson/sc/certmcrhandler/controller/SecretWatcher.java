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

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.reactivex.subjects.BehaviorSubject;

import java.util.Objects;
import java.util.Optional;

import com.ericsson.sc.certmcrhandler.data.SecretTlsDataList;
import com.ericsson.sc.certmcrhandler.k8s.io.K8sClient;

import io.fabric8.kubernetes.api.model.Secret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.disposables.Disposable;

public class SecretWatcher
{

    private static final Logger log = LoggerFactory.getLogger(SecretWatcher.class);

    private SecretTlsDataList trafficSecretDataList = new SecretTlsDataList();
    private BehaviorSubject<SecretTlsDataList> trafficSecretDataSbj = BehaviorSubject.create();

    private final SecretEventHandler eventHandler;
    private SharedIndexInformer<Secret> secretInformer;

    private Disposable disposal = trafficSecretDataSbj.doOnDispose(() -> log.info("Stopped secret watcher"))
                                                      .doOnSubscribe(x -> log.info("Secret watcher disposal"))
                                                      .subscribe();

    /**
     * @return the disposal
     */
    public Disposable getDisposal()
    {
        return disposal;
    }

    public SecretWatcher()
    {
        // Initialize the client
        var client = new K8sClient();
        this.eventHandler = new SecretEventHandler(this::handleSecretEvent);
        // Add event handler to the informer
        client.getSecretInformer(eventHandler).ifPresentOrElse(informer -> this.secretInformer = informer, () ->
        {
            throw new IllegalStateException("Failed to initialize secretInformer. SharedInformerFactory is not available.");
        });
    }

    public SecretWatcher(KubernetesClient client,
                         String namespace)
    {
        // Initialize the client
        this.eventHandler = new SecretEventHandler(this::handleSecretEvent);
        // Add event handler to the informer
        try
        {
            Optional<SharedIndexInformer<Secret>> informer = Optional.ofNullable(client.secrets().inNamespace(namespace).inform(this.eventHandler));
            informer.ifPresentOrElse(x -> this.secretInformer = x, () ->
            {
                throw new IllegalStateException("Failed to initialize secretInformer. SharedInformerFactory is not available.");
            });
        }
        catch (Exception e)
        {
            log.error("Fail to create informer.", e);
        }

    }

    public void handleSecretEvent(ExternalCertificateInfo secretData)
    {
        if (secretData.getTlsCertValue() != null && secretData.getTlsKeyValue() != null)
        {
            updateTrafficSecretDataList(secretData);
        }
        else
        {
            updateCaSecretDataList(secretData);
        }
    }

    /**
     * @param secretData
     */
    private void updateTrafficSecretDataList(ExternalCertificateInfo secretData)
    {
        switch (secretData.getEventType())
        {
            case ADD:
                trafficSecretDataList.addSecret(secretData.getSecretName(), secretData.getTlsCertValue(), secretData.getTlsKeyValue());
                trafficSecretDataSbj.onNext(trafficSecretDataList);
                break;
            case UPDATE:
                trafficSecretDataList.updateSecret(secretData.getSecretName(), secretData.getTlsCertValue(), secretData.getTlsKeyValue());
                trafficSecretDataSbj.onNext(trafficSecretDataList);
                break;
            case DELETE:
                trafficSecretDataList.deleteSecret(secretData.getSecretName());
                trafficSecretDataSbj.onNext(trafficSecretDataList);
                break;
            default:
                break;
        }
    }

    /**
     * @param secretData
     */
    private void updateCaSecretDataList(ExternalCertificateInfo secretData)
    {
        switch (secretData.getEventType())
        {
            case ADD:
                trafficSecretDataList.addSecret(secretData.getSecretName(), secretData.getCaCrtValue());
                trafficSecretDataSbj.onNext(trafficSecretDataList);
                break;
            case UPDATE:
                trafficSecretDataList.updateSecret(secretData.getSecretName(), secretData.getCaCrtValue());
                trafficSecretDataSbj.onNext(trafficSecretDataList);
                break;
            case DELETE:
                trafficSecretDataList.deleteSecret(secretData.getSecretName());
                trafficSecretDataSbj.onNext(trafficSecretDataList);
                break;
            default:
                break;
        }
    }

    public void start()
    {
        this.secretInformer.start();
    }

    public void stopInformer()
    {
        trafficSecretDataSbj.onComplete();
        this.secretInformer.stop();
    }

    /**
     * @return the trafficSecretDataSbj
     */
    public BehaviorSubject<SecretTlsDataList> getTrafficSecretDataSbj()
    {
        return trafficSecretDataSbj;
    }

    /**
     * @return the trafficSecretDataList
     */
    public SecretTlsDataList getTrafficSecretDataList()
    {
        return trafficSecretDataList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(eventHandler, secretInformer, trafficSecretDataList, trafficSecretDataSbj);
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
        SecretWatcher other = (SecretWatcher) obj;
        return Objects.equals(eventHandler, other.eventHandler) && Objects.equals(secretInformer, other.secretInformer)
               && Objects.equals(trafficSecretDataList, other.trafficSecretDataList) && Objects.equals(trafficSecretDataSbj, other.trafficSecretDataSbj);
    }

    /**
     * @return the secretInformer
     */
    public SharedIndexInformer<Secret> getSecretInformer()
    {
        return secretInformer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "SecretWatcher [trafficSecretDataList=" + trafficSecretDataList + ", trafficSecretDataSbj=" + trafficSecretDataSbj + ", eventHandler="
               + eventHandler + ", secretInformer=" + secretInformer + "]";
    }

}
