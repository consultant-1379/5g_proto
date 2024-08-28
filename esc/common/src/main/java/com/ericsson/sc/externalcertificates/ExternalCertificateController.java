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
 *     Author: zpitgio
 */

package com.ericsson.sc.externalcertificates;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.certmcrhandler.controller.SecretWatcher;
import com.ericsson.sc.certmcrhandler.k8s.io.CrHandler;
import com.ericsson.sc.certmcrhandler.k8s.resource.ExternalCertificateSpec;
import com.ericsson.sc.certmcrhandler.k8s.resource.GeneratedSecretTypeEnum;

import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.NfInstance;
import com.ericsson.utilities.common.EnvVars;

import io.fabric8.kubernetes.client.KubernetesClientException;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * 
 */
public class ExternalCertificateController
{
    private static final String SEPP_ANNOTATIONS = EnvVars.get("SEPP_ANNOTATIONS");
    private static final String SEPP_LABELS = EnvVars.get("SEPP_LABELS");
    private static final String CR_PREFIX = EnvVars.get("GLOBAL_ERIC_SEPP_NAME");
    private static final String NEW_CERTIFICATE_PREFIX = CR_PREFIX + "-sepp-extcert-";
    private static final String CR_PREFIX_MANAGER = CR_PREFIX + "-manager";
    private String crName;

    protected SecretWatcher secretWatcher;

    private static final Logger log = LoggerFactory.getLogger(ExternalCertificateController.class);

    public ExternalCertificateController()
    {
        startSecretWatcher();
    }

    public ExternalCertificateController(BehaviorSubject<Optional<EricssonSepp>> ericssonSepp)
    {
        ericssonSepp.toFlowable(BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).distinctUntilChanged().subscribe(optEricssonSepp ->
        {
            log.info("Configuration update received");
            List<String> crList = new ArrayList<>();
            CrHandler handler = new CrHandler();

            if (optEricssonSepp.isPresent() && optEricssonSepp.get().getEricssonSeppSeppFunction() != null
                && !optEricssonSepp.get().getEricssonSeppSeppFunction().getNfInstance().isEmpty()
                && newYangNodes(optEricssonSepp.get().getEricssonSeppSeppFunction().getNfInstance().get(0)))
            {
                log.info("Start creating the new External Certificate COs");

                try
                {
                    crList = handleConfigUpdate(optEricssonSepp.get().getEricssonSeppSeppFunction().getNfInstance().get(0), handler);
                }
                catch (KubernetesClientException exception)
                {
                    log.error("Error interacting with Kubernetes: {}", exception.getMessage());
                }
            }

            try
            {
                handler.deleteUselessCr(crList);
            }
            catch (KubernetesClientException exception)
            {
                log.error("Error interacting with Kubernetes: {}", exception.getMessage());
            }
            catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e)
            {
                log.error("Error accessing resource metadata: {}", e.getMessage());
            }

        }, (e -> log.error("Failed to get configuration update: {}", e.getMessage())));
    }

    protected void startSecretWatcher()
    {
        try
        {
            this.secretWatcher = new SecretWatcher();
            secretWatcher.start();
            log.info("Secret watcher started");

        }
        catch (IllegalStateException exception)
        {
            log.error("Failed to initialize SecretWatcher: {}", exception.getMessage());
        }
    }

    private List<String> handleConfigUpdate(NfInstance instance,
                                            CrHandler handler) throws IllegalArgumentException, SecurityException
    {
        log.debug("Manager start creating/updating CRs");
        List<String> crList = new ArrayList<>();
        Map<String, String> annotations = new HashMap<>();
        Map<String, String> labels = new HashMap<>();
        // externalCertificate for trusted-cert-list

        // Split the YAML content into lines
        String[] linesAnnotations = SEPP_ANNOTATIONS.split("\\r?\\n");
        String[] linesLabels = SEPP_LABELS.split("\\r?\\n");
        // Iterate over each line and extract key-value pairs
        for (String line : linesAnnotations)
        {
            // Split each line into key and value
            String[] keyValue = line.split(":", 2);
            if (keyValue.length == 2)
            {
                // Trim key and value to remove leading/trailing spaces
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                // Remove quotes if present
                value = value.replaceAll("^\"|\"$", "");
                // Put key-value pair into the map
                annotations.put(key, value);
            }
        }

        for (String line : linesLabels)
        {
            // Split each line into key and value
            String[] keyValue = line.split(":", 2);
            if (keyValue.length == 2)
            {
                // Trim key and value to remove leading/trailing spaces
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                // Remove quotes if present
                value = value.replaceAll("^\"|\"$", "");
                // Put key-value pair into the map
                labels.put(key, value);
            }
        }
        labels.put("app.kubernetes.io/managed-by", "sepp-manager");
        labels.put("app", CR_PREFIX);
        // Print the map
        log.debug("Annotations Map:");
        for (Map.Entry<String, String> entry : annotations.entrySet())
        {
            log.info("{} = {}", entry.getKey(), entry.getValue());
        }

        // Print the map
        log.debug("Labels Map:");
        for (Map.Entry<String, String> entry : labels.entrySet())
        {
            log.info("{} = {}", entry.getKey(), entry.getValue());
        }
        if (instance.getTrustedCertList() != null && !instance.getTrustedCertList().isEmpty())
        {
            instance.getTrustedCertList().stream().forEach(tc ->
            {
                this.crName = NEW_CERTIFICATE_PREFIX + tc.getTrustedCertListRef() + "-ca-certificate";
                crList.add(this.crName);
                if (Boolean.FALSE.equals(handler.checkExistingCr(this.crName)))
                {
                    ExternalCertificateSpec spec = ExternalCertificateSpec.builder()
                                                                          .generatedSecretName(this.crName)
                                                                          .generatedSecretType(GeneratedSecretTypeEnum.OPAQUE)
                                                                          .trustedCertificateListName(tc.getTrustedCertListRef())
                                                                          .trustedCertificatesFileName("cert1.pem")
                                                                          .build();

                    handler.createCrWithAnnotations(spec, this.crName, annotations, labels, CR_PREFIX_MANAGER).subscribeOn(Schedulers.io()).subscribe();
                }
                else
                {
                    handler.updateCrWithAnnotations(this.crName, annotations, labels, CR_PREFIX_MANAGER).subscribeOn(Schedulers.io()).subscribe();
                }

            });
        }
        // externalCertificate for asymmetric-key
        if (instance.getAsymKeyList() != null && !instance.getAsymKeyList().isEmpty())
        {
            instance.getAsymKeyList().stream().forEach(asymKey ->
            {
                this.crName = NEW_CERTIFICATE_PREFIX + asymKey.getAsymmetricKey() + "-" + asymKey.getCertificate() + "-certificate";
                crList.add(crName);
                if (Boolean.FALSE.equals(handler.checkExistingCr(this.crName)))
                {
                    ExternalCertificateSpec spec = ExternalCertificateSpec.builder()
                                                                          .generatedSecretName(this.crName)
                                                                          .generatedSecretType(GeneratedSecretTypeEnum.TLS)
                                                                          .asymmetricKeyCertificateName(asymKey.getAsymmetricKey() + "/"
                                                                                                        + asymKey.getCertificate())
                                                                          .build();

                    handler.createCrWithAnnotations(spec, this.crName, annotations, labels, CR_PREFIX_MANAGER).subscribeOn(Schedulers.io()).subscribe();
                }
                else
                {
                    handler.updateCrWithAnnotations(this.crName, annotations, labels, CR_PREFIX_MANAGER).subscribeOn(Schedulers.io()).subscribe();
                }
            });
        }
        log.debug("CR names to be kept: {}", crList);
        return crList;
    }

    /**
     * @param nfInstance
     * @return
     */
    private boolean newYangNodes(NfInstance nfInstance)
    {
        return (nfInstance.getTrustedCertList() != null && !nfInstance.getTrustedCertList().isEmpty())
               || (nfInstance.getAsymKeyList() != null && !nfInstance.getAsymKeyList().isEmpty());
    }

    public Completable stop()
    {
        return Completable.fromAction(() -> this.secretWatcher.stopInformer());
    }

    public Completable stopSDS()
    {
        return Completable.fromAction(() ->
        {
            this.secretWatcher.stopInformer();
            var dis = this.secretWatcher.getDisposal();
            if (this.secretWatcher.getDisposal() != null)
            {
                dis.dispose();
                dis = null;
            }
        })
                          .doOnSubscribe(__ -> log.info("Stopping informer"))
                          .doOnComplete(() -> log.info("Stopped informer"))
                          .doOnError(__ -> log.info("Error while stopping SDS"));
    }

    public SecretWatcher getSecretWatcher()
    {
        return this.secretWatcher;
    }
}
