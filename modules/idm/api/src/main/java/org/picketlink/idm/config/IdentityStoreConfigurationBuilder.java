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

package org.picketlink.idm.config;

import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.FeatureSet.FeatureOperation;
import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.ContextInitializer;
import org.picketlink.idm.spi.IdentityContextHandler;

/**
 * @author Pedro Igor
 *
 */
public interface IdentityStoreConfigurationBuilder<T extends IdentityStoreConfiguration, S extends IdentityStoreConfigurationBuilder<T, S>> extends Builder<T> {

    S addTier(String... tierNames);

    S addRealm(String... realmNames);

    S supportAllFeatures();

    S supportRelationshipType(Class<? extends Relationship>... types);

    S supportFeature(FeatureGroup... feature);

    S addCredentialHandler(Class<? extends CredentialHandler> credentialHandler);

    S setCredentialHandlerProperty(String propertyName, Object value);

    S addContextInitializer(ContextInitializer contextInitializer);

    S setIdentityContextHandler(IdentityContextHandler handler);

    S removeFeature(FeatureGroup feature, FeatureOperation operation);

    S removeFeature(FeatureGroup feature);

    S removeRelationshipFeature(Class<? extends Relationship> relationshipClass, FeatureOperation operation);

    S removeRelationship(Class<? extends Relationship>... relationshipClasses);

}
