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
package org.picketlink.identity.federation.api.util;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.Base64;
import org.picketlink.common.util.DocumentUtil;
import org.w3c.dom.Element;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Utility dealing with PublicKey/Certificates and xml-dsig KeyInfoType
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 29, 2009
 */
public class KeyUtil {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

    private static String EOL = getSystemProperty("line.separator", "\n");

    /**
     * Base64 encode the certificate
     *
     * @param certificate
     *
     * @return
     *
     * @throws CertificateEncodingException
     */
    public static String encodeAsString(Certificate certificate) throws CertificateEncodingException {
        return Base64.encodeBytes(certificate.getEncoded());
    }

    /**
     * Given a certificate, build a keyinfo type
     *
     * @param certificate
     *
     * @return
     *
     * @throws CertificateException
     * @throws ProcessingException
     * @throws ParsingException
     * @throws ConfigurationException
     */
    public static Element getKeyInfo(Certificate certificate) throws CertificateException, ConfigurationException,
            ParsingException, ProcessingException {
        if (certificate == null)
            throw logger.nullArgumentError("certificate is null");

        StringBuilder builder = new StringBuilder();

        if (certificate instanceof X509Certificate) {
            X509Certificate x509 = (X509Certificate) certificate;

            // Add the binary encoded x509 cert
            String certStr = Base64.encodeBytes(x509.getEncoded(), 76);

            builder.append("<KeyInfo xmlns=\'http://www.w3.org/2000/09/xmldsig#\'>").append(EOL).append("<X509Data>")
                    .append(EOL).append("<X509Certificate>").append(EOL).append(certStr).append(EOL)
                    .append("</X509Certificate>").append("</X509Data>").append("</KeyInfo>");
        } else
            throw logger.notImplementedYet("Only X509Certificate are supported");

        return DocumentUtil.getDocument(builder.toString()).getDocumentElement();
    }

    /**
     * Get the system property
     *
     * @param key
     * @param defaultValue
     *
     * @return
     */
    static String getSystemProperty(final String key, final String defaultValue) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return System.getProperty(key, defaultValue);
            }
        });
    }
}
