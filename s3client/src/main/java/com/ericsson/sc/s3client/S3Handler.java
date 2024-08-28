package com.ericsson.sc.s3client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.reactivex.RxShutdownHook;

import io.reactivex.Completable;

/**
 * Class to be used for the handling of all needed operations of s3 client
 * service Initialize a client with environmental parameters and pass this to
 * agent Agent will monitor file in ObjectStorage, volumes on specific interval
 * and it will upload files to ObjectStorage that exist in volumes but not in
 * ObjectStorage
 */
public class S3Handler
{
    private static final Logger log = LoggerFactory.getLogger(S3Handler.class);
    private final RxShutdownHook shutdownHook;
    private final S3Parameters parameters;
    private final S3Agent agent;
    private final S3Monitor fileWatcher;

    public S3Handler(S3Parameters parameters,
                     RxShutdownHook shutdownHook)
    {
        this.parameters = parameters;
        this.shutdownHook = shutdownHook;
        this.fileWatcher = new S3Monitor(this.parameters.getMonitorDirectory(), this.parameters.getDebounceTimeout());
        this.agent = new S3Agent(this.fileWatcher, this.parameters);
    }

    public static void main(String[] args)
    {
        int exitStatus = 1;

        try (var shutdownHook = new RxShutdownHook())
        {
            final S3Parameters params = S3Parameters.fromEnvironment();
            log.debug("Environmental variables: {}", params);
            final S3Handler s3Handler = new S3Handler(params, shutdownHook);
//            run from Eclipse
//            ObjectStorageData objectStorageParams = new ObjectStorageData("/home/ekoteva/tmp/s3client", 3000L, 3L, "vaggelis", "trelos-123-");
//            EndpointData endpointDataParams = new EndpointData("localhost", 9000, true);
//            ClientAccess clientAccessParams = new ClientAccess("admin", "rootroot", "/home/ekoteva/tmp/my.crt");
//            ClientTimeout clientTimeoutParams = new ClientTimeout(300, 300, 300);
//            S3Parameters parameters = S3Parameters.with(true, objectStorageParams, endpointDataParams, clientAccessParams, clientTimeoutParams);
//            final S3Handler s3Handler = new S3Handler(parameters, shutdownHook);

            s3Handler.start().blockingAwait();
            log.info("Simple Storage Service (S3) client service terminated normally");
            exitStatus = 0;
        }
        catch (Exception e)
        {
            log.error("Simple Storage Service (S3) client service terminated abnormally due to exception", e);
            exitStatus = 1;
        }

        System.exit(exitStatus);
    }

    private Completable start()
    {
        return Completable.complete()
                          .andThen(this.agent.start())
                          .andThen(Completable.ambArray(this.shutdownHook.get()))
                          .onErrorResumeNext(Completable::error)
                          .andThen(this.stop());
    }

    private Completable stop()
    {
        return Completable.complete();
    }
}
