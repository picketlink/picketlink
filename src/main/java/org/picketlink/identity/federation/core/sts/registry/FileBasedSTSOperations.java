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
import java.io.IOException;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;

/**
 * A base class for file based STS operations
 * @author Anil.Saldhana@redhat.com
 * @since Jan 4, 2011
 */
public abstract class FileBasedSTSOperations
{
   private static Logger logger = Logger.getLogger(FileBasedSTSOperations.class);

   protected File directory;

   public FileBasedSTSOperations()
   {
      // use the default location registry file location.
      StringBuilder builder = new StringBuilder();
      builder.append(System.getProperty("user.home"));
      builder.append(System.getProperty("file.separator") + "picketlink-store");
      builder.append(System.getProperty("file.separator") + "sts");

      // check if the $HOME/picketlink-store/sts directory exists.
      directory = new File(builder.toString());
      if (!directory.exists())
         directory.mkdirs();
   }

   /**
    * Create a file with the provided name
    * @param fileName
    * @return {@code File} handle
    */
   protected File create(String fileName)
   {
      if (fileName == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "file name");

      // check if the specified file exists. If not, create it.
      File createdFile = new File(fileName);
      if (!createdFile.exists())
      {
         try
         {
            createdFile.createNewFile();
         }
         catch (IOException ioe)
         {
            if (logger.isDebugEnabled())
               logger.debug("Error creating file: " + ioe.getMessage());
            ioe.printStackTrace();
         }
      }
      return createdFile;
   }
}