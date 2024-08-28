package com.ericsson.utilities.reactivex;

import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Utility class
 */
public class FutureUtil
{
    private FutureUtil()
    {
    }

    /**
     * Convert a {@link ListenableFuture} to {@link Single}
     * 
     * @param <T>    The Future type
     * @param future The Future to convert
     * @return A Single that emits whenever the Future is complete
     */
    public static <T> Single<T> toSingle(ListenableFuture<T> future)
    {
        return Single.create(source ->
        {
            final var callback = new FutureCallback<T>()
            {
                @Override
                public void onSuccess(T result)
                {
                    if (!source.isDisposed())
                    {
                        source.onSuccess(result);
                    }
                }

                @Override
                public void onFailure(Throwable t)
                {
                    if (!source.isDisposed())
                    {
                        source.onError(t);
                    }
                }
            };
            source.setCancellable(() -> future.cancel(false));

            Futures.addCallback(future, callback, MoreExecutors.directExecutor());
        });
    }

    /**
     * Returns a Single that emits the value of the CompletableFuture, its error or
     * NoSuchElementException if it signals null.
     * 
     * @param <T>               the value type
     * @param completableFuture The CompletableFuture to convert
     * @return the new Single instance
     */
    public static <T> Single<T> singleFromFuture(CompletableFuture<T> completableFuture)
    {
        return Single.create(emitter ->
        {
            completableFuture.whenComplete((value,
                                            error) ->
            {
                if (emitter.isDisposed())
                    return;
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

    public static Completable completableFromFuture(CompletableFuture<Void> completableFuture)
    {
        return Completable.create(emitter ->
        {
            completableFuture.whenComplete((value,
                                            error) ->
            {
                if (emitter.isDisposed())
                    return;
                if (error != null)
                {
                    emitter.onError(error);
                }
                else
                {
                    emitter.onComplete();
                }
            });

            emitter.setCancellable(() -> completableFuture.cancel(true));
        });
    }

}
