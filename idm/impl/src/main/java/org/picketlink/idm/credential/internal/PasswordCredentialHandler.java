package org.picketlink.idm.credential.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.password.internal.SHASaltedPasswordEncoder;
import org.picketlink.idm.password.internal.SHASaltedPasswordStorage;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityStore;

/**
 * <p>
 * This particular implementation supports the validation of {@link UsernamePasswordCredentials}, and updating {@link Password}
 * credentials.
 * </p>
 * <p>
 * Passwords can be encoded or not. This behavior is configured by setting the <code>encodedPassword</code> property of the
 * {@link Password}.
 * </p>
 * 
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({ UsernamePasswordCredentials.class, Password.class })
public class PasswordCredentialHandler implements CredentialHandler {

    @Override
    public void validate(Credentials credentials, IdentityStore<?> identityStore) {
        CredentialStore store = validateCredentialStore(identityStore);

        if (!UsernamePasswordCredentials.class.isInstance(credentials)) {
            throw new IllegalArgumentException("Credentials class [" + credentials.getClass().getName()
                    + "] not supported by this handler.");
        }

        UsernamePasswordCredentials usernamePassword = (UsernamePasswordCredentials) credentials;

        usernamePassword.setStatus(Status.INVALID);

        Agent agent = identityStore.getAgent(usernamePassword.getUsername());

        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            SHASaltedPasswordStorage hash = store.retrieveCurrentCredential(agent, SHASaltedPasswordStorage.class);

            // If the stored hash is null we automatically fail validation
            if (hash != null) {
                if (!CredentialUtils.isCredentialExpired(hash)) {
                    SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
                    String encoded = encoder.encodePassword(hash.getSalt(), new String(usernamePassword.getPassword()
                            .getValue()));

                    if (hash.getEncodedHash().equals(encoded)) {
                        usernamePassword.setStatus(Status.VALID);
                        usernamePassword.setValidatedAgent(agent);
                    }
                } else {
                    usernamePassword.setStatus(Status.EXPIRED);
                }
            }
        }
    }

    @Override
    public void update(Agent agent, Object credential, IdentityStore<?> identityStore, Date effectiveDate, Date expiryDate) {
        CredentialStore store = validateCredentialStore(identityStore);

        if (!Password.class.isInstance(credential)) {
            throw new IllegalArgumentException("Credential class [" + credential.getClass().getName()
                    + "] not supported by this handler.");
        }

        Password password = (Password) credential;

        SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
        SHASaltedPasswordStorage hash = new SHASaltedPasswordStorage();

        hash.setSalt(generateSalt());
        hash.setEncodedHash(encoder.encodePassword(hash.getSalt(), new String(password.getValue())));
        hash.setEffectiveDate(effectiveDate);

        if (expiryDate != null) {
            hash.setExpiryDate(expiryDate);
        }

        store.storeCredential(agent, hash);
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

    private CredentialStore validateCredentialStore(IdentityStore<?> identityStore) {
        if (!CredentialStore.class.isInstance(identityStore)) {
            throw new IdentityManagementException("Provided IdentityStore [" + identityStore.getClass().getName()
                    + "] is not an instance of CredentialStore.");
        } else {
            return (CredentialStore) identityStore;
        }
    }
}
