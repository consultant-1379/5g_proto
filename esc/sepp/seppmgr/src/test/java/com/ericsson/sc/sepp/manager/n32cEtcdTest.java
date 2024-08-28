package com.ericsson.sc.sepp.manager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.sc.rxetcd.RxEtcd;
import com.ericsson.sc.rxetcd.util.EtcdTestBed;
import com.ericsson.sc.sepp.manager.N32cInterface.SecurityNegotiationItemBuilder;

public class n32cEtcdTest
{
    private static final Logger log = LoggerFactory.getLogger(n32cEtcdTest.class);

    private static final String PREFIX = N32cSerializer.SEPP_PREFIX;

    private EtcdTestBed etcdTestBed;
    private RxEtcd rxEtcd;
    private N32cInterface db;
    private N32cAlarmHandler n32cah;

    @BeforeClass
    private void setUpTestEnvironment() throws IOException
    {
        log.info("Before class.");

        etcdTestBed = new EtcdTestBed(PREFIX, true);
        etcdTestBed.start();
        rxEtcd = etcdTestBed.createEtcdClient(3, 5, TimeUnit.SECONDS, etcdTestBed.getEndpoint());
        rxEtcd.ready().blockingAwait(); // Ensure DB is up
        db = new N32cInterface(rxEtcd, n32cah);
    }

    @AfterClass
    private void cleanUpTestEnvironment()
    {
        log.info("After class.");

        etcdTestBed.closeClient();
        etcdTestBed.stopEtcdServers();
    }

    @Test(groups = "functest")
    public void readTest()
    {
        db.writeSecurityNegotiationDatum(new SecurityNegotiationItemBuilder().newItem()
                                                                             .withSeppName("nf-ref")
                                                                             .withRoamingPartnerRef("rp-ref")
                                                                             .withOperationalState("inactive"));

        log.info(db.getNfInstances().toString());
    }

    @AfterMethod
    public void cleanupDb()
    {
        this.etcdTestBed.clearKeyspace();
    }
}
