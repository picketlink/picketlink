package org.picketlink.credential;

/**
 * Represents the credentials the current user will use to authenticate
 * Only valid during the authentication process
 * 
 * @author Shane Bryzak
 */
public interface LoginCredentials
{
    String getUserId();

    void setUserId(String userId);
    
    Credential getCredential();

    void setCredential(Credential credential);

    void invalidate();
}
