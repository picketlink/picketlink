package org.picketlink.producer;

import org.picketlink.IdentityConfigurationEvent;
import org.picketlink.extension.PicketLinkExtension;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoreConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoresConfigurationBuilder;
import org.picketlink.idm.config.JPAStoreConfigurationBuilder;
import org.picketlink.idm.config.NamedIdentityConfigurationBuilder;
import org.picketlink.idm.config.SecurityConfigurationException;
import org.picketlink.idm.config.TokenStoreConfigurationBuilder;
import org.picketlink.idm.credential.Token;
import org.picketlink.internal.AuthenticatedAccountContextInitializer;
import org.picketlink.internal.CDIEventBridge;
import org.picketlink.internal.EntityManagerContextInitializer;
import org.picketlink.internal.EntityManagerProvider;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.picketlink.log.BaseLog.ROOT_LOGGER;

/**
 * <p>The configuration is built depending on the existence of any {@link IdentityConfiguration} produced by the
 * application. If any configuration is found, it will be used. Otherwise the default configuration will be used.</p>
 *
 * <p>It's also possible to observe a specific event during the startup of the PicketLink IDM subsystem. In such
 * situations the application can provide any additional information as a last attempt before the subsystem is fully
 * initialized. See {@link org.picketlink.event.IdentityConfigurationEvent}.
 * </p>
 *
 * @author Shane Bryzak
 * @author Pedro Igor
 */
public class IdentityManagementConfiguration {

    private static final String DEFAULT_CONFIGURATION_NAME = "default";

    @Inject
    private PicketLinkExtension picketLinkExtension;

    @Inject
    private Instance<IdentityConfiguration> identityConfigInstance;

    @Inject
    private EntityManagerProvider entityManagerProvider;

    @Inject
    private EntityManagerContextInitializer entityManagerContextInitializer;

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

            for (IdentityStoreConfigurationBuilder storeBuilder : stores.getIdentityStoresConfigurationBuilder()) {
                storeBuilder.addContextInitializer(this.authenticatedAccountContextInitializer);

                if (JPAStoreConfigurationBuilder.class.isInstance(storeBuilder)) {
                    JPAStoreConfigurationBuilder jpaBuilder = (JPAStoreConfigurationBuilder) storeBuilder;

                    if (jpaBuilder.getMappedEntities().isEmpty()) {
                        Class<?>[] mappedEntities = this.entityManagerProvider.getMappedEntities();

                        if (mappedEntities.length == 0) {
                            throw new SecurityConfigurationException("You provided a configuration for the JPA Identity Store, but no mapped entities were found.");
                        }

                        jpaBuilder.mappedEntity(mappedEntities);
                    }

                    jpaBuilder.addContextInitializer(this.entityManagerContextInitializer);
                }

                if (TokenStoreConfigurationBuilder.class.isInstance(storeBuilder)) {
                    TokenStoreConfigurationBuilder tokenBuilder = (TokenStoreConfigurationBuilder) storeBuilder;

                    if (!this.tokenConsumerInstance.isUnsatisfied()) {
                        tokenBuilder.tokenConsumer(this.tokenConsumerInstance.get());
                    }
                }
            }
        }
    }

    private void configureDefaults(IdentityConfigurationBuilder builder) {
        if (ROOT_LOGGER.isDebugEnabled()) {
            ROOT_LOGGER.debugf("No configuration provided by the application. Configuring defaults.");
        }

        if (this.entityManagerProvider.hasMappedEntities()) {
            builder
                .named(DEFAULT_CONFIGURATION_NAME)
                    .stores()
                        .jpa()
                            .supportAllFeatures();
            if (ROOT_LOGGER.isDebugEnabled()) {
                ROOT_LOGGER.debugf("Auto configuring JPA Identity Store.");
            }
        } else {
            builder
                .named(DEFAULT_CONFIGURATION_NAME)
                    .stores()
                        .file()
                            .supportAllFeatures();
            if (ROOT_LOGGER.isDebugEnabled()) {
                ROOT_LOGGER.debugf("Auto configuring File Identity Store.");
            }
        }
    }


}
