package org.picketlink.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.persistence.Entity;

/**
 * Automatic configuration builder for JPAIdentityStore - this CDI extension registers any entity
 * bean classes that are annotated with the PicketLink Identity Management JPA annotations.
 * 
 * @author Shane Bryzak
 *
 */
@ApplicationScoped
public class IdentityStoreAutoConfiguration implements Extension {

    private static final String JPA_ANNOTATION_PACKAGE = "org.picketlink.idm.jpa.annotations";

    private boolean configured;

    private Set<Class<?>> entities = new HashSet<Class<?>>();

    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event,
            final BeanManager beanManager) {

        if (event.getAnnotatedType().isAnnotationPresent(Entity.class)) {
            AnnotatedType<X> type = event.getAnnotatedType();
            if (isIdentityEntity(type.getJavaClass())) {
                entities.add(type.getJavaClass());
                configured = true;
            }
        }
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

    public Set<Class<?>> getEntities() {
        return entities;
    }

    public boolean isConfigured() {
        return this.configured;
    }
}
