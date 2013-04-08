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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.PropertyNamingStrategy;
import org.jboss.logging.Logger;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.credential.UsernamePasswordCredentials;
import org.picketlink.idm.model.Agent;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.oauth.common.OAuthConstants;
import org.picketlink.oauth.grants.AuthorizationCodeGrant;
import org.picketlink.oauth.grants.ResourceOwnerPasswordCredentialsGrant;
import org.picketlink.oauth.grants.ResourceOwnerPasswordCredentialsGrant.PasswordAccessTokenRequest;
import org.picketlink.oauth.messages.AccessTokenRequest;
import org.picketlink.oauth.messages.AuthorizationRequest;
import org.picketlink.oauth.messages.ErrorResponse;
import org.picketlink.oauth.messages.ErrorResponse.ErrorResponseCode;
import org.picketlink.oauth.messages.OAuthResponse;
import org.picketlink.oauth.messages.RegistrationRequest;
import org.picketlink.oauth.messages.ResourceAccessRequest;

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
    @SuppressWarnings("unchecked")
    public static IdentityManager handleIdentityManager(ServletContext context) throws IOException {
        // FIXME: Need to correctly handle the JPA store configuration. Need to provide ways to configure the
        // JPAContextInitializer in order to use the EntityManager properly between invocations to the identity manager.

        // IdentityManager identityManager = null;
        // if (context == null) {
        // throw new IllegalArgumentException("context is null");
        // }
        // identityManager = (IdentityManager) context.getAttribute("identityManager");
        // if (identityManager == null) {
        // // Need to handle IM
        // identityManager = new DefaultIdentityManager();
        // String storeType = context.getInitParameter("storeType");
        // if (storeType == null || "db".equals(storeType)) {
        //
        // EntityManagerFactory emf = Persistence.createEntityManagerFactory("oauth-pu");
        // EntityManager entityManager = emf.createEntityManager();
        //
        // entityManager.getTransaction().begin();
        //
        // IdentityConfiguration identityConfig = new IdentityConfiguration();
        // JPAIdentityStoreConfiguration jpaStoreConfig = new JPAIdentityStoreConfiguration();
        //
        // jpaStoreConfig.addRealm("default");
        //
        // jpaStoreConfig.setIdentityClass(IdentityObject.class);
        // jpaStoreConfig.setAttributeClass(IdentityObjectAttribute.class);
        // jpaStoreConfig.setRelationshipClass(RelationshipObject.class);
        // jpaStoreConfig.setRelationshipIdentityClass(RelationshipIdentityObject.class);
        // jpaStoreConfig.setRelationshipAttributeClass(RelationshipObjectAttribute.class);
        // jpaStoreConfig.setCredentialClass(CredentialObject.class);
        // jpaStoreConfig.setCredentialAttributeClass(CredentialObjectAttribute.class);
        // jpaStoreConfig.setPartitionClass(PartitionObject.class);
        //
        // FeatureSet.addFeatureSupport(jpaStoreConfig.getFeatureSet());
        // FeatureSet.addRelationshipSupport(jpaStoreConfig.getFeatureSet());
        // FeatureSet.addRelationshipSupport(jpaStoreConfig.getFeatureSet(), Authorization.class);
        // jpaStoreConfig.getFeatureSet().setSupportsCustomRelationships(true);
        // jpaStoreConfig.getFeatureSet().setSupportsMultiRealm(true);
        //
        // identityConfig.addStoreConfiguration(jpaStoreConfig);
        //
        // identityManager = new DefaultIdentityManager();
        // DefaultIdentityStoreInvocationContextFactory icf = new DefaultIdentityStoreInvocationContextFactory(emf);
        // icf.setEntityManager(entityManager);
        // identityManager.bootstrap(identityConfig, icf);
        // context.setAttribute("identityManager", identityManager);
        // }
        // if ("ldap".equalsIgnoreCase(storeType)) {
        // LDAPIdentityStoreConfiguration ldapConfiguration = new LDAPIdentityStoreConfiguration();
        //
        // // LDAPConfiguration ldapConfiguration = new LDAPConfiguration();
        //
        // Properties properties = getProperties(context);
        // ldapConfiguration.setBaseDN(properties.getProperty("baseDN")).setBindDN(properties.getProperty("bindDN"))
        // .setBindCredential(properties.getProperty("bindCredential"));
        // ldapConfiguration.setLdapURL(properties.getProperty("ldapURL"));
        // ldapConfiguration.setUserDNSuffix(properties.getProperty("userDNSuffix"))
        // .setRoleDNSuffix(properties.getProperty("roleDNSuffix"))
        // .setAgentDNSuffix(properties.getProperty("agentDNSuffix"));
        // ldapConfiguration.setGroupDNSuffix(properties.getProperty("groupDNSuffix"));
        //
        // // store.setup(ldapConfiguration, DefaultIdentityStoreInvocationContextFactory.DEFAULT);
        //
        // // Create Identity Configuration
        // IdentityConfiguration config = new IdentityConfiguration();
        // config.addStoreConfiguration(ldapConfiguration);
        //
        // identityManager.bootstrap(config, DefaultIdentityStoreInvocationContextFactory.DEFAULT);
        // context.setAttribute("identityManager", identityManager);
        // }
        // }
        //
        // return identityManager;
        throw new RuntimeException("Not implemented yet.");
    }

    /**
     * Handle an Authorization Code Grant Type Request
     *
     * @param request
     * @param identityManager
     * @return
     */
    public static OAuthResponse authorizationCodeRequest(HttpServletRequest request, IdentityManager identityManager) {

        AuthorizationCodeGrant grant = new AuthorizationCodeGrant();

        try {
            // Let us parse the authorization request
            AuthorizationRequest authorizationRequest = parseAuthorizationRequest(request);
            String responseType = authorizationRequest.getResponseType();

            if (responseType.equals(OAuthConstants.CODE) == false) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setErrorDescription("response_type should be :code").setError(ErrorResponseCode.invalid_client)
                        .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

                return errorResponse;
            }

            grant.setAuthorizationRequest(authorizationRequest);

            String passedClientID = authorizationRequest.getClientId();

            if (passedClientID == null) {
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setErrorDescription("client_id is null").setError(ErrorResponseCode.invalid_client)
                        .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

                return errorResponse;
            }

            IdentityQuery<Agent> agentQuery = identityManager.createIdentityQuery(Agent.class);
            agentQuery.setParameter(IdentityType.ATTRIBUTE.byName("clientID"), passedClientID);

            List<Agent> agents = agentQuery.getResultList();
            if (agents.size() == 0) {
                log.error(passedClientID + " not found");

                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setErrorDescription("client_id not found").setError(ErrorResponseCode.invalid_client)
                        .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

                return errorResponse;
            }
            if (agents.size() > 1) {
                log.error(passedClientID + " multiple found");
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setErrorDescription("Multiple client_id found").setError(ErrorResponseCode.invalid_client)
                        .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

                return errorResponse;
            }

            Agent clientApp = agents.get(0);

            // User clientApp = users.get(0);
            Attribute<String> clientIDAttr = clientApp.getAttribute("clientID");
            String clientID = clientIDAttr.getValue();

            // check if clientid is valid
            if (!clientID.equals(passedClientID)) {
                log.error(passedClientID + " not found");
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setErrorDescription("client_id not found").setError(ErrorResponseCode.invalid_client)
                        .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

                return errorResponse;
            }

            OAuthResponse oauthResponse = null;

            String authorizationCode = grant.getValueGenerator().value();
            grant.setAuthorizationCode(authorizationCode);

            clientApp.setAttribute(new Attribute<String>("authorizationCode", authorizationCode));
            identityManager.update(clientApp);

            oauthResponse = grant.authorizationResponse();
            oauthResponse.setStatusCode(HttpServletResponse.SC_FOUND);

            String redirectURI = authorizationRequest.getRedirectUri();

            oauthResponse.setLocation(redirectURI + "?" + oauthResponse.asQueryParams());
            return oauthResponse;
        } catch (Exception e) {
            log.error("Exception:", e);
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorDescription("client_id not found").setError(ErrorResponseCode.invalid_client)
                    .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

            return errorResponse;
        }
    }

    /**
     * Handle Token Request
     *
     * @param request
     * @param identityManager
     * @return
     */
    public static OAuthResponse tokenRequest(HttpServletRequest request, IdentityManager identityManager) {
        String grantType = request.getParameter(OAuthConstants.GRANT_TYPE);
        // Authorization Code Grant
        if (grantType.equals(AuthorizationCodeGrant.GRANT_TYPE)) {
            return authorizationCodeGrantTypeTokenRequest(request, identityManager);
        }
        if (grantType.equals(OAuthConstants.PASSWORD)) {
            return passwordGrantTypeTokenRequest(request, identityManager);
        }
        if (grantType.equals(OAuthConstants.REFRESH_TOKEN)) {
            return refreshTokenRequest(request, identityManager);
        }
        return null;
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

    /**
     * Parse a {@link ResourceAccessRequest} with application/x-www-form-urlencoded
     *
     * @param request
     * @return
     */
    public static ResourceAccessRequest parseResourceRequest(HttpServletRequest request) {
        ResourceAccessRequest resourceAccessRequest = new ResourceAccessRequest();
        resourceAccessRequest.setAccessToken(request.getParameter(OAuthConstants.ACCESS_TOKEN));
        return resourceAccessRequest;
    }

    /**
     * Parse a {@link RegistrationRequest} coming as application/x-www-form-urlencoded
     *
     * @param request
     * @return
     */
    public static RegistrationRequest parseRegistrationRequestWithFORM(HttpServletRequest request) {
        RegistrationRequest registrationRequest = new RegistrationRequest();
        registrationRequest.setClientName(request.getParameter(OAuthConstants.CLIENT_NAME));
        registrationRequest.setClientDescription(request.getParameter(OAuthConstants.CLIENT_DESCRIPTION));
        registrationRequest.setClient_Icon(request.getParameter(OAuthConstants.CLIENT_ICON));
        registrationRequest.setClientUrl(request.getParameter(OAuthConstants.CLIENT_URL));
        registrationRequest.setClientRedirecturl(request.getParameter(OAuthConstants.CLIENT_REDIRECT_URL));

        return registrationRequest;
    }

    /**
     * Parse a {@link RegistrationRequest} coming as application/json
     *
     * @param request
     * @return
     */
    public static RegistrationRequest parseRegistrationRequestWithJSON(HttpServletRequest request) {
        // Read JSON from request
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);

        try {
            return mapper.readValue(request.getInputStream(), RegistrationRequest.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // Private Methods

    /**
     * Refresh Token Request
     *
     * @param request
     * @param identityManager
     * @return
     */
    private static OAuthResponse refreshTokenRequest(HttpServletRequest request, IdentityManager identityManager) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorDescription("refresh_token not supported").setError(ErrorResponseCode.invalid_client)
                .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

        return errorResponse;
    }

    /**
     * Handle Password Grant Type token request
     *
     * @param request
     * @param identityManager
     * @return
     */
    private static OAuthResponse passwordGrantTypeTokenRequest(HttpServletRequest request, IdentityManager identityManager) {

        OAuthResponse oauthResponse = null;

        ResourceOwnerPasswordCredentialsGrant grant = new ResourceOwnerPasswordCredentialsGrant();

        PasswordAccessTokenRequest accessTokenRequest = parsePasswordAccessTokenRequest(request);
        grant.setAccessTokenRequest(accessTokenRequest);

        String passedClientID = accessTokenRequest.getClientId();
        if (passedClientID == null) {

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorDescription("client_id is null").setError(ErrorResponseCode.invalid_client)
                    .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

            return errorResponse;
        }
        String username = accessTokenRequest.getUsername();
        String password = accessTokenRequest.getPassword();

        UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials();
        usernamePasswordCredentials.setUsername(username);
        usernamePasswordCredentials.setPassword(new Password(password.toCharArray()));
        try {
            identityManager.validateCredentials(usernamePasswordCredentials);
        } catch (Exception e) {

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorDescription("invalid username or password").setError(ErrorResponseCode.invalid_grant)
                    .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

            return errorResponse;
        }

        return oauthResponse;
    }

    /**
     * Handle Token Request
     *
     * @param request
     * @param identityManager
     * @return
     */
    private static OAuthResponse authorizationCodeGrantTypeTokenRequest(HttpServletRequest request,
            IdentityManager identityManager) {

        OAuthResponse oauthResponse = null;

        AuthorizationCodeGrant grant = new AuthorizationCodeGrant();

        AccessTokenRequest accessTokenRequest = parseAccessTokenRequest(request);
        grant.setAccessTokenRequest(accessTokenRequest);

        String passedClientID = accessTokenRequest.getClientId();
        if (passedClientID == null) {

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorDescription("client_id is null").setError(ErrorResponseCode.invalid_client)
                    .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

            return errorResponse;
        }

        IdentityQuery<Agent> agentQuery = identityManager.createIdentityQuery(Agent.class);
        agentQuery.setParameter(IdentityType.ATTRIBUTE.byName("clientID"), passedClientID);

        List<Agent> agents = agentQuery.getResultList();
        if (agents.size() == 0) {
            log.error(passedClientID + " not found");

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorDescription("passed client_id not found").setError(ErrorResponseCode.invalid_client)
                    .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

            return errorResponse;
        }
        if (agents.size() > 1) {
            log.error(passedClientID + " multiple found");

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorDescription("passed client_id multiple found").setError(ErrorResponseCode.invalid_client)
                    .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

            return errorResponse;
        }

        Agent clientApp = agents.get(0);

        // Get the values from DB
        Attribute<String> clientIDAttr = clientApp.getAttribute("clientID");
        String clientID = clientIDAttr.getValue();
        Attribute<String> authorizationCodeAttr = clientApp.getAttribute("authorizationCode");
        if (authorizationCodeAttr == null) {
            log.error("authorization code is null");

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorDescription("authorization code null").setError(ErrorResponseCode.invalid_client)
                    .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);

            return errorResponse;
        }
        String authorizationCode = authorizationCodeAttr.getValue();

        // check if clientid is valid
        if (!clientID.equals(passedClientID)) {

            log.error("client_id does not match");

            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setErrorDescription("client_id does not match").setError(ErrorResponseCode.invalid_client)
                    .setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
            return errorResponse;
        }

        if (accessTokenRequest.getGrantType().equals(AuthorizationCodeGrant.GRANT_TYPE)) {
            if (!authorizationCode.equals(accessTokenRequest.getCode())) {

                log.error("authorization_code does not match");

                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setErrorDescription("authorization_code does not match")
                        .setError(ErrorResponseCode.invalid_grant).setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                return errorResponse;
            }
        }

        String accessToken = grant.getValueGenerator().value();
        clientApp.setAttribute(new Attribute<String>("accessToken", accessToken));
        identityManager.update(clientApp);

        grant.setAccessToken(accessToken);

        oauthResponse = grant.accessTokenResponse();
        oauthResponse.setStatusCode(HttpServletResponse.SC_FOUND);

        return oauthResponse;
    }

    private static Properties getProperties(ServletContext context) throws IOException {
        Properties properties = new Properties();
        InputStream is = context.getResourceAsStream("/WEB-INF/idm.properties");
        properties.load(is);
        return properties;
    }

    private static AuthorizationRequest parseAuthorizationRequest(HttpServletRequest request) {
        AuthorizationRequest authorizationRequest = new AuthorizationRequest();

        authorizationRequest.setClientId(request.getParameter(OAuthConstants.CLIENT_ID))
                .setRedirectUri(request.getParameter(OAuthConstants.REDIRECT_URI))
                .setResponseType(request.getParameter(OAuthConstants.RESPONSE_TYPE));

        return authorizationRequest;
    }

    private static AccessTokenRequest parseAccessTokenRequest(HttpServletRequest request) {
        AccessTokenRequest accessTokenRequest = new AccessTokenRequest();

        accessTokenRequest.setCode(request.getParameter(OAuthConstants.CODE))
                .setRedirectUri(request.getParameter(OAuthConstants.REDIRECT_URI))
                .setGrantType(request.getParameter(OAuthConstants.GRANT_TYPE))
                .setClientId(request.getParameter(OAuthConstants.CLIENT_ID));

        return accessTokenRequest;
    }

    private static PasswordAccessTokenRequest parsePasswordAccessTokenRequest(HttpServletRequest request) {
        ResourceOwnerPasswordCredentialsGrant grant = new ResourceOwnerPasswordCredentialsGrant();
        PasswordAccessTokenRequest accessTokenRequest = grant.new PasswordAccessTokenRequest();

        accessTokenRequest.setPassword(request.getParameter(OAuthConstants.PASSWORD));
        accessTokenRequest.setUsername(request.getParameter(OAuthConstants.USERNAME));

        accessTokenRequest.setCode(request.getParameter(OAuthConstants.CODE))
                .setRedirectUri(request.getParameter(OAuthConstants.REDIRECT_URI))
                .setGrantType(request.getParameter(OAuthConstants.GRANT_TYPE))
                .setClientId(request.getParameter(OAuthConstants.CLIENT_ID));

        return accessTokenRequest;
    }
}
