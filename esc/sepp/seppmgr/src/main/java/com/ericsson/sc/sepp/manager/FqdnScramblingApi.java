package com.ericsson.sc.sepp.manager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.GCMParameterSpec;

import org.apache.commons.codec.binary.Base32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.re2j.Pattern;

public class FqdnScramblingApi
{
    private static final Logger log = LoggerFactory.getLogger(FqdnScramblingApi.class);
    private static final String AES_FLAVOR = "AES/GCM/NoPadding";
    private static final String SCRAMBLING_ALGORITHM_VERSION = "A";
    private static final int TAG_LENGTH = 32; // tag length in bits. For version "A" we agreed tag length of 4 characters
    private static final String FIVEGC = "5gc";
    private static final String PADDING_CHARACTER = "="; // Envoy deletes padding produced by base32 encoding

    private byte[] initialVector;
    private SecretKeySpec symmetricKey;
    private String keyId;
    private Cipher cipher;
    private static FqdnScramblingApi instance;

    public static FqdnScramblingApi getInstance()
    {
        if (instance == null)
        {
            instance = new FqdnScramblingApi();
        }
        return instance;
    }

    public Optional<String> scramble(String fqdnToScramble)
    {
        try
        {
            var fqdnNetworkPart = fqdnToScramble.substring(fqdnToScramble.indexOf(FIVEGC));
            fqdnToScramble = fqdnToScramble.substring(0, fqdnToScramble.indexOf("." + FIVEGC));

            var hostNames = findHostName(fqdnToScramble);
            StringBuilder scrambledResult = new StringBuilder(SCRAMBLING_ALGORITHM_VERSION + keyId);
            var base32 = new Base32();
            for (String host : hostNames)
            {
                initCipher(Cipher.ENCRYPT_MODE);
                var encryptedHost = cipher.doFinal(host.getBytes(StandardCharsets.UTF_8));
                var encodedHost = base32.encodeAsString(encryptedHost).replace(PADDING_CHARACTER, "").strip();
                scrambledResult.append(encodedHost + ".");
            }
            scrambledResult.append(fqdnNetworkPart);
            return Optional.ofNullable((scrambledResult.toString()));
        }
        catch (IllegalBlockSizeException | BadPaddingException e)
        {
            log.error("Cipher encryption failed. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));
            return Optional.empty();
        }
    }

    public Optional<String> descramble(String fqdnToDescramble)
    {
        try
        {
            var fqdnNetworkPart = fqdnToDescramble.substring(fqdnToDescramble.indexOf(FIVEGC));
            fqdnToDescramble = fqdnToDescramble.substring(fqdnToDescramble.indexOf(keyId) + keyId.length(), fqdnToDescramble.indexOf("." + FIVEGC));

            var scrambledHostNames = findHostName(fqdnToDescramble);
            StringBuilder deScrambledResult = new StringBuilder();
            var base32 = new Base32();
            for (String host : scrambledHostNames)
            {
                initCipher(Cipher.DECRYPT_MODE);
                var decryptedHost = cipher.doFinal(base32.decode(host.toUpperCase()));
                deScrambledResult.append(new String(decryptedHost) + ".");
            }
            deScrambledResult.append(fqdnNetworkPart);
            return Optional.ofNullable(deScrambledResult.toString());
        }
        catch (IllegalBlockSizeException | BadPaddingException e)
        {
            log.error("Cipher encryption failed. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));
            return Optional.empty();
        }
    }

    private void initCipher(int mode)
    {
        var bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);

        try
        {
            this.cipher = Cipher.getInstance(AES_FLAVOR, "BC");
            AlgorithmParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_LENGTH, this.initialVector);
            cipher.init(mode, this.symmetricKey, gcmParamSpec);

        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            log.error("Cipher creation failed. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));
        }
        catch (InvalidKeyException | InvalidAlgorithmParameterException e)
        {
            log.error("Cipher initialization failed. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));
        }
        catch (NoSuchProviderException e)
        {
            log.error("Invalid cipher provider. Cause: {}", com.ericsson.utilities.exceptions.Utils.toString(e, log.isDebugEnabled()));
        }
    }

    /**
     * @param fqdnToScramble
     * @return A String array of host names
     */
    private String[] findHostName(String fqdnToScramble)
    {
        fqdnToScramble = fqdnToScramble.replaceFirst("^(https?://)", "");
        var pattern = Pattern.compile("\\.");
        return pattern.split(fqdnToScramble);
    }

    /**
     * @param symmetricKey
     */
    public void setSymmetricKey(String symmetricKey)
    {
        this.symmetricKey = new SecretKeySpec(symmetricKey.getBytes(StandardCharsets.UTF_8), "AES");
    }

    /**
     * @param initialVector
     */
    public void setInitialVector(String initialVector)
    {
        this.initialVector = initialVector.getBytes(StandardCharsets.UTF_8);
    }

    public void setKeyId(String keyId)
    {
        this.keyId = keyId;
    }

    public String getKeyId()
    {
        return this.keyId;
    }

}
