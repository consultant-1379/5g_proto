package com.ericsson.esc.bsf.worker;

import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.ericsson.sc.bsf.model.CheckUponLookup;

public final class RecoveryTimeConfig
{
    private boolean rtResolution = true;
    private boolean deletionUponLookup = true;
    private BindingDatabaseScan.Configuration scanConfig = BindingDatabaseScan.Configuration.AUTO;

    public RecoveryTimeConfig(CheckUponLookup rt,
                              BindingDatabaseScan scanCfg)
    {
        this.rtResolution = rt.getEnabled();
        this.deletionUponLookup = rt.getDeletionUponLookup();
        this.scanConfig = scanCfg.getConfiguration();
    }

    public RecoveryTimeConfig(boolean resolution,
                              boolean deletion)
    {
        this.rtResolution = resolution;
        this.deletionUponLookup = deletion;
    }

    public BindingDatabaseScan.Configuration getScanConfig()
    {
        return scanConfig;
    }

    /**
     * @return the deletionUponLookup
     */
    public boolean getDeletionUponLookup()
    {
        return deletionUponLookup;
    }

    /**
     * @return the rtResolution
     */
    public boolean getRtResolution()
    {
        return rtResolution;
    }

}
