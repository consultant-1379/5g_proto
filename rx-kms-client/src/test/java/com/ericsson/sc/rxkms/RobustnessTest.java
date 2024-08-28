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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Future;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.startupcheck.IndefiniteWaitOneShotStartupCheckStrategy;
import org.testcontainers.vault.VaultContainer;
import org.testcontainers.k3s.K3sContainer;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
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
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.reactivex.RetryFunction;

// import com.github.dockerjava.api.command.InspectContainerResponse;

public class RobustnessTest
{

    private static final String VAULT_K8S_HEALTH_PATH = "/v1/sys/health";
    private VaultContainer<?> vaultContainer;
    private K3sContainer k3s;
    private NetworkImpl network;
    private SetupTestEnv setup;
    private KmsClient client;
    private KmsParameters params;
    private KmsClientUtilities utilities;
    private HashMap<String, String> uuidMap;
    private ArrayList<String> uuidArray;
    private Thread setupThread;
    private final CompositeDisposable disposables = new CompositeDisposable();
    // private ConsulContainer consulContainer;

    private static final Logger log = LoggerFactory.getLogger(RobustnessTest.class);

    @BeforeMethod(groups = { "robust" })
    public void before() throws IOException, InterruptedException, ExecutionException, URISyntaxException
    {
        this.uuidMap = new HashMap<String, String>();
        this.uuidArray = new ArrayList<String>();
        this.params = KmsParameters.instance;
        this.setup = new SetupTestEnv();
        // setup.setupComplete(params, "10s");

        this.utilities = KmsClientUtilities.get(KmsParameters.instance, "encrypt-role");
        this.setupThread = new Thread(() ->
        {
            log.info("sleep before setting up KMS");
            try
            {
                Thread.sleep(30000L);
                this.setup.setupComplete(params, "10s");
            }
            catch (InterruptedException e)
            {
                log.error("Thread interrupted", e);
            }
            catch (IOException e)
            {
                log.error("Thread interrupted", e);
            }

        });
        this.setupThread.start();
    }

    @AfterMethod(groups = { "robust" })
    public void after() throws IOException, InterruptedException, ExecutionException, URISyntaxException
    {
        this.disposables.clear();
        this.utilities.dispose();
        this.setup.cleanUpComplete();
    }

    @Test(groups = { "robust" }, invocationCount = 3)
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection2() throws InterruptedException, IOException
    {
        log.info("Start wait");
        boolean test = this.utilities.getReady().retry().doOnComplete(() ->
        {
            log.info("Client is ready");
        }).doOnError(err ->
        {
            log.error("Client failed to get ready", err);
        }).blockingAwait(2, TimeUnit.MINUTES);
        Assert.assertTrue(test);
        Optional<String> encrypted = this.utilities.encryptBase64Blocking(new String(Base64.getEncoder().encode("secret text".getBytes()),
                                                                                     StandardCharsets.UTF_8));
        Assert.assertTrue(encrypted.isPresent());
        log.info(encrypted.get());
    }

    /*
     * Start traffic, restart Kms during traffic
     */
    @Test(groups = { "robust" }, timeOut = 320000)
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection4() throws InterruptedException, IOException
    {
        log.info("Start wait");
        AtomicInteger counter = new AtomicInteger(0);

        Single<Boolean> req = Observable.interval(200L, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io()).take(3L, TimeUnit.MINUTES).map(tick ->
        {
            String uuid = UUID.randomUUID().toString().replaceAll("_", "");
            log.info("UUID: " + uuid + " Tick:" + tick);

            Disposable encrypted = Single.defer(() -> this.utilities.encrypt(new String(Base64.getEncoder().encode(uuid.getBytes()), StandardCharsets.UTF_8)))
                                         .retryWhen(new RetryFunction().withDelay(10 * 1000L)
                                                                       .withRetries(5)
                                                                       .withRetryAction((err,
                                                                                         i) ->
                                                                       {
                                                                           log.info("Retry for tick: " + tick + " time " + i);
                                                                       })
                                                                       .create())
                                         .onErrorReturnItem(Optional.<String>of(""))
                                         .subscribeOn(Schedulers.io())
                                         .doOnTerminate(() ->
                                         {
                                             log.info("Finished with Tick: " + tick);
                                         })
                                         .doOnSubscribe(sub ->
                                         {
                                             log.info("Subscribed tick: " + tick);
                                         })
                                         .subscribe(resp ->
                                         {
                                             Assert.assertTrue(resp.isPresent());
                                             Assert.assertNotEquals(resp.get(), "");
                                             log.info(resp.get());
                                             // this.uuidMap.put(resp.get(), uuid);
                                             // this.uuidArray.add(resp.get());
                                             counter.incrementAndGet();
                                         }, err ->
                                         {
                                             log.error("Error: ", err);
                                         });
            disposables.add(encrypted);
            return true;
        }).doOnTerminate(() ->
        {
            log.info("The loop has terminated");
        }).last(false);

        new Thread(() ->
        {
            log.info("sleep before restarting vault");
            try
            {
                Thread.sleep(1000L * 100);
                log.info("Restart Vault");
                this.setup.restartVault(this.params.globalTlsEnabled, "10s");
                Thread.sleep(35000L);
                log.info("Restart Vault");
                this.setup.restartVault(this.params.globalTlsEnabled, "10s");
            }
            catch (InterruptedException e)
            {
                log.error("Thread interrupted", e);
            }

        }).start();

        this.utilities.getReady().subscribeOn(Schedulers.io()).doOnComplete(() ->
        {
            log.info("Client is ready");
        }).doOnError(err ->
        {
            log.error("Client failed to get ready", err);
        }).retry().andThen(req).blockingGet();

        log.info("" + counter.get());
        Assert.assertTrue(counter.get() > 890);
    }

}