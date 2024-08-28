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

package com.ericsson.sc.certmcrhandler.k8s.resource;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Kind;
import io.fabric8.kubernetes.model.annotation.Plural;
import io.fabric8.kubernetes.model.annotation.Singular;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("certm.sec.ericsson.com")
@Kind("ExternalCertificate")
@Plural("externalcertificates")
@Singular("externalcertificate")
public class ExternalCertificate extends CustomResource<ExternalCertificateSpec, ExternalCertificateStatus> implements Namespaced
{

    private static final long serialVersionUID = 1L;

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        String extCertName = getMetadata().getName();
        result = prime * result + ((extCertName == null) ? 0 : extCertName.hashCode());
        String extCertNamespace = getMetadata().getNamespace();
        result = prime * result + ((extCertNamespace == null) ? 0 : extCertNamespace.hashCode());
        result = prime * result + ((spec == null) ? 0 : spec.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExternalCertificate other = (ExternalCertificate) obj;

        if (this.getMetadata().getName() == null)
        {
            if (other.getMetadata().getName() != null)
                return false;
        }
        else if (!this.getMetadata().getName().equals(other.getMetadata().getName()))
            return false;

        if (spec == null)
        {
            if (other.spec != null)
                return false;
        }
        else if (!spec.equals(other.spec))
            return false;
        return true;
    }
}