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

import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenResponseCollectionType;
import org.picketlink.identity.federation.ws.trust.RequestSecurityTokenResponseType;

/**
 * <p>
 * This class represents a WS-Trust {@code RequestSecurityTokenResponseCollection}. It wraps the JAXB representation of
 * the security token collection response.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class RequestSecurityTokenResponseCollection implements BaseRequestSecurityTokenResponse
{

   private final RequestSecurityTokenResponseCollectionType delegate;

   private final List<RequestSecurityTokenResponse> requestSecurityTokenResponses;

   /**
    * <p>
    * Creates an instance of {@code RequestSecurityTokenResponseCollection}.
    * </p>
    */
   public RequestSecurityTokenResponseCollection()
   {
      this.requestSecurityTokenResponses = new ArrayList<RequestSecurityTokenResponse>();
      this.delegate = new RequestSecurityTokenResponseCollectionType();
   }

   /**
    * <p>
    * Creates an instance of {@code RequestSecurityTokenResponseCollection} using the specified delegate.
    * </p>
    * 
    * @param delegate the JAXB {@code RequestSecurityTokenResponseCollectionType} that represents a WS-Trust request
    *            collection.
    */
   public RequestSecurityTokenResponseCollection(RequestSecurityTokenResponseCollectionType delegate)
   {
      this.delegate = delegate;
      this.requestSecurityTokenResponses = new ArrayList<RequestSecurityTokenResponse>();
      for (RequestSecurityTokenResponseType response : delegate.getRequestSecurityTokenResponse())
         this.requestSecurityTokenResponses.add(new RequestSecurityTokenResponse(response));
   }

   /**
    * <p>
    * Obtains the collection of {@code RequestSecurityTokenResponse} objects. The returned collection is immutable, so
    * addition or removal of requests must be carried by the appropriate add/remove methods.
    * </p>
    * 
    * @return a {@code List<RequestSecurityToken>} containing the token requests.
    */
   public List<RequestSecurityTokenResponse> getRequestSecurityTokenResponses()
   {
      return Collections.unmodifiableList(this.requestSecurityTokenResponses);
   }

   /**
    * <p>
    * Adds the specified {@code RequestSecurityTokenResponse} object to the collection of token requests.
    * </p>
    * 
    * @param request the {@code RequestSecurityTokenResponse} to be added.
    */
   public void addRequestSecurityTokenResponse(RequestSecurityTokenResponse response)
   {
      this.delegate.add(response.getDelegate());
      this.requestSecurityTokenResponses.add(response);
   }

   /**
    * <p>
    * Removes the specified {@code RequestSecurityTokenResponse} object from the collection of token requests.
    * </p>
    * 
    * @param request the {@code RequestSecurityTokenResponse} to be removed.
    */
   public void removeRequestSecurityTokenResponse(RequestSecurityTokenResponse response)
   {
      this.delegate.remove(response.getDelegate());
      this.requestSecurityTokenResponses.remove(response);
   }

   /**
    * <p>
    * Obtains a reference to the {@code RequestSecurityTokenResponseCollectionType} delegate.
    * </p>
    * 
    * @return a reference to the delegate instance.
    */
   public RequestSecurityTokenResponseCollectionType getDelegate()
   {
      return this.delegate;
   }

}
