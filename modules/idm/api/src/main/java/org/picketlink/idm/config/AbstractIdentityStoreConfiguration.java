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

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.picketlink.idm.IDMMessages;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContext;
import org.picketlink.idm.spi.IdentityStore;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.picketlink.idm.IDMLogger.LOGGER;

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
     * <p>{@link AttributedType} types are not supported by this configuration.
     * This allows us to trim any type that we don't want to support off the hierarchy tree</p>
     */
    private final Map<Class<? extends AttributedType>, Set<IdentityOperation>> unsupportedTypes;

    /**
     * <p>{@link ContextInitializer} instances that should be used to initialize the
     * {@link org.picketlink.idm.spi.IdentityContext} before invoking an identity store operation.</p>
     */
    private final List<ContextInitializer> contextInitializers;

    /**
     * <p>Configuration properties for {@CredentialHandler}.</p>
     */
    private final Map<String, Object> credentialHandlerProperties;

    /**
     * <p>Additional {@link CredentialHandler} types supported by this configuration.</p>
     */
    private final List<Class<? extends CredentialHandler>> credentialHandlers;

    protected AbstractIdentityStoreConfiguration(
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes,
            Map<Class<? extends AttributedType>, Set<IdentityOperation>> unsupportedTypes,
            List<ContextInitializer> contextInitializers,
            Map<String, Object> credentialHandlerProperties,
            List<Class<? extends CredentialHandler>> credentialHandlers) {
        this.supportedTypes = unmodifiableMap(supportedTypes);
        this.unsupportedTypes = unmodifiableMap(unsupportedTypes);
        this.contextInitializers = unmodifiableList(contextInitializers);
        this.credentialHandlerProperties = unmodifiableMap(credentialHandlerProperties);
        this.credentialHandlers = unmodifiableList(credentialHandlers);
    }

    @Override
    public final void init() throws SecurityConfigurationException {
        initConfig();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("FeatureSet for %s", this);
            LOGGER.debug("Features [");

            // FIXME
            //for (Entry<FeatureGroup, Set<FeatureOperation>> entry : getSupportedFeatures().entrySet()) {
            //    LOGGER.debugf("%s.%s", entry.getKey(), entry.getValue());
           // }

            LOGGER.debug("]");

            LOGGER.debug("Relationships [");

            // FIXME
            //for (Entry<Class<? extends Relationship>, Set<FeatureOperation>> entry : getSupportedRelationships().entrySet()) {
            //    LOGGER.debugf("%s.%s", entry.getKey(), entry.getValue());
            //}

            LOGGER.debug("]");
        }
    }

    protected abstract void initConfig();

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
        return this.credentialHandlers;
    }

    @Override
    public Map<String, Object> getCredentialHandlerProperties() {
        return this.credentialHandlerProperties;
    }

    @Override
    public Set<Class<? extends AttributedType>> getSupportedTypes() {
        return this.supportedTypes.keySet();
    }

    @Override
    public Set<Class<? extends Relationship>> getSupportedRelationships() {
        return null;
    }

    public boolean supportsType(Class<? extends AttributedType> type, IdentityOperation operation) {
        if (operation == null) {
            throw IDMMessages.MESSAGES.nullArgument("TypeOperation");
        }

        return isTypeOperationSupported(type, operation) != -1;
    }

    @Override
    public boolean supportsPartition() {
        return supportsType(Partition.class, IdentityOperation.create);
    }

    private int isTypeOperationSupported(Class<? extends AttributedType> type, IdentityOperation operation) {
        int score = -1;

        for (Class<? extends AttributedType> cls : supportedTypes.keySet()) {
            int clsScore = calcScore(type, cls);
            if (clsScore > score && supportedTypes.get(cls).contains(operation)) {
                score = clsScore;
            }
        }

        for (Class<? extends AttributedType> cls : unsupportedTypes.keySet()) {
            if (cls.isAssignableFrom(type) && unsupportedTypes.get(cls).contains(operation)) {
                score = -1;
                break;
            }
        }
        return score;
    }

    private int calcScore(Class<?> type, Class<?> targetClass) {
        if (type.equals(targetClass)) {
            return 0;
        } else if (targetClass.isAssignableFrom(type)) {
            int score = 0;

            Class<?> cls = type.getSuperclass();
            while (!cls.equals(Object.class)) {
                if (targetClass.isAssignableFrom(cls)) {
                    score++;
                } else {
                    break;
                }
                cls = cls.getSuperclass();
            }
            return score;
        } else {
            return -1;
        }
    }
}