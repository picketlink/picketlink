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

import org.junit.Test;
import org.picketlink.config.HttpSecurityBuilder;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.config.http.AuthenticationConfiguration;
import org.picketlink.config.http.BasicAuthenticationConfiguration;
import org.picketlink.config.http.DigestAuthenticationConfiguration;
import org.picketlink.config.http.HttpSecurityConfiguration;
import org.picketlink.config.http.HttpSecurityConfigurationException;
import org.picketlink.config.http.PathConfiguration;
import org.picketlink.config.http.TokenAuthenticationConfiguration;
import org.picketlink.config.http.X509AuthenticationConfiguration;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Pedro Igor
 */
public class HttpSecurityConfigurationTestCase {

    @Test
    public void testSingleUri() {
        SecurityConfigurationBuilder configurationBuilder = new SecurityConfigurationBuilder();

        HttpSecurityBuilder builder = configurationBuilder.http();

        builder
            .forPath("/*")
                .authenticateWith()
                    .form()
                        .loginPage("/login.html")
                        .errorPage("/error.html")
                .authorizeWith()
                    .role("Role A", "Role B")
                    .group("Group A", "Group B")
                    .realm("Realm A", "Realm B")
                    .expression("#{identity.isLoggedIn()}");

        HttpSecurityConfiguration configuration = builder.build().getHttpSecurityConfiguration();

        assertEquals(1, configuration.getPaths().size());
    }

    @Test
    public void testChooseMethodsForUri() {
        SecurityConfigurationBuilder configurationBuilder = new SecurityConfigurationBuilder();
        HttpSecurityBuilder builder = configurationBuilder.http();

        String[] expectedMethods = {"POST", "GET"};

        builder
            .forPath("/*")
            .authenticateWith()
            .form()
            .loginPage("/login.html")
            .errorPage("/error.html")
            .authorizeWith()
            .role("Role A", "Role B")
            .group("Group A", "Group B")
            .realm("Realm A", "Realm B")
            .expression("#{identity.isLoggedIn()}");

        HttpSecurityConfiguration configuration = builder.build().getHttpSecurityConfiguration();

        assertEquals(1, configuration.getPaths().size());
    }

    @Test
    public void testPermissiveFiltering() {
        SecurityConfigurationBuilder configurationBuilder = new SecurityConfigurationBuilder();
        HttpSecurityBuilder builder = configurationBuilder.http();

        String[] expectedMethods = {"POST", "GET"};

        builder
            .forPath("/*")
            .authenticateWith()
            .form()
            .loginPage("/login.html")
            .errorPage("/error.html")
            .authorizeWith()
            .role("Role A", "Role B")
            .group("Group A", "Group B")
            .realm("Realm A", "Realm B")
            .expression("#{identity.isLoggedIn()}");

        HttpSecurityConfiguration configuration = builder.build().getHttpSecurityConfiguration();

        assertEquals(1, configuration.getPaths().size());
    }

    @Test
    public void testForAll() {
        SecurityConfigurationBuilder configurationBuilder = new SecurityConfigurationBuilder();
        HttpSecurityBuilder builder = configurationBuilder.http();

        builder
            .allPaths()
            .authenticateWith()
            .form()
            .loginPage("/login.html")
            .errorPage("/error.html")
            .authorizeWith()
            .role("Role A", "Role B")
            .group("Group A", "Group B")
            .realm("Realm A", "Realm B")
            .expression("#{identity.isLoggedIn()}");

        HttpSecurityConfiguration configuration = builder.build().getHttpSecurityConfiguration();

        assertEquals(1, configuration.getPaths().size());

        PathConfiguration pathConfiguration = configuration.getPaths().values().iterator().next().get(0);

        assertEquals("/*", pathConfiguration.getUri());
    }

    @Test
    public void testGroupOfUris() {
        SecurityConfigurationBuilder configurationBuilder = new SecurityConfigurationBuilder();
        HttpSecurityBuilder builder = configurationBuilder.http();

        builder
            .forGroup("REST Service Group A")
                .authenticateWith()
                    .form()
                        .loginPage("/loginA.html")
                        .errorPage("/errorA.html")
                .authorizeWith()
                    .role("Role A")
            .forGroup("REST Service Group B")
                .authenticateWith()
                    .form()
                        .loginPage("/loginB.html")
                        .errorPage("/errorB.html")
                .authorizeWith()
                    .role("Role B")
            .forPath("/rest/a/*", "REST Service Group A")
            .forPath("/rest/b/*", "REST Service Group B");

        HttpSecurityConfiguration configuration = builder.build().getHttpSecurityConfiguration();

        assertEquals(2, configuration.getPaths().size());
        assertEquals(2, configuration.getGroups().size());
    }

    @Test(expected = HttpSecurityConfigurationException.class)
    public void failMissingUri() {
        SecurityConfigurationBuilder configurationBuilder = new SecurityConfigurationBuilder();
        HttpSecurityBuilder builder = configurationBuilder.http();

        builder
            .forGroup("REST Service")
            .authenticateWith()
            .form()
            .loginPage("/login.html")
            .errorPage("/login.html")
            .authorizeWith()
            .role("Role A");

        builder.build();
    }

    @Test
    public void testBasicAuthcConfigUri() {
        SecurityConfigurationBuilder configurationBuilder = new SecurityConfigurationBuilder();
        HttpSecurityBuilder builder = configurationBuilder.http();

        builder
            .forPath("/*")
            .authenticateWith()
            .basic()
            .realmName("My Realm")
            .authorizeWith()
            .role("Role A", "Role B")
            .group("Group A", "Group B")
            .realm("Realm A", "Realm B")
            .expression("#{identity.isLoggedIn()}");

        HttpSecurityConfiguration configuration = builder.build().getHttpSecurityConfiguration();

        Map<String, List<PathConfiguration>> uris = configuration.getPaths();

        assertEquals(1, uris.size());

        PathConfiguration pathConfiguration = uris.values().iterator().next().get(0);
        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();

        assertTrue(BasicAuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));
    }

    @Test
    public void testDigestAuthcConfigUri() {
        SecurityConfigurationBuilder configurationBuilder = new SecurityConfigurationBuilder();
        HttpSecurityBuilder builder = configurationBuilder.http();

        builder
            .forPath("/*")
            .authenticateWith()
            .digest()
            .realmName("My Realm")
            .authorizeWith()
            .role("Role A", "Role B")
            .group("Group A", "Group B")
            .realm("Realm A", "Realm B")
            .expression("#{identity.isLoggedIn()}");

        HttpSecurityConfiguration configuration = builder.build().getHttpSecurityConfiguration();

        Map<String, List<PathConfiguration>> uris = configuration.getPaths();

        assertEquals(1, uris.size());

        PathConfiguration pathConfiguration = uris.values().iterator().next().get(0);
        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();

        assertTrue(DigestAuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));
    }

    @Test
    public void testX509AuthcConfigUri() {
        SecurityConfigurationBuilder configurationBuilder = new SecurityConfigurationBuilder();
        HttpSecurityBuilder builder = configurationBuilder.http();

        builder
            .forPath("/*")
            .authenticateWith()
            .x509()
                .subjectRegex("someExpression")
            .authorizeWith()
            .role("Role A", "Role B")
            .group("Group A", "Group B")
            .realm("Realm A", "Realm B")
            .expression("#{identity.isLoggedIn()}");

        HttpSecurityConfiguration configuration = builder.build().getHttpSecurityConfiguration();

        Map<String, List<PathConfiguration>> uris = configuration.getPaths();

        assertEquals(1, uris.size());

        PathConfiguration pathConfiguration = uris.values().iterator().next().get(0);
        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();

        assertTrue(X509AuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));
    }

    @Test
    public void testTokenAuthcConfigUri() {
        SecurityConfigurationBuilder configurationBuilder = new SecurityConfigurationBuilder();
        HttpSecurityBuilder builder = configurationBuilder.http();

        builder
            .forPath("/*")
            .authenticateWith()
            .token()
            .authorizeWith()
            .role("Role A", "Role B")
            .group("Group A", "Group B")
            .realm("Realm A", "Realm B")
            .expression("#{identity.isLoggedIn()}");

        HttpSecurityConfiguration configuration = builder.build().getHttpSecurityConfiguration();

        Map<String, List<PathConfiguration>> uris = configuration.getPaths();

        assertEquals(1, uris.size());

        PathConfiguration pathConfiguration = uris.values().iterator().next().get(0);
        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();

        assertTrue(TokenAuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));
    }
}
