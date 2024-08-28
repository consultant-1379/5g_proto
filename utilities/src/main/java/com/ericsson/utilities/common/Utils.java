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
 * Created on: May 14, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.json.Json;

import io.reactivex.functions.BiPredicate;
import io.reactivex.functions.Predicate;

public class Utils
{
    private static final String ENV_IP_FAMILY = "IP_FAMILY";

    private static final Logger log = LoggerFactory.getLogger(Utils.class);

    /**
     * Converts Double to Integer. If parameter rhs is null the result is also null.
     * 
     * @param rhs The value to be converted, may also be null.
     * @return The Integer representation of rhs or null if rhs is null.
     */
    public static Integer convert(final Double rhs)
    {
        return rhs != null ? rhs.intValue() : null;
    }

    /**
     * Searches the list passed for the item named as passed in name and returns the
     * item found (or null).
     * 
     * @param list The list to search in.
     * @param name The name the item to lookup in list.
     * @return The item found or null if not found.
     */
    public static <T extends IfNamedListItem> T getByName(final List<T> list,
                                                          final String name)
    {
        final Iterator<T> iterator = list.iterator();

        while (iterator.hasNext())
        {
            final T item = iterator.next();

            if (item.getName().equals(name))
            {
                return item;
            }
        }

        return null;
    }

    public static <T extends IfNamedListItem> T getByName(final Set<T> list,
                                                          final String name)
    {
        final Iterator<T> iterator = list.iterator();

        while (iterator.hasNext())
        {
            final T item = iterator.next();

            if (item.getName().equals(name))
            {
                return item;
            }
        }

        return null;
    }

    /**
     * Returns a subset of the list provided by list, containing items whose names
     * are included in the list names, or an empty list if no occurrences found.
     * 
     * @param list The list to search in.
     * @param name The list of names of the items to lookup in list.
     * @return A list of found items.
     */
    public static <T extends IfNamedListItem> List<T> getListByNames(final List<T> list,
                                                                     final List<String> names)
    {
        final List<T> subset = new ArrayList<>();
        final Iterator<T> iterator = list.iterator();

        while (iterator.hasNext())
        {
            final T item = iterator.next();

            if (names.contains(item.getName()))
                subset.add(item);
        }

        return subset;
    }

    /**
     * Creates and returns a comparator that calculates the difference of two JSON
     * objects {@code prev} and {@code curr} and filters the result according to the
     * {@code selector} passed.
     * <p>
     * For use with e.g. {@code Obserable.distinctUntilChanged(comparator)}.
     * 
     * @param <T>      The type of the JSON objects to compare.
     * @param purpose  Used for logging only.
     * @param selector The selector applied on the calculated difference of JSON
     *                 objects {@code prev} and {@code curr}.
     * @return {@code true} on no change detected, {@code false} otherwise.
     */
    public static <T> BiPredicate<Optional<T>, Optional<T>> getComparator(final String purpose,
                                                                          final Predicate<? super List<Json.Patch>> selector)
    {
        return (prev,
                curr) ->
        {
            boolean changeDetected = prev != curr;

            if (changeDetected)
            {
                List<Json.Patch> patches = new ArrayList<>();

                if (prev.isPresent() && curr.isPresent())
                {
                    try
                    {
                        patches = Json.diff(prev.get(), curr.get());
                        changeDetected = !patches.isEmpty() && selector.test(patches);
                    }
                    catch (IOException e)
                    {
                        log.error("{}: error comparing JSON objects. Cause: {}", purpose, e);
                    }
                }
                else if (prev.isEmpty() && curr.isEmpty())
                {
                    changeDetected = false;
                }

                if (changeDetected)
                {
                    log.debug("{}: changeDetected={}, patches.size={}, patches={}",
                              purpose,
                              changeDetected,
                              patches.size(),
                              patches.size() > 100 ? "Won't print more than 100 patches." : patches);
                }
            }

            return !changeDetected;
        };
    }

    /**
     * Check lists passed for equality. Return {@code true} if they are equal,
     * otherwise {@code false}.
     * 
     * Lists are semantically checked, i.e. the order of their elements does not
     * matter.
     * 
     * @param lhs The left-hand-side of the equation lhs == rhs.
     * @param rhs The right-hand-side of the equation lhs == rhs.
     * @return {@code true} if both lists are equal, otherwise {@code false}.
     */
    public static <T> boolean isEqual(final List<T> lhs,
                                      final List<T> rhs)
    {
        if (lhs == rhs)
            return true;

        if (lhs == null || rhs == null)
            return false;

        if (lhs.size() == rhs.size())
        {
            if (lhs.isEmpty())
                return true;

            Set<T> l = new HashSet<>();
            Set<T> r = new HashSet<>();

            l.addAll(lhs);
            r.addAll(rhs);

            return l.containsAll(r);
        }

        return false;
    }

    /**
     * Gets as an input a list reference and returns a stream if the reference is
     * not null, otherwise it returns an empty stream
     * 
     * @param <T>
     * @param list
     * @return
     */
    public static <T> Stream<T> streamIfExists(final List<T> list)
    {
        if (Objects.nonNull(list))
        {
            return list.stream();
        }
        return Stream.<T>empty();
    }

    /**
     * Gets as an input a set reference and returns a stream if the reference is not
     * null, otherwise it returns an empty stream
     * 
     * @param <T>
     * @param set
     * @return
     */
    public static <T> Stream<T> streamIfExists(final Set<T> set)
    {
        if (Objects.nonNull(set))
        {
            return set.stream();
        }
        return Stream.<T>empty();
    }

    /**
     * Determines the local address of the local host (IPv4 or IPv6) and returns it.
     * Depends on environment variable IP_FAMILY (can have values "ipv4" or "ipv6")
     * which should be injected by helm charts.
     * 
     * @return The determined local address of the host
     * @throws UnknownHostException
     */
    public static String getLocalAddress() throws UnknownHostException
    {
        final String localAddress;
        final String ipFamily = EnvVars.get(ENV_IP_FAMILY, "");

        if (ipFamily.equalsIgnoreCase("ipv6"))
        {
            localAddress = Arrays.stream(InetAddress.getAllByName(InetAddress.getLocalHost().getHostName()))
                                 .filter(Inet6Address.class::isInstance)
                                 .findAny()
                                 .orElseThrow(() -> new UnknownHostException("IP_FAMILY was set to " + ipFamily + " but cluster does not support this"))
                                 .getHostAddress();
        }
        else if (ipFamily.equalsIgnoreCase("ipv4"))
        {
            localAddress = Arrays.stream(InetAddress.getAllByName(InetAddress.getLocalHost().getHostName()))
                                 .filter(Inet4Address.class::isInstance)
                                 .findAny()
                                 .orElseThrow(() -> new UnknownHostException("IP_FAMILY was set to " + ipFamily + " but cluster does not support this"))
                                 .getHostAddress();
        }
        else
        {
            localAddress = InetAddress.getLocalHost().getHostAddress();
        }

        log.info("localAddress={}", localAddress);

        return localAddress;
    }

    private Utils()
    {
    }
}
