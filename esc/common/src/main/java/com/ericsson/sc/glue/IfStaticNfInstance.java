package com.ericsson.sc.glue;

import java.util.List;

/**
 * 
 */
public interface IfStaticNfInstance extends IfTypedNfInstance
{
    <T extends IfTypedNfService> List<T> getStaticNfService();
}
