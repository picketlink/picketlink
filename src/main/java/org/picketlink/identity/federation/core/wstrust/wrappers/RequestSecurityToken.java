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
package org.picketlink.identity.federation.core.wstrust.wrappers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.picketlink.identity.federation.ws.policy.AppliesTo;
import org.picketlink.identity.federation.ws.policy.Policy;
import org.picketlink.identity.federation.ws.policy.PolicyReference;
import org.picketlink.identity.federation.ws.trust.AllowPostdatingType;
import org.picketlink.identity.federation.ws.trust.CancelTargetType;
import org.picketlink.identity.federation.ws.trust.ClaimsType;
import org.picketlink.identity.federation.ws.trust.DelegateToType;
import org.picketlink.identity.federation.ws.trust.EncryptionType;
import org.picketlink.identity.federation.ws.trust.EntropyType;
import org.picketlink.identity.federation.ws.trust.LifetimeType;
import org.picketlink.identity.federation.ws.trust.OnBehalfOfType;
import org.picketlink.identity.federation.ws.trust.ProofEncryptionType;
import org.picketlink.identity.federation.ws.trust.RenewTargetType;
import org.picketlink.identity.federation.ws.trust.RenewingType;
import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenType;
import org.picketlink.identity.federation.ws.trust.UseKeyType;
import org.picketlink.identity.federation.ws.trust.ValidateTargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * This class represents a WS-Trust {@code RequestSecurityToken}. It wraps the JAXB representation of the security
 * token request and offers a series of getter/setter methods that make it easy to work with elements that are
 * represented by the {@code Any} XML type.
 * </p>
 * <p>
 * The following shows the intended content model of a {@code RequestSecurityToken}:
 * 
 * <pre>
 *     &lt;xs:element ref='wst:TokenType' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:RequestType' /&gt;
 *     &lt;xs:element ref='wsp:AppliesTo' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:Claims' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:Entropy' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:Lifetime' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:AllowPostdating' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:Renewing' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:OnBehalfOf' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:Issuer' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:AuthenticationType' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:KeyType' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:KeySize' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:SignatureAlgorithm' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:Encryption' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:EncryptionAlgorithm' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:CanonicalizationAlgorithm' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:ProofEncryption' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:UseKey' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:SignWith' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:EncryptWith' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:DelegateTo' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:Forwardable' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wst:Delegatable' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wsp:Policy' minOccurs='0' /&gt;
 *     &lt;xs:element ref='wsp:PolicyReference' minOccurs='0' /&gt;
 *     &lt;xs:any namespace='##other' processContents='lax' minOccurs='0' maxOccurs='unbounded' /&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class RequestSecurityToken implements BaseRequestSecurityToken
{

   private final RequestSecurityTokenType delegate;

   private URI tokenType;

   private URI requestType;

   private AppliesTo appliesTo;

   private ClaimsType claims;

   private EntropyType entropy;

   private Lifetime lifetime;

   private AllowPostdatingType allowPostDating;

   private RenewingType renewing;

   private OnBehalfOfType onBehalfOf;

   private EndpointReferenceType issuer;

   private URI authenticationType;

   private URI keyType;

   private long keySize;

   private URI signatureAlgorithm;

   private EncryptionType encryption;

   private URI encryptionAlgorithm;

   private URI canonicalizationAlgorithm;

   private URI keyWrapAlgorithm;

   private ProofEncryptionType proofEncryption;

   private UseKeyType useKey;

   private URI signWith;

   private URI encryptWith;

   private DelegateToType delegateTo;

   private boolean forwardable;

   private boolean delegatable;

   private Policy policy;

   private PolicyReference policyReference;

   private ValidateTargetType validateTarget;

   private RenewTargetType renewTarget;

   private CancelTargetType cancelTarget;

   private final List<Object> extensionElements = new ArrayList<Object>();

   private Document rstDocument;

   private URI binaryValueType;

   private Node binaryToken;

   /**
    * <p>
    * Creates an instance of {@code RequestSecurityToken}.
    * </p>
    */
   public RequestSecurityToken()
   {
      this.delegate = new RequestSecurityTokenType();
   }

   /**
    * <p>
    * Creates an instance of {@code RequestSecurityToken} using the specified delegate.
    * </p>
    * 
    * @param delegate the JAXB {@code RequestSecurityTokenType} that represents a WS-Trust token request.
    */
   public RequestSecurityToken(RequestSecurityTokenType delegate)
   {
      this.delegate = delegate;
      // parse the delegate's Any contents.
      for (Object obj : this.delegate.getAny())
      {
         if (obj instanceof AppliesTo)
         {
            this.appliesTo = (AppliesTo) obj;
         }
         else if (obj instanceof Policy)
         {
            this.policy = (Policy) obj;
         }
         else if (obj instanceof PolicyReference)
         {
            this.policyReference = (PolicyReference) obj;
         }
         else if (obj instanceof JAXBElement)
         {
            JAXBElement<?> element = (JAXBElement<?>) obj;
            String localName = element.getName().getLocalPart();
            if (localName.equalsIgnoreCase("TokenType"))
               this.tokenType = URI.create((String) element.getValue());
            else if (localName.equalsIgnoreCase("RequestType"))
               this.requestType = URI.create((String) element.getValue());
            else if (localName.equalsIgnoreCase("Claims"))
               this.claims = (ClaimsType) element.getValue();
            else if (localName.equalsIgnoreCase("Entropy"))
               this.entropy = (EntropyType) element.getValue();
            else if (localName.equalsIgnoreCase("Lifetime"))
               this.lifetime = new Lifetime((LifetimeType) element.getValue());
            else if (localName.equalsIgnoreCase("AllowPostdating"))
               this.allowPostDating = (AllowPostdatingType) element.getValue();
            else if (localName.equalsIgnoreCase("Renewing"))
               this.renewing = (RenewingType) element.getValue();
            else if (localName.equalsIgnoreCase("OnBehalfOf"))
               this.onBehalfOf = (OnBehalfOfType) element.getValue();
            else if (localName.equalsIgnoreCase("Issuer"))
               this.issuer = (EndpointReferenceType) element.getValue();
            else if (localName.equalsIgnoreCase("AuthenticationType"))
               this.authenticationType = URI.create((String) element.getValue());
            else if (localName.equalsIgnoreCase("KeyType"))
               this.keyType = URI.create((String) element.getValue());
            else if (localName.equalsIgnoreCase("KeySize"))
               this.keySize = (Long) element.getValue();
            else if (localName.equalsIgnoreCase("SignatureAlgorithm"))
               this.signatureAlgorithm = URI.create((String) element.getValue());
            else if (localName.equalsIgnoreCase("Encryption"))
               this.encryption = (EncryptionType) element.getValue();
            else if (localName.equalsIgnoreCase("EntropyAlgorithm"))
               this.encryptionAlgorithm = URI.create((String) element.getValue());
            else if (localName.equalsIgnoreCase("CanonicalizationAlgorithm"))
               this.canonicalizationAlgorithm = URI.create((String) element.getValue());
            else if (localName.equalsIgnoreCase("KeyWrapAlgorithm"))
               this.keyWrapAlgorithm = URI.create((String) element.getValue());
            else if (localName.equalsIgnoreCase("ProofEncryption"))
               this.proofEncryption = (ProofEncryptionType) element.getValue();
            else if (localName.equalsIgnoreCase("UseKey"))
               this.useKey = (UseKeyType) element.getValue();
            else if (localName.equalsIgnoreCase("SignWith"))
               this.signWith = URI.create((String) element.getValue());
            else if (localName.equalsIgnoreCase("EncryptWith"))
               this.encryptWith = URI.create((String) element.getValue());
            else if (localName.equalsIgnoreCase("DelegateTo"))
               this.delegateTo = (DelegateToType) element.getValue();
            else if (localName.equalsIgnoreCase("Forwardable"))
               this.forwardable = (Boolean) element.getValue();
            else if (localName.equalsIgnoreCase("Delegatable"))
               this.delegatable = (Boolean) element.getValue();
            else if (localName.equalsIgnoreCase("CancelTarget"))
               this.cancelTarget = (CancelTargetType) element.getValue();
            else if (localName.equalsIgnoreCase("RenewTarget"))
               this.renewTarget = (RenewTargetType) element.getValue();
            else if (localName.equalsIgnoreCase("ValidateTarget"))
               this.validateTarget = (ValidateTargetType) element.getValue();
            else
               this.extensionElements.add(element.getValue());
         }
         else
         {
            this.extensionElements.add(obj);
         }
      }
   }

   /**
    * Creates an instance of {@code RequestSecurityTokenType} and {@code Document}
    * @param delegate
    * @param rstDocument
    */
   public RequestSecurityToken(RequestSecurityTokenType delegate, Document rstDocument)
   {
      this(delegate);
      this.rstDocument = rstDocument;
   }

   /**
    * Get the Binary Value Type
    * @return
    */
   public URI getBinaryValueType()
   {
      return binaryValueType;
   }

   public void setBinaryValueType(URI binaryValueType)
   {
      this.binaryValueType = binaryValueType;
   }

   /**
    * Get the Binary Token from the SOAP Header
    * @return
    */
   public Node getBinaryToken()
   {
      return binaryToken;
   }

   public void setBinaryToken(Node binaryToken)
   {
      this.binaryToken = binaryToken;
   }

   /**
    * <p>
    * Obtains the {@code URI} that identifies the token type.
    * </p>
    * 
    * @return a {@code URI} that represents the token type.
    */
   public URI getTokenType()
   {
      return this.tokenType;
   }

   /**
    * <p>
    * Sets the token type.
    * </p>
    * 
    * @param tokenType a {@code URI} that identifies the token type.
    */
   public void setTokenType(URI tokenType)
   {
      this.tokenType = tokenType;
      this.delegate.addAny(tokenType.toString());

   }

   /**
    * <p>
    * Obtains the request type.
    * </p>
    * 
    * @return a {@code URI} that identifies the request type.
    */
   public URI getRequestType()
   {
      return this.requestType;
   }

   /**
    * <p>
    * Sets the request type. The type must be one of the request types described in the WS-Trust specification.
    * </p>
    * 
    * @param requestType a {@code URI} that identifies the request type.
    */
   public void setRequestType(URI requestType)
   {
      this.requestType = requestType;
      this.delegate.addAny(requestType.toString());
   }

   /**
    * <p>
    * Obtains the {@code AppliesTo} value of this request. The {@code AppliesTo} object identifies the service provider
    * (web service) that requires a token to be presented by clients. A STS uses this object to find the type of the
    * token that is accepted by the service provider so that it can issue appropriate tokens to clients.
    * </p>
    * 
    * @return the reference to the {@code AppliesTo} object.
    */
   public AppliesTo getAppliesTo()
   {
      return this.appliesTo;
   }

   /**
    * <p>
    * Sets the {@code AppliesTo} value of this request. The {@code AppliesTo} object identifies the service provider
    * (web service) that requires a token to be presented by clients. A STS uses this object to find the type of the
    * token that is accepted by the service provider so that it can issue appropriate tokens to clients.
    * </p>
    * 
    * @param appliesTo a reference to the {@code AppliesTo} object that identifies the service provider.
    */
   public void setAppliesTo(AppliesTo appliesTo)
   {
      this.appliesTo = appliesTo;
      this.delegate.addAny(appliesTo);
   }

   /**
    * <p>
    * Obtains the set of claims of this request.
    * </p>
    * 
    * @return a reference to the {@code ClaimsType} object that represents the request's claims.
    */
   public ClaimsType getClaims()
   {
      return this.claims;
   }

   /**
    * <p>
    * Sets the claims of this request.
    * </p>
    * 
    * @param claims the {@code ClaimsType} object that represents the claims to be set.
    */
   public void setClaims(ClaimsType claims)
   {
      this.claims = claims;
      this.delegate.addAny(claims);
   }

   /**
    * <p>
    * Obtains the entropy that will be used in creating the key.
    * </p>
    * 
    * @return a reference to the {@code EntropyType} that represents the entropy.
    */
   public EntropyType getEntropy()
   {
      return this.entropy;
   }

   /**
    * <p>
    * Sets the entropy that must be used when creating the key.
    * </p>
    * 
    * @param entropy the {@code EntropyType} representing the entropy to be set.
    */
   public void setEntropy(EntropyType entropy)
   {
      this.entropy = entropy;
      this.delegate.addAny(entropy);
   }

   /**
    * <p>
    * Obtains the desired lifetime of the requested token.
    * </p>
    * 
    * @return a reference to the {@code Lifetime} that represents the lifetime.
    */
   public Lifetime getLifetime()
   {
      return this.lifetime;
   }

   /**
    * <p>
    * Sets the desired lifetime of the requested token.
    * </p>
    * 
    * @param lifetime the {@code Lifetime} object representing the lifetime to be set.
    */
   public void setLifetime(Lifetime lifetime)
   {
      this.lifetime = lifetime;
      this.delegate.addAny(lifetime.getDelegate());
   }

   /**
    * <p>
    * Checks whether a request for a postdated token should be allowed or not.
    * </p>
    * 
    * @return {@code null} if the token can't have a future lifetime (e.g. a token to be used the next day); a
    *         {@code AllowPostdatingType} otherwise.
    */
   public AllowPostdatingType getAllowPostDating()
   {
      return this.allowPostDating;
   }

   /**
    * <p>
    * Specifies whether a request for a postdated token should be allowed or not.
    * </p>
    * 
    * @param allowPostDating {@code null} if the token can't have a future lifetime (e.g. a token to be used the next
    *            day); a {@code AllowPostdatingType} otherwise.
    */
   public void setAllowPostDating(AllowPostdatingType allowPostDating)
   {
      this.allowPostDating = allowPostDating;
      this.delegate.addAny(allowPostDating);
   }

   /**
    * <p>
    * Obtains the renew semantics for this request.
    * </p>
    * 
    * @return a reference to the {@code RenewingType} that represents the renew semantics for this request.
    */
   public RenewingType getRenewing()
   {
      return this.renewing;
   }

   /**
    * <p>
    * Sets the renew semantics for this request.
    * </p>
    * 
    * @param renewing the {@code RenewingType} object representing the semantics to be set.
    */
   public void setRenewing(RenewingType renewing)
   {
      this.renewing = renewing;
      this.delegate.addAny(renewing);
   }

   /**
    * <p>
    * Obtains the identity on whose behalf this request was made.
    * </p>
    * 
    * @return a reference to the {@code OnBehalfOfType} that represents the identity on whose behalf this request was
    *         made.
    */
   public OnBehalfOfType getOnBehalfOf()
   {
      return this.onBehalfOf;
   }

   /**
    * <p>
    * Specifies the identity on whose behalf this request is being made.
    * </p>
    * 
    * @param onBehalfOf the {@code OnBehalfOfType} object representing the identity to be set.
    */
   public void setOnBehalfOf(OnBehalfOfType onBehalfOf)
   {
      this.onBehalfOf = onBehalfOf;
      this.delegate.addAny(onBehalfOf);
   }

   /**
    * <p>
    * Obtains the issuer of the token included in the request in the scenarios where the requestor is obtaining a token
    * on behalf of another party.
    * </p>
    * 
    * @return a reference to the {@code EndpointReferenceType} that represents the issuer.
    */
   public EndpointReferenceType getIssuer()
   {
      return this.issuer;
   }

   /**
    * <p>
    * Sets the issuer of the token included in the request in scenarios where the requestor is obtaining a token on
    * behalf of another party.
    * </p>
    * 
    * @param issuer the {@code EndpointReferenceType} object representing the issuer to be set.
    */
   public void setIssuer(EndpointReferenceType issuer)
   {
      this.issuer = issuer;
      this.delegate.addAny(issuer);
   }

   /**
    * <p>
    * Obtains the type of authentication that has been set as part of the request.
    * </p>
    * 
    * @return a {@code URI} that identifies the desired authentication type.
    */
   public URI getAuthenticationType()
   {
      return this.authenticationType;
   }

   /**
    * <p>
    * Sets the authentication type in the request.
    * </p>
    * 
    * @param authenticationType a {@code URI} that identifies the authentication type to be set.
    */
   public void setAuthenticationType(URI authenticationType)
   {
      this.authenticationType = authenticationType;
      this.delegate.addAny(authenticationType.toString());
   }

   /**
    * <p>
    * Obtains the type of the key that has been set in the request.
    * </p>
    * 
    * @return a {@code URI} that identifies the key type.
    */
   public URI getKeyType()
   {
      return this.keyType;
   }

   /**
    * <p>
    * Sets the key type in the request.
    * </p>
    * 
    * @param keyType a {@code URI} that specifies the key type.
    */
   public void setKeyType(URI keyType)
   {
      this.keyType = keyType;
      this.delegate.addAny(keyType.toString());
   }

   /**
    * <p>
    * Obtains the size of they key that has been set in the request.
    * </p>
    * 
    * @return a {@code long} representing the key size.
    */
   public long getKeySize()
   {
      return this.keySize;
   }

   /**
    * <p>
    * Sets the size of the key in the request.
    * </p>
    * 
    * @param keySize a {@code long} representing the key size.
    */
   public void setKeySize(long keySize)
   {
      this.keySize = keySize;
      this.delegate.addAny(keySize);
   }

   /**
    * <p>
    * Obtains the signature algorithm that has been set in the request.
    * </p>
    * 
    * @return a {@code URI} that represents the signature algorithm.
    */
   public URI getSignatureAlgorithm()
   {
      return this.signatureAlgorithm;
   }

   /**
    * <p>
    * Sets the signature algorithm in the request.
    * </p>
    * 
    * @param signatureAlgorithm a {@code URI} that represents the algorithm to be set.
    */
   public void setSignatureAlgorithm(URI signatureAlgorithm)
   {
      this.signatureAlgorithm = signatureAlgorithm;
      this.delegate.addAny(signatureAlgorithm.toString());
   }

   /**
    * <p>
    * Obtains the {@code Encryption} section of the request. The {@code Encryption} element indicates that the requestor
    * desires any returned secrets in issued security tokens to be encrypted.
    * </p>
    * 
    * @return a reference to the {@code EncryptionType} object.
    */
   public EncryptionType getEncryption()
   {
      return this.encryption;
   }

   /**
    * <p>
    * Sets the {@code Encryption} section of the request. The {@code Encryption} element indicates that the requestor
    * desires any returned secrets in issued security tokens to be encrypted.
    * </p>
    * 
    * @param encryption the {@code EncryptionType} to be set.
    */
   public void setEncryption(EncryptionType encryption)
   {
      this.encryption = encryption;
      this.delegate.addAny(encryption);
   }

   /**
    * <p>
    * Obtains the encryption algorithm that has been set in the request.
    * </p>
    * 
    * @return a {@code URI} that represents the encryption algorithm.
    */
   public URI getEncryptionAlgorithm()
   {
      return this.encryptionAlgorithm;
   }

   /**
    * <p>
    * Sets the encryption algorithm in the request.
    * </p>
    * 
    * @param encryptionAlgorithm a {@code URI} that represents the encryption algorithm to be set.
    */
   public void setEncryptionAlgorithm(URI encryptionAlgorithm)
   {
      this.encryptionAlgorithm = encryptionAlgorithm;
      this.delegate.addAny(encryptionAlgorithm.toString());
   }

   /**
    * <p>
    * Obtains the canonicalization algorithm that has been set in the request.
    * </p>
    * 
    * @return a {@code URI} that represents the canonicalization algorithm.
    */
   public URI getCanonicalizationAlgorithm()
   {
      return this.canonicalizationAlgorithm;
   }

   /**
    * <p>
    * Sets the canonicalization algorithm in the request.
    * </p>
    * 
    * @param canonicalizationAlgorithm a {@code URI} that represents the algorithm to be set.
    */
   public void setCanonicalizationAlgorithm(URI canonicalizationAlgorithm)
   {
      this.canonicalizationAlgorithm = canonicalizationAlgorithm;
      this.delegate.addAny(canonicalizationAlgorithm.toString());
   }

   /**
    * <p>
    * Obtains the key wrap algorithm that has been set in the request.
    * </p>
    * 
    * @return a {@code URI} that represents the key wrap algorithm.
    */
   public URI getKeyWrapAlgorithm()
   {
      return this.keyWrapAlgorithm;
   }

   /**
    * <p>
    * Sets the key wrap algorithm in the request.
    * </p>
    * 
    * @param keyWrapAlgorithm a {@code URI} that represents the algorithm to be set.
    */
   public void setKeyWrapAlgorithm(URI keyWrapAlgorithm)
   {
      this.keyWrapAlgorithm = keyWrapAlgorithm;
   }

   /**
    * <p>
    * Obtains the {@code ProofEncryption} section of the request. The {@code ProofEncryption} indicates that the
    * requester desires any returned secrets in issued security tokens to be encrypted.
    * </p>
    * 
    * @return a reference to the {@code ProofEncryptionType} object.
    */
   public ProofEncryptionType getProofEncryption()
   {
      return this.proofEncryption;
   }

   /**
    * <p>
    * Sets the {@code ProofEncryption} section of the request. The {@code ProofEncryption} indicates that the requester
    * desires any returned secrets in issued security tokens to be encrypted.
    * </p>
    * 
    * @param proofEncryption the {@code ProofEncryptionType} to be set.
    */
   public void setProofEncryption(ProofEncryptionType proofEncryption)
   {
      this.proofEncryption = proofEncryption;
      this.delegate.addAny(proofEncryption);
   }

   /**
    * <p>
    * Obtains the key that should be used in the returned token.
    * </p>
    * 
    * @return a reference to the {@code UseKeyType} instance that represents the key to be used.
    */
   public UseKeyType getUseKey()
   {
      return this.useKey;
   }

   /**
    * <p>
    * Sets the key that should be used in the returned token.
    * </p>
    * 
    * @param useKey the {@code UseKeyType} instance to be set.
    */
   public void setUseKey(UseKeyType useKey)
   {
      this.useKey = useKey;
      this.delegate.addAny(useKey);
   }

   /**
    * <p>
    * Obtains the signature algorithm that should be used with the issued security token.
    * </p>
    * 
    * @return a {@code URI} representing the algorithm that should be used.
    */
   public URI getSignWith()
   {
      return this.signWith;
   }

   /**
    * <p>
    * Sets the signature algorithm that should be used with the issued security token.
    * </p>
    * 
    * @param signWith a {@code URI} representing the algorithm to be used.
    */
   public void setSignWith(URI signWith)
   {
      this.signWith = signWith;
      this.delegate.addAny(signWith.toString());
   }

   /**
    * <p>
    * Obtains the encryption algorithm that should be used with the issued security token.
    * </p>
    * 
    * @return a {@code URI} representing the encryption algorithm that should be used.
    */
   public URI getEncryptWith()
   {
      return this.encryptWith;
   }

   /**
    * <p>
    * Sets the encryption algorithm that should be used with the issued security token.
    * </p>
    * 
    * @param encryptWith a {@code URI} representing the algorithm to be used.
    */
   public void setEncryptWith(URI encryptWith)
   {
      this.encryptWith = encryptWith;
      this.delegate.addAny(encryptWith.toString());
   }

   /**
    * <p>
    * Obtains the identity to which the requested token should be delegated.
    * </p>
    * 
    * @return a reference to the {@code DelegateToType} instance that represents the identity.
    */
   public DelegateToType getDelegateTo()
   {
      return this.delegateTo;
   }

   /**
    * <p>
    * Sets the identity to which the requested token should be delegated.
    * </p>
    * 
    * @param delegateTo the {@code DelegateToType} object representing the identity to be set.
    */
   public void setDelegateTo(DelegateToType delegateTo)
   {
      this.delegateTo = delegateTo;
      this.delegate.addAny(delegateTo);
   }

   /**
    * <p>
    * Indicates whether the requested token should be marked as "forwardable" or not. In general, this flag is used when
    * a token is normally bound to the requestor's machine or service. Using this flag, the returned token MAY be used
    * from any source machine so long as the key is correctly proven.
    * </p>
    * 
    * @return {@code true} if the requested token should be marked as "forwardable"; {@code false} otherwise.
    */
   public boolean isForwardable()
   {
      return this.forwardable;
   }

   /**
    * <p>
    * Specifies whether the requested token should be marked as "forwardable" or not. In general, this flag is used when
    * a token is normally bound to the requestor's machine or service. Using this flag, the returned token MAY be used
    * from any source machine so long as the key is correctly proven.
    * </p>
    * 
    * @param forwardable {@code true} if the requested token should be marked as "forwardable"; {@code false} otherwise.
    */
   public void setForwardable(boolean forwardable)
   {
      this.forwardable = forwardable;
      this.delegate.addAny(forwardable);
   }

   /**
    * <p>
    * Indicates whether the requested token should be marked as "delegatable" or not. Using this flag, the returned
    * token MAY be delegated to another party.
    * </p>
    * 
    * @return {@code true} if the requested token should be marked as "delegatable"; {@code false} otherwise.
    */
   public boolean isDelegatable()
   {
      return this.delegatable;
   }

   /**
    * <p>
    * Specifies whether the requested token should be marked as "delegatable" or not. Using this flag, the returned
    * token MAY be delegated to another party.
    * </p>
    * 
    * @param delegatable {@code true} if the requested token should be marked as "delegatable"; {@code false} otherwise.
    */
   public void setDelegatable(boolean delegatable)
   {
      this.delegatable = delegatable;
      this.delegate.addAny(delegatable);
   }

   /**
    * <p>
    * Obtains the {@code Policy} associated with the request. The policy specifies defaults that can be overridden by
    * the previous properties.
    * </p>
    * 
    * @return a reference to the {@code Policy} that has been set in the request.
    */
   public Policy getPolicy()
   {
      return this.policy;
   }

   /**
    * <p>
    * Sets the {@code Policy} in the request. The policy specifies defaults that can be overridden by the previous
    * properties.
    * </p>
    * 
    * @param policy the {@code Policy} instance to be set.
    */
   public void setPolicy(Policy policy)
   {
      this.policy = policy;
      this.delegate.addAny(policy);
   }

   /**
    * <p>
    * Obtains the reference to the {@code Policy} that should be used.
    * </p>
    * 
    * @return a {@code PolicyReference} that specifies where the {@code Policy} can be found.
    */
   public PolicyReference getPolicyReference()
   {
      return this.policyReference;
   }

   /**
    * <p>
    * Sets the reference to the {@code Policy} that should be used.
    * </p>
    * 
    * @param policyReference the {@code PolicyReference} object to be set.
    */
   public void setPolicyReference(PolicyReference policyReference)
   {
      this.policyReference = policyReference;
      this.delegate.addAny(policyReference);
   }

   /**
    * <p>
    * Obtains the list of request elements that are not part of the standard content model.
    * </p>
    * 
    * @return a {@code List<Object>} containing the extension elements.
    */
   public List<Object> getExtensionElements()
   {
      return Collections.unmodifiableList(this.extensionElements);
   }

   /**
    * <p>
    * Obtains the request context.
    * </p>
    * 
    * @return a {@code String} that identifies the request.
    */
   public String getContext()
   {
      return this.delegate.getContext();
   }

   /**
    * <p>
    * Sets the request context.
    * </p>
    * 
    * @param context a {@code String} that identifies the request.
    */
   public void setContext(String context)
   {
      this.delegate.setContext(context);
   }

   /**
    * <p>
    * Obtains the {@code CancelTarget} section of the request. This element identifies the token that is to be canceled.
    * </p>
    * 
    * @return a reference to the {@code CancelTargetType} that represents the {@code CancelTarget} section of the
    *         WS-Trust cancel request.
    */
   public CancelTargetType getCancelTarget()
   {
      return this.cancelTarget;
   }

   /**
    * <p>
    * Sets the {@code CancelTarget} section of the request. This element identifies the token that is to be canceled.
    * </p>
    * 
    * @param cancelTarget a reference to the {@code CancelTargetType} that identifies the token that must be canceled.
    */
   public void setCancelTarget(CancelTargetType cancelTarget)
   {
      this.cancelTarget = cancelTarget;
      this.delegate.addAny(cancelTarget);
   }

   /**
    * <p>
    * Obtains the {@code RenewTarget} section of the request. This element identifies the token that is to be renewed.
    * </p>
    * 
    * @return a reference to the {@code RenewTargetType} that represents the {@code RenewTarget} section of the WS-Trust
    *         renew request.
    */
   public RenewTargetType getRenewTarget()
   {
      return this.renewTarget;
   }

   /**
    * <p>
    * Sets the {@code RenewTarget} section of the request. This element identifies the token that is to be renewed.
    * </p>
    * 
    * @param renewTarget a reference to the {@code RenewTargetType} that identifies the token that must be renewed.
    */
   public void setRenewTarget(RenewTargetType renewTarget)
   {
      this.renewTarget = renewTarget;
      this.delegate.addAny(renewTarget);
   }

   /**
    * <p>
    * Obtains the {@code ValidateTarget} section of the request. This element identifies the token that is to be
    * validated.
    * </p>
    * 
    * @return a reference to the {@code ValidateTargetType} that represents the {@code ValidateTarget} section of the
    *         WS-Trust validate request.
    */
   public ValidateTargetType getValidateTarget()
   {
      return this.validateTarget;
   }

   /**
    * Return the element in the document that represents
    * the validate type
    * @return
    */
   public Element getValidateTargetElement()
   {
      if (rstDocument == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "RST Document");

      String ns = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
      String localPart = "ValidateTarget";

      NodeList nodeList = rstDocument.getElementsByTagNameNS(ns, localPart);
      if (nodeList != null && nodeList.getLength() > 0)
         return (Element) nodeList.item(0);
      else
         return null;
   }

   /**
    * <p>
    * Returns the element in the document that represents the renew target type.
    * </p>
    * 
    * @return the {@code Element} that represents the renew target type, or {@code null} if no renew target is found in
    * the document.
    */
   public Element getRenewTargetElement()
   {
      if (this.rstDocument == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "RST Document");
      String ns = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
      String localName = "RenewTarget";

      NodeList nodeList = rstDocument.getElementsByTagNameNS(ns, localName);
      if (nodeList != null && nodeList.getLength() > 0)
         return (Element) nodeList.item(0);
      else
         return null;
   }

   /**
    * <p>
    * Returns the element in the document that represents the cancel target type.
    * </p>
    * 
    * @return the {@code Element} that represents the renew target type, or {@code null} if no renew target is found in
    * the document.
    */
   public Element getCancelTargetElement()
   {
      if (this.rstDocument == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "RST Document");
      String ns = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
      String localName = "CancelTarget";

      NodeList nodeList = rstDocument.getElementsByTagNameNS(ns, localName);
      if (nodeList != null && nodeList.getLength() > 0)
         return (Element) nodeList.item(0);
      else
         return null;
   }

   /**
    * <p>
    * Sets the {@code ValidateTarget} section of the request. This elements identifies the token that is to be
    * validated.
    * </p>
    * 
    * @param validateTarget a reference to the {@code ValidateTargetType} that identifies the token that must be
    *            validated.
    */
   public void setValidateTarget(ValidateTargetType validateTarget)
   {
      this.validateTarget = validateTarget;
      this.delegate.addAny(validateTarget);
   }

   /**
    * <p>
    * Obtains a map that contains attributes that aren't bound to any typed property on the request. This is a live
    * reference, so attributes can be added/changed/removed directly. For this reason, there is no setter method.
    * </p>
    * 
    * @return a {@code Map<QName, String>} that contains the attributes.
    */
   public Map<QName, String> getOtherAttributes()
   {
      return this.delegate.getOtherAttributes();
   }

   /**
    * <p>
    * Gets a reference to the list that holds all request element values.
    * </p>
    * 
    * @return a {@code List<Object>} containing all values specified in the request.
    */
   public List<Object> getAny()
   {
      return this.delegate.getAny();
   }

   /**
    * <p>
    * Obtains a reference to the {@code RequestSecurityTokenType} delegate.
    * </p>
    * 
    * @return a reference to the delegate instance.
    */
   public RequestSecurityTokenType getDelegate()
   {
      return this.delegate;
   }

   /**
    * Get the {@code Document} document representing the request
    * @return
    */
   public Document getRSTDocument()
   {
      return this.rstDocument;
   }

   public void setRSTDocument(Document rstDocument)
   {
      this.rstDocument = rstDocument;
   }

   @Override
   public String toString()
   {
      return "RequestSecurityToken [delegate=" + delegate + ", tokenType=" + tokenType + ", requestType=" + requestType
            + ", appliesTo=" + appliesTo + ", claims=" + claims + ", entropy=" + entropy + ", lifetime=" + lifetime
            + ", allowPostDating=" + allowPostDating + ", renewing=" + renewing + ", onBehalfOf=" + onBehalfOf
            + ", issuer=" + issuer + ", authenticationType=" + authenticationType + ", keyType=" + keyType
            + ", keySize=" + keySize + ", signatureAlgorithm=" + signatureAlgorithm + ", encryption=" + encryption
            + ", encryptionAlgorithm=" + encryptionAlgorithm + ", canonicalizationAlgorithm="
            + canonicalizationAlgorithm + ", keyWrapAlgorithm=" + keyWrapAlgorithm + ", proofEncryption="
            + proofEncryption + ", useKey=" + useKey + ", signWith=" + signWith + ", encryptWith=" + encryptWith
            + ", delegateTo=" + delegateTo + ", forwardable=" + forwardable + ", delegatable=" + delegatable
            + ", policy=" + policy + ", policyReference=" + policyReference + ", validateTarget=" + validateTarget
            + ", renewTarget=" + renewTarget + ", cancelTarget=" + cancelTarget + ", extensionElements="
            + extensionElements + ", rstDocument=" + rstDocument + "]";
   }
}