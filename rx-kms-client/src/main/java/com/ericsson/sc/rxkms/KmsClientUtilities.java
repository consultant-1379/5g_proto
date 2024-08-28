package com.ericsson.sc.rxkms;

import java.util.HashMap;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import io.reactivex.Completable;
import io.reactivex.Single;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KmsClientUtilities
{
    private static final String DEFAULT_ROLE = "";
    private static final Logger log = LoggerFactory.getLogger(KmsClientUtilities.class);

    private KmsClient client;
    private HashMap<String, String> lookupTableDec;
    private HashMap<String, String> lookupTableEnc;

    private static KmsClientUtilities utilities;

    public static KmsClientUtilities get()
    {
        return KmsClientUtilities.get(KmsParameters.instance, DEFAULT_ROLE);
    }

    public static KmsClientUtilities get(KmsParameters params,
                                         String role)
    {
        if (KmsClientUtilities.utilities == null || KmsClientUtilities.utilities.client == null)
            KmsClientUtilities.utilities = new KmsClientUtilities(params, role);
        if (!KmsClientUtilities.utilities.client.hasStarted())
        {
            log.debug("Starting KMS client");
            KmsClientUtilities.utilities.client.start();
        }
        return KmsClientUtilities.utilities;
    }

    private KmsClientUtilities(KmsParameters params,
                               String role)
    {
        this.client = new KmsClient(params, role);
        this.client.start();
        this.lookupTableDec = new HashMap<>();
        this.lookupTableEnc = new HashMap<>();
    }

    public void dispose()
    {
        this.client.stop();
        this.client = null;
    }

    public Completable getReady()
    {
        return this.client.waitReady();
    }

    public Single<Optional<String>> decrypt(String plainText)
    {
        return this.decrypt(plainText, false);
    }

    public Single<Optional<String>> decrypt(String plainText,
                                            boolean cache)
    {
        if (cache && lookupTableDec.containsKey(plainText))
        {
            log.debug("Data were available in cache");
            return Single.<Optional<String>>just(Optional.<String>of(lookupTableDec.get(plainText)));
        }

        return this.client.decryptPlainText(plainText).map(response ->
        {
            if (response.statusCode() == 200)
            {
                final var jsonBody = response.bodyAsJsonObject();
                final var jsonData = jsonBody.getJsonObject("data");
                String plaintext = jsonData.getString("plaintext");

                if (plaintext == null)
                {
                    return Optional.<String>ofNullable(null);
                }

                final String decryptedText = jsonData == null ? null : new String(Base64.getDecoder().decode(plaintext.getBytes()), StandardCharsets.UTF_8);
                if (cache)
                {
                    this.lookupTableDec.put(plainText, decryptedText);
                }
                return Optional.<String>ofNullable(decryptedText == null ? null : decryptedText);
            }
            else
            {
                throw new KmsException("Failed to decrypt data", response.statusCode(), response.statusMessage());
            }
        }).doOnError(err ->
        {
            log.debug("Failed to decrypt", err);
            this.client.restartLogin();
        }).doOnSuccess(resp ->
        {
            log.debug("Successfully decrypted data");
            log.debug("Data not in cache");
        });
    }

    public Optional<String> decryptBlocking(String plainText)
    {
        return this.decrypt(plainText).blockingGet();
    }

    public Single<Optional<String>> encrypt(String base64Text)
    {
        return this.encrypt(base64Text, false);
    }

    public Single<Optional<String>> encrypt(String base64Text,
                                            boolean cache)
    {
        if (cache && lookupTableEnc.containsKey(base64Text))
        {
            log.debug("Data were available in cache");
            return Single.<Optional<String>>just(Optional.<String>of(lookupTableEnc.get(base64Text)));
        }

        if (!this.client.isReady())
        {
            log.debug("Client not ready");
            return Single.<Optional<String>>error(new KmsException("Failed to encrypt data, Client not ready"));
        }

        return this.client.encryptBase64(base64Text).map(response ->
        {
            switch (response.statusCode())
            {
                case 200:
                    // Configuration exists
                    final var jsonBody = response.bodyAsJsonObject();
                    final var jsonData = jsonBody.getJsonObject("data");

                    final String encryptedText = jsonData == null ? null : jsonData.getString("ciphertext");
                    if (cache)
                    {
                        this.lookupTableEnc.put(base64Text, encryptedText);
                    }
                    return Optional.<String>ofNullable(encryptedText == null ? null : encryptedText);
                case 403:
                    throw new KmsException("Failed to encrypt data, Forbidden", response.statusCode(), response.statusMessage());
                default:
                    throw new KmsException("Failed to encrypt data", response.statusCode(), response.statusMessage());
            }
        }).doOnError(err ->
        {
            log.debug("Failed to encrypt", err);
            this.client.restartLogin();
        }).doOnSuccess(resp ->
        {
            log.debug("Successfully encrypted data");
            log.debug("Data not in cache");
        });
    }

    public Optional<String> encryptBase64Blocking(String base64Text)
    {
        return this.encrypt(base64Text).blockingGet();
    }
}
