package org.picketlink.identity.federation.core.wstrust.exceptions;

public class SamlCredentialParseException extends WSTrustGeneralException
{
    private static final long serialVersionUID = 8877976632951911364L;

    public SamlCredentialParseException()
    {
        super();
    }

    public SamlCredentialParseException(final String message, final Throwable cause)
    {
        super(message, cause);
    }

    public SamlCredentialParseException(final String message)
    {
        super(message);
    }

    public SamlCredentialParseException(final Throwable cause)
    {
        super(cause);
    }

}
