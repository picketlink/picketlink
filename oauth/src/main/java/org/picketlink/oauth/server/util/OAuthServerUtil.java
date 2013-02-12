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
package org.picketlink.oauth.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.logging.Logger;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.credential.internal.Password;
import org.picketlink.idm.credential.internal.UsernamePasswordCredentials;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.jpa.internal.JPAIdentityStoreConfiguration;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.ldap.internal.LDAPConfigurationBuilder;
import org.picketlink.idm.ldap.internal.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
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
    private static Logger log = Logger.getLogger(OAuthServerUtil.class);

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
            if (storeType == null || "db".equals(storeType)) {

                EntityManagerFactory emf = Persistence.createEntityManagerFactory("oauth-pu");
                EntityManager entityManager = emf.createEntityManager();

                entityManager.getTransaction().begin();

                IdentityConfiguration identityConfig = new IdentityConfiguration();
                JPAIdentityStoreConfiguration jpaStoreConfig = new JPAIdentityStoreConfiguration();

                jpaStoreConfig.setRealm("default");

                jpaStoreConfig.setIdentityClass(IdentityObject.class);
                jpaStoreConfig.setAttributeClass(IdentityObjectAttribute.class);
                jpaStoreConfig.setRelationshipClass(RelationshipObject.class);
                jpaStoreConfig.setRelationshipIdentityClass(RelationshipIdentityObject.class);
                jpaStoreConfig.setRelationshipAttributeClass(RelationshipObjectAttribute.class);
                jpaStoreConfig.setCredentialClass(CredentialObject.class);
                jpaStoreConfig.setCredentialAttributeClass(CredentialObjectAttribute.class);
                jpaStoreConfig.setPartitionClass(PartitionObject.class);

                identityConfig.addStoreConfiguration(jpaStoreConfig);

                identityManager = new DefaultIdentityManager();
                DefaultIdentityStoreInvocationContextFactory icf = new DefaultIdentityStoreInvocationContextFactory(emf);
                icf.setEntityManager(entityManager);
                identityManager.bootstrap(identityConfig, icf);
                context.setAttribute("identityManager", identityManager);
            }
            if ("ldap".equalsIgnoreCase(storeType)) {

                // LDAPIdentityStore store = new LDAPIdentityStore();
                LDAPConfigurationBuilder builder = new LDAPConfigurationBuilder();
                LDAPIdentityStoreConfiguration ldapConfiguration = (LDAPIdentityStoreConfiguration) builder.build();

                // LDAPConfiguration ldapConfiguration = new LDAPConfiguration();

                Properties properties = getProperties(context);
                ldapConfiguration.setBaseDN(properties.getProperty("baseDN")).setBindDN(properties.getProperty("bindDN"))
                        .setBindCredential(properties.getProperty("bindCredential"));
                ldapConfiguration.setLdapURL(properties.getProperty("ldapURL"));
                ldapConfiguration.setUserDNSuffix(properties.getProperty("userDNSuffix"))
                        .setRoleDNSuffix(properties.getProperty("roleDNSuffix"))
                        .setAgentDNSuffix(properties.getProperty("agentDNSuffix"));
                ldapConfiguration.setGroupDNSuffix(properties.getProperty("groupDNSuffix"));

                // store.setup(ldapConfiguration, DefaultIdentityStoreInvocationContextFactory.DEFAULT);

                // Create Identity Configuration
                IdentityConfiguration config = new IdentityConfiguration();
                config.addStoreConfiguration(ldapConfiguration);

                identityManager.bootstrap(config, DefaultIdentityStoreInvocationContextFactory.DEFAULT);
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

            IdentityQuery<Agent> agentQuery = identityManager.createIdentityQuery(Agent.class);
            agentQuery.setParameter(IdentityType.ATTRIBUTE.byName("clientID"), passedClientID);

            List<Agent> agents = agentQuery.getResultList();
            if (agents.size() == 0) {
                log.error(passedClientID + " not found");
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
            }
            if (agents.size() > 1) {
                log.error(passedClientID + " multiple found");
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("Multiple client_id found")
                        .buildJSONMessage();
            }

            Agent clientApp = agents.get(0);

            // User clientApp = users.get(0);
            Attribute<String> clientIDAttr = clientApp.getAttribute("clientID");
            String clientID = clientIDAttr.getValue();

            // check if clientid is valid
            if (!clientID.equals(passedClientID)) {
                log.error(passedClientID + " not found");
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
            e.printStackTrace();
            log.error("Exception:", e);
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

            IdentityQuery<Agent> agentQuery = identityManager.createIdentityQuery(Agent.class);
            agentQuery.setParameter(IdentityType.ATTRIBUTE.byName("clientID"), passedClientID);

            List<Agent> agents = agentQuery.getResultList();
            if (agents.size() == 0) {
                log.error(passedClientID + " not found");
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                        .buildJSONMessage();
            }
            if (agents.size() > 1) {
                log.error(passedClientID + " multiple found");
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("Multiple client_id found")
                        .buildJSONMessage();
            }

            Agent clientApp = agents.get(0);

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
                try {
                    identityManager.validateCredentials(usernamePasswordCredentials);
                } catch (Exception e) {
                    return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription("invalid username or password").buildJSONMessage();
                }
            } else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE).equals(GrantType.REFRESH_TOKEN.toString())) {
                return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                        .setError(OAuthError.TokenResponse.INVALID_GRANT)
                        .setErrorDescription("Refresh Token not yet supported").buildJSONMessage();
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

    /**
     * Validate the access token
     *
     * @param passedAccessToken
     * @param identityManager
     * @return
     */
    public static boolean validateAccessToken(String passedAccessToken, IdentityManager identityManager) {

        IdentityQuery<Agent> agentQuery = identityManager.createIdentityQuery(Agent.class);
        agentQuery.setParameter(IdentityType.ATTRIBUTE.byName("accessToken"), passedAccessToken);

        List<Agent> agents = agentQuery.getResultList();
        int size = agents.size();

        if (size == 0) {
            return false;
        }
        if (size != 1) {
            return false;
        }
        return true;
    }

    private static Properties getProperties(ServletContext context) throws IOException {
        Properties properties = new Properties();
        InputStream is = context.getResourceAsStream("/WEB-INF/idm.properties");
        properties.load(is);
        return properties;
    }
}