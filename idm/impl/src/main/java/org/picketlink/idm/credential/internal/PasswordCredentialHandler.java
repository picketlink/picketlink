package org.picketlink.idm.credential.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.PlainTextPassword;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.password.internal.PlainTextPasswordStorage;
import org.picketlink.idm.password.internal.SHASaltedPasswordEncoder;
import org.picketlink.idm.password.internal.SHASaltedPasswordHash;
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

        if (password.isEncodePassword()) {
            SHASaltedPasswordHash hash = null; // FIXME identityStore.retrieveCredential(agent, SHASaltedPasswordHash.class);

            // If the stored hash is null we automatically fail validation
            if (hash != null) {
                String salt = getSalt(agent, identityStore);

                SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
                String encoded = encoder.encodePassword(salt, new String(usernamePassword.getPassword().getValue()));

                if (hash.getEncodedHash().equals(encoded)) {
                    usernamePassword.setStatus(Status.VALID);
                    usernamePassword.setValidatedAgent(agent);
                }
            }
        } else {
            PlainTextPasswordStorage storedPassword = null; // identityStore.retrieveCredential(agent, PlainTextPasswordStorage.class);

            if (storedPassword != null) {
                if (storedPassword.getPassword().equals(String.valueOf(password.getValue()))) {
                    usernamePassword.setStatus(Status.VALID);
                    usernamePassword.setValidatedAgent(agent);
                }
            }
        }
    }

    protected String getSalt(Agent agent, IdentityStore<?> store) {
        Attribute<String> saltAttribute = agent.<String> getAttribute(PASSWORD_SALT_USER_ATTRIBUTE);
        String salt = null;

        if (saltAttribute != null) {
            salt = saltAttribute.getValue();
        }

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
    public void update(Agent agent, Object credential, IdentityStore<?> store, Date effectiveDate, Date expiryDate) {
        if (!PlainTextPassword.class.isInstance(credential)) {
            throw new IllegalArgumentException("Credential class [" + credential.getClass().getName()
                    + "] not supported by this handler.");
        }

        PlainTextPassword password = (PlainTextPassword) credential;

        doUpdate(agent, store, password);
    }

    protected void doUpdate(Agent agent, IdentityStore<?> store, PlainTextPassword password) {
        if (password.isEncodePassword()) {
            SHASaltedPasswordEncoder encoder = new SHASaltedPasswordEncoder(512);
            SHASaltedPasswordHash hash = new SHASaltedPasswordHash();
            hash.setEncodedHash(encoder.encodePassword(getSalt(agent, store), new String(password.getValue())));

            // FIXME store.<SHASaltedPasswordHash> storeCredential(agent, hash);
        } else {
            PlainTextPasswordStorage storage = new PlainTextPasswordStorage(String.valueOf(password.getValue()));

            // FIXME store.storeCredential(agent, storage);
        }
    }

}
