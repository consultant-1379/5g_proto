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
 * Created on: Jul 26, 2023
 *     Author: zpavcha
 */

package com.ericsson.esc.bsf.worker;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.jose4j.jwt.NumericDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile.Alg;
import com.ericsson.utilities.common.EnvVars;
import com.ericsson.utilities.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.AsyncCache;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.impl.jose.JWK;
import io.vertx.ext.auth.impl.jose.JWT;
import io.vertx.reactivex.ext.web.RoutingContext;

/**
 * Validates the OAuth2.0 access token for the NbsfManagement interface.
 */
public final class AuthAccessTokenValidator
{
    enum ErrorType
    {
        AUTH_HEADER_MISSING("The OAuth 2.0 authorization header is not included in the headers of the request"),
        INVALID_AUTH_HEADER("The OAuth 2.0 authorization header is invalid"),
        TOKEN_INVALID("The Oauth 2.0 access token in invalid"),
        TOKEN_NOTBEFORE("The OAuth 2.0 access token can not be processed before the not before time has passed"),
        TOKEN_EXPIRED("The OAuth 2.0 access token has expired"),
        TOKEN_NOEXPR_VALUE("The OAuth 2.0 access token has no expiration time claim value"),
        KEYID_UNKNOWN("Unknown key_id of the OAuth 2.0 access token"),
        INVALID_SIGNATURE("Invalid signature in OAuth 2.0 access token"),
        ISSUER_UNKNOWN("Unknown issuer of the OAuth 2.0 access token"),
        ISSUER_MISSING("Issuer parameter is missing from the token payload"),
        SCOPE_MISSING("Scope parameter is missing from the token payload"),
        INSUFFICIENT_SCOPE("The OAuth 2.0 access token does not have the required scope to invoke the service operation"),
        AUD_MISSING("Audience parameter is missing from the token payload"),
        INVALID_AUDIENCE("Invalid audience in OAuth 2.0 access token"),
        INVALID_NFSETID("Invalid producer nfSetId in OAuth 2.0 access token"),
        INVALID_NSILIST("Invalid producer nsiList in OAuth 2.0 access token"),
        INVALID_SNSSAILIST("Invalid producer snssaiList in OAuth 2.0 access token"),
        INVALID_PRODUCER_PLMNID("Invalid producer plmn in OAuth 2.0 access token"),
        INVALID_CONSUMER_PLMNID("Invalid consumer plmn in OAuth 2.0 access token"),
        INVALID_ALGORITHM("Algorithm of the access token is different to the one configured");

        private final String problemDetails;

        private ErrorType(final String problemDetails)
        {
            this.problemDetails = problemDetails;
        }

        final String getProblemDetails()
        {
            return this.problemDetails;
        }

    }

    private static final Logger log = LoggerFactory.getLogger(AuthAccessTokenValidator.class);
    private static final ObjectMapper om = Jackson.om();
    private static final String BEARER_REALM = "Bearer realm=";
    static final String NO_TOKEN_BEARER = BEARER_REALM.concat("\"%s\"");
    static final String INVALID_TOKEN_BEARER = BEARER_REALM.concat("\"%s\", error=\"invalid_token\"");
    static final String INVALID_SCOPE_TOKEN_BEARER = BEARER_REALM.concat("\"%s\", error=\"insufficient_scope\", scope=\"nbsf-management\"");
    private static final String NBSF_MANAGEMENT_SCOPE = "nbsf-management";
    private static final String SERIALIZATION_ERROR = "unable to serialize token with error: ";
    private static final String BSF_NF_TYPE = "BSF";
    private static final String EXPECTED_TOKEN_TYPE = "Bearer";
    private static final String KEY_TYPE_PEM = "pem";

    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private static final Base64.Decoder decoder = Base64.getUrlDecoder();

    static
    {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static final Pair<Boolean, Reason> validateToken(final RoutingContext rc,
                                                            final BsfCmConfig config,
                                                            AsyncCache<String, String> tokenCache)
    {
        log.debug("Starting the token validation process");

        final var authorizationHttpHeader = rc.request().getHeader(HttpHeaders.AUTHORIZATION);
        log.debug("oAuth token received in header request");
        if (authorizationHttpHeader == null) // tokenHeader does not exist
        {
            log.debug("OAuth token authorization http header does not exist in the HTTP request");
            final var reason = new Reason(ErrorType.AUTH_HEADER_MISSING.getProblemDetails(), NO_TOKEN_BEARER);
            return new Pair<>(Boolean.FALSE, reason);
        }

        final var headerSegments = authorizationHttpHeader.split(" ");

        final var authHeaderValidation = validateOAuthHeader(headerSegments);

        if (!authHeaderValidation.getValue0().booleanValue())
        {
            return authHeaderValidation;
        }

        final var jwt = headerSegments[1];
        final var cacheLookUp = cacheLookup(tokenCache, jwt);

        if (cacheLookUp.getValue0().booleanValue())
        {
            return new Pair<>(Boolean.TRUE, null);
        }

        else if (cacheLookUp.getValue1() != null)
        {
            return cacheLookUp;
        }

        final var jwtSegments = getSegments(jwt);

        if (jwtSegments.isEmpty())
        {
            log.debug("OAuth2 token is invalid, empty or invalid number of access token's segments");
            final var reason = new Reason(ErrorType.TOKEN_INVALID.getProblemDetails(), INVALID_TOKEN_BEARER);
            return new Pair<>(Boolean.FALSE, reason);
        }

        final var decodedHeader = getDecodedHeader(jwtSegments);

        final var extractedKid = decodedHeader.getString("kid");

        final var fetchedKeyProfileFromYang = config.getOAuthkeyProfilesMap().get(extractedKid);

        if (fetchedKeyProfileFromYang == null)
        {
            log.debug("OAuth token is invalid, oAuthKeyProfile and its keyID is not set");
            final var reason = new Reason(ErrorType.KEYID_UNKNOWN.getProblemDetails(), INVALID_TOKEN_BEARER);
            return new Pair<>(Boolean.FALSE, reason);
        }

        // fetch alg from the extractedKey
        final var fetchedAlgFromConfig = fetchedKeyProfileFromYang.getAlg();

        final var publicKeyFormatType = fetchedKeyProfileFromYang.getType().value();

        final var isPemFormat = publicKeyFormatType.equals(KEY_TYPE_PEM);

        final var jwk = isPemFormat ? convertPemToJwk(fetchedKeyProfileFromYang, fetchedAlgFromConfig) : convertKeyProfileToJwk(fetchedKeyProfileFromYang);

        if (jwk == null)
        {
            log.debug("Jwk is null");
            final var reason = new Reason(ErrorType.INVALID_SIGNATURE.getProblemDetails(), INVALID_TOKEN_BEARER);
            return new Pair<>(Boolean.FALSE, reason);
        }

        final var resValidationAlg = validateAlg(isPemFormat, jwk, decodedHeader, fetchedAlgFromConfig);
        if (!resValidationAlg.getValue0().booleanValue())
        {
            return resValidationAlg;
        }

        final var verifiedPayload = decodeToken(jwt, jwk);

        if (verifiedPayload == null)
        {
            log.debug("OAuth2 token is invalid, invalid signature");
            final var reason = new Reason(ErrorType.INVALID_SIGNATURE.getProblemDetails(), INVALID_TOKEN_BEARER);
            return new Pair<>(Boolean.FALSE, reason);
        }

        final var validateNotBefore = validateNotBefore(verifiedPayload);

        if (!validateNotBefore.getValue0().booleanValue())
        {
            log.debug("OAuth2 token is invalid current time is not after the not before time");
            return validateNotBefore;
        }

        final var mandatoryValidation = validateMandatory(config, verifiedPayload, extractedKid);
        if (!mandatoryValidation.getValue0().booleanValue())
        {
            return mandatoryValidation;
        }

        final var isOptionalValidationsEnabled = Boolean.parseBoolean(EnvVars.get("OAUTH2_VALIDATE_OPTIONAL_PARAMETERS", true));
        final var expTime = getTokenExpiration(verifiedPayload).orElseThrow(); // The orElse normally will not be executed. If the token has no exp value the
                                                                               // validation function will exit earlier with exp missing error.

        if (isOptionalValidationsEnabled)
        {
            final var optionalValidation = validateOptional(config, verifiedPayload);
            if (!optionalValidation.getValue0().booleanValue())
            {
                return optionalValidation;
            }
            tokenCache.synchronous().put(jwt, expTime);
            log.debug("Token is added in cache with expTime: {}", expTime);

            return new Pair<>(Boolean.TRUE, null);
        }
        else
        {
            tokenCache.synchronous().put(jwt, expTime);
            log.debug("Token is added in cache with expTime: {}", expTime);
            log.debug("Token validation process has been completed successfully");

            return new Pair<>(Boolean.TRUE, null);
        }

    }

    /**
     * Converts the fetched key from PEM to JWK format.
     * 
     * @param fetchedKeyProfileFromYang The fetched key profile from config
     * @param algorithm                 The algorithm to use in order to convert the
     *                                  key.
     * @return The added key as a JSON Web Key on success, otherwise null.
     */
    private static final JWK convertPemToJwk(final Oauth2KeyProfile fetchedKeyProfileFromYang,
                                             final Alg algorithm)
    {
        log.debug("This is a pem type public key");
        final var pemKey = fetchedKeyProfileFromYang.getValue(); // It is implied that value exists since the key is in
                                                                 // PEM format

        JWK key = null;

        try
        {
            final var pubKeyBuffer = Buffer.buffer(pemKey, "utf-8");
            final var options = new PubSecKeyOptions().setAlgorithm(algorithm.value()).setBuffer(pubKeyBuffer);

            key = new JWK(options);
        }
        catch (final Exception ex)
        {
            log.debug("Exception received while converting key from PEM to JWK", ex); // this should never happen
        }

        return key;
    }

    /**
     * Examines if the token has expired taking into consideration a time leeway.
     * 
     * @param accessToken The expiration value time to be examined
     * @param leeway      The time leeway in seconds
     * @return True if the expiration value has not expired, otherwise false.
     */
    private static final boolean validateExp(final String exp,
                                             final long leeway)
    {
        final var currentTime = NumericDate.now();
        final var expirationTime = NumericDate.fromSeconds(Long.parseLong(exp));

        currentTime.addSeconds(leeway);

        return currentTime.isBefore(expirationTime);
    }

    /**
     * Examines when the token should be processed taking into consideration a time
     * leeway.
     * 
     * @param accessToken The not before value time to be examined for the not
     *                    before claim
     * @param leeway      The time leeway in seconds
     * @return True if the current time is after the not-before value, otherwise
     *         false.
     * 
     */
    private static final boolean validateNbf(final String nbf,
                                             final long leeway)
    {
        final var currentTime = NumericDate.now();
        final var notBefore = NumericDate.fromSeconds(Long.parseLong(nbf));

        currentTime.addSeconds(leeway);

        return currentTime.isAfter(notBefore);
    }

    /**
     * Lookup in token payload and returns the issuer.
     * 
     * @param tokenPayload The token payload
     * @return The issuer of the access token as Optional String or an empty
     *         optional if the field does not exist.
     */
    private static final Optional<String> getTokenIssuer(final JsonObject tokenPayload)
    {
        return Optional.ofNullable(tokenPayload.getString("iss"));
    }

    /**
     * Lookup in token payload and returns the expiration time of the token.
     * 
     * @param tokenPayload The token payload
     * @return The expiration of the access token as Optional String or
     *         NullPointerException if the field does not exist.
     */
    private static final Optional<String> getTokenExpiration(final JsonObject tokenPayload)
    {
        return Optional.ofNullable(tokenPayload.getString("exp"));
    }

    /**
     * Lookup in token payload and returns the token scope.
     * 
     * @param tokenPayload The token payload
     * @return The scope of the access token as Optional String or
     *         NullPointerException if the field does not exist.
     */
    private static final Optional<String> getTokenScope(final JsonObject tokenPayload)
    {
        return Optional.ofNullable(tokenPayload.getString("scope"));
    }

    /**
     * Lookup in token payload and returns the token audience. The audience can be
     * either Array or String for the nfIntanceId or nf type respectively.
     * 
     * @param tokenPayload The token payload
     * @return The audience of the access token as Optional or NullPointerException
     *         if the field does not exist.
     */
    private static final Optional<String> getTokenAudience(final JsonObject tokenPayload)
    {
        return Optional.ofNullable(tokenPayload.getString("aud"));
    }

    /**
     * Lookup in token payload and returns the not before time of the token.
     * 
     * @param tokenPayload The token payload
     * @return The not before time of the access token as Optional String or an
     *         empty Optional if the field does not exist.
     */
    private static final Optional<String> getTokenNotBefore(final JsonObject tokenPayload)
    {
        return Optional.ofNullable(tokenPayload.getString("nbf"));
    }

    private static final Optional<PlmnId> getConsumerPlmnId(final JsonObject tokenPayload) throws JsonProcessingException
    {
        return (tokenPayload.getValue("consumerPlmnId") != null) ? Optional.of(om.readValue(tokenPayload.getValue("consumerPlmnId").toString(), PlmnId.class))
                                                                 : Optional.empty();
    }

    private static final Optional<PlmnId> getProducerPlmnId(final JsonObject tokenPayload) throws JsonProcessingException
    {
        return (tokenPayload.getValue("producerPlmnId") != null) ? Optional.of(om.readValue(tokenPayload.getValue("producerPlmnId").toString(), PlmnId.class))
                                                                 : Optional.empty();
    }

    private static final Optional<List<Snssai>> getSnnsai1List(final JsonObject tokenPayload) throws JsonProcessingException
    {
        return (tokenPayload.getValue("producerSnssaiList") != null) ? Optional.of(om.readValue(tokenPayload.getValue("producerSnssaiList").toString(),
                                                                                                new TypeReference<List<Snssai>>()
                                                                                                {
                                                                                                }))
                                                                     : Optional.empty();

    }

    private static final Optional<List<String>> getProducerNsiList(final JsonObject tokenPayload) throws JsonProcessingException
    {
        return (tokenPayload.getValue("producerNsiList") != null) ? Optional.of(om.readValue(tokenPayload.getValue("producerNsiList").toString(),
                                                                                             new TypeReference<List<String>>()
                                                                                             {
                                                                                             }))
                                                                  : Optional.empty();
    }

    private static final Optional<String> getProducerNfSetId(final JsonObject tokenPayload)
    {
        return Optional.ofNullable(tokenPayload.getString("producerNfSetId"));
    }

    /**
     * Decodes the access token and returns the header segment. Does not verify the
     * access token neither validates its contents.
     * 
     * @param accessToken The access token to be decoded
     * @return The decoded header of the access token as a JsonObject
     */
    private static final JsonObject getDecodedHeader(final Optional<Triplet<String, String, String>> accessTokenSegments)
    {
        return new JsonObject(decodeBase64Url(accessTokenSegments.orElseThrow().getValue0())); // it should never throw
    }

    /**
     * Splits the access token into segments according to the format:
     * "Header"."Payload".("Signature")
     * 
     * @param accessToken The access token to be split in segments
     * @return A String array with the segments.
     * 
     */
    private static final Optional<Triplet<String, String, String>> getSegments(final String accessToken)
    {
        try
        {
            final var segments = accessToken.split("\\.");
            return Optional.of(Triplet.fromArray(segments));
        }
        catch (final NullPointerException | IllegalArgumentException ex)
        {
            log.debug("Access token is null or does not have the required number of segments", ex);
            return Optional.empty();
        }

    }

    /**
     * Verifies the integrity of the access token for a JSON Web Key and returns the
     * decoded payload as a JsonObject.
     * 
     * @param accessToken The access token to be decoded
     * @param key         The JSON Web Key
     * @return The payload of the access token as a JsonObject if the access token
     *         is successfully verified, otherwise null.
     */
    private static final JsonObject decodeToken(final String accessToken,
                                                final JWK key)
    {
        JsonObject payload = null;

        final var tokenObject = new JWT();

        tokenObject.addJWK(key);

        payload = decode(accessToken, tokenObject);

        return payload;
    }

    /**
     * Verifies the integrity and validity of the access token against a JSON Web
     * Key and returns the decoded payload as a JsonObject.
     * 
     * @param accessToken The access token to be decoded
     * @param tokenObject A JWT object with a defined JWK
     * @return The payload of the access token as a JsonObject if the access token
     *         is successfully verified, otherwise null.
     */
    private static final JsonObject decode(final String accessToken,
                                           final JWT tokenObject)
    {
        JsonObject payload = null;

        try
        {
            // Throws Runtime Exception when:
            //
            // - Invalid number of token segments
            // - Empty signature segment when JWT is defined as Secure
            // - Not supported or mismatched signing algorithm
            // - Signature segment does not match the calculated signature for this token,
            // this signing algorithm and this JWK.
            payload = tokenObject.decode(accessToken);
        }
        catch (final RuntimeException ex)
        {
            log.debug("Exception received while decoding token", ex);
        }

        return payload;
    }

    /**
     * Decodes a Base64url-encoded String value.
     * 
     * @param value The String value to be decoded
     * @return The Base64url-decoded value
     */
    private static final String decodeBase64Url(final String value)
    {
        return new String(decoder.decode(value.getBytes(UTF8)), UTF8);
    }

    private static final JWK convertKeyProfileToJwk(final Oauth2KeyProfile fetchedKeyProfileFromYang)
    {
        JWK jwk = null;

        try
        {
            log.debug("This is a JWK type public key");
            final var jsonBody = fetchedKeyProfileFromYang.getJsonBody();

            final var json = new JsonObject(jsonBody);

            jwk = new JWK(json);
        }
        catch (final Exception ex)
        {
            log.debug("Exception received while creating JWK from JSON", ex); // this should never happen
        }

        return jwk;
    }

    private static final Pair<Boolean, Reason> validateOAuthHeader(final String[] headerSegments)
    {
        if (headerSegments.length != 2)
        {
            log.debug("Invalid Authorization header length");
            final var reason = new Reason(ErrorType.INVALID_AUTH_HEADER.getProblemDetails(), INVALID_TOKEN_BEARER);
            return new Pair<>(Boolean.FALSE, reason);
        }

        final var tokenHeaderType = headerSegments[0];

        if (!tokenHeaderType.equalsIgnoreCase(EXPECTED_TOKEN_TYPE))
        {
            log.debug("Token type is not Bearer");
            final var reason = new Reason(ErrorType.INVALID_AUTH_HEADER.getProblemDetails(), INVALID_TOKEN_BEARER);
            return new Pair<>(Boolean.FALSE, reason);
        }

        return new Pair<>(Boolean.TRUE, null);
    }

    private static final Pair<Boolean, Reason> validateAlg(final boolean isPemFormat,
                                                           final JWK jwk,
                                                           final JsonObject decodedHeader,
                                                           final Alg fetchedAlgFromConfig)
    {
        final var algFromJwt = Alg.fromValue(decodedHeader.getString("alg"));
        final var algFromJwk = Alg.fromValue(jwk.getAlgorithm());

        if ((isPemFormat && !algFromJwt.equals(fetchedAlgFromConfig)) || (!isPemFormat && !algFromJwt.equals(algFromJwk)))
        {
            log.debug("Algorithm is different from the one defined in the configured public key for oauth2 token");
            final var reason = new Reason(ErrorType.INVALID_ALGORITHM.getProblemDetails(), INVALID_TOKEN_BEARER);
            return new Pair<>(Boolean.FALSE, reason);
        }

        return new Pair<>(Boolean.TRUE, null);
    }

    private static final Pair<Boolean, Reason> validateNotBefore(final JsonObject verifiedPayload)
    {
        return getTokenNotBefore(verifiedPayload).<Pair<Boolean, Reason>>map(nbf -> validateNbf(nbf,
                                                                                                0) ? new Pair<>(Boolean.TRUE, null)
                                                                                                   : new Pair<>(Boolean.FALSE,
                                                                                                                new Reason(ErrorType.TOKEN_NOTBEFORE.getProblemDetails(),
                                                                                                                           INVALID_TOKEN_BEARER)))
                                                 .orElse(new Pair<>(Boolean.TRUE, null));
    }

    private static final Pair<Boolean, Reason> validateTokenExpiration(final JsonObject verifiedPayload)
    {
        return getTokenExpiration(verifiedPayload).<Pair<Boolean, Reason>>map(exp -> validateExp(exp,
                                                                                                 0) ? new Pair<>(Boolean.TRUE, null)
                                                                                                    : new Pair<>(Boolean.FALSE,
                                                                                                                 new Reason(ErrorType.TOKEN_EXPIRED.getProblemDetails(),
                                                                                                                            INVALID_TOKEN_BEARER)))
                                                  .orElse(new Pair<>(Boolean.FALSE,
                                                                     new Reason(ErrorType.TOKEN_NOEXPR_VALUE.getProblemDetails(), INVALID_TOKEN_BEARER)));
    }

    private static final Pair<Boolean, Reason> validateScope(final JsonObject verifiedPayload)
    {
        return getTokenScope(verifiedPayload).<Pair<Boolean, Reason>>map(scope -> scope.toLowerCase()
                                                                                       .contains(NBSF_MANAGEMENT_SCOPE) ? new Pair<>(Boolean.TRUE, null)
                                                                                                                        : new Pair<>(Boolean.FALSE,
                                                                                                                                     new Reason(ErrorType.INSUFFICIENT_SCOPE.getProblemDetails(),
                                                                                                                                                INVALID_SCOPE_TOKEN_BEARER)))
                                             .orElse(new Pair<>(Boolean.FALSE, new Reason(ErrorType.SCOPE_MISSING.getProblemDetails(), INVALID_TOKEN_BEARER)));
    }

    private static final Pair<Boolean, Reason> validateAudience(final JsonObject verifiedPayload,
                                                                final String nfIntanceId)
    {
        return getTokenAudience(verifiedPayload).<Pair<Boolean, Reason>>map(aud -> isAudienceValid(aud,
                                                                                                   nfIntanceId) ? new Pair<>(Boolean.TRUE, null)
                                                                                                                : new Pair<>(Boolean.FALSE,
                                                                                                                             new Reason(ErrorType.INVALID_AUDIENCE.getProblemDetails(),
                                                                                                                                        INVALID_TOKEN_BEARER)))
                                                .orElse(new Pair<>(Boolean.FALSE, new Reason(ErrorType.AUD_MISSING.getProblemDetails(), INVALID_TOKEN_BEARER)));
    }

    private static final Pair<Boolean, Reason> validateNrf(final BsfCmConfig config,
                                                           final String extractedKid,
                                                           final String tokenIss)
    {
        final var nrfList = config.getNrfs();

        final var matchedNrfList = nrfList.stream()
                                          .filter(nrf -> nrf.getoauth2KeyProfileRef().contains(extractedKid) && nrf.getNrfInstanceId().equals(tokenIss))
                                          .toList();

        return matchedNrfList.isEmpty() ? new Pair<>(Boolean.FALSE, new Reason(ErrorType.ISSUER_UNKNOWN.getProblemDetails(), INVALID_TOKEN_BEARER))
                                        : new Pair<>(Boolean.TRUE, null);

    }

    private static final Pair<Boolean, Reason> validateMandatory(final BsfCmConfig config,
                                                                 final JsonObject verifiedPayload,
                                                                 final String extractedKid)
    {
        final var tokenExpInvalidReason = validateTokenExpiration(verifiedPayload);
        if (!tokenExpInvalidReason.getValue0().booleanValue())
        {
            log.debug("OAuth2 token is invalid, verify token that has not been expired or token expiration claim is missing");
            return tokenExpInvalidReason;
        }

        final var tokenIss = getTokenIssuer(verifiedPayload);
        if (tokenIss.isEmpty())
        {
            log.debug("OAuth2 token is invalid, issuer token claim is missing");
            final var reason = new Reason(ErrorType.ISSUER_MISSING.getProblemDetails(), INVALID_TOKEN_BEARER);
            return new Pair<>(Boolean.FALSE, reason);
        }

        final var nrfInvalidReason = validateNrf(config, extractedKid, tokenIss.get());
        if (!nrfInvalidReason.getValue0().booleanValue())
        {
            log.debug("OAuth2 token is invalid, verify issuer");
            return nrfInvalidReason;
        }

        final var validateScope = validateScope(verifiedPayload);
        if (!validateScope.getValue0().booleanValue())
        {
            log.debug("OAuth2 token is invalid, verify scope");
            return validateScope;
        }

        final var nfInstanceId = config.getNfInstanceId();
        final var validateAudience = validateAudience(verifiedPayload, nfInstanceId);
        if (!validateAudience.getValue0().booleanValue())
        {
            log.debug("OAuth2 token is invalid, verify audience");
            return validateAudience;
        }

        return new Pair<>(true, null);
    }

    private static final Pair<Boolean, Reason> validateOptional(final BsfCmConfig config,
                                                                final JsonObject verifiedPayload)
    {
        final var validateProducerNfSetId = validateProducerNfSetId(config, verifiedPayload);
        if (!validateProducerNfSetId.getValue0().booleanValue())
        {
            log.debug("OAuth2 token is invalid, verify nfSetId");
            return validateProducerNfSetId;
        }

        final var validateProducerNsiList = validateProducerNsiList(config, verifiedPayload);
        if (!validateProducerNsiList.getValue0().booleanValue())
        {
            log.debug("OAuth2 token is invalid, verify nsiList");
            return validateProducerNsiList;
        }

        final var validateProducerSnssaiList = validateProducerSnssaiList(config, verifiedPayload);

        if (!validateProducerSnssaiList.getValue0().booleanValue())
        {
            log.debug("OAuth2 token is invalid, verify SnssaiList");
            return validateProducerSnssaiList;
        }

        final var validateProducerPlmn = validateProducerPlmn(config, verifiedPayload);
        if (!validateProducerPlmn.getValue0().booleanValue())
        {
            log.debug("OAuth2 token is invalid, verify producer plmn");
            return validateProducerPlmn;
        }

        final var validateConsnumerPlmn = validateConsumerPlmn(config, verifiedPayload);
        if (!validateConsnumerPlmn.getValue0().booleanValue())
        {
            log.debug("OAuth2 token is invalid, verify consumer plmn");
            return validateConsnumerPlmn;
        }

        return new Pair<>(Boolean.TRUE, null);
    }

    // Optional validation in case the parameter exists in the token.
    private static final Pair<Boolean, Reason> validateProducerNfSetId(final BsfCmConfig config,
                                                                       final JsonObject verifiedPayload)
    {
        final var nfSetIds = config.getNfSetId();
        final var producerNfSetId = getProducerNfSetId(verifiedPayload);

        return producerNfSetId.<Pair<Boolean, Reason>>map(setId -> nfSetIds.contains(setId) ? new Pair<>(Boolean.TRUE, null)
                                                                                            : new Pair<>(Boolean.FALSE,
                                                                                                         new Reason(ErrorType.INVALID_NFSETID.getProblemDetails(),
                                                                                                                    INVALID_TOKEN_BEARER)))
                              .orElse(new Pair<>(Boolean.TRUE, null));
    }

    private static final Pair<Boolean, Reason> validateProducerNsiList(final BsfCmConfig config,
                                                                       final JsonObject verifiedPayload)
    {
        final var nsiList = config.getNsi();

        try
        {
            final var producerNsiList = getProducerNsiList(verifiedPayload);

            return producerNsiList.<Pair<Boolean, Reason>>map(nsi -> nsiList.isEmpty()
                                                                     || nsi.stream().anyMatch(nsiList::contains)
                                                                                                                 ? new Pair<>(Boolean.TRUE, null)
                                                                                                                 : new Pair<>(Boolean.FALSE,
                                                                                                                              new Reason(ErrorType.INVALID_NSILIST.getProblemDetails(),
                                                                                                                                         INVALID_TOKEN_BEARER)))
                                  .orElse(new Pair<>(Boolean.TRUE, null));
        }
        catch (final Exception e)
        {
            log.debug(SERIALIZATION_ERROR, e);
            return new Pair<>(Boolean.FALSE, new Reason(ErrorType.INVALID_NSILIST.getProblemDetails(), INVALID_TOKEN_BEARER));
        }

    }

    private static final Pair<Boolean, Reason> validateProducerSnssaiList(final BsfCmConfig config,
                                                                          final JsonObject verifiedPayload)
    {

        final var snssaiCnal = config.getSnssai1()//
                                     .stream()
                                     .map(snssai -> new Snssai().sd(snssai.getSd()).sst(snssai.getSst()))
                                     .toList();

        try
        {
            final var producesSnssaiList = getSnnsai1List(verifiedPayload);

            return producesSnssaiList.<Pair<Boolean, Reason>>map(list -> snssaiCnal.isEmpty()
                                                                         || list.stream().anyMatch(snssaiCnal::contains)
                                                                                                                         ? new Pair<>(Boolean.TRUE, null)
                                                                                                                         : new Pair<>(Boolean.FALSE,
                                                                                                                                      new Reason(ErrorType.INVALID_SNSSAILIST.getProblemDetails(),
                                                                                                                                                 INVALID_TOKEN_BEARER)))
                                     .orElse(new Pair<>(Boolean.TRUE, null));
        }
        catch (final Exception e)
        {
            log.debug(SERIALIZATION_ERROR, e);
            return new Pair<>(Boolean.FALSE, new Reason(ErrorType.INVALID_SNSSAILIST.getProblemDetails(), INVALID_TOKEN_BEARER));
        }

    }

    private static final Pair<Boolean, Reason> validateProducerPlmn(final BsfCmConfig config,
                                                                    final JsonObject verifiedPayload)
    {
        final var plmnCnal = config.getPlmn()
                                   .stream()
                                   .map(plmn -> new com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId().mcc(plmn.getMcc()).mnc(plmn.getMnc()))
                                   .toList();

        try
        {
            final var producerPlmn = getProducerPlmnId(verifiedPayload);

            return producerPlmn.<Pair<Boolean, Reason>>map(plmnid -> plmnCnal.isEmpty()
                                                                     || plmnCnal.contains(plmnid) ? new Pair<>(Boolean.TRUE, null)
                                                                                                  : new Pair<>(Boolean.FALSE,
                                                                                                               new Reason(ErrorType.INVALID_PRODUCER_PLMNID.getProblemDetails(),
                                                                                                                          INVALID_TOKEN_BEARER)))
                               .orElse(new Pair<>(Boolean.TRUE, null));
        }
        catch (final Exception e)
        {
            log.debug(SERIALIZATION_ERROR, e);
            return new Pair<>(Boolean.FALSE, new Reason(ErrorType.INVALID_PRODUCER_PLMNID.getProblemDetails(), INVALID_TOKEN_BEARER));
        }

    }

    private static final Pair<Boolean, Reason> validateConsumerPlmn(final BsfCmConfig config,
                                                                    final JsonObject verifiedPayload)
    {
        final var allowedPlmnCnal = config.getAllowedPlmn()
                                          .stream()
                                          .map(plmn -> new com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId().mcc(plmn.getMcc()).mnc(plmn.getMnc()))
                                          .toList();
        try
        {
            final var consumerPlmn = getConsumerPlmnId(verifiedPayload);

            return consumerPlmn.<Pair<Boolean, Reason>>map(plmnid -> allowedPlmnCnal.isEmpty()
                                                                     || allowedPlmnCnal.contains(plmnid) ? new Pair<>(Boolean.TRUE, null)
                                                                                                         : new Pair<>(Boolean.FALSE,
                                                                                                                      new Reason(ErrorType.INVALID_CONSUMER_PLMNID.getProblemDetails(),
                                                                                                                                 INVALID_TOKEN_BEARER)))
                               .orElse(new Pair<>(Boolean.TRUE, null));
        }
        catch (final Exception e)
        {
            log.debug(SERIALIZATION_ERROR, e);
            return new Pair<>(Boolean.FALSE, new Reason(ErrorType.INVALID_CONSUMER_PLMNID.getProblemDetails(), INVALID_TOKEN_BEARER));
        }
    }

    private static final boolean isAudienceValid(final String aud,
                                                 final String nfintanceId)
    {
        try
        {
            final var jsonArray = new JsonArray(aud);

            return jsonArray.contains(nfintanceId);
        }
        catch (final DecodeException ex)
        {
            log.debug("Decode exception received while demarshalling audience", ex);
            return aud.equalsIgnoreCase(BSF_NF_TYPE);
        }

    }

    // 3 cases :
    // 1) found in cache and not expired.
    // 2) found in cache and expired.
    // 3 ) not found - continue validation.
    private static final Pair<Boolean, Reason> cacheLookup(AsyncCache<String, String> tokenCache,
                                                           String jwt)
    {
        final var cachedToken = tokenCache.synchronous().getIfPresent(jwt);
        if (cachedToken != null)
        {
            if (validateExp(cachedToken, 0))
            {
                log.debug("Token exists in cache with exp time {}", cachedToken);
                return new Pair<>(Boolean.TRUE, null);
            }
            else
            {
                tokenCache.asMap().remove(jwt);
                return new Pair<>(Boolean.FALSE, new Reason(ErrorType.TOKEN_EXPIRED.getProblemDetails(), INVALID_TOKEN_BEARER));
            }
        }
        else
        {
            return new Pair<>(Boolean.FALSE, null);
        }
    }

}

record Reason(String errorType,
              String headerMsg)
{
}
