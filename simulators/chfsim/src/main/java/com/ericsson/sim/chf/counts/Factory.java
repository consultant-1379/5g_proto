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
 * Created on: Jul 2, 2019
 *     Author: eedstl
 */

package com.ericsson.sim.chf.counts;

/**
 * Interface for a Factory of Pool-items.
 * 
 * @param <T> The type of the Pool-item to be created.
 */
public interface Factory<T>
{
    /**
     * 
     * @param id The ID to be used as a key in a Pool of Pool-items.
     * @return A newly created Pool-item.
     */
    T create(String id);
}
