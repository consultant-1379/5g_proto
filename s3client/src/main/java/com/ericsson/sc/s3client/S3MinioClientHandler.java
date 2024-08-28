package com.ericsson.sc.s3client;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.MinioException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Protocol;

/**
 * Class responsible for the creation handling of minion clients
 */
public class S3MinioClientHandler
{
    private static final Logger log = LoggerFactory.getLogger(S3MinioClientHandler.class);
    private final OkHttpClient httpClient;
    private static final String HTTP_TRACE = "HTTP trace: {}";
    private final MinioClient minioClient;

    public S3MinioClientHandler(S3Parameters parameters,
                                Path caCertificate)
    {
        if (!caCertificate.toFile().exists())
            log.warn("Internal CA not available!");

        this.httpClient = this.createOkHttpClient(parameters.isTlsEnabled(),
                                                  parameters.getConnectionTimeout(),
                                                  parameters.getReadTimeout(),
                                                  parameters.getWriteTimeout(),
                                                  caCertificate);

        this.minioClient = MinioClient.builder()
                                      .endpoint(parameters.getEndpoint(), parameters.getEndpointPort(), parameters.isEndpointSslEnabled())
                                      .credentials(parameters.getAccessKey(), parameters.getSecretKey())
                                      .httpClient(this.httpClient)
                                      .build();
    }

    public MinioClient getClient()
    {
        return this.minioClient;
    }

    private OkHttpClient createOkHttpClient(boolean tlsEnabled,
                                            int connectionTimeout,
                                            int readTimeout,
                                            int writeTimeout,
                                            Path caFiles)
    {
        Builder httpClientBuilder = new OkHttpClient().newBuilder()
                                                      .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                                                      .readTimeout(readTimeout, TimeUnit.SECONDS)
                                                      .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                                                      .protocols(Arrays.asList(Protocol.HTTP_1_1))
                                                      .followRedirects(true)
                                                      .followSslRedirects(true);

        if (tlsEnabled)
        {
            log.debug("TLS enabled: HTTP Client will use provided certificates.");
            try
            {
                httpClientBuilder = this.addCertificates(httpClientBuilder, caFiles);
            }
            catch (UnrecoverableKeyException e)
            {
                log.error("Key in the keystore cannot be recovered", e);
            }
            catch (KeyManagementException e)
            {
                log.error("Key management error occured (KeyIDConflict, KeyAuthorizationFailure, ExpiredKeyException).", e);
            }
            catch (CertificateException e)
            {
                log.error("General certificate problems identified.", e);
            }
            catch (KeyStoreException e)
            {
                log.error("Generic KeyStore problems identified.", e);
            }
            catch (NoSuchAlgorithmException e)
            {
                log.error("The requested cryptographic algorithm is not available.", e);
            }
            catch (IOException e)
            {
                log.error("Error while loading certificates in httpClient.", e);
            }
        }
        return httpClientBuilder.build();
    }

    private Builder addCertificates(Builder builder,
                                    Path caCertificate) throws CertificateException, KeyStoreException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException, KeyManagementException
    {
        // TODO: remove printing of certificates
        String certContents = Files.readString(caCertificate);
        log.debug("certificate contents: {}", certContents);

        Collection<? extends Certificate> certificates = null;
        try (FileInputStream fis = new FileInputStream(caCertificate.toFile().getAbsolutePath()))
        {
            certificates = CertificateFactory.getInstance("X.509").generateCertificates(fis);
        }

        if (certificates == null || certificates.isEmpty())
        {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        char[] password = "password".toCharArray();

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, password);

        int index = 0;
        for (Certificate certificate : certificates)
        {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

//        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
//        keyManagerFactory.init(keyStore, password);
//        final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        // TODO: Accept given TLS
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        // TODO: Accept all TLS
//        TrustManager[] trustManagers = new TrustManager[] { new X509TrustManager()
//        {
//
//            @Override
//            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
//                                           String authType) throws CertificateException
//            {
//                // nothing to do
//            }
//
//            @Override
//            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
//                                           String authType) throws CertificateException
//            {
//                // nothing to do
//            }
//
//            @Override
//            public java.security.cert.X509Certificate[] getAcceptedIssuers()
//            {
//                return new java.security.cert.X509Certificate[] {};
//            }
//        } };

        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
//        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
//        sslContext.init(null, trustManagers, null);
        sslContext.init(null, trustManagers, null);
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        return builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManagers[0]);
    }

    public boolean bucketExists(String bucketName) throws IOException, InvalidKeyException, NoSuchAlgorithmException
    {
        boolean result = false;
        try
        {
            result = this.minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        }
        catch (MinioException e)
        {
            log.error("Failed to check bucket {} existence.", bucketName, e);
            log.error(HTTP_TRACE, e.httpTrace());
        }
        return result;
    }

    public void createBucket(String bucketName) throws IOException, InvalidKeyException, NoSuchAlgorithmException
    {
        if (!bucketExists(bucketName))
        {
            try
            {
                this.minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                // TODO: add bucket policy
                // https://docs.min.io/docs/java-client-api-reference.html#setBucketPolicy
            }
            catch (MinioException e)
            {
                log.error("Failed to create bucket {}.", bucketName, e);
                log.error(HTTP_TRACE, e.httpTrace());
            }
        }
        else
        {
            log.error("Bucket {} already exists.", bucketName);
        }
    }

    public void uploadFile(String bucketName,
                           String objectName,
                           String fileName)
    {
        try
        {
            this.minioClient.uploadObject(UploadObjectArgs.builder()
                                                          .bucket(bucketName) // example: asiatrip
                                                          .object(objectName) // example: asiaphotos-2015.zip
                                                          .filename(fileName) // example: /home/user/Photos/asiaphotos.zip
                                                          .build());
            // TODO: add object retention policy
            // https://docs.min.io/docs/java-client-api-reference.html#setObjectRetention
        }
        catch (InvalidKeyException | ErrorResponseException | InsufficientDataException | InternalException | InvalidResponseException
               | NoSuchAlgorithmException | ServerException | XmlParserException | IllegalArgumentException | IOException e)
        {
            log.error("Failed to upload file {} as object {} in bucket {}.", fileName, objectName, bucketName, e);
        }
    }

    public List<String> getObjectNames(String bucketName)
    {
        List<String> objectList = new ArrayList<>();
        this.minioClient.listObjects(ListObjectsArgs.builder().bucket(bucketName).build()).forEach(item ->
        {
            try
            {
                objectList.add(item.get().objectName());
            }
            catch (InvalidKeyException | ErrorResponseException | IllegalArgumentException | InsufficientDataException | InternalException
                   | InvalidResponseException | NoSuchAlgorithmException | ServerException | XmlParserException | IOException e)
            {
                log.error("Failed to extract the object names for bucket {} and item {}.", bucketName, item, e);
            }
        });
        return objectList;
    }
}
