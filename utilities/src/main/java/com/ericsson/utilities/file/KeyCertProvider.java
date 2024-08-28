package com.ericsson.utilities.file;

import io.reactivex.Flowable;

public interface KeyCertProvider
{

    Flowable<KeyCert> watchKeyCert();

}