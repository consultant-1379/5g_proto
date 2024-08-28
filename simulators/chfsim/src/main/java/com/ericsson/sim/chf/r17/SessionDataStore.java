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
 * Created on: Feb 8, 2019
 *     Author: eedala, xkonska
 */

package com.ericsson.sim.chf.r17;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage session data, that is chargingDataRefs, which are session-IDs between
 * the CHF and its clients. An instance of this class maintains a range of
 * numbers that can be specified in the constructor. This class:
 * <ul>
 * <li>Returns a currently unused chargingDataRef and marks it as in use
 * <li>Checks if a chargingDataRef supplied by a client is in use
 * <li>Frees up a chargingDataRef after a session with the client is terminated.
 * <li>Frees up all chargingDataRefs for testing purposes
 * </ul>
 */
public class SessionDataStore
{
    private final Integer startVal;
    private final Integer numRefs;
    private final String prefix;
    private Refs refs;
    private static final Logger log = LoggerFactory.getLogger(SessionDataStore.class);

    /**
     * Constructors
     * 
     * @param int startVal The first chargingDataRef in the range. Must be > 0.
     * @param int numRefs The number of chargingDataRefs available
     * @param int prefix The prefix of chargingDataRefs
     */
    public SessionDataStore(final Integer startVal,
                            final Integer numRefs,
                            final String prefix)
    {
        // Ensure that the startVal is > 0:
        if (startVal <= 0)
        {
            this.startVal = 1;
            this.numRefs = numRefs + 1;
        }
        else
        {
            this.startVal = startVal;
            this.numRefs = numRefs;
        }

        this.prefix = prefix;

        // Initialize all references
        releaseAllRefs();
    }

    /**
     * Return a currently unused chargingDataRef and mark it as in use
     * 
     * @return string chargingDataRef If the return value is "", then no free
     *         reference was found
     */
    public String getRef()
    {
        return this.refs.seize().map(refNumber -> this.prefix + refNumber).orElse("ERROR: SessionDataStore ran out of IDs");
    }

    /**
     * Check if a chargingDataRef is valid/in use
     * 
     * @param String chargingDataRef
     * @return boolean isValid true if in use
     */
    public boolean isValid(final String chargingDataRef)
    {
        try
        {
            final String[] resCode = chargingDataRef.split("-");

            if (resCode.length < 2)
            {
                log.error("Invalid chargingDataRef: '{}'. It must have the format '.+[-][0-9]+'.", chargingDataRef);
                return false;
            }

            return this.refs.isInUse(Integer.parseInt(resCode[1]));
        }
        catch (NumberFormatException e)
        {
            log.error("Invalid chargingDataRef: '{}'. It must have the format '.+[-][0-9]+'.", chargingDataRef);
            return false;
        }
    }

    /**
     * Release a chargingDataRef and move it from the allocated list to the free
     * list
     * 
     * @param int chargingDataRef
     * @return boolean success true if the chargingDataRef was found, false
     *         otherwise
     */
    public boolean releaseRef(final String chargingDataRef)
    {
        try
        {
            final String[] resCode = chargingDataRef.split("-");

            if (resCode.length < 2)
            {
                log.error("Invalid chargingDataRef: '{}'. It must have the format '.+[-][0-9]+'.", chargingDataRef);
                return false;
            }

            return this.refs.release(Integer.parseInt(resCode[1]));
        }
        catch (NumberFormatException e)
        {
            log.error("Invalid chargingDataRef: '{}'. It must have the format '.+[-][0-9]+'.", chargingDataRef);
            return false;
        }

    }

    /**
     * Release all chargingDatarefs. This is typically used by the constructor, and
     * by the admin interface during testing.
     * 
     */
    public final void releaseAllRefs()
    {
        this.refs = new Refs();
    }

    /**
     * returns a String representation of all chargingDataRefs and their notifyUris
     * (if not null) for debugging
     */
    public String getAllRefs()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());
        sb.append("----All allocated chargingDataRefs:");
        sb.append(System.lineSeparator());

        if (this.refs.getAllocated().size() < 1)
        {
            sb.append("No chargingDataRefs allocated.").append(System.lineSeparator());
        }
        else
        {
            for (var entry : this.refs.getAllocated())
            {
                sb.append("chargingDataRef = ").append(entry).append(System.lineSeparator());
            }
        }
        sb.append("-------------------").append(System.lineSeparator());
        return sb.toString();
    }

    /**
     * log all chargingDataRefs and their notifyUris (if not null) for debugging
     */
    public void logAllRefs()
    {
        log.info(getAllRefs());
    }

    private class Refs
    {
        private final ConcurrentLinkedQueue<Integer> freeRefs = new ConcurrentLinkedQueue<>();
        private final Set<Integer> allocatedRefs = ConcurrentHashMap.<Integer>newKeySet();

        public Refs()
        {
            for (var chargingDataRef = SessionDataStore.this.startVal; chargingDataRef < SessionDataStore.this.startVal + SessionDataStore.this.numRefs;
                 chargingDataRef++)
            {
                this.freeRefs.add(chargingDataRef);
            }
        }

        public Optional<Integer> seize()
        {
            try
            {
                // Move a charging data reference from the free to the allocated list
                var chargingDataRef = freeRefs.remove();
                // Set the notifyUri:
                allocatedRefs.add(chargingDataRef);
                return Optional.of(chargingDataRef);
            }
            catch (NoSuchElementException e)
            {
                return Optional.empty();
            }
        }

        public boolean isInUse(Integer refNumber)
        {
            return allocatedRefs.contains(refNumber);
        }

        public boolean release(Integer refNumber)
        {
            if (allocatedRefs.remove(refNumber))
            {
                freeRefs.add(refNumber);
                return true;
            }
            else
            {
                return false;
            }
        }

        public Set<Integer> getAllocated()
        {
            return Collections.unmodifiableSet(this.allocatedRefs);
        }
    }
}
