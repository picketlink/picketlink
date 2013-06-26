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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.spi.ContextInitializer;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.picketlink.idm.IDMLogger.LOGGER;

/**
 * The base class for store configurations
 *
 * @author Shane Bryzak
 */
public abstract class AbstractIdentityStoreConfiguration implements IdentityStoreConfiguration {

    private Map<Class<? extends AttributedType>, Set<TypeOperation>> supportedTypes = new HashMap<Class<? extends AttributedType>, Set<TypeOperation>>();
    private Map<Class<? extends AttributedType>, Set<TypeOperation>> unsupportedTypes = new HashMap<Class<? extends AttributedType>, Set<TypeOperation>>();

    private List<ContextInitializer> contextInitializers = new ArrayList<ContextInitializer>();
    private Map<String, Object> credentialHandlerProperties = new HashMap<String, Object>();
    private List<Class<? extends CredentialHandler>> credentialHandlers = new ArrayList<Class<? extends CredentialHandler>>();

    protected AbstractIdentityStoreConfiguration(
            Map<Class<? extends AttributedType>, Set<TypeOperation>> supportedTypes,
            Map<Class<? extends AttributedType>, Set<TypeOperation>> unsupportedTypes,
            List<ContextInitializer> contextInitializers,
            Map<String, Object> credentialHandlerProperties,
            List<Class<? extends CredentialHandler>> credentialHandlers) {
        this.supportedTypes = supportedTypes;
        this.unsupportedTypes = unsupportedTypes;
        this.contextInitializers.addAll(contextInitializers);
        this.credentialHandlerProperties.putAll(credentialHandlerProperties);
        this.credentialHandlers.addAll(credentialHandlers);
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
    public List<ContextInitializer> getContextInitializers() {
        return unmodifiableList(this.contextInitializers);
    }

    @Override
    public List<Class<? extends CredentialHandler>> getCredentialHandlers() {
        return unmodifiableList(this.credentialHandlers);
    }

    @Override
    public Map<String, Object> getCredentialHandlerProperties() {
        return unmodifiableMap(this.credentialHandlerProperties);
    }

    /**
     * <p>
     * Check if the {@link FeatureGroup} is supported.
     * </p>
     *
     * @param feature
     * @param operation
     * @return
     */
    @Override
    public boolean supportsType(Class<? extends AttributedType> type, TypeOperation operation) {
        if (operation == null) {
            throw new IllegalArgumentException("operation may not be null");
        }

        return isTypeOperationSupported(type, operation) != -1;
    }

    private int isTypeOperationSupported(Class<? extends AttributedType> type, TypeOperation operation) {
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
            }
            return score;
        } else {
            return -1;
        }
    }
}
