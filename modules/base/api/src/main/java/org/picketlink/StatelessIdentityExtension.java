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
package org.picketlink;

import org.picketlink.annotations.StatelessIdentity;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Stereotype;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>{@link javax.enterprise.inject.spi.Extension} that registers a {@link org.picketlink.Identity} bean based on the presence of
 * the {@link org.picketlink.annotations.StatelessIdentity} stereotype.</p>
 *
 * <p>This extension is a workaround for CDI 1.0. This version only enable stereotypes for beans inside the module that defined the
 * stereotype via <code>beans.xml</code>. In other words, if an application has defined the stereotype, the beans provided by
 * PicketLink from its jars would not inherit that configuration and would never have the stateless identity bean injected.</p>
 *
 * <p>This extension should be removed as soon as we start supprting JEE7/CDI 1.1+.</p>
 *
 * @author Pedro Igor
 * @see org.picketlink.annotations.StatelessIdentity
 */
public class StatelessIdentityExtension implements Extension {

    private final boolean enableStatelessIdentity;

    public StatelessIdentityExtension() {
        this.enableStatelessIdentity = isStatelessIdentityDefined();
    }

    <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat, final BeanManager beanManager) {
        if (this.enableStatelessIdentity) {
            final AnnotatedType<T> annotatedType = pat.getAnnotatedType();
            Class<T> javaClass = annotatedType.getJavaClass();

            if (!Identity.class.equals(javaClass) && Identity.class.isAssignableFrom(javaClass)) {
                if (!javaClass.isAnnotationPresent(StatelessIdentity.class)) {
                    pat.veto();
                } else {
                    pat.setAnnotatedType(wrapStatalessIdentityBean(beanManager, annotatedType));
                }
            }
        }
    }

    private <T> AnnotatedType<T> wrapStatalessIdentityBean(final BeanManager beanManager, final AnnotatedType<T> annotatedType) {
        return new AnnotatedType<T>() {
            public Set<Annotation> annotations;

            @Override
            public Class<T> getJavaClass() {
                return annotatedType.getJavaClass();
            }

            @Override
            public Set<AnnotatedConstructor<T>> getConstructors() {
                return annotatedType.getConstructors();
            }

            @Override
            public Set<AnnotatedMethod<? super T>> getMethods() {
                return annotatedType.getMethods();
            }

            @Override
            public Set<AnnotatedField<? super T>> getFields() {
                return annotatedType.getFields();
            }

            @Override
            public Type getBaseType() {
                return annotatedType.getBaseType();
            }

            @Override
            public Set<Type> getTypeClosure() {
                return annotatedType.getTypeClosure();
            }

            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
                for (Annotation annotation : getAnnotations()) {
                    if (annotation.annotationType().equals(annotatedType)) {
                        return (T) annotation;
                    }
                }

                return null;
            }

            @Override
            public Set<Annotation> getAnnotations() {
                if (this.annotations == null) {
                    Set<Annotation> annotations = new HashSet<Annotation>(beanManager.getStereotypeDefinition(StatelessIdentity.class));

                    annotations.addAll(annotatedType.getAnnotations());

                    for (Annotation annotation : new HashSet<Annotation>(annotations)) {
                        if (annotation.annotationType().equals(StatelessIdentity.class)
                            || annotation.annotationType().equals(Stereotype.class)
                            || annotation.annotationType().equals(Alternative.class)) {
                            annotations.remove(annotation);
                        }
                    }

                    this.annotations = Collections.unmodifiableSet(annotations);
                }

                return this.annotations;
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                for (Annotation annotation : getAnnotations()) {
                    if (annotation.annotationType().equals(annotationType)) {
                        return true;
                    }
                }

                return false;
            }
        };
    }

    /**
     * <p>This is a nasty hack. It helps to read all <code>META-INF/beans.xml</code> or <code>WEB-INF/beans.xml</code> from the classpath and
     * check if the {@link org.picketlink.annotations.StatelessIdentity} stereotype is defined.</p>
     *
     * @return True if the {@link org.picketlink.annotations.StatelessIdentity} was found in any beans.xml.
     */
    private boolean isStatelessIdentityDefined() {
        BufferedReader reader = null;

        try {
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = tccl.getResources(".");

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String resourcePath = url.toString();

                if (resourcePath.endsWith("WEB-INF/classes") || resourcePath.endsWith("WEB-INF/classes/")) {
                    InputStream beansXmlAsStream = getInputStream(new URL(resourcePath + "/../beans.xml"));

                    if (beansXmlAsStream == null) {
                        beansXmlAsStream = getInputStream(new URL(resourcePath + "/META-INF/beans.xml"));
                    }

                    if (beansXmlAsStream != null) {
                        reader = new BufferedReader(new InputStreamReader(beansXmlAsStream));

                        String line;

                        while ((line = reader.readLine()) != null) {
                            if (line.contains(StatelessIdentity.class.getName())) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read beans.xml", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
        }

        return false;
    }

    private InputStream getInputStream(URL url) throws IOException {
        try {
            return new URL(url.toString()).openStream();
        } catch (FileNotFoundException ignore) {
        }

        return null;
    }
}