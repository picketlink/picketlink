package org.picketlink.authentication.internal;

import javax.inject.Inject;
import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.authentication.LockedAccountException;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.DigestCredentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.User;

/**
 * Authenticator that uses the Identity Management API to authenticate.  Assumes that the
 * user has provided a username and password via the DefaultLoginCredentials bean.
 *
 * @author Shane Bryzak
 */
public class IdmAuthenticator extends BaseAuthenticator {

    @Inject
    IdentityManager identityManager;

    @Inject
    DefaultLoginCredentials credentials;

    @Override
    public void authenticate() {
        if (credentials.getCredential() == null) {
            return;
        }

        Credentials creds = null;

        if (isUsernamePasswordCredential()) {
            creds = new UsernamePasswordCredentials(credentials.getUserId(),
                    (Password) credentials.getCredential());
        } else if (isDigestCredential()) {
            creds = new DigestCredentials((Digest) credentials.getCredential());
        } else if (isCustomCredential()) {
            creds = (Credentials) credentials.getCredential();
        } else {
            throw new IllegalArgumentException("Unsupported credential type [" + credentials.getCredential() + "].");
        }

        identityManager.validateCredentials(creds);

        if (Credentials.Status.VALID.equals(creds.getStatus())) {
            setStatus(AuthenticationStatus.SUCCESS);
            setAgent((User) creds.getValidatedAgent());
        } else if (Credentials.Status.AGENT_DISABLED.equals(creds.getStatus())) {
            throw new LockedAccountException("Agent [" + this.credentials.getUserId() + "] is disabled.");
        }
    }

    private boolean isCustomCredential() {
        return Credentials.class.isInstance(credentials.getCredential());
    }

    private boolean isDigestCredential() {
        return Digest.class.equals(credentials.getCredential().getClass());
    }

    private boolean isUsernamePasswordCredential() {
        return Password.class.equals(credentials.getCredential().getClass()) && credentials.getUserId() != null;
    }

}