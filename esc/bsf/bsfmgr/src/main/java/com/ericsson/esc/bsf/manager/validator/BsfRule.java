package com.ericsson.esc.bsf.manager.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.adpal.cm.validator.RuleResult;
import com.ericsson.esc.bsf.helper.BsfHelper;
import com.ericsson.esc.bsf.manager.DbScanConfigMapper;
import com.ericsson.sc.bsf.model.BindingDatabaseScan;
import com.ericsson.sc.bsf.model.BindingDatabaseScan.Configuration;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.bsf.model.NrfGroup;
import com.ericsson.sc.bsf.model.ServiceAddress;
import com.ericsson.sc.nfm.model.IpEndpoint;
import com.ericsson.sc.nfm.model.NfService;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile.Type;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.nfm.model.ServiceName;
import com.ericsson.sc.rxkms.KmsClientUtilities;
import com.ericsson.sc.rxkms.KmsParameters;
import com.ericsson.sc.validator.Rule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.impl.jose.JWK;

/**
 * Enum that keeps the BSF validation {@link Rule}
 */

public enum BsfRule
{
    /**
     * Validate BindingDatabase scan configuration. If Configuration is set to
     * SCHEDULED, Schedule attribute should be a valid UNIX or QUARTZ cron
     * expression.
     */
    SCHEDULE_RULE(BsfRule::validateSchedule),
    /**
     * Validate oauth2required leaf configuration. If oauth2required is configured,
     * a check that verifies that all common endpoints (scheme + port + ip) of a
     * service have the same oauth2required value is performed
     */
    VALID_OAUTH2REQUIRED_RULE(BsfRule::validateOauth2required),
    /**
     * Validate nf-instance-id leaf configuration. If at least one oauth2-required
     * is set to true, the nf-instance-id must be configured.
     */
    CONFIGURED_NF_INSTANCE_RULE(BsfRule::validateNfInstance),
    /**
     * Validate json body and oauth2 public key format . If oauth2 is configured,
     * checks that the json-body is valid JSON and oauth2-key-profile refers to a
     * valid JWK
     */
    VALID_PUBLIC_KEY_FORMAT_RULE(BsfRule::validateKeyFormat);

    private static final Logger log = LoggerFactory.getLogger(BsfRule.class);
    private static final String NA = "Not applicable";
    Rule<EricssonBsf> rule;

    BsfRule(final Rule<EricssonBsf> rule)
    {
        this.rule = rule;
    }

    /**
     * Method that retrieves the entire ServiceAddress object for a given
     * serviceAddressRef. Note that service-address-ref is unique per ServiceAddress
     * object
     * 
     * @param serviceAddressRef {@link String} the service address reference to be
     *                          matched
     * @param the               {@link List<ServiceAddress>} the list of service
     *                          addresses
     * @return the {@link ServiceAddress} the service address object that matches
     *         the service address reference
     */
    private static ServiceAddress findServiceAddress(final String serviceAddressRef,
                                                     final List<ServiceAddress> serviceAddressList)
    {
        return serviceAddressList.stream().filter(svcAdd -> svcAdd.getName().equals(serviceAddressRef)).findFirst().orElseThrow();
    }

    /**
     * Method that uses the information from ServiceAddress, NfService and NfProfile
     * to create an endpoint
     * 
     * @param the                        {@link NfService} the constructed endpoint
     *                                   is referring
     * @param nfProfileServiceAddressRef {@link String} the NfProfile's service
     *                                   address reference
     * @return the {@link List<ServiceAddress>} the entire service address list of
     *         the nf instance
     */
    private static IpEndpoint createEndpointFromNfService(final NfService nfService,
                                                          final String nfProfileServiceAddressRef,
                                                          final List<ServiceAddress> serviceAddressList)
    {

        // serviceAddressRef in nfService is not mandatory, in case this field is
        // missing use the nfProfile serviceAddressRef
        final var serviceAddressRef = (nfService.getServiceAddressRef().isEmpty()) ? nfProfileServiceAddressRef : nfService.getServiceAddressRef().get(0);
        final var serviceAddress = findServiceAddress(serviceAddressRef, serviceAddressList);

        final var serviceAddressPort = (nfService.getScheme().equals(Scheme.HTTP)) ? serviceAddress.getPort() : serviceAddress.getTlsPort();
        return new IpEndpoint().withIpv4Address(serviceAddress.getIpv4Address()).withIpv6Address(serviceAddress.getIpv6Address()).withPort(serviceAddressPort);

    }

    /**
     * Method that compares all the elements of the list and returns false if there
     * is at least one element different from the rest.
     * 
     * @param listToBeChecked {@link List<String>} the boolean list to be checked
     * @return true {@link Boolean} if all list elements are equal return true,
     *         otherwise false
     */
    private static boolean allEqual(final List<Boolean> listToBeChecked)
    {
        return listToBeChecked.stream().distinct().count() <= 1;
    }

    /**
     * Caller method for each {@link BsfRule} defined in the class
     * 
     * @param value {@link List<String>} the json body to be checked
     * @return true {@link Boolean} if value is a valid json returns true, otherwise
     *         false
     */
    private static boolean isValidJson(final String value)
    {
        final var objectMapper = new ObjectMapper();

        try
        {
            log.info("isValidJson value: {} , objMapper: {}", value, objectMapper);
            objectMapper.readTree(value);
            return true;
        }
        catch (final JsonProcessingException e)
        {
            log.error("Json is not valid ", e);
            return false;
        }
    }

    /*
     * Checks if the provided JSON contains at least one of the specified
     * properties.
     * 
     * True if the JSON contains one of the provided properties, false otherwise.
     * 
     */
    private static boolean jsonHasProperties(final JsonObject json,
                                             final String... properties)
    {
        for (final var property : properties)
        {
            if (json.containsKey(property) && json.getValue(property) != null)
            {
                return true;
            }
        }

        return false;
    }

    private static RuleResult validateJwkOrPem(final String decKey,
                                               final Oauth2KeyProfile keyProfile)
    {

        return Oauth2KeyProfile.Type.JWK.equals(keyProfile.getType()) ? validateJwk(decKey) : validatePem(keyProfile.getAlg(), decKey);
    }

    public static RuleResult validateJwk(final String jwkString)
    {
        log.info("Validating JwkPem: {}", jwkString);
        if (!isValidJson(jwkString))
        {
            return new RuleResult(false, "Invalid json body");
        }
        else
        {
            final var json = new JsonObject(jwkString);
            try
            {
                if (!json.containsKey("alg"))
                {
                    return new RuleResult(false, "Key in JWK format does not have the alg parameter");
                }

                final var jwk = new JWK(json);

                final var kty = jwk.kty();

                if (!kty.equals("oct") && jwk.publicKey() == null)
                {
                    return new RuleResult(false, "Key in JWK format does not have all the required public parameters");
                }

                switch (kty)
                {
                    case ("EC"), ("OKP"):
                        if (jsonHasProperties(json, "d"))
                        {
                            return new RuleResult(false, "Key in JWK format has private key parameters");
                        }
                        break;
                    case ("RSA"), ("RSASSA"):
                        if (jsonHasProperties(json, "d", "p", "q", "dp", "dq", "qi"))
                        {
                            return new RuleResult(false, "Key in JWK format has private key parameters");
                        }
                        break;
                    default:
                        break;
                }

            }
            catch (final Exception e)
            {
                return new RuleResult(false, "Invalid key in JWK format");
            }
            return new RuleResult(true, NA);
        }
    }

    public static RuleResult validatePem(final Oauth2KeyProfile.Alg alg,
                                         final String pemString)
    {
        log.info("Validating Pem: {}", pemString);

        if (isSymmetricAlg(alg) && isAsymmetricKey(pemString))
        {
            return new RuleResult(false, "Key mismatch, symmetric alg and asymmetric key");
        }
        final var options = new PubSecKeyOptions().setAlgorithm(alg.toString()).setBuffer(pemString);
        try
        {
            final var jwk = new JWK(options);
            final var kty = jwk.kty();

            if (!(kty.equals("oct")) && (jwk.publicKey() == null))
            {
                return new RuleResult(false, "Key in PEM format is not public");
            }

        }
        catch (final Exception e)
        {
            return new RuleResult(false, "Invalid public key in PEM format");
        }
        return new RuleResult(true, NA);
    }

    public static Single<RuleResult> validateSchedule(final EricssonBsf config)
    {
        if (config == null || config.getEricssonBsfBsfFunction() == null)
        {
            return Single.just(new RuleResult(true, NA));
        }

        final var schedules = config.getEricssonBsfBsfFunction()
                                    .getNfInstance()
                                    .stream()
                                    .flatMap(nfinstance -> nfinstance.getBsfService().stream())
                                    .filter(bsfSvc -> bsfSvc.getPcfRecoveryTime() != null && bsfSvc.getPcfRecoveryTime().getBindingDatabaseScan() != null)
                                    .map(bsfSvc -> bsfSvc.getPcfRecoveryTime().getBindingDatabaseScan())
                                    .filter(dbScan -> dbScan.getConfiguration().equals(Configuration.SCHEDULED))
                                    .map(BindingDatabaseScan::getSchedule)
                                    .toList();

        final var invalidSchedules = schedules.stream().filter(schedule -> !DbScanConfigMapper.isValidCron(schedule)).toList();

        final var result = invalidSchedules.isEmpty();
        final var msg = "Invalid schedule expression: " + invalidSchedules.toString();

        return schedules.isEmpty() ? Single.just(new RuleResult(true, NA)) : Single.just(new RuleResult(result, msg));
    }

    public static Single<RuleResult> validateKeyFormat(final EricssonBsf config)
    {
        if (config == null || config.getEricssonBsfBsfFunction() == null)
        {
            return Single.just(new RuleResult(true, NA));
        }

        final var oauth2KeyProfileList = config.getEricssonBsfBsfFunction()
                                               .getNfInstance()
                                               .stream()
                                               .flatMap(nfInstance -> nfInstance.getOauth2KeyProfile().stream())
                                               .toList();

        if (oauth2KeyProfileList.isEmpty())
        {
            return Single.just(new RuleResult(true, NA));
        }

        final var kmsUtilities = KmsClientUtilities.get(KmsParameters.instance, "eric-cm-key-role");

        return Flowable.fromIterable(oauth2KeyProfileList)//
                       .flatMapMaybe(keyProfile ->
                       {
                           final var encKey = Type.JWK.equals(keyProfile.getType()) ? keyProfile.getJsonBody() : keyProfile.getValue();

                           return BsfHelper.decrypt(kmsUtilities, encKey)
                                           .map(decKey -> decKey.isPresent() ? validateJwkOrPem(decKey.get(), keyProfile)
                                                                             : new RuleResult(false, "Failed to decrypt JWK - empty result"))
                                           .timeout(10, TimeUnit.SECONDS)
                                           .filter(rule -> !rule.getResult()); // let pass only the failures

                       })
                       .toList()
                       .map(ruleList -> ruleList.isEmpty() ? new RuleResult(true, NA) : ruleList.get(0));

    }

    public static Single<RuleResult> validateOauth2required(final EricssonBsf config)
    {

        if (config == null || config.getEricssonBsfBsfFunction() == null)
        {
            return Single.just(new RuleResult(true, NA));
        }

        final var nfServiceEndpointSets = new HashMap<Pair<IpEndpoint, ServiceName>, List<NfService>>();
        final var nfInstanceList = config.getEricssonBsfBsfFunction().getNfInstance();

        final var nrfGroupinNfManagement = nfInstanceList.stream()
                                                         .filter(nfinstance -> (nfinstance.getNrfService() != null
                                                                                && nfinstance.getNrfService().getNfManagement() != null
                                                                                && nfinstance.getNrfService().getNfManagement().getNrfGroupRef() != null))
                                                         .flatMap(nfinstance -> nfinstance.getNrfService().getNfManagement().getNrfGroupRef().stream())
                                                         .toList();

        final var nfProfilesinNrfGroups = nfInstanceList.stream()
                                                        .flatMap(nfinstance -> nfinstance.getNrfGroup().stream())
                                                        .filter(nrfGroup -> nrfGroupinNfManagement.contains(nrfGroup.getName())
                                                                            && nrfGroup.getNfProfileRef() != null)
                                                        .map(NrfGroup::getNfProfileRef)
                                                        .toList();

        final var serviceAddressList = nfInstanceList.stream().flatMap(nfInstance -> nfInstance.getServiceAddress().stream()).toList();

        final var nfProfileList = nfInstanceList.stream()
                                                .flatMap(nfInstance -> nfInstance.getNfProfile().stream())
                                                .filter(nfProfile -> nfProfilesinNrfGroups.contains(nfProfile.getName()))
                                                .toList();
        nfProfileList.forEach(nfProfile ->
        {
            final var nfProfileServiceAddressRef = nfProfile.getServiceAddressRef();
            final var nfServicesList = nfProfile.getNfService();

            nfServicesList.forEach(nfService ->
            {
                final var endpoint = createEndpointFromNfService(nfService, nfProfileServiceAddressRef, serviceAddressList);
                final var endpointServiceNamePair = new Pair<IpEndpoint, ServiceName>(endpoint, nfService.getServiceName());

                final var endpointsSvs = nfServiceEndpointSets.get(endpointServiceNamePair);
                final var newEndpointSvcs = new ArrayList<NfService>();

                if (Objects.isNull(endpointsSvs)) // endpoint not in hashMap
                {
                    nfServiceEndpointSets.put(endpointServiceNamePair, List.of(nfService));
                }
                else
                {
                    newEndpointSvcs.addAll(endpointsSvs); // create a copy since get operation return immutable list
                    newEndpointSvcs.add(nfService);
                    nfServiceEndpointSets.replace(endpointServiceNamePair, newEndpointSvcs);
                }
            });
        });

        for (final var entry : nfServiceEndpointSets.entrySet())
        {
            final var oauth2RequiredList = entry.getValue()
                                                .stream()
                                                .map(NfService::getOauth2Required)
                                                .map(Boolean.TRUE::equals) // when oauth2required is null is considered FALSE
                                                .toList();
            if (oauth2RequiredList != null && !oauth2RequiredList.isEmpty() && !allEqual(oauth2RequiredList))
            {
                return Single.just(new RuleResult(false, "Invalid oauth2Required configuration"));
            }
        }

        return Single.just(new RuleResult(true, NA));

    }

    public static Single<RuleResult> validateNfInstance(final EricssonBsf config)
    {
        if (config != null && config.getEricssonBsfBsfFunction() != null)
        {
            final var nfInstanceList = config.getEricssonBsfBsfFunction().getNfInstance();

            for (final var nfInstance : nfInstanceList)
            {
                for (final var nfProfile : nfInstance.getNfProfile())
                {
                    if (!nfProfile.getNfService().stream().map(NfService::getOauth2Required).filter(Boolean.TRUE::equals).toList().isEmpty()
                        && nfInstance.getNfInstanceId() == null)
                    {
                        return Single.just(new RuleResult(false,
                                                          String.format("Since oAuth2.0 is enabled, NF instance id must be configured for NF instance %s",
                                                                        nfInstance.getName())));
                    }

                }

            }

            return Single.just(new RuleResult(true, NA));

        }

        else
        {
            return Single.just(new RuleResult(true, NA));
        }
    }

    /**
     * Caller method for each {@link BsfRule} defined in the class
     * 
     * @param config as {@link EricssonBsf} POJO Schema
     * @return the {@link RuleResult}
     */
    public Single<RuleResult> validateOn(final EricssonBsf config)
    {
        return this.rule.apply(config).doOnError(e -> log.error("Error occured while validating", e));
    }

    private static boolean isSymmetricAlg(final Oauth2KeyProfile.Alg alg)
    {
        return alg.equals(Oauth2KeyProfile.Alg.HS_256) || alg.equals(Oauth2KeyProfile.Alg.HS_512) || alg.equals(Oauth2KeyProfile.Alg.HS_384);
    }

    private static boolean isAsymmetricKey(final String key)
    {
        return key.contains("PUBLIC KEY"); // Asymmetric key can be identified by the keyword "PUBLIC KEY" in the pem body.
    }
}
