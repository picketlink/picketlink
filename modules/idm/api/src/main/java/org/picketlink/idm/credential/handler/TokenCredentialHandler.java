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

/**
 * @author Pedro Igor
 */
@SupportsCredentials(
    credentialClass = { TokenCredential.class, Token.class },
    credentialStorage = TokenCredentialStorage.class
)
public class TokenCredentialHandler<S extends CredentialStore<?>, V extends TokenCredential, U extends Token> extends AbstractCredentialHandler<S, V, U> {

    /**
     * <p>Stores a <b>stateless</b> and thread-safe instance of {@link org.picketlink.idm.credential.Token.Provider}.</p>
     */
    public static final String TOKEN_PROVIDER = "TOKEN_PROVIDER";

    private final List<Token.Provider> tokenProvider = new ArrayList<Token.Provider>();

    @Override
    public void setup(S store) {
        super.setup(store);

        Object configuredTokenProviders = store.getConfig().getCredentialHandlerProperties().get(TOKEN_PROVIDER);

        if (configuredTokenProviders != null) {
            try {
                if (Token.Provider.class.isInstance(configuredTokenProviders)) {
                    this.tokenProvider.add((Token.Provider) configuredTokenProviders);
                } else if (configuredTokenProviders.getClass().isArray()) {
                    this.tokenProvider.addAll(Arrays.asList((Token.Provider[]) configuredTokenProviders));
                } else if (List.class.isInstance(configuredTokenProviders)) {
                    this.tokenProvider.addAll((List<Token.Provider>) configuredTokenProviders);
                }
            } catch (ClassCastException cce) {
                throw new SecurityConfigurationException("Token provider is not a " + Token.Provider.class.getName() + " instance. You provided " + configuredTokenProviders);
            }
        }
    }

    @Override
    protected boolean validateCredential(IdentityContext context, CredentialStorage credentialStorage, V credentials) {
        return getTokenProvider(credentials.getToken()).validate(credentials.getToken());
    }

    @Override
    protected Account getAccount(IdentityContext context, V credentials) {
        Token token = credentials.getToken();

        return getTokenProvider(token).getAccount(token);
    }

    @Override
    protected CredentialStorage getCredentialStorage(IdentityContext context, Account account, V credentials, S store) {
        return store.retrieveCurrentCredential(context, account, TokenCredentialStorage.class);
    }

    @Override
    public void update(IdentityContext context, Account account, U credential, S store, Date effectiveDate, Date expiryDate) {
        TokenCredentialStorage tokenStorage = getTokenProvider(credential).getTokenStorage(account, credential);

        // if no storage was provided by the token provider, we use the default one.
        if (tokenStorage == null) {
            tokenStorage = new TokenCredentialStorage();

            tokenStorage.setType(credential.getType());
            tokenStorage.setValue(credential.getToken());
        }

        if (effectiveDate != null) {
            tokenStorage.setEffectiveDate(effectiveDate);
        }

        if (tokenStorage.getExpiryDate() == null) {
            tokenStorage.setExpiryDate(expiryDate);
        }

        if (tokenStorage.getType() == null) {
            throw new IdentityManagementException("TokenCredentialStorage can not have a null type.");
        }

        store.storeCredential(context, account, tokenStorage);
    }

    private Token.Provider getTokenProvider(Token token) {
        if (this.tokenProvider.isEmpty()) {
            throw new SecurityConfigurationException("You must provide one or more(Array or List) " + Token.Provider.class.getName() + " instances using the following credential property: " + TokenCredentialHandler.class.getName() + ".TOKEN_PROVIDER");
        }

        for (Token.Provider selectedProvider : this.tokenProvider) {
            if (selectedProvider.supports(token)) {
                return selectedProvider;
            }
        }

        throw new SecurityConfigurationException("There is no " + Token.Provider.class.getName() + " that supports this token [" + token + "]");
    }
}