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
 * Created on: Dec 13, 2018
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

/**
 * Wrapper for wrapping java.lang types that would otherwise be passed by value
 * when passed as argument to a method. Example is java.lang.String: If pass by
 * reference of a java.lang.String is needed, wrap it first before passing it to
 * the method.
 */
public class Wrapper<T>
{
    private T source;

    public T get()
    {
        return this.source;
    }
}