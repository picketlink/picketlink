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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.model.Realm;
import org.picketlink.idm.model.Relationship;


/**
 * The base class for store configurations
 *
 * @author Shane Bryzak
 */
public abstract class BaseAbstractStoreConfiguration implements IdentityStoreConfiguration {

    private final FeatureSet featureSet = new FeatureSet();

    private final Set<String> realms = new HashSet<String>();

    public FeatureSet getFeatureSet() {
        return featureSet;
    }

    public void addRealm(String name) {
        realms.add(name);
    }

    public Set<String> getRealms() {
        if (this.realms.isEmpty()) {
            this.realms.add(Realm.DEFAULT_REALM);
        }

        return Collections.unmodifiableSet(this.realms);
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

    public abstract void initConfig();
}
