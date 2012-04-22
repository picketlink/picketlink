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
package org.picketlink.identity.federation.web.util;

import static org.picketlink.identity.federation.core.util.StringUtil.isNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.picketlink.identity.federation.api.util.DeflateUtil;
import org.picketlink.identity.federation.core.util.Base64;

/**
 * Utility class for SAML HTTP/Redirect binding
 * @author Anil.Saldhana@redhat.com
 * @since Jan 14, 2009
 */
public class RedirectBindingUtil
{
   /**
    * URL encode the string
    * @param str
    * @return
    * @throws IOException
    */
   public static String urlEncode(String str) throws IOException
   {
      return URLEncoder.encode(str, "UTF-8");
   }
   
   /**
    * URL decode the string
    * @param str
    * @return
    * @throws IOException
    */
   public static String urlDecode(String str) throws IOException
   {
      return URLDecoder.decode(str, "UTF-8");
   }
   
   /**
    * On the byte array, apply base64 encoding following by URL encoding
    * @param stringToEncode
    * @return
    * @throws IOException  
    */
   public static String base64URLEncode(byte[] stringToEncode) throws IOException 
   {
      String base64Request = Base64.encodeBytes(stringToEncode, Base64.DONT_BREAK_LINES); 
      return urlEncode(base64Request);
   }
   
   /**
    * On the byte array, apply URL decoding followed by base64 decoding
    * @param encodedString
    * @return
    * @throws IOException  
    */
   public static byte[] urlBase64Decode(String encodedString) throws IOException 
   {
      String decodedString = urlDecode(encodedString);
      return Base64.decode(decodedString);
   } 
   
   /**
    * Apply deflate compression followed by base64 encoding and URL encoding
    * @param stringToEncode
    * @return
    * @throws IOException  
    */
   public static String deflateBase64URLEncode(String stringToEncode) throws IOException 
   {
      return deflateBase64URLEncode(stringToEncode.getBytes("UTF-8")); 
   }
   
   /**
    * Apply deflate compression followed by base64 encoding and URL encoding
    * @param stringToEncode
    * @return
    * @throws IOException
    */
   public static String deflateBase64URLEncode(byte[] stringToEncode) throws IOException
   {
      byte[] deflatedMsg = DeflateUtil.encode(stringToEncode); 
      return base64URLEncode(deflatedMsg); 
   }
   
   /**
    * Apply deflate compression followed by base64 encoding 
    * @param stringToEncode
    * @return
    * @throws IOException
    */
   public static String deflateBase64Encode(byte[] stringToEncode) throws IOException
   {
      byte[] deflatedMsg = DeflateUtil.encode(stringToEncode);
      return Base64.encodeBytes(deflatedMsg);
   }
   
   /**
    * Apply URL decoding, followed by base64 decoding followed by deflate decompression
    * @param encodedString
    * @return
    * @throws IOException  
    */
   public static InputStream urlBase64DeflateDecode(String encodedString) throws IOException 
   {
      byte[] deflatedString  = urlBase64Decode(encodedString);
      return DeflateUtil.decode(deflatedString);
   }
   
   /**
    * Base64 decode followed by Deflate decoding
    * @param encodedString
    * @return 
    */
   public static InputStream base64DeflateDecode(String encodedString) 
   {
      byte[] base64decodedMsg = Base64.decode(encodedString);
      return DeflateUtil.decode(base64decodedMsg);
   }
   
   /**
    * Get the Query String for the destination url
    * @param urlEncodedRequest
    * @param urlEncodedRelayState
    * @param sendRequest either going to be saml request or response
    * @return
    */
   public static String getDestinationQueryString(String urlEncodedRequest, String urlEncodedRelayState,
         boolean sendRequest)
   {
      StringBuilder sb = new StringBuilder();
      if(sendRequest)
        sb.append("SAMLRequest=").append(urlEncodedRequest);
      else
         sb.append("SAMLResponse=").append(urlEncodedRequest);
      if(isNotNull(urlEncodedRelayState))
         sb.append("&RelayState=").append(urlEncodedRelayState);
      return sb.toString();
   }
    
   /**
    * Get the destination url
    * @param holder
    * @return
    * @throws UnsupportedEncodingException
    * @throws IOException
    */
   public static String getDestinationURL(RedirectBindingUtilDestHolder holder) throws UnsupportedEncodingException, IOException
   { 
      String destination = holder.destination;
      StringBuilder destinationURL = new StringBuilder(destination);

      if(destination.contains("?"))
         destinationURL.append("&");
      else
         destinationURL.append("?");

      destinationURL.append( holder.destinationQueryString);  

      return destinationURL.toString();
   } 
   
   /**
    * A Destination holder that holds
    * the destination host url and the destination query
    * string 
    */
   public static class RedirectBindingUtilDestHolder
   {   
      private String destination;
      private String destinationQueryString;
      
      public RedirectBindingUtilDestHolder setDestinationQueryString(String dest)
      {
         destinationQueryString = dest;
         return this;
      }  
      
      public RedirectBindingUtilDestHolder setDestination(String dest)
      {
         destination = dest;
         return this;
      } 
   }
}