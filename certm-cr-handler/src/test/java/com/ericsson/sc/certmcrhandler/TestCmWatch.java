package com.ericsson.sc.certmcrhandler;

//import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.Assert;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.reactivex.Flowable;

import com.ericsson.sc.certmcrhandler.k8s.io.CrHandler;
import com.ericsson.sc.certmcrhandler.k8s.resource.*;

import java.util.concurrent.TimeUnit;
//

public class TestCmWatch
{

    private SetupTestEnv setup;
    private CrHandler handler;
    private String namespace;
    private String k8sConfig;
    private KubernetesClient client;

    // The order of these testcases is important. They can be refactored to be
    // agnostic of execution sequence but the execution will require much time.

    @BeforeClass
    void before() throws Exception
    {

        this.setup = new SetupTestEnv();
        setup.setupComplete();
        this.namespace = setup.getNamespace();
        this.k8sConfig = setup.getK3sKubeConfig();
        this.client = setup.getClient();

        this.handler = new CrHandler(this.k8sConfig, this.namespace);
    }

    @AfterClass
    void after()
    {
        setup.cleanUpComplete();
    }

    void createCm() throws Exception
    {
        setup.addConfigMap("/configMap.yaml");
        ExternalCertificateSpec spec = ExternalCertificateSpec.builder()
                                                              .generatedSecretName("testsecretname")
                                                              .generatedSecretType(GeneratedSecretTypeEnum.TLS)
                                                              .build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("testsecretname");
        ec.setSpec(spec);

        boolean flag = Flowable.interval(15, TimeUnit.SECONDS).takeUntil(tick ->
        {
            return this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("testsecretname").get() != null;
        }).ignoreElements().blockingAwait(180, TimeUnit.SECONDS);

        Assert.assertTrue(flag);
        Assert.assertEquals(spec, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("testsecretname").get().getSpec());
    }

    void updateCm() throws Exception
    {
        setup.updateConfigMap("/configMap_update_add.yaml");
        boolean flag = Flowable.interval(15, TimeUnit.SECONDS).takeUntil(tick ->
        {
            return this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("testsecretname").get() != null
                   && this.client.resources(ExternalCertificate.class)
                                 .inNamespace(this.namespace)
                                 .withName("testsecretname")
                                 .get()
                                 .getSpec()
                                 .getGeneratedSecretType()
                                 .equals(GeneratedSecretTypeEnum.OPAQUE)
                   && this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("testsecretname2").get() != null;
        }).ignoreElements().blockingAwait(180, TimeUnit.SECONDS);

        Assert.assertTrue(flag);

        ExternalCertificateSpec spec = ExternalCertificateSpec.builder()
                                                              .generatedSecretName("testsecretname")
                                                              .generatedSecretType(GeneratedSecretTypeEnum.OPAQUE)
                                                              .build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("testsecretname");
        ec.setSpec(spec);

        Assert.assertEquals(spec, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("testsecretname").get().getSpec());

        spec = ExternalCertificateSpec.builder().generatedSecretName("testsecretname2").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ec = new ExternalCertificate();
        ec.getMetadata().setName("testsecretname2");
        ec.setSpec(spec);

        Assert.assertEquals(spec, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("testsecretname2").get().getSpec());
    }

    void deleteCm() throws Exception
    {
        setup.updateConfigMap("/configMap_delete_update.yaml");
        boolean flag = Flowable.interval(15, TimeUnit.SECONDS).takeUntil(tick ->
        {
            return this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("testsecretname").get() != null
                   && this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("testsecretname2").get() == null;
        }).ignoreElements().blockingAwait(180, TimeUnit.SECONDS);

        Assert.assertTrue(flag);

        ExternalCertificateSpec spec = ExternalCertificateSpec.builder()
                                                              .generatedSecretName("testsecretname")
                                                              .generatedSecretType(GeneratedSecretTypeEnum.TLS)
                                                              .build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("testsecretname");
        ec.setSpec(spec);

        Assert.assertEquals(spec, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("testsecretname").get().getSpec());

        Assert.assertEquals(null, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("testsecretname2").get());
    }

    /*
     * @Test Τest exist for implmentation that you do not use
     */
    void checkCmCreated() throws Exception
    {
        createCm();
        setup.deleteConfigMap();
        Thread.sleep(1000 * 15);

    }

    /*
     * @Test Τest exist for implmentation that you do not use
     */
    void checkCmUpdated() throws Exception
    {
        createCm();
        updateCm();
        setup.deleteConfigMap();
        Thread.sleep(1000 * 15);

    }

    /*
     * @Test Τest exist for implmentation that you do not use
     */
    void checkCmDelete() throws Exception
    {
        createCm();
        deleteCm();
        setup.deleteConfigMap();
        Thread.sleep(1000 * 15);

    }

    /*
     * @Test Τest exist for implmentation that you do not use
     */
    void checkCmDelay() throws Exception
    {
        Thread.sleep(1000 * 150);
        createCm();
        setup.deleteConfigMap();
        Thread.sleep(1000 * 15);
    }

    /*
     * @Test Τest exist for implmentation that you do not use
     */
    void checkCmMalformed() throws Exception
    {
        setup.addConfigMap("/configMapMalformed.yaml");
        Thread.sleep(1000 * 60);
        setup.deleteConfigMap();
        Thread.sleep(1000 * 15);
        createCm();
        setup.deleteConfigMap();
        Thread.sleep(1000 * 15);

    }

}
