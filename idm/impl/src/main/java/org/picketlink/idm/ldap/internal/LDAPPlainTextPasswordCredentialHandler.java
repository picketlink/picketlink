package org.picketlink.idm.ldap.internal;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.PlainTextPassword;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.internal.PlainTextPasswordCredentialHandler;
import org.picketlink.idm.credential.spi.annotations.SupportsCredentials;
import org.picketlink.idm.credential.spi.annotations.SupportsStores;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.spi.IdentityStore;

/**
 * This particular implementation supports the validation of UsernamePasswordCredentials, and updating PlainTextPassword credentials.
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials({UsernamePasswordCredentials.class, PlainTextPassword.class})
@SupportsStores({LDAPIdentityStore.class})
public class LDAPPlainTextPasswordCredentialHandler extends PlainTextPasswordCredentialHandler {
    
    private static final String USER_PASSWORD_ATTRIBUTE = "userpassword";

    @Override
    protected void doUpdate(Agent agent, IdentityStore<?> store, PlainTextPassword password) {
        LDAPIdentityStore ldapIdentityStore = getLDAPIdentityStore(store);
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

    private LDAPIdentityStore getLDAPIdentityStore(IdentityStore<?> store) {
        if (!LDAPIdentityStore.class.isInstance(store)) {
            throw new IllegalArgumentException("IdentityStore class [" + 
                    store.getClass() + "] not supported by this handler.");
        }
        
        return (LDAPIdentityStore) store;
    }
    
    @Override
    protected void doValidate(Agent agent, UsernamePasswordCredentials usernamePassword, IdentityStore<?> identityStore) {
        LDAPIdentityStore ldapIdentityStore = getLDAPIdentityStore(identityStore);
        LDAPUser ldapUser = (LDAPUser) ldapIdentityStore.getUser(agent.getId());
        char[] password = usernamePassword.getPassword().getValue();
        
        boolean isValid = getLDAPIdentityStore(identityStore).getLdapManager().authenticate(ldapUser.getDN(), new String(password));
        
        if (isValid) {
            usernamePassword.setStatus(Status.VALID);
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
