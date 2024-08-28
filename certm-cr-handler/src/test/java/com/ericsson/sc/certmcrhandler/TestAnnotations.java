package com.ericsson.sc.certmcrhandler;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.Assert;

import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.Map;
import java.util.List;

import com.ericsson.sc.certmcrhandler.k8s.io.CrHandler;
import com.ericsson.sc.certmcrhandler.k8s.resource.ExternalCertificateSpec;
import com.ericsson.sc.certmcrhandler.k8s.resource.ExternalCertificate;
import com.ericsson.sc.certmcrhandler.k8s.resource.GeneratedSecretTypeEnum;

public class TestAnnotations
{

    private SetupTestEnv setup;
    private CrHandler handler;
    private String namespace;
    private String k8sConfig;
    private KubernetesClient client;

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

    @Test
    void checkAnnotationOfCRs() throws Exception
    {

        ExternalCertificateSpec spec = ExternalCertificateSpec.builder().generatedSecretName("test").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("mytestspec");
        ec.setSpec(spec);

        ExternalCertificateSpec spec2 = ExternalCertificateSpec.builder().generatedSecretName("test").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec2 = new ExternalCertificate();
        ec2.getMetadata().setName("mytestspec2");
        ec2.setSpec(spec2);

        ExternalCertificateSpec spec3 = ExternalCertificateSpec.builder().generatedSecretName("test").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec3 = new ExternalCertificate();
        ec3.getMetadata().setName("mytestspec3");
        ec3.setSpec(spec3);

        handler.createCr(spec, "mytestspec").blockingAwait();
        handler.createCr(spec2, "mytestspec2").blockingAwait();
        handler.createCr(spec3, "mytestspec3").blockingAwait();

        Assert.assertEquals(spec, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec").get().getSpec());
        Assert.assertEquals(spec2, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec2").get().getSpec());
        Assert.assertEquals(spec3, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec3").get().getSpec());

        Map<String, String> annotations = Map.of("myTestAnnotation", "myTestValue", "helm.sh/resource-policy", "keep");

        handler.annotate(List.of("mytestspec", "mytestspec2", "mytestspec3"), annotations).blockingAwait();

        Assert.assertEquals(annotations,
                            this.client.resources(ExternalCertificate.class)
                                       .inNamespace(this.namespace)
                                       .withName("mytestspec")
                                       .get()
                                       .getMetadata()
                                       .getAnnotations());
        Assert.assertEquals(annotations,
                            this.client.resources(ExternalCertificate.class)
                                       .inNamespace(this.namespace)
                                       .withName("mytestspec2")
                                       .get()
                                       .getMetadata()
                                       .getAnnotations());
        Assert.assertEquals(annotations,
                            this.client.resources(ExternalCertificate.class)
                                       .inNamespace(this.namespace)
                                       .withName("mytestspec3")
                                       .get()
                                       .getMetadata()
                                       .getAnnotations());

        this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec").delete();
        this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec2").delete();
        this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec3").delete();
    }

    @Test
    void checkAnnotationOfCRsWithInitialAnnotations() throws Exception
    {

        Map<String, String> initAnnotations = Map.of("myTestAnnotation", "myTestValue");
        Map<String, String> newAnnotations = Map.of("myTestAnnotation2", "myTestValue", "helm.sh/resource-policy", "keep");
        Map<String, String> completeAnnotations = Map.of("myTestAnnotation",
                                                         "myTestValue",
                                                         "myTestAnnotation2",
                                                         "myTestValue",
                                                         "helm.sh/resource-policy",
                                                         "keep");

        ExternalCertificateSpec spec = ExternalCertificateSpec.builder().generatedSecretName("test").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName("mytestspec");
        ec.getMetadata().setAnnotations(initAnnotations);
        ec.setSpec(spec);

        ExternalCertificateSpec spec2 = ExternalCertificateSpec.builder().generatedSecretName("test").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec2 = new ExternalCertificate();
        ec2.getMetadata().setName("mytestspec2");
        ec2.getMetadata().setAnnotations(initAnnotations);
        ec2.setSpec(spec2);

        ExternalCertificateSpec spec3 = ExternalCertificateSpec.builder().generatedSecretName("test").generatedSecretType(GeneratedSecretTypeEnum.TLS).build();
        ExternalCertificate ec3 = new ExternalCertificate();
        ec3.getMetadata().setName("mytestspec3");
        ec3.getMetadata().setAnnotations(initAnnotations);
        ec3.setSpec(spec3);

        this.client.resource(ec).inNamespace(this.namespace).create();
        this.client.resource(ec2).inNamespace(this.namespace).create();
        this.client.resource(ec3).inNamespace(this.namespace).create();

        Assert.assertEquals(spec, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec").get().getSpec());
        Assert.assertEquals(spec2, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec2").get().getSpec());
        Assert.assertEquals(spec3, this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec3").get().getSpec());

        handler.annotate(List.of("mytestspec", "mytestspec2", "mytestspec3"), newAnnotations).blockingAwait();

        Assert.assertEquals(completeAnnotations,
                            this.client.resources(ExternalCertificate.class)
                                       .inNamespace(this.namespace)
                                       .withName("mytestspec")
                                       .get()
                                       .getMetadata()
                                       .getAnnotations());
        Assert.assertEquals(completeAnnotations,
                            this.client.resources(ExternalCertificate.class)
                                       .inNamespace(this.namespace)
                                       .withName("mytestspec2")
                                       .get()
                                       .getMetadata()
                                       .getAnnotations());
        Assert.assertEquals(completeAnnotations,
                            this.client.resources(ExternalCertificate.class)
                                       .inNamespace(this.namespace)
                                       .withName("mytestspec3")
                                       .get()
                                       .getMetadata()
                                       .getAnnotations());

        this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec").delete();
        this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec2").delete();
        this.client.resources(ExternalCertificate.class).inNamespace(this.namespace).withName("mytestspec3").delete();
    }
}
