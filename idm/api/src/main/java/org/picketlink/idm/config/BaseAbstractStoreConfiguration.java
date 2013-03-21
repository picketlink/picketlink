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

import static org.picketlink.idm.IDMLogger.LOGGER;
import static org.picketlink.idm.config.FeatureSet.addFeatureSupport;
import static org.picketlink.idm.config.FeatureSet.addRelationshipSupport;
import static org.picketlink.idm.config.FeatureSet.getDefaultRelationshipClasses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.ContextInitializer;


/**
 * The base class for store configurations
 *
 * @author Shane Bryzak
 */
public abstract class BaseAbstractStoreConfiguration<C extends BaseAbstractStoreConfiguration<?>> implements IdentityStoreConfiguration<C> {

    private final FeatureSet featureSet = new FeatureSet();

    private final Set<String> realms = new HashSet<String>();

    private final Set<String> tiers = new HashSet<String>();

    private List<ContextInitializer> contextInitializers = new ArrayList<ContextInitializer>();

    @Override
    public List<ContextInitializer> getContextInitializers() {
        return this.contextInitializers;
    }

    @Override
    public C addContextInitializer(ContextInitializer contextInitializer) {
        this.contextInitializers.add(contextInitializer);
        return (C) this;
    }

    @Override
    public C supportFeature(FeatureGroup... feature) {
        FeatureSet.addFeatureSupport(getFeatureSet(), feature);
        return (C) this;
    }

    @Override
    public C supportRelationshipType(Class<? extends Relationship>... types) {
        addRelationshipSupport(getFeatureSet(), types);

        if (types != null) {
            for (Class<? extends Relationship> relationshipType : types) {
                if (!getDefaultRelationshipClasses().contains(relationshipType)) {
                    getFeatureSet().setSupportsCustomRelationships(true);
                }
            }
        }

        return (C) this;
    }

    @Override
    public C supportAllFeatures() {
        addFeatureSupport(getFeatureSet());
        return (C) this;
    }

    @Override
    public FeatureSet getFeatureSet() {
        return featureSet;
    }

    public C addRealm(String... realmNames) {
        if (realmNames != null) {
            this.realms.addAll(Arrays.asList(realmNames));
        }

        return (C) this;
    }

    public C addTier(String... tierNames) {
        if (tierNames != null) {
            this.tiers.addAll(Arrays.asList(tierNames));
        }

        return (C) this;
    }

    public Set<String> getRealms() {
        if (this.realms.isEmpty()) {
            this.realms.add(Realm.DEFAULT_REALM);
        }

        return Collections.unmodifiableSet(this.realms);
    }

    public Set<String> getTiers() {
        return Collections.unmodifiableSet(this.tiers);
    }

    @Override
    public final void init() throws SecurityConfigurationException {
        initConfig();
        this.featureSet.lock();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debugf("FeatureSet for %s", this);
            LOGGER.debug("Features [");

            for (Entry<FeatureGroup, Set<FeatureOperation>> entry : this.featureSet.getSupportedFeatures().entrySet()) {
                LOGGER.debugf("%s.%s", entry.getKey(), entry.getValue());
            }

            LOGGER.debug("]");

            LOGGER.debug("Relationships [");

            for (Entry<Class<? extends Relationship>, Set<FeatureOperation>> entry : this.featureSet.getSupportedRelationships().entrySet()) {
                LOGGER.debugf("%s.%s", entry.getKey(), entry.getValue());
            }

            LOGGER.debug("]");
        }
    }

    protected abstract void initConfig();
}
