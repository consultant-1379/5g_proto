package com.ericsson.supreme.config;

import java.util.List;

import com.ericsson.supreme.exceptions.ValidationException;

public class CertificateAuthority
{
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

        @Override
        public String toString()
        {
            var sb = new StringBuilder();
            sb.append("\n  bits: ");
            sb.append(bits);
            return sb.toString();
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

    protected String name;
    protected String outputDir;
    protected Algorithm algorithm;
    protected Integer expirationDays;
    protected String commonName;

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

        return sb.toString();
    }

    /**
     * Check that mandatory fields are defined. Check that nested fields are valid.
     */
    public void validate()
    {
        var mandatoryFields = List.of(name, outputDir, algorithm, expirationDays, commonName);
        mandatoryFields.forEach(f ->
        {
            if (f == null)
            {
                throw new ValidationException("Mandatory field in certificateAuthority not configured");
            }
        });

    }
}
