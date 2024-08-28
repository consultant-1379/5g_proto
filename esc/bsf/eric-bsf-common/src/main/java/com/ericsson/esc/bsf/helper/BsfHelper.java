package com.ericsson.esc.bsf.helper;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.rxkms.KmsClientUtilities;
import com.ericsson.utilities.reactivex.RetryFunction;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class BsfHelper
{
    private static final Logger log = LoggerFactory.getLogger(BsfHelper.class);

    private BsfHelper()
    {
    }

    public static String desideListenIP(String ipFamily) throws UnknownHostException
    {

        if (Objects.nonNull(ipFamily) && ipFamily.contains("4"))
        {

            log.debug("using ipv4 value");
            return Arrays.stream(InetAddress.getAllByName(InetAddress.getLocalHost().getHostName()))
                         .filter(Inet4Address.class::isInstance)
                         .findAny()
                         .orElseThrow(() -> new UnknownHostException("IP_VERSION was set to 4 but cluster does not support this"))
                         .getHostAddress();
        }
        else if (Objects.nonNull(ipFamily) && ipFamily.contains("6"))
        {
            log.debug("using ipv6 value");
            return Arrays.stream(InetAddress.getAllByName(InetAddress.getLocalHost().getHostName()))
                         .filter(Inet6Address.class::isInstance)
                         .findAny()
                         .orElseThrow(() -> new UnknownHostException("IP_VERSION was set to 6 but cluster does not support this"))
                         .getHostAddress();
        }
        else
        {
            log.debug("using default value");
            return InetAddress.getLocalHost().getHostAddress();
        }

    }

    public static Completable monitorKms(KmsClientUtilities kmsCLient)
    {
        return Completable.defer(kmsCLient::getReady)
                          .timeout(1, TimeUnit.MINUTES)
                          .subscribeOn(Schedulers.io())
                          .doOnComplete(() -> log.debug("Connected to KMS!"))
                          .doOnError(error -> log.error("Unable to connect or login to KMS, error: ", error))
                          .onErrorComplete()
                          .delay(15, TimeUnit.SECONDS)
                          .repeat();
    }

    public static Single<Optional<String>> decrypt(KmsClientUtilities kmsCLient,
                                                   String key)
    {
        return Single.defer(() -> kmsCLient.decrypt(key))
                     .timeout(10, TimeUnit.SECONDS)
                     .retryWhen(new RetryFunction().withDelay(10 * 1000L)
                                                   .withRetries(-1)
                                                   .withRetryAction((error,
                                                                     retry) -> log.error("Unable to decrypt key, error: ", error))
                                                   .create());
    }
}
