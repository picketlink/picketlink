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
package org.picketlink.identity.federation.web.listeners;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;
import org.picketlink.identity.federation.core.ErrorCodes;
import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.common.SAMLProtocolContext;
import org.picketlink.identity.federation.core.sts.PicketLinkCoreSTS;
import org.picketlink.identity.federation.saml.v2.assertion.AssertionType;
import org.picketlink.identity.federation.web.constants.GeneralConstants;

/**
 * An instance of {@link HttpSessionListener} at the IDP
 * that performs actions when an {@link HttpSession} is created or destroyed.
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Feb 3, 2012
 */
public class IDPHttpSessionListener implements HttpSessionListener
{
   private static Logger log = Logger.getLogger(IDPHttpSessionListener.class);

   private final boolean trace = log.isTraceEnabled();

   public void sessionCreated(HttpSessionEvent se)
   {
   }

   public void sessionDestroyed(HttpSessionEvent se)
   {
      HttpSession httpSession = se.getSession();
      if (httpSession == null)
         throw new RuntimeException(ErrorCodes.NULL_ARGUMENT + ":session");
      AssertionType assertion = (AssertionType) httpSession.getAttribute(GeneralConstants.ASSERTION);

      //If the user had logged out, then the assertion would not be available in the session.
      //The case when the user closes the browser and does not logout, the session will time out on the 
      //server. So we know that the token has not been canceled by the STS.
      if (assertion != null)
      {
         if (trace)
         {
            log.trace("User has closed the browser. So we proceed to cancel the STS issued token.");
         }
         PicketLinkCoreSTS sts = PicketLinkCoreSTS.instance();
         SAMLProtocolContext samlProtocolContext = new SAMLProtocolContext();
         samlProtocolContext.setIssuedAssertion(assertion);
         try
         {
            sts.cancelToken(samlProtocolContext);
         }
         catch (ProcessingException e)
         {
            log.error(ErrorCodes.PROCESSING_EXCEPTION, e);
         }
         httpSession.removeAttribute(GeneralConstants.ASSERTION);
      }
   }
}