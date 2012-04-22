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
package org.picketlink.identity.federation.web.servlets;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.login.LoginException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.web.constants.GeneralConstants;
import org.picketlink.identity.federation.web.handlers.DefaultLoginHandler;
import org.picketlink.identity.federation.web.interfaces.ILoginHandler;

/**
 * Handles login at the IDP
 * @author Anil.Saldhana@redhat.com
 * @since Aug 21, 2009
 */
public class IDPLoginServlet extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   private transient ServletContext context;

   private transient ILoginHandler loginHandler = null;

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      HttpSession session = request.getSession();

      //Check if we are already authenticated
      Principal principal = (Principal) session.getAttribute(GeneralConstants.PRINCIPAL_ID);
      if (principal != null)
      {
         this.saveRequest(request, session);
         redirectToIDP(request, response);
         return;
      }

      final String username = request.getParameter(GeneralConstants.USERNAME_FIELD);
      String passwd = request.getParameter(GeneralConstants.PASS_FIELD);

      if (username == null || passwd == null)
      {
         String samlMessage = request.getParameter(GeneralConstants.SAML_REQUEST_KEY);

         if (samlMessage == null || "".equals(samlMessage))
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

         log("No username or password found. Redirecting to login page");
         this.saveRequest(request, session);

         if (response.isCommitted())
            throw new RuntimeException(ErrorCodes.PROCESSING_EXCEPTION
                  + "Response is committed. Cannot forward to login page.");

         this.redirectToLoginPage(request, response);
      }
      else
      {
         //we have the username and password
         try
         {
            boolean isValid = loginHandler.authenticate(username, passwd);
            if (!isValid)
            {
               response.sendError(HttpServletResponse.SC_FORBIDDEN);
               return;
            }

            session.setAttribute(GeneralConstants.PRINCIPAL_ID, new Principal()
            {
               public String getName()
               {
                  return username;
               }
            });

            this.redirectToIDP(request, response);
            return;
         }
         catch (LoginException e)
         {
            log("Exception logging in :", e);
            //TODO: Send back invalid user SAML
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
         }
      }
   }

   @Override
   public void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      this.context = config.getServletContext();

      String loginClass = config.getInitParameter("loginClass");
      if (loginClass == null || loginClass.length() == 0)
         loginClass = DefaultLoginHandler.class.getName();
      //Lets set up the login class
      try
      {
         Class<?> clazz = SecurityActions.loadClass(getClass(), loginClass);
         loginHandler = (ILoginHandler) clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new ServletException(e);
      }
   }

   public void testPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      this.doPost(request, response);
   }

   private void saveRequest(HttpServletRequest request, HttpSession session)
   {
      //Save the SAMLRequest and relayState
      session.setAttribute(GeneralConstants.SAML_REQUEST_KEY, request.getParameter(GeneralConstants.SAML_REQUEST_KEY));
      session
            .setAttribute(GeneralConstants.SAML_RESPONSE_KEY, request.getParameter(GeneralConstants.SAML_RESPONSE_KEY));

      String relayState = request.getParameter(GeneralConstants.RELAY_STATE);
      if (relayState != null && !"".equals(relayState))
         session.setAttribute(GeneralConstants.RELAY_STATE, relayState);
      session.setAttribute("Referer", request.getHeader("Referer"));
   }

   private void redirectToIDP(HttpServletRequest request, HttpServletResponse response) throws ServletException,
         IOException
   {
      RequestDispatcher dispatch = context.getRequestDispatcher("/IDPServlet");
      if (dispatch == null)
         log("Cannot dispatch to the IDP Servlet");
      else
         dispatch.forward(request, response);
      return;
   }

   private void redirectToLoginPage(HttpServletRequest request, HttpServletResponse response) throws ServletException,
         IOException
   {
      RequestDispatcher dispatch = context.getRequestDispatcher("/jsp/login.jsp");
      if (dispatch == null)
         log("Cannot find the login page");
      else
         dispatch.forward(request, response);
      return;
   }
}