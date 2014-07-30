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

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.handler.AbstractCredentialHandler;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.spi.IdentityContext;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.util.Date;

/**
 * This particular implementation supports the validation of UsernamePasswordCredentials, and updating PlainTextPassword
 * credentials.
 *
 * @author Shane Bryzak
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 */
@SupportsCredentials(
        credentialClass = {UsernamePasswordCredentials.class, Password.class},
        credentialStorage = SupportsCredentials.NO_CREDENTIAL_STORAGE.class)
public class LDAPPlainTextPasswordCredentialHandler<S, V, U> extends AbstractCredentialHandler<LDAPIdentityStore, UsernamePasswordCredentials, Password> {

    private static final String USER_PASSWORD_ATTRIBUTE = "userpassword";

    @Override
    protected boolean validateCredential(IdentityContext context, CredentialStorage credentialStorage, UsernamePasswordCredentials credentials, LDAPIdentityStore store) {
        Account account = getAccount(context, credentials.getUsername());

        LDAPIdentityStore ldapIdentityStore = (LDAPIdentityStore) store;
        char[] password = credentials.getPassword().getValue();
        String bindingDN = ldapIdentityStore.getBindingDN(account, true);
        LDAPOperationManager operationManager = ldapIdentityStore.getOperationManager();

        if (operationManager.authenticate(bindingDN, new String(password))) {
            return true;
        }

        return false;
    }

    @Override
    protected Account getAccount(IdentityContext context, UsernamePasswordCredentials credentials) {
        return getAccount(context, credentials.getUsername());
    }

    @Override
    protected CredentialStorage getCredentialStorage(IdentityContext context, Account account, UsernamePasswordCredentials credentials, LDAPIdentityStore store) {
        return null; // dummy storage, this handler does not store passwords using a credential storage.
    }

    @Override
    public void update(IdentityContext context, Account account, Password password, LDAPIdentityStore store,
        Date effectiveDate, Date expiryDate) {
        if (store.getConfig().isActiveDirectory()) {
            updateADPassword(account, new String(password.getValue()), store);
        } else {
            ModificationItem[] mods = new ModificationItem[1];

            try {
                BasicAttribute mod0 = new BasicAttribute(USER_PASSWORD_ATTRIBUTE, new String(password.getValue()));

                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, mod0);

                store.getOperationManager().modifyAttribute(store.getBindingDN(account, true), mod0);
            } catch (Exception e) {
                throw new IdentityManagementException("Error updating password.", e);
            }
        }
    }

    @Override
    protected CredentialStorage createCredentialStorage(IdentityContext context, Account account, Password password, LDAPIdentityStore store, Date effectiveDate, Date expiryDate) {
        throw new RuntimeException("This handler does not store passwords using a credential storage.");
    }

    private void updateADPassword(Account account, String password, LDAPIdentityStore store) {
        try {
            // Replace the "unicdodePwd" attribute with a new value
            // Password must be both Unicode and a quoted string
            String newQuotedPassword = "\"" + password + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

            BasicAttribute unicodePwd = new BasicAttribute("unicodePwd", newUnicodePassword);

            store.getOperationManager().modifyAttribute(store.getBindingDN(account, true), unicodePwd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
