package org.picketlink.authentication.internal;

import javax.inject.Inject;

import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.User;

/**
 * Authenticator that uses the Identity Management API to authenticate.  Assumes that the
 * user has provided a username and password via the DefaultLoginCredentials bean.
 * 
 * @author Shane Bryzak
 *
 */
public class IdmAuthenticator extends BaseAuthenticator {

    @Inject IdentityManager identityManager;

    @Inject DefaultLoginCredentials credentials;

    @Override
    public void authenticate() {
        if (credentials.getPassword() == null) {
            setStatus(AuthenticationStatus.FAILURE);
            return;
        }

        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(credentials.getUserId(),
                (Password) credentials.getCredential());
        identityManager.validateCredentials(creds);


        if (Credentials.Status.VALID.equals(creds.getStatus())) {
            setStatus(AuthenticationStatus.SUCCESS);
            setUser((User) creds.getValidatedAgent());
        } else {
            setStatus(AuthenticationStatus.FAILURE);
        }
    }

}
