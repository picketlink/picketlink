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

import org.picketlink.identity.federation.core.saml.md.providers.MetaDataBuilderDelegate;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.OrganizationType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SSODescriptorType;

import java.util.List;

/**
 * SAML2 Metadata Builder API
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 19, 2009
 */
public class MetaDataBuilder {

    /**
     * Create an Endpoint (SingleSignOnEndpoint or SingleLogoutEndpoint)
     *
     * @param binding
     * @param location
     * @param responseLocation
     *
     * @return
     */
    public static EndpointType createEndpoint(String binding, String location, String responseLocation) {
        return MetaDataBuilderDelegate.createEndpoint(binding, location, responseLocation);
    }

    /**
     * Create an Organization
     *
     * @param organizationName
     * @param organizationDisplayName
     * @param organizationURL
     * @param lang
     *
     * @return
     */
    public static OrganizationType createOrganization(String organizationName, String organizationDisplayName,
                                                      String organizationURL, String lang) {
        return MetaDataBuilderDelegate.createOrganization(organizationName, organizationDisplayName, organizationURL, lang);
    }

    /**
     * Create an Entity Descriptor
     *
     * @param idpOrSPDescriptor a descriptor for either the IDP or SSO
     *
     * @return
     */
    public static EntityDescriptorType createEntityDescriptor(SSODescriptorType idpOrSPDescriptor) {
        return MetaDataBuilderDelegate.createEntityDescriptor(idpOrSPDescriptor);
    }

    /**
     * Create a IDP SSO metadata descriptor
     *
     * @param requestsSigned
     * @param keyDescriptorType
     * @param ssoEndPoint
     * @param sloEndPoint
     * @param attributes
     * @param org
     *
     * @return
     */
    public static IDPSSODescriptorType createIDPSSODescriptor(boolean requestsSigned, KeyDescriptorType keyDescriptorType,
                                                              EndpointType ssoEndPoint, EndpointType sloEndPoint, List<AttributeType> attributes, OrganizationType org) {
        return MetaDataBuilderDelegate.createIDPSSODescriptor(requestsSigned, keyDescriptorType, ssoEndPoint, sloEndPoint,
                attributes, org);
    }

    /**
     * Create a IDP SSO metadata descriptor
     *
     * @param requestsSigned
     * @param keyDescriptorType
     * @param ssoEndPoint
     * @param sloEndPoint
     * @param attributes
     * @param org
     *
     * @return
     */
    public static SPSSODescriptorType createSPSSODescriptor(boolean requestsSigned, KeyDescriptorType keyDescriptorType,
                                                            EndpointType sloEndPoint, List<AttributeType> attributes, OrganizationType org) {
        return MetaDataBuilderDelegate.createSPSSODescriptor(requestsSigned, keyDescriptorType, sloEndPoint, attributes, org);
    }
}