/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.wstrust.plugins.saml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.exceptions.ConfigurationException;
import org.picketlink.identity.federation.core.exceptions.ParsingException;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.parsers.saml.SAMLParser;
import org.picketlink.identity.federation.core.saml.v1.writers.SAML11AssertionWriter;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
import org.picketlink.identity.federation.core.util.JAXPValidationUtil;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * This class contains utility methods and constants that are used by the SAML token providers.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class SAMLUtil
{
   protected static Logger log = Logger.getLogger(SAMLUtil.class);

   protected static boolean trace = log.isTraceEnabled();

   public static final String SAML11_BEARER_URI = "urn:oasis:names:tc:SAML:1.0:cm:bearer";

   public static final String SAML11_HOLDER_OF_KEY_URI = "urn:oasis:names:tc:SAML:1.0:cm:holder-of-key";

   public static final String SAML11_SENDER_VOUCHES_URI = "urn:oasis:names:tc:SAML:1.0:cm:sender-vouches";

   public static final String SAML2_BEARER_URI = "urn:oasis:names:tc:SAML:2.0:cm:bearer";

   public static final String SAML2_HOLDER_OF_KEY_URI = "urn:oasis:names:tc:SAML:2.0:cm:holder-of-key";

   public static final String SAML2_SENDER_VOUCHES_URI = "urn:oasis:names:tc:SAML:2.0:cm:sender-vouches";

   public static final String SAML11_TOKEN_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1";

   public static final String SAML11_VALUE_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.0#SAMLAssertionID";

   public static final String SAML2_TOKEN_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0";

   public static final String SAML2_VALUE_TYPE = "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLID";

   /**
    * <p>
    * Utility method that marshals the specified {@code AssertionType} object into an {@code Element} instance.
    * </p>
    * 
    * @param assertion
    *           an {@code AssertionType} object representing the SAML assertion to be marshaled.
    * @return a reference to the {@code Element} that contains the marshaled SAML assertion.
    * @throws Exception
    *            if an error occurs while marshaling the assertion.
    */
   public static Element toElement(AssertionType assertion) throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAMLAssertionWriter writer = new SAMLAssertionWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(assertion);

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      Document document = DocumentUtil.getDocument(bis);

      if (trace)
      {
         log.trace("Written Assertion=" + DocumentUtil.asString(document));
      }

      return document.getDocumentElement();
   }

   /**
    * <p>
    * Utility method that marshals the specified {@code AssertionType} object into an {@code Element} instance.
    * </p>
    * 
    * @param assertion
    *           an {@code AssertionType} object representing the SAML assertion to be marshaled.
    * @return a reference to the {@code Element} that contains the marshaled SAML assertion.
    * @throws Exception
    *            if an error occurs while marshaling the assertion.
    */
   public static Element toElement(SAML11AssertionType assertion) throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SAML11AssertionWriter writer = new SAML11AssertionWriter(StaxUtil.getXMLStreamWriter(baos));
      writer.write(assertion);

      ByteArrayInputStream bis = new ByteArrayInputStream(baos.toByteArray());
      Document document = DocumentUtil.getDocument(bis);

      return document.getDocumentElement();
   }

   /**
    * <p>
    * Utility method that unmarshals the specified {@code Element} into an {@code AssertionType} instance.
    * </p>
    * 
    * @param assertionElement
    *           the {@code Element} that contains the marshaled SAMLV2.0 assertion.
    * @return a reference to the unmarshaled {@code AssertionType} instance.
    * @throws ConfigurationException 
    * @throws ProcessingException 
    * @throws ParsingException 
    */
   public static AssertionType fromElement(Element assertionElement) throws ProcessingException,
         ConfigurationException, ParsingException
   {
      SAMLParser samlParser = new SAMLParser();

      JAXPValidationUtil.checkSchemaValidation(assertionElement);
      AssertionType assertion = (AssertionType) samlParser.parse(DocumentUtil.getNodeAsStream(assertionElement));
      return assertion;
   }

   /**
    * Given a {@link Element} that represents a SAML 1.1 assertion, convert it into a {@link SAML11AssertionType}
    * @param assertionElement
    * @return
    * @throws GeneralSecurityException
    */
   public static SAML11AssertionType saml11FromElement(Element assertionElement) throws GeneralSecurityException
   {
      SAMLParser samlParser = new SAMLParser();

      JAXPValidationUtil.checkSchemaValidation(assertionElement);
      return (SAML11AssertionType) samlParser.parse(DocumentUtil.getNodeAsStream(assertionElement));
   }
}