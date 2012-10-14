package org.picketlink.authentication;

/**
 * 
 * 
 * @author Shane Bryzak
 *
 */
public class UnexpectedCredentialException extends AuthenticationException
{
    private static final long serialVersionUID = 4827200587997989123L;

    public UnexpectedCredentialException(String message)
    {
        super(message);
    }

    @SuppressWarnings("UnusedDeclaration")
    public UnexpectedCredentialException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
