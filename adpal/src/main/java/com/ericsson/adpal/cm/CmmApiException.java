package com.ericsson.adpal.cm;

/**
 * Indicates a failed CM API operation
 */
public class CmmApiException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private final int statusCode;
    private final String messageBody;

    /**
     * 
     * @return The HTTP status code
     */
    public int statusCode()
    {
        return statusCode;
    }

    /**
     * 
     * @return The error message body
     */
    public String getMessageBody()
    {
        return messageBody;
    }

    public CmmApiException(String msg,
                           int statusCode,
                           String messageBody)
    {
        super(msg + " statusCode: " + statusCode + " message:" + messageBody);
        this.statusCode = statusCode;
        this.messageBody = messageBody;
    }
}
