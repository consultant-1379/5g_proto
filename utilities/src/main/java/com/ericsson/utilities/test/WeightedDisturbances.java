/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 14, 2020
 *     Author: eedstl
 */

package com.ericsson.utilities.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.util.concurrent.AtomicDouble;

public class WeightedDisturbances
{
    public static class Disturbance implements Comparable<Disturbance>
    {
        private enum Action
        {
            NONE("none"),
            DELAY("delay"),
            DROP("drop");

            public static Action fromString(final String s)
            {
                for (Action a : Action.values())
                    if (a.value.equals(s))
                        return a;

                throw new IllegalArgumentException("Unexpected value '" + s + "'");
            }

            final String value;

            private Action(final String action)
            {
                this.value = action;
            }

            @Override
            public String toString()
            {
                return this.value;
            }
        }

        /**
         * Creates a disturbance according to the definition passed.
         * <p>
         * Format of a disturbance definition:
         * 
         * <pre>
         *  "delay" ',' delayInMillis | "drop" [ ',' dropAnswer ] | "none"
         * </pre>
         * 
         * Example:
         * 
         * <pre>
         * "delay,300" or "drop"
         * </pre>
         * 
         * @param definition The disturbance definition.
         */
        public static Disturbance fromString(final String definition)
        {
            final String[] tokens = definition.split(",");

            if (tokens.length == 0)
                throw new IllegalArgumentException("Invalid disturbance definition '" + definition
                                                   + "'. Format: '\"delay\" ',' delayInMillis | \"drop\" [ ',' dropAnswer ] | \"none\"'");

            final Action action = Action.fromString(tokens[0]);

            if (action == Action.DELAY)
            {
                if (tokens.length != 2)
                    throw new IllegalArgumentException("Invalid disturbance definition '" + definition
                                                       + "'. Format: '\"delay\" ',' delayInMillis | \"drop\" [ ',' dropAnswer ] | \"none\"'");

                return new Disturbance(action, Integer.parseInt(tokens[1]));
            }

            if (action == Action.DROP)
                return new Disturbance(action, tokens.length == 2 ? Integer.parseInt(tokens[1]) : 1);

            return new Disturbance(action, null);
        }

        private final Action action;
        private final Integer arg;

        private Disturbance(final Action action,
                            final Integer arg)
        {
            this.action = action;
            this.arg = arg;
        }

        @Override
        public int compareTo(Disturbance o)
        {
            int result = this.action.compareTo(o.action);

            if (result == 0 && (this.isDelayAction() || this.isDropAction()))
                result = this.arg - o.arg;

            return result;
        }

        public Integer getDelayInMillis()
        {
            return this.arg;
        }

        public Integer getDropAnswer()
        {
            return this.arg;
        }

        public boolean isDelayAction()
        {
            return this.action == Action.DELAY;
        }

        public boolean isDropAction()
        {
            return this.action == Action.DROP;
        }

        @Override
        public String toString()
        {
            return new StringBuilder().append(this.action.toString()).append(this.isDelayAction() || this.isDropAction() ? "," + this.arg : "").toString();
        }
    }

    private static final int CAPACITY = 10000;

    /**
     * Creates a list of weighted disturbances according to the definition passed.
     * <p>
     * Format of a disturbance definition:
     * 
     * <pre>
     * weight ',' ( "delay" ',' delayInMillis | "drop" [ ',' "dropAnswer" ] | "none" ) (';' weight ',' ( "delay" ',' delayInMillis | "drop" [ ',' "dropAnswer" ] | "none" ) ) *
     * </pre>
     * 
     * Example:
     * 
     * <pre>
     * "90,delay,300;5,delay,500;5,drop,7"
     * </pre>
     * 
     * @param definitions The disturbance definition.
     */
    public static WeightedDisturbances fromString(final String definitions)
    {
        return fromString(definitions.split(";"));
    }

    /**
     * Creates a disturbance according to the definitions passed.
     * <p>
     * Format of a disturbance definition:
     * 
     * <pre>
     * weight ',' ( "delay" ',' delayInMillis | "drop" [ ',' dropAnswer ] | "none" )
     * </pre>
     * 
     * Example:
     * 
     * <pre>
     * ["90,delay,300", "5,delay,500", "5,drop,7"]
     * </pre>
     * 
     * @param definitions The list of disturbance definitions.
     */
    public static WeightedDisturbances fromString(final String... definitions)
    {
        final Map<Disturbance, Double> weightPerDisturbance = new TreeMap<>();

        for (String definition : definitions)
        {
            final int posComma = definition.indexOf(',');

            if (posComma < 0)
                throw new IllegalArgumentException("Invalid disturbance definition '" + definition
                                                   + "'. Format: '( weight ',' ( \"delay\" ',' delayInMillis | \"drop\" [ ',' dropAnswer ] | \"none\" ) (',' weight ',' ( \"delay\" ',' delayInMillis | \"drop\" [ ',' dropAnswer ] | \"none\" ) *'");

            weightPerDisturbance.put(Disturbance.fromString(definition.substring(posComma + 1)), Double.valueOf(definition.substring(0, posComma)));
        }

        final AtomicDouble sumOfWeights = new AtomicDouble(0d);

        weightPerDisturbance.values().stream().forEach(sumOfWeights::addAndGet);

        final List<Disturbance> distributionOfDisturbances = new ArrayList<>(CAPACITY);
        final AtomicInteger imax = new AtomicInteger(CAPACITY);

        weightPerDisturbance.entrySet().forEach(e ->
        {
            final long quota = Math.round(CAPACITY * e.getValue() / sumOfWeights.get());

            for (int i = 0; imax.get() > 0 && i < quota; imax.decrementAndGet(), i++)
                distributionOfDisturbances.add(e.getKey());
        });

        return new WeightedDisturbances(distributionOfDisturbances);
    }

//    public static void main(String[] args)
//    {
////        WeightedDisturbances wd = WeightedDisturbances.fromString("1,none");
//        WeightedDisturbances wd = WeightedDisturbances.fromString("100,delay,300", "100,delay,500", "50,drop");
////      WeightedDisturbances wd = WeightedDisturbances.fromString("88.57,delay,300", "7.44,delay,500", "3.99,drop");
////        WeightedDisturbances wd = WeightedDisturbances.fromString("88.57,delay,300;7.44,delay,500;3.99,drop");
//        System.out.println(wd.toString());
//
//        List<Disturbance> l = new ArrayList<>();
//
//        for (int i = 0; i < 1000000; i++)
//            l.add(wd.next());
//
//        int numDelay300 = 0;
//        int numDelay500 = 0;
//        int numDrop = 0;
//        int numNone = 0;
//
//        for (Disturbance d : l)
//        {
//            if (d.isDelayAction())
//            {
//                if (d.getDelayInMillis() == 300)
//                    numDelay300++;
//                else if (d.getDelayInMillis() == 500)
//                    numDelay500++;
//            }
//            else if (d.isDropAction())
//            {
//                numDrop++;
//            }
//            else
//            {
//                numNone++;
//            }
//        }
//
//        System.out.println("delay300=" + numDelay300);
//        System.out.println("delay500=" + numDelay500);
//        System.out.println("drop=" + numDrop);
//        System.out.println("none=" + numNone);
//    }

    private final List<Disturbance> distributionOfDisturbances;
    private final Random random;

    private WeightedDisturbances(List<Disturbance> distributionOfDisturbances)
    {
        this.distributionOfDisturbances = distributionOfDisturbances;
        this.random = new Random(System.currentTimeMillis());
    }

    public Disturbance next()
    {
        return this.distributionOfDisturbances.get(this.random.nextInt(this.distributionOfDisturbances.size()));
    }

    @Override
    public String toString()
    {
        final StringBuilder b = new StringBuilder();

        final Map<Disturbance, Long> numPerDisturbance = new TreeMap<>();

        this.distributionOfDisturbances.forEach(d -> numPerDisturbance.compute(d,
                                                                               (k,
                                                                                v) -> (v == null) ? 1 : v + 1));

        b.append("[");
        numPerDisturbance.entrySet()
                         .forEach(e -> b.append(b.length() > 1 ? "," : "")
                                        .append("\"")
                                        .append(100d * e.getValue() / CAPACITY)
                                        .append(",")
                                        .append(e.getKey().toString())
                                        .append("\""));
        b.append("]");

        return b.toString();
    }
}
