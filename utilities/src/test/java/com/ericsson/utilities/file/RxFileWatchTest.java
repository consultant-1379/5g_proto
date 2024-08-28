package com.ericsson.utilities.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.ericsson.utilities.file.RxFileWatch.Event;

public class RxFileWatchTest
{
    private static final Logger log = LoggerFactory.getLogger(RxFileWatchTest.class);

    @Test(enabled = true)
    private void createTest() throws InterruptedException, IOException
    {

        Path tmpFolder = Files.createTempDirectory("tmpUnitTestFolder");
        tmpFolder.toFile().deleteOnExit();
        log.debug("Temp folder created in: {}", tmpFolder);

        var fWatcher = RxFileWatch.create(tmpFolder).replay().refCount();
        var testObserver = fWatcher.doOnNext(event -> log.debug("New Event: {}", event.toString()))//
                                   .doOnError(err -> err.printStackTrace())//
                                   .test();

        fWatcher.blockingFirst();
        Path fileName = Files.createTempFile(tmpFolder, "tmpFile", null);
        fileName.toFile().deleteOnExit();
        log.debug("File {} created", fileName);

        testObserver.awaitCount(2)
                    .assertValueCount(2)
                    .assertNoErrors()
                    .assertValueAt(1, event -> fileName.equals(event.getPath()) && StandardWatchEventKinds.ENTRY_CREATE.equals(event.getKind()))
                    .dispose();

    }

    @Test(enabled = true)
    private void createModifyTest() throws InterruptedException, IOException
    {

        Path tmpFolder = Files.createTempDirectory("tmpUnitTestFolder");
        tmpFolder.toFile().deleteOnExit();
        log.debug("Temp folder created in: {}", tmpFolder);

        var fWatcher = RxFileWatch.create(tmpFolder).replay().refCount();
        var testObserver = fWatcher.doOnNext(event -> log.debug("New Event: {}", event.toString()))//
                                   .doOnError(err -> err.printStackTrace())//
                                   .test();

        fWatcher.blockingFirst();
        Path fileName = Files.createTempFile(tmpFolder, "tmpFile", null);
        log.debug("File {} created", fileName);
        Files.write(fileName, "modify this file".getBytes(), StandardOpenOption.SYNC);
        log.debug("File {} modified", fileName);

        testObserver.awaitCount(3)
                    .assertValueCount(3)
                    .assertNoErrors()
                    .assertValueAt(1, event -> fileName.equals(event.getPath()) && StandardWatchEventKinds.ENTRY_CREATE.equals(event.getKind()))
                    .assertValueAt(2, event -> fileName.equals(event.getPath()) && StandardWatchEventKinds.ENTRY_MODIFY.equals(event.getKind()))
                    .dispose();

    }

    @Test(enabled = true)
    private void createDeleteTest() throws InterruptedException, IOException
    {

        Path tmpFolder = Files.createTempDirectory("tmpUnitTestFolder");
        tmpFolder.toFile().deleteOnExit();
        log.debug("Temp folder created in: {}", tmpFolder);

        var fWatcher = RxFileWatch.create(tmpFolder).replay().refCount();
        var testObserver = fWatcher.doOnNext(event -> log.debug("New Event: {}", event.toString()))//
                                   .doOnError(err -> err.printStackTrace())//
                                   .test();

        fWatcher.blockingFirst();
        Path fileName = Files.createTempFile(tmpFolder, "tmpFile", null);
        fileName.toFile().deleteOnExit();
        log.debug("File {} created", fileName);
        Files.deleteIfExists(fileName);
        log.debug("File {} deleted", fileName);

        testObserver.awaitCount(3)
                    .assertValueCount(3)
                    .assertNoErrors()
                    .assertValueAt(1, event -> fileName.equals(event.getPath()) && StandardWatchEventKinds.ENTRY_CREATE.equals(event.getKind()))
                    .assertValueAt(2, event -> fileName.equals(event.getPath()) && StandardWatchEventKinds.ENTRY_DELETE.equals(event.getKind()))
                    .dispose();

    }

    @Test(enabled = true)
    private void createModifyDeleteTest() throws InterruptedException, IOException
    {

        Path tmpFolder = Files.createTempDirectory("tmpUnitTestFolder");
        tmpFolder.toFile().deleteOnExit();
        log.debug("Temp folder created in: {}", tmpFolder);

        var fWatcher = RxFileWatch.create(tmpFolder).replay().refCount();
        var testObserver = fWatcher.doOnNext(event -> log.debug("New Event: {}", event.toString()))//
                                   .doOnError(err -> err.printStackTrace())//
                                   .test();

        fWatcher.blockingFirst();
        Path fileName = Files.createTempFile(tmpFolder, "tmpFile", null);
        fileName.toFile().deleteOnExit();
        log.debug("File {} created", fileName);
        Files.write(fileName, "modify this file".getBytes(), StandardOpenOption.SYNC);
        log.debug("File {} modified", fileName);
        Files.deleteIfExists(fileName);
        log.debug("File {} deleted", fileName);

        testObserver.awaitCount(4)
                    .assertValueCount(4)
                    .assertNoErrors()
                    .assertValueAt(1, event -> fileName.equals(event.getPath()) && StandardWatchEventKinds.ENTRY_CREATE.equals(event.getKind()))
                    .assertValueAt(2, event -> fileName.equals(event.getPath()) && StandardWatchEventKinds.ENTRY_MODIFY.equals(event.getKind()))
                    .assertValueAt(3, event -> fileName.equals(event.getPath()) && StandardWatchEventKinds.ENTRY_DELETE.equals(event.getKind()))
                    .dispose();

    }

    @Test(enabled = true)
    private void stressCreateTest() throws InterruptedException, IOException
    {

        final int count = 100;
        Path tmpFolder = Files.createTempDirectory("tmpUnitTestFolder");
        tmpFolder.toFile().deleteOnExit();
        log.debug("Temp folder created in: {}", tmpFolder);

        var fWatcher = RxFileWatch.create(tmpFolder).replay().refCount();
        var testObserver = fWatcher.doOnNext(event -> log.debug("New Event: {}", event.toString()))//
                                   .doOnError(err -> err.printStackTrace())//
                                   .test();

        fWatcher.blockingFirst();

        List<Event> assertEvents = new ArrayList<>();
        assertEvents.add(new Event(RxFileWatch.INIT_EVENT, tmpFolder));

        for (var i = 0; i < count; i++)
        {
            Path fileName = Files.createTempFile(tmpFolder, "tmpFile", null);
            assertEvents.add(new Event(StandardWatchEventKinds.ENTRY_CREATE, fileName));
        }

        testObserver.awaitCount(count + 1).assertValueCount(count + 1).assertNoErrors().assertValueSequence(assertEvents).dispose();

    }

    @Test(enabled = true)
    private void stressCreateDeleteTest() throws InterruptedException, IOException
    {

        final int count = 100;
        Path tmpFolder = Files.createTempDirectory("tmpUnitTestFolder");
        tmpFolder.toFile().deleteOnExit();
        log.debug("Temp folder created in: {}", tmpFolder);

        var fWatcher = RxFileWatch.create(tmpFolder).replay().refCount();
        var testObserver = fWatcher.doOnNext(event -> log.debug("New Event: {}", event.toString()))//
                                   .doOnError(err -> err.printStackTrace())//
                                   .test();

        fWatcher.blockingFirst();

        List<Event> assertEvents = new ArrayList<>();
        assertEvents.add(new Event(RxFileWatch.INIT_EVENT, tmpFolder));

        for (var i = 0; i < count; i++)
        {
            Path fileName = Files.createTempFile(tmpFolder, "tmpFile", null);
            assertEvents.add(new Event(StandardWatchEventKinds.ENTRY_CREATE, fileName));
            Files.deleteIfExists(fileName);
            assertEvents.add(new Event(StandardWatchEventKinds.ENTRY_DELETE, fileName));
        }

        testObserver.awaitCount(2 * count + 1).assertValueCount(2 * count + 1).assertNoErrors().assertValueSequence(assertEvents).dispose();

    }

    @Test(enabled = true)
    private void stressCreateModifyTest() throws InterruptedException, IOException
    {

        final int count = 100;
        Path tmpFolder = Files.createTempDirectory("tmpUnitTestFolder");
        tmpFolder.toFile().deleteOnExit();
        log.debug("Temp folder created in: {}", tmpFolder);

        var fWatcher = RxFileWatch.create(tmpFolder).replay().refCount();
        var testObserver = fWatcher.doOnNext(event -> log.debug("New Event: {}", event.toString()))//
                                   .doOnError(err -> err.printStackTrace())//
                                   .test();

        fWatcher.blockingFirst();

        List<Event> assertEvents = new ArrayList<>();
        assertEvents.add(new Event(RxFileWatch.INIT_EVENT, tmpFolder));

        for (var i = 0; i < count; i++)
        {
            Path fileName = Files.createTempFile(tmpFolder, "tmpFile", null);
            assertEvents.add(new Event(StandardWatchEventKinds.ENTRY_CREATE, fileName));
            Files.write(fileName, "modify this file".getBytes(), StandardOpenOption.SYNC);
            assertEvents.add(new Event(StandardWatchEventKinds.ENTRY_MODIFY, fileName));
        }

        testObserver.awaitCount(2 * count + 1).assertValueCount(2 * count + 1).assertNoErrors().assertValueSequence(assertEvents).dispose();

    }

    @Test(enabled = true)
    private void stressCreateModifyDeleteTest() throws InterruptedException, IOException
    {

        final int count = 100;
        Path tmpFolder = Files.createTempDirectory("tmpUnitTestFolder");
        tmpFolder.toFile().deleteOnExit();
        log.debug("Temp folder created in: {}", tmpFolder);

        var fWatcher = RxFileWatch.create(tmpFolder).replay().refCount();
        var testObserver = fWatcher.doOnNext(event -> log.debug("New Event: {}", event.toString()))//
                                   .doOnError(err -> err.printStackTrace())//
                                   .test();

        fWatcher.blockingFirst();

        List<Event> assertEvents = new ArrayList<>();
        assertEvents.add(new Event(RxFileWatch.INIT_EVENT, tmpFolder));

        for (var i = 0; i < count; i++)
        {
            Path fileName = Files.createTempFile(tmpFolder, "tmpFile", null);
            assertEvents.add(new Event(StandardWatchEventKinds.ENTRY_CREATE, fileName));
            Files.write(fileName, "modify this file".getBytes(), StandardOpenOption.SYNC);
            assertEvents.add(new Event(StandardWatchEventKinds.ENTRY_MODIFY, fileName));
            Files.deleteIfExists(fileName);
            assertEvents.add(new Event(StandardWatchEventKinds.ENTRY_DELETE, fileName));
        }

        testObserver.awaitCount(3 * count + 1).assertValueCount(3 * count + 1).assertNoErrors().assertValueSequence(assertEvents).dispose();

    }
}
