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
package org.picketlink.oauth.filters;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
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
import org.picketlink.idm.ldap.internal.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.oauth.common.OAuthConstants;
import org.picketlink.oauth.messages.ResourceAccessRequest;
import org.picketlink.oauth.server.util.OAuthServerUtil;

/**
 * An instance of {@link Filter} that performs OAuth checks before allowing access to a resource
 *
 * @author anil saldhana
 * @since Sep 14, 2012
 */
public class OAuthResourceFilter implements Filter {

    protected IdentityManager identityManager = null;

    protected ServletContext context;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            context = filterConfig.getServletContext();
            handleIdentityManager();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        try {

            ResourceAccessRequest resourceAccessRequest = OAuthServerUtil.parseResourceRequest((HttpServletRequest) request);
            // Get the access token
            String passedClientID = httpRequest.getParameter(OAuthConstants.CLIENT_ID);
            String accessToken = resourceAccessRequest.getAccessToken();

            /*
             * // Get the OAuth Request out of this request and validate it OAuthAccessResourceRequest oauthRequest = new
             * OAuthAccessResourceRequest(httpRequest, ParameterStyle.BODY);
             *
             * // Get the access token String passedClientID = httpRequest.getParameter(OAuthConstants.CLIENT_ID); String
             * accessToken = oauthRequest.getAccessToken();
             */

            IdentityQuery<User> userQuery = identityManager.createIdentityQuery(User.class);
            userQuery.setParameter(User.ID, passedClientID);

            List<User> users = userQuery.getResultList();

            if (users.size() == 0) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "client_id not found");
                return;
            }

            if (users.size() > 1) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "More than one user with the same client id");
                return;
            }

            User clientApp = users.get(0);

            // Get the values from DB
            String clientID = (String) clientApp.getAttribute("clientID").getValue();
            String tokenCode = (String) clientApp.getAttribute("accessToken").getValue();

            // check if clientid is valid
            if (!clientID.equals(passedClientID)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Client ID is wrong");
                return;
            }
            // Check if the token is valid
            if (tokenCode.equals(accessToken)) {
                // Return the resource
                chain.doFilter(httpRequest, httpResponse);
                return;
            }

            // TODO: Check if the token is not expired
            // TODO: Check if the token is sufficient

            // Return the OAuth error message

            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "UnAuthorized");
            return;

        } catch (Exception e) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e.getLocalizedMessage());
            return;
        }
    }

    @Override
    public void destroy() {
    }

    private void handleIdentityManager() throws IOException {
        if (identityManager == null) {
            if (context == null) {
                throw new RuntimeException("Servlet Context has not been injected");
            }
            identityManager = new DefaultIdentityManager();
            String storeType = context.getInitParameter("storeType");
            if (storeType == null || "db".equals(storeType)) {

                EntityManagerFactory emf = Persistence.createEntityManagerFactory("oauth-pu");
                EntityManager entityManager = emf.createEntityManager();
                entityManager.getTransaction().begin();

                IdentityConfiguration identityConfig = new IdentityConfiguration();
                JPAIdentityStoreConfiguration jpaStoreConfig = new JPAIdentityStoreConfiguration();

                jpaStoreConfig.addRealm("default");

                jpaStoreConfig.setIdentityClass(IdentityObject.class);
                jpaStoreConfig.setAttributeClass(IdentityObjectAttribute.class);
                jpaStoreConfig.setRelationshipClass(RelationshipObject.class);
                jpaStoreConfig.setRelationshipIdentityClass(RelationshipIdentityObject.class);
                jpaStoreConfig.setRelationshipAttributeClass(RelationshipObjectAttribute.class);
                jpaStoreConfig.setCredentialClass(CredentialObject.class);
                jpaStoreConfig.setCredentialAttributeClass(CredentialObjectAttribute.class);
                jpaStoreConfig.setPartitionClass(PartitionObject.class);

                identityConfig.addStoreConfiguration(jpaStoreConfig);

                DefaultIdentityStoreInvocationContextFactory icf = new DefaultIdentityStoreInvocationContextFactory(emf);
                icf.setEntityManager(entityManager);
                identityManager.bootstrap(identityConfig, icf);
            }
            if ("ldap".equalsIgnoreCase(storeType)) {
                LDAPIdentityStoreConfiguration ldapConfiguration = new LDAPIdentityStoreConfiguration();

                // LDAPConfiguration ldapConfiguration = new LDAPConfiguration();

                Properties properties = getProperties();
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