package com.ericsson.sc.rxkms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KmsException extends RuntimeException
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

    public KmsException(String msg,
                        int statusCode,
                        String messageBody)
    {
        super(msg + " statusCode: " + statusCode + " message:" + messageBody);
        this.statusCode = statusCode;
        this.messageBody = messageBody;
    }

    public KmsException(String msg)
    {
        super(msg);
        this.statusCode = -1;
        this.messageBody = "";
    }
}
