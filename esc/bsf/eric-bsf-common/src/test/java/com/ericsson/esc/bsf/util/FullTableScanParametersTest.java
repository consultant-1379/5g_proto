package com.ericsson.esc.bsf.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertThrows;

import com.ericsson.esc.bsf.db.FullTableScanParameters;
import org.testng.annotations.Test;

public class FullTableScanParametersTest
{

    @Test(groups = "functest", enabled = true)
    public void validFullTableScanParameters()
    {
        final var paramsValues = new FullTableScanParameters.Builder().setPageSize(1000)
                                                                      .setPageThrottlingMillis(10000)
                                                                      .setDeleteThrottlingMillis(10000)
                                                                      .build();

        assertEquals(paramsValues.getPageSize(), 1000, "Expected valid PageSize");
        assertEquals(paramsValues.getPageThrottlingMillis(), 10000, "Expected valid PageThrottlingMillis");
        assertEquals(paramsValues.getDeleteThrottlingMillis(), 10000, "Expected valid DeleteThrottlingMillis");
    }

    @Test(groups = "functest", enabled = true)
    public void InvalidArgumentExceptionInputPageSize()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> new FullTableScanParameters.Builder().setPageSize(-1).setPageThrottlingMillis(2).setDeleteThrottlingMillis(1).build());
    }

    @Test(groups = "functest", enabled = true)
    public void InvalidArgumentExceptionInputPageThrottlingMillis()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> new FullTableScanParameters.Builder().setPageSize(1).setPageThrottlingMillis(0).setDeleteThrottlingMillis(1).build());
    }

    @Test(groups = "functest", enabled = true)
    public void InvalidArgumentExceptionInputDeleteThrottlingMillis()
    {
        assertThrows(IllegalArgumentException.class,
                     () -> new FullTableScanParameters.Builder().setPageSize(1).setPageThrottlingMillis(1).setDeleteThrottlingMillis(0).build());
    }

    @Test(groups = "functest", enabled = true)
    public void checkEqualsObjects()
    {
        final var fullTableScanBuilder = new FullTableScanParameters.Builder().setPageSize(1).setPageThrottlingMillis(1).setDeleteThrottlingMillis(1);

        final var fullTableScanParameters1 = fullTableScanBuilder.build();
        final var fullTableScanParameters2 = fullTableScanParameters1;

        assertEquals(fullTableScanParameters1, fullTableScanParameters2, "Expected equal objects");
    }

    @Test(groups = "functest", enabled = true)
    public void checkClassObject()
    {
        final var fullTableScanParameters = new FullTableScanParameters.Builder().setPageSize(1)
                                                                                 .setPageThrottlingMillis(1)
                                                                                 .setDeleteThrottlingMillis(1)
                                                                                 .build();

        final var otherClass = 1;

        assertNotEquals(fullTableScanParameters, otherClass, "Expected non-equal objects");
    }

    @Test(groups = "functest", enabled = true)
    public void checkObjectEachValues()
    {
        final var fullTableScanParameters1 = new FullTableScanParameters.Builder().setPageSize(1)
                                                                                  .setPageThrottlingMillis(1)
                                                                                  .setDeleteThrottlingMillis(3)
                                                                                  .build();

        final var fullTableScanParameters2 = new FullTableScanParameters.Builder().setPageSize(2)
                                                                                  .setPageThrottlingMillis(2)
                                                                                  .setDeleteThrottlingMillis(3)
                                                                                  .build();

        assertNotEquals(fullTableScanParameters1, fullTableScanParameters2, "Expected non-equal objects");
    }

}
