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
package org.picketlink.authentication.web;

import java.io.IOException;
import java.security.cert.X509Certificate;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.X509CertificateCredentials;

/**
 * <p>An implementation of {@link org.picketlink.authentication.web.HTTPAuthenticationScheme} that supports the Servlet Specification
 * CLIENT-CERT Authentication Scheme</p>
 * <p>When using this authentication scheme, the container must be properly configured to validate client certificates.</p>
 *
 * @author Anil Saldhana
 * @author Pedro Igor
 */
public class ClientCertAuthenticationScheme implements HTTPAuthenticationScheme {

    public static final String X509_CLIENT_CERT_REQUEST_ATTRIBUTE = "javax.servlet.request.X509Certificate";

    public ClientCertAuthenticationScheme(FilterConfig config) {

    }

    @Override
    public void extractCredential(HttpServletRequest request, DefaultLoginCredentials creds) {
        X509Certificate[] clientCerts = getClientCertificate(request);

        if (clientCerts != null && clientCerts.length > 0) {
            X509CertificateCredentials credential = new X509CertificateCredentials(clientCerts[0]);

            credential.setTrusted(true);

            creds.setCredential(credential);
        }
    }

    @Override
    public void challengeClient(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "The requested resource requires a valid certificate.");
    }

    @Override
    public boolean postAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return true;
    }

    private X509Certificate[] getClientCertificate(HttpServletRequest request) {
        return (X509Certificate[]) request.getAttribute(X509_CLIENT_CERT_REQUEST_ATTRIBUTE);
    }

}