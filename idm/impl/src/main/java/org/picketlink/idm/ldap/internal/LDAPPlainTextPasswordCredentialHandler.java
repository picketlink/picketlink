package org.picketlink.idm.ldap.internal;

import java.util.Date;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.internal.Password;
import org.picketlink.idm.credential.internal.UsernamePasswordCredentials;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.spi.IdentityStore;

/**
 * This particular implementation supports the validation of UsernamePasswordCredentials, and updating PlainTextPassword credentials.
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({UsernamePasswordCredentials.class, Password.class})
public class LDAPPlainTextPasswordCredentialHandler implements CredentialHandler {
    
    private static final String USER_PASSWORD_ATTRIBUTE = "userpassword";
    
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
            LDAPIdentityStore ldapIdentityStore = (LDAPIdentityStore) identityStore;
            LDAPUser ldapUser = (LDAPUser) ldapIdentityStore.getUser(agent.getId());
            char[] password = usernamePassword.getPassword().getValue();
            
            boolean isValid = ldapIdentityStore.getLdapManager().authenticate(ldapUser.getDN(), new String(password));
            
            if (isValid) {
                usernamePassword.setStatus(Status.VALID);
            }
        }
    }

    @Override
    public void update(Agent agent, Object credential, IdentityStore<?> identityStore, Date effectiveDate, Date expiryDate) {
        checkIdentityStoreInstance(identityStore);
        
        if (!Password.class.isInstance(credential)) {
            throw new IllegalArgumentException("Credential class [" + credential.getClass().getName()
                    + "] not supported by this handler.");
        }

        Password password = (Password) credential;

        LDAPIdentityStore ldapIdentityStore = (LDAPIdentityStore) identityStore;
        LDAPUser ldapuser = (LDAPUser) ldapIdentityStore.getUser(agent.getId());
        
        if (ldapIdentityStore.getConfig().isActiveDirectory()) {
            updateADPassword(ldapuser, new String(password.getValue()), ldapIdentityStore);
        } else {

            ModificationItem[] mods = new ModificationItem[1];
            
            try {
                Attribute mod0 = new BasicAttribute(USER_PASSWORD_ATTRIBUTE, new String(password.getValue()));

                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);

                ldapIdentityStore.getLdapManager().modifyAttribute(ldapuser.getDN(), mod0);
            } catch (Exception e) {
            }
        }
    }

    private void checkIdentityStoreInstance(IdentityStore<?> store) {
        if (!LDAPIdentityStore.class.isInstance(store)) {
            throw new IllegalArgumentException("IdentityStore class [" + 
                    store.getClass() + "] not supported by this handler.");
        }
    }
    
    private void updateADPassword(LDAPUser user, String password, LDAPIdentityStore store) {
        try {
            // Replace the "unicdodePwd" attribute with a new value
            // Password must be both Unicode and a quoted string
            String newQuotedPassword = "\"" + password + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

            BasicAttribute unicodePwd = new BasicAttribute("unicodePwd", newUnicodePassword);

            store.getLdapManager().modifyAttribute(user.getDN(), unicodePwd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
