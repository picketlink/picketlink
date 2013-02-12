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
package org.picketlink.oauth.registration;

import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.SimpleAgent;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthProblemException;
import org.picketlink.oauth.amber.oauth2.common.message.OAuthResponse;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.server.request.JSONHttpServletRequestWrapper;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.server.request.OAuthServerRegistrationRequest;
import org.picketlink.oauth.amber.oauth2.ext.dynamicreg.server.response.OAuthServerRegistrationResponse;
import org.picketlink.oauth.server.endpoint.BaseEndpoint;

/**
 * Endpoint used in registration of OAuth Client Applications
 *
 * @author anil saldhana
 * @since Aug 28, 2012
 */
@Path("/register")
public class RegistrationEndpoint extends BaseEndpoint {
    private static final long serialVersionUID = 1L;
    private static Logger log = Logger.getLogger(RegistrationEndpoint.class.getName());

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response register(@Context HttpServletRequest request) {
        super.setup();

        try {
            OAuthServerRegistrationRequest oauthRequest = null;
            try {
                oauthRequest = new OAuthServerRegistrationRequest(new JSONHttpServletRequestWrapper(request));
                oauthRequest.discover();
                String clientName = oauthRequest.getClientName();
                String clientURL = oauthRequest.getClientUrl();
                String clientDescription = oauthRequest.getClientDescription();
                String clientRedirectURI = oauthRequest.getRedirectURI();

                String generatedClientID = generateClientID();
                String generatedSecret = generateClientSecret();

                // User user = identityManager.createUser(clientName);

                Agent oauthApp = new SimpleAgent(clientName);

                oauthApp.setAttribute(new Attribute<String>("appURL", clientURL));
                oauthApp.setAttribute(new Attribute<String>("appDesc", clientDescription));
                oauthApp.setAttribute(new Attribute<String>("redirectURI", clientRedirectURI));
                oauthApp.setAttribute(new Attribute<String>("clientID", generatedClientID));
                oauthApp.setAttribute(new Attribute<String>("clientSecret", generatedSecret));

                identityManager.add(oauthApp);

                /*
                 * User user = new SimpleUser(clientName); user.setFirstName(clientName); user.setLastName(" ");
                 *
                 * user.setAttribute(new Attribute("url", clientURL));
                 *
                 * user.setAttribute(new Attribute("description", clientDescription)); user.setAttribute(new
                 * Attribute("redirectURI", clientRedirectURI)); user.setAttribute(new Attribute("clientID",
                 * generatedClientID));
                 *
                 * identityManager.add(user);
                 */

                // identityManager.updateCredential(oauthApp, new Password(generatedSecret.toCharArray()));

                OAuthResponse response = OAuthServerRegistrationResponse.status(HttpServletResponse.SC_OK)
                        .setClientId(generatedClientID).setClientSecret(generatedSecret).setIssuedAt(getCurrentTime() + "")
                        .setExpiresIn("3600").buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

            } catch (OAuthProblemException e) {
                e.printStackTrace();
                OAuthResponse response = OAuthServerRegistrationResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .error(e).buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "OAuth Server Registration Processing:", e);
            return Response.serverError().build();
        }
    }

    private String generateClientID() {
        return UUID.randomUUID().toString();
    }

    private String generateClientSecret() {
        StringBuilder sb = new StringBuilder();
        Date date = new Date();
        sb.append(date.getTime());
        return sb.toString();
    }

    private long getCurrentTime() {
        Date date = new Date();
        return date.getTime();
    }
}