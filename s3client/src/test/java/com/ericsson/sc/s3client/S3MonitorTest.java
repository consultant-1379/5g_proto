package com.ericsson.sc.s3client;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.reactivex.Flowable;
import io.reactivex.subscribers.TestSubscriber;

public class S3MonitorTest
{
    private static final Logger log = LoggerFactory.getLogger(S3MonitorTest.class);
    private String monitorDirectoryPath;
    private S3Monitor monitor;

    private static final String POD_DIR = "src/test/resources/coredumps";
    private static final String TMP_FILE = "challenge.tmp";
    private static final String NEW_TMP_FILE = "newChallenge.tmp";
    private static final String CONTAINER_1_DIR = "container-1";
    private static final String CONTAINER_2_DIR = "container-2";
    private static final String CONTAINER_3_DIR = "container-3";
    private static final Long DEBOUNCE_3000MS = 3000L;

    @BeforeClass
    public void beforeClass() throws IOException
    {
        log.debug("Setup environment prior execution of any method in this test class.");
        assertFalse(Files.exists(Paths.get(POD_DIR)), "Error " + POD_DIR + " already exists.");
        String directoryPath = "";
        Path podDirPath = Files.createDirectories(Paths.get(POD_DIR));
        assertTrue(Files.exists(podDirPath));
        directoryPath = podDirPath.toFile().getAbsolutePath();
        this.monitorDirectoryPath = directoryPath;
        this.monitor = new S3Monitor(this.monitorDirectoryPath, DEBOUNCE_3000MS);
    }

    @AfterClass
    public void afterClass() throws IOException
    {
        log.debug("Cleanup activities after the execution of all methods in this test class.");
        assertTrue(Files.exists(Paths.get(POD_DIR)), "Error " + POD_DIR + " does not exist");
        Files.walk(Paths.get(this.monitorDirectoryPath)) // scan the tree from monitored path
             .sorted(Comparator.reverseOrder()) // sort in reverse order
             .map(Path::toFile) // get the file objects
             .forEach(File::delete); // delete each file/directory
        assertFalse(Files.exists(Paths.get(POD_DIR)), "Failed to delete " + POD_DIR);
    }

    @BeforeMethod
    @AfterMethod
    public void checkMonitorDir() throws IOException
    {
        File podDir = new File(this.monitorDirectoryPath);
        File[] files = podDir.listFiles();
        if (files.length > 0)
        {
            log.debug("Cleanup files {} in {}", files.toString(), podDir.getAbsoluteFile());
            Files.walk(Paths.get(this.monitorDirectoryPath)) // scan the tree from monitored path
                 .map(Path::toFile) // get the file objects
                 .filter(file -> !file.equals(podDir)) // filter file objects that do not match monitored directory
                 .forEach(File::delete); // delete each file/directory
        }
        else
        {
            log.debug("No files present in {}", podDir.getAbsoluteFile());
        }
    }

    @Test
    public void tc000() throws IOException
    {
        File file = new File("src/test/resources");
        assertTrue(file.exists(), "Error reading test resources directory.");

        File podDir = new File(file.getAbsolutePath(), "pod");
        assertFalse(podDir.exists(), "Unexpected error, pod directory already exists.");

        podDir = new File(POD_DIR);
        assertTrue(podDir.exists(), POD_DIR + " directory does not exist.");
    }

    @Test
    public void tc001() throws IOException, InterruptedException
    {
        Flowable<List<File>> filesEvent = this.monitor.getFileEvents().replay().refCount();
        TestSubscriber<List<File>> testObserver = filesEvent.doOnNext(files -> log.debug("New list of files event: {}", files))
                                                            .doOnError(err -> log.error("Error during the handling of new list of files event.", err))
                                                            .test();

        filesEvent.blockingFirst().forEach(file -> log.debug("New file: {}", file.getAbsoluteFile()));

        Path containerPath = Paths.get(this.monitorDirectoryPath, CONTAINER_1_DIR);
        containerPath.toFile().mkdir();

        Path file1 = Files.createTempFile(containerPath, TMP_FILE, null);
        log.debug("File {} created", file1);

        Path file2 = Files.createTempFile(containerPath, TMP_FILE, null);
        log.debug("File {} created", file2);

        File newFile = this.mergeFiles(containerPath.toFile(), NEW_TMP_FILE);
        TimeUnit.MILLISECONDS.sleep(200);

        file1.toFile().delete();
        file2.toFile().delete();

        // Expect 2 events: INIT_EVENT, ENTRY_CREATE
        testObserver.awaitCount(2).assertValueCount(2).assertNoErrors().assertValueAt(1, files -> files.contains(newFile)).dispose();
    }

    private File mergeFiles(File directory,
                            String newFile) throws FileNotFoundException
    {
        Path newFilePath = Paths.get(directory.getAbsolutePath(), newFile);
        // PrintWriter pw = new PrintWriter(directory.getAbsoluteFile() + "/" +
        // newFile);
        PrintWriter pw = new PrintWriter(newFilePath.toString());
        String[] files = directory.list();
        for (String file : files)
        {
            log.debug("Merging file {} to {}", file, newFilePath.toAbsolutePath());
            File fileObj = new File(directory, file);
            pw.println("Contents of file " + fileObj.getAbsolutePath());
            String line;
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(fileObj));
                line = br.readLine();
                while (line != null)
                {
                    pw.println(line);
                    line = br.readLine();
                }
                br.close();
            }
            catch (IOException e)
            {
                log.error("Error during the merge of file {} content.", file);
            }
            pw.flush();
        }
        pw.close();
        log.debug("All files {} merged into new file {}.", files, newFilePath.toAbsolutePath());
        return newFilePath.toFile();
    }

    @Test
    public void tc002() throws IOException
    {
        Flowable<List<File>> filesEvent = this.monitor.getFileEvents().replay().refCount();
        TestSubscriber<List<File>> testObserver = filesEvent.doOnNext(files -> log.debug("New list of files event: {}", files))
                                                            .doOnError(err -> log.error("Error during the handling of new list of files event.", err))
                                                            .test();

        filesEvent.blockingFirst().forEach(file -> log.debug("New file: {}", file.getAbsoluteFile()));

        Path containerPath = Paths.get(this.monitorDirectoryPath, CONTAINER_1_DIR);
        containerPath.toFile().mkdir();
        Path fileName = Files.createTempFile(containerPath, TMP_FILE, null);
        log.debug("File {} created", fileName);

        // Expect 2 events: INIT_EVENT, ENTRY_CREATE
        testObserver.awaitCount(2).assertValueCount(2).assertNoErrors().assertValueAt(1, files -> files.contains(fileName.toFile())).dispose();
    }

    @Test
    public void tc003() throws IOException
    {
        Flowable<List<File>> filesEvent = this.monitor.getFileEvents().replay().refCount();
        TestSubscriber<List<File>> testObserver = filesEvent.doOnNext(files -> log.debug("New list of files event: {}", files))
                                                            .doOnError(err -> log.error("Error during the handling of new list of files event.", err))
                                                            .test();

        filesEvent.blockingFirst().forEach(file -> log.debug("New file: {}", file.getAbsoluteFile()));

        Path containerPath = Paths.get(this.monitorDirectoryPath, CONTAINER_2_DIR);
        containerPath.toFile().mkdir();
        Path tmpFileName = Files.createTempFile(containerPath, TMP_FILE, null);

        Path newFileName = Files.move(tmpFileName, tmpFileName, StandardCopyOption.REPLACE_EXISTING);
        log.debug("File {} moved to same directory", newFileName);

        String name = newFileName.getFileName().toString();
        String copiedName = name.replaceFirst("(\\.[^\\.]*)?$", "-copy$0");
        Path copiedFile = newFileName.resolveSibling(copiedName);
        Files.copy(newFileName, copiedFile);

        // Expect 2 events: INIT_EVENT, [ENTRY_CREATE together with ENTRY_MODIFY]
        testObserver.awaitCount(2).assertValueCount(2).assertNoErrors().assertValueAt(1, files -> files.contains(copiedFile.toFile())).dispose();
    }

    @Test
    public void tc004() throws IOException, InterruptedException
    {
        Flowable<List<File>> filesEvent = this.monitor.getFileEvents().replay().refCount();
        TestSubscriber<List<File>> testObserver = filesEvent.doOnNext(files -> log.debug("New list of files event: {}", files))
                                                            .doOnError(err -> log.error("Error during the handling of new list of files event.", err))
                                                            .test();

        filesEvent.blockingFirst().forEach(file -> log.debug("New file: {}", file.getAbsoluteFile()));

        Path containerPath = Paths.get(this.monitorDirectoryPath, CONTAINER_3_DIR);
        containerPath.toFile().mkdir();
        Path tmpFileName = Files.createTempFile(containerPath, TMP_FILE, null);

        Path fileName = Files.move(tmpFileName, tmpFileName, StandardCopyOption.REPLACE_EXISTING);
        log.debug("File {} moved to same directory", fileName);

        String name = fileName.getFileName().toString();
        String copiedName = name.replaceFirst("(\\.[^\\.]*)?$", "-copy$0");
        Path copiedFile = fileName.resolveSibling(copiedName);
        Files.copy(fileName, copiedFile);

        String newContent = "\r\nChallengers\r\nRock";
        for (int i = 0; i < 6; i++)
        {
            Files.write(copiedFile, newContent.getBytes(), StandardOpenOption.APPEND);
            TimeUnit.MILLISECONDS.sleep(20);
        }

        // Expect 2 events: INIT_EVENT, [ENTRY_CREATE together with ENTRY_MODIFYx7]
        testObserver.awaitCount(2).assertValueCount(2).assertNoErrors().assertValueAt(1, files -> files.contains(copiedFile.toFile())).dispose();
    }
}
