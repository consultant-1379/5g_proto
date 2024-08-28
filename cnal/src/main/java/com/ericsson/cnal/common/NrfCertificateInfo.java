/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Apr 15, 2024
 *     Author: zavvann
 */

package com.ericsson.cnal.common;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class NrfCertificateInfo
{
    private final NrfCertificateHandling handling;
    private final Optional<ExternalCertificate> extCertificate;
    private static final Logger log = LoggerFactory.getLogger(NrfCertificateInfo.class);

    public enum NrfCertificateHandling
    {
        NEW,
        OLD,
        HYBRID
    }

    public NrfCertificateInfo(NrfCertificateHandling handling,
                              Optional<ExternalCertificate> extCertificate)
    {
        this.handling = handling;
        this.extCertificate = extCertificate;
    }

    public NrfCertificateHandling getHandling()
    {
        return handling;
    }

    public Optional<ExternalCertificate> getExtCertificate()
    {
        return extCertificate;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(extCertificate, handling);
    }

    @Override
    public boolean equals(Object obj)
    {
//        if (this == obj)
//        {
//            log.info("Prev equals to curr");
//            return true;
//        }
        if (obj == null)
        {
            log.info("object is null -> predicate false");
            return false;
        }
        if (getClass() != obj.getClass())
        {
            log.info("different class in objects");
            return false;
        }
        NrfCertificateInfo other = (NrfCertificateInfo) obj;
        log.info("CHeck predicate for ext cert: {}",
                 ((this.extCertificate.isPresent() && other.extCertificate.isPresent() && Objects.equals(this.extCertificate.get(), other.extCertificate.get()))
                  || (this.extCertificate.isEmpty() && other.extCertificate.isEmpty())) && this.handling == other.handling);
        return ((this.extCertificate.isPresent() && other.extCertificate.isPresent() && Objects.equals(this.extCertificate.get(), other.extCertificate.get()))
                || (this.extCertificate.isEmpty() && other.extCertificate.isEmpty()))
               && this.handling == other.handling;
    }

    @Override
    public String toString()
    {
        return "NrfCertificateInfo [handling=" + handling + ", extCertificate=" + extCertificate + "]";
    }

    public static class ExternalCertificate
    {
        private String certificate;
        private String key;
        private String trustCa;

        public ExternalCertificate()
        {
            this.certificate = null;
            this.key = null;
            this.trustCa = null;
        }

        public ExternalCertificate(String certificate,
                                   String key,
                                   String trustCa)
        {
            this.certificate = certificate;
            this.key = key;
            this.trustCa = trustCa;
        }

        public String getCertificate()
        {
            return certificate;
        }

        public void setCertificate(String certificate)
        {
            this.certificate = certificate;
        }

        public String getKey()
        {
            return key;
        }

        public void setKey(String key)
        {
            this.key = key;
        }

        public String getTrustCa()
        {
            return trustCa;
        }

        public void setTrustCa(String trustCa)
        {
            this.trustCa = trustCa;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(certificate, key, trustCa);
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                log.info("Prev extcert equals to curr extcert");
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExternalCertificate other = (ExternalCertificate) obj;
            return Objects.equals(this.certificate, other.certificate) && Objects.equals(this.key, other.key) && Objects.equals(this.trustCa, other.trustCa);
        }

        @Override
        public String toString()
        {
            return "ExternalCertificate [certificate=" + certificate + ", key=" + key + ", trustCa=" + trustCa + "]";
        }

    }

}
