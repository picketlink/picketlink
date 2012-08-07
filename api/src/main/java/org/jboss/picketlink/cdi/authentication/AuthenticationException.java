package org.jboss.picketlink.cdi.authentication;

/**
 * Thrown if there is an error during the authentication process
 * 
 * @author Shane Bryzak
 */
public class AuthenticationException extends SecurityException
{
    private static final long serialVersionUID = -7486433031372506270L;

    public AuthenticationException(String message) 
    {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) 
    {
        super(message, cause);
    }
}
