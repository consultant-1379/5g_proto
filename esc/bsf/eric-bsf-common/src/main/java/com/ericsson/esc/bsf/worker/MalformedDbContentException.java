package com.ericsson.esc.bsf.worker;

/**
 * Thrown whenever BSF database contains an invalid entry. This should normally
 * not happen unless the database has been manipulated outside BSF
 */
public class MalformedDbContentException extends RuntimeException
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MalformedDbContentException(String msg,
                                       Throwable reason)
    {
        super(msg, reason);
    }

}
