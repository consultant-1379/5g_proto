/**
 * COPYRIGHT ERICSSON GMBH 2020
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Feb 15, 2020
 *     Author: eedstl
 */

package com.ericsson.cnal.common;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.utilities.exceptions.Utils;
import com.ericsson.utilities.file.FileAlterationListenerImpl.EventKind;
import com.ericsson.utilities.file.FileAlterationListenerImpl.FileEvent;
import com.ericsson.utilities.file.FileWatcher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * Singleton providing the service of certificate supervision to its
 * subscribers.
 */
public class CertificateObserver extends FileWatcher
{
    public static class Secret
    {
        public static class Item
        {
            final boolean exists;
            final long lastModified;
            final long length;
            final String name;

            public Item()
            {
                this.exists = false;
                this.lastModified = 0;
                this.length = 0;
                this.name = "";
            }

            public Item(final File f)
            {
                this.exists = f.exists();
                this.lastModified = f.lastModified();
                this.length = f.length();
                this.name = f.getAbsolutePath();
            }

            @Override
            public boolean equals(final Object obj)
            {
                if (obj == null)
                    return false;

                if (this.getClass() != obj.getClass())
                    return false;

                final Item rhs = (Item) obj;

                return this == rhs || this.exists() == rhs.exists() && this.lastModified() == rhs.lastModified() && this.length() == rhs.length()
                                      && this.name().equals(rhs.name());
            }

            public boolean exists()
            {
                return this.exists && this.length > 0;
            }

            @Override
            public int hashCode()
            {
                return Objects.hash(this.exists, this.lastModified, this.length, this.name);
            }

            public long lastModified()
            {
                return this.lastModified;
            }

            public long length()
            {
                return this.length;
            }

            public String name()
            {
                return this.name;
            }

            @Override
            public String toString()
            {
                return new StringBuilder().append("{")
                                          .append("exists=")
                                          .append(this.exists())
                                          .append(", lastModified=")
                                          .append(this.lastModified())
                                          .append(", name=")
                                          .append(this.name())
                                          .append("}")
                                          .toString();
            }
        }

        private Item certificate;
        private Item privateKey;
        private final Map<String, Item> trustCas;
        private boolean hasChangedSinceLastNotify;

        public Secret()
        {
            this.certificate = new Item();
            this.privateKey = new Item();
            this.trustCas = new HashMap<>();
            this.hasChangedSinceLastNotify = false;
        }

        public Secret(final Secret original)
        {
            this.certificate = original.getCertificate();
            this.privateKey = original.getPrivateKey();
            this.trustCas = new HashMap<>(original.trustCas);
            this.hasChangedSinceLastNotify = original.hasChangedSinceLastNotify(true);
        }

        public Secret adjustTrustCas(final File trustCa)
        {
            final Item n = new Item(trustCa);
            final Item o = this.trustCas.get(n.name());

            if (o != null)
            {
                if (!o.equals(n))
                {
                    if (n.exists())
                        this.trustCas.replace(n.name(), n);
                    else
                        this.trustCas.remove(n.name());

                    this.hasChangedSinceLastNotify = true;
                }
            }
            else
            {
                if (n.exists())
                {
                    this.trustCas.put(n.name(), n);
                    this.hasChangedSinceLastNotify = true;
                }
            }

            return this;
        }

        public Item getCertificate()
        {
            return this.certificate;
        }

        public Item getPrivateKey()
        {
            return this.privateKey;
        }

        public Collection<Item> getTrustCas()
        {
            return this.trustCas.values();
        }

        public boolean hasChangedSinceLastNotify()
        {
            return this.hasChangedSinceLastNotify(false);
        }

        private boolean hasChangedSinceLastNotify(boolean reset)
        {
            if (!reset)
                return this.hasChangedSinceLastNotify;

            final boolean b = this.hasChangedSinceLastNotify;
            this.hasChangedSinceLastNotify = false;
            return b;
        }

        public Secret setCertificate(final File f)
        {
            final Item n = new Item(f);

            if (!this.certificate.equals(n))
            {
                this.certificate = n;
                this.hasChangedSinceLastNotify = true;
            }

            return this;
        }

        public Secret setPrivateKey(final File f)
        {
            final Item n = new Item(f);

            if (!this.privateKey.equals(n))
            {
                this.privateKey = n;
                this.hasChangedSinceLastNotify = true;
            }

            return this;
        }

        @Override
        public String toString()
        {
            StringBuilder b = new StringBuilder().append("{")
                                                 .append("hasChangedSinceLastNotify=")
                                                 .append(this.hasChangedSinceLastNotify)
                                                 .append(", certificate='")
                                                 .append(this.certificate.toString())
                                                 .append("', privateKey='")
                                                 .append(this.privateKey.toString())
                                                 .append("', trustCas=[");

            boolean isFirst = true;

            for (final Item trustCa : this.trustCas.values())
            {
                if (isFirst)
                    isFirst = false;
                else
                    b.append(", ");

                b.append("'").append(trustCa.toString()).append("'");
            }

            b.append("]}");

            return b.toString();
        }

        public boolean useTls()
        {
            return this.getCertificate().exists() && this.getPrivateKey().exists() && !this.getTrustCas().isEmpty();
        }
    }

    private static final String TRUSTCA = "trustCA";

    private static final Logger log = LoggerFactory.getLogger(CertificateObserver.class);

    /**
     * @return A filter that filters for CERTIFICATES/*.pem and
     *         CERTIFICATES_TRUSTCA/*.pem
     */
    private static FileFilter createFilter()
    {
        final IOFileFilter filefilter = FileFilterUtils.and(FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter(".pem"));
        final IOFileFilter subFilefilter = FileFilterUtils.or(FileFilterUtils.and(FileFilterUtils.nameFileFilter(TRUSTCA),
                                                                                  FileFilterUtils.directoryFileFilter()),
                                                              filefilter);
        return FileFilterUtils.or(filefilter, subFilefilter);
    }

    private final String CERTIFICATES;
    private final String CERTIFICATES_TRUSTCA;
    private final String CERTIFICATE_PEM;
    private final String KEY_PEM;

    private final Subject<Secret> secretSubject;
    private final BehaviorSubject<Secret> secrets;

    private Secret current;

    public CertificateObserver(final String certificatesPath)
    {
        super(certificatesPath, createFilter());

        this.CERTIFICATES = certificatesPath;
        this.CERTIFICATES_TRUSTCA = CERTIFICATES + "/" + TRUSTCA;
        this.CERTIFICATE_PEM = CERTIFICATES + "/certificate.pem";
        this.KEY_PEM = CERTIFICATES + "/key.pem";

        this.current = new Secret();
        this.secretSubject = BehaviorSubject.create();
        this.secrets = BehaviorSubject.create();
        this.secretSubject.debounce(2, TimeUnit.SECONDS).subscribe(this.secrets);
    }

    public Flowable<Secret> getSecrets()
    {
        return this.secrets.toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Completable start()
    {
        return super.start().doOnComplete(() -> this.getFileChangesStream()
                                                    .map(this::processEvent)
                                                    .filter(Optional::isPresent)
                                                    .doOnNext(o -> this.secretSubject.toSerialized().onNext(o.get()))
                                                    .ignoreElements()
                                                    .doOnComplete(this.secretSubject::onComplete)
                                                    .doOnSubscribe(d -> log.info("Started supervising files."))
                                                    .subscribe(() -> log.info("Stopped supervising files."),
                                                               t -> log.error("Stopped supervising files. Cause: {}",
                                                                              Utils.toString(t, log.isDebugEnabled()))));
    }

    private synchronized Optional<Secret> processEvent(final FileEvent event)
    {
        boolean validEvent = false;

        log.info("Before: current={}", this.current);

        if (event.getFile().getAbsolutePath().equals(this.CERTIFICATES))
        {
            if (event.getEventKind() == EventKind.CREATE)
            {
                // Explicitly check for all files.

                validEvent = true;

                File f;

                f = new File(this.CERTIFICATE_PEM);
                log.debug("file={}, exists={}, length={}, lastModified={}", f.getName(), f.exists(), f.length(), f.lastModified());
                this.current.setCertificate(f);

                f = new File(this.KEY_PEM);
                log.debug("file={}, exists={}, length={}, lastModified={}", f.getName(), f.exists(), f.length(), f.lastModified());
                this.current.setPrivateKey(f);

                try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(this.CERTIFICATES_TRUSTCA)))
                {
                    for (Path path : stream)
                    {
                        if (!Files.isDirectory(path) && path.getFileName().toString().endsWith(".pem"))
                        {
                            f = new File(path.toAbsolutePath().toString());
                            log.debug("file={}, exists={}, length={}, lastModified={}", f.getName(), f.exists(), f.length(), f.lastModified());
                            this.current.adjustTrustCas(f);
                        }
                    }
                }
                catch (IOException e)
                {
                    log.warn("Error traversing folder '{}'. Cause: {}", this.CERTIFICATES_TRUSTCA, Utils.toString(e, log.isDebugEnabled()));
                    validEvent = false;
                }
            }
        }
        else if (event.getFile().getAbsolutePath().equals(this.CERTIFICATE_PEM))
        {
            final File f = event.getFile();
            log.debug("file={}, exists={}, length={}, lastModified={}", f.getName(), f.exists(), f.length(), f.lastModified());
            this.current.setCertificate(event.getFile());

            validEvent = true;
        }
        else if (event.getFile().getAbsolutePath().equals(this.KEY_PEM))
        {
            final File f = event.getFile();
            log.debug("file={}, exists={}, length={}, lastModified={}", f.getName(), f.exists(), f.length(), f.lastModified());
            this.current.setPrivateKey(event.getFile());

            validEvent = true;
        }
        else if (event.getFile().getAbsolutePath().startsWith(this.CERTIFICATES_TRUSTCA))
        {
            final File f = event.getFile();
            log.debug("file={}, exists={}, length={}, lastModified={}", f.getName(), f.exists(), f.length(), f.lastModified());
            this.current.adjustTrustCas(f);

            validEvent = true;
        }

        log.info("After: current={}", this.current);

        if (validEvent && this.current.hasChangedSinceLastNotify())
            return Optional.of(new Secret(this.current));

        return Optional.empty();
    }
}
