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
package org.picketlink.test.identity.federation.bindings.util;

import java.util.ArrayList;
import java.util.List;

import org.picketlink.identity.federation.api.soap.SOAPSAMLXACML;
import org.picketlink.identity.federation.api.soap.SOAPSAMLXACML.Result;
import org.jboss.security.xacml.core.model.context.ActionType;
import org.jboss.security.xacml.core.model.context.AttributeType;
import org.jboss.security.xacml.core.model.context.AttributeValueType;
import org.jboss.security.xacml.core.model.context.EnvironmentType;
import org.jboss.security.xacml.core.model.context.RequestType;
import org.jboss.security.xacml.core.model.context.ResourceType;
import org.jboss.security.xacml.core.model.context.SubjectType;
import org.jboss.security.xacml.factories.RequestAttributeFactory;

import junit.framework.TestCase;

/**
 * Unit test the SOAP SAML XACML Unit Test
 * @author Anil.Saldhana@redhat.com
 * @since Jul 31, 2009
 */
public class SOAPSAMLXACMLUnitTestCase extends TestCase
{
   //Change it to true when you have an end point running locally
   private boolean sendRequest = false;
   
   private String endpoint = "http://localhost:8080/test/SOAPServlet";
   
   private String issuer = "testIssuer";

   public void testXACML() throws Exception
   {
      if(sendRequest)
      {
         //Create an XACML Request
         RequestType xacmlRequest = getXACMLRequest();
         SOAPSAMLXACML soapSAMLXACML = new SOAPSAMLXACML();
         
         Result result = soapSAMLXACML.send(endpoint, issuer, xacmlRequest);
         assertTrue("No fault", result.isFault() == false);
         assertTrue("Decision available", result.isResponseAvailable());
         assertTrue("Deny", result.isDeny());
      }
   }
   
   private RequestType getXACMLRequest()
   {
      RequestType requestType = new RequestType();
      requestType.getSubject().add(createSubject());
      requestType.getResource().add(createResource());
      requestType.setAction(createAction());
      requestType.setEnvironment(createEnvironment());
      return requestType;
   }
   
   private SubjectType createSubject()
   {
      //Create a subject type
      SubjectType subject = new SubjectType();
      subject.setSubjectCategory("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
      
      subject.getAttribute().addAll(getSubjectAttributes()); 

      return subject;
   }

   public ResourceType createResource()
   {
      ResourceType resourceType = new ResourceType();

      AttributeType attResourceID = RequestAttributeFactory.createStringAttributeType(
            "urn:va:xacml:2.0:interop:rsa8:resource:hl7:type", issuer, 
            "urn:va:xacml:2.0:interop:rsa8:resource:hl7:medical-record");
        
      //Create a multi-valued attribute - hl7 permissions
      AttributeType  multi = new AttributeType();
      multi.setAttributeId("urn:va:xacml:2.0:interop:rsa8:subject:hl7:permission");
      multi.setDataType("http://www.w3.org/2001/XMLSchema#string");
      
      if (issuer != null)
         multi.setIssuer(issuer); 
      
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-010"));
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-012"));
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-005"));
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-003"));
        
      
      AttributeType attConfidentialityCode = RequestAttributeFactory.createStringAttributeType(
            "urn:va:xacml:2.0:interop:rsa8:resource:hl7:confidentiality-code", issuer, 
            "MA");
      
      AttributeType attDissentedSubjectId = RequestAttributeFactory.createStringAttributeType(
            "urn:va:xacml:2.0:interop:rsa8:resource:hl7:radiology:dissented-subject-id", issuer, 
            "Doctor, Bob I");
      
      //Add the attributes into the resource
      resourceType.getAttribute().add(attResourceID);
      resourceType.getAttribute().add(multi);
      resourceType.getAttribute().add(attConfidentialityCode);
      resourceType.getAttribute().add(attDissentedSubjectId); 
      return resourceType;
   }

   private ActionType createAction()
   {
      ActionType actionType = new ActionType();
      AttributeType attActionID = RequestAttributeFactory.createStringAttributeType(
            "urn:oasis:names:tc:xacml:1.0:action:action-id", issuer, "read");
      actionType.getAttribute().add(attActionID);
      return actionType;
   }
   
   private List<AttributeType> getSubjectAttributes()
   {
      List<AttributeType> attrList = new ArrayList<AttributeType>();
      
      //create the subject attributes
      
      //SubjectID - Bob
      AttributeType attSubjectID = RequestAttributeFactory.createStringAttributeType(
            "urn:oasis:names:tc:xacml:1.0:subject:subject-id", issuer, "Doctor, Bob I"); 

      //Role - Physician      
      AttributeType attRole = RequestAttributeFactory.createStringAttributeType(
            "urn:va:xacml:2.0:interop:rsa8:subject:role", issuer, "Physician");
      
      
      //Create a multi-valued attribute - hl7 permissions
      AttributeType  multi = new AttributeType();
      multi.setAttributeId("urn:va:xacml:2.0:interop:rsa8:subject:hl7:permission");
      multi.setDataType("http://www.w3.org/2001/XMLSchema#string");
      
      if (issuer != null)
         multi.setIssuer(issuer); 
      
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-010"));
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-012"));
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-017"));
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-005"));
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-003"));
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-009"));
      multi.getAttributeValue().add(createAttributeValueType("urn:va:xacml:2.0:interop:rsa8:hl7:prd-006"));
      
      //Locality
      AttributeType attLocality = RequestAttributeFactory.createStringAttributeType(
            "urn:oasis:names:tc:xacml:1.0:subject:locality", issuer, "Facility A"); 
            
      attrList.add(attSubjectID);
      attrList.add(attRole);
      attrList.add(multi); 
      attrList.add(attLocality);
      
      return attrList;
   }
   
   private EnvironmentType createEnvironment()
   {
      EnvironmentType env = new EnvironmentType();
      
      AttributeType attFacility = RequestAttributeFactory.createStringAttributeType(
            "urn:va:xacml:2.0:interop:rsa8:environment:locality", issuer, "Facility A"); 
      
      env.getAttribute().add(attFacility);
      return env;
   }
   
   private AttributeValueType createAttributeValueType(String value)
   {
      AttributeValueType avt = new AttributeValueType();
      avt.getContent().add(value);
      return avt;
   }
}