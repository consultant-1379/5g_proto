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

public class RxKmsInterfaceTest
{

    private static final String VAULT_K8S_HEALTH_PATH = "/v1/sys/health";
    private VaultContainer<?> vaultContainer;
    private K3sContainer k3s;
    private NetworkImpl network;
    private SetupTestEnv setup;
    private KmsClient client;
    private KmsParameters params;
    // private ConsulContainer consulContainer;

    private static final Logger log = LoggerFactory.getLogger(RxKmsInterfaceTest.class);

    @BeforeClass(groups = { "tls", "nonTls" })
    public void before() throws IOException, InterruptedException, ExecutionException, URISyntaxException
    {
        this.params = KmsParameters.instance;
        this.setup = new SetupTestEnv();
        setup.setupComplete(params, "180s");

        this.client = new KmsClient(KmsParameters.instance, "encrypt-role");
        this.client.start();
        this.client.waitReady().retry().blockingGet();
    }

    @AfterClass(groups = { "tls", "nonTls" })
    public void after() throws IOException, InterruptedException, ExecutionException, URISyntaxException
    {
        this.client.stop();
        this.setup.cleanUpComplete();
    }

    @Test(groups = { "tls", "nonTls" })
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection2() throws InterruptedException, IOException
    {
        this.client.waitReady().retry().blockingGet();
        this.client.encryptBase64(new String(Base64.getEncoder().encode("secret text".getBytes()), StandardCharsets.UTF_8)).doOnSuccess(response ->
        {
            Assert.assertNotEquals(response.bodyAsJsonObject().getJsonObject("data").getString("ciphertext"), null);
            Assert.assertEquals(response.statusCode(), 200);
            log.debug(response.bodyAsString());
        }).blockingGet();

    }

    @Test(groups = { "tls", "nonTls" })
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection3() throws InterruptedException, IOException
    {
        this.client.waitReady().retry().blockingGet();
        String encryptedText = this.client.encryptBase64(new String(Base64.getEncoder().encode("secret text".getBytes()), StandardCharsets.UTF_8))
                                          .doOnSuccess(response ->
                                          {
                                              Assert.assertNotEquals(response.bodyAsJsonObject().getJsonObject("data").getString("ciphertext"), null);
                                              Assert.assertEquals(response.statusCode(), 200);
                                              log.debug(response.bodyAsString());
                                          })
                                          .blockingGet()
                                          .bodyAsJsonObject()
                                          .getJsonObject("data")
                                          .getString("ciphertext");

        this.client.decryptPlainText(encryptedText).doOnSuccess(response ->
        {
            String decryptedText = response.bodyAsJsonObject().getJsonObject("data").getString("plaintext");
            Assert.assertEquals(new String(Base64.getDecoder().decode(decryptedText.getBytes()), StandardCharsets.UTF_8), "secret text");
            Assert.assertEquals(response.statusCode(), 200);
            log.debug(response.bodyAsString());
        }).blockingGet();

    }

    @Test(groups = { "tls" })
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection4() throws InterruptedException, IOException
    {
        HttpRequest<Buffer> req = this.client.getWebClient().get(this.params.vaultPort, this.params.vaultHost, VAULT_K8S_HEALTH_PATH);
        req.rxSend()
           .doOnSuccess(response -> System.out.println("Successfully got health status, code:" + response.statusCode() + response.bodyAsString()))
           .doOnError(err -> System.out.println("Failed to get health status " + err.getMessage()))
           .blockingGet();

        this.setup.renewVaultCerts(true);

        req.rxSend()
           .doOnSuccess(response -> System.out.println("Successfully got health status, code:" + response.statusCode() + response.bodyAsString()))
           .doOnError(err -> System.out.println("Failed to get health status " + err.getMessage()))
           .blockingGet();
    }

    @Test(groups = { "tls" })
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection5() throws InterruptedException, IOException
    {
        HttpRequest<Buffer> req = this.client.getWebClient().get(this.params.vaultPort, this.params.vaultHost, VAULT_K8S_HEALTH_PATH);
        boolean flag = false;
        req.rxSend()
           .doOnSuccess(response -> System.out.println("Successfully got health status, code:" + response.statusCode() + response.bodyAsString()))
           .doOnError(err -> System.out.println("Failed to get health status " + err.getMessage()))
           .blockingGet();

        this.setup.renewVaultCerts(false);

        try
        {
            req.rxSend()
               .doOnSuccess(response -> System.out.println("Successfully got health status, code:" + response.statusCode() + response.bodyAsString()))
               .doOnError(err -> System.out.println("Failed to get health status " + err.getMessage()))
               .blockingGet();
        }
        catch (Exception e)
        {
            log.debug("", e);
            flag = true;
        }
        Assert.assertTrue(flag);

    }

    @Test(groups = { "tls" })
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection() throws InterruptedException, IOException
    {

        this.client.stop();
        this.setup.addVaultConfig(true, "18s");
        this.client.start();
        this.client.waitReady().retry().blockingGet();
        AtomicReference<String> oldToken = new AtomicReference<String>("");
        int counter = 0;
        Observable.interval(17L, TimeUnit.SECONDS).take(18 * 5, TimeUnit.SECONDS).map(tick ->
        {
            String token = this.client.getVaultToken();
            log.trace("Tick: " + tick + ", old token: " + oldToken.get());
            log.trace("new token: " + token);
            if (token.equals(oldToken.get()))
            {
                Assert.assertTrue(false);
            }
            oldToken.set(token);
            return tick;
        }).takeUntil(c -> c == 4).lastOrError().blockingGet();

        this.client.stop();
        this.setup.addVaultConfig(true, "180s");
        this.client.start();
        this.client.waitReady().retry().blockingGet();
    }
}