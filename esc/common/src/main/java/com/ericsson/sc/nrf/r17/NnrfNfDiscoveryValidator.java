/**
 * COPYRIGHT ERICSSON GMBH 2023
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: May 3, 2023
 *     Author: eedstl
 */

package com.ericsson.sc.nrf.r17;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import com.ericsson.cnal.common.OpenApiObjectMapper;
import com.ericsson.cnal.openapi.r17.ts29510.nnrf.nfmanagement.NFType;
import com.ericsson.sc.glue.IfAddress;
import com.ericsson.sc.glue.IfDiscoveredNfInstance;
import com.ericsson.sc.glue.IfDiscoveredScpInstance;
import com.ericsson.sc.glue.IfMultipleIpEndpoint;
import com.ericsson.sc.glue.IfTypedNfService;
import com.ericsson.sc.glue.IfTypedScpDomainInfo;
import com.ericsson.sc.nfm.model.Scheme;
import com.ericsson.sc.nfm.model.Transport;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class NnrfNfDiscoveryValidator
{
    @JsonPropertyOrder({ "nfInstanceId", "details" })
    public static class ValidationError
    {

        @JsonProperty("details")
        private String details;

        @JsonProperty("nfInstanceId")
        private String nfInstanceId;

        public ValidationError details(final String details)
        {
            this.details = details;
            return this;
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other == this)
                return true;

            if (!(other instanceof ValidationError))
                return false;

            final ValidationError that = (ValidationError) other;

            return (this.getNfInstanceId() == that.getNfInstanceId() || this.getNfInstanceId() != null && this.getNfInstanceId().equals(that.getNfInstanceId()))
                   && (this.getDetails() == that.getDetails() || this.getDetails() != null && this.getDetails().equals(that.getDetails()));
        }

        public String getDetails()
        {
            return this.details;
        }

        public String getNfInstanceId()
        {
            return this.nfInstanceId;
        }

        @Override
        public int hashCode()
        {
            int result = 1;
            result = result * 31 + (this.getNfInstanceId() == null ? 0 : this.getNfInstanceId().hashCode());
            result = result * 31 + (this.getDetails() == null ? 0 : this.getDetails().hashCode());
            return result;
        }

        public ValidationError nfInstanceId(final String nfInstanceId)
        {
            this.nfInstanceId = nfInstanceId;
            return this;
        }

        @Override
        public String toString()
        {
            try
            {
                return json.writeValueAsString(this);
            }
            catch (JsonProcessingException e)
            {
                return e.toString();
            }
        }
    }

    private static final String INVALID_PARAMETER_VALUE = "Invalid parameter value: ";
    private static final String MISSING_MANDATORY_PARAMETER = "Missing mandatory parameter: ";

    // Regex to match valid FQDNs, taken from CPI
    private static final String REGEX_FQDN = "((([a-zA-Z0-9]([a-zA-Z0-9\\-]){0,61})?[a-zA-Z0-9]\\.)*([a-zA-Z0-9]([a-zA-Z0-9\\-]){0,61})?[a-zA-Z0-9]\\.?)|\\.";
    static final Pattern patternFqdn = Pattern.compile(REGEX_FQDN);

    // Regex to match valid IPv4 addresses, taken from Android source code
    // https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/util/Patterns.java#120
    // Apache 2.0 license
    private static final String REGEX_IPV4 = "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                                             + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                                             + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}" + "|[1-9][0-9]|[0-9]))";
    private static final Pattern patternIpv4 = Pattern.compile(REGEX_IPV4);
    // Regex to match valid IPv6 addresses, taken from:
    // https://stackoverflow.com/questions/53497/regular-expression-that-matches-valid-ipv6-addresses
    private static final String REGEX_IPV6 = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";
    private static final Pattern patternIpv6 = Pattern.compile(REGEX_IPV6);

    private static final ObjectMapper json = OpenApiObjectMapper.singleton();
    private static final NnrfNfDiscoveryValidator singleton = new NnrfNfDiscoveryValidator();

    public static NnrfNfDiscoveryValidator singleton()
    {
        return singleton;
    }

    private final Subject<List<ValidationError>> validationErrorsStream = BehaviorSubject.createDefault(List.of());

    private AtomicReference<List<ValidationError>> validationErrors = new AtomicReference<>(new ArrayList<>());

    private NnrfNfDiscoveryValidator()
    {
    }

    public Subject<List<ValidationError>> getValidationErrorsStream()
    {
        return this.validationErrorsStream;
    }

    public void publish()
    {
        this.validationErrorsStream.toSerialized().onNext(this.validationErrors.getAndSet(new ArrayList<>()));
    }

    public boolean validateNfInstance(final IfDiscoveredNfInstance i)
    {
        final StringBuilder b = new StringBuilder();

        if (i.getNfInstanceId() == null)
        {
            b.append(MISSING_MANDATORY_PARAMETER).append("'nfInstanceId'.");
            this.validationErrors.get().add(new ValidationError().nfInstanceId("UNKNOWN").details(b.toString()));
            return false;
        }

        try
        {
            UUID.fromString(i.getNfInstanceId());
        }
        catch (IllegalArgumentException e)
        {
            b.append(INVALID_PARAMETER_VALUE).append("'nfInstanceId' must be of type UUID.");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(i.getNfInstanceId()).details(b.toString()));
            return false;
        }

        if (i.getNfStatus() == null)
        {
            b.append(MISSING_MANDATORY_PARAMETER).append("'nfStatus'.");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(i.getNfInstanceId()).details(b.toString()));
            return false;
        }

        if (i.getNfType() == null)
        {
            b.append(MISSING_MANDATORY_PARAMETER).append("'nfType'.");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(i.getNfInstanceId()).details(b.toString()));
            return false;
        }

        // NF instance without NF services only allowed for SCP or SEPP.
        if (i.getDiscoveredNfService().isEmpty() && (!NFType.SCP.equalsIgnoreCase(i.getNfType()) || !NFType.SEPP.equalsIgnoreCase(i.getNfType())))
        {
            b.append(MISSING_MANDATORY_PARAMETER).append("'discoveredNfService'.");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(i.getNfInstanceId()).details(b.toString()));
            return false;
        }

        for (IfTypedNfService s : i.getDiscoveredNfService())
        {
            if (!this.validateNfService(i.getNfInstanceId(), s))
                return false;
        }

        return true;
    }

    public boolean validateScpInstance(final IfDiscoveredScpInstance i)
    {
        final StringBuilder b = new StringBuilder();

        if (i.getNfInstanceId() == null)
        {
            b.append(MISSING_MANDATORY_PARAMETER).append("'nfInstanceId'.");
            this.validationErrors.get().add(new ValidationError().nfInstanceId("UNKNOWN").details(b.toString()));
            return false;
        }

        try
        {
            UUID.fromString(i.getNfInstanceId());
        }
        catch (IllegalArgumentException e)
        {
            b.append(INVALID_PARAMETER_VALUE).append("'nfInstanceId' must be of type UUID.");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(i.getNfInstanceId()).details(b.toString()));
            return false;
        }

        if (i.getNfStatus() == null)
        {
            b.append(MISSING_MANDATORY_PARAMETER).append("'nfStatus'.");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(i.getNfInstanceId()).details(b.toString()));
            return false;
        }

        if (i.getNfType() == null)
        {
            b.append(MISSING_MANDATORY_PARAMETER).append("'nfType'.");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(i.getNfInstanceId()).details(b.toString()));
            return false;
        }

        for (IfTypedScpDomainInfo di : i.getDiscoveredScpDomainInfo())
        {
            if (!this.validateScpDomainInfo(i.getNfInstanceId(), di))
                return false;
        }

        return true;
    }

    public boolean validateNfService(final String nfInstanceId,
                                     final IfTypedNfService s)
    {
        final StringBuilder b = new StringBuilder();

        if (s.getCapacity() != null && (s.getCapacity() < 0 || s.getCapacity() > 65535))
        {
            b.append(INVALID_PARAMETER_VALUE).append("'capacity' must be in the range 0..65535': '").append(s.getCapacity()).append("'");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
            return false;
        }

        if (s.getPriority() != null && (s.getPriority() < 0 || s.getPriority() > 65535))
        {
            b.append(INVALID_PARAMETER_VALUE).append("'priority' must be in the range 0..65535': '").append(s.getPriority()).append("'");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
            return false;
        }

        if (s.getAddress() != null)
        {
            final IfAddress a = s.getAddress();

            if (a.getFqdn() != null && !patternFqdn.matcher(a.getFqdn()).matches())
            {
                b.append(INVALID_PARAMETER_VALUE).append("'fqdn' must comply to an FQDN but not contain '_': '").append(a.getFqdn()).append("'");
                this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
                return false;
            }

            if (a.getInterPlmnFqdn() != null && !patternFqdn.matcher(a.getInterPlmnFqdn()).matches())
            {
                b.append(INVALID_PARAMETER_VALUE)
                 .append("'interPlmnFqdn' must comply to an FQDN but not contain '_': '")
                 .append(a.getInterPlmnFqdn())
                 .append("'");
                this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
                return false;
            }

            if (a.getScheme() != null && !a.getScheme().equals(Scheme.HTTP) && !a.getScheme().equals(Scheme.HTTPS))
            {
                b.append(INVALID_PARAMETER_VALUE).append("'scheme' must be one of 'http' or 'https': '").append(a.getScheme()).append("'");
                this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
                return false;
            }

            for (IfMultipleIpEndpoint ep : a.getMultipleIpEndpoint())
            {
                if (ep.getPort() != null && (ep.getPort() < 0 || ep.getPort() > 65535))
                {
                    b.append(INVALID_PARAMETER_VALUE).append("'port' must be in the range 0..65535': '").append(ep.getPort()).append("'");
                    this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
                    return false;
                }

                if (ep.getTransport() != null && !ep.getTransport().equals(Transport.TCP))
                {
                    b.append(INVALID_PARAMETER_VALUE).append("'transport' must be 'tcp': '").append(a.getScheme()).append("'");
                    this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
                    return false;
                }
            }
        }

        return true;
    }

    public boolean validateScpDomainInfo(final String nfInstanceId,
                                         final IfTypedScpDomainInfo di)
    {
        final StringBuilder b = new StringBuilder();

        if (di.getCapacity() != null && (di.getCapacity() < 0 || di.getCapacity() > 65535))
        {
            b.append(INVALID_PARAMETER_VALUE).append("'capacity' must be in the range 0..65535': '").append(di.getCapacity()).append("'");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
            return false;
        }

        if (di.getPriority() != null && (di.getPriority() < 0 || di.getPriority() > 65535))
        {
            b.append(INVALID_PARAMETER_VALUE).append("'priority' must be in the range 0..65535': '").append(di.getPriority()).append("'");
            this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
            return false;
        }

        if (di.getAddress() != null)
        {
            final IfAddress a = di.getAddress();

            if (a.getFqdn() != null && !patternFqdn.matcher(a.getFqdn()).matches())
            {
                b.append(INVALID_PARAMETER_VALUE).append("'fqdn' must comply to an FQDN but not contain '_': '").append(a.getFqdn()).append("'");
                this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
                return false;
            }

            if (a.getInterPlmnFqdn() != null && !patternFqdn.matcher(a.getInterPlmnFqdn()).matches())
            {
                b.append(INVALID_PARAMETER_VALUE)
                 .append("'interPlmnFqdn' must comply to an FQDN but not contain '_': '")
                 .append(a.getInterPlmnFqdn())
                 .append("'");
                this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
                return false;
            }

            if (a.getScheme() != null && !a.getScheme().equals(Scheme.HTTP) && !a.getScheme().equals(Scheme.HTTPS))
            {
                b.append(INVALID_PARAMETER_VALUE).append("'scheme' must be one of 'http' or 'https': '").append(a.getScheme()).append("'");
                this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
                return false;
            }

            for (IfMultipleIpEndpoint ep : a.getMultipleIpEndpoint())
            {
                if (ep.getPort() != null && (ep.getPort() < 0 || ep.getPort() > 65535))
                {
                    b.append(INVALID_PARAMETER_VALUE).append("'port' must be in the range 0..65535': '").append(ep.getPort()).append("'");
                    this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
                    return false;
                }

                if (ep.getTransport() != null && !ep.getTransport().equals(Transport.TCP))
                {
                    b.append(INVALID_PARAMETER_VALUE).append("'transport' must be 'tcp': '").append(a.getScheme()).append("'");
                    this.validationErrors.get().add(new ValidationError().nfInstanceId(nfInstanceId).details(b.toString()));
                    return false;
                }
            }
        }

        return true;
    }
}
