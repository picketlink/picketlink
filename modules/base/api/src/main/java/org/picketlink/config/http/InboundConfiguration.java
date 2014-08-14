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

import org.picketlink.web.HttpMethod;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Pedro Igor
 */
public class InboundConfiguration {

    private final PathConfiguration pathConfiguration;
    private Set<String> methods;
    private AuthenticationConfiguration authenticationConfiguration;
    private AuthorizationConfiguration authorizationConfiguration;
    private InboundHeaderConfiguration inboundHeaderConfiguration;
    private LogoutConfiguration logoutConfiguration;

    public InboundConfiguration(PathConfiguration pathConfiguration, Set<String> methods) {
        this.pathConfiguration = pathConfiguration;

        if (methods != null && !methods.isEmpty()) {
            this.methods = methods;
        } else {
            this.methods = HttpMethod.names();
        }
    }

    public AuthenticationConfiguration getAuthenticationConfiguration() {
        PathConfiguration pathConfiguration = getPathConfiguration();

        if (pathConfiguration.isUri() && pathConfiguration.getGroupName() != null && !pathConfiguration.isDefaultGroup()) {
            Map<String, PathConfiguration> groups = pathConfiguration.getSecurityConfiguration().getGroups();
            PathConfiguration groupConfiguration = groups.get(pathConfiguration.getGroupName());
            AuthenticationConfiguration actualConfig = new AuthenticationConfiguration(this);

            if (this.authenticationConfiguration != null) {
                if (this.authenticationConfiguration.getAuthenticationSchemeConfiguration() == null) {
                    AuthenticationConfiguration groupAuthcConfig = groupConfiguration.getInboundConfiguration().getAuthenticationConfiguration();

                    actualConfig.setAuthenticationSchemeConfiguration(groupAuthcConfig.getAuthenticationSchemeConfiguration());

                    return actualConfig;
                }
            } else {
                return groupConfiguration.getInboundConfiguration().getAuthenticationConfiguration();
            }
        }

        return this.authenticationConfiguration;
    }

    protected void setAuthenticationConfiguration(AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }

    public AuthorizationConfiguration getAuthorizationConfiguration() {
        return this.authorizationConfiguration;
    }

    protected void setAuthorizationConfiguration(AuthorizationConfiguration authorizationConfiguration) {
        this.authorizationConfiguration = authorizationConfiguration;
    }

    public InboundHeaderConfiguration getInboundHeaderConfiguration() {
        return this.inboundHeaderConfiguration;
    }

    protected void setInboundHeaderConfiguration(InboundHeaderConfiguration inboundHeaderConfiguration) {
        this.inboundHeaderConfiguration = inboundHeaderConfiguration;
    }

    public LogoutConfiguration getLogoutConfiguration() {
        return this.logoutConfiguration;
    }

    protected void setLogoutConfiguration(LogoutConfiguration logoutConfiguration) {
        this.logoutConfiguration = logoutConfiguration;
    }

    public PathConfiguration getPathConfiguration() {
        return this.pathConfiguration;
    }

    public Set<String> getMethods() {
        return Collections.unmodifiableSet(this.methods);
    }

    protected void setMethods(Set<String> methods) {
        this.methods = methods;
    }
}
