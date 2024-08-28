package com.ericsson.sc.proxyal.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.LogLimitter;
import com.ericsson.sc.util.LogLimitter.Loggers;
import com.ericsson.utilities.common.EnvVars;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import io.vertx.core.http.HttpVersion;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.client.WebClient;

public class PvtbApiService
{
    private enum Lbl
    {
        PVTB_RESPONSE
    }

    private static final long WINDOW_DURATION_MILLIS = 600 * 1000L;
    private static final Logger log = LoggerFactory.getLogger(PvtbApiService.class);
    private Loggers<Lbl> safeLog = LogLimitter.create(Lbl.class, log, WINDOW_DURATION_MILLIS);
    private static final String MSG_ENV_VAR_NOT_FOUND = "Environamental variable {} not found. Aborting PVTB creation of PVTB polling";

    private static final String ENV_VTAP_ENABLED = "VTAP_ENABLED";
    private static final String ENV_VTAP_DOMAIN = "VTAP_DOMAIN";
    private static final String ENV_VTAP_PROTOCOL = "VTAP_PROTOCOL";
    private static final String ENV_POD_NAME = "POD_NAME";
    private static final String ENV_PVTB_HOST = "PVTB_HOST";
    private static final String ENV_PVTB_PORT = "PVTB_API_PORT";
    private static final String ENV_PVTB_POLLING_INTERVAL_SECONDS = "PVTB_POLLING_INTERVAL_SECONDS";

    public static final int HTTP_KEEP_ALIVE_TIMEOUT_SECS = 24 * 60 * 60;
    public static final int HTTP_CONNECT_TIMEOUT_MILLIS = 10 * 1000;
    public static final int HTTP_REQUEST_TIMEOUT_MILLIS = 10 * 1000;

    private static final String JSON_KEY_CONFIG = "config";
    private static final String JSON_KEY_PROTOCOLS = "protocols";

    public final String pvtbProtocol;
    public final String pvtbHost;
    public final Integer pvtbPort;

    private Long pollingIntervalMillis = 50 * 1000L;
    private Subject<Optional<PvtbConfig>> pvtbConfigs = BehaviorSubject.createDefault(Optional.of(new PvtbConfig(false))).toSerialized();
    private final Completable pvtbPollingChain;
    private final String domain;
    private final String podName;
    private final String pvtbUrl;
    private final WebClient client;

    // PvtbApiService
    public PvtbApiService(String host,
                          Integer port,
                          String domain,
                          String protocol,
                          String podName,
                          Vertx vertxInstance)
    {
        this.pvtbHost = host;
        this.pvtbPort = port;
        this.domain = domain;
        this.pvtbProtocol = protocol;
        this.podName = podName;

        this.client = createWebClient(vertxInstance);

        this.pvtbUrl = new StringBuilder().append("http://")
                                          .append(this.pvtbHost)
                                          .append(":")
                                          .append(this.pvtbPort)
                                          .append("/api/v1/config")
                                          .append("/")
                                          .append(this.domain)
                                          .append("/")
                                          .append(this.podName)
                                          .toString();

        this.pvtbPollingChain = Observable.interval(0, pollingIntervalMillis, TimeUnit.MILLISECONDS)
                                          .flatMapCompletable(tick -> this.requestPvtbConfig())
                                          .doOnSubscribe(s -> log.info("Started PVTB config polling"))
                                          .doFinally(() -> this.closeWebClient().subscribe());
    }

    public PvtbApiService(String host,
                          Integer port,
                          String domain,
                          String protocol,
                          String podName,
                          Long pollingIntervalMillis,
                          Vertx vertxInstance)
    {
        this(host, port, domain, protocol, podName, vertxInstance);
        this.pollingIntervalMillis = pollingIntervalMillis;
    }

    /**
     * Creates a new PvtbService from environmental variables.
     * 
     * @return a new PvtbService or empty optional if environmental variables are
     *         missing.
     */
    public static Optional<PvtbApiService> fromEnvVars(Vertx vertxInstance)
    {
        if (EnvVars.get(ENV_VTAP_ENABLED) == null || EnvVars.get(ENV_VTAP_ENABLED).equalsIgnoreCase("FALSE"))
        {
            return Optional.empty();
        }

        var pvtbHost = EnvVars.get(ENV_PVTB_HOST);
        if (pvtbHost == null || pvtbHost.isEmpty())
        {
            log.error(MSG_ENV_VAR_NOT_FOUND, ENV_PVTB_HOST);
            return Optional.empty();
        }

        var pvtbPortString = EnvVars.get(ENV_PVTB_PORT);
        if (pvtbPortString == null || pvtbPortString.isEmpty())
        {
            log.error(MSG_ENV_VAR_NOT_FOUND, ENV_PVTB_PORT);
            return Optional.empty();
        }
        Integer pvtbPort;
        try
        {
            pvtbPort = Integer.valueOf(pvtbPortString);
        }
        catch (NumberFormatException e)
        {
            log.error("Environmental Variable for PVTB port has incorrect format: {}", e.getMessage());
            return Optional.empty();
        }

        var pvtbDomain = EnvVars.get(ENV_VTAP_DOMAIN);
        if (pvtbDomain == null || pvtbDomain.isEmpty())
        {
            log.error(MSG_ENV_VAR_NOT_FOUND, ENV_VTAP_DOMAIN);
            return Optional.empty();
        }

        var pvtbProtocol = EnvVars.get(ENV_VTAP_PROTOCOL);
        if (pvtbProtocol == null || pvtbProtocol.isEmpty())
        {
            log.error(MSG_ENV_VAR_NOT_FOUND, ENV_VTAP_DOMAIN);
            return Optional.empty();
        }

        var podName = EnvVars.get(ENV_POD_NAME);
        if (podName == null || podName.isEmpty())
        {
            log.error(MSG_ENV_VAR_NOT_FOUND, ENV_POD_NAME);
            return Optional.empty();
        }

        var pvtbPollingIntervalString = EnvVars.get(ENV_PVTB_POLLING_INTERVAL_SECONDS);
        if (pvtbPollingIntervalString != null && !pvtbPollingIntervalString.isEmpty())
        {
            try
            {
                var pvtbPollingInterval = Long.valueOf(pvtbPollingIntervalString);
                return Optional.of(new PvtbApiService(pvtbHost, pvtbPort, pvtbDomain, pvtbProtocol, podName, pvtbPollingInterval * 1000L, vertxInstance));
            }
            catch (NumberFormatException e)
            {
                log.error("Environmental Variable for PVTB polling interval has incorrect format: {}", e.getMessage());
            }
        }

        return Optional.of(new PvtbApiService(pvtbHost, pvtbPort, pvtbDomain, pvtbProtocol, podName, vertxInstance));
    }

    /**
     * Starts the polling towards PVTB and emits the result to BehaviorSubject
     * 
     * @return A Completable that performs the operation upon subscription
     */
    public Completable run()
    {
        log.info("Starting PVTB config polling");
        return this.pvtbPollingChain.cache();
    }

    /**
     * Sends a request towards PVTB and processes the result. The result is emitted
     * to a BehaviorSubject.
     * 
     * @return A Single that performs the operation upon subscription
     */
    Completable requestPvtbConfig()
    {
        return this.client.getAbs(this.pvtbUrl).timeout(HTTP_REQUEST_TIMEOUT_MILLIS).rxSend().doOnSuccess(resp ->
        {
            safeLog.log(Lbl.PVTB_RESPONSE,
                        logger -> logger.debug("Response from PVTB:{}, {}, {}", resp.statusCode(), resp.statusMessage(), resp.bodyAsString()));
            if (resp.statusCode() == 200)
            {
                var jsonBodyProtocols = extractPvtbProtocols(resp.bodyAsJsonObject());
                var pvtbConfig = new PvtbConfig(jsonBodyProtocols.contains(this.pvtbProtocol));
                pvtbConfigs.onNext(Optional.of(pvtbConfig));
                log.debug("Emitting PVTB configuration: {}", pvtbConfig);
            }
            else
            {
                log.error("Negative response from PVTB. :{} {} {}", resp.statusCode(), resp.statusMessage(), resp.bodyAsString());
            }
        }).doOnError(e -> log.error("Request towards PVTB failed", e)).ignoreElement().onErrorComplete();

    }

    /**
     * Extract the protocols that are applied for a specific domain in the PVTB.
     * 
     * @param jsonBody
     * @return All the protocols as a List<String>, or an empty List if the
     *         structure of the JsonBody is unexpected;
     */
    private List<String> extractPvtbProtocols(JsonObject jsonBody)
    {
        if (!jsonBody.containsKey(JSON_KEY_CONFIG))
        {
            return List.of();
        }

        var jsonBodyConfig = jsonBody.getJsonObject(JSON_KEY_CONFIG);
        if (!jsonBodyConfig.containsKey(JSON_KEY_PROTOCOLS))
        {
            return List.of();
        }

        return jsonBodyConfig.getJsonArray(JSON_KEY_PROTOCOLS).stream().map(String::valueOf).collect(Collectors.toList());
    }

    public Flowable<Optional<PvtbConfig>> getPvtbConfigs()
    {
        return pvtbConfigs.toFlowable(BackpressureStrategy.LATEST);
    }

    /**
     * Creates a new vertx web client to query PVTB api.
     * 
     * @return a simple WebClient.
     */
    private static WebClient createWebClient(Vertx vertxInstance)
    {
        return WebClient.create(vertxInstance, new WebClientOptions().setProtocolVersion(HttpVersion.HTTP_1_1));
    }

    /**
     * Starts the polling towards PVTB and emits the result to BehaviorSubject
     * 
     * @return A Completable that performs the operation upon subscription
     */
    private Completable closeWebClient()
    {
        return Completable.fromAction(this.client::close)
                          .subscribeOn(Schedulers.io())
                          .doOnError(e -> log.error("Unexpected error while closing web client", e))
                          .onErrorComplete();
    }

}
