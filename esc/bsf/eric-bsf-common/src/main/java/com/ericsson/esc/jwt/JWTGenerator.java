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
 * Created on: Aug 25, 2023
 *     Author: znpvaap
 */

package com.ericsson.esc.jwt;

import java.io.IOException;
import java.io.StringWriter;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cnal.openapi.r17.ts29571.commondata.PlmnId;
import com.ericsson.cnal.openapi.r17.ts29571.commondata.Snssai;
import com.ericsson.sc.nfm.model.Oauth2KeyProfile;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import com.nimbusds.jose.jwk.AsymmetricJWK;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.jwk.gen.OctetSequenceKeyGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.vertx.core.json.JsonArray;

/**
 * A utility class used to create a signed JWT for OAuth 2.0. It supports the
 * following algorithms HS256, HS384, HS512, RS256, RS384, RS512, PS256, PS384,
 * PS512, ES256, ES384, ES512, ES256K, EdDSA.
 */
public class JWTGenerator
{
    private static final Logger log = LoggerFactory.getLogger(JWTGenerator.class);
    private static final String PRODUCER_SNSSAI_LIST = "producerSnssaiList";
    private static final String PRODUCER_NSI_LIST = "producerNsiList";
    private static final String PRODUCER_NF_SET_ID = "producerNfSetId";
    private static final String CONSUMER_PLMN_ID = "consumerPlmnId";
    private static final String PRODUCER_PLMN_ID = "producerPlmnId";
    private static final String SCOPE = "scope";
    private static final Base64.Decoder base64UrlDecoder = Base64.getUrlDecoder();

    private static final String PRIVATE_KEY_HEADER = "PRIVATE KEY";
    private static final String PUBLIC_KEY_HEADER = "PUBLIC KEY";

    // Asymmetric keys:

    /**
     * Generates a JWSSigner signer using an asymmetric JWK.
     * 
     * @param type The type of the key used for the signer. JWK or PEM.
     * @param jwk  The JWK.
     * 
     * @return The JWSSigner
     * @throws IOException
     * @throws JOSEException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private static JWSSigner generateAsymmetricSigner(final Oauth2KeyProfile.Type type,
                                                      final AsymmetricJWK jwk) throws JOSEException, IOException, InvalidKeySpecException, NoSuchAlgorithmException
    {
        if (jwk instanceof RSAKey rsaJwk)
        {
            return type.equals(Oauth2KeyProfile.Type.JWK) ? new RSASSASigner(rsaJwk) : new RSASSASigner(generatePemPrivateKeyFromJwk(rsaJwk));
        }
        else if (jwk instanceof ECKey ecJwk)
        {
            return switch (type)
            {
                case JWK ->
                {
                    final var ecSigner = new ECDSASigner(ecJwk);
                    ecSigner.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
                    yield ecSigner;
                }
                default ->
                {
                    final var ecSigner = new ECDSASigner(generatePemPrivateKeyFromJwk(ecJwk),
                                                         inferEcCurveForAlgorithm(JWSAlgorithm.parse(ecJwk.getAlgorithm().getName())));
                    ecSigner.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
                    yield ecSigner;
                }
            };
        }
        else if (jwk instanceof OctetKeyPair edDsaJwk)
        {
            return switch (type)
            {
                case JWK -> new Ed25519Signer(edDsaJwk);
                default -> throw new UnsupportedOperationException("Signing with EdDSA key in PEM format is not supported");
            };
        }
        else
        {
            throw new IllegalArgumentException("Invalid asymmetric key type");
        }

    }

    /**
     * Generates a private key from an asymmetric JWK.
     * 
     * @param asymmetricJwk The JWK.
     * @return A PrivateKey object.
     * @throws JOSEException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    private static PrivateKey generatePemPrivateKeyFromJwk(final AsymmetricJWK asymmetricJwk) throws JOSEException, IOException, InvalidKeySpecException, NoSuchAlgorithmException
    {
        final var jwk = (JWK) asymmetricJwk;
        final var alg = JWSAlgorithm.parse(jwk.getAlgorithm().getName());

        final var privateKeyPem = generatePrivatePublicKeysPemFormatFromJwk(asymmetricJwk).getValue0();
        return (PrivateKey) extractAsymmetricKey(privateKeyPem, inferKeyType(alg), true);
    }

    /**
     * Generates a symmetric (HMAC) key in PEM format from a JWK.
     * 
     * @param algorithm The algorithm.
     * @return The Secret key in PEM format (String).
     * @throws NumberFormatException
     */
    public static final String generateSecretKeyPemFormatFromJwk(final OctetSequenceKey secretJwk) throws NumberFormatException
    {
        return Base64.getEncoder().encodeToString(secretJwk.toByteArray());
    }

    /**
     * Creates an asymmetric JWK using the specified algorithm.
     * 
     * @param algorithm The algorithm.
     * @return The JWK.
     * @throws JOSEException
     */
    public static AsymmetricJWK generateAsymmetricKeyPair(final JWSAlgorithm algorithm) throws JOSEException
    {
        final var kty = inferKeyType(algorithm);

        return switch (kty)
        {
            case "RSA", "RSASSA" -> new RSAKeyGenerator(2048).algorithm(algorithm).generate();
            case "EC" -> new ECKeyGenerator(inferEcCurveForAlgorithm(algorithm)).algorithm(algorithm)
                                                                                .provider(BouncyCastleProviderSingleton.getInstance())
                                                                                .generate();
            case "EdDSA" -> new OctetKeyPairGenerator(Curve.Ed25519).algorithm(algorithm).generate();
            default -> throw new IllegalArgumentException("Invalid asymmetric key type");
        };
    }

    /**
     * Parses an asymmetric key in PEM format and converts it into a Key object.
     * 
     * @param key       The key in PEM format.
     * @param kty       The key type ("EC", "RSA", etc.).
     * @param isPrivate If the key is private or not.
     * @return The Key object.
     * @throws InvalidKeySpecException
     * @throws NoSuchAlgorithmException
     */
    private static Key extractAsymmetricKey(final String key,
                                            final String kty,
                                            final boolean isPrivate) throws InvalidKeySpecException, NoSuchAlgorithmException
    {
        final var header = isPrivate ? PRIVATE_KEY_HEADER : PUBLIC_KEY_HEADER;

        final var keyBody = key.replace(String.format("-----BEGIN %s-----", header), "")
                               .replace(String.format("-----END %s-----", header), "")
                               .replaceAll("\\s", "");

        final var keyBytes = Base64.getDecoder().decode(keyBody);

        final var keySpec = isPrivate ? new PKCS8EncodedKeySpec(keyBytes) : new X509EncodedKeySpec(keyBytes);

        final var keyFactory = KeyFactory.getInstance(kty);

        return isPrivate ? keyFactory.generatePrivate(keySpec) : keyFactory.generatePublic(keySpec);
    }

    /**
     * Infers the kty (key type) of the key depending on the algorithm used.
     * 
     * @param algorithm The algoritm.
     * @return The kty.
     */
    private static String inferKeyType(final JWSAlgorithm algorithm)
    {
        final var algName = algorithm.getName();

        if (algName.contains("ES"))
        {
            return "EC";
        }
        else if (algName.contains("RS") || algName.contains("PS"))
        {
            return "RSA";
        }
        else if (algName.equals("EdDSA"))
        {
            return algName;
        }
        else
        {
            throw new IllegalArgumentException("Key type not supported");
        }
    }

    /**
     * Generates private/public keys in PEM format from a JWK.
     * 
     * @param jwk The JWK.
     * @return A pair consisting of the private and the public key in PEM format
     *         (String).
     * @throws JOSEException
     * @throws IOException
     */
    public static final Pair<String, String> generatePrivatePublicKeysPemFormatFromJwk(final AsymmetricJWK jwk) throws JOSEException, IOException
    {
        return jwk instanceof OctetKeyPair octetKeyPair ? generatePrivatePublicKeysEdDsaPem(octetKeyPair)
                                                        : new Pair<>(createPem(jwk.toPrivateKey()), createPem(jwk.toPublicKey()));
    }

    // Symmetric keys:

    /**
     * Generates a JWSSigner signer using a symmetric (HMAC) JWK.
     * 
     * @param type The type of the key used for the signer. JWK or PEM.
     * @param jwk  The JWK.
     * 
     * @return The JWSSigner
     * @throws JOSEException
     */
    private static JWSSigner generateSymmetricSigner(final Oauth2KeyProfile.Type type,
                                                     final OctetSequenceKey hmacJwk) throws JOSEException
    {
        return switch (type)
        {
            case JWK ->
            {
                final var hmacSigner = new MACSigner(hmacJwk);
                hmacSigner.getJCAContext().setProvider(BouncyCastleProviderSingleton.getInstance());
                yield hmacSigner;
            }
            default ->
            {
                final var secretKeyPem = generateSecretKeyPemFormatFromJwk(hmacJwk);
                yield new MACSigner(secretKeyPem);
            }
        };
    }

    /**
     * Generates a symmetric key in JWK format using the specified algorithm.
     * 
     * @param algorithm The algorithm.
     * @return a Secret key in JWK format.
     * @throws NumberFormatException
     * @throws JOSEException
     */
    public static final OctetSequenceKey generateSymmetricKey(final JWSAlgorithm algorithm) throws NumberFormatException, JOSEException
    {
        return new OctetSequenceKeyGenerator(Integer.parseInt(algorithm.getName().replace("HS", ""))).algorithm(algorithm).generate();
    }

    // Tokens:

    /**
     * Generates a JWT token using an asymmetric key as per RFC-7519
     * 
     * @param joseHeader The JOSE header of the token
     * @param payload    The JWT payload which consists of the token claims.
     * @param jwk        The asymmetric JWK that will be used to sign the newly
     *                   created token.
     * @param type       The type of the key to be used for signing. JWK or PEM.
     * 
     * @return The created token in string format.
     * @throws JOSEException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static final String createJwt(final JOSEHeader joseHeader,
                                         final JWSPayload payload,
                                         final AsymmetricJWK jwk,
                                         final Oauth2KeyProfile.Type type) throws JOSEException, InvalidKeySpecException, NoSuchAlgorithmException, IOException
    {
        final var signedJwt = createToBeSignedJwt(joseHeader, payload);

        final var signer = generateAsymmetricSigner(type, jwk);

        // Compute the signature
        signedJwt.sign(signer);

        // Serialize the JWS to compact form
        final var jwtRfcFormat = signedJwt.serialize();

        log.info("Created JWT: {}", jwtRfcFormat);

        return jwtRfcFormat;
    }

    /**
     * Generates a JWT token using a symmetric key as per RFC-7519
     * 
     * @param joseHeader The JOSE header of the token
     * @param payload    The JWT payload which consists of the token claims.
     * @param jwk        The symmetric JWK (HMAC) that will be used to sign the
     *                   newly created token.
     * @param type       The type of the key to be used for signing. JWK or PEM.
     * 
     * @return The created token in string format.
     * @throws JOSEException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public static final String createJwt(final JOSEHeader joseHeader,
                                         final JWSPayload payload,
                                         final OctetSequenceKey jwk,
                                         final Oauth2KeyProfile.Type type) throws JOSEException
    {
        final var signedJWT = createToBeSignedJwt(joseHeader, payload);

        final var signer = generateSymmetricSigner(type, jwk);

        // Compute the signature
        signedJWT.sign(signer);

        // Serialize the JWS to compact form
        final var jwtRfcFormat = signedJWT.serialize();

        log.info("Created JWT: {}", jwtRfcFormat);

        return jwtRfcFormat;
    }

    /**
     * Creates a new to-be-signed JWT using the specified JOSE Header and JWT
     * Payload.
     * 
     * @param joseHeader The joseHeader.
     * @param payload    The payload.
     * @return A SignedJWT object.
     */
    private static SignedJWT createToBeSignedJwt(final JOSEHeader joseHeader,
                                                 final JWSPayload payload)
    {
        final var joseNimbusHeader = new JWSHeader.Builder(joseHeader.getAlgorithm()).keyID(joseHeader.getKeyId())
                                                                                     .type(joseHeader.getType())
                                                                                     .criticalParams(joseHeader.getCritical())
                                                                                     .build();

        log.info("The JOSE Header: {}", joseNimbusHeader);

        final var issueTime = Objects.isNull(payload.getIssuedAt()) ? null : Date.from(payload.getIssuedAt().toInstant());
        final var expirationTime = Objects.isNull(payload.getExpirationTime()) ? null : Date.from(payload.getExpirationTime().toInstant());
        final var notValidBeforeTime = Objects.isNull(payload.getNotValidBefore()) ? null : Date.from(payload.getNotValidBefore().toInstant());

        List<Snssai> producerSnssaiList = null;
        List<String> producerNsiList = null;
        String producerNfSetId = null;
        PlmnId consumerPlmnId = null;
        PlmnId producerPlmnId = null;
        if (payload.getAdditionalClaims() != null)
        {
            producerSnssaiList = payload.getAdditionalClaims().getProducerSnssaiList();
            producerNsiList = payload.getAdditionalClaims().getProducerNsiList();
            producerNfSetId = payload.getAdditionalClaims().getProducerNfSetId();
            consumerPlmnId = payload.getAdditionalClaims().getConsumerPlmnId();
            producerPlmnId = payload.getAdditionalClaims().getProducerPlmnId();
        }

        final var claimsSet = new JWTClaimsSet.Builder().audience(payload.getAudience())
                                                        .issueTime(issueTime)
                                                        .expirationTime(expirationTime)
                                                        .notBeforeTime(notValidBeforeTime)
                                                        .jwtID(payload.getJwtId())
                                                        .issuer(payload.getIssuer())
                                                        .claim(SCOPE, payload.getScope())
                                                        .claim(PRODUCER_SNSSAI_LIST, producerSnssaiList)
                                                        .claim(PRODUCER_NSI_LIST, producerNsiList)
                                                        .claim(PRODUCER_NF_SET_ID, producerNfSetId)
                                                        .claim(CONSUMER_PLMN_ID, consumerPlmnId)
                                                        .claim(PRODUCER_PLMN_ID, producerPlmnId)
                                                        .build();

        log.info("The JWT Payload: {}", claimsSet);

        return new SignedJWT(joseNimbusHeader, claimsSet);
    }

    // Misc:

    /**
     * Generates private/public keys in PEM format using the EdDSA algorithm.
     * 
     * @param octetKeyPair The octetKeyPair of the EdDSA algorithm.
     * @return A pair consisting of the private and the public key in PEM format
     *         (String).
     * @throws IOException
     */
    private static Pair<String, String> generatePrivatePublicKeysEdDsaPem(final OctetKeyPair octetKeyPair) throws IOException
    {
        final var privateKey = new Ed25519PrivateKeyParameters(base64UrlDecoder.decode(octetKeyPair.getD().toString()));
        final var publicKey = new Ed25519PublicKeyParameters(base64UrlDecoder.decode(octetKeyPair.getX().toString()));

        final var privateKeyInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(privateKey);

        final var publicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(publicKey);

        final var base64encodedPrivateKey = Base64.getEncoder().encodeToString(privateKeyInfo.getEncoded());
        final var base64encodedPublicKey = Base64.getEncoder().encodeToString(publicKeyInfo.getEncoded());

        return new Pair<>(convertToPem(base64encodedPrivateKey, PRIVATE_KEY_HEADER), convertToPem(base64encodedPublicKey, PUBLIC_KEY_HEADER));
    }

    /**
     * Infers the Curve depending on the EC algorithm used.
     * 
     * @param algorithm The EC algorithm.
     * @return The Curve.
     */
    private static Curve inferEcCurveForAlgorithm(final JWSAlgorithm algorithm)
    {
        if (algorithm.equals(JWSAlgorithm.ES256))
        {
            return Curve.P_256;
        }
        else if (algorithm.equals(JWSAlgorithm.ES256K))
        {
            return Curve.SECP256K1;
        }
        else if (algorithm.equals(JWSAlgorithm.ES384))
        {
            return Curve.P_384;
        }
        else if (algorithm.equals(JWSAlgorithm.ES512))
        {
            return Curve.P_521;
        }
        else
        {
            throw new IllegalArgumentException("Invalid EC JWSAlgorithm: " + algorithm);
        }
    }

    /**
     * Creates a private or public key in PEM format from a specified Key object.
     * 
     * @param key The key
     * @return The key in PEM format (String).
     */
    private static String createPem(final Key key)
    {
        final var stringWriter = new StringWriter();

        var keyInPemFormat = "";

        try (final var pemWriter = new JcaPEMWriter(stringWriter))
        {
            pemWriter.writeObject(key instanceof PrivateKey privateKey ? new JcaPKCS8Generator(privateKey, null) : key);
            pemWriter.flush();
            keyInPemFormat = stringWriter.toString();
        }
        catch (final IOException ex)
        {
            log.error("Exception received while creating PEM", ex);
        }

        return keyInPemFormat;
    }

    /**
     * Creates a PEM key from a specified base64 encoded body.
     * 
     * @param encodedValue The encodedValue to be used as the key body.
     * @param header       The key header. For instance, "PUBLIC KEY".
     * @return The key in PEM format (String).
     */
    private static String convertToPem(final String encodedValue,
                                       final String header)
    {
        return new StringBuilder().append("-----BEGIN ")
                                  .append(header)
                                  .append("-----\n")
                                  .append(encodedValue)
                                  .append("\n")
                                  .append("-----END ")
                                  .append(header)
                                  .append("-----")
                                  .toString();
    }

    public static void main(String[] args) throws ParseException, JOSEException, InvalidKeySpecException, NoSuchAlgorithmException, IOException
    {
        final var alg = JWSAlgorithm.PS256;

        final var joseHeader = new JOSEHeader.Builder().withAlgorithm(alg).withType(JOSEObjectType.JWT).withCritical(Set.of("exp")).withKeyId("1").build();

        final var snssaiList = List.of(new Snssai().sst(1).sd("ABCDE1"), new Snssai().sst(3).sd("ABCDE3"));
        final var nsiIdList = List.of("1234", "5678");

        JsonArray audArray = new JsonArray();
        audArray.add("441a1afd-eb6e-4e1b-88b4-dc7f5915464e");

        final var payload = new JWSPayload.Builder().withAudience(audArray.toString())
                                                    .withIssuedAt(OffsetDateTime.now())
                                                    .withExpirationTime(OffsetDateTime.now().plusHours(100))
                                                    .withNotValidBefore(OffsetDateTime.now().plusNanos(10))
                                                    .withIssuer("8272a8a9-5f9d-4475-acf2-d43f6ab55627")
                                                    .withScope("nbsf-management")
                                                    .withJwtId("jwt-uuid")
                                                    .withAdditionalClaims(new AdditionalClaims.Builder().withProducerSnssaiList(snssaiList)
                                                                                                        .withProducerNsiList(nsiIdList)
                                                                                                        .withProducerNfSetId("NfSetId1")
                                                                                                        .withConsumerPlmnId(new PlmnId().mcc("2").mnc("2"))
                                                                                                        .withProducerPlmnId(new PlmnId().mcc("1").mnc("1"))
                                                                                                        .build())
                                                    .build();

        final var ecPair = generateAsymmetricKeyPair(JWSAlgorithm.PS256);

        final var jwt = createJwt(joseHeader, payload, ecPair, Oauth2KeyProfile.Type.PEM);

        final var signedJWT = SignedJWT.parse(jwt);

        log.info("The JWT claims are: {}", signedJWT.getJWTClaimsSet());

        log.info("The public key Parameter for the EC alg is: {}", ecPair);
    }
}
