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
package org.picketlink.idm.internal;

import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.credential.Credentials;
import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.credential.handler.annotations.SupportsCredentials;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.RelationshipQuery;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.picketlink.idm.IDMLog.IDENTITY_STORE_LOGGER;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author pedroigor
 */
public abstract class AbstractIdentityStore<C extends IdentityStoreConfiguration> implements IdentityStore<C> {

    private C configuration;
    private Map<Class<? extends CredentialHandler>, CredentialHandler> credentialHandlers = new HashMap<Class<? extends CredentialHandler>, CredentialHandler>();

    @Override
    public void setup(C config) {
        IDENTITY_STORE_LOGGER.storeInitializing(getClass());

        this.configuration = config;

        if (IDENTITY_STORE_LOGGER.isDebugEnabled()) {
            IDENTITY_STORE_LOGGER.debugf("[%s]: [", this.configuration);
            IDENTITY_STORE_LOGGER.debugf("  Type: %s", this.configuration.getIdentityStoreType());
            IDENTITY_STORE_LOGGER.debugf("  Supports partition: %s", this.configuration.supportsPartition());
            IDENTITY_STORE_LOGGER.debugf("  Supports attribute: %s", this.configuration.supportsAttribute());
            IDENTITY_STORE_LOGGER.debugf("  Supports credential: %s", this.configuration.supportsCredential());
            IDENTITY_STORE_LOGGER.debugf("  Credential Handlers: %s", this.configuration.getCredentialHandlers());
            IDENTITY_STORE_LOGGER.debugf("  Supported types: %s", this.configuration.getSupportedTypes().keySet());
            IDENTITY_STORE_LOGGER.debugf("  Unsupported types: %s", this.configuration.getUnsupportedTypes().keySet());
            IDENTITY_STORE_LOGGER.debugf("  Context Initializers: %s", this.configuration.getContextInitializers());
            IDENTITY_STORE_LOGGER.debug("]");
        }

        initializeCredentialHandlers();
    }

    @Override
    public C getConfig() {
        return this.configuration;
    }

    @Override
    public void add(IdentityContext context, AttributedType attributedType) {
        attributedType.setId(context.getIdGenerator().generate());

        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;

            identityType.setPartition(context.getPartition());

            if (isTraceEnabled()) {
                IDENTITY_STORE_LOGGER.tracef("Type with identifier [%s] belongs to partition [%s][%s]", attributedType.getId(), context.getPartition().getName(), context.getPartition());
            }
        }

        addAttributedType(context, attributedType);

        if (isTraceEnabled()) {
            IDENTITY_STORE_LOGGER.tracef("Type with identifier [%s] successfully added to identity store [%s].", attributedType.getId(), this);
        }
    }

    @Override
    public void update(IdentityContext context, AttributedType attributedType) {
        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;
            identityType.setPartition(context.getPartition());
        }

        updateAttributedType(context, attributedType);

        if (isTraceEnabled()) {
            IDENTITY_STORE_LOGGER.tracef("Type with identifier [%s] successfully updated to identity store [%s].", attributedType.getId(), this);
        }
    }

    @Override
    public void remove(IdentityContext context, AttributedType attributedType) {
        if (IdentityType.class.isInstance(attributedType)) {
            IdentityType identityType = (IdentityType) attributedType;
            identityType.setPartition(context.getPartition());

            removeFromRelationships(context, identityType);

            if (Account.class.isInstance(identityType)) {
                removeCredentials(context, (Account) identityType);
            }
        }

        removeAttributedType(context, attributedType);

        if (isTraceEnabled()) {
            IDENTITY_STORE_LOGGER.tracef("Type with identifier [%s] successfully removed from identity store [%s].", attributedType.getId(), this);
        }
    }

    protected abstract void removeFromRelationships(IdentityContext context, IdentityType identityType);
    protected abstract void removeCredentials(IdentityContext context, Account account);

    @Override
    public void validateCredentials(IdentityContext context, Credentials credentials) {
        Class<? extends CredentialHandler> credentialHandler = getCredentialHandler(credentials);
        this.credentialHandlers.get(credentialHandler).validate(context, credentials, this);
    }

    @Override
    public void updateCredential(IdentityContext context, Account account, Object credential, Date effectiveDate, Date expiryDate) {
        Class<? extends CredentialHandler> credentialHandler = getCredentialHandler(credential);
        this.credentialHandlers.get(credentialHandler).update(context, account, credential, this, effectiveDate, expiryDate);
    }

    @Override
    public <V extends IdentityType> int countQueryResults(IdentityContext context, IdentityQuery<V> identityQuery) {
        int limit = identityQuery.getLimit();
        int offset = identityQuery.getOffset();

        identityQuery.setLimit(0);
        identityQuery.setOffset(0);

        int resultCount = identityQuery.getResultList().size();

        identityQuery.setLimit(limit);
        identityQuery.setOffset(offset);

        return resultCount;
    }

    @Override
    public <V extends Relationship> int countQueryResults(final IdentityContext context, final RelationshipQuery<V> query) {
        int limit = query.getLimit();
        int offset = query.getOffset();

        query.setLimit(0);
        query.setOffset(0);

        int resultCount = query.getResultList().size();

        query.setLimit(limit);
        query.setOffset(offset);

        return resultCount;
    }

    protected void addAttributedType(IdentityContext context, AttributedType attributedType) {

    }

    protected abstract void updateAttributedType(IdentityContext context, AttributedType attributedType);
    protected abstract void removeAttributedType(IdentityContext context, AttributedType attributedType);

    private Class<? extends CredentialHandler> getCredentialHandler(Object credentials) {
        Class<? extends CredentialHandler> credentialHandler = null;

        if (credentialHandler == null) {
            for (Class<? extends CredentialHandler> handlerClass : getConfig().getCredentialHandlers()) {
                if (handlerClass.isAnnotationPresent(SupportsCredentials.class)) {
                    for (Class<?> cls : handlerClass.getAnnotation(SupportsCredentials.class).credentialClass()) {
                        if (cls.isAssignableFrom(credentials.getClass())) {
                            credentialHandler = handlerClass;

                            // if we found a specific handler for the credential, immediately return.
                            if (cls.equals(credentials.getClass())) {
                                return handlerClass;
                            }
                        }
                    }
                }
            }
        }

        if (credentialHandler == null) {
            throw MESSAGES.credentialHandlerNotFoundForCredentialType(credentials.getClass());
        }

        return credentialHandler;
    }

    private void initializeCredentialHandlers() {
        for (Class<? extends CredentialHandler> handlerType : configuration.getCredentialHandlers()) {
            CredentialHandler credentialHandler = null;

            try {
                credentialHandler = handlerType.newInstance();
                credentialHandler.setup(this);
            } catch (Exception e) {
                throw MESSAGES.instantiationError(handlerType, e);
            }

            this.credentialHandlers.put(handlerType, credentialHandler);
        }
    }

    private boolean isTraceEnabled() {
        return IDENTITY_STORE_LOGGER.isTraceEnabled();
    }

}