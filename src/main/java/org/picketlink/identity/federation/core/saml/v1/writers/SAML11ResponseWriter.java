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
package org.picketlink.identity.federation.core.saml.v1.writers;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v1.SAML11Constants;
import org.picketlink.identity.federation.core.saml.v2.constants.JBossSAMLConstants;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.core.util.StringUtil;
import org.picketlink.identity.federation.saml.common.CommonStatusDetailType;
import org.picketlink.identity.federation.saml.v1.assertion.SAML11AssertionType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11ResponseType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11StatusCodeType;
import org.picketlink.identity.federation.saml.v1.protocol.SAML11StatusType;
import org.w3c.dom.Element;

/**
 * Write the {@link SAML11ResponseType} to stream
 * @author Anil.Saldhana@redhat.com
 * @since Jun 29, 2011
 */
public class SAML11ResponseWriter extends BaseSAML11Writer
{
   protected String namespace = SAML11Constants.PROTOCOL_11_NSURI;

   protected SAML11AssertionWriter assertionWriter;

   public SAML11ResponseWriter(XMLStreamWriter writer)
   {
      super(writer);
      assertionWriter = new SAML11AssertionWriter(writer);
   }

   public void write(SAML11ResponseType response) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SAML11Constants.RESPONSE, namespace);
      StaxUtil.writeNameSpace(writer, PROTOCOL_PREFIX, namespace);
      StaxUtil.writeNameSpace(writer, ASSERTION_PREFIX, SAML11Constants.ASSERTION_11_NSURI);

      // Attributes
      StaxUtil.writeAttribute(writer, SAML11Constants.RESPONSE_ID, response.getID());
      StaxUtil.writeAttribute(writer, SAML11Constants.MAJOR_VERSION, response.getMajorVersion() + "");
      StaxUtil.writeAttribute(writer, SAML11Constants.MINOR_VERSION, response.getMinorVersion() + "");
      StaxUtil.writeAttribute(writer, JBossSAMLConstants.ISSUE_INSTANT.get(), response.getIssueInstant().toString());
      String inResp = response.getInResponseTo();
      if (StringUtil.isNotNull(inResp))
      {
         StaxUtil.writeAttribute(writer, SAML11Constants.IN_RESPONSE_TO, inResp);
      }

      URI recipient = response.getRecipient();
      if (recipient != null)
      {
         StaxUtil.writeAttribute(writer, SAML11Constants.RECIPIENT, recipient.toString());
      }

      Element sig = response.getSignature();
      if (sig != null)
      {
         StaxUtil.writeDOMElement(writer, sig);
      }

      SAML11StatusType status = response.getStatus();
      if (status != null)
      {
         write(status);
      }

      List<SAML11AssertionType> assertions = response.get();
      for (SAML11AssertionType assertion : assertions)
      {
         assertionWriter.write(assertion);
      }

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   public void write(SAML11StatusType status) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SAML11Constants.STATUS, namespace);

      SAML11StatusCodeType statusCode = status.getStatusCode();
      if (statusCode != null)
      {
         write(statusCode);
      }

      String statusMsg = status.getStatusMessage();
      if (StringUtil.isNotNull(statusMsg))
      {
         StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SAML11Constants.STATUS_MSG, namespace);
         StaxUtil.writeCharacters(writer, statusMsg);
         StaxUtil.writeEndElement(writer);
      }

      CommonStatusDetailType details = status.getStatusDetail();
      if (details != null)
      {
         StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SAML11Constants.STATUS_DETAIL, namespace);
         List<Object> objs = details.getAny();
         for (Object theObj : objs)
         {
            StaxUtil.writeCharacters(writer, theObj.toString());
         }
         StaxUtil.writeEndElement(writer);
      }
      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   public void write(SAML11StatusCodeType statusCode) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PROTOCOL_PREFIX, SAML11Constants.STATUS_CODE, namespace);

      QName value = statusCode.getValue();
      if (value == null)
         throw new ProcessingException(ErrorCodes.WRITER_NULL_VALUE + "Attribute Value");
      StaxUtil.writeAttribute(writer, SAML11Constants.VALUE, value);

      SAML11StatusCodeType secondCode = statusCode.getStatusCode();
      if (secondCode != null)
      {
         write(secondCode);
      }

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }
}