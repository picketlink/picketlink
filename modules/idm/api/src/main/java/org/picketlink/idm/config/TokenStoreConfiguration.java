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

import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.spi.ContextInitializer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.picketlink.idm.credential.Token.Consumer;

/**
 * <p>Holds all the configuration for a token-based identity store.</p>
 *
 * @author Pedro Igor
 */
public class TokenStoreConfiguration extends AbstractIdentityStoreConfiguration {

    private List<Consumer> tokenConsumer;

    protected TokenStoreConfiguration(
        List<Consumer> tokenConsumer,
        Map<Class<? extends AttributedType>,
            Set<IdentityOperation>> supportedTypes,
        Map<Class<? extends AttributedType>,
            Set<IdentityOperation>>
            unsupportedTypes,
        List<ContextInitializer> contextInitializers,
        Map<String, Object> credentialHandlerProperties,
        Set<Class<? extends CredentialHandler>> credentialHandlers,
        boolean supportsAttribute,
        boolean supportsCredential,
        boolean supportsPermissions) {
        super(supportedTypes, unsupportedTypes, contextInitializers, credentialHandlerProperties, credentialHandlers, supportsAttribute, supportsCredential, supportsPermissions);
        this.tokenConsumer = tokenConsumer;
    }

    public List<Consumer> getTokenConsumer() {
        return this.tokenConsumer;
    }

    @Override
    public boolean supportsPartition() {
        return false;
    }

    @Override
    public boolean supportsAttribute() {
        return false;
    }

    @Override
    public boolean supportsPermissions() {
        return false;
    }

    @Override
    public boolean supportsCredential() {
        return true;
    }
}
