package org.picketlink.idm.jpa.internal;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.PlainTextPassword;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.password.internal.SHASaltedPasswordHash;
import org.picketlink.idm.spi.IdentityStore;

/**
 * A CredentialHandler designed for use with JPAIdentityStore. This particular implementation supports 
 * the validation of UsernamePasswordCredentials, and updating PlainTextPassword credentials.
 *
 * @author Shane Bryzak
 */
// TODO Should we support this kind of metadata for convenience? It may be helpful for reducing the amount of parameter validation
//@SupportsCredentials({UsernamePasswordCredentials.class, PlainTextPassword.class});
//@SupportsStores({JPAIdentityStore.class});
public class JPAPlainTextPasswordCredentialHandler implements CredentialHandler {

    @Override
    public void validate(Credentials credentials, IdentityStore store) {
        if (!UsernamePasswordCredentials.class.isInstance(credentials)) {
            throw new IllegalArgumentException("Credentials class [" + 
                    credentials.getClass().getName() + "] not supported by this handler.");
        }

        if (!JPAIdentityStore.class.isInstance(store)) {
            throw new IllegalArgumentException("IdentityStore class [" + 
                    store.getClass() + "] not supported by this handler.");
        }

        UsernamePasswordCredentials usernamePassword = (UsernamePasswordCredentials) credentials;
        JPAIdentityStore identityStore = (JPAIdentityStore) store;

        Agent agent = identityStore.getAgent(usernamePassword.getUsername());

        // If the user for the provided username cannot be found we fail validation
        if (agent == null) {
            usernamePassword.setStatus(Status.INVALID);
        } else {

            SHASaltedPasswordHash hash = identityStore.retrieveCredential(agent, SHASaltedPasswordHash.class);

            // If the stored hash is null we automatically fail validation
            if (hash == null) {
                usernamePassword.setStatus(Status.INVALID);
            } else {

                // TODO calculate the hash on the provided password and compare it to the stored hash

            }
        }
    }

    @Override
    public void update(Agent agent, Object credential, IdentityStore store) {
        if (!PlainTextPassword.class.isInstance(credential)) {
            throw new IllegalArgumentException("Credential class [" + 
                    credential.getClass().getName() + "] not supported by this handler.");
        }

        if (!JPAIdentityStore.class.isInstance(store)) {
            throw new IllegalArgumentException("IdentityStore class [" + 
                    store.getClass() + "] not supported by this handler.");
        }
    }

}
