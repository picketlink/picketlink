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
package org.picketlink.identity.federation.core.saml.v2.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2Handler;
import org.picketlink.identity.federation.core.saml.v2.interfaces.SAML2HandlerChain;

/**
 * Default implementation of the SAML2 handler chain
 * @author Anil.Saldhana@redhat.com
 * @since Oct 1, 2009
 */
public class DefaultSAML2HandlerChain implements SAML2HandlerChain
{
   private Set<SAML2Handler> handlers = new LinkedHashSet<SAML2Handler>();
   
   /**
    * @see SAML2HandlerChain#add(SAML2Handler)
    */
   public boolean add(SAML2Handler handler)
   {
      return handlers.add(handler);
   }

   /**
    * @see SAML2HandlerChain#add(SAML2Handler)
    */
   public boolean addAll(Collection<SAML2Handler> handlers)
   {
      return this.handlers.addAll(handlers);
   }
   
   /**
    * @see SAML2HandlerChain#handlers()
    */
   public Set<SAML2Handler> handlers()
   {
      return Collections.unmodifiableSet(handlers);
   }

   /**
    * @see SAML2HandlerChain#remove(SAML2Handler)
    */
   public boolean remove(SAML2Handler handler)
   {
      return handlers.remove(handler);
   }
   
   /**
    * @see SAML2HandlerChain#size()
    */
   public int size()
   {
      return handlers.size();
   }

   /**
    * @see SAML2HandlerChain#removeAll(Collection)
    */
   public boolean removeAll(Collection<SAML2Handler> handlers)
   {
      return handlers.removeAll(handlers);
   } 
}