package com.ericsson.utilities.logger;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.utilities.file.ConfigmapWatch;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.logger.LogLevelChanger;
import com.ericsson.utilities.logger.LogSeverity;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;

public class LogLevelChangeTest
{

    private static final Logger log = LoggerFactory.getLogger(LogLevelChangeTest.class);
    private static final String TMP_LOG_TEST_FOLDER = "tmpLogTestFolder";
    private static final String TMP_LOG_TEST_FILE = "tmpLogTestFile";
    private Path folder;
    private Path file;
    private static final String ROOT = "ROOT";
    private static final String TEST_CONTAINER = "test";
    private static final ObjectMapper json = Jackson.om() //
                                                    .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
    private LogLevelChanger llc;

    @BeforeClass
    public void beforeClass() throws IOException
    {
        // crate tmp folder
        this.folder = Files.createTempDirectory(TMP_LOG_TEST_FOLDER);
        this.folder.toFile().deleteOnExit();
        log.debug("Temp folder created in: {}", this.folder);

        // create tmp file
        this.file = Files.createTempFile(this.folder, TMP_LOG_TEST_FILE, null);
        this.file.toFile().deleteOnExit();
        log.debug("File {} created", this.file);

        // get content - expected to be empty
        var content = new String(Files.readAllBytes(this.file), StandardCharsets.UTF_8);
        log.debug("File {} content: {}", this.file, content);

        // create new dummy log control and set severity to debug
        var test = new LogSeverity("test", Level.OFF.levelStr);
        var dummy = new LogSeverity("dummy", Level.TRACE.levelStr);
        var severities = new ArrayList<LogSeverity>();
        severities.add(test);
        severities.add(dummy);
        var dummyLogControl = json.writeValueAsString(severities);
        log.debug("Dummy log control data: {}", dummyLogControl);

        // add dummy log control json array to tmp file
        Files.writeString(this.file, dummyLogControl, StandardOpenOption.SYNC);
        log.debug("File {} modified", this.file);

        // get content - expected to be the dummy log control
        content = new String(Files.readAllBytes(this.file), StandardCharsets.UTF_8);
        log.debug("File {} content: {}", this.file, content);

        // set the configmap watch file to monitor
        var cfgWatch = ConfigmapWatch.builder() //
                                     .withFileName(this.file.toFile().getName()) //
                                     .withRoot(this.folder.toAbsolutePath().toString()) //
                                     .build();

        // initiate log level changer
        this.llc = new LogLevelChanger(cfgWatch, TEST_CONTAINER);
    }

    /**
     * Change ROOT logger level to INFO and then update log control file with debug
     * severity and verify that new log level is DEBUG
     * 
     * @throws IOException
     */
    @Test(enabled = true)
    private void infoToDebug() throws IOException
    {
        // get root logger
        var lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        var logger = lc.exists(ROOT);
        assertTrue(logger != null, "Failed to extract ROOT logger");

        System.out.println("vaggelis");

        // set log level to info
        logger.setLevel(Level.valueOf("info"));
        assertTrue(logger.getLevel().equals(Level.INFO));

        // create new dummy log control and set severity to debug
        var test = new LogSeverity("test", Level.DEBUG.levelStr);
        var dummy1 = new LogSeverity("dummy", Level.TRACE.levelStr);
        var dummy2 = new LogSeverity("tralala", Level.ERROR.levelStr);
        var severities = new ArrayList<LogSeverity>();
        severities.add(test);
        severities.add(dummy1);
        severities.add(dummy2);
        var dummyLogControl = json.writeValueAsString(severities);
        log.debug("Dummy log control data: {}", dummyLogControl);

        // keep flowable active
        var fileWatcher = llc.logControl().replay().refCount();

        // create test objserver
        var testObserver = fileWatcher.doOnNext(event -> log.debug("New Event: {}", event))//
                                      .doOnError(e -> log.error("Error while waiting for new events", e))//
                                      .test();

        // block till we get first emitted item
        fileWatcher.blockingFirst();

        // write new content to file
        Files.writeString(this.file, dummyLogControl, StandardOpenOption.WRITE);
        log.debug("File {} modified", this.file);

        // get content - expected to be the new dummy log control
        var content = new String(Files.readAllBytes(this.file), StandardCharsets.UTF_8);
        log.debug("File {} content: {}", this.file, content);

        // let test observer wait for the completion of all actions
        testObserver.awaitCount(2) // until it gets 2 items (1-initial, 2-change info2debug)
                    .assertValueCount(2) // count 2 on next events
                    .assertNoErrors() // expect no onError events
                    .assertValueAt(1, Level.DEBUG)
                    .assertValueAt(1, event -> logger.getLevel().equals(Level.DEBUG))
                    .dispose();

    }

    /**
     * Change ROOT logger level to INFO and then update log control file with
     * invalid severity and verify that new log level is set to DEBUG
     * 
     * @throws IOException
     */
    @Test(enabled = true)
    private void debugToInvalid() throws IOException
    {
        // get root logger
        var lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        var logger = lc.exists(ROOT);
        assertTrue(logger != null, "Failed to extract ROOT logger");

        // set log level to info
        logger.setLevel(Level.valueOf("debug"));
        assertTrue(logger.getLevel().equals(Level.DEBUG));

        // create new dummy log control and set severity to debug
        var test = new LogSeverity("test", "challengers");
        var dummy1 = new LogSeverity("dummy", Level.WARN.levelStr);
        var dummy2 = new LogSeverity("tralala", Level.ERROR.levelStr);
        var severities = new ArrayList<LogSeverity>();
        severities.add(test);
        severities.add(dummy1);
        severities.add(dummy2);
        var dummyLogControl = json.writeValueAsString(severities);
        log.debug("Dummy log control data: {}", dummyLogControl);

        // keep flowable active
        var fileWatcher = this.llc.logControl().replay().refCount();

        // create test objserver
        var testObserver = fileWatcher.doOnNext(event -> log.debug("New Event: {}", event))//
                                      .doOnError(e -> log.error("Error while waiting for new events", e))//
                                      .test();

        // block till we get first emitted item
        fileWatcher.blockingFirst();

        // write new content to file
        Files.writeString(this.file, dummyLogControl, StandardOpenOption.SYNC);
        log.info("File {} modified", this.file);

        // get content - expected to be the new dummy log control
        var content = new String(Files.readAllBytes(this.file), StandardCharsets.UTF_8);
        log.info("File {} content: {}", this.file, content);

        // let test observer wait for the completion of all actions
        testObserver.awaitCount(2) // until it gets 2 items (1-initial, 2-change info2debug)
                    .assertValueCount(2) // count 2 on next events
                    .assertNoErrors() // expect no onError events
                    .assertValueAt(1, Level.INFO)
                    .assertValueAt(1, event -> logger.getLevel().equals(Level.INFO))
                    .dispose();
    }

    /**
     * Change ROOT logger level to INFO and then update log control file with
     * invalid severity and verify that new log level is set to DEBUG
     * 
     * @throws IOException
     */
    @Test(enabled = true)
    private void debugToUnsupported() throws IOException
    {
        // get root logger
        var lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        var logger = lc.exists(ROOT);
        assertTrue(logger != null, "Failed to extract ROOT logger");

        // set log level to info
        logger.setLevel(Level.valueOf("debug"));
        assertTrue(logger.getLevel().equals(Level.DEBUG));

        // create new dummy log control and set severity to debug
        var test = new LogSeverity("test", Level.TRACE.levelStr);
        var dummy1 = new LogSeverity("dummy", Level.WARN.levelStr);
        var dummy2 = new LogSeverity("tralala", Level.ERROR.levelStr);
        var severities = new ArrayList<LogSeverity>();
        severities.add(test);
        severities.add(dummy1);
        severities.add(dummy2);
        var dummyLogControl = json.writeValueAsString(severities);
        log.debug("Dummy log control data: {}", dummyLogControl);

        // keep flowable active
        var fileWatcher = this.llc.logControl().replay().refCount();

        // create test objserver
        var testObserver = fileWatcher.doOnNext(event -> log.debug("New Event: {}", event))//
                                      .doOnError(e -> log.error("Error while waiting for new events", e))//
                                      .test();

        // block till we get first emitted item
        fileWatcher.blockingFirst();

        // write new content to file
        Files.writeString(this.file, dummyLogControl, StandardOpenOption.SYNC);
        log.info("File {} modified", this.file);

        // get content - expected to be the new dummy log control
        var content = new String(Files.readAllBytes(this.file), StandardCharsets.UTF_8);
        log.info("File {} content: {}", this.file, content);

        // let test observer wait for the completion of all actions
        testObserver.awaitCount(2) // until it gets 2 items (1-initial, 2-change info2debug)
                    .assertValueCount(2) // count 2 on next events
                    .assertNoErrors() // expect no onError events
                    .assertValueAt(1, Level.INFO)
                    .assertValueAt(1, event -> logger.getLevel().equals(Level.INFO))
                    .dispose();
    }
}
