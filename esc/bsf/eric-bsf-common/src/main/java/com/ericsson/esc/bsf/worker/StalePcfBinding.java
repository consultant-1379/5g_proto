package com.ericsson.esc.bsf.worker;

import java.util.UUID;

public class StalePcfBinding
{
    private final UUID bindingId;
    private final UUID pcfId;
    private final Reason reason;

    public enum Reason
    {
        MULTIPLE_BINDINGS_FOUND,
        PCF_RECOVERY_TIME
    }

    public StalePcfBinding(UUID bindingId,
                           UUID pcfId,
                           Reason reason)
    {
        this.bindingId = bindingId;
        this.pcfId = pcfId;
        this.reason = reason;
    }

    /**
     * @return the bindingId
     */
    public UUID getBindingId()
    {
        return bindingId;
    }

    /**
     * @return the pcfId
     */
    public UUID getPcfId()
    {
        return pcfId;
    }

    /**
     * @return the reason
     */
    public Reason getReason()
    {
        return reason;
    }

}
