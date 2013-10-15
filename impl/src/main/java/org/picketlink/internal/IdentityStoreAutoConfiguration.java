package org.picketlink.internal;

import org.picketlink.idm.config.IdentityConfigurationBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Collections.unmodifiableSet;

/**
 * Automatic configuration builder for JPAIdentityStore - this CDI extension registers any entity
 * bean classes that are annotated with the PicketLink Identity Management JPA annotations.
 *
 * @author Shane Bryzak
 */
@ApplicationScoped
public class IdentityStoreAutoConfiguration implements Extension {

    private static final String DEFAULT_CONFIGURATION_NAME = "default";
    private static final String JPA_ANNOTATION_PACKAGE = "org.picketlink.idm.jpa.annotations";

    private Set<Class<?>> entities = new HashSet<Class<?>>();
    private BeanManager beanManager;

    public void configure(final IdentityConfigurationBuilder builder) {
        if (isJPAStoreConfiguration()) {
            Class<?>[] entities = new Class[getEntities().size()];

            getEntities().toArray(entities);

            builder
                    .named(DEFAULT_CONFIGURATION_NAME)
                    .stores()
                    .jpa()
                    .mappedEntity(entities)
                    .addContextInitializer(getJPAContextInitializer())
                    .supportAllFeatures();
        } else {
            builder
                    .named(DEFAULT_CONFIGURATION_NAME)
                    .stores()
                    .file()
                    .supportAllFeatures();
        }
    }

    public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event, final BeanManager beanManager) {
        this.beanManager = beanManager;
        if (event.getAnnotatedType().isAnnotationPresent(Entity.class)) {
            AnnotatedType<X> type = event.getAnnotatedType();
            Class<X> entityType = type.getJavaClass();

            if (!isAbstract(entityType.getModifiers()) && isIdentityEntity(entityType)) {
                entities.add(entityType);
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
        return unmodifiableSet(this.entities);
    }

    private boolean isJPAStoreConfiguration() {
        return !getEntities().isEmpty();
    }

    private EEJPAContextInitializer getJPAContextInitializer() {
        Set<Bean<?>> beans = this.beanManager.getBeans(EEJPAContextInitializer.class);

        if (beans.isEmpty()) {
            throw new SecurityException("Could not find JPA Context Initializer. Expected type [" +
                    EEJPAContextInitializer.class + "].");
        } else if (beans.size() > 1) {
            throw new SecurityException("Multiple references found for JPA Context Initializer. " +
                    "Expected type [" + EEJPAContextInitializer.class + "].");
        }

        Bean<?> bean = beans.iterator().next();

        return (EEJPAContextInitializer) this.beanManager.getReference(bean, EEJPAContextInitializer.class,
                this.beanManager.createCreationalContext(bean));
    }
}
