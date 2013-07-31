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

package org.picketlink.test.config.idm;

import org.junit.Test;
import org.picketlink.common.exceptions.ParsingException;
import org.picketlink.config.PicketLinkConfigParser;
import org.picketlink.config.federation.IDPType;
import org.picketlink.config.federation.PicketLinkType;
import org.picketlink.config.idm.ConfigBuilderMethodType;
import org.picketlink.config.idm.IDMType;
import org.picketlink.config.idm.XMLConfigurationProvider;
import org.picketlink.idm.config.FileIdentityStoreConfiguration;
import org.picketlink.idm.config.IdentityConfiguration;
import org.picketlink.idm.config.IdentityConfigurationBuilder;
import org.picketlink.idm.config.IdentityStoreConfiguration;
import org.picketlink.idm.config.NamedIdentityConfigurationBuilder;
import org.picketlink.idm.model.Relationship;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test for parsing of IDM configuration in picketlink.xml file.
 * It just tests parsing (more complex test is XMLConfigurationTestCase in idm/tests module)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class IDMConfigParserTestCase {

    @Test
    public void testParseIDMConfiguration() throws ParsingException {
        System.setProperty("property.existing", "org.picketlink.idm.jpa.schema.IdentityObject");

        ClassLoader tcl = Thread.currentThread().getContextClassLoader();
        InputStream configStream = tcl.getResourceAsStream("parser/config/picketlink.xml");
        PicketLinkConfigParser parser = new PicketLinkConfigParser();
        Object result = parser.parse(configStream);
        assertNotNull(result);
        PicketLinkType picketlink = (PicketLinkType) result;

        // Check that other types (handlers, IDP config and STS) are presented
        IDPType idp = (IDPType) picketlink.getIdpOrSP();
        assertNotNull(idp);
        assertFalse(picketlink.isEnableAudit());
        assertNotNull(picketlink.getStsType());
        assertNotNull(picketlink.getHandlers());

        // test IDM configuration
        IDMType idmType = picketlink.getIdmType();
        assertNotNull(idmType);

        assertTrue(idmType.getBuilderMethods().size() > 9);
        ConfigBuilderMethodType named = idmType.getBuilderMethods().get(0);
        ConfigBuilderMethodType bindCredential = idmType.getBuilderMethods().get(5);
        ConfigBuilderMethodType attribute = idmType.getBuilderMethods().get(9);

        assertEquals("named", named.getMethodId());
        assertEquals("SIMPLE_LDAP_STORE_CONFIG", named.getMethodParameters().get("value"));
        assertEquals("bindCredential", bindCredential.getMethodId());
        assertEquals("secret", bindCredential.getMethodParameters().get("value"));
        assertEquals("attribute", attribute.getMethodId());
        assertNull(attribute.getMethodParameters().get("value"));
        assertEquals("UID", attribute.getMethodParameters().get("ldapAttributeName"));
    }
}
