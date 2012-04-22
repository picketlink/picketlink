/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.picketlink.identity.federation.core.sts.registry;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * <p>
 * {@code RevokedToken} is a simple JPA entity used by the {@code JPABasedRevocationRegistry} to persist the ids of
 * the revoked security tokens.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
@Entity
public class RevokedToken
{

   @Column
   private String tokenType;
   
   @Id
   private String tokenId;

   /**
    * <p>
    * Default constructor.
    * </p>
    */
   public RevokedToken()
   {
   }

   /**
    * <p>
    * Creates an instance of {@code RevokedToken} with the specified token type and token id.
    * </p>
    * 
    * @param tokenType a {@code String} representing the token type.
    * @param tokenId a {@code String} representing the token id.
    */
   public RevokedToken(String tokenType, String tokenId)
   {
      this.tokenType = tokenType;
      this.tokenId = tokenId;
   }
   
   /**
    * <p>
    * Obtains the type of the revoked security token.
    * </p>
    * 
    * @return a {@code String} containing the revoked token type.
    */
   public String getTokenType()
   {
      return this.tokenType;
   }

   /**
    * <p>
    * Sets the type of revoked security token.
    * </p>
    * 
    * @param tokenType a {@code String} containing the type to be set.
    */
   public void setTokenType(String tokenType)
   {
      this.tokenType = tokenType;
   }

   /**
    * <p>
    * Obtains the id of the revoked security token.
    * </p>
    * 
    * @return a {@code String} containing the revoked token id.
    */
   public String getTokenId()
   {
      return this.tokenId;
   }

   /**
    * <p>
    * Sets the id of the revoked security token.
    * </p>
    * 
    * @param tokenId a {@code String} containing the id to be set.
    */
   public void setTokenId(String tokenId)
   {
      this.tokenId = tokenId;
   }
   
   
}
