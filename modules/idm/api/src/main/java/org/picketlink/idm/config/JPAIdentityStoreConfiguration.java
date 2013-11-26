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
package org.picketlink.idm.config;

import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.spi.ContextInitializer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * Defines the configuration for a JPA based IdentityStore implementation.
 *
 * @author Shane Bryzak
 */
public class JPAIdentityStoreConfiguration extends AbstractIdentityStoreConfiguration {

    private final Set<Class<?>> entityTypes;

    protected JPAIdentityStoreConfiguration(
            Set<Class<?>> entityTypes,
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes,
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> unsupportedTypes,
            List<ContextInitializer> contextInitializers,
            Map<String, Object> credentialHandlerProperties,
            Set<Class<? extends CredentialHandler>> credentialHandlers,
            boolean supportsAttribute,
            boolean supportsCredential,
            boolean supportsPermissions) {
        super(supportedTypes, unsupportedTypes, contextInitializers,credentialHandlerProperties, credentialHandlers,
                supportsAttribute, supportsCredential, supportsPermissions);

        if (entityTypes == null) {
            throw MESSAGES.configJpaStoreNoEntityClassesProvided();
        }

        this.entityTypes = entityTypes;
    }

    public Set<Class<?>> getEntityTypes() {
        return this.entityTypes;
    }

}
