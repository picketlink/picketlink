package org.picketlink.authentication.internal;

import org.picketlink.authentication.BaseAuthenticator;
import org.picketlink.authentication.LockedAccountException;
import org.picketlink.authentication.UnexpectedCredentialException;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.DigestCredentials;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import static org.picketlink.log.BaseLog.AUTHENTICATION_LOGGER;

/**
 * Authenticator that uses the Identity Management API to authenticate.
 *
 * @author Shane Bryzak
 */
public class IdmAuthenticator extends BaseAuthenticator {

    @Inject
    Instance<IdentityManager> identityManager;

    @Inject
    DefaultLoginCredentials credentials;

    @Override
    public void authenticate() {
        if (credentials.getCredential() == null) {
            return;
        }

        Credentials creds;

        if (isUsernamePasswordCredential()) {
            creds = new UsernamePasswordCredentials(credentials.getUserId(),
                    (Password) credentials.getCredential());
        } else if (isDigestCredential()) {
            creds = new DigestCredentials((Digest) credentials.getCredential());
        } else if (isCustomCredential()) {
            creds = (Credentials) credentials.getCredential();
        } else {
            throw new UnexpectedCredentialException("Unsupported credential type [" + credentials.getCredential() + "].");
        }

        if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
            AUTHENTICATION_LOGGER.debugf("Validating credentials [%s] using PicketLink IDM.", creds);
        }

        identityManager.get().validateCredentials(creds);

        this.credentials.setStatus(creds.getStatus());
        this.credentials.setValidatedAccount(creds.getValidatedAccount());

        if (AUTHENTICATION_LOGGER.isDebugEnabled()) {
            AUTHENTICATION_LOGGER.debugf("Credential status is [%s] and validated account [%s]", this.credentials.getStatus(), this.credentials.getValidatedAccount());
        }

        if (Credentials.Status.VALID.equals(creds.getStatus())) {
            setStatus(AuthenticationStatus.SUCCESS);
            setAccount(creds.getValidatedAccount());
        } else if (Credentials.Status.ACCOUNT_DISABLED.equals(creds.getStatus())) {
            throw new LockedAccountException("Account [" + this.credentials.getUserId() + "] is disabled.");
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