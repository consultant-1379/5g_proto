package com.ericsson.sc.s3client;

import com.ericsson.utilities.common.EnvVars;

import java.util.Objects;
import java.util.Optional;

/**
 * Store all s3 service environmental parameters
 */
public class S3Parameters
{

    private Boolean tlsEnabled;
    private EndpointData endpointData;
    private ClientTimeout clientTimeout;
    private ClientAccess clientAccess;
    private ObjectStorageData objectStorageData;

    private S3Parameters(Boolean tlsEnabled,
                         ObjectStorageData objectStorageData,
                         EndpointData endpointData,
                         ClientAccess clientAccess,
                         ClientTimeout clientTimeout)
    {
        Objects.requireNonNull(tlsEnabled);

        this.tlsEnabled = tlsEnabled;
        this.objectStorageData = objectStorageData;
        this.endpointData = endpointData;
        this.clientAccess = clientAccess;
        this.clientTimeout = clientTimeout;
    }

    public static S3Parameters with(Boolean tlsEnabled,
                                    ObjectStorageData objectStorageData,
                                    EndpointData endpointData,
                                    ClientAccess clientAccess,
                                    ClientTimeout clientTimeout)
    {
        return new S3Parameters(tlsEnabled, objectStorageData, endpointData, clientAccess, clientTimeout);
    }

    public Boolean isTlsEnabled()
    {
        return this.tlsEnabled;
    }

    public ObjectStorageData getObjectStorageData()
    {
        return this.objectStorageData;
    }

    public String getBucketName()
    {
        return this.getObjectStorageData().bucketName;
    }

    public String getMonitorDirectory()
    {
        return this.getObjectStorageData().monitorDirectory;
    }

    public String getObjectPrefix()
    {
        return this.getObjectStorageData().objectPrefix;
    }

    public Long getDebounceTimeout()
    {
        return this.getObjectStorageData().debounceTimeout;
    }

    public Long getThrottleObjestStorage()
    {
        return this.getObjectStorageData().throttleTimeout;
    }

    public EndpointData getEndpointData()
    {
        return this.endpointData;
    }

    public String getEndpoint()
    {
        return this.getEndpointData().endpoint;
    }

    public Integer getEndpointPort()
    {
        return this.getEndpointData().endpointPort;
    }

    public Boolean isEndpointSslEnabled()
    {
        return this.getEndpointData().endpointSslEnabled;
    }

    public ClientTimeout getClientTimeout()
    {
        return this.clientTimeout;
    }

    public Integer getConnectionTimeout()
    {
        return this.getClientTimeout().connectionTimeout;
    }

    public Integer getReadTimeout()
    {
        return this.getClientTimeout().readTimeout;
    }

    public Integer getWriteTimeout()
    {
        return this.getClientTimeout().writeTimeout;
    }

    public ClientAccess getClientAccess()
    {
        return this.clientAccess;
    }

    public String getAccessKey()
    {
        return this.getClientAccess().accessKey;
    }

    public String getSecretKey()
    {
        return this.getClientAccess().secretKey;
    }

    public static S3Parameters fromEnvironment()
    {
        final ClientTimeout clientTimeout = new ClientTimeout(Integer.parseInt(EnvVars.get("CONNECTION_TIMEOUT", 300)),
                                                              Integer.parseInt(EnvVars.get("READ_TIMEOUT", 300)),
                                                              Integer.parseInt(EnvVars.get("WRITE_TIMEOUT", 300)));

        final ClientAccess clientAccess = new ClientAccess(EnvVars.get("ACCESS_KEY"), EnvVars.get("SECRET_KEY"));

        final EndpointData endpointData = new EndpointData(EnvVars.get("ENDPOINT"),
                                                           Integer.parseInt(EnvVars.get("ENDPOINT_PORT", 9000)),
                                                           Boolean.valueOf(EnvVars.get("ENDPOINT_SSL")));

        final ObjectStorageData objectStorageData = new ObjectStorageData(EnvVars.get("DIRECTORY"),
                                                                          Long.parseLong(EnvVars.get("DEBOUNCE_TIMEOUT", 3000L)),
                                                                          Long.parseLong(EnvVars.get("THROTTLE_OBJECT_STORAGE_UPLOAD", 60L)),
                                                                          EnvVars.get("BUCKET"),
                                                                          EnvVars.get("HOSTNAME"));

        return new S3Parameters(Boolean.valueOf(EnvVars.get("TLS_ENABLED")), objectStorageData, endpointData, clientAccess, clientTimeout);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Minio Agent Parameters [endpoint=");
        builder.append(this.getEndpoint());
        builder.append(", endpoint-port=");
        builder.append(this.getEndpointPort());
        builder.append(", endpoint-ssl-enabled=");
        builder.append(this.isEndpointSslEnabled());
        builder.append(", tls-enabled=");
        builder.append(this.isTlsEnabled());
        builder.append(", access-key=");
        builder.append(this.getAccessKey());
        builder.append(", secret-key=");
        builder.append(this.getSecretKey());
        builder.append(", connection-timeout=");
        builder.append(this.getConnectionTimeout());
        builder.append(", read-timeout=");
        builder.append(this.getReadTimeout());
        builder.append(", write-timeout=");
        builder.append(this.getWriteTimeout());
        builder.append(", bucket=");
        builder.append(this.getBucketName());
        builder.append(", monitor-directrory=");
        builder.append(this.getMonitorDirectory());
        builder.append(", object-prefix=");
        builder.append(this.getObjectPrefix());
        builder.append(", debounce-timeout=");
        builder.append(this.getDebounceTimeout());
        builder.append(", throttle-object-storage-timeout=");
        builder.append(this.getThrottleObjestStorage());
        builder.append("]");
        return builder.toString();
    }

    public static class ObjectStorageData
    {
        private final String monitorDirectory;
        private final String bucketName;
        private final String objectPrefix;
        private final Long debounceTimeout;
        private final Long throttleTimeout;

        public ObjectStorageData(final String monitorDirectory,
                                 final Long debounceTimeout,
                                 final Long throttleTimeout,
                                 final String bucketName,
                                 final String objectPrefix)
        {
            Objects.requireNonNull(monitorDirectory);
            Objects.requireNonNull(bucketName);
            Objects.requireNonNull(objectPrefix);

            /*
             * Directory that s3client will monitor and the coredumps expected
             */
            this.monitorDirectory = monitorDirectory;

            /*
             * Avoid reading quickly file changes
             */
            this.debounceTimeout = debounceTimeout;

            /*
             * Throttles items from the files identified handling latest emitted item from
             * file stream when the specified timeout elapses between them.
             * 
             */
            this.throttleTimeout = throttleTimeout;

            /*
             * The bucket name to be used
             */
            this.bucketName = bucketName;

            /*
             * The object prefix to be used.
             */
            this.objectPrefix = objectPrefix + "-";
        }
    }

    public static class EndpointData
    {
        private final String endpoint;
        private final Integer endpointPort;
        private final Boolean endpointSslEnabled;

        public EndpointData(final String endpoint,
                            final Integer endpointPort,
                            final Boolean endpointSslEnabled)
        {
            Objects.requireNonNull(endpoint);
            Objects.requireNonNull(endpointSslEnabled);

            this.endpoint = endpoint;
            this.endpointPort = endpointPort;
            this.endpointSslEnabled = endpointSslEnabled;
        }
    }

    public static class ClientAccess
    {
        private final String accessKey;
        private final String secretKey;

        public ClientAccess(final String accessKey,
                            final String secretKey)
        {
            Objects.requireNonNull(accessKey);
            Objects.requireNonNull(secretKey);

            this.accessKey = accessKey;
            this.secretKey = secretKey;
        }
    }

    public static class ClientTimeout
    {
        private final Integer connectionTimeout;
        private final Integer readTimeout;
        private final Integer writeTimeout;

        public ClientTimeout(final Integer connectionTimeout,
                             final Integer readTimeout,
                             final Integer writeTimeout)
        {
            this.connectionTimeout = connectionTimeout;
            this.readTimeout = readTimeout;
            this.writeTimeout = writeTimeout;
        }
    }
}
