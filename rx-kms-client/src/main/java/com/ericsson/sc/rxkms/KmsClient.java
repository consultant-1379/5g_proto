package com.ericsson.sc.rxkms;

import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.reactivex.VertxInstance;
import io.vertx.reactivex.ext.web.client.WebClient;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.HttpRequest;
import io.vertx.reactivex.core.buffer.Buffer;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.CompositeDisposable;

import java.util.concurrent.TimeUnit;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The client that interacts with KMS(Vault) microservice.<br>
 * Actions supported:<br>
 * - Encryption<br>
 * - Decryption<br>
 * The client is parametrized through a KmsParameters object.<br>
 * The client is used through the KmsClientUtilities class, which is a wrapper
 * of this class.<br>
 * 
 * @see KmsParameters
 * @see KmsClientUtilities
 */
public class KmsClient
{
    private static final Logger log = LoggerFactory.getLogger(KmsClient.class);

    private static final String VAULTLOGINPATH = "/v1/auth/kubernetes/login";
    private static final String VAULTENCRYPTPATH = "/v1/transit/encrypt/";
    private static final String VAULTDECRYPTPATH = "/v1/transit/decrypt/";
    private static final int DEFAULTTTL = 20;

    private AtomicBoolean ready;

    private WebClientProvider webClientProvider;
    private WebClient webClient;
    private String vaultToken;
    private String role;
    private long ttl;
    private FileHandler fileHandler;
    private KmsParameters params;
    private Subject<Irrelevant> renew = BehaviorSubject.create();

    private Thread renewThread;
    private Disposable loginObservable;

    private HttpRequest<Buffer> webClientEnc;
    private HttpRequest<Buffer> webClientDec;
    private WebClientProvider.Builder wcb;
    private boolean started;
    private CompositeDisposable disposables;

    enum Irrelevant
    {
        INSTANCE;
    }

    /**
     * The constructor requires a KmsParameters object and a role(String).
     * 
     * @param params KmsParameters for the client
     * @param role   String the name of the role in KMS(Vault)
     * @see KmsParameters
     */
    public KmsClient(KmsParameters params,
                     String role)
    {
        this.role = role;
        this.params = params;
        this.fileHandler = new FileHandler();
        this.ready = new AtomicBoolean(false);
        this.wcb = WebClientProvider.builder();
        this.disposables = new CompositeDisposable();
        log.debug("Kms Rx Client created");
    }

    /**
     * Starts the client. Creates a web client with certificates from SipTls.<br>
     * Uses the web client to login to KMS (Vault) and acquire the token.<br>
     * Renews the login token before it expires.
     * 
     * @throws Exception in case the login to KMS (vault) fails.
     */
    public void start()
    {
        Disposable webClientSingle;
        Disposable renewObservable;

        if (this.params.globalTlsEnabled)
        {
            wcb.withDynamicCaCert(DynamicTlsCertManager.create(SipTlsCertWatch.trustedCert(params.sipTlsRootCaPath), DynamicTlsCertManager.Type.TRUSTED_CERT));
        }
        this.wcb.withFollowRedirectPost();
        this.webClientProvider = this.wcb.build(VertxInstance.getCore());
        webClientSingle = this.webClientProvider.getWebClient().subscribeOn(Schedulers.io()).timeout(10, TimeUnit.SECONDS).retry().subscribe(client ->
        {
            this.webClient = client;
            this.webClientDec = webClient.post(this.params.vaultPort, this.params.vaultHost, VAULTDECRYPTPATH + this.params.keyName);
            this.webClientEnc = webClient.post(this.params.vaultPort, this.params.vaultHost, VAULTENCRYPTPATH + this.params.keyName);
            log.debug("Successfully got web client");
        }, err ->
        {
            log.error("Failed to get web client", err);
            throw new KmsException("Failed to get web client");
        });

        disposables.add(webClientSingle);
        renewObservable = this.renew.toFlowable(BackpressureStrategy.LATEST).subscribeOn(Schedulers.io()).subscribe(irr ->
        {
            this.loginObservable = this.login().subscribeOn(Schedulers.io()).timeout(20, TimeUnit.SECONDS).subscribe(() ->
            {
                log.debug("Successfully loged in to KMS");
                log.debug("Start renew in {}", (900 * this.ttl));
                this.renewThread = renewableThread(900 * this.ttl);
                this.renewThread.start();
            }, err ->
            {
                log.debug("Failed to login to KMS. Retry after 20s", err);
                this.renewThread = renewableThread(DEFAULTTTL * 1000L);
                this.renewThread.start();
            });
            disposables.add(this.loginObservable);
        }, err ->
        {
            log.debug("Failed to login to KMS. Retry after 20s", err);
            this.renewThread = renewableThread(DEFAULTTTL * 1000L);
            this.renewThread.start();
        });

        this.renewThread = renewableThread(2000);
        this.renewThread.start();

        this.disposables.add(renewObservable);
        this.started = true;
    }

    private Thread renewableThread(long timeout)
    {
        if (this.renewThread != null)
            this.renewThread.interrupt();
        return new Thread(() ->
        {
            try
            {
                Thread.sleep(timeout);
                while (this.webClient == null)
                {
                    Thread.sleep(1000 * 2L);
                }
                this.renew.onNext(Irrelevant.INSTANCE);
            }
            catch (InterruptedException e)
            {
                log.debug("Renew thread has been interrupted", e);
            }
        });
    }

    /**
     * Stops the active instance of the client to KMS (Vault).<br>
     * It releases all the resources of the client instance.
     * 
     * @throws Exception in case of error during releasing of resources.
     */
    public void stop()
    {
        try
        {
            this.ready.set(false);
            this.disposables.clear();
            this.vaultToken = "";
            this.ttl = 0;
            this.renewThread.interrupt();
            if (this.webClient != null)
                this.webClient.close();
            if (this.webClientProvider != null)
                this.webClientProvider.close()
                                      .observeOn(Schedulers.io())
                                      .doOnError(e -> log.error("Unexpected error while disposing web client provider", e))
                                      .onErrorComplete();
        }
        catch (Exception e)
        {
            log.error("Failed while stopping KMS client", e);
            throw new KmsException("Failed while stopping KMS client");
        }
        this.started = false;
    }

    /**
     * Sets the state of the active client to not ready.
     */
    public void setNotReady()
    {
        this.ready.set(false);
    }

    /**
     * Return the state of the active client.
     */
    public boolean isReady()
    {
        return this.ready.get();
    }

    /**
     * Wait 20 seconds for the active client to become ready.
     * 
     * @throws exception in case the client is not ready after 20 seconds.
     */
    public Completable waitReady()
    {

        return Completable.fromCallable(() ->
        {
            for (int i = 0; !this.ready.get(); i++)
            {
                Thread.sleep(10000);
                if (i == 2)
                    throw new KmsException("Kms client is not yet ready, timeout");
            }
            return null;
        });
    }

    public WebClientProvider getWebClientProvider()
    {
        return this.webClientProvider;
    }

    public WebClient getWebClient()
    {
        return this.webClient;
    }

    /**
     * Returns the vault token generated after the login.
     * 
     * @return String The vault token
     */
    public String getVaultToken()
    {
        return this.vaultToken;
    }

    private Completable login() throws KmsException
    {
        String jwt = "";
        Optional<String> accountToken = fileHandler.readFile(params.accountTokenPath);
        if (accountToken.isPresent())
        {
            jwt = accountToken.get();
        }
        else
        {
            log.error("Failed to read JWT");
            throw new KmsException("failed to read JWT");
        }

        JsonObject login = new JsonObject();
        login.put("role", this.role);
        login.put("jwt", jwt);

        log.debug("Start login reuest with role {} and jwt {}", this.role, jwt);

        return this.webClient.post(this.params.vaultPort, this.params.vaultHost, VAULTLOGINPATH).rxSendJsonObject(login).doOnSuccess(response ->
        {
            if (response.statusCode() != 200)
            {
                log.debug("Failed to login to Kms. Set TTL to 0.");
                throw new KmsException("failed to login to KMS", response.statusCode(), response.statusMessage());
            }
            log.debug("Successfully login, code: {} {}", response.statusCode(), response.statusMessage());
            this.vaultToken = response.bodyAsJsonObject().getJsonObject("auth").getString("client_token");
            this.ttl = response.bodyAsJsonObject().getJsonObject("auth").getLong("lease_duration");
            log.debug("TTL: {}", this.ttl);
            this.webClientDec.putHeader("X-Vault-Token", this.vaultToken);
            this.webClientEnc.putHeader("X-Vault-Token", this.vaultToken);
            this.ready.set(true);
        }).doOnError(err ->
        {
            this.ready.set(false);
            log.debug("Failed to login to KMS", err);
            throw new KmsException("failed to login to KMS");
        }).timeout(4L, TimeUnit.SECONDS).ignoreElement();
    }

    /**
     * Encrypts the String passed as argument. The string must be base64 encoded.
     * 
     * @param baseText String, Text to be encrypted. Must be base64.
     * @return Single, It contains the response of KMS (Vault).
     */
    public Single<HttpResponse<Buffer>> encryptBase64(String baseText)
    {

        JsonObject encrypt = new JsonObject();
        encrypt.put("plaintext", baseText);

        return this.webClientEnc.rxSendJsonObject(encrypt);
    }

    /**
     * Decrypts the String passed as argument. The string must start with vault:v1:
     * otherwise the response will be 400.
     * 
     * @param plaintext String, Text to be decrypted. Must start with vault:v1:.
     * @return Single, It contains the response of KMS (Vault).
     */
    public Single<HttpResponse<Buffer>> decryptPlainText(String plaintext)
    {

        JsonObject decrypt = new JsonObject();
        decrypt.put("ciphertext", plaintext);

        return this.webClientDec.rxSendJsonObject(decrypt);
    }

    /**
     * Return the status of the active client.
     * 
     * @return boolean, True the client has started. False the client has not
     *         started.
     */
    public boolean hasStarted()
    {
        return this.started;
    }

    /**
     * Retries to login to KMS (Vault).<br>
     * The existing token will become invalid.
     */
    public void restartLogin()
    {

        if (this.ready.get())
        {
            log.debug("Restart login Completable");
            this.renewThread.interrupt();
            this.renewThread = renewableThread(2000);
            this.renewThread.start();
            this.loginObservable.dispose();
            this.ready.set(false);
        }
    }

}
