/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.picketlink.idm.config.internal;

import java.beans.Introspector;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.picketlink.config.idm.parsers.IDMConfigParser;
import org.picketlink.identity.federation.core.config.PicketLinkType;
import org.picketlink.identity.federation.core.config.idm.IDMType;
import org.picketlink.identity.federation.core.config.idm.IdentityConfigurationType;
import org.picketlink.identity.federation.core.config.idm.IdentityStoreInvocationContextFactoryType;
import org.picketlink.identity.federation.core.config.idm.StoreConfigurationType;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.parsers.config.PicketLinkConfigParser;
import org.picketlink.idm.IdGenerator;
import org.picketlink.idm.IdentityCache;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.SecurityConfigurationException;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.StoreConfiguration;
import org.picketlink.idm.credential.spi.CredentialHandlerFactory;
import org.picketlink.idm.event.EventBridge;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.internal.util.properties.Property;
import org.picketlink.idm.internal.util.properties.query.NamedPropertyCriteria;
import org.picketlink.idm.internal.util.properties.query.PropertyQueries;
import org.picketlink.idm.internal.util.properties.query.PropertyQuery;
import org.picketlink.idm.internal.util.reflection.Reflections;
import org.picketlink.idm.spi.IdentityStoreInvocationContextFactory;
import org.picketlink.idm.spi.PartitionStore;
import org.picketlink.idm.spi.StoreFactory;

/**
 * Creating IDM runtime from parsed XML configuration
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class XMLBasedIdentityManagerProvider {

    private static final String DEFAULT_IDENTITY_MANAGER_CLASS = "org.picketlink.idm.internal.DefaultIdentityManager";

    private static final String DEFAULT_IDENTITY_STORE_INVOCATION_CONTEXT_FACTORY_CLASS = "org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory";
    private static final String DEFAULT_IDENTITY_CACHE_CLASS = "org.picketlink.idm.DefaultIdentityCache";
    private static final String DEFAULT_ID_GENERATOR_CLASS = "org.picketlink.idm.internal.DefaultIdGenerator";
    private static final String DEFAULT_CREDENTIAL_HANDLER_FACTORY_CLASS = "org.picketlink.idm.credential.internal.DefaultCredentialHandlerFactory";

    private static final ClassLoader IDM_CLASSLOADER = IdentityManager.class.getClassLoader();

    public IdentityManager buildIdentityManager(InputStream inputStream) {
        try {
            // TODO: Think about subclassing AbstractSAMLConfigurationProvider (if it's going to be decoupled from federation module)
            PicketLinkConfigParser parser = new PicketLinkConfigParser();
            PicketLinkType plType = (PicketLinkType)parser.parse(inputStream);
            IDMType idmConfiguration = plType.getIdmType();
            return buildIdentityManager(idmConfiguration);
        } catch (ParsingException pe) {
            throw new SecurityConfigurationException("Could not parse picketlink configuration", pe);
        }
    }

    protected IdentityManager buildIdentityManager(IDMType idmType) {
        // TODO: implement
        String identityManagerClass = idmType.getIdentityManagerClass() != null ? idmType.getIdentityManagerClass() : DEFAULT_IDENTITY_MANAGER_CLASS;
        IdentityManager identityManager = (IdentityManager)instantiateComponent(identityManagerClass);

        if (idmType.getStoreFactoryClass() != null) {
            StoreFactory storeFactory = (StoreFactory)instantiateComponent(idmType.getStoreFactoryClass());
            identityManager.setIdentityStoreFactory(storeFactory);
        }

        IdentityStoreInvocationContextFactory invContextFactory = buildIdentityStoreInvocationContextFactory(idmType.getIdentityStoreInvocationContextFactory());
        IdentityConfiguration identityConfiguration = buildIdentityConfiguration(idmType.getIdentityConfigurationType());

        // Bootstrap identity manager
        identityManager.bootstrap(identityConfiguration, invContextFactory);

        return identityManager;
    }

    protected IdentityStoreInvocationContextFactory buildIdentityStoreInvocationContextFactory(
            IdentityStoreInvocationContextFactoryType factoryType) {
        // Default identityStoreInvocationContextFactory if not provided from configuration
        if (factoryType == null) {
            return DefaultIdentityStoreInvocationContextFactory.DEFAULT;
        }

        // Use null as default value for EntityManagerFactory
        EntityManagerFactory emf = null;
        if (factoryType.getEntityManagerFactoryClass() != null) {
            emf = (EntityManagerFactory)instantiateComponent(factoryType.getEntityManagerFactoryClass());
        }

        // Use null as default value for EventBridge (it's handled in DefaultIdentityStoreInvocationContextFactory to be non null)
        EventBridge eventBridge = null;
        if (factoryType.getEventBridgeClass() != null) {
            eventBridge = (EventBridge)instantiateComponent(factoryType.getEventBridgeClass());
        }

        // Use default values for rest of the components
        String identityCacheClass = factoryType.getIdentityCacheClass()!=null ? factoryType.getIdentityCacheClass() : DEFAULT_IDENTITY_CACHE_CLASS;
        IdentityCache identityCache = (IdentityCache)instantiateComponent(identityCacheClass);

        String idGeneratorClass = factoryType.getIdGeneratorClass()!=null ? factoryType.getIdGeneratorClass() : DEFAULT_ID_GENERATOR_CLASS;
        IdGenerator idGenerator = (IdGenerator)instantiateComponent(idGeneratorClass);

        String credentialHandlerFactoryClass = factoryType.getCredentialHandlerFactoryClass()!=null ? factoryType.getCredentialHandlerFactoryClass()
                : DEFAULT_CREDENTIAL_HANDLER_FACTORY_CLASS;
        CredentialHandlerFactory credHandlerFactory = (CredentialHandlerFactory)instantiateComponent(credentialHandlerFactoryClass);

        try {
            String identityStoreInvocationContextFactoryClassName = factoryType.getClassName()!=null ? factoryType.getClassName() : DEFAULT_IDENTITY_STORE_INVOCATION_CONTEXT_FACTORY_CLASS;
            Class<?> identityStoreInvocationContextFactoryClass = Reflections.classForName(identityStoreInvocationContextFactoryClassName, IDM_CLASSLOADER);
            Constructor<?> invContextFactoryConstructor = Reflections.findDeclaredConstructor(identityStoreInvocationContextFactoryClass,
                    EntityManagerFactory.class, CredentialHandlerFactory.class, IdentityCache.class, EventBridge.class, IdGenerator.class);
            return (IdentityStoreInvocationContextFactory)invContextFactoryConstructor.newInstance(emf, credHandlerFactory, identityCache, eventBridge, idGenerator);
        } catch (Exception e) {
            throw new SecurityConfigurationException("Exception during creation of identityStoreInvocationContextFactory", e);
        }

    }

    protected IdentityConfiguration buildIdentityConfiguration(IdentityConfigurationType identityConfigurationType) {
        IdentityConfiguration identityConfig = new IdentityConfiguration();

        for (StoreConfigurationType storeConfigType : identityConfigurationType.getIdentityStoreConfigurations()) {
            identityConfig.addStoreConfiguration(buildStoreConfiguration(storeConfigType));
        }

        if (identityConfigurationType.getPartitionStoreConfiguration() != null) {
            StoreConfiguration partitionStoreConfig = buildStoreConfiguration(identityConfigurationType.getPartitionStoreConfiguration());
            identityConfig.addStoreConfiguration(partitionStoreConfig);
        }

        return identityConfig;
    }

    protected StoreConfiguration buildStoreConfiguration(StoreConfigurationType storeConfigType) {
        String className = storeConfigType.getClassName();

        if (className == null) {
            throw new SecurityConfigurationException("Classname of all storeConfigurationTypes must be provided!");
        }

        StoreConfiguration storeConfig = (StoreConfiguration)instantiateComponent(className);
        Class<?> storeConfigClass = storeConfig.getClass();

        Map<String, Object> props = storeConfigType.getAllProperties();
        for (String propertyName : props.keySet()) {
            // It's not optimal as we need to query and iterate methods separately for each property.
            // But performance shouldn't be big deal during reading configuration as it usually needs to be read only once...
            PropertyQuery<Object> propertyQuery = PropertyQueries.createQuery(storeConfigClass);
            propertyQuery.addCriteria(new NamedPropertyCriteria(propertyName));
            Property<Object> property = propertyQuery.getWritableSingleResult();

            property.setValue(storeConfig, props.get(propertyName));
        }

        return storeConfig;
    }

    private Object instantiateComponent(String className) {
        try {
            Class<?> clazz = Reflections.classForName(className, IDM_CLASSLOADER);
            return clazz.newInstance();
        } catch (Exception e) {
            throw new SecurityConfigurationException("Exception during creation of component " + className, e);
        }
    }
}
