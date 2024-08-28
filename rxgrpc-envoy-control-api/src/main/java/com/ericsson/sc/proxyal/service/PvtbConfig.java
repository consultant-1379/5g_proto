package com.ericsson.sc.proxyal.service;

public class PvtbConfig
{
    private final Boolean isConfigured;

    public PvtbConfig(Boolean isConfigured)
    {
        this.isConfigured = isConfigured;
    }

    public Boolean getIsConfigured()
    {
        return isConfigured;
    }

    @Override
    public int hashCode()
    {
        final var prime = 31;
        var result = 1;
        result = prime * result + ((isConfigured == null) ? 0 : isConfigured.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        var builder = new StringBuilder();
        builder.append("PvtbConfig [isConfigured=");
        builder.append(isConfigured);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (!(obj instanceof PvtbConfig))
            return false;
        PvtbConfig other = (PvtbConfig) obj;
        if (isConfigured == null)
        {
            if (other.isConfigured != null)
                return false;
        }
        else if (!isConfigured.equals(other.isConfigured))
            return false;
        return true;
    }

}
