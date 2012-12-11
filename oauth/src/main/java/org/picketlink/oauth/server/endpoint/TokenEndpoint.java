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
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.picketlink.oauth.amber.oauth2.as.issuer.MD5Generator;
import org.picketlink.oauth.amber.oauth2.as.issuer.OAuthIssuer;
import org.picketlink.oauth.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.picketlink.oauth.amber.oauth2.as.request.OAuthTokenRequest;
import org.picketlink.oauth.amber.oauth2.as.response.OAuthASResponse;
import org.picketlink.oauth.amber.oauth2.common.OAuth;
import org.picketlink.oauth.amber.oauth2.common.error.OAuthError;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthProblemException;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthSystemException;
import org.picketlink.oauth.amber.oauth2.common.message.OAuthResponse;
import org.picketlink.oauth.amber.oauth2.common.message.types.GrantType;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.credential.PlainTextPassword;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;

/**
 * Token End Point
 *
 * @author anil saldhana
 * @since Aug 27, 2012
 */
@Path("/token")
public class TokenEndpoint implements Serializable {

    private static final long serialVersionUID = 1L;

    protected IdentityManager identityManager = null;

    @Context
    protected ServletContext context;

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {
        try {
            handleIdentityManager();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

        OAuthTokenRequest oauthRequest = null;

        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        try {
            oauthRequest = new OAuthTokenRequest(request);

            String passedClientID = oauthRequest.getClientId();
            String passedClientSecret = oauthRequest.getClientSecret();

            if (passedClientID == null) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id is null")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            if (passedClientSecret == null) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_secret is null")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            IdentityQuery<User> userQuery = identityManager.createQuery(User.class);
            userQuery.setParameter(IdentityType.ATTRIBUTE.byName("clientID"), passedClientID);
            /*
             * UserQuery userQuery = identityManager.createUserQuery().setAttributeFilter("clientID", new String[] {
             * passedClientID });
             */

            List<User> users = userQuery.getResultList();

            if (users.size() == 0) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            if (users.size() > 1) {
                throw new RuntimeException("More than one user with the same client id");
            }

            User clientApp = users.get(0);

            // Get the values from DB
            Attribute<String> clientIDAttr = clientApp.getAttribute("clientID");
            String clientID = clientIDAttr.getValue();
            Attribute<String> authorizationCodeAttr = clientApp.getAttribute("authorizationCode");
            if (authorizationCodeAttr == null) {

                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("authorization code is null")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }
            String authorizationCode = authorizationCodeAttr.getValue();

            String password = "something";
            String username = "yz";

            // check if clientid is valid
            if (!clientID.equals(passedClientID)) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            // Validate client secret
            UsernamePasswordCredentials upc = new UsernamePasswordCredentials();
            upc.setUsername(clientApp.getId());
            upc.setPassword(new PlainTextPassword(passedClientSecret.toCharArray()));

            try {
                identityManager.validateCredentials(upc);
            } catch (SecurityException se) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("Client secret mismatch")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }
            /*
             * if (identityManager.validateCredentials(upc)) == false) { OAuthResponse response =
             * OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
             * .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("Client secret mismatch")
             * .buildJSONMessage(); return Response.status(response.getResponseStatus()).entity(response.getBody()).build(); }
             */

            // do checking for different grant types
            if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.AUTHORIZATION_CODE.toString())) {
                if (!authorizationCode.equals(oauthRequest.getParam(OAuth.OAUTH_CODE))) {
                    OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid authorization code")
                            .buildJSONMessage();
                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.PASSWORD.toString())) {
                if (!password.equals(oauthRequest.getPassword()) || !username.equals(oauthRequest.getUsername())) {
                    OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription("invalid username or password").buildJSONMessage();
                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.REFRESH_TOKEN.toString())) {
                OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid username or password")
                        .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            String accessToken = oauthIssuerImpl.accessToken();
            clientApp.setAttribute(new Attribute("accessToken", accessToken));

            identityManager.update(clientApp);

            OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK).setAccessToken(accessToken)
                    .setExpiresIn("3600").buildJSONMessage();

            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        } catch (OAuthProblemException e) {
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e).buildJSONMessage();
            return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
        }
    }

    @GET
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorizeGet(@Context HttpServletRequest request) throws OAuthSystemException {
        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
                .setAccessToken(oauthIssuerImpl.accessToken()).setExpiresIn("3600").buildJSONMessage();

        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
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

                // store.setConfiguration(ldapConfiguration);
                // Create Identity Configuration
                IdentityConfiguration config = new IdentityConfiguration();
                config.addStoreConfiguration(ldapConfiguration);

                identityManager.bootstrap(config, DefaultIdentityStoreInvocationContextFactory.DEFAULT);

                // ((DefaultIdentityManager) identityManager).setIdentityStore(store);
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