package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.Test;

import com.ericsson.esc.bsf.db.MockPcfRtService;
import com.ericsson.sc.bsf.etcd.PcfRt;

import io.reactivex.Flowable;

public class PcfRtDbUpdaterTest
{
    private final String date1 = "1985-04-13T23:20:50.52Z";
    private final Instant recoveryTime1 = Instant.parse(date1);
    private final String date2 = "1985-04-13T23:50:50.52Z";
    private final Instant recoveryTime2 = Instant.parse(date2);

    private final UUID pcfId1 = UUID.randomUUID();
    private final UUID pcfId2 = UUID.randomUUID();

    @Test(groups = "functest")
    public void updateTest()
    {

        final var pcfRts = Flowable.just(List.of(new PcfRt(pcfId1, recoveryTime1), new PcfRt(pcfId2, recoveryTime2)));

        final var pcfRtService = new MockPcfRtService();
        final var updater = new PcfRtDbUpdater(pcfRtService, pcfRts);

        final var updateTester = pcfRtService.getFetchedResult().test();
        final var runDisp = updater.run().subscribe();

        final var updatesResult = updateTester.awaitCount(2).values();

        assertTrue(updatesResult.size() == 2);
        assertTrue(pcfRtService.getReportCount() == 2);
        assertTrue(!updatesResult.contains(false));

        updateTester.dispose();
        runDisp.dispose();
    }
}
