
package com.ericsson.sc.glue;

import java.util.List;

import com.ericsson.sc.nfm.model.NfStatus;

/**
 * 
 */

public interface IfDiscoveredScpInstance extends IfTypedScpInstance
{
    List<String> getServedNfSetId();

    <T extends IfTypedScpDomainInfo> List<T> getDiscoveredScpDomainInfo();

}
