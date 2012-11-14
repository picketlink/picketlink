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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.util.DocumentUtil;
import org.w3c.dom.Document;

/**
 * Utility class dealing with SAAJ
 *
 * @author Anil.Saldhana@redhat.com
 * @since Jun 16, 2011
 */
public class SOAPUtil {
    /**
     * Create an empty {@link SOAPMessage}
     *
     * @return
     * @throws SOAPException
     */
    public static SOAPMessage create() throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();

        SOAPMessage soapMessage = messageFactory.createMessage();
        return soapMessage;
    }

    /**
     * Create a SOAP 1.2 Message
     *
     * @return
     * @throws SOAPException
     */
    public static SOAPMessage createSOAP12() throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        SOAPMessage soapMessage = messageFactory.createMessage();
        return soapMessage;
    }

    /**
     * Given a stream of {@link SOAPMessage}, construct the {@link SOAPMessage}
     *
     * @param is
     * @return
     * @throws IOException
     * @throws SOAPException
     */
    public static SOAPMessage getSOAPMessage(InputStream is) throws IOException, SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        return messageFactory.createMessage(null, is);
    }

    /**
     * Given a stream of {@link SOAPMessage} that is SOAP 1.2, construct the {@link SOAPMessage}
     *
     * @param is
     * @return
     * @throws IOException
     * @throws SOAPException
     */
    public static SOAPMessage getSOAP12Message(InputStream is) throws IOException, SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        return messageFactory.createMessage(null, is);
    }

    /**
     * Given a string message, create a {@link SOAPFault}
     *
     * @param message
     * @return
     * @throws SOAPException
     */
    public static SOAPMessage createFault(String message) throws SOAPException {
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
     * Given a string message, create a {@link SOAPFault} that is SOAP 1.2
     *
     * @param message
     * @return
     * @throws SOAPException
     */
    public static SOAPMessage createFault12(String message) throws SOAPException {
        MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
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
     *
     * @param soapMessage
     * @return
     * @throws SOAPException
     */
    public static Document getSOAPData(SOAPMessage soapMessage) throws SOAPException {
        return soapMessage.getSOAPBody().extractContentAsDocument();
    }

    /**
     * Determine if a SOAPMessage is SOAP 1.2
     *
     * @param soapMessage
     * @return
     * @throws SOAPException
     */
    public static boolean isSOAP12(SOAPMessage soapMessage) throws SOAPException {
        SOAPPart soapPart = soapMessage.getSOAPPart();
        SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
        if (SOAPConstants.URI_NS_SOAP_1_2_ENVELOPE.equals(soapEnvelope.getNamespaceURI()))
            return true;
        return false;
    }

    /**
     * Add content to {@link SOAPMessage}
     *
     * @param data
     * @param soapMessage
     * @throws SOAPException
     */
    public static void addData(Source data, SOAPMessage soapMessage) throws SOAPException {
        try {
            soapMessage.getSOAPBody().addDocument(DocumentUtil.getDocumentFromSource(data));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
    
   /**
    * Utility method to dump soapMessage to String.
    * Used for logging purpose. Use only with TRACE level, please. 
    * @param soapMessage
    * @return String representation of soapMessage
    */
   public static String soapMessageAsString(SOAPMessage soapMessage) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      try {
         soapMessage.writeTo(baos);
      } catch (Exception almostIgnored) {
         return ErrorCodes.SOAP_MESSAGE_DUMP_ERROR + almostIgnored;
      }
      return baos.toString();
   }
}