package org.jboss.picketlink.example.securityconsole;

import org.picketlink.authentication.Authenticator;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.internal.DefaultLoginCredentials;
import org.picketlink.idm.model.SimpleUser;

import javax.inject.Inject;

public class BasicAuthenticator extends BaseAuthenticator
{
    @Inject 
    private DefaultLoginCredentials credentials;

    public void authenticate() 
    {
        if ("shane".equals(credentials.getUserId()) &&
                "password".equals(credentials.getCredential().toString()))
        {
            setUser(new SimpleUser("shane"));
            setStatus(Authenticator.AuthenticationStatus.SUCCESS);
        }
    }
}
