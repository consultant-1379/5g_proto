package com.ericsson.sc.keyexporter;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class SftpTestContainer
{
    private final Integer PORT = 22;

    private final String pass;
    private final String user;
    private final String remotePath;
    private final GenericContainer<?> sftpServer;

    @SuppressWarnings("resource")
    public SftpTestContainer(String user,
                             String pass,
                             String remotePath)
    {
        this.user = user;
        this.pass = pass;
        this.remotePath = remotePath;
        this.sftpServer = new GenericContainer<>(new ImageFromDockerfile() //
                                                                          .withDockerfileFromBuilder(builder -> builder.from("armdockerhub.rnd.ericsson.se/atmoz/sftp:latest")
                                                                                                                       .run("mkdir -p /home/" + user + "/"
                                                                                                                            + remotePath
                                                                                                                            + "; chmod -R 007 /home/" + user)
                                                                                                                       .build())).withExposedPorts(PORT)
                                                                                                                                 .withCommand(user + ":" + pass
                                                                                                                                              + ":::"
                                                                                                                                              + remotePath);
    }

    /**
     * The User of the Sftp Server
     * 
     * @return
     */
    public String getUser()
    {
        return this.user;
    }

    /**
     * The Password of the Sftp Server
     * 
     * @return
     */
    public String getPass()
    {
        return this.pass;
    }

    /**
     * The remote path of the Sftp Server
     * 
     * @return
     */
    public String getRemotePath()
    {
        return this.remotePath;
    }

    /**
     * Start the sftpServer running
     * 
     */
    public void start()
    {
        this.sftpServer.start();
    }

    /**
     * Stop the sftpServer running
     * 
     */
    public void stop()
    {
        this.sftpServer.stop();
    }

    /**
     * Get the local port where the sftpServer is running
     * 
     * @return
     */
    public int getMappedPort()
    {
        return this.sftpServer.getMappedPort(PORT);
    }

    /**
     * Get the local host where the sftpServer is running
     * 
     * @return
     */
    public String getHost()
    {
        return this.sftpServer.getHost();
    }

}
