package com.ericsson.sc.keyexporter;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SftpExporter
{
    private static final Logger log = LoggerFactory.getLogger(SftpExporter.class);
    private static final long DISCONNECT_TIMEOUT = 2000; // 2 seconds
    private static final int SFTP_CONNECT_TIMEOUT_MILLIS = 30 * 1000; // 30 seconds
    private static final int RETRY_PERIOD_MILLIS = 1 * 1000; // 1 second
    private static final int POLL_PERIOD_MILLIS = 1 * 1000; // 1 second
    private final BlockingQueue<String> queue;
    private final SftpConfig sftpConfig;
    private final String prefixFileLabel;
    private final Thread thread;
    private final String remotePath;
    private final long fileSizeLimit;

    private final AtomicReference<Session> jschSession = new AtomicReference<>();
    private final AtomicReference<ChannelSftp> channelSftp = new AtomicReference<>();
    private final ConcurrentLinkedDeque<String> remoteFiles = new ConcurrentLinkedDeque<>();

    private final AtomicLong sizeCounter = new AtomicLong(0);
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);

    public enum State
    {
        RUNNING,
        CONNECTED,
        CLOSED;
    }

    public SftpExporter(BlockingQueue<String> queue,
                        SftpConfig sftpConfig,
                        String prefixFileLabel,
                        long fileSizeLimit)
    {
        Objects.requireNonNull(queue);
        Objects.requireNonNull(sftpConfig);
        Objects.requireNonNull(prefixFileLabel);
        Objects.requireNonNull(fileSizeLimit);

        if (fileSizeLimit <= 0)
            throw new IllegalArgumentException("Invalid file size limit: " + fileSizeLimit);

        this.queue = queue;
        this.sftpConfig = sftpConfig;
        this.thread = new Thread(this::run);
        this.thread.setDaemon(true);
        this.prefixFileLabel = prefixFileLabel;
        this.fileSizeLimit = fileSizeLimit;
        this.remotePath = sftpConfig.getRemotePath();
    }

    /**
     * Stop exporting to Sftp server, possibly blocking until associated thread has
     * finished. Once stopped, the exporter cannot be restarted
     */
    public void stopBlocking()
    {
        this.state.set(State.CLOSED);

        try
        {
            // Gracefully drain
            this.thread.join(DISCONNECT_TIMEOUT);

            // Force stop if thread not dead already
            disconnect();
            this.thread.join(DISCONNECT_TIMEOUT);
        }
        catch (InterruptedException e)
        {
            disconnect();
            Thread.currentThread().interrupt();
        }
    }

    public void start()
    {
        this.thread.start();
    }

    /**
     * 
     * @return remoteFiles
     */
    public List<String> getRemoteFiles()
    {
        return new ArrayList<>(this.remoteFiles);
    }

    private void run()
    {

        this.remoteFiles.add(remotePath + createRemoteFileName());
        log.info("TLS key log will be exported to sftp://{}:{}/{}", sftpConfig.getHost(), sftpConfig.getPort(), remoteFiles.getLast());
        state.set(State.RUNNING);
        String line = null;
        byte[] bytesToAppend;

        while (state.get() != State.CLOSED)
        {
            try
            {
                connect();
                final var sftp = channelSftp.get();
                while (state.get() == State.CONNECTED)
                {
                    try
                    {
                        line = (line == null ? (this.queue.poll(POLL_PERIOD_MILLIS, TimeUnit.MILLISECONDS)) : line);
                        bytesToAppend = (line + '\n').getBytes();
                        updateRemoteFile(bytesToAppend.length);
                        if (line == null)
                        {
                            continue;
                        }
                        if (writeSftp(bytesToAppend, sftp, remoteFiles.getLast()))
                        {
                            line = null;
                        }
                    }
                    catch (InterruptedException e1)
                    {
                        state.set(State.CLOSED);
                        Thread.currentThread().interrupt();
                    }
                } // While in Connected state
            }
            finally
            {
                disconnect();
            }
        }
        log.info("SFTP write thread terminated");
    }

    private boolean writeSftp(byte[] bytesToAppend,
                              ChannelSftp sftp,
                              String remoteFilePath) throws InterruptedException
    {
        try
        {
            final var is = new ByteArrayInputStream(bytesToAppend);
            sftp.put(is, remoteFilePath, ChannelSftp.APPEND);
            sizeCounter.addAndGet(bytesToAppend.length);
            return true;
        }
        catch (Exception e)
        {
            if (!state.get().equals(State.CLOSED))
            {
                log.warn("sftp write error", e);
                setStateRunning();
                Thread.sleep(RETRY_PERIOD_MILLIS);
            }
            return false;
        }
    }

    /**
     * If potential size of writing file exceeds the limit, resets sizeCounter,
     * rotates writing to new generated file.
     * 
     */
    private void updateRemoteFile(int sizeToAdd)
    {
        final var potentialFileSize = sizeCounter.get() + sizeToAdd;
        if (potentialFileSize > fileSizeLimit)
        {
            resetSizeCounter();
            this.remoteFiles.add(remotePath + createRemoteFileName());
            log.info("Exportation is rotating to file: {}", remoteFiles.getLast());
        }
    }

    /**
     * Connect to the Sftp server. If failed, try again.
     */
    private void connect()
    {
        try
        {
            final var sess = new JSch().getSession(sftpConfig.getUser(), sftpConfig.getHost(), sftpConfig.getPort());
            sess.setConfig("StrictHostKeyChecking", "no");
            sess.setPassword(sftpConfig.getPassword());
            this.jschSession.set(sess);

            sess.connect(SFTP_CONNECT_TIMEOUT_MILLIS);
            final var channel = (ChannelSftp) sess.openChannel("sftp");
            channelSftp.set(channel);
            channel.connect(SFTP_CONNECT_TIMEOUT_MILLIS);
            state.set(State.CONNECTED);

            log.info("Connected to sftp://{}:{}", sess.getHost(), sess.getPort());
        }
        catch (Exception e)
        {
            if (!state.get().equals(State.CLOSED))
            {
                log.warn("Failed to connect to sftp://{}:{}", sftpConfig.getHost(), sftpConfig.getPort(), e);
                try
                {
                    Thread.sleep(RETRY_PERIOD_MILLIS);
                    setStateRunning(); // Retry connection
                }
                catch (InterruptedException ie)
                {
                    state.set(State.CLOSED); // Terminate retry loop
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Disconnect from the Sftp server, if connected
     */
    private void disconnect()
    {
        try
        {
            Optional.ofNullable(channelSftp.get()).ifPresent(ChannelSftp::disconnect);
        }
        finally
        {
            Optional.ofNullable(jschSession.get()).ifPresent(Session::disconnect);
        }
    }

    /**
     * Create the text remote file name, where the data will be exported.
     * 
     * @return remoteFilename
     */
    private String createRemoteFileName()
    {
        final var currentTime = new SimpleDateFormat("-yyyy-MM-dd'T'HH-mm-ss").format(new java.util.Date());
        return "/" + prefixFileLabel + currentTime + ".txt";
    }

    private boolean setStateRunning()
    {
        return this.state.compareAndSet(State.CONNECTED, State.RUNNING);
    }

    private void resetSizeCounter()
    {
        this.sizeCounter.set(0);
    }

}
