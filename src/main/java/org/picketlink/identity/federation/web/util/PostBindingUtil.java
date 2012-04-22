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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.saml.v2.holders.DestinationInfoHolder;
import org.picketlink.identity.federation.core.util.Base64;
import org.picketlink.identity.federation.web.constants.GeneralConstants;

/**
 * Utility for the HTTP/Post binding
 * @author Anil.Saldhana@redhat.com
 * @since May 22, 2009
 */
public class PostBindingUtil
{
   private static Logger log = Logger.getLogger(PostBindingUtil.class);

   private static boolean trace = log.isTraceEnabled();

   /**
    * Apply base64 encoding on the message 
    * @param stringToEncode
    * @return
    */
   public static String base64Encode(String stringToEncode) throws IOException
   {
      return Base64.encodeBytes(stringToEncode.getBytes("UTF-8"), Base64.DONT_BREAK_LINES);
   }

   /**
    * Apply base64 decoding on the message and return the byte array
    * @param encodedString
    * @return
    */
   public static byte[] base64Decode(String encodedString)
   {
      if (encodedString == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "encodedString");

      return Base64.decode(encodedString);
   }

   /**
    * Apply base64 decoding on the message and return the stream
    * @param encodedString
    * @return
    */
   public static InputStream base64DecodeAsStream(String encodedString)
   {
      if (encodedString == null)
         throw new IllegalArgumentException(ErrorCodes.NULL_ARGUMENT + "encodedString");

      return new ByteArrayInputStream(base64Decode(encodedString));
   }

   /**
    * Send the response to the redirected destination while
    * adding the character encoding of "UTF-8" as well as
    * adding headers for cache-control and Pragma
    * @param destination Destination URI where the response needs to redirect
    * @param response HttpServletResponse
    * @throws IOException
    */
   public static void sendPost(DestinationInfoHolder holder, HttpServletResponse response, boolean request)
         throws IOException
   {
      String key = request ? GeneralConstants.SAML_REQUEST_KEY : GeneralConstants.SAML_RESPONSE_KEY;

      String relayState = holder.getRelayState();
      String destination = holder.getDestination();
      String samlMessage = holder.getSamlMessage();

      if (destination == null)
         throw new IllegalStateException(ErrorCodes.NULL_VALUE + "Destination is null");

      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      common(holder.getDestination(), response);
      StringBuilder builder = new StringBuilder();

      builder.append("<HTML>");
      builder.append("<HEAD>");
      if (request)
         builder.append("<TITLE>HTTP Post Binding (Request)</TITLE>");
      else
         builder.append("<TITLE>HTTP Post Binding Response (Response)</TITLE>");

      builder.append("</HEAD>");
      builder.append("<BODY Onload=\"document.forms[0].submit()\">");

      builder.append("<FORM METHOD=\"POST\" ACTION=\"" + destination + "\">");
      builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"" + key + "\"" + " VALUE=\"" + samlMessage + "\"/>");
      if (isNotNull(relayState))
      {
         builder.append("<INPUT TYPE=\"HIDDEN\" NAME=\"RelayState\" " + "VALUE=\"" + relayState + "\"/>");
      }
      builder.append("</FORM></BODY></HTML>");

      String str = builder.toString();
      if (trace)
         log.trace(str);
      out.println(str);
      out.close();
   }

   private static void common(String destination, HttpServletResponse response)
   {
      response.setCharacterEncoding("UTF-8");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Cache-Control", "no-cache, no-store");
   }
}