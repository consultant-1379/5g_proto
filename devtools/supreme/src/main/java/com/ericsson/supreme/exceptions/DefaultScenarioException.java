package com.ericsson.supreme.exceptions;

public class DefaultScenarioException extends Exception
{

    private static final long serialVersionUID = 1L;

    public DefaultScenarioException(String msg)
    {
        super(msg);
    }

    public DefaultScenarioException(String msg,
                                    Throwable e)
    {
        super(msg, e);
    }
}
