package org.jboss.picketlink.example.securityconsole;

import javax.inject.Inject;

import org.jboss.picketlink.cdi.authentication.BaseAuthenticator;
import org.jboss.picketlink.cdi.credential.LoginCredentials;
import org.jboss.picketlink.idm.model.SimpleUser;

public class BasicAuthenticator extends BaseAuthenticator 
{
    @Inject 
    private LoginCredentials credentials;

    public void authenticate() 
    {
        if ("shane".equals(credentials.getUserId()) &&
                "password".equals(credentials.getCredential().getValue()))
        {
            setUser(new SimpleUser("shane"));
            setStatus(AuthenticationStatus.SUCCESS);
        }
    }
}
