package com.ericsson.esc.bsf.services;

import rx.Completable;

public interface ServiceProvider {
    Completable startService();
    Completable stopService();
}
