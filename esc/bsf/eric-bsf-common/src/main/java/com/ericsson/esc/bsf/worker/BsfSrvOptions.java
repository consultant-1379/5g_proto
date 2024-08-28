package com.ericsson.esc.bsf.worker;

import java.util.Objects;

import com.ericsson.sc.bsf.model.IngressConnectionProfile;

import io.vertx.core.http.Http2Settings;

public class BsfSrvOptions
{
    private final boolean globalTracing;
    private final long hpack;
    private final int dscp;

    public BsfSrvOptions(Boolean globalTracing,
                         Long hpack,
                         Integer dscp)
    {
        this.globalTracing = globalTracing != null && globalTracing;
        this.hpack = Objects.nonNull(hpack) ? hpack : Http2Settings.DEFAULT_HEADER_TABLE_SIZE;
        this.dscp = Objects.nonNull(dscp) ? dscp : 0;
    }

    public BsfSrvOptions(Boolean globalTracing,
                         IngressConnectionProfile ingressConnectionProfile)
    {
        this.globalTracing = globalTracing != null && globalTracing;

        final var getHpack = Objects.nonNull(ingressConnectionProfile) && Objects.nonNull(ingressConnectionProfile.getHpackTableSize());
        final var getDscp = Objects.nonNull(ingressConnectionProfile) && Objects.nonNull(ingressConnectionProfile.getDscpMarking());
        this.hpack = getHpack ? ingressConnectionProfile.getHpackTableSize() : Http2Settings.DEFAULT_HEADER_TABLE_SIZE;
        this.dscp = getDscp ? ingressConnectionProfile.getDscpMarking() : 0;
    }

    public BsfSrvOptions()
    {
        this.globalTracing = false;
        this.hpack = Http2Settings.DEFAULT_HEADER_TABLE_SIZE;
        this.dscp = 0;
    }

    public boolean getGlobalTracing()
    {
        return globalTracing;
    }

    public long getHpack()
    {
        return hpack;
    }

    public int getDscp()
    {
        return dscp;
    }

    @Override
    public String toString()
    {
        return "BsfSrvOptions [globalTracing=" + globalTracing + ", hpack=" + hpack + ", dscp=" + dscp + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (globalTracing ? 1231 : 1237);
        result = prime * result + (int) (hpack ^ (hpack >>> 32));
        result = prime * result + dscp;
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {

        if (!(obj instanceof BsfSrvOptions))
        {
            return false;
        }
        else
        {
            final var other = (BsfSrvOptions) obj;
            return this.globalTracing == other.globalTracing && this.hpack == other.hpack && this.dscp == other.dscp;
        }
    }

}
