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

/**
 * <p>Provides a set of options to configure authorization for a specific path.</p>
 *
 * @author Pedro Igor
 */
public class AuthorizationConfigurationBuilder extends AbstractPathConfigurationChildBuilder {

    private String[] rolesAllowed;
    private String[] groupsAllowed;
    private String[] realmsAllowed;
    private String[] elExpresion;

    AuthorizationConfigurationBuilder(PathConfigurationBuilder parentBuilder) {
        super(parentBuilder);
    }

    /**
     * <p>Specifies one or more roles that must be enforced when sending requests to a specific path.</p>
     *
     * <p>Only users with all the specified roles are allowed to access.</p>
     *
     * @param roleNames
     * @return
     */
    public AuthorizationConfigurationBuilder role(String... roleNames) {
        this.rolesAllowed = roleNames;
        return this;
    }

    /**
     * <p>Specifies one or more groups that must be enforced when sending requests to a specific path.</p>
     *
     * <p>Only users members of all the specified groups are allowed to access.</p>
     *
     * @param groupNames
     * @return
     */
    public AuthorizationConfigurationBuilder group(String... groupNames) {
        this.groupsAllowed = groupNames;
        return this;
    }

    /**
     * <p>Specifies one or more realms that must be enforced when sending requests to a specific path.</p>
     *
     * <p>Only users that belong to any of the given realms are allowed to access.</p>
     *
     * @param realmNames
     * @return
     */
    public AuthorizationConfigurationBuilder realm(String... realmNames) {
        this.realmsAllowed = realmNames;
        return this;
    }

    /**
     * <p>Specified an EL Expression that will be used to enforce authorization when sending requests to a specific path.</p>
     *
     * @param expression An EL Expression. Expressions must always evaluate to a boolean. When defining expressions some important components are available for use
     * such as the {@link org.picketlink.Identity} instance. For instance, #{identity.isLoggedIn()}.
     * @return
     */
    public AuthorizationConfigurationBuilder expression(String... expression) {
        this.elExpresion = expression;
        return this;
    }

    AuthorizationConfiguration create(PathConfiguration pathConfiguration) {
        return new AuthorizationConfiguration(pathConfiguration, this.rolesAllowed, this.groupsAllowed, this.realmsAllowed, this.elExpresion);
    }
}