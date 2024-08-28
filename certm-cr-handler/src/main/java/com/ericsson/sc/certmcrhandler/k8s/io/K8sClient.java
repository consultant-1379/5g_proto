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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.certmcrhandler.k8s.resource.ExternalCertificate;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.reactivex.Completable;

public class K8sClient
{

    private static final String NAMESPACE_FILE_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/namespace";
    private static final Logger log = LoggerFactory.getLogger(K8sClient.class);
    private KubernetesClient client;
    private String namespace;

    public K8sClient()
    {
        this.client = new KubernetesClientBuilder().withConfig(getClusterConfig()).build();
        this.namespace = getNamespace();
    }

    K8sClient(String kubeConfigYaml,
              String namespace)
    {
        Config config = Config.fromKubeconfig(kubeConfigYaml);
        this.client = new KubernetesClientBuilder().withConfig(config).build();
        this.namespace = namespace;
    }

    public Completable createCr(ExternalCertificate ec)
    {
        return Completable.fromCallable(() ->
        {
            try
            {
                this.client.resource(ec).inNamespace(this.namespace).create();
                return Completable.complete();
            }
            catch (KubernetesClientException e)
            {
                log.error("Creation of CR failed", e);
                throw new Exception(e);
            }
        });
    }

    public Completable deleteCr(String name,
                                long millis)
    {
        return Completable.fromCallable(() ->
        {
            try
            {
                var deletionRes = this.client.resources(ExternalCertificate.class)
                                             .inNamespace(this.namespace)
                                             .withName(name)
                                             .withTimeoutInMillis(millis)
                                             .delete();
                log.debug("resource name to be deleted: {}", name);
                if (deletionRes.isEmpty())
                {
                    throw new Exception("Nothing has been deleted");
                }
                return Completable.complete();
            }
            catch (KubernetesClientException e)
            {
                log.error("Deletion of CR failed", e);
                throw new Exception(e);
            }
        });
    }

    public Completable updateCr(ExternalCertificate ec)
    {
        return Completable.fromCallable(() ->
        {
            try
            {
                this.client.resource(ec).inNamespace(this.namespace).update();
                return Completable.complete();
            }
            catch (KubernetesClientException e)
            {
                log.error("Update of CR failed", e);
                throw new Exception(e);
            }
        });
    }

    public Secret getSecrets(String name)
    {
        try
        {
            return client.secrets().inNamespace(this.namespace).withName(name).get();
        }
        catch (Exception e)
        {
            log.error("Fail to retrieve secret {}.", name, e);
            throw e;
        }
    }

    public List<Secret> getSecretList(String name)
    {
        try
        {
            NonNamespaceOperation<Secret, SecretList, Resource<Secret>> secretOperation = client.secrets().inNamespace(namespace);
            SecretList secretList = secretOperation.list();
            return secretList.getItems();
        }
        catch (Exception e)
        {
            log.error("Error occurred while listing secrets in namespace {}: {}", namespace, e.getMessage());
            throw e;
        }
    }

    public Optional<SharedIndexInformer<Secret>> getSecretInformer(ResourceEventHandler<Secret> handler)
    {
        try
        {
            SharedIndexInformer<Secret> informer = client.secrets().inNamespace(getNamespace()).inform(handler);
            return Optional.ofNullable(informer);
        }
        catch (Exception e)
        {
            log.error("Fail to create informer.", e);
            return Optional.empty();
        }
    }

    public Completable createSecret(Secret secret) throws KubernetesClientException
    {
        return Completable.fromCallable(() ->
        {
            try
            {
                Secret existingSecret = client.secrets().inNamespace(this.namespace).withName(secret.getMetadata().getName()).get();
                if (existingSecret != null)
                {
                    client.secrets().inNamespace(this.namespace).withName(secret.getMetadata().getName()).delete();
                }
                client.secrets().inNamespace(this.namespace).resource(secret).create();
                log.info("Secret {} created successfully", secret.getMetadata().getName());
                return Completable.complete();
            }
            catch (KubernetesClientException e)
            {
                log.error("Failed to create secret {}", e.getMessage());
                throw new Exception(e);
            }
        });
    }

    public ExternalCertificate getResourceByName(String name)
    {
        return this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName(name).get();
    }

    public Boolean checkResourceLabel(String name,
                                      String labelKey,
                                      String labelValue)
    {
        var resource = getResourceByName(name);
        log.debug("resource with name {}: {}", name, resource);
        if (resource != null && !resource.getMetadata().getLabels().isEmpty() && resource.getMetadata().getLabels().containsKey(labelKey))
        {
            if (resource.getMetadata().getLabels().get(labelKey).equals(labelValue))
            {
                return true;
            }
        }
        return false;
    }

    public String getDeploymentUid(String deploymentName)
    {
        try
        {
            Resource<Deployment> deploymentResource = client.apps().deployments().inNamespace(namespace).withName(deploymentName);
            Deployment deployment = deploymentResource.get();
            if (deployment != null)
            {
                String uid = deployment.getMetadata().getUid();
                log.info("UID of the deployment: " + uid);
                return uid;
            }
            else
            {
                log.info("Deployment not found!");
            }
        }
        catch (Exception e)
        {
            log.error("Exception occurred: {}", e.getMessage());
        }
        return "Error: could not get deployment UID.";
    }

    public String getDeploymentApiVersion(String deploymentName)
    {
        try
        {
            Resource<Deployment> deploymentResource = client.apps().deployments().inNamespace(namespace).withName(deploymentName);
            Deployment deployment = deploymentResource.get();
            if (deployment != null)
            {
                String apiVersion = deployment.getApiVersion();
                log.info("ApiVersion of the deployment: {}", apiVersion);
                return apiVersion;
            }
            else
            {
                log.info("Deployment not found!");
            }
        }
        catch (Exception e)
        {
            log.error("Exception occurred: {}", e.getMessage());
        }
        return "Error: could not get deployment API Version.";
    }

    private static String readFile(String path,
                                   Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    private static Config getClusterConfig()
    {
        Config config = Config.autoConfigure(null);
        // AutoConfigure by default will create master URL with host name, which can be
        // IPv6 address with double-colon,
        // which will cause the host verification failed. ADPPRG-13721.
        // Therefore we set the default master url explicitly.
        // config.setMasterUrl(Config.DEFAULT_MASTER_URL);
        return config;
    }

    private static String getNamespace()
    {
        try
        {
            return readFile(NAMESPACE_FILE_PATH, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            log.error("Failed to get namespace. Reason: {}", e.getMessage());
            throw new RuntimeException("Error in fetching namespace! {}", e);
        }
    }

    public List<String> getExtCertNamesWithLabel(String labelKey,
                                                 String labelValue) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        KubernetesResourceList<ExternalCertificate> resources = this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).list();
        List<ExternalCertificate> javaResourceList = new ArrayList<>();
        if (resources != null && resources.getItems() != null)
        {
            javaResourceList.addAll(resources.getItems());
        }
        log.debug("javaResourceList: {}", javaResourceList);
        List<String> resourceNames = new ArrayList<>();
        for (ExternalCertificate resource : javaResourceList)
        {
            ObjectMeta metadata = (ObjectMeta) resource.getClass().getMethod("getMetadata").invoke(resource);
            if (metadata.getLabels().containsKey(labelKey))
            {
                if (metadata.getLabels().get(labelKey).equals(labelValue))
                {
                    resourceNames.add(metadata.getName());
                }
            }
        }
        log.debug("resources made by manager: {}", resourceNames);
        return resourceNames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(client, namespace);
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
        K8sClient other = (K8sClient) obj;
        return Objects.equals(client, other.client) && Objects.equals(namespace, other.namespace);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "K8sClient [client=" + client + ", namespace=" + namespace + "]";
    }

}
