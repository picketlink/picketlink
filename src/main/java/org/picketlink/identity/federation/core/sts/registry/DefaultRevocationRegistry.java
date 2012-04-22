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
package org.picketlink.identity.federation.core.sts.registry;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * A simple {@code RevocationRegistry} that keeps the revoked token ids in a memory-only cache. This registry is only
 * used if no other implementation has been configured and it doesn't persist the revoked ids. For these reasons it is
 * highly recommended that this implementation be used only in testing scenarios.
 * </p>
 * 
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */
public class DefaultRevocationRegistry implements RevocationRegistry
{
   private static Set<String> ids = new HashSet<String>();
   
   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.plugins.RevocationRegistry#isRevoked(java.lang.String, java.lang.String)
    */
   public boolean isRevoked(String tokenType, String id)
   {
      return ids.contains(id);
   }

   /*
    * (non-Javadoc)
    * @see org.picketlink.identity.federation.core.wstrust.plugins.RevocationRegistry#revokeToken(java.lang.String, java.lang.String)
    */
   public void revokeToken(String tokenType, String id)
   {
      ids.add(id);
   }
}