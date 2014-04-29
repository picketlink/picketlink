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
package org.picketlink.test.authentication.web.mock;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.picketlink.authentication.web.AuthenticationFilter;
import org.picketlink.authentication.web.ClientCertAuthenticationScheme;
import org.picketlink.common.util.Base64;

/**
 * @author pedroigor
 */
public class MockClientCertAuthenticationFilter extends AuthenticationFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        super.doFilter(new HttpServletRequestWrapper((HttpServletRequest) servletRequest) {
            @Override
            public Object getAttribute(String name) {
                if (name.equals(ClientCertAuthenticationScheme.X509_CLIENT_CERT_REQUEST_ATTRIBUTE)) {
                    String parameter = getParameter("x-client-cert");

                    if (parameter != null) {
                        return new X509Certificate[] {(X509Certificate) Base64.decodeToObject(parameter)};
                    }
                }

                return super.getAttribute(name);
            }
        }, servletResponse, chain);
    }

    public static X509Certificate getTestingCertificate(ClassLoader classLoader, String fromTextFile) {
        // Certificate
        InputStream bis = classLoader.getResourceAsStream(fromTextFile);
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

}
