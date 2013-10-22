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
package org.picketlink.idm.credential.handler;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.AbstractBaseCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.credential.util.CredentialUtils;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import static org.picketlink.idm.IDMLog.CREDENTIAL_LOGGER;
import static org.picketlink.idm.credential.Credentials.Status;
import static org.picketlink.idm.model.basic.BasicModel.getAgent;
import static org.picketlink.idm.model.basic.BasicModel.getUser;

/**
 * <p>Base class for {@link CredentialHandler} implementations.</p>
 *
 * @author pedroigor
 */
public abstract class AbstractCredentialHandler<S extends IdentityStore<?>, V extends AbstractBaseCredentials, U>
        implements CredentialHandler<S, V, U> {

    protected Account getAccount(IdentityContext context, String loginName) {
        IdentityManager identityManager = getIdentityManager(context);

        if (isDebugEnabled()) {
            CREDENTIAL_LOGGER.debugf("Trying to find account [%s] using default account type [%s]. If you're using a custom account type, it will not be retrieved until you provide a credential handler that knows how to retrieve it.", loginName, Agent.class);
        }

        Account agent = getAgent(identityManager, loginName);

        if (agent == null) {
            agent = getUser(identityManager, loginName);
        }

        return agent;
    }

    @Override
    public void validate(final IdentityContext context, final V credentials, final S store) {
        credentials.setStatus(Status.IN_PROGRESS);

        if (isDebugEnabled()) {
            CREDENTIAL_LOGGER.debugf("Starting validation for credentials [%s][%s] using identity store [%s] and credential handler [%s].", credentials.getClass(), credentials, store, this);
        }

        Account account = getAccount(context, credentials);

        if (account != null) {
            if (isDebugEnabled()) {
                CREDENTIAL_LOGGER.debugf("Found account [%s] from credentials [%s].", account, credentials);
            }

            if (account.isEnabled()) {
                if (isDebugEnabled()) {
                    CREDENTIAL_LOGGER.debugf("Account [%s] is ENABLED.", account, credentials);
                }

                CredentialStorage credentialStorage = getCredentialStorage(context, account, credentials, store);

                if (isDebugEnabled()) {
                    CREDENTIAL_LOGGER.debugf("Current credential storage for account [%s] is [%s].", account, credentialStorage);
                }

                if (validateCredential(credentialStorage, credentials)) {
                    if (credentialStorage != null && CredentialUtils.isCredentialExpired(credentialStorage)) {
                        credentials.setStatus(Status.EXPIRED);
                    } else if (Status.IN_PROGRESS.equals(credentials.getStatus())) {
                        credentials.setStatus(Status.VALID);
                    }
                }
            } else {
                if (isDebugEnabled()) {
                    CREDENTIAL_LOGGER.debugf("Account [%s] is DISABLED.", account, credentials);
                }
                credentials.setStatus(Status.ACCOUNT_DISABLED);
            }
        } else {
            if (isDebugEnabled()) {
                CREDENTIAL_LOGGER.debugf("Account NOT FOUND for credentials [%s][%s].", credentials.getClass(), credentials);
            }
        }

        credentials.setValidatedAccount(null);

        if (Status.VALID.equals(credentials.getStatus())) {
            credentials.setValidatedAccount(account);
        } else if (Status.IN_PROGRESS.equals(credentials.getStatus())) {
            credentials.setStatus(Status.INVALID);
        }

        if (isDebugEnabled()) {
            CREDENTIAL_LOGGER.debugf("Finishing validation for credential [%s][%s] validated using identity store [%s] and credential handler [%s]. Status [%s]. Validated Account [%s]",
                    credentials.getClass(), credentials, store, this, credentials.getStatus(), credentials.getValidatedAccount());
        }
    }

    protected abstract boolean validateCredential(final CredentialStorage credentialStorage, final V credentials);
    protected abstract Account getAccount(final IdentityContext context, final V credentials);
    protected abstract CredentialStorage getCredentialStorage(final IdentityContext context, final Account account,
                                                              final V credentials,
                                                              final S store);

    protected IdentityManager getIdentityManager(IdentityContext context) {
        IdentityManager identityManager = context.getParameter(IdentityManager.IDENTITY_MANAGER_CTX_PARAMETER);

        if (identityManager == null) {
            throw new IdentityManagementException("IdentityManager not set into context.");
        }

        return identityManager;
    }

    protected boolean isDebugEnabled() {
        return CREDENTIAL_LOGGER.isDebugEnabled();
    }

}
