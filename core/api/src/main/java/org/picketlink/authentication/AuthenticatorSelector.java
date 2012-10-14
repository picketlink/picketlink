package org.picketlink.authentication;

/**
 * Selects which Authenticator implementation is used to manage the authentication process 
 * 
 * @author Shane Bryzak
 */
public interface AuthenticatorSelector
{
    Class<? extends Authenticator> getAuthenticatorClass();

    void setAuthenticatorClass(Class<? extends Authenticator> authenticatorClass);

    String getAuthenticatorName();

    void setAuthenticatorName(String authenticatorName);
    
    Authenticator getSelectedAuthenticator();
}
