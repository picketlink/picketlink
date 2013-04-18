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
import org.picketlink.idm.config.FeatureSet.FeatureGroup;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.jpa.internal.JPAContextInitializer;
import org.picketlink.idm.jpa.schema.CredentialObject;
import org.picketlink.idm.jpa.schema.CredentialObjectAttribute;
import org.picketlink.idm.jpa.schema.IdentityObject;
import org.picketlink.idm.jpa.schema.IdentityObjectAttribute;
import org.picketlink.idm.jpa.schema.PartitionObject;
import org.picketlink.idm.jpa.schema.RelationshipIdentityObject;
import org.picketlink.idm.jpa.schema.RelationshipObject;
import org.picketlink.idm.jpa.schema.RelationshipObjectAttribute;
import org.picketlink.idm.model.Realm;
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

    private EntityManagerFactory entityManagerFactory;
    private ThreadLocal<EntityManager> entityManager = new ThreadLocal<EntityManager>();

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

        initializeEntityManager();

        try {

            ResourceAccessRequest resourceAccessRequest = OAuthServerUtil.parseResourceRequest((HttpServletRequest) request);
            // Get the access token
            String passedClientID = httpRequest.getParameter(OAuthConstants.CLIENT_ID);
            String accessToken = resourceAccessRequest.getAccessToken();

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
        } finally {
            closeEntityManager();
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

            if (isJPAStoreConfigured()) {
                this.entityManagerFactory = Persistence.createEntityManagerFactory("picketlink-oauth-pu");

                IdentityConfiguration configuration = new IdentityConfiguration();

                configuration.jpaStore().addRealm(Realm.DEFAULT_REALM).setIdentityClass(IdentityObject.class)
                        .setAttributeClass(IdentityObjectAttribute.class).setRelationshipClass(RelationshipObject.class)
                        .setRelationshipIdentityClass(RelationshipIdentityObject.class)
                        .setRelationshipAttributeClass(RelationshipObjectAttribute.class)
                        .setCredentialClass(CredentialObject.class)
                        .setCredentialAttributeClass(CredentialObjectAttribute.class).setPartitionClass(PartitionObject.class)
                        .supportAllFeatures().addContextInitializer(new JPAContextInitializer(this.entityManagerFactory) {
                            @Override
                            public EntityManager getEntityManager() {
                                return entityManager.get();
                            }
                        });

                identityManager = configuration.buildIdentityManagerFactory().createIdentityManager();
            }

            if (isLDAPStoreConfigured()) {
                IdentityConfiguration configuration = new IdentityConfiguration();

                Properties properties = getProperties();

                configuration
                        .ldapStore()
                        .setBaseDN(properties.getProperty("baseDN"))
                        .setBindDN(properties.getProperty("bindDN"))
                        .setBindCredential(properties.getProperty("bindCredential"))
                        .setLdapURL(properties.getProperty("ldapURL"))
                        .setUserDNSuffix(properties.getProperty("userDNSuffix"))
                        .setRoleDNSuffix(properties.getProperty("roleDNSuffix"))
                        .setAgentDNSuffix(properties.getProperty("agentDNSuffix"))
                        .setGroupDNSuffix(properties.getProperty("groupDNSuffix"))
                        .addRealm(Realm.DEFAULT_REALM)
                        .supportFeature(FeatureGroup.user, FeatureGroup.agent, FeatureGroup.user, FeatureGroup.group,
                                FeatureGroup.role, FeatureGroup.attribute, FeatureGroup.relationship, FeatureGroup.credential);

                identityManager = configuration.buildIdentityManagerFactory().createIdentityManager();
            }
        }
    }

    private boolean isLDAPStoreConfigured() {
        return "ldap".equalsIgnoreCase(context.getInitParameter("storeType"));
    }

    private boolean isJPAStoreConfigured() {
        return context.getInitParameter("storeType") == null || "db".equals(context.getInitParameter("storeType"));
    }

    private void closeEntityManager() {
        if (isJPAStoreConfigured() && this.entityManagerFactory != null) {
            EntityManager entityManager = this.entityManager.get();

            entityManager.getTransaction().commit();
            entityManager.close();

            this.entityManager.remove();
        }
    }

    private void initializeEntityManager() {
        if (isJPAStoreConfigured() && this.entityManagerFactory != null) {
            EntityManager entityManager = this.entityManagerFactory.createEntityManager();

            entityManager.getTransaction().begin();

            this.entityManager.set(entityManager);
        }
    }

    private Properties getProperties() throws IOException {
        Properties properties = new Properties();
        InputStream is = context.getResourceAsStream("/WEB-INF/idm.properties");
        properties.load(is);
        return properties;
    }
}
