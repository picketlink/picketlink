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
package org.picketlink.identity.federation.core.wstrust;

import java.security.Principal;
import java.security.PublicKey;
import java.util.Map;

import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.interfaces.ProtocolContext;
import org.picketlink.identity.federation.core.interfaces.SecurityTokenProvider;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.ws.trust.RequestedReferenceType;
import org.picketlink.identity.federation.ws.trust.StatusType;
import org.picketlink.identity.xmlsec.w3.xmldsig.KeyInfoType;

/**
 * <p>
 * The {@code WSTrustRequestContext} contains all the information that is relevant for the security token request
 * processing. Its attributes are divided into two groups: attributes set by the request handler before calling a token
 * provider, and attributes set by the token provider after processing the token request.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class WSTrustRequestContext implements ProtocolContext
{

   // information supplied by the request handler.
   private String tokenIssuer;

   private PublicKey providerPublicKey;

   private Principal onBehalfOfPrincipal;
   
   private final Principal callerPrincipal;
   
   private final RequestSecurityToken request;

   private KeyInfoType proofTokenInfo;

   private Map<String, Object> claimedAttributes;

   // information supplied by the token provider.
   private SecurityToken securityToken;

   private StatusType status;

   private RequestedReferenceType attachedReference;

   private RequestedReferenceType unattachedReference;

   private String tokenType;

   private QName qname;

   /**
    * <p>
    * Creates an instance of {@code WSTrustRequestContext} using the specified request.
    * </p>
    * 
    * @param request a {@code RequestSecurityToken} object that contains the information about the security token
    *            request.
    * @param callerPrincipal the {@code Principal} of the security token requester.
    */
   public WSTrustRequestContext(RequestSecurityToken request, Principal callerPrincipal)
   {
      this.request = request;
      this.callerPrincipal = callerPrincipal;
   }

   /**
    * <p>
    * Obtains the name of the token issuer (security token service name).
    * </p>
    * 
    * @return a {@code String} representing the token issuer name.
    */
   public String getTokenIssuer()
   {
      return tokenIssuer;
   }

   /**
    * <p>
    * Sets the name of the token issuer.
    * </p>
    * 
    * @param tokenIssuer a {@code String} representing the token issuer name.
    */
   public void setTokenIssuer(String tokenIssuer)
   {
      this.tokenIssuer = tokenIssuer;
   }

   /**
    * <p>
    * Obtains the {@code PublicKey} of the service provider that requires a security token.
    * </p>
    * 
    * @return the service provider's {@code PublicKey}.
    */
   public PublicKey getServiceProviderPublicKey()
   {
      return this.providerPublicKey;
   }

   /**
    * <p>
    * Sets the {@code PublicKey} of the service provider that requires a security token.
    * </p>
    * 
    * @param providerPublicKey the service provider's {@code PublicKey}.
    */
   public void setServiceProviderPublicKey(PublicKey providerPublicKey)
   {
      this.providerPublicKey = providerPublicKey;
   }

   /**
    * <p>
    * Obtains the principal on behalf of which the WS-Trust request was made.
    * </p>
    * 
    * @return a {@code Principal} instance.
    */
   public Principal getOnBehalfOfPrincipal()
   {
      return this.onBehalfOfPrincipal;
   }
   
   /**
    * <p>
    * Sets the principal on behalf of which the request was made.
    * </p>
    * 
    * @param onBehalfOfPrincipal a {@code Principal} instance.
    */
   public void setOnBehalfOfPrincipal(Principal onBehalfOfPrincipal)
   {
      this.onBehalfOfPrincipal = onBehalfOfPrincipal;
   }
   
   /**
    * <p>
    * Obtains the principal of the WS-Trust token requester.
    * </p>
    * 
    * @return a reference to the caller {@code Principal} object.
    */
   public Principal getCallerPrincipal()
   {
      return this.callerPrincipal;
   }

   /**
    * <p>
    * Obtains the object the contains the information about the security token request.
    * </p>
    * 
    * @return a reference to the {@code RequestSecurityToken} instance.
    */
   public RequestSecurityToken getRequestSecurityToken()
   {
      return this.request;
   }

   /**
    * <p>
    * Obtains the {@code KeyInfoType} that contains the proof-of-possession token.
    * </p>
    * 
    * @return a reference to the {@code KeyInfoType} that wraps the proof-of-possession token.
    */
   public KeyInfoType getProofTokenInfo()
   {
      return this.proofTokenInfo;
   }

   /**
    * <p>
    * Sets the {@code KeyInfoType} that contains the proof-of-possession token.
    * </p>
    * 
    * @param proofTokenInfo a reference to the {@code KeyInfoType} that wraps the proof-of-possession token.
    */
   public void setProofTokenInfo(KeyInfoType proofTokenInfo)
   {
      this.proofTokenInfo = proofTokenInfo;
   }

   /**
    * <p>
    * Gets the {@code Map} that contains the attributes claimed by the caller. Token providers use this method to
    * obtain the attributes that must be inserted in the security token.
    * </p>
    * 
    * @return a {@code Map<String, Object>} that contains the caller's attributes keyed by the attribute name.
    */
   public Map<String, Object> getClaimedAttributes()
   {
      return this.claimedAttributes;
   }

   /**
    * <p>
    * Sets the caller's attributes. The caller uses the {@code Claims} section of the WS-Trust request to specify the
    * attributes that need to be present in the generated security token. The token service parses this section and
    * (possibly) interacts with other services to determine the values of the required attributes. After the attributes
    * have been determined the STS uses this method to set them in the request context and make them available for
    * token providers.
    * </p>
    * 
    * @param attributes a {@code Map<String, Object} that contains the caller's attributes keyed by the attribute name.
    */
   public void setClaimedAttributes(Map<String, Object> attributes)
   {
      this.claimedAttributes = attributes;
   }

   /**
    * <p>
    * Obtains the security token set by the token provider.
    * </p>
    * 
    * @return a reference to the {@code SecurityToken} instance.
    */
   public SecurityToken getSecurityToken()
   {
      return this.securityToken;
   }

   /**
    * <p>
    * Sets the security token in the context.
    * </p>
    * 
    * @param token the {@code SecurityToken} instance to be set.
    */
   public void setSecurityToken(SecurityToken token)
   {
      this.securityToken = token;
   }

   /**
    * <p>
    * Obtains the status of the security token validation.
    * </p>
    * 
    * @return a reference to the resulting {@code StatusType}.
    */
   public StatusType getStatus()
   {
      return this.status;
   }

   /**
    * <p>
    * Sets the status of the security token validation.
    * </p>
    * 
    * @param status a reference to the {@code StatusType} that represents the validation status.
    */
   public void setStatus(StatusType status)
   {
      this.status = status;
   }

   /**
    * <p>
    * Obtains the security token attached reference. This reference is used to locate the token inside the WS-Trust
    * response message when that token doesn't support references using URI fragments.
    * </p>
    * 
    * @return a {@code RequestedReferenceType} representing the attached reference.
    */
   public RequestedReferenceType getAttachedReference()
   {
      return this.attachedReference;
   }

   /**
    * <p>
    * Sets the security token attached reference. This reference is used to locate the token inside the WS-Trust
    * response message when that token doesn't support references using URI fragments.
    * </p>
    * 
    * @param attachedReference a {@code RequestedReferenceType} representing the attached reference.
    */
   public void setAttachedReference(RequestedReferenceType attachedReference)
   {
      this.attachedReference = attachedReference;
   }

   /**
    * <p>
    * Obtains the security token unattached reference. This reference is used to locate the token when it is not placed
    * inside the WS-Trust response message.
    * </p>
    * 
    * @return a {@code RequestedReferenceType} representing the unattached reference.
    */
   public RequestedReferenceType getUnattachedReference()
   {
      return this.unattachedReference;
   }

   /**
    * <p>
    * Sets the security token unattached reference. This reference is used to locate the token when it is not placed
    * inside the WS-Trust response message.
    * </p>
    * 
    * @param unattachedReference a {@code RequestedReferenceType} representing the unattached reference.
    */
   public void setUnattachedReference(RequestedReferenceType unattachedReference)
   {
      this.unattachedReference = unattachedReference;
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#serviceName()
    */
   public String serviceName()
   {
      return WSTrustUtil.getServiceNameFromAppliesTo( request );
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#tokenType()
    */
   public String tokenType()
   { 
      return tokenType; 
   }
   
   public void setTokenType( String tokenType )
   {
      this.tokenType = tokenType;
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#getQName()
    */
   public QName getQName()
   {   
      return qname;
   }
   
   public void setQName( QName qname )
   {
      this.qname = qname;
   }

   /**
    * @see org.picketlink.identity.federation.core.interfaces.ProtocolContext#family()
    */
   public String family()
   {
      return SecurityTokenProvider.FAMILY_TYPE.WS_TRUST.toString();
   } 
   
   
   @Override
   public String toString()
   {
      return "WSTrustRequestContext [tokenIssuer=" + tokenIssuer + ", providerPublicKey=" + providerPublicKey
            + ", onBehalfOfPrincipal=" + onBehalfOfPrincipal + ", callerPrincipal=" + callerPrincipal + ", request="
            + request + ", proofTokenInfo=" + proofTokenInfo + ", claimedAttributes=" + claimedAttributes
            + ", securityToken=" + securityToken + ", status=" + status + ", attachedReference=" + attachedReference
            + ", unattachedReference=" + unattachedReference + ", tokenType=" + tokenType + ", qname=" + qname + "]";
   }
}