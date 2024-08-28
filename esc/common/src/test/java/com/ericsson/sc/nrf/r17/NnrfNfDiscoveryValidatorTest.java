package com.ericsson.sc.nrf.r17;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class NnrfNfDiscoveryValidatorTest
{
    @Test
    void testValidationOfFqdn() throws IOException
    {
        final List<String> positives = List.of("eric-chfsim-1", "eric-chfsim-1.5g-bsf-eedstl", "eric-chfsim-1.5g-bsf-eedstl.svc.cluster.local", "127.0.0.1");
        final List<String> negatives = List.of("eric_chfsim-1",
                                               "eric-chfsim-1.5g_bsf-eedstl",
                                               "eric-chfsim-1.5g-bsf-eedstl.svc.cluster.l_ocal",
                                               "::",
                                               "[::]",
                                               "::1",
                                               "[::1]");

        for (int i = 0; i < positives.size(); ++i)
            Assertions.assertTrue(NnrfNfDiscoveryValidator.patternFqdn.matcher(positives.get(i)).matches(), positives.get(i));

        for (int i = 0; i < negatives.size(); ++i)
            Assertions.assertFalse(NnrfNfDiscoveryValidator.patternFqdn.matcher(negatives.get(i)).matches(), negatives.get(i));
    }
}
