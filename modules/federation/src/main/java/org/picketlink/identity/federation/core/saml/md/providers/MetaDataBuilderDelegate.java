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
package org.picketlink.identity.federation.core.saml.md.providers;

import org.picketlink.common.PicketLinkLogger;
import org.picketlink.common.PicketLinkLoggerFactory;
import org.picketlink.common.constants.JBossSAMLConstants;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType.EDTDescriptorChoiceType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.LocalizedNameType;
import org.picketlink.identity.federation.saml.v2.metadata.LocalizedURIType;
import org.picketlink.identity.federation.saml.v2.metadata.OrganizationType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.SSODescriptorType;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * SAML2 Metadata Builder API
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 19, 2009
 */
public class MetaDataBuilderDelegate {

    private static final PicketLinkLogger logger = PicketLinkLoggerFactory.getLogger();

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
        EndpointType endpoint = new EndpointType(URI.create(binding), URI.create(location));
        endpoint.setResponseLocation(URI.create(responseLocation));
        return endpoint;
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
        if (organizationName == null)
            throw logger.nullArgumentError("organizationName");
        if (organizationDisplayName == null)
            throw logger.nullArgumentError("organizationDisplayName");
        if (organizationURL == null)
            throw logger.nullArgumentError("organizationURL");
        if (lang == null)
            lang = JBossSAMLConstants.LANG_EN.get();

        // orgName
        LocalizedNameType orgName = new LocalizedNameType(lang);
        orgName.setValue(organizationName);

        // orgDisplayName
        LocalizedNameType orgDisplayName = new LocalizedNameType(lang);
        orgDisplayName.setValue(organizationDisplayName);

        // orgURL
        LocalizedURIType orgURL = new LocalizedURIType(lang);
        orgURL.setValue(URI.create(organizationURL));

        OrganizationType orgType = new OrganizationType();
        orgType.addOrganizationName(orgName);
        orgType.addOrganizationDisplayName(orgDisplayName);
        orgType.addOrganizationURL(orgURL);
        return orgType;
    }

    /**
     * Create an Entity Descriptor
     *
     * @param idpOrSPDescriptor a descriptor for either the IDP or SSO
     *
     * @return
     */
    public static EntityDescriptorType createEntityDescriptor(SSODescriptorType idpOrSPDescriptor) {
        EDTDescriptorChoiceType edtDescriptorChoiceType = new EDTDescriptorChoiceType(idpOrSPDescriptor);

        List<EDTDescriptorChoiceType> edtList = new ArrayList<EntityDescriptorType.EDTDescriptorChoiceType>();
        edtList.add(edtDescriptorChoiceType);

        EDTChoiceType choiceType = new EDTChoiceType(edtList);

        EntityDescriptorType entity = new EntityDescriptorType(" ");
        entity.addChoiceType(choiceType);
        return entity;
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
        List<String> emptyList = new ArrayList<String>();
        IDPSSODescriptorType idp = new IDPSSODescriptorType(emptyList);
        idp.addSingleSignOnService(ssoEndPoint);
        idp.addSingleLogoutService(sloEndPoint);

        for (AttributeType attr : attributes) {
            idp.addAttribute(attr);
        }
        idp.addKeyDescriptor(keyDescriptorType);
        idp.setWantAuthnRequestsSigned(requestsSigned);
        idp.setOrganization(org);
        return idp;
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
        List<String> protocolEnumList = new ArrayList<String>();
        protocolEnumList.add(JBossSAMLURIConstants.PROTOCOL_NSURI.get());

        SPSSODescriptorType sp = new SPSSODescriptorType(protocolEnumList);
        sp.addSingleLogoutService(sloEndPoint);
        sp.addKeyDescriptor(keyDescriptorType);
        sp.setAuthnRequestsSigned(requestsSigned);
        sp.setOrganization(org);
        return sp;
    }
}