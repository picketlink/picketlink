/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors. 
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.picketlink.test.identity.federation.core.parser.wst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.datatype.DatatypeFactory;

import org.junit.Test;
import org.picketlink.identity.federation.core.parsers.wst.WSTrustParser;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.plugins.saml.SAMLUtil;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.writers.WSTrustRequestWriter;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.ConditionsType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectConfirmationType;
import org.picketlink.identity.federation.saml.v2.assertion.SubjectType;
import org.picketlink.identity.federation.ws.trust.CancelTargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Validate the WST Cancel Target for SAML assertions
 * @author Anil.Saldhana@redhat.com
 * @since Oct 14, 2010
 */
public class WSTrustCancelTargetSamlTestCase
{
   @Test
   public void testWST_CancelTargetSaml() throws Exception
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream configStream = tcl.getResourceAsStream("parser/wst/wst-cancel-saml.xml");

      WSTrustParser parser = new WSTrustParser();
      RequestSecurityToken requestToken = (RequestSecurityToken) parser.parse(configStream);
      assertEquals("cancelcontext", requestToken.getContext());
      assertEquals(WSTrustConstants.CANCEL_REQUEST, requestToken.getRequestType().toASCIIString());

      CancelTargetType cancelTarget = requestToken.getCancelTarget();

      Element assertionElement = (Element) cancelTarget.getAny().get(0);
      AssertionType assertion = SAMLUtil.fromElement(assertionElement);
      validateAssertion(assertion);

      //Now for the writing part
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      WSTrustRequestWriter rstWriter = new WSTrustRequestWriter(baos);

      rstWriter.write(requestToken);

      Document doc = DocumentUtil.getDocument(new ByteArrayInputStream(baos.toByteArray()));
      JAXPValidationUtil.validate(DocumentUtil.getNodeAsStream(doc));
   }

   private void validateAssertion(AssertionType assertion) throws Exception
   {
      DatatypeFactory dtf = DatatypeFactory.newInstance();

      assertNotNull(assertion);

      assertEquals("ID_cb1eadf5-50a6-4fdf-96bc-412514f52882", assertion.getID());
      assertEquals(dtf.newXMLGregorianCalendar("2010-09-30T19:13:37.603Z"), assertion.getIssueInstant());
      //Issuer
      assertEquals("Test STS", assertion.getIssuer().getValue());

      //Subject
      SubjectType subject = assertion.getSubject();

      NameIDType subjectNameID = (NameIDType) subject.getSubType().getBaseID();

      assertEquals("jduke", subjectNameID.getValue());
      assertEquals("urn:picketlink:identity-federation", subjectNameID.getNameQualifier());

      SubjectConfirmationType subjectConfirmationType = subject.getConfirmation().get(0);
      assertEquals(JBossSAMLURIConstants.BEARER.get(), subjectConfirmationType.getMethod());

      /*List<JAXBElement<?>> content = subject.getContent(); 
      
      int size = content.size();
      
      assertEquals( 2, size );
      
      for( int i = 0 ; i < size; i++ )
      {
         JAXBElement<?> node = content.get(i);
         if( node.getDeclaredType().equals( NameIDType.class ))
         {
            NameIDType subjectNameID = (NameIDType) node.getValue();
            
            assertEquals( "jduke", subjectNameID.getValue() );
            assertEquals( "urn:picketlink:identity-federation", subjectNameID.getNameQualifier() ); 
         }
         
         if( node.getDeclaredType().equals( SubjectConfirmationType.class ))
         {
            SubjectConfirmationType subjectConfirmationType = (SubjectConfirmationType) node.getValue();
            assertEquals( JBossSAMLURIConstants.BEARER.get(), subjectConfirmationType.getMethod() );
         }
      } */

      //Conditions
      ConditionsType conditions = assertion.getConditions();
      assertEquals(dtf.newXMLGregorianCalendar("2010-09-30T19:13:37.603Z"), conditions.getNotBefore());
      assertEquals(dtf.newXMLGregorianCalendar("2010-09-30T21:13:37.603Z"), conditions.getNotOnOrAfter());
   }
}