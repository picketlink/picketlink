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

import org.picketlink.http.authorization.PathAuthorizer;

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * @author Pedro Igor
 */
public class AuthorizationConfiguration {

    private final String[] allowedRoles;
    private final String[] allowedGroups;
    private final String[] allowedRealms;
    private final String[] expressions;
    private final List<Class<? extends PathAuthorizer>> authorizers;
    private final PathConfiguration pathConfiguration;

    public AuthorizationConfiguration(
        PathConfiguration pathConfiguration,
        String[] allowedRoles,
        String[] allowedGroups,
        String[] allowedRealms,
        String[] expressions,
        List<Class<? extends PathAuthorizer>> authorizers) {
        this.pathConfiguration = pathConfiguration;
        this.allowedRoles = allowedRoles;
        this.allowedGroups = allowedGroups;
        this.allowedRealms = allowedRealms;
        this.expressions = expressions;
        this.authorizers = authorizers;
    }

    public String[] getAllowedRoles() {
        return this.allowedRoles;
    }

    public String[] getAllowedGroups() {
        return this.allowedGroups;
    }

    public String[] getAllowedRealms() {
        return this.allowedRealms;
    }

    public String[] getExpressions() {
        return this.expressions;
    }

    public List<Class<? extends PathAuthorizer>> getAuthorizers() {
        return unmodifiableList(this.authorizers);
    }
}