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
package org.picketlink.trust.jbossws.handler;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.xml.ws.handler.MessageContext;

import org.jboss.security.AuthenticationManager;
import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.picketlink.identity.federation.core.ErrorCodes;

/**
 * Perform Authentication for POJO Web Services
 * 
 * Based on the Authorize Operation on the JBossWS Native stack
 * 
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @author Anil.Saldhana@redhat.com
 * @since Apr 11, 2011
 */
public class WSAuthenticationHandler extends AbstractPicketLinkTrustHandler
{
   @Override
   protected boolean handleInbound(MessageContext msgContext)
   { 
      if(trace)
      {
         log.trace("Handling Inbound Message");
         trace(msgContext);
      }
      AuthenticationManager authenticationManager = getAuthenticationManager();
      SecurityAdaptor securityAdaptor = secAdapterfactory.newSecurityAdapter();
      Principal principal = securityAdaptor.getPrincipal();
      Object credential = securityAdaptor.getCredential();

      Subject subject = new Subject();

      if (authenticationManager.isValid(principal, credential, subject) == false)
      {
         String msg = ErrorCodes.PROCESSING_EXCEPTION + "Authentication failed, principal=" + principal;
         log.error(msg);
         SecurityException e = new SecurityException(msg);
         throw new RuntimeException(e);
      }
      if(trace)
      {
         log.trace("Successfully Authenticated:Principal="+principal + "::subject="+subject);
      }
      securityAdaptor.pushSubjectContext(subject, principal, credential);

      return true;
   }
}