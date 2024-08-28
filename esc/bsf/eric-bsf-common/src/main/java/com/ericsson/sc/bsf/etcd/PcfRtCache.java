package com.ericsson.sc.bsf.etcd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class PcfRtCache
{
    private AtomicReference<ConcurrentHashMap<UUID, PcfRt>> cache = new AtomicReference<>(new ConcurrentHashMap<>());

    public void reset(List<PcfRt> pcfRts)
    {
        cache.set(initialize(pcfRts));
    }

    private ConcurrentHashMap<UUID, PcfRt> initialize(List<PcfRt> pcfRts)
    {

        final var tmpCache = new ConcurrentHashMap<UUID, PcfRt>();
        pcfRts.stream().forEach(pcfRt -> tmpCache.put(pcfRt.getId(), pcfRt));
        return tmpCache;
    }

    public void update(PcfRt pcfRt)
    {
        this.cache.get()
                  .merge(pcfRt.getId(),
                         pcfRt,
                         (oldValue,
                          newValue) -> Stream.of(oldValue, newValue).max(Comparator.comparing(PcfRt::getRecoverytime)).get());
    }

    public void remove(UUID id)
    {
        this.cache.get().remove(id);
    }

    public PcfRt get(UUID id)
    {
        return this.cache.get().get(id);
    }

    /**
     * Get all PcfRt entries in cache
     * 
     * @return A snapshot of all cache entries.
     */
    public List<PcfRt> getAll()
    {
        final var currentCache = Optional.ofNullable(this.cache.get());
        return currentCache.map(c -> List.copyOf(c.values())).orElse(new ArrayList<>());
    }

    public void clear()
    {
        cache.get().clear();
    }

    ConcurrentMap<UUID, PcfRt> get()
    {
        return this.cache.get();
    }

}
