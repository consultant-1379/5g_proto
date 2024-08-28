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
 * Created on: Nov 27, 2019
 *     Author: emldpng
 */

package com.ericsson.sc.rxetcd;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.sc.rxetcd.util.EtcdTestBed;

import io.etcd.jetcd.Txn;
import io.etcd.jetcd.op.Cmp;
import io.etcd.jetcd.op.CmpTarget;
import io.etcd.jetcd.op.Op;
import io.etcd.jetcd.options.DeleteOption;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.LeaseOption;
import io.etcd.jetcd.options.PutOption;
import io.reactivex.Observable;

/**
 * Test class for RxEtcd operations get, put, delete and transactions.
 */
public class RxEtcdOperationsTest
{
    private static final Logger log = LoggerFactory.getLogger(RxEtcdOperationsTest.class);

    private static final String KEYSPACE = "/eric-sc-spr/test/";
    private static EtcdTestBed etcdTestBed;

    private static final int RETRIES = 3;
    private static final int REQUEST_TIMEOUT = 3;

    private RxEtcd rxEtcd;

    @BeforeClass
    private void setUpTestEnvironment()
    {
        log.info("Before class.");

        etcdTestBed = new EtcdTestBed(KEYSPACE, false); // TLS disabled

        log.info("Starting etcd server, keyspace {}", etcdTestBed.getKeyspace());
        etcdTestBed.start();

        rxEtcd = etcdTestBed.createEtcdClient(RETRIES, REQUEST_TIMEOUT, TimeUnit.SECONDS, etcdTestBed.getEndpoint());

    }

    @Test(groups = "functest")
    public void getKey_existingKey()
    {
        var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        var value = EtcdTestBed.btSqn("enabled");

        // Put a key.
        rxEtcd.put(key, value).blockingGet();

        // Check that the key exists and has the correct value.
        var getResponse = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse.getCount() == 1, "There is no key.");
        assertTrue(getResponse.getKvs().get(0).getValue().equals(value), "The key has wrong value.");

        // Cleanup
        assertEquals(rxEtcd.delete(key).blockingGet().getDeleted(), 1);
    }

    @Test(groups = "functest")
    public void getKey_nonExistingKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");

        // Check that the key exists.
        final var getResponse = rxEtcd.get(key).blockingGet();
        assertFalse(getResponse.getCount() > 0, "The key shouldn't exist.");
    }

    @Test(groups = "functest")
    public void getMultipleKeys_existingKeys()
    {
        final var directoryKey = EtcdTestBed.btSqn(KEYSPACE + "occ/");
        final var keyVal = Map.of(EtcdTestBed.btSqn(KEYSPACE + "occ/alpha"),
                                  EtcdTestBed.btSqn("enabled"),
                                  EtcdTestBed.btSqn(KEYSPACE + "occ/beta"),
                                  EtcdTestBed.btSqn("disabled"),
                                  EtcdTestBed.btSqn(KEYSPACE + "occ/gamma"),
                                  EtcdTestBed.btSqn("suspended"));

        // Put the keys.
        keyVal.forEach((k,
                        v) -> rxEtcd.put(k, v).blockingGet());

        // Check that the keys exist and have the correct values.
        final var getResponse = rxEtcd.get(directoryKey, GetOption.newBuilder().withPrefix(directoryKey).build()).blockingGet();
        assertTrue(getResponse.getCount() == 3, "Unexpected number of keys on the given directory.");
        getResponse.getKvs().forEach(kv -> assertTrue(keyVal.get(kv.getKey()).equals(kv.getValue()), "Unexpected key value."));

        // Cleanup
        keyVal.forEach((k,
                        v) -> assertEquals(rxEtcd.delete(k).blockingGet().getDeleted(), 1));
    }

    @Test(groups = "functest")
    public void putKey_newKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var value = EtcdTestBed.btSqn("enabled");

        // Check that the key does not exist.
        final var getResponse1 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse1.getCount() == 0, "The key already exists.");

        // Put the key.
        final var putResponse = rxEtcd.put(key, value).blockingGet();

        // Check that the revision was stepped correctly.
        final var prePutRevision = getResponse1.getHeader().getRevision();
        final var postPutRevision = putResponse.getHeader().getRevision();
        assertTrue(postPutRevision == prePutRevision + 1, "The revision number is not correct.");

        // Check that the key exists and has the correct value.
        final var getResponse2 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse2.getCount() == 1, "There is no key.");
        assertTrue(getResponse2.getKvs().get(0).getValue().equals(value), "The key has wrong value.");

        // Check that the creation revision of the value is correct.
        final var creationRevision = getResponse2.getKvs().get(0).getCreateRevision();
        assertTrue(creationRevision == postPutRevision, "The creation revision number is not correct.");

        // Cleanup
        assertEquals(rxEtcd.delete(key).blockingGet().getDeleted(), 1);
    }

    @Test(groups = "functest")
    public void putKey_existingKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var value1 = EtcdTestBed.btSqn("enabled");
        final var value2 = EtcdTestBed.btSqn("disabled");

        // Check that the key does not exist.
        final var getResponse1 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse1.getCount() == 0, "The key already exists.");

        // Put the key.
        final var putResponse1 = rxEtcd.put(key, value1).blockingGet();

        // Check that the key exists and has the correct value.
        final var getResponse2 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse2.getCount() == 1, "There is no key.");
        assertTrue(getResponse2.getKvs().get(0).getValue().equals(value1), "The key has wrong value.");

        // Update the key's value.
        final var putResponse2 = rxEtcd.put(key, value2).blockingGet();

        // Check that the key exists and has the correct value.
        final var getResponse3 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse3.getCount() == 1, "There is no key or more keys.");
        assertTrue(getResponse3.getKvs().get(0).getValue().equals(value2), "The key has wrong value.");

        // Check that the creation and modification revisions are correct.
        final var putRevision1 = putResponse1.getHeader().getRevision();
        final var putRevision2 = putResponse2.getHeader().getRevision();
        final var creationRevision = getResponse3.getKvs().get(0).getCreateRevision();
        final var modificationRevision = getResponse3.getKvs().get(0).getModRevision();
        assertTrue(creationRevision == putRevision1, "The creation revision number is not correct.");
        assertTrue(modificationRevision == putRevision2, "The modification revision number is not correct.");

        // Check the version of the key.
        final var keyVersion = getResponse3.getKvs().get(0).getVersion();
        assertTrue(keyVersion == 2, "The key version is not correct.");

        // Cleanup
        assertEquals(rxEtcd.delete(key).blockingGet().getDeleted(), 1);
    }

    @Test(groups = "functest")
    public void putKeyWithLease_newKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var value = EtcdTestBed.btSqn("enabled");
        final var leaseTTL = 3;

        // Check that the key does not exist.
        final var getResponse1 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse1.getCount() == 0, "The key already exists.");

        // Create a lease.
        final var lease = rxEtcd.leaseCreate(leaseTTL).blockingGet();
        // Put the key.
        rxEtcd.put(key, value, PutOption.newBuilder().withLeaseId(lease.getID()).build()).blockingGet();

        // Check immediately if the key exists.
        final var getResponse2 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse2.getCount() == 1, "The key does not exist.");

        // Check after the lease expiration, if the key expired. Provide a safety net of
        // 500 milliseconds to allow the ETCD cluster to delete the key.
        final var getResponse3 = Observable.timer(leaseTTL * 1000 + 500, TimeUnit.MILLISECONDS) //
                                           .map(i -> rxEtcd.get(key))
                                           .blockingFirst()
                                           .blockingGet();
        assertTrue(getResponse3.getCount() == 0, "The key should have expired.");
    }

    @Test(groups = "functest")
    public void putKeyWithLease_existingKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var value = EtcdTestBed.btSqn("enabled");
        final var leaseTTL = 3;

        // Check that the key does not exist.
        final var getResponse1 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse1.getCount() == 0, "The key already exists.");

        // Put the key.
        rxEtcd.put(key, value).blockingGet();

        // Check that the key was written.
        final var getResponse2 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse2.getCount() == 1, "The key does not exist.");

        // Create a lease.
        final var lease = rxEtcd.leaseCreate(leaseTTL).blockingGet();
        // Update the key.
        rxEtcd.put(key, value, PutOption.newBuilder().withLeaseId(lease.getID()).build()).blockingGet();

        // Check immediately if the key exists and that it has the correct version.
        final var getResponse3 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse3.getCount() == 1, "The key does not exist.");
        assertTrue(getResponse3.getKvs().get(0).getVersion() == 2, "The key version is not correct.");

        // Check after the lease expiration, if the key expired. Provide a safety net of
        // 500 milliseconds to allow the ETCD cluster to delete the key.
        final var getResponse4 = Observable.timer(leaseTTL * 1000 + 500, TimeUnit.MILLISECONDS) //
                                           .map(i -> rxEtcd.get(key))
                                           .blockingFirst()
                                           .blockingGet();
        assertTrue(getResponse4.getCount() == 0, "The key should have expired.");
    }

    @Test(groups = "functest")
    public void leaseRevoke_existingKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var value = EtcdTestBed.btSqn("enabled");
        final var leaseTTL = 20;

        // Check that the key does not exist.
        final var getResponse1 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse1.getCount() == 0, "The key already exists.");

        // Create a lease.
        final var lease = rxEtcd.leaseCreate(leaseTTL).blockingGet();
        // Put the key.
        rxEtcd.put(key, value, PutOption.newBuilder().withLeaseId(lease.getID()).build()).blockingGet();

        // Check immediately if the key exists.
        final var getResponse2 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse2.getCount() == 1, "The key does not exist.");

        // Revoke the lease for the key.
        final var revokeResponse = rxEtcd.leaseRevoke(lease.getID()).test();
        revokeResponse.awaitDone(5, TimeUnit.SECONDS).assertComplete();

        // Check the key is removed before the lease expiration, because the lease was
        // revoked.
        final var getResponse3 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse3.getCount() == 0, "The key should have expired.");
    }

    @Test(groups = "functest")
    public void leaseRenewOnce_existingKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var value = EtcdTestBed.btSqn("enabled");
        final var leaseTTL = 3;

        // Check that the key does not exist.
        final var getResponse1 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse1.getCount() == 0, "The key already exists.");

        // Create a lease.
        final var lease = rxEtcd.leaseCreate(leaseTTL).blockingGet();
        // Put the key.
        rxEtcd.put(key, value, PutOption.newBuilder().withLeaseId(lease.getID()).build()).blockingGet();

        // Renew the lease for the key 0.5 seconds before the expiration.
        Observable.timer(leaseTTL * 1000 - 500, TimeUnit.MILLISECONDS) //
                  .map(i -> rxEtcd.leaseRenewOnce(lease.getID()))
                  .blockingFirst()
                  .blockingGet();

        // Check that the key exists even after the initial lease expiration, because it
        // was renewed.
        final var getResponse2 = Observable.timer(leaseTTL * 1000 - 500, TimeUnit.MILLISECONDS) //
                                           .map(i -> rxEtcd.get(key))
                                           .blockingFirst()
                                           .blockingGet();
        assertTrue(getResponse2.getCount() == 1, "The key should have been renewed.");

        // Check that the key expires after all.
        final var getResponse3 = Observable.timer(1000, TimeUnit.MILLISECONDS) //
                                           .map(i -> rxEtcd.get(key))
                                           .blockingFirst()
                                           .blockingGet();
        assertTrue(getResponse3.getCount() == 0, "The key should have expired.");
    }

    @Test(groups = "functest")
    public void leaseExistsAfterDeletingKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var value = EtcdTestBed.btSqn("enabled");
        final var leaseTTL = 20;

        // Check that the key does not exist.
        final var getResponse1 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse1.getCount() == 0, "The key already exists.");

        // Create a lease.
        final var lease = rxEtcd.leaseCreate(leaseTTL).blockingGet();
        // Put the key.
        rxEtcd.put(key, value, PutOption.newBuilder().withLeaseId(lease.getID()).build()).blockingGet();

        // Check immediately if the key exists.
        final var getResponse2 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse2.getCount() == 1, "The key does not exist.");

        // Delete the key
        final var delResponse = rxEtcd.delete(key).blockingGet();
        assertTrue(delResponse.getDeleted() == 1, "The key was not deleted.");

        // Check if the lease exists.
        final var leaseInfo = rxEtcd.getLease(lease.getID(), LeaseOption.DEFAULT).blockingGet();
        assertTrue(leaseInfo.getTTL() > 0, "Wrong TTL.");
    }

    @Test(groups = "functest")
    public void deleteKey_existingKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var value = EtcdTestBed.btSqn("enabled");

        // Put the key.
        rxEtcd.put(key, value).blockingGet();

        // Check that the key exists and has the correct value.
        final var getResponse1 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse1.getCount() == 1, "There is no key.");
        assertTrue(getResponse1.getKvs().get(0).getValue().equals(value), "The key has wrong value.");

        // Delete the key and assert the response.
        final var deleteResponse = rxEtcd.delete(key).blockingGet();
        assertTrue(deleteResponse.getDeleted() == 1, "No key was deleted.");

        // Check that the key does not exist.
        final var getResponse2 = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse2.getCount() == 0, "The key shouldn't exist.");
    }

    @Test(groups = "functest")
    public void deleteKey_nonExistingKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");

        // Delete the key and assert the response.
        final var deleteResponse = rxEtcd.delete(key).blockingGet();
        assertTrue(deleteResponse.getDeleted() == 0, "No key should be deleted.");
    }

    @Test(groups = "functest")
    public void deleteMultipleKeys_existingKeys()
    {
        final var directoryKey = EtcdTestBed.btSqn(KEYSPACE + "occ/");
        final var keyVal = Map.of(EtcdTestBed.btSqn(KEYSPACE + "occ/alpha"),
                                  EtcdTestBed.btSqn("enabled"),
                                  EtcdTestBed.btSqn(KEYSPACE + "occ/beta"),
                                  EtcdTestBed.btSqn("disabled"),
                                  EtcdTestBed.btSqn(KEYSPACE + "occ/gamma"),
                                  EtcdTestBed.btSqn("suspended"));

        // Put the keys.
        keyVal.forEach((k,
                        v) -> rxEtcd.put(k, v).blockingGet());

        // Check that the keys exist.
        final var getResponse1 = rxEtcd.get(directoryKey, GetOption.newBuilder().withPrefix(directoryKey).build()).blockingGet();
        assertTrue(getResponse1.getCount() == 3, "Unexpected number of keys on the given directory.");

        // Delete the keys.
        rxEtcd.delete(directoryKey, DeleteOption.newBuilder().withPrefix(directoryKey).build()).blockingGet();

        // Check that the keys are erased.
        final var getResponse2 = rxEtcd.get(directoryKey, GetOption.newBuilder().withPrefix(directoryKey).build()).blockingGet();
        assertTrue(getResponse2.getCount() == 0, "The keys shouldn't exist.");
    }

    @Test(groups = "functest")
    public void txnPutKeyIfEmpty_nonExistingKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var value = EtcdTestBed.btSqn("enabled");

        // Create the arguments for the transaction.
        UnaryOperator<Txn> txnArguments = txn -> txn.If(new Cmp(key, Cmp.Op.EQUAL, CmpTarget.version(0)))
                                                    .Then(Op.put(key, value, PutOption.newBuilder().build()));

        // Execute the transaction.
        final var txnResponse = rxEtcd.txn(txnArguments).blockingGet();

        // Assert the response
        assertTrue(txnResponse.isSucceeded(), "The transaction should succeed.");

        // Check that the key exists.
        final var getResponse = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse.getCount() == 1, "The key does not exist.");

        // Cleanup
        assertEquals(rxEtcd.delete(key).blockingGet().getDeleted(), 1);
    }

    @Test(groups = "functest")
    public void txnPutKeyIfEmpty_existingKey()
    {
        final var key = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var value = EtcdTestBed.btSqn("enabled");

        // Put the key.
        rxEtcd.put(key, value).blockingGet();

        // Create the arguments for the transaction.
        UnaryOperator<Txn> txnArguments = txn -> txn.If(new Cmp(key, Cmp.Op.EQUAL, CmpTarget.version(0)))
                                                    .Then(Op.put(key, value, PutOption.newBuilder().build()));

        // Execute the transaction.
        final var txnResponse = rxEtcd.txn(txnArguments).blockingGet();

        // Assert the response
        assertFalse(txnResponse.isSucceeded(), "The transaction should not succeed.");

        // Check that the key maintained its version and was not overwritten.
        final var getResponse = rxEtcd.get(key).blockingGet();
        assertTrue(getResponse.getCount() == 1, "The key does not exist.");
        assertTrue(getResponse.getKvs().get(0).getVersion() == 1, "The version of the key is not correct.");

        // Cleanup
        assertEquals(rxEtcd.delete(key).blockingGet().getDeleted(), 1);
    }

    @Test(groups = "functest")
    public void txnPutOtherKeyIfKeyExists_existingKey()
    {
        final var conditionKey = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var newKey = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha/status");
        final var conditionValue = EtcdTestBed.btSqn("enabled");
        final var newValue = EtcdTestBed.btSqn("running");

        // Put the condition key.
        rxEtcd.put(conditionKey, conditionValue).blockingGet();

        // Create the arguments for the transaction.
        UnaryOperator<Txn> txnArguments = txn -> txn.If(new Cmp(conditionKey, Cmp.Op.GREATER, CmpTarget.version(0)))
                                                    .Then(Op.put(newKey, newValue, PutOption.newBuilder().build()));

        // Execute the transaction.
        final var txnResponse = rxEtcd.txn(txnArguments).blockingGet();

        // Assert the response.
        assertTrue(txnResponse.isSucceeded(), "The transaction should succeed.");

        // Check that the new key was written and has the correct value.
        final var getResponse = rxEtcd.get(newKey).blockingGet();
        assertTrue(getResponse.getCount() == 1, "The key does not exist.");
        assertTrue(getResponse.getKvs().get(0).getValue().equals(newValue), "The value of the key is not correct.");
        // Cleanup
        assertEquals(rxEtcd.delete(conditionKey).blockingGet().getDeleted(), 1);
        assertEquals(rxEtcd.delete(newKey).blockingGet().getDeleted(), 1);
    }

    @Test(groups = "functest")
    public void txnPutOtherKeyWhenKeyHasSpecificValue_correctConditionValue()
    {
        final var conditionKey = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var newKey = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha/status");
        final var actualValue = EtcdTestBed.btSqn("enabled");
        final var conditionValue = EtcdTestBed.btSqn("enabled");
        final var newValueThen = EtcdTestBed.btSqn("running");
        final var newValueElse = EtcdTestBed.btSqn("terminated");

        // Put the condition key.
        rxEtcd.put(conditionKey, actualValue).blockingGet();

        // Create the arguments for the transaction.
        UnaryOperator<Txn> txnArguments = txn -> txn.If(new Cmp(conditionKey, Cmp.Op.EQUAL, CmpTarget.value(conditionValue)))
                                                    .Then(Op.put(newKey, newValueThen, PutOption.newBuilder().build()))
                                                    .Else(Op.put(newKey, newValueElse, PutOption.newBuilder().build()));

        // Execute the transaction.
        final var txnResponse = rxEtcd.txn(txnArguments).blockingGet();

        // Assert the response.
        assertTrue(txnResponse.isSucceeded(), "The transaction should succeed.");

        // Check that the new key was written and has the correct value.
        final var getResponse = rxEtcd.get(newKey).blockingGet();
        assertTrue(getResponse.getCount() == 1, "The key does not exist.");
        assertTrue(getResponse.getKvs().get(0).getValue().equals(newValueThen), "The value of the key is not correct.");

        // Cleanup
        assertEquals(rxEtcd.delete(conditionKey).blockingGet().getDeleted(), 1);
        assertEquals(rxEtcd.delete(newKey).blockingGet().getDeleted(), 1);
    }

    @Test(groups = "functest")
    public void txnPutOtherKeyWhenKeyHasSpecificValue_incorrectConditionValue()
    {
        final var conditionKey = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha");
        final var newKey = EtcdTestBed.btSqn(KEYSPACE + "occ/alpha/status");
        final var actualValue = EtcdTestBed.btSqn("disabled");
        final var conditionValue = EtcdTestBed.btSqn("enabled");
        final var newValueThen = EtcdTestBed.btSqn("running");
        final var newValueElse = EtcdTestBed.btSqn("terminated");

        // Put the condition key.
        rxEtcd.put(conditionKey, actualValue).blockingGet();

        // Create the arguments for the transaction.
        UnaryOperator<Txn> txnArguments = txn -> txn.If(new Cmp(conditionKey, Cmp.Op.EQUAL, CmpTarget.value(conditionValue)))
                                                    .Then(Op.put(newKey, newValueThen, PutOption.newBuilder().build()))
                                                    .Else(Op.put(newKey, newValueElse, PutOption.newBuilder().build()));

        // Execute the transaction.
        final var txnResponse = rxEtcd.txn(txnArguments).blockingGet();

        // Assert the response.
        assertFalse(txnResponse.isSucceeded(), "The transaction should not succeed. Else clause should be executed.");

        // Check that the new key was written and has the correct value.
        final var getResponse = rxEtcd.get(newKey).blockingGet();
        assertTrue(getResponse.getCount() == 1, "The key does not exist.");
        assertTrue(getResponse.getKvs().get(0).getValue().equals(newValueElse), "The value of the key is not correct.");

        // Cleanup
        assertEquals(rxEtcd.delete(conditionKey).blockingGet().getDeleted(), 1);
        assertEquals(rxEtcd.delete(newKey).blockingGet().getDeleted(), 1);
    }

    @AfterClass
    private void cleanUpTestEnvironment()
    {
        log.info("After class.");

        log.info("Closing non-tls and tls containers");
        rxEtcd.close().blockingAwait();
        etcdTestBed.stopEtcdServers();

    }

    @BeforeMethod
    private void prepare(Method method)
    {
        log.info("Before method.");
        log.info("Executing: {}", method.getName());
    }
}
