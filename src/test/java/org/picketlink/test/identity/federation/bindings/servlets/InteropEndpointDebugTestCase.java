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

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.picketlink.identity.federation.core.saml.v2.util.SOAPSAMLXACMLUtil;
import org.picketlink.identity.federation.core.util.JAXBUtil;
import org.picketlink.identity.federation.org.xmlsoap.schemas.soap.envelope.Envelope;
import org.picketlink.identity.federation.org.xmlsoap.schemas.soap.envelope.Fault;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.assertion.XACMLAuthzDecisionStatementType;
import org.picketlink.identity.federation.saml.v2.profiles.xacml.protocol.XACMLAuthzDecisionQueryType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.jboss.security.xacml.core.model.context.DecisionType;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResultType;


/**
 * Test Case that acts as a debug tool
 * for the endpoint for interop
 * @author Anil.Saldhana@redhat.com
 * @since Mar 16, 2009
 */
public class InteropEndpointDebugTestCase extends TestCase
{   
   String endpoint = null; 

   //String endpoint = "http://interop.demo.jboss.com/test/SOAPServlet";
   //String endpoint = "http://localhost:8080/test/SOAPServlet";
 
    
   public void testUseCase1() throws Exception
   {
     if(endpoint != null)
     {  
        JAXBElement<?> jb = getResponse("xacml/requests/interop-request.xml");
        Envelope env = (Envelope) jb.getValue();
        check(env, true);
     }
   }
   
   public void testUseCase2() throws Exception
   {
     if(endpoint != null)
     {  
        JAXBElement<?> jb = getResponse("xacml/requests/soap-request.xml");
        Envelope env = (Envelope) jb.getValue();
        check(env, true);
     }
   }
   
   public void testHimss() throws Exception
   {
      if(endpoint != null)
      {  
         JAXBElement<?> jb = getResponse("xacml/requests/himss-soap-request.xml");
         Envelope env = (Envelope) jb.getValue();
         Marshaller marshaller = JAXBUtil.getMarshaller(SOAPSAMLXACMLUtil.getPackage());
         marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
         marshaller.marshal(jb, System.out);
         
         check(env, false);
      }
   } 
   
   public void testSAMLXACML() throws Exception
   {
      //Read the saml request from the file
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream is = tcl.getResourceAsStream("xacml/requests/samlxacml.xml"); 
      
      Unmarshaller um = JAXBUtil.getUnmarshaller(SOAPSAMLXACMLUtil.getPackage());
      um.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

      JAXBElement<?> obj = (JAXBElement<?>) um.unmarshal(is);
      XACMLAuthzDecisionQueryType xat = (XACMLAuthzDecisionQueryType) obj.getValue(); 
      assertNotNull(xat);
      RequestType requestType = xat.getRequest();
      assertTrue(requestType.getEnvironment().getAttribute().size() > 0); 
   }
   
   private void check(Envelope env, boolean permit)
   {
      JAXBElement<?> samlResponse = (JAXBElement<?>) env.getBody().getAny().get(0);
      Object response = samlResponse.getValue();
      if(response instanceof Fault)
      {
         Fault fault = (Fault) response;
         System.out.println(fault.getFaultstring());
         fail("fault");
      }
      ResponseType responseType = (ResponseType) response;
      AssertionType at = (AssertionType) responseType.getAssertionOrEncryptedAssertion().get(0);
      XACMLAuthzDecisionStatementType xst = (XACMLAuthzDecisionStatementType) at.getStatementOrAuthnStatementOrAuthzDecisionStatement().get(0);
      ResultType rt = xst.getResponse().getResult().get(0);
      DecisionType dt = rt.getDecision(); 
      
      if(permit)
        assertEquals(DecisionType.PERMIT, dt);
      else
         assertEquals(DecisionType.DENY, dt);
   }
   
   private JAXBElement<?> getResponse(String fileName) throws Exception
   {
      //Read the saml request from the file
      ClassLoader tcl = Thread.currentThread().getContextClassLoader();
      InputStream is = tcl.getResourceAsStream(fileName); 
      
      Unmarshaller um = JAXBUtil.getUnmarshaller(SOAPSAMLXACMLUtil.getPackage());
      Object soapRequest = um.unmarshal(is);
      
      Marshaller m = JAXBUtil.getMarshaller(SOAPSAMLXACMLUtil.getPackage());
      
      URL url = new URL(endpoint);
      URLConnection conn = url.openConnection();
      conn.setDoOutput(true); 
      m.marshal(soapRequest, System.out);
      m.marshal(soapRequest, conn.getOutputStream());
      
      return (JAXBElement<?>) um.unmarshal(conn.getInputStream()); 
   }
}