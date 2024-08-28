package com.ericsson.esc.bsf.services.cm;

import com.ericsson.esc.bsf.services.cm.adp.CMProviderADPImpl;

public class CMProviderFactory {
    private static CMProvider cmProvider = null;

    public static CMProvider getCMProvider() {
        if (cmProvider == null)
            cmProvider = new CMProviderADPImpl();

        return cmProvider;
    }
}
