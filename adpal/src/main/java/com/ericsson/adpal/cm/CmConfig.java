package com.ericsson.adpal.cm;

public class CmConfig<T>
{
    private final T config;
    private final String eTag;

    public CmConfig(T config,
                    String eTag)
    {
        this.config = config;
        this.eTag = eTag;
    }

    public T get()
    {
        return config;
    }

    public String getETag()
    {
        return this.eTag;
    }
}
