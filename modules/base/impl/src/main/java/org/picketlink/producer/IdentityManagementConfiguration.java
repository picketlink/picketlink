package org.picketlink.producer;

import org.picketlink.IdentityConfigurationEvent;
import org.picketlink.annotations.PicketLink;
import org.picketlink.extension.PicketLinkExtension;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoreConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoresConfigurationBuilder;
import org.picketlink.idm.config.JPAStoreConfigurationBuilder;
import org.picketlink.idm.config.NamedIdentityConfigurationBuilder;
import org.picketlink.idm.config.TokenStoreConfigurationBuilder;
import org.picketlink.idm.credential.Token;
import org.picketlink.internal.AuthenticatedAccountContextInitializer;
import org.picketlink.internal.CDIEventBridge;
import org.picketlink.internal.EEJPAContextInitializer;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.lang.reflect.Modifier.isAbstract;
import static org.picketlink.log.BaseLog.ROOT_LOGGER;

/**
 * <p>The configuration is built depending on the existence of any {@link IdentityConfiguration} produced by the
 * application. If any configuration is found, it will be used. Otherwise the default configuration will be used.</p>
 *
 * <p>It's also possible to observe a specific event during the startup of the PicketLink IDM subsystem. In such
 * situations the application can provide any additional information as a last attempt before the subsystem is fully
 * initialized. See {@link IdentityConfigurationEvent}.
 * </p>
 *
 * @author Shane Bryzak
 * @author Pedro Igor
 */
public class IdentityManagementConfiguration {

    private static final String DEFAULT_CONFIGURATION_NAME = "default";
    private static final String JPA_ANNOTATION_PACKAGE = "org.picketlink.idm.jpa.annotations";

    @Inject
    private PicketLinkExtension picketLinkExtension;

    @Inject
    private Instance<IdentityConfiguration> identityConfigInstance;

    @Inject
    @PicketLink
    private Instance<EntityManager> entityManagerInstance;

    @Inject
    private EEJPAContextInitializer entityManagerContextInitializer;

    @Inject
    private AuthenticatedAccountContextInitializer authenticatedAccountContextInitializer;

    @Inject
    private Instance<Token.Consumer<?>> tokenConsumerInstance;

    @Inject
    private Instance<Token.Provider<?>> tokenProviderInstance;

    @Inject
    private CDIEventBridge eventBridge;

    private List<IdentityConfiguration> identityConfiguration;

    /**
     * <p>Returns all configurations produced by the application.</p>
     *
     * @return
     */
    List<IdentityConfiguration> getIdentityConfiguration() {
        if (this.identityConfiguration == null) {
            if (ROOT_LOGGER.isDebugEnabled()) {
                ROOT_LOGGER.debugf("Building identity management configuration.");
            }

            List<IdentityConfiguration> configurations = new ArrayList<IdentityConfiguration>();

            if (!this.identityConfigInstance.isUnsatisfied()) {
                for (Iterator<IdentityConfiguration> iterator = this.identityConfigInstance.iterator(); iterator.hasNext(); ) {
                    configurations.add(iterator.next());
                }
            }

            IdentityConfigurationBuilder builder;

            if (configurations.isEmpty()) {
                if (ROOT_LOGGER.isDebugEnabled()) {
                    ROOT_LOGGER.debugf("IdentityConfiguration not provided by the application, creating a default IdentityConfigurationBuilder.");
                }

                builder = this.picketLinkExtension.getSecurityConfigurationBuilder().idmConfig();
            } else {
                if (ROOT_LOGGER.isDebugEnabled()) {
                    ROOT_LOGGER.debugf("Found IdentityConfiguration from the environment. Creating a IdentityConfigurationBuilder with them.");
                }

                builder = new IdentityConfigurationBuilder(configurations);
            }

            this.eventBridge.fireEvent(new IdentityConfigurationEvent(builder));

            if (!builder.isConfigured()) {
                configureDefaults(builder);
            }

            configureIdentityStores(builder);

            this.identityConfiguration = builder.buildAll();
        }

        return this.identityConfiguration;
    }

    private void configureIdentityStores(IdentityConfigurationBuilder builder) {
        for (NamedIdentityConfigurationBuilder identityConfigurationBuilder : builder.getNamedIdentityConfigurationBuilders()) {
            IdentityStoresConfigurationBuilder stores = identityConfigurationBuilder.stores();

            for (IdentityStoreConfigurationBuilder storeConfigurationBuilder : stores.getIdentityStoresConfigurationBuilder()) {
                storeConfigurationBuilder.addContextInitializer(this.authenticatedAccountContextInitializer);

                if (JPAStoreConfigurationBuilder.class.isInstance(storeConfigurationBuilder)) {
                    JPAStoreConfigurationBuilder jpaStoreBuilder = (JPAStoreConfigurationBuilder) storeConfigurationBuilder;

                    if (jpaStoreBuilder.getMappedEntities().isEmpty()) {
                        jpaStoreBuilder.mappedEntity(getEntities());
                    }

                    storeConfigurationBuilder.addContextInitializer(this.entityManagerContextInitializer);
                }

                if (TokenStoreConfigurationBuilder.class.isInstance(storeConfigurationBuilder)) {
                    TokenStoreConfigurationBuilder tokenStoreBuilder = (TokenStoreConfigurationBuilder) storeConfigurationBuilder;

                    if (!this.tokenConsumerInstance.isUnsatisfied()) {
                        tokenStoreBuilder.tokenConsumer(this.tokenConsumerInstance.get());
                    }
                }
            }
        }
    }

    private void configureDefaults(IdentityConfigurationBuilder builder) {
        if (ROOT_LOGGER.isDebugEnabled()) {
            ROOT_LOGGER.debugf("No configuration provided by the application. Configuring defaults.");
        }

        Class<?>[] entities = getEntities();

        if (entities.length == 0) {
            builder
                .named(DEFAULT_CONFIGURATION_NAME)
                .stores()
                .file()
                .supportAllFeatures();
            if (ROOT_LOGGER.isDebugEnabled()) {
                ROOT_LOGGER.debugf("Auto configuring File Identity Store. All features are going to be supported.", entities);
            }
        } else {
            builder
                .named(DEFAULT_CONFIGURATION_NAME)
                .stores()
                .jpa()
                .mappedEntity(entities)
                .addContextInitializer(this.entityManagerContextInitializer)
                .supportAllFeatures();
            if (ROOT_LOGGER.isDebugEnabled()) {
                ROOT_LOGGER.debugf("Auto configuring JPA Identity Store. All features are going to be supported. Entities [%s]", entities);
            }
        }
    }

    private Class<?>[] getEntities() {
        Set<Class<?>> entities = new HashSet<Class<?>>();

        if (!this.entityManagerInstance.isUnsatisfied()) {
            EntityManager entityManager = this.entityManagerInstance.get();

            for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
                Class<?> javaType = entityType.getJavaType();

                if (!isAbstract(javaType.getModifiers()) && isIdentityEntity(javaType)) {
                    if (ROOT_LOGGER.isDebugEnabled()) {
                        ROOT_LOGGER.debugf("PicketLink IDM mapped entity found [%s].", entityType);
                    }

                    entities.add(javaType);
                }
            }
        }

        return entities.toArray(new Class<?>[entities.size()]);
    }

    private boolean isIdentityEntity(Class<?> cls) {
        while (!cls.equals(Object.class)) {
            for (Annotation a : cls.getAnnotations()) {
                if (a.annotationType().getName().startsWith(JPA_ANNOTATION_PACKAGE)) {
                    return true;
                }
            }

            // No class annotation was found, check the fields
            for (Field f : cls.getDeclaredFields()) {
                for (Annotation a : f.getAnnotations()) {
                    if (a.annotationType().getName().startsWith(JPA_ANNOTATION_PACKAGE)) {
                        return true;
                    }
                }
            }

            // Check the superclass
            cls = cls.getSuperclass();
        }

        return false;
    }
}
