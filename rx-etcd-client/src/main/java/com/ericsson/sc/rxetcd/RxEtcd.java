/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 27, 2019
 *     Author: emldpng
 */

package com.ericsson.sc.rxetcd;

import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.util.tls.DynamicTlsCertManager;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Txn;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.etcd.jetcd.kv.TxnResponse;
import io.etcd.jetcd.lease.LeaseGrantResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.etcd.jetcd.lease.LeaseRevokeResponse;
import io.etcd.jetcd.lease.LeaseTimeToLiveResponse;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.LeaseOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchResponse;
import io.netty.handler.ssl.SslProvider;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

/**
 * RxJava wrapper for jetcd library. Can be used to perform etcd operations in a
 * reactive manner.
 */
public class RxEtcd
{
    /**
     * Builder for RxEtcd objects.
     */
    public static class Builder
    {
        private String user;
        private String password;
        private String endpoint;
        private long requestTimeout;
        private TimeUnit requestTimeoutUnit;
        private int maxThreads = 2000;
        private int connectionRetries;
        private boolean tlsEnabled = false;
        private DynamicTlsCertManager dynamicTls;

        protected Builder()
        {
        }

        // Copy constructor
        protected Builder(Builder builder)
        {
            Objects.requireNonNull(builder);
            this.user = builder.user;
            this.password = builder.password;
            this.endpoint = builder.endpoint;
            this.requestTimeout = builder.requestTimeout;
            this.requestTimeoutUnit = builder.requestTimeoutUnit;
            this.maxThreads = builder.maxThreads;
            this.connectionRetries = builder.connectionRetries;
            this.tlsEnabled = builder.tlsEnabled;
            this.dynamicTls = builder.dynamicTls;
        }

        /**
         * Create new builder initialized with default values
         * 
         * @return The created builder
         */
        public static Builder create()
        {
            return new Builder();
        }

        /**
         * @return The etcd user name
         */
        public String getUser()
        {
            return user;
        }

        /**
         * Set the etcd user name
         * 
         * @param A non null user name
         */
        public Builder withUser(String user)
        {
            this.user = user;
            return this;
        }

        /**
         * 
         * @return The etcd password
         */
        public String getPassword()
        {
            return password;
        }

        /**
         * Set the etcd password
         * 
         * @param password A non null password
         */
        public Builder withPassword(String password)
        {
            this.password = password;
            return this;
        }

        /**
         * Get etcd endpoint
         * 
         * @return The etcd endpoint
         */
        public String getEndpoint()
        {
            return endpoint;
        }

        /**
         * Set etcd endpoint. This is a URI
         * 
         * @param endpoint The non null endpoint
         */
        public Builder withEndpoint(String endpoint)
        {
            this.endpoint = endpoint;
            return this;
        }

        /**
         * Get the request timeout.The unit is configured with
         * {@link #withRequestTimeout(long)}
         * 
         * @return The request timeout
         */
        public long getRequestTimeout()
        {
            return requestTimeout;
        }

        /**
         * Set the request timeout for all etcd operations
         * 
         * @param requestTimeout     The nonnegative request timeout
         * @param requestTimeoutUnit The unit of the request timeout
         */
        public Builder withRequestTimeout(long requestTimeout,
                                          TimeUnit requestTimeoutUnit)
        {
            // Sanitize timeout
            this.requestTimeout = requestTimeout > 0 ? requestTimeout : 0;
            this.requestTimeoutUnit = requestTimeoutUnit;
            return this;
        }

        /**
         * Get the maximum thread count for the etcd driver threadpool
         * 
         * @return the maximum number of threads
         */
        public int getMaxThreads()
        {
            return maxThreads;
        }

        /**
         * Sets the maximum thread count for the etcd driver threadpool
         * 
         * @param maxThreads A positive number
         * @return a buider with max number of Threads set
         */
        public Builder withMaxThreads(int maxThreads)
        {
            this.maxThreads = maxThreads;
            return this;
        }

        /**
         * Get the connection retries, to be used for initial connection to etcd
         * database
         * 
         * @return The number of connection retries
         */
        public int getConnectionRetries()
        {
            return connectionRetries;
        }

        /**
         * Sets the number of connection retires. RxEtcd will try to re-initialize the
         * etcd driver in case of error, following a linear backoff pattern.
         * 
         * @param connectionRetries A positive number, or zero, if no retries are
         *                          desired
         * @return a buider with connectionRetries set
         */
        public Builder withConnectionRetries(int connectionRetries)
        {
            this.connectionRetries = connectionRetries;
            return this;
        }

        public Builder withTls()
        {
            this.tlsEnabled = true;
            return this;
        }

        public Builder withDynamicTls(DynamicTlsCertManager dynamicTls)
        {
            withTls();
            this.dynamicTls = dynamicTls;
            return this;
        }

        /**
         * Build a new RxEtcd object, as configured by the builder.
         * 
         * @return The built object
         * @throws SSLException The sslException from error in the TLS - communication
         *                      of the client
         */
        public RxEtcd build()
        {

            return new RxEtcd(this);
        }

        public Builder copy()
        {
            return new Builder(this);

        }

    }

    private static final Logger log = LoggerFactory.getLogger(RxEtcd.class);

    /**
     * The maximum threadpool size for the jetcd executor
     */

    private final Single<Client> clientSingle;
    private final long requestTimeout;
    private final TimeUnit requestTimeoutUnit;
    private final Optional<DynamicTlsCertManager> dynamicTls;

    protected RxEtcd(Builder builder)
    {
        Objects.requireNonNull(builder.endpoint);
        Objects.requireNonNull(builder.requestTimeoutUnit);

        if (builder.requestTimeout < 0)
        {
            throw new IllegalArgumentException("Invalid requestTimeout " + builder.requestTimeout);
        }
        if (builder.connectionRetries < 0)
        {
            throw new IllegalArgumentException("Invalid connectionRetries " + builder.connectionRetries);
        }

        log.debug("Initializing ETCD client.");
        this.requestTimeout = builder.requestTimeout;
        this.requestTimeoutUnit = builder.requestTimeoutUnit;
        this.dynamicTls = Optional.ofNullable(builder.dynamicTls);
        final Completable init = this.dynamicTls.map(DynamicTlsCertManager::start) //
                                                .orElse(Completable.complete());
        this.clientSingle = init //
                                .andThen(tryCreateClient(builder))
                                .cache();
    }

    /**
     * Creates a new builder
     * 
     * @return The created builder
     */
    public static Builder newBuilder()
    {
        return Builder.create();
    }

    /**
     * Ensure that etcd is ready
     * 
     * @return A Completable that completes as soon as etcd driver has been
     *         initialized and connected to etcd database.
     */
    public Completable ready()
    {
        return this.clientSingle.ignoreElement();
    }

    /**
     * Closes the client and releases its resources.
     * 
     * @return Returns when the client is terminated.
     */
    public Completable close()
    {
        return clientSingle //
                           .flatMapCompletable(RxEtcd::close)
                           .andThen(this.dynamicTls.map(DynamicTlsCertManager::terminate).orElse(Completable.complete()))
                           .onErrorComplete();
    }

    private static Completable close(Client client)
    {
        return Completable.fromAction(client::close)
                          .subscribeOn(Schedulers.io()) // Possibly blocking operation
                          .doOnSubscribe(disp -> log.debug("Closing ETCD client"))
                          .doOnComplete(() -> log.debug("Terminated ETCD client"))
                          .doOnError(e -> log.warn("Error while terminating ETCD client", e))
                          .onErrorComplete();
    }

    /**
     * Writes a key-value pair in the ETCD cluster.
     * 
     * @param key   The key to write.
     * @param value The value of the key.
     * @return Single<PutResponse> The response of the put request.
     */
    public Single<PutResponse> put(ByteSequence key,
                                   ByteSequence value)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(client.getKVClient().put(key, value), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * Writes a key-value pair in the ETCD cluster.
     * 
     * @param key    The key to write.
     * @param value  The value of the key.
     * @param option Options to use.
     * @return Single<PutResponse> The response of the put request.
     */
    public Single<PutResponse> put(ByteSequence key,
                                   ByteSequence value,
                                   PutOption option)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(client.getKVClient().put(key, value, option), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * Queries ETCD cluster for the value of the given key.
     * 
     * @param key The key to fetch.
     * @return Single<GetResponse> The response of the get request.
     */
    public Single<GetResponse> get(ByteSequence key)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(client.getKVClient().get(key), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * Queries ETCD cluster for the value of the given key.
     * 
     * @param key    The key to fetch.
     * @param option Options to use.
     * @return Single<GetResponse> The response of the get request.
     */
    public Single<GetResponse> get(ByteSequence key,
                                   GetOption option)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(client.getKVClient().get(key, option), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * Deletes a key-value pair from the ETCD cluster.
     * 
     * @param key The key to delete.
     * @return Single<DeleteResponse> The response of the delete request.
     */
    public Single<DeleteResponse> delete(ByteSequence key)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(client.getKVClient().delete(key), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * Deletes a key-value pair from the ETCD cluster.
     * 
     * @param key    The key to delete.
     * @param option Options to use.
     * @return Single<DeleteResponse> The response of the delete request.
     */
    public Single<DeleteResponse> delete(ByteSequence key,
                                         DeleteOption option)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(client.getKVClient().delete(key, option), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * An atomic transaction.
     * 
     * @param txnArguments transaction wrap or chain
     * @return Single<TxnResponse> The response of the transaction.
     */
    public Single<TxnResponse> txn(UnaryOperator<Txn> txnArguments)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(txnArguments.apply(client.getKVClient().txn()).commit(), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * Gets the lease that has a given leaseId.
     * 
     * @param leaseId The id of the lease.
     * @param option  Options to use.
     * @return Single<LeaseTimeToLiveResponse> The response of the lease get.
     */
    public Single<LeaseTimeToLiveResponse> getLease(long leaseId,
                                                    LeaseOption option)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(client.getLeaseClient().timeToLive(leaseId, option), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * Creates a lease with a given TTL.
     * 
     * @param ttl Time to live for the lease.
     * @return Single<LeaseGrantResponse> The response of the lease grant.
     */
    public Single<LeaseGrantResponse> leaseCreate(long ttl)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(client.getLeaseClient().grant(ttl), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * Revokes a lease. Any key associated with the given lease id will be removed.
     * 
     * @param leaseId The id of the lease.
     * @return Single<LeaseGrantResponse> The response of the lease grant.
     */
    public Single<LeaseRevokeResponse> leaseRevoke(long leaseId)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(client.getLeaseClient().revoke(leaseId), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * Renews a lease for a period equal to its existing TTL from the moment of
     * renewal.
     * 
     * @param leaseId The id of the lease.
     * @return Single<LeaseGrantResponse> The response of the lease grant.
     */
    public Single<LeaseKeepAliveResponse> leaseRenewOnce(long leaseId)
    {
        return clientSingle.flatMap(client -> singleFromFuture(timeout(client.getLeaseClient().keepAliveOnce(leaseId), //
                                                                       this.requestTimeout,
                                                                       this.requestTimeoutUnit)));
    }

    /**
     * Creates a Watcher that receives watch notifications when a key is modified.
     * 
     * @param key The key to watch.
     * @return Flowable<WatchResponse> A Flowable with watch notifications.
     */
    public Flowable<WatchResponse> watch(ByteSequence key)
    {
        return this.watch(key, null);
    }

    /**
     * Creates a Watcher that receives watch notifications when a key or set of keys
     * are modified.
     * 
     * @param key    The key to watch.
     * @param option Options to use.
     * @return Flowable<WatchResponse> A Flowable with watch notifications.
     */
    public Flowable<WatchResponse> watch(ByteSequence key,
                                         WatchOption option)
    {
        return clientSingle.flatMapPublisher(client -> Flowable.<WatchResponse>create(emitter ->
        {
            final var listener = Watch.listener(event ->
            {
                if (!emitter.isCancelled())
                {
                    emitter.onNext(event);
                    log.debug("Watch event: {}", event);
                }
            }, error ->
            {
                log.warn(error.getMessage());
                emitter.onError(error);
            }, () ->
            {
                if (!emitter.isCancelled())
                    emitter.onComplete();
                log.debug("ETCD watcher completed.");
            });

            Watch.Watcher watcher = (option == null) ? client.getWatchClient().watch(key, listener) //
                                                     : client.getWatchClient().watch(key, option, listener);

            emitter.setCancellable(() ->
            {
                watcher.close();
                log.debug("ETCD watcher was disposed.");
            });

        }, BackpressureStrategy.ERROR));
    }

    /**
     * Returns the timeout interval that is applied to all requests.
     * 
     * @return long The requestTimeout.
     */
    public long getRequestTimeout()
    {
        return requestTimeout;
    }

    /**
     * Returns the time unit of the timeout interval.
     * 
     * @return TimeUnit The requestTimeoutUnit.
     */
    public TimeUnit getRequestTimeoutUnit()
    {
        return requestTimeoutUnit;
    }

    private Single<Client> tryCreateClient(Builder builder)
    {
        // Sanitize retries.
        final int attempts = builder.connectionRetries > 0 ? builder.connectionRetries : 1;

        return createClient(builder).flatMap(this::testClientConnection)
                                    .doOnError(e -> log.warn("Failed to initialize etcd client: ", e))
                                    .retryWhen(errors -> errors.takeWhile(error -> builder.connectionRetries > 0)
                                                               .zipWith(Flowable.range(1, attempts),
                                                                        (n,
                                                                         i) -> i)
                                                               .flatMap(i ->
                                                               {
                                                                   var delay = (i * 200 + (i - 1) * 200);
                                                                   log.debug("Retrying ETCD connection in {} s", (double) delay / 1000);
                                                                   return Flowable.timer(delay, TimeUnit.MILLISECONDS);
                                                               }))
                                    .onErrorResumeNext(err -> Single.error(new IllegalArgumentException("Failed to connect to ETCD", err)));
    }

    private Single<Client> testClientConnection(Client client)
    {
        final var testKey = ByteSequence.from("__".getBytes());
        final var test = Completable.defer(() -> singleFromFuture(timeout(client.getKVClient().get(testKey), //
                                                                          this.requestTimeout,
                                                                          this.requestTimeoutUnit)).ignoreElement());

        return test.onErrorResumeNext(err -> close(client) //
                                                          .andThen(Completable.error(err)))
                   .toSingleDefault(client);
    }

    private Single<Client> createClient(Builder builder)
    {

        final var cb = Client.builder()//
                             .executorService(new ThreadPoolExecutor(0,
                                                                     builder.maxThreads,
                                                                     60L, //
                                                                     TimeUnit.SECONDS,
                                                                     new SynchronousQueue<>()))
                             .endpoints(builder.endpoint)
                             .maxInboundMessageSize(Integer.MAX_VALUE)
                             .keepaliveWithoutCalls(true)
                             .keepaliveTime(Duration.ofSeconds(20)) // 20 seconds
                             .keepaliveTimeout(Duration.ofSeconds(5)); // Timeout after 5 seconds
        if (builder.tlsEnabled)
        {
            try
            {
                cb.sslContext(ctx -> ctx.sslProvider(SslProvider.OPENSSL)
                                        .keyManager(builder.dynamicTls.getKeyManager())
                                        .trustManager(builder.dynamicTls.getTrustManager())
                                        .protocols(List.of("TLSv1.2", "TLSv1.3")));
            }
            catch (SSLException e)
            {
                // This error is fatal
                throw new IllegalArgumentException("Unable to configure TLS for etcd client", e);
            }
        }
        else
        {
            cb.user(ByteSequence.from(builder.user.getBytes())).password(ByteSequence.from(builder.password.getBytes()));
        }

        return Single.fromCallable(cb::build) //
                     .doOnError(err -> log.debug("Failed to create etcd client", err));
    }

    /**
     * Applies timeout to a Completable Future if the timeout period is a positive
     * long. Otherwise, it returns the Completable Future, as it is.
     * 
     * @param future  A Completable Future.
     * @param timeout The timeout period.
     * @param unit    The time unit of the timeout period.
     * @return CompletableFuture<T> Returns the Completable Future with timeout if
     *         the timeout parameter is a positive long, otherwise returns the
     *         future parameter.
     */
    public static <T> CompletableFuture<T> timeout(CompletableFuture<T> future,
                                                   long timeout,
                                                   TimeUnit unit)
    {
        if (timeout > 0)
            return future.orTimeout(timeout, unit);
        else
            return future;
    }

    /**
     * Returns a Single that emits the value of the CompletableFuture, its error or
     * NoSuchElementException if it signals null.
     * 
     * @param <T>               the value type
     * @param completableFuture The CompletableFuture to convert
     * @return the new Single instance
     */
    // FIXME Use common utility function instead of private implementation
    public static <T> Single<T> singleFromFuture(CompletableFuture<T> completableFuture)
    {
        return Single.create(emitter ->
        {
            completableFuture.whenComplete((value,
                                            error) ->
            {
                if (emitter.isDisposed())
                {
                    return;
                }
                if (error != null)
                {
                    emitter.onError(error);
                }
                else if (value != null)
                {
                    emitter.onSuccess(value);
                }
                else
                {
                    emitter.onError(new NoSuchElementException());
                }
            });

            emitter.setCancellable(() -> completableFuture.cancel(true));
        });
    }
}
