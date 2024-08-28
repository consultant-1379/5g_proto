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
 * Created on: Jan 21, 2019
 *     Author: eedstl
 */

package com.ericsson.utilities.json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.openapitools.jackson.nullable.JsonNullableModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utility class for JSON support.
 */
public class Json
{
    @JsonPropertyOrder({ Patch.JSON_PROPERTY_OP, Patch.JSON_PROPERTY_PATH, Patch.JSON_PROPERTY_FROM, Patch.JSON_PROPERTY_VALUE })
    public static class Patch
    {
        public enum Operation
        {
            ADD("add"),
            COPY("copy"),
            MOVE("move"),
            REMOVE("remove"),
            REPLACE("replace"),
            TEST("test");

            @JsonCreator
            public static Operation fromValue(String value)
            {
                for (Operation b : Operation.values())
                {
                    if (b.value.equals(value))
                    {
                        return b;
                    }
                }

                throw new IllegalArgumentException("Unexpected value '" + value + "'");
            }

            private String value;

            Operation(String value)
            {
                this.value = value;
            }

            @JsonValue
            public String getValue()
            {
                return this.value;
            }

            @Override
            public String toString()
            {
                return String.valueOf(this.value);
            }
        }

        private static final ObjectMapper json = Jackson.newOm(); // create once, reuse

        public static final String JSON_PROPERTY_OP = "op";
        public static final String JSON_PROPERTY_PATH = "path";
        public static final String JSON_PROPERTY_FROM = "from";
        public static final String JSON_PROPERTY_VALUE = "value";

        public static Patch of()
        {
            return new Patch();
        }

        private Operation op;
        private String path;
        private String from;
        private Object value;

        private Patch()
        {
            this.op = null;
            this.path = null;
            this.from = null;
            this.value = null;
        }

        @Override
        public boolean equals(final java.lang.Object o)
        {
            if (this == o)
                return true;

            if (o == null || getClass() != o.getClass())
                return false;

            final Patch patchItem = (Patch) o;
            return Objects.equals(this.op, patchItem.op) && Objects.equals(this.path, patchItem.path) && Objects.equals(this.from, patchItem.from)
                   && Objects.equals(this.value, patchItem.value);
        }

        public Patch from(String from)
        {
            this.from = from;
            return this;
        }

        @javax.annotation.Nullable
        @JsonProperty(JSON_PROPERTY_FROM)
        @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
        public String getFrom()
        {
            return from;
        }

        @JsonProperty(JSON_PROPERTY_OP)
        @JsonInclude(value = JsonInclude.Include.ALWAYS)
        public Operation getOp()
        {
            return op;
        }

        @JsonProperty(JSON_PROPERTY_PATH)
        @JsonInclude(value = JsonInclude.Include.ALWAYS)
        public String getPath()
        {
            return path;
        }

        @javax.annotation.Nullable
        @JsonProperty(JSON_PROPERTY_VALUE)
        @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
        public Object getValue()
        {
            return value;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(op, path, from, value);
        }

        public Patch op(Operation op)
        {
            this.op = op;
            return this;
        }

        public Patch path(String path)
        {
            this.path = path;
            return this;
        }

        public void setFrom(String from)
        {
            this.from = from;
        }

        public void setOp(Operation op)
        {
            this.op = op;
        }

        public void setPath(String path)
        {
            this.path = path;
        }

        public void setValue(Object value)
        {
            this.value = value;
        }

        @Override
        public String toString()
        {
            try
            {
                return json.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                return e.toString();
            }
        }

        public Patch value(Object value)
        {
            this.value = value;
            return this;
        }
    }

    private static class Diff
    {
        private static final Set<Class<?>> JSON_PRIMITIVES = Set.of(Integer.class, Long.class, Double.class, String.class);

        /**
         * Returns a list of JSON patches representing the differences of {@code from}
         * and {@code to} passed.
         * 
         * @param from
         * @param to
         * @param path The JSON path pointing to the root object ({@code from} and
         *             {@code to}) from where to compare. Must not end with "/".
         * @return A list of JSON patches representing the differences of {@code from}
         *         and {@code to} passed.
         */
        public static List<Patch> diff(final Map<String, Object> from,
                                       final Map<String, Object> to,
                                       final String path)
        {
            if (from == to)
                return List.of();

            final List<Patch> patches = new ArrayList<>();
            final Set<String> keys = new HashSet<>();
            keys.addAll(from.keySet());
            keys.addAll(to.keySet());

            keys.forEach(key ->
            {
                final StringBuilder p = new StringBuilder(path).append("/").append(key);

                if (!to.containsKey(key) && from.containsKey(key))
                {
                    patches.add(new Patch().op(Patch.Operation.REMOVE).path(p.toString()));
                }
                else if (to.containsKey(key) && !from.containsKey(key))
                {
                    patches.add(new Patch().op(Patch.Operation.ADD).path(p.toString()).value(to.get(key)));
                }
                else
                {
                    patches.addAll(compare(from.get(key), to.get(key), p.toString(), false));
                }
            });

            return patches;
        }

        private static boolean bothAreArrays(final Class<?> from,
                                             final Class<?> to)
        {
            return from == ArrayList.class && to == ArrayList.class;
        }

        private static boolean bothAreObjects(final Object from,
                                              final Object to)
        {
            return from instanceof Map && to instanceof Map;
        }

        @SuppressWarnings("unchecked")
        private static List<Patch> compare(final Object from,
                                           final Object to,
                                           final String path,
                                           final boolean isArrayPath)
        {
            final ArrayList<Patch> patches = new ArrayList<>();
            final Class<? extends Object> fromClass = from.getClass();
            final Class<? extends Object> toClass = to.getClass();

            if (oneIsPrimitive(fromClass, toClass))
            {
                if (!from.equals(to))
                {
                    if (isArrayPath)
                        patches.add(new Patch().op(Patch.Operation.REPLACE).path(path).value(to));
                    else
                        patches.add(new Patch().op(Patch.Operation.ADD).path(path).value(to)); // ADD adds or replaces if the path exists already.
                }
            }
            else if (bothAreObjects(from, to))
            {
                patches.addAll(diff((Map<String, Object>) from, (Map<String, Object>) to, path));
            }
            else if (bothAreArrays(fromClass, toClass))
            {
                final ArrayList<Object> fromArray = (ArrayList<Object>) from;
                final ArrayList<Object> toArray = (ArrayList<Object>) to;
                final ArrayList<Patch> arrayDiffs = new ArrayList<>();

                for (int i = 0; i < Math.min(fromArray.size(), toArray.size()); i++)
                    arrayDiffs.addAll(compare(fromArray.get(i), toArray.get(i), path + "/" + i, true));

                // add new to fromArray
                if (toArray.size() > fromArray.size())
                {
                    for (int i = fromArray.size(); i < toArray.size(); i++)
                        arrayDiffs.add(new Patch().op(Patch.Operation.ADD).path(path + "/" + i).value(toArray.get(i)));
                }

                // remove extra from fromArray
                if (toArray.size() < fromArray.size())
                {
                    for (int i = 0; i < fromArray.size() - toArray.size(); ++i)
                        arrayDiffs.add(new Patch().op(Patch.Operation.REMOVE).path(path + "/" + toArray.size()));
                }

                patches.addAll(arrayDiffs);
            }
            else
            {
                patches.add(new Patch().op(Patch.Operation.ADD).path(path).value(to));
            }

            return patches;
        }

        private static boolean oneIsPrimitive(final Class<?> from,
                                              final Class<?> to)
        {
            return JSON_PRIMITIVES.contains(to) || JSON_PRIMITIVES.contains(from);
        }

        private Diff()
        {
        }
    }

    private static final Logger log = LoggerFactory.getLogger(Json.class);
    private static final ObjectMapper json = com.ericsson.utilities.json.Jackson.newOm().registerModule(new JsonNullableModule());

    /**
     * Copies the source object using Jackson serialization/deserialization
     *
     * @param <T>    The object Type
     * @param source The object
     * @param clazz  The object class
     * @return A copy of the source object or null if object cannot be copied.
     */
    public static <T> T copy(final T source,
                             final Class<T> clazz)
    {
        try
        {
            return json.readValue(json.writeValueAsBytes(source), clazz);
        }
        catch (Exception e)
        {
            log.error("Unexpected error while copying source object {}", source, e);
            return null;
        }
    }

    /**
     * Convenience method for calling {@link #diff(Object, Object, String)} with
     * {@code path = ""}.
     */
    public static <T> List<Patch> diff(final T from,
                                       final T to) throws IOException
    {
        return diff(from, to, "");
    }

    /**
     * Compares two JSON objects and returns the differences as a list of JSON
     * patches.
     * <p>
     * Input parameter {@code from} and {@code to} must either be JSON formatted
     * Strings or have JSON annotated properties (@JsonProperty).
     * 
     * @param <T>  The type of the objects to be compared with each other.
     * @param from
     * @param to
     * @param path The JSON path pointing to the root object ({@code from} and
     *             {@code to}) from where to compare. Must not end with "/".
     * @return A list of JSON patches representing the differences of {@code from}
     *         and {@code to} passed.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static <T> List<Patch> diff(final T from,
                                       final T to,
                                       final String path) throws IOException
    {
        final Map<String, Object> f = from instanceof String ? json.readValue((String) from, LinkedHashMap.class)
                                                             : json.readValue(json.writeValueAsBytes(from), LinkedHashMap.class);
        final Map<String, Object> t = to instanceof String ? json.readValue((String) to, LinkedHashMap.class)
                                                           : json.readValue(json.writeValueAsBytes(to), LinkedHashMap.class);

        return Diff.diff(f, t, path);
    }

    /**
     * Finds the object pointed to by {@code path} in {@code source} and returns it.
     * 
     * @param source The object to search in.
     * @param path   The path pointing to the object to be found in {@code source}.
     * @param level  The starting level in {@code path}.
     * @return The object found or {@code null}.
     * @throws IOException
     */

    @SuppressWarnings("unchecked")
    public static Object find(final Object source,
                              final String[] path,
                              final int level) throws IOException
    {
        if (source == null)
            return source;

        if (path.length == level)
        {
            log.info("source={}", source);
            return source; // OK: Leaf exists.
        }

        log.debug("level={}, path[level]={}, source={}", level, path[level], source);

        if (source instanceof LinkedHashMap)
            return find(((LinkedHashMap<String, Object>) source).get(path[level]), path, level + 1);

        if (source instanceof ArrayList)
        {
            final ArrayList<Object> s = (ArrayList<Object>) source;

            if (path[level].equals("*"))
                return s;

            final int i = Integer.parseInt(path[level]);

            if (s.size() <= i)
            {
                log.debug("Erroneous path: index too large: {}", path[level]);
                return null;
            }

            return find(s.get(i), path, level + 1);
        }

        log.debug("Object not found at path: {}", path[level]);
        return null;
    }

    /**
     * Compare two instances of class T with JSON annotated properties
     * (@JsonProperty).
     * 
     * @param lhs The left-hand-side of the equation lhs == rhs.
     * @param rhs The right-hand-side of the equation lhs == rhs.
     * @throws IOException Thrown e.g. in case the class T has no JSON annotated
     *                     properties.
     */
    public static <T> boolean isEqual(final T lhs,
                                      final T rhs) throws IOException
    {
        boolean isEqual = lhs == rhs;

        if (!isEqual)
        {
            if (lhs == null || rhs == null)
            {
                isEqual = false;
            }
            else
            {
                final JsonNode jlhs = json.readTree(json.writeValueAsBytes(lhs));
                final JsonNode jrhs = json.readTree(json.writeValueAsBytes(rhs));

                isEqual = jlhs.equals(jrhs); // Lists are equal only if they contain the same elements in the very same
                                             // order.

                log.debug("isEqual={}, lhs={}, rhs={}", isEqual, jlhs, jrhs);
            }
        }

        return isEqual;
    }

    /**
     * Applies the JSON patch passed in {@code jsonPatchStr} to the JSON object
     * passed in {@code jsonSourceStr} and returns the result as String.
     * 
     * @param [in] jsonSourceStr The source in JSON format of a JSON object.
     *             Example: {"load": 49 }
     * @param [in] jsonPatchStr The patch in JSON format of a JSON array. Example:
     *             [{"op": "replace", "path": "/load", "value": 50 }]
     * @return The patched result in JSON format.
     */
    public static final String patch(final String jsonSourceStr,
                                     final String jsonPatchStr)
    {
        jakarta.json.JsonReader jsonReader;

        jsonReader = jakarta.json.Json.createReader(IOUtils.toInputStream(jsonPatchStr, StandardCharsets.UTF_8));
        jakarta.json.JsonPatch jsonPatch = jakarta.json.Json.createPatch(jsonReader.readArray());
        jsonReader.close();

        jsonReader = jakarta.json.Json.createReader(IOUtils.toInputStream(jsonSourceStr, StandardCharsets.UTF_8));
        final String jsonResultStr = jsonPatch.apply(jsonReader.readObject()).toString();
        jsonReader.close();

        log.debug("result={}, source={}, patch={}", jsonResultStr, jsonSourceStr, jsonPatchStr);
        return jsonResultStr;
    }

    /**
     * Convenience method for calling {@link #patch(String, String)}.
     * <p>
     * Input parameter {@code jsonSource} and {@code jsonPatch} must either be JSON
     * formatted Strings or have JSON annotated properties (@JsonProperty). The
     * patched result is converted to class R.
     */
    @SuppressWarnings("unchecked")
    public static final <R, T, U> R patch(final T jsonSource,
                                          final U jsonPatch,
                                          final Class<R> clazz) throws JsonProcessingException
    {
        final String jsonSourceStr = jsonSource instanceof String ? (String) jsonSource : json.writeValueAsString(jsonSource);
        final String jsonPatchStr = jsonPatch instanceof String ? (String) jsonPatch : json.writeValueAsString(jsonPatch);

        return clazz == String.class ? (R) patch(jsonSourceStr, jsonPatchStr) : json.readValue(patch(jsonSourceStr, jsonPatchStr), clazz);
    }

    private Json()
    {
    }
}
