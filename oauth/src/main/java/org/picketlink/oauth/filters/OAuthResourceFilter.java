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
package org.picketlink.oauth.filters;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.picketlink.oauth.amber.oauth2.common.OAuth;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthProblemException;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthSystemException;
import org.picketlink.oauth.amber.oauth2.common.message.types.ParameterStyle;
import org.picketlink.oauth.amber.oauth2.common.utils.OAuthUtils;
import org.picketlink.oauth.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPConfigurationBuilder;
import org.picketlink.idm.ldap.internal.LDAPIdentityStore;
import org.picketlink.idm.ldap.internal.LDAPIdentityStoreConfiguration;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;

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

            // Get the OAuth Request out of this request and validate it
            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(httpRequest, ParameterStyle.BODY);

            // Get the access token
            String passedClientID = httpRequest.getParameter(OAuth.OAUTH_CLIENT_ID);
            String accessToken = oauthRequest.getAccessToken();

            IdentityQuery<User> userQuery = identityManager.createIdentityQuery(User.class);
            userQuery.setParameter(User.ID, passedClientID);

            List<User> users = userQuery.getResultList();

            /*
             * UserQuery userQuery = identityManager.createQ.createUserQuery().setAttributeFilter("clientID", new String[] {
             * passedClientID }); List<User> users = userQuery.executeQuery();
             */

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

        } catch (OAuthProblemException e) {

            // Check if the error code has been set
            String errorCode = e.getError();
            if (OAuthUtils.isEmpty(errorCode)) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
                return;
            }

            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, errorCode);
            return;
        } catch (OAuthSystemException e1) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, e1.getLocalizedMessage());
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
            if (storeType == null || "ldap".equalsIgnoreCase(storeType)) {
                LDAPIdentityStore store = new LDAPIdentityStore();
                LDAPConfigurationBuilder builder = new LDAPConfigurationBuilder();
                LDAPIdentityStoreConfiguration ldapConfiguration = (LDAPIdentityStoreConfiguration) builder.build();

                // LDAPConfiguration ldapConfiguration = new LDAPConfiguration();

                Properties properties = getProperties();
                ldapConfiguration.setBindDN(properties.getProperty("bindDN")).setBindCredential(
                        properties.getProperty("bindCredential"));
                ldapConfiguration.setLdapURL(properties.getProperty("ldapURL"));
                ldapConfiguration.setUserDNSuffix(properties.getProperty("userDNSuffix")).setRoleDNSuffix(
                        properties.getProperty("roleDNSuffix"));
                ldapConfiguration.setGroupDNSuffix(properties.getProperty("groupDNSuffix"));

                store.setup(ldapConfiguration, null);

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