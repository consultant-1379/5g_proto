/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 10, 2019
 *     Author: xchrfar
 */

package com.ericsson.utilities.cassandra;

import java.util.AbstractMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.reactivex.FutureUtil;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.CompletableSubject;

/**
 * RxJava wrapper for the Cassandra driver {@link CqlSession}
 */
public final class RxSession
{
    public static final long DEFAULT_INIT_TIMEOUT_MILLIS = -1; // Infinite timeout
    public static final long DEFAULT_INIT_RETRIES = 0; // No retries
    public static final long DEFAULT_CLOSE_TIMEOUT_MILLIS = 10 * 1000L; // 10 seconds

    private static final Logger log = LoggerFactory.getLogger(RxSession.class);

    private final CompletableSubject stopSubject = CompletableSubject.create();
    private final Single<SessionHolder> holder;

    public static final class SessionHolder
    {
        private final CqlSession cqlSession;

        public Flowable<ReactiveRow> executeReactive(Statement<?> statement)
        {
            return Flowable.fromPublisher(this.cqlSession.executeReactive(statement))
                           .doOnSubscribe(disp -> log.trace("Executing statement with session {}", this));

        }

        /**
         * Create built prepared statements
         * 
         * @param <K>         An enumeration that shall be used as a key for the
         *                    prepared statements
         * @param clazz       The enumeration class
         * @param definitions A map that holds the statements to be prepared
         * @return A Single that emits a map holding the built prepared statements when
         *         subscribed
         */
        public <K extends Enum<K>> Single<PreparedStatements<K>> prepare(Class<K> clazz,
                                                                         Map<K, SimpleStatement> queries)
        {
            return Observable //
                             .fromIterable(queries.entrySet())
                             .flatMapSingle(entry -> prepare(entry.getValue()) //
                                                                              .doOnError(throwable -> log.error("Failed to create prepared statement {} -> {}'",
                                                                                                                entry.getKey(),
                                                                                                                entry.getValue().getQuery()))
                                                                              .map(preparedStatement -> new AbstractMap.SimpleEntry<K, PreparedStatement>(entry.getKey(),
                                                                                                                                                          preparedStatement)) //
                             )
                             .toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, () -> new EnumMap<K, PreparedStatement>(clazz))
                             .map(PreparedStatements::new)
                             .doOnError(e -> log.error("Could not prepare Cassandra statements", e));
        }

        /**
         * Create a prepared statement
         * 
         * @param statement The statement to prepare
         * @return A Single that emits the prepared statement upon subscription
         */
        public Single<PreparedStatement> prepare(SimpleStatement statement)
        {
            return Single.defer(() -> FutureUtil.singleFromFuture(cqlSession.prepareAsync(statement) //
                                                                            .toCompletableFuture()));
        }

        /**
         * Close session gracefully
         * 
         * @param closeTimeoutMillis The maximum time to wait until session is release,
         *                           zero or negative number to close immediately
         * @return
         */
        public Completable close()
        {
            return close(getCqlSession()).doOnComplete(() -> log.info("Closed session {}", this))
                                         .doOnError(err -> log.error("Failed to close session {}", this, err))
                                         .onErrorComplete()
                                         .cache();
        }

        public boolean isClosed()
        {
            return getCqlSession().isClosed();
        }

        public Single<Boolean> checkSchemaAgreement()
        {
            return Single.defer(() -> singleFromFuture(this.cqlSession.checkSchemaAgreementAsync()));
        }

        public CqlSession getCqlSession()
        {
            return this.cqlSession;
        }

        private SessionHolder(CqlSession cqlSession)
        {
            this.cqlSession = cqlSession;
        }

        private static Completable close(CqlSession cqlSession)
        {
            return Completable.defer(() -> completableFromFuture(cqlSession.closeAsync()));
        }
    }

    public static Builder builder()
    {
        return new Builder();
    }

    private RxSession(final Builder buildr)
    {
        Objects.requireNonNull(buildr);
        Objects.requireNonNull(buildr.config);
        final var config = buildr.config;

        final var initTimeoutMillis = buildr.initTimeoutMillis;
        final var connectionRetries = buildr.connectionRetries;
        final var dynamicTls = buildr.dynamicTls;
        final var openSsl = buildr.openSsl;

        final var init = dynamicTls != null ? dynamicTls.start() : Completable.complete();

        this.holder = init.toSingle(() ->
        {
            if (!openSsl)
            {
                return CqlSession.builder().withConfigLoader(config);
            }
            else
            {

                final var eb = buildr.dynamicTls != null ? EnhancedBuilder.create(dynamicTls.getKeyManager(), dynamicTls.getTrustManager())
                                                         : EnhancedBuilder.create();
                return eb.withConfigLoader(config);
            }
        })
                          .doOnSubscribe(disp -> log.info("Cassandra session handler is starting"))
                          .doOnSuccess(x -> log.info("Cassandra session handler is shutting down"))
                          .flatMap(sessionBuilder -> timeout(singleFromFuture(sessionBuilder.buildAsync()),
                                                             initTimeoutMillis).doOnSubscribe(disp -> log.info("Initializing Cassandra session"))
                                                                               .onErrorResumeNext(err -> err instanceof CompletionException
                                                                                                         && err.getCause() != null ? Single.error(err.getCause())
                                                                                                                                   : Single.error(err)))
                          .retryWhen(new RetryFunction() //
                                                        .withDelay(1000)
                                                        .withRetries(connectionRetries)
                                                        .withRetryAction((err,
                                                                          cnt) -> log.warn("Retry cassandra session creation, retry: {}", cnt, err))
                                                        .create())
                          .map(SessionHolder::new)
                          .doOnError(err -> log.error("Cassandra session cration failed", err))
                          .doOnSuccess(s -> log.debug("Initialized Cassandra session"))

                          .takeUntil(stopSubject)
                          .cache();
    }

    // Helper function to apply timeout operator if given timeout is > 0
    private static Single<CqlSession> timeout(Single<CqlSession> sess,
                                              long timeoutMillis)
    {
        return (timeoutMillis > 0) ? sess.timeout(timeoutMillis, TimeUnit.MILLISECONDS) : sess;
    }

    public Completable close()
    {
        return Completable.complete() //
                          .doOnComplete(this.stopSubject::onComplete)
                          .andThen(sessionHolder().flatMapCompletable(SessionHolder::close));
    }

    public Single<SessionHolder> sessionHolder()
    {
        return this.holder;
    }

    public Completable testInitialConnection()
    {
        return sessionHolder().flatMapCompletable(SessionHolder::close);
    }

    /**
     * Returns a Single that emits the value of the CompletionStage, its error or
     * NoSuchElementException if it signals null.
     * 
     * @param <T>    the value type
     * @param future the source CompletionStage instance
     * @return the new Single instance
     */
    private static <T> Single<T> singleFromFuture(CompletionStage<T> future)
    {
        return FutureUtil.singleFromFuture(future.toCompletableFuture());
    }

    /**
     * Returns a Completable that terminates when the given CompletionStage
     * terminates.
     * 
     * @param future the source CompletionStage instance
     * @return the new Completable instance
     */
    public static Completable completableFromFuture(CompletionStage<Void> future)
    {

        return FutureUtil.completableFromFuture(future.toCompletableFuture());
    }

    public static final class Builder
    {
        private long initTimeoutMillis = DEFAULT_INIT_TIMEOUT_MILLIS;
        private long connectionRetries = DEFAULT_INIT_RETRIES;
        private DriverConfigLoader config;
        private DynamicTlsCertManager dynamicTls;
        private boolean openSsl = true;

        private Builder()
        {
        }

        /**
         * 
         * @param timeout Maximum time in milliseconds to allow for the successfull
         *                initialization of the driver. Negative or zero value indicate
         *                no timeout
         * @return
         */
        public Builder withInitTimeoutMillis(long timeout)
        {
            this.initTimeoutMillis = timeout;
            return this;
        }

        public Builder withInitRetries(long retries)
        {
            this.connectionRetries = retries;
            return this;
        }

        public Builder withConfig(DriverConfigLoader config)
        {
            this.config = config;
            return this;
        }

        public Builder withDynamicTls(DynamicTlsCertManager dynamicTls)
        {
            this.dynamicTls = dynamicTls;
            this.openSsl = true;
            return this;
        }

        public Builder withOpenSsl(boolean useOpenSsl)
        {
            this.openSsl = useOpenSsl;
            return this;
        }

        public RxSession build()
        {
            return new RxSession(this);
        }
    }
}
