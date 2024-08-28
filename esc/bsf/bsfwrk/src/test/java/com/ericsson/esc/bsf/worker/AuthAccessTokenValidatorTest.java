/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Aug 11, 2023
 *     Author: znpvaap
 */

package com.ericsson.esc.bsf.worker;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.javatuples.Pair;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.esc.bsf.worker.AuthAccessTokenValidator.ErrorType;
import com.ericsson.esc.jwt.AdditionalClaims;
import com.ericsson.esc.jwt.JOSEHeader;
import com.ericsson.esc.jwt.JWSPayload;
import com.ericsson.esc.jwt.JWTGenerator;
import com.ericsson.sc.bsf.model.Nrf;
import com.ericsson.sc.nfm.model.AllowedPlmn;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile.Alg;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile.Type;
import com.ericsson.sc.nfm.model.Plmn;
import com.ericsson.sc.nfm.model.Snssai1;
import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.RSAKey;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * 
 */
public class AuthAccessTokenValidatorTest
{
    private static final String BEARER_REALM = "Bearer realm=";
    private static final String NO_TOKEN_BEARER = BEARER_REALM.concat("\"%s\"");
    private static final String INVALID_TOKEN_BEARER = BEARER_REALM.concat("\"%s\", error=\"invalid_token\"");
    private static final String INVALID_SCOPE_TOKEN_BEARER = BEARER_REALM.concat("\"%s\", error=\"insufficient_scope\", scope=\"nbsf-management\"");

    private static final String BEARER_HEADER = "Bearer %s";

    private static final Logger log = LoggerFactory.getLogger(AuthAccessTokenValidator.class);
    private final RoutingContext ctx = Mockito.mock(RoutingContext.class);
    private final HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
    private final BsfCmConfig config = Mockito.mock(BsfCmConfig.class);
    private final Oauth2KeyProfile oAuthKeyProfile = Mockito.mock(Oauth2KeyProfile.class);
    private final Oauth2KeyProfile.Type oAuthKeyProfileType = Mockito.mock(Oauth2KeyProfile.Type.class);
    private final Nrf nrf = Mockito.mock(Nrf.class);
    private @NonNull AsyncCache<String, String> tokenCache;

    @BeforeClass
    private void setup() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException
    {
        setupMockitoConfigurationValues();
        this.tokenCache = Caffeine.newBuilder().maximumSize(100).buildAsync();
    }

    @AfterMethod
    private void resetAuthHeader() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException
    {
        log.info("Resetting to initial values");

        setupMockitoConfigurationValues();

        log.info("Reset has completed");
    }

    @Test(enabled = true)
    private void emptyAuthHeaderRequest()
    {
        Mockito.when(ctx.request().getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
        final var expectedReason = new Reason(ErrorType.AUTH_HEADER_MISSING.getProblemDetails(), NO_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error message has not NO_TOKEN_BEARER value");
    }

    @Test(enabled = true)
    private void invalidAuthHeaderHttpRequest() throws NoSuchAlgorithmException, NoSuchProviderException, JOSEException, ParseException, IOException, InvalidKeySpecException
    {
        final var jwt = createJwtTokenAndAuthHeader(false, 0, 2).getValue0();

        final var invalidAuthHeader = String.format("Bearer auth %s", jwt);
        Mockito.when(ctx.request().getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(invalidAuthHeader);
        final var expectedReason = new Reason(ErrorType.INVALID_AUTH_HEADER.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error message has not INVALID_TOKEN_BEARER value");
    }

    @Test(enabled = true)
    private void invalidAuthTokenBearerType() throws NoSuchAlgorithmException, NoSuchProviderException, JOSEException, ParseException, IOException, InvalidKeySpecException
    {
        final var jwt = createJwtTokenAndAuthHeader(false, 0, 2).getValue0();

        final var authHeader = String.format("Beaarer %s", jwt);
        Mockito.when(ctx.request().getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        final var expectedReason = new Reason(ErrorType.INVALID_AUTH_HEADER.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error message has not INVALID_TOKEN_BEARER value");
    }

    @Test(enabled = true)
    private void invalidAuthTokenMissing()
    {
        final var authHeader = "Bearer auth";
        Mockito.when(ctx.request().getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        final var expectedReason = new Reason(ErrorType.TOKEN_INVALID.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not TOKEN_MISSING value");
    }

    @Test(enabled = true)
    private void invalidMatchKeyIdOauthKeyProfile() throws NoSuchAlgorithmException, NoSuchProviderException, JOSEException, ParseException, IOException, InvalidKeySpecException
    {
        final var jwtKeyPair = createJwtTokenAndAuthHeader(false, 0, 2);

        final var jwt = jwtKeyPair.getValue0();
        final var publicKey = jwtKeyPair.getValue1();

        setupMockitoTokenAndPublicKey(jwt, publicKey, "2");
        Mockito.when(ctx.request().getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(String.format(BEARER_HEADER, jwt));

        final var expectedReason = new Reason(ErrorType.KEYID_UNKNOWN.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not KEYID_UNKNOWN value");
    }

    @Test(enabled = true)
    private void invalidSignatureValidationWithJwkFormat() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {
        final var jwtKeyPair = createJwtTokenAndAuthHeader(false, 0, 2);

        final var jwt = jwtKeyPair.getValue0();
        final var publicKey = jwtKeyPair.getValue1();

        setupMockitoTokenAndPublicKey(jwt, publicKey, "1");
        Mockito.when(ctx.request().getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(String.format(BEARER_HEADER, jwt));

        final var malformedJwkAsString = (ECKey) JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        Mockito.when(oAuthKeyProfile.getJsonBody()).thenReturn(malformedJwkAsString.toJSONString());
        final var expectedReason = new Reason(ErrorType.INVALID_SIGNATURE.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not INVALID_SIGNATURE value");
    }

    @Test(enabled = true)
    private void invalidSignatureValidationWithPemFormat() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final var jwtKeyPair = createJwtTokenAndAuthHeader(false, 0, 1);

        final var jwt = jwtKeyPair.getValue0();
        final var publicKey = jwtKeyPair.getValue1();

        this.setupMockitoTokenAndPublicKey(jwt, publicKey, "1");
        Mockito.when(ctx.request().getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(String.format(BEARER_HEADER, jwt));

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var pemPublicKey = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(pemPublicKey);

        final var expectedReason = new Reason(ErrorType.INVALID_SIGNATURE.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not INVALID_SIGNATURE value");
    }

    @Test(enabled = true)
    private void invalidNotBeforeTimeClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {
        final var jwtKeyPair = createJwtTokenAndAuthHeader(true, 2, 2);

        final var jwt = jwtKeyPair.getValue0();
        final var publicKey = jwtKeyPair.getValue1();

        this.setupMockitoTokenAndPublicKey(jwt, publicKey, "1");
        Mockito.when(ctx.request().getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(String.format(BEARER_HEADER, jwt));

        this.setupMockitoConfigurationValues();

        final var expectedReason = new Reason(ErrorType.TOKEN_NOTBEFORE.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not TOKEN_NOTBEFORE value");
    }

    @Test(enabled = true)
    private void invalidTokenExpWithPresentNotBeforeTimeClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {
        final var jwtKeyPair = createJwtTokenAndAuthHeader(true, -2, -4);

        final var jwt = jwtKeyPair.getValue0();
        final var publicKey = jwtKeyPair.getValue1();

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.TOKEN_EXPIRED.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not TOKEN_EXPIRED value");
    }

    @Test(enabled = true)
    private void invalidTokenExpWithNotPresentNotBeforeTimeClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {
        final var jwtKeyPair = createJwtTokenAndAuthHeader(false, 0, -2);

        final var jwt = jwtKeyPair.getValue0();
        final var publicKey = jwtKeyPair.getValue1();

        this.setupMockitoConfigurationValues();

        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.TOKEN_EXPIRED.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not TOKEN_EXPIRED value");
    }

    @Test(enabled = true)
    private void invalidTokenNoTokenExpClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withAudience("bsf-a-id")
                                                    .withIssuedAt(OffsetDateTime.now())
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.TOKEN_NOEXPR_VALUE.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not TOKEN_NOEXPR_VALUE value");
    }

    @Test(enabled = true)
    private void invalidTokenNoTokenIssuerClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withAudience("bsf-a-id")
                                                    .withIssuedAt(OffsetDateTime.now())
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.ISSUER_MISSING.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not ISSUER_MISSING value");
    }

    @Test(enabled = true)
    private void invalidTokenNoTokenAudClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.AUD_MISSING.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0());
        assertEquals(result.getValue1(), expectedReason);
    }

    @Test(enabled = true)
    private void invalidTokenAudClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        JsonArray audArray = new JsonArray();
        audArray.add("bsf-a-id-dummy");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience(audArray.toString())
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");
        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.INVALID_AUDIENCE.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0());
        assertEquals(result.getValue1(), expectedReason);
    }

    @Test(enabled = true)
    private void validTokenAudClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));

        final var nsiIdList = List.of("1234", "5678");

        JsonArray audArray = new JsonArray();
        audArray.add("bsf-a-id");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience(audArray.toString())
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");
        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0());
    }

    @Test(enabled = true)
    private void validTokenSuccessfulJwkFormat() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));

        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");
        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0());
    }

    @Test(enabled = true)
    private void invalidTokenNfSetIdTypeClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));

        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("dummy")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");
        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.INVALID_NFSETID.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0());
        assertEquals(result.getValue1(), expectedReason);
    }

    @Test(enabled = true)
    private void invalidTokenAudNfTypeClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("pcf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");
        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.INVALID_AUDIENCE.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0());
        assertEquals(result.getValue1(), expectedReason);
    }

    @Test(enabled = true)
    private void invalidTokenNsiListClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));

        final var nsiIdList = List.of("dummy1");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");
        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.INVALID_NSILIST.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0());
        assertEquals(result.getValue1(), expectedReason);
    }

    @Test(enabled = true)
    private void invalidTokenProducerPlmnClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));

        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("00").mnc("1"))
                                                                                                        .build())
                                                    .build();

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");
        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.INVALID_PRODUCER_PLMNID.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0());
        assertEquals(result.getValue1(), expectedReason);
    }

    @Test(enabled = true)
    private void invalidTokenConsumerPlmnClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));

        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("00"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");
        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.INVALID_CONSUMER_PLMNID.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0());
        assertEquals(result.getValue1(), expectedReason);
    }

    @Test(enabled = true)
    private void invalidTokenSnssaiClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("dummy"));

        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");
        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK);

        final var publicKey = ((ECKey) ecJwk).toPublicJWK().toJSONString();

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        final var expectedReason = new Reason(ErrorType.INVALID_SNSSAILIST.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0());
        assertEquals(result.getValue1(), expectedReason);
    }

    @Test(enabled = true)
    private void invalidTokenScopeClaim() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.RS256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        JsonArray audArray = new JsonArray();
        audArray.add("bsf-a-id");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience(audArray.toString())
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("diameter")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");

        final var rsaJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.RS256);
        final var rsaPublicJwk = ((RSAKey) rsaJwk).toPublicJWK().toJSONString();

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, rsaJwk, Type.JWK);

        setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), rsaPublicJwk, "1");

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);
        final var expectedReason = new Reason(ErrorType.INSUFFICIENT_SCOPE.getProblemDetails(), INVALID_SCOPE_TOKEN_BEARER);

        assertFalse(result.getValue0());
        assertEquals(result.getValue1(), expectedReason);
    }

    @Test(enabled = true)
    private void invalidTokenIssuerUnknownDiffNrfInstanceId() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {
        final var jwtKeyPair = createJwtTokenAndAuthHeader(false, 0, 2);

        final var jwt = jwtKeyPair.getValue0();
        final var publicKey = jwtKeyPair.getValue1();

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");

        final var expectedReason = new Reason(ErrorType.ISSUER_UNKNOWN.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not ISSUER_UNKNOWN value");
    }

    @Test(enabled = true)
    private void invalidTokenIssuerUnknownMatchedKeyIdNotFound() throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {
        final var jwtKeyPair = createJwtTokenAndAuthHeader(false, 0, 2);

        final var jwt = jwtKeyPair.getValue0();
        final var publicKey = jwtKeyPair.getValue1();

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), publicKey, "1");

        Mockito.when(this.nrf.getoauth2KeyProfileRef()).thenReturn(List.of("2"));
        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("");

        final var expectedReason = new Reason(ErrorType.ISSUER_UNKNOWN.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0(), "The validation is true instead of false");
        assertEquals(result.getValue1(), expectedReason, "The error type has not ISSUER_UNKNOWN value");
    }

    @Test(enabled = true)
    private void invalidAlg() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_512);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);

        final var expectedReason = new Reason(ErrorType.INVALID_ALGORITHM.getProblemDetails(), INVALID_TOKEN_BEARER);
        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertFalse(result.getValue0());
        assertEquals(result.getValue1(), expectedReason);
    }

    @Test(enabled = true)
    private void validTokenSuccessfulPemFormat() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final Reason expectedReason = null;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();

        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0(), "The validation is false instead of true");
        assertEquals(result.getValue1(), expectedReason, "The error type is not null");
    }

    @Test(enabled = true)
    private void validTokenNoSnssai() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final Reason expectedReason = null;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();
        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0(), "The validation is false instead of true");
        assertEquals(result.getValue1(), expectedReason, "The error type is not null");
    }

    @Test(enabled = true)
    private void validTokenNoNsi() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final Reason expectedReason = null;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)

                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();
        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0(), "The validation is false instead of true");
        assertEquals(result.getValue1(), expectedReason, "The error type is not null");
    }

    @Test(enabled = true)
    private void validTokenNoConsumerPlmn() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final Reason expectedReason = null;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")

                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();
        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0(), "The validation is false instead of true");
        assertEquals(result.getValue1(), expectedReason, "The error type is not null");
    }

    @Test(enabled = true)
    private void validTokenNoProducerPlmn() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final Reason expectedReason = null;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))

                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();
        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0(), "The validation is false instead of true");
        assertEquals(result.getValue1(), expectedReason, "The error type is not null");
    }

    @Test(enabled = true)
    private void validNoNfSetId() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final Reason expectedReason = null;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();
        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0(), "The validation is false instead of true");
        assertEquals(result.getValue1(), expectedReason, "The error type is not null");
    }

    @Test(enabled = true)
    private void validOptionalNsi() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final Reason expectedReason = null;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("dummy1", "dummy2");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();
        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);
        Mockito.when(this.config.getNsi()).thenReturn(List.of());

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0(), "The validation is false instead of true");
        assertEquals(result.getValue1(), expectedReason, "The error type is not null");
    }

    @Test(enabled = true)
    private void validOptionalAllowedPlmn() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final Reason expectedReason = null;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("250").mnc("300"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();
        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);
        Mockito.when(this.config.getAllowedPlmn()).thenReturn(List.of());

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0(), "The validation is false instead of true");
        assertEquals(result.getValue1(), expectedReason, "The error type is not null");
    }

    @Test(enabled = true)
    private void validOptionalPlmn() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final Reason expectedReason = null;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("150").mnc("150"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();
        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        Mockito.when(this.config.getPlmn()).thenReturn(List.of());

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0(), "The validation is false instead of true");
        assertEquals(result.getValue1(), expectedReason, "The error type is not null");
    }

    @Test(enabled = true)
    private void validOptionalSnssai() throws IOException, JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException
    {
        final Reason expectedReason = null;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(50).sd("dummy"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withIssuedAt(OffsetDateTime.now())
                                                    .withAudience("bsf")
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(2))
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withIssuer("nrf-a-id")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);
        final var ecPublicKeyPem = JWTGenerator.generatePrivatePublicKeysPemFormatFromJwk(ecJwk).getValue1();
        final var jwt = JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.PEM);

        this.setupMockitoConfigurationValues();
        this.setupMockitoTokenAndPublicKey(String.format(BEARER_HEADER, jwt), ecPublicKeyPem, "1");

        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("pem");
        Mockito.when(this.oAuthKeyProfile.getAlg()).thenReturn(Alg.ES_256);

        Mockito.when(this.oAuthKeyProfile.getValue()).thenReturn(ecPublicKeyPem);

        Mockito.when(this.config.getSnssai1()).thenReturn(List.of());

        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        keyProfileMap.put("1", this.oAuthKeyProfile);
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);

        final var result = AuthAccessTokenValidator.validateToken(ctx, config, this.tokenCache);

        assertTrue(result.getValue0(), "The validation is false instead of true");
        assertEquals(result.getValue1(), expectedReason, "The error type is not null");
    }

    private Pair<String, String> createJwtTokenAndAuthHeader(final boolean notBefore,
                                                             final long nbfValue,
                                                             final long tokenExpiration) throws JOSEException, ParseException, NoSuchAlgorithmException, NoSuchProviderException, IOException, InvalidKeySpecException
    {
        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(JWSAlgorithm.ES256)
                                                       .withType(JOSEObjectType.JWT)
                                                       .withCritical(Set.of("exp"))
                                                       .withKeyId("1")
                                                       .build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        final var payload = new JWSPayload.Builder().withAudience("bsf")
                                                    .withIssuedAt(OffsetDateTime.now())
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(tokenExpiration))
                                                    .withNotValidBefore(notBefore ? OffsetDateTime.now().plusMinutes(nbfValue) : null)
                                                    .withIssuer("nrf-a-id")
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecJwk = JWTGenerator.generateAsymmetricKeyPair(JWSAlgorithm.ES256);

        return new Pair<>(JWTGenerator.createJwt(joseHeader, payload, ecJwk, Type.JWK), ((ECKey) ecJwk).toPublicJWK().toJSONString());

    }

    private void setupMockitoConfigurationValues()
    {
        Mockito.when(this.ctx.request()).thenReturn(this.request);
        Mockito.when(this.config.getNfInstanceId()).thenReturn("bsf-a-id");
        Mockito.when(this.config.getNsi()).thenReturn(List.of("1234", "5678"));
        Mockito.when(this.config.getSnssai1())
               .thenReturn(List.of(new Snssai1().withSst(1).withSd("ABCDE1").withName("snssai1"),
                                   new Snssai1().withSst(3).withSd("ABCDE3").withName("snssai3")));
        Mockito.when(this.config.getPlmn()).thenReturn(List.of(new Plmn().withMcc("1").withMnc("1")));
        Mockito.when(this.config.getAllowedPlmn()).thenReturn(List.of(new AllowedPlmn().withMcc("2").withMnc("2")));
        Mockito.when(this.config.getNfSetId()).thenReturn(List.of("NfSetId1"));
        Mockito.when(this.config.getOAuthkeyProfiles()).thenReturn(List.of(this.oAuthKeyProfile));
        Mockito.when(this.config.getNrfs()).thenReturn(List.of(this.nrf));
        Mockito.when(this.nrf.getoauth2KeyProfileRef()).thenReturn(List.of("1"));
        Mockito.when(this.nrf.getNrfInstanceId()).thenReturn("nrf-a-id");
        Mockito.when(this.oAuthKeyProfile.getKeyId()).thenReturn("1");
        Mockito.when(this.oAuthKeyProfile.getType()).thenReturn(this.oAuthKeyProfileType);
        Mockito.when(this.oAuthKeyProfile.getType().value()).thenReturn("jwk");

    }

    private void setupMockitoTokenAndPublicKey(final String authHeader,
                                               final String jwkAsString,
                                               final String keyId)
    {
        Mockito.when(this.ctx.request().getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(authHeader);
        Mockito.when(this.oAuthKeyProfile.getJsonBody()).thenReturn(jwkAsString);
        HashMap<String, Oauth2KeyProfile> keyProfileMap = new HashMap<>();
        Mockito.when(this.config.getOAuthkeyProfilesMap()).thenReturn(keyProfileMap);
        keyProfileMap.put(keyId, this.oAuthKeyProfile);
    }

}
