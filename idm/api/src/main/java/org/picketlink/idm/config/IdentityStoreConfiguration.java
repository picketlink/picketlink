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

import java.util.HashSet;
import java.util.Set;

import org.picketlink.idm.credential.spi.CredentialHandler;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.IdentityStore;

/**
 * Represents a configuration for {@link IdentityStore}
 *
 * @author Anil Saldhana
 * @author Shane Bryzak
 * @since Sep 6, 2012
 */
public abstract class IdentityStoreConfiguration extends BaseAbstractStoreConfiguration implements StoreConfiguration {

    /**
     * This enum defines the individual features that an IdentityStore configuration may support
     */
    public enum Feature {
        /**
         * 
         */
        createUser,
        /**
         * 
         */
        readUser,
        /**
         * 
         */
        updateUser,
        /**
         * 
         */
        deleteUser, 
        createGroup, 
        readGroup, 
        updateGroup, 
        deleteGroup,
        createRole, 
        readRole, 
        updateRole, 
        deleteRole,
        createRelationship, 
        readRelationship, 
        updateRelationship, 
        deleteRelationship,
        readAttribute, 
        updateAttribute, 
        deleteAttribute,
        manageCredentials,
        supportsTiers, 
        supportsRealms, 
        disableRole, 
        disableGroup, 
        disableUser,
        createAgent,
        updateAgent, 
        deleteAgent, 
        readAgent,
        managePartitions,
        all;
    }

    public class FeatureSet {

        /**
         * Metadata reflecting which features are supported by this identity store
         */
        private final Set<Feature> supportedFeatures = new HashSet<Feature>();
        private final Set<Class<? extends Relationship>> supportedRelationships = new HashSet<Class<? extends Relationship>>();

        public void addSupportedFeature(Feature feature) {
            supportedFeatures.add(feature);
        }

        public boolean supports(Feature feature) {
            return supportedFeatures.contains(feature);
        }

        public void addSupportedRelationship(Class<? extends Relationship> relationshipClass) {
            supportedRelationships.add(relationshipClass);
        }

        public boolean supportsRelationship(Class<? extends Relationship> relationshipClass) {
            for (Class<? extends Relationship> cls : supportedRelationships) {
                if (cls.isAssignableFrom(relationshipClass)) {
                    return true;
                }
            }
            return false;
        }
    }

    private FeatureSet featureSet;

    /**
     * Defines the realm supported by this identity store.  If no realm is specified, then
     * this identity store will be used during all supported operations where the selected
     * realm is not explicitly served by a different identity store.
     */
    private String realm;


    /**
     * Metadata reflecting which {@link CredentialHandler} are supported by this identity store.
     */
    private final Set<Class<? extends CredentialHandler>> supportedCredentialHandlers = new HashSet<Class<? extends CredentialHandler>>();

    /**
     * Returns the realm for this identity store
     * 
     * @return
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Sets the realm for this identity store
     * 
     * @param realm
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    /**
     * Returns a Set describing the features supported by this identity store
     * 
     * @return
     */
    public FeatureSet getFeatureSet() {
        return featureSet;
    }
    
    /**
     * Returns a {@link Set} describing the {@link CredentialHandler} types supported by this identity store
     * 
     * @return
     */
    public Set<Class<? extends CredentialHandler>> getSupportedCredentialHandlers() {
        return supportedCredentialHandlers;
    }

}