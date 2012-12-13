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
package org.picketlink.oauth.server.endpoint;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.picketlink.idm.IdentityManager;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthSystemException;
import org.picketlink.oauth.amber.oauth2.common.message.OAuthResponse;
import org.picketlink.oauth.server.util.OAuthServerUtil;

/**
 * OAuth2 Authorization Endpoint
 *
 * @author anil saldhana
 * @since Aug 27, 2012
 */
@Path("/authz")
public class AuthorizationEndpoint implements Serializable {
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(AuthorizationEndpoint.class.getName());

    protected IdentityManager identityManager = null;

    @Context
    protected ServletContext context;

    @GET
    public Response authorize(@Context HttpServletRequest request) {
        if (context == null) {
            throw new RuntimeException("Servlet Context has not been injected");
        }
        if (identityManager == null) {
            try {
                identityManager = OAuthServerUtil.handleIdentityManager(context);
            } catch (IOException e) {
                log.log(Level.SEVERE, "Identity Manager setup:", e);
                throw new RuntimeException(e);
            }
            if (identityManager == null) {
                throw new RuntimeException("Identity Manager has not been created");
            }
        }

        OAuthResponse response = null;
        try {
            response = OAuthServerUtil.authorizationCodeRequest(request, identityManager);
        } catch (OAuthSystemException e) {
            log.log(Level.SEVERE, "OAuth Server Authorization Processing:", e);
            return Response.serverError().build();
        }
        // Check for successful processing
        String location = response.getLocationUri();
        if (location != null && location.isEmpty() == false) {
            return Response.status(response.getResponseStatus()).location(URI.create(location)).build();
        } else { // We have error
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }
    }
}