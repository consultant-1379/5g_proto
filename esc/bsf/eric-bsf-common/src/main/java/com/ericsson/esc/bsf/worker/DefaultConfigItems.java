package com.ericsson.esc.bsf.worker;

import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.ericsson.sc.bsf.model.CheckUponLookup;
import com.ericsson.sc.bsf.model.PcfRecoveryTime;
import com.ericsson.sc.bsf.model.BindingDatabaseScan.Configuration;

public class DefaultConfigItems
{

    public static PcfRecoveryTime generateDefaultPcfRt()
    {
        final var checkUponLookup = new CheckUponLookup().withDeletionUponLookup(true).withEnabled(true);
        final var bindingDatabaseScan = new BindingDatabaseScan().withConfiguration(Configuration.AUTO).withSchedule(null);

        return new PcfRecoveryTime().withBindingDatabaseScan(bindingDatabaseScan).withCheckUponLookup(checkUponLookup);
    }
}
