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

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.oauth.common.OAuthConstants;
import org.picketlink.oauth.messages.ResourceAccessRequest;
import org.picketlink.oauth.server.util.OAuthServerUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

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
            identityManager = OAuthServerUtil.handleIdentityManager(context);
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

    private Properties getProperties() throws IOException {
        Properties properties = new Properties();
        InputStream is = context.getResourceAsStream("/WEB-INF/idm.properties");
        properties.load(is);
        return properties;
    }
}
