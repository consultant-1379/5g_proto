package com.ericsson.utilities.http;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface ReconfigurableWebServer extends RouterHandler
{
    <T extends RouterHandler> Single<T> reconfigure(WebServerBuilder builder);

    Completable shutdown();

    Completable shutdown(long timeoutMillis);

    /**
     * The actual port the server is listening on. This is useful if you bound the
     * server specifying 0 as port number signifying an ephemeral port
     * 
     * @return the actual port the server is listening on.
     */
    int actualPort();
}
