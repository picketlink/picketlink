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
package org.picketlink.identity.federation.core.wstrust;

import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.picketlink.common.util.DocumentUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;

/**
 * Unit test for {@link SamlCredential}.
 *
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class SamlCredentialTestCase extends TestCase {

    private Element assertionElement;
    private InputSource expectedAssertion;

    public void setUp() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        final Document assertionDoc = DocumentUtil.getDocument(getClass().getResourceAsStream("/wstrust/assertion.xml"));
        assertionElement = (Element) assertionDoc.getFirstChild();
        expectedAssertion = new InputSource(getClass().getResourceAsStream("/wstrust/assertion-expected.xml"));
    }

    public void testStringConstructor() throws Exception {
        final SamlCredential samlPrincipal = new SamlCredential(DocumentUtil.getNodeAsString(assertionElement));

        final InputSource actual = new InputSource(new StringReader(samlPrincipal.getAssertionAsString()));
        XMLAssert.assertXMLEqual(expectedAssertion, actual);
    }

    public void testElementConstructor() throws Exception {
        final SamlCredential samlPrincipal = new SamlCredential(assertionElement);

        final InputSource actual = new InputSource(new StringReader(samlPrincipal.getAssertionAsString()));
        XMLAssert.assertXMLEqual(expectedAssertion, actual);
    }

    public void testShouldThrowIfStringIsNull() {
        try {
            new SamlCredential((String) null);
            fail("Should not be allowed to create a SamlCredential with a null token string");
        } catch (final Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    public void testEqualsContract() throws Exception {
        final SamlCredential samlPrincipal1 = new SamlCredential(assertionElement);
        final SamlCredential samlPrincipal2 = new SamlCredential(assertionElement);
        assertEquals(samlPrincipal1, samlPrincipal2);
        assertEquals(samlPrincipal1.hashCode(), samlPrincipal2.hashCode());
    }

}
