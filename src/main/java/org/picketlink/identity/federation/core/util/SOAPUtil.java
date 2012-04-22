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
package org.picketlink.identity.federation.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.w3c.dom.Document;

/**
 * Utility class dealing with SAAJ
 * @author Anil.Saldhana@redhat.com
 * @since Jun 16, 2011
 */
public class SOAPUtil
{
   /**
    * Create an empty {@link SOAPMessage}
    * @return
    * @throws SOAPException
    */
   public static SOAPMessage create() throws SOAPException
   {
      MessageFactory messageFactory = MessageFactory.newInstance();

      SOAPMessage soapMessage = messageFactory.createMessage();
      return soapMessage;
   }

   /**
    * Given a stream of {@link SOAPMessage}, construct the {@link SOAPMessage}
    * @param is
    * @return
    * @throws IOException
    * @throws SOAPException
    */
   public static SOAPMessage getSOAPMessage(InputStream is) throws IOException, SOAPException
   {
      MessageFactory messageFactory = MessageFactory.newInstance();
      return messageFactory.createMessage(null, is);
   }

   /**
    * Given a string message, create a {@link SOAPFault}
    * @param message
    * @return
    * @throws SOAPException
    */
   public static SOAPMessage createFault(String message) throws SOAPException
   {
      MessageFactory messageFactory = MessageFactory.newInstance();
      SOAPMessage msg = messageFactory.createMessage();
      SOAPEnvelope envelope = msg.getSOAPPart().getEnvelope();
      SOAPBody body = envelope.getBody();
      SOAPFault fault = body.addFault();
      fault.setFaultCode("Server");
      fault.setFaultActor("urn:picketlink");
      fault.setFaultString(message);
      return msg;
   }

   /**
    * Given a {@link SOAPMessage}, get the content as a {@link Document}
    * @param soapMessage
    * @return
    * @throws SOAPException
    */
   public static Document getSOAPData(SOAPMessage soapMessage) throws SOAPException
   {
      return soapMessage.getSOAPBody().extractContentAsDocument();
   }

   /**
    * Add content to {@link SOAPMessage}
    * @param data
    * @param soapMessage
    * @throws SOAPException
    */
   public static void addData(Source data, SOAPMessage soapMessage) throws SOAPException
   {
      try
      {
         soapMessage.getSOAPBody().addDocument(DocumentUtil.getDocumentFromSource(data));
      }
      catch (GeneralSecurityException e)
      {
         throw new RuntimeException(e);
      }
   }
}