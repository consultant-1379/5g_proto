package com.ericsson.sim.chf.r17;

import com.ericsson.sim.chf.counts.Instance;

public class Statistics
{
    public static class ChargingInstance extends Instance
    {
        public static class Pool extends com.ericsson.sim.chf.counts.Pool<ChargingInstance>
        {
            public Pool()
            {
                super(id -> new ChargingInstance(id));
            }
        }

        public ChargingInstance(String id)
        {
            super(id);
        }
    }

    public static class Nrf extends Instance
    {
        public static class Pool extends com.ericsson.sim.chf.counts.Pool<Nrf>
        {
            public Pool()
            {
                super(id -> new Nrf(id));
            }
        }

        public Nrf(String id)
        {
            super(id);
        }
    }

    public static class Subscription extends Instance
    {
        public static class Pool extends com.ericsson.sim.chf.counts.Pool<Subscription>
        {
            public Pool()
            {
                super(id -> new Subscription(id));
            }
        }

        public Subscription(String id)
        {
            super(id);
        }
    }
}
