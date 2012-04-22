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
package org.picketlink.identity.federation.web.process;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.picketlink.identity.federation.core.exceptions.ProcessingException;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerRequest;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerResponse;
import org.picketlink.identity.federation.saml.v2.SAML2Object;
import org.picketlink.identity.federation.saml.v2.protocol.RequestAbstractType;
import org.picketlink.identity.federation.web.core.HTTPContext;

/**
 * Processor for the SAML2 Handler Chain
 * @author Anil.Saldhana@redhat.com
 * @since Oct 27, 2009
 */
public class SAMLHandlerChainProcessor
{
   private final Set<SAML2Handler> handlers = new LinkedHashSet<SAML2Handler>();

   public SAMLHandlerChainProcessor(Set<SAML2Handler> handlers)
   {
      this.handlers.addAll(handlers);
   }

   public void callHandlerChain(SAML2Object samlObject, SAML2HandlerRequest saml2HandlerRequest,
         SAML2HandlerResponse saml2HandlerResponse, HTTPContext httpContext, Lock chainLock)
         throws ProcessingException, IOException
   {
      try
      {
         chainLock.lock();
         //Deal with handler chains
         for (SAML2Handler handler : handlers)
         {
            if (saml2HandlerResponse.isInError())
            {
               httpContext.getResponse().sendError(saml2HandlerResponse.getErrorCode());
               break;
            }
            if (samlObject instanceof RequestAbstractType)
            {
               handler.handleRequestType(saml2HandlerRequest, saml2HandlerResponse);
            }
            else
            {
               handler.handleStatusResponseType(saml2HandlerRequest, saml2HandlerResponse);
            }
         }
      }
      finally
      {
         chainLock.unlock();
      }
   }
}