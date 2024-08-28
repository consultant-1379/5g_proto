/**
 * COPYRIGHT ERICSSON GMBH 2021
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Aug 2, 2021
 *     Author: eaoknkr
 */

package com.ericsson.utilities.file;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Flowable;

/**
 * Watches different paths with mounted secrets for changes. To be used by
 * worker's sds service Each CertificateWatcher monitors a specific path (one
 * for asym keys and one for the ca). Multiple subdirectories can exist on the
 * same path, pointing different certificates in which case the name of the
 * secret queried by envoy matches the name of the subdir (e.g. worker_rlf). In
 * case only one certificate is provided under a path, a default asym key/ ca
 * name must be provided.
 * 
 *
 */

public class CertificateWatch
{
    private List<CertificateWatcher> certificateWatchers;
    private final MonitoredCertificates monitoredCertificates = new MonitoredCertificates();

    private CertificateWatch(List<CertificateWatcher> certificateWatchers)
    {
        this.certificateWatchers = certificateWatchers;
        this.certificateWatchers.forEach(watcher -> watcher.setMonitoredCertificates(monitoredCertificates));
    }

    public Flowable<MonitoredCertificates> watch()
    {
        return Flowable.merge(this.certificateWatchers.stream().map(CertificateWatcher::watch).toList());
    }

    public static Builder create()
    {
        return new Builder();
    }

    public static class CertificateWatcher
    {
        private final String certFilename;
        private final String keyFilename;
        private final String cacertbundleFilename;
        private final Optional<String> defaultAsymName;
        private final Optional<String> defaultCaName;
        private final String caRoot;
        private final String asymKeyRoot;
        private final boolean readContents;

        private Set<String> asymKeys = new HashSet<>();
        private Set<String> caCerts = new HashSet<>();

        private MonitoredCertificates monitoredCertificates;

        private CertificateWatcher(String asymKeyRoot,
                                   String caRoot,
                                   boolean readContents,
                                   Set<String> asymKeys,
                                   Set<String> caCerts,
                                   String defaultAsymName,
                                   String defaultCaName,
                                   CertNamingConvention certNamingConvention)
        {
            Objects.requireNonNull(asymKeyRoot);
            Objects.requireNonNull(caRoot);
            Objects.requireNonNull(certNamingConvention);
            this.asymKeyRoot = asymKeyRoot;
            this.caRoot = caRoot;
            this.readContents = readContents;
            this.asymKeys = asymKeys;
            this.caCerts = caCerts;
            this.defaultAsymName = Optional.ofNullable(defaultAsymName);
            this.defaultCaName = Optional.ofNullable(defaultCaName);

            if (certNamingConvention == CertNamingConvention.EXTERNAL)
            {
                this.certFilename = "certificate.pem";
                this.keyFilename = "key.pem";
                this.cacertbundleFilename = "cert1.pem";
            }
            else
            {
                this.certFilename = "cert.pem";
                this.keyFilename = "key.pem";
                this.cacertbundleFilename = "cacertbundle.pem";
            }

            if (!(this.asymKeys.isEmpty() ^ this.defaultAsymName.isEmpty()))
            {
                throw new IllegalStateException("A set of asymmetric key names or a default name should be provided");
            }

            if (!(this.caCerts.isEmpty() ^ this.defaultCaName.isEmpty()))
            {
                throw new IllegalStateException("A set of ca names or a default ca name should be provided");
            }

        }

        private void setMonitoredCertificates(MonitoredCertificates monitoredCerts)
        {
            this.monitoredCertificates = monitoredCerts;
        }

        public static Builder create()
        {
            return new Builder();
        }

        private Flowable<MonitoredCertificates> watchAsymmetricKey(String certName)
        {
            return KubernetesFileWatch.create() //
                                      .withRoot(this.asymKeyRoot + "/" + certName)
                                      .withFile(certFilename)
                                      .withFile(keyFilename)
                                      .withFilterNonExistingFiles(false)
                                      .build()
                                      .watch(this.readContents)
                                      .map(contents -> new AsymmetricKey(certName, contents, keyFilename, certFilename))
                                      .map(ak -> ak.hasContents() ? monitoredCertificates.updateAsymKeys(ak) : monitoredCertificates.removeAsymKey(ak));
        }

        private Flowable<MonitoredCertificates> watchCa(String caName)
        {
            return KubernetesFileWatch.create() //
                                      .withRoot(this.caRoot + "/" + caName)
                                      .withFile(cacertbundleFilename)
                                      .withFilterNonExistingFiles(false)
                                      .build()
                                      .watch(this.readContents)
                                      .map(contents -> new CaCert(caName, contents, cacertbundleFilename))
                                      .map(ca -> ca.hasContents() ? monitoredCertificates.updateCa(ca) : monitoredCertificates.removeCa(ca));
        }

        private Flowable<MonitoredCertificates> watchAsymmetricKey()
        {
            return KubernetesFileWatch.create() //
                                      .withRoot(this.asymKeyRoot)
                                      .withFile(certFilename)
                                      .withFile(keyFilename)
                                      .withFilterNonExistingFiles(false)
                                      .build()
                                      .watch(this.readContents)
                                      .map(contents -> new AsymmetricKey(defaultAsymName.get(), contents, keyFilename, certFilename))
                                      .map(ak -> ak.hasContents() ? monitoredCertificates.updateAsymKeys(ak) : monitoredCertificates.removeAsymKey(ak));
        }

        private Flowable<MonitoredCertificates> watchCa()
        {
            return KubernetesFileWatch.create() //
                                      .withRoot(this.caRoot)
                                      .withFile(cacertbundleFilename)
                                      .withFilterNonExistingFiles(false)
                                      .build()
                                      .watch(this.readContents)
                                      .map(contents -> new CaCert(defaultCaName.get(), contents, cacertbundleFilename))
                                      .map(ca -> ca.hasContents() ? monitoredCertificates.updateCa(ca) : monitoredCertificates.removeCa(ca));
        }

        private Flowable<MonitoredCertificates> watch()
        {
            var asymKeyMap = this.defaultAsymName.isEmpty() ? Flowable.merge(this.asymKeys.stream().map(this::watchAsymmetricKey).toList())
                                                            : this.watchAsymmetricKey();
            var caMap = this.defaultCaName.isEmpty() ? Flowable.merge(this.caCerts.stream().map(this::watchCa).toList()) : this.watchCa();

            return Flowable.merge(asymKeyMap, caMap);
        }

        public static class Builder
        {
            private Set<String> asymKeys = new HashSet<>();
            private Set<String> caCerts = new HashSet<>();
            private String defaultCaName;
            private String defaultAsymName;
            private String caRootPath;
            private String asymKeyRootPath;
            private CertNamingConvention certNamingConvention;

            public Builder withCaRoot(String caRootPath)
            {
                Objects.requireNonNull(caRootPath);
                this.caRootPath = caRootPath;
                return this;
            }

            public Builder withAsymKeyRoot(String asymKeyRootPath)
            {
                Objects.requireNonNull(asymKeyRootPath);
                this.asymKeyRootPath = asymKeyRootPath;
                return this;
            }

            public Builder withAsymmetricKeys(Set<String> asymKeys)
            {
                Objects.requireNonNull(asymKeys);
                this.asymKeys = asymKeys;
                return this;
            }

            public Builder withCaCerts(Set<String> caCerts)
            {
                Objects.requireNonNull(caCerts);
                this.caCerts = caCerts;
                return this;
            }

            public Builder withDefaultCaName(String caCertName)
            {
                Objects.requireNonNull(caCertName);
                this.defaultCaName = caCertName;
                return this;
            }

            public Builder withDefaultAsymKeyName(String asymKeyName)
            {
                Objects.requireNonNull(asymKeyName);
                this.defaultAsymName = asymKeyName;
                return this;
            }

            public Builder withCertificateNamingConvention(CertNamingConvention n)
            {
                Objects.requireNonNull(n);
                this.certNamingConvention = n;
                return this;

            }

            public CertificateWatcher build()
            {
                return new CertificateWatcher(asymKeyRootPath, caRootPath, true, asymKeys, caCerts, defaultAsymName, defaultCaName, certNamingConvention);
            }

        }

    }// CertificateWatcher END

    public static class MonitoredCertificates
    {
        private Map<String, AsymmetricKey> asymKeys = new ConcurrentHashMap<>();
        private Map<String, CaCert> caCerts = new ConcurrentHashMap<>();

        public MonitoredCertificates updateAsymKeys(AsymmetricKey ak)
        {
            this.asymKeys.put(ak.getName(), ak);
            return this;
        }

        public MonitoredCertificates updateCa(CaCert ca)
        {
            this.caCerts.put(ca.getName(), ca);
            return this;
        }

        public MonitoredCertificates removeAsymKey(AsymmetricKey ak)
        {
            this.asymKeys.remove(ak.getName());
            return this;
        }

        public MonitoredCertificates removeCa(CaCert ca)
        {
            this.caCerts.remove(ca.getName());
            return this;
        }

        public boolean isAsymmetric(String name)
        {
            return asymKeys.containsKey(name);
        }

        public boolean isCa(String name)
        {
            return caCerts.containsKey(name);
        }

        public AsymmetricKey getAsymmetricKey(String name)
        {
            return this.asymKeys.get(name);
        }

        public CaCert getCaCert(String name)
        {
            return this.caCerts.get(name);
        }

        public Map<String, AsymmetricKey> getAllAsymKeys()
        {
            return this.asymKeys;
        }

        public Map<String, CaCert> getAllCaCerts()
        {
            return this.caCerts;
        }

        public MonitoredCertificates merge(MonitoredCertificates other)
        {
            other.getAllAsymKeys().entrySet().stream().forEach(ak -> this.updateAsymKeys(ak.getValue()));
            other.getAllCaCerts().entrySet().stream().forEach(ca -> this.updateCa(ca.getValue()));

            return this;
        }

        @Override
        public String toString()
        {
            return "MonitoredCertificates [asymKeys=" + asymKeys.keySet() + ", caCerts=" + caCerts.keySet() + "]";
        }

    }

    public static class AsymmetricKey
    {
        private final String name;
        private final String key;
        private final String certificate;

        public AsymmetricKey(String name,
                             Map<String, String> contents,
                             String keyFilename,
                             String certFilename)
        {
            Objects.requireNonNull(name);
            Objects.requireNonNull(contents);
            this.name = name;
            this.key = contents.get(keyFilename);
            this.certificate = contents.get(certFilename);
        }

        public AsymmetricKey(String name,
                             String key,
                             String cert)
        {
            Objects.requireNonNull(name);
            Objects.requireNonNull(key);
            Objects.requireNonNull(cert);
            this.name = name;
            this.key = key;
            this.certificate = cert;
        }

        public String getKey()
        {
            return key;
        }

        public String getCertificate()
        {
            return this.certificate;
        }

        public String getName()
        {
            return this.name;
        }

        public boolean hasContents()
        {
            return this.key != null && !this.key.isBlank() && this.certificate != null && !this.certificate.isBlank();
        }

    }

    public static class CaCert
    {
        private final String name;
        private final String ca;

        public CaCert(String name,
                      String ca)
        {
            Objects.requireNonNull(name);
            Objects.requireNonNull(ca);
            this.name = name;
            this.ca = ca;
        }

        CaCert(String name,
               Map<String, String> contents,
               String cacertbundleFilename)
        {
            Objects.requireNonNull(name);
            Objects.requireNonNull(contents);
            this.name = name;
            this.ca = contents.get(cacertbundleFilename);
        }

        public String getCa()
        {
            return ca;
        }

        public String getName()
        {
            return this.name;
        }

        public boolean hasContents()
        {
            return this.ca != null && !this.ca.isBlank();
        }
    }

    /*
     * Depends on the names of the mounted files. Currently external certificates
     * use the older {key.pem, certificate.pem, cert1.pem} naming convention while
     * internal ones the {key.pem, cert.pem, certificatebundle.pem}
     * 
     * TODO: Make both kinds use the same naming convention
     */
    public enum CertNamingConvention
    {
        EXTERNAL,
        INTERNAL
    }

    public static class Builder
    {

        private List<CertificateWatcher> certificateWatchers = new ArrayList<>();

        public Builder withCertificateWatcher(CertificateWatcher certificateWatcher)
        {
            Objects.requireNonNull(certificateWatcher);
            this.certificateWatchers.add(certificateWatcher);
            return this;
        }

        public CertificateWatch build()
        {
            return new CertificateWatch(certificateWatchers);
        }

    }

}