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

import org.picketlink.Identity;
import org.picketlink.common.properties.Property;
import org.picketlink.common.properties.query.AnnotatedPropertyCriteria;
import org.picketlink.common.properties.query.PropertyQueries;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.model.annotation.StereotypeProperty;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.security.Principal;
import java.util.List;

import static org.picketlink.authorization.util.AuthorizationUtil.hasRole;

/**
 * <p>PicketLink wrapper to {@link javax.servlet.http.HttpServletRequest} instances.</p>
 *
 * <p>This class provides a better integration with Servlet API.</p>
 *
 * @author Pedro Igor
 */
public class PicketLinkHttpServletRequest extends HttpServletRequestWrapper {

    private final Identity identity;
    private DefaultLoginCredentials credentials;
    private final PartitionManager partitionManager;
    private final String requestedUri;

    /**
     * Constructs a request object wrapping the given request.
     *
     *
     * @param requestedUri
     * @param request
     *
     * @throws IllegalArgumentException if the request is null
     */
    public PicketLinkHttpServletRequest(String requestedUri, Identity identity, DefaultLoginCredentials credentials, PartitionManager partitionManager, HttpServletRequest request) {
        super(request);
        this.requestedUri = requestedUri;
        this.identity = identity;
        this.credentials = credentials;
        this.partitionManager = partitionManager;
    }

    @Override
    public void login(String username, String password) throws ServletException {
        this.credentials.setUserId(username);
        this.credentials.setPassword(password);
        this.identity.login();
    }

    @Override
    public void logout() throws ServletException {
        if (isLoggedIn()) {
            this.identity.logout();
        }
        super.logout();
    }

    @Override
    public Principal getUserPrincipal() {
        if (!isLoggedIn()) {
            return null;
        }

        return new Principal() {
            @Override
            public String getName() {
                List<Property<Object>> result = PropertyQueries.createQuery(identity.getAccount().getClass())
                    .addCriteria(new AnnotatedPropertyCriteria(StereotypeProperty.class))
                    .getResultList();

                for (Property stereotypeProperty : result) {
                    StereotypeProperty stereotypePropertyAnnotation = stereotypeProperty.getAnnotatedElement()
                        .getAnnotation(StereotypeProperty.class);

                    if (StereotypeProperty.Property.IDENTITY_USER_NAME.equals(stereotypePropertyAnnotation.value())) {
                        Object userName = stereotypeProperty.getValue(identity.getAccount());

                        if (userName != null) {
                            return userName.toString();
                        }
                    }
                }

                return null;
            }
        };
    }

    @Override
    public String getRequestURI() {
        if (this.requestedUri == null) {
            return super.getRequestURI();
        }

        return this.requestedUri;
    }

    @Override
    public String getServletPath() {
        if (this.requestedUri == null) {
            return super.getServletPath();
        }

        return this.requestedUri.substring(this.requestedUri.indexOf(getContextPath()) + getContextPath().length());
    }

    @Override
    public boolean isUserInRole(String roleName) {
        if (isLoggedIn()) {
            return hasRole(this.identity, this.partitionManager, roleName);
        }

        return super.isUserInRole(roleName);
    }

    private boolean isLoggedIn() {
        return this.identity.isLoggedIn();
    }
}
