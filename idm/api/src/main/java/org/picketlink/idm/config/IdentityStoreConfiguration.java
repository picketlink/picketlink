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

import java.util.Set;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.spi.IdentityStore;

/**
 * Represents a configuration for {@link IdentityStore}
 *
 * @author Anil Saldhana
 * @author Shane Bryzak
 * @since Sep 6, 2012
 */
public interface IdentityStoreConfiguration {

    /**
     * Initializes the store configuration with the specified FeatureSet
     * 
     * @throws SecurityConfigurationException
     */
    void init() throws SecurityConfigurationException;

    /**
     * Sets the realm for this identity store
     * 
     * @param realm
     */
    void addRealm(String realm);

    /**
     * 
     * @return
     */
    Set<String> getRealms();

    /**
     * Returns a FeatureSet describing the features supported by this identity store configuration
     * 
     * @return
     */
    FeatureSet getFeatureSet();

}