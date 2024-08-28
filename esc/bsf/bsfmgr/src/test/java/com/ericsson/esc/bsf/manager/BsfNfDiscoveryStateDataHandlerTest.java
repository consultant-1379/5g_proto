/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 9, 2024
 *     Author: znpvaap
 */

package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.sc.rxetcd.EtcdEntries;
import com.ericsson.sc.rxetcd.EtcdEntry;
import com.ericsson.sc.rxetcd.EtcdKv;
import com.ericsson.sc.rxetcd.JsonValueSerializer;
import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.util.EtcdTestBed;
import com.ericsson.utilities.json.Smile;
import com.google.protobuf.ByteString;

import io.etcd.jetcd.ByteSequence;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * 
 */
public class BsfNfDiscoveryStateDataHandlerTest
{
    private static final Logger log = LoggerFactory.getLogger(BsfNfDiscoveryStateDataHandlerTest.class);

    private EtcdTestBed etcdTestBed;
    private RxEtcd rxEtcd;
    private static final String LAST_UPDATE = "last-update";
    private final PcfDiscoverer pcfDiscoverer = Mockito.mock(PcfDiscoverer.class);

    private static final JsonValueSerializer<String, String> etcdSerializer = new JsonValueSerializer<>("",
                                                                                                        String.class,
                                                                                                        Smile.om(),
                                                                                                        (String key) -> ByteString.copyFrom(key,
                                                                                                                                            StandardCharsets.UTF_8),
                                                                                                        key -> new String(key, StandardCharsets.UTF_8));

    private static final Flowable<String> TIMESTAMP = Flowable.just("1");

    @BeforeClass
    private void setUpTestEnvironment()
    {
        log.info("Before class.");

        this.etcdTestBed = new EtcdTestBed("", true);
        this.etcdTestBed.start();
        this.rxEtcd = this.etcdTestBed.createEtcdClient(3, 5, TimeUnit.SECONDS, this.etcdTestBed.getEndpoint());
        this.rxEtcd.ready().blockingAwait(); // Ensure DB is up
    }

    @BeforeMethod
    private void checkEtcdEmptyAndInitMockSetup()
    {
        final var etcdKvSize = this.rxEtcd.get(ByteSequence.from(LAST_UPDATE, StandardCharsets.UTF_8)).map(response -> response.getKvs().size()).blockingGet();
        assertTrue(etcdKvSize == 0, "The etcd key-value store is not empty");

        Mockito.when(this.pcfDiscoverer.getTimestamp()).thenReturn(BsfNfDiscoveryStateDataHandlerTest.TIMESTAMP);
        Mockito.when(this.pcfDiscoverer.lastUpdateTimestampToEtcd(etcdSerializer, this.rxEtcd, LAST_UPDATE)).thenCallRealMethod();
    }

    @AfterMethod
    private void cleanUpEtcdKv()
    {
        this.rxEtcd.delete(ByteSequence.from(LAST_UPDATE, StandardCharsets.UTF_8));
    }

    @AfterClass
    private void cleanUpTestEnvironment()
    {
        log.info("After class.");

        this.etcdTestBed.closeClient();
        this.etcdTestBed.stopEtcdServers();
    }

    @Test(groups = "functest")
    private void writeTimestampInEtcd()
    {
        this.pcfDiscoverer.lastUpdateTimestampToEtcd(etcdSerializer, rxEtcd, LAST_UPDATE).blockingAwait();
        final var fetchedData = getTimestampFromEtcd().blockingGet();
        assertEquals(fetchedData, "1", "The fetched data from etcd is different to the data that is stored in it");
    }

    private Single<String> getTimestampFromEtcd()
    {
        return this.rxEtcd.get(etcdSerializer.keyBytes(LAST_UPDATE))
                          .map(getResp -> new EtcdEntries<>(getResp.getHeader().getRevision(),
                                                            getResp.getKvs() //
                                                                   .stream()
                                                                   .map(kv ->
                                                                   {
                                                                       try
                                                                       {
                                                                           return new EtcdEntry<>(etcdSerializer, kv);
                                                                       }
                                                                       catch (NullPointerException e)
                                                                       {
                                                                           log.warn("Serializer, value or key is not instanitated as an etcd entry: {}",
                                                                                    kv.getKey(),
                                                                                    e);
                                                                           return null;
                                                                       }
                                                                   })
                                                                   .filter(Objects::nonNull)
                                                                   .toList()))
                          .map(entries -> entries.getEntries().stream().findFirst().map(EtcdKv::getValue).orElse(""));
    }
}
