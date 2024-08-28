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
 * Created on: Jan 27, 2020
 *     Author: emldpng
 */

package com.ericsson.sc.rxetcd;

import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.sc.rxetcd.util.EtcdTestBed;

import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Test class for the error handling of the rxEtcd client. It includes TestCases
 * for connection retries and expected behavior, when ETCD container is down.
 */
public class RxEtcdErrorHandlingTest
{
    private static final Logger log = LoggerFactory.getLogger(RxEtcdErrorHandlingTest.class);

    private static final String KEYSPACE = "/eric-sc-spr/test/";
    private EtcdTestBed etcdTestBed;
    private RxEtcd rxEtcd;

    @Test(groups = "functest")
    public void rxEtcdInitialConnectionRetries_SuccessfulConnection() throws InterruptedException, SSLException
    {
        try
        {
            var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");

            etcdTestBed = new EtcdTestBed(KEYSPACE, false);

            // Start the ETCD server after a small delay to allow for connection retries.
            Observable.timer(3, TimeUnit.SECONDS).flatMapCompletable(i -> Completable.fromAction(() ->
            {
                etcdTestBed.start();
            })).subscribe(() ->
            {
                log.info("Started etcd server");
            }, err -> log.error("Error starting container", err));

            // Create a rxEtcd client with enough connectionRetries, so that it waits for
            // the ETCD container to start.

            final var endpoint = etcdTestBed.getEndpoint();

            rxEtcd = etcdTestBed.createEtcdClient(15, 3, TimeUnit.SECONDS, endpoint);

            rxEtcd.ready().blockingAwait();

            // Get the header from a get response to confirm the connection.
            var getResponse = rxEtcd.get(key).doOnSuccess(next -> log.info("Got response")).blockingGet();
            assertTrue(getResponse.getHeader().getRevision() == 1, "Incorrect revision number.");
        }
        finally
        {
            log.info("Cleaning up");
            etcdTestBed.closeClient();
            etcdTestBed.stopEtcdServers();
            etcdTestBed = null;
        }
    }

    @Test(groups = "functest", expectedExceptions = RuntimeException.class)
    public void rxEtcdInitialConnectionRetries_UnsuccessfulConnection()
    {
        try
        {
            etcdTestBed = new EtcdTestBed(KEYSPACE, false);

            // Start the ETCD server after a small delay to allow for connection retries.
            Observable.timer(10, TimeUnit.SECONDS).flatMapCompletable(i -> Completable.fromAction(() ->
            {
                etcdTestBed.start();
            })).subscribe();

            // Create a rxEtcd client with enough connectionRetries, so that it waits for
            // the ETCD container to start.

            var endpoint = etcdTestBed.getEndpoint();
            rxEtcd = etcdTestBed.createEtcdClient(4, 3, TimeUnit.SECONDS, endpoint);
        }
        finally
        {
            etcdTestBed.closeClient();
            etcdTestBed.stopEtcdServers();
            etcdTestBed = null;
        }
    }

    @Test(groups = "functest")
    public void rxEtcdClientExceptionAfterETCDstop()
    {

        // Start etcdServer and etcdClient.
        etcdTestBed = new EtcdTestBed(KEYSPACE, false);
        etcdTestBed.start();
        // rxEtcd = etcdTestBed.createEtcdClient(3);
        var endpoint = etcdTestBed.getEndpoint();

        rxEtcd = etcdTestBed.createEtcdClient(3, 3, TimeUnit.SECONDS, endpoint);

        // Put a value.
        var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        var value = EtcdTestBed.btSqn("enabled");
        rxEtcd.put(key, value).blockingGet();

        // Get the value from the key.
        var getResponse = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse.getCount() == 1, "There is no value for this key.");
        assertTrue(getResponse.getKvs().get(0).getValue().equals(value), "The key has wrong value.");

        // Stop etcdServer.
        etcdTestBed.stopEtcdServers();

        // Try to get the same value.
        try
        {
            rxEtcd.get(key).blockingGet();
        }
        catch (Exception e)
        {
            // Got expected exception
            log.info("Got expected exception ", e);
        }

        etcdTestBed.closeClient();
        etcdTestBed.stopEtcdServers();
        etcdTestBed = null;
    }

    @Test(groups = "functest")
    public void rxEtcdClientSuccessfulGetAfterETCDrestart() throws InterruptedException, SSLException
    {
        try
        {
            // Start etcdServer and etcdClient.
            etcdTestBed = new EtcdTestBed(KEYSPACE, true);
            etcdTestBed.start();
            // rxEtcd = etcdTestBed.createEtcdClient(3);

            final var endpoint = "http://" + etcdTestBed.getEtcdContainer().getEtcdIp() + ":" + etcdTestBed.getEtcdContainer().getEtcdPort();

            rxEtcd = etcdTestBed.createEtcdClient(3, 3, TimeUnit.SECONDS, endpoint);
            // Put a value.
            var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
            var value = EtcdTestBed.btSqn("enabled");
            rxEtcd.put(key, value).blockingGet();

            // Get the value from the key.
            var getResponse = rxEtcd.get(key).blockingGet();
            assertTrue(getResponse.getCount() == 1, "There is no value for this key.");
            assertTrue(getResponse.getKvs().get(0).getValue().equals(value), "The key has wrong value.");

            // Restart etcdServer.
            etcdTestBed.restartEtcdServer(10);

            // Try to get the same value.
            var getResponse2 = rxEtcd.get(key).blockingGet();
            assertTrue(getResponse2.getCount() == 1, "There is no value for this key.");
            assertTrue(getResponse2.getKvs().get(0).getValue().equals(value), "The key has wrong value.");
        }
        finally
        {
            etcdTestBed.closeClient();
            etcdTestBed.stopEtcdServers();
            etcdTestBed = null;
        }
    }

    @BeforeClass
    private void setUpTestEnvironment()
    {
        log.info("Before class.");

        etcdTestBed = new EtcdTestBed(KEYSPACE, false);
        ;
        rxEtcd = null;
    }

    @AfterClass
    private void cleanUpTestEnvironment()
    {
        log.info("After class.");
    }

    @BeforeMethod
    private void prepare(Method method)
    {
        log.info("Before method.");
        log.info("Executing: {}", method.getName());
        if (etcdTestBed != null)
            etcdTestBed = null;
    }

    @AfterMethod
    private void cleanup()
    {
        log.info("After method.");
        etcdTestBed = null;
    }
}
