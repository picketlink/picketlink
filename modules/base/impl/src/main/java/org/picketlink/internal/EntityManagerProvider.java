/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.picketlink.internal;

import org.picketlink.annotations.PicketLink;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static java.lang.reflect.Modifier.isAbstract;
import static org.picketlink.log.BaseLog.ROOT_LOGGER;

/**
 * <p>Provides useful methods to properly manage {@link javax.persistence.EntityManager} instances and related metadata when using in
 * conjunction with the {@link org.picketlink.idm.jpa.internal.JPAIdentityStore}.</p>
 *
 * @author Pedro Igor
 */
public class EntityManagerProvider {

    private static final String JPA_ANNOTATION_PACKAGE = "org.picketlink.idm.jpa.annotations";

    @Inject
    @Any
    private Instance<EntityManager> entityManagerInstance;
    private Class<?>[] mappedEntities;

    @Inject
    public void init() {
        this.mappedEntities = resolveMappedEntities();
    }

    /**
     * <p>Returns an {@link javax.persistence.EntityManager} if produced by the application.</p>
     *
     * <p>The entity manager is returned based on the following rules:</p>
     *
     * <ul>
     *     <li>If the entity manager is produced using the {@link org.picketlink.annotations.PicketLink} qualifier, it will be used.
     *     <li>Otherwise, returns a non-qualified entity manager if it exists.</li>
     * </ul>
     *
     * @return An {@link javax.persistence.EntityManager} instance or null if none was found.
     */
    public EntityManager getEntityManager() {
        if (!this.entityManagerInstance.isUnsatisfied()) {
            Instance<EntityManager> picketLinkEntityManager = this.entityManagerInstance.select(new AnnotationLiteral<PicketLink>() {});

            if (!picketLinkEntityManager.isUnsatisfied()) {
                return picketLinkEntityManager.get();
            }

            return this.entityManagerInstance.get();
        }

        return null;
    }

    public Class<?>[] getMappedEntities() {
        return this.mappedEntities;
    }

    /**
     * <p>Indicates if the {@link EntityManagerProvider#getEntityManager()} is configured with any entity class annotated
     * with the PicketLink IDM JPA Annotations.</p>
     *
     * <p>If true, it usually means that the application is configured with a JPA Identity Store.</p>
     *
     * @return True if there are IDM entities. Otherwise, returns false.
     */
    public boolean hasMappedEntities() {
        return resolveMappedEntities().length != 0;
    }

    /**
     * <p>Returns all entities from the {@link EntityManagerProvider#getEntityManager()} which are annotated with PicketLink IDM JPA Annotations.</p>
     *
     * @return An array with all IDM entity classes.
     */
    private Class<?>[] resolveMappedEntities() {
        Set<Class<?>> entities = new HashSet<Class<?>>();
        EntityManager entityManager = getEntityManager();

        if (entityManager != null) {
            for (EntityType<?> entityType : entityManager.getMetamodel().getEntities()) {
                Class<?> javaType = entityType.getJavaType();

                if (javaType != null) {
                    if (!isAbstract(javaType.getModifiers()) && isMappedEntity(javaType)) {
                        if (ROOT_LOGGER.isDebugEnabled()) {
                            ROOT_LOGGER.debugf("PicketLink IDM mapped entity found [%s].", entityType);
                        }

                        entities.add(javaType);
                    }
                }
            }
        }

        return entities.toArray(new Class<?>[entities.size()]);
    }

    private boolean isMappedEntity(Class<?> cls) {
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
