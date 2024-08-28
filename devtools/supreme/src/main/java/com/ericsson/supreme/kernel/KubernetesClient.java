package com.ericsson.supreme.kernel;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.exceptions.KubernetesClientException;
import com.ericsson.utilities.common.Pair;
import com.google.gson.internal.LinkedTreeMap;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.ApiextensionsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.models.V1CustomResourceDefinition;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1NodeAddress;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

public class KubernetesClient
{
    protected static final String NETCONF = "netconf";
    protected static final String CMYP_K8S_NAME_ANNOTATION = "app.kubernetes.io/name=eric-cm-yang-provider";
    protected static final String LB_SVC_TYPE = "LoadBalancer";

    protected static final Logger log = LoggerFactory.getLogger(KubernetesClient.class);
    private final CoreV1Api api;
    private final String namespace;
    private ApiClient client;

    static
    {
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
    }

    public KubernetesClient(String namespace) throws KubernetesClientException
    {
        this.namespace = namespace;

        try
        {
            this.client = ClientBuilder.defaultClient();
            Configuration.setDefaultApiClient(client);

            api = new CoreV1Api(client);
        }
        catch (IOException e)
        {
            throw new KubernetesClientException("Could not initialize kubernetes client.", e);
        }
    }

    public KubernetesClient(String namespace,
                            String kubeconfig) throws KubernetesClientException
    {
        this.namespace = namespace;

        try (var kubeConfigReader = new FileReader(kubeconfig))
        {
            this.client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(kubeConfigReader)).build();
            Configuration.setDefaultApiClient(client);

            api = new CoreV1Api(client);
        }
        catch (IOException e)
        {
            throw new KubernetesClientException("Could not initialize kubernetes client.", e);
        }
    }

    public void exportCaToSecret(String crt,
                                 String secretName) throws KubernetesClientException
    {
        Map<String, String> dataMap = new HashMap<>();

        dataMap.put("cert1.pem", crt);
        V1Secret secret = new V1Secret().metadata(new V1ObjectMeta().name(secretName).namespace(namespace)).stringData(dataMap).type("Opaque");

        try
        {
            api.createNamespacedSecret(namespace, secret, "false", null, null, null);
        }
        catch (ApiException e)
        {
            throw new KubernetesClientException("Error when creating certificate authority secret " + secretName, e);
        }
    }

    public void exportCertToTlsSecret(String crt,
                                      String key,
                                      String secretName) throws KubernetesClientException
    {

        Map<String, String> dataMap = new HashMap<>();

        dataMap.put("tls.crt", crt);
        dataMap.put("tls.key", key);

        V1Secret secret = new V1Secret().metadata(new V1ObjectMeta().name(secretName).namespace(namespace)).stringData(dataMap).type("kubernetes.io/tls");

        try
        {
            api.createNamespacedSecret(namespace, secret, "false", null, null, null);
        }
        catch (ApiException e)
        {
            log.error("{}{}", e.getMessage(), e);
            throw new KubernetesClientException("Error when creating certificate secret " + secretName, e);
        }

    }

    public void exportCaAndCertToSecret(String ca,
                                        String crt,
                                        String key,
                                        String secretName) throws KubernetesClientException
    {

        Map<String, String> dataMap = new HashMap<>();

        dataMap.put("cert.pem", crt);
        dataMap.put("key.pem", key);
        dataMap.put("rootCA.crt", ca);

        V1Secret secret = new V1Secret().metadata(new V1ObjectMeta().name(secretName).namespace(namespace)).stringData(dataMap).type("Opaque");

        try
        {
            api.createNamespacedSecret(namespace, secret, "false", null, null, null);
        }
        catch (ApiException e)
        {
            log.error("{}{}", e.getMessage(), e);
            throw new KubernetesClientException("Error when creating certificate secret " + secretName, e);
        }

    }

    public void exportCertToGenericSecret(Map<String, String> dataMap,
                                          String secretName) throws KubernetesClientException
    {

        V1Secret secret = new V1Secret().metadata(new V1ObjectMeta().name(secretName).namespace(namespace)).stringData(dataMap).type("Opaque");

        try
        {
            api.createNamespacedSecret(namespace, secret, "false", null, null, null);
        }
        catch (ApiException e)
        {
            throw new KubernetesClientException("Error when creating certificate secret " + secretName, e);
        }

    }

    public boolean secretExists(String secretName) throws KubernetesClientException
    {
        try
        {
            return this.api.listNamespacedSecret(namespace, "false", false, null, null, null, null, null, null, null, false)
                           .getItems()
                           .stream()
                           .anyMatch(s -> s.getMetadata().getName().equals(secretName));
        }
        catch (ApiException e)
        {
            throw new KubernetesClientException("Error when searching for secret " + secretName, e);
        }
    }

    /**
     * @param secretName
     * @throws KubernetesClientException
     * @throws ApiException
     */
    public void deleteSecret(String secretName) throws KubernetesClientException
    {
        try
        {
            this.api.deleteNamespacedSecret(secretName, namespace, "false", null, 20, false, null, new V1DeleteOptions());
        }
        catch (ApiException e)
        {
            throw new KubernetesClientException("Error when deleting secret " + secretName, e);
        }
    }

    public String fetchHttpProxyFqdn(String httpProxyName) throws KubernetesClientException
    {
        var extensions = new ApiextensionsV1Api(client);
        List<V1CustomResourceDefinition> crds;
        try
        {
            crds = extensions.listCustomResourceDefinition(null, false, null, null, null, null, null, null, null, null).getItems();
            var httpProxy = crds.stream().filter(crd -> crd.getSpec().getNames().getKind().equals("HTTPProxy")).findFirst();

            if (httpProxy.isEmpty())
            {
                throw new KubernetesClientException("HttpProxy CRD was not found");
            }

            var customResource = new CustomObjectsApi(this.client);
            var httpProxyObject = customResource.getNamespacedCustomObject(httpProxy.get().getSpec().getGroup(), //
                                                                           httpProxy.get().getStatus().getStoredVersions().get(0), //
                                                                           namespace, //
                                                                           httpProxy.get().getSpec().getNames().getPlural(), //
                                                                           httpProxyName);
            @SuppressWarnings("unchecked")
            LinkedTreeMap<Object, Object> t = (LinkedTreeMap<Object, Object>) httpProxyObject;
            String spec = t.get("spec").toString();
            Pattern p = Pattern.compile("virtualhost=\\{fqdn=([^,|}]*)");
            Matcher m = p.matcher(spec);
            if (m.find())
            {
                return m.group().subSequence(18, m.group().length()).toString();
            }
            else
            {
                throw new KubernetesClientException("Unable to find " + httpProxyName + " httpproxy.");
            }

        }
        catch (ApiException e)
        {
            throw new KubernetesClientException("A kubernetes api exception happened", e);
        }

    }

    public Integer fetchServiceNodePorts(V1Service service,
                                         String portName) throws KubernetesClientException
    {
        return service.getSpec()
                      .getPorts()
                      .stream() //
                      .filter(portItem -> portItem.getName().equals(portName)) //
                      .map(V1ServicePort::getNodePort)
                      .findFirst()
                      .orElseThrow(() -> new KubernetesClientException("The service node port could not be fetched"));
    }

    public Integer fetchServicePorts(V1Service service,
                                     String portName) throws KubernetesClientException
    {
        return service.getSpec()
                      .getPorts()
                      .stream() //
                      .filter(p -> p.getName().contains(NETCONF))
                      .map(V1ServicePort::getPort)
                      .findFirst()
                      .orElseThrow(() -> new KubernetesClientException("The service port could not be fetched"));
    }

    private String fetchNodeIp() throws KubernetesClientException
    {
        try
        {
            return this.api.listNode(null, null, null, null, "!node-role.kubernetes.io/master", null, null, null, null, null)
                           .getItems()
                           .stream()
                           .flatMap(node -> node.getStatus().getAddresses().stream())
                           .filter(addr -> addr.getType().equals("InternalIP"))
                           .map(V1NodeAddress::getAddress)
                           .findFirst()
                           .orElseThrow(() -> new KubernetesClientException("The node IP could not be fetched"));
        }
        catch (ApiException e)
        {
            throw new KubernetesClientException("Error when retrieving nodes of the cluster", e);
        }
    }

    public Pair<String, Integer> fetchYangProviderInfo() throws KubernetesClientException
    {
        try
        {
            var svc = this.api.listNamespacedService(this.namespace, null, null, null, null, CMYP_K8S_NAME_ANNOTATION, null, null, null, null, null)
                              .getItems()
                              .parallelStream()
                              .filter(p -> p.getSpec()
                                            .getType()//
                                            .toString()
                                            .equals(LB_SVC_TYPE))
                              .findFirst()
                              .orElseThrow(() -> new KubernetesClientException("Cannot find a cm-yang-provider in the cluster"));
            log.info("Fetching ip/port info for svc {}", svc.getMetadata().getName());

            if (svc.getSpec().getType().equals(LB_SVC_TYPE))
            {
                var ingress = svc.getStatus().getLoadBalancer().getIngress();

                if (ingress != null && !ingress.isEmpty())
                {
                    log.debug("Using LoadBalancer IP and port");

                    var ip = ingress.get(0).getIp();
                    var port = this.fetchServicePorts(svc, NETCONF);

                    return Pair.of(ip, port);
                }
                else
                {
                    // ip is pending due to port conflicts
                    log.debug("LoadBalancer seems to be in state <Pending> due to port conflicts on 80 or 443. Falling back to combination of <NodeIP>:<NodePort>.");
                    var ip = this.fetchNodeIp();
                    var port = this.fetchServiceNodePorts(svc, NETCONF);

                    return Pair.of(ip, port);
                }
            }
            else
            {
                log.debug("Service type is not LoadBalancer. Using <NodeIP>:<NodePort>");
                var ip = this.fetchNodeIp();
                var port = this.fetchServiceNodePorts(svc, NETCONF);

                return Pair.of(ip, port);
            }
        }
        catch (ApiException e)
        {
            throw new KubernetesClientException("Error when retrieving CM yang provider service", e);
        }
    }
}
