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
 * Created on: Jun 3, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulation of a Reverse Distinguished Name (RDN). An RDN is a
 * comma-separated sequence of RDNs. An RDN is a tuple of attribute name and
 * attribute value. The order of the sequence of RDNs is most-specific-first.
 * 
 * Example:
 * nrf=nrf_1,nrf-group=group_1,nf-instance=instance_1,nf-type=ericsson-bsf
 */

public class Rdn
{
//    public static void main(String[] args)
//    {
//        Rdn group = new Rdn("nf", "scp-function").add("nf-instance", "instance_1").add("nrf-group", "group_1");
//        System.out.println("last=" + group.last().toString(true));
//        group.values(false, null, "nrf-group").forEach(v -> System.out.println(v));
//        System.out.println("group=" + group.values(false, null, "nrf-group").get(0));
//        System.out.println("group.value()=" + group.value());
//
//        Rdn rdn1 = new Rdn("a", "0").add("b", "1").add("c", "2");
//        Rdn rdn2 = new Rdn("d", "3").add("e", "4");
//        System.out.println("rdn1=" + rdn1.toString(false));
//        System.out.println("rdn2=" + rdn2.toString(false));
//        System.out.println("rdn1+rdn2=" + rdn1.add(rdn2).toString(false));
//        System.out.println("rdn1=" + rdn1.toString(false));
//        System.out.println("rdn2=" + rdn2.toString(false));
//
//        rdn2.last().values(false, null, "b").forEach(v -> System.out.println(v));
////        rdn2.values(false, "d", "b").forEach(v -> System.out.println(v));
////        rdn2.values(false).forEach(v -> System.out.println(v));
////        rdn2.values(false, null, null).forEach(v -> System.out.println(v));
////        rdn2.values(false, "d", "b").forEach(v -> System.out.println(v));
//
//        System.out.println("rdn3=" + Rdn.fromString("a=va,b=vb,c=vc").toString(false));
//    }

    /**
     * Example: Rdn.fromString("a=va,b=vb,c=vc")
     * 
     * @param dn The distinguished name to be converted.
     * @return The RDN representing the distinguished name passed in dn.
     */
    public static Rdn fromString(final String dn)
    {
        Rdn result = null;

        for (String rdn : dn.split(","))
        {
            final String[] tokens = rdn.split("=");

            if (result == null)
                result = new Rdn(tokens[0], tokens[1]);
            else
                result = result.add(tokens[0], tokens[1]);
        }

        return result;
    }

    private final String name;
    private final String value;
    private final String rdn;

    private Rdn parent;

    public Rdn(final String attrName,
               final String attrValue)
    {
        this.name = attrName;
        this.value = attrValue;
        this.rdn = new StringBuilder().append(attrName).append("=").append(attrValue).toString();
        this.parent = null;
    }

    /**
     * Add the RDN passed as a child to this RDN and returns the child.
     * 
     * @param rdn The RDN to be added as a child to this RDN.
     * @return The RDN passed.
     */
    public Rdn add(final Rdn rdn)
    {
        rdn.last().parent(this);
        return rdn;
    }

    /**
     * Add a new RDN as a child to this RDN.
     * 
     * @param attrName  The name of the new RDN.
     * @param attrValue The value of the new RDN.
     * @return The new RDN.
     */
    public Rdn add(final String attrName,
                   final String attrValue)
    {
        return this.add(new Rdn(attrName, attrValue));
    }

    /**
     * Returns The least specific part of this RDN (the root).
     * 
     * @return The root of this RDN.
     */
    public Rdn last()
    {
        if (this.parent == null)
            return this;

        return this.parent.last();
    }

    /**
     * Returns the name of this RDN.
     * 
     * @return The name of this RDN.
     */
    public String name()
    {
        return this.name;
    }

    /**
     * Returns the parent RND of this RDN.
     * 
     * @return The parent RDN of this RDN.
     */
    public Rdn parent()
    {
        return this.parent;
    }

    /**
     * Returns the tuple ("name=value") of this RDN.
     * 
     * @return The tuple ("name=value") of this RDN.
     */
    public String rdn()
    {
        return this.rdn;
    }

    /**
     * Returns a string representation of this RDN, order is child-first.
     * 
     * @return A string representation of this RDN, order is child-first.
     */
    @Override
    public String toString()
    {
        return this.toString(true);
    }

    /**
     * Returns a string representation of this RDN, order is as specified with
     * parameter childFirst.
     * 
     * @param childFirst If true, the order of the RNDs in the string is
     *                   child-first, otherwise it is parent-first.
     * 
     * @return A string representation of this RDN.
     */
    public String toString(boolean childFirst)
    {
        final StringBuilder b = new StringBuilder();

        if (childFirst)
        {
            b.append(this.rdn);

            if (this.parent != null)
                b.append(",").append(this.parent.toString(childFirst));
        }
        else
        {
            if (this.parent != null)
                b.append(this.parent.toString(childFirst)).append(",");

            b.append(this.rdn);
        }

        return b.toString();
    }

    /**
     * Returns the value RND of this RDN.
     * 
     * @return The value RDN of this RDN.
     */
    public String value()
    {
        return this.value;
    }

    public List<String> values()
    {
        return this.values(true, null, null);
    }

    public List<String> values(boolean childFirst)
    {
        return this.values(childFirst, null, null);
    }

    public List<String> values(boolean childFirst,
                               final String first,
                               final String last)
    {
        List<String> values = null;
        boolean firstFound = first == null || first.isEmpty() || first.equals(this.name);
        boolean lastFound = last == null || last.equals(this.name);

        if (this.parent != null && !lastFound)
            values = this.parent.values(false, firstFound ? null : first, last);
        else
            values = new ArrayList<>();

        if (firstFound && lastFound || !values.isEmpty())
            values.add(this.value);

        if (childFirst) // Maybe true only for first child called.
            Collections.reverse(values);

        return values;
    }

    public List<String> values(final String first,
                               final String last)
    {
        return this.values(true, first, last);
    }

    private Rdn parent(final Rdn rdn)
    {
        this.parent = rdn;
        return this;
    }
}
