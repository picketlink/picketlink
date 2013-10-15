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
package org.picketlink.test.identity.federation.api.saml.v2.metadata;

import org.junit.Test;
import org.picketlink.common.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.api.saml.v2.metadata.KeyDescriptorMetaDataBuilder;
import org.picketlink.identity.federation.api.saml.v2.metadata.MetaDataBuilder;
import org.picketlink.identity.federation.api.w3.xmldsig.KeyInfoBuilder;
import org.picketlink.identity.federation.saml.v2.assertion.AttributeType;
import org.picketlink.identity.federation.saml.v2.metadata.EndpointType;
import org.picketlink.identity.federation.saml.v2.metadata.EntityDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.IDPSSODescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.picketlink.identity.federation.saml.v2.metadata.OrganizationType;
import org.picketlink.identity.federation.saml.v2.metadata.SPSSODescriptorType;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test the MetaDataBuilder API
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 20, 2009
 */
public class MetaDataBuilderUnitTestCase {

    String organizationName = "JBoss";
    String organizationDisplayName = "JBoss Unit";
    String organizationURL = "http://www.jboss.org";
    String lang = "en";

    @Test
    public void testCreateOrganization() {
        OrganizationType org = createJBossOrganization("en");

        assertNotNull("Org is not null", org);
        assertEquals(organizationName, org.getOrganizationName().get(0).getValue());
        assertEquals(organizationDisplayName, org.getOrganizationDisplayName().get(0).getValue());
        assertEquals(organizationURL, org.getOrganizationURL().get(0).getValue().toString());

        // Check the lang
        assertEquals(lang, org.getOrganizationName().get(0).getLang());
        assertEquals(lang, org.getOrganizationDisplayName().get(0).getLang());
        assertEquals(lang, org.getOrganizationURL().get(0).getLang());
    }

    @Test
    public void testCreateEntityDescriptor() {
        IDPSSODescriptorType idp = this.createIDPSSODescriptor();
        EntityDescriptorType idpEntity = MetaDataBuilder.createEntityDescriptor(idp);
        assertNotNull("IDP Entity Descriptor not null", idpEntity);

        SPSSODescriptorType sp = this.createSPSSODescriptor();
        EntityDescriptorType spEntity = MetaDataBuilder.createEntityDescriptor(sp);
        assertNotNull("SP Entity Descriptor not null", spEntity);
    }

    @Test
    public void testCreateIDPSSODescriptor() {
        IDPSSODescriptorType idp = this.createIDPSSODescriptor();
        assertNotNull("IDPSSODescriptor is not null", idp);
    }

    @Test
    public void testCreateSPSSODescriptor() {
        SPSSODescriptorType sp = createSPSSODescriptor();

        assertNotNull("IDPSSODescriptor is not null", sp);
    }

    private SPSSODescriptorType createSPSSODescriptor() {
        String id = "test-key";

        // TODO: improve keyinfo
        Element keyInfo = KeyInfoBuilder.createKeyInfo(id);

        String algorithm = null;

        KeyDescriptorType keyDescriptorType = KeyDescriptorMetaDataBuilder.createKeyDescriptor(keyInfo, algorithm, 0, true,
                false);

        List<AttributeType> attributes = new ArrayList<AttributeType>();

        EndpointType sloEndPoint = MetaDataBuilder.createEndpoint(JBossSAMLURIConstants.METADATA_HTTP_REDIRECT_BINDING.get(),
                "https://SProvider.com/SAML/SLO/Browser", "https://SProvider.com/SAML/SLO/Response");

        SPSSODescriptorType sp = MetaDataBuilder.createSPSSODescriptor(true, keyDescriptorType, sloEndPoint, attributes,
                createJBossOrganization(lang));
        return sp;
    }

    private OrganizationType createJBossOrganization(String language) {
        return MetaDataBuilder.createOrganization(organizationName, organizationDisplayName, organizationURL, language);
    }

    private IDPSSODescriptorType createIDPSSODescriptor() {
        String id = "test-key";

        // TODO: improve keyinfo
        Element keyInfo = KeyInfoBuilder.createKeyInfo(id);

        String algorithm = null;

        KeyDescriptorType keyDescriptorType = KeyDescriptorMetaDataBuilder.createKeyDescriptor(keyInfo, algorithm, 0, true,
                false);

        List<AttributeType> attributes = new ArrayList<AttributeType>();

        EndpointType ssoEndPoint = MetaDataBuilder.createEndpoint(JBossSAMLURIConstants.METADATA_HTTP_REDIRECT_BINDING.get(),
                "https://IdentityProvider.com/SAML/SSO/Browser", "https://IdentityProvider.com/SAML/SSO/Response");

        EndpointType sloEndPoint = MetaDataBuilder.createEndpoint(JBossSAMLURIConstants.METADATA_HTTP_REDIRECT_BINDING.get(),
                "https://IdentityProvider.com/SAML/SLO/Browser", "https://IdentityProvider.com/SAML/SLO/Response");

        return MetaDataBuilder.createIDPSSODescriptor(true, keyDescriptorType, ssoEndPoint, sloEndPoint, attributes,
                createJBossOrganization(lang));
    }
}