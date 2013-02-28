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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;


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
        return Collections.unmodifiableSet(realms);
    }

    @Override
    public final void init() throws SecurityConfigurationException {
        initConfig();
        this.featureSet.lock();
    }

    public abstract void initConfig();
}
