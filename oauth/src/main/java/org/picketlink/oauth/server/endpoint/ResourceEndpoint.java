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

import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.picketlink.oauth.amber.oauth2.as.response.OAuthASResponse;
import org.picketlink.oauth.amber.oauth2.common.error.OAuthError;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthProblemException;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthSystemException;
import org.picketlink.oauth.amber.oauth2.common.message.OAuthResponse;
import org.picketlink.oauth.amber.oauth2.common.message.types.ParameterStyle;
import org.picketlink.oauth.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.picketlink.oauth.server.util.OAuthServerUtil;

/**
 * OAuth2 Resource Endpoint
 *
 * @author anil saldhana
 * @since Aug 27, 2012
 */
@Path("/resource")
public class ResourceEndpoint extends BaseEndpoint {
    private static final long serialVersionUID = 1L;

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/html")
    public Response authorize(@Context HttpServletRequest request) throws URISyntaxException, OAuthSystemException {
        super.setup();

        OAuthAccessResourceRequest oauthRequest = null;
        try {
            oauthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.BODY);
        } catch (OAuthProblemException ope) {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("accessToken not found")
                    .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }

        // Get the access token
        String accessToken = oauthRequest.getAccessToken();
        boolean validateAccessToken = OAuthServerUtil.validateAccessToken(accessToken, identityManager);

        // TODO: Deal with scope
        if (validateAccessToken) {
            return Response.ok().entity("I am a Resource").build();
        } else {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("accessToken not valid")
                    .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }

    }
}