package com.ericsson.adpal.cm;

import java.util.Optional;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * The CM Mediator API
 * 
 * @param <T> The CM model
 */
public interface CmmApi<T>

{
    /**
     * Get configuration from CMM
     * 
     * @return The configuration and relevant eTag, or an empty Optional if
     *         configuration does not exist
     */
    Single<Optional<CmConfig<T>>> getCmConfig();

    /**
     * Update CM configuration.
     * <p>
     * The returned Completable might throw a {@link CmmTransactionException}, in
     * case of eTag mismatch or other concurrency issues. In that case the update
     * may be retried with a different eTag. Furthermore a {@link CmmApiException}
     * might be emitted. In that case the failure may or may not be retried.
     * 
     * @param config
     * @return A Completable that completes when the update is finished
     * 
     */
    Completable updateCmConfig(CmConfig<T> config);

    /**
     * Observe configuration state.
     * <p>
     * The returned observable can be safely re-subscribed.
     * 
     * @return An Observable that, upon subscription, emits the latest known
     *         configuration state, or an empty Optional, if configuration no longer
     *         exists.
     *         <p>
     * 
     */
    Observable<Optional<T>> configUpdates();
}
