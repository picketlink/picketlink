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
package org.picketlink.identity.federation.core.saml.v2.interfaces;

import java.util.Collection;
import java.util.Set;

/**
 * A SAML2 chain of handlers
 * @author Anil.Saldhana@redhat.com
 * @since Oct 1, 2009
 */
public interface SAML2HandlerChain
{
   /**
    * Number of handlers
    * @return
    */
   int size();
   
   /**
    * Get a read-only set of handlers
    * @return
    */
   Set<SAML2Handler> handlers();
   
   /**
    * Add an handler
    * @param handler
    * @return whether add was successful
    */
   boolean add(SAML2Handler handler);
   
   /**
    * Add a collection of handlers
    * @param handlers
    * @return
    */
   boolean addAll(Collection<SAML2Handler> handlers);
   
   /**
    * Remove an handler
    * @param handler
    * @return whether remove was successful
    */
   boolean remove(SAML2Handler handler);
   
   /**
    * Remove a collection of handlers
    * @param handlers
    * @return
    */
   boolean removeAll(Collection<SAML2Handler> handlers);
}