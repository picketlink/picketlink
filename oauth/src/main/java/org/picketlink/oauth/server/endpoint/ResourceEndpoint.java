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
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.internal.DefaultIdentityManager;
import org.picketlink.idm.internal.DefaultIdentityStoreInvocationContextFactory;
import org.picketlink.idm.ldap.internal.LDAPConfiguration;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.model.User;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.oauth.amber.oauth2.as.response.OAuthASResponse;
import org.picketlink.oauth.amber.oauth2.common.error.OAuthError;
import org.picketlink.oauth.amber.oauth2.common.exception.OAuthSystemException;
import org.picketlink.oauth.amber.oauth2.common.message.OAuthResponse;

/**
 * OAuth2 Resource Endpoint
 *
 * @author anil saldhana
 * @since Aug 27, 2012
 */
@Path("/resource")
public class ResourceEndpoint implements Serializable {
    private static final long serialVersionUID = 1L;

    protected IdentityManager identityManager = null;

    @Context
    protected ServletContext context;

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/html")
    public Response authorize(@Context HttpServletRequest request) throws URISyntaxException, OAuthSystemException {
        try {
            handleIdentityManager();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        String accessToken = request.getParameter("access_token");

        IdentityQuery<User> userQuery = identityManager.createQuery(User.class);
        userQuery.setParameter(IdentityType.ATTRIBUTE.byName("accessToken"), accessToken);

        List<User> users = userQuery.getResultList();
        if (users.size() == 0) {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("accessToken not found")
                    .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }
        if (users.size() > 1) {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("Multiple accessToken found")
                    .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        }

        // TODO: Deal with scope

        return Response.ok().entity("I am a Resource").build();
    }

    private void handleIdentityManager() throws IOException {
        if (identityManager == null) {
            if (context == null) {
                throw new RuntimeException("Servlet Context has not been injected");
            }
            identityManager = new DefaultIdentityManager();
            String storeType = context.getInitParameter("storeType");
            if (storeType == null || "ldap".equalsIgnoreCase(storeType)) {
                LDAPConfiguration ldapConfiguration = new LDAPConfiguration();

                Properties properties = getProperties();
                ldapConfiguration.setBindDN(properties.getProperty("bindDN")).setBindCredential(
                        properties.getProperty("bindCredential"));
                ldapConfiguration.setLdapURL(properties.getProperty("ldapURL"));
                ldapConfiguration.setUserDNSuffix(properties.getProperty("userDNSuffix")).setRoleDNSuffix(
                        properties.getProperty("roleDNSuffix"));
                ldapConfiguration.setGroupDNSuffix(properties.getProperty("groupDNSuffix"));

                // Create Identity Configuration
                IdentityConfiguration config = new IdentityConfiguration();
                config.addStoreConfiguration(ldapConfiguration);

                identityManager.bootstrap(config, DefaultIdentityStoreInvocationContextFactory.DEFAULT);
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