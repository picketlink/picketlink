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
package org.picketlink.config;

import org.picketlink.idm.config.Builder;
import org.picketlink.idm.config.SecurityConfigurationException;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import java.lang.annotation.Annotation;

/**
 * <p>A configuration builder with covenience methods to configure the behavior of the {@link org.picketlink.Identity} bean.</p>
 *
 * @author Pedro Igor
 */
public class IdentityBeanConfigurationBuilder extends AbstractSecurityConfigurationBuilder<IdentityBeanConfiguration> {

    private Class<? extends Annotation> scope = SessionScoped.class;

    public IdentityBeanConfigurationBuilder(SecurityConfigurationBuilder builder) {
        super(builder);
    }

    /**
     * <p>Enables the stateless mode of the {@link org.picketlink.Identity} bean. In this case, the bean will be
     * {@link javax.enterprise.context.RequestScoped}.</p>
     *
     * <p>Default is false.</p>
     *
     * @return
     */
    public IdentityBeanConfigurationBuilder stateless() {
        scope(RequestScoped.class);
        return this;
    }

    /**
     * <p>Sepcifies the scope of the {@link org.picketlink.Identity} bean.</p>
     *
     * @param scope The scope of the identity bean. It can not be null.
     * @return
     */
    public IdentityBeanConfigurationBuilder scope(Class<? extends Annotation> scope) {
        this.scope = scope;
        return this;
    }

    @Override
    protected IdentityBeanConfiguration create() throws SecurityConfigurationException {
        return new IdentityBeanConfiguration(this.scope);
    }

    @Override
    protected void validate() throws SecurityConfigurationException {
    }

    @Override
    protected Builder<IdentityBeanConfiguration> readFrom(IdentityBeanConfiguration fromConfiguration) throws SecurityConfigurationException {
        if (RequestScoped.class.equals(fromConfiguration.getScope())) {
            this.stateless();
        }

        return this;
    }
}
