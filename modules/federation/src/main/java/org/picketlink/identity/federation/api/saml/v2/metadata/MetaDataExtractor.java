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
package org.picketlink.identity.federation.api.saml.v2.metadata;

import org.picketlink.common.exceptions.ProcessingException;
import org.picketlink.common.util.StaxUtil;
import org.picketlink.identity.federation.core.saml.v2.util.SAMLMetadataUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLMetadataWriter;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IndexedEndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SSODescriptorType;

import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Extract useful information out of metadata
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 29, 2009
 */
public class MetaDataExtractor {

    public static String LINE_SEPARATOR = SecurityActions.getSystemProperty("line.separator", "\n");

    /**
     * Get the {@link X509Certificate} from the KeyInfo
     *
     * @param keyDescriptor
     *
     * @return
     */
    public static X509Certificate getCertificate(KeyDescriptorType keyDescriptor) {
        try {
            return SAMLMetadataUtil.getCertificate(keyDescriptor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generate a string from the information in the metadata
     *
     * @param edt
     *
     * @return
     */
    public static String toString(EntityDescriptorType edt) {
        StringWriter sw = new StringWriter();
        try {
            XMLStreamWriter writer = StaxUtil.getXMLStreamWriter(sw);

            SAMLMetadataWriter metaWriter = new SAMLMetadataWriter(writer);
            metaWriter.writeEntityDescriptor(edt);
        } catch (ProcessingException e) {
            throw new RuntimeException(e);
        }

        return sw.toString();

        /*
         * StringBuilder builder = new StringBuilder(); List<RoleDescriptorType> rolesD =
         * edt.getRoleDescriptorOrIDPSSODescriptorOrSPSSODescriptor();
         *
         * for(RoleDescriptorType rdt: rolesD) { builder.append("ID=").append(rdt.getID()); builder.append(LINE_SEPARATOR);
         *
         * if(rdt instanceof IDPSSODescriptorType) { IDPSSODescriptorType idp = (IDPSSODescriptorType) rdt;
         * builder.append(toString(idp)); } if(rdt instanceof SPSSODescriptorType) { SPSSODescriptorType sp =
         * (SPSSODescriptorType) rdt; builder.append(toString(sp)); } }
         *
         * return builder.toString();
         */
    }

    /**
     * Information from the IDP SSO Descriptor
     *
     * @param idp
     *
     * @return
     */
    public static String toString(IDPSSODescriptorType idp) {
        StringBuilder builder = new StringBuilder();
        builder.append(LINE_SEPARATOR);

        // Get the SSODescriptor tags
        SSODescriptorType sdt = idp;
        builder.append(toString(sdt));

        List<EndpointType> ssoServices = idp.getSingleSignOnService();
        if (ssoServices != null) {
            builder.append("Single Sign On Services are:[");

            for (EndpointType edt : ssoServices) {
                builder.append(toString(edt));
            }
            builder.append("]");
            builder.append(LINE_SEPARATOR);
        }
        return builder.toString();
    }

    /**
     * Information from the SP SSO Descriptor
     *
     * @param sp
     *
     * @return
     */
    public static String toString(SPSSODescriptorType sp) {
        StringBuilder builder = new StringBuilder();
        builder.append(LINE_SEPARATOR);

        // Get the SSODescriptor tags
        SSODescriptorType sdt = sp;
        builder.append(toString(sdt));

        List<IndexedEndpointType> assertionConsumerServices = sp.getAssertionConsumerService();
        if (assertionConsumerServices != null) {
            builder.append("AssertionConsumer Services are:[");

            for (IndexedEndpointType edt : assertionConsumerServices) {
                builder.append(toString(edt));
            }
            builder.append("]");
            builder.append(LINE_SEPARATOR);
        }

        builder.append("AuthnRequests Signed=").append(sp.isAuthnRequestsSigned());
        builder.append(LINE_SEPARATOR);
        builder.append("Requires Assertions Signed=").append(sp.isWantAssertionsSigned());
        builder.append(LINE_SEPARATOR);

        return builder.toString();
    }

    /**
     * Information from the general SSO descriptor
     *
     * @param sso
     *
     * @return
     */
    public static String toString(SSODescriptorType sso) {
        StringBuilder builder = new StringBuilder();
        List<String> nameIDs = sso.getNameIDFormat();
        if (nameIDs != null) {
            for (String nameID : nameIDs) {
                builder.append("NameID=").append(nameID);
                builder.append(LINE_SEPARATOR);
            }
        }

        List<IndexedEndpointType> attrResServices = sso.getArtifactResolutionService();
        if (attrResServices != null) {
            builder.append("AttributeResolutionServices are:[");
            builder.append(LINE_SEPARATOR);
            for (IndexedEndpointType iet : attrResServices) {
                builder.append(toString(iet));
            }
            builder.append("]");
        }

        List<EndpointType> sloServices = sso.getSingleLogoutService();
        if (sloServices != null) {
            builder.append("Single Logout Services are:[");
            builder.append(LINE_SEPARATOR);

            for (EndpointType edt : sloServices) {
                builder.append(toString(edt));
            }
            builder.append("]");
            builder.append(LINE_SEPARATOR);
        }
        return builder.toString();
    }

    /**
     * Information from an endpoint
     *
     * @param ept
     *
     * @return
     */
    public static String toString(EndpointType ept) {
        StringBuilder builder = new StringBuilder();
        builder.append("[Location=").append(ept.getLocation());

        builder.append(",ResponseLocation=").append(ept.getResponseLocation());
        builder.append("]");
        builder.append(LINE_SEPARATOR);
        return builder.toString();
    }
}