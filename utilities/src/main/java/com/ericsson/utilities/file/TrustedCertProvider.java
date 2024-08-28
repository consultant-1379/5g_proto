package com.ericsson.utilities.file;

import io.reactivex.Flowable;

public interface TrustedCertProvider
{
    Flowable<TrustedCert> watchTrustedCerts();
}