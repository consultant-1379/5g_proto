package com.ericsson.sc.rxkms;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.Optional;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.concurrent.atomic.AtomicReference;
import java.util.UUID;
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

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.CompositeDisposable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// import com.github.dockerjava.api.command.InspectContainerResponse;

public class StressTest
{

    private static final String VAULT_K8S_HEALTH_PATH = "/v1/sys/health";
    private final CompositeDisposable disposables = new CompositeDisposable();
    private VaultContainer<?> vaultContainer;
    private K3sContainer k3s;
    private NetworkImpl network;
    private SetupTestEnv setup;
    private KmsClient client;
    private KmsParameters params;
    private KmsClientUtilities utilities;
    private HashMap<String, String> uuidMap;
    private ArrayList<String> uuidArray;
    // private ConsulContainer consulContainer;

    private static final Logger log = LoggerFactory.getLogger(StressTest.class);

    @BeforeClass(groups = { "stress" })
    public void before() throws IOException, InterruptedException, ExecutionException, URISyntaxException
    {
        this.uuidMap = new HashMap<String, String>();
        this.uuidArray = new ArrayList<String>();
        this.params = KmsParameters.instance;
        this.setup = new SetupTestEnv();
        setup.setupComplete(params, "10s");

        this.utilities = KmsClientUtilities.get(KmsParameters.instance, "encrypt-role");
        this.utilities.getReady().blockingAwait();
    }

    @AfterClass(groups = { "stress" })
    public void after() throws IOException, InterruptedException, ExecutionException, URISyntaxException
    {
        disposables.dispose();
        this.utilities.dispose();
        this.setup.cleanUpComplete();
    }

    @Test(groups = { "stress" })
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection2() throws InterruptedException, IOException
    {

        Observable.interval(200L, TimeUnit.MILLISECONDS).take(5L, TimeUnit.MINUTES).map(tick ->
        {
            String uuid = UUID.randomUUID().toString().replaceAll("_", "");
            log.trace("UUID: " + uuid);

            Disposable encrypted = this.utilities.encrypt(new String(Base64.getEncoder().encode(uuid.getBytes()), StandardCharsets.UTF_8)).subscribe(resp ->
            {
                Assert.assertTrue(resp.isPresent());
                log.info(resp.get());
                this.uuidMap.put(resp.get(), uuid);
                this.uuidArray.add(resp.get());
            }, err ->
            {
                log.error("Error: ", err);
                Assert.assertTrue(false);
            });
            disposables.add(encrypted);

            return tick;
        }).takeUntil(c -> c == 1000).lastOrError().blockingGet();

        Observable.interval(200L, TimeUnit.MILLISECONDS).take(5L, TimeUnit.MINUTES).map(tick ->
        {
            String uuid = uuidArray.get(tick.intValue());
            log.trace("UUID: " + uuid);

            Disposable decrypted = this.utilities.decrypt(uuid).subscribe(resp ->
            {
                Assert.assertTrue(resp.isPresent());
                Assert.assertEquals(resp.get(), uuidMap.get(uuid));

                log.info(resp.get());
            }, err ->
            {
                log.error("Error: ", err);
                Assert.assertTrue(false);
            });
            disposables.add(decrypted);

            return tick;
        }).takeUntil(c -> c == 1000).lastOrError().blockingGet();

    }

}