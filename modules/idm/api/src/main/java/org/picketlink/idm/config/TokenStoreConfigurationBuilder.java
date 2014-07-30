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
package org.picketlink.idm.config;

import org.picketlink.idm.credential.handler.TokenCredentialHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.picketlink.idm.credential.Token.Consumer;

/**
 * @author Pedro Igor
 */
public class TokenStoreConfigurationBuilder extends IdentityStoreConfigurationBuilder<TokenStoreConfiguration, TokenStoreConfigurationBuilder> {

    private final List<Consumer> tokenConsumer = new ArrayList<Consumer>();

    public TokenStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
        super(builder);
    }

    @Override
    protected TokenStoreConfiguration create() throws SecurityConfigurationException {
        return new TokenStoreConfiguration(
            this.tokenConsumer,
            getSupportedTypes(),
            getUnsupportedTypes(),
            getContextInitializers(),
            getCredentialHandlerProperties(),
            getCredentialHandlers(),
            isSupportAttributes(),
            isSupportCredentials(),
            isSupportPermissions()
        );
    }

    /**
     * <p>You must set a {@link org.picketlink.idm.credential.Token.Consumer} if you want to validate tokens and extract identity
     * information from them.</p>
     *
     * @param tokenConsumers A single or an array of {@link org.picketlink.idm.credential.Token.Consumer} instances.
     * @return
     */
    public TokenStoreConfigurationBuilder tokenConsumer(Consumer... tokenConsumers) {
        this.tokenConsumer.addAll(Arrays.asList(tokenConsumers));
        setCredentialHandlerProperty(TokenCredentialHandler.TOKEN_CONSUMER, this.tokenConsumer);
        return this;
    }
}
