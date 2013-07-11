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

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.picketlink.idm.IDMLogger.LOGGER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentitySessionHandler;

/**
 * The base class for store configurations
 *
 * @author Shane Bryzak
 */
public abstract class BaseAbstractStoreConfiguration implements IdentityStoreConfiguration {

    private final Set<String> realms = new HashSet<String>();
    private final Set<String> tiers = new HashSet<String>();
    private List<ContextInitializer> contextInitializers = new ArrayList<ContextInitializer>();
    private IdentitySessionHandler identityContextHandler;
    private Map<String, Object> credentialHandlerProperties = new HashMap<String, Object>();
    private List<Class<? extends CredentialHandler>> credentialHandlers = new ArrayList<Class<? extends CredentialHandler>>();
    /**
     * Metadata reflecting which features are supported by this identity store
     */
    private final Map<FeatureGroup, Set<FeatureOperation>> supportedFeatures;
    private final Map<Class<? extends Relationship>, Set<FeatureOperation>> supportedRelationships;

    protected BaseAbstractStoreConfiguration(Map<FeatureGroup, Set<FeatureOperation>> supportedFeatures,
            Map<Class<? extends Relationship>, Set<FeatureOperation>> supportedRelationships, Set<String> realms,
            Set<String> tiers, List<ContextInitializer> contextInitializers, IdentitySessionHandler identityContextHandler, Map<String, Object> credentialHandlerProperties,
            List<Class<? extends CredentialHandler>> credentialHandlers) {
        this.realms.addAll(realms);
        this.tiers.addAll(tiers);
        this.contextInitializers.addAll(contextInitializers);
        this.identityContextHandler = identityContextHandler;
        this.credentialHandlerProperties.putAll(credentialHandlerProperties);
        this.credentialHandlers.addAll(credentialHandlers);
        this.supportedFeatures = supportedFeatures;
        this.supportedRelationships = supportedRelationships;
    }

    @Override
    public final void init() throws SecurityConfigurationException {
        initConfig();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("FeatureSet for %s", this);
            LOGGER.debug("Features [");

            for (Entry<FeatureGroup, Set<FeatureOperation>> entry : getSupportedFeatures().entrySet()) {
                LOGGER.debugf("%s.%s", entry.getKey(), entry.getValue());
            }

            LOGGER.debug("]");

            LOGGER.debug("Relationships [");

            for (Entry<Class<? extends Relationship>, Set<FeatureOperation>> entry : getSupportedRelationships().entrySet()) {
                LOGGER.debugf("%s.%s", entry.getKey(), entry.getValue());
            }

            LOGGER.debug("]");
        }
    }

    protected abstract void initConfig();

    @Override
    public IdentitySessionHandler getIdentitySessionHandler() {
        return identityContextHandler;
    }

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
    public boolean supportsFeature(FeatureGroup feature, FeatureOperation operation) {
        if (!this.supportedFeatures.containsKey(feature)) {
            return false;
        }

        if (operation == null) {
            return true;
        }

        return this.supportedFeatures.get(feature).contains(operation);
    }

    /**
     * <p>
     * Check if the given Relationship type is supported.
     * </p>
     *
     * @param feature
     * @param operation
     * @return
     */
    @Override
    public boolean supportsRelationship(Class<? extends Relationship> relationshipType, FeatureOperation operation) {
        if (!this.supportedRelationships.containsKey(relationshipType)) {
            return false;
        }

        if (operation == null) {
            return true;
        }

        return this.supportedRelationships.get(relationshipType).contains(operation);
    }

    @Override
    public Set<String> getRealms() {
        return unmodifiableSet(this.realms);
    }

    @Override
    public Set<String> getTiers() {
        return unmodifiableSet(this.tiers);
    }

    @Override
    public Map<FeatureGroup, Set<FeatureOperation>> getSupportedFeatures() {
        return unmodifiableMap(this.supportedFeatures);
    }

    @Override
    public Map<Class<? extends Relationship>, Set<FeatureOperation>> getSupportedRelationships() {
        return unmodifiableMap(this.supportedRelationships);
    }

    /**
     * <p>
     * Removes the given {@link FeatureGroup} and all supported {@link FeatureOperation} from the features set.
     * </p>
     *
     * @param feature
     * @throws SecurityConfigurationException If this instance is locked and changes are no more allowed.
     */
    protected void removeFeature(FeatureGroup feature) throws SecurityConfigurationException {
        this.supportedFeatures.remove(feature);

        if (FeatureGroup.relationship.equals(feature)) {
            this.supportedRelationships.clear();
        }
    }

}
