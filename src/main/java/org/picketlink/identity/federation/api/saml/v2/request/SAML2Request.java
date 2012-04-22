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
package org.picketlink.identity.federation.api.saml.v2.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URL;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.ParserConfigurationException;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v2.common.IDGenerator;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLDocumentHolder;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.util.XMLTimeUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLRequestWriter;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLResponseWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.LogoutRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.NameIDPolicyType;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * API for SAML2 Request
 * @author Anil.Saldhana@redhat.com
 * @since Jan 5, 2009
 */
public class SAML2Request
{
   private SAMLDocumentHolder samlDocumentHolder = null;

   private String nameIDFormat = JBossSAMLURIConstants.NAMEID_FORMAT_TRANSIENT.get();

   /**
    * Set the NameIDFormat 
    * @param nameIDFormat
    */
   public void setNameIDFormat(String nameIDFormat)
   {
      this.nameIDFormat = nameIDFormat;
   }

   /**
    * Create an authentication request
    * @param id
    * @param assertionConsumerURL
    * @param destination
    * @param issuerValue
    * @return 
    * @throws ConfigurationException 
    */
   public AuthnRequestType createAuthnRequestType(String id, String assertionConsumerURL, String destination,
         String issuerValue) throws ConfigurationException
   {
      XMLGregorianCalendar issueInstant = XMLTimeUtil.getIssueInstant();

      AuthnRequestType authnRequest = new AuthnRequestType(id, issueInstant);
      authnRequest.setAssertionConsumerServiceURL(URI.create(assertionConsumerURL));
      authnRequest.setProtocolBinding(URI.create(JBossSAMLConstants.HTTP_POST_BINDING.get()));
      if (destination != null)
      {
         authnRequest.setDestination(URI.create(destination));
      }

      //Create an issuer 
      NameIDType issuer = new NameIDType();
      issuer.setValue(issuerValue);

      authnRequest.setIssuer(issuer);

      //Create a default NameIDPolicy
      NameIDPolicyType nameIDPolicy = new NameIDPolicyType();
      nameIDPolicy.setAllowCreate(Boolean.TRUE);
      nameIDPolicy.setFormat(URI.create(this.nameIDFormat));

      authnRequest.setNameIDPolicy(nameIDPolicy);

      return authnRequest;
   }

   /**
    * Get AuthnRequestType from a file
    * @param fileName file with the serialized AuthnRequestType
    * @return AuthnRequestType 
    * @throws ParsingException 
    * @throws ProcessingException 
    * @throws ConfigurationException 
    * @throws IllegalArgumentException if the input fileName is null
    *         IllegalStateException if the InputStream from the fileName is null
    */
   public AuthnRequestType getAuthnRequestType(String fileName) throws ConfigurationException, ProcessingException,
         ParsingException
   {
      if (fileName == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "fileName");
      URL resourceURL = SecurityActions.loadResource(getClass(), fileName);
      if (resourceURL == null)
         throw new ProcessingException(ErrorCodes.RESOURCE_NOT_FOUND + fileName + " could not be loaded");

      InputStream is = null;
      try
      {
         is = resourceURL.openStream();
      }
      catch (IOException e)
      {
         throw new ProcessingException(e);
      }
      return getAuthnRequestType(is);
   }

   /**
    * Get the Underlying SAML2Object from the input stream
    * @param is
    * @return
    * @throws IOException
    * @throws ParsingException
    */
   public SAML2Object getSAML2ObjectFromStream(InputStream is) throws ConfigurationException, ParsingException,
         ProcessingException
   {
      if (is == null)
         throw new IllegalStateException(ErrorCodes.NULL_ARGUMENT + "InputStream");

      Document samlDocument = DocumentUtil.getDocument(is);

      SAMLParser samlParser = new SAMLParser();
      JAXPValidationUtil.checkSchemaValidation(samlDocument);
      SAML2Object requestType = (SAML2Object) samlParser.parse(DocumentUtil.getNodeAsStream(samlDocument));

      samlDocumentHolder = new SAMLDocumentHolder(requestType, samlDocument);
      return requestType;
   }

   /**
    * Get a Request Type from Input Stream
    * @param is
    * @return 
    * @throws ProcessingException 
    * @throws ConfigurationException 
    * @throws  
    * @throws IllegalArgumentException inputstream is null
    */
   public RequestAbstractType getRequestType(InputStream is) throws ParsingException, ConfigurationException,
         ProcessingException
   {
      if (is == null)
         throw new IllegalStateException(ErrorCodes.NULL_ARGUMENT + "InputStream");

      Document samlDocument = DocumentUtil.getDocument(is);

      SAMLParser samlParser = new SAMLParser();
      JAXPValidationUtil.checkSchemaValidation(samlDocument);
      RequestAbstractType requestType = (RequestAbstractType) samlParser.parse(DocumentUtil
            .getNodeAsStream(samlDocument));

      samlDocumentHolder = new SAMLDocumentHolder(requestType, samlDocument);
      return requestType;
   }

   /**
    * Get the AuthnRequestType from an input stream
    * @param is Inputstream containing the AuthnRequest
    * @return 
    * @throws ParsingException 
    * @throws ProcessingException 
    * @throws ConfigurationException 
    * @throws IllegalArgumentException inputstream is null
    */
   public AuthnRequestType getAuthnRequestType(InputStream is) throws ConfigurationException, ProcessingException,
         ParsingException
   {
      if (is == null)
         throw new IllegalStateException(ErrorCodes.NULL_ARGUMENT + "InputStream");

      Document samlDocument = DocumentUtil.getDocument(is);

      SAMLParser samlParser = new SAMLParser();
      JAXPValidationUtil.checkSchemaValidation(samlDocument);

      AuthnRequestType requestType = (AuthnRequestType) samlParser.parse(DocumentUtil.getNodeAsStream(samlDocument));
      samlDocumentHolder = new SAMLDocumentHolder(requestType, samlDocument);
      return requestType;
   }

   /**
    * Get the parsed {@code SAMLDocumentHolder}
    * @return
    */
   public SAMLDocumentHolder getSamlDocumentHolder()
   {
      return samlDocumentHolder;
   }

   /**
    * Create a Logout Request
    * @param issuer
    * @return  
    * @throws ConfigurationException 
    */
   public LogoutRequestType createLogoutRequest(String issuer) throws ConfigurationException
   {
      LogoutRequestType lrt = new LogoutRequestType(IDGenerator.create("ID_"), XMLTimeUtil.getIssueInstant());

      //Create an issuer 
      NameIDType issuerNameID = new NameIDType();
      issuerNameID.setValue(issuer);

      lrt.setIssuer(issuerNameID);

      return lrt;
   }

   /**
    * Return the DOM object
    * @param rat
    * @return
    * @throws ProcessingException 
    * @throws ParsingException 
    * @throws ConfigurationException 
    */
   public Document convert(RequestAbstractType rat) throws ProcessingException, ConfigurationException,
         ParsingException
   {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();

      SAMLRequestWriter writer = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(bos));
      if (rat instanceof AuthnRequestType)
      {
         writer.write((AuthnRequestType) rat);
      }
      else if (rat instanceof LogoutRequestType)
      {
         writer.write((LogoutRequestType) rat);
      }

      return DocumentUtil.getDocument(new String(bos.toByteArray()));
   }

   /**
    * Convert a SAML2 Response into a Document
    * @param responseType
    * @return 
    * @throws ParserConfigurationException
    */
   public Document convert(ResponseType responseType) throws ProcessingException, ParsingException,
         ConfigurationException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAMLResponseWriter writer = new SAMLResponseWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(responseType);

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      return DocumentUtil.getDocument(bis);
   }

   /**
    * Marshall the AuthnRequestType to an output stream
    * @param requestType
    * @param os 
    * @throws SAXException 
    */
   public void marshall(RequestAbstractType requestType, OutputStream os) throws ProcessingException
   {
      SAMLRequestWriter samlRequestWriter = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(os));
      if (requestType instanceof AuthnRequestType)
      {
         samlRequestWriter.write((AuthnRequestType) requestType);
      }
      else if (requestType instanceof LogoutRequestType)
      {
         samlRequestWriter.write((LogoutRequestType) requestType);
      }
      else
         throw new RuntimeException(ErrorCodes.UNSUPPORTED_TYPE + requestType.getClass().getName());
   }

   /**
    * Marshall the AuthnRequestType to a writer
    * @param requestType
    * @param writer 
    * @throws SAXException 
    */
   public void marshall(RequestAbstractType requestType, Writer writer) throws ProcessingException
   {
      SAMLRequestWriter samlRequestWriter = new SAMLRequestWriter(StaxUtil.getXMLStreamWriter(writer));
      if (requestType instanceof AuthnRequestType)
      {
         samlRequestWriter.write((AuthnRequestType) requestType);
      }
      else if (requestType instanceof LogoutRequestType)
      {
         samlRequestWriter.write((LogoutRequestType) requestType);
      }
      else
         throw new RuntimeException(ErrorCodes.UNSUPPORTED_TYPE + requestType.getClass().getName());
   }
}