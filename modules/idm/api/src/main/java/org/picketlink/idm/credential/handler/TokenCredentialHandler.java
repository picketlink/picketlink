/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.idm.credential.handler;

import org.picketlink.common.reflection.Reflections;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.credential.Token;
import org.picketlink.idm.credential.TokenCredential;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.credential.storage.CredentialStorage;
import org.picketlink.idm.credential.storage.TokenCredentialStorage;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.spi.CredentialStore;
import org.picketlink.idm.spi.IdentityContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.picketlink.idm.credential.Token.Consumer;

/**
 * @author Pedro Igor
 */
@SupportsCredentials(
    credentialClass = { TokenCredential.class, Token.class },
    credentialStorage = TokenCredentialStorage.class
)
public class TokenCredentialHandler<S extends CredentialStore<?>, V extends TokenCredential, U extends Token> extends AbstractCredentialHandler<S, V, U> {

    /**
     * <p>Stores a <b>stateless</b> and thread-safe instances of {@link org.picketlink.idm.credential.Token.Consumer}. The value can be
     * a single instance, a {@list List} or an array.</p>
     */
    public static final String TOKEN_CONSUMER = "TOKEN_CONSUMER";

    private final List<Consumer> tokenConsumers = new ArrayList<Consumer>();

    @Override
    public void setup(S store) {
        super.setup(store);

        Object configuredTokenConsumers = store.getConfig().getCredentialHandlerProperties().get(TOKEN_CONSUMER);

        if (configuredTokenConsumers != null) {
            try {
                if (Consumer.class.isInstance(configuredTokenConsumers)) {
                    this.tokenConsumers.add((Consumer) configuredTokenConsumers);
                } else if (configuredTokenConsumers.getClass().isArray()) {
                    this.tokenConsumers.addAll(Arrays.asList((Consumer[]) configuredTokenConsumers));
                } else if (List.class.isInstance(configuredTokenConsumers)) {
                    this.tokenConsumers.addAll((List<Consumer>) configuredTokenConsumers);
                }
            } catch (ClassCastException cce) {
                throw new SecurityConfigurationException("Token consumer is not a " + Consumer.class.getName() + " instance. You provided " + configuredTokenConsumers);
            }
        }
    }

    @Override
    protected boolean validateCredential(IdentityContext context, CredentialStorage credentialStorage, V credentials, S store) {
        Token token = credentials.getToken();

        if (getTokenConsumer(token) != null) {
            return getTokenConsumer(token).validate(token);
        }

        if (credentialStorage != null) {
            TokenCredentialStorage tokenCredentialStorage = (TokenCredentialStorage) credentialStorage;

            if (tokenCredentialStorage.getToken().equals(token.getToken())
                && tokenCredentialStorage.getType().equals(token.getType())) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected Account getAccount(IdentityContext context, V credentials) {
        Token token = credentials.getToken();

        if (token != null) {
            String subject = token.getSubject();

            if (subject == null) {
                throw new IdentityManagementException("No subject returned from token [" + token + "].");
            }

            Account account = getAccount(context, subject);

            if (account == null) {
                account = getAccountById(context, subject);
            }

            return account;
        }

        return null;
    }

    @Override
    protected CredentialStorage getCredentialStorage(IdentityContext context, Account account, V credentials, S store) {
        return store.retrieveCurrentCredential(context, account, getCredentialStorageType());
    }

    @Override
    public CredentialStorage createCredentialStorage(IdentityContext context, Account account, U credential, S store, Date effectiveDate, Date expiryDate) {
        TokenCredentialStorage tokenStorage = createCredentialStorageInstance();

        tokenStorage.setType(credential.getType());
        tokenStorage.setToken(credential.getToken());

        if (effectiveDate != null) {
            tokenStorage.setEffectiveDate(effectiveDate);
        }

        if (tokenStorage.getExpiryDate() == null) {
            tokenStorage.setExpiryDate(expiryDate);
        }

        if (tokenStorage.getType() == null) {
            throw new IdentityManagementException("TokenCredentialStorage can not have a null type.");
        }

        return tokenStorage;
    }

    protected Class<? extends TokenCredentialStorage> getCredentialStorageType() {
        SupportsCredentials supportsCredentials = getClass().getAnnotation(SupportsCredentials.class);
        Class<? extends CredentialStorage> credentialStorage = supportsCredentials.credentialStorage();

        try {
            return (Class<? extends TokenCredentialStorage>) credentialStorage;
        } catch (ClassCastException cce) {
            throw new IdentityManagementException("CredentialStorage [" + credentialStorage + "] is not a " + TokenCredentialStorage.class + " type.", cce);
        }
    }

    protected TokenCredentialStorage createCredentialStorageInstance() {
        try {
            return Reflections.newInstance(getCredentialStorageType());
        } catch (Exception e) {
            throw new IdentityManagementException("Could not create TokenStorageCredential [" + getCredentialStorageType() + "].", e);
        }
    }

    private <T extends Token> Consumer<T> getTokenConsumer(T token) {
        for (Consumer selectedConsumer : this.tokenConsumers) {
            if (selectedConsumer.getTokenType().isAssignableFrom(token.getClass())) {
                return selectedConsumer;
            }
        }

        return null;
    }
}