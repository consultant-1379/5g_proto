/**
 * COPYRIGHT ERICSSON GMBH 2022
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 15, 2022
 *     Author: eaoknkr
 */

package com.ericsson.utilities.common;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.UniqueValueMap.UniqueValueMapBuilder;

/**
 * 
 */
class UniqueValueMapTest
{
    private static final Logger log = LoggerFactory.getLogger(UniqueValueMapTest.class);

    @Test
    void test_simple() throws IOException
    {
        var map = new UniqueValueMapBuilder<Integer, HashSet<String>, String>(HashSet::new).keepSmaller().create();

        map.put(1, "value1");
        map.put(2, "value2");
        map.put(120, "value4");

        var result = new TreeMap<>();
        result.put(1, Set.of("value1"));
        result.put(2, Set.of("value2"));
        result.put(120, Set.of("value4"));

        Assertions.assertEquals(result, map);
    }

    @Test
    void test_simple2() throws IOException
    {
        var map = new UniqueValueMapBuilder<Integer, HashSet<String>, String>(HashSet::new).keepSmaller().create();

        map.put(1, "value1");
        map.put(2, "value2");
        map.put(120, "value4");
        map.put(2, "value3");
        map.put(1, "value100");
        map.put(2, "value5");

        var result = new TreeMap<>();
        result.put(1, Set.of("value1", "value100"));
        result.put(2, Set.of("value2", "value3", "value5"));
        result.put(120, Set.of("value4"));

        Assertions.assertEquals(result, map);
    }

    @Test
    void test_simple3() throws IOException
    {
        var map = new UniqueValueMapBuilder<Integer, HashSet<String>, String>(HashSet::new).keepSmaller().create();

        map.put(1, "value1");
        map.put(2, "value2");
        map.put(120, "value4");
        map.put(2, "value3");
        map.put(1, "value4");
        map.put(2, "value4");

        var result = new TreeMap<>();
        result.put(1, Set.of("value1", "value4"));
        result.put(2, Set.of("value2", "value3"));

        Assertions.assertEquals(result, map);
    }

    @Test
    void test_simple4() throws IOException
    {
        var map = new UniqueValueMapBuilder<Integer, HashSet<String>, String>(HashSet::new).keepSmaller().create();

        map.put(1, "value1");
        map.put(2, "value2");
        map.put(2, "value3");
        map.put(1, "value4");
        map.put(2, "value4");

        map.put(120, "value4");
        map.put(110, "value4");
        map.put(10, "value4");

        map.put(0, "value3");
        map.put(120, "value3");
        map.put(120, "value12");

        var result = new TreeMap<>();
        result.put(0, Set.of("value3"));
        result.put(1, Set.of("value1", "value4"));
        result.put(2, Set.of("value2"));
        result.put(120, Set.of("value12"));

        Assertions.assertEquals(result, map);
    }

    @Test
    void test_simple5() throws IOException
    {
        var uvmap = new UniqueValueMapBuilder<Integer, HashSet<String>, String>(HashSet::new).keepSmaller().create();

        var map = new TreeMap<Integer, List<String>>();
        map.put(1, List.of("value1", "value4"));
        map.put(2, List.of("value2", "value3", "value4"));

        map.put(120, List.of("value4", "value3", "value12"));
        map.put(110, List.of("value4"));
        map.put(10, List.of("value4"));

        map.put(0, List.of("value3"));

        uvmap.putAllItems(map);

        var result = new TreeMap<>();
        result.put(0, Set.of("value3"));
        result.put(1, Set.of("value1", "value4"));
        result.put(2, Set.of("value2"));
        result.put(120, Set.of("value12"));

        Assertions.assertEquals(result, uvmap);
    }

    @Test
    void test_negative() throws IOException
    {
        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> new UniqueValueMapBuilder<Integer, HashSet<String>, String>(HashSet::new).keepSmaller().keepLarger().create());
    }
}
