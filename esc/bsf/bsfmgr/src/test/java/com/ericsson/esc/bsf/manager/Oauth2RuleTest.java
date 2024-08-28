package com.ericsson.esc.bsf.manager;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.ericsson.esc.bsf.manager.validator.BsfRule;
import com.ericsson.esc.jwt.JWTGenerator;
import com.ericsson.sc.bsf.model.EricssonBsf;
import com.ericsson.sc.bsf.model.EricssonBsfBsfFunction;
import com.ericsson.sc.bsf.model.NfInstance;
import com.ericsson.sc.bsf.model.NfManagement;
import com.ericsson.sc.bsf.model.NrfGroup;
import com.ericsson.sc.bsf.model.NrfService;
import com.ericsson.sc.bsf.model.ServiceAddress;
import com.ericsson.sc.nfm.model.NfProfile;
import com.ericsson.sc.nfm.model.NfService;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile.Alg;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile.Type;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.nfm.model.ServiceName;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.RSAKey;

import io.vertx.core.json.JsonObject;

public class Oauth2RuleTest
{
    private final NfService nfService = new NfService().withServiceInstanceId("instance1")
                                                       .withServiceAddressRef(List.of("serviceAddress1"))
                                                       .withServiceName(ServiceName.NBSF_MANAGEMENT)
                                                       .withScheme(Scheme.HTTP)
                                                       .withOauth2Required(Boolean.TRUE);

    private final NfService nfService1 = new NfService().withServiceInstanceId("instance2")
                                                        .withServiceName(ServiceName.N5G_EIR_EIC)
                                                        .withServiceAddressRef(List.of("serviceAddress1"))
                                                        .withOauth2Required(Boolean.TRUE)
                                                        .withScheme(Scheme.HTTP);

    private final NfService nfService2 = new NfService().withServiceInstanceId("instance3")
                                                        .withServiceName(ServiceName.NBSF_MANAGEMENT)
                                                        .withServiceAddressRef(List.of("serviceAddress1"))
                                                        .withOauth2Required(Boolean.FALSE)
                                                        .withScheme(Scheme.HTTPS);

    private final NfService nfService3 = new NfService().withServiceInstanceId("instance3")
                                                        .withServiceName(ServiceName.NBSF_MANAGEMENT)
                                                        .withServiceAddressRef(List.of("serviceAddress1"))
                                                        .withOauth2Required(Boolean.FALSE)
                                                        .withScheme(Scheme.HTTPS);

    private final Oauth2KeyProfile oauth2KeyProfile = new Oauth2KeyProfile().withKeyId("abcde")
                                                                            .withType(Type.JWK)
                                                                            .withJsonBody(new JsonObject().put("alg", "ES256").toString());// alg parameter
                                                                                                                                           // should be set

    private final ServiceAddress serviceAddress1 = new ServiceAddress().withName("serviceAddress1")
                                                                       .withIpv4Address("127.0.0.0")
                                                                       .withPort(5551)
                                                                       .withTlsPort(5552);

    private final ServiceAddress serviceAddress2 = new ServiceAddress().withName("serviceAddress2")
                                                                       .withIpv4Address("127.0.0.0")
                                                                       .withIpv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334")
                                                                       .withPort(5551)
                                                                       .withTlsPort(5552);

    private final NfProfile nfProfile = new NfProfile().withName("abcd")
                                                       .withServiceAddressRef("serviceAddress1")
                                                       .withNfService(List.of(nfService, nfService1, nfService2));

    private final NfProfile nfProfile1 = new NfProfile().withName("nfProfile1").withServiceAddressRef("serviceAddress1").withNfService(List.of(nfService3));
    private final NrfGroup nrfGroup = new NrfGroup().withName("nrGroup").withNfProfileRef("abcd");
    private final NrfGroup nrfGroup1 = new NrfGroup().withName("nrGroup1").withNfProfileRef("nfProfile1");

    private final NfManagement nfManagement = new NfManagement().withNrfGroupRef(List.of("nrGroup", "nrGroup1"));
    private final NrfService nrfService = new NrfService().withNfManagement(nfManagement);
    private final NfInstance nfInstance = new NfInstance().withNrfGroup(List.of(nrfGroup, nrfGroup1))
                                                          .withNrfService(nrfService)
                                                          .withServiceAddress(List.of(serviceAddress1, serviceAddress2))
                                                          .withNfProfile(List.of(nfProfile, nfProfile1))
                                                          .withOauth2KeyProfile(List.of(oauth2KeyProfile));

    private final EricssonBsfBsfFunction bsfFunc = new EricssonBsfBsfFunction().withNfInstance(List.of(nfInstance));
    private final EricssonBsf config = new EricssonBsf().withEricssonBsfBsfFunction(bsfFunc);

    @AfterMethod
    public void afterMethod()
    {
        oauth2KeyProfile.setType(Type.JWK);
    }

    @Test(enabled = true)
    public void testDefaultConfig()
    {
        var res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Default configuration under oauth2Required rule validation should be accepted");

        res = BsfRule.validateJwk(new JsonObject().put("alg", "ES256").toString());
        assertFalse(res.getResult(), "Default configuration under public key format rule rule validation should be accepted.");
        assertEquals(res.getErrorMessage(), "Invalid key in JWK format");
    }

    @Test(enabled = true)
    public void testValidOAuth2RequiredRulewithNoNfServices()
    {
        this.nfProfile.setNfService(List.of());
        this.nfProfile1.setNfService(List.of());
        var res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Configuration with no nfServices should be accepted.");

        this.nfProfile.setNfService(List.of(nfService, nfService1, nfService2));
        this.nfProfile.setNfService(List.of(nfService3));
    }

    @Test(enabled = true)
    public void testValidOAuth2RequiredRuleServiceAddressPrespective()
    {
        this.nrfGroup.setNfProfileRef("abcd");
        this.nfService1.setServiceAddressRef(List.of("serviceAddress2"));
        this.nfService.setServiceName(ServiceName.NBSF_MANAGEMENT);
        this.nrfGroup.setNfProfileRef("abcd");
        var res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(),
                   "Configuration with same oauth2required in common endpoints (combined dual stack and single stack case) should be accepetd");
        assertEquals(res.getErrorMessage(), "Not applicable");

        this.serviceAddress2.setIpv6Address(null);
        this.serviceAddress1.setIpv6Address(null);
        this.nfService.setOauth2Required(Boolean.FALSE);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Configuration with ipv6 addresses set to null should be dependent on the ipv4 + port combination");
        assertEquals(res.getErrorMessage(), "Not applicable");

        this.serviceAddress1.setPort(null);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Configuration with endpoints that missing the port field should be accepted");

        this.serviceAddress1.setPort(5551);
        this.serviceAddress2.setIpv4Address(null);
        this.serviceAddress1.setIpv4Address(null);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Configuration with ipv4 addresses set to null should be dependent on the ipv64 + port combination");
        assertEquals(res.getErrorMessage(), "Not applicable");

        this.serviceAddress2.setIpv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        this.serviceAddress1.setIpv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        final var res6 = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res6.getResult(), "Configuration with common endpoints (only ipv6 case) and same oauth2required value should be accepted");
        assertEquals(res6.getErrorMessage(), "Not applicable");

        this.serviceAddress1.setIpv4Address("127.0.0.0");
        this.serviceAddress2.setIpv4Address("127.0.0.0");
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Configuration with common endpoints (only ipv6 case) and different oauth2required value should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        this.serviceAddress1.setIpv4Address(null);
        this.serviceAddress2.setIpv6Address(null);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Configuration with nfservices with different endpoints should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        this.serviceAddress1.setIpv4Address("127.0.0.0");
        this.serviceAddress2.setIpv6Address("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        this.nfService1.setServiceAddressRef(List.of("serviceAddress1"));
        this.nfService1.setOauth2Required(Boolean.TRUE);

    }

    @Test(enabled = true)
    public void testValidOAuth2RequiredRule()
    {
        var res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Configuration with nfservices with different endpoints and different oauth2required should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        this.nfService1.setOauth2Required(Boolean.FALSE);
        this.nfService1.setServiceName(ServiceName.NBSF_MANAGEMENT);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertFalse(res.getResult(), "Configuration with nfservices with common endpoints and different oauth2required should be rejected");
        assertEquals(res.getErrorMessage(), "Invalid oauth2Required configuration");

        this.nfService1.setOauth2Required(null);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertFalse(res.getResult(), "Configuration with nfservices with common endpoints should have the same (non-null) oauth2Required value");
        assertEquals(res.getErrorMessage(), "Invalid oauth2Required configuration");

        this.nfService.setOauth2Required(null);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(config).blockingGet();
        assertTrue(res.getResult(), "Configuration with nfservices with common endpoints and same (null) oauth2required should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        this.nfService3.setOauth2Required(Boolean.TRUE);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(config).blockingGet();
        assertFalse(res.getResult(),
                    "Configuration with nfservices in different nf profiles with common endpoints and service name with different oauth2Required value should be rejected");
        assertEquals(res.getErrorMessage(), "Invalid oauth2Required configuration");

        this.nfService3.setOauth2Required(Boolean.FALSE);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(config).blockingGet();
        assertTrue(res.getResult(),
                   "Configuration with nfservices in different nf profiles with common endpoints and service name with different oauth2Required [false, null] value should be rejected");
        assertEquals(res.getErrorMessage(), "Not applicable");

        this.nfService3.setOauth2Required(Boolean.FALSE);
        this.nfService.setOauth2Required(Boolean.TRUE);
        this.nfService1.setServiceAddressRef(List.of());
        this.nfService1.setOauth2Required(Boolean.TRUE);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(),
                   "Configuration with nfservices missing service-address-ref (in this case nf-profile's service-address-ref is used) should be accepted");

        this.nrfGroup.setNfProfileRef(null);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Configuration with nf profiles that are not being referenced by an nrf group should be accepted");

        this.nrfGroup.setNfProfileRef("abcd");
        this.nfService1.setServiceName(null);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Configuration with nf services with different service names [NBSF_MANAGEMENT, null] should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        this.nfService.setServiceName(null);
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(this.config).blockingGet();
        assertTrue(res.getResult(), "Configuration with nf services that do not specify a service name should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");
    }

    @Test(enabled = true)
    public void testValidRsaPublicKeyJwkFormat() throws JOSEException
    {
        final var jwkRsaValidPublicKey = ((RSAKey) JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.RS256)).toPublicJWK();
        final var jwkRsaValidPublicKeyJson = jwkRsaValidPublicKey.toJSONString();

        final var jwkRsaInvalidJsonBody = jwkRsaValidPublicKeyJson.replaceAll("\\}", "");

        final var jwkRsaPublicParamMissing = jwkRsaValidPublicKey.toJSONObject();
        jwkRsaPublicParamMissing.remove("e");

        final var jwkRsaInvalidPublicKey = jwkRsaValidPublicKey.toJSONObject();
        jwkRsaInvalidPublicKey.remove("kty");

        final var jwkRsaInvalidAlg = jwkRsaValidPublicKey.toJSONObject();
        jwkRsaInvalidAlg.remove("alg");

        final var jwkRsaPublicKeyContainsPrivateParam = jwkRsaValidPublicKeyJson.replaceAll("\\}", " ,\"d\":\"sample\"}");

        var res = BsfRule.validateJwk(jwkRsaValidPublicKeyJson);
        assertTrue(res.getResult(), "Configuration with a valid json public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validateJwk(jwkRsaInvalidJsonBody);
        assertFalse(res.getResult(), "Configuration with a invalid json body should be rejected");
        assertEquals(res.getErrorMessage(), "Invalid json body");

        res = BsfRule.validateJwk(new JsonObject(jwkRsaInvalidPublicKey).encode());
        assertFalse(res.getResult(), "Configuration with an invalid json public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Invalid key in JWK format");

        res = BsfRule.validateJwk(new JsonObject(jwkRsaPublicParamMissing).encode());
        assertFalse(res.getResult(), "Configuration with a valid private key in jwk format should not be accepted.");
        assertEquals(res.getErrorMessage(), "Key in JWK format does not have all the required public parameters");

        res = BsfRule.validateJwk(jwkRsaPublicKeyContainsPrivateParam);
        assertFalse(res.getResult(), "Configuration with a valid json public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Key in JWK format has private key parameters");

        res = BsfRule.validateJwk(new JsonObject(jwkRsaInvalidAlg).encode());
        assertFalse(res.getResult(), "Configuration with a valid json public key format string should not be accepted");
        assertEquals(res.getErrorMessage(), "Key in JWK format does not have the alg parameter");
    }

    @Test(enabled = true)
    public void testValidRsaSsaPublicKeyJwkFormat() throws JOSEException
    {
        final var jwkRsaSsaValidPublicKey = ((RSAKey) JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.PS256)).toPublicJWK();
        final var jwkRsaSsaValidPublicKeyJson = jwkRsaSsaValidPublicKey.toJSONString();

        final var jwkRsaSsaInvalidJsonBody = jwkRsaSsaValidPublicKeyJson.replaceAll("\\}", "");

        final var jwkRsaSsaPublicParamMissing = jwkRsaSsaValidPublicKey.toJSONObject();
        jwkRsaSsaPublicParamMissing.remove("e");

        final var jwkRsaSsaInvalidPublicKey = jwkRsaSsaValidPublicKey.toJSONObject();
        jwkRsaSsaInvalidPublicKey.remove("kty");

        final var jwkRsaSsaInvalidAlg = jwkRsaSsaValidPublicKey.toJSONObject();
        jwkRsaSsaInvalidAlg.remove("alg");

        final var jwkRsaSsaPublicKeyContainsPrivateParam = jwkRsaSsaValidPublicKeyJson.replaceAll("\\}", " ,\"d\":\"sample\"}");

        var res = BsfRule.validateJwk(jwkRsaSsaValidPublicKeyJson);
        assertTrue(res.getResult(), "Configuration with a valid json public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validateJwk(jwkRsaSsaInvalidJsonBody);
        assertFalse(res.getResult(), "Configuration with a invalid json body should be rejected");
        assertEquals(res.getErrorMessage(), "Invalid json body");

        res = BsfRule.validateJwk(new JsonObject(jwkRsaSsaInvalidPublicKey).encode());
        assertFalse(res.getResult(), "Configuration with an invalid json public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Invalid key in JWK format");

        res = BsfRule.validateJwk(new JsonObject(jwkRsaSsaPublicParamMissing).encode());
        assertFalse(res.getResult(), "Configuration with a valid private key in jwk format should not be accepted.");
        assertEquals(res.getErrorMessage(), "Key in JWK format does not have all the required public parameters");

        res = BsfRule.validateJwk(jwkRsaSsaPublicKeyContainsPrivateParam);
        assertFalse(res.getResult(), "Configuration with a valid json public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Key in JWK format has private key parameters");

        res = BsfRule.validateJwk(new JsonObject(jwkRsaSsaInvalidAlg).encode());
        assertFalse(res.getResult(), "Configuration with a valid json public key format string should not be accepted");
        assertEquals(res.getErrorMessage(), "Key in JWK format does not have the alg parameter");
    }

    @Test(enabled = true)
    public void testValidEcPublicKeyJwkFormat() throws JOSEException
    {
        final var jwkEcValidPublicKey = ((ECKey) JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256)).toPublicJWK();
        final var jwkEcValidPublicKeyJson = jwkEcValidPublicKey.toJSONString();

        final var jwkEcInvalidJsonBody = jwkEcValidPublicKeyJson.replaceAll("\\}", "");

        final var jwkEcPublicParamMissing = jwkEcValidPublicKey.toJSONObject();
        jwkEcPublicParamMissing.remove("x");

        final var jwkEcInvalidPublicKey = jwkEcValidPublicKey.toJSONObject();
        jwkEcInvalidPublicKey.remove("kty");

        final var jwkEcInvalidAlg = jwkEcValidPublicKey.toJSONObject();
        jwkEcInvalidAlg.remove("alg");

        final var jwkRsaValidKey = ((RSAKey) JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.RS256));
        final var jwkRsaInValidPublicKey = jwkRsaValidKey.toPublicJWK().toJSONString().replaceAll("\\}", "");
        final var jwkRsaInValidNoAlgPublicKey = jwkRsaValidKey.toPublicJWK().toJSONObject();
        jwkRsaInValidNoAlgPublicKey.remove("alg");

        final var jwkEcPublicKeyContainsPrivateParam = jwkEcValidPublicKeyJson.replaceAll("\\}", " ,\"d\":\"33viMAhuCWXiK1-Nq-b-Qbk4tyUlo8srtiNDeMLdEZs\"}");

        final var jwkRsaMissingPublicParam = jwkRsaValidKey.toJSONObject();
        jwkRsaMissingPublicParam.remove("n");
        var res = BsfRule.validateJwk(jwkEcValidPublicKeyJson);
        assertTrue(res.getResult(), "Configuration with a valid json public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validateJwk(jwkEcInvalidJsonBody);
        assertFalse(res.getResult(), "Configuration with a invalid json body should be rejected.");
        assertEquals(res.getErrorMessage(), "Invalid json body");

        res = BsfRule.validateJwk(jwkRsaInValidPublicKey);
        assertFalse(res.getResult(), "Configuration with a invalid json body should be rejected.");
        assertEquals(res.getErrorMessage(), "Invalid json body");

        res = BsfRule.validateJwk(new JsonObject(jwkRsaInValidNoAlgPublicKey).encode());
        assertFalse(res.getResult(), "Configuration with an invalid json public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Key in JWK format does not have the alg parameter");

        res = BsfRule.validateJwk(new JsonObject(jwkRsaMissingPublicParam).encode());
        assertFalse(res.getResult(), "Configuration with a valid private key in jwk format should not be accepted.");
        assertEquals(res.getErrorMessage(), "Key in JWK format does not have all the required public parameters");

        res = BsfRule.validateJwk(jwkEcPublicKeyContainsPrivateParam);
        assertFalse(res.getResult(), "Configuration with a valid json public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Key in JWK format has private key parameters");

        res = BsfRule.validateJwk(new JsonObject(jwkEcInvalidAlg).encode());
        assertFalse(res.getResult(), "Configuration with a valid json public key format string should not be accepted");
        assertEquals(res.getErrorMessage(), "Key in JWK format does not have the alg parameter");
    }

    @Test(enabled = true)
    public void testValidHmacPublicKeyJwkFormat() throws NumberFormatException, NoSuchAlgorithmException, NoSuchProviderException, JOSEException
    {
        final var jwkHmacValidSecretKey = (JWTGenerator.generateSymmetricKey(JWSAlgorithm.HS256));
        final var jwkHmacValidSecretKeyJson = new JsonObject(jwkHmacValidSecretKey.toJSONObject());
        final var jwkHmacValidSecretKeyJsonStr = jwkHmacValidSecretKey.toJSONString();
        final var jwkHmacInvalidJsonBody = jwkHmacValidSecretKeyJsonStr.replaceAll("\\}", "");

        final var jwkHmacInvalidKey = jwkHmacValidSecretKeyJson.copy();
        jwkHmacInvalidKey.remove("kty");

        final var jwkHmacKeyParamMissing = jwkHmacValidSecretKeyJson.copy();
        jwkHmacKeyParamMissing.remove("k");

        var res = BsfRule.validateJwk(jwkHmacValidSecretKeyJson.encode());
        assertTrue(res.getResult(), "Configuration with a valid json public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validateJwk(jwkHmacInvalidJsonBody);
        assertFalse(res.getResult(), "Configuration with a invalid json body should be rejected.");
        assertEquals(res.getErrorMessage(), "Invalid json body");

        res = BsfRule.validateJwk(jwkHmacInvalidKey.encode());
        assertFalse(res.getResult(), "Configuration with an invalid json public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Invalid key in JWK format");

        res = BsfRule.validateJwk(jwkHmacKeyParamMissing.encode());
        assertFalse(res.getResult(), "Configuration with an invalid json public key format string should not be accepted");
        assertEquals(res.getErrorMessage(), "Invalid key in JWK format");

    }

    @Test(enabled = true)
    public void testValidEdDsaPublicKeyJwkFormat() throws JOSEException
    {
        final var jwkEdDsaValidPublicKey = ((OctetKeyPair) JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.EdDSA)).toPublicJWK();
        final var jwkEdDsaValidPublicKeyJson = jwkEdDsaValidPublicKey.toJSONString();

        final var jwkEdDsaInvalidJsonBody = jwkEdDsaValidPublicKeyJson.replaceAll("\\}", "");

        final var jwkEdDsaPublicParamMissing = jwkEdDsaValidPublicKey.toJSONObject();
        jwkEdDsaPublicParamMissing.remove("x");

        final var jwkEdDsaInvalidPublicKey = jwkEdDsaValidPublicKey.toJSONObject();
        jwkEdDsaInvalidPublicKey.remove("kty");

        final var jwkEdDsaInvalidAlg = jwkEdDsaValidPublicKey.toJSONObject();
        jwkEdDsaInvalidAlg.remove("alg");

        final var jwkEdDsaPublicKeyContainsPrivateParam = jwkEdDsaValidPublicKeyJson.replaceAll("\\}",
                                                                                                " ,\"d\":\"33viMAhuCWXiK1-Nq-b-Qbk4tyUlo8srtiNDeMLdEZs\"}");

        var res = BsfRule.validateJwk(jwkEdDsaValidPublicKeyJson);
        assertTrue(res.getResult(), "Configuration with a valid json public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validateJwk(jwkEdDsaInvalidJsonBody);
        assertFalse(res.getResult(), "Configuration with a invalid json body should be rejected.");
        assertEquals(res.getErrorMessage(), "Invalid json body");

        res = BsfRule.validateJwk(new JsonObject(jwkEdDsaInvalidPublicKey).encode());
        assertFalse(res.getResult(), "Configuration with an invalid json public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Invalid key in JWK format");

        res = BsfRule.validateJwk(new JsonObject(jwkEdDsaPublicParamMissing).encode());
        assertFalse(res.getResult(), "Configuration with a valid private key in jwk format should not be accepted.");
        assertEquals(res.getErrorMessage(), "Key in JWK format does not have all the required public parameters");

        res = BsfRule.validateJwk(jwkEdDsaPublicKeyContainsPrivateParam);
        assertFalse(res.getResult(), "Configuration with a valid json public key format string should not be accepted");
        assertEquals(res.getErrorMessage(), "Key in JWK format has private key parameters");

        res = BsfRule.validateJwk(new JsonObject(jwkEdDsaInvalidAlg).encode());
        assertFalse(res.getResult(), "Configuration with a valid json public key format string should not be accepted");
        assertEquals(res.getErrorMessage(), "Key in JWK format does not have the alg parameter");
    }

    // PEM format

    @Test(enabled = true)
    public void testValidSymmetricAlg() throws JOSEException, IOException, NoSuchAlgorithmException, NoSuchProviderException
    {

        final var ecPair = JWTGenerator.generateSymmetricKey(JWSAlgorithm.HS256);
        final var pem = JWTGenerator.generateSecretKeyPemFormatFromJwk(ecPair);

        var res = BsfRule.validatePem(Alg.HS_256, pem);
        assertTrue(res.getResult(), "Configuration with a valid pem public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

    }

    @Test(enabled = true)
    public void testInvalidSymmetricAlg() throws JOSEException, IOException, NoSuchAlgorithmException, NoSuchProviderException
    {

        final var rsaKey = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.RS256);
        final var rsaKeyPair = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(rsaKey);
        final var pemValidRsaPublicKey = rsaKeyPair.getValue1();

        var res = BsfRule.validatePem(Alg.HS_256, pemValidRsaPublicKey);
        assertFalse(res.getResult(), "Configuration with mismatch in symmetric alg and asymmetric value should not be accepted.");
        assertEquals(res.getErrorMessage(), "Key mismatch, symmetric alg and asymmetric key");

    }

    @Test(enabled = true)
    public void testValidRsaPublicKeyPemFormat() throws JOSEException, IOException, NoSuchAlgorithmException, NoSuchProviderException
    {
        final var pemInvalidRsaPublicKey = "-----BEGIN PUBLIC KEY-----\n" + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxPSbCQY5mBKFDIn1kggv\n"
                                           + "byaOSWdkl535rCYR5AxDSjwnuSXsSp54pvB+fEEFDPFF81GHixepIbqXCB+BnCTg\n"
                                           + "N65BqwNn/1Vgqv6+H3nweNlbTv8e/scEgbg6ZYcsnBBB9kYLp69FSwNWpvPmd60e\n"
                                           + "3DWyIo3WCUmKlQgjHL4PHLKYwwKgOHG/aNl4hN4/wqTixCAHe6KdLnehLn71x+Z0\n"
                                           + "SyXbWooftefpJP1wMbwlCpH3ikBzVIfHKLWT9QIOVoRgchPU3WAsZv/ePgl5i8Co\n" + "qwIDAQAB\n" + "-----END PUBLIC KEY-----";

        final var rsaJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.RS256);
        final var rsaKeyPair = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(rsaJwk);
        final var pemValidRsaPrivateKey = rsaKeyPair.getValue0();
        final var pemValidRsaPublicKey = rsaKeyPair.getValue1();

        var res = BsfRule.validatePem(Alg.RS_256, pemValidRsaPublicKey);
        assertTrue(res.getResult(), "Configuration with a valid pem public key format string should be accepted.");
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validatePem(Alg.RS_256, pemInvalidRsaPublicKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Invalid public key in PEM format");

        res = BsfRule.validatePem(Alg.RS_256, pemValidRsaPrivateKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Key in PEM format is not public");

    }

    @Test(enabled = true)
    public void testValidRsaSsaPublicKeyPemFormat() throws JOSEException, IOException, NoSuchAlgorithmException, NoSuchProviderException
    {
        final var pemInvalidRsaPublicKey = "-----BEGIN PUBLIC KEY-----\n" + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxPSbCQY5mBKFDIn1kggv\n"
                                           + "byaOSWdkl535rCYR5AxDSjwnuSXsSp54pvB+fEEFDPFF81GHixepIbqXCB+BnCTg\n"
                                           + "N65BqwNn/1Vgqv6+H3nweNlbTv8e/scEgbg6ZYcsnBBB9kYLp69FSwNWpvPmd60e\n"
                                           + "3DWyIo3WCUmKlQgjHL4PHLKYwwKgOHG/aNl4hN4/wqTixCAHe6KdLnehLn71x+Z0\n"
                                           + "SyXbWooftefpJP1wMbwlCpH3ikBzVIfHKLWT9QIOVoRgchPU3WAsZv/ePgl5i8Co\n" + "qwIDAQAB\n" + "-----END PUBLIC KEY-----";

        final var rsaSsaJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.PS256);
        final var rsaSsaKeyPair = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(rsaSsaJwk);
        final var pemValidRsaPrivateKey = rsaSsaKeyPair.getValue0();
        final var pemValidRsaPublicKey = rsaSsaKeyPair.getValue1();

        var res = BsfRule.validatePem(Alg.PS_256, pemValidRsaPublicKey);
        assertTrue(res.getResult(), "Configuration with a valid pem public key format string should be accepted.");
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validatePem(Alg.PS_256, pemInvalidRsaPublicKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Invalid public key in PEM format");

        res = BsfRule.validatePem(Alg.PS_256, pemValidRsaPrivateKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Key in PEM format is not public");

        oauth2KeyProfile.setJsonBody(new JsonObject().put("key", "value").toString());
    }

    @Test(enabled = true)
    public void testValidEcPublicKeyPemFormat() throws JOSEException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        oauth2KeyProfile.setType(Type.PEM);

        final var pemInvalidRsaPublicKey = "-----BEGIN PUBLIC KEY-----\n" + "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxPSbCQY5mBKFDIn1kggv\n"
                                           + "byaOSWdkl535rCYR5AxDSjwnuSXsSp54pvB+fEEFDPFF81GHixepIbqXCB+BnCTg\n"
                                           + "N65BqwNn/1Vgqv6+H3nweNlbTv8e/scEgbg6ZYcsnBBB9kYLp69FSwNWpvPmd60e\n"
                                           + "3DWyIo3WCUmKlQgjHL4PHLKYwwKgOHG/aNl4hN4/wqTixCAHe6KdLnehLn71x+Z0\n"
                                           + "SyXbWooftefpJP1wMbwlCpH3ikBzVIfHKLWT9QIOVoRgchPU3WAsZv/ePgl5i8Co\n" + "qwIDAQAB\n" + "-----END PUBLIC KEY-----";

        final var esJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var esKeyPair = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(esJwk);
        final var pemValidEsPrivateKey = esKeyPair.getValue0();
        final var pemValidEsPublicKey = esKeyPair.getValue1();

        var res = BsfRule.validatePem(Alg.ES_256, pemValidEsPublicKey);
        assertTrue(res.getResult(), "Configuration with a valid pem public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validatePem(Alg.ES_256, pemValidEsPrivateKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Key in PEM format is not public");

        res = BsfRule.validatePem(Alg.ES_256, pemInvalidRsaPublicKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Invalid public key in PEM format");

    }

    @Test(enabled = true)
    public void testValidEdDsaDPublicKeyPemFormat() throws JOSEException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final var pemInvalidEdDsaPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxPSbCQY5mBKFDIn1kggv\n"
                                             + "byaOSWdkl535rCYR5AxDSjwnuSXsSp54pvB+fEEFDPFF81GHixepIbqXCB+BnCTg\n"
                                             + "N65BqwNn/1Vgqv6+H3nweNlbTv8e/scEgbg6ZYcsnBBB9kYLp69FSwNWpvPmd60e\n"
                                             + "3DWyIo3WCUmKlQgjHL4PHLKYwwKgOHG/aNl4hN4/wqTixCAHe6KdLnehLn71x+Z0\n"
                                             + "SyXbWooftefpJP1wMbwlCpH3ikBzVIfHKLWT9QIOVoRgchPU3WAsZv/ePgl5i8Co\n" + "qwIDAQAB\n";

        final var edDsaJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.EdDSA);
        final var edDsaKeyPair = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(edDsaJwk);

        final var pemValidEdDsaPrivateKey = edDsaKeyPair.getValue0();
        final var pemValidEdDsaPublicKey = edDsaKeyPair.getValue1();

        var res = BsfRule.validatePem(Alg.ED_DSA, pemValidEdDsaPublicKey);
        assertTrue(res.getResult(), "Configuration with a valid pem public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validatePem(Alg.ED_DSA, pemValidEdDsaPrivateKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Key in PEM format is not public");

        res = BsfRule.validatePem(Alg.ED_DSA, pemInvalidEdDsaPublicKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted.");
        assertEquals(res.getErrorMessage(), "Invalid public key in PEM format");
    }

    @Test(enabled = true)
    public void testValidHmacSecretKeyPemFormat() throws JOSEException, IOException, NoSuchAlgorithmException, NoSuchProviderException, InterruptedException
    {
        final var pemInvalidHmacSecretKeyEmpty = "";

        final var hmacDsaJwk = JWTGenerator.generateSymmetricKey(JWSAlgorithm.HS256);
        final var pemValidHmacSecretKey = JWTGenerator.generateSecretKeyPemFormatFromJwk(hmacDsaJwk);

        var res = BsfRule.validatePem(Alg.HS_256, pemValidHmacSecretKey);
        assertTrue(res.getResult(), "Configuration with a valid pem public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validatePem(Alg.HS_256, pemInvalidHmacSecretKeyEmpty);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted"); // fails
        assertEquals(res.getErrorMessage(), "Invalid public key in PEM format");
    }

    @Test(enabled = true)
    public void testEmptyBsfConfig()
    {
        var emptyConfig = new EricssonBsf().withEricssonBsfBsfFunction(null);
        var res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(emptyConfig).blockingGet();
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validateKeyFormat(emptyConfig).blockingGet();
        assertEquals(res.getErrorMessage(), "Not applicable");

        emptyConfig = null;
        res = BsfRule.VALID_OAUTH2REQUIRED_RULE.validateOn(emptyConfig).blockingGet();
        assertEquals(res.getErrorMessage(), "Not applicable");

        res = BsfRule.validateKeyFormat(emptyConfig).blockingGet();
        assertEquals(res.getErrorMessage(), "Not applicable");

    }

    @Test(enabled = true)
    public void testUnsupportedAlg() throws NoSuchAlgorithmException, NoSuchProviderException, JOSEException, IOException
    {
        final var jwkUnsupportedPublicKey = """
                {"kty": "other",
                 "crv": "P-256",
                 "x": "33viMAhuCWXiK1-Nq-b-Qbk4tyUlo8srtiNDeMLdEZs",
                 "y": "xObTLxGiTYDR4KXAphno-jlYig_xIUrRm3jKGsIvmHg",
                 "alg":"ES256"}
                """;

        var res = BsfRule.validateJwk(jwkUnsupportedPublicKey);
        assertFalse(res.getResult(), "Configuration with a valid json public key format string should be accepted");
        assertEquals(res.getErrorMessage(), "Invalid key in JWK format");
    }

    @Test(enabled = true)
    public void testInvalidPemConfig() throws NoSuchAlgorithmException, NoSuchProviderException, JOSEException, IOException
    {
        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var pemValidEcPublicKey = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();

        final var rsaJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.RS256);
        final var pemValidRsaPublicKey = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(rsaJwk).getValue1();

        final var hmacJwk = JWTGenerator.generateSymmetricKey(JWSAlgorithm.HS256);
        final var pemValidHmacSecretKey = JWTGenerator.generateSecretKeyPemFormatFromJwk(hmacJwk);

        var res = BsfRule.validatePem(Alg.RS_256, pemValidEcPublicKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted");
        assertEquals(res.getErrorMessage(), "Invalid public key in PEM format");

        res = BsfRule.validatePem(Alg.RS_256, pemValidHmacSecretKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted");
        assertEquals(res.getErrorMessage(), "Invalid public key in PEM format");

        res = BsfRule.validatePem(Alg.ES_256, pemValidRsaPublicKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted");
        assertEquals(res.getErrorMessage(), "Invalid public key in PEM format");

        res = BsfRule.validatePem(Alg.ES_256, pemValidHmacSecretKey);
        assertFalse(res.getResult(), "Configuration with an invalid pem public key format string should not be accepted");
        assertEquals(res.getErrorMessage(), "Invalid public key in PEM format");

        // FIXME set alg to HS256 and value with EC key is valid from vert.x but it
        // shouldn't
    }

    @Test(enabled = true)
    public void testConfiguredNfInstanceId()
    {
        var res = BsfRule.CONFIGURED_NF_INSTANCE_RULE.validateOn(this.config).blockingGet();

        for (final var nfInstance : config.getEricssonBsfBsfFunction().getNfInstance())
        {
            final var errorMsg = String.format("Since oAuth2.0 is enabled, NF instance id must be configured for NF instance %s", nfInstance.getName());

            assertEquals(res.getErrorMessage(), errorMsg);
        }

        this.config.getEricssonBsfBsfFunction().getNfInstance().get(0).setNfInstanceId(UUID.randomUUID().toString());

        res = BsfRule.CONFIGURED_NF_INSTANCE_RULE.validateOn(this.config).blockingGet();

        assertEquals(res.getErrorMessage(), "Not applicable");
    }

}
