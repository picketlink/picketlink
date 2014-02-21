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
import org.picketlink.idm.credential.handler.annotations.CredentialHandlers;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.util.IDMUtil.isTypeOperationSupported;

/**
 * <p>Base class for {@link IdentityStoreConfiguration} implementations.</p>
 *
 * @author Shane Bryzak
 */
public abstract class AbstractIdentityStoreConfiguration implements IdentityStoreConfiguration {

    /**
     * <p>{@link AttributedType} types are supported by this configuration.</p>
     */
    private final Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes;
    /**
     * <p>{@link AttributedType} types are not supported by this configuration. This allows us to trim any type that we
     * don't want to support off the hierarchy tree</p>
     */
    private final Map<Class<? extends AttributedType>, Set<IdentityOperation>> unsupportedTypes;
    /**
     * <p>{@link ContextInitializer} instances that should be used to initialize the {@link
     * org.picketlink.idm.spi.IdentityContext} before invoking an identity store operation.</p>
     */
    private final List<ContextInitializer> contextInitializers;
    /**
     * <p>Configuration properties for {@CredentialHandler}.</p>
     */
    private final Map<String, Object> credentialHandlerProperties;
    /**
     * <p>Additional {@link CredentialHandler} types supported by this configuration.</p>
     */
    private final Set<Class<? extends CredentialHandler>> credentialHandlers;
    private final boolean supportsCredential;
    private Class<? extends IdentityStore> identityStoreType;
    private final boolean supportsAttribute;
    private List<Class<? extends CredentialHandler>> supportedCredentialHandlers;
    private final boolean supportsPermissions;

    protected AbstractIdentityStoreConfiguration(
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes,
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> unsupportedTypes,
            List<ContextInitializer> contextInitializers,
            Map<String, Object> credentialHandlerProperties,
            Set<Class<? extends CredentialHandler>> credentialHandlers,
            boolean supportsAttribute,
            boolean supportsCredential,
            boolean supportsPermissions) {
        if(supportedTypes == null){
            throw MESSAGES.nullArgument("supportedTypes");
        }
        this.supportedTypes = unmodifiableMap(supportedTypes);
        this.unsupportedTypes = unmodifiableMap(unsupportedTypes);
        this.contextInitializers = unmodifiableList(contextInitializers);
        this.credentialHandlers = unmodifiableSet(credentialHandlers);
        this.credentialHandlerProperties = unmodifiableMap(credentialHandlerProperties);
        this.supportsAttribute = supportsAttribute;
        this.supportsCredential = supportsCredential;
        this.supportsPermissions = supportsPermissions;
    }

    @Override
    public void addContextInitializer(ContextInitializer contextInitializer) {
        this.contextInitializers.add(contextInitializer);
    }

    public void initializeContext(IdentityContext context, IdentityStore<?> store) {
        for (ContextInitializer initializer : contextInitializers) {
            initializer.initContextForStore(context, store);
        }
    }

    @Override
    public List<Class<? extends CredentialHandler>> getCredentialHandlers() {
        if (this.supportedCredentialHandlers == null) {
            this.supportedCredentialHandlers = new ArrayList<Class<? extends CredentialHandler>>(credentialHandlers);

            if (getIdentityStoreType() != null) {
                CredentialHandlers storeHandlers = getIdentityStoreType().getAnnotation(CredentialHandlers.class);

                if (storeHandlers != null) {
                    this.supportedCredentialHandlers.addAll(this.supportedCredentialHandlers.size(), Arrays.asList
                            (storeHandlers.value()));
                }
            }
        }

        return this.supportedCredentialHandlers;
    }

    @Override
    public Map<String, Object> getCredentialHandlerProperties() {
        return this.credentialHandlerProperties;
    }

    public boolean supportsType(Class<? extends AttributedType> type, IdentityOperation operation) {
        if (operation == null) {
            throw MESSAGES.nullArgument("TypeOperation");
        }

        return isTypeOperationSupported(type, operation, this.supportedTypes, this.unsupportedTypes) != -1;
    }

    @Override
    public boolean supportsPartition() {
        for (Class<?> supportedType : getSupportedTypes().keySet()) {
            if (Partition.class.isAssignableFrom(supportedType)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Class<? extends IdentityStore> getIdentityStoreType() {
        return this.identityStoreType;
    }

    public <T extends IdentityStore> void setIdentityStoreType(Class<T> identityStoreType) {
        this.identityStoreType = identityStoreType;
    }

    @Override
    public Map<Class<? extends AttributedType>, Set<IdentityOperation>> getSupportedTypes() {
        return this.supportedTypes;
    }

    @Override
    public Map<Class<? extends AttributedType>, Set<IdentityOperation>> getUnsupportedTypes() {
        return this.unsupportedTypes;
    }

    @Override
    public List<ContextInitializer> getContextInitializers() {
        return this.contextInitializers;
    }

    @Override
    public boolean supportsAttribute() {
        return this.supportsAttribute;
    }

    @Override
    public boolean supportsCredential() {
        return this.supportsCredential;
    }

    @Override
    public boolean supportsPermissions() {
        return this.supportsPermissions;
    }

}