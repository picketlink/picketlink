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
package org.picketlink.config.http;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>Provides a set of options to configure how requests should be handled for a specific path.</p>
 *
 * @author Pedro Igor
 */
public class InboundConfigurationBuilder extends AbstractPathConfigurationChildBuilder implements InboundConfigurationChildBuilder{

    private AuthenticationConfigurationBuilder authenticationConfigBuilder;
    private AuthorizationConfigurationBuilder authorizationConfigurationBuilder;
    private InboundHeaderConfigurationBuilder inboundHeaderConfigurationBuilder;
    private LogoutConfigurationBuilder logoutConfigurationBuilder;
    private Set<String> methods = new HashSet<String>();

    InboundConfigurationBuilder(PathConfigurationBuilder parentBuilder) {
        super(parentBuilder);
    }

    /**
     * <p>Provides a set of options to configure authentication for a specific path.</p>
     *
     * @return
     */
    public AuthenticationConfigurationBuilder authc() {
        if (this.authenticationConfigBuilder == null) {
            this.authenticationConfigBuilder = new AuthenticationConfigurationBuilder(this);
        }

        return this.authenticationConfigBuilder;
    }

    /**
     * <p>Provides a set of options to configure authorization for a specific path.</p>
     *
     * @return
     */
    public AuthorizationConfigurationBuilder authz() {
        if (this.authorizationConfigurationBuilder == null) {
            this.authorizationConfigurationBuilder = new AuthorizationConfigurationBuilder(this);
        }

        return this.authorizationConfigurationBuilder;
    }

    /**
     * <p>Identifies a specific path as being responsible to provide logout functionality.</p>
     *
     * @return
     */
    public LogoutConfigurationBuilder logout() {
        if (this.logoutConfigurationBuilder == null) {
            this.logoutConfigurationBuilder = new LogoutConfigurationBuilder(this);
        }

        return this.logoutConfigurationBuilder;
    }

    /**
     * <p>Provides a set of options regarding the request headers for a specific request.</p>
     *
     * <p>In this case, headers are used to identify requests from each other and also enforce different security policies
     * depending on their presence.</p>
     *
     * @return
     */
    public InboundHeaderConfigurationBuilder headers() {
        if (this.inboundHeaderConfigurationBuilder == null) {
            this.inboundHeaderConfigurationBuilder = new InboundHeaderConfigurationBuilder(this);
        }

        return this.inboundHeaderConfigurationBuilder;
    }

    /**
     * <p>Configures the {@link org.picketlink.web.HttpMethod} that are supported by a specific path.</p>
     *
     * <p>In this case, methods can also be used to identity requests from each other and also enforce different security policies
     * dependending on their presence.</p>
     *
     * @param methods
     * @return
     */
    public InboundConfigurationBuilder methods(String... methods) {
        this.methods.addAll(Arrays.asList(methods));
        return this;
    }

    InboundConfiguration create(PathConfiguration pathConfiguration) {
        InboundConfiguration inboundConfiguration = new InboundConfiguration(pathConfiguration, this.methods);

        AuthenticationConfiguration authenticationConfiguration = null;

        if (this.authenticationConfigBuilder != null) {
            authenticationConfiguration = this.authenticationConfigBuilder.create(inboundConfiguration);
        }

        AuthorizationConfiguration authorizationConfiguration = null;

        if (this.authorizationConfigurationBuilder != null) {
            authorizationConfiguration = this.authorizationConfigurationBuilder.create(inboundConfiguration);
        }

        InboundHeaderConfiguration inboundHeaderConfiguration = null;

        if (this.inboundHeaderConfigurationBuilder != null) {
            inboundHeaderConfiguration = this.inboundHeaderConfigurationBuilder.create(inboundConfiguration);
        }

        LogoutConfiguration logoutConfiguration = null;

        if (this.logoutConfigurationBuilder != null) {
            logoutConfiguration = this.logoutConfigurationBuilder.create(inboundConfiguration);
        }

        inboundConfiguration.setAuthenticationConfiguration(authenticationConfiguration);
        inboundConfiguration.setAuthorizationConfiguration(authorizationConfiguration);
        inboundConfiguration.setInboundHeaderConfiguration(inboundHeaderConfiguration);
        inboundConfiguration.setLogoutConfiguration(logoutConfiguration);

        return inboundConfiguration;
    }
}