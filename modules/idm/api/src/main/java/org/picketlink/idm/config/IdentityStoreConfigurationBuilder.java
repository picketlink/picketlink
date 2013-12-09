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

import org.picketlink.idm.credential.handler.CredentialHandler;
import org.picketlink.idm.model.AttributedType;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.Partition;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.spi.ContextInitializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static org.picketlink.idm.IDMMessages.MESSAGES;
import static org.picketlink.idm.config.IdentityStoreConfiguration.IdentityOperation;

/**
 * <p>Base class for {@link IdentityStoreConfigurationBuilder} implementations.</p>
 *
 * @author Pedro Igor
 */
public abstract class IdentityStoreConfigurationBuilder<T extends IdentityStoreConfiguration, S extends IdentityStoreConfigurationBuilder<T, S>>
        extends AbstractIdentityConfigurationChildBuilder<T>
        implements IdentityStoreConfigurationChildBuilder {

    private final Map<Class<? extends AttributedType>, Set<IdentityOperation>> supportedTypes;
    private final Map<Class<? extends AttributedType>, Set<IdentityOperation>> unsupportedTypes;
    private final Set<Class<? extends Relationship>> globalRelationshipTypes;
    private final Set<Class<? extends Relationship>> selfRelationshipTypes;
    private final Set<Class<? extends CredentialHandler>> credentialHandlers;
    private final Map<String, Object> credentialHandlerProperties;
    private final List<ContextInitializer> contextInitializers;
    private final IdentityStoresConfigurationBuilder identityStoresConfigurationBuilder;
    private boolean supportCredentials;
    private boolean supportAttributes;
    private boolean supportPermissions;

    protected IdentityStoreConfigurationBuilder(IdentityStoresConfigurationBuilder builder) {
        super(builder);
        this.supportedTypes = new HashMap<Class<? extends AttributedType>, Set<IdentityOperation>>();
        this.unsupportedTypes = new HashMap<Class<? extends AttributedType>, Set<IdentityOperation>>();
        this.globalRelationshipTypes = new HashSet<Class<? extends Relationship>>();
        this.selfRelationshipTypes = new HashSet<Class<? extends Relationship>>();
        this.credentialHandlers = new HashSet<Class<? extends CredentialHandler>>();
        this.credentialHandlerProperties = new HashMap<String, Object>();
        this.contextInitializers = new ArrayList<ContextInitializer>();
        this.identityStoresConfigurationBuilder = builder;
    }

    @Override
    public FileStoreConfigurationBuilder file() {
        return this.identityStoresConfigurationBuilder.file();
    }

    @Override
    public JPAStoreConfigurationBuilder jpa() {
        return this.identityStoresConfigurationBuilder.jpa();
    }

    @Override
    public LDAPStoreConfigurationBuilder ldap() {
        return this.identityStoresConfigurationBuilder.ldap();
    }

    /**
     * <p>Defines which types should be supported by this configuration.</p>
     *
     * @param types
     * @return
     */
    public S supportType(Class<? extends AttributedType>... attributedTypes) {
        if (attributedTypes == null) {
            throw MESSAGES.nullArgument("Attributed Types");
        }

        for (Class<? extends AttributedType> attributedType : attributedTypes) {
            if (!this.supportedTypes.containsKey(attributedType)) {
                List<IdentityOperation> defaultTypeOperations = Arrays.asList(IdentityOperation.values());
                HashSet<IdentityOperation> supportedOperations =
                        new HashSet<IdentityOperation>(defaultTypeOperations);
                this.supportedTypes.put(attributedType, supportedOperations);
            }
        }

        return (S) this;
    }

    /**
     * <p>Defines which type should not be supported by this configuration.</p> <p>If the operation was not provided,
     * the type should be completely removed from the supported types. Otherwise, only the provided operations should
     * not be supported.</p>
     *
     * @param type
     * @param operation
     * @return
     */
    public S unsupportType(Class<? extends AttributedType> type, IdentityOperation... operations) {
        if (!this.unsupportedTypes.containsKey(type)) {
            this.unsupportedTypes.put(type, new HashSet<IdentityOperation>());
        }

        if (operations != null && operations.length == 0) {
            operations = IdentityOperation.values();
        }

        for (IdentityOperation op : operations) {
            this.unsupportedTypes.get(type).add(op);
        }

        return (S) this;
    }

    /**
     * <p>Defines which types should be supported by this configuration.</p>
     *
     * @param types
     * @return
     */
    public S supportGlobalRelationship(Class<? extends Relationship>... types) {
        this.globalRelationshipTypes.addAll(Arrays.asList(types));
        supportType(types);
        return (S) this;
    }

    /**
     * <p>Defines which types should be supported by this configuration.</p>
     *
     * @param types
     * @return
     */
    public S supportSelfRelationship(Class<? extends Relationship>... types) {
        this.selfRelationshipTypes.addAll(Arrays.asList(types));
        supportType(types);
        return (S) this;
    }

    /**
     * <p>Enables the default feature set for this configuration.</p>
     *
     * @return
     */
    public S supportAllFeatures() {
        supportType(getDefaultIdentityModelClasses());
        supportCredentials(true);
        supportGlobalRelationship(Relationship.class);
        supportAttributes(true);
        supportPermissions(true);

        return (S) this;
    }

    /**
     * <p>Adds a {@link ContextInitializer}.</p>
     *
     * @param contextInitializer
     * @return
     */
    public S addContextInitializer(ContextInitializer contextInitializer) {
        this.contextInitializers.add(contextInitializer);
        return (S) this;
    }

    /**
     * <p>Sets a configuration property for a {@link CredentialHandler}.</p>
     *
     * @param propertyName
     * @param value
     * @return
     */
    public S setCredentialHandlerProperty(String propertyName, Object value) {
        this.credentialHandlerProperties.put(propertyName, value);
        return (S) this;
    }

    /**
     * <p>Adds a custom {@CredentialHandler}.</p>
     *
     * @param credentialHandler
     * @return
     */
    public S addCredentialHandler(Class<? extends CredentialHandler> credentialHandler) {
        this.credentialHandlers.add(credentialHandler);
        return (S) this;
    }

    /**
     * <p>Enable/Disable credentials support</p>
     *
     * @param supportCredentials
     * @return
     */
    public S supportCredentials(boolean supportCredentials) {
        this.supportCredentials = supportCredentials;
        return (S) this;
    }

    /**
     * <p>Enable/Disable permissions support</p>
     *
     * @param supportPermissions
     * @return
     */
    public S supportPermissions(boolean supportPermissions) {
        this.supportPermissions = supportPermissions;
        return (S) this;
    }

    /**
     * <p>Enable/Disable attribute support</p>
     *
     * @param supportAttributes
     * @return
     */
    public S supportAttributes(boolean supportAttributes) {
        this.supportAttributes = supportAttributes;
        return (S) this;
    }

    @Override
    protected void validate() {
        if (this.supportedTypes.isEmpty()) {
            throw new SecurityConfigurationException("The store configuration must have at least one supported type.");
        }
    }

    @Override
    protected Builder<T> readFrom(T configuration) {
        for (Class<? extends CredentialHandler> credentialHandler : configuration.getCredentialHandlers()) {
            addCredentialHandler(credentialHandler);
        }

        for (String credentialProperty : configuration.getCredentialHandlerProperties().keySet()) {
            Object value = configuration.getCredentialHandlerProperties().get(credentialProperty);
            setCredentialHandlerProperty(credentialProperty, value);
        }

        for (Class<? extends AttributedType> supportedType : configuration.getSupportedTypes().keySet()) {
            supportType(supportedType);

            if (Relationship.class.isAssignableFrom(supportedType)) {
                supportGlobalRelationship((Class<? extends Relationship>) supportedType);
            }
        }

        for (Class<? extends AttributedType> unsupportedType : configuration.getUnsupportedTypes().keySet()) {
            unsupportType(unsupportedType);
        }

        for (ContextInitializer contextInitializer : configuration.getContextInitializers()) {
            addContextInitializer(contextInitializer);
        }

        supportAttributes(configuration.supportsAttribute());
        supportCredentials(configuration.supportsCredential());
        supportPermissions(configuration.supportsPermissions());

        return this;
    }

    protected List<ContextInitializer> getContextInitializers() {
        return unmodifiableList(this.contextInitializers);
    }

    protected Map<String, Object> getCredentialHandlerProperties() {
        return unmodifiableMap(this.credentialHandlerProperties);
    }

    protected Set<Class<? extends CredentialHandler>> getCredentialHandlers() {
        return unmodifiableSet(this.credentialHandlers);
    }

    protected Map<Class<? extends AttributedType>, Set<IdentityOperation>> getSupportedTypes() {
        return unmodifiableMap(this.supportedTypes);
    }

    protected Map<Class<? extends AttributedType>, Set<IdentityOperation>> getUnsupportedTypes() {
        return unmodifiableMap(this.unsupportedTypes);
    }

    protected Set<Class<? extends Relationship>> getGlobalRelationshipTypes() {
        return this.globalRelationshipTypes;
    }

    protected Set<Class<? extends Relationship>> getSelfRelationshipTypes() {
        return this.selfRelationshipTypes;
    }

    protected boolean isSupportAttributes() {
        return this.supportAttributes;
    }

    protected boolean isSupportCredentials() {
        return this.supportCredentials;
    }

    protected boolean isSupportPermissions() {
        return this.supportPermissions;
    }

    private static Class<? extends AttributedType>[] getDefaultIdentityModelClasses() {
        List<Class<? extends AttributedType>> classes = new ArrayList<Class<? extends AttributedType>>();

        // identity types
        classes.add(IdentityType.class);

        // relationship types
        classes.add(Relationship.class);

        // partition types
        classes.add(Partition.class);

        return (Class<? extends AttributedType>[]) classes.toArray(new Class<?>[classes.size()]);
    }

    @Override
    public <U extends IdentityStoreConfigurationBuilder<?, ?>> U add(
            Class<? extends IdentityStoreConfiguration> identityStoreConfiguration,
            Class<U> builder) {
        return this.identityStoresConfigurationBuilder.add(
                identityStoreConfiguration, builder);
    }
}