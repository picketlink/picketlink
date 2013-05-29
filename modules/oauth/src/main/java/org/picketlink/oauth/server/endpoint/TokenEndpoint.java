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

import org.picketlink.oauth.messages.OAuthResponse;
import org.picketlink.oauth.server.util.OAuthServerUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Token End Point
 *
 * @author anil saldhana
 * @since Aug 27, 2012
 */
@Path("/token")
public class TokenEndpoint extends BaseEndpoint {
    private static final long serialVersionUID = 1L;
    protected static Logger log = Logger.getLogger(TokenEndpoint.class.getName());

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorize(@Context HttpServletRequest request) {
        super.setup();

        OAuthResponse response = null;
        try {
            response = OAuthServerUtil.tokenRequest(request, identityManager);
        } catch (Exception e) {
            log.log(Level.SEVERE, "OAuth Server Token Processing:", e);
            return Response.serverError().build();
        }
        return Response.status(response.getStatusCode()).entity(response.asJSON()).build();
    }

    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorizeGet(@Context HttpServletRequest request) {
        super.setup();

        OAuthResponse response = null;
        try {
            response = OAuthServerUtil.tokenRequest(request, identityManager);
        } catch (Exception e) {
            log.log(Level.SEVERE, "OAuth Server Token Processing:", e);
            return Response.serverError().build();
        }
        return Response.status(response.getStatusCode()).entity(response.asJSON()).build();
    }
}