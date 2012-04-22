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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;

/**
 * A File based implementation of the {@code SecurityTokenRegistry}
 * @author Anil.Saldhana@redhat.com
 * @since Jan 4, 2011
 */
public class FileBasedTokenRegistry extends FileBasedSTSOperations implements SecurityTokenRegistry
{
   protected static final String FILE_NAME = "token.registry";

   // the file that stores the tokens.
   protected File registryFile;

   protected Map<String, TokenHolder> holders = new HashMap<String, TokenHolder>();

   public FileBasedTokenRegistry()
   {
      this(FILE_NAME);
   }

   public FileBasedTokenRegistry(String fileName)
   {
      super();
      if (directory == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "directory");

      // check if the default registry file exists.
      this.registryFile = create(fileName);

      try
      {
         read();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#addToken(java.lang.String, java.lang.Object)
    */
   public void addToken(String tokenID, Object token) throws IOException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(PicketLinkCoreSTS.rte);

      if (!(token instanceof Serializable))
         throw new IOException(ErrorCodes.NOT_SERIALIZABLE + "Token");

      holders.put(tokenID, new TokenHolder(tokenID, token));
      flush();
   }

   /**
    * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#removeToken(java.lang.String)
    */
   public void removeToken(String tokenID) throws IOException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(PicketLinkCoreSTS.rte);

      holders.remove(tokenID);
      flush();
   }

   /**
    * @see org.picketlink.identity.federation.core.sts.registry.SecurityTokenRegistry#getToken(java.lang.String)
    */
   public Object getToken(String tokenID)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(PicketLinkCoreSTS.rte);

      TokenHolder holder = holders.get(tokenID);
      if (holder != null)
         return holder.token;

      return null;
   }

   protected synchronized void flush() throws IOException
   {
      FileOutputStream fos = new FileOutputStream(registryFile);
      ObjectOutputStream oos = new ObjectOutputStream(fos);
      oos.writeObject(holders);
      oos.close();
   }

   @SuppressWarnings("unchecked")
   protected synchronized void read() throws IOException
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
         sm.checkPermission(PicketLinkCoreSTS.rte);

      FileInputStream fis = new FileInputStream(registryFile);
      ObjectInputStream ois = new ObjectInputStream(fis);
      try
      {
         holders = (Map<String, TokenHolder>) ois.readObject();
      }
      catch (ClassNotFoundException e)
      {
         throw new IOException(e);
      }
      finally
      {
         ois.close();
      }
   }

   protected static class TokenHolder implements Serializable
   {
      private static final long serialVersionUID = 1L;

      String id;

      Object token;

      public TokenHolder(String id, Object token)
      {
         super();
         this.id = id;
         this.token = token;
      }

      public String getId()
      {
         return id;
      }

      public Object getToken()
      {
         return token;
      }
   }
}