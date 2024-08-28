package com.ericsson.esc.bsf.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ericsson.esc.bsf.worker.CmConfigurationUtil;
import com.ericsson.sc.bsf.model.NfInstance;
import com.ericsson.sc.bsf.model.Vtap;

import org.testng.annotations.Test;

public class CmConfigurationUtilTest
{

    @Test()
    public void isVtapEnabledNullTest()
    {
        // empty Vtap
        final var instance = new NfInstance();
        final var vtap = CmConfigurationUtil.isVtapEnabled(instance);
        assertFalse("Empty vtap should be by default false", vtap);

        // Vtap with enabled True
        instance.withVtap(new Vtap());
        final var enabledTrue = CmConfigurationUtil.isVtapEnabled(instance);
        assertTrue("Vtap enabled is true but something went wrong", enabledTrue);

        // Vtap with enabled False
        instance.withVtap(new Vtap().withEnabled(false));
        final var enabledFalse = CmConfigurationUtil.isVtapEnabled(instance);
        assertFalse("Vtap is false, expected result is false", enabledFalse);

        // Vtap with enabled Null
        instance.withVtap(new Vtap().withEnabled(null));
        final var enabledNull = CmConfigurationUtil.isVtapEnabled(instance);
        assertFalse("Vtap is null, expected result is false", enabledNull);

    }

}
