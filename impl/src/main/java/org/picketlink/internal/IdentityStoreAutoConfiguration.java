package org.picketlink.internal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.persistence.Entity;

import org.picketlink.idm.config.JPAIdentityStoreConfiguration;
import org.picketlink.idm.jpa.annotations.CredentialAttribute;
import org.picketlink.idm.jpa.annotations.IdentityAttribute;
import org.picketlink.idm.jpa.annotations.IdentityCredential;
import org.picketlink.idm.jpa.annotations.IdentityType;
import org.picketlink.idm.jpa.annotations.Partition;
import org.picketlink.idm.jpa.annotations.RelationshipAttribute;
import org.picketlink.idm.jpa.annotations.RelationshipIdentity;

/**
 * Automatic configuration builder for JPAIdentityStore
 * 
 * @author Shane Bryzak
 *
 */
@ApplicationScoped
public class IdentityStoreAutoConfiguration implements Extension {

    private JPAIdentityStoreConfiguration jpaConfig = new JPAIdentityStoreConfiguration();

    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event,
            final BeanManager beanManager) {

        if (event.getAnnotatedType().isAnnotationPresent(Entity.class)) {
            AnnotatedType<X> type = event.getAnnotatedType();

            if (type.isAnnotationPresent(IdentityType.class)) {
                jpaConfig.setIdentityClass(type.getJavaClass());
            } else if (type.isAnnotationPresent(IdentityCredential.class)) {
                jpaConfig.setCredentialClass(type.getJavaClass());
            } else if (type.isAnnotationPresent(CredentialAttribute.class)) {
                jpaConfig.setCredentialAttributeClass(type.getJavaClass());
            } else if (type.isAnnotationPresent(IdentityAttribute.class)) {
                jpaConfig.setAttributeClass(type.getJavaClass()); 
            } else if (type.isAnnotationPresent(RelationshipIdentity.class)) {
                jpaConfig.setRelationshipIdentityClass(type.getJavaClass());
            } else if (type.isAnnotationPresent(RelationshipAttribute.class)) {
                jpaConfig.setRelationshipAttributeClass(type.getJavaClass());
            } else if (type.isAnnotationPresent(Partition.class)) {
                jpaConfig.setPartitionClass(type.getJavaClass());
            }
        }
    }

    public JPAIdentityStoreConfiguration getJPAConfiguration() {
        return jpaConfig;
    }
}
