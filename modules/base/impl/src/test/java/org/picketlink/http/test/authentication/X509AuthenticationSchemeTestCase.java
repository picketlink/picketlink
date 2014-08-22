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
package org.picketlink.http.test.authentication;

import org.junit.Test;
import org.mockito.Mockito;
import org.picketlink.config.SecurityConfigurationBuilder;
import org.picketlink.event.SecurityConfigurationEvent;
import org.picketlink.http.internal.authentication.schemes.X509AuthenticationScheme;
import org.picketlink.http.test.AbstractSecurityFilterTestCase;
import org.picketlink.test.weld.Deployment;
import org.picketlink.http.test.SecurityInitializer;

import javax.enterprise.event.Observes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * @author Pedro Igor
 */
@Deployment(
    beans = {
        X509AuthenticationSchemeTestCase.SecurityConfiguration.class, SecurityInitializer.class
    },
    excludeBeansFromPackage = "org.picketlink.http.test"
)
public class X509AuthenticationSchemeTestCase extends AbstractSecurityFilterTestCase {

    @Test
    public void testSuccessfulAuthentication() throws Exception {
        when(this.request.getServletPath()).thenReturn("/x509ProtectedUri/");

        prepareAuthenticationRequest(this.request);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        Mockito.verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testWithSubjectRegex() throws Exception {
        when(this.request.getServletPath()).thenReturn("/x509ProtectedWithSubjectRegexUri/");

        prepareAuthenticationRequest(this.request);

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        Mockito.verify(this.filterChain, times(1)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    public void testNoCert() throws Exception {
        when(this.request.getServletPath()).thenReturn("/x509ProtectedUri/");

        this.securityFilter.doFilter(this.request, this.response, this.filterChain);

        Mockito.verify(this.filterChain, times(0)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    private void prepareAuthenticationRequest(HttpServletRequest request) {
        X509Certificate certificate = getTestingCertificate(getClass().getClassLoader());

        when(request.getAttribute(X509AuthenticationScheme.X509_CLIENT_CERT_REQUEST_ATTRIBUTE)).thenReturn(new X509Certificate[] {certificate});
    }

    private X509Certificate getTestingCertificate(ClassLoader classLoader) {
        InputStream bis = classLoader.getResourceAsStream("cert/servercert.txt");
        X509Certificate cert = null;

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            cert = (X509Certificate) cf.generateCertificate(bis);
        } catch (Exception e) {
            throw new IllegalStateException("Could not load testing certificate.", e);
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }
        }
        return cert;
    }

    public static class SecurityConfiguration {
        public void configureHttpSecurity(@Observes SecurityConfigurationEvent event) {
            SecurityConfigurationBuilder builder = event.getBuilder();

            builder
                .http()
                .forPath("/x509ProtectedWithSubjectRegexUri/*")
                .authenticateWith()
                .x509()
                .subjectRegex("CN=(.*?), ")
                .forPath("/x509ProtectedUri/*")
                .authenticateWith()
                .x509();
        }
    }
}
