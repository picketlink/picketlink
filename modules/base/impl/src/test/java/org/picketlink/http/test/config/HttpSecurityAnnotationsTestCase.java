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
import org.picketlink.config.http.AuthenticationConfiguration;
import org.picketlink.config.http.BasicAuthenticationConfiguration;
import org.picketlink.config.http.DigestAuthenticationConfiguration;
import org.picketlink.config.http.FormAuthenticationConfiguration;
import org.picketlink.config.http.HttpSecurityConfiguration;
import org.picketlink.config.http.PathConfiguration;
import org.picketlink.config.http.TokenAuthenticationConfiguration;
import org.picketlink.config.http.X509AuthenticationConfiguration;
import org.picketlink.http.internal.HttpSecurityAnnotationsParser;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Giriraj Sharma
 */
public class HttpSecurityAnnotationsTestCase {

    @Test
    public void testHttpPathAnnotation() {
        HttpSecurityConfiguration configuration = null;
        configuration = new HttpSecurityAnnotationsParser().processAnnotatedType(HttpSecurityPathConfig.class).build().getHttpSecurityConfiguration();

        assertEquals(8, configuration.getPaths().size());
        assertEquals(2, configuration.getGroups().size());

        PathConfiguration pathConfiguration = configuration.getPaths().values().iterator().next().get(0);
        assertEquals("/formProtectedUri/*", pathConfiguration.getUri());
    }

    @Test
    public void testHttpPathGroupAnnotation() {
        HttpSecurityConfiguration configuration = null;
        configuration = new HttpSecurityAnnotationsParser().processAnnotatedType(HttpSecurityPathGroupConfig.class).build().getHttpSecurityConfiguration();

        assertEquals(2, configuration.getPaths().size());
        assertEquals(2, configuration.getGroups().size());
    }
    
    @Test
    public void testHttpFormAnnotation() {
        HttpSecurityConfiguration configuration = null;
        configuration = new HttpSecurityAnnotationsParser().processAnnotatedType(HttpSecurityFormAuthConfig.class).build().getHttpSecurityConfiguration();
        assertEquals(1, configuration.getPaths().size());

        Map<String, List<PathConfiguration>> uris = configuration.getPaths();
        assertEquals(1, uris.size());

        PathConfiguration pathConfiguration = uris.values().iterator().next().get(0);
        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();

        assertTrue(FormAuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));
    }
    
    @Test
    public void testHttpBasicAnnotation() {
        HttpSecurityConfiguration configuration = null;
        configuration = new HttpSecurityAnnotationsParser().processAnnotatedType(HttpSecurityBasicAuthConfig.class).build().getHttpSecurityConfiguration();
        assertEquals(1, configuration.getPaths().size());

        Map<String, List<PathConfiguration>> uris = configuration.getPaths();
        assertEquals(1, uris.size());

        PathConfiguration pathConfiguration = uris.values().iterator().next().get(0);
        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();

        assertTrue(BasicAuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));
    }
    
    @Test
    public void testHttpDigestAnnotation() {
        HttpSecurityConfiguration configuration = null;
        configuration = new HttpSecurityAnnotationsParser().processAnnotatedType(HttpSecurityDigestAuthConfig.class).build().getHttpSecurityConfiguration();
        assertEquals(1, configuration.getPaths().size());

        Map<String, List<PathConfiguration>> uris = configuration.getPaths();
        assertEquals(1, uris.size());

        PathConfiguration pathConfiguration = uris.values().iterator().next().get(0);
        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();

        assertTrue(DigestAuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));
    }
    
    @Test
    public void testHttpTokenAnnotation() {
        HttpSecurityConfiguration configuration = null;
        configuration = new HttpSecurityAnnotationsParser().processAnnotatedType(HttpSecurityTokenAuthConfig.class).build().getHttpSecurityConfiguration();
        assertEquals(1, configuration.getPaths().size());

        Map<String, List<PathConfiguration>> uris = configuration.getPaths();
        assertEquals(1, uris.size());

        PathConfiguration pathConfiguration = uris.values().iterator().next().get(0);
        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();

        assertTrue(TokenAuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));
    }
    
    @Test
    public void testHttpX509Annotation() {
        HttpSecurityConfiguration configuration = null;
        configuration = new HttpSecurityAnnotationsParser().processAnnotatedType(HttpSecurityX509AuthConfig.class).build().getHttpSecurityConfiguration();
        assertEquals(1, configuration.getPaths().size());

        Map<String, List<PathConfiguration>> uris = configuration.getPaths();
        assertEquals(1, uris.size());

        PathConfiguration pathConfiguration = uris.values().iterator().next().get(0);
        AuthenticationConfiguration authenticationConfiguration = pathConfiguration.getAuthenticationConfiguration();

        assertTrue(X509AuthenticationConfiguration.class.isInstance(authenticationConfiguration.getAuthenticationSchemeConfiguration()));
    }

}
