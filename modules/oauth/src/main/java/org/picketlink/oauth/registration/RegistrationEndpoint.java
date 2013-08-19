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
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.Agent;
import org.picketlink.oauth.messages.RegistrationRequest;
import org.picketlink.oauth.messages.RegistrationResponse;
import org.picketlink.oauth.server.endpoint.BaseEndpoint;
import org.picketlink.oauth.server.util.OAuthServerUtil;

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
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response registerAsForm(@Context HttpServletRequest request) {
        super.setup();
        try {
            RegistrationRequest registrationRequest = OAuthServerUtil.parseRegistrationRequestWithFORM(request);

            String clientName = registrationRequest.getClientName();
            String clientURL = registrationRequest.getClientURL();
            String clientDescription = registrationRequest.getClientDescription();
            String clientRedirectURI = registrationRequest.getClientRedirectURI();

            String generatedClientID = generateClientID();
            String generatedSecret = generateClientSecret();

            // User user = identityManager.createUser(clientName);

            Agent oauthApp = new Agent(clientName);

            oauthApp.setAttribute(new Attribute<String>("appURL", clientURL));
            oauthApp.setAttribute(new Attribute<String>("appDesc", clientDescription));
            oauthApp.setAttribute(new Attribute<String>("redirectURI", clientRedirectURI));
            oauthApp.setAttribute(new Attribute<String>("clientID", generatedClientID));
            oauthApp.setAttribute(new Attribute<String>("clientSecret", generatedSecret));

            identityManager.add(oauthApp);

            RegistrationResponse response = new RegistrationResponse();
            response.setStatusCode(HttpServletResponse.SC_OK);
            response.setClientID(generatedClientID).setClientSecret(generatedSecret).setExpiresIn(3600L)
                    .setIssued(getCurrentTime() + "");

            return Response.status(response.getStatusCode()).entity(response.asJSON()).build();
        } catch (Exception e) {
            log.log(Level.SEVERE, "OAuth Server Registration Processing:", e);
            return Response.serverError().build();
        }
    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response register(@Context HttpServletRequest request) {
        super.setup();

        try {
            RegistrationRequest registrationRequest = OAuthServerUtil.parseRegistrationRequestWithJSON(request);

            String clientName = registrationRequest.getClientName();
            String clientURL = registrationRequest.getClientURL();
            String clientDescription = registrationRequest.getClientDescription();
            String clientRedirectURI = registrationRequest.getClientRedirectURI();

            String generatedClientID = generateClientID();
            String generatedSecret = generateClientSecret();

            // User user = identityManager.createUser(clientName);

            Agent oauthApp = new Agent(clientName);

            oauthApp.setAttribute(new Attribute<String>("appURL", clientURL));
            oauthApp.setAttribute(new Attribute<String>("appDesc", clientDescription));
            oauthApp.setAttribute(new Attribute<String>("redirectURI", clientRedirectURI));
            oauthApp.setAttribute(new Attribute<String>("clientID", generatedClientID));
            oauthApp.setAttribute(new Attribute<String>("clientSecret", generatedSecret));

            identityManager.add(oauthApp);

            RegistrationResponse response = new RegistrationResponse();
            response.setStatusCode(HttpServletResponse.SC_OK);
            response.setClientID(generatedClientID).setClientSecret(generatedSecret).setExpiresIn(3600L)
                    .setIssued(getCurrentTime() + "");

            return Response.status(response.getStatusCode()).entity(response.asJSON()).build();
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