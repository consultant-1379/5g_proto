package com.ericsson.sc.s3client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Completable;
import io.reactivex.Flowable;

/**
 * Class responsible monitoring the files in ObjectStorage and volumes. In case
 * that files identified in volumes but not in ObjectStorage, it will add those
 * in cache and upload them one after the other in a row.
 */
public class S3Agent
{
    private static final Logger log = LoggerFactory.getLogger(S3Agent.class);
    private S3Parameters parameters;
    private Flowable<List<File>> files;
    private static final String CA_PATH = "/etc/certificates/sip-tls-ca/ca.crt";

    public S3Agent(S3Monitor fileWatcher,
                   S3Parameters parameters)
    {
        this.parameters = parameters;
        this.files = fileWatcher.getFileEvents()//
                                .replay(1)//
                                .autoConnect()//
                                .doOnNext(fs -> log.info("New file emissions {}", fs));
    }

    public Completable start()
    {
        log.debug("New file collected S3Agent is triggered");

        return this.files.throttleLatest(this.parameters.getThrottleObjestStorage(), TimeUnit.SECONDS, true)
                         .onBackpressureLatest()
                         .doOnNext(f -> log.debug("Preparing to upload files {}", f))
                         .concatMapCompletable(f -> uploadFileToBucket(f));
    }

    public Completable uploadFileToBucket(List<File> files)
    {
        log.debug("Uploading files {}", files);
        return Completable.fromAction(() -> files.stream().filter(File::exists).forEach(file ->
        {
            // Create new minio client
            S3MinioClientHandler handler = new S3MinioClientHandler(this.parameters, Paths.get(CA_PATH));

            // Check if Bucket exists and if not create it
            this.checkBucket(handler);

            // UploadFile
            handler.uploadFile(this.parameters.getBucketName(), createObjectName(file), file.getAbsolutePath());
            log.debug("Uploading file:{} to bucket:{} as object:{}", //
                      file.getAbsolutePath(), // get the file full path
                      this.parameters.getBucketName(), // get the bucket name
                      createObjectName(file)); // get the object name

            // verify file is uploaded
            boolean fileUploadedToBucket = verifyUploadedFile(file, handler);
            String result = (fileUploadedToBucket) ? "Object successfully uploaded to bucket" : "Object failed to be uploaded to bucket";
            log.debug(result);
            // delete file from volume
            removeUploadeFile(file);
        }));
    }

    private void checkBucket(S3MinioClientHandler handler)
    {
        try
        {
            String bucket = this.parameters.getBucketName();
            if (!handler.bucketExists(bucket))
            {
                log.debug("Creating bucket {}", bucket);
                handler.createBucket(bucket);
            }
        }
        catch (InvalidKeyException | NoSuchAlgorithmException | IOException e)
        {
            log.error("Failed to find or create bucket.", e);
        }
    }

    private String createObjectName(File file)
    {
        return this.parameters.getObjectPrefix() + "-" + file.getParentFile().getName() + "-" + file.getName();
    }

    private boolean verifyUploadedFile(File file,
                                       S3MinioClientHandler handler)
    {
        // check if uploaded file exists in the bucket
        List<String> bucketObjects = handler.getObjectNames(this.parameters.getBucketName());
        return bucketObjects.stream().anyMatch(bucketObject ->
        {
            log.debug("Checking for Bucket Object: {}", bucketObject);
            return bucketObject.equals(createObjectName(file));
        });
    }

    private void removeUploadeFile(File file)
    {
        if (file.exists())
        {
            // some action to remove file
            log.debug("Deleting file {}", file.getAbsoluteFile().getName());
            try
            {
                Files.delete(file.toPath());
            }
            catch (IOException e)
            {
                log.error("Failed to delete file {}", file.getAbsolutePath(), e);
            }
        }
        else
            log.debug("File {} is not removed because it doesn't exists", file.getAbsoluteFile());
    }
}
