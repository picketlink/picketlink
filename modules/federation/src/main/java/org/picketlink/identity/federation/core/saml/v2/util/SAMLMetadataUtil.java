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
package org.picketlink.identity.federation.core.saml.v2.util;

import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.common.exceptions.ConfigurationException;
import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.util.XMLSignatureUtil;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.security.cert.X509Certificate;

/**
 * Deals with SAML2 Metadata
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jan 31, 2011
 */
public class SAMLMetadataUtil {

    /**
     * Get the {@link X509Certificate} from the KeyInfo
     *
     * @param keyDescriptor
     *
     * @return
     *
     * @throws ProcessingException
     * @throws ConfigurationException
     */
    public static X509Certificate getCertificate(KeyDescriptorType keyDescriptor) throws ConfigurationException,
            ProcessingException {
        X509Certificate cert = null;
        Element keyInfo = keyDescriptor.getKeyInfo();
        if (keyInfo != null) {
            NodeList x509DataNodes = keyInfo.getElementsByTagName("X509Data");
            if (x509DataNodes == null || x509DataNodes.getLength() == 0) {
                x509DataNodes = keyInfo.getElementsByTagNameNS(JBossSAMLURIConstants.XMLDSIG_NSURI.get(), "X509Data");
            }

            if (x509DataNodes == null || x509DataNodes.getLength() == 0) {
                x509DataNodes = keyInfo.getElementsByTagName("ds:X509Data");
            }

            if (x509DataNodes != null && x509DataNodes.getLength() > 0) {
                // Choose the first one
                Node x509DataNode = x509DataNodes.item(0);
                NodeList children = x509DataNode.getChildNodes();
                int len = children != null ? children.getLength() : 0;
                for (int i = 0; i < len; i++) {
                    Node nl = children.item(i);
                    if (nl.getNodeName().contains("X509Certificate")) {
                        Node certNode = nl.getFirstChild();
                        String certNodeValue = certNode.getNodeValue();
                        cert = XMLSignatureUtil.getX509CertificateFromKeyInfoString(certNodeValue.replaceAll("\\s", ""));
                        break;
                    }
                }
            }
        }
        return cert;
    }
}