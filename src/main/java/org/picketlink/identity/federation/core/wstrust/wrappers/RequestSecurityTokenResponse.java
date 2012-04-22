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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.picketlink.identity.federation.ws.addressing.EndpointReferenceType;
import org.picketlink.identity.federation.ws.policy.AppliesTo;
import org.picketlink.identity.federation.ws.policy.Policy;
import org.picketlink.identity.federation.ws.policy.PolicyReference;
import org.picketlink.identity.federation.ws.trust.AllowPostdatingType;
import org.picketlink.identity.federation.ws.trust.AuthenticatorType;
import org.picketlink.identity.federation.ws.trust.DelegateToType;
import org.picketlink.identity.federation.ws.trust.EncryptionType;
import org.picketlink.identity.federation.ws.trust.EntropyType;
import org.picketlink.identity.federation.ws.trust.LifetimeType;
import org.picketlink.identity.federation.ws.trust.OnBehalfOfType;
import org.picketlink.identity.federation.ws.trust.ProofEncryptionType;
import org.picketlink.identity.federation.ws.trust.RenewingType;
import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenResponseType;
import org.picketlink.identity.federation.ws.trust.RequestedProofTokenType;
import org.picketlink.identity.federation.ws.trust.RequestedReferenceType;
import org.picketlink.identity.federation.ws.trust.RequestedSecurityTokenType;
import org.picketlink.identity.federation.ws.trust.RequestedTokenCancelledType;
import org.picketlink.identity.federation.ws.trust.StatusType;
import org.picketlink.identity.federation.ws.trust.UseKeyType;

/**
 * <p>
 * This class represents a WS-Trust {@code RequestSecurityTokenResponse}. It wraps the JAXB representation of the
 * security token response and offers a series of getter/setter methods that make it easy to work with elements that are
 * represented by the {@code Any} XML type.
 * </p>
 * <p>
 * The following shows the intended content model of a {@code RequestSecurityTokenResponse}:
 * 
 * <pre>
 *    &lt;xs:element ref='wst:TokenType' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:RequestType' /&gt;
 *    &lt;xs:element ref='wst:RequestedSecurityToken'  minOccurs='0' /&gt;
 *    &lt;xs:element ref='wsp:AppliesTo' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:RequestedAttachedReference' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:RequestedUnattachedReference' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:RequestedProofToken' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:Entropy' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:Lifetime' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:Status' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:AllowPostdating' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:Renewing' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:OnBehalfOf' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:Issuer' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:AuthenticationType' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:Authenticator' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:KeyType' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:KeySize' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:SignatureAlgorithm' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:Encryption' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:EncryptionAlgorithm' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:CanonicalizationAlgorithm' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:ProofEncryption' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:UseKey' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:SignWith' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:EncryptWith' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:DelegateTo' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:Forwardable' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wst:Delegatable' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wsp:Policy' minOccurs='0' /&gt;
 *    &lt;xs:element ref='wsp:PolicyReference' minOccurs='0' /&gt;
 *    &lt;xs:any namespace='##other' processContents='lax' minOccurs='0' maxOccurs='unbounded' /&gt;
 * </pre>
 * 
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
/**
 * <p>
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class RequestSecurityTokenResponse implements BaseRequestSecurityTokenResponse
{

   private final RequestSecurityTokenResponseType delegate;

   private URI tokenType;

   private URI requestType;

   private RequestedSecurityTokenType requestedSecurityToken;

   private AppliesTo appliesTo;

   private RequestedReferenceType requestedAttachedReference;

   private RequestedReferenceType requestedUnattachedReference;

   private RequestedProofTokenType requestedProofToken;

   private RequestedTokenCancelledType requestedTokenCancelled;

   private EntropyType entropy;

   private Lifetime lifetime;

   private StatusType status;

   private AllowPostdatingType allowPostDating;

   private RenewingType renewing;

   private OnBehalfOfType onBehalfOf;

   private EndpointReferenceType issuer;

   private URI authenticationType;

   private AuthenticatorType authenticator;

   private URI keyType;

   private long keySize;

   private URI signatureAlgorithm;

   private EncryptionType encryption;

   private URI encryptionAlgorithm;

   private URI canonicalizationAlgorithm;

   private ProofEncryptionType proofEncryption;

   private UseKeyType useKey;

   private URI signWith;

   private URI encryptWith;

   private DelegateToType delegateTo;

   private boolean forwardable;

   private boolean delegatable;

   private Policy policy;

   private PolicyReference policyReference;

   private final List<Object> extensionElements = new ArrayList<Object>();

   /**
    * <p>
    * Creates an instance of {@code RequestSecurityTokenResponse}.
    * </p>
    */
   public RequestSecurityTokenResponse()
   {
      this.delegate = new RequestSecurityTokenResponseType();
   }

   /**
    * <p>
    * Creates an instance of {@code RequestSecurityTokenResponse} using the specified delegate.
    * </p>
    * 
    * @param delegate the JAXB {@code RequestSecurityTokenResponseType} that represents a WS-Trust response.
    */
   public RequestSecurityTokenResponse(RequestSecurityTokenResponseType delegate)
   {
      this.delegate = delegate;
      // parse the delegate's Any contents.
      try
      {
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
                  this.tokenType = new URI((String) element.getValue());
               else if (localName.equalsIgnoreCase("RequestType"))
                  this.requestType = new URI((String) element.getValue());
               else if (localName.equalsIgnoreCase("RequestedSecurityToken"))
                  this.requestedSecurityToken = (RequestedSecurityTokenType) element.getValue();
               else if (localName.equalsIgnoreCase("RequestedAttachedReference"))
                  this.requestedAttachedReference = (RequestedReferenceType) element.getValue();
               else if (localName.equalsIgnoreCase("RequestedUnattachedReference"))
                  this.requestedUnattachedReference = (RequestedReferenceType) element.getValue();
               else if (localName.equalsIgnoreCase("RequestedProofToken"))
                  this.requestedProofToken = (RequestedProofTokenType) element.getValue();
               else if (localName.equalsIgnoreCase("RequestedTokenCancelled"))
                  this.requestedTokenCancelled = (RequestedTokenCancelledType) element.getValue();
               else if (localName.equalsIgnoreCase("Entropy"))
                  this.entropy = (EntropyType) element.getValue();
               else if (localName.equalsIgnoreCase("Lifetime"))
                  this.lifetime = new Lifetime((LifetimeType) element.getValue());
               else if (localName.equalsIgnoreCase("Status"))
                  this.status = (StatusType) element.getValue();
               else if (localName.equalsIgnoreCase("AllowPostdating"))
                  this.allowPostDating = (AllowPostdatingType) element.getValue();
               else if (localName.equalsIgnoreCase("Renewing"))
                  this.renewing = (RenewingType) element.getValue();
               else if (localName.equalsIgnoreCase("OnBehalfOf"))
                  this.onBehalfOf = (OnBehalfOfType) element.getValue();
               else if (localName.equalsIgnoreCase("Issuer"))
                  this.issuer = (EndpointReferenceType) element.getValue();
               else if (localName.equalsIgnoreCase("AuthenticationType"))
                  this.authenticationType = new URI((String) element.getValue());
               else if (localName.equalsIgnoreCase("Authenticator"))
                  this.authenticator = (AuthenticatorType) element.getValue();
               else if (localName.equalsIgnoreCase("KeyType"))
                  this.keyType = new URI((String) element.getValue());
               else if (localName.equalsIgnoreCase("KeySize"))
                  this.keySize = (Long) element.getValue();
               else if (localName.equalsIgnoreCase("SignatureAlgorithm"))
                  this.signatureAlgorithm = new URI((String) element.getValue());
               else if (localName.equalsIgnoreCase("Encryption"))
                  this.encryption = (EncryptionType) element.getValue();
               else if (localName.equalsIgnoreCase("EntropyAlgorithm"))
                  this.encryptionAlgorithm = new URI((String) element.getValue());
               else if (localName.equalsIgnoreCase("CanonicalizationAlgorithm"))
                  this.canonicalizationAlgorithm = new URI((String) element.getValue());
               else if (localName.equalsIgnoreCase("ProofEncryption"))
                  this.proofEncryption = (ProofEncryptionType) element.getValue();
               else if (localName.equalsIgnoreCase("UseKey"))
                  this.useKey = (UseKeyType) element.getValue();
               else if (localName.equalsIgnoreCase("SignWith"))
                  this.signWith = new URI((String) element.getValue());
               else if (localName.equalsIgnoreCase("EncryptWith"))
                  this.encryptWith = new URI((String) element.getValue());
               else if (localName.equalsIgnoreCase("DelegateTo"))
                  this.delegateTo = (DelegateToType) element.getValue();
               else if (localName.equalsIgnoreCase("Forwardable"))
                  this.forwardable = (Boolean) element.getValue();
               else if (localName.equalsIgnoreCase("Delegatable"))
                  this.delegatable = (Boolean) element.getValue();
               else
                  this.extensionElements.add(element.getValue());
            }
            else
            {
               this.extensionElements.add(obj);
            }
         }
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e.getMessage(), e);
      }
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
      return tokenType;
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
      return requestType;
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
    * Obtains the requested security token that has been set in the response.
    * </p>
    * 
    * @return a reference to the {@code RequestedSecurityTokenType} that contains the token.
    */
   public RequestedSecurityTokenType getRequestedSecurityToken()
   {
      return requestedSecurityToken;
   }

   /**
    * <p>
    * Sets the requested security token in the response.
    * </p>
    * 
    * @param requestedSecurityToken the {@code RequestedSecurityTokenType} instance to be set.
    */
   public void setRequestedSecurityToken(RequestedSecurityTokenType requestedSecurityToken)
   {
      this.requestedSecurityToken = requestedSecurityToken;
      this.delegate.addAny(requestedSecurityToken);
   }

   /**
    * <p>
    * Obtains the scope to which the security token applies.
    * </p>
    * 
    * @return a reference to the {@code AppliesTo} instance that represents the token scope.
    */
   public AppliesTo getAppliesTo()
   {
      return appliesTo;
   }

   /**
    * <p>
    * Sets the scope to which the security token applies.
    * </p>
    * 
    * @param appliesTo a reference to the {@code AppliesTo} object that represents the scope to be set.
    */
   public void setAppliesTo(AppliesTo appliesTo)
   {
      this.appliesTo = appliesTo;
      this.delegate.addAny(appliesTo);
   }

   /**
    * <p>
    * Obtains the {@code RequestedAttachedReference} that indicate how to reference the returned token when that token
    * doesn't support references using URI fragments (XML ID).
    * </p>
    * 
    * @return a {@code RequestedReferenceType} that represents the token reference.
    */
   public RequestedReferenceType getRequestedAttachedReference()
   {
      return requestedAttachedReference;
   }

   /**
    * <p>
    * Sets the {@code RequestedAttachedReference} that indicate how to reference the returned token when that token
    * doesn't support references using URI fragments (XML ID).
    * </p>
    * 
    * @param requestedAttachedReference the {@code RequestedReferenceType} instance to be set.
    */
   public void setRequestedAttachedReference(RequestedReferenceType requestedAttachedReference)
   {
      this.requestedAttachedReference = requestedAttachedReference;
      this.delegate.addAny(requestedAttachedReference);
   }

   /**
    * <p>
    * Obtains the {@code RequestedUnattachedReference} that specifies to indicate how to reference the token when it is
    * not placed inside the message.
    * </p>
    * 
    * @return a {@code RequestedReferenceType} that represents the unattached reference.
    */
   public RequestedReferenceType getRequestedUnattachedReference()
   {
      return requestedUnattachedReference;
   }

   /**
    * <p>
    * Sets the {@code RequestedUnattachedReference} that specifies to indicate how to reference the token when it is not
    * placed inside the message.
    * </p>
    * 
    * @param requestedUnattachedReference the {@code RequestedReferenceType} instance to be set.
    */
   public void setRequestedUnattachedReference(RequestedReferenceType requestedUnattachedReference)
   {
      this.requestedUnattachedReference = requestedUnattachedReference;
      this.delegate.addAny(requestedUnattachedReference);
   }

   /**
    * <p>
    * Obtains the proof of possession token that has been set in the response.
    * </p>
    * 
    * @return a reference to the {@code RequestedProofTokenType} that contains the token.
    */
   public RequestedProofTokenType getRequestedProofToken()
   {
      return requestedProofToken;
   }

   /**
    * <p>
    * Sets the proof of possesion token in the response.
    * </p>
    * 
    * @param requestedProofToken the {@code RequestedProofTokenType} instance to be set.
    */
   public void setRequestedProofToken(RequestedProofTokenType requestedProofToken)
   {
      this.requestedProofToken = requestedProofToken;
      this.delegate.addAny(requestedProofToken);
   }

   /**
    * <p>
    * Obtains the {@code RequestedTokenCancelled} section of the response, if it has been set. The presence of this
    * element indicates that the security token specified in the cancel request has been successfully canceled by
    * the STS.
    * </p>
    * 
    * @return a reference to the {@code RequestedTokenCancelledType}, or {@code null} if the response doesn't have
    * a {@code RequestedTokenCancelled} section.
    */
   public RequestedTokenCancelledType getRequestedTokenCancelled()
   {
      return this.requestedTokenCancelled;
   }

   /**
    * <p>
    * Sets the {@code RequestedTokenCancelled} section of the response. This element is used to inform the client that
    * the token specified in a cancel request has been successfully canceled by the STS.
    * </p>
    * 
    * @param requestedTokenCancelled a reference to the {@code RequestedTokenCancelledType}.
    */
   public void setRequestedTokenCancelled(RequestedTokenCancelledType requestedTokenCancelled)
   {
      this.requestedTokenCancelled = requestedTokenCancelled;
      this.delegate.addAny(requestedTokenCancelled);
   }

   /**
    * <p>
    * Obtains the entropy that has been used in creating the key.
    * </p>
    * 
    * @return a reference to the {@code EntropyType} that represents the entropy.
    */
   public EntropyType getEntropy()
   {
      return entropy;
   }

   /**
    * <p>
    * Sets the entropy that has been used in creating the key.
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
    * Obtains the lifetime of the security token.
    * </p>
    * 
    * @return a reference to the {@code Lifetime} that represents the lifetime of the security token.
    */
   public Lifetime getLifetime()
   {
      return lifetime;
   }

   /**
    * <p>
    * Sets the lifetime of the security token.
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
    * Obtains the result of a security token validation.
    * </p>
    * 
    * @return a referece to the {@code StatusType} instance that represents the status of the validation.
    */
   public StatusType getStatus()
   {
      return status;
   }

   /**
    * <p>
    * Sets the result of a security token validation.
    * </p>
    * 
    * @param status the {@code StatusType} instance to be set.
    */
   public void setStatus(StatusType status)
   {
      this.status = status;
      this.delegate.addAny(status);
   }

   /**
    * <p>
    * Checks whether the returned token is a postdated token or not.
    * </p>
    * 
    * @return {@code null} if the token is not postdated; a {@code AllowPostdatingType} otherwise.
    */
   public AllowPostdatingType getAllowPostDating()
   {
      return allowPostDating;
   }

   /**
    * <p>
    * Specifies whether the returned token is a postdated token or not.
    * </p>
    * 
    * @param allowPostDating {@code null} if the token is not postdated; a {@code AllowPostdatingType} otherwise.
    */
   public void setAllowPostDating(AllowPostdatingType allowPostDating)
   {
      this.allowPostDating = allowPostDating;
      this.delegate.addAny(allowPostDating);
   }

   /**
    * <p>
    * Obtains the renew semantics for the token request.
    * </p>
    * 
    * @return a reference to the {@code RenewingType} that represents the renew semantics for the request.
    */
   public RenewingType getRenewing()
   {
      return renewing;
   }

   /**
    * <p>
    * Sets the renew semantics for the token request.
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
    * Obtains the identity on whose behalf the token request was made.
    * </p>
    * 
    * @return a reference to the {@code OnBehalfOfType} that represents the identity on whose behalf the token request
    *         was made.
    */
   public OnBehalfOfType getOnBehalfOf()
   {
      return onBehalfOf;
   }

   /**
    * <p>
    * Specifies the identity on whose behalf the token request was made.
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
    * Obtains the type of authentication that is to be conducted.
    * </p>
    * 
    * @return a {@code URI} that identifies the authentication type.
    */
   public URI getAuthenticationType()
   {
      return authenticationType;
   }

   /**
    * <p>
    * Sets the authentication type in the response.
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
    * Obtains the authenticator that must be used in authenticating exchanges.
    * </p>
    * 
    * @return a reference to the {@code AuthenticatorType} that represents the authenticator.
    */
   public AuthenticatorType getAuthenticator()
   {
      return authenticator;
   }

   /**
    * <p>
    * Sets the authenticator that must be used in authenticating exchanges.
    * </p>
    * 
    * @param authenticator the {@code AuthenticatorType} instance to be set.
    */
   public void setAuthenticator(AuthenticatorType authenticator)
   {
      this.authenticator = authenticator;
      this.delegate.addAny(authenticator);
   }

   /**
    * <p>
    * Obtains the type of the key that has been set in the response.
    * </p>
    * 
    * @return a {@code URI} that identifies the key type.
    */
   public URI getKeyType()
   {
      return keyType;
   }

   /**
    * <p>
    * Sets the key type in the response.
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
    * Obtains the size of they key that has been set in the response.
    * </p>
    * 
    * @return a {@code long} representing the key size in bytes.
    */
   public long getKeySize()
   {
      return keySize;
   }

   /**
    * <p>
    * Sets the size of the key in the response.
    * </p>
    * 
    * @param keySize a {@code long} representing the key size in bytes.
    */
   public void setKeySize(long keySize)
   {
      this.keySize = keySize;
      this.delegate.addAny(keySize);
   }

   /**
    * <p>
    * Obtains the signature algorithm that has been set in the response.
    * </p>
    * 
    * @return a {@code URI} that represents the signature algorithm.
    */
   public URI getSignatureAlgorithm()
   {
      return signatureAlgorithm;
   }

   /**
    * <p>
    * Sets the signature algorithm in the response.
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
    * Obtains the {@code Encryption} section of the response. The {@code Encryption} element indicates that the
    * requestor desires any returned secrets in issued security tokens to be encrypted.
    * </p>
    * 
    * @return a reference to the {@code EncryptionType} object.
    */
   public EncryptionType getEncryption()
   {
      return encryption;
   }

   /**
    * <p>
    * Sets the {@code Encryption} section of the response. The {@code Encryption} element indicates that the requestor
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
    * Obtains the encryption algorithm that has been set in the response.
    * </p>
    * 
    * @return a {@code URI} that represents the encryption algorithm.
    */
   public URI getEncryptionAlgorithm()
   {
      return encryptionAlgorithm;
   }

   /**
    * <p>
    * Sets the encryption algorithm in the response.
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
    * Obtains the canonicalization algorithm that has been set in the response.
    * </p>
    * 
    * @return a {@code URI} that represents the canonicalization algorithm.
    */
   public URI getCanonicalizationAlgorithm()
   {
      return canonicalizationAlgorithm;
   }

   /**
    * <p>
    * Sets the canonicalization algorithm in the response.
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
    * Obtains the {@code ProofEncryption} section of the response. The {@code ProofEncryption} indicates that the
    * requestor desires any returned secrets in issued security tokens to be encrypted.
    * </p>
    * 
    * @return a reference to the {@code ProofEncryptionType} object.
    */
   public ProofEncryptionType getProofEncryption()
   {
      return proofEncryption;
   }

   /**
    * <p>
    * Sets the {@code ProofEncryption} section of the response. The {@code ProofEncryption} indicates that the requestor
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
    * Obtains the key that used in the returned token.
    * </p>
    * 
    * @return a reference to the {@code UseKeyType} instance that represents the key used.
    */
   public UseKeyType getUseKey()
   {
      return useKey;
   }

   /**
    * <p>
    * Sets the key that used in the returned token.
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
    * Obtains the signature algorithm used with the issued security token.
    * </p>
    * 
    * @return a {@code URI} representing the algorithm used.
    */
   public URI getSignWith()
   {
      return signWith;
   }

   /**
    * <p>
    * Sets the signature algorithm used with the issued security token.
    * </p>
    * 
    * @param signWith a {@code URI} representing the algorithm used.
    */
   public void setSignWith(URI signWith)
   {
      this.signWith = signWith;
      this.delegate.addAny(signWith.toString());
   }

   /**
    * <p>
    * Obtains the encryption algorithm used with the issued security token.
    * </p>
    * 
    * @return a {@code URI} representing the encryption algorithm used.
    */
   public URI getEncryptWith()
   {
      return encryptWith;
   }

   /**
    * <p>
    * Sets the encryption algorithm used with the issued security token.
    * </p>
    * 
    * @param encryptWith a {@code URI} representing the algorithm used.
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
      return delegateTo;
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
    * Indicates whether the requested token has been marked as "forwardable" or not. In general, this flag is used when
    * a token is normally bound to the requestor's machine or service. Using this flag, the returned token MAY be used
    * from any source machine so long as the key is correctly proven.
    * </p>
    * 
    * @return {@code true} if the requested token has been marked as "forwardable"; {@code false} otherwise.
    */
   public boolean isForwardable()
   {
      return forwardable;
   }

   /**
    * <p>
    * Specifies whether the requested token has been marked as "forwardable" or not. In general, this flag is used when
    * a token is normally bound to the requestor's machine or service. Using this flag, the returned token MAY be used
    * from any source machine so long as the key is correctly proven.
    * </p>
    * 
    * @param forwardable {@code true} if the requested token has been marked as "forwardable"; {@code false} otherwise.
    */
   public void setForwardable(boolean forwardable)
   {
      this.forwardable = forwardable;
      this.delegate.addAny(forwardable);
   }

   /**
    * <p>
    * Indicates whether the requested token has been marked as "delegatable" or not. Using this flag, the returned token
    * MAY be delegated to another party.
    * </p>
    * 
    * @return {@code true} if the requested token has been marked as "delegatable"; {@code false} otherwise.
    */
   public boolean isDelegatable()
   {
      return delegatable;
   }

   /**
    * <p>
    * Specifies whether the requested token has been marked as "delegatable" or not. Using this flag, the returned token
    * MAY be delegated to another party.
    * </p>
    * 
    * @param delegatable {@code true} if the requested token has been marked as "delegatable"; {@code false} otherwise.
    */
   public void setDelegatable(boolean delegatable)
   {
      this.delegatable = delegatable;
      this.delegate.addAny(delegatable);
   }

   /**
    * <p>
    * Obtains the {@code Policy} that was associated with the request. The policy specifies defaults that can be
    * overridden by the previous properties.
    * </p>
    * 
    * @return a reference to the {@code Policy} that was associated with the request.
    */
   public Policy getPolicy()
   {
      return policy;
   }

   /**
    * <p>
    * Sets the {@code Policy} in the response. The policy specifies defaults that can be overridden by the previous
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
    * Obtains the reference to the {@code Policy} that was associated with the request.
    * </p>
    * 
    * @return a {@code PolicyReference} that specifies where the {@code Policy} can be found.
    */
   public PolicyReference getPolicyReference()
   {
      return policyReference;
   }

   /**
    * <p>
    * Sets the reference to the {@code Policy} that was associated with the request.
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
    * Obtains the response context.
    * </p>
    * 
    * @return a {@code String} that identifies the original request.
    */
   public String getContext()
   {
      return this.delegate.getContext();
   }

   /**
    * <p>
    * Sets the response context.
    * </p>
    * 
    * @param context a {@code String} that identifies the original request.
    */
   public void setContext(String context)
   {
      this.delegate.setContext(context);
   }

   /**
    * <p>
    * Obtains a map that contains attributes that aren't bound to any typed property on the response. This is a live
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
    * Gets a reference to the list that holds all response element values.
    * </p>
    * 
    * @return a {@code List<Object>} containing all values specified in the response.
    */
   public List<Object> getAny()
   {
      return this.delegate.getAny();
   }

   /**
    * <p>
    * Obtains a reference to the {@code RequestSecurityTokenResponseType} delegate.
    * </p>
    * 
    * @return a reference to the delegate instance.
    */
   public RequestSecurityTokenResponseType getDelegate()
   {
      return this.delegate;
   }
}