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

import org.picketlink.Identity;
import org.picketlink.config.SecurityConfiguration;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Customizes the {@link org.picketlink.Identity} behavior accordingly with the configuration provided by the
 * application.</p>
 *
 * @author Pedro Igor
 */
public class IdentityBeanDefinition implements Bean<DefaultIdentity>, Serializable, PassivationCapable {

    private static final long serialVersionUID = -4725126763788040967L;

    private final BeanManager beanManager;
    private final InjectionTarget<DefaultIdentity> injectionTarget;
    private SecurityConfiguration securityConfiguration;

    public IdentityBeanDefinition(BeanManager beanManager) {
        this.beanManager = beanManager;

        AnnotatedType<DefaultIdentity> annotatedType = this.beanManager.createAnnotatedType(getBeanClass());
        this.injectionTarget = this.beanManager.createInjectionTarget(annotatedType);
    }

    public void setSecurityConfiguration(SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    @Override
    public Set<Type> getTypes() {
        Set<Type> types = new HashSet<Type>();

        types.add(Identity.class);
        types.add(Object.class);

        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<Annotation>();

        qualifiers.add( new AnnotationLiteral<Default>() {} );
        qualifiers.add( new AnnotationLiteral<Any>() {} );

        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        Class<? extends Annotation> scope = SessionScoped.class;

        if (this.securityConfiguration != null && this.securityConfiguration.getIdentityBeanConfiguration() != null) {
            scope = this.securityConfiguration.getIdentityBeanConfiguration().getScope();
        }

        if (scope == null) {
            throw new IllegalStateException("No scope defined for " + Identity.class.getSimpleName() + " bean. Check your configuration.");
        }

        return scope;
    }

    @Override
    public String getName() {
        return "identity";
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public Class<DefaultIdentity> getBeanClass() {
        return DefaultIdentity.class;
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return this.injectionTarget.getInjectionPoints();
    }

    @Override
    public DefaultIdentity create(CreationalContext<DefaultIdentity> creationalContext) {
        DefaultIdentity identity = this.injectionTarget.produce(creationalContext);

        this.injectionTarget.inject(identity, creationalContext);
        this.injectionTarget.postConstruct(identity);

        return identity;
    }

    @Override
    public void destroy(DefaultIdentity instance, CreationalContext<DefaultIdentity> creationalContext) {
        this.injectionTarget.preDestroy(instance);
        this.injectionTarget.dispose(instance);
        creationalContext.release();
    }

    @Override
    public String getId() {
        return IdentityBeanDefinition.class.getName();
    }
}
