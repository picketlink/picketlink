package org.picketlink.idm.credential.internal;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Digest;
import org.picketlink.idm.credential.DigestCredentials;
import org.picketlink.idm.credential.DigestUtil;
import org.picketlink.idm.credential.PlainTextPassword;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.file.internal.FileBasedIdentityStore;
import org.picketlink.idm.jpa.internal.JPAIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.password.internal.PlainTextPasswordStorage;
import org.picketlink.idm.spi.IdentityStore;

/**
 * <p>
 * This particular implementation supports the validation of {@link DigestCredentials}.
 * </p>
 * <p>
 * Digest validation requires that the password was previously stored as a {@link PlainTextPassword} without encoding using the
 * {@link PlainTextPasswordStorage}.
 * </p>
 * 
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({ DigestCredentials.class, Digest.class })
public class DigestCredentialHandler implements CredentialHandler {

    @Override
    public void validate(Credentials credentials, IdentityStore<?> identityStore) {
        DigestCredentials digestCredential = (DigestCredentials) credentials;

        Agent agent = identityStore.getAgent(digestCredential.getDigest().getUsername());
        PlainTextPasswordStorage storedPassword = null; // FIXME identityStore.retrieveCredential(agent, PlainTextPasswordStorage.class);

        if (storedPassword != null) {
            if (DigestUtil.matchCredential(digestCredential.getDigest(), storedPassword.getPassword().toCharArray())) {
                digestCredential.setStatus(Status.VALID);
                digestCredential.setValidatedAgent(agent);
            }
        }
    }

    public void update(Agent agent, Object credential, IdentityStore<?> store) {
        // this handler only supports validation
    }
}
