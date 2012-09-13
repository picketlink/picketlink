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
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.as.request.OAuthAuthzRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.jboss.picketlink.idm.IdentityManager;
import org.jboss.picketlink.idm.internal.DefaultIdentityManager;
import org.jboss.picketlink.idm.internal.LDAPIdentityStore;
import org.jboss.picketlink.idm.internal.config.LDAPConfiguration;
import org.jboss.picketlink.idm.model.User;
import org.jboss.picketlink.idm.query.UserQuery;

/**
 * OAuth2 Authorization Endpoint
 *
 * @author anil saldhana
 * @since Aug 27, 2012
 */
// public class AuthorizationEndpoint extends HttpServlet {
@Path("/authz")
public class AuthorizationEndpoint implements Serializable {
    private static final long serialVersionUID = 1L;

    protected IdentityManager identityManager = null;

    @Context
    protected ServletContext context;

    @GET
    public Response authorize(@Context HttpServletRequest request) throws URISyntaxException, OAuthSystemException {
        try {
            handleIdentityManager();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        OAuthAuthzRequest oauthRequest = null;

        OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        try {
            oauthRequest = new OAuthAuthzRequest(request);

            String passedClientID = oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID);
            UserQuery userQuery = identityManager.createUserQuery().setAttributeFilter("clientID",
                    new String[] { passedClientID });
            List<User> users = userQuery.executeQuery();
            if (users.size() == 0) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }
            if (users.size() > 1) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("Multiple client_id found")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            User clientApp = users.get(0);
            String clientID = clientApp.getAttribute("clientID");

            // check if clientid is valid
            if (!clientID.equals(passedClientID)) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            // build response according to response_type
            String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

            OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse.authorizationResponse(request,
                    HttpServletResponse.SC_FOUND);

            if (responseType.equals(ResponseType.CODE.toString())) {
                String authorizationCode = oauthIssuerImpl.authorizationCode();
                clientApp.setAttribute("authorizationCode", authorizationCode);
                builder.setCode(authorizationCode);
            }
            /*
             * if (responseType.equals(ResponseType.TOKEN.toString())) { builder.setAccessToken(oauthIssuerImpl.accessToken());
             * builder.setExpiresIn(3600L); }
             */

            String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);

            final OAuthResponse response = builder.location(redirectURI).buildQueryMessage();
            URI url = new URI(response.getLocationUri());

            return Response.status(response.getResponseStatus()).location(url).build();

        } catch (OAuthProblemException e) {

            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);

            String redirectUri = e.getRedirectUri();

            if (OAuthUtils.isEmpty(redirectUri)) {
                throw new WebApplicationException(responseBuilder
                        .entity("OAuth callback url needs to be provided by client!!!").build());
            }
            final OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
                    .location(redirectUri).buildQueryMessage();
            final URI location = new URI(response.getLocationUri());
            return responseBuilder.location(location).build();
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
}