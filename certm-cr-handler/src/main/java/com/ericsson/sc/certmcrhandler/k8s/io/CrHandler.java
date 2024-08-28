/**
 * COPYRIGHT ERICSSON GMBH 2024
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Mar 27, 2024
 *     Author: Avengers
 */

package com.ericsson.sc.certmcrhandler.k8s.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.sc.certmcrhandler.k8s.resource.ExternalCertificate;
import com.ericsson.sc.certmcrhandler.k8s.resource.ExternalCertificateSpec;
import com.ericsson.utilities.common.EnvVars;

import io.fabric8.kubernetes.api.model.OwnerReference;
import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CrHandler
{
    private K8sClient client;
    private static final Logger log = LoggerFactory.getLogger(CrHandler.class);
    private static final String LABEL_KEY = "app.kubernetes.io/managed-by";
    private static final String LABEL_VALUE_SEPP = "sepp-manager";
    private static final String CR_PREFIX = EnvVars.get("GLOBAL_ERIC_SEPP_NAME");
    private static final String CR_PREFIX_MANAGER = CR_PREFIX + "-manager";

    public CrHandler(String k8sConfig,
                     String namespace)
    {
        this.client = new K8sClient(k8sConfig, namespace);
    }

    public CrHandler()
    {
        this.client = new K8sClient();
    }

    public Completable createCr(ExternalCertificateSpec spec,
                                String name)
    {
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName(name.toLowerCase());
        ec.setSpec(spec);
        return this.client.createCr(ec);
    }

    public Completable createCrWithAnnotations(ExternalCertificateSpec spec,
                                               String name,
                                               Map<String, String> annotations,
                                               Map<String, String> labels,
                                               String prefix)
    {
        ExternalCertificate ec = new ExternalCertificate();
        OwnerReference ownerRef = new OwnerReference();
        ec.getMetadata().setName(name.toLowerCase());
        ec.getMetadata().setAnnotations(annotations);
        ec.getMetadata().setLabels(labels);
        ownerRef.setApiVersion(client.getDeploymentApiVersion(prefix));
        ownerRef.setBlockOwnerDeletion(true);
        ownerRef.setKind("Deployment");
        ownerRef.setController(false);
        ownerRef.setName(prefix);
        ownerRef.setUid(client.getDeploymentUid(prefix));
        ec.getMetadata().setOwnerReferences(List.of(ownerRef));
        ec.setSpec(spec);
        return this.client.createCr(ec);
    }

    public Completable deleteCr(String name)
    {
        return this.deleteCr(name, 0);
    }

    public Completable deleteCr(String name,
                                long millis)
    {
        return this.client.deleteCr(name, millis);
    }

    public Completable updateCr(ExternalCertificateSpec spec,
                                String name)
    {
        ExternalCertificate ec = new ExternalCertificate();
        ec.getMetadata().setName(name.toLowerCase());
        ec.setSpec(spec);
        return this.client.updateCr(ec);
    }

    public Completable updateCrWithAnnotations(String name,
                                               Map<String, String> annotations,
                                               Map<String, String> labels,
                                               String prefix)
    {
        ExternalCertificate ec = this.client.getResourceByName(name);
        ec.getMetadata().setName(name.toLowerCase());
        ec.getMetadata().setAnnotations(annotations);
        ec.getMetadata().setLabels(labels);
        ec.getMetadata().getOwnerReferences().get(0).setName(prefix);
        return this.client.updateCr(ec);
    }

    public Boolean checkExistingCr(String name)
    {
        return this.client.checkResourceLabel(name, LABEL_KEY, LABEL_VALUE_SEPP);
    }

    public void deleteUselessCr(List<String> crList) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        log.debug("Start deleting useless CRs");
        List<String> extCertListNames = client.getExtCertNamesWithLabel(LABEL_KEY, LABEL_VALUE_SEPP);
        log.debug("external certificates made by sepp-manager: {}", extCertListNames);
        if (!extCertListNames.isEmpty())
        {
            List<String> difElementsList = extCertListNames.stream().filter(el -> !crList.contains(el)).toList();
            log.debug("elements to be deleted: {}", difElementsList);
            if (!difElementsList.isEmpty())
            {
                difElementsList.forEach(el -> deleteCr(el, 3000).subscribeOn(Schedulers.io()).subscribe());
            }
        }
    }

    public Completable annotate(List<String> names,
                                Map<String, String> annotations)
    {
        ArrayList<Completable> completables = new ArrayList<>();
        names.forEach(name ->
        {
            ExternalCertificate ec = this.client.getResourceByName(name);
            Map<String, String> cAnnotations = ec.getMetadata().getAnnotations();
            cAnnotations.putAll(annotations);
            ec.getMetadata().setAnnotations(cAnnotations);
            completables.add(this.client.updateCr(ec));
        });
        return Completable.merge(completables);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(client);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CrHandler other = (CrHandler) obj;
        return Objects.equals(client, other.client);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "CrHandler [client=" + client + "]";
    }

}
