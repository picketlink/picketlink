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
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.spi.IdentityContext;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.util.Date;

import static org.picketlink.idm.IDMLog.CREDENTIAL_LOGGER;
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
    public void validate(IdentityContext context, UsernamePasswordCredentials credentials,
            LDAPIdentityStore store) {
        credentials.setStatus(Status.INVALID);
        credentials.setValidatedAccount(null);

        if (CREDENTIAL_LOGGER.isDebugEnabled()) {
            CREDENTIAL_LOGGER.debugf("Validating credentials [%s][%s] using identity store [%s] and credential handler [%s].", credentials.getClass(), credentials, store, this);
        }

        Account account = getAccount(context, credentials.getUsername());

        // If the user for the provided username cannot be found we fail validation
        if (account != null) {
            if (CREDENTIAL_LOGGER.isDebugEnabled()) {
                CREDENTIAL_LOGGER.debugf("Found account [%s] from credentials [%s].", account, credentials);
            }

            if (account.isEnabled()) {
                if (CREDENTIAL_LOGGER.isDebugEnabled()) {
                    CREDENTIAL_LOGGER.debugf("Account [%s] is ENABLED.", account, credentials);
                }

                LDAPIdentityStore ldapIdentityStore = (LDAPIdentityStore) store;
                char[] password = credentials.getPassword().getValue();
                String bindingDN = ldapIdentityStore.getBindingDN(account);
                LDAPOperationManager operationManager = ldapIdentityStore.getOperationManager();

                if (operationManager.authenticate(bindingDN, new String(password))) {
                    credentials.setValidatedAccount(account);
                    credentials.setStatus(Status.VALID);
                }
            } else {
                if (CREDENTIAL_LOGGER.isDebugEnabled()) {
                    CREDENTIAL_LOGGER.debugf("Account [%s] is DISABLED.", account, credentials);
                }
                credentials.setStatus(Status.ACCOUNT_DISABLED);
            }
        } else {
            if (CREDENTIAL_LOGGER.isDebugEnabled()) {
                CREDENTIAL_LOGGER.debugf("Account NOT FOUND for credentials [%s][%s].", credentials.getClass(), credentials);
            }
        }

        if (CREDENTIAL_LOGGER.isDebugEnabled()) {
            CREDENTIAL_LOGGER.debugf("Credential [%s][%s] validated using identity store [%s] and credential handler [%s]. Status [%s]. Validated Account [%s]",
                    credentials.getClass(), credentials, store, this, credentials.getStatus(), credentials.getValidatedAccount());
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

        if (CREDENTIAL_LOGGER.isDebugEnabled()) {
            CREDENTIAL_LOGGER.debugf("Trying to find account [%s] using default account type [%s]. If you're using a custom account type, it will not be retrieved until you provide a credential handler that knows how to retrieve it.", loginName, Agent.class);
        }

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
