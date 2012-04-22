/*
 * JBoss, Home of Professional Open Source. Copyright 2008, Red Hat Middleware LLC, and individual contributors as
 * indicated by the @author tags. See the copyright.txt file in the distribution for a full listing of individual
 * contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site:
 * http://www.fsf.org.
 */
package org.picketlink.identity.federation.core.wstrust.writers;

import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.wrappers.Lifetime;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponseCollection;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.ws.trust.BinarySecretType;
import org.picketlink.identity.federation.ws.trust.ComputedKeyType;
import org.picketlink.identity.federation.ws.trust.EntropyType;
import org.picketlink.identity.federation.ws.trust.RequestedProofTokenType;
import org.picketlink.identity.federation.ws.trust.RequestedReferenceType;
import org.picketlink.identity.federation.ws.trust.StatusType;
import org.w3c.dom.Element;

/**
 * <p>
 * A Stax writer for WS-Trust response messages.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class WSTrustResponseWriter
{
   private final XMLStreamWriter writer;

   /**
    * <p>
    * Creates a {@code WSTrustResponseWriter} instance that writes WS-Trust response messages to the specified {@code
    * OutputStream}.
    * </p>
    * 
    * @param stream
    *           the where the response is to be written.
    * @throws ProcessingException
    *            if an error occurs when creating the {@code XMLStreamWriter} for the specified stream.
    */
   public WSTrustResponseWriter(OutputStream stream) throws ProcessingException
   {
      this.writer = StaxUtil.getXMLStreamWriter(stream);
   }

   /**
    * <p>
    * Creates a {@code WSTrustResponseWriter} instance that writes WS-Trust response messages to the specified {@code
    * Result}.
    * </p>
    * 
    * @param result
    *           the {@code Result} object where the response is to be written.
    * @throws ProcessingException
    *            if an error occurs when creating the {@code XMLStreamWriter} for the specified result.
    */
   public WSTrustResponseWriter(Result result) throws ProcessingException
   {
      this.writer = StaxUtil.getXMLStreamWriter(result);
   }

   /**
    * <p>
    * Creates a {@code WSTrustResponseWriter} instance that uses the specified {@code XMLStreamWriter} to write the
    * WS-Trust response messages.
    * </p>
    * 
    * @param writer
    *           the {@code XMLStreamWriter} that will be used to write the response messages.
    */
   public WSTrustResponseWriter(XMLStreamWriter writer)
   {
      this.writer = writer;
   }

   /**
    * <p>
    * Writes the WS-Trust response message represented by the specified {@code RequestSecurityTokenResponseCollection}
    * object.
    * </p>
    * 
    * @param collection
    *           the object that contains the WS-Trust response message to be written.
    * @throws ProcessingException
    *            if an error occurs while writing the response message.
    */
   public void write(RequestSecurityTokenResponseCollection collection) throws ProcessingException
   {
      // write the "root" response collection element with its namespace.
      StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.RSTR_COLLECTION,
            WSTrustConstants.BASE_NAMESPACE);
      StaxUtil.writeNameSpace(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.BASE_NAMESPACE);

      // write all individual response messages.
      List<RequestSecurityTokenResponse> responses = collection.getRequestSecurityTokenResponses();
      if (responses == null)
         throw new ProcessingException(ErrorCodes.NULL_VALUE + "WS-Trust response message doesn't contain any response");

      for (RequestSecurityTokenResponse response : responses)
         this.write(response);

      // write the response collection end element.
      StaxUtil.writeEndElement(this.writer);
      StaxUtil.flush(this.writer);
   }

   private void write(RequestSecurityTokenResponse response) throws ProcessingException
   {
      // write the response element and the context attribute.
      StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.RSTR,
            WSTrustConstants.BASE_NAMESPACE);
      String context = response.getContext();
      StaxUtil.writeAttribute(this.writer, WSTrustConstants.RST_CONTEXT, context);

      // write the token type, if available.
      if (response.getTokenType() != null)
      {
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.TOKEN_TYPE,
               WSTrustConstants.BASE_NAMESPACE);
         StaxUtil.writeCharacters(this.writer, response.getTokenType().toASCIIString());
         StaxUtil.writeEndElement(this.writer);
      }

      // write the token lifetime, if available.
      if (response.getLifetime() != null)
      {
         Lifetime lifetime = response.getLifetime();
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.LIFETIME,
               WSTrustConstants.BASE_NAMESPACE);
         new WSSecurityWriter(this.writer).writeLifetime(lifetime.getCreated(), lifetime.getExpires());
         StaxUtil.writeEndElement(this.writer);
      }

      // write the applies-to element, if available.
      if (response.getAppliesTo() != null)
      {
         WSPolicyWriter policyWriter = new WSPolicyWriter(this.writer);
         policyWriter.write(response.getAppliesTo());
      }

      // write the key size, if available.
      if (response.getKeySize() != 0)
      {
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.KEY_SIZE,
               WSTrustConstants.BASE_NAMESPACE);
         StaxUtil.writeCharacters(this.writer, Long.toString(response.getKeySize()));
         StaxUtil.writeEndElement(this.writer);
      }

      // write the key type, if available.
      if (response.getKeyType() != null)
      {
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.KEY_TYPE,
               WSTrustConstants.BASE_NAMESPACE);
         StaxUtil.writeCharacters(this.writer, response.getKeyType().toString());
         StaxUtil.writeEndElement(this.writer);
      }

      // write the security token, if available.
      if (response.getRequestedSecurityToken() != null)
      {
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.REQUESTED_TOKEN,
               WSTrustConstants.BASE_NAMESPACE);
         List<Object> theList = response.getRequestedSecurityToken().getAny();
         for (Object securityToken : theList)
         {
            if (securityToken instanceof AssertionType)
            {
               AssertionType assertion = (AssertionType) securityToken;
               SAMLAssertionWriter samlAssertionWriter = new SAMLAssertionWriter(this.writer);
               samlAssertionWriter.write(assertion);
            }
            else if (securityToken instanceof Element)
            {
               StaxUtil.writeDOMElement(this.writer, (Element) securityToken);
            }
            else
               throw new ProcessingException(ErrorCodes.WRITER_UNKNOWN_TYPE + "Unknown security token type="
                     + securityToken.getClass().getName());
         }
         /*Object securityToken = response.getRequestedSecurityToken().getAny();
         if (securityToken != null)
         {
            if (securityToken instanceof AssertionType)
            {
               AssertionType assertion = (AssertionType) securityToken;
               SAMLAssertionWriter samlAssertionWriter = new SAMLAssertionWriter(this.writer);
               samlAssertionWriter.write(assertion);
            }
            else if (securityToken instanceof Element)
            {
               StaxUtil.writeDOMElement(this.writer, (Element) securityToken);
            }
            else
               throw new ProcessingException("Unknown security token type=" + securityToken.getClass().getName());
         }*/
         StaxUtil.writeEndElement(this.writer);
      }

      // write the attached reference, if available.
      if (response.getRequestedAttachedReference() != null)
      {
         RequestedReferenceType ref = response.getRequestedAttachedReference();
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX,
               WSTrustConstants.REQUESTED_ATTACHED_REFERENCE, WSTrustConstants.BASE_NAMESPACE);
         new WSSecurityWriter(this.writer).writeSecurityTokenReference(ref.getSecurityTokenReference());
         StaxUtil.writeEndElement(this.writer);
      }

      // write the requested proof token, if available.
      if (response.getRequestedProofToken() != null)
      {
         RequestedProofTokenType requestedProof = response.getRequestedProofToken();

         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.REQUESTED_PROOF_TOKEN,
               WSTrustConstants.BASE_NAMESPACE);
         List<Object> theList = requestedProof.getAny();
         for (Object content : theList)
         {
            if (content instanceof BinarySecretType)
            {
               BinarySecretType binarySecret = (BinarySecretType) content;
               StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.BINARY_SECRET,
                     WSTrustConstants.BASE_NAMESPACE);
               StaxUtil.writeAttribute(this.writer, WSTrustConstants.TYPE, binarySecret.getType());
               StaxUtil.writeCharacters(this.writer, new String(binarySecret.getValue()));
               StaxUtil.writeEndElement(this.writer);
            }
            else if (content instanceof ComputedKeyType)
            {
               ComputedKeyType computedKey = (ComputedKeyType) content;
               StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.COMPUTED_KEY,
                     WSTrustConstants.BASE_NAMESPACE);
               StaxUtil.writeCharacters(this.writer, computedKey.getAlgorithm());
               StaxUtil.writeEndElement(this.writer);
            }

         }

         StaxUtil.writeEndElement(this.writer);
      }

      // write the server entropy, if available.
      if (response.getEntropy() != null)
      {
         EntropyType entropy = response.getEntropy();
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.ENTROPY,
               WSTrustConstants.BASE_NAMESPACE);

         List<Object> entropyList = entropy.getAny();
         if (entropyList != null && entropyList.size() != 0)
         {
            for (Object entropyObj : entropyList)
            {
               if (entropyObj instanceof BinarySecretType)
               {
                  BinarySecretType binarySecret = (BinarySecretType) entropyObj;
                  StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.BINARY_SECRET,
                        WSTrustConstants.BASE_NAMESPACE);
                  StaxUtil.writeAttribute(this.writer, WSTrustConstants.TYPE, binarySecret.getType());
                  StaxUtil.writeCharacters(this.writer, new String(binarySecret.getValue()));
                  StaxUtil.writeEndElement(this.writer);
               }
            }
         }
         StaxUtil.writeEndElement(writer);
      }

      // write the validation status, if available.
      if (response.getStatus() != null)
      {
         StatusType status = response.getStatus();
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.STATUS,
               WSTrustConstants.BASE_NAMESPACE);

         // write the status code.
         if (status.getCode() == null || status.getCode() == "")
            throw new ProcessingException(ErrorCodes.NULL_VALUE + "Validation status code is missing");
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.CODE,
               WSTrustConstants.BASE_NAMESPACE);
         StaxUtil.writeCharacters(this.writer, response.getStatus().getCode());
         StaxUtil.writeEndElement(this.writer);

         // write the status reason, if available.
         if (status.getReason() != null)
         {
            StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.REASON,
                  WSTrustConstants.BASE_NAMESPACE);
            StaxUtil.writeCharacters(this.writer, response.getStatus().getReason());
            StaxUtil.writeEndElement(this.writer);
         }

         // write the status end element.
         StaxUtil.writeEndElement(this.writer);
      }

      // write the cancel status, if available.
      if (response.getRequestedTokenCancelled() != null)
      {
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.REQUESTED_TOKEN_CANCELLED,
               WSTrustConstants.BASE_NAMESPACE);
         StaxUtil.writeEndElement(this.writer);
      }

      // write the response end element.
      StaxUtil.writeEndElement(this.writer);
      StaxUtil.flush(writer);
   }
}
