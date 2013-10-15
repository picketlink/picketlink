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
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.spi.IdentityContext;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.util.Date;

import static org.picketlink.idm.credential.Credentials.Status;
import static org.picketlink.idm.model.basic.BasicModel.getAgent;
import static org.picketlink.idm.model.basic.BasicModel.getUser;

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
public class LDAPPlainTextPasswordCredentialHandler<S, V, U>
    implements CredentialHandler<LDAPIdentityStore, UsernamePasswordCredentials, Password> {

    private static final String USER_PASSWORD_ATTRIBUTE = "userpassword";

    @Override
    public void setup(LDAPIdentityStore store) {
    }

    @Override
    public void validate(IdentityContext context, UsernamePasswordCredentials usernamePassword,
            LDAPIdentityStore identityStore) {
        usernamePassword.setStatus(Status.INVALID);
        usernamePassword.setValidatedAccount(null);

        Account agent = getAccount(context, usernamePassword.getUsername());

        // If the user for the provided username cannot be found we fail validation
        if (agent != null) {
            if (agent.isEnabled()) {
                LDAPIdentityStore ldapIdentityStore = (LDAPIdentityStore) identityStore;
                char[] password = usernamePassword.getPassword().getValue();
                String bindingDN = ldapIdentityStore.getBindingDN(agent);
                LDAPOperationManager operationManager = ldapIdentityStore.getOperationManager();

                if (operationManager.authenticate(bindingDN, new String(password))) {
                    usernamePassword.setValidatedAccount(agent);
                    usernamePassword.setStatus(Status.VALID);
                }
            } else {
                usernamePassword.setStatus(Status.ACCOUNT_DISABLED);
            }
        }
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

                store.getOperationManager().modifyAttribute(store.getBindingDN(account), mod0);
            } catch (Exception e) {
                throw new IdentityManagementException("Error updating password.", e);
            }
        }
    }

    private void updateADPassword(Account account, String password, LDAPIdentityStore store) {
        try {
            // Replace the "unicdodePwd" attribute with a new value
            // Password must be both Unicode and a quoted string
            String newQuotedPassword = "\"" + password + "\"";
            byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");

            BasicAttribute unicodePwd = new BasicAttribute("unicodePwd", newUnicodePassword);

            store.getOperationManager().modifyAttribute(store.getBindingDN(account), unicodePwd);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected Account getAccount(IdentityContext context, String loginName) {
        IdentityManager identityManager = getIdentityManager(context);

        Account agent = getAgent(identityManager, loginName);

        if (agent == null) {
            agent = getUser(identityManager, loginName);
        }

        return agent;
    }

    protected IdentityManager getIdentityManager(IdentityContext context) {
        IdentityManager identityManager = context.getParameter(IdentityManager.IDENTITY_MANAGER_CTX_PARAMETER);

        if (identityManager == null) {
            throw new IdentityManagementException("IdentityManager not set into context.");
        }

        return identityManager;
    }
}
