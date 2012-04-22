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
package org.picketlink.identity.federation.core.saml.v2.writers;

import static org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants.ASSERTION_NSURI;
import static org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLURIConstants.PROTOCOL_NSURI;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.EncryptedAssertionType;
import org.picketlink.identity.federation.saml.v2.assertion.NameIDType;
import org.picketlink.identity.federation.saml.v2.protocol.ArtifactResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.AuthnRequestType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.ResponseType.RTChoiceType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusCodeType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusDetailType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusResponseType;
import org.picketlink.identity.federation.saml.v2.protocol.StatusType;
import org.w3c.dom.Element;

/**
 * Write a SAML Response to stream
 * @author Anil.Saldhana@redhat.com
 * @since Nov 2, 2010
 */
public class SAMLResponseWriter extends BaseWriter
{
   private final SAMLAssertionWriter assertionWriter;

   public SAMLResponseWriter(XMLStreamWriter writer)
   {
      super(writer);
      this.assertionWriter = new SAMLAssertionWriter(writer);
   }

   /**
    * Write a {@code ResponseType} to stream
    * @param response
    * @param out
    * @throws ProcessingException
    */
   public void write(ResponseType response) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.RESPONSE.get(), PROTOCOL_NSURI.get());

      StaxUtil.writeNameSpace(writer, PROTOCOL_PREFIX, PROTOCOL_NSURI.get());
      StaxUtil.writeNameSpace(writer, ASSERTION_PREFIX, ASSERTION_NSURI.get());

      writeBaseAttributes(response);

      NameIDType issuer = response.getIssuer();
      if (issuer != null)
      {
         write(issuer, new QName(ASSERTION_NSURI.get(), JBossSAMLConstants.ISSUER.get(), ASSERTION_PREFIX));
      }

      Element sig = response.getSignature();
      if (sig != null)
      {
         StaxUtil.writeDOMElement(writer, sig);
      }

      StatusType status = response.getStatus();
      write(status);

      List<RTChoiceType> choiceTypes = response.getAssertions();
      if (choiceTypes != null)
      {
         for (RTChoiceType choiceType : choiceTypes)
         {
            AssertionType assertion = choiceType.getAssertion();
            if (assertion != null)
            {
               assertionWriter.write(assertion);
            }

            EncryptedAssertionType encryptedAssertion = choiceType.getEncryptedAssertion();
            if (encryptedAssertion != null)
            {
               Element encElement = encryptedAssertion.getEncryptedElement();
               StaxUtil.writeDOMElement(writer, encElement);
            }
         }
      }
      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   public void write(ArtifactResponseType response) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.ARTIFACT_RESPONSE.get(),
            PROTOCOL_NSURI.get());

      StaxUtil.writeNameSpace(writer, PROTOCOL_PREFIX, PROTOCOL_NSURI.get());
      StaxUtil.writeNameSpace(writer, ASSERTION_PREFIX, ASSERTION_NSURI.get());
      StaxUtil.writeDefaultNameSpace(writer, ASSERTION_NSURI.get());

      writeBaseAttributes(response);

      NameIDType issuer = response.getIssuer();
      if (issuer != null)
      {
         write(issuer, new QName(ASSERTION_NSURI.get(), JBossSAMLConstants.ISSUER.get(), ASSERTION_PREFIX));
      }

      Element sig = response.getSignature();
      if (sig != null)
      {
         StaxUtil.writeDOMElement(writer, sig);
      }

      StatusType status = response.getStatus();
      if (status != null)
      {
         write(status);
      }
      Object anyObj = response.getAny();
      if (anyObj instanceof AuthnRequestType)
      {
         AuthnRequestType authn = (AuthnRequestType) anyObj;
         SAMLRequestWriter requestWriter = new SAMLRequestWriter(writer);
         requestWriter.write(authn);
      }
      else if (anyObj instanceof ResponseType)
      {
         ResponseType rt = (ResponseType) anyObj;
         write(rt);
      }

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   /**
    * Write a {@code StatusResponseType}
    * @param response
    * @param qname QName of the starting element
    * @param out
    * @throws ProcessingException
    */
   public void write(StatusResponseType response, QName qname) throws ProcessingException
   {
      if (qname == null)
      {
         StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.STATUS_RESPONSE_TYPE.get(),
               PROTOCOL_NSURI.get());
      }
      else
      {
         StaxUtil.writeStartElement(writer, qname.getPrefix(), qname.getLocalPart(), qname.getNamespaceURI());
      }

      StaxUtil.writeNameSpace(writer, PROTOCOL_PREFIX, PROTOCOL_NSURI.get());
      StaxUtil.writeDefaultNameSpace(writer, ASSERTION_NSURI.get());

      writeBaseAttributes(response);

      NameIDType issuer = response.getIssuer();
      write(issuer, new QName(ASSERTION_NSURI.get(), JBossSAMLConstants.ISSUER.get(), ASSERTION_PREFIX));

      StatusType status = response.getStatus();
      write(status);

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   /**
    * Write a {@code StatusType} to stream
    * @param status
    * @param out
    * @throws ProcessingException
    */
   public void write(StatusType status) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.STATUS.get(), PROTOCOL_NSURI.get());

      StatusCodeType statusCodeType = status.getStatusCode();
      write(statusCodeType);

      String statusMessage = status.getStatusMessage();
      if (StringUtil.isNotNull(statusMessage))
      {
         StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.STATUS_MESSAGE.get(),
               PROTOCOL_NSURI.get());
         StaxUtil.writeEndElement(writer);
      }

      StatusDetailType statusDetail = status.getStatusDetail();
      if (statusDetail != null)
         write(statusDetail);

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   /**
    * Write a {@code StatusCodeType} to stream
    * @param statusCodeType
    * @param out
    * @throws ProcessingException
    */
   public void write(StatusCodeType statusCodeType) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.STATUS_CODE.get(), PROTOCOL_NSURI.get());

      URI value = statusCodeType.getValue();
      if (value != null)
      {
         StaxUtil.writeAttribute(writer, JBossSAMLConstants.VALUE.get(), value.toASCIIString());
      }
      StatusCodeType subStatusCode = statusCodeType.getStatusCode();
      if (subStatusCode != null)
         write(subStatusCode);

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   /**
    * Write a {@code StatusDetailType} to stream
    * @param statusDetailType
    * @param out
    * @throws ProcessingException
    */
   public void write(StatusDetailType statusDetailType) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, JBossSAMLConstants.STATUS_CODE.get(), PROTOCOL_NSURI.get());
      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   /**
    * Write the common attributes for all response types
    * @param statusResponse
    * @throws ProcessingException
    */
   private void writeBaseAttributes(StatusResponseType statusResponse) throws ProcessingException
   {
      //Attributes 
      StaxUtil.writeAttribute(writer, JBossSAMLConstants.ID.get(), statusResponse.getID());
      StaxUtil.writeAttribute(writer, JBossSAMLConstants.VERSION.get(), statusResponse.getVersion());
      StaxUtil.writeAttribute(writer, JBossSAMLConstants.ISSUE_INSTANT.get(), statusResponse.getIssueInstant()
            .toString());

      String destination = statusResponse.getDestination();
      if (StringUtil.isNotNull(destination))
         StaxUtil.writeAttribute(writer, JBossSAMLConstants.DESTINATION.get(), destination);

      String consent = statusResponse.getConsent();
      if (StringUtil.isNotNull(consent))
         StaxUtil.writeAttribute(writer, JBossSAMLConstants.CONSENT.get(), consent);

      String inResponseTo = statusResponse.getInResponseTo();
      if (StringUtil.isNotNull(inResponseTo))
         StaxUtil.writeAttribute(writer, JBossSAMLConstants.IN_RESPONSE_TO.get(), inResponseTo);
   }
}