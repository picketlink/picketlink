package org.picketlink.internal;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Extension;

/**
 * Automatic configuration builder for JPAIdentityStore
 * 
 * @author Shane Bryzak
 *
 */
@ApplicationScoped
public class IdentityStoreAutoConfiguration implements Extension {
//FIXME
//    private JPAStoreConfigurationBuilder jpaConfig = new IdentityConfigurationBuilder().stores().jpa();
//    private boolean configured;
//
//    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event,
//            final BeanManager beanManager) {
//
//        if (event.getAnnotatedType().isAnnotationPresent(Entity.class)) {
//            AnnotatedType<X> type = event.getAnnotatedType();
//
//            if (type.isAnnotationPresent(IdentityType.class)) {
//                jpaConfig.identityClass(type.getJavaClass());
//                this.configured = true;
//            } else if (type.isAnnotationPresent(IdentityCredential.class)) {
//                jpaConfig.credentialClass(type.getJavaClass());
//            } else if (type.isAnnotationPresent(CredentialAttribute.class)) {
//                jpaConfig.credentialAttributeClass(type.getJavaClass());
//            } else if (type.isAnnotationPresent(AttributeOf.class)) {
//                jpaConfig.attributeClass(type.getJavaClass());
//            } else if (type.isAnnotationPresent(Relationship.class)) {
//                jpaConfig.relationshipClass(type.getJavaClass());
//            } else if (type.isAnnotationPresent(RelationshipIdentity.class)) {
//                jpaConfig.relationshipIdentityClass(type.getJavaClass());
//            } else if (type.isAnnotationPresent(RelationshipAttribute.class)) {
//                jpaConfig.relationshipAttributeClass(type.getJavaClass());
//            } else if (type.isAnnotationPresent(Partition.class)) {
//                jpaConfig.partitionClass(type.getJavaClass());
//            }
//        }
//    }
//
//    public JPAStoreConfigurationBuilder getJPAConfiguration() {
//        return this.jpaConfig;
//    }
//
//    public boolean isConfigured() {
//        return this.configured;
//    }
}
