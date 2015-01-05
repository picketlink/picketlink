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

import org.picketlink.http.HttpMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>A configuration builder with covenience methods to configure protected paths.</p>
 *
 * @author Pedro Igor
 */
public class PathConfigurationBuilder extends AbstracHttpSecurityConfigurationChildBuilder implements PathConfigurationChildBuilder {

    private final String groupName;
    private final String uri;
    private Boolean secured;
    private AuthenticationConfigurationBuilder authenticationConfigBuilder;
    private AuthorizationConfigurationBuilder authorizationConfigurationBuilder;
    private InboundHeaderConfigurationBuilder inboundHeaderConfigurationBuilder;
    private LogoutConfigurationBuilder logoutConfigurationBuilder;
    private Set<HttpMethod> methods = new HashSet<HttpMethod>();
    private final List<OutboundRedirectConfigurationBuilder> redirects = new ArrayList<OutboundRedirectConfigurationBuilder>();

    PathConfigurationBuilder(String groupName, String uri, Boolean secured, AbstractHttpSecurityBuilder parentBuilder) {
        super(parentBuilder);
        this.groupName = groupName;
        this.uri = uri;
        this.secured = secured;
    }

    /**
     * <p>Provides a set of options to configure authentication for a specific path.</p>
     *
     * @return
     */
    public AuthenticationConfigurationBuilder authenticateWith() {
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
    public AuthorizationConfigurationBuilder authorizeWith() {
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
    public InboundHeaderConfigurationBuilder withHeaders() {
        if (this.inboundHeaderConfigurationBuilder == null) {
            this.inboundHeaderConfigurationBuilder = new InboundHeaderConfigurationBuilder(this);
        }

        return this.inboundHeaderConfigurationBuilder;
    }

    /**
     * <p>Configures the {@link org.picketlink.http.HttpMethod} that are supported by a specific path.</p>
     *
     * <p>In this case, methods can also be used to identity requests from each other and also enforce different security policies
     * dependending on their presence.</p>
     *
     * @param methods
     * @return
     */
    public PathConfigurationBuilder withMethod(HttpMethod... methods) {
        this.methods.addAll(Arrays.asList(methods));
        return this;
    }

    /**
     * <p>Specifies a url that will be used to redirect the user after a specific path is processed.</p>
     *
     * <p>For instance, after a logout request you may use this method to redirect the user to a different url.</p>
     *
     * @param redirectUrl
     * @return
     */
    @Override
    public OutboundRedirectConfigurationBuilder redirectTo(String redirectUrl) {
        OutboundRedirectConfigurationBuilder builder = new OutboundRedirectConfigurationBuilder(this, redirectUrl);

        this.redirects.add(builder);

        return builder;
    }

    /**
     * <p>Mark a specific path as not protected. When a path is not protected, no security enforcement is applied.</p>
     *
     * @return
     */
    public PathConfigurationBuilder unprotected() {
        this.secured = false;
        return this;
    }

    @Override
    protected PathConfiguration create() {
        List<OutboundRedirectConfiguration> redirectConfigurations = new ArrayList<OutboundRedirectConfiguration>();

        for (OutboundRedirectConfigurationBuilder builder : this.redirects) {
            redirectConfigurations.add(builder.create());
        }

        PathConfiguration pathConfiguration = new PathConfiguration(
            this.groupName,
            this.uri,
            this.secured,
            this.methods,
            redirectConfigurations);

        AuthenticationConfiguration authenticationConfiguration = null;

        if (this.authenticationConfigBuilder != null) {
            authenticationConfiguration = this.authenticationConfigBuilder.create(pathConfiguration);
        }

        AuthorizationConfiguration authorizationConfiguration = null;

        if (this.authorizationConfigurationBuilder != null) {
            authorizationConfiguration = this.authorizationConfigurationBuilder.create(pathConfiguration);
        }

        InboundHeaderConfiguration inboundHeaderConfiguration = null;

        if (this.inboundHeaderConfigurationBuilder != null) {
            inboundHeaderConfiguration = this.inboundHeaderConfigurationBuilder.create(pathConfiguration);
        }

        LogoutConfiguration logoutConfiguration = null;

        if (this.logoutConfigurationBuilder != null) {
            logoutConfiguration = this.logoutConfigurationBuilder.create(pathConfiguration);
        }

        pathConfiguration.setAuthenticationConfiguration(authenticationConfiguration);
        pathConfiguration.setAuthorizationConfiguration(authorizationConfiguration);
        pathConfiguration.setInboundHeaderConfiguration(inboundHeaderConfiguration);
        pathConfiguration.setLogoutConfiguration(logoutConfiguration);

        return pathConfiguration;
    }
}
