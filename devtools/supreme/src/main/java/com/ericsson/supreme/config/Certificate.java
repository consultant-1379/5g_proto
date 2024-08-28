package com.ericsson.supreme.config;

public class Certificate
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
    }

    protected String name;
    protected String outputDir;
    protected Algorithm algorithm;
    protected String expirationDays;
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

    public String getExpirationDays()
    {
        return expirationDays;
    }

    public void setExpirationDays(String expirationDays)
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
}
