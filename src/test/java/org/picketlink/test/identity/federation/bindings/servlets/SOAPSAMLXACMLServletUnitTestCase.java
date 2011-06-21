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
package org.picketlink.test.identity.federation.bindings.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.jboss.security.xacml.core.model.context.DecisionType;
import org.jboss.security.xacml.core.model.context.ResultType;
import org.junit.Test;
import org.picketlink.identity.federation.bindings.servlets.SOAPSAMLXACMLServlet;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.SOAPSAMLXACMLUtil;
import org.picketlink.identity.federation.core.util.SOAPUtil;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion.XACMLAuthzDecisionStatementType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Unit Test the SOAP SAML XACML Servlet
 * @author Anil.Saldhana@redhat.com
 * @since Jan 28, 2009
 */
public class SOAPSAMLXACMLServletUnitTestCase
{
   @Test
   public void testPermit() throws Exception
   {
      validate("xacml/requests/XacmlRequest-01-01.xml", DecisionType.PERMIT.value(), true);

      validate("xacml/requests/XacmlRequest-format2-01-01.xml", DecisionType.PERMIT.value(), true);
   }

   @Test
   public void testDeny() throws Exception
   {
      validate("xacml/requests/XacmlRequest-01-02.xml", DecisionType.DENY.value(), true);
   }

   @Test
   public void testIncorrectInput() throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      String garbage = "<fdfdsfdfk/>";
      ByteArrayInputStream bis = new ByteArrayInputStream(garbage.getBytes());

      SOAPSAMLXACMLServlet servlet = new SOAPSAMLXACMLServlet();
      servlet.init(new TestServletConfig(getServletContext()));
      ServletRequest sreq = new TestServletRequest(getSOAPStream(bis));
      ServletResponse sresp = new TestServletResponse(baos);
      servlet.service(sreq, sresp);

      sresp.flushBuffer(); //Flush the servlet response ServletOutputStream to our baos

      bis = new ByteArrayInputStream(baos.toByteArray());

      SOAPMessage soapMessage = SOAPUtil.getSOAPMessage(bis);
      Node xacmlNode = soapMessage.getSOAPBody().getChildNodes().item(0);
      assertTrue(xacmlNode instanceof Element);
      Element xacmlElement = (Element) xacmlNode;
      assertTrue(xacmlElement.getLocalName().equals("Fault"));
      /*Unmarshaller un = JAXBUtil.getUnmarshaller(SOAPSAMLXACMLUtil.getPackage());
      JAXBElement<Envelope> jax = (JAXBElement<Envelope>) un.unmarshal(bis);
      Envelope envelope = jax.getValue();
      assertNotNull("Envelope is not null", envelope); 
      JAXBElement<?> fault = (JAXBElement<?>) envelope.getBody().getAny().get(0);
      assertTrue(fault.getValue() instanceof Fault);*/
   }

   @Test
   public void testInteropSOAPRequest() throws Exception
   {
      validate("xacml/requests/interop-request.xml", DecisionType.PERMIT.value(), false);
   }

   private void validate(String requestFile, String value, boolean needSOAPWrapping) throws Exception
   {
      InputStream is = getInputStream(requestFile);
      if (is == null)
         throw new IllegalArgumentException("Input Stream to request file is null");

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      SOAPSAMLXACMLServlet servlet = new SOAPSAMLXACMLServlet();
      servlet.init(new TestServletConfig(getServletContext()));

      if (needSOAPWrapping)
         is = getSOAPStream(is);

      ServletRequest sreq = new TestServletRequest(is);
      ServletResponse sresp = new TestServletResponse(baos);
      servlet.service(sreq, sresp);

      sresp.flushBuffer(); //Flush the servlet response ServletOutputStream to our baos

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());

      SOAPMessage soapMessage = SOAPUtil.getSOAPMessage(bis);

      Node xacmlNode = soapMessage.getSOAPBody().getChildNodes().item(0);
      XACMLAuthzDecisionStatementType xacmlStatement = SOAPSAMLXACMLUtil.getDecisionStatement(xacmlNode);
      /*Unmarshaller un = JAXBUtil.getUnmarshaller(SOAPSAMLXACMLUtil.getPackage());
      JAXBElement<Envelope> jax = (JAXBElement<Envelope>) un.unmarshal(bis);
      Envelope envelope = jax.getValue();
      assertNotNull("Envelope is not null", envelope);
      
      JAXBElement<ResponseType> jaxbResponseType = (JAXBElement<ResponseType>) envelope.getBody().getAny().get(0);
      ResponseType responseType = jaxbResponseType.getValue();
      
      assertNotNull("ResponseType is not null", responseType); 
      AssertionType assertion = (AssertionType) responseType.getAssertionOrEncryptedAssertion().get(0);
      XACMLAuthzDecisionStatementType xacmlStatement = (XACMLAuthzDecisionStatementType) assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement().get(0);
      */

      assertNotNull("XACML Authorization Statement is not null", xacmlStatement);
      org.jboss.security.xacml.core.model.context.ResponseType xacmlResponse = xacmlStatement.getResponse();
      ResultType resultType = xacmlResponse.getResult().get(0);
      DecisionType decision = resultType.getDecision();
      assertNotNull("Decision is not null", decision);
      assertEquals(value, decision.value());
   }

   private ServletContext getServletContext()
   {
      HashMap<String, String> map = new HashMap<String, String>();
      map.put("policyConfigFileName", "xacml/policies/config/rsaConfPolicyConfig.xml");
      return new TestServletContext(map);
   }

   private InputStream getInputStream(String requestFileLoc)
   {
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      return tcl.getResourceAsStream(requestFileLoc);
   }

   private InputStream getSOAPStream(InputStream dataStream) throws Exception
   {
      SOAPMessage message = SOAPUtil.create();
      SOAPPart soapPart = message.getSOAPPart();
      SOAPEnvelope envelope = soapPart.getEnvelope();
      SOAPBody body = envelope.getBody();

      body.addDocument(DocumentUtil.getDocument(dataStream));
      message.saveChanges();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      message.writeTo(baos);

      return new ByteArrayInputStream(baos.toByteArray());
   }
}