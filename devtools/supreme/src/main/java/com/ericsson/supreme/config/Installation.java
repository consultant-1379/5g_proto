package com.ericsson.supreme.config;

import java.util.List;

import com.ericsson.supreme.exceptions.ValidationException;

public class Installation
{
    public static class Netconf
    {
        private String listName;
        private String keyName;
        private String certificateName;

        public String getListName()
        {
            return listName;
        }

        public String getKeyName()
        {
            return keyName;
        }

        public void setListName(String listName)
        {
            this.listName = listName;
        }

        public void setKeyName(String keyName)
        {
            this.keyName = keyName;
        }

        public String getCertificateName()
        {
            return certificateName;
        }

        public void setCertificateName(String certificateName)
        {
            this.certificateName = certificateName;
        }

        @Override
        public String toString()
        {
            return "Netconf [listName=" + listName + ", keyName=" + keyName + ", certificateName=" + certificateName + "]";
        }

        /**
         * Check that mandatory fields are defined. Check that nested fields are valid.
         */
        public void validate()
        {

            if (keyName == null && listName == null)
            {
                throw new ValidationException("One of keyName or listName name should be configured");
            }
            if (keyName != null && listName != null)
            {
                throw new ValidationException("Only one of keyName or listName name should be configured");
            }
            if (certificateName == null)
            {
                throw new ValidationException("Field certificateName should be configured");
            }
        }
    }

    public static class Target
    {
        private String secretName;
        private Netconf netconf;

        public String getSecretName()
        {
            return secretName;
        }

        public void setSecretName(String secretName)
        {
            this.secretName = secretName;
        }

        public Netconf getNetconf()
        {
            return netconf;
        }

        public void setNetconf(Netconf netconf)
        {
            this.netconf = netconf;
        }

        @Override
        public String toString()
        {
            var sb = new StringBuilder();
            sb.append("\n  secretName: ");
            sb.append(secretName);
            sb.append("\n  netconf: ");
            sb.append(netconf);
            return sb.toString();
        }

        /**
         * Check if only one of the fields is configured
         */
        public void validate()
        {
            if (secretName == null && netconf == null)
            {
                throw new ValidationException("One of secretName or netconf must be configured");
            }

            if (secretName != null && netconf != null)
            {
                throw new ValidationException("Only one of secretName or netconf must be configured");
            }
        }
    }

    private String name;
    private String dir;
    private Target target;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDir()
    {
        return dir;
    }

    public void setDir(String dir)
    {
        this.dir = dir;
    }

    public Target getTarget()
    {
        return target;
    }

    public void setTarget(Target target)
    {
        this.target = target;
    }

    @Override
    public String toString()
    {
        var sb = new StringBuilder();
        sb.append("\n  name: ");
        sb.append(name);
        sb.append("\n  dir: ");
        sb.append(dir);
        sb.append("\n  target: ");
        sb.append(target);
        return sb.toString();
    }

    /**
     * Check that mandatory fields are defined. Check that nested fields are valid.
     */
    public void validate()
    {
        var mandatoryFields = List.of(name, dir, target);
        mandatoryFields.forEach((f) ->
        {
            if (f == null)
            {
                throw new ValidationException("Mandatory field in Installation not configured");
            }
        });

        target.validate();
    }
}