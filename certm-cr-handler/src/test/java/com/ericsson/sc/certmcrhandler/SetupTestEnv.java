package com.ericsson.sc.certmcrhandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.testcontainers.k3s.K3sContainer;
import org.testcontainers.containers.BindMode;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.Container;

import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.api.model.apiextensions.v1.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.ConfigMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetupTestEnv
{

    private static final Logger log = LoggerFactory.getLogger(SetupTestEnv.class);
    private static final String K3S_NETWORK_ALIAS = "kubernetes";
    private static final String K3S_IMAGE = "rancher/k3s:v1.21.3-k3s1";
    private static final String user = System.getenv("USER");
    private static final String NAMESPACE = "5g-bsf-" + user;
    private static final String CONFIG_MAP_NAME = "test-config-map";

    private Network network;
    private K3sContainer k3s;
    private KubernetesClient client = null;

    SetupTestEnv()
    {
        this.network = Network.builder().build();
        log.debug("Network has been created");
    }

    public void cleanUpComplete()
    {
        this.k3s.stop();
    }

    public void setupComplete() throws IOException
    {
        log.info("Started environment setup");
        this.initK3sContainer();
        log.debug("K3s container started");
        this.setupK8sEnv();
    }

    static void modifyFile(String filePath,
                           String oldString,
                           String newString)
    {
        File fileToBeModified = new File(filePath);
        String oldContent = "";
        BufferedReader reader = null;
        FileWriter writer = null;

        try
        {
            reader = new BufferedReader(new FileReader(fileToBeModified));
            String line = reader.readLine();

            while (line != null)
            {
                oldContent = oldContent + line + System.lineSeparator();
                line = reader.readLine();
            }

            String newContent = oldContent.replaceAll(oldString, newString);
            writer = new FileWriter(fileToBeModified);
            writer.write(newContent);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                reader.close();
                writer.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void initK3sContainer()
    {
        modifyFile("src/test/resources/testContainerDeployment.yaml", "userNamespace", user);

        this.k3s = new K3sContainer(DockerImageName.parse(K3S_IMAGE)).withNetwork(this.network)
                                                                     .withNetworkAliases(K3S_NETWORK_ALIAS)
                                                                     .withClasspathResourceMapping("/testContainerDeployment.yaml",
                                                                                                   "/home/",
                                                                                                   BindMode.READ_ONLY)
                                                                     .withClasspathResourceMapping("/certm-cr-handler-test.tar", "/home/", BindMode.READ_ONLY);
        this.k3s.start();
    }

    private void setupK8sEnv() throws IOException
    {
        String kubeConfigYaml = k3s.getKubeConfigYaml();
        log.trace("K8s config yaml: " + kubeConfigYaml);

        Config config = Config.fromKubeconfig(kubeConfigYaml);

        try
        {
            client = new io.fabric8.kubernetes.client.KubernetesClientBuilder().withConfig(config).build();
        }
        catch (KubernetesClientException e)
        {
            System.err.println("Error creating Kubernetes client: " + e.getMessage());
            // Handle the exception
        }
        log.debug("K8s client has been created");

        Namespace ns = new NamespaceBuilder().withNewMetadata().withName(NAMESPACE).endMetadata().build();
        client.namespaces().resource(ns).create();

        try (InputStream inputStream = SetupTestEnv.class.getResourceAsStream("/external-certificate.yaml"))
        {
            CustomResourceDefinition crd = client.apiextensions().v1().customResourceDefinitions().load(inputStream).createOrReplace();

            // Now you can work with the created CRD
            System.out.println("CRD Name: " + crd.getMetadata().getName());
        }
        catch (KubernetesClientException e)
        {
            System.err.println("Error occurred while creating CRD: " + e.getMessage());
            // Handle KubernetesClientException
        }
        catch (Exception e)
        {
            System.err.println("Error occurred: " + e.getMessage());
            // Handle other exceptions
        }

        try
        {
            Container.ExecResult result = this.k3s.execInContainer("ctr", "images", "import", "/home/certm-cr-handler-test.tar");
            String stdout = result.getStdout();
            int exitCode = result.getExitCode();
            log.info(stdout + " Code: " + exitCode);

            result = this.k3s.execInContainer("kubectl", "apply", "-n", NAMESPACE, "-f", "/home/testContainerDeployment.yaml");
            stdout = result.getStdout();
            exitCode = result.getExitCode();
            log.info(stdout + " Code: " + exitCode);
        }
        catch (IOException e)
        {
            log.error("Error: ", e);
        }
        catch (InterruptedException e)
        {
            log.error("Error: ", e);
        }
    }

    public void addConfigMap(String cmFile)
    {
        ConfigMap configMap = client.configMaps().load(SetupTestEnv.class.getResourceAsStream(cmFile)).item();
        client.configMaps().inNamespace(NAMESPACE).resource(configMap).create();
    }

    public void updateConfigMap(String cmFile)
    {
        ConfigMap configMap = client.configMaps().load(SetupTestEnv.class.getResourceAsStream(cmFile)).item();
        client.configMaps().inNamespace(NAMESPACE).resource(configMap).update();
    }

    public void deleteConfigMap()
    {
        this.client.resources(ConfigMap.class).inNamespace(NAMESPACE).withName(CONFIG_MAP_NAME).delete();
    }

    public String getK3sKubeConfig()
    {
        return this.k3s.getKubeConfigYaml();
    }

    public String getNamespace()
    {
        return NAMESPACE;
    }

    public KubernetesClient getClient()
    {
        return this.client;
    }

}