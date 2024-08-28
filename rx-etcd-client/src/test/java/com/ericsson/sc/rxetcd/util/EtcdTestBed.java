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
 * Created on: Jan 23, 2020
 *     Author: emldpng
 */

package com.ericsson.sc.rxetcd.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.RxEtcd.Builder;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;

/**
 * This class provides all necessary functionality for integration tests which
 * are based on rxEtcd package. It deploys an ETCD container in order to use it
 * as an ETCD server instance and creates an rxEtcd client.
 */
public class EtcdTestBed
{
    private static final Logger log = LoggerFactory.getLogger(EtcdTestBed.class);

    private final String keyspace;
    private EtcdContainer etcdContainer;

    private RxEtcd etcdClient;

    private boolean tlsEnabled;

    /**
     * Initializes the etcdContainer. The deployment of the ETCD container and the
     * creation of the ETCD client must be triggered manually.
     * 
     * @param testKeyspace    The keyspace used by the ETCD client.
     * @param statefulBackend Set to true if a restart of the ETCD container is
     *                        needed, in order to maintain the data.
     * @throws IOException In case of wrong server certificates setup
     */
    public EtcdTestBed(String testKeyspace,
                       boolean statefulBackend)
    {
        this.keyspace = testKeyspace;
        this.etcdContainer = new EtcdContainer(statefulBackend);
    }

    public EtcdTestBed(String testKeyspace,
                       boolean statefulBackend,
                       String serverCert,
                       String serverKey,
                       String rootCa)
    {
        this.keyspace = testKeyspace;
        try
        {
            this.etcdContainer = new EtcdContainer(statefulBackend, serverCert, serverKey, rootCa);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Error in adding server certificates", e);

        }
    }

    public String getHost()
    {
        return this.etcdContainer.getHost();
    }

    public String getEtcdIp()
    {
        return this.etcdContainer.getEtcdIp();
    }

    public int getEtcdPort()
    {
        return this.etcdContainer.getEtcdPort();
    }

    public EtcdContainer getEtcdContainer()
    {
        return this.etcdContainer;
    }

    public void start()
    {
        this.etcdContainer.start();
    }

    public void stopEtcdServers()
    {
        this.etcdContainer.stop();
    }

    public void restartEtcdServer(int restartDelay)
    {
        this.etcdContainer.restart(restartDelay);
    }

    public RxEtcd createEtcdClient(int connectionRetries,
                                   String serverEndpoint)
    {
        return createEtcdClient(connectionRetries, 0, TimeUnit.MILLISECONDS, serverEndpoint);
    }

    public String getEndpoint()
    {
        String scheme = tlsEnabled ? "https://" : "http://";

        String host = getHost();
        String ip = getEtcdIp();
        int port = getEtcdPort();
        log.info("host: {}, ip:{}, port:{}", host, ip, port);

        log.info("endpoint: {} ", scheme + ip + ":" + port);

        return scheme + ip + ":" + port;
    }

    public RxEtcd createEtcdClient(int connectionRetries,
                                   long requestTimeout,
                                   TimeUnit requestTimeoutUnit,
                                   String serverEndpoint)
    {

        log.info("Creating new ETCD client for {}.", serverEndpoint);
        log.info("tls enabled : {}", tlsEnabled);

        var etcdClientBuilder = createEtcdClientBuilder(connectionRetries, requestTimeout, requestTimeoutUnit);

        this.etcdClient = etcdClientBuilder.build();
        log.info("Waiting for client to become ready");
        this.etcdClient.ready().blockingAwait();
        log.info("Client is ready");
        return this.etcdClient;
    }

    public RxEtcd getEtcdClient()
    {
        return this.etcdClient;
    }

    public Builder createEtcdClientBuilder(int connectionRetries,
                                           long requestTimeout,
                                           TimeUnit requestTimeoutUnit)
    {
        return RxEtcd.newBuilder()
                     .withUser(EtcdContainer.getEtcdClientUsername())
                     .withPassword(EtcdContainer.getEtcdClientPassword())
                     .withEndpoint(this.getEndpoint())
                     .withConnectionRetries(connectionRetries)
                     .withRequestTimeout(requestTimeout, requestTimeoutUnit);
    }

    public void clearKeyspace()
    {
        var delOption = DeleteOption.newBuilder().withPrefix(btSqn(keyspace)).build();
        this.etcdClient.delete(btSqn(keyspace), delOption).blockingGet();
    }

    public String getKeyspace()
    {
        return keyspace;
    }

    public boolean isKeyspaceClean()
    {

        var getOption = GetOption.newBuilder().withPrefix(btSqn(keyspace)).build();

        var getResponse = etcdClient.get(btSqn(keyspace), getOption).blockingGet();
        log.debug("response: {}", getResponse);
        return getResponse.getCount() < 1;
    }

    public void closeClient() throws NullPointerException
    {
        if (this.etcdClient != null)
            this.etcdClient.close().blockingGet();
    }

    public static ByteSequence btSqn(String value)
    {
        return ByteSequence.from(value.getBytes());
    }
}
