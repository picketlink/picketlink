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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * <p>Represents the global configuration options for HTTP security.</p>
 *
 * @author Pedro Igor
 */
public class HttpSecurityConfiguration {

    private final Map<String, List<PathConfiguration>> uriConfiguration = new LinkedHashMap<String, List<PathConfiguration>>();
    private final Map<String, PathConfiguration> groupConfiguration = new LinkedHashMap<String, PathConfiguration>();
    private final FilteringMode filteringMode;

    public HttpSecurityConfiguration(List<PathConfiguration> uriConfigs, FilteringMode filteringMode) {
        for (PathConfiguration configuration : uriConfigs) {
            configuration.setSecurityConfiguration(this);

            if (configuration.isGroup()) {
                String groupName = configuration.getGroupName();

                if (this.groupConfiguration.containsKey(groupName)) {
                    throw new HttpSecurityConfigurationException("Duplicated Group[" + groupName + "] configuration.");
                }

                this.groupConfiguration.put(groupName, configuration);
            } else {
                String uri = configuration.getUri();
                List<PathConfiguration> configurations = this.uriConfiguration.get(uri);

                if (configurations == null) {
                    configurations = new ArrayList<PathConfiguration>();
                    this.uriConfiguration.put(uri, configurations);
                }

                configurations.add(configuration);
            }
        }

        validate();

        if (filteringMode == null) {
            filteringMode = FilteringMode.PERMISSIVE;
        }

        this.filteringMode = filteringMode;
    }

    public Map<String, List<PathConfiguration>> getPaths() {
        return unmodifiableMap(this.uriConfiguration);
    }

    public Map<String, PathConfiguration> getGroups() {
        return unmodifiableMap(this.groupConfiguration);
    }

    private void validate() {
        if (this.uriConfiguration.isEmpty()) {
            throw new HttpSecurityConfigurationException("No URI configuration is defined. You must provide at least one URI to protect.");
        }

        for (List<PathConfiguration> configurations : this.uriConfiguration.values()) {
            for (PathConfiguration configuration : configurations) {
                if (!configuration.isGroup()) {
                    String groupName = configuration.getGroupName();

                    if (!this.groupConfiguration.containsKey(groupName) && !configuration.isDefaultGroup()) {
                        throw new HttpSecurityConfigurationException("Group [" + groupName + "] for URI [" + configuration
                            .getUri() + "] is not defined.");
                    }
                }

                if (configuration.isSecured() && ((configuration == null)
                    || ((configuration != null && configuration.getAuthenticationConfiguration() == null)
                    && (configuration == null && configuration.getAuthorizationConfiguration() == null)
                    && (configuration == null && configuration.getLogoutConfiguration() == null)
                    && (configuration == null && configuration.getMethods() == null || configuration.getMethods().isEmpty())
                    && (configuration.getRedirects().isEmpty())))) {
                    throw new HttpSecurityConfigurationException("You must provide an authentication, authorization or logout configuration for URI [" + configuration.getUri() + "].");
                }
            }
        }
    }

    public boolean isPermissive() {
        return FilteringMode.PERMISSIVE.equals(this.filteringMode);
    }
}