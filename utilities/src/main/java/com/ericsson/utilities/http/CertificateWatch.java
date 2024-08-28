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
 * Created on: Oct 7, 2020
 *     Author: echfari
 */
package com.ericsson.utilities.http;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.common.Pair;
import com.ericsson.utilities.file.KeyCert;
import com.ericsson.utilities.file.RxFileWatch;
import com.ericsson.utilities.file.TrustedCert;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@Deprecated
public class CertificateWatch
{
    private static final Logger log = LoggerFactory.getLogger(CertificateWatch.class);

    private final Path certificateRoot;
    private final CertPaths paths;
    private Flowable<Optional<CertPaths>> watch;

    public CertificateWatch(String root,
                            List<String> caPaths)
    {
        this.certificateRoot = Path.of(root);
        List<Path> caPathsList = caPaths.stream().map(certificateRoot::resolve).collect(Collectors.toList());
        this.paths = new CertPaths(certificateRoot.resolve("certificate.pem"), // cert
                                   certificateRoot.resolve("key.pem"), // key
                                   caPathsList); // ca

        this.watch = RxFileWatch.create(certificateRoot) //
                                .debounce(1, TimeUnit.SECONDS) //
                                .observeOn(Schedulers.io()) // File validation is blocking operation so we need to change scheduler
                                .flatMapSingle(ev -> validatePaths())
                                .doOnError(err -> log.warn("Error validating certificate paths", err))
                                .doOnNext(ev -> log.info("Certificate change event: {}", ev));
    }

    public CertificateWatch(String root,
                            String caPath)
    {
        this(root, List.of(caPath));
    }

    public CertificateWatch(String root)
    {
        this(root, List.of("trustCA/cert1.pem"));
    }

    public Flowable<Optional<CertPaths>> getEvents()
    {
        return this.watch;
    }

    private static class KeyCertImpl implements KeyCert, TrustedCert
    {

        String pk;
        String cert;
        List<String> trustedCert;

        @Override
        public String getPrivateKey()
        {
            return pk;
        }

        @Override
        public String getCertificate()
        {
            return cert;
        }

        @Override
        public List<String> getTrustedCertificate()
        {
            return trustedCert;
        }

    }

    public Pair<Flowable<KeyCert>, Flowable<TrustedCert>> getCertificates()
    {
        final var combinedFlow = getEvents().filter(Optional::isPresent).map(Optional::get).map(ev ->
        {
            final var result = new KeyCertImpl();
            result.pk = Files.readString(ev.getKeyPath());
            result.cert = Files.readString(ev.getCertPath());

            final var caList = ev.getCaPathList();
            if (!caList.isEmpty())
            {
                result.trustedCert = new ArrayList<>();
                for (var ca : caList)
                {
                    result.trustedCert.add(Files.readString(ca));
                }
            }

            return result;
        }).share();
        if (this.paths.caPathList.isEmpty())
        {
            return Pair.of(combinedFlow.map(KeyCert.class::cast), null);
        }
        else
        {
            return Pair.of(combinedFlow.map(KeyCert.class::cast), combinedFlow.map(TrustedCert.class::cast));
        }
    }

    public CertPaths paths()
    {
        return this.paths;
    }

    public static Flowable<Optional<CertPaths>> create(String certDir)
    {
        return new CertificateWatch(certDir, List.of("trustCA/cert1.pem")).watch;
    }

    private Single<Optional<CertPaths>> validatePaths()
    {
        return Single.fromCallable(() -> //

        realPath(this.paths.getCertPath()).flatMap(certPath -> //
        realPath(this.paths.getKeyPath()).flatMap(keyPath -> //
        realCaPath(this.paths.getCaPathList()).map(caPathList -> //
        new CertPaths(certPath, keyPath, caPathList)))))//

                     .subscribeOn(Schedulers.io()); // Blocking operation
    }

    private Optional<Path> realPath(Path path)
    {
        // Warning: Blocking operations
        try
        {
            log.debug("Validating path: {}", path);
            final var realPath = path.toRealPath();
            final var length = realPath.toFile().length();
            log.debug("{} -> length: {}", path, length);
            return length > 0 ? Optional.of(realPath) : Optional.empty();
        }
        catch (Exception e)
        {
            log.debug("Invalid path: {}, {}", path, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<List<Path>> realCaPath(List<Path> caPathList)
    {
        // Warning: Blocking operations
        List<Optional<Path>> realCaPaths = caPathList.stream().map(this::realPath).collect(Collectors.toList());
        if (realCaPaths.stream().anyMatch(Optional::isEmpty))
        {
            return Optional.empty();
        }
        else
        {
            return Optional.of(realCaPaths.stream().map(Optional::get).collect(Collectors.toList()));
        }
    }

    public static final class CertPaths
    {
        private final Path certPath;
        private final Path keyPath;
        private final List<Path> caPathList;

        public CertPaths(Path certPath,
                         Path keyPath,
                         List<Path> caPathList)
        {
            this.certPath = certPath;
            this.keyPath = keyPath;
            this.caPathList = caPathList;
        }

        public CertPaths(Path certPath,
                         Path keyPath,
                         Path caPath)
        {
            this(certPath, keyPath, List.of(caPath));
        }

        public Path getCertPath()
        {
            return certPath;
        }

        public Path getKeyPath()
        {
            return keyPath;
        }

        public List<Path> getCaPathList()
        {
            return caPathList;
        }

        private boolean validatePath(Path path)
        {
            // Warning: Blocking operations
            try
            {
                log.debug("Validating path: {}", path);
                final var length = path.toRealPath().toFile().length();
                log.debug("{} -> length: {}", path, length);
                return length > 0;
            }
            catch (Exception e)
            {
                log.debug("Invalid path: {}, {}", path, e.getMessage());
                return false;
            }
        }

        public boolean isCertPathValid()
        {
            return validatePath(this.certPath);
        }

        public boolean isKeyPathValid()
        {
            return validatePath(this.keyPath);
        }

        public List<Path> getCaFiles()
        {
            var files = new ArrayList<Path>();

            this.caPathList.stream().forEach(ca ->
            {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(ca, entry -> entry.getFileName().toString().matches("cert[0-9]+.pem")))
                {
                    for (final var path : ds)
                    {
                        files.add(path);
                    }
                }
                catch (IOException e)
                {
                    log.error("Could not open CA file.", e);
                }
            });
            return files;
        }

        private List<Path> getCaFilesFromDir(Path ca)
        {
            var files = new ArrayList<Path>();

            try (DirectoryStream<Path> ds = Files.newDirectoryStream(ca, entry -> entry.getFileName().toString().matches("cert[0-9]+.pem")))
            {
                for (final var path : ds)
                {
                    files.add(path);
                }
            }
            catch (IOException e)
            {
                log.error("Could not open CA file.", e);
            }

            return files;
        }

        public boolean isCaPathValid()
        {

            return this.caPathList.stream().allMatch(ca ->
            {
                log.debug("CA for validation is: {}", ca);
                var flag = false;
                if (Files.isDirectory(ca))
                {
                    for (final var path : this.getCaFilesFromDir(ca))
                    {
                        flag |= validatePath(path);
                    }
                }
                else
                {
                    // make this fast and just lookup the single file
                    flag = validatePath(ca);
                }

                log.debug("CA validation result is: {}", flag);
                return flag;
            });
        }

        @Override
        public String toString()
        {
            var builder = new StringBuilder();
            builder.append("CertPaths [certPath=");
            builder.append(certPath);
            builder.append(", keyPath=");
            builder.append(keyPath);
            builder.append(", caPath=");
            builder.append(caPathList);
            builder.append("]");
            return builder.toString();
        }

    }

}
