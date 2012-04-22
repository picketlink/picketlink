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
package org.picketlink.identity.federation.core.sts.registry;

import java.io.IOException;


/**
 * A registry of Security Tokens that may be issued by
 * instances of {@code SecurityTokenProvider}
 * @author Anil.Saldhana@redhat.com
 * @since Jan 4, 2011
 */
public interface SecurityTokenRegistry
{
   /**
    * Add a token to the registry with the given id
    * @param tokenID
    * @param token
    * @throws {@code IOException}
    */
   void addToken( String tokenID, Object token ) throws IOException;
   
   /**
    * Remove a token given the ID
    * @param tokenID
    * @param token
    * @throws {@code IOException}
    */
   void removeToken( String tokenID ) throws IOException;
   
   /**
    * Given the id, return a token
    * @param tokenID
    * @return
    */
   Object getToken( String tokenID ); 
}