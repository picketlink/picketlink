package org.picketlink.internal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.persistence.Entity;

import org.picketlink.IdentityConfigurationEvent;
import org.picketlink.idm.config.FeatureSet;
import org.picketlink.idm.jpa.annotations.IDMEntity;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration;

/**
 * Automatic configuration builder for JPAIdentityStore
 * 
 * @author Shane Bryzak
 *
 */
@ApplicationScoped
public class JPAIdentityStoreAutoConfig implements Extension {

    private JPAIdentityStoreConfiguration config = new JPAIdentityStoreConfiguration();

    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event,
            final BeanManager beanManager) {

        if (event.getAnnotatedType().isAnnotationPresent(Entity.class)) {
            AnnotatedType<X> type = event.getAnnotatedType();

            if (type.isAnnotationPresent(IDMEntity.class)) {
                IDMEntity a = type.getAnnotation(IDMEntity.class);

                switch(a.value()) { 
                    case IDENTITY_TYPE: 
                        config.setIdentityClass(type.getJavaClass());
                        break;
                    case IDENTITY_CREDENTIAL:
                        config.setCredentialClass(type.getJavaClass());
                        break;
                    case CREDENTIAL_ATTRIBUTE:
                        config.setCredentialAttributeClass(type.getJavaClass());
                    case IDENTITY_ATTRIBUTE:
                        config.setAttributeClass(type.getJavaClass());
                        break;
                    case RELATIONSHIP:
                        config.setRelationshipClass(type.getJavaClass());
                        break;
                    case RELATIONSHIP_IDENTITY:
                        config.setRelationshipIdentityClass(type.getJavaClass());
                        break;
                    case RELATIONSHIP_ATTRIBUTE:
                        config.setRelationshipAttributeClass(type.getJavaClass());
                        break;
                    case PARTITION:
                        config.setPartitionClass(type.getJavaClass());
                        break;
                }
            }
        }
    }

    public void observesConfigurationEvent(@Observes IdentityConfigurationEvent event) {
        if (config.isConfigured()) {
            FeatureSet.addFeatureSupport(config.getFeatureSet());
            FeatureSet.addRelationshipSupport(config.getFeatureSet());
            config.getFeatureSet().setSupportsCustomRelationships(true);
            config.getFeatureSet().setSupportsMultiRealm(true);
            event.getConfig().addStoreConfiguration(config);
        }
    }
}
