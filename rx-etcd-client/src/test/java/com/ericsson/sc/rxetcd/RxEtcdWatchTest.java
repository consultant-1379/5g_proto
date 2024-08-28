/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Dec 2, 2019
 *     Author: emldpng
 */

package com.ericsson.sc.rxetcd;

import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.sc.rxetcd.util.EtcdTestBed;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent.EventType;
import io.etcd.jetcd.watch.WatchResponse;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Predicate;

/**
 * Test class for RxEtcd watch client.
 */
public class RxEtcdWatchTest
{
    private static final Logger log = LoggerFactory.getLogger(RxEtcdWatchTest.class);

    private static final String KEYSPACE = "/eric-sc-spr/test/";
    private EtcdTestBed etcdTestBed;

    private RxEtcd rxEtcd;

    private static final int RETRIES_MIN = 3;
    private static final int REQUEST_TIMEOUT = 5;

    private static final String LOWER_CHARS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER_CHARS = LOWER_CHARS.toUpperCase();
    private static final String NUM_CHARS = "0123456789";
    private static final String DATA_CHARS = UPPER_CHARS + LOWER_CHARS + NUM_CHARS;
    private static SecureRandom random = new SecureRandom();

    public Map<ByteSequence, ByteSequence> generateRandomKeyVal(int mapSize,
                                                                int keySize,
                                                                String keyPrefix)
    {
        if ((mapSize < 1) || (keySize < 1))
            throw new IllegalArgumentException("Wrong keyVal size requested.");
        Map<ByteSequence, ByteSequence> kv = new HashMap<ByteSequence, ByteSequence>();
        while (kv.size() != mapSize)
        {
            StringBuilder sb = new StringBuilder(keySize);
            while (sb.length() != keySize)
            {
                sb.append(DATA_CHARS.charAt(random.nextInt(DATA_CHARS.length())));
            }
            ByteSequence key = EtcdTestBed.btSqn(keyPrefix + sb.toString());
            log.info("key ---> {}", key.toString());
            if (!kv.containsKey(key))
                kv.put(key, EtcdTestBed.btSqn("enabled"));
        }
        return kv;
    }

    @Test(groups = "functest")
    public void watchPutOperation_newKey()
    {
        final var rxEtcd = etcdTestBed.createEtcdClient(RETRIES_MIN, REQUEST_TIMEOUT, TimeUnit.SECONDS, etcdTestBed.getEndpoint());

        final var cleanUp = new CompositeDisposable();

        try
        {
            var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
            var value = EtcdTestBed.btSqn("enabled");

            // Create a watcher.
            var testWatcher = rxEtcd.watch(key).test();
            cleanUp.add(testWatcher);

            // Allow the watcher to start.
            Observable.timer(500, TimeUnit.MILLISECONDS).blockingFirst();

            // Put the key.
            rxEtcd.put(key, value).blockingGet();

            testWatcher.awaitCount(1) //
                       .assertValueCount(1)
                       .assertValueAt(0, response ->
                       {
                           var event = response.getEvents().get(0);
                           return event.getKeyValue().getKey().equals(key) //
                                  && event.getKeyValue().getValue().equals(value) //
                                  && event.getEventType().equals(EventType.PUT);
                       });
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    @Test(groups = "functest")
    public void watchPutOperation_existingKey()
    {
        final var cleanUp = new CompositeDisposable();
        try
        {
            var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
            var valueOld = EtcdTestBed.btSqn("enabled");
            var valueNew = EtcdTestBed.btSqn("disabled");

            // Create a watcher.
            var testWatcher = rxEtcd.watch(key).test();
            cleanUp.add(testWatcher);

            // Allow the watcher to start.
            Observable.timer(500, TimeUnit.MILLISECONDS).blockingFirst();

            // Put the key.
            rxEtcd.put(key, valueOld).blockingGet();

            // Update the key.
            rxEtcd.put(key, valueNew).blockingGet();

            testWatcher.awaitCount(2) //
                       .assertValueCount(2)
                       .assertValueAt(0, response ->
                       {
                           var event = response.getEvents().get(0);
                           return event.getKeyValue().getKey().equals(key) //
                                  && event.getKeyValue().getValue().equals(valueOld) //
                                  && event.getKeyValue().getVersion() == 1 //
                                  && event.getEventType().equals(EventType.PUT);
                       })
                       .assertValueAt(1, response ->
                       {
                           var event = response.getEvents().get(0);
                           return event.getKeyValue().getKey().equals(key) //
                                  && event.getKeyValue().getValue().equals(valueNew) //
                                  && event.getKeyValue().getVersion() == 2 //
                                  && event.getEventType().equals(EventType.PUT);
                       });
        }
        finally
        {
            cleanUp.dispose();
        }

    }

    @Test(groups = "functest")
    public void watchPutOperation_otherKey()
    {
        final var cleanUp = new CompositeDisposable();
        try
        {
            var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
            var value = EtcdTestBed.btSqn("enabled");
            var watchKey = EtcdTestBed.btSqn(KEYSPACE + "occ/beta");

            // Create a watcher.
            var testWatcher = rxEtcd.watch(watchKey).test();
            cleanUp.add(testWatcher);

            // Allow the watcher to start.
            Observable.timer(500, TimeUnit.MILLISECONDS).blockingFirst();

            // Put the key.
            rxEtcd.put(key, value).blockingGet();

            // No notification should be received for the key.
            testWatcher.awaitDone(800, TimeUnit.MILLISECONDS).assertNoValues();
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    @Test(groups = "functest")
    public void watchMultiplePutOperations_prefixKey()
    {
        final var cleanUp = new CompositeDisposable();
        try
        {
            var directoryKey = EtcdTestBed.btSqn(KEYSPACE + "occ/");
            var keyVal = Map.of(EtcdTestBed.btSqn(KEYSPACE + "occ/alpha"),
                                EtcdTestBed.btSqn("enabled"),
                                EtcdTestBed.btSqn(KEYSPACE + "occ/beta"),
                                EtcdTestBed.btSqn("disabled"),
                                EtcdTestBed.btSqn(KEYSPACE + "occ/gamma"),
                                EtcdTestBed.btSqn("suspended"));

            // Create a watcher.
            var testWatcher = rxEtcd.watch(directoryKey, WatchOption.newBuilder().withPrefix(directoryKey).build()).test();
            cleanUp.add(testWatcher);

            // Allow the watcher to start.
            Observable.timer(500, TimeUnit.MILLISECONDS).blockingFirst();

            // Put a key that shouldn't be detected by the watcher.
            rxEtcd.put(EtcdTestBed.btSqn("dummy/delta"), EtcdTestBed.btSqn("invalid")).blockingGet();

            // Put the keys.
            keyVal.forEach((k,
                            v) -> rxEtcd.put(k, v).blockingGet());

            // Define the assertions for each watch notification.
            Predicate<WatchResponse> assertions = response ->
            {
                var event = response.getEvents().get(0);
                var type = event.getEventType();
                var key = event.getKeyValue().getKey();
                var val = event.getKeyValue().getValue();

                // Check type of event and key-values.
                return type.equals(EventType.PUT) && keyVal.get(key).equals(val);
            };

            testWatcher.awaitCount(3) //
                       .assertValueCount(3)
                       .assertValueAt(0, assertions)
                       .assertValueAt(1, assertions)
                       .assertValueAt(2, assertions);
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    @Test(groups = "functest")
    public void watchDeleteOperation_specificKey()
    {
        final var cleanUp = new CompositeDisposable();

        try
        {
            var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
            var value = EtcdTestBed.btSqn("enabled");

            // Create a watcher.
            var testWatcher = rxEtcd.watch(key).test();
            cleanUp.add(testWatcher);

            // Allow the watcher to start.
            Observable.timer(500, TimeUnit.MILLISECONDS).blockingFirst();

            // Put the key.
            rxEtcd.put(key, value).blockingGet();

            // Delete the key.
            rxEtcd.delete(key).blockingGet();

            testWatcher.awaitCount(2) //
                       .assertValueCount(2)
                       .assertValueAt(0, response ->
                       {
                           var event = response.getEvents().get(0);
                           return event.getKeyValue().getKey().equals(key) //
                                  && event.getKeyValue().getValue().equals(value) //
                                  && event.getEventType().equals(EventType.PUT);
                       })
                       .assertValueAt(1, response ->
                       {
                           var event = response.getEvents().get(0);
                           return event.getKeyValue().getKey().equals(key) && event.getEventType().equals(EventType.DELETE);
                       });
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    @Test(groups = "functest")
    public void watchMultipleDeleteOperations_prefixKey()
    {
        final var cleanUp = new CompositeDisposable();

        try
        {
            var directoryKey = EtcdTestBed.btSqn(KEYSPACE + "occ/");
            var keyVal = Map.of(EtcdTestBed.btSqn(KEYSPACE + "occ/alpha"),
                                EtcdTestBed.btSqn("enabled"),
                                EtcdTestBed.btSqn(KEYSPACE + "occ/beta"),
                                EtcdTestBed.btSqn("disabled"),
                                EtcdTestBed.btSqn(KEYSPACE + "occ/gamma"),
                                EtcdTestBed.btSqn("suspended"));

            // Put the keys.
            keyVal.forEach((k,
                            v) -> rxEtcd.put(k, v).blockingGet());

            // Create a watcher.
            var testWatcher = rxEtcd.watch(directoryKey, WatchOption.newBuilder().withPrefix(directoryKey).build()).test();
            cleanUp.add(testWatcher);

            // Allow the watcher to start.
            Observable.timer(500, TimeUnit.MILLISECONDS).blockingFirst();

            // Delete the keys.
            keyVal.forEach((k,
                            v) -> rxEtcd.delete(k).blockingGet());

            // Define the assertions for each watch notification.
            Predicate<WatchResponse> assertions = response ->
            {
                var event = response.getEvents().get(0);
                var type = event.getEventType();
                var key = event.getKeyValue().getKey();
                // Check type of event and key-values.
                return type.equals(EventType.DELETE) && keyVal.containsKey(key);
            };

            testWatcher.awaitCount(3) //
                       .assertValueCount(3)
                       .assertValueAt(0, assertions)
                       .assertValueAt(1, assertions)
                       .assertValueAt(2, assertions);
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    @Test(groups = "functest")
    public void watchMultipleOperationsWithRevision_prefixKey()
    {
        final var cleanUp = new CompositeDisposable();

        try
        {
            var directoryKey = EtcdTestBed.btSqn(KEYSPACE + "occ/");
            var keyVal = Map.of(EtcdTestBed.btSqn(KEYSPACE + "occ/alpha"),
                                EtcdTestBed.btSqn("enabled"),
                                EtcdTestBed.btSqn(KEYSPACE + "occ/beta"),
                                EtcdTestBed.btSqn("disabled"),
                                EtcdTestBed.btSqn(KEYSPACE + "occ/gamma"),
                                EtcdTestBed.btSqn("suspended"));

            // Put the keys.
            keyVal.forEach((k,
                            v) -> rxEtcd.put(k, v).blockingGet());

            // Get current revision of ETCD cluster.
            var revision = rxEtcd.get(EtcdTestBed.btSqn(KEYSPACE + "occ/alpha")).blockingGet().getHeader().getRevision();

            // Create a watcher.
            var testWatcher = rxEtcd.watch(directoryKey,
                                           WatchOption.newBuilder() //
                                                      .withRevision(revision + 1)
                                                      .withPrefix(directoryKey)
                                                      .build()) //
                                    .take(3)
                                    .test();
            cleanUp.add(testWatcher);

            // Allow the watcher to start.
            Observable.timer(500, TimeUnit.MILLISECONDS).blockingFirst();

            // Update a key.
            var updateKey = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
            var updateValue = EtcdTestBed.btSqn("disabled");
            rxEtcd.put(updateKey, updateValue).blockingGet();

            // Delete two keys.
            var delKey1 = EtcdTestBed.btSqn(KEYSPACE + "occ/beta");
            var delKey2 = EtcdTestBed.btSqn(KEYSPACE + "occ/gamma");
            rxEtcd.delete(delKey1).blockingGet();
            rxEtcd.delete(delKey2).blockingGet();

            // Define the assertions for each watch notification.
            Predicate<WatchResponse> assertions = response ->
            {
                var resRevision = response.getHeader().getRevision();
                var resModRevision = response.getEvents().get(0).getKeyValue().getModRevision();
                return resRevision > revision && resModRevision > revision;
            };

            testWatcher.awaitCount(3) //
                       .assertValueCount(3)
                       .assertValueAt(0, assertions)
                       .assertValueAt(0, response -> response.getEvents().get(0).getEventType().equals(EventType.PUT))
                       .assertValueAt(1, assertions)
                       .assertValueAt(1, response -> response.getEvents().get(0).getEventType().equals(EventType.DELETE))
                       .assertValueAt(2, assertions)
                       .assertValueAt(2, response -> response.getEvents().get(0).getEventType().equals(EventType.DELETE));
        }
        finally
        {
            cleanUp.dispose();
        }
    }

    @BeforeClass
    private void setUpTestEnvironment()
    {
        log.info("Before class.");
        etcdTestBed = new EtcdTestBed(KEYSPACE, false);
        etcdTestBed.start();
        this.rxEtcd = etcdTestBed.createEtcdClient(RETRIES_MIN, REQUEST_TIMEOUT, TimeUnit.SECONDS, etcdTestBed.getEndpoint());
        this.rxEtcd.ready().blockingGet();
    }

    @AfterClass
    private void cleanUpTestEnvironment()
    {
        log.info("After class.");

        log.info("Closing non-tls and tls containers");
        etcdTestBed.stopEtcdServers();

    }

    @AfterMethod
    private void cleanup(Method method)
    {
        log.info("Cleaning keyspace");

        if (!etcdTestBed.isKeyspaceClean())
            etcdTestBed.clearKeyspace();

    }

}
