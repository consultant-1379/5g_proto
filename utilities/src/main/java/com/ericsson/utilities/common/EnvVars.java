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
 * Created on: Jan 15, 2021
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class encapsulates the retrieval of environment variables. Added
 * value is validity checking of variable values obtained.
 */
public class EnvVars
{
    private static final Logger log = LoggerFactory.getLogger(EnvVars.class);

    /**
     * Default validator doing some sanity checks.
     * <li>The maximum length of a value is limited to 511 characters.
     */
    private static Predicate<String> validator = value -> value.length() < 512;

    /**
     * Calling {@link #get(String,Function)} with the default {@link #validator}.
     * 
     * @param name The name of the environment variable to retrieve.
     * @return The validated value or {@code null}.
     */
    public static String get(final String name)
    {
        return get(name, null, validator);
    }

    /**
     * Calling {@link #get(String,String,Function)} with the {@code defaultValue}
     * and the default {@link #validator}.
     * <p>
     * If the value retrieved is invalid or not present the default value is
     * returned.
     * 
     * @param name         The name of the environment variable to retrieve.
     * @param defaultValue The value to be returned in case the value retrieved is
     *                     invalid or not present.
     * @return The validated value or the {@code defaultValue}.
     */
    public static String get(final String name,
                             final String defaultValue)
    {
        return get(name, defaultValue, validator);
    }

    public static <T> String get(final String name,
                                 final T defaultValue)
    {
        return get(name, defaultValue, validator);
    }

    /**
     * Calling {@link #get(String,String,Function)} with the {@code validator}.
     * <p>
     * If the value retrieved is invalid {@code null} is returned.
     * 
     * @param name      The name of the environment variable to retrieve.
     * @param validator The validation function used for validation.
     * @return The validated value or {@code null}.
     */
    public static String get(final String name,
                             final Predicate<String> validator)
    {
        return get(name, null, validator);
    }

    /**
     * Retrieves the requested variable value from the environment and validates it.
     * <p>
     * If the value retrieved is invalid or not present the default value is
     * returned.
     * 
     * @param name         The name of the environment variable to retrieve.
     * @param defaultValue The value to be returned in case the value retrieved is
     *                     invalid or not present.
     * @param validator    The validation function used for validation.
     * @return The validated value or the {@code defaultValue}.
     */
    public static <T> String get(final String name,
                                 final T defaultValue,
                                 final Predicate<String> validator)
    {
        final String value = System.getenv(name);

        if (value == null)
            return defaultValue == null ? null : defaultValue.toString();

        if (!validator.test(value))
        {
            log.error("Value of environment variable '{}' is invalid: '{}', returning default value '{}'.", name, value, defaultValue);
            return defaultValue == null ? null : defaultValue.toString();
        }

        return value;
    }

    /**
     * Users shall not instantiate this utility class.
     */
    private EnvVars()
    {
    }
}
