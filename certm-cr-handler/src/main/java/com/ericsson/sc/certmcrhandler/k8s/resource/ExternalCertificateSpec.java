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
 * Created on: Mar 27, 2024
 *     Author: Avengers
 */

package com.ericsson.sc.certmcrhandler.k8s.resource;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(using = JsonDeserializer.None.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "asymmetric-key-certificate-name",
                     "asymmetric-key-name",
                     "crl-file-name",
                     "generated-secret-name",
                     "generated-secret-type",
                     "trusted-certificate-list-name",
                     "trusted-certificates-file-name" })

public class ExternalCertificateSpec implements KubernetesResource
{

    private static final long serialVersionUID = 1L;

    @JsonProperty(value = "asymmetric-key-certificate-name", required = false)
    @JsonPropertyDescription("Name of the asymmetric-key-certificate instance according to ietf-keystore configuration. The name must be prefixed with the parent asymmetric-key name and forward slash as separator, for example \"netconfkey/certificate\".")
    private String asymmetricKeyCertificateName;

    @JsonProperty(value = "asymmetric-key-name", required = false)
    @JsonPropertyDescription("Name of the asymmetric-key instance according to ietf-keystore configuration..")
    private String asymmetricKeyName;

    @JsonProperty(value = "crl-file-name", required = false)
    @JsonPropertyDescription("Name of file to contain all Certificate Revocation Lists (CRL) related to the trusted certificates list. The default filename is ca.crl. Only applicable for trusted certificates, that is, when trusted-certificate-list-name is used.")
    private String crlFileName;

    @JsonProperty(value = "generated-secret-name", required = true)
    @JsonFormat(pattern = "^[a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*$")
    @JsonPropertyDescription("Name of secret to be created and used when provisioning certificates and keys")
    private String generatedSecretName;

    @JsonProperty(value = "generated-secret-type", required = true)
    @JsonPropertyDescription("The type of secret to be created. The tls type is required for Ingress Controllers. Note that the tls type secret contains one private key and one certificate which means it's only applicable for asymmetric-key-certificate consumers.")
    private GeneratedSecretTypeEnum generatedSecretType;

    @JsonProperty(value = "trusted-certificate-list-name", required = false)
    @JsonPropertyDescription("Name of the trusted certificates list instance according to ietf-truststore configuration.")
    private String trustedCertificateListName;

    @JsonProperty(value = "trusted-certificates-file-name", required = false)
    @JsonPropertyDescription("Name of file to contain all trusted certificates. When set, all trusted certificates are stored in one PEM file with specified name. This overrides the default behavior where each trusted certificate is stored in its own PEM file. Only applicable for trusted certificates, that is, when trusted-certificate-list-name is used..")
    private String trustedCertificatesFileName;

    private ExternalCertificateSpec(ExternalCertificateSpecBuilder builder)
    {
        this.asymmetricKeyCertificateName = builder.asymmetricKeyCertificateName;
        this.asymmetricKeyName = builder.asymmetricKeyName;
        this.crlFileName = builder.crlFileName;
        this.generatedSecretName = builder.generatedSecretName;
        this.generatedSecretType = builder.generatedSecretType;
        this.trustedCertificateListName = builder.trustedCertificateListName;
        this.trustedCertificatesFileName = builder.trustedCertificatesFileName;
    }

    public ExternalCertificateSpec()
    {

    }

    public static ExternalCertificateSpecBuilder builder()
    {
        return new ExternalCertificateSpecBuilder();
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }

    /**
     * @return the asymmetricKeyCertificateName
     */
    public String getAsymmetricKeyCertificateName()
    {
        return asymmetricKeyCertificateName;
    }

    /**
     * @return the asymmetricKeyName
     */
    public String getAsymmetricKeyName()
    {
        return asymmetricKeyName;
    }

    /**
     * @return the crlFileName
     */
    public String getCrlFileName()
    {
        return crlFileName;
    }

    /**
     * @return the generatedSecretName
     */
    public String getGeneratedSecretName()
    {
        return generatedSecretName;
    }

    /**
     * @return the generatedSecretType
     */
    public GeneratedSecretTypeEnum getGeneratedSecretType()
    {
        return generatedSecretType;
    }

    /**
     * @return the trustedCertificateListName
     */
    public String getTrustedCertificateListName()
    {
        return trustedCertificateListName;
    }

    /**
     * @return the trustedCertificatesFileName
     */
    public String getTrustedCertificatesFileName()
    {
        return trustedCertificatesFileName;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(asymmetricKeyCertificateName,
                            asymmetricKeyName,
                            crlFileName,
                            generatedSecretName,
                            generatedSecretType,
                            trustedCertificateListName,
                            trustedCertificatesFileName);
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
        ExternalCertificateSpec other = (ExternalCertificateSpec) obj;
        return Objects.equals(asymmetricKeyCertificateName, other.asymmetricKeyCertificateName) && Objects.equals(asymmetricKeyName, other.asymmetricKeyName)
               && Objects.equals(crlFileName, other.crlFileName) && Objects.equals(generatedSecretName, other.generatedSecretName)
               && generatedSecretType == other.generatedSecretType && Objects.equals(trustedCertificateListName, other.trustedCertificateListName)
               && Objects.equals(trustedCertificatesFileName, other.trustedCertificatesFileName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "ExternalCertificateSpec [asymmetricKeyCertificateName=" + asymmetricKeyCertificateName + ", asymmetricKeyName=" + asymmetricKeyName
               + ", crlFileName=" + crlFileName + ", generatedSecretName=" + generatedSecretName + ", generatedSecretType=" + generatedSecretType
               + ", trustedCertificateListName=" + trustedCertificateListName + ", trustedCertificatesFileName=" + trustedCertificatesFileName + "]";
    }

    public static class ExternalCertificateSpecBuilder
    {

        private String asymmetricKeyCertificateName;
        private String asymmetricKeyName;
        private String crlFileName;
        private String generatedSecretName;
        private GeneratedSecretTypeEnum generatedSecretType;
        private String trustedCertificateListName;
        private String trustedCertificatesFileName;

        /**
         * @param asymmetricKeyCertificateName the asymmetricKeyCertificateName to set
         */
        public ExternalCertificateSpecBuilder asymmetricKeyCertificateName(String asymmetricKeyCertificateName)
        {
            this.asymmetricKeyCertificateName = asymmetricKeyCertificateName;
            return this;
        }

        /**
         * @param asymmetricKeyName the asymmetricKeyName to set
         */
        public ExternalCertificateSpecBuilder asymmetricKeyName(String asymmetricKeyName)
        {
            this.asymmetricKeyName = asymmetricKeyName;
            return this;
        }

        /**
         * @param crlFileName the crlFileName to set
         */
        public ExternalCertificateSpecBuilder crlFileName(String crlFileName)
        {
            this.crlFileName = crlFileName;
            return this;
        }

        /**
         * @param generatedSecretName the generatedSecretName to set
         */
        public ExternalCertificateSpecBuilder generatedSecretName(String generatedSecretName)
        {
            this.generatedSecretName = generatedSecretName;
            return this;
        }

        /**
         * @param generatedSecretType the generatedSecretType to set
         */
        public ExternalCertificateSpecBuilder generatedSecretType(GeneratedSecretTypeEnum generatedSecretType)
        {
            this.generatedSecretType = generatedSecretType;
            return this;
        }

        /**
         * @param trustedCertificateListName the trustedCertificateListName to set
         */
        public ExternalCertificateSpecBuilder trustedCertificateListName(String trustedCertificateListName)
        {
            this.trustedCertificateListName = trustedCertificateListName;
            return this;
        }

        /**
         * @param trustedCertificatesFileName the trustedCertificatesFileName to set
         * @return
         */
        public ExternalCertificateSpecBuilder trustedCertificatesFileName(String trustedCertificatesFileName)
        {
            this.trustedCertificatesFileName = trustedCertificatesFileName;
            return this;
        }

        public ExternalCertificateSpec build()
        {
            return new ExternalCertificateSpec(this);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode()
        {
            return Objects.hash(asymmetricKeyCertificateName,
                                asymmetricKeyName,
                                crlFileName,
                                generatedSecretName,
                                generatedSecretType,
                                trustedCertificateListName,
                                trustedCertificatesFileName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExternalCertificateSpecBuilder other = (ExternalCertificateSpecBuilder) obj;
            return Objects.equals(asymmetricKeyCertificateName, other.asymmetricKeyCertificateName)
                   && Objects.equals(asymmetricKeyName, other.asymmetricKeyName) && Objects.equals(crlFileName, other.crlFileName)
                   && Objects.equals(generatedSecretName, other.generatedSecretName) && generatedSecretType == other.generatedSecretType
                   && Objects.equals(trustedCertificateListName, other.trustedCertificateListName)
                   && Objects.equals(trustedCertificatesFileName, other.trustedCertificatesFileName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString()
        {
            return "ExternalCertificateSpecBuilder [asymmetricKeyCertificateName=" + asymmetricKeyCertificateName + ", asymmetricKeyName=" + asymmetricKeyName
                   + ", crlFileName=" + crlFileName + ", generatedSecretName=" + generatedSecretName + ", generatedSecretType=" + generatedSecretType
                   + ", trustedCertificateListName=" + trustedCertificateListName + ", trustedCertificatesFileName=" + trustedCertificatesFileName + "]";
        }

    }
}