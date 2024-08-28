/**
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jul 29, 2021
 *     Author: echfari
 */
package com.ericsson.utilities.file;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Flowable;

/**
 * Watches for changes in sip-tls certificates, mounted in a standard manner to
 * a POD
 */
public class SipTlsCertWatch implements KeyCertProvider, TrustedCertProvider
{
    private static final Logger log = LoggerFactory.getLogger(SipTlsCertWatch.class);
    private static final String CERT_FILENAME = "cert.pem";
    private static final String KEY_FILENAME = "key.pem";
    private static final String TRUSTED_CERT_FILENAME = "cacertbundle.pem";
    private final String root;
    private final boolean readContents;
    private final String certFileName;
    private final String keyFileName;
    private final String trustedCertFileName;

    public SipTlsCertWatch(Builder builder)
    {
        this.certFileName = builder.certFilename;
        this.keyFileName = builder.keyFileName;
        this.trustedCertFileName = builder.trustedCertFileName;
        this.root = builder.root;
        this.readContents = true;
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static KeyCertProvider keyCert(String root)
    {
        return builder().buildKeyCert(root);
    }

    public static TrustedCertProvider trustedCert(String root)
    {
        return builder().buildTrustedCert(root);
    }

    public static TrustedCertProvider combine(TrustedCertProvider... tcps)
    {
        final var flows = Arrays.stream(tcps).map(TrustedCertProvider::watchTrustedCerts).collect(Collectors.toUnmodifiableList());
        final Flowable<TrustedCert> combinedCertFlow = //
                Flowable.combineLatest(flows,
                                       tc -> () -> Arrays.stream(tc) //
                                                         .map(TrustedCert.class::cast)
                                                         .map(TrustedCert::getTrustedCertificate)
                                                         .flatMap(List::stream)
                                                         .collect(Collectors.toUnmodifiableList()));
        return () -> combinedCertFlow;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.utilities.file.KeyCertProvider#watchInternalCertificate()
     */
    @Override
    public Flowable<KeyCert> watchKeyCert()
    {
        return KubernetesFileWatch.create() //
                                  .withRoot(this.root + "/")
                                  .withFile(certFileName)
                                  .withFile(keyFileName)
                                  .build()
                                  .watch(this.readContents)
                                  .doOnSubscribe(d -> log.info("Started monitoring TLS certificate/private key on dir: {}/[{} {}]",
                                                               this.root,
                                                               certFileName,
                                                               keyFileName))
                                  .map(Cert::new)
                                  .filter(Cert::isNonEmpty)
                                  .map(KeyCert.class::cast);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ericsson.utilities.file.TrustedCertProvider#watchCa()
     */
    @Override
    public Flowable<TrustedCert> watchTrustedCerts()
    {
        return KubernetesFileWatch.create() //
                                  .withRoot(this.root + "/")
                                  .withFile(this.trustedCertFileName)
                                  .withFilterNonExistingFiles(false)
                                  .build()
                                  .watch(this.readContents)
                                  .doOnSubscribe(d -> log.info("Start monitoring TLS trusted certificate on dir: {}/{}", this.root, trustedCertFileName))
                                  .map(CaCert::new)
                                  .filter(CaCert::isNonEmpty)
                                  .map(TrustedCert.class::cast);
    }

    private class Cert implements KeyCert
    {
        private final String key;
        private final String certificate;

        Cert(Map<String, String> contents)
        {
            Objects.requireNonNull(contents);
            this.key = contents.get(keyFileName);
            this.certificate = contents.get(certFileName);
        }

        boolean isNonEmpty()
        {
            return !this.key.isBlank() && !this.certificate.isBlank();
        }

        @Override
        public String getPrivateKey()
        {
            return key;
        }

        @Override
        public String getCertificate()
        {
            return this.certificate;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(certificate, key);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Cert other = (Cert) obj;
            return Objects.equals(certificate, other.certificate) && Objects.equals(key, other.key);
        }

    }

    private class CaCert implements TrustedCert
    {
        private final String ca;

        private final List<String> trustedCerts;

        CaCert(Map<String, String> contents)
        {
            Objects.requireNonNull(contents);
            this.ca = contents.get(trustedCertFileName);
            this.trustedCerts = List.of(ca);
        }

        public boolean isNonEmpty()
        {
            return !this.ca.isBlank();
        }

        @Override
        public List<String> getTrustedCertificate()
        {
            return trustedCerts;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(ca);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CaCert other = (CaCert) obj;
            return Objects.equals(ca, other.ca);
        }
    }

    public static class Builder
    {
        private String certFilename = CERT_FILENAME;
        private String keyFileName = KEY_FILENAME;
        private String trustedCertFileName = TRUSTED_CERT_FILENAME;
        private String root;

        public Builder withRoot(String root)
        {
            this.root = root;
            return this;
        }

        public Builder withCertFileName(String certFileName)
        {
            this.certFilename = certFileName;
            return this;
        }

        public Builder withKeyFileName(String keyFileName)
        {
            this.keyFileName = keyFileName;
            return this;
        }

        public Builder withTrustedCertFileName(String trustedCertFileName)
        {
            this.trustedCertFileName = trustedCertFileName;
            return this;
        }

        public KeyCertProvider buildKeyCert(String root)
        {
            this.root = root;
            return new SipTlsCertWatch(this);
        }

        public TrustedCertProvider buildTrustedCert(String root)
        {
            this.root = root;
            return build();
        }

        public SipTlsCertWatch build()
        {
            return new SipTlsCertWatch(this);
        }
    }

}