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
* Created on: Mar 16, 2020
*     Author: eedstl
*/

package com.ericsson.sc.sepp.manager.upgrade;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.CmAdapter;
import com.ericsson.adpal.cm.model.Data;
import com.ericsson.adpal.cm.model.JsonSchema;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PatchItem;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.PatchOperation;
import com.ericsson.sc.nfm.model.IpEndpoint;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.sepp.manager.SeppManagerInterfacesParameters;
import com.ericsson.sc.sepp.model.EricssonSepp;
import com.ericsson.sc.sepp.model.NfManagement;
import com.ericsson.sc.sepp.model.NrfService;
import com.ericsson.sc.util.tls.DynamicTlsCertManager;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.file.SipTlsCertWatch;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.json.Jackson;
import com.ericsson.utilities.json.Json;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.Single;

/**
 * Encapsulates the transformation of configuration data from an old schema to a
 * new schema. In principle:
 * <li>Creates the new schema</li>
 * <li>Reads the configuration data from the old schema</li>
 * <li>Transforms the data to follow the new schema</li>
 * <li>Writes the configuration data to the new schema</li>
 * <p>
 * Transformations are only needed when there is a change of the major version
 * of the schema, because only then it means that the change is
 * non-backward-compatible.
 * </p>
 */
public class ConfigTransformer
{
    @SuppressWarnings("serial")
    public static class TransformationException extends RuntimeException
    {
        public TransformationException(final String message)
        {
            super(message);
        }
    }

    private static class UpgradePath implements Comparable<UpgradePath>
    {
        private String versionFrom;
        private final String versionTo;

        public UpgradePath(final String versionFrom,
                           final String versionTo)
        {
            this.versionFrom = versionFrom;
            this.versionTo = versionTo;
        }

        @Override
        public int compareTo(UpgradePath o)
        {
            int result = this.versionFrom.compareTo(o.versionFrom);

            if (result == 0)
                result = this.versionTo.compareTo(o.versionTo);

            return result;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (!(o instanceof UpgradePath))
                return false;

            final UpgradePath other = (UpgradePath) o;

            return Objects.equals(this.versionFrom, other.versionFrom) && Objects.equals(this.versionTo, other.versionTo);
        }

        /**
         * @return The version to upgrade from.
         */
        public String getVersionFrom()
        {
            return this.versionFrom;
        }

        /**
         * @return The version to upgrade to.
         */
        public String getVersionTo()
        {
            return this.versionTo;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(this.versionFrom, this.versionTo);
        }

        /**
         * @return Indicated whether or not the upgrade is backwards compatible.
         */
        public boolean isBackwardsCompatible()
        {
            return this.versionFrom.equals(this.versionTo);
        }

        @SuppressWarnings("unused")
        public UpgradePath setVersionFrom(final String versionFrom)
        {
            this.versionFrom = versionFrom;
            return this;
        }

        @Override
        public String toString()
        {
            return new StringBuilder("'").append(this.getVersionFrom()).append("' -> '").append(this.getVersionTo()).append("'").toString();
        }
    }

    /**
     * The transformation shall be performed from schema version
     * ERICSSON_SEPP_SCHEMA_FROM to schema version ERICSSON_SEPP_SCHEMA_TO.
     * <p>
     * Example:
     * <p>
     * <li>ERICSSON_SEPP_SCHEMA_FROM = "ericsson-sepp.x"
     * <li>ERICSSON_SEPP_SCHEMA_TO = "ericsson-sepp.y"</li>
     * <p>
     * where 1 <= x <= y (exemption is the initial schema version (1) which has got
     * no version number for historical reasons)
     * <p>
     * Each upgrade path (ERICSSON_SEPP_SCHEMA_FROM -> ERICSSON_SEPP_SCHEMA_TO)
     * needs its own method called like
     * <p>
     * addTranformationFromVersion_x_ToVersion_y()
     * <p>
     * which then encapsulates the JSON patches that have to be applied on the
     * configuration data following the old schema to get the configuration data
     * following the new schema. This method must be added to the static
     * initialization block for execution at startup.
     */
    private static final String ERICSSON_SEPP_SCHEMA_FROM = "ericsson-sepp";
    private static final String ERICSSON_SEPP_SCHEMA_TO = "ericsson-sepp"; //

    private static final Logger log = LoggerFactory.getLogger(ConfigTransformer.class);
    private static final ObjectMapper json = Jackson.om(); // create once, reuse
    private static final Map<UpgradePath, List<PatchItem>> transformations = new TreeMap<>();

    private static final SeppManagerInterfacesParameters params = SeppManagerInterfacesParameters.instance;
    /**
     * Indicates whether or not local testing is enabled.
     * <p>
     * If yes, tries to connect to the CM-mediator on localhost:55003 and reads the
     * new schema (for ease of use, only the JSON schema is required then) from path
     * <code>eric-sepp-manager/src/main/resources/com/ericsson/sc/sepp/model/</code>.
     */
    private static final boolean LOCAL_TEST = EnvVars.get("CM_MEDIATOR") == null;

    static
    {
        // Example method call for transformation from schema version x to version y:
        // addTransformationFromVersion_x_ToVersion_y_();
    }

    /**
     * Extracts the major version of the schema from the schema
     * <code>description</code> attribute of the schema passed and returns it.
     * <p>
     * The <code>description</code> attribute usually looks like this:
     * 
     * <pre>
     * "Generated by Yang2JsonSchema ...: ericsson-... (2.0.1), ..."
     *                this is what we are looking for --^
     * </pre>
     * </p>
     * 
     * @param schema The schema of which the major version is extracted.
     * @return The major version of the schema or <code>null</code>.
     */
    public static Integer getSchemaVersion(final JsonSchema schema)
    {
        final String description = schema.getAdditionalProperties().get("description").toString();
        final Matcher m = Pattern.compile("ericsson-[^(]*[(]([0-9]+)").matcher(description);

        if (!m.find())
        {
            log.error("Cound not extract schema version from description '{}'.", description);
            return null;
        }

        return Integer.valueOf(m.group(1));
    }

    public static void main(String[] args)
    {

        // 1. Deploy SC
        // 2. Pick the the updated and valid certificates of scp-manager for the cm
        // mediator interface (/interfaces/client/mediator/certificates)
        // 3. Use the certificates from the ConfigTransformer class
        // 4. Port forward mediator 5004 (tls port)

        int result = 0;

        try
        {
            new ConfigTransformer().transform()
                                   .ignoreElement()
                                   .doOnError(e -> log.error("Error transforming configuration. Cause: {}", e.getMessage()))
                                   .blockingAwait();
        }
        catch (Exception e)
        {
            log.error("Error transforming configuration.", e);
            result = -1;
        }

        log.info("Result of the transformation: {}successful", result == 0 ? "" : "un");

        System.exit(result);
    }

    /**
     * Template method for the transformation from schema version x to version y.
     * <p>
     * At the end of the method the list of patch items must be added to the static
     * map <code>transformations</code>.
     */
    @SuppressWarnings("unused")
    private static void addTransformationFromVersion_x_ToVersion_y_()
    {
        final List<PatchItem> items = new ArrayList<>();
        PatchItem patch;

        {
            // Rename attribute 'ericsson-sepp:sepp-function' to
            // 'ericsson-sepp-2:sepp-function':

            patch = new PatchItem();
            patch.setOp(PatchOperation.COPY);
            patch.setFrom("/ericsson-sepp:sepp-function");
            patch.setPath("/ericsson-sepp-2:sepp-function");
            items.add(patch);

            patch = new PatchItem();
            patch.setOp(PatchOperation.REMOVE);
            patch.setPath("/ericsson-sepp:sepp-function");
            items.add(patch);
        }

        {
            // Rename attribute 'registration-priority' to 'priority' in all 'nrf's:

            patch = new PatchItem();
            patch.setOp(PatchOperation.MOVE);
            patch.setFrom("/ericsson-sepp-2:sepp-function/nf-instance/*/nrf-group/*/nrf/*/registration-priority");
            patch.setPath("/ericsson-sepp-2:sepp-function/nf-instance/*/nrf-group/*/nrf/*/priority");
            items.add(patch);
        }

        {
            // Add attribute 'nf-type="chf"' to all 'nf-profile's:

            patch = new PatchItem();
            patch.setOp(PatchOperation.ADD);
            patch.setPath("/ericsson-sepp-2:sepp-function/nf-instance/*/nf-profile/*/nf-type");
            patch.setValue(NFType.CHF);
            items.add(patch);
        }

        {
            // Add attribute 'scheme="http"' to all 'nrf's:

            patch = new PatchItem();
            patch.setOp(PatchOperation.ADD);
            patch.setPath("/ericsson-sepp-2:sepp-function/nf-instance/*/nrf-group/*/nrf/*/scheme");
            patch.setValue(Scheme.HTTP.value());
            items.add(patch);
        }

        {
            // Transforming 'nrf/url' to 'nrf/ip-endpoint':
            // Using ADD/REMOVE combination as MOVE ignores the value set in the PatchItem.

            final UnaryOperator<Object> valueConversion = source ->
            {
                if (!(source instanceof ArrayList))
                    throw new IllegalArgumentException("Argument source is not a list.");

                @SuppressWarnings("unchecked")
                final List<Object> urls = (ArrayList<Object>) source;
                final List<IpEndpoint> result = new ArrayList<>();

                for (int i = 0; i < urls.size(); ++i)
                {
                    final String spec = (String) urls.get(i);

                    try
                    {
                        final URL url = new URL(spec);

                        result.add(new IpEndpoint().withName(String.format("ep-%02d", i + 1)).withPort(url.getPort()).withIpv4Address(url.getHost()));
                    }
                    catch (MalformedURLException e)
                    {
                        throw new IllegalArgumentException("Error converting url '" + spec + "' to ip-endpoint.", e);
                    }
                }

                return result;
            };

            patch = new PatchItem();
            patch.setOp(PatchOperation.ADD);
            patch.setFrom("/ericsson-sepp-2:sepp-function/nf-instance/*/nrf-group/*/nrf/*/url");
            patch.setPath("/ericsson-sepp-2:sepp-function/nf-instance/*/nrf-group/*/nrf/*/ip-endpoint");
            patch.setValue(valueConversion);
            items.add(patch);

            patch = new PatchItem();
            patch.setOp(PatchOperation.REMOVE);
            patch.setPath("/ericsson-sepp-2:sepp-function/nf-instance/*/nrf-group/*/nrf/*/url");
            items.add(patch);
        }

        {
            // Add names of all 'nrf-group's to new attribute 'nrf-service/nf-management':

            final UnaryOperator<Object> valueConversion = source ->
            {
                if (!(source instanceof ArrayList))
                    throw new IllegalArgumentException("Argument source is not a list.");

                @SuppressWarnings("unchecked")
                final List<Object> groups = (ArrayList<Object>) source;
                final List<String> groupNames = new ArrayList<>();

                for (int i = 0; i < groups.size(); ++i)
                    groupNames.add((String) ((LinkedHashMap<?, ?>) groups.get(i)).get("name"));

                final NrfService result = new NrfService();

                if (!groupNames.isEmpty())
                    result.withNfManagement(new NfManagement().withNrfGroupRef(groupNames));

                return result;
            };

            patch = new PatchItem();
            patch.setOp(PatchOperation.ADD);
            patch.setFrom("/ericsson-sepp-2:sepp-function/nf-instance/*/nrf-group");
            patch.setPath("/ericsson-sepp-2:sepp-function/nf-instance/*/nrf-service");
            patch.setValue(valueConversion);
            items.add(patch);
        }

        // Add all path-items to the static map 'transformations'.
        // Observe the key-format: "x -> y"
        transformations.put(new UpgradePath("ericsson-sepp-1", "ericsson-sepp-2"), items);
    }

    @SuppressWarnings("unchecked")
    private static Object find(final Object source,
                               final String[] path,
                               final int level)
    {
        if (source == null)
            return source;

        if (path.length == level)
        {
            log.info("source={}", source);
            return source; // OK: Leaf exists.
        }

        log.info("level={}, path[level]={}, source={}", level, path[level], source);

        if (source instanceof LinkedHashMap)
            return find(((LinkedHashMap<String, Object>) source).get(path[level]), path, level + 1);

        if (source instanceof ArrayList)
        {
            final ArrayList<Object> s = (ArrayList<Object>) source;

            if (path[level].equals("*"))
                return s;

            final int i = Integer.parseInt(path[level]);

            if (s.size() <= i)
                throw new IllegalArgumentException("Erroneous path: index too large: " + path[level]);

            return find(s.get(i), path, level + 1);
        }

        throw new IllegalArgumentException("Object not found.");
    }

    private final UpgradePath upgradePath;

    private WebClientProvider webClientProvider;
    private CmAdapter<Data> dataCmAdapter;
    private CmAdapter<EricssonSepp> schemaCmAdapter;

    public ConfigTransformer()
    {
        final var wcb = WebClientProvider.builder().withHostName(params.serviceHostname);

        if (params.globalTlsEnabled)
            wcb.withDynamicTls(DynamicTlsCertManager.create(SipTlsCertWatch.keyCert(params.mediatorClientCertPath),
                                                            SipTlsCertWatch.trustedCert(params.sipTlsRootCaPath)));
        this.webClientProvider = wcb.build(VertxInstance.get());

        this.upgradePath = new UpgradePath(ERICSSON_SEPP_SCHEMA_FROM, ERICSSON_SEPP_SCHEMA_TO);

        this.dataCmAdapter = new CmAdapter<>(Data.class,
                                             this.upgradePath.getVersionFrom(),
                                             VertxInstance.get(),
                                             LOCAL_TEST ? 55003 : params.mediatorPort,
                                             LOCAL_TEST ? "localhost" : params.mediatorHostname,
                                             this.webClientProvider,
                                             params.globalTlsEnabled,
                                             params.subscribeValidity,
                                             params.subscribeRenewal,
                                             params.subscribeHeartbeat);

        this.schemaCmAdapter = new CmAdapter<>(EricssonSepp.class,
                                               this.upgradePath.getVersionTo(),
                                               VertxInstance.get(),
                                               LOCAL_TEST ? 55003 : params.mediatorPort,
                                               LOCAL_TEST ? "localhost" : params.mediatorHostname,
                                               webClientProvider,
                                               params.globalTlsEnabled,
                                               params.subscribeValidity,
                                               params.subscribeRenewal,
                                               params.subscribeHeartbeat);
    }

    public Single<Integer> transform()
    {
        return this.updateSchemaTo()//
                   .filter(result ->
                   {
                       if (this.upgradePath.isBackwardsCompatible())
                           return true;

                       if (result == HttpResponseStatus.OK.code())
                       {
                           log.info("Schema '{}' already exists, skipping updating configuration.", this.upgradePath.getVersionTo());
                           return true;
                       }

                       return false;
                   })
                   .switchIfEmpty(Single.defer(() ->
                   {
                       log.info("Transforming the configuration for upgrade path: {}", this.upgradePath);

                       return this.dataCmAdapter.getSchema()
                                                .get()
                                                .zipWith(this.dataCmAdapter.getConfiguration().get(),
                                                         (schemaFrom,
                                                          configFrom) ->
                                                         {
                                                             // Hook for special treatment for upgrades from ericsson-sepp to
                                                             // ericsson-sepp-x,
                                                             // in case the ericsson-sepp may have different versions at different customers,
                                                             // for example:
                                                             // the version of ericsson-sepp may be 1 or 2. Discover the schema version of
                                                             // the
                                                             // old schema and adjust the upgrade path correspondingly.

//                                                        if (schemaFrom.isPresent() && ERICSSON_SEPP_SCHEMA_FROM.equals("ericsson-sepp"))
//                                                        {
//                                                            final String versionFrom = ERICSSON_SEPP_SCHEMA_FROM + "-"
//                                                                                       + String.valueOf(getSchemaVersion(schemaFrom.get()));
//                                                            this.upgradePath.setVersionFrom(versionFrom);
//                                                        }

                                                             return configFrom;
                                                         })
                                                .flatMap(configFrom ->
                                                {
                                                    if (configFrom.isPresent())
                                                    {
                                                        log.info("Adjusted upgrade path: {}", this.upgradePath);

                                                        final Data dataFrom = configFrom.get();
                                                        final List<PatchItem> patch = this.convertValues(this.removeInvalidItems(this.expand(transformations.get(this.upgradePath),
                                                                                                                                             dataFrom),
                                                                                                                                 dataFrom),
                                                                                                         dataFrom);
                                                        final Data dataTo = Json.patch(dataFrom, patch.toArray(new PatchItem[0]), Data.class);

                                                        log.info("Configuration before transformation: {}", dataFrom);
                                                        log.info("Configuration after transformation: {}", dataTo);

                                                        log.info("Updating transformed configuration '{}'.", this.upgradePath.getVersionTo());

                                                        return this.dataCmAdapter.getConfiguration()
                                                                                 .updateRaw(dataTo)
                                                                                 .doOnSuccess(statusCode -> log.info("HTTP-result from CM Mediator for updating configuration '{}': {}",
                                                                                                                     this.upgradePath.getVersionTo(),
                                                                                                                     HttpResponseStatus.valueOf(statusCode)))
                                                                                 .doOnError(e -> log.error("Error updating configuration '{}'. Cause: {}",
                                                                                                           this.upgradePath.getVersionTo(),
                                                                                                           e.getMessage()))
                                                                                 .filter(result -> result == HttpResponseStatus.CREATED.code()
                                                                                                   || result == HttpResponseStatus.OK.code())
                                                                                 .switchIfEmpty(Single.error(new TransformationException("Unexpected response received.")))
                                                                                 .onErrorResumeNext(e -> this.removeSchema()
                                                                                                             .onErrorReturnItem(HttpResponseStatus.OK.code())
                                                                                                             .flatMap(result -> Single.error(e)));
                                                    }
                                                    else
                                                    {
                                                        log.info("Configuration '{}' not found, skipping transformation.", this.upgradePath.getVersionFrom());
                                                        return Single.just(HttpResponseStatus.NOT_FOUND.code());
                                                    }
                                                });
                   }));
    }

    private List<PatchItem> convertValues(List<PatchItem> items,
                                          Data data)
    {
        for (PatchItem item : items)
        {
            if (item.getValue() instanceof UnaryOperator)
            {
                @SuppressWarnings("unchecked")
                final UnaryOperator<Object> func = (UnaryOperator<Object>) item.getValue();
                item.setValue(func.apply(find(data.getAdditionalProperties().get(ERICSSON_SEPP_SCHEMA_FROM + ":sepp-function"), item.getFrom().split("/"), 2)));
                item.setFrom(null); // Remove from ADD operation.
            }
        }

        return items;
    }

    /**
     * Replaces PatchItems with wild-cards in their path with as many copies
     * according to the content of the data passed.
     * 
     * @param items The list of possibly wild-carded PatchItems.
     * @param data  The configuration data needed as blueprint for the expansion of
     *              the wild-carded PatchItems.
     * @return The expanded list of PatchItems
     */
    @SuppressWarnings("unchecked")
    private List<PatchItem> expand(final List<PatchItem> items,
                                   final Data data)
    {
        List<PatchItem> expandedItems = items;
        boolean hasExpanded;

        do
        {
            hasExpanded = false;

            final List<PatchItem> newExpandedItems = new ArrayList<>();

            for (PatchItem item : expandedItems)
            {
                if (item.getPath().contains("*"))
                {
                    hasExpanded = true;

                    final Object result = find(data.getAdditionalProperties().get(ERICSSON_SEPP_SCHEMA_FROM + ":sepp-function"), item.getPath().split("/"), 2);
                    int size = 0;

                    log.info("result={}", result);

                    if (result != null)
                    {
                        if (result instanceof ArrayList)
                            size = ((ArrayList<Object>) result).size();

                        for (int i = 0; i < size; ++i)
                        {
                            final PatchItem copy = new PatchItem();
                            copy.setFrom(item.getFrom() != null ? item.getFrom().replaceFirst("\\*", String.valueOf(i)) : null);
                            copy.setOp(item.getOp());
                            copy.setPath(item.getPath().replaceFirst("\\*", String.valueOf(i)));
                            copy.setValue(item.getValue());

                            newExpandedItems.add(copy);
                        }
                    }
                }
                else
                {
                    newExpandedItems.add(item);
                }
            }

            expandedItems = newExpandedItems;
        }
        while (hasExpanded);

        return expandedItems;
    }

    private List<PatchItem> removeInvalidItems(final List<PatchItem> items,
                                               final Data data)
    {
        final List<PatchItem> invalidItems = new ArrayList<>();

        for (PatchItem item : items)
        {
            final Object source = data.getAdditionalProperties().get(ERICSSON_SEPP_SCHEMA_FROM + ":sepp-function");
            boolean itemIsValid = true;

            switch (item.getOp().toLowerCase())
            {
                case PatchOperation.ADD:
                    // If "from" is present in the item, verify that "from" is present in the source
                    // data:
                    itemIsValid = item.getFrom() == null || !item.getFrom().contains("*") && find(source, item.getFrom().split("/"), 2) != null;
                    break;

                case PatchOperation.COPY:
                case PatchOperation.MOVE:
                    // Verify that "from" is present in the source data:
                    itemIsValid = !item.getFrom().contains("*") && find(source, item.getFrom().split("/"), 2) != null;
                    break;

                case PatchOperation.REMOVE:
                case PatchOperation.REPLACE:
                case PatchOperation.TEST:
                    // Verify that "path" is present in the source data:
                    itemIsValid = !item.getPath().contains("*") && find(source, item.getPath().split("/"), 2) != null;
                    break;

                default:
                    itemIsValid = false;
                    break;
            }

            if (!itemIsValid)
            {
                log.info("Source data do not contain required input. Skipping patch {}", item);
                invalidItems.add(item);
            }
        }

        items.removeAll(invalidItems);
        return items;
    }

    private Single<Integer> removeSchema()
    {
        try
        {

            Single<Integer> deleteConfig = this.schemaCmAdapter.getConfiguration()
                                                               .delete()
                                                               .doOnSuccess(result -> log.info("HTTP-result from CM Mediator for removing configuration '{}': {}",
                                                                                               this.upgradePath.getVersionTo(),
                                                                                               HttpResponseStatus.valueOf(result)))
                                                               .filter(result -> result >= 200 && result < 500) // Retry on server error. Don't retry on client
                                                                                                                // error, makes no
                                                                                                                // sense.
                                                               .switchIfEmpty(Single.error(new TransformationException("Unexpected response received.")))
                                                               .doOnError(e -> log.error("Removing configuration '{}' failed, retrying. Cause: {}",
                                                                                         this.upgradePath.getVersionTo(),
                                                                                         Utils.toString(e, log.isDebugEnabled())))
                                                               .retryWhen(handler -> handler.delay(1, TimeUnit.SECONDS));

            Single<Integer> deleteSchema = this.schemaCmAdapter.getSchema()
                                                               .delete()
                                                               .doOnSuccess(result -> log.info("HTTP-result from CM Mediator for removing schema '{}': {}",
                                                                                               this.upgradePath.getVersionTo(),
                                                                                               HttpResponseStatus.valueOf(result)))
                                                               .filter(result -> result >= 200 && result < 500) // Retry on server error. Don't retry on client
                                                                                                                // error, makes no
                                                                                                                // sense.
                                                               .switchIfEmpty(Single.error(new TransformationException("Unexpected response received.")))
                                                               .doOnError(e -> log.error("Removing schema '{}' failed, retrying. Cause: {}",
                                                                                         this.upgradePath.getVersionTo(),
                                                                                         Utils.toString(e, log.isDebugEnabled())))
                                                               .retryWhen(handler -> handler.delay(1, TimeUnit.SECONDS));

            return deleteConfig.filter(result -> result < 200 || result >= 500).switchIfEmpty(deleteSchema);
        }
        catch (final Exception t)
        {
            throw new TransformationException("Could not update schema '" + this.upgradePath.getVersionTo() + "'. Cause: "
                                              + Utils.toString(t, log.isDebugEnabled()));
        }
    }

    private Single<Integer> updateSchemaTo()
    {
        try
        {

            final String resourcePath = EricssonSepp.class.getPackage().getName().replace(".", "/");
            log.info("resourcePath: '{}'", resourcePath);

            final var jsonSchema = json.readValue(ClassLoader.getSystemResourceAsStream(resourcePath + "/" + this.upgradePath.getVersionTo() + ".json"),
                                                  com.ericsson.adpal.cm.model.JsonSchema.class);

//            final Single<Integer> updateSchema = cm.getSchema().updateIfNeeded(jsonSchema); // May be used for testing only, replacing the following 2 statements.
            final String resourcePathPrefix = LOCAL_TEST ? "src/main/resources/" : "/java-exec/classes/";
            log.info("resourcePathPrefix: '{}'", resourcePathPrefix);

            final Single<Integer> updateSchema = this.schemaCmAdapter.getSchema()
                                                                     .updateIfNeeded(jsonSchema,
                                                                                     resourcePathPrefix + resourcePath,
                                                                                     this.upgradePath.getVersionTo() + ".json",
                                                                                     resourcePathPrefix + resourcePath,
                                                                                     this.upgradePath.getVersionTo() + ".tar.gz");

            return updateSchema.doOnSuccess(result -> log.info("HTTP-result from CM Mediator for schema '{}': {}",
                                                               this.upgradePath.getVersionTo(),
                                                               HttpResponseStatus.valueOf(result)))
                               .filter(result -> result == HttpResponseStatus.OK.code() || result == HttpResponseStatus.CREATED.code())
                               .switchIfEmpty(Single.error(new TransformationException("Unexpected response received.")))
                               .doOnError(e -> log.error("Updating schema '{}' failed, retrying. Cause: {}",
                                                         this.upgradePath.getVersionTo(),
                                                         Utils.toString(e, log.isDebugEnabled())))
                               .retryWhen(handler -> handler.delay(1, TimeUnit.SECONDS))
                               .filter(result -> result == HttpResponseStatus.OK.code())
                               .toSingle()
                               .doOnSuccess(result -> log.info("Schema created"))
                               .doOnError(e -> log.error("Creation of schema '{}' failed. Cause: {}",
                                                         this.upgradePath.getVersionTo(),
                                                         Utils.toString(e, log.isDebugEnabled())));
        }
        catch (final Exception t)
        {
            throw new TransformationException("Could not update schema '" + this.upgradePath.getVersionTo() + "'. Cause: "
                                              + Utils.toString(t, log.isDebugEnabled()));
        }
    }
}
