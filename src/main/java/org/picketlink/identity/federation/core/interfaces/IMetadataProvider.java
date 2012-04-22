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
package org.picketlink.identity.federation.core.interfaces;

import java.io.InputStream;
import java.security.PublicKey;
import java.util.Map;

/**
 * MetadataProvider
 * @author Anil.Saldhana@redhat.com
 * @since Apr 21, 2009
 */
public interface IMetadataProvider<T>
{
   /**
    * Initialize the provider with options
    * @param options
    */
   void init(Map<String,String> options);
   
   /**
    * Is multiple descriptors attached?
    * @return
    */
   boolean isMultiple();
   
   /**
    * Get the Metadata descriptors
    * @return
    */
   T getMetaData();
   
   /**
    * Provider indicates that it requires
    * an injection of File instance
    * @return File Name (need injection) or null
    */
   String requireFileInjection();
   
   /**
    * Inject a File instance depending on
    * @see #requireFileInjection() method
    * @param fileStream
    */
   void injectFileStream(InputStream fileStream);
 
   /**
    * Inject a public key used for signing
    * @param publicKey
    */
   void injectSigningKey(PublicKey publicKey);
   
   /**
    * Inject a public key used for encryption
    * @param publicKey
    */
   void injectEncryptionKey(PublicKey publicKey);
}