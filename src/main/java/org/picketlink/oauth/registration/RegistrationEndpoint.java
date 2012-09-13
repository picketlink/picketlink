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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.ext.dynamicreg.server.request.JSONHttpServletRequestWrapper;
import org.apache.amber.oauth2.ext.dynamicreg.server.request.OAuthServerRegistrationRequest;
import org.apache.amber.oauth2.ext.dynamicreg.server.response.OAuthServerRegistrationResponse;
import org.jboss.picketlink.idm.IdentityManager;
import org.jboss.picketlink.idm.internal.DefaultIdentityManager;
import org.jboss.picketlink.idm.internal.LDAPIdentityStore;
import org.jboss.picketlink.idm.internal.config.LDAPConfiguration;
import org.jboss.picketlink.idm.model.User;

/**
 * Endpoint used in registration of OAuth Client Applications
 *
 * @author anil saldhana
 * @since Aug 28, 2012
 */
// public class RegistrationEndpoint extends HttpServlet {
@Path("/register")
public class RegistrationEndpoint implements Serializable {
    private static final long serialVersionUID = 1L;

    protected IdentityManager identityManager = null;

    @Context
    protected ServletContext context;

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response register(@Context HttpServletRequest request) throws OAuthSystemException {
        try {
            handleIdentityManager();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        OAuthServerRegistrationRequest oauthRequest = null;
        try {
            oauthRequest = new OAuthServerRegistrationRequest(new JSONHttpServletRequestWrapper(request));
            oauthRequest.discover();
            String clientName = oauthRequest.getClientName();
            String clientURL = oauthRequest.getClientUrl();
            String clientDescription = oauthRequest.getClientDescription();
            String clientRedirectURI = oauthRequest.getRedirectURI();

            String generatedClientID = generateClientID(); // TODO: store in DB
            String generatedSecret = generateClientSecret();

            User user = identityManager.createUser(clientName);
            user.setAttribute("url", clientURL);

            user.setAttribute("description", clientDescription);
            user.setAttribute("redirectURI", clientRedirectURI);
            user.setAttribute("clientID", generatedClientID);
            user.setAttribute("clientSecret", generatedSecret);

            OAuthResponse response = OAuthServerRegistrationResponse.status(HttpServletResponse.SC_OK)
                    .setClientId(generatedClientID).setClientSecret(generatedSecret).setIssuedAt(getCurrentTime() + "")
                    .setExpiresIn("3600").buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();

        } catch (OAuthProblemException e) {
            OAuthResponse response = OAuthServerRegistrationResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                    .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }
    }

    private void handleIdentityManager() throws IOException {
        if (identityManager == null) {
            if (context == null) {
                throw new RuntimeException("Servlet Context has not been injected");
            }
            identityManager = new DefaultIdentityManager();
            String storeType = context.getInitParameter("storeType");
            if (storeType == null || "ldap".equalsIgnoreCase(storeType)) {
                LDAPIdentityStore store = new LDAPIdentityStore();
                LDAPConfiguration ldapConfiguration = new LDAPConfiguration();

                Properties properties = getProperties();
                ldapConfiguration.setBindDN(properties.getProperty("bindDN")).setBindCredential(
                        properties.getProperty("bindCredential"));
                ldapConfiguration.setLdapURL(properties.getProperty("ldapURL"));
                ldapConfiguration.setUserDNSuffix(properties.getProperty("userDNSuffix")).setRoleDNSuffix(
                        properties.getProperty("roleDNSuffix"));
                ldapConfiguration.setGroupDNSuffix(properties.getProperty("groupDNSuffix"));

                store.setConfiguration(ldapConfiguration);

                ((DefaultIdentityManager) identityManager).setIdentityStore(store);
            }
        }
    }

    private Properties getProperties() throws IOException {
        Properties properties = new Properties();
        InputStream is = context.getResourceAsStream("/WEB-INF/idm.properties");
        properties.load(is);
        return properties;
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