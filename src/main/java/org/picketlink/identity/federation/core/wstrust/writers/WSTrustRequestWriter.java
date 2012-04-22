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
package org.picketlink.identity.federation.core.wstrust.writers;

import static org.picketlink.identity.federation.core.wstrust.WSTrustConstants.BASE_NAMESPACE;
import static org.picketlink.identity.federation.core.wstrust.WSTrustConstants.PREFIX;
import static org.picketlink.identity.federation.core.wstrust.WSTrustConstants.RST;
import static org.picketlink.identity.federation.core.wstrust.WSTrustConstants.RST_COLLECTION;
import static org.picketlink.identity.federation.core.wstrust.WSTrustConstants.RST_CONTEXT;

import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.writers.SAMLAssertionWriter;
import org.picketlink.identity.federation.core.util.StaxUtil;
import org.picketlink.identity.federation.core.wsa.WSAddressingConstants;
import org.picketlink.identity.federation.core.wstrust.WSTrustConstants;
import org.picketlink.identity.federation.core.wstrust.wrappers.Lifetime;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenCollection;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.picketlink.identity.federation.ws.policy.AppliesTo;
import org.picketlink.identity.federation.ws.trust.BinarySecretType;
import org.picketlink.identity.federation.ws.trust.CancelTargetType;
import org.picketlink.identity.federation.ws.trust.EntropyType;
import org.picketlink.identity.federation.ws.trust.OnBehalfOfType;
import org.picketlink.identity.federation.ws.trust.RenewTargetType;
import org.picketlink.identity.federation.ws.trust.UseKeyType;
import org.picketlink.identity.federation.ws.trust.ValidateTargetType;
import org.picketlink.identity.federation.ws.wss.secext.UsernameTokenType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyValueType;
import org.picketlink.identity.xmlsec.w3.xmldsig.RSAKeyValueType;
import org.w3c.dom.Element;

/**
 * Given a {@code RequestSecurityToken}, write into an {@code OutputStream}
 * @author Anil.Saldhana@redhat.com
 * @since Oct 19, 2010
 */
public class WSTrustRequestWriter
{
   private final XMLStreamWriter writer;

   /**
    * <p>
    * Creates a {@code WSTrustRequestWriter} that writes {@code RequestSecurityToken} instances to the specified
    * {@code OutputStream}.
    * </p>
    * 
    * @param out the stream where the request is to be written.
    * @throws ProcessingException if an error occurs while processing the request.
    */
   public WSTrustRequestWriter(OutputStream out) throws ProcessingException
   {
      this.writer = StaxUtil.getXMLStreamWriter(out);
   }

   /**
    * <p>
    * Creates a {@code WSTrustRequestWriter} that writes {@code RequestSecurityToken} instances to the specified
    * {@code Result}.
    * </p>
    * 
    * @param result the {@code Result} where the request it to be written.
    * @throws ProcessingException if an error occurs while processing the request.
    */
   public WSTrustRequestWriter(Result result) throws ProcessingException
   {
      this.writer = StaxUtil.getXMLStreamWriter(result);
   }

   /**
    * <p>
    * Creates a {@code WSTrustRequestWriter} that uses the specified {@code XMLStreamWriter} to write the request
    * objects. 
    * </p>
    * 
    * @param writer the {@code XMLStreamWriter} to be used to write requests.
    */
   public WSTrustRequestWriter(XMLStreamWriter writer)
   {
      this.writer = writer;
   }

   /**
    * Write the {@code RequestSecurityTokenCollection} into the {@code OutputStream}
    * @param requestTokenCollection
    * @param out
    * @throws ProcessingException
    */
   public void write(RequestSecurityTokenCollection requestTokenCollection) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, RST_COLLECTION, BASE_NAMESPACE);
      StaxUtil.writeNameSpace(writer, PREFIX, BASE_NAMESPACE);

      List<RequestSecurityToken> tokenList = requestTokenCollection.getRequestSecurityTokens();
      if (tokenList == null)
         throw new ProcessingException(ErrorCodes.NULL_VALUE + "RST list is null");

      for (RequestSecurityToken token : tokenList)
      {
         write(token);
      }

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   /**
    * Write the {@code RequestSecurityToken} into the {@code OutputStream}
    * @param requestToken
    * @param out
    * @throws ProcessingException
    */
   public void write(RequestSecurityToken requestToken) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, RST, BASE_NAMESPACE);
      StaxUtil.writeNameSpace(writer, PREFIX, BASE_NAMESPACE);
      String context = requestToken.getContext();
      StaxUtil.writeAttribute(writer, RST_CONTEXT, context);

      URI requestType = requestToken.getRequestType();
      if (requestType != null)
      {
         writeRequestType(writer, requestType);
      }

      URI tokenType = requestToken.getTokenType();
      if (tokenType != null)
      {
         writeTokenType(writer, tokenType);
      }

      //Deal with Issuer
      EndpointReferenceType endpoint = requestToken.getIssuer();
      if (endpoint != null)
      {
         StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.ISSUER, BASE_NAMESPACE);
         StaxUtil.writeStartElement(writer, WSAddressingConstants.WSA_PREFIX, WSAddressingConstants.ADDRESS,
               WSAddressingConstants.WSA_NS);
         StaxUtil.writeCharacters(writer, endpoint.getAddress().getValue());
         StaxUtil.writeEndElement(writer);
         StaxUtil.writeEndElement(writer);
      }

      // deal with the token lifetime.
      if (requestToken.getLifetime() != null)
      {
         Lifetime lifetime = requestToken.getLifetime();
         StaxUtil.writeStartElement(this.writer, WSTrustConstants.PREFIX, WSTrustConstants.LIFETIME,
               WSTrustConstants.BASE_NAMESPACE);
         new WSSecurityWriter(this.writer).writeLifetime(lifetime.getCreated(), lifetime.getExpires());
         StaxUtil.writeEndElement(this.writer);
      }

      //Deal with AppliesTo
      AppliesTo appliesTo = requestToken.getAppliesTo();
      if (appliesTo != null)
      {
         WSPolicyWriter wsPolicyWriter = new WSPolicyWriter(this.writer);
         wsPolicyWriter.write(appliesTo);
      }

      long keySize = requestToken.getKeySize();
      if (keySize != 0)
      {
         StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.KEY_SIZE, BASE_NAMESPACE);
         StaxUtil.writeCharacters(writer, Long.toString(keySize));
         StaxUtil.writeEndElement(writer);
      }

      URI keyType = requestToken.getKeyType();
      if (keyType != null)
      {
         StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.KEY_TYPE, BASE_NAMESPACE);
         StaxUtil.writeCharacters(writer, keyType.toString());
         StaxUtil.writeEndElement(writer);
      }
      EntropyType entropy = requestToken.getEntropy();
      if (entropy != null)
      {
         writeEntropyType(entropy);
      }

      UseKeyType useKeyType = requestToken.getUseKey();
      if (useKeyType != null)
      {
         writeUseKeyType(useKeyType);
      }

      OnBehalfOfType onBehalfOf = requestToken.getOnBehalfOf();
      if (onBehalfOf != null)
      {
         writeOnBehalfOfType(onBehalfOf);
      }

      ValidateTargetType validateTarget = requestToken.getValidateTarget();
      if (validateTarget != null)
      {
         writeValidateTargetType(validateTarget);
      }

      CancelTargetType cancelTarget = requestToken.getCancelTarget();
      if (cancelTarget != null)
      {
         writeCancelTargetType(cancelTarget);
      }

      RenewTargetType renewTarget = requestToken.getRenewTarget();
      if (renewTarget != null)
      {
         writeRenewTargetType(renewTarget);
      }

      StaxUtil.writeEndElement(writer);
      StaxUtil.flush(writer);
   }

   /**
    * Write an {@code EntropyType} to stream
    * @param entropy
    * @throws ProcessingException
    */
   private void writeEntropyType(EntropyType entropy) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.ENTROPY, BASE_NAMESPACE);

      List<Object> entropyList = entropy.getAny();
      if (entropyList != null)
      {
         for (Object entropyObj : entropyList)
         {
            if (entropyObj instanceof BinarySecretType)
            {
               BinarySecretType binarySecret = (BinarySecretType) entropyObj;
               writeBinarySecretType(writer, binarySecret);
            }
         }
      }
      StaxUtil.writeEndElement(writer);
   }

   /**
    * Write an {@code UseKeyType} to stream
    * @param useKeyType
    * @throws ProcessingException
    */
   private void writeUseKeyType(UseKeyType useKeyType) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.USE_KEY, BASE_NAMESPACE);

      List<Object> theList = useKeyType.getAny();
      for (Object useKeyTypeValue : theList)
      {
         if (useKeyTypeValue instanceof Element)
         {
            Element domElement = (Element) useKeyTypeValue;
            StaxUtil.writeDOMElement(writer, domElement);
         }
         else if (useKeyTypeValue instanceof byte[])
         {
            byte[] certificate = (byte[]) useKeyTypeValue;
            StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.X509CERT,
                  WSTrustConstants.DSIG_NS);
            StaxUtil.writeNameSpace(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.DSIG_NS);
            StaxUtil.writeCharacters(writer, new String(certificate));
            StaxUtil.writeEndElement(writer);
         }
         else if (useKeyTypeValue instanceof KeyValueType)
         {
            writeKeyValueType((KeyValueType) useKeyTypeValue);
         }
         else
            throw new RuntimeException(ErrorCodes.WRITER_UNKNOWN_TYPE + useKeyTypeValue.getClass().getName());

      }
      /*Object useKeyTypeValue = useKeyType.getAny();
      if (useKeyTypeValue instanceof Element)
      {
         Element domElement = (Element) useKeyTypeValue;
         StaxUtil.writeDOMElement(writer, domElement);
      }
      else if (useKeyTypeValue instanceof byte[])
      {
         byte[] certificate = (byte[]) useKeyTypeValue;
         StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.X509CERT,
               WSTrustConstants.DSIG_NS);
         StaxUtil.writeNameSpace(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.DSIG_NS);
         StaxUtil.writeCharacters(writer, new String(certificate));
         StaxUtil.writeEndElement(writer);
      }
      else if (useKeyTypeValue instanceof KeyValueType)
      {
         writeKeyValueType((KeyValueType) useKeyTypeValue);
      }
      else
         throw new RuntimeException(" Unknown use key type:" + useKeyTypeValue.getClass().getName());*/

      StaxUtil.writeEndElement(writer);
   }

   private void writeKeyValueType(KeyValueType type) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.XMLDSig.KEYVALUE,
            WSTrustConstants.DSIG_NS);
      StaxUtil.writeNameSpace(writer, WSTrustConstants.XMLDSig.DSIG_PREFIX, WSTrustConstants.DSIG_NS);
      if (type.getContent().size() == 0)
         throw new ProcessingException(ErrorCodes.NULL_VALUE + "KeyValueType must contain at least one value");

      for (Object obj : type.getContent())
      {
         if (obj instanceof RSAKeyValueType)
         {
            RSAKeyValueType rsaKeyValue = (RSAKeyValueType) obj;
            writeRSAKeyValueType(rsaKeyValue);
         }
      }
      StaxUtil.writeEndElement(writer);
   }

   private void writeRSAKeyValueType(RSAKeyValueType type) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, "dsig", WSTrustConstants.XMLDSig.RSA_KEYVALUE, WSTrustConstants.DSIG_NS);
      // write the rsa key modulus.
      byte[] modulus = type.getModulus();
      StaxUtil.writeStartElement(writer, "dsig", WSTrustConstants.XMLDSig.MODULUS, WSTrustConstants.DSIG_NS);
      StaxUtil.writeCharacters(writer, new String(modulus));
      StaxUtil.writeEndElement(writer);

      // write the rsa key exponent.
      byte[] exponent = type.getExponent();
      StaxUtil.writeStartElement(writer, "dsig", WSTrustConstants.XMLDSig.EXPONENT, WSTrustConstants.DSIG_NS);
      StaxUtil.writeCharacters(writer, new String(exponent));
      StaxUtil.writeEndElement(writer);

      StaxUtil.writeEndElement(writer);
   }

   /**
    * Write an {@code OnBehalfOfType} to stream
    * @param onBehalfOf
    * @param out
    * @throws ProcessingException
    */
   private void writeOnBehalfOfType(OnBehalfOfType onBehalfOf) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.ON_BEHALF_OF, BASE_NAMESPACE);
      UsernameTokenType usernameToken = (UsernameTokenType) onBehalfOf.getAny().get(0);
      WSSecurityWriter wsseWriter = new WSSecurityWriter(this.writer);
      wsseWriter.write(usernameToken);
      StaxUtil.writeEndElement(writer);
   }

   /**
    * Write an {@code ValidateTargetType} to stream
    * @param validateTarget
    * @param out
    * @throws ProcessingException
    */
   private void writeValidateTargetType(ValidateTargetType validateTarget) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.VALIDATE_TARGET, BASE_NAMESPACE);

      List<Object> list = validateTarget.getAny();
      for (Object validateTargetObj : list)
      {
         if (validateTargetObj instanceof AssertionType)
         {
            AssertionType assertion = (AssertionType) validateTargetObj;
            SAMLAssertionWriter samlAssertionWriter = new SAMLAssertionWriter(this.writer);
            samlAssertionWriter.write(assertion);
         }
         else if (validateTargetObj instanceof Element)
         {
            StaxUtil.writeDOMElement(writer, (Element) validateTargetObj);
         }
         else
            throw new ProcessingException(ErrorCodes.WRITER_UNKNOWN_TYPE + "Unknown validate target type="
                  + validateTargetObj.getClass().getName());
      }
      /*Object validateTargetObj = validateTarget.getAny();
      if (validateTargetObj != null)
      {
         if (validateTargetObj instanceof AssertionType)
         {
            AssertionType assertion = (AssertionType) validateTargetObj;
            SAMLAssertionWriter samlAssertionWriter = new SAMLAssertionWriter(this.writer);
            samlAssertionWriter.write(assertion);
         }
         else if (validateTargetObj instanceof Element)
         {
            StaxUtil.writeDOMElement(writer, (Element) validateTargetObj);
         }
         else
            throw new ProcessingException("Unknown validate target type=" + validateTargetObj.getClass().getName());
      }*/
      StaxUtil.writeEndElement(writer);
   }

   private void writeRenewTargetType(RenewTargetType renewTarget) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.RENEW_TARGET, BASE_NAMESPACE);

      List<Object> list = renewTarget.getAny();
      for (Object renewTargetObj : list)
      {
         if (renewTargetObj instanceof AssertionType)
         {
            AssertionType assertion = (AssertionType) renewTargetObj;
            SAMLAssertionWriter samlAssertionWriter = new SAMLAssertionWriter(this.writer);
            samlAssertionWriter.write(assertion);
         }
         else if (renewTargetObj instanceof Element)
         {
            StaxUtil.writeDOMElement(writer, (Element) renewTargetObj);
         }
         else
            throw new ProcessingException(ErrorCodes.WRITER_UNKNOWN_TYPE + "Unknown renew target type="
                  + renewTargetObj.getClass().getName());
      }
      /*Object renewTargetObj = renewTarget.getAny();
      if (renewTargetObj != null)
      {
         if (renewTargetObj instanceof AssertionType)
         {
            AssertionType assertion = (AssertionType) renewTargetObj;
            SAMLAssertionWriter samlAssertionWriter = new SAMLAssertionWriter(this.writer);
            samlAssertionWriter.write(assertion);
         }
         else if (renewTargetObj instanceof Element)
         {
            StaxUtil.writeDOMElement(writer, (Element) renewTargetObj);
         }
         else
            throw new ProcessingException("Unknown renew target type=" + renewTargetObj.getClass().getName());
      }*/
      StaxUtil.writeEndElement(writer);
   }

   /**
    * Write an {@code CancelTargetType} to Stream
    * @param cancelTarget
    * @param out
    * @throws ProcessingException
    */
   private void writeCancelTargetType(CancelTargetType cancelTarget) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.CANCEL_TARGET, BASE_NAMESPACE);

      List<Object> list = cancelTarget.getAny();

      for (Object cancelTargetObj : list)
      {
         if (cancelTargetObj instanceof AssertionType)
         {
            AssertionType assertion = (AssertionType) cancelTargetObj;
            SAMLAssertionWriter samlAssertionWriter = new SAMLAssertionWriter(this.writer);
            samlAssertionWriter.write(assertion);
         }
         else if (cancelTargetObj instanceof Element)
         {
            StaxUtil.writeDOMElement(writer, (Element) cancelTargetObj);
         }
         else
            throw new ProcessingException(ErrorCodes.WRITER_UNKNOWN_TYPE + "Unknown cancel target type="
                  + cancelTargetObj.getClass().getName());
      }

      /*Object cancelTargetObj = cancelTarget.getAny();
      if (cancelTargetObj != null)
      {
         if (cancelTargetObj instanceof AssertionType)
         {
            AssertionType assertion = (AssertionType) cancelTargetObj;
            SAMLAssertionWriter samlAssertionWriter = new SAMLAssertionWriter(this.writer);
            samlAssertionWriter.write(assertion);
         }
         else if (cancelTargetObj instanceof Element)
         {
            StaxUtil.writeDOMElement(writer, (Element) cancelTargetObj);
         }
         else
            throw new ProcessingException("Unknown cancel target type=" + cancelTargetObj.getClass().getName());
      }*/
      StaxUtil.writeEndElement(writer);
   }

   /**
    * Write a {@code BinarySecretType} to stream
    * @param writer
    * @param binarySecret
    * @throws ProcessingException
    */
   private void writeBinarySecretType(XMLStreamWriter writer, BinarySecretType binarySecret) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.BINARY_SECRET, BASE_NAMESPACE);
      String type = binarySecret.getType();
      StaxUtil.writeAttribute(writer, WSTrustConstants.TYPE, type);
      StaxUtil.writeCharacters(writer, new String(binarySecret.getValue()));
      StaxUtil.writeEndElement(writer);
   }

   /**
    * Write a Request Type
    * @param writer
    * @param uri
    * @throws ProcessingException
    */
   private void writeRequestType(XMLStreamWriter writer, URI uri) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.REQUEST_TYPE, BASE_NAMESPACE);
      StaxUtil.writeCharacters(writer, uri.toASCIIString());
      StaxUtil.writeEndElement(writer);
   }

   /**
    * Write Token Type
    * @param writer
    * @param uri
    * @throws ProcessingException
    */
   private void writeTokenType(XMLStreamWriter writer, URI uri) throws ProcessingException
   {
      StaxUtil.writeStartElement(writer, PREFIX, WSTrustConstants.TOKEN_TYPE, BASE_NAMESPACE);
      StaxUtil.writeCharacters(writer, uri.toASCIIString());
      StaxUtil.writeEndElement(writer);
   }
}