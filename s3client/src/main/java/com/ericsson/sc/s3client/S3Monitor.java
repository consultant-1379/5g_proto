package com.ericsson.sc.s3client;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.file.RxFileWatch;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class S3Monitor
{
    private static final Logger log = LoggerFactory.getLogger(S3Monitor.class);
    private final Path monitoredPath;
    private Flowable<List<File>> monitorNewFiles;

    public S3Monitor(String directory,
                     Long debounceTimeout)
    {
        this.monitoredPath = Path.of(directory);
        log.debug("Path to monitor is: {}", this.monitoredPath);
        this.monitorNewFiles = RxFileWatch.create(this.monitoredPath)
                                          .filter(event -> !event.getKind().equals(StandardWatchEventKinds.ENTRY_DELETE))
                                          .doOnNext(event -> log.info("New file event occured: {}", event.getKind()))
                                          .debounce(debounceTimeout, TimeUnit.MILLISECONDS)
                                          .observeOn(Schedulers.io())
                                          .flatMapSingle(event -> getFiles())
                                          .doOnError(e -> log.error("Error extracting files from directory: {}", directory))
                                          .doOnNext(event -> log.info("New files identified event: {}", event));
    }

    private Single<List<File>> getFiles()
    {
        return Single.fromCallable(() -> Files.walk(this.monitoredPath).filter(Files::isRegularFile).map(Path::toFile).collect(Collectors.toList()))
                     .subscribeOn(Schedulers.io());
    }

    public Flowable<List<File>> getFileEvents()
    {
        return this.monitorNewFiles;
    }
}
