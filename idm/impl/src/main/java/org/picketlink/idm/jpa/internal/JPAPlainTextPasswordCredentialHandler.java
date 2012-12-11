package org.picketlink.idm.jpa.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.PlainTextPassword;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.password.internal.SHASaltedPasswordEncoder;
import org.picketlink.idm.password.internal.SHASaltedPasswordHash;
import org.picketlink.idm.spi.IdentityStore;

/**
 * A CredentialHandler designed for use with JPAIdentityStore. This particular implementation supports 
 * the validation of UsernamePasswordCredentials, and updating PlainTextPassword credentials.
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
// TODO Should we support this kind of metadata for convenience? It may be helpful for reducing the amount of parameter validation
//@SupportsCredentials({UsernamePasswordCredentials.class, PlainTextPassword.class});
//@SupportsStores({JPAIdentityStore.class});
public class JPAPlainTextPasswordCredentialHandler implements CredentialHandler {

    private static final String PASSWORD_SALT_USER_ATTRIBUTE = "passwordSalt";

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
                String salt = getSalt(agent, store);

                SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
                String encoded = encoder.encodePassword(salt, new String(usernamePassword.getPassword().getValue()));

                if (hash.getEncodedHash().equals(encoded)) {
                    usernamePassword.setStatus(Status.VALID);
                    usernamePassword.setValidatedAgent(agent);
                }
            }
        }
    }

    private String getSalt(Agent agent, IdentityStore store) {
        String salt = agent.<String>getAttribute(PASSWORD_SALT_USER_ATTRIBUTE).getValue();

        // Agent does not have a salt. let's generate a fresh one.
        if (salt == null) {
            SecureRandom psuedoRng = null;
            String algorithm = "SHA1PRNG";

            try {
                psuedoRng = SecureRandom.getInstance(algorithm);
                psuedoRng.setSeed(1024);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Error getting SecureRandom instance: " + algorithm, e);
            }

            salt = String.valueOf(psuedoRng.nextLong());

            agent.setAttribute(new Attribute<String>(PASSWORD_SALT_USER_ATTRIBUTE, salt));
            store.update(agent);
        }

        return salt;
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

        PlainTextPassword password = (PlainTextPassword) credential;
        JPAIdentityStore identityStore = (JPAIdentityStore) store;

        SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
        SHASaltedPasswordHash hash = new SHASaltedPasswordHash();
        hash.setEncodedHash(encoder.encodePassword(getSalt(agent, store), 
                new String(password.getValue())));

        identityStore.<SHASaltedPasswordHash>storeCredential(agent, hash);
    }

}
