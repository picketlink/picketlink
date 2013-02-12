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

package org.picketlink.idm.file.internal;

import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.spi.IdentityStore;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Silva</a>
 * 
 */
public class FileIdentityStoreConfiguration extends IdentityStoreConfiguration {

    /**
     * <p>
     * Defines the feature set for this {@link IdentityStore}.
     * </p>
     */
    private FeatureSet featureSet = new FeatureSet();
    
    private FileDataSource dataSource = new FileDataSource();

    @Override
    public void init() throws SecurityConfigurationException {
        configureFeatureSet();
        this.dataSource.init();
    }
    
    /**
     * <p>
     * Configures the {@link Feature} set supported by this store.
     * </p>
     */
    private void configureFeatureSet() {
        this.featureSet.addSupportedFeature(Feature.all);
    }

    @Override
    public FeatureSet getFeatureSet() {
        return this.featureSet;
    }

    public void setDataSource(FileDataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public FileDataSource getDataSource() {
        return this.dataSource;
    }

}
