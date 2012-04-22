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

import org.w3c.dom.Element;

/**
 * <p>
 * Standard implementation of the {@code SecurityToken} interface. This implementation stores the issued token as an
 * {@code Element}. The token providers are responsible for marshaling the security token into an {@code Element}
 * instance because the security token marshaling process falls out of the scope of the STS (the STS only deals with
 * WS-Trust classes and doesn't know how to marshal each specific token type).
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class StandardSecurityToken implements SecurityToken
{
   private final String tokenType;

   private final String tokenId;

   private final Element token;

   /**
    * <p>
    * Creates an instance of {@code StandardSecurityToken} with the specified parameters.
    * </p>
    * 
    * @param tokenType
    *           a {@code String} representing the type of the security token. This is usually the same type as specified
    *           in the WS-Trust request message.
    * @param token
    *           the security token in its {@code Element} form (i.e. the marshaled security token).
    * @param tokenID
    *           a {@code String} representing the id of the security token.
    */
   public StandardSecurityToken(String tokenType, Element token, String tokenID)
   {
      this.tokenType = tokenType;
      this.tokenId = tokenID;
      this.token = token;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.SecurityToken#getTokenType()
    */
   public String getTokenType()
   {
      return this.tokenType;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.SecurityToken#getTokenValue()
    */
   public Object getTokenValue()
   {
      return this.token;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.picketlink.identity.federation.core.wstrust.SecurityToken#getTokenID()
    */
   public String getTokenID()
   {
      return this.tokenId;
   }
}
