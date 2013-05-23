package org.picketlink.internal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.persistence.Entity;

import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.JPAStoreConfigurationBuilder;
import org.picketlink.idm.jpa.annotations.CredentialAttribute;
import org.picketlink.idm.jpa.annotations.IdentityAttribute;
import org.picketlink.idm.jpa.annotations.IdentityCredential;
import org.picketlink.idm.jpa.annotations.IdentityType;
import org.picketlink.idm.jpa.annotations.Partition;
import org.picketlink.idm.jpa.annotations.Relationship;
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

    private JPAStoreConfigurationBuilder jpaConfig = new IdentityConfigurationBuilder().stores().jpa();
    private boolean configured;
    
    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event,
            final BeanManager beanManager) {

        if (event.getAnnotatedType().isAnnotationPresent(Entity.class)) {
            AnnotatedType<X> type = event.getAnnotatedType();

            if (type.isAnnotationPresent(IdentityType.class)) {
                jpaConfig.identityClass(type.getJavaClass());
                this.configured = true;
            } else if (type.isAnnotationPresent(IdentityCredential.class)) {
                jpaConfig.credentialClass(type.getJavaClass());
            } else if (type.isAnnotationPresent(CredentialAttribute.class)) {
                jpaConfig.credentialAttributeClass(type.getJavaClass());
            } else if (type.isAnnotationPresent(IdentityAttribute.class)) {
                jpaConfig.attributeClass(type.getJavaClass()); 
            } else if (type.isAnnotationPresent(Relationship.class)) {
                jpaConfig.relationshipClass(type.getJavaClass());
            } else if (type.isAnnotationPresent(RelationshipIdentity.class)) {
                jpaConfig.relationshipIdentityClass(type.getJavaClass());
            } else if (type.isAnnotationPresent(RelationshipAttribute.class)) {
                jpaConfig.relationshipAttributeClass(type.getJavaClass());
            } else if (type.isAnnotationPresent(Partition.class)) {
                jpaConfig.partitionClass(type.getJavaClass());
            }
        }
    }

    public JPAStoreConfigurationBuilder getJPAConfiguration() {
        return this.jpaConfig;
    }
    
    public boolean isConfigured() {
        return this.configured;
    }
}
