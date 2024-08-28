package com.ericsson.sc.certmcrhandler;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.Assert;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.KubernetesClientTimeoutException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ericsson.sc.certmcrhandler.controller.SecretWatcher;
import com.ericsson.sc.certmcrhandler.k8s.io.CrHandler;
import com.ericsson.sc.certmcrhandler.k8s.io.SecretReader;
import com.ericsson.sc.certmcrhandler.k8s.resource.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCrsCreation
{
    private static final Logger log = LoggerFactory.getLogger(TestCrsCreation.class);

    private static final String LABEL_KEY = "app.kubernetes.io/managed-by";
    private static final String LABEL_VALUE_SEPP = "sepp-manager";
    private static final String myDir = System.getenv("USER");

    private SetupTestEnv setup;
    private CrHandler handler;
    private SecretReader secretReader;
    private String namespace;
    private String k8sConfig;
    private KubernetesClient client;
    private SecretWatcher secretWatcher;

    @BeforeClass
    void before() throws Exception
    {
        this.setup = new SetupTestEnv();
        setup.setupComplete();
        this.namespace = setup.getNamespace();
        this.k8sConfig = setup.getK3sKubeConfig();
        this.client = setup.getClient();

        this.handler = new CrHandler(this.k8sConfig, this.namespace);
        this.secretReader = new SecretReader(this.k8sConfig, this.namespace);
        this.secretWatcher = new SecretWatcher(this.client, this.namespace);
    }

    @AfterClass
    void after()
    {
        setup.cleanUpComplete();
    }

    @Test
    void checkCreationOfCRs() throws Exception
    {
        // test if informer is running properly
        log.info("Verify informer is running properly.");
        this.secretWatcher.start();
        Assert.assertTrue(this.secretWatcher.getSecretInformer().isRunning());

        ExternalCertificateSpec spec = ExternalCertificateSpec.builder().generatedSecretName("test").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("mytestspec");
        ec.setSpec(spec);

        handler.createCr(spec, "mytestspec").blockingAwait();

        Assert.assertEquals(spec, this.client.resource(ec).inNamespace(this.namespace).get().getSpec());
        secretReader.createSecretForCr(this.client.resource(ec).inNamespace(this.namespace).get().getSpec());

        Secret secret = secretReader.getSecret(spec.getGeneratedSecretName());

        if (secret != null)
        {
            Assert.assertEquals("Secret name should match generated secret name", spec.getGeneratedSecretName(), secret.getMetadata().getName());
        }
        else
        {
            Assert.assertNotNull("Secret should not be null");
        }

        // test if the certificates created with Annotations are created properly
        Map<String, String> annotations = Map.of("myTestAnnotation", "myTestValue", "helm.sh/resource-policy", "keep");
        Map<String, String> labels = new HashMap<>();
        labels.put("myLabel", "myLabelValue");

        if (Boolean.FALSE.equals(handler.checkExistingCr("mytestspec1")))
        {

            ExternalCertificateSpec spec1 = ExternalCertificateSpec.builder()
                                                                   .generatedSecretName("test1")
                                                                   .generatedSecretType(GeneratedSecretTypeEnum.TLS)
                                                                   .build();
            ExternalCertificate ec1 = new ExternalCertificate();
            ec1.getMetadata().setName("mytestspec1");
            ec1.setSpec(spec1);

            handler.createCrWithAnnotations(spec1, "mytestspec1", annotations, labels, "certm-cr-handler-test").blockingAwait();
            Assert.assertTrue(annotations.equals(this.client.resources(ExternalCertificate.class)
                                                            .inNamespace(this.namespace)
                                                            .withName("mytestspec1")
                                                            .get()
                                                            .getMetadata()
                                                            .getAnnotations()));
            this.client.resource(ec1).inNamespace(this.namespace).delete();
        }

        this.client.resource(ec).inNamespace(this.namespace).delete();

        // test if informer stops properly
        log.info("Verify informer stops properly.");
        this.secretWatcher.stopInformer();
        Assert.assertFalse(this.secretWatcher.getSecretInformer().isRunning());

    }

    @Test
    void checkCreationOfCRsFailsBadSecretName() throws Exception, KubernetesClientException
    {
        AtomicBoolean flag = new AtomicBoolean(false);

        ExternalCertificateSpec spec = ExternalCertificateSpec.builder()
                                                              .generatedSecretName("testTest")
                                                              .generatedSecretType(GeneratedSecretTypeEnum.TLS)
                                                              .build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("mytestspec");
        ec.setSpec(spec);

        handler.createCr(spec, "mytestspec").doOnError((e) ->
        {
            log.info("Creation of CR has failed.", e);
            flag.set(true);
        }).onErrorComplete().blockingAwait();

        Assert.assertTrue(flag.get());
    }

    @Test
    void checkDeleteUselessUsers() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, InterruptedException
    {
        List<String> crList = new ArrayList<>();
        Map<String, String> labels = new HashMap<>();
        labels.put(LABEL_KEY, LABEL_VALUE_SEPP);
        String cr1 = "mytestspec1";
        crList.add(cr1);

        String cr3 = "mytestspec3";

        ExternalCertificateSpec spec1 = ExternalCertificateSpec.builder().generatedSecretName(cr1).generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec1 = new ExternalCertificate();
        ec1.getMetadata().setName("mytestspec1");
        ec1.setSpec(spec1);
        ec1.getMetadata().setLabels(labels);
        handler.createCrWithAnnotations(spec1, "mytestspec1", null, labels, "certm-cr-handler-test").blockingAwait();

        ExternalCertificateSpec spec3 = ExternalCertificateSpec.builder().generatedSecretName(cr3).generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec3 = new ExternalCertificate();
        ec3.getMetadata().setName("mytestspec3");
        ec3.setSpec(spec3);
        ec3.getMetadata().setLabels(labels);
        handler.createCrWithAnnotations(spec3, "mytestspec3", null, labels, "certm-cr-handler-test").blockingAwait();

        handler.deleteUselessCr(crList);
        TimeUnit.SECONDS.sleep(1);
        Assert.assertNull(this.client.resources(ExternalCertificate.class).inNamespace(namespace).withName("mytestspec3").get());

        this.client.resource(ec1).inNamespace(this.namespace).delete();
    }

    @Test
    void checkCreationOfCRsMultiple() throws Exception
    {

        ExternalCertificateSpec spec = ExternalCertificateSpec.builder().generatedSecretName("test").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificateSpec spec2 = ExternalCertificateSpec.builder().generatedSecretName("test2").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("mytestspec");
        ec.setSpec(spec);

        ExternalCertificate ec2 = new ExternalCertificate();
        ec2.getMetadata().setName("mytestspec2");
        ec2.setSpec(spec2);

        handler.createCr(spec, "mytestspec").blockingAwait();
        handler.createCr(spec2, "mytestspec2").blockingAwait();

        Assert.assertEquals(spec, this.client.resource(ec).inNamespace(this.namespace).get().getSpec());
        Assert.assertEquals(spec2, this.client.resource(ec2).inNamespace(this.namespace).get().getSpec());

        this.client.resource(ec).inNamespace(this.namespace).delete();
        this.client.resource(ec2).inNamespace(this.namespace).delete();
    }

    @Test
    void checkCreationOfCRsFull() throws Exception
    {

        ExternalCertificateSpec spec = ExternalCertificateSpec.builder()
                                                              .generatedSecretName("test")
                                                              .generatedSecretType(GeneratedSecretTypeEnum.TLS)
                                                              .asymmetricKeyCertificateName("certName")
                                                              .asymmetricKeyName("keyName")
                                                              .crlFileName("crlName")
                                                              .trustedCertificateListName("listName")
                                                              .trustedCertificatesFileName("trustedFileName")
                                                              .build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("mytestspec");
        ec.setSpec(spec);

        handler.createCr(spec, "mytestspec").blockingAwait();

        Assert.assertEquals(spec, this.client.resource(ec).inNamespace(this.namespace).get().getSpec());

        this.client.resource(ec).inNamespace(this.namespace).delete();
    }

    @Test
    void checkDeletionOfCRs() throws Exception
    {

        ExternalCertificateSpec spec = ExternalCertificateSpec.builder().generatedSecretName("test").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificateSpec spec2 = ExternalCertificateSpec.builder().generatedSecretName("test2").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("mytestspec");
        ec.setSpec(spec);

        ExternalCertificate ec2 = new ExternalCertificate();
        ec2.getMetadata().setName("mytestspec2");
        ec2.setSpec(spec2);

        handler.createCr(spec, "mytestspec").blockingAwait();
        handler.createCr(spec2, "mytestspec2").blockingAwait();

        Assert.assertEquals(spec, this.client.resource(ec).inNamespace(this.namespace).get().getSpec());
        Assert.assertEquals(spec2, this.client.resource(ec2).inNamespace(this.namespace).get().getSpec());

        handler.deleteCr("mytestspec").blockingAwait();
        handler.deleteCr("mytestspec2").blockingAwait();

        Assert.assertEquals(null, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec").get());
        Assert.assertEquals(null, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec2").get());
    }

    @Test
    void checkDeletionOfCRsFails() throws Exception
    {
        AtomicBoolean flag = new AtomicBoolean(false);
        Assert.assertEquals(null, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("test").get());
        handler.deleteCr("test", 3000).doOnComplete(() ->
        {
        }).doOnError((e) ->
        {
            log.info("Deletion failed.");
            flag.set(true);
        }).onErrorComplete().blockingAwait();

        Assert.assertTrue(flag.get());
    }

    @Test
    void checkDeletionOfCRsFailsTimeout() throws Exception, KubernetesClientTimeoutException
    {
        AtomicBoolean flag = new AtomicBoolean(false);
        Assert.assertEquals(null, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("test").get());
        handler.deleteCr("test", 1).doOnComplete(() ->
        {
        }).doOnError((e) ->
        {
            log.info("Deletion failed {}.", e);
            flag.set(true);
        }).onErrorComplete().blockingAwait();

        Assert.assertTrue(flag.get());
    }

    @Test
    void checkUpdateOfCRsFull() throws Exception
    {

        ExternalCertificateSpec spec = ExternalCertificateSpec.builder()
                                                              .generatedSecretName("test")
                                                              .generatedSecretType(GeneratedSecretTypeEnum.TLS)
                                                              .asymmetricKeyCertificateName("certName")
                                                              .asymmetricKeyName("keyName")
                                                              .crlFileName("crlName")
                                                              .trustedCertificateListName("listName")
                                                              .trustedCertificatesFileName("trustedFileName")
                                                              .build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("mytestspec");
        ec.setSpec(spec);

        handler.createCr(spec, "mytestspec").blockingAwait();

        Assert.assertEquals(spec, this.client.resource(ec).inNamespace(this.namespace).get().getSpec());

        spec = ExternalCertificateSpec.builder()
                                      .generatedSecretName("updatedname")
                                      .generatedSecretType(GeneratedSecretTypeEnum.TLS)
                                      .asymmetricKeyCertificateName("updatedName")
                                      .asymmetricKeyName("updatedKeyName")
                                      .crlFileName("updatedCrlName")
                                      .trustedCertificateListName("UpdatedListName")
                                      .trustedCertificatesFileName("UpdatedTrustedFileName")
                                      .build();

        handler.updateCr(spec, "mytestspec").blockingAwait();

        Assert.assertEquals(spec, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec").get().getSpec());

        // test if the certificates created with Annotations are created properly
        Map<String, String> annotations = Map.of("myTestAnnotation", "myTestValue", "helm.sh/resource-policy", "keep");
        Map<String, String> labels = new HashMap<>();
        labels.put("myLabel", "myLabelValue");

        if (Boolean.TRUE.equals(handler.checkExistingCr("mytestspec")))
        {
            handler.updateCrWithAnnotations("mytestspec", annotations, labels, "certm-cr-handler-test");
            Assert.assertTrue(annotations.equals(this.client.resources(ExternalCertificate.class)
                                                            .inNamespace(this.namespace)
                                                            .withName("mytestspec")
                                                            .get()
                                                            .getMetadata()
                                                            .getAnnotations()));
        }

        this.client.resource(ec).inNamespace(this.namespace).delete();
    }
}
