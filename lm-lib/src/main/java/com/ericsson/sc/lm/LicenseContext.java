/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jan 27, 2020
 *     Author: eaoknkr
 */

package com.ericsson.sc.lm;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.ericsson.sc.lm.model.ur.CapacityLicenseType;
import com.ericsson.sc.lm.model.lr.LicenseType;
import com.ericsson.adpal.pm.PmAdapter.Query;
import com.google.common.collect.ImmutableMap;

/**
 * 
 */
public class LicenseContext
{
    private final String productType;
    private final String consumerId;
    private final HashMap<String, LicenseKey> licenseKeys = new HashMap<>();

    private static final ImmutableMap<CapacityLicenseType, LicenseType> capacitiesMap = ImmutableMap.of(CapacityLicenseType.PEAK,
                                                                                                        LicenseType.CAPACITY_PEAK,
                                                                                                        CapacityLicenseType.CUMULATIVE,
                                                                                                        LicenseType.CAPACITY_CUMULATIVE,
                                                                                                        CapacityLicenseType.PEAK_PERSISTENT,
                                                                                                        LicenseType.CAPACITY_PEAK_PERSISTENT);

    public LicenseContext(String productType,
                          String consumerId,
                          List<LicenseKey> licenseKeys)
    {
        this.productType = productType;
        this.consumerId = consumerId;
        licenseKeys.forEach(lk -> this.licenseKeys.put(lk.getId(), lk));
    }

    public String getProductType()
    {
        return this.productType;
    }

    public String getConsumerId()
    {
        return this.consumerId;
    }

    public Collection<LicenseKey> getLicenseKeys()
    {
        return this.licenseKeys.values();
    }

    public LicenseKey find(String licenseId)
    {
        return this.licenseKeys.get(licenseId);
    }

    public static class LicenseKey
    {
        private final String id;
        private final CapacityLicenseType capacityLicenseType;
        private final LicenseType licenseType;
        private final Query.Element metric;

        public LicenseKey(String id,
                          CapacityLicenseType capacityLicenseType,
                          Query.Element metric)
        {
            this.id = id;
            this.capacityLicenseType = capacityLicenseType;
            this.licenseType = capacitiesMap.get(this.capacityLicenseType);
            this.metric = metric;
        }

        public String getId()
        {
            return id;
        }

        /**
         * @return the capacityLicenseType
         */
        public CapacityLicenseType getCapacityLicenseType()
        {
            return capacityLicenseType;
        }

        /**
         * @return the licenseType
         */
        public LicenseType getLicenseType()
        {
            return licenseType;
        }

        public Query.Element getMetric()
        {
            return this.metric;
        }
    }

}
