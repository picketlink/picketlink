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
import org.picketlink.http.authorization.PathAuthorizer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * @author Pedro Igor
 */
public class PathConfiguration {

    public static final String URI_ALL = "/*";
    public static final String DEFAULT_GROUP_NAME = "Default";

    private final Boolean secured;
    private final List<OutboundRedirectConfiguration> redirects;
    private String groupName = DEFAULT_GROUP_NAME;
    private final String uri;
    private HttpSecurityConfiguration securityConfiguration;
    private LogoutConfiguration logoutConfiguration;
    private AuthenticationConfiguration authenticationConfiguration;
    private AuthorizationConfiguration authorizationConfiguration;
    private InboundHeaderConfiguration inboundHeaderConfiguration;
    private Set<HttpMethod> methods;

    public PathConfiguration(
        String groupName,
        String uri,
        Boolean secured,
        Set<HttpMethod> methods, List<OutboundRedirectConfiguration> redirects) {
        if (groupName == null && uri == null) {
            throw new HttpSecurityConfigurationException("You must provide a group name or uri. Or even both.");
        }

        this.groupName = groupName;
        this.uri = uri;
        this.secured = secured;

        if (methods != null && !methods.isEmpty()) {
            this.methods = methods;
        } else {
            this.methods = new HashSet(Arrays.asList(HttpMethod.values()));
        }

        if (redirects == null) {
            redirects = emptyList();
        }

        this.redirects = redirects;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public String getUri() {
        return this.uri;
    }

    public boolean isSecured() {
        if (this.secured == null) {
            if (hasGroup()) {
                PathConfiguration groupConfiguration = getGroupConfiguration();
                return groupConfiguration.isSecured();
            }

            return true;
        }

        return this.secured;
    }

    public boolean isGroup() {
        return this.groupName != null && !isDefaultGroup() && this.uri == null;
    }

    public boolean isUri() {
        return !isGroup();
    }

    public boolean isDefaultGroup() {
        return DEFAULT_GROUP_NAME.equals(this.groupName);
    }

    public HttpSecurityConfiguration getSecurityConfiguration() {
        return this.securityConfiguration;
    }

    protected void setSecurityConfiguration(HttpSecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    private PathConfiguration getGroupConfiguration() {
        Map<String, PathConfiguration> groups = getSecurityConfiguration().getGroups();
        return groups.get(getGroupName());
    }

    private boolean hasGroup() {
        return isUri() && getGroupName() != null && !isDefaultGroup();
    }

    public AuthenticationConfiguration getAuthenticationConfiguration() {
        PathConfiguration pathConfiguration = this;

        if (pathConfiguration.isUri() && pathConfiguration.getGroupName() != null && !pathConfiguration.isDefaultGroup()) {
            Map<String, PathConfiguration> groups = pathConfiguration.getSecurityConfiguration().getGroups();
            PathConfiguration groupConfiguration = groups.get(pathConfiguration.getGroupName());
            AuthenticationConfiguration actualConfig = new AuthenticationConfiguration(this);

            if (this.authenticationConfiguration != null) {
                if (this.authenticationConfiguration.getAuthenticationSchemeConfiguration() == null) {
                    AuthenticationConfiguration groupAuthcConfig = groupConfiguration.getAuthenticationConfiguration();

                    actualConfig.setAuthenticationSchemeConfiguration(groupAuthcConfig.getAuthenticationSchemeConfiguration());

                    return actualConfig;
                }
            } else if (groupConfiguration != null) {
                return groupConfiguration.getAuthenticationConfiguration();
            }
        }

        return this.authenticationConfiguration;
    }

    protected void setAuthenticationConfiguration(AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
    }

    public AuthorizationConfiguration getAuthorizationConfiguration() {
        PathConfiguration pathConfiguration = this;

        if (pathConfiguration.isUri() && pathConfiguration.getGroupName() != null && !pathConfiguration.isDefaultGroup()) {
            Map<String, PathConfiguration> groups = pathConfiguration.getSecurityConfiguration().getGroups();
            PathConfiguration groupConfiguration = groups.get(pathConfiguration.getGroupName());
            AuthorizationConfiguration groupAuthz = groupConfiguration.getAuthorizationConfiguration();

            if (this.authorizationConfiguration != null && groupAuthz != null) {
                String[] allowedGroups = this.authorizationConfiguration.getAllowedGroups();
                String[] allowedRealms = this.authorizationConfiguration.getAllowedRealms();
                String[] allowedRoles = this.authorizationConfiguration.getAllowedRoles();
                String[] expressions = this.authorizationConfiguration.getExpressions();
                List<Class<? extends PathAuthorizer>> authorizers = this.authorizationConfiguration.getAuthorizers();

                if (allowedGroups == null) {
                    allowedGroups = groupAuthz.getAllowedGroups();
                }

                if (allowedRealms == null) {
                    allowedRealms = groupAuthz.getAllowedRealms();
                }

                if (allowedRoles == null) {
                    allowedRoles = groupAuthz.getAllowedRoles();
                }

                if (expressions == null) {
                    expressions = groupAuthz.getExpressions();
                }

                if (authorizers == null) {
                    authorizers = groupAuthz.getAuthorizers();
                }

                return new AuthorizationConfiguration(this, allowedRoles, allowedGroups, allowedRealms, expressions, authorizers);
            } else if (groupAuthz != null) {
                return groupConfiguration.getAuthorizationConfiguration();
            }
        }

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

    public List<OutboundRedirectConfiguration> getRedirects() {
        if (hasGroup()) {
            List<OutboundRedirectConfiguration> redirects = getGroupConfiguration().getRedirects();

            if (!redirects.isEmpty()) {
                return redirects;
            }
        }

        return this.redirects;
    }

    public String getRedirectUrl(OutboundRedirectConfiguration.Condition condition) {
        String redirectUrl = null;

        if (hasGroup()) {
            redirectUrl = getGroupConfiguration().getRedirectUrl(condition);
        }

        for (OutboundRedirectConfiguration redirectConfiguration : this.redirects) {
            if (condition.equals(redirectConfiguration.getCondition())) {
                return redirectConfiguration.getRedirectUrl();
            }
        }

        return redirectUrl;
    }

    public boolean hasRedirectWhen(OutboundRedirectConfiguration.Condition condition) {
        boolean hasRedirect = false;

        if (hasGroup()) {
            hasRedirect = getGroupConfiguration().hasRedirectWhen(condition);
        }

        for (OutboundRedirectConfiguration redirectConfiguration : this.redirects) {
            if (condition.equals(redirectConfiguration.getCondition())) {
                return true;
            }
        }

        return hasRedirect;
    }

    public Set<HttpMethod> getMethods() {
        return Collections.unmodifiableSet(this.methods);
    }

    @Override
    public String toString() {
        return "UriConfiguration{" +
            "groupName='" + groupName + '\'' +
            ", uri='" + uri + '\'' +
            '}';
    }

}
