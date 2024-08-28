package com.ericsson.utilities.json;

import java.io.IOException;

public class JacksonException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public JacksonException(IOException cause)
    {
        super(cause);
    }

    public JacksonException(String message,
                            IOException cause)
    {
        super(message, cause);
    }
}
