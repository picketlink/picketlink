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
package org.picketlink.identity.federation.core.interfaces;

import java.util.Map;

import javax.xml.namespace.QName;

import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.wstrust.WSTrustException;

/**
 * <p>
 * This interface defines the methods that must be implemented by security token providers.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface SecurityTokenProvider
{
   /**
    * An enumeration that identifies the family to which
    * the security token provider belongs 
    */
   public enum FAMILY_TYPE {
      SAML2, SAML11, WS_TRUST, OPENID, OAUTH, CUSTOM;
   }

   /**
    * <p>
    * Initializes the {@code SecurityTokenProvider} using the specified properties map.
    * </p>
    * 
    * @param properties a {@code Map<String, String>} that contains the properties that have been configured for
    * this {@code SecurityTokenProvider}.
    */
   public void initialize(Map<String, String> properties);

   /**
    * Specify whether this token provider supports a particular namespace
    * @param namespace a string value representing a namespace
    * @return
    */
   public boolean supports(String namespace);

   /**
    * Token Type
    * @return
    */
   public String tokenType();

   /**
    * Provide an optional {@code QName} for configuration
    * @return
    */
   public QName getSupportedQName();

   /**
    * The family where this security token provider belongs
    * @see {@code FAMILY_TYPE}}
    * @return
    */
   public String family();

   /**
    * <p>
    * Generates a security token using the information contained in the specified request context and stores the
    * newly-created token in the context itself.
    * </p>
    * 
    * @param context the {@code ProtocolContext} to be used when generating the token.
    * @throws WSTrustException if an error occurs while creating the security token.
    */
   public void issueToken(ProtocolContext context) throws ProcessingException;

   /**
    * <p>
    * Renews the security token contained in the specified request context. This method is used when a previously
    * generated token has expired, generating a new version of the same token with different expiration semantics.
    * </p>
    * 
    * @param context the {@code ProtocolContext} that contains the token to be renewed.
    * @throws WSTrustException if an error occurs while renewing the security token.
    */
   public void renewToken(ProtocolContext context) throws ProcessingException;

   /**
    * <p>
    * Cancels the token contained in the specified request context. A security token is usually canceled when one wants
    * to make sure that the token will not be used anymore. A security token can't be renewed once it has been canceled.
    * </p>
    * 
    * @param context the {@code ProtocolContext} that contains the token to be canceled.
    * @throws WSTrustException if an error occurs while canceling the security token.
    */
   public void cancelToken(ProtocolContext context) throws ProcessingException;

   /**
    * <p>
    * Evaluates the validity of the token contained in the specified request context and sets the result in the context
    * itself. The result can be a status, a new token, or both.
    * </p>
    * 
    * @param context the {@code ProtocolContext} that contains the token to be validated.
    * @throws WSTrustException if an error occurs while validating the security token.
    */
   public void validateToken(ProtocolContext context) throws ProcessingException;
}