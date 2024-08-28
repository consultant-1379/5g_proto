package com.ericsson.sc.glue;

import java.util.List;

import com.ericsson.sc.nfm.model.NfStatus;

/**
 * 
 */
public interface IfStaticScpInstance extends IfTypedScpInstance, IfTypedNfInstance
{
    List<String> getServedNfSetId();

    <T extends IfTypedNfService> List<T> getStaticNfService();

    <T extends IfTypedScpDomainInfo> List<T> getStaticScpDomainInfo();

    @Override
    default NfStatus getNfStatus()
    {
        return IfTypedScpInstance.super.getNfStatus();

    }
}
