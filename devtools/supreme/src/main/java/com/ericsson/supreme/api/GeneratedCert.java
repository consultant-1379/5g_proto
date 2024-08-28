package com.ericsson.supreme.api;

import java.io.IOException;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX500NameUtil;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.exceptions.CertificateIOException;
import com.ericsson.supreme.kernel.CertificateTool;
import com.ericsson.supreme.kernel.Utils;
import com.ericsson.utilities.file.KeyCert;
import com.ericsson.utilities.file.TrustedCert;

// To create a certificate chain we need the issuers' certificate and private key. Keep these together to pass around
public final class GeneratedCert
{
    protected static final Logger log = LoggerFactory.getLogger(GeneratedCert.class);

    private final String name;
    private final PrivateKey privateKey;
    private final X509Certificate certificate;
    private final byte[] pkcs12;
    private final String pkcs12InBase64Format;
    private final String base64Cert;
    private final String base64PrivateKey;
    private final String pemCert;
    private final String pemPrivateKey;

    public GeneratedCert(String name,
                         PrivateKey privateKey,
                         X509Certificate certificate) throws CertificateIOException
    {
        this.name = name;
        this.privateKey = privateKey;
        this.certificate = certificate;
        this.base64Cert = Utils.toBase64(Utils.toPemString(this.certificate));
        this.base64PrivateKey = Utils.toBase64(Utils.toPemString(this.privateKey));
        this.pkcs12 = Utils.toPkcs12(this);
        this.pkcs12InBase64Format = Utils.toBase64(Utils.toPkcs12(this));
        this.pemCert = Utils.toPemString(this.certificate);
        var sw = new StringWriter();
        try (var jcaPEMWriter = new JcaPEMWriter(sw);)
        {
            jcaPEMWriter.writeObject(new JcaPKCS8Generator(privateKey, null));
            jcaPEMWriter.flush();
            this.pemPrivateKey = sw.toString();
        }
        catch (IOException e)
        {
            throw (new CertificateIOException("Error for GeneratedCertificate " + name + ". Could not convert private key to PKCS8.", e));
        }
    }

    public String getCertificate()
    {
        return this.pemCert;
    }

    public String getPrivateKey()
    {
        return this.pemPrivateKey;
    }

    public String getBase64Certificate()
    {
        return this.base64Cert;
    }

    public String getBase64PrivateKey()
    {
        return this.base64PrivateKey;
    }

    public PrivateKey getPrivateKeyPrivateKey()
    {
        return privateKey;
    }

    public String getName()
    {
        return this.name;
    }

    public X509Certificate getX509Certificate()
    {
        return certificate;
    }

    public byte[] getPkcs12()
    {
        return this.pkcs12;
    }

    public String getPkcs12Base64Format()
    {
        return this.pkcs12InBase64Format;
    }

    public X500Name getSubject()
    {
        return JcaX500NameUtil.getSubject(this.certificate);
    }

    public X500Name getIssuer()
    {
        return JcaX500NameUtil.getIssuer(this.certificate);
    }

    public KeyPair getKeyPair()
    {
        return new KeyPair(this.certificate.getPublicKey(), this.getPrivateKeyPrivateKey());
    }

    public String getPassword()
    {
        return CertificateTool.PASSWORD;
    }

    public KeyCert toKeyCert()
    {
        var cert = this.pemCert;
        var key = this.pemPrivateKey;
        return new KeyCert()
        {

            @Override
            public String getPrivateKey()
            {
                return key;
            }

            @Override
            public String getCertificate()
            {
                return cert;
            }
        };
    }

    public TrustedCert toTrustedCert()
    {
        var cert = this.pemCert;
        return () -> new ArrayList<>(List.of(cert));

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((base64Cert == null) ? 0 : base64Cert.hashCode());
        result = prime * result + ((base64PrivateKey == null) ? 0 : base64PrivateKey.hashCode());
        result = prime * result + ((certificate == null) ? 0 : certificate.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((privateKey == null) ? 0 : privateKey.hashCode());
        return result;
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
        GeneratedCert other = (GeneratedCert) obj;
        if (base64Cert == null)
        {
            if (other.base64Cert != null)
                return false;
        }
        else if (!base64Cert.equals(other.base64Cert))
            return false;
        if (base64PrivateKey == null)
        {
            if (other.base64PrivateKey != null)
                return false;
        }
        else if (!base64PrivateKey.equals(other.base64PrivateKey))
            return false;
        if (certificate == null)
        {
            if (other.certificate != null)
                return false;
        }
        else if (!certificate.equals(other.certificate))
            return false;
        if (name == null)
        {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        if (privateKey == null)
        {
            if (other.privateKey != null)
                return false;
        }
        else if (!privateKey.equals(other.privateKey))
            return false;
        return true;
    }

}