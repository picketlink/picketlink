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
package org.picketlink.identity.federation.core.saml.md.providers;

import java.io.InputStream;
import java.security.PublicKey;

import org.picketlink.identity.federation.core.interfaces.IMetadataProvider; 
import org.picketlink.identity.federation.saml.v2.metadata.EntitiesDescriptorType;

/**
 * File based provider that handles multiple entities
 * @author Anil.Saldhana@redhat.com
 * @since Apr 21, 2009
 */
public class FileBasedEntitiesMetadataProvider extends AbstractMetadataProvider
implements IMetadataProvider<EntitiesDescriptorType>
{
   public EntitiesDescriptorType getMetaData()
   {
      throw new RuntimeException("NYI");
   }

   public boolean isMultiple()
   {
      return true;
   }

   public void injectEncryptionKey(PublicKey publicKey)
   {
   }

   public void injectFileStream(InputStream fileStream)
   {  
   }

   public void injectSigningKey(PublicKey publicKey)
   {  
   }

   public String requireFileInjection()
   {
      return null;
   } 
}