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

import org.picketlink.idm.credential.internal.Password;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
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
                User user = new SimpleUser(clientName);
                user.setFirstName(clientName);
                user.setLastName(" ");

                user.setAttribute(new Attribute("url", clientURL));

                user.setAttribute(new Attribute("description", clientDescription));
                user.setAttribute(new Attribute("redirectURI", clientRedirectURI));
                user.setAttribute(new Attribute("clientID", generatedClientID));

                identityManager.add(user);

                identityManager.updateCredential(user, new Password(generatedSecret.toCharArray()));

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