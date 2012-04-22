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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenCollectionType;
import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenType;

/**
 * <p>
 * This class represents a WS-Trust {@code RequestSecurityTokenCollection}. It wraps the JAXB representation of the
 * security token collection request.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class RequestSecurityTokenCollection implements BaseRequestSecurityToken
{

   private final RequestSecurityTokenCollectionType delegate;

   private final List<RequestSecurityToken> requestSecurityTokens;

   /**
    * <p>
    * Creates an instance of {@code RequestSecurityTokenCollection}.
    * </p>
    */
   public RequestSecurityTokenCollection()
   {
      this.requestSecurityTokens = new ArrayList<RequestSecurityToken>();
      this.delegate = new RequestSecurityTokenCollectionType();
   }

   /**
    * <p>
    * Creates an instance of {@code RequestSecurityTokenCollection} using the specified delegate.
    * </p>
    * 
    * @param delegate the JAXB {@code RequestSecurityTokenCollectionType} that represents a WS-Trust request collection.
    */
   public RequestSecurityTokenCollection(RequestSecurityTokenCollectionType delegate)
   {
      this.delegate = delegate;
      this.requestSecurityTokens = new ArrayList<RequestSecurityToken>();
      for (RequestSecurityTokenType request : delegate.getRequestSecurityToken())
         this.requestSecurityTokens.add(new RequestSecurityToken(request));
   }

   /**
    * <p>
    * Obtains the collection of {@code RequestSecurityToken} objects. The returned collection is immutable, so addition
    * or removal of requests must be carried by the appropriate add/remove methods.
    * </p>
    * 
    * @return a {@code List<RequestSecurityToken>} containing the token requests.
    */
   public List<RequestSecurityToken> getRequestSecurityTokens()
   {
      return Collections.unmodifiableList(this.requestSecurityTokens);
   }

   /**
    * <p>
    * Adds the specified {@code RequestSecurityToken} object to the collection of token requests.
    * </p>
    * 
    * @param request the {@code RequestSecurityToken} to be added.
    */
   public void addRequestSecurityToken(RequestSecurityToken request)
   {
      this.delegate.add(request.getDelegate());
      this.requestSecurityTokens.add(request);
   }

   /**
    * <p>
    * Removes the specified {@code RequestSecurityToken} object from the collection of token requests.
    * </p>
    * 
    * @param request the {@code RequestSecurityToken} to be removed.
    */
   public void removeRequestSecurityToken(RequestSecurityToken request)
   {
      this.delegate.remove(request.getDelegate());
      this.requestSecurityTokens.remove(request);
   }

   /**
    * <p>
    * Obtains a reference to the {@code RequestSecurityTokenCollectionType} delegate.
    * </p>
    * 
    * @return a reference to the delegate instance.
    */
   public RequestSecurityTokenCollectionType getDelegate()
   {
      return this.delegate;
   }
}
