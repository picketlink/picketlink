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
package org.picketlink.http.test.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.picketlink.config.http.AuthenticationConfiguration;
import org.picketlink.config.http.AuthorizationConfiguration;
import org.picketlink.config.http.CORSConfiguration;
import org.picketlink.config.http.FormAuthenticationConfiguration;
import org.picketlink.config.http.HttpSecurityConfiguration;
import org.picketlink.config.http.PathConfiguration;
import org.picketlink.http.internal.HttpSecurityAnnotationsParser;

/**
 * @author Giriraj Sharma
 */
public class HttpSecurityCorsConfigTestCase {

    @Test
    public void testHttpCorsPathConfig() {
        HttpSecurityConfiguration configuration = null;
        configuration = new HttpSecurityAnnotationsParser().processAnnotatedType(HttpSecurityCorsPathConfig.class).build().getHttpSecurityConfiguration();

        assertEquals(2, configuration.getPaths().size());
        assertEquals(1, configuration.getGroups().size());

        PathConfiguration pathConfiguration = configuration.getPaths().values().iterator().next().get(0);
        assertEquals(configuration.getPaths().values().size(), 2);
        assertEquals("/corsProtectedUri/*", pathConfiguration.getUri());

        CORSConfiguration corsConfiguration = pathConfiguration.getCORSConfiguration();
        Set<String> allowedOrigins = corsConfiguration.getAllowedOrigins();
        assertEquals(allowedOrigins.size(), 2);
        assertTrue(allowedOrigins.contains("http://www.example.org:9000"));
        assertTrue(allowedOrigins.contains("http://www.example.com:8008"));

        Set<String> supportedMethods = corsConfiguration.getAllowedMethods();
        assertEquals(supportedMethods.size(), 5);
        assertTrue(supportedMethods.contains("GET"));
        assertTrue(supportedMethods.contains("PUT"));
        assertTrue(supportedMethods.contains("POST"));
        assertTrue(supportedMethods.contains("DELETE"));
        assertTrue(supportedMethods.contains("OPTIONS"));

        Set<String> supportedHeaders = corsConfiguration.getAllowedHeaders();
        assertEquals(supportedHeaders.size(), 5);
        assertTrue(supportedHeaders.contains("Origin"));
        assertTrue(supportedHeaders.contains("X-Requested-With"));
        assertTrue(supportedHeaders.contains("Content-Type"));
        assertTrue(supportedHeaders.contains("Accept"));
        assertTrue(supportedHeaders.contains("Authorization"));

        Set<String> exposedHeaders = corsConfiguration.getExposedHeaders();
        assertEquals(exposedHeaders.size(), 2);
        assertTrue(exposedHeaders.contains("X-Requested-With"));
        assertTrue(exposedHeaders.contains("Content-Type"));

        assertTrue(corsConfiguration.isAllowCredentials());

        assertEquals(corsConfiguration.getMaxAge(), 3600);

        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();
        assertTrue(FormAuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));

        AuthorizationConfiguration authorizationConfiguration = pathConfiguration.getAuthorizationConfiguration();
        String roles[] = authorizationConfiguration.getAllowedRoles();
        assertEquals(roles.length, 2);
        assertEquals(roles[0], "Role A");
        assertEquals(roles[1], "Role B");

        String realms[] = authorizationConfiguration.getAllowedRealms();
        assertEquals(realms.length, 2);
        assertEquals(realms[0], "Realm A");
        assertEquals(realms[1], "Realm B");

        String groups[] = authorizationConfiguration.getAllowedGroups();
        assertEquals(groups.length, 2);
        assertEquals(groups[0], "Group A");
        assertEquals(groups[1], "Group B");

        String expressions[] = authorizationConfiguration.getExpressions();
        assertEquals(expressions.length, 1);
        assertEquals(expressions[0], "#{identity.isLoggedIn()}");

    }

    @Test
    public void testHttpPathGroupConfig() {
        HttpSecurityConfiguration configuration = null;
        configuration = new HttpSecurityAnnotationsParser().processAnnotatedType(HttpSecurityCorsPathGroupConfig.class).build().getHttpSecurityConfiguration();

        assertEquals(1, configuration.getPaths().size());
        assertEquals(1, configuration.getGroups().size());

        PathConfiguration pathConfiguration = configuration.getPaths().values().iterator().next().get(0);
        assertEquals(configuration.getPaths().values().size(), 1);
        assertEquals("REST Service Group A", pathConfiguration.getGroupName());

        CORSConfiguration corsConfiguration = pathConfiguration.getCORSConfiguration();
        Set<String> allowedOrigins = corsConfiguration.getAllowedOrigins();
        assertEquals(allowedOrigins.size(), 2);
        assertTrue(allowedOrigins.contains("http://www.example.org:9000"));
        assertTrue(allowedOrigins.contains("http://www.example.com:8008"));

        Set<String> supportedMethods = corsConfiguration.getAllowedMethods();
        assertEquals(supportedMethods.size(), 3);
        assertTrue(supportedMethods.contains("POST"));
        assertTrue(supportedMethods.contains("DELETE"));
        assertTrue(supportedMethods.contains("OPTIONS"));

        assertTrue(corsConfiguration.isAllowAnyHeader());

        Set<String> exposedHeaders = corsConfiguration.getExposedHeaders();
        assertEquals(exposedHeaders.size(), 1);
        assertTrue(exposedHeaders.contains("Authorization"));

        assertTrue(corsConfiguration.isAllowCredentials());

        assertEquals(corsConfiguration.getMaxAge(), 3600);

        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();
        assertTrue(FormAuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));

        AuthorizationConfiguration authorizationConfiguration = pathConfiguration.getAuthorizationConfiguration();
        String roles[] = authorizationConfiguration.getAllowedRoles();
        assertEquals(roles.length, 2);
        assertEquals(roles[0], "Role A");
        assertEquals(roles[1], "Role B");

        String expressions[] = authorizationConfiguration.getExpressions();
        assertEquals(expressions.length, 1);
        assertEquals(expressions[0], "#{identity.isLoggedIn()}");

    }

}
