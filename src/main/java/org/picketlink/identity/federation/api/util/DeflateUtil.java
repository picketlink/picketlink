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
package org.picketlink.identity.federation.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Encoder of saml messages based on DEFLATE compression
 * @author Anil.Saldhana@redhat.com
 * @since Dec 11, 2008
 */
public class DeflateUtil
{
   /**
    * Apply DEFLATE encoding
    * @param message
    * @return
    * @throws IOException
    */
   public static byte[] encode(byte[] message) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Deflater deflater = new Deflater(Deflater.DEFLATED, true);
      DeflaterOutputStream deflaterStream = new DeflaterOutputStream(baos, deflater);
      deflaterStream.write(message);
      deflaterStream.finish();
      
      return baos.toByteArray(); 
   }
   
   /**
    * Apply DEFLATE encoding
    * @param message
    * @return
    * @throws IOException
    */
   public static byte[] encode(String message) throws IOException
   {
      return encode(message.getBytes()); 
   } 
   
   /**
    * DEFLATE decoding
    * @param msgToDecode the message that needs decoding
    * @return
    */
   public static InputStream decode(byte[] msgToDecode) 
   {
      ByteArrayInputStream bais = new ByteArrayInputStream(msgToDecode);
      return new InflaterInputStream(bais, new Inflater(true)); 
   }
}