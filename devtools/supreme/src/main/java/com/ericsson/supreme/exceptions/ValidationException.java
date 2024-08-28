package com.ericsson.supreme.exceptions;

public class ValidationException extends RuntimeException
{

    private static final long serialVersionUID = 1L;

    public ValidationException(String msg,
                               Throwable e,
                               Object... args)
    {
        super(String.format(msg, args), e);
    }

    public ValidationException(String msg,
                               Object... args)
    {
        super(String.format(msg, args));
    }

    public ValidationException(String msg,
                               Throwable e)
    {
        super(msg, e);
    }

}
