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
package org.picketlink.identity.federation.web.core;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.web.constants.GeneralConstants;

/**
 * Represents an Identity Server
 * @author Anil.Saldhana@redhat.com
 * @since Sep 17, 2009
 */
public class IdentityServer implements HttpSessionListener
{
   private static Logger log = Logger.getLogger(IdentityServer.class);

   private final boolean trace = log.isTraceEnabled();

   //Configurable count for the active session count
   private static int count = AccessController.doPrivileged(new PrivilegedAction<Integer>()
   {
      public Integer run()
      {
         String val = System.getProperty("identity.server.log.count", "100");
         return Integer.parseInt(val);
      }
   });

   private static int activeSessionCount = 0;

   private IdentityParticipantStack stack = new STACK();

   public class STACK implements IdentityParticipantStack
   {
      private final ConcurrentHashMap<String, Stack<String>> sessionParticipantsMap = new ConcurrentHashMap<String, Stack<String>>();

      private final ConcurrentHashMap<String, Set<String>> inTransitMap = new ConcurrentHashMap<String, Set<String>>();

      private final ConcurrentHashMap<String, Boolean> postBindingMap = new ConcurrentHashMap<String, Boolean>();

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#peek(java.lang.String)
       */
      public String peek(String sessionID)
      {
         Stack<String> stack = sessionParticipantsMap.get(sessionID);
         if (stack != null)
            return stack.peek();
         return "";
      }

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#pop(java.lang.String)
       */
      public String pop(String sessionID)
      {
         String result = null;
         Stack<String> stack = sessionParticipantsMap.get(sessionID);
         if (stack != null && stack.isEmpty() == false)
         {
            result = stack.pop();
         }
         return result;
      }

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#register(java.lang.String, java.lang.String, boolean)
       */
      public void register(String sessionID, String participant, boolean postBinding)
      {
         Stack<String> stack = sessionParticipantsMap.get(sessionID);
         if (stack == null)
         {
            stack = new Stack<String>();
            sessionParticipantsMap.put(sessionID, stack);
         }
         if (stack.contains(participant) == false)
         {
            stack.push(participant);
            postBindingMap.put(participant, Boolean.valueOf(postBinding));
         }
      }

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#getParticipants(java.lang.String)
       */
      public int getParticipants(String sessionID)
      {
         Stack<String> stack = sessionParticipantsMap.get(sessionID);
         if (stack != null)
            return stack.size();

         return 0;
      }

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#registerTransitParticipant(java.lang.String, java.lang.String)
       */
      public boolean registerTransitParticipant(String sessionID, String participant)
      {
         Set<String> transitSet = inTransitMap.get(sessionID);
         if (transitSet == null)
         {
            transitSet = new HashSet<String>();
            inTransitMap.put(sessionID, transitSet);
         }
         return transitSet.add(participant);
      }

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#deRegisterTransitParticipant(java.lang.String, java.lang.String)
       */
      public boolean deRegisterTransitParticipant(String sessionID, String participant)
      {
         Set<String> transitSet = inTransitMap.get(sessionID);
         if (transitSet != null)
         {
            postBindingMap.remove(participant);
            return transitSet.remove(participant);
         }
         return false;
      }

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#getNumOfParticipantsInTransit(java.lang.String)
       */
      public int getNumOfParticipantsInTransit(String sessionID)
      {
         Set<String> transitSet = inTransitMap.get(sessionID);
         if (transitSet != null)
            return transitSet.size();
         return 0;
      }

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#getBinding(java.lang.String)
       */
      public Boolean getBinding(String participant)
      {
         return postBindingMap.get(participant);
      }

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#totalSessions()
       */
      public int totalSessions()
      {
         return sessionParticipantsMap.keySet().size();
      }

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#createSession(java.lang.String)
       */
      public void createSession(String id)
      {
         sessionParticipantsMap.put(id, new Stack<String>());
         inTransitMap.put(id, new HashSet<String>());
      }

      /**
       * @see org.picketlink.identity.federation.web.core.IdentityParticipantStack#removeSession(java.lang.String)
       */
      public void removeSession(String id)
      {
         sessionParticipantsMap.remove(id);
         inTransitMap.remove(id);
      }
   }

   /**
    * Return the active session count
    * @return
    */
   public int getActiveSessionCount()
   {
      return activeSessionCount;
   }

   /**
    * Return a reference to the internal stack 
    * @return
    */
   public IdentityParticipantStack stack()
   {
      return stack;
   }

   /**
    * Set a custom instance of the {@link IdentityParticipantStack}
    * @param theStack
    */
   public void setStack(IdentityParticipantStack theStack)
   {
      this.stack = theStack;
   }

   /**
    * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
    */
   public void sessionCreated(HttpSessionEvent sessionEvent)
   {
      activeSessionCount++;

      if (activeSessionCount % count == 0)
         log.info("Active Session Count=" + activeSessionCount);

      HttpSession session = sessionEvent.getSession();

      if (trace)
         log.trace("Session Created with id=" + session.getId() + "::active session count=" + activeSessionCount);

      //Ensure that the IdentityServer instance is set on the servlet context
      ServletContext servletContext = session.getServletContext();

      IdentityServer idserver = (IdentityServer) servletContext.getAttribute(GeneralConstants.IDENTITY_SERVER);

      if (idserver == null)
      {
         idserver = this;
         servletContext.setAttribute(GeneralConstants.IDENTITY_SERVER, this);
      }

      if (idserver != this)
         throw new IllegalStateException(ErrorCodes.NOT_EQUAL + "Identity Server mismatch");

      String id = sessionEvent.getSession().getId();
      stack.createSession(id);
   }

   /**
    * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
    */
   public void sessionDestroyed(HttpSessionEvent sessionEvent)
   {
      --activeSessionCount;

      String id = sessionEvent.getSession().getId();
      if (trace)
         log.trace("Session Destroyed with id=" + id + "::active session count=" + activeSessionCount);
      stack.removeSession(id);
   }
}