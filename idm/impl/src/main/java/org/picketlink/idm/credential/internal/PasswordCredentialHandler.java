package org.picketlink.idm.credential.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.PlainTextPassword;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.CredentialStorage;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.password.internal.PlainTextPasswordStorage;
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

    private static final String PASSWORD_SALT_USER_ATTRIBUTE = "passwordSalt";

    @Override
    public void validate(Credentials credentials, IdentityStore<?> identityStore) {
        if (!CredentialStore.class.isInstance(identityStore)) {
            throw new IdentityManagementException("Provided IdentityStore [" + identityStore
                    + "] is not an instance of CredentialStore.");
        }

        if (!UsernamePasswordCredentials.class.isInstance(credentials)) {
            throw new IllegalArgumentException("Credentials class [" + credentials.getClass().getName()
                    + "] not supported by this handler.");
        }

        UsernamePasswordCredentials usernamePassword = (UsernamePasswordCredentials) credentials;

        usernamePassword.setStatus(Status.INVALID);

        Agent agent = identityStore.getAgent(usernamePassword.getUsername());

        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            doValidate(agent, usernamePassword, identityStore);
        }
    }

    protected void doValidate(Agent agent, UsernamePasswordCredentials usernamePassword, IdentityStore<?> identityStore) {
        PlainTextPassword password = usernamePassword.getPassword();
        CredentialStore store = (CredentialStore) identityStore;

        if (password.isEncodePassword()) {
            SHASaltedPasswordHash hash = store.retrieveCurrentCredential(agent, SHASaltedPasswordHash.class);

            // If the stored hash is null we automatically fail validation
            if (hash != null) {
                Status status = validateDate(hash);

                if (status == null) {
                    SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
                    String encoded = encoder.encodePassword(hash.getSalt(), new String(usernamePassword.getPassword().getValue()));

                    if (hash.getEncodedHash().equals(encoded)) {
                        usernamePassword.setStatus(Status.VALID);
                        usernamePassword.setValidatedAgent(agent);
                    }
                } else {
                    usernamePassword.setStatus(status);
                }
            }
        } else {
            PlainTextPasswordStorage storedPassword = store.retrieveCurrentCredential(agent, PlainTextPasswordStorage.class);

            if (storedPassword != null) {
                Status status = validateDate(storedPassword);

                if (status == null) {
                    if (storedPassword.getPassword().equals(String.valueOf(password.getValue()))) {
                        usernamePassword.setStatus(Status.VALID);
                        usernamePassword.setValidatedAgent(agent);
                    }
                } else {
                    usernamePassword.setStatus(status);
                }
            }
        }
    }

    private Status validateDate(CredentialStorage credentialStorage) {
        Status status = null;
        Date actualDate = new Date();

        if (credentialStorage.getEffectiveDate() != null && actualDate.before(credentialStorage.getEffectiveDate())) {
            status = Status.NOT_EFFECTIVE;
        }

        if (credentialStorage.getExpiryDate() != null && actualDate.after(credentialStorage.getExpiryDate())) {
            status = Status.EXPIRED;
        }

        return status;
    }

    protected String getSalt() {
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

    @Override
    public void update(Agent agent, Object credential, IdentityStore<?> identityStore, Date effectiveDate, Date expiryDate) {
        if (!CredentialStore.class.isInstance(identityStore)) {
            throw new IdentityManagementException("Provided IdentityStore [" + identityStore
                    + "] is not an instance of CredentialStore.");
        }

        if (!PlainTextPassword.class.isInstance(credential)) {
            throw new IllegalArgumentException("Credential class [" + credential.getClass().getName()
                    + "] not supported by this handler.");
        }

        PlainTextPassword password = (PlainTextPassword) credential;

        doUpdate(agent, password, identityStore, effectiveDate, expiryDate);
    }

    protected void doUpdate(Agent agent, PlainTextPassword password, IdentityStore<?> identityStore, Date effectiveDate, Date expiryDate) {
        CredentialStore store = (CredentialStore) identityStore;
        
        if (password.isEncodePassword()) {
            SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
            SHASaltedPasswordHash hash = new SHASaltedPasswordHash();
            
            String salt = getSalt();
            
            hash.setEncodedHash(encoder.encodePassword(salt, new String(password.getValue())));
            hash.setEffectiveDate(effectiveDate);
            hash.setExpiryDate(expiryDate);
            hash.setSalt(salt);

            store.storeCredential(agent, hash);
        } else {
            PlainTextPasswordStorage storage = new PlainTextPasswordStorage(String.valueOf(password.getValue()));
            
            storage.setEffectiveDate(effectiveDate);
            storage.setExpiryDate(expiryDate);
            
            store.storeCredential(agent, storage);
        }
    }
}
