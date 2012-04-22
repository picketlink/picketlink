/*
 * JBoss, Home of Professional Open Source Copyright 2009, Red Hat Middleware
 * LLC, and individual contributors by the @authors tag. See the copyright.txt
 * in the distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.wstrust;

import java.io.StringReader;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.picketlink.identity.federation.core.wstrust.SamlCredential;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Unit test for {@link SamlCredential}.
 * 
 * @author <a href="mailto:dbevenius@jboss.com">Daniel Bevenius</a>
 */
public class SamlCredentialTestCase extends TestCase
{
    private Element assertionElement;
    private InputSource expectedAssertion;
    
    public void setUp() throws Exception
    {
        XMLUnit.setIgnoreWhitespace(true);
        final Document assertionDoc = DocumentUtil.getDocument(getClass().getResourceAsStream("/wstrust/assertion.xml"));
        assertionElement = (Element) assertionDoc.getFirstChild();
        expectedAssertion = new InputSource(getClass().getResourceAsStream("/wstrust/assertion-expected.xml"));
    }
    
    public void testStringConstructor() throws Exception
    {
        final SamlCredential samlPrincipal = new SamlCredential(DocumentUtil.getNodeAsString(assertionElement));
        
        final InputSource actual = new InputSource(new StringReader(samlPrincipal.getAssertionAsString()));
        XMLAssert.assertXMLEqual(expectedAssertion, actual);
    }
    
    public void testElementConstructor() throws Exception
    {
        final SamlCredential samlPrincipal = new SamlCredential(assertionElement);
        
        final InputSource actual = new InputSource(new StringReader(samlPrincipal.getAssertionAsString()));
        XMLAssert.assertXMLEqual(expectedAssertion, actual);
    }
    
    public void testShouldThrowIfStringIsNull()
    {
        try
        {
	        new SamlCredential((String)null);
	        fail("Should not be allowed to create a SamlCredential with a null token string");
        }
        catch(final Exception e)
        {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }
    
    public void testEqualsContract() throws Exception
    {
        final SamlCredential samlPrincipal1 = new SamlCredential(assertionElement);
        final SamlCredential samlPrincipal2 = new SamlCredential(assertionElement);
        assertEquals(samlPrincipal1, samlPrincipal2);
        assertEquals(samlPrincipal1.hashCode(), samlPrincipal2.hashCode());
    }
    
}

