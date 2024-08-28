package com.ericsson.sc.rxkms;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;
import java.time.Duration;
import java.net.URISyntaxException;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy;
import org.testcontainers.vault.VaultContainer;
import org.testcontainers.k3s.K3sContainer;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.Assert;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.containers.BindMode;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.containers.Container.ExecResult;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.testcontainers.containers.Network.NetworkImpl;
import org.testcontainers.containers.Network;

import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import io.fabric8.kubernetes.api.model.rbac.ClusterRoleBinding;

import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.reactivex.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;

import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.WebClientProvider;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetupTestEnv
{

    private static final Logger log = LoggerFactory.getLogger(SetupTestEnv.class);
    private static final String K3S_NETWORK_ALIAS = "kubernetes";
    private static final String VAULT_NETWORK_ALIAS = "vault";
    private static final String K3S_IMAGE = "rancher/k3s:v1.21.3-k3s1";
    private static final String NAMESPACE = "5g-bsf-zkounik";
    private static final String DEPLOYMENT_RESOURCE = "deployment.yaml";
    private static final String VAULT_RBAC_RESOURCE = "vault-rbac.yaml";
    private static final String VAULT_SA_RESOURCE = "vault-sa.yaml";
    private static final String SA_RESOURCE = "sa.yaml";
    private static final String VAULT_ACCOUNT = "vault-account";
    private static final String VAULT_TOKEN = "VAULT_TOKEN";
    private static final String VAULT_CERT_PATH = "/tmp/unitTests/vault";
    private static final String VAULT_CONFIG_PATH = "/vault/config";
    private static final String VAULT_POLICY_PATH = "/v1/sys/policy/encrypt-policy";
    private static final String VAULT_ROLE_PATH = "/v1/auth/kubernetes/role/encrypt-role";
    private static final String VAULT_K8S_CONFIG_PATH = "/v1/auth/kubernetes/config";
    private static final String CA_CERT_PATH = "/tmp/unitTests/ca";
    private static final int VAULT_TLS_PORT = 8300;
    private static final int VAULT_NON_TLS_PORT = 8200;

    private Network network;
    private VaultContainer<?> vaultContainer;
    private K3sContainer k3s;
    private CertificatesHandler ch;
    private String ca;
    private String vaultJwt;
    private FileHandlerTest fileHandler;
    private WebClientProvider webClientProvider;

    SetupTestEnv()
    {
        this.network = Network.builder().build();
        log.debug("Network has been created");
        this.ch = new CertificatesHandler();
        this.fileHandler = new FileHandlerTest();
        this.ca = "";
        this.vaultJwt = "";
    }

    public void cleanUpComplete()
    {
        if (this.webClientProvider != null)
            this.webClientProvider.close()
                                  .observeOn(Schedulers.io())
                                  .doOnError(e -> log.error("Unexpected error while disposing web client provider", e))
                                  .onErrorComplete();
        if (this.k3s != null)
            this.k3s.stop();
        if (this.vaultContainer != null)
            this.vaultContainer.stop();
        this.fileHandler.deleteFile(new File(VAULT_CERT_PATH + "/cert.pem"));
        this.fileHandler.deleteFile(new File(VAULT_CERT_PATH + "/key.pem"));
        this.fileHandler.deleteFile(new File(CA_CERT_PATH + "/cacertbundle.pem"));
        this.fileHandler.deleteFile(new File(VAULT_CERT_PATH));
        this.fileHandler.deleteFile(new File(CA_CERT_PATH));
    }

    public void setupComplete(KmsParameters params,
                              String vaultTokenTtl) throws IOException
    {
        this.fileHandler.createFolder(new File(VAULT_CERT_PATH));
        this.fileHandler.createFolder(new File(CA_CERT_PATH));
        this.ch.writeCaCert(CA_CERT_PATH + "/cacertbundle.pem");
        log.info("Started environment setup");
        this.initK3sContainer();
        log.debug("K3s container started");
        this.setupK8sEnv(params.accountTokenPath);
        log.debug("K8s resources added");
        this.setupVault(params.globalTlsEnabled);
        log.debug("Vault container started");
        this.addVaultConfig(params.globalTlsEnabled, vaultTokenTtl);
        log.debug("Vault configuration added");
    }

    public void renewVaultCerts(boolean renewClientCaCert) throws java.lang.UnsupportedOperationException, java.io.IOException, java.lang.InterruptedException
    {
        this.ch = new CertificatesHandler("newCA");
        // this.fileHandler.deleteFile(new File(VAULT_CERT_PATH + "/cert.pem"));
        // this.fileHandler.deleteFile(new File(VAULT_CERT_PATH + "/key.pem"));
        this.ch.createSignedCert("/tmp/unitTests/vault/cert.pem", "/tmp/unitTests/vault/key.pem", "vault", List.of("vault", "localhost"));
        if (renewClientCaCert)
        {
            this.fileHandler.deleteFile(new File(CA_CERT_PATH + "/cacertbundle.pem"));
            this.ch.writeCaCert(CA_CERT_PATH + "/cacertbundle.pem");
            log.debug("Overwrite cacert file");
        }
        ExecResult res = this.vaultContainer.execInContainer("pkill", "-SIGHUP", "vault");
        // Wait for keep alive timeout
        Thread.sleep(61000L);
        if (res.getExitCode() != 0)
        {
            log.error(res.getStderr());
        }
        log.debug(res.getStdout());
    }

    public void restartVault(boolean tls,
                             String ttl)
    {
        this.vaultContainer.stop();
        this.vaultContainer.start();
        this.addVaultConfig(tls, ttl);
    }

    private void initK3sContainer()
    {
        this.k3s = new K3sContainer(DockerImageName.parse(K3S_IMAGE)).withNetwork(this.network).withNetworkAliases(K3S_NETWORK_ALIAS);
        this.k3s.start();
    }

    @SuppressWarnings("unchecked")
    private void setupK8sEnv(String accountTokenPath) throws IOException
    {
        String kubeConfigYaml = k3s.getKubeConfigYaml();
        log.trace("K8s config yaml: " + kubeConfigYaml);

        Config config = Config.fromKubeconfig(kubeConfigYaml);

        DefaultKubernetesClient client = new DefaultKubernetesClient(config);
        Namespace ns = new NamespaceBuilder().withNewMetadata().withName(NAMESPACE).endMetadata().build();
        log.debug("K8s client has been created");

        client.namespaces().resource(ns).create();
        Deployment deploy = client.apps().deployments().load(getClass().getClassLoader().getResource(DEPLOYMENT_RESOURCE)).item();
        ClusterRoleBinding vaultRbac = client.rbac().clusterRoleBindings().load(getClass().getClassLoader().getResource(VAULT_RBAC_RESOURCE)).item();
        ServiceAccount sa = client.serviceAccounts().load(getClass().getClassLoader().getResource(SA_RESOURCE)).item();
        ServiceAccount vaultSa = client.serviceAccounts().load(getClass().getClassLoader().getResource(VAULT_SA_RESOURCE)).item();

        // Apply it to Kubernetes Cluster
        client.serviceAccounts().inNamespace(NAMESPACE).resource(sa).create();
        client.rbac().clusterRoleBindings().resource(vaultRbac).create();
        client.serviceAccounts().inNamespace(NAMESPACE).resource(vaultSa).create();
        client.apps().deployments().inNamespace(NAMESPACE).resource(deploy).create();
        log.debug("K8s resources have been created");

        client.serviceAccounts().inNamespace(NAMESPACE).withName(VAULT_ACCOUNT).waitUntilCondition((serviceAcc) ->
        {
            return serviceAcc != null;
        }, 100, TimeUnit.SECONDS);

        vaultSa = client.serviceAccounts().inNamespace(NAMESPACE).withName(VAULT_ACCOUNT).get();

        String secret = vaultSa.getSecrets().get(0).getName();

        this.vaultJwt = client.secrets().inNamespace(NAMESPACE).withName(secret).get().getData().get("token");
        this.vaultJwt = new String(Base64.getDecoder().decode(this.vaultJwt), StandardCharsets.UTF_8);
        log.trace("Vault JWT: " + vaultJwt);

        while (client.pods().inNamespace(NAMESPACE).list().getItems().size() < 1)
            ;

        String podName = client.pods().inNamespace(NAMESPACE).list().getItems().get(0).getMetadata().getName();

        byte[] array = new byte[2000];
        ExecWatch exec = client.pods()
                               .inNamespace(NAMESPACE)
                               .withName(podName)
                               .redirectingOutput()
                               .exec("sh", "-c", "cat /var/run/secrets/kubernetes.io/serviceaccount/token");

        exec.getOutput().read(array);
        System.out.println(client.namespaces().withName(NAMESPACE).get());
        String userJwt = new String(array, StandardCharsets.UTF_8).trim();
        log.info("Succesfully acquired JWT");
        log.debug("USER JWT: " + userJwt);

        this.fileHandler.createFile(new File(accountTokenPath), userJwt);

        Yaml yaml = new Yaml();
        this.ca = ((Map<String, List<Map<String, Map<String, String>>>>) yaml.load(kubeConfigYaml)).get("clusters")
                                                                                                   .get(0)
                                                                                                   .get("cluster")
                                                                                                   .get("certificate-authority-data");
        this.ca = new String(Base64.getDecoder().decode(this.ca), StandardCharsets.UTF_8);
        log.info("Succesfully acquired CA cert");
        log.debug("CA CERT: " + this.ca);
    }

    private void setupVault(boolean tls)
    {
        if (tls)
        {
            log.info("Setting up Vault container with TLS");
            this.ch.createSignedCert("/tmp/unitTests/vault/cert.pem", "/tmp/unitTests/vault/key.pem", "vault", List.of("vault", "localhost"));
        }
        int port = tls ? VAULT_TLS_PORT : VAULT_NON_TLS_PORT;
        Consumer<CreateContainerCmd> cmd = e -> e.withPortBindings(new PortBinding(Ports.Binding.bindPort(port), new ExposedPort(port)));

        this.vaultContainer = new VaultContainer<>("hashicorp/vault:1.13").withVaultToken(VAULT_TOKEN)
                                                                          .withNetwork(this.network)
                                                                          .withNetworkAliases(VAULT_NETWORK_ALIAS)
                                                                          .withEnv("VAULT_LOG_LEVEL", "trace")
                                                                          .withInitCommand("secrets enable transit",
                                                                                           "write -f transit/keys/mykey",
                                                                                           "auth enable kubernetes")
                                                                          .withExposedPorts(port)
                                                                          .withCreateContainerCmdModifier(cmd);

        // if (tls)
        // {
        // MountableFile configFile = MountableFile.forClasspathResource("config.hcl");
        // MountableFile cert = MountableFile.forHostPath(VAULT_CERT_PATH +
        // "/cert.pem");
        // MountableFile key = MountableFile.forHostPath(VAULT_CERT_PATH + "/key.pem");
        // this.vaultContainer = this.vaultContainer.withCopyFileToContainer(configFile,
        // VAULT_CONFIG_PATH + "/config.hcl")
        // .withCopyFileToContainer(cert, VAULT_CONFIG_PATH + "/cert.pem")
        // .withCopyFileToContainer(key, VAULT_CONFIG_PATH + "/key.pem");
        // }

        if (tls)
        {
            MountableFile configFile = MountableFile.forClasspathResource("config.hcl");
            this.vaultContainer = this.vaultContainer.withCopyFileToContainer(configFile, VAULT_CONFIG_PATH + "/config.hcl")
                                                     .withFileSystemBind(VAULT_CERT_PATH + "/cert.pem", VAULT_CONFIG_PATH + "/cert.pem", BindMode.READ_WRITE)
                                                     .withFileSystemBind(VAULT_CERT_PATH + "/key.pem", VAULT_CONFIG_PATH + "/key.pem", BindMode.READ_WRITE);
        }

        this.vaultContainer.setWaitStrategy(Wait.defaultWaitStrategy().withStartupTimeout(Duration.ofSeconds(10)));

        this.vaultContainer.start();
    }

    public void addVaultConfig(boolean tls,
                               String vaultTokenTtl)
    {
        // WebClient vaultClient = WebClient.create(VertxInstance.get());
        final var wcb = WebClientProvider.builder();

        if (tls)
        {
            log.info("Adding Vault with TLS enabled");
            wcb.withDynamicCaCert(DynamicTlsCertManager.create(SipTlsCertWatch.trustedCert(CA_CERT_PATH), DynamicTlsCertManager.Type.TRUSTED_CERT));
        }
        int port = tls ? VAULT_TLS_PORT : VAULT_NON_TLS_PORT;

        this.webClientProvider = wcb.build(VertxInstance.get());
        WebClient vaultClient = webClientProvider.getWebClient().blockingGet();

        JsonObject policy = new JsonObject();
        JsonObject role = new JsonObject();
        JsonObject kubeauth = new JsonObject();
        JsonArray policies = new JsonArray();
        JsonObject login = new JsonObject();
        JsonObject encrypt = new JsonObject();
        JsonObject decrypt = new JsonObject();
        policy.put("policy",
                   "path \"transit/encrypt/mykey\"\n{\n  capabilities = [\"read\", \"update\", \"list\"]\n}\n\npath \"transit/decrypt/mykey\"\n{\n  capabilities = [\"read\", \"update\", \"list\"]\n}");

        policies.add("encrypt-policy");
        role.put("bound_service_account_names", "*-account");
        role.put("bound_service_account_namespaces", NAMESPACE);
        role.put("policies", policies);
        role.put("ttl", "1800");
        role.put("token_ttl", vaultTokenTtl);

        kubeauth.put("kubernetes_host", "https://kubernetes:6443");
        kubeauth.put("token_reviewer_jwt", this.vaultJwt);
        kubeauth.put("disable_local_ca_jwt", true);
        kubeauth.put("kubernetes_ca_cert", this.ca);

        // Thread.sleep(10000000);

        vaultClient.post(this.vaultContainer.getMappedPort(port), this.vaultContainer.getHost(), VAULT_POLICY_PATH)
                   .putHeader("X-Vault-Token", VAULT_TOKEN)
                   .rxSendJsonObject(policy)
                   .doOnSuccess(response -> System.out.println("Successfully added policy, code:" + response.statusCode() + response.statusMessage()))
                   .doOnError(err -> System.out.println("Failed to add policy " + err.getMessage()))
                   .blockingGet();

        vaultClient.post(this.vaultContainer.getMappedPort(port), this.vaultContainer.getHost(), VAULT_ROLE_PATH)
                   .putHeader("X-Vault-Token", VAULT_TOKEN)
                   .rxSendJsonObject(role)
                   .doOnSuccess(response -> System.out.println("Successfully added role, code:" + response.statusCode() + response.statusMessage()))
                   .doOnError(err -> System.out.println("Failed to add role " + err.getMessage()))
                   .blockingGet();

        vaultClient.post(this.vaultContainer.getMappedPort(port), this.vaultContainer.getHost(), VAULT_K8S_CONFIG_PATH)
                   .putHeader("X-Vault-Token", VAULT_TOKEN)
                   .rxSendJsonObject(kubeauth)
                   .doOnSuccess(response -> System.out.println("Successfully added config, code:" + response.statusCode() + response.statusMessage()))
                   .doOnError(err -> System.out.println("Failed to add config " + err.getMessage()))
                   .blockingGet();
    }

}
