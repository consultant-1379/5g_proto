package com.ericsson.sc.rxkms;

import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;
import java.time.Duration;
import java.net.URISyntaxException;
import javax.net.ssl.SSLHandshakeException;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy;
import org.testcontainers.vault.VaultContainer;
import org.testcontainers.k3s.K3sContainer;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.Assert;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;
import org.testcontainers.containers.BindMode;
import org.testcontainers.utility.DockerImageName;
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
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.core.buffer.Buffer;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.Ports;

import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.reactivex.VertxInstance;

import io.reactivex.Observable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import com.github.dockerjava.api.command.InspectContainerResponse;

public class UtilitiesTest
{

    private static final String VAULT_K8S_HEALTH_PATH = "/v1/sys/health";
    private VaultContainer<?> vaultContainer;
    private K3sContainer k3s;
    private NetworkImpl network;
    private SetupTestEnv setup;
    private KmsClient client;
    private KmsParameters params;
    private KmsClientUtilities utilities;
    // private ConsulContainer consulContainer;

    private static final Logger log = LoggerFactory.getLogger(RxKmsInterfaceTest.class);

    @BeforeClass(groups = { "tls", "nonTls" })
    public void before() throws IOException, InterruptedException, ExecutionException, URISyntaxException
    {
        this.params = KmsParameters.instance;
        this.setup = new SetupTestEnv();
        setup.setupComplete(params, "180s");

        this.utilities = KmsClientUtilities.get(KmsParameters.instance, "encrypt-role");
        this.utilities.getReady().retry().blockingAwait();
    }

    @AfterClass(groups = { "tls", "nonTls" })
    public void after() throws IOException, InterruptedException, ExecutionException, URISyntaxException
    {
        // this.utilities.dispose();
        this.setup.cleanUpComplete();
    }

    @Test(groups = { "tls", "nonTls" })
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection2() throws InterruptedException, IOException
    {
        Optional<String> encrypted = this.utilities.encryptBase64Blocking(new String(Base64.getEncoder().encode("secret text".getBytes()),
                                                                                     StandardCharsets.UTF_8));
        Assert.assertTrue(encrypted.isPresent());
        log.debug(encrypted.get());

    }

    @Test(groups = { "tls", "nonTls" })
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection3() throws InterruptedException, IOException
    {
        Optional<String> encrypted = this.utilities.encryptBase64Blocking(new String(Base64.getEncoder().encode("secret text".getBytes()),
                                                                                     StandardCharsets.UTF_8));
        Assert.assertTrue(encrypted.isPresent());
        log.debug(encrypted.get());

        Optional<String> decrypted = this.utilities.decryptBlocking(encrypted.get());
        Assert.assertTrue(decrypted.isPresent());
        Assert.assertEquals(decrypted.get(), "secret text");
        log.debug(decrypted.get());

    }

}