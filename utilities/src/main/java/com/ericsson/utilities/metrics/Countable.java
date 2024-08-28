/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Nov 26, 2018
 *     Author: eedstl
 */

package com.ericsson.utilities.metrics;

/**
 * 
 */
public interface Countable
{
    String id();

    double get();

    void inc();

    void inc(double amount);

    void dec();

    void dec(double amount);

    void set(double value);
}
