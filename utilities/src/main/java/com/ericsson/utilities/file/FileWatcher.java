/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 10, 2020
 *     Author: eaoknkr
 */

package com.ericsson.utilities.file;

import java.io.File;
import java.io.FileFilter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.file.FileAlterationListenerImpl.EventKind;
import com.ericsson.utilities.file.FileAlterationListenerImpl.FileEvent;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Wrapper class for FileSystemWatcher
 */
public class FileWatcher extends FileAlterationListenerAdaptor
{
    private static final Logger log = LoggerFactory.getLogger(FileWatcher.class);

    private final FileAlterationMonitor monitor;
    private final BehaviorSubject<Optional<FileEvent>> behaviorSubject;
    private final String pathToSupervise;
    private AtomicBoolean isRunning;

    public FileWatcher(final String pathToSupervise,
                       final FileFilter filter)
    {
        this.pathToSupervise = pathToSupervise.endsWith("/") ? pathToSupervise : (pathToSupervise + "/");
        this.behaviorSubject = BehaviorSubject.create();
        var observer = new FileAlterationObserver(this.pathToSupervise, filter);
        observer.addListener(new FileAlterationListenerImpl(this.behaviorSubject));

        // Create a File Change Listener
        this.monitor = new FileAlterationMonitor();
        this.monitor.addObserver(observer);
        this.isRunning = new AtomicBoolean(false);
    }

    public Completable start()
    {
        return Completable.defer(() ->
        {
            if (!this.isRunning.compareAndExchange(false, true))
            {
                // In case files already there and eg. pod restarts, scale out etc.

                if (new File(this.pathToSupervise).exists())
                    this.behaviorSubject.toSerialized().onNext(Optional.of(new FileEvent(new File(this.pathToSupervise), EventKind.CREATE)));

                log.info("Starting file monitor.");
                this.monitor.start();
            }

            return Completable.complete();
        })//
                          .doOnSubscribe(m -> log.info("Starting file watcher."));
    }

    public Completable stop()
    {
        return Completable.defer(() ->
        {
            this.behaviorSubject.onComplete();

            if (this.isRunning.compareAndExchange(true, false))
            {
                log.info("Stopping file monitor.");
                this.monitor.stop();
            }

            return Completable.complete();
        }).doOnSubscribe(m -> log.info("Stopping file watcher."));
    }

    public Flowable<FileEvent> getFileChangesStream()
    {
        return this.behaviorSubject.toFlowable(BackpressureStrategy.LATEST).filter(Optional::isPresent).map(Optional::get);
    }
}
