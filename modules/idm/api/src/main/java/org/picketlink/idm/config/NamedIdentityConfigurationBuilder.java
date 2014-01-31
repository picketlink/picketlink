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

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;

import java.util.HashSet;
import java.util.Set;

import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.RelationshipPolicy;

/**
 * <p>A class used to build {@link IdentityConfiguration} instances.</p>
 *
 * @author pedroigor
 */
public class NamedIdentityConfigurationBuilder extends AbstractIdentityConfigurationChildBuilder<IdentityConfiguration> {

    private final IdentityStoresConfigurationBuilder identityStoresConfigurationBuilder;
    private final String name;

    private Set<Class<? extends IdentityType>> registeredIdentityTypes = new HashSet<Class<? extends IdentityType>>();

    private Set<Class<? extends Relationship>> registeredRelationshipTypes = new HashSet<Class<? extends Relationship>>();

    protected NamedIdentityConfigurationBuilder(String name, IdentityConfigurationBuilder builder) {
        super(builder);

        if (isNullOrEmpty(name)) {
            throw MESSAGES.nullArgument("Configuration name");
        }

        this.identityStoresConfigurationBuilder = new IdentityStoresConfigurationBuilder(this);
        this.name = name;
    }

    public void registerIdentityType(Class<? extends IdentityType> identityType) {
        registeredIdentityTypes.add(identityType);
    }

    public void registerRelationshipType(Class<? extends Relationship> relationshipType) {
        registeredRelationshipTypes.add(relationshipType);
    }

    /**
     * <p>This method should be used to provide all the necessary configuration for the identity stores supported by
     * this configuration.</p>
     *
     * @return
     */
    public IdentityStoresConfigurationBuilder stores() {
        return this.identityStoresConfigurationBuilder;
    }

    @Override
    protected IdentityConfiguration create() {
        return new IdentityConfiguration(this.name,
                this.identityStoresConfigurationBuilder.create(),
                new RelationshipPolicy(this.identityStoresConfigurationBuilder.getSelfRelationships(),
                        this.identityStoresConfigurationBuilder.getGlobalRelationships()),
                registeredIdentityTypes, registeredRelationshipTypes);
    }

    @Override
    protected void validate() {
        this.identityStoresConfigurationBuilder.validate();
    }

    @Override
    protected Builder<IdentityConfiguration> readFrom(IdentityConfiguration configuration) {
        this.identityStoresConfigurationBuilder.readFrom(configuration.getStoreConfiguration());
        return this;
    }

    protected String getName() {
        return this.name;
    }
}
