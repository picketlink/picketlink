package org.jboss.picketlink.cdi;

/**
 * Base class for security related exceptions
 * 
 * @author Shane Bryzak
 *
 */
public class SecurityException extends Throwable 
{
    private static final long serialVersionUID = -1809156359762519539L;

    public SecurityException(String message) 
    {
        super(message);
    }

    public SecurityException(String message, Throwable cause) 
    {
        super(message, cause);
    }
}
