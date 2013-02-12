/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.oauth.server.endpoint;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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
public class AuthorizationEndpoint extends BaseEndpoint {
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(AuthorizationEndpoint.class.getName());

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