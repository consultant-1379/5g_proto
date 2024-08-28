package com.ericsson.supreme.config;

import java.util.List;
import java.util.stream.Collectors;

public class Generation
{
    private List<ClientCertificate> certificates;
    private List<CertificateAuthority> certificateAuthorities;

    public List<ClientCertificate> getCertificates()
    {
        return certificates;
    }

    public void setCertificates(List<ClientCertificate> certificates)
    {
        this.certificates = certificates;
    }

    public List<CertificateAuthority> getCertificateAuthorities()
    {
        return certificateAuthorities;
    }

    public void setCertificateAuthorities(List<CertificateAuthority> certificateAuthorities)
    {
        this.certificateAuthorities = certificateAuthorities;
    }

    public String toString()
    {
        var sb = new StringBuilder();
        sb.append("\n  certificates: ");
        sb.append(certificates);
        sb.append("\n  certificateAuthorities: ");
        sb.append(certificateAuthorities);
        return sb.toString();
    }

    public void validate()
    {
        if (certificateAuthorities != null)
        {
            certificateAuthorities.forEach(CertificateAuthority::validate);
        }

        if (certificates != null)
        {
            List<String> storedCAs = certificateAuthorities != null ? certificateAuthorities.stream().map(ca -> ca.getName()).collect(Collectors.toList())
                                                                    : List.of();
            certificates.forEach(cc -> cc.validate(storedCAs));
        }
    }
}
