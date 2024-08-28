package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.ericsson.esc.bsf.manager.validator.BsfRule;
import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.ericsson.sc.bsf.model.BindingDatabaseScan.Configuration;
import com.ericsson.sc.bsf.model.BsfService;
import com.ericsson.sc.bsf.model.CheckUponLookup;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.bsf.model.EricssonBsfBsfFunction;
import com.ericsson.sc.bsf.model.NfInstance;
import com.ericsson.sc.bsf.model.PcfRecoveryTime;

public class RuleTest
{
    private final BindingDatabaseScan dbScanCfg = new BindingDatabaseScan().withConfiguration(Configuration.AUTO).withSchedule(null);
    private final BsfService bsfSvc = new BsfService().withName("BsfSerivce")
                                                      .withPcfRecoveryTime(new PcfRecoveryTime().withBindingDatabaseScan(dbScanCfg)
                                                                                                .withCheckUponLookup(new CheckUponLookup().withEnabled(true)
                                                                                                                                          .withDeletionUponLookup(true)));
    private final NfInstance nfInstance = new NfInstance().withName("BsfSerivce").withBsfService(List.of(bsfSvc));
    private final EricssonBsfBsfFunction bsfFunc = new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance));
    private final EricssonBsf config = new EricssonBsf().withEricssonBsfBsfFunction(bsfFunc);

    @Test
    public void testDefaultConfig()
    {

        var res = BsfRule.SCHEDULE_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Invalid cron expression found with SCHEDULED configuration");

        this.dbScanCfg.setConfiguration(Configuration.DISABLED);
        res = BsfRule.SCHEDULE_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "SCHEDULE should be null when DISABLED configuration is used");

        this.dbScanCfg.setConfiguration(Configuration.SCHEDULED);
        this.dbScanCfg.setSchedule("* * * * *");
        res = BsfRule.SCHEDULE_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "SCHEDULE should be null when AUTO configuration is used");

        this.dbScanCfg.setSchedule("* * * * * ?");
        res = BsfRule.SCHEDULE_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Invalid Quartz cron expression found with SCHEDULED configuration");

    }

    @Test
    public void testNegativeScheduleRule()
    {
        this.dbScanCfg.setSchedule("* ** * * ?");
        var res = BsfRule.SCHEDULE_RULE.validateOn(this.config).blockingGet();
        assertFalse(res.getResult(), "Valid Quartz cron expression found with SCHEDULED configuration");

        this.dbScanCfg.setSchedule("");
        res = BsfRule.SCHEDULE_RULE.validateOn(this.config).blockingGet();
        assertFalse(res.getResult(), "Valid cron expression found with SCHEDULED configuration");

        this.dbScanCfg.setSchedule(" ");
        res = BsfRule.SCHEDULE_RULE.validateOn(this.config).blockingGet();
        assertFalse(res.getResult(), "Valid cron expression found with SCHEDULED configuration");

        this.dbScanCfg.setSchedule(null);
        res = BsfRule.SCHEDULE_RULE.validateOn(this.config).blockingGet();
        assertFalse(res.getResult(), "Null cron expression should not be acceptable with SCHEDULED configuration");
    }

    @Test
    public void testScheduleRuleEmptyPcfRecoveryTime()
    {
        final var bsfSvc = new BsfService().withName("BsfSerivce");
        final var nfInstance = new NfInstance().withName("BsfSerivce").withBsfService(List.of(bsfSvc));
        final var bsfFunc = new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance));
        final var config = new EricssonBsf().withEricssonBsfBsfFunction(bsfFunc);

        final var res = BsfRule.SCHEDULE_RULE.validateOn(config).blockingGet();
        assertTrue(res.getResult());
        assertEquals(res.getErrorMessage(), "Not applicable");
    }
}
