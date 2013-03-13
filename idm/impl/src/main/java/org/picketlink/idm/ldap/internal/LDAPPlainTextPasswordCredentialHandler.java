/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.picketlink.idm.ldap.internal;

import java.util.Date;

import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.Credentials.Status;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
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
            LDAPUser ldapUser = ldapIdentityStore.lookupEntryById(LDAPUser.class, agent.getId());
            char[] password = usernamePassword.getPassword().getValue();

            boolean isValid = ldapIdentityStore.getLDAPManager().authenticate(ldapUser.getDN(), new String(password));

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
        LDAPUser ldapuser = ldapIdentityStore.lookupEntryById(LDAPUser.class, agent.getId());

        if (ldapIdentityStore.getConfig().isActiveDirectory()) {
            updateADPassword(ldapuser, new String(password.getValue()), ldapIdentityStore);
        } else {
            ModificationItem[] mods = new ModificationItem[1];

            try {
                Attribute mod0 = new BasicAttribute(USER_PASSWORD_ATTRIBUTE, new String(password.getValue()));

                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);

                ldapIdentityStore.getLDAPManager().modifyAttribute(ldapuser.getDN(), mod0);
            } catch (Exception e) {
                throw new IdentityManagementException("Error updating password.", e);
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

            store.getLDAPManager().modifyAttribute(user.getDN(), unicodePwd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
