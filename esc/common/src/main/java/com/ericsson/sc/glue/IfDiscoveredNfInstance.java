package com.ericsson.sc.glue;

import java.util.List;

/**
 * 
 */
public interface IfDiscoveredNfInstance extends IfTypedNfInstance
{
    <T extends IfTypedNfService> List<T> getDiscoveredNfService();
}
