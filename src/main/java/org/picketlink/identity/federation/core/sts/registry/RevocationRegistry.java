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

/**
 * <p>
 * A {@code RevocationRegistry} is used to store the ids of revoked (canceled) security tokens.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public interface RevocationRegistry
{
   /**
    * <p>
    * Indicates whether the token with the specified id has been revoked or not.
    * </p>
    * 
    * @param tokenType a {@code String} representing the token type.
    * @param id a {@code String} representing the token id.
    * @return {@code true} if the specified id has been revoked; {@code false} otherwise.
    */
   public boolean isRevoked(String tokenType, String id);
   
   /**
    * <p>
    * Adds the specified id to the revocation registry. The security token type can be used to distinguish tokens
    * that may have the same id but that are of different types.
    * </p>
    * 
    * @param tokenType a {@code String} representing the security token type.
    * @param id the id to registered.
    */
   public void revokeToken(String tokenType, String id);
}
