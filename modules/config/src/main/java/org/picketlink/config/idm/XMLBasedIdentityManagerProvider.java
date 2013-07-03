package org.picketlink.config.idm;

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

import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.NamedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.common.properties.query.PropertyQuery;
import org.picketlink.common.reflection.Reflections;
import org.picketlink.config.PicketLinkConfigParser;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.idm.resolver.PropertyResolverMapper;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityStoresConfiguration;
import org.picketlink.idm.config.SecurityConfigurationException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public static final ClassLoader IDM_CLASSLOADER = IdentityManager.class.getClassLoader();

    public IdentityManager buildIdentityManager(InputStream inputStream) {
        IDMType idmConfiguration = parseIDMType(inputStream);
        return buildIdentityManager(idmConfiguration);
    }

    public IDMType parseIDMType(InputStream inputStream) {
        try {
            // TODO: Think about subclassing AbstractSAMLConfigurationProvider (if it's going to be decoupled from federation
            // module)
            PicketLinkConfigParser parser = new PicketLinkConfigParser();
            PicketLinkType plType = (PicketLinkType) parser.parse(inputStream);
            return plType.getIdmType();
        } catch (ParsingException pe) {
            throw new SecurityConfigurationException("Could not parse picketlink configuration", pe);
        }
    }

    // FIXME: update with latest design
    public IdentityManager buildIdentityManager(IDMType idmType) {
        // String identityManagerClass = idmType.getIdentityManagerClass() != null ? idmType.getIdentityManagerClass() :
        // DEFAULT_IDENTITY_MANAGER_CLASS;
        // IdentityManager identityManager = (IdentityManager)instantiateComponent(identityManagerClass);
        //
        // if (idmType.getStoreFactoryClass() != null) {
        // StoreFactory selector = (StoreFactory)instantiateComponent(idmType.getStoreFactoryClass());
        // identityManager.setIdentityStoreFactory(selector);
        // }
        //
        // IdentityStoreInvocationContextFactory invContextFactory =
        // buildIdentityStoreInvocationContextFactory(idmType.getIdentityStoreInvocationContextFactory());
        // IdentityConfiguration identityConfiguration = buildIdentityConfiguration(idmType.getIdentityConfigurationType());
        //
        // // Bootstrap identity manager
        // identityManager.bootstrap(identityConfiguration, invContextFactory);
        //
        // return identityManager;

        return null;
    }

    // FIXME: update with latest design
    // protected IdentityStoreInvocationContextFactory buildIdentityStoreInvocationContextFactory(
    // IdentityStoreInvocationContextFactoryType factoryType) {
    // // Default identityStoreInvocationContextFactory if not provided from configuration
    // if (factoryType == null) {
    // return DefaultIdentityStoreInvocationContextFactory.DEFAULT;
    // }
    //
    // // Use null as default value for EntityManagerFactory
    // EntityManagerFactory emf = null;
    // if (factoryType.getEntityManagerFactoryClass() != null) {
    // emf = (EntityManagerFactory)instantiateComponent(factoryType.getEntityManagerFactoryClass());
    // }
    //
    // // Use null as default value for EventBridge (it's handled in DefaultIdentityStoreInvocationContextFactory to be non
    // null)
    // EventBridge eventBridge = null;
    // if (factoryType.getEventBridgeClass() != null) {
    // eventBridge = (EventBridge)instantiateComponent(factoryType.getEventBridgeClass());
    // }
    //
    // // Use default values for rest of the components
    // String identityCacheClass = factoryType.getIdentityCacheClass()!=null ? factoryType.getIdentityCacheClass() :
    // DEFAULT_IDENTITY_CACHE_CLASS;
    // IdentityCache identityCache = (IdentityCache)instantiateComponent(identityCacheClass);
    //
    // String idGeneratorClass = factoryType.getIdGeneratorClass()!=null ? factoryType.getIdGeneratorClass() :
    // DEFAULT_ID_GENERATOR_CLASS;
    // IdGenerator idGenerator = (IdGenerator)instantiateComponent(idGeneratorClass);
    //
    // String credentialHandlerFactoryClass = factoryType.getCredentialHandlerFactoryClass()!=null ?
    // factoryType.getCredentialHandlerFactoryClass()
    // : DEFAULT_CREDENTIAL_HANDLER_FACTORY_CLASS;
    // CredentialHandlerFactory credHandlerFactory =
    // (CredentialHandlerFactory)instantiateComponent(credentialHandlerFactoryClass);
    //
    // try {
    // String identityStoreInvocationContextFactoryClassName = factoryType.getClassName()!=null ? factoryType.getClassName() :
    // DEFAULT_IDENTITY_STORE_INVOCATION_CONTEXT_FACTORY_CLASS;
    // Class<?> identityStoreInvocationContextFactoryClass =
    // Reflections.classForName(identityStoreInvocationContextFactoryClassName, IDM_CLASSLOADER);
    // Constructor<?> invContextFactoryConstructor =
    // Reflections.findDeclaredConstructor(identityStoreInvocationContextFactoryClass,
    // EntityManagerFactory.class, CredentialHandlerFactory.class, IdentityCache.class, EventBridge.class, IdGenerator.class);
    // return (IdentityStoreInvocationContextFactory)invContextFactoryConstructor.newInstance(emf, credHandlerFactory,
    // identityCache, eventBridge, idGenerator);
    // } catch (Exception e) {
    // throw new SecurityConfigurationException("Exception during creation of identityStoreInvocationContextFactory", e);
    // }
    //
    // }

    protected IdentityConfiguration buildIdentityConfiguration(IdentityConfigurationType identityConfigurationType) {
        IdentityConfigurationBuilder builder = new IdentityConfigurationBuilder();

        List<IdentityStoreConfiguration> storeConfigs = new ArrayList<IdentityStoreConfiguration>();
        
        for (StoreConfigurationType storeConfigType : identityConfigurationType.getIdentityStoreConfigurations()) {
            storeConfigs.add(buildStoreConfiguration(storeConfigType));
        }

        if (identityConfigurationType.getPartitionStoreConfiguration() != null) {
            IdentityStoreConfiguration partitionStoreConfig = buildStoreConfiguration(identityConfigurationType
                    .getPartitionStoreConfiguration());
            storeConfigs.add(partitionStoreConfig);
        }

        //FIXME: apply new design
//        builder.stores().readFrom(new IdentityStoresConfiguration(storeConfigs, null));
        
        return builder.build();
    }

    protected IdentityStoreConfiguration buildStoreConfiguration(StoreConfigurationType storeConfigType) {
        String className = storeConfigType.getClassName();

        if (className == null) {
            throw new SecurityConfigurationException("Classname of all storeConfigurationTypes must be provided!");
        }

        IdentityStoreConfiguration storeConfig = (IdentityStoreConfiguration) instantiateComponent(className);
        Class<?> storeConfigClass = storeConfig.getClass();

        Map<String, Object> props = storeConfigType.getAllProperties();
        for (String propertyName : props.keySet()) {
            // It's not optimal as we need to query and iterate methods separately for each property.
            // But performance shouldn't be big deal during reading configuration as it usually needs to be read only once...
            PropertyQuery<Object> propertyQuery = PropertyQueries.createQuery(storeConfigClass);
            propertyQuery.addCriteria(new NamedPropertyCriteria(propertyName));
            Property<Object> property = propertyQuery.getWritableSingleResult();

            // Obtain value from XML configuration
            Object propertyValueFromConfig = props.get(propertyName);

            // Create real instance of property from XML configuration
            Object propertyValue = PropertyResolverMapper.getInstance().resolveProperty(propertyValueFromConfig,
                    property.getJavaClass());

            // Set property to current storeConfiguration
            property.setValue(storeConfig, propertyValue);
        }

        return storeConfig;
    }

    public static Object instantiateComponent(String className) {
        try {
            // we should have better policy for classloaders (possibility to add other classloaders provided by user)
            Class<?> clazz = Reflections.classForName(className, IDM_CLASSLOADER);
            return clazz.newInstance();
        } catch (Exception e) {
            throw new SecurityConfigurationException("Exception during creation of component " + className, e);
        }
    }
}
