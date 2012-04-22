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

import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityToken;
import org.picketlink.identity.federation.core.wstrust.wrappers.RequestSecurityTokenResponse;
import org.w3c.dom.Document;

/**
 * <p>
 * The {@code WSTrustRequestHandler} interface defines the methods that will be responsible for handling the different
 * types of WS-Trust request messages.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface WSTrustRequestHandler
{  
   /**
    * <p>
    * Initializes the concrete {@code WSTrustRequestHandler} instance.
    * </p>
    * 
    * @param configuration a reference to object that contains the STS configuration.
    */
   public void initialize(STSConfiguration configuration);

   /**
    * <p>
    * Generates a security token according to the information specified in the request message and returns the created
    * token in the response.
    * </p>
    * 
    * @param request the security token request message.
    * @param callerPrincipal the {@code Principal} of the ws-trust token requester.
    * @return a {@code RequestSecurityTokenResponse} containing the generated token.
    * @throws WSTrustException if an error occurs while handling the request message.
    */
   public RequestSecurityTokenResponse issue(RequestSecurityToken request, Principal callerPrincipal)
         throws WSTrustException;

   /**
    * <p>
    * Renews the security token as specified in the request message, returning the renewed token in the response.
    * </p>
    * 
    * @param request the request message that contains the token to be renewed.
    * @param callerPrincipal the {@code Principal} of the ws-trust token requester.
    * @return a {@code RequestSecurityTokenResponse} containing the renewed token.
    * @throws WSTrustException if an error occurs while handling the renewal process.
    */
   public RequestSecurityTokenResponse renew(RequestSecurityToken request, Principal callerPrincipal)
         throws WSTrustException;

   /**
    * <p>
    * Cancels the security token as specified in the request message.
    * </p>
    * 
    * @param request the request message that contains the token to be canceled.
    * @param callerPrincipal the {@code Principal} of the ws-trust token requester.
    * @return a {@code RequestSecurityTokenResponse} indicating whether the token has been canceled or not.
    * @throws WSTrustException if an error occurs while handling the cancellation process.
    */
   public RequestSecurityTokenResponse cancel(RequestSecurityToken request, Principal callerPrincipal)
         throws WSTrustException;

   /**
    * <p>
    * Validates the security token as specified in the request message.
    * </p>
    * 
    * @param request the request message that contains the token to be validated.
    * @param callerPrincipal the {@code Principal} of the ws-trust token requester.
    * @return a {@code RequestSecurityTokenResponse} containing the validation status or a new token.
    * @throws WSTrustException if an error occurs while handling the validation process.
    */
   public RequestSecurityTokenResponse validate(RequestSecurityToken request, Principal callerPrincipal)
         throws WSTrustException;
   
   /**
    * Perform Post Processing on the generated RSTR Collection Document
    * Steps such as signing and encryption need to be done here.
    * @param rstrDocument
    * @param request
    * @return
    * @throws WSTrustException
    */
   public Document postProcess(Document rstrDocument, RequestSecurityToken request) throws WSTrustException;
}
