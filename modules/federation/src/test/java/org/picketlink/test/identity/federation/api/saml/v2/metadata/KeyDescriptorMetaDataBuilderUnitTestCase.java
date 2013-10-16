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
import org.picketlink.identity.federation.api.saml.v2.metadata.KeyDescriptorMetaDataBuilder;
import org.picketlink.identity.federation.api.w3.xmldsig.KeyInfoBuilder;
import org.picketlink.identity.federation.saml.v2.metadata.KeyDescriptorType;
import org.w3c.dom.Element;

import static org.junit.Assert.assertNotNull;

/**
 * Unit Test the KeyDescriptorMetaDataBuilder
 *
 * @author Anil.Saldhana@redhat.com
 * @since Apr 20, 2009
 */
public class KeyDescriptorMetaDataBuilderUnitTestCase {

    @Test
    public void testCreateKeyDescriptor() {
        Element keyInfo = KeyInfoBuilder.createKeyInfo("testKey");

        String algorithm = "http://www.w3.org/2001/04/xmlenc#rsa-1_5";

        KeyDescriptorType keyDescriptor = KeyDescriptorMetaDataBuilder
                .createKeyDescriptor(keyInfo, algorithm, 256, false, true);
        assertNotNull("Key Descriptor not null", keyDescriptor);
    }

}