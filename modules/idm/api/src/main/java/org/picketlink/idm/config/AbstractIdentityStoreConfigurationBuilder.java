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
import org.picketlink.idm.model.Grant;
import org.picketlink.idm.model.GroupMembership;
import org.picketlink.idm.model.GroupRole;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.ContextInitializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * @author Pedro Igor
 *
 */
public abstract class AbstractIdentityStoreConfigurationBuilder<T extends IdentityStoreConfiguration, S extends IdentityStoreConfigurationBuilder<T, S>>
        extends AbstractIdentityConfigurationChildBuilder implements IdentityStoreConfigurationBuilder<T, S> {

    private final Map<FeatureGroup, Set<FeatureOperation>> supportedFeatures = new HashMap<FeatureGroup, Set<FeatureOperation>>();
    private final Map<Class<? extends Relationship>, Set<FeatureOperation>> supportedRelationships = new HashMap<Class<? extends Relationship>, Set<FeatureOperation>>();
    private Set<String> realms = new HashSet<String>();
    private Set<String> tiers = new HashSet<String>();
    private List<ContextInitializer> contextInitializers = new ArrayList<ContextInitializer>();
    private Map<String, Object> credentialHandlerProperties = new HashMap<String,Object>();
    private List<Class<? extends CredentialHandler>> credentialHandlers = new ArrayList<Class<? extends CredentialHandler>>();
    private IdentityStoresConfigurationBuilder identityStoresConfigurationBuilder;

    protected AbstractIdentityStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
        super(builder);
        this.identityStoresConfigurationBuilder = builder;
    }

    public FileStoreConfigurationBuilder file() {
        return this.identityStoresConfigurationBuilder.file();
    }

    public JPAStoreConfigurationBuilder jpa() {
        return this.identityStoresConfigurationBuilder.jpa();
    }

    public LDAPStoreConfigurationBuilder ldap() {
        return this.identityStoresConfigurationBuilder.ldap();
    }

    @Override
    public S supportFeature(FeatureGroup... groups) {
        FeatureGroup[] features = (groups != null && groups.length > 0) ? groups : FeatureGroup.values();

        for (FeatureGroup feature : features) {
            switch (feature) {
                case agent:
                    addBasicOperations(feature);
                    break;
                case user:
                    addBasicOperations(feature);
                    break;
                case group:
                    addBasicOperations(feature);
                    break;
                case role:
                    addBasicOperations(feature);
                    break;
                case relationship:
                    supportRelationshipType(null);
                    addBasicOperations(feature);
                    break;
                case attribute:
                    addBasicOperations(feature);
                    break;
                case realm:
                    addBasicOperations(feature);
                    break;
                case tier:
                    addBasicOperations(feature);
                    break;
                case credential:
                    supportFeature(feature, FeatureOperation.update);
                    supportFeature(feature, FeatureOperation.validate);
                    break;
            }
        }

        return (S) this;
    }

    public S supportFeature(FeatureGroup feature, FeatureOperation operation) {
        getFeatureOperations(feature).add(operation);
        return (S) this;
    }

    @Override
    public S supportRelationshipType(Class<? extends Relationship>... relationshipClass) {
        List<Class<? extends Relationship>> classes;

        if (relationshipClass != null && relationshipClass.length > 0) {
            classes = Arrays.<Class<? extends Relationship>> asList(relationshipClass);
        } else {
            classes = getDefaultRelationshipClasses();
        }

        for (Class<? extends Relationship> cls : classes) {
            getRelationshipOperations(cls).add(FeatureOperation.create);
            getRelationshipOperations(cls).add(FeatureOperation.read);
            getRelationshipOperations(cls).add(FeatureOperation.update);
            getRelationshipOperations(cls).add(FeatureOperation.delete);
        }

        return (S) this;
    }

    /**
     * <p>
     * Removes the given {@link Relationship} types.
     * </p>
     *
     * @param featureSet
     * @param relationshipClass
     */
    @Override
    public S removeRelationship(Class<? extends Relationship>... relationshipClasses) {
        if (relationshipClasses != null) {
            for (Class<? extends Relationship> relationshipClass : relationshipClasses) {
                removeRelationshipFeature(relationshipClass, FeatureOperation.create);
                removeRelationshipFeature(relationshipClass, FeatureOperation.read);
                removeRelationshipFeature(relationshipClass, FeatureOperation.update);
                removeRelationshipFeature(relationshipClass, FeatureOperation.delete);
            }
        }

        return (S) this;
    }

    /**
     * <p>
     * Removes the given {@link FeatureOperation} related to the provided {@link Relationship} type from the feature set.
     * </p>
     *
     * @param feature
     * @param operation
     * @throws SecurityConfigurationException If this instance is locked and changes are no more allowed.
     */
    @Override
    public S removeRelationshipFeature(Class<? extends Relationship> relationshipClass, FeatureOperation operation) {
        if (this.supportedRelationships.containsKey(relationshipClass)) {
            this.supportedRelationships.get(relationshipClass).remove(operation);
        }

        return (S) this;
    }

    @Override
    public S supportAllFeatures() {
        supportFeature(FeatureGroup.agent);
        supportFeature(FeatureGroup.attribute);
        supportFeature(FeatureGroup.credential);
        supportFeature(FeatureGroup.group);
        supportFeature(FeatureGroup.realm);
        supportFeature(FeatureGroup.relationship);
        supportFeature(FeatureGroup.role);
        supportFeature(FeatureGroup.tier);
        supportFeature(FeatureGroup.user);

        return (S) this;
    }

    @Override
    public S addRealm(String... realmNames) {
        if (realmNames != null) {
            this.realms.addAll(Arrays.asList(realmNames));
        }

        return (S) this;
    }

    @Override
    public S addTier(String... tierNames) {
        if (tierNames != null) {
            this.tiers.addAll(Arrays.asList(tierNames));
        }

        return (S) this;
    }

    @Override
    public S addContextInitializer(ContextInitializer contextInitializer) {
        this.contextInitializers.add(contextInitializer);
        return (S) this;
    }

    @Override
    public S setCredentialHandlerProperty(String propertyName, Object value) {
        this.credentialHandlerProperties.put(propertyName, value);
        return (S) this;
    }

    @Override
    public S addCredentialHandler(Class<? extends CredentialHandler> credentialHandler) {
        this.credentialHandlers.add(credentialHandler);
        return (S) this;
    }

    /**
     * <p>
     * Removes the given {@link FeatureGroup} and all supported {@link FeatureOperation} from the features set.
     * </p>
     *
     * @param feature
     * @throws SecurityConfigurationException If this instance is locked and changes are no more allowed.
     */
    @Override
    public S removeFeature(FeatureGroup feature) throws SecurityConfigurationException {
        this.supportedFeatures.remove(feature);

        if (FeatureGroup.relationship.equals(feature)) {
            this.supportedRelationships.clear();
        }

        return (S) this;
    }

    /**
     * <p>
     * Removes the given {@link FeatureOperation} for the provided {@link FeatureGroup} from the features set.
     * </p>
     *
     * @param feature
     * @param operation
     * @throws SecurityConfigurationException If this instance is locked and changes are no more allowed.
     */
    @Override
    public S removeFeature(FeatureGroup feature, FeatureOperation operation) {
        getFeatureOperations(feature).remove(operation);

        if (FeatureGroup.relationship.equals(feature)) {
            Set<Entry<Class<? extends Relationship>, Set<FeatureOperation>>> relationShipsEntrySet = this.supportedRelationships
                    .entrySet();

            for (Entry<Class<? extends Relationship>, Set<FeatureOperation>> entry : relationShipsEntrySet) {
                getRelationshipOperations(entry.getKey()).remove(operation);
            }
        }

        return (S) this;
    }

    @Override
    public void validate() {
        if (this.supportedFeatures.isEmpty()) {
            throw new SecurityConfigurationException(
                    "You must provide which features should be supported by the identity store.");
        }
    }

    @Override
    public Builder<?> readFrom(T configuration) {
        if (configuration == null) {
            throw MESSAGES.nullArgument("Configuration to read.");
        }

        this.realms.addAll(configuration.getRealms());
        this.tiers.addAll(configuration.getTiers());
        this.supportedFeatures.putAll(configuration.getSupportedFeatures());
        this.supportedRelationships.putAll(configuration.getSupportedRelationships());

        return this;
    }

    protected Map<FeatureGroup, Set<FeatureOperation>> getSupportedFeatures() {
        return this.supportedFeatures;
    }

    protected Map<Class<? extends Relationship>, Set<FeatureOperation>> getSupportedRelationships() {
        return this.supportedRelationships;
    }

    protected Set<String> getRealms() {
        return this.realms;
    }

    protected Set<String> getTiers() {
        return this.tiers;
    }

    protected List<ContextInitializer> getContextInitializers() {
        return this.contextInitializers;
    }

    protected Map<String, Object> getCredentialHandlerProperties() {
        return this.credentialHandlerProperties;
    }

    protected List<Class<? extends CredentialHandler>> getCredentialHandlers() {
        return this.credentialHandlers;
    }

    private Set<FeatureOperation> getFeatureOperations(FeatureGroup group) {
        if (!this.supportedFeatures.containsKey(group)) {
            this.supportedFeatures.put(group, new HashSet<FeatureOperation>());
        }
        return this.supportedFeatures.get(group);
    }

    private Set<FeatureOperation> getRelationshipOperations(Class<? extends Relationship> relationshipClass) {
        if (!this.supportedRelationships.containsKey(relationshipClass)) {
            this.supportedRelationships.put(relationshipClass, new HashSet<FeatureOperation>());
        }
        return this.supportedRelationships.get(relationshipClass);
    }

    private void addBasicOperations(FeatureGroup feature) {
        supportFeature(feature, FeatureOperation.create);
        supportFeature(feature, FeatureOperation.read);
        supportFeature(feature, FeatureOperation.update);
        supportFeature(feature, FeatureOperation.delete);
    }

    protected static List<Class<? extends Relationship>> getDefaultRelationshipClasses() {
        List<Class<? extends Relationship>> classes = new ArrayList<Class<? extends Relationship>>();

        classes.add(Relationship.class);
        classes.add(Grant.class);
        classes.add(GroupMembership.class);
        classes.add(GroupRole.class);

        return Collections.unmodifiableList(classes);
    }

}