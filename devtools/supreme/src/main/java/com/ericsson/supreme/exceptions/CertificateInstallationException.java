package com.ericsson.supreme.exceptions;

public class CertificateInstallationException extends Exception
{

    private static final long serialVersionUID = 1L;

    public CertificateInstallationException(String msg,
                                            Throwable e)
    {
        super(msg, e);
    }

    public CertificateInstallationException(String msg)
    {
        super(msg);
    }

}
