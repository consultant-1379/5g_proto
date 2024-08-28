package com.ericsson.sc.bsf.etcd;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class PcfRt
{
    private final UUID id;
    private final long recoveryTime;

    public PcfRt(UUID id,
                 long recoveryTime)
    {
        Objects.requireNonNull(id);
        if (recoveryTime < 0)
            throw new IllegalArgumentException("Recovery time should be greater than 0");

        this.id = id;
        this.recoveryTime = recoveryTime;
    }

    public PcfRt(UUID id,
                 Instant recoveryTime)
    {
        Objects.requireNonNull(id);
        Objects.requireNonNull(recoveryTime);

        this.id = id;
        this.recoveryTime = recoveryTime.toEpochMilli();
    }

    @JsonGetter("pcfId")
    public UUID getId()
    {
        return id;
    }

    @JsonGetter("recoveryTime")
    public long getRecoverytime()
    {
        return recoveryTime;
    }

    @JsonIgnore
    public Instant getRtInstant()
    {
        return Instant.ofEpochMilli(recoveryTime);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, recoveryTime);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PcfRt other = (PcfRt) obj;
        return Objects.equals(id, other.id) && Objects.equals(recoveryTime, other.recoveryTime);
    }

    @Override
    public String toString()
    {
        final var builder2 = new StringBuilder();
        builder2.append("PcfRt [nfInstanceId=");
        builder2.append(id);
        builder2.append(", recoveryTime=");
        builder2.append(recoveryTime);
        builder2.append("]");
        return builder2.toString();
    }

}
