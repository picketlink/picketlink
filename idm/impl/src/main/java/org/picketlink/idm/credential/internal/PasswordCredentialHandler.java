package org.picketlink.idm.credential.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.PlainTextPassword;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.password.internal.SHASaltedPasswordEncoder;
import org.picketlink.idm.password.internal.SHASaltedPasswordHash;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;

/**
 * <p>
 * This particular implementation supports the validation of {@link UsernamePasswordCredentials}, and updating
 * {@link PlainTextPassword} credentials.
 * </p>
 * <p>
 * Passwords can be encoded or not. This behavior is configured by setting the <code>encodedPassword</code> property of the
 * {@link PlainTextPassword}.
 * </p>
 * 
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({ UsernamePasswordCredentials.class, PlainTextPassword.class })
public class PasswordCredentialHandler implements CredentialHandler {

    @Override
    public void validate(Credentials credentials, IdentityStore<?> identityStore) {
        checkIdentityStoreInstance(identityStore);

        if (!UsernamePasswordCredentials.class.isInstance(credentials)) {
            throw new IllegalArgumentException("Credentials class [" + credentials.getClass().getName()
                    + "] not supported by this handler.");
        }

        UsernamePasswordCredentials usernamePassword = (UsernamePasswordCredentials) credentials;

        usernamePassword.setStatus(Status.INVALID);

        Agent agent = identityStore.getAgent(usernamePassword.getUsername());

        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            CredentialStore store = (CredentialStore) identityStore;

            Class<SHASaltedPasswordHash> storageClass = SHASaltedPasswordHash.class;

            SHASaltedPasswordHash hash = store.retrieveCurrentCredential(agent, storageClass);

            // If the stored hash is null we automatically fail validation
            if (hash != null) {
                SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
                String encoded = encoder.encodePassword(hash.getSalt(), new String(usernamePassword.getPassword().getValue()));

                if (hash.getEncodedHash().equals(encoded)) {
                    usernamePassword.setStatus(Status.VALID);
                    usernamePassword.setValidatedAgent(agent);
                }
            } else if (isLastCredentialExpired(agent, store, storageClass)) {
                usernamePassword.setStatus(Status.EXPIRED);
            }
        }
    }

    @Override
    public void update(Agent agent, Object credential, IdentityStore<?> identityStore, Date effectiveDate, Date expiryDate) {
        checkIdentityStoreInstance(identityStore);

        if (!PlainTextPassword.class.isInstance(credential)) {
            throw new IllegalArgumentException("Credential class [" + credential.getClass().getName()
                    + "] not supported by this handler.");
        }

        PlainTextPassword password = (PlainTextPassword) credential;

        CredentialStore store = (CredentialStore) identityStore;

        SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
        SHASaltedPasswordHash hash = new SHASaltedPasswordHash();

        String salt = generateSalt();

        hash.setEncodedHash(encoder.encodePassword(salt, new String(password.getValue())));
        hash.setEffectiveDate(effectiveDate);
        hash.setExpiryDate(expiryDate);
        hash.setSalt(salt);

        store.storeCredential(agent, hash);
    }

    private boolean isCredentialExpired(CredentialStorage credentialStorage) {
        return credentialStorage.getExpiryDate() == null || new Date().after(credentialStorage.getExpiryDate());
    }

    @SuppressWarnings("unchecked")
    private boolean isLastCredentialExpired(Agent agent, CredentialStore store, Class<? extends CredentialStorage> storageClass) {
        List<CredentialStorage> credentials = (List<CredentialStorage>) store.retrieveCredentials(agent, storageClass);
        CredentialStorage lastCredential = null;

        for (CredentialStorage storedCredential : credentials) {
            if (lastCredential == null || lastCredential.getEffectiveDate().before(storedCredential.getEffectiveDate())) {
                lastCredential = storedCredential;
            }
        }

        if (isCredentialExpired(lastCredential)) {
            return true;
        }

        return false;
    }
    
    private String generateSalt() {
        String salt = null;

        SecureRandom psuedoRng = null;
        String algorithm = "SHA1PRNG";

        try {
            psuedoRng = SecureRandom.getInstance(algorithm);
            psuedoRng.setSeed(1024);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error getting SecureRandom instance: " + algorithm, e);
        }

        salt = String.valueOf(psuedoRng.nextLong());

        return salt;
    }
    
    private void checkIdentityStoreInstance(IdentityStore<?> identityStore) {
        if (!CredentialStore.class.isInstance(identityStore)) {
            throw new IdentityManagementException("Provided IdentityStore [" + identityStore
                    + "] is not an instance of CredentialStore.");
        }
    }
}
