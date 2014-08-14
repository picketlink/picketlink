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

import java.util.Map;

/**
 * @author Pedro Igor
 */
public class PathConfiguration {

    public static final String URI_ALL = "/*";
    public static final String DEFAULT_GROUP_NAME = "Default";

    private final Boolean secured;
    private String groupName = DEFAULT_GROUP_NAME;
    private final String uri;
    private HttpSecurityConfiguration securityConfiguration;
    private InboundConfiguration inboundConfiguration;
    private OutboundConfiguration outboundConfiguration;

    public PathConfiguration(
        String groupName,
        String uri, Boolean secured) {
        if (groupName == null && uri == null) {
            throw new HttpSecurityConfigurationException("You must provide a group name or uri. Or even both.");
        }

        this.groupName = groupName;
        this.uri = uri;
        this.secured = secured;
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

    public InboundConfiguration getInboundConfiguration() {
        if (hasGroup()) {
            PathConfiguration groupConfiguration = getGroupConfiguration();

            if (this.inboundConfiguration != null) {
                InboundConfiguration actualConfig = new InboundConfiguration(this, groupConfiguration.getInboundConfiguration().getMethods());

                if (this.inboundConfiguration.getAuthenticationConfiguration() != null) {
                    actualConfig.setAuthenticationConfiguration(this.inboundConfiguration
                        .getAuthenticationConfiguration());
                }

                if (this.inboundConfiguration.getAuthorizationConfiguration() != null) {
                    actualConfig.setAuthorizationConfiguration(this.inboundConfiguration.getAuthorizationConfiguration());
                }

                if (this.inboundConfiguration.getInboundHeaderConfiguration() != null) {
                    actualConfig.setInboundHeaderConfiguration(this.inboundConfiguration.getInboundHeaderConfiguration());
                }

                if (this.inboundConfiguration.getMethods().size() != groupConfiguration.getInboundConfiguration().getMethods().size()) {
                    actualConfig.setMethods(this.inboundConfiguration.getMethods());
                }

                return actualConfig;
            } else {
                return groupConfiguration.getInboundConfiguration();
            }
        }

        return this.inboundConfiguration;
    }

    private PathConfiguration getGroupConfiguration() {
        Map<String, PathConfiguration> groups = getSecurityConfiguration().getGroups();
        return groups.get(getGroupName());
    }

    private boolean hasGroup() {
        return isUri() && getGroupName() != null && !isDefaultGroup();
    }

    protected void setInboundConfiguration(InboundConfiguration inboundConfiguration) {
        this.inboundConfiguration = inboundConfiguration;
    }

    public OutboundConfiguration getOutboundConfiguration() {
        return this.outboundConfiguration;
    }

    protected void setOutboundConfiguration(OutboundConfiguration outboundConfiguration) {
        this.outboundConfiguration = outboundConfiguration;
    }

    @Override
    public String toString() {
        return "UriConfiguration{" +
            "groupName='" + groupName + '\'' +
            ", uri='" + uri + '\'' +
            '}';
    }
}
