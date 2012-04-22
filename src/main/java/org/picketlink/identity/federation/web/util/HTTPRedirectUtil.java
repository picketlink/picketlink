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

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
 

/**
 * Utility Class for http/redirect
 * @author Anil.Saldhana@redhat.com
 * @since Dec 15, 2008
 */
public class HTTPRedirectUtil
{
   /**
    * Send the response to the redirected destination while
    * adding the character encoding of "UTF-8" as well as
    * adding headers for cache-control and Pragma
    * @param destination Destination URI where the response needs to redirect
    * @param response HttpServletResponse
    * @throws IOException
    */
   public static void sendRedirectForRequestor(String destination, HttpServletResponse response)
   throws IOException
   {
      common(destination, response); 
      response.setHeader("Cache-Control", "no-cache, no-store"); 
      sendRedirect(response,destination); 
   } 
   
   /**
    * @see #sendRedirectForRequestor(String, HttpServletResponse)
    */
   public static void sendRedirectForResponder(String destination, HttpServletResponse response)
   throws IOException
   {
      common(destination, response);
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate,private");
      sendRedirect(response,destination); 
   } 
   
   private static void common(String destination, HttpServletResponse response)
   {
      response.setCharacterEncoding("UTF-8"); 
      response.setHeader("Location", destination);
      response.setHeader("Pragma", "no-cache");  
   }
   
   private static void sendRedirect(HttpServletResponse response, String destination) throws IOException
   {
      response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
      response.sendRedirect(destination);  
   }
}