package com.ericsson.esc.bsf.manager;

import io.reactivex.Completable;

public class PcfRtManager
{
    private final PcfRtDbUpdater pcfRtDbUpdater;

    public PcfRtManager(PcfRtDbUpdater pcfRtDbUpdater)
    {
        this.pcfRtDbUpdater = pcfRtDbUpdater;
    }

    public Completable run()
    {
        return this.pcfRtDbUpdater.run();
    }

    public Completable init()
    {
        return this.pcfRtDbUpdater.init();
    }

    public Completable stop()
    {
        return this.pcfRtDbUpdater.stop();
    }

}
