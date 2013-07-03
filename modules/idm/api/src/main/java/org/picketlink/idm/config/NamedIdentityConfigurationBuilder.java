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
import java.util.List;

/**
 * @author pedroigor
 */
public class NamedIdentityConfigurationBuilder extends AbstractIdentityConfigurationChildBuilder<IdentityConfiguration> implements Builder<IdentityConfiguration> {

    private final IdentityStoresConfigurationBuilder identityStoresConfigurationBuilder;
    private final List<NamedIdentityConfigurationBuilder> identityConfigurationBuilders;
    private String name = "default";

    protected NamedIdentityConfigurationBuilder(String name, IdentityConfigurationChildBuilder builder) {
        super(builder);
        this.identityConfigurationBuilders = new ArrayList<NamedIdentityConfigurationBuilder>();
        this.identityStoresConfigurationBuilder = new IdentityStoresConfigurationBuilder(this);

        if (name != null) {
            this.name = name;
        }

    }

    public IdentityStoresConfigurationBuilder stores() {
        return this.identityStoresConfigurationBuilder;
    }

    @Override
    protected IdentityConfiguration create() {
        return new IdentityConfiguration(this.name, this.identityStoresConfigurationBuilder.create());
    }

    @Override
    protected void validate() {
        if (this.name == null) {
            throw new SecurityConfigurationException("All configuration must have a name.");
        }

        this.identityStoresConfigurationBuilder.validate();
    }

    @Override
    public Builder<IdentityConfiguration> readFrom(IdentityConfiguration configuration) {
        return null;  //TODO: Implement readFrom
    }
}
