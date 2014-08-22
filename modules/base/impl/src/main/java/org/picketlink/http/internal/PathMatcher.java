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
package org.picketlink.http.internal;

import org.picketlink.config.http.InboundHeaderConfiguration;
import org.picketlink.config.http.PathConfiguration;
import org.picketlink.http.HttpMethod;
import org.picketlink.internal.el.ELProcessor;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Pedro Igor
 */
public class PathMatcher {

    private static final String ANY_RESOURCE_PATTERN = "/*";
    private final Map<String, List<PathConfiguration>> uriConfiguration;
    private final ELProcessor elProcessor;

    public PathMatcher(Map<String, List<PathConfiguration>> uriConfiguration, ELProcessor elProcessor) {
        this.uriConfiguration = uriConfiguration;
        this.elProcessor = elProcessor;
    }

    public PathConfiguration matches(HttpServletRequest request) {
        String requestedUri = request.getRequestURI();
        int contextPathIndex = requestedUri.indexOf(request.getContextPath());

        if (contextPathIndex != -1) {
            requestedUri = requestedUri.substring(contextPathIndex + request.getContextPath().length());
        }

        List<PathConfiguration> configurations = null;
        String actualConfig = null;

        for (Map.Entry<String, List<PathConfiguration>> entry : this.uriConfiguration.entrySet()) {
            String protectedUri = entry.getKey();
            String selectedUri = null;

            if (protectedUri.equals(ANY_RESOURCE_PATTERN) && actualConfig == null) {
                configurations = this.uriConfiguration.get(entry.getKey());
                selectedUri = protectedUri;
            }

            int suffixIndex = protectedUri.indexOf(ANY_RESOURCE_PATTERN + ".");

            if (suffixIndex != -1) {
                String protectedSuffix = protectedUri.substring(suffixIndex + ANY_RESOURCE_PATTERN.length());

                if (requestedUri.endsWith(protectedSuffix)) {
                    configurations = this.uriConfiguration.get(entry.getKey());
                    selectedUri = protectedUri;
                }
            }

            if (protectedUri.equals(requestedUri)) {
                configurations = this.uriConfiguration.get(entry.getKey());
                selectedUri = protectedUri;
            }

            if (protectedUri.endsWith(ANY_RESOURCE_PATTERN)) {
                String formattedPattern = removeWildCardsFromUri(protectedUri);

                if (!formattedPattern.equals("/") && requestedUri.startsWith(formattedPattern)) {
                    configurations = this.uriConfiguration.get(entry.getKey());
                    selectedUri = protectedUri;
                }

                if (!formattedPattern.equals("/") && formattedPattern.endsWith("/") && formattedPattern.substring(0, formattedPattern.length() - 1).equals(requestedUri)) {
                    configurations = this.uriConfiguration.get(entry.getKey());
                    selectedUri = protectedUri;
                }
            }

            int startRegex = protectedUri.indexOf('{');

            if (startRegex != -1) {
                String prefix = protectedUri.substring(0, startRegex);

                if (requestedUri.startsWith(prefix)) {
                    configurations = this.uriConfiguration.get(entry.getKey());
                    selectedUri = protectedUri;
                }
            }

            if (selectedUri != null) {
                configurations = this.uriConfiguration.get(entry.getKey());
                selectedUri = protectedUri;
            }

            if (selectedUri != null) {
                if (actualConfig == null) {
                    actualConfig = entry.getKey();
                } else {
                    if (actualConfig.equals(ANY_RESOURCE_PATTERN)) {
                        actualConfig = entry.getKey();
                    }

                    if (protectedUri.startsWith(removeWildCardsFromUri(actualConfig))) {
                        actualConfig = entry.getKey();
                    }
                }
            }
        }

        if (configurations != null) {
            if (configurations.size() == 1) {
                return configurations.get(0);
            }

            int configIndex = -1;
            int lastMatchCount = 0;

            for (int i = 0; i < configurations.size(); i++) {
                PathConfiguration pathConfiguration = configurations.get(i);
                InboundHeaderConfiguration headerConfiguration = pathConfiguration.getInboundHeaderConfiguration();
                Set<HttpMethod> methods = pathConfiguration.getMethods();

                if (!methods.contains(HttpMethod.valueOf(request.getMethod().toUpperCase()))) {
                    continue;
                }

                if (headerConfiguration == null) {
                    if (configIndex == -1) {
                        configIndex = i;
                    }
                } else {
                    Map<String, String[]> inboundHeaders = headerConfiguration.getHeaders();

                    if (inboundHeaders.isEmpty()) {
                        configIndex = i;
                    } else {
                        for (String inboundHeaderName : inboundHeaders.keySet()) {
                            Enumeration<String> requestHeaderValues = request.getHeaders(inboundHeaderName);

                            if (requestHeaderValues == null) {
                                break;
                            }

                            List<String> inboundHeaderValues = Arrays.asList(inboundHeaders.get(inboundHeaderName));
                            int matchCount = 0;

                            while (requestHeaderValues.hasMoreElements()) {
                                String requestHeaderValue = requestHeaderValues.nextElement();

                                if (inboundHeaderValues.contains(requestHeaderValue)) {
                                    matchCount++;
                                }
                            }

                            if (matchCount > lastMatchCount) {
                                lastMatchCount = matchCount;
                                configIndex = i;
                            }
                        }
                    }
                }
            }

            if (configIndex >= 0) {
                return configurations.get(configIndex);
            }
        }

        return null;
    }

    private String removeWildCardsFromUri(String protectedUri) {
        return protectedUri.replaceAll("/[*]", "/");
    }
}
