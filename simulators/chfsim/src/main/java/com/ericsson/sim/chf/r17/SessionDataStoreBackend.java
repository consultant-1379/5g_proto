/**
 * COPYRIGHT ERICSSON GMBH 2019
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: 7 May 2019
 *     Author: enubars
 */

package com.ericsson.sim.chf.r17;

import java.util.HashMap;

/**
 * Backend, which stores all chargingDataRef
 */
class SessionDataStoreBackend
{

    private static HashMap<String, String> allocatedRefsBackendMap;

    /**
     * Constructor
     */
    public SessionDataStoreBackend()
    {

        allocatedRefsBackendMap = new HashMap<String, String>();
    }

    /**
     * Checks if chargingDataRef is valid
     * 
     * @param chargingDataRef
     * @return true, if valid otherwise false
     */
    public boolean isValid(String chargingDataRef)
    {

        if (allocatedRefsBackendMap.containsKey(chargingDataRef))
        {
            return true;
        }
        // If we make it here, the chargingDataRef was not found
        return false;
    }

    /**
     * Given a chargingDataRef, return the notifyUri for it
     * 
     * @param chargingDataRef
     * @return NotifyUri
     */
    public String getNotifyUri(String chargingDataRef)
    {

        return allocatedRefsBackendMap.get(chargingDataRef);
    }

    /**
     * adds a new notifyUri to the Backend
     * 
     * @param chargingDataRef
     * @param notifyUri,      which will be added
     */
    public void addRef(String chargingDataRef,
                       String notifyUri)
    {
        allocatedRefsBackendMap.put(chargingDataRef, notifyUri);
    }

    /**
     * Deletes a chargingDataRef
     * 
     * @param chargingDataRef
     * @return true, if it was successful
     */
    public boolean deleteRef(String chargingDataRef)
    {

        if (allocatedRefsBackendMap.containsKey(chargingDataRef))
        {
            // Found!

            // remove it from the backend list:
            allocatedRefsBackendMap.remove(chargingDataRef);
            return true;
        }

        // If we make it here, the chargingDataRef was not found
        return false;
    }

    /**
     * update the charingDataRef with a new notifyUri
     * 
     * @param chargingDataRef
     * @param notifyUri
     * @return true, if it was successful
     */
    public boolean updateURI(String chargingDataRef,
                             String notifyUri)
    {
        if (notifyUri != null)
        {

            allocatedRefsBackendMap.put(chargingDataRef, notifyUri);

            return true;
        }
        else
        {
            return false;
        }
    }

}
