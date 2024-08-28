package com.ericsson.supreme.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.supreme.kernel.CertificateTool;
import com.ericsson.supreme.kernel.Utils;

class CertificateGeneratorTest
{
    protected static final Logger log = LoggerFactory.getLogger(CertificateGeneratorTest.class);

    @Test
    void test() throws Exception
    {
        Integer bits = 3076;
        Integer expirationDays = 365;
        String caCommonName = "test.sepp.com";
        String certCommonName = "cert.sepp.com";
        var sans = List.of("*.test.sepp", certCommonName);

        var ca = CertificateGenerator.createCertificateAuthority("test", bits, expirationDays, caCommonName);
        var cert = CertificateGenerator.createCertificateSignedByRoot("test", bits, expirationDays, certCommonName, sans, ca);

        var notBefore = cert.getX509Certificate().getNotBefore();
        var notAfter = cert.getX509Certificate().getNotAfter();

        assertEquals(expirationDays, getDateDiff(notBefore, notAfter, TimeUnit.DAYS), "");

        assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + caCommonName, cert.getX509Certificate().getIssuerDN().toString());
        assertEquals("C=GE,L=Aachen,O=Ericsson,OU=EDD,CN=" + certCommonName, cert.getX509Certificate().getSubjectDN().toString());
        assertEquals(sans, Utils.getSubjectAltNames(cert.getX509Certificate()));
        assertEquals(bits, ((RSAPublicKey) cert.getX509Certificate().getPublicKey()).getModulus().bitLength());
        assertEquals(-1, cert.getX509Certificate().getBasicConstraints());
        assertDoesNotThrow(() -> cert.getX509Certificate().verify(ca.getX509Certificate().getPublicKey(), CertificateTool.BC_PROVIDER));

        System.out.println(Utils.toBase64(Utils.toPkcs12(cert)));
        // Files.write(cert.getPkcs12Base64Format().getBytes(), new File("./lala"));

    }

    public static Integer getDateDiff(Date date1,
                                      Date date2,
                                      TimeUnit timeUnit)
    {
        long diffInMillies = date2.getTime() - date1.getTime();
        return (int) (timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS));
    }
}
