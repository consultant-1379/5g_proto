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
 * Created on: Jan 21, 2020
 *     Author: eaoknkr
 */

package com.ericsson.utilities.file;

import java.io.File;
import java.util.Optional;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.subjects.BehaviorSubject;

/**
 * 
 */
public class FileAlterationListenerImpl implements FileAlterationListener
{
    private static final Logger log = LoggerFactory.getLogger(FileAlterationListenerImpl.class);

    private final BehaviorSubject<Optional<FileEvent>> behaviorSubject;

    public FileAlterationListenerImpl(BehaviorSubject<Optional<FileEvent>> behaviorSubject)
    {
        this.behaviorSubject = behaviorSubject;
    }

    @Override
    public void onStart(FileAlterationObserver observer)
    {
        // Event ignored.
    }

    @Override
    public void onDirectoryCreate(File directory)
    {
        // Event ignored.
    }

    @Override
    public void onDirectoryChange(File directory)
    {
        // Event ignored.
    }

    @Override
    public void onDirectoryDelete(File directory)
    {
        // Event ignored.
    }

    @Override
    public void onFileCreate(File file)
    {
        log.debug("File {} was created. Length: {}", file.getAbsoluteFile(), file.length());
        this.behaviorSubject.toSerialized().onNext(Optional.of(new FileEvent(file.getAbsoluteFile(), EventKind.CREATE, file.length())));
    }

    @Override
    public void onFileChange(File file)
    {
        log.debug("File {} was modified. Length: {}", file.getAbsoluteFile(), file.length());
        this.behaviorSubject.toSerialized().onNext(Optional.of(new FileEvent(file.getAbsoluteFile(), EventKind.MODIFY, file.length())));
    }

    @Override
    public void onFileDelete(File file)
    {
        log.debug("File {} was deleted.", file.getAbsoluteFile());
        this.behaviorSubject.toSerialized().onNext(Optional.of(new FileEvent(file.getAbsoluteFile(), EventKind.DELETE)));
    }

    @Override
    public void onStop(FileAlterationObserver observer)
    {
        // Event ignored.
    }

    public enum EventKind
    {
        CREATE,
        MODIFY,
        DELETE
    }

    public static class FileEvent
    {
        private final File file;
        private final EventKind eventKind;
        private final long length;

        public FileEvent(File file,
                         EventKind eventKind,
                         long length)
        {
            this.file = file;
            this.eventKind = eventKind;
            this.length = length;
        }

        public FileEvent(File file,
                         EventKind eventKind)
        {
            this(file, eventKind, 0);
        }

        public EventKind getEventKind()
        {
            return this.eventKind;
        }

        public File getFile()
        {
            return this.file;
        }

        public long getLength()
        {
            return this.length;
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append("event=")
                                      .append(this.eventKind.toString())
                                      .append(", file=")
                                      .append(this.file.toString())
                                      .append(", length=")
                                      .append(this.length)
                                      .toString();
        }
    }
}
