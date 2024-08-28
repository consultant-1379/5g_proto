package com.ericsson.supreme.config;

import java.util.List;

import com.ericsson.supreme.exceptions.ValidationException;

public class Admin
{
    public static class YangProvider
    {
        private String ip;
        private int port;
        private String username;
        private String password;

        public String getIp()
        {
            return ip;
        }

        public void setIp(String ip)
        {
            this.ip = ip;
        }

        public String getUsername()
        {
            return username;
        }

        public void setUsername(String username)
        {
            this.username = username;
        }

        public int getPort()
        {
            return port;
        }

        public void setPort(int port)
        {
            this.port = port;
        }

        public String getPassword()
        {
            return password;
        }

        public void setPassword(String password)
        {
            this.password = password;
        }

        @Override
        public String toString()
        {
            var sb = new StringBuilder();
            sb.append("\n  ip: ");
            sb.append(ip);
            sb.append("\n  port: ");
            sb.append(port);
            sb.append("\n  usename ");
            sb.append(username);
            sb.append("\n  password: ");
            sb.append(password);
            return sb.toString();
        }

        /**
         * Check that mandatory fields are defined. Check that nested fields are valid.
         */
        public void validate()
        {
            var mandatoryFields = List.of(username, password);
            mandatoryFields.forEach(f ->
            {
                if (f == null)
                {
                    throw new ValidationException("Mandatory field in yangProvider not configured");
                }
            });
        }
    }

    private String namespace;
    private YangProvider yangProvider;
    private String kubeconfig;

    public String getNamespace()
    {
        return namespace;
    }

    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    public YangProvider getYangProvider()
    {
        return yangProvider;
    }

    public void setYangProvider(YangProvider yangProvider)
    {
        this.yangProvider = yangProvider;
    }

    public String getKubeconfig()
    {
        return kubeconfig;
    }

    public void setKubeconfig(String kubeconfig)
    {
        this.kubeconfig = kubeconfig;
    }

    @Override
    public String toString()
    {
        var sb = new StringBuilder();
        sb.append("\n  namespace: ");
        sb.append(namespace);
        sb.append("\n  yangProvider: ");
        sb.append(yangProvider);
        sb.append("\n kubeconfig: ");
        sb.append(kubeconfig);
        return sb.toString();
    }

    /**
     * Check that mandatory fields are defined. Check that nested fields are valid.
     */
    public void validate()
    {
        if (yangProvider != null)
        {
            yangProvider.validate();
        }

        if (namespace == null)
        {
            throw new ValidationException("Mandatory field 'namespace' was not defined in admin");
        }
    }

}
