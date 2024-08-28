package com.ericsson.sc.keyexporter;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.nimbusds.jose.util.IOUtils;

public class SftpExporterTest
{

    private static final Logger log = LoggerFactory.getLogger(SftpExporterTest.class);
    private static final int SFTP_CONNECT_TIMEOUT_MILLIS = 30 * 1000; // 30 seconds
    private final String sftpPass = "pass";
    private final String sftpUser = "foo";
    private final String remotePath = "upload";
    private final String prefixLabel = "sftp-unitTest";
    private final long fileSizeLimit = 1000; // 1000 bytes

    private String sftpHost;
    private int sftpPort;

    private SftpTestContainer sftpServer;

    @BeforeClass
    public void beforeClass() throws InterruptedException, JSchException
    {
        sftpServer = new SftpTestContainer(sftpUser, sftpPass, remotePath);
        sftpServer.start();

        sftpHost = sftpServer.getHost();
        sftpPort = sftpServer.getMappedPort();

        log.info("Sftp server started with host: {} and port: {}", sftpHost, sftpPort);
    }

    @AfterClass
    public void afterClass()
    {
        sftpServer.stop();
    }

    @Test
    public void tc001TestExportedData() throws JSchException
    {
        Session jschSession;
        ChannelSftp channelSftp = null;

        jschSession = new JSch().getSession(sftpUser, sftpHost, sftpPort);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.setPassword(sftpPass);

        var sftpConfig = new SftpConfig(sftpHost, sftpPort, sftpUser, sftpPass, remotePath);
        BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
        try
        {
            final String uploadData = "Hello World";

            inputQueue.put(uploadData);
            final SftpExporter exporter = new SftpExporter(inputQueue, sftpConfig, prefixLabel, fileSizeLimit);
            exporter.start();
            exporter.stopBlocking();

            jschSession.connect(SFTP_CONNECT_TIMEOUT_MILLIS);
            channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
            log.info("Connecting to sftp://{}:{}", jschSession.getHost(), jschSession.getPort());
            channelSftp.connect(SFTP_CONNECT_TIMEOUT_MILLIS);

            final var remoteFiles = exporter.getRemoteFiles();
            final var remoteFilePath = remoteFiles.get(0);

            log.info("Downloading data from remote file: {}", remoteFilePath);
            final var downloadStream = channelSftp.get(remoteFilePath);
            final var downloadData = IOUtils.readInputStreamToString(downloadStream, StandardCharsets.UTF_8);

            assertTrue(downloadData.equals(uploadData + "\n"), "Downloaded data are not correct ");

            channelSftp.disconnect();
            jschSession.disconnect();
        }
        catch (InterruptedException e)
        {
            log.error("Interrupted Exception", e);
        }
        catch (SftpException e)
        {
            log.error("SftpException", e);
        }
        catch (IOException e)
        {
            log.error("IO Exception", e);
        }
    }

    @Test
    public void tc002TestFileRotation() throws JSchException
    {
        Session jschSession;
        final ChannelSftp channelSftp;

        jschSession = new JSch().getSession(sftpUser, sftpHost, sftpPort);
        jschSession.setConfig("StrictHostKeyChecking", "no");
        jschSession.setPassword(sftpPass);

        var sftpConfig = new SftpConfig(sftpHost, sftpPort, sftpUser, sftpPass, remotePath);
        BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();

        final SftpExporter exporter = new SftpExporter(inputQueue, sftpConfig, prefixLabel, fileSizeLimit);
        exporter.start();
        final String uploadData = "Rotation checking line";
        final var inputCounter = 100;
        IntStream.range(1, inputCounter).forEach(data ->
        {
            try
            {
                inputQueue.put(uploadData);
                TimeUnit.MILLISECONDS.sleep(30);
            }
            catch (InterruptedException e)
            {
                log.error("Interrupted Exception {}", e);
            }
        });
        exporter.stopBlocking();

        jschSession.connect(SFTP_CONNECT_TIMEOUT_MILLIS);
        channelSftp = (ChannelSftp) jschSession.openChannel("sftp");
        log.info("Connecting to sftp://{}:{}", jschSession.getHost(), jschSession.getPort());
        channelSftp.connect(SFTP_CONNECT_TIMEOUT_MILLIS);

        final var remoteFiles = exporter.getRemoteFiles();
        final var invalidFiles = remoteFiles.stream() //
                                            .map(filePath ->
                                            {
                                                long fileSize = 0;
                                                try
                                                {
                                                    fileSize = channelSftp.lstat(filePath).getSize();
                                                }
                                                catch (SftpException e)
                                                {
                                                    log.error("Sftp Exception", e);
                                                }
                                                return fileSize;
                                            })
                                            .filter(size -> (size >= fileSizeLimit || size == 0))
                                            .collect(Collectors.toList());
        final var expectedNumberOfFiles = (int) Math.ceil((inputCounter * (uploadData.length() + 1)) / (float) fileSizeLimit);

        assertTrue(expectedNumberOfFiles == remoteFiles.size(), "Rotation files generation failed");
        assertTrue(invalidFiles.isEmpty(), "File extended maximum size, rotation failed");
        channelSftp.disconnect();
        jschSession.disconnect();
    }

}
