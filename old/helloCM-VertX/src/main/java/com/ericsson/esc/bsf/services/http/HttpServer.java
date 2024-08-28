package com.ericsson.esc.bsf.services.http;

import com.ericsson.esc.bsf.services.ServiceProvider;
import rx.Completable;

public interface HttpServer extends ServiceProvider {
    Completable startService(int port);
}
