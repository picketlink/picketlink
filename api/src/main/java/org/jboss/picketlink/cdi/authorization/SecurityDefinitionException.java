package org.jboss.picketlink.cdi.authorization;

import org.jboss.picketlink.cdi.SecurityException;

/**
 * This exception is thrown when a security-related configuration error is detected,
 * such as a missing or ambiguous security binding type
 * 
 * @author Shane Bryzak
 */
public class SecurityDefinitionException extends SecurityException
{
    private static final long serialVersionUID = -5683365417825375411L;

    public SecurityDefinitionException(String message) 
    {
        super(message);
    }

    public SecurityDefinitionException(String message, Throwable cause) 
    {
        super(message, cause);
    }
}
