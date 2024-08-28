package com.ericsson.adpal.cm;

/**
 * Indicates a failed operation due to eTag mismatch or other concurrent updates
 */
public class CmmTransactionException extends CmmApiException
{
    private static final long serialVersionUID = 1L;

    public CmmTransactionException(String msg,
                                   int statusCode,
                                   String messageBody)
    {
        super(msg, statusCode, messageBody);
    }

}
