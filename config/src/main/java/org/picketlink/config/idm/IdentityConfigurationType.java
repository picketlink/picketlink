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

package org.picketlink.config.idm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Type representing identity configuration
 *
 * TODO: Move this class to config module. For now it needs to be in federation because needs to be accessible from PicketlinkType class
 * TODO: Add XML config snippet similarly like for other type classes
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IdentityConfigurationType {

    private List<StoreConfigurationType> identityStoreConfigurations = new ArrayList<StoreConfigurationType>();
    private StoreConfigurationType partitionStoreConfiguration;

    public List<StoreConfigurationType> getIdentityStoreConfigurations() {
        return Collections.unmodifiableList(identityStoreConfigurations);
    }

    public void addIdentityStoreConfiguration(StoreConfigurationType storeConfigurationType) {
        identityStoreConfigurations.add(storeConfigurationType);
    }

    public StoreConfigurationType getPartitionStoreConfiguration() {
        return partitionStoreConfiguration;
    }

    public void setPartitionStoreConfiguration(StoreConfigurationType partitionStoreConfiguration) {
        this.partitionStoreConfiguration = partitionStoreConfiguration;
    }
}
