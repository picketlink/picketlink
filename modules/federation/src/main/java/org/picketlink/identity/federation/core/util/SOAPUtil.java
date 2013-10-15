/*
 * JBoss, Home of Professional Open Source
 *
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.picketlink.identity.federation.core.util;

import org.picketlink.common.ErrorCodes;
import org.picketlink.common.util.DocumentUtil;
import org.w3c.dom.Document;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

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
     *
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
     *
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
     *
     * @return
     *
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
     *
     * @return
     *
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
     *
     * @return
     *
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
     *
     * @return
     *
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
     *
     * @return
     *
     * @throws SOAPException
     */
    public static Document getSOAPData(SOAPMessage soapMessage) throws SOAPException {
        return soapMessage.getSOAPBody().extractContentAsDocument();
    }

    /**
     * Determine if a SOAPMessage is SOAP 1.2
     *
     * @param soapMessage
     *
     * @return
     *
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
     *
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
     *
     * @param soapMessage
     *
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