/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Oct 19, 2021
 *     Author: emldpng
 */

package com.ericsson.esc.bsf.load.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import io.reactivex.Flowable;

/**
 * A storage for bindingIds.
 */
public class BindingStorage
{
    private Map<String, List<String>> bindings = new ConcurrentHashMap<>();

    /**
     * Create a storage.
     * 
     * @param storageId The id of the bindingId storage.
     */
    public void create(String storageId)
    {
        bindings.put(storageId, Collections.synchronizedList(new LinkedList<>()));
    }

    /**
     * Delete a storage.
     * 
     * @param storageId The id of the storage to be deleted.
     */
    public void delete(String storageId)
    {
        bindings.remove(storageId);
    }

    /**
     * Stores a bindingId in a storage.
     * 
     * @param storageId The storage id.
     * @param bindingId The bindingId to be stored.
     */
    public synchronized void store(String storageId,
                                   String bindingId)
    {
        bindings.get(storageId).add(bindingId);
    }

    /**
     * Returns all stored bindingIds of a storage.
     * 
     * @param storageId The storage to be returned.
     * @return A Flowable of bindingIds.
     */
    public synchronized Flowable<String> get(String storageId)
    {
        return Flowable.fromIterable(bindings.get(storageId));
    }

    /**
     * Returns the number of bindingIds in a storage.
     * 
     * @param storageId The storage id.
     * @return The size of the storage list.
     */
    public synchronized long getCount(String storageId)
    {
        return bindings.get(storageId).size();
    }

    /**
     * Delete all storages.
     */
    public void deleteAll()
    {
        bindings.clear();
    }
}
