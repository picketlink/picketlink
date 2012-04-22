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
package org.picketlink.identity.federation.ws.trust;

import org.picketlink.identity.federation.ws.addressing.AnyAddressingType;

/**
 * 
 *         Actual content model is non-deterministic, hence wildcard. The following shows intended content model:
 * 
 *         <xs:element ref='wst:TokenType' minOccurs='0' />
 *         <xs:element ref='wst:RequestType' />
 *         <xs:element ref='wst:RequestedSecurityToken'  minOccurs='0' />
 *         <xs:element ref='wsp:AppliesTo' minOccurs='0' />
 *         <xs:element ref='wst:RequestedAttachedReference' minOccurs='0' />
 *         <xs:element ref='wst:RequestedUnattachedReference' minOccurs='0' />
 *         <xs:element ref='wst:RequestedProofToken' minOccurs='0' />
 *         <xs:element ref='wst:Entropy' minOccurs='0' />
 *         <xs:element ref='wst:Lifetime' minOccurs='0' />
 *         <xs:element ref='wst:Status' minOccurs='0' />
 *         <xs:element ref='wst:AllowPostdating' minOccurs='0' />
 *         <xs:element ref='wst:Renewing' minOccurs='0' />
 *         <xs:element ref='wst:OnBehalfOf' minOccurs='0' />
 *         <xs:element ref='wst:Issuer' minOccurs='0' />
 *         <xs:element ref='wst:AuthenticationType' minOccurs='0' />
 *         <xs:element ref='wst:Authenticator' minOccurs='0' />
 *         <xs:element ref='wst:KeyType' minOccurs='0' />
 *         <xs:element ref='wst:KeySize' minOccurs='0' />
 *         <xs:element ref='wst:SignatureAlgorithm' minOccurs='0' />
 *         <xs:element ref='wst:Encryption' minOccurs='0' />
 *         <xs:element ref='wst:EncryptionAlgorithm' minOccurs='0' />
 *         <xs:element ref='wst:CanonicalizationAlgorithm' minOccurs='0' />
 *         <xs:element ref='wst:ProofEncryption' minOccurs='0' />
 *         <xs:element ref='wst:UseKey' minOccurs='0' />
 *         <xs:element ref='wst:SignWith' minOccurs='0' />
 *         <xs:element ref='wst:EncryptWith' minOccurs='0' />
 *         <xs:element ref='wst:DelegateTo' minOccurs='0' />
 *         <xs:element ref='wst:Forwardable' minOccurs='0' />
 *         <xs:element ref='wst:Delegatable' minOccurs='0' />
 *         <xs:element ref='wsp:Policy' minOccurs='0' />
 *         <xs:element ref='wsp:PolicyReference' minOccurs='0' />
 *         <xs:any namespace='##other' processContents='lax' minOccurs='0' maxOccurs='unbounded' />
 * 
 *       
 * 
 * <p>Java class for RequestSecurityTokenResponseType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RequestSecurityTokenResponseType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="Context" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
public class RequestSecurityTokenResponseType extends AnyAddressingType
{
   protected String context;

   /**
    * Gets the value of the context property.
    * 
    * @return
    *     possible object is
    *     {@link String }
    *     
    */
   public String getContext()
   {
      return context;
   }

   /**
    * Sets the value of the context property.
    * 
    * @param value
    *     allowed object is
    *     {@link String }
    *     
    */
   public void setContext(String value)
   {
      this.context = value;
   }
}