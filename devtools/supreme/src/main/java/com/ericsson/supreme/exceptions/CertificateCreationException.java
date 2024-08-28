package com.ericsson.supreme.exceptions;

public class CertificateCreationException extends Exception
{

    private static final long serialVersionUID = 1L;

    public CertificateCreationException(String msg,
                                        Throwable e)
    {
        super(msg, e);
    }

    public CertificateCreationException(String msg)
    {
        super(msg);
    }

}
