package com.ericsson.supreme.config;

import java.util.List;

import com.ericsson.supreme.exceptions.ValidationException;

public class ClientCertificate
{

    public static class Sign
    {
        private String certificateAuthority;
        private Boolean selfSigned;

        public String getCertificateAuthority()
        {
            return certificateAuthority;
        }

        public void setCertificateAuthority(String certificateAuthority)
        {
            this.certificateAuthority = certificateAuthority;
        }

        public boolean isSelfSigned()
        {
            return selfSigned;
        }

        public void setSelfSigned(boolean selfSigned)
        {
            this.selfSigned = selfSigned;
        }

        @Override
        public String toString()
        {
            var sb = new StringBuilder();
            sb.append("\n  selfSigned: ");
            sb.append(selfSigned);
            sb.append("\n  certificateAuthority: ");
            sb.append(certificateAuthority);
            return sb.toString();
        }

        /**
         * Check if only one of the fields is configured
         */
        public void validate()
        {
            if (certificateAuthority == null && (selfSigned == null || !selfSigned))
            {
                throw new ValidationException("One of certificateAuthority or selfSigned must be configured");
            }

            if (certificateAuthority != null && (selfSigned != null && selfSigned))
            {
                throw new ValidationException("Only one of certificateAuthority or selfSigned must be configured");
            }
        }
    }

    private String name;
    private String outputDir;
    private Algorithm algorithm;
    private Integer expirationDays;
    private String commonName;
    private List<String> sans;
    private Sign sign;

    public List<String> getSans()
    {
        return sans;
    }

    public void setSans(List<String> sans)
    {
        this.sans = sans;
    }

    public Sign getSign()
    {
        return sign;
    }

    public void setSign(Sign sign)
    {
        this.sign = sign;
    }

    public static class Algorithm
    {
        protected Integer bits;

        public Integer getBits()
        {
            return bits;
        }

        public void setBits(Integer bits)
        {
            this.bits = bits;
        }

        public void validate()
        {
            if (bits == null)
            {
                throw new ValidationException("bits in Algorithm should be configured");
            }
            var validBits = List.of(1024, 2048, 3072, 4096);
            if (!validBits.contains(bits))
            {
                throw new ValidationException("Invalid bits for Algorithm. %d not in %s.", bits, validBits);
            }
        }
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getOutputDir()
    {
        return outputDir;
    }

    public void setOutputDir(String outputDir)
    {
        this.outputDir = outputDir;
    }

    public Algorithm getAlgorithm()
    {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm)
    {
        this.algorithm = algorithm;
    }

    public Integer getExpirationDays()
    {
        return expirationDays;
    }

    public void setExpirationDays(Integer expirationDays)
    {
        this.expirationDays = expirationDays;
    }

    public String getCommonName()
    {
        return commonName;
    }

    public void setCommonName(String commonName)
    {
        this.commonName = commonName;
    }

    @Override
    public String toString()
    {
        var sb = new StringBuilder();
        sb.append("\n  name: ");
        sb.append(name);
        sb.append("\n  outputDir: ");
        sb.append(outputDir);
        sb.append("\n  algorithm: ");
        sb.append(algorithm);
        sb.append("\n  expirationDays: ");
        sb.append(expirationDays);
        sb.append("\n  commonName: ");
        sb.append(commonName);
        sb.append("\n  sans: ");
        sb.append(sans);
        sb.append("\n  sign: ");
        sb.append(sign);

        return sb.toString();
    }

    /**
     * Check that mandatory fields are defined. Check that nested fields are valid.
     */
    public void validate(List<String> storedCAs)
    {
        var mandatoryFields = List.of(name, outputDir, algorithm, expirationDays, commonName, sans, sign);
        mandatoryFields.forEach(f ->
        {
            if (f == null)
            {
                throw new ValidationException("Mandatory field in certificate not configured");
            }
        });

        if (sans.isEmpty())
        {
            throw new ValidationException("sans must not be empty");
        }

        sign.validate();
        if (!sign.isSelfSigned() && !storedCAs.contains(sign.getCertificateAuthority()))
        {
            throw new ValidationException("Non existing CA %s used as certificate authority", sign.getCertificateAuthority());
        }
    }
}
