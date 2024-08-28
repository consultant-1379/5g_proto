package com.ericsson.sc.rxkms;

import org.testng.annotations.Test;
import org.testng.Assert;

public class KmsParametersTest
{

    @Test
    void checkParameters()
    {
        KmsParameters params = KmsParameters.instance;
        Assert.assertEquals(params.sipTlsRootCaPath, "/tmp/unitTests");
        Assert.assertEquals(params.globalTlsEnabled, false);
    }
}