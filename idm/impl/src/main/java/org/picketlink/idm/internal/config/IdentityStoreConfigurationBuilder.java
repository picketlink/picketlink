/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.picketlink.idm.internal.config;

import org.picketlink.idm.config.FeatureSet;
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.model.Relationship;

/**
 * @author Pedro Silva
 *
 */
public class IdentityStoreConfigurationBuilder<B extends IdentityStoreConfigurationBuilder<?, ?>, C extends IdentityStoreConfiguration> extends ConfigurationBuilder<B> {

    private C configuration;

    protected IdentityStoreConfigurationBuilder(C configuration, ConfigurationBuilder<?> builder) {
        super(builder);
        this.configuration = configuration;
    }

    public B addRealm(String realm) {
        getConfiguration().addRealm(realm);
        return (B) this;
    }

    public B addTier(String tier) {
        getConfiguration().addTier(tier);
        return (B) this;
    }

    public B supportFeature(FeatureGroup... feature) {
        FeatureSet.addFeatureSupport(getConfiguration().getFeatureSet(), feature);
        return (B) this;
    }

    public B supportRelationshipType(Class<? extends Relationship> type) {
        FeatureSet.addRelationshipSupport(getConfiguration().getFeatureSet(), type);
        return (B) this;
    }

    protected C getConfiguration() {
        return this.configuration;
    }

    public B supportAllFeatures() {
        FeatureSet.addFeatureSupport(getConfiguration().getFeatureSet());
        return (B) this;
    }

}
