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
package org.picketlink.oauth.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.SimpleIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.oauth.amber.oauth2.as.issuer.MD5Generator;
import org.picketlink.oauth.amber.oauth2.as.issuer.OAuthIssuer;
import org.picketlink.oauth.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.picketlink.oauth.amber.oauth2.as.request.OAuthAuthzRequest;
import org.picketlink.oauth.amber.oauth2.as.request.OAuthTokenRequest;
import org.picketlink.oauth.amber.oauth2.as.response.OAuthASResponse;
import org.picketlink.oauth.amber.oauth2.common.OAuth;
import org.picketlink.oauth.amber.oauth2.common.error.OAuthError;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthProblemException;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthSystemException;
import org.picketlink.oauth.amber.oauth2.common.message.OAuthResponse;
import org.picketlink.oauth.amber.oauth2.common.message.types.GrantType;
import org.picketlink.oauth.amber.oauth2.common.message.types.ResponseType;

/**
 * Utility
 *
 * @author anil saldhana
 * @since Dec 12, 2012
 */
public class OAuthServerUtil {
    /**
     * Centralize the IDM setup
     *
     * @param context
     * @return
     * @throws IOException
     */
    public static IdentityManager handleIdentityManager(ServletContext context) throws IOException {
        IdentityManager identityManager = null;
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        identityManager = (IdentityManager) context.getAttribute("identityManager");
        if (identityManager == null) {
            // Need to handle IM
            identityManager = new DefaultIdentityManager();
            String storeType = context.getInitParameter("storeType");
            if (storeType == null || "ldap".equalsIgnoreCase(storeType)) {
                LDAPConfiguration ldapConfiguration = new LDAPConfiguration();

                Properties properties = getProperties(context);
                ldapConfiguration.setBindDN(properties.getProperty("bindDN")).setBindCredential(
                        properties.getProperty("bindCredential"));
                ldapConfiguration.setLdapURL(properties.getProperty("ldapURL"));
                ldapConfiguration.setUserDNSuffix(properties.getProperty("userDNSuffix")).setRoleDNSuffix(
                        properties.getProperty("roleDNSuffix"));
                ldapConfiguration.setGroupDNSuffix(properties.getProperty("groupDNSuffix"));
                ldapConfiguration.setAdditionalProperties(properties);

                // Create Identity Configuration
                IdentityConfiguration config = new IdentityConfiguration();
                config.addStoreConfiguration(ldapConfiguration);

                identityManager.bootstrap(config, new SimpleIdentityStoreInvocationContextFactory());
                context.setAttribute("identityManager", identityManager);
            }
        }

        return identityManager;
    }

    /**
     * Handle an Authorization Code Grant Type Request
     *
     * @param request
     * @param identityManager
     * @return
     * @throws OAuthSystemException
     */
    public static OAuthResponse authorizationCodeRequest(HttpServletRequest request, IdentityManager identityManager)
            throws OAuthSystemException {
        OAuthAuthzRequest oauthRequest = null;
        try {
            oauthRequest = new OAuthAuthzRequest(request);

            String passedClientID = oauthRequest.getClientId();

            if (passedClientID == null) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id is null")
                        .buildJSONMessage();
            }
            IdentityQuery<User> userQuery = identityManager.createIdentityQuery(User.class);
            userQuery.setParameter(IdentityType.ATTRIBUTE.byName("clientID"), passedClientID);

            List<User> users = userQuery.getResultList();
            if (users.size() == 0) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
            }
            if (users.size() > 1) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("Multiple client_id found")
                        .buildJSONMessage();
            }

            User clientApp = users.get(0);
            Attribute<String> clientIDAttr = clientApp.getAttribute("clientID");
            String clientID = clientIDAttr.getValue();

            // check if clientid is valid
            if (!clientID.equals(passedClientID)) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
            }

            // build response according to response_type
            String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

            OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse.authorizationResponse(request,
                    HttpServletResponse.SC_FOUND);

            OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
            if (responseType.equals(ResponseType.CODE.toString())) {
                String authorizationCode = oauthIssuerImpl.authorizationCode();

                clientApp.setAttribute(new Attribute<String>("authorizationCode", authorizationCode));
                identityManager.update(clientApp);

                builder.setCode(authorizationCode);
            }

            String redirectURI = oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);

            return builder.location(redirectURI).buildQueryMessage();
        } catch (Exception e) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                    .buildJSONMessage();
        }
    }

    /**
     * Handle Token Request
     *
     * @param request
     * @param identityManager
     * @return
     * @throws OAuthSystemException
     */
    public static OAuthResponse tokenRequest(HttpServletRequest request, IdentityManager identityManager)
            throws OAuthSystemException {
        OAuthTokenRequest oauthRequest = null;

        try {
            oauthRequest = new OAuthTokenRequest(request);

            String passedClientID = oauthRequest.getClientId();
            String passedClientSecret = oauthRequest.getClientSecret();
            Set<String> scopes = oauthRequest.getScopes();

            if (passedClientID == null) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id is null")
                        .buildJSONMessage();
            }

            if (passedClientSecret == null) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_secret is null")
                        .buildJSONMessage();
            }

            IdentityQuery<User> userQuery = identityManager.createIdentityQuery(User.class);
            userQuery.setParameter(IdentityType.ATTRIBUTE.byName("clientID"), passedClientID);

            List<User> users = userQuery.getResultList();

            if (users.size() == 0) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
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
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("authorization code is null")
                        .buildJSONMessage();
            }
            String authorizationCode = authorizationCodeAttr.getValue();

            String username = oauthRequest.getUsername();
            String password = oauthRequest.getPassword();

            // check if clientid is valid
            if (!clientID.equals(passedClientID)) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
            }

            // Validate client secret
            UsernamePasswordCredentials upc = new UsernamePasswordCredentials();
            upc.setUsername(clientApp.getId());
            upc.setPassword(new Password(passedClientSecret.toCharArray()));

            try {
                identityManager.validateCredentials(upc);
            } catch (SecurityException se) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("Client secret mismatch")
                        .buildJSONMessage();
            }

            // do checking for different grant types
            if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.AUTHORIZATION_CODE.toString())) {
                if (!authorizationCode.equals(oauthRequest.getParam(OAuth.OAUTH_CODE))) {
                    return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("invalid authorization code")
                            .buildJSONMessage();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.PASSWORD.toString())) {
                UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials();
                usernamePasswordCredentials.setUsername(username);
                usernamePasswordCredentials.setPassword(new Password(password.toCharArray()));
                try{
                    identityManager.validateCredentials(usernamePasswordCredentials);
                }catch(Exception e){
                    return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription("invalid username or password").buildJSONMessage();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.REFRESH_TOKEN.toString())) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT).setErrorDescription("Refresh Token not yet supported")
                        .buildJSONMessage();
            }

            OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
            String accessToken = oauthIssuerImpl.accessToken();
            clientApp.setAttribute(new Attribute<String>("accessToken", accessToken));

            // Let us store the scopes also
            clientApp.setAttribute(new Attribute<String>("scopes", scopes.toString()));
            identityManager.update(clientApp);

            return OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK).setAccessToken(accessToken).setExpiresIn("3600")
                    .buildJSONMessage();
        } catch (OAuthProblemException e) {
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e).buildJSONMessage();
        }
    }

    private static Properties getProperties(ServletContext context) throws IOException {
        Properties properties = new Properties();
        InputStream is = context.getResourceAsStream("/WEB-INF/idm.properties");
        properties.load(is);
        return properties;
    }
}