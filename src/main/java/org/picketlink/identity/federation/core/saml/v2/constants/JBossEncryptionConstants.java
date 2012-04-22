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
package org.picketlink.identity.federation.core.saml.v2.constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Encryption Algorithm and XMLEnC URI
 * @author Anil.Saldhana@redhat.com
 * @since Feb 4, 2009
 */
public class JBossEncryptionConstants
{
   private static Map<String,String> algoToXmlEncURL = new HashMap<String,String>();
   
   static
   {
      algoToXmlEncURL.put("DESede", "http://www.w3.org/2001/04/xmlenc#kw-tripledes");
      algoToXmlEncURL.put("TRIPLEDES", "http://www.w3.org/2001/04/xmlenc#kw-tripledes");
      
      algoToXmlEncURL.put("AES_128", "http://www.w3.org/2001/04/xmlenc#aes128-cbc");
      algoToXmlEncURL.put("AES_192", "http://www.w3.org/2001/04/xmlenc#aes192-cbc");
      algoToXmlEncURL.put("AES_256", "http://www.w3.org/2001/04/xmlenc#aes256-cbc");
   } 
   
   public static String getURL(String algo, int keySize)
   {
      if(keySize == 0)
         return algoToXmlEncURL.get(algo);
      return algoToXmlEncURL.get(algo+ "_" +keySize);
   }
}
